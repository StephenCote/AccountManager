package org.cote.rest.services;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.SessionSecurity;

import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.SessionBean;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;
import org.cote.util.BeanUtil;

@Path("/session")
public class SessionService{


	private static SchemaBean schemaBean = null;	
	public SessionService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();

	}
	
	@GET @Path("/getSession") @Produces(MediaType.APPLICATION_JSON)
	public SessionBean getSession(@Context HttpServletRequest request){

		String sessionId = request.getSession(true).getId();
		UserSessionType session = null;
		
		try {
			session = SessionSecurity.getUserSession(sessionId, ServiceUtil.getOrganizationFromRequest(request));

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		

		return BeanUtil.getSessionBean(session, sessionId);
	}
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}