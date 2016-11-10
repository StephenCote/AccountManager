package org.cote.rest.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.util.JSONUtil;

/*
  GET
  Single - /resources/{type}/{objectId}
  
  POST
  Single - (add, update)
 */

@DeclareRoles({"admin","user"})
@Path("/resource/{type:[A-Za-z]+}")
public class GenericResourceService {
	
	protected static Set<AuditEnumType> parentType = new HashSet<>(Arrays.asList(AuditEnumType.GROUP, AuditEnumType.ROLE, AuditEnumType.PERMISSION));
	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(GenericResourceService.class);

	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	
	@RolesAllowed({"user"})
	@GET
	@Path("/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObject(@PathParam("type") String type, @PathParam("objectId") String objectId,@Context HttpServletRequest request){
		logger.info("Request for object: " + type + " " + objectId);
		Object obj = BaseService.readByObjectId(AuditEnumType.valueOf(type), objectId, request);
		return Response.status(200).entity(obj).build();
	}
	
	@RolesAllowed({"user"})
	@GET
	@Path("/{parentId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjectByNameInParent(@PathParam("type") String type, @PathParam("parentId") String parentId,@PathParam("name") String name,@Context HttpServletRequest request){
		Object obj = null;
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		INameIdFactory iFact = BaseService.getFactory(auditType);
		if(iFact.isClusterByParent() && !iFact.isClusterByGroup()){
			logger.info("Request to get " + type + " object by parent in " + type + " " + parentId);
			NameIdType parentObj = (NameIdType)getObject(type,parentId,request).getEntity();
			if(parentObj != null){
				obj = BaseService.readByNameInParent(auditType, parentObj, name, "UNKNOWN", request);
			}
		}
		else{
			logger.info("Request to get " + type + " object by name in GROUP " + parentId);
			DirectoryGroupType dir = (DirectoryGroupType)getObject("GROUP",parentId,request).getEntity();
			if(dir != null) obj = BaseService.readByName(auditType, dir, name, request);
		}
		
		return Response.status(200).entity(obj).build();
	}

	@RolesAllowed({"user"})
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(String json, @PathParam("type") String type, @Context HttpServletRequest request){
		boolean updated = false;
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		Class cls = Factories.getFactoryTypeClasses().get(FactoryEnumType.valueOf(type));
		if(cls != null){
			NameIdType obj = (NameIdType)JSONUtil.importObject(json, cls);
			if(obj != null){
				//logger.info("Imported " + obj.getName());
				if(obj.getObjectId() == null || obj.getObjectId().length() == 0){
					updated = BaseService.add(auditType, obj, request);
				}
				else{
					updated = BaseService.update(auditType, obj, request);
				}
			}
			else{
				logger.error("Failed to restore object", json);
			}
		}
		return Response.status(200).entity(updated).build();
	}
	
}
