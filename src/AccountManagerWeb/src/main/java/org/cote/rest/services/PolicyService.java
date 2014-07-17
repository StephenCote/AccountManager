package org.cote.rest.services;

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

import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.services.PolicyServiceImpl;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;


@Path("/policy")
public class PolicyService{
	private static SchemaBean schemaBean = null;
	
	public PolicyService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	@GET @Path("/count/{group:[~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("group") String group,@Context HttpServletRequest request){
		return PolicyServiceImpl.count(group, request);
	}
	@POST @Path("/delete") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean delete(PolicyType bean,@Context HttpServletRequest request){
		return PolicyServiceImpl.delete(bean, request);
	}
	
	@POST @Path("/add") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean add(PolicyType bean,@Context HttpServletRequest request){
		return PolicyServiceImpl.add(bean, request);
	}
	
	@POST @Path("/update") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean update(PolicyType bean,@Context HttpServletRequest request){
		return PolicyServiceImpl.update(bean, request);
	}
	@GET @Path("/read/{name: [%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public PolicyType read(@PathParam("name") String name,@Context HttpServletRequest request){
		return PolicyServiceImpl.read(name, request);
	}
	@GET @Path("/readByGroupId/{groupId:[0-9]+}/{name: [%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public PolicyType readByGroupId(@PathParam("name") String name,@PathParam("groupId") long groupId,@Context HttpServletRequest request){
		return PolicyServiceImpl.readByGroupId(groupId, name, request);
	}	
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public PolicyType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		return PolicyServiceImpl.readById(id, request);
	}

	@POST @Path("/evaluate") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public PolicyResponseType evaluate(PolicyRequestType polRequest, @Context HttpServletRequest request){
		return PolicyServiceImpl.evaluatePolicy(polRequest, request);
	}
	
	@GET @Path("/define/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public PolicyDefinitionType define(@PathParam("id") long id,@Context HttpServletRequest request){
		return PolicyServiceImpl.getPolicyDefinition(id, request);
	}

	
	@GET @Path("/list") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<PolicyType> list(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PolicyServiceImpl.getGroupList(user, PolicyServiceImpl.defaultDirectory, 0,0 );

	}
	@GET @Path("/listInGroup/{path : [~%\\s0-9a-zA-Z\\/]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	
	public List<PolicyType> listInGroup(@PathParam("path") String path,@PathParam("startIndex") int startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PolicyServiceImpl.getGroupList(user, path, startIndex, recordCount );

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
		AuditService.targetAudit(audit, AuditEnumType.POLICY, "Policy Factory");
		Factories.getFactFactory().clearCache();
		AuditService.permitResult(audit,user.getName() + " flushed Policy Factory cache");
		return true;
	}	
	
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

}