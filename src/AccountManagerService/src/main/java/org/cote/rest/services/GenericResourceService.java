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
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;

/*
  GET
  Single - /resources/{type}/{objectId}
  List - /resources/{type}/{groupId}/{startIndex}/{count}
  ListInParent - /resources/{type}/{parentId}/{startIndex}/{count}
  Count - /resources/{type}/{parentId}/count
     ** Note: GROUP, PERMISSION, and ROLE use countInParent vs. countInGroup
 */

@DeclareRoles({"admin","user"})
@Path("/resources/{type:[A-Za-z]+}")
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
	@Path("/{objectId:[0-9A-Za-z\\-]+}/{startIndex:[\\d]+}/{count:[\\d]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listObjects(@PathParam("type") String type, @PathParam("objectId") String objectId, @PathParam("startIndex") long startIndex, @PathParam("count") int recordCount, @Context HttpServletRequest request){
		logger.info("Request to list objects in: " + type + " " + objectId);
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		List<Object> objs = new ArrayList<>();
		if(parentType.contains(auditType)){
			logger.error("REFACTOR LIST IN PARENT");
			objs = BaseService.listByParentObjectId(auditType, "UNKNOWN", objectId, startIndex, recordCount, request);
		}
		else{
			objs = BaseService.listByGroup(auditType, "UNKNOWN", objectId, startIndex, recordCount, request);
		}
		return Response.status(200).entity(objs).build();
	}
	
	@RolesAllowed({"user"})
	@GET
	@Path("/{objectId:[0-9A-Za-z\\-]+}/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response countObjects(@PathParam("type") String type, @PathParam("objectId") String objectId, @Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		//List<Object> objs = new ArrayList<>();
		int count = 0;
		if(parentType.contains(auditType)){
			logger.error("REFACTOR COUNT IN PARENT");
			Response r = getObject(type,objectId,request);
			//NameIdType parent = r.readEntity(NameIdType.class);
			NameIdType parent = (NameIdType)r.getEntity();
			if(parent != null){
				count = BaseService.countInParent(auditType, parent, request);
			}
		}
		else{
			BaseGroupType group = (BaseGroupType)getObject("GROUP",objectId,request).getEntity();
			count = BaseService.countByGroup(auditType, group, request);
		}
		return Response.status(200).entity(count).build();
	}
	
	@RolesAllowed({"user"})
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(String json, @PathParam("type") String type, @Context HttpServletRequest request){
		boolean updated = false;
		
		return Response.status(200).entity(updated).build();
	}
	
}
