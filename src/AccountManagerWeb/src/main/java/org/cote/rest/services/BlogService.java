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
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.services.BlogServiceImpl;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;
import org.cote.util.ArticleUtil;

import java.util.List;

@Path("/blog")
public class BlogService{

	private static SchemaBean schemaBean = null;
	public static final Logger logger = Logger.getLogger(BlogService.class.getName());
	public BlogService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	
	@GET @Path("/getAuthorRole/{organizationId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public UserRoleType getAuthorRole(@PathParam("organizationId") long orgId,@Context HttpServletRequest request){
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(org == null) return null;
		return ArticleUtil.getRoleByType("Blog", org);
	}
	
	@GET @Path("/read/{organizationId:[\\d]+}/{user : [@\\.~%\\s0-9a-z_A-Z\\/\\-]+}/{name: [@%\\sa-zA-Z_0-9\\-\\.]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public DataType read(@PathParam("organizationId") long orgId, @PathParam("user") String userName, @PathParam("name") String name,@Context HttpServletRequest request){
		return BlogServiceImpl.read(orgId, userName, name);
	}

	@GET @Path("/list/{organizationId:[\\d]+}/{user : [@\\.~%\\s0-9a-z_A-Z\\/\\-]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<DataType> list(@PathParam("organizationId") long orgId, @PathParam("user") String userName,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		return BlogServiceImpl.list(orgId, userName, true, startIndex,recordCount);
	}
	
	@GET @Path("/listFull/{organizationId:[\\d]+}/{user : [@\\.~%\\s0-9a-z_A-Z\\/\\-]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<DataType> listFull(@PathParam("organizationId") long orgId, @PathParam("user") String userName,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		return BlogServiceImpl.list(orgId, userName, false, startIndex,recordCount);
	}
	
	
	
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

}