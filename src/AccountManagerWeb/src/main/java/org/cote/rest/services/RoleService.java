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
import org.cote.accountmanager.data.services.SessionSecurity;

import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.services.DataServiceImpl;
import org.cote.accountmanager.services.RoleServiceImpl;
import org.cote.accountmanager.services.RoleServiceImpl;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.MessageBean;
import org.cote.beans.SessionBean;
import org.cote.beans.SchemaBean;
//import org.cote.beans.UserBean;
import org.cote.rest.schema.ServiceSchemaBuilder;
import org.cote.util.BeanUtil;
import org.cote.util.RegistrationUtil;

@Path("/role")
public class RoleService{

	public static final Logger logger = Logger.getLogger(RoleService.class.getName());
	private static SchemaBean schemaBean = null;	
	public RoleService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();

	}

	@GET @Path("/authorizeUser/{organizationId:[\\d]+}/{userId:[\\d]+}/{roleId:[\\d]+}/{view:(true|false)}/{edit:(true|false)}/{delete:(true|false)}/{create:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean authorizeUser(@PathParam("organizationId") long organizationId,@PathParam("userId") long userId,@PathParam("roleId") long roleId,@PathParam("view") boolean view,@PathParam("edit") boolean edit,@PathParam("delete") boolean delete,@PathParam("create") boolean create,@Context HttpServletRequest request){
		return RoleServiceImpl.authorizeUser(organizationId, userId, roleId, view, edit, delete, create, request);
	}
	@GET @Path("/count/{organizationId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("organizationId") long organizationId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){
		return RoleServiceImpl.count(organizationId,request);
	}	
	@GET @Path("/countInParent/{organizationId:[\\d]+}/{parentId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int countInParent(@PathParam("organizationId") long organizationId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){
		return RoleServiceImpl.countInParent(organizationId, parentId,request);
	}
	@POST @Path("/delete") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean delete(BaseRoleType bean,@Context HttpServletRequest request){
		return RoleServiceImpl.delete(bean, request);
	}
	
	@POST @Path("/add") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean add(BaseRoleType bean,@Context HttpServletRequest request){
		return RoleServiceImpl.add(bean, request);
	}
	
	@POST @Path("/update") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean update(BaseRoleType bean,@Context HttpServletRequest request){
		return RoleServiceImpl.update(bean, request);
	}
	
	@GET @Path("/getUserRole") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType read(@Context HttpServletRequest request){
		return RoleServiceImpl.getUserRole(ServiceUtil.getOrganizationFromRequest(request),request);
	}
	
	@GET @Path("/read/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType read(@PathParam("name") String name,@Context HttpServletRequest request){
		return RoleServiceImpl.readByOrganizationId(ServiceUtil.getOrganizationFromRequest(request).getId(),name, request);
	}
	@GET @Path("/readByOrganizationId/{orgId:[0-9]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType readByOrganizationId(@PathParam("name") String name,@PathParam("orgId") long orgId,@Context HttpServletRequest request){
		return RoleServiceImpl.readByOrganizationId(orgId, name, request);
	}
	@GET @Path("/readByParentId/{orgId: [0-9]+}/{parentId:[0-9]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType readByParentId(@PathParam("name") String name,@PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){

		return RoleServiceImpl.readByParent(orgId, parentId, name, request);
	}
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		return RoleServiceImpl.readById(id, request);
	}

	@GET @Path("/listGroups/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserGroupType> listGroups(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		UserRoleType targRole = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			targRole = Factories.getRoleFactory().getById(recordId, targOrg);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return RoleServiceImpl.getListOfGroups(user, targRole);
	}
	
	@GET @Path("/listUsers/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserType> listUsers(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		UserRoleType targRole = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			targRole = Factories.getRoleFactory().getById(recordId, targOrg);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return RoleServiceImpl.getListOfUsers(user, targRole);
	}
	
	@GET @Path("/listForUser/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserRoleType> listForUser(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		UserType targUser = null;
		OrganizationType targOrg = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targUser = Factories.getUserFactory().getById(recordId, targOrg);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(targUser == null){
			logger.error("Null user specified for org id " + orgId + " and user id " + recordId);
			return new ArrayList<UserRoleType>();
		}
		return RoleServiceImpl.getListForUser(user, targUser);
	}
	
	@GET @Path("/list") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseRoleType> list(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return RoleServiceImpl.getListInOrganization(user, user.getOrganization(),0,0);
	}
	@GET @Path("/listInParent/{orgId : [\\d]+}/{parentId : [\\d]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseRoleType> listInParent(@PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@PathParam("startIndex") int startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		BaseRoleType parent = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getById(orgId, null);
			if(org != null) parent =Factories.getRoleFactory().getById(parentId, org);
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
			System.out.println("Null role for id " + parentId + " in org " + org);
			return new ArrayList<BaseRoleType>();
		}
		return RoleServiceImpl.getListInParent(user, parent, startIndex, recordCount );

	}
	@GET @Path("/listInOrganization/{orgId : [\\d]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseRoleType> listInOrganization(@PathParam("orgId") long orgId,@PathParam("startIndex") int startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		BaseRoleType parent = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getById(orgId, null);

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(org == null){
			return new ArrayList<BaseRoleType>();
		}
		return RoleServiceImpl.getListInOrganization(user, org, startIndex, recordCount );

	}
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}