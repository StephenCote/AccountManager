/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.ParticipationFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DataTagSearchRequest;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ParticipationSearchRequest;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;

/*
 *   List - /resources/{type}/{groupId}/{startIndex}/{count}
  ListInParent - /resources/{type}/{parentId}/{startIndex}/{count}
  Count - /resources/{type}/{parentId}/count
     ** Note: GROUP, PERMISSION, and ROLE use countInParent vs. countInGroup
 */

@DeclareRoles({"admin","user"})
@Path("/list/{type:[A-Za-z]+}")
public class GenericListService {
	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(GenericListService.class);
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	/*
	 * countMembers:
	 * 	type --- base type for the participation factory
	 * searchRequest.participationType --- the member type [unknown - all; or specific]
	 */
	
	@RolesAllowed({"user"})
	@POST
	@Path("/member/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response countMembers(@PathParam("type") String type, ParticipationSearchRequest searchRequest, @Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(type);

		int count = 0;
		if(searchRequest == null || searchRequest.getParticipations().size() == 0) {
			logger.warn("Null or empty request");
			return Response.status(200).entity(count).build();
		}
		UserType user = ServiceUtil.getUserFromSession(request);
		try{
			int canRead = 0;
			for(NameIdType obj : searchRequest.getParticipations()) {
				if(!obj.getNameType().toString().equals(type)) {
					logger.warn("Mixed participation types not supported");
					continue;
				}
				if(BaseService.canViewType(auditType, user, obj)) canRead++;
			}
			if(canRead != searchRequest.getParticipations().size()) {
				logger.error("One or more provided participations is not visible to the current user");
				return Response.status(200).entity(count).build();	
			}
			
			/// get the participation factory
			///
			IParticipationFactory pFact = Factories.getParticipationFactory(FactoryEnumType.valueOf(type + "PARTICIPATION"));

			count = pFact.countParticipations(searchRequest.getParticipations().toArray(new NameIdType[0]), searchRequest.getParticipantFactoryType());
			
		}
		catch(FactoryException | ArgumentException f){
			logger.error(f);
		}
		return Response.status(200).entity(count).build();
	}
	
	@RolesAllowed({"user"})
	@POST
	@Path("/member/list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listMembers(@PathParam("type") String type, ParticipationSearchRequest searchRequest, @Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		List<Object> objs = new ArrayList<>();
		if(searchRequest == null || searchRequest.getParticipations().size() == 0) {
			logger.warn("Null or empty request");
			return Response.status(200).entity(objs).build();
		}
		UserType user = ServiceUtil.getUserFromSession(request);
		try{
			int canRead = 0;
			for(NameIdType obj : searchRequest.getParticipations()) {
				if(!obj.getNameType().toString().equals(type)) {
					logger.warn("Mixed participation types not supported");
					continue;
				}
				if(BaseService.canViewType(auditType, user, obj)) canRead++;
			}
			if(canRead != searchRequest.getParticipations().size()) {
				logger.error("One or more provided participations is not visible to the current user");
				return Response.status(200).entity(objs).build();	
			}
			
			/// get the participation factory
			///
			IParticipationFactory pFact = Factories.getParticipationFactory(FactoryEnumType.valueOf(type + "PARTICIPATION"));
			
			objs = pFact.listParticipations(searchRequest.getParticipantFactoryType(), searchRequest.getParticipations().toArray(new NameIdType[0]), searchRequest.getStartRecord(), searchRequest.getRecordCount(), user.getOrganizationId());
		}
		catch(FactoryException | ArgumentException f){
			logger.error(f);
		}

		return Response.status(200).entity(objs).build();
	}
	
	@RolesAllowed({"user"})
	@GET
	@Path("/{objectId:[0-9A-Za-z\\-]+}/{startIndex:[\\d]+}/{count:[\\d]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listObjects(@PathParam("type") String type, @PathParam("objectId") String objectId, @PathParam("startIndex") long startIndex, @PathParam("count") int recordCount, @Context HttpServletRequest request){

		AuditEnumType auditType = AuditEnumType.valueOf(type);
		List<Object> objs = new ArrayList<>();
		try{
			INameIdFactory iFact = BaseService.getFactory(auditType);
			if(iFact.isClusterByParent() && !iFact.isClusterByGroup()){
				logger.debug("Request to list " + type + " objects by parent in " + type + " " + objectId);
				objs = BaseService.listByParentObjectId(auditType, "UNKNOWN", objectId, startIndex, recordCount, request);
			}
			else if(auditType == AuditEnumType.DATA || iFact.isClusterByGroup() || iFact.isClusterByParent()){
				logger.debug("Request to list " + type + " objects by GROUP " + objectId);
				objs = BaseService.listByGroup(auditType, "UNKNOWN", objectId, startIndex, recordCount, request);
			}
			else{
				UserType user = ServiceUtil.getUserFromSession(request);
				logger.debug("Request to list " + type + " objects by ORGANIZATION " + user.getOrganizationId());
				objs = BaseService.listByOrganization(auditType, startIndex, recordCount, request);
				
			}
		}
		catch(FactoryException f){
			logger.error(f);
		}
		return Response.status(200).entity(objs).build();
	}
	
	@RolesAllowed({"user"})
	@GET
	@Path("/{objectId:[0-9A-Za-z\\-]+}/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response countObjects(@PathParam("type") String type, @PathParam("objectId") String objectId, @Context HttpServletRequest request){
		AuditEnumType auditType = AuditEnumType.valueOf(type);

		int count = 0;
		try{
			INameIdFactory iFact = BaseService.getFactory(auditType);
			if(iFact.isClusterByParent() && !iFact.isClusterByGroup()){
				NameIdType parent = (NameIdType)BaseService.readByObjectId(auditType, objectId, request);
				if(parent != null){
					logger.debug("Counting " + type + " objects in parent " + parent.getUrn());
					count = BaseService.countInParent(auditType, parent, request);
				}
			}
			else if(auditType == AuditEnumType.DATA || iFact.isClusterByGroup() || iFact.isClusterByParent()){
				BaseGroupType group = (BaseGroupType)BaseService.readByObjectId(AuditEnumType.GROUP, objectId, request);
				if(group != null){
					logger.debug("Counting " + type + " objects in GROUP " + group.getUrn());
					count = BaseService.countByGroup(auditType, group, request);
				}
			}
			else{
				UserType user = ServiceUtil.getUserFromSession(request);
				count = BaseService.countByOrganization(auditType, user.getOrganizationId(), request);
			}
		}
		catch(FactoryException f){
			logger.error(f);
		}
		return Response.status(200).entity(count).build();
	}
	
	
}
