package org.cote.rest.services;

import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.util.BeanUtil;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.rest.SessionBean;
import org.cote.accountmanager.service.util.RegistrationUtil;
import org.cote.accountmanager.service.util.ServiceUtil;

@Path("/registration")
public class RegistrationService {
	
	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(RegistrationService.class);

	@GET
	@Path("/smd")
	@Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

	
	@POST
	@Path("/confirm")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean confirmRegistration(AuthenticationRequestType authRequest, @Context HttpServletRequest request) {

		if(!authRequest.getTokens().isEmpty()){
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
		
		boolean registered = false;
		try {
			registered = RegistrationUtil.confirmUserRegistration(id,  regId, new String(authRequest.getCredential(),"UTF-8"),request.getRemoteAddr(), sessionId);
		} catch (UnsupportedEncodingException e) {
			
			logger.error("Error",e);
		}
		return registered;
		
	}
	
	
	
	@POST
	@Path("/postRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public SessionBean postRegistration(UserType user, @Context HttpServletRequest request, @Context HttpServletResponse response){
		String sessionId = ServiceUtil.getSessionId(request,response,true);
		SessionBean ctxSession = null;
		try {
			Factories.getNameIdFactory(FactoryEnumType.USER).normalize(user);
		} catch (ArgumentException | FactoryException e) {
			
			logger.error("Error",e);
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
				logger.error("Error",fe);
			}
		}
		return BeanUtil.getSessionBean(session1, (session1 == null ? null : session1.getSessionId()));
	}

}
