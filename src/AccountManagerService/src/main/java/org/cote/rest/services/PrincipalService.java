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

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.security.UserPrincipal;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.services.UserService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ApplicationProfileType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.rocket.services.RoleService;

@DeclareRoles({"user"})
@Path("/principal")
public class PrincipalService {
	private static final Logger logger = LogManager.getLogger(Principal.class);
	private static SchemaBean schemaBean = null;
	
	private static Map<String, ApplicationProfileType> profiles = Collections.synchronizedMap(new HashMap<>());	
	
	public static void clearCache() {
		profiles.clear();
	}
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	@RolesAllowed({"user"})
	@GET
	@Path("/anonymous")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocumentControl(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		UserType docUser = Factories.getDocumentControl(user.getOrganizationId());
		BaseService.populate(AuditEnumType.USER, docUser);
		return Response.status(200).entity(docUser).build();
	}

	
	
	@RolesAllowed({"user"})
	@GET
	@Path("/person/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOtherPerson(@PathParam("objectId") String objectId,@Context HttpServletRequest request){
		PersonType person = getPerson(objectId, request);
		return Response.status(200).entity(person).build();
	}
	
	public PersonType getPerson(String objectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		UserType contUser = BaseService.readByObjectId(AuditEnumType.USER, objectId, user);
		PersonType person = null;
		if(contUser != null && user != null){
			person = UserService.readSystemPersonForUser(user, contUser);
			if(person != null && BaseService.getEnableExtendedAttributes()){
				Factories.getAttributeFactory().populateAttributes(person);
			}
		}
		return person;
	}
	
	
	@RolesAllowed({"user"})
	@GET
	@Path("/person")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSelfPerson(@Context HttpServletRequest request){
		UserType user = SessionSecurity.getPrincipalUser(request);
		PersonType person = getPerson(user.getObjectId(), request);
		return Response.status(200).entity(person).build();
	}

	@RolesAllowed({"user"})
	@GET
	@Path("/application")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getApplicationProfile(@Context HttpServletRequest request){
		ApplicationProfileType app = null;
		UserType user = SessionSecurity.getPrincipalUser(request);
		if(!profiles.containsKey(user.getUrn())) {
			app = new ApplicationProfileType();
			try {
				if(
						RoleService.isFactoryAdministrator(user, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)))
						||
						RoleService.isFactoryReader(user, ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)))
				) {
					//app.getSystemRoles().addAll(RoleService.getSystemRoles(user.getOrganizationId()));
					app.getSystemRoles().addAll(BaseService.listSystemEntitlements(AuditEnumType.ROLE, user));
					app.getSystemPermissions().addAll(BaseService.listSystemEntitlements(AuditEnumType.PERMISSION, user));
				}
			} catch (ArgumentException | FactoryException e1) {
				logger.error(FactoryException.LOGICAL_EXCEPTION,e1);
			}
			app.setUser(user);
			app.setPerson(getPerson(user.getObjectId(), request));
			try {
				app.getUserRoles().addAll(EffectiveAuthorizationService.getEffectiveRoles(user));
			} catch (ArgumentException | FactoryException e) {
				logger.error(e);
			}
			app.setOrganizationPath(user.getOrganizationPath());
		}
		else {
			profiles.put(user.getUrn(), app);
		}
		return Response.status(200).entity(app).build();
	}
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSelf(@Context HttpServletRequest request){
		UserType outUser = SessionSecurity.getPrincipalUser(request);
		return Response.status(200).entity(outUser).build();
	}
	
	/*
	private UserType getSelfUser(HttpServletRequest request){
		Principal principal = request.getUserPrincipal();
		UserType outUser = null;
		if(principal != null && principal instanceof UserPrincipal){
			UserPrincipal userp = (UserPrincipal)principal;
			logger.info("UserPrincipal: " + userp.toString());
			try {
				OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(userp.getOrganizationPath());

				UserType user = Factories.getNameIdFactory(FactoryEnumType.USER).getById(userp.getId(), org.getId());
				if(user != null){
					outUser = user;
					if(BaseService.getEnableExtendedAttributes()){
						Factories.getAttributeFactory().populateAttributes(outUser);
					}
				}
				else {
					logger.warn("User is null for " + userp.getId() + " in " + org.getId());
				}
			} catch (FactoryException | ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		else{
			logger.debug("Don't know what: " + (principal == null ? "Null" : "Uknown") + " principal");
		}
		return outUser;
	}
	*/
}
