/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.rest.services;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;


@Path("/organization")
public class OrganizationService{

	public static final Logger logger = LogManager.getLogger(OrganizationService.class);
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
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(path);
			if(org != null) ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).denormalize(org);
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		}
		return org;
	}

	@GET @Path("/read/{parentId:[\\d]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public OrganizationType read(@PathParam("parentId") long parentId,@PathParam("name") String name,@Context HttpServletRequest request){
		OrganizationType parent = null;
		OrganizationType out_org = null;
		try {
			parent = (parentId > 0 ? ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(parentId) : Factories.getRootOrganization());
			if(parent != null){
				System.out.println("Looking for organization '" + name + "' in parent '" + parent.getName() + "'");
				out_org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationByName(name, parent);
				if(out_org != null) ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).denormalize(out_org);
			}
			else{
				System.out.println("Parent organization not found for id #" + parentId);
			}
		
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		}

		return out_org;
	}
	
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public OrganizationType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		OrganizationType out_org = null;
		try {
			out_org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(id);
			if(out_org != null) ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).denormalize(out_org);
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
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