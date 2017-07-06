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
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;

@DeclareRoles({"admin","user"})
@Path("/make/{type:[A-Za-z]+}")
public class GenericMakeService {

	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(GenericMakeService.class);
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

	@RolesAllowed({"user"})
	@GET
	@Path("/{objectType:[A-Za-z]+}/{path:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findMakeObject(@PathParam("type") String type, @PathParam("objectType") String objectType, @PathParam("path") String path, @Context HttpServletRequest request){
		logger.info("Request to find object from: " + type + " " + path + ", and if it doesn't exist, then make it");
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		if(path.startsWith("~") == false && path.startsWith(".") == false){
			path = "/" + path;
			/// Doubled up to allow for actual punctuation use
			/// Clearly this is a bandaid
			///
			if(path.contains("..")) path = path.replaceAll("\\.\\.", "/");
			else path = path.replace('.', '/');
			logger.info("Alt path: " + path);
		}
		

		Object obj = BaseService.makeFind(auditType, objectType, path, request);
		return Response.status(200).entity(obj).build();
	}


}
