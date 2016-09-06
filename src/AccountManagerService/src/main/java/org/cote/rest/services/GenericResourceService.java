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

@Path("/{type:[A-Za-z]+}")
public class GenericResourceService {
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(GenericResourceService.class);
	
	@GET
	@Path("/{objectId:[A-Za-z\\-]+")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObject(){
		BaseService.rea
		return Response.status(200).entity(true).build();
	}
}
