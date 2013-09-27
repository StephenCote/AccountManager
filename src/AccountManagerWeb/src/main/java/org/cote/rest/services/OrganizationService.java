package org.cote.rest.services;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.services.DataServiceImpl;

import org.cote.beans.SchemaBean;
//import org.cote.beans.UserBean;
import org.cote.rest.schema.ServiceSchemaBuilder;
import org.cote.util.BeanUtil;


@Path("/organization")
public class OrganizationService{

	public static final Logger logger = Logger.getLogger(OrganizationService.class.getName());
	private static SchemaBean schemaBean = null;	
	public OrganizationService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();

	}

	@GET @Path("/getPublic") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public OrganizationType getPublic(@Context HttpServletRequest request){
		return Factories.getPublicOrganization();
	}
	
	@GET @Path("/getRoot") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public OrganizationType getRoot(@Context HttpServletRequest request){
		return Factories.getRootOrganization();
	}
	
	@GET @Path("/getDevelopment") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public OrganizationType getDevelopment(@Context HttpServletRequest request){
		return Factories.getDevelopmentOrganization();
	}
	
	@GET @Path("/find/{path : [~%\\s0-9a-zA-Z\\/]+}")  @Produces(MediaType.APPLICATION_JSON)
	public OrganizationType find(@PathParam("path") String path,@Context HttpServletRequest request){
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().findOrganization(path);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return org;
	}

	@GET @Path("/read/{parentId:[\\d]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public OrganizationType read(@PathParam("parentId") long parentId,@PathParam("name") String name,@Context HttpServletRequest request){
		OrganizationType parent = null;
		OrganizationType out_org = null;
		try {
			parent = (parentId > 0 ? Factories.getOrganizationFactory().getOrganizationById(parentId) : Factories.getRootOrganization());
			if(parent != null){
				System.out.println("Looking for organization '" + name + "' in parent '" + parent.getName() + "'");
				out_org = Factories.getOrganizationFactory().getOrganizationByName(name, parent);
			}
			else{
				System.out.println("Parent organization not found for id #" + parentId);
			}
		
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return out_org;
	}
	
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public OrganizationType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		OrganizationType out_org = null;
		try {
			out_org = Factories.getOrganizationFactory().getOrganizationById(id);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return out_org;
	}



	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}