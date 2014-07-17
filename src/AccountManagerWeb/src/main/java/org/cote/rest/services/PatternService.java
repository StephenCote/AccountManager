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
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.services.PatternServiceImpl;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;


@Path("/pattern")
public class PatternService{
	private static SchemaBean schemaBean = null;
	
	public PatternService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	@GET @Path("/count/{group:[~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("group") String group,@Context HttpServletRequest request){
		return PatternServiceImpl.count(group, request);
	}
	@POST @Path("/delete") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean delete(PatternType bean,@Context HttpServletRequest request){
		return PatternServiceImpl.delete(bean, request);
	}
	
	@POST @Path("/add") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean add(PatternType bean,@Context HttpServletRequest request){
		return PatternServiceImpl.add(bean, request);
	}
	
	@POST @Path("/update") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean update(PatternType bean,@Context HttpServletRequest request){
		return PatternServiceImpl.update(bean, request);
	}
	@GET @Path("/read/{name: [%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public PatternType read(@PathParam("name") String name,@Context HttpServletRequest request){
		return PatternServiceImpl.read(name, request);
	}
	@GET @Path("/readByGroupId/{groupId:[0-9]+}/{name: [%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public PatternType readByGroupId(@PathParam("name") String name,@PathParam("groupId") long groupId,@Context HttpServletRequest request){
		return PatternServiceImpl.readByGroupId(groupId, name, request);
	}	
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public PatternType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		return PatternServiceImpl.readById(id, request);
	}
	
	@GET @Path("/list") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<PatternType> list(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PatternServiceImpl.getGroupList(user, PatternServiceImpl.defaultDirectory, 0,0 );

	}
	@GET @Path("/listInGroup/{path : [~%\\s0-9a-zA-Z\\/]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	
	public List<PatternType> listInGroup(@PathParam("path") String path,@PathParam("startIndex") int startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return PatternServiceImpl.getGroupList(user, path, startIndex, recordCount );

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
		AuditService.targetAudit(audit, AuditEnumType.PATTERN, "Pattern Factory");
		Factories.getFactFactory().clearCache();
		AuditService.permitResult(audit,user.getName() + " flushed Pattern Factory cache");
		return true;
	}	
	
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

}