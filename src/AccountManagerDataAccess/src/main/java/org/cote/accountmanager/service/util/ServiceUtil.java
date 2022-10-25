/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.service.util;

import java.security.Principal;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

public class ServiceUtil {
	public static final Logger logger = LogManager.getLogger(ServiceUtil.class);
	/// Bit indicating that the session id should be generated independent of the application server
	/// This in no way mitigates the need for any secondary credential or token
	///
	private static boolean useAccountManagerSession = true;
	private static String am5CookieName = "am5";
	private static int am5CookieExpiry = 7200;
	private static boolean useSecureCookie = false;
	public static void setUseAccountManagerSession(boolean b){
		useAccountManagerSession = b;
	}
	public static void setCookieExpiration(int ms){
		am5CookieExpiry = ms;
	}
	public static void setUseSecureCookie(boolean b){
		useSecureCookie = b;
	}
	
	private static Pattern jerseyCookie = Pattern.compile(",\\$Version");
	
	public static String getSessionId(HttpServletRequest request){
		return getSessionId(request,null,false);
	}
	public static String getSessionId(HttpServletRequest request, HttpServletResponse response, boolean create){
		String sessionId = null;
		if(useAccountManagerSession){
			sessionId = getCookieValue(request,am5CookieName);
			if((sessionId == null || sessionId.length() == 0) && response != null && create){
				
				sessionId = UUID.randomUUID().toString();
				addCookie(response,am5CookieName,sessionId);
				logger.info("Assigning AM5 Session Id: " + sessionId);
			}
		}
		else{
			HttpSession sess = request.getSession(create);
			sessionId = (sess != null ? sess.getId() : null);
		}
		if(sessionId == null){
			logger.debug("Null Session Id. AM5 Session Mode Is " + (useAccountManagerSession ? "On":"Off") + ". This is expected for service calls");

		}
		return sessionId;
	}
	public static OrganizationType getOrganizationFromRequest(HttpServletRequest request){
		String orgPathStr = getCookieValue(request, "OrganizationPath");
		String orgIdStr = null;
		if(orgPathStr == null) orgIdStr = getCookieValue(request, "OrganizationId");
		OrganizationType org = null;
		if(orgIdStr != null || orgPathStr != null){
			long orgId = 0L;
			try{
				if(orgPathStr != null){
					org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(orgPathStr);
				}
				else{
					orgId = Long.parseLong(orgIdStr);
					if(orgId > 0) org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(orgId);
				}
			}
			catch(NumberFormatException | FactoryException | ArgumentException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
			
		}
		if(org == null){
			logger.warn("Organization is not specified.  Using Public.");
			Cookie[] cookies = request.getCookies();

			logger.warn("Cookies: " + (cookies == null ? "0" : cookies.length));
			for(int i = 0; cookies != null && i < cookies.length;i++){
				logger.warn("Cookie '" + cookies[i].getName() + "' = '" + cookies[i].getValue() + "'");
			}
			org = Factories.getPublicOrganization();
		}
		return org;
	}
	public static String getCookieValue(HttpServletRequest request, String name){
		String out_val = null;
		Cookie[] cookies = request.getCookies();
		for(int i = 0; cookies != null && i < cookies.length;i++){
			if(cookies[i] != null && cookies[i].getName().equals(name)){
				out_val = jerseyCookie.matcher(cookies[i].getValue()).replaceAll("");
				break;
			}
		}
		return out_val;
	}
	public static void addCookie(HttpServletResponse response, String name, String value){
		Cookie c = new Cookie(name, value);
		c.setPath("/");
		c.setSecure(useSecureCookie);
		c.setVersion(1);
		c.setMaxAge(am5CookieExpiry);
		response.addCookie(c);
		logger.info("Creating cookie: " + name + "=" + value);
	}
	public static void clearCookie(HttpServletResponse response, String name){
		Cookie c = new Cookie(name, "v");
		c.setPath("/");
		c.setSecure(useSecureCookie);
		c.setMaxAge(0);
		response.addCookie(c);
		logger.info("Clearing cookie: " + name);
	}
	public static UserType getUserFromSession(AuditType audit, HttpServletRequest request){

		UserType user = getUserFromSession(request);
		if(user == null || SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit,  "Invalid user. " + (user == null ? "Null user" : "Status is " + (user.getSession() != null ? user.getSession().getSessionStatus() : " Unknown")));
			return null;
		}

		AuditService.sourceAudit(audit,AuditEnumType.USER,user.getUrn());
		return user;
	}
	public static UserType getUserFromSession(HttpServletRequest request){
		String sessionId = getSessionId(request);
		return getUserFromSession(request, sessionId);
	}
	public synchronized static UserType getUserFromSession(HttpServletRequest request,String sessionId){

		UserType user = null;
		OrganizationType org = null;
		Principal principal = request.getUserPrincipal();
		if(principal != null){
			//user = SessionSecurity.authenticatePrincipal(principal,  sessionId);
			user = SessionSecurity.getPrincipalUser(request);
		}
		else{
			try {
				org = ServiceUtil.getOrganizationFromRequest(request);
				if(org != null){
					user = SessionSecurity.getUserBySession(sessionId, org.getId());
				}
				else{
					logger.error("Organization is null");
				}
	
			} catch (FactoryException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				logger.error(e.getMessage());
			} catch (ArgumentException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return user;
	}
}
