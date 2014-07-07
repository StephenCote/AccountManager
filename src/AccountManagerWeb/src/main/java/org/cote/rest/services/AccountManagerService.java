package org.cote.rest.services;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;


@Path("/accountmanager")
public class AccountManagerService{

	public static final Logger logger = Logger.getLogger(AccountManagerService.class.getName());
	private static SchemaBean schemaBean = null;
	
	public AccountManagerService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	@GET @Path("/flushCache") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean flushCache(@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "flushAll",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, AuditEnumType.INFO, "Flush all factory caches");
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		boolean out_bool = Factories.clearCaches();
		AuditService.permitResult(audit,"Flushed cache");
		return out_bool;
	}

	@GET @Path("/clearAuthorizationCache") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean clearAuthorizationCache(@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Clear Authorization Cache",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, AuditEnumType.INFO, "Flush authorization caches");
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		EffectiveAuthorizationService.clearCache();
		AuditService.permitResult(audit,"Flushed cache");
		return true;
	}	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}