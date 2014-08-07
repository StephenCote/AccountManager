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
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.services.PermissionServiceImpl;
import org.cote.accountmanager.services.RoleServiceImpl;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;


@Path("/permission")
public class PermissionService{
	private static SchemaBean schemaBean = null;
	public static final Logger logger = Logger.getLogger(PermissionService.class.getName());
	public PermissionService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	/*
	@GET @Path("/count/{group:[~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("group") String group,@Context HttpServletRequest request){
		return PermissionServiceImpl.count(group, request);
	}
	*/
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

	/*
	@GET @Path("/list") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BasePermissionType> list(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.getGroupList(user, PermissionServiceImpl.defaultDirectory, 0,0 );

	}
	@GET @Path("/listInGroup/{path : [~%\\s0-9a-zA-Z\\/]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BasePermissionType> listInGroup(@PathParam("path") String path,@PathParam("startIndex") int startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PermissionServiceImpl.getGroupList(user, path, startIndex, recordCount );

	}
	*/
	@GET @Path("/listInParent/{orgId : [\\d]+}/{parentId : [\\d]+}/{type : [~%\\s0-9a-zA-Z\\/]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BasePermissionType> listInParent(@PathParam("type") String type, @PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@PathParam("startIndex") int startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		BasePermissionType parent = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getById(orgId, null);
			if(org != null) parent =Factories.getPermissionFactory().getById(parentId, org);
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
		return PermissionServiceImpl.setPermissionOnGroupForUser(user,groupId,userId,permissionId, enable);
	}
	
	@GET @Path("/clearCache") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean flushCache(@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "clearCache",AuditEnumType.SESSION, request.getSession(true).getId());
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