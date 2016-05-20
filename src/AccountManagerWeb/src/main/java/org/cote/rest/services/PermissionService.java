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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.services.PermissionServiceImpl;


@Path("/permission")
public class PermissionService{
	private static SchemaBean schemaBean = null;
	public static final Logger logger = Logger.getLogger(PermissionService.class.getName());
	public PermissionService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	
	@GET @Path("/count/{group:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("organizationId") long organizationId,@Context HttpServletRequest request){
		return PermissionServiceImpl.count(organizationId, request);
	}
	
	@GET @Path("/countInParent/{organizationId:[\\d]+}/{parentId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int countInParent(@PathParam("organizationId") long organizationId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){
		return PermissionServiceImpl.countInParent(organizationId, parentId,request);
	}

	@POST @Path("/delete") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean delete(BasePermissionType bean,@Context HttpServletRequest request){
		return PermissionServiceImpl.delete(bean, request);
	}
	
	@POST @Path("/add") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean add(BasePermissionType bean,@Context HttpServletRequest request){
		return PermissionServiceImpl.add(bean, request);
	}
	
	@POST @Path("/update") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean update(BasePermissionType bean,@Context HttpServletRequest request){
		return PermissionServiceImpl.update(bean, request);
	}
	/*
	@GET @Path("/read/{name: [%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BasePermissionType read(@PathParam("name") String name,@Context HttpServletRequest request){
		return PermissionServiceImpl.read(name, request);
	}
	*/
	/*
	@GET @Path("/readByGroupId/{groupId:[0-9]+}/{name: [%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BasePermissionType readByGroupId(@PathParam("name") String name,@PathParam("groupId") long groupId,@Context HttpServletRequest request){
		return PermissionServiceImpl.readByGroupId(groupId, name, request);
	}
	*/	
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BasePermissionType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		return PermissionServiceImpl.readById(id, request);
	}
	
	@GET @Path("/readByParentId/{orgId: [0-9]+}/{parentId:[0-9]+}/{type: [%\\sa-zA-Z_0-9\\-]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BasePermissionType readByParentId(@PathParam("name") String name,@PathParam("type") String type, @PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){

		return PermissionServiceImpl.readByParent(orgId, parentId, name, type, request);
	}
	
	@GET @Path("/getPath/{id: [0-9]+}") @Produces(MediaType.TEXT_PLAIN) @Consumes(MediaType.TEXT_PLAIN)
	public String getPath(@PathParam("id") long id,@Context HttpServletRequest request){
		String path = null;
		BasePermissionType permission = PermissionServiceImpl.readById(id, request);
	
			try {
				if(permission != null) path = Factories.getPermissionFactory().getPermissionPath(permission);
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		return path;
	}

	/*
	@GET @Path("/list") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BasePermissionType> list(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.getGroupList(user, PermissionServiceImpl.defaultDirectory, 0,0 );

	}
	@GET @Path("/listInGroup/{path : [~%\\s0-9a-zA-Z\\/]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BasePermissionType> listInGroup(@PathParam("path") String path,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.getGroupList(user, path, startIndex, recordCount );

	}
	*/
	@GET @Path("/listInParent/{orgId : [\\d]+}/{parentId : [\\d]+}/{type : [~%\\s0-9a-zA-Z\\/]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BasePermissionType> listInParent(@PathParam("type") String type, @PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		BasePermissionType parent = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getById(orgId, 0L);
			if(org != null) parent =Factories.getPermissionFactory().getById(parentId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(parent == null){
			System.out.println("Null permission for id " + parentId + " in org " + org);
			return new ArrayList<BasePermissionType>();
		}
		return PermissionServiceImpl.getListInParent(user, type, parent, startIndex, recordCount );

	}
	@GET @Path("/getUserPermission/{type : [~%\\s0-9a-zA-Z\\/]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BasePermissionType getUserPermission(@PathParam("type") String type, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.getUserPermission(user,type,request);
	}
	
	@GET @Path("/setPermissionOnGroupForUser/{gid : [0-9]+}/{uid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnGroupForUser(@PathParam("gid") long groupId, @PathParam("uid") long userId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.GROUP, groupId, AuditEnumType.USER, userId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnGroupForAccount/{gid : [0-9]+}/{aid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnGroupForAccount(@PathParam("gid") long groupId, @PathParam("aid") long accountId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.GROUP, groupId, AuditEnumType.ACCOUNT, accountId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnGroupForPerson/{gid : [0-9]+}/{peid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnGroupForPerson(@PathParam("gid") long groupId, @PathParam("peid") long personId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.GROUP, groupId, AuditEnumType.PERSON, personId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnGroupForRole/{gid : [0-9]+}/{rid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnGroupForRole(@PathParam("gid") long groupId, @PathParam("rid") long roleId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.GROUP, groupId, AuditEnumType.ROLE, roleId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnGroupForGroup/{gid : [0-9]+}/{rid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnGroupForGroup(@PathParam("gid") long groupId, @PathParam("rid") long targGroupId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.GROUP, groupId, AuditEnumType.GROUP, targGroupId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnDataForUser/{did : [0-9]+}/{uid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnDataForUser(@PathParam("did") long dataId, @PathParam("uid") long userId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.DATA, dataId, AuditEnumType.USER, userId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnDataForGroup/{did : [0-9]+}/{uid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnDataForGroup(@PathParam("did") long dataId, @PathParam("uid") long groupId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.DATA, dataId, AuditEnumType.GROUP, groupId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnDataForAccount/{did : [0-9]+}/{aid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnDataForAccount(@PathParam("did") long dataId, @PathParam("aid") long accountId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.DATA, dataId, AuditEnumType.ACCOUNT, accountId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnDataForPerson/{did : [0-9]+}/{peid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnDataForPerson(@PathParam("did") long dataId, @PathParam("peid") long personId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.DATA, dataId, AuditEnumType.PERSON, personId, permissionId, enable);
	}
	@GET @Path("/setPermissionOnDataForRole/{did : [0-9]+}/{rid : [0-9]+}/{pid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setPermissionOnDataForRole(@PathParam("did") long dataId, @PathParam("rid") long roleId, @PathParam("pid") long permissionId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.setPermission(user, AuditEnumType.DATA, dataId, AuditEnumType.ROLE, roleId, permissionId, enable);
	}
	@GET @Path("/listSystemPermissions/{oid : [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BasePermissionType> setPermissionOnDataForRole(@PathParam("oid") long organizationId, @Context HttpServletRequest request){
		List<BasePermissionType> permissions = new ArrayList<BasePermissionType>();
		
		try {
			OrganizationType organization = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(organization == null){
				logger.error("Invalid organization");
				return permissions;
			}
			logger.error("TODO: Revise per new permission setup");
			/*
			permissions.add(AuthorizationService.getCreateDataPermission(organizationId));
			permissions.add(AuthorizationService.getCreateGroupPermission(organizationId));
			permissions.add(AuthorizationService.getCreateRolePermission(organizationId));
			permissions.add(AuthorizationService.getDeleteDataPermission(organizationId));
			permissions.add(AuthorizationService.getDeleteGroupPermission(organizationId));
			permissions.add(AuthorizationService.getDeleteRolePermission(organizationId));
			permissions.add(AuthorizationService.getEditDataPermission(organizationId));
			permissions.add(AuthorizationService.getEditGroupPermission(organizationId));
			permissions.add(AuthorizationService.getEditRolePermission(organizationId));
			permissions.add(AuthorizationService.getViewDataPermission(organizationId));
			permissions.add(AuthorizationService.getViewGroupPermission(organizationId));
			permissions.add(AuthorizationService.getViewRolePermission(organizationId));
			*/
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return permissions;
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
		AuditService.targetAudit(audit, AuditEnumType.PERMISSION, "Permission Factory");
		Factories.getFactFactory().clearCache();
		AuditService.permitResult(audit,user.getName() + " flushed Permission Factory cache");
		return true;
	}	
	
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

}