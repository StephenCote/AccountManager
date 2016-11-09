package org.cote.rest.services;

import javax.ws.rs.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/generic")
public class GenericService {
	public static final Logger logger = LogManager.getLogger(GenericService.class);
	/*
	@POST @Path("/add/{type: [a-zA-Z0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public <T> boolean addType(T bean,@PathParam("type") Class<T> classZ,@Context HttpServletRequest request){
		logger.info("Received request");
		return false;
	}
	
	*/
	/*
	@GET @Path("/get/{type: [a-zA-Z0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public GenericType getType(@Context HttpServletRequest request){
		// @PathParam("type") Class<T> classZ,
		logger.info("Received request");
		return null;
	}
	*/

}
