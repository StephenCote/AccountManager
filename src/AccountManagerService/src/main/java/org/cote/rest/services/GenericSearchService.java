/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

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
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.TagFactory;
import org.cote.accountmanager.data.factory.TagParticipationFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.DataTagSearchRequest;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;

@DeclareRoles({"admin","user"})
@Path("/search/{type:[A-Za-z]+}")
public class GenericSearchService {

	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(GenericSearchService.class);
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	@RolesAllowed({"user"})
	@GET
	@Path("/{objectType:[A-Za-z]+}/{path:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findObject(@PathParam("type") String type, @PathParam("objectType") String objectType, @PathParam("path") String path, @Context HttpServletRequest request){
		logger.info("Request to find object from: " + type + " " + path);
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		if(path.startsWith("~") == false && path.startsWith(".") == false){
			path = "/" + path;
			/// Doubled up to allow for actual punctuation use
			/// Clearly this is a bandaid
			///
			if(path.contains("..")) path = path.replaceAll("\\.\\.", "/");
			else path = path.replace('.', '/');
			logger.info("Alt path: " + path);
		}
		
		
		Object obj = null;
		if(auditType.equals(AuditEnumType.ORGANIZATION)){
			UserType user = ServiceUtil.getUserFromSession(request);
			AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "GenericSearchService", AuditEnumType.USER, user.getUrn());
			AuditService.targetAudit(audit, auditType, path);
			try {
				obj = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(path);
			} catch (FactoryException | ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}
			if(obj != null){
				AuditService.permitResult(audit, "Global permit to find organization");
			}
			else{
				AuditService.denyResult(audit, "Organization not found from path '" + path + "'");
			}
		}
		else{
			obj = BaseService.find(auditType, objectType, path, request);
		}
		return Response.status(200).entity(obj).build();
	}
	
	@RolesAllowed({"user"})
	@POST
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<DataType> findByTag(DataTagSearchRequest searchRequest,@Context HttpServletRequest request){

		UserType user = ServiceUtil.getUserFromSession(request);
		logger.warn("AuthZ Not Implemented Yet For listByTags");
		logger.info("Searching for " + searchRequest.getRecordCount() + " data items starting at " + searchRequest.getStartRecord() + " having " + searchRequest.getTags().size() + " tags");
		List<DataType> list = new ArrayList<DataType>();
		try {
			list = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getDataForTags(searchRequest.getTags().toArray(new BaseTagType[0]), searchRequest.getStartRecord(), searchRequest.getRecordCount(), user.getOrganizationId());
			if(searchRequest.getPopulateGroup()){
				logger.info("Pre-populating referenced groups");
				for(int i = 0; i < list.size();i++){
					//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(list.get(i).getGroupId());
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).normalize(list.get(i));
				}
			}
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		logger.info("Returning " + list.size() + " items");
		return list;
		

	}
	
	@RolesAllowed({"user"})
	@POST
	@Path("/tags/count")
	@Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int countByTag(DataTagSearchRequest searchRequest,@Context HttpServletRequest request){
		int count = 0;
		UserType user = ServiceUtil.getUserFromSession(request);
		logger.warn("AuthZ Not Implemented Yet For countByTags");
		logger.info("Counting for " + searchRequest.getTags().size() + " tags");

		try {
			count = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).countTagParticipations(searchRequest.getTags().toArray(new BaseTagType[0]), ParticipantEnumType.UNKNOWN);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		logger.info("Returning " + count + " count");
		return count;
		

	}

}
