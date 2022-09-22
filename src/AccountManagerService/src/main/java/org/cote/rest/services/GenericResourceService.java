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
import javax.ws.rs.DELETE;
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
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.MessageFactory;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.TagService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.rocket.services.RoleService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

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
		logger.debug("Request for object: " + type + " " + objectId);
		AuditEnumType atype = AuditEnumType.valueOf(type);
		UserType user = ServiceUtil.getUserFromSession(request);
		Object obj = null;
		if(atype.equals(AuditEnumType.MESSAGE)) {
			/// messagefactory does not entirely follow the NameIdFactory pattern, so the authorization services won't work for it.
			///

			AuditType audit = AuditService.beginAudit(ActionEnumType.READ, objectId, AuditEnumType.USER, user.getUrn());

			try {
				MessageFactory mfact = Factories.getFactory(FactoryEnumType.MESSAGE);
				MessageSpoolType mst = mfact.getMessageByObjectId(objectId, user.getOrganizationId());
				if(mst != null) {
					/// same owner

					AuditService.targetAudit(audit, AuditEnumType.MESSAGE, mst.getName());
					if(
						mst.getOwnerId().equals(user.getId())
						||
						RoleService.getIsUserInRole(RoleService.getAccountAdministratorUserRole(user.getOrganizationId()), user)
						||
						RoleService.getIsUserInRole(RoleService.getDataAdministratorUserRole(user.getOrganizationId()), user)
					) {
						obj = mst;
						AuditService.permitResult(audit, "Authorized");
					}
					else {
						AuditService.denyResult(audit, "Not authorized");
					}
							
				}
				else {
					AuditService.denyResult(audit, "Object doesn't exist");
				}
			}
			catch(FactoryException | ArgumentException e) {
				logger.error(e);
			}
		}
		else {
			obj = BaseService.readByObjectId(atype, objectId, user);
		}
		return Response.status(200).entity(obj).build();
	}
	
	@RolesAllowed({"user"})
	@DELETE
	@Path("/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteObject(@PathParam("type") String type, @PathParam("objectId") String objectId,@Context HttpServletRequest request){
		logger.debug("Request for object: " + type + " " + objectId);
		Object obj = BaseService.readByObjectId(AuditEnumType.valueOf(type), objectId, request);
		boolean outBool = false;
		if(obj != null){
			outBool = BaseService.delete(AuditEnumType.valueOf(type), obj, request);
		}
		return Response.status(200).entity(outBool).build();
	}
	
	@RolesAllowed({"user"})
	@GET
	@Path("/{id:[0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjectById(@PathParam("type") String type, @PathParam("id") long id,@Context HttpServletRequest request){
		logger.debug("Request for object: " + type + " " + id);
		Object obj = BaseService.readById(AuditEnumType.valueOf(type), id, request);
		return Response.status(200).entity(obj).build();
	}
	
	@RolesAllowed({"user"})
	@GET
	@Path("/{parentId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjectByNameInParent(@PathParam("type") String type, @PathParam("parentId") String parentId,@PathParam("name") String name,@Context HttpServletRequest request){
		Object obj = null;
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		try{
			INameIdFactory iFact = BaseService.getFactory(auditType);
			if(iFact.isClusterByParent() && !iFact.isClusterByGroup()){
				logger.debug("Request to get " + type + " object by parent in " + type + " " + parentId);
				NameIdType parentObj = (NameIdType)getObject(type,parentId,request).getEntity();
				if(parentObj != null){
					obj = BaseService.readByNameInParent(auditType, parentObj, name, "UNKNOWN", request);
				}
			}
			else if(auditType == AuditEnumType.DATA || iFact.isClusterByGroup()){
				logger.debug("Request to get " + type + " object by name in GROUP " + parentId);
				DirectoryGroupType dir = (DirectoryGroupType)getObject("GROUP",parentId,request).getEntity();
				if(dir != null) obj = BaseService.readByName(auditType, dir, name, request);
			}
			else{
				logger.debug("Request to get " + type + " object by name in organization");
				UserType user = ServiceUtil.getUserFromSession(request);
				obj = BaseService.readByNameInOrganization(auditType, user.getOrganizationId(), name, request);
			}
		}
		catch(FactoryException f){
			logger.error(f);
		}
		return Response.status(200).entity(obj).build();
	}
	
	/// Specifically to allow for the variation where a factory is clustered by both group and parent
	/// To retrieve an object using a parent id vs. the group id
	///
	@RolesAllowed({"user"})
	@GET
	@Path("/tag/{objectId:[0-9A-Za-z\\-]+}/{tagId:[0-9A-Za-z\\\\-]+}/{tag:(true|false)}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response tagObject(@PathParam("type") String type, @PathParam("objectId") String objectId,@PathParam("tagId") String tagId,@PathParam("tag") boolean enableTag,@Context HttpServletRequest request){
		boolean outBool = false;
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		UserType user = ServiceUtil.getUserFromSession(request);
		try{
			NameIdType obj = BaseService.readByObjectId(auditType, objectId, user);
			BaseTagType tag = BaseService.readByObjectId(AuditEnumType.TAG, tagId, user);
			outBool = TagService.applyTag(user, tag, obj, enableTag);
		}
	
		catch(FactoryException | ArgumentException | DataAccessException f){
			logger.error(f);
		}
		return Response.status(200).entity(outBool).build();
	}

	
	/// Specifically to allow for the variation where a factory is clustered by both group and parent
	/// To retrieve an object using a parent id vs. the group id
	///
	@RolesAllowed({"user"})
	@GET
	@Path("/parent/{parentId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroupedObjectByNameInParent(@PathParam("type") String type, @PathParam("parentId") String parentId,@PathParam("name") String name,@Context HttpServletRequest request){
		Object obj = null;
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		try{
			INameIdFactory iFact = BaseService.getFactory(auditType);
			if(!iFact.isClusterByParent() || !iFact.isClusterByGroup()){
				logger.warn("Service intended for factories that are both clustered by group and parent");
			}
			NameIdType parentObj = (NameIdType)getObject(type,parentId,request).getEntity();
			if(parentObj == null){
				logger.error("Parent Object " + type + " " + parentId + " is null or not accessible");
				return Response.status(200).entity(obj).build();
			}
			obj = BaseService.readByNameInParent(auditType, parentObj, name, "UNKNOWN", request);
		}
	
		catch(FactoryException f){
			logger.error(f);
		}
		return Response.status(200).entity(obj).build();
	}

	/// Specifically to allow for the variation where a factory is clustered by both group and parent
	/// To retrieve an object using a parent id vs. the group id
	///
	@RolesAllowed({"user"})
	@GET
	@Path("/urn/{urn: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.:]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByUrn(@PathParam("type") String type, @PathParam("urn") String urn, @Context HttpServletRequest request){

		AuditEnumType auditType = AuditEnumType.valueOf(type);
		Object obj = BaseService.readByUrn(auditType, urn, request);
		return Response.status(200).entity(obj).build();
	}
	
	@RolesAllowed({"user"})
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(String json, @PathParam("type") String type, @Context HttpServletRequest request){
		boolean updated = false;
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		Class<?> cls = Factories.getFactoryTypeClasses().get(FactoryEnumType.valueOf(type));
		if(cls != null){
			NameIdType obj = (NameIdType)JSONUtil.importObject(json, cls);
			if(obj != null){
				if(auditType.equals(AuditEnumType.REQUEST)) {
					updated = org.cote.accountmanager.data.services.RequestService.updateRequest(ServiceUtil.getUserFromSession(request), (AccessRequestType)obj);
				}
				else if(auditType.equals(AuditEnumType.CONTROL)) {
					updated = org.cote.accountmanager.data.services.PolicyService.updateControl(ServiceUtil.getUserFromSession(request), (ControlType)obj);
				}
				else if(obj.getObjectId() == null || obj.getObjectId().length() == 0 || obj.getObjectId().equalsIgnoreCase("undefined")){
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
	
	@RolesAllowed({"user"})
	@POST
	@Path("/bulk")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObjects(String json, @PathParam("type") String type, @Context HttpServletRequest request){
		int updated = 0;
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		UserType user = ServiceUtil.getUserFromSession(request);
		Class<?> cls = Factories.getFactoryTypeClasses().get(FactoryEnumType.valueOf(type));
		if(cls != null){
			ObjectMapper mapper = new ObjectMapper();
			CollectionType listType =  mapper.getTypeFactory().constructCollectionType(ArrayList.class, cls);
			try {
			List<?> objs = mapper.readValue(json, listType);
			if(objs != null){
				String sessionId = BulkFactories.getBulkFactory().newBulkSession();
				for(Object obj : objs) {
					NameIdType objNT = (NameIdType)obj;
					if(objNT.getObjectId() == null || objNT.getObjectId().length() == 0 || objNT.getObjectId().equalsIgnoreCase("undefined")){
						if(BaseService.add(auditType, obj, user, sessionId)) {
							updated++;
						}
					}
					else{
						if(BaseService.update(auditType, obj, user, sessionId)) {
							updated++;
						}
					}
				}
				BulkFactories.getBulkFactory().write(sessionId);
			}
			else{
				logger.error("Failed to restore object");
			}
			}
			catch(JsonProcessingException | ArgumentException | FactoryException | DataAccessException jme) {
				logger.error(jme);
			}
		}
		return Response.status(200).entity(updated).build();
	}
	
}
