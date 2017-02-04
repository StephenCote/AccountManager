package org.cote.rest.services;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;

@DeclareRoles({"admin","user"})
@Path("/cache")
public class GenericCacheService {

	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(GenericCacheService.class);
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

	@RolesAllowed({"admin","user"})
	@GET
	@Path("/clearAll")
	@Produces(MediaType.APPLICATION_JSON)
	public Response clearFactoryCaches(@PathParam("type") String type, @PathParam("objectType") String objectType, @PathParam("path") String path, @Context HttpServletRequest request){
		logger.info("Request to clear all factory caches");
		Factories.clearCaches();
		return Response.status(200).entity(true).build();
	}

	@RolesAllowed({"admin","user"})
	@GET
	@Path("/clearAuthorization")
	@Produces(MediaType.APPLICATION_JSON)
	public Response clearAuthorizationCache(@PathParam("type") String type, @PathParam("objectType") String objectType, @PathParam("path") String path, @Context HttpServletRequest request){
		logger.info("Request to clear authorization cache");
		EffectiveAuthorizationService.clearCache();
		return Response.status(200).entity(true).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/clear/{type:[A-Za-z]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response clearFactoryCache(@PathParam("type") String type, @PathParam("objectType") String objectType, @PathParam("path") String path, @Context HttpServletRequest request){
		logger.info("Request to clear cache on: " + type);
		boolean out_bool = false;
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		INameIdFactory factory = BaseService.getFactory(auditType);
		if(factory != null){
			factory.clearCache();
			out_bool = true;
		}
		return Response.status(200).entity(out_bool).build();
	}


}
