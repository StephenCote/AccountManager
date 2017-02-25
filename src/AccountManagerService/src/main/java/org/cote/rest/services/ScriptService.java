package org.cote.rest.services;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;

@DeclareRoles({"admin","user"})
@Path("/script")
public class ScriptService {
	
	private static final Logger logger = LogManager.getLogger(ScriptService.class);
	private static SchemaBean schemaBean = null;
	private static ICommunityProvider provider = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;

	private ICommunityProvider getProvider(){
		if(provider != null) return provider;
		String pcls = context.getInitParameter("factories.community");
		try {
			logger.info("Initializing community provider " + pcls);
			Class cls = Class.forName(pcls);
			ICommunityProvider f = (ICommunityProvider)cls.newInstance();
			provider = f;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.error("Trace", e);
		}
		
		return provider;
	}
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	
	
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/community/exec/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response executeCommunityProjectScript(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("name") String name, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		Object report = null;
		if(cp != null) report = cp.executeCommunityProjectScript(user, communityId, projectId, name);
		return Response.status(200).entity(report).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/community/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityProjectScript(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("name") String name, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		String report = null;
		if(cp != null) report = cp.getCommunityProjectScript(user, communityId, projectId, name);
		return Response.status(200).entity(report).build();
	}
	
	@RolesAllowed({"admin","user"})
	@POST
	@Path("/community/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response updateCommunityProjectScript(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("name") String name, String dataStr,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean out_bool = false;
		if(cp != null) out_bool = cp.updateCommunityProjectScript(user, communityId, projectId, name, dataStr);
		return Response.status(200).entity(out_bool).build();
	}

}
