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

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;


@Path("/credential")
public class CredentialService {
	public static final Logger logger = LogManager.getLogger(CredentialService.class);

	private static SchemaBean schemaBean = null;
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }


	@POST @Path("/{type:[A-Za-z]+}/{objectId:[A-Za-z0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean newPrimaryCredential(@PathParam("type") String objectType, @PathParam("objectId") String objectId,AuthenticationRequestType authReq,@Context HttpServletRequest request){

		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "newCredential", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditEnumType type = AuditEnumType.valueOf(objectType);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		UserType owner = null;
		boolean outBool = false;
		if(user == null) return outBool;
		CredentialType newCred = null;
		try{
			NameIdType targetObject = null;
			
			/*
			 * To create a new user credential:
			 *    The authenticated user must be an account administrator
			 *    Or the current credential must be supplied
			 */
			//boolean accountAdmin = org.cote.accountmanager.data.services.AuthorizationService.is
			boolean accountAdmin = RoleService.isFactoryAdministrator(user,((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)));
			boolean dataAdmin = RoleService.isFactoryAdministrator(user, ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)));
			if(type == AuditEnumType.USER){
				UserType updateUser = BaseService.readByObjectId(AuditEnumType.valueOf(objectType), objectId, request);
				if(updateUser == null){
					AuditService.denyResult(audit, "Target user " + authReq.getSubject() + " does not exist");
					return outBool;
				}
				AuditService.targetAudit(audit, AuditEnumType.USER, updateUser.getName() + " (#" + updateUser.getId() + ")");
				/// If not account admin, then validate the current password
				if(accountAdmin == false){
					if(authReq.getCheckCredential() == null || authReq.getCheckCredential().length == 0){
						AuditService.denyResult(audit, "The current credential is required to create a new one.");
						return outBool;
					}
					if(org.cote.accountmanager.data.services.AuthorizationService.isMapOwner(user, updateUser) == false){
						logger.warn("User ownership mapping error: " + user.getUrn() + " (" + user.getId() + ") :: " + updateUser.getUrn() + " (" + updateUser.getId() + ")");
						AuditService.denyResult(audit, "Non account administrators are not premitted to change user passowrds that are not their own.");
						return outBool;
					}
					
					CredentialType currentCred = org.cote.accountmanager.data.security.CredentialService.getPrimaryCredential(updateUser);
					if(currentCred == null){
						logger.warn("Current credential is null for " + updateUser.getUrn());
						return outBool;
					}

					if(org.cote.accountmanager.data.security.CredentialService.validatePasswordCredential(updateUser, currentCred, new String(authReq.getCheckCredential(),StandardCharsets.UTF_8).trim())==false){
						AuditService.denyResult(audit, "Failed to validate current credential");
						return outBool;
					}
					
				}
				targetObject = updateUser;
				owner = updateUser;
			}
			/*
			 * To create an object credential,
			 * the current user must be a data administrator
			 * Or the object owner
			 * Or have access to update the object and current password must be supplied
			 */
			else if(type == AuditEnumType.GROUP){
				BaseGroupType updateGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getByUrn(authReq.getSubject());
				if(updateGroup == null){
					AuditService.denyResult(audit, "Target group " + authReq.getSubject() + " does not exist");
					return outBool;
				}
				AuditService.targetAudit(audit, AuditEnumType.GROUP, updateGroup.getName() + " (#" + updateGroup.getId() + ")");
				/// If not account admin, then validate the current password
				if(dataAdmin == false && org.cote.accountmanager.data.services.AuthorizationService.canChange(user, updateGroup) == false){

					CredentialType currentCred = org.cote.accountmanager.data.security.CredentialService.getPrimaryCredential(updateGroup);
					if(authReq.getCheckCredential() == null || authReq.getCheckCredential().length == 0){
						AuditService.denyResult(audit, "The current credential is required to create a new one.");
						return outBool;
					}
					if(org.cote.accountmanager.data.security.CredentialService.validatePasswordCredential(updateGroup, currentCred, new String(authReq.getCheckCredential(),StandardCharsets.UTF_8).trim())==false){
						AuditService.denyResult(audit, "Failed to validate current credential");
						return outBool;
					}
				}
				targetObject = updateGroup;
				owner = Factories.getNameIdFactory(FactoryEnumType.USER).getById(updateGroup.getOwnerId(), updateGroup.getOrganizationId());
			}
			else{
				AuditService.denyResult(audit, "Subject type " + authReq.getSubjectType().toString() + " is unsupported");;
				return outBool;
			}
			/// Create a new primary credential for target user
			byte[] credByte = (new String(authReq.getCredential())).trim().getBytes(StandardCharsets.UTF_8);
			newCred = org.cote.accountmanager.data.security.CredentialService.newCredential(authReq.getCredentialType(), null, owner, targetObject, credByte, true, true);
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		if(newCred != null){
			AuditService.permitResult(audit, "Created a new primary credential");
			outBool = true;
		}
		else{
			AuditService.denyResult(audit, "Failed to create new primary credential");
		}
		return outBool;

	}

}
