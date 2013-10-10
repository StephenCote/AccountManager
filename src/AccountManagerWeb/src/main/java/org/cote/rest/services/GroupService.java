package org.cote.rest.services;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;

import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.services.BaseService;
import org.cote.accountmanager.services.DataServiceImpl;
import org.cote.accountmanager.services.RoleServiceImpl;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.ServiceUtil;

import org.cote.beans.MessageBean;
import org.cote.beans.SessionBean;
import org.cote.beans.SchemaBean;

import org.cote.rest.schema.ServiceSchemaBuilder;
import org.cote.util.BeanUtil;
import org.cote.util.RegistrationUtil;

@Path("/group")
public class GroupService {

	public static final Logger logger = Logger.getLogger(GroupService.class.getName());
	private static SchemaBean schemaBean = null;	
	public GroupService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();

	}
	
	@GET @Path("/count/{group:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("group") String path,@Context HttpServletRequest request){
		int out_count = 0;
		String sessionId = request.getSession(true).getId();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "Count Group",AuditEnumType.SESSION,request.getSession(true).getId());

		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return 0;

		try{
			DirectoryGroupType group = Factories.getGroupFactory().findGroup(user, path, user.getOrganization());
			if(group == null ){
				AuditService.denyResult(audit, "Group not found");
				return 0;
			}
			AuditService.targetAudit(audit, AuditEnumType.GROUP, group.getName() + "(#" + group.getId() + ")");
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
			if(org != null) group = Factories.getGroupFactory().getGroupById(groupId, org);
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
			if(org != null) group = Factories.getGroupFactory().getGroupById(groupId, org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(group != null) out_bool = BaseService.authorizeRole(AuditEnumType.GROUP, org, roleId, group, view, edit, delete, create, request);
		return out_bool;
	}
	
	@GET @Path("/authorizeUser/{organizationId:[\\d]+}/{userId:[\\d]+}/{groupId:[\\d]+}/{view:(true|false)}/{edit:(true|false)}/{delete:(true|false)}/{create:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean authorizeUser(@PathParam("organizationId") long organizationId,@PathParam("userId") long userId,@PathParam("groupId") long groupId,@PathParam("view") boolean view,@PathParam("edit") boolean edit,@PathParam("delete") boolean delete,@PathParam("create") boolean create,@Context HttpServletRequest request){
		boolean out_bool = false;

		NameIdType group = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(org != null) group = Factories.getGroupFactory().getGroupById(groupId, org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(group != null) out_bool = BaseService.authorizeUser(AuditEnumType.GROUP, org, userId, group, view, edit, delete, create, request);
		return out_bool;
	}
	
	@GET @Path("/home") @Produces(MediaType.APPLICATION_JSON)
	public DirectoryGroupType home(@Context HttpServletRequest request){
		DirectoryGroupType bean = null;
		String sessionId = request.getSession(true).getId();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "home",AuditEnumType.SESSION,request.getSession(true).getId());
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return bean;

		try{
			Factories.getUserFactory().populate(user);
			///DirectoryGroupType clone = BeanUtil.getBean(DirectoryGroupType.class, user.getHomeDirectory());
			///user.setHomeDirectory(clone);
			Factories.getGroupFactory().populate(user.getHomeDirectory());

			AuditService.targetAudit(audit, AuditEnumType.GROUP, user.getHomeDirectory().getName() + " (#" + user.getHomeDirectory().getId() + ")");
			AuditService.permitResult(audit, "Access authenticated user's home group");
			bean = BeanUtil.getSanitizedGroup(user.getHomeDirectory(),false);
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
	
	/// NOTE: This is the same as read, but is left as 'cd' for OS familiarity
	/// But needs to be made consistent with the model used in the Rocket API.
	///
	@GET @Path("/cd/{path : [@\\.\\.~%\\s0-9a-z_A-Z\\/\\-]+}")  @Produces(MediaType.APPLICATION_JSON)
	public DirectoryGroupType cd(@PathParam("path") String path,@Context HttpServletRequest request){
		DirectoryGroupType bean = null;
		if(path == null || path.length() == 0) path = "~";
		if(path.startsWith("~") == false && path.startsWith("/") == false) path = "/" + path;
		//logger.error("Path = '" + path + "'");
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.SESSION,request.getSession(true).getId());
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return bean;
		try {
			DirectoryGroupType dir = Factories.getGroupFactory().findGroup(user, path, user.getOrganization());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path");
				return bean;
			}
			if(AuthorizationService.canViewGroup(user, dir) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			Factories.getGroupFactory().populate(dir);	
			/// Work with a clone of the group because if it's cached, don't null out the cached copy's version
			dir = BeanUtil.getBean(DirectoryGroupType.class,dir);

			//Factories.getGroupFactory().get
			
			Factories.getGroupFactory().populateSubDirectories(dir);
			for(int i = 0; i < dir.getSubDirectories().size();i++){
				Factories.getGroupFactory().populate(dir.getSubDirectories().get(i));
			}
			
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			
			bean = BeanUtil.getSanitizedGroup(dir,false);
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;
	}

		/// TODO - dir should return a list, not a single group
		/// NOTE - this is the same as 'listInGroup', but is left as 'dir' for familiarity
		/// Change this to CD, and then make dir use CD to obtain the parent.
		@GET @Path("/dir/{path : [\\.~%\\s0-9a-z_A-Z\\/\\-]+}")  @Produces(MediaType.APPLICATION_JSON)
		public DirectoryGroupType[] dir(@PathParam("path") String path,@Context HttpServletRequest request){
			DirectoryGroupType dir = cd(path, request);

			try {
				//Factories.getGroupFactory().populate(dir);
				Factories.getGroupFactory().populateSubDirectories(dir);
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			
			return BeanUtil.getSanitizedGroups((dir == null? new DirectoryGroupType[0] : dir.getSubDirectories().toArray(new DirectoryGroupType[0])));

		}
	
	
	/// TODO - dir should return a list, not a single group
	/// NOTE - this is the same as 'listInGroup', but is left as 'dir' for familiarity
	/// Change this to CD, and then make dir use CD to obtain the parent.
	@GET @Path("/listInGroup/{path : [@\\.~%\\s0-9a-z_A-Z\\/\\-]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}")  @Produces(MediaType.APPLICATION_JSON)
	public DirectoryGroupType[] listInGroup(@PathParam("path") String path,@PathParam("startIndex") int startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		DirectoryGroupType dir = cd(path, request);
		List<DirectoryGroupType> dirs = new ArrayList<DirectoryGroupType>();
		if(dir == null) return dirs.toArray(new DirectoryGroupType[0]);
		try {
			dirs = Factories.getGroupFactory().getDirectoryListByParent(dir,  startIndex, recordCount, dir.getOrganization());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return dirs.toArray(new DirectoryGroupType[0]);
		/*
		try {
			//Factories.getGroupFactory().populate(dir);
			Factories.getGroupFactory().populateSubDirectories(dir);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return BeanUtil.getSanitizedGroups((dir == null? new DirectoryGroupType[0] : dir.getSubDirectories().toArray(new DirectoryGroupType[0])));
		*/
	}

	//@GET @Path("/dir/{parentId : [0-9]+}/{name : [%\\sa-zA-Z_0-9\\-]+}")  @Produces(MediaType.APPLICATION_JSON)
	@GET @Path("/find/{parentId : [0-9]+}")  @Produces(MediaType.APPLICATION_JSON)
	public DirectoryGroupType find(@PathParam("parentId") long parentId,@Context HttpServletRequest request){
		DirectoryGroupType bean = null;
		String sessionId = request.getSession(true).getId();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "#" + parentId,AuditEnumType.SESSION,request.getSession(true).getId());
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return bean;
		if(parentId <= 0){
			AuditService.denyResult(audit, "Invalid parent id: " + parentId);
			return bean;
		}
		try{
			DirectoryGroupType dir = Factories.getGroupFactory().getById(parentId, user.getOrganization());
			if(dir == null){
				AuditService.denyResult(audit, "Id " + parentId + " doesn't exist in org " + user.getOrganization().getId());
				return bean;
			}
	
			if(!AuthorizationService.canViewGroup(user, dir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			Factories.getGroupFactory().populate(dir);	
			
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			bean = BeanUtil.getSanitizedGroup(dir,false);
			
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
	@GET @Path("/getCreatePath/{path : [@\\.~%\\s0-9_a-zA-Z\\/\\-]+}")  @Produces(MediaType.APPLICATION_JSON)
	public DirectoryGroupType getCreatePath(@PathParam("path") String path, @Context HttpServletRequest request){
		DirectoryGroupType bean = null;
		String sessionId = request.getSession(true).getId();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "GetCreate Group",AuditEnumType.SESSION,request.getSession(true).getId());
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
			bean = Factories.getGroupFactory().findGroup(user, path, user.getOrganization());
			if(bean == null && path.startsWith("~") == false && path.startsWith("/Home/" + user.getName() + "/") == false){
				AuditService.denyResult(audit, "Paths can only be created from the home directory");
				return null;
			}
			if(bean == null) bean = Factories.getGroupFactory().getCreatePath(user, path, user.getOrganization());
			if(bean != null){
				Factories.getGroupFactory().populate(bean);
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
	public DirectoryGroupType add(DirectoryGroupType new_group, @Context HttpServletRequest request){
		DirectoryGroupType bean = null;
		String sessionId = request.getSession(true).getId();
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "New Group",AuditEnumType.SESSION,request.getSession(true).getId());
		if(new_group == null || new_group.getParentId() == null || new_group.getParentId() <= 0 || new_group.getName() == null){
			AuditService.denyResult(audit, "Group name or parent not specified");
			return null;
		}
		AuditService.targetAudit(audit, AuditEnumType.GROUP, new_group.getName() + " to parent (#" + new_group.getParentId() + ")");
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return null;

		try{
			DirectoryGroupType pdir = Factories.getGroupFactory().getById(new_group.getParentId(), user.getOrganization());
			if(pdir == null){
				AuditService.denyResult(audit, "Parent group (#" + new_group.getParentId() + ") doesn't exist in organization " + user.getOrganization().getName() + " (#" + user.getOrganization().getId() + ")");
				return null;
			}
			if(!AuthorizationService.canCreateGroup(user, pdir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to create in group " + pdir.getName() + " (#" + pdir.getId() + ")");
				return null;
			}
			DirectoryGroupType new_dir = null;
			new_dir = Factories.getGroupFactory().newDirectoryGroup(user,new_group.getName(), pdir, user.getOrganization());

			if(Factories.getGroupFactory().addGroup(new_dir)){
				new_dir = Factories.getGroupFactory().getDirectoryByName(new_group.getName(), pdir, user.getOrganization());
				Factories.getGroupFactory().populate(new_dir);			
				AuditService.targetAudit(audit, AuditEnumType.GROUP, new_dir.getName() + " (#" + new_dir.getId() + ")");
				AuditService.permitResult(audit, "Create authorized for group " + new_dir.getName());
				bean = BeanUtil.getSanitizedGroup(new_dir,false);
			}
			else{
				AuditService.denyResult(audit, "Failed to create group " + new_dir.getName() + " in parent " + pdir.getName() + " (#" + pdir.getId() + ")");
				return null;
			}

			
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
	@POST @Path("/update")  @Produces(MediaType.APPLICATION_JSON)
	public boolean update(DirectoryGroupType group, @Context HttpServletRequest request){
		boolean out_bool = false;
		String sessionId = request.getSession(true).getId();
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Update Group",AuditEnumType.SESSION,request.getSession(true).getId());
		if(group == null || group.getParentId() == null || group.getParentId() <= 0 || group.getName() == null){
			AuditService.denyResult(audit, "Group name or parent not specified");
			return false;
		}
		if(group.getParentId() == group.getId()){
			AuditService.denyResult(audit, "Cannot parent group to itself");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.GROUP, group.getName() + " to parent (#" + group.getParentId() + ")");
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return false;

		try{
			DirectoryGroupType edir = Factories.getGroupFactory().getDirectoryById(group.getId(), user.getOrganization());
			DirectoryGroupType opdir = Factories.getGroupFactory().getById(edir.getParentId(), user.getOrganization());
			DirectoryGroupType pdir = Factories.getGroupFactory().getById(group.getParentId(), user.getOrganization());
			if(opdir == null){
				AuditService.denyResult(audit, "Original Parent group (#" + edir.getParentId() + ") doesn't exist in organization " + user.getOrganization().getName() + " (#" + user.getOrganization().getId() + ")");
				return false;
			}
			if(pdir == null){
				AuditService.denyResult(audit, "Specified Parent group (#" + group.getParentId() + ") doesn't exist in organization " + user.getOrganization().getName() + " (#" + user.getOrganization().getId() + ")");
				return false;
			}
			if(opdir.getId() != pdir.getId() && !AuthorizationService.canCreateGroup(user, pdir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to create in group " + pdir.getName() + " (#" + pdir.getId() + ")");
				return false;
			}
			if(!AuthorizationService.canChangeGroup(user, edir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to change group " + edir.getName() + " (#" + edir.getId() + ")");
				return false;
			}
			
			Factories.getGroupFactory().removeFromCache(edir);
			if(Factories.getGroupFactory().updateGroup(group)){
				
				out_bool = true;
				AuditService.permitResult(audit, "Update authorized for group " + group.getName());
			}
			else{
				AuditService.denyResult(audit, "Failed to update group " + group.getName() + " in parent " + pdir.getName() + " (#" + pdir.getId() + ")");
			}

			
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
		return out_bool;
	}
	@POST @Path("/delete")  @Produces(MediaType.APPLICATION_JSON)
	public boolean delete(DirectoryGroupType group, @Context HttpServletRequest request){
		boolean out_bool = false;
		String sessionId = request.getSession(true).getId();
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "Delete Group",AuditEnumType.SESSION,request.getSession(true).getId());
		if(group == null || group.getId() == null || group.getId() <= 0 ){
			AuditService.denyResult(audit, "Group id not specified");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.GROUP, "#" + group.getId() );
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return false;
		try{
			DirectoryGroupType edir = Factories.getGroupFactory().getDirectoryById(group.getId(), user.getOrganization());

			if(edir == null){
				AuditService.denyResult(audit, "Original  group (#" + edir.getParentId() + ") doesn't exist in organization " + user.getOrganization().getName() + " (#" + user.getOrganization().getId() + ")");
				return false;
			}
			AuditService.targetAudit(audit, AuditEnumType.GROUP, group.getName() + " in parent (#" + group.getParentId() + ")");
			
			if(!AuthorizationService.canDeleteGroup(user, edir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to delete group " + edir.getName() + " (#" + edir.getId() + ")");
				return false;
			}
			
			if(Factories.getGroupFactory().deleteDirectoryGroup(edir)){
				
				out_bool = true;
				AuditService.permitResult(audit, "Delete authorized for group " + group.getName());
			}
			else{
				AuditService.denyResult(audit, "Failed to delete group " + group.getName() + " in parent #" + edir.getId());
			}

			
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
		return out_bool;
	}
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}
