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
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.services.GroupServiceImpl;
import org.cote.accountmanager.services.RoleServiceImpl;

@Path("/group")
public class GroupService {

	public static final Logger logger = Logger.getLogger(GroupService.class.getName());
	private static SchemaBean schemaBean = null;	
	public GroupService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();

	}
	
	@GET @Path("/setGroupForPerson/{peid : [0-9]+}/{rid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setGroupForPerson(@PathParam("peid") long personId, @PathParam("rid") long groupId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return GroupServiceImpl.setGroup(user, groupId, AuditEnumType.PERSON, personId, enable);
	}

	@GET @Path("/setGroupForAccount/{peid : [0-9]+}/{rid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setGroupForAccount(@PathParam("peid") long accountId, @PathParam("rid") long groupId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return GroupServiceImpl.setGroup(user, groupId, AuditEnumType.ACCOUNT, accountId, enable);
	}

	@GET @Path("/setGroupForUser/{peid : [0-9]+}/{rid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setGroupForUser(@PathParam("peid") long userId, @PathParam("rid") long groupId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return GroupServiceImpl.setGroup(user, groupId, AuditEnumType.USER, userId, enable);
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
		AuditService.targetAudit(audit, AuditEnumType.GROUP, "Group Factory");
		Factories.getGroupFactory().clearCache();
		AuditService.permitResult(audit,user.getName() + " flushed Group Factory cache");
		return true;
	}
	
	@GET @Path("/countInParent/{organizationId:[\\d]+}/{parentId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int countInParent(@PathParam("organizationId") long organizationId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){
		return GroupServiceImpl.countInParent(organizationId, parentId,request);
	}
	
	/// Legacy - counts only directory groups
	@GET @Path("/count/{group:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("group") String path,@Context HttpServletRequest request){
		int out_count = 0;
		String sessionId = ServiceUtil.getSessionId(request);
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "Count Group",AuditEnumType.SESSION,ServiceUtil.getSessionId(request));

		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return 0;

		try{
			BaseGroupType group = (BaseGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.UNKNOWN, path, user.getOrganizationId());
			if(group == null ){
				AuditService.denyResult(audit, "Group not found");
				return 0;
			}
			AuditService.targetAudit(audit, AuditEnumType.GROUP, group.getUrn());
			if(!AuthorizationService.canViewGroup(user, group)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to view group " + group.getName() + " (#" + group.getId() + ")");
				return 0;
			}
			
			out_count = Factories.getGroupFactory().getCount(group);

			AuditService.permitResult(audit, "Count authorized for group " + group.getName());

			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			AuditService.denyResult(audit, fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} 
		return out_count;
	}

	@GET @Path("/listAuthorizedRoles/{organizationId:[\\d]+}/{groupId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseRoleType> listAuthorizedRoles(@PathParam("organizationId") long organizationId,@PathParam("groupId") long groupId, @Context HttpServletRequest request){
		List<BaseRoleType> roles = new ArrayList<BaseRoleType>();
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user == null){
			return roles;
		}
		BaseGroupType group = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(org != null) group = Factories.getGroupFactory().getGroupById(groupId, organizationId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		
		if(group != null) roles = RoleServiceImpl.getListOfRoles(user, group);
		return roles;
	}
	
	@GET @Path("/authorizeRole/{organizationId:[\\d]+}/{roleId:[\\d]+}/{groupId:[\\d]+}/{view:(true|false)}/{edit:(true|false)}/{delete:(true|false)}/{create:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean authorizeRole(@PathParam("organizationId") long organizationId,@PathParam("roleId") long roleId,@PathParam("groupId") long groupId,@PathParam("view") boolean view,@PathParam("edit") boolean edit,@PathParam("delete") boolean delete,@PathParam("create") boolean create,@Context HttpServletRequest request){
		boolean out_bool = false;

		NameIdType group = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(org != null) group = Factories.getGroupFactory().getGroupById(groupId, organizationId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(group != null) out_bool = BaseService.authorizeRole(AuditEnumType.GROUP, organizationId, roleId, group, view, edit, delete, create, request);
		return out_bool;
	}
	
	@GET @Path("/authorizeUser/{organizationId:[\\d]+}/{userId:[\\d]+}/{groupId:[\\d]+}/{view:(true|false)}/{edit:(true|false)}/{delete:(true|false)}/{create:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean authorizeUser(@PathParam("organizationId") long organizationId,@PathParam("userId") long userId,@PathParam("groupId") long groupId,@PathParam("view") boolean view,@PathParam("edit") boolean edit,@PathParam("delete") boolean delete,@PathParam("create") boolean create,@Context HttpServletRequest request){
		boolean out_bool = false;

		NameIdType group = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(org != null) group = Factories.getGroupFactory().getGroupById(groupId, organizationId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(group != null) out_bool = BaseService.authorizeUser(AuditEnumType.GROUP, organizationId, userId, group, view, edit, delete, create, request);
		return out_bool;
	}
	
	@GET @Path("/home") @Produces(MediaType.APPLICATION_JSON)
	public DirectoryGroupType home(@Context HttpServletRequest request){
		DirectoryGroupType bean = null;
		String sessionId = ServiceUtil.getSessionId(request);
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "home",AuditEnumType.SESSION,ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return bean;

		try{
			Factories.getUserFactory().populate(user);
			///DirectoryGroupType clone = BeanUtil.getBean(DirectoryGroupType.class, user.getHomeDirectory());
			///user.setHomeDirectory(clone);
			Factories.getGroupFactory().populate(user.getHomeDirectory());
			Factories.getGroupFactory().denormalize(user.getHomeDirectory());

			AuditService.targetAudit(audit, AuditEnumType.GROUP, user.getHomeDirectory().getUrn());
			AuditService.permitResult(audit, "Access authenticated user's home group");
			bean = user.getHomeDirectory();
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			AuditService.denyResult(audit, fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return bean;
	}
	
	@GET @Path("/readByParentId/{orgId: [0-9]+}/{parentId:[0-9]+}/{type: [%\\sa-zA-Z_0-9\\-]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseGroupType readByParentId(@PathParam("name") String name,@PathParam("type") String type, @PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){

		return GroupServiceImpl.readByParent(orgId, parentId, name, type, request);
	}
	
	/// NOTE: This is the same as read, but is left as 'cd' for OS familiarity
	/// But needs to be made consistent with the model used in the Rocket API.
	///
	@GET @Path("/readByPath/{type: [%\\sa-zA-Z_0-9\\-]+}/{path : [@\\.\\.~%\\s0-9a-z_A-Z\\/\\-]+}")  @Produces(MediaType.APPLICATION_JSON)
	public BaseGroupType readByPath(@PathParam("type") String type,@PathParam("path") String path,@Context HttpServletRequest request){
		return (BaseGroupType)BaseService.findGroup(GroupEnumType.valueOf(type), path, request);

	}

	@GET @Path("/listPersons/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<PersonType> listPersons(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		PersonGroupType targGroup = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targGroup = Factories.getGroupFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		if(targGroup != null) return GroupServiceImpl.getListOfPersons(user, targGroup);
		return new ArrayList<PersonType>();
	}
	@GET @Path("/listAccounts/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<AccountType> listAccounts(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		AccountGroupType targGroup = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targGroup = Factories.getGroupFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		if(targGroup != null) return GroupServiceImpl.getListOfAccounts(user, targGroup);
		return new ArrayList<AccountType>();
	}
	@GET @Path("/listUsers/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserType> listUsers(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		UserGroupType targGroup = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targGroup = Factories.getGroupFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		if(targGroup != null) return GroupServiceImpl.getListOfUsers(user, targGroup);
		return new ArrayList<UserType>();
	}
	
	/// TODO - dir should return a list, not a single group
	/// NOTE - this is the same as 'listInGroup', but is left as 'dir' for familiarity
	/// Legacy code
	/// Change this to CD, and then make dir use CD to obtain the parent.
	/*
	@GET @Path("/listInDataGroup/{path : [@\\.~%\\s0-9a-z_A-Z\\/\\-]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}")  @Produces(MediaType.APPLICATION_JSON)
	public List<BaseGroupType> listInDataGroup(@PathParam("path") String path,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		return GroupServiceImpl.listInGroup(GroupEnumType.DATA, path, startIndex, recordCount, request);

	}
	@GET @Path("/listInUserGroup/{path : [@\\.~%\\s0-9a-z_A-Z\\/\\-]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}")  @Produces(MediaType.APPLICATION_JSON)
	public List<BaseGroupType> listInUserGroup(@PathParam("path") String path,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		return GroupServiceImpl.listInGroup(GroupEnumType.USER, path, startIndex, recordCount, request);
	}
	*/
	@GET @Path("/listInParent/{orgId : [\\d]+}/{parentId : [\\d]+}/{type: [%\\sa-zA-Z_0-9\\-]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseGroupType> listInParent(@PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@PathParam("type") String type, @PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		BaseGroupType parent = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getById(orgId, 0L);
			if(org != null) parent =Factories.getGroupFactory().getById(parentId, orgId);
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
			System.out.println("Null group for id " + parentId + " in org " + org);
			return new ArrayList<BaseGroupType>();
		}
		return GroupServiceImpl.getListInParent(user, type, parent, startIndex, recordCount );

	}
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseGroupType readById(@PathParam("id") long parentId,@Context HttpServletRequest request){
		//return GroupServiceImpl.readById(id, request);

		BaseGroupType bean = null;
		String sessionId = ServiceUtil.getSessionId(request);
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "#" + parentId,AuditEnumType.SESSION,ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return bean;
		if(parentId <= 0){
			AuditService.denyResult(audit, "Invalid parent id: " + parentId);
			return bean;
		}
		try{
			BaseGroupType dir = Factories.getGroupFactory().getById(parentId, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Id " + parentId + " (Group) doesn't exist in org " + user.getOrganizationId());
				return bean;
			}
	
			if(!AuthorizationService.canViewGroup(user, dir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			Factories.getGroupFactory().populate(dir);	
			Factories.getGroupFactory().denormalize(dir);
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getUrn());
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			bean = dir;
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			AuditService.denyResult(audit, fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		}
		return bean;
	}
	
	@GET @Path("/getCreatePath/{type: [%\\sa-zA-Z_0-9\\-]+}/{path : [@\\.~%\\s0-9_a-zA-Z\\/\\-]+}")  @Produces(MediaType.APPLICATION_JSON)
	public BaseGroupType getCreatePath(@PathParam("type") String type, @PathParam("path") String path, @Context HttpServletRequest request){
		BaseGroupType bean = null;
		String sessionId = ServiceUtil.getSessionId(request);
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "GetCreate Group",AuditEnumType.SESSION,ServiceUtil.getSessionId(request));
		if(path == null || path.length() == 0){
			AuditService.denyResult(audit, "Path is null");
			return null;
		}

		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return null;

		try {
			/// Find first - this is done because the current factory layer will just build out the path
			/// And it needs to include an AuthZ check so someone can't just go making directory groups all over the place
			///
			///
			bean = (BaseGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.valueOf(type), path, user.getOrganizationId());
			if(bean == null && path.startsWith("~") == false && path.startsWith("/Home/" + user.getName() + "/") == false){
				AuditService.denyResult(audit, "Paths can only be created from the home directory");
				return null;
			}
			if(bean == null) bean = Factories.getGroupFactory().getCreatePath(user, path, user.getOrganizationId());
			if(bean != null){
				Factories.getGroupFactory().populate(bean);
				Factories.getGroupFactory().denormalize(bean);
				AuditService.permitResult(audit, "GetCreate group " + bean.getName());
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			AuditService.denyResult(audit, "Failed to create path with error " + e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			AuditService.denyResult(audit, "Failed to create path with error " + e.getMessage());
		}
		return bean;

	}
	@POST @Path("/add")  @Produces(MediaType.APPLICATION_JSON)
	public boolean add(BaseGroupType new_group, @Context HttpServletRequest request){
		return GroupServiceImpl.add(new_group, request);
		
	}
	@POST @Path("/update")  @Produces(MediaType.APPLICATION_JSON)
	public boolean update(BaseGroupType group, @Context HttpServletRequest request){
		return GroupServiceImpl.update(group, request);

	}
	@POST @Path("/updateDirectory")  @Produces(MediaType.APPLICATION_JSON)
	public boolean update(DirectoryGroupType group, @Context HttpServletRequest request){
		return GroupServiceImpl.update(group, request);

	}
	/// Jackson chokes on reconstituting the correct object when the base class is specified as the parameter type
	/// The REST parser chokes on overloading the path
	/// The backend code is mostly the same except for a processing fork, so the choice here is just define an alternate method name
	///
	@POST @Path("/deleteDirectory")  @Produces(MediaType.APPLICATION_JSON)
	public boolean deleteDirectory(DirectoryGroupType group, @Context HttpServletRequest request){
		return GroupServiceImpl.delete(group, request);
	}
	@POST @Path("/delete")  @Produces(MediaType.APPLICATION_JSON)
	public boolean delete(BaseGroupType group, @Context HttpServletRequest request){
		return GroupServiceImpl.delete(group, request);
		
	}
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}
