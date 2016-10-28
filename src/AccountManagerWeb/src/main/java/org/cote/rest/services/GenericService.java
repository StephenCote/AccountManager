package org.cote.rest.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
//import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.services.AccountServiceImpl;

@Path("/generic")
public class GenericService {
	public static final Logger logger = Logger.getLogger(GenericService.class.getName());
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
