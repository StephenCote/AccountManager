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
import javax.ws.rs.Consumes;
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
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;



@DeclareRoles({"admin","user"})
@Path("/authorization/{type:[A-Za-z]+}")
public class AuthorizationService {
	
	protected static Set<AuditEnumType> parentType = new HashSet<>(Arrays.asList(AuditEnumType.GROUP, AuditEnumType.ROLE, AuditEnumType.PERMISSION));
	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(AuthorizationService.class);

	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/systemRoles")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getSystemRoles(@Context HttpServletRequest request){
		List<BasePermissionType> outList = BaseService.listSystemEntitlements(AuditEnumType.ROLE, request);
		return Response.status(200).entity(outList).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/systemPolicies")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getSystemPolicies(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		List<PolicyType> outList = new ArrayList<>();
		DirectoryGroupType dir = BaseService.find(AuditEnumType.GROUP, GroupEnumType.DATA.toString(), "/Home/PolicyUser/Policies", user);
		if(dir != null) {
			outList = BaseService.listByGroup(AuditEnumType.POLICY, "DATA", dir.getObjectId(), 0L, 0, user);
		}
		else {
			logger.error("Failed to access policy directory");
		}
		
		return Response.status(200).entity(outList).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/systemPermissions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getSystemPermissions(@Context HttpServletRequest request){
		

		List<BasePermissionType> outList = BaseService.listSystemEntitlements(AuditEnumType.PERMISSION, request);
		return Response.status(200).entity(outList).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/{objectId:[0-9A-Za-z\\-]+}/permit/{actorType:[A-Za-z]+}/{actorId:[0-9A-Za-z\\-]+}/{view:(true|false)}/{edit:(true|false)}/{delete:(true|false)}/{create:(true|false)}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response permitSystem(@PathParam("type") String objectType, @PathParam("objectId") String objectId, @PathParam("actorType") String actorType, @PathParam("actorId") String actorId,  @PathParam("view") boolean view,@PathParam("edit") boolean edit,@PathParam("delete") boolean delete,@PathParam("create") boolean create,@Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(objectType);
		AuditEnumType auditActorType = AuditEnumType.valueOf(actorType);
		UserType user = ServiceUtil.getUserFromSession(request);
		boolean permitted = false;
		if(auditActorType == AuditEnumType.USER || auditActorType == AuditEnumType.ROLE){
			NameIdType obj = BaseService.readByObjectId(auditType, objectId, request);
			NameIdType actor = BaseService.readByObjectId(auditActorType, actorId, request);
			if(obj == null || actor == null){
				logger.error("Object or actor is null");
			}
			else{
				if(auditActorType == AuditEnumType.USER){
					permitted = BaseService.authorizeUser(auditType, user.getOrganizationId(), actor.getId(), obj, view, edit, delete, create, request);
				}
				else if(auditActorType == AuditEnumType.ROLE){
					permitted = BaseService.authorizeRole(auditType, user.getOrganizationId(), actor.getId(), obj, view, edit, delete, create, request);
				}
				
			}
		}
		else{
			logger.warn("This method is  for setting system level permissions, and intentionally restricted to USER and ROLE actors.  Use the alternate method for setting arbitrary permissions by actor on objects");
		}
		return Response.status(200).entity(permitted).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/{objectId:[0-9A-Za-z\\-]+}/permit/{actorType:[A-Za-z]+}/{actorId:[0-9A-Za-z\\-]+}/{permissionId:[0-9A-Za-z\\-]+}/{permit:(true|false)}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response permit(@PathParam("type") String objectType, @PathParam("objectId") String objectId, @PathParam("actorType") String actorType, @PathParam("actorId") String actorId,  @PathParam("permissionId") String permissionId,@PathParam("permit") boolean permit,@Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(objectType);
		UserType user = ServiceUtil.getUserFromSession(request);
		boolean permitted = BaseService.setPermission(user, auditType, objectId, AuditEnumType.valueOf(actorType), actorId, permissionId, permit);
		return Response.status(200).entity(permitted).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/{objectId:[0-9A-Za-z\\-]+}/member/{actorType:[A-Za-z]+}/{actorId:[0-9A-Za-z\\-]+}/{enable:(true|false)}")
	@Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public Response enableMember(@PathParam("type") String objectType, @PathParam("objectId") String objectId, @PathParam("actorType") String actorType, @PathParam("actorId") String actorId, @PathParam("enable") boolean enable,@Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(objectType);
		UserType user = ServiceUtil.getUserFromSession(request);
		//boolean permitted = BaseService.setPermission(user, auditType, objectId, AuditEnumType.valueOf(actorType), actorId, permissionId, permit);
		boolean permitted = BaseService.setMember(user, auditType, objectId, AuditEnumType.valueOf(actorType), actorId, enable);
		return Response.status(200).entity(permitted).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET @Path("/{objectId:[0-9A-Za-z\\-]+}/{actorType:[A-Za-z]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response members(@PathParam("type") String objectType, @PathParam("objectId") String objectId, @PathParam("actorType") String actorType, @Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(objectType);
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType container = BaseService.readByObjectId(auditType, objectId, request);
		List<Object> members = new ArrayList<>();
		if(container != null){
			members = BaseService.listMembers(auditType, user, container, FactoryEnumType.valueOf(actorType));
		}
		return Response.status(200).entity(members).build();
	}
	
	// [PERMISSION|ROLE]
	// Used to retrieve permission or role objects relative to the user, which the user owns
	// Used as a reference point for defining custom roles and permissions
	//
	@RolesAllowed({"admin","user"})
	@GET @Path("/user/{otype:[A-Za-z]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getPrincipalType(@PathParam("type") String type, @PathParam("otype") String otype, @Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		UserType user = ServiceUtil.getUserFromSession(request);
		Object obj = null;
		try{
		if(user != null){
			switch(auditType){
				case PERMISSION:
					BasePermissionType per = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getUserPermission(user, PermissionEnumType.valueOf(otype), user.getOrganizationId());
					((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).denormalize(per);
					obj = per;
					break;
				case ROLE:
					BaseRoleType role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(user, RoleEnumType.valueOf(otype), user.getOrganizationId());
					((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).denormalize(role);
					obj = role;
					break;
				case GROUP:
					/// ignores otype
					///
					BaseGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserDirectory(user);
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(group);
					obj = group;
					break;
				default:
					logger.error(String.format(FactoryException.UNHANDLED_TYPE, auditType.toString()));
					break;
				}
			}
		}
		catch(FactoryException | ArgumentException | DataAccessException e){
			logger.error(e);
		}
		return Response.status(200).entity(obj).build();
	}
	

	@GET @Path("/roles/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed({"admin","user"})
	public Response listUserRolesForType(@PathParam("type") String objectType,@PathParam("objectId") String objectId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		//NameIdType targUser = null;
		FactoryEnumType factType = FactoryEnumType.USER;
		if(objectType != null && objectType.equalsIgnoreCase("unknown") == false) {
			factType = FactoryEnumType.valueOf(objectType);
		}
		else {
			factType = FactoryEnumType.USER;
			objectType = "USER";
		}
		if(objectId == null || objectId.length() == 0 || objectId.equalsIgnoreCase("null")) {
			objectId = user.getObjectId();
		}
		NameIdType obj = BaseService.readByObjectId(AuditEnumType.fromValue(objectType), objectId, user);
		if(obj == null) {
			logger.error(objectType + " " + objectId + " was not accessible or does not exist");
			return Response.status(200).entity(new ArrayList<EntitlementType>()).build();
		}

		/*
		try {
			targUser = ((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).getByObjectId(objectId, user.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		*/
		List<Object> objs = BaseService.listForMember(AuditEnumType.ROLE, user, obj, factType);
		return Response.status(200).entity(objs).build();
	}
	
	@GET @Path("/entitlements/{objectId:[0-9A-Za-z\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed({"admin","user"})
	public Response listEntitlementsForMemberType(@PathParam("type") String objectType,@PathParam("objectId") String objectId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType obj = BaseService.readByObjectId(AuditEnumType.fromValue(objectType), objectId, user);
		if(obj == null) {
			logger.error(objectType + " " + objectId + " was not accessible or does not exist");
			return Response.status(200).entity(new ArrayList<EntitlementType>()).build();
		}
		return Response.status(200).entity(BaseService.aggregateEntitlementsForMember(user, obj)).build();
	}
	
	@GET @Path("/system/{objectId:[0-9A-Za-z\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed({"admin","user"})
	public Response getSystemAuthorizationForObject(@PathParam("type") String objectType,@PathParam("objectId") String objectId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType obj = BaseService.readByObjectId(AuditEnumType.fromValue(objectType), objectId, user);
		ObjectAuthorization authZ = new ObjectAuthorization(NameEnumType.valueOf(objectType), objectId);
		if(obj == null) {
			logger.error(objectType + " " + objectId + " was not accessible or does not exist");
			return Response.status(200).entity(authZ).build();
		}
		authZ.setView(true);
		AuditEnumType type = AuditEnumType.valueOf(objectType);
		try {
			authZ.setCreate(BaseService.canCreateType(type, user, obj));
			authZ.setDelete(BaseService.canDeleteType(type, user, obj));
			authZ.setExecute(BaseService.canExecuteType(type, user, obj));
			authZ.setChange(BaseService.canChangeType(type, user, obj));
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return Response.status(200).entity(authZ).build();
	}
	
	class ObjectAuthorization{
		
		private NameEnumType objectType = NameEnumType.UNKNOWN;
		private String objectId = null;
		private boolean view = false;
		private boolean change = false;
		private boolean delete = false;
		private boolean create = false;
		private boolean execute = false;
		
		public ObjectAuthorization(NameEnumType type, String objectId) {
			this.objectId = objectId;
			this.objectType = type;
		}
		
		public NameEnumType getObjectType() {
			return objectType;
		}
		public void setObjectType(NameEnumType objectType) {
			this.objectType = objectType;
		}
		public String getObjectId() {
			return objectId;
		}
		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}
		public boolean isView() {
			return view;
		}
		public void setView(boolean view) {
			this.view = view;
		}
		public boolean isChange() {
			return change;
		}
		public void setChange(boolean change) {
			this.change = change;
		}
		public boolean isDelete() {
			return delete;
		}
		public void setDelete(boolean delete) {
			this.delete = delete;
		}
		public boolean isCreate() {
			return create;
		}
		public void setCreate(boolean create) {
			this.create = create;
		}
		public boolean isExecute() {
			return execute;
		}
		public void setExecute(boolean execute) {
			this.execute = execute;
		}
		
	}
	
}
