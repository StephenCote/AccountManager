package org.cote.rest.services;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.objects.OrganizationType;

@Path("/test")
public class TestService {

	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(TestService.class);
	
	@GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test(){
		return Response.status(200).entity(true).build();
	}
	
	@GET
	@Path("/test2")
	@Produces(MediaType.APPLICATION_JSON)
	public Response test2(){
		OrganizationType pub = Factories.getPublicOrganization();
		return Response.status(200).entity(pub).build();
	}
}
