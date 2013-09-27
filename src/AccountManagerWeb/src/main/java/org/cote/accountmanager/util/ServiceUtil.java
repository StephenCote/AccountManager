package org.cote.accountmanager.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class ServiceUtil {
	public static final Logger logger = Logger.getLogger(ServiceUtil.class.getName());
	public static OrganizationType getOrganizationFromRequest(HttpServletRequest request){
		String orgIdStr = getCookieValue(request, "OrganizationId");
		OrganizationType org = null;
		if(orgIdStr != null){
			long orgId = 0;
			try{
				orgId = Long.parseLong(orgIdStr);
				if(orgId > 0) org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			}
			catch(NumberFormatException nfe){
				
				logger.error(nfe.getMessage());
				nfe.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			
		}
		if(org == null){
			System.out.println("Organization is not specified.  Using Public.");
			org = Factories.getPublicOrganization();
		}
		return org;
	}
	public static String getCookieValue(HttpServletRequest request, String name){
		String out_val = null;
		Cookie[] cookies = request.getCookies();
		for(int i = 0; cookies != null && i < cookies.length;i++){
			if(cookies[i] != null && cookies[i].getName().equals(name)){
				out_val = cookies[i].getValue();
				break;
			}
		}
		return out_val;
	}
	public static void addCookie(HttpServletResponse response, String name, String value){
		Cookie c = new Cookie(name, value);
		c.setPath("/");
		c.setMaxAge(-1);
		response.addCookie(c);
	}
	public static void clearCookie(HttpServletResponse response, String name){
		Cookie c = new Cookie(name, "");
		c.setPath("/");
		c.setMaxAge(0);
		response.addCookie(c);
	}
	public static UserType getUserFromSession(AuditType audit, HttpServletRequest request){
		UserType user = getUserFromSession(request);
		if(user==null || SessionSecurity.isAuthenticated(user.getSession()) == false){
			AuditService.denyResult(audit,  "Invalid user. " + (user == null ? "Null user" : "Status is " + user.getSession().getSessionStatus()));
			System.out.println("User is null or not authenticated");
			return null;
		}
		AuditService.sourceAudit(audit,AuditEnumType.USER,user.getName() + " (#" + user.getId() + ") in Org " + user.getOrganization().getName() + " (#" + user.getOrganization().getId() + ")");
		return user;
	}
	public static UserType getUserFromSession(HttpServletRequest request){
		String sessionId = request.getSession(true).getId();
		//System.out.println("Session Id=" + sessionId);
		return getUserFromSession(request, sessionId);
	}
	public static UserType getUserFromSession(HttpServletRequest request,String sessionId){

		UserType user = null;
		
		try {
			user = SessionSecurity.getUserBySession(sessionId, ServiceUtil.getOrganizationFromRequest(request));

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return user;
	}
}
