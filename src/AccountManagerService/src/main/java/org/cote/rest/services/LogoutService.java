package org.cote.rest.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/logout")
public class LogoutService {
	private static final Logger logger = LogManager.getLogger(LogoutService.class);
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(@Context HttpServletRequest request){
		boolean out_bool = false;
		try{
			request.logout();
			request.getSession().invalidate();
			out_bool = true;
		}
		catch(Exception e){
			
		}
		return Response.status(200).entity(out_bool).build();
	}
	
}
