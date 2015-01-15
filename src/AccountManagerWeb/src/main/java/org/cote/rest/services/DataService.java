package org.cote.rest.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.services.BaseService;
import org.cote.accountmanager.services.DataServiceImpl;
import org.cote.accountmanager.services.RoleServiceImpl;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;






import java.util.ArrayList;
import java.util.List;

@Path("/data")
public class DataService{
	private static SchemaBean schemaBean = null;
	public static final Logger logger = Logger.getLogger(DataService.class.getName());
	public DataService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	@POST @Path("/updateProfile") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean updateProfile(DataType data, @Context HttpServletRequest request){
		boolean out_bool = false;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "getProfile",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, AuditEnumType.DATA, "Read profile information");
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null){
			AuditService.denyResult(audit, "Deny for anonymous user");
			return false;
		}
		try{
		Factories.getUserFactory().populate(user);
		if(data.getOwnerId() != user.getId() || data.getGroup().getId() != user.getHomeDirectory().getId() || data.getName().equals(".profile") == false){
			AuditService.denyResult(audit, "Profile data is not the right name, owner, or in the right group");
			return false;
		}
		if(Factories.getDataFactory().updateData(data) && Factories.getAttributeFactory().updateAttributes(data)){
			AuditService.permitResult(audit, "Updated profile information with " + data.getAttributes().size() + " attributes");
			out_bool = true;
		}
		else{
			AuditService.denyResult(audit, "Failed to update profile information");
		}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
			AuditService.denyResult(audit,e.getMessage());

		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			AuditService.denyResult(audit,e.getMessage());

		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			AuditService.denyResult(audit,e.getMessage());

		}
		return out_bool;
	}
	@GET @Path("/getProfile") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public DataType getProfile(@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "getProfile",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, AuditEnumType.DATA, "Read profile information");
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null){
			AuditService.denyResult(audit, "Deny for anonymous user");
			return null;
		}
		return DataServiceImpl.getProfile(user,audit);
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
		AuditService.targetAudit(audit, AuditEnumType.DATA, "Data Factory");
		Factories.getDataFactory().clearCache();
		AuditService.permitResult(audit,user.getName() + " flushed Data Factory cache");
		return true;
	}
	
	@GET @Path("/count/{group:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("group") String group,@Context HttpServletRequest request){
		return DataServiceImpl.count(group, request);
	}
	
	@GET @Path("/listAuthorizedRoles/{organizationId:[\\d]+}/{dataId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseRoleType> listAuthorizedRoles(@PathParam("organizationId") long organizationId,@PathParam("dataId") long dataId, @Context HttpServletRequest request){
		List<BaseRoleType> roles = new ArrayList<BaseRoleType>();
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user == null){
			return roles;
		}
		DataType data = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(org != null) data = Factories.getDataFactory().getDataById(dataId, true, org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		
		if(data != null) roles = RoleServiceImpl.getListOfRoles(user, data);
		return roles;
	}
	
	@GET @Path("/authorizeRole/{organizationId:[\\d]+}/{roleId:[\\d]+}/{dataId:[\\d]+}/{view:(true|false)}/{edit:(true|false)}/{delete:(true|false)}/{create:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean authorizeRole(@PathParam("organizationId") long organizationId,@PathParam("roleId") long roleId,@PathParam("dataId") long dataId,@PathParam("view") boolean view,@PathParam("edit") boolean edit,@PathParam("delete") boolean delete,@PathParam("create") boolean create,@Context HttpServletRequest request){
		boolean out_bool = false;

		NameIdType data = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(org != null) data = Factories.getDataFactory().getDataById(dataId, org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(data != null) out_bool = BaseService.authorizeRole(AuditEnumType.DATA, org, roleId, data, view, edit, delete, create, request);
		return out_bool;
	}
	
	@GET @Path("/authorizeUser/{organizationId:[\\d]+}/{userId:[\\d]+}/{dataId:[\\d]+}/{view:(true|false)}/{edit:(true|false)}/{delete:(true|false)}/{create:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean authorizeUser(@PathParam("organizationId") long organizationId,@PathParam("userId") long userId,@PathParam("dataId") long dataId,@PathParam("view") boolean view,@PathParam("edit") boolean edit,@PathParam("delete") boolean delete,@PathParam("create") boolean create,@Context HttpServletRequest request){
		boolean out_bool = false;

		NameIdType data = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(org != null) data = Factories.getDataFactory().getDataById(dataId, org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(data != null) out_bool = BaseService.authorizeUser(AuditEnumType.DATA, org, userId, data, view, edit, delete, create, request);
		return out_bool;
	}
	
	
	@POST @Path("/delete") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean delete(DataType bean,@Context HttpServletRequest request){
		return DataServiceImpl.delete(bean, request);
	}
	
	@POST @Path("/add") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean add(DataType bean,@Context HttpServletRequest request){
		return DataServiceImpl.add(bean, request);
	}
	
	@POST @Path("/addFeedback") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean addFeedback(DataType bean,@Context HttpServletRequest request){
		return DataServiceImpl.addFeedback(bean, request);
	}

	
	@POST @Path("/update") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean update(DataType bean,@Context HttpServletRequest request){
		return DataServiceImpl.update(bean, request);
	}
	@GET @Path("/read/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public DataType read(@PathParam("name") String name,@Context HttpServletRequest request){
		return DataServiceImpl.read(name, request);
	}
	@GET @Path("/readByGroupId/{groupId:[0-9]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public DataType readByGroupId(@PathParam("name") String name,@PathParam("groupId") long groupId,@Context HttpServletRequest request){
		return DataServiceImpl.readByGroupId(groupId, name, request);
	}	
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public DataType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		return DataServiceImpl.readById(id, request);
	}
	
	@GET @Path("/list") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<DataType> list(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return DataServiceImpl.getGroupList(user, null,true,DataServiceImpl.defaultDirectory,0,0);

	}
	@GET @Path("/listInGroup/{path : [@\\.~%\\s0-9a-z_A-Z\\/\\-]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	
	public List<DataType> listInGroup(@PathParam("path") String path,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return DataServiceImpl.getGroupList(user, instruction,true,path, startIndex, recordCount );
	}
	
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

}