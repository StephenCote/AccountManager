/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rest.services;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.AuthenticationResponseEnumType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.services.DataServiceImpl;
import org.cote.accountmanager.services.UserServiceImpl;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.beans.MessageBean;
import org.cote.beans.SessionBean;
//import org.cote.beans.UserBean;
import org.cote.util.BeanUtil;
import org.cote.util.RegistrationUtil;

@Path("/user")
public class UserService{

	public static final Logger logger = Logger.getLogger(UserService.class.getName());
	private static SchemaBean schemaBean = null;	
	public UserService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();

	}
	@GET @Path("/clearCache") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean flushCache(@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "clearCache",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, AuditEnumType.INFO, "Request clear factory cache");
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null){
			AuditService.denyResult(audit, "Deny for anonymous user");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.USER, "User Factory");
		Factories.getUserFactory().clearCache();
		AuditService.permitResult(audit,user.getName() + " flushed User Factory cache");
		return true;
	}
	@GET @Path("/getPublicUser") @Produces(MediaType.APPLICATION_JSON)
	public UserType getPublicUser(@Context HttpServletRequest request){
		OrganizationType org = ServiceUtil.getOrganizationFromRequest(request);
		if(org != null) return Factories.getDocumentControl(org.getId());
		return null;
	}
	@GET @Path("/getSelf") @Produces(MediaType.APPLICATION_JSON)
	public UserType getSelf(@Context HttpServletRequest request){
		//UserType bean = null;
		String sessionId = ServiceUtil.getSessionId(request);
		UserType user = null;
		try{
			OrganizationType org = ServiceUtil.getOrganizationFromRequest(request);
			if(org != null) user = SessionSecurity.getUserBySession(sessionId,org.getId());
			if(user != null){
				/*
				if(user.getContactInformation() == null){
					user.setContactInformation(new ContactInformationType());
					user.getContactInformation().setEmail("none");
				}
				*/
				Factories.getAttributeFactory().populateAttributes(user);
				//bean = BeanUtil.getUserType(user);
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return user;
	}
	@POST @Path("/authenticate") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public AuthenticationResponseType authenticate(AuthenticationRequestType authRequest, @Context HttpServletRequest request, @Context HttpServletResponse response){
		
		String sessionId = ServiceUtil.getSessionId(request,response,true);
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHENTICATE, "authenticate", AuditEnumType.SESSION, sessionId);
		AuthenticationResponseType authResponse = new AuthenticationResponseType();
		authResponse.setSessionId(sessionId);
		authResponse.setResponse(AuthenticationResponseEnumType.NOT_AUTHENTICATED);
		AuditService.targetAudit(audit, AuditEnumType.USER, authRequest.getSubject());
		if(authRequest == null || authRequest.getSubject() == null || authRequest.getCredential() == null || authRequest.getCredential().length == 0){
			logger.error("Invalid authentication request");
			AuditService.denyResult(audit, "Invalid authentication request");
			authResponse.setMessage("Invalid arguments");
			return authResponse;
		}
		if(authRequest.getOrganizationPath() == null) authRequest.setOrganizationPath("/Public");
		AuditService.targetAudit(audit, AuditEnumType.USER, authRequest.getSubject() + " in " + authRequest.getOrganizationPath());
		boolean requireSSL = Boolean.parseBoolean(request.getServletContext().getInitParameter("ssl.login.required"));
		if(requireSSL && request.isSecure() == false){
			AuditService.denyResult(audit, "Authentication requires a secure connection");
			authResponse.setMessage("Secure connection required");
			return authResponse;

		}
		UserType user = null;

		try{
			OrganizationType org = Factories.getOrganizationFactory().findOrganization(authRequest.getOrganizationPath());
			if(org == null){
				AuditService.denyResult(audit, "Invalid organization");
				authResponse.setMessage("Authentication failed.");
				return authResponse;
			}
			user = SessionSecurity.login(sessionId,authRequest.getSubject(), authRequest.getCredentialType(), new String(authRequest.getCredential(),"UTF-8"), org.getId());
			if(user != null){
				//Factories.getUserFactory().populate(user);
				AuditService.targetAudit(audit, AuditEnumType.USER, user.getUrn());
				Factories.getAttributeFactory().populateAttributes(user);
				authResponse.setResponse(AuthenticationResponseEnumType.AUTHENTICATED);
				authResponse.setUser(user);
				AuditService.permitResult(audit,"#" + user.getId());
				
			}
			else{
				AuditService.denyResult(audit, "User not found");
				authResponse.setMessage("Authentication failed.");
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			//fe.printStackTrace();
			user = null;
			AuditService.denyResult(audit, "Error: " + fe.getMessage());
			authResponse.setMessage("An error occured.");
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			//e.printStackTrace();
			user = null;
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			authResponse.setMessage("An error occured.");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.error(e.getMessage());
		}
		if(user != null){
			//ServiceUtil.addCookie(response,"OrganizationId",Long.toString(user.getOrganizationId()));
			ServiceUtil.addCookie(response,"OrganizationPath",user.getOrganizationPath());
		}
		
		return authResponse;
	}
	

	@GET @Path("/safeLogout/{id : [a-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public SessionBean safeLogout(@PathParam("id") String id,@Context HttpServletRequest request, @Context HttpServletResponse response){
		return doLogout(request, response);
	}

	@GET @Path("/logout/") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public SessionBean logout(@Context HttpServletRequest request, @Context HttpServletResponse response){
		return doLogout(request, response);
	}
	public SessionBean doLogout(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession(true);
		String sessionId = ServiceUtil.getSessionId(request);
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "getLogout",AuditEnumType.SESSION, sessionId);
		OrganizationType org = null;
		
		UserType user = null;
		UserSessionType userSession = null;
		try{
			org = ServiceUtil.getOrganizationFromRequest(request);
			if(org != null) user = SessionSecurity.getUserBySession(sessionId, org.getId());
			if(user != null){
				AuditService.targetAudit(audit, AuditEnumType.USER, user.getName());
				SessionSecurity.logout(user.getSession());
				userSession = user.getSession();
				AuditService.permitResult(audit, "User session logged out");
			}
			else{
				AuditService.denyResult(audit, "User not found");
				if(org != null) userSession = SessionSecurity.getUserSession(sessionId, org.getId());
			}
			logger.info("Not invalidating the session while JEE LoginModule is not being used.");
			//session.invalidate();

		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			AuditService.denyResult(audit, "Error: " + fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			e.printStackTrace();
		}
		ServiceUtil.clearCookie(response, "OrganizationId");
		ServiceUtil.clearCookie(response, "OrganizationPath");
		ServiceUtil.clearCookie(response, ServiceUtil.AM5_COOKIE_NAME);
		//ServiceUtil.clearCookie(response, "JSESSIONID");
		if(userSession != null) return BeanUtil.getSessionBean(userSession, sessionId);
		return null;
	}

	//@POST @Path("/postConfirmation") @Produces(MediaType.TEXT_PLAIN) @Consumes(MediaType.APPLICATION_JSON)
	//public boolean postRegistration(String id, String regId, @Context HttpServletRequest request){
	@POST @Path("/confirm") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean confirmRegistration(AuthenticationRequestType authRequest, @Context HttpServletRequest request) {
		StringBuffer buff = new StringBuffer();
		if(authRequest.getTokens().size() == 0){
			logger.error("Token is missing");
			return false;
		}
		if(authRequest.getTokens().size() == 0){
			logger.error("Token is missing");
			return false;
		}
		if(authRequest.getCredentialType() != CredentialEnumType.HASHED_PASSWORD || authRequest.getCredential() == null || authRequest.getCredential().length == 0){
			logger.error("Invalid credential");
			return false;
		}
		String sessionId = ServiceUtil.getSessionId(request);
		String id = authRequest.getSubject();
		String regId = authRequest.getTokens().get(0);
		
		//boolean registered = false;
		boolean registered = false;
		try {
			registered = RegistrationUtil.confirmUserRegistration(id,  regId, new String(authRequest.getCredential(),"UTF-8"),request.getRemoteAddr(), sessionId);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		MessageBean bean = new MessageBean();
		bean.setId(UUID.randomUUID().toString());
		bean.setId(id);
		bean.setName("Registration");
		bean.setData(regId);
		logger.error("id=" + id + " / reg=" + regId);
		*/
		//buff.append("<html><head></head><body>[ ... " + registered + " ...]</body></html>");
		//return buff.toString();
		//return bean;
		return registered;
		
	}
	
	
	
	@POST @Path("/postRegistration") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public SessionBean postRegistration(UserType user, @Context HttpServletRequest request, @Context HttpServletResponse response){
		//logger.error(request.getRemoteAddr());
		String sessionId = ServiceUtil.getSessionId(request,response,true);
		SessionBean ctxSession = null;
		try {
			Factories.getUserFactory().normalize(user);
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(user.getOrganizationId().compareTo(0L) == 0) user.setOrganizationId(Factories.getPublicOrganization().getId());
		
		logger.info("Processing registration in organization " + user.getOrganizationId());
		
		boolean ipLimit = Boolean.parseBoolean(request.getServletContext().getInitParameter("registration.ip.limit"));
		boolean regEnabled = Boolean.parseBoolean(request.getServletContext().getInitParameter("registration.enabled"));
		if(regEnabled == false){
			AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Registration", AuditEnumType.USER, user.getName() + " in " + user.getOrganizationId());
			AuditService.denyResult(audit, "Web Registration is disabled");
			return null;
		}

				
		UserSessionType session1 = RegistrationUtil.createUserRegistration(user, request.getRemoteAddr(),ipLimit);
		if(session1 != null){
			try{
				ctxSession = BeanUtil.getSessionBean(SessionSecurity.getUserSession(sessionId, user.getOrganizationId()),sessionId);
				ctxSession.setValue("registration-session-id", session1.getSessionId());
				ctxSession.setValue("registration-organization-id", Long.toString(user.getOrganizationId()));
				Factories.getSessionFactory().updateData(ctxSession);
			}
			catch(FactoryException fe){
				logger.error(fe.getMessage());
				fe.printStackTrace();
			}
		}
		return BeanUtil.getSessionBean(session1, (session1 == null ? null : session1.getSessionId()));
	}
	
	
	@GET @Path("/count/{organizationId:[0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("organizationId") long organizationId,@Context HttpServletRequest request){
		return UserServiceImpl.count(organizationId, request);
	}
	@POST @Path("/delete") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean delete(UserType bean,@Context HttpServletRequest request){
		return UserServiceImpl.delete(bean, request);
	}
	
	@POST @Path("/add") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean add(UserType bean,@Context HttpServletRequest request){
		
		return UserServiceImpl.add(bean, request);
	}
	
	@POST @Path("/update") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean update(UserType bean,@Context HttpServletRequest request){
		return UserServiceImpl.update(bean, request);
	}
	@GET @Path("/read/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public UserType read(@PathParam("name") String name,@Context HttpServletRequest request){
		
		return UserServiceImpl.readByOrganizationId(ServiceUtil.getOrganizationFromRequest(request).getId(),name, request);
	}
	@GET @Path("/readByOrganizationId/{orgId:[0-9]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public UserType readByOrganizationId(@PathParam("name") String name,@PathParam("orgId") long orgId,@Context HttpServletRequest request){
		return UserServiceImpl.readByOrganizationId(orgId, name, request);
	}	
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public UserType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		return UserServiceImpl.readById(id, request);
	}
	
	@GET @Path("/list") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserType> list(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return UserServiceImpl.getList(user, user.getOrganizationId(),0,0);

	}
	@GET @Path("/listInOrganization/{orgId : [\\d]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserType> listInOrganization(@PathParam("orgId") long orgId,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType org = null;
		try {
			org =Factories.getOrganizationFactory().getOrganizationById(orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(org == null){
			return new ArrayList<UserType>();
		}
		return UserServiceImpl.getList(user, orgId, startIndex, recordCount );

	}
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}