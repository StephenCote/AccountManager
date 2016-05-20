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


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.security.ApiConnectionConfigurationService;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.beans.CryptoBean;


@Path("/crypto")
public class CryptoService{

	public static final Logger logger = Logger.getLogger(CryptoService.class.getName());
	private static SchemaBean schemaBean = null;
	private static Pattern uidTokenPattern = Pattern.compile("^(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)");
	public CryptoService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	
	private CryptoBean getCryptoBean(SecurityBean bean,String guid){
		if(bean == null || guid == null){
			logger.error("Bean or guid is null");
			return null;
		}
		CryptoBean cBean = new CryptoBean();
		cBean.setSpoolId(guid);
		cBean.setCipherIV(bean.getCipherIV());
		cBean.setCipherKey(bean.getCipherKey());
		cBean.setCipherKeySpec(bean.getCipherKeySpec());
		cBean.setCipherProvider(bean.getCipherProvider());
		cBean.setSymmetricCipherKeySpec(bean.getSymmetricCipherKeySpec());
		return cBean;
	}

	private SecuritySpoolType newSecurityToken(HttpServletRequest request){
		SecuritySpoolType tokenType = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.REQUEST, "newSecurityToken", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user == null) return null;
		try{
			SecurityBean bean = new SecurityBean();
			SecurityFactory.getSecurityFactory().generateSecretKey(bean);
			tokenType = Factories.getSecurityTokenFactory().newSecurityToken(user.getSession().getSessionId(), user.getOrganizationId());
			tokenType.setOwnerId(user.getId());
			tokenType.setData(SecurityFactory.getSecurityFactory().serializeCipher(bean));
			AuditService.targetAudit(audit, AuditEnumType.SECURITY_TOKEN, tokenType.getGuid());
			if(Factories.getSecurityTokenFactory().addSecurityToken(tokenType) == false){
				AuditService.denyResult(audit, "Failed to persist token");
				logger.error("Failed to persist tokens");
				tokenType = null;
			}
			else{
				AuditService.permitResult(audit, "Created token");
			}

		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return tokenType;
	}
	
	
	@GET @Path("/accessToken") @Produces(MediaType.APPLICATION_JSON)
	public String getAccessToken(@Context HttpServletRequest request){
		SecuritySpoolType token = newSecurityToken(request);
		if(token == null) return null;
		/*
		SecurityBean bean = new SecurityBean();
		SecurityFactory.getSecurityFactory().importSecurityBean(bean, token.getData(), false);
		return getCryptoBean(bean,token.getGuid());
		*/
		return token.getGuid();
	}
	
	@GET @Path("/getKeyRing") @Produces(MediaType.APPLICATION_JSON)
	public CryptoBean[] getKeyRing(@Context HttpServletRequest request){

		String sessionId = ServiceUtil.getSessionId(request);
		UserSessionType session = null;
		SecuritySpoolType[] tokens = new SecuritySpoolType[0];
		List<CryptoBean> secs = new ArrayList<CryptoBean>();
		OrganizationType org = ServiceUtil.getOrganizationFromRequest(request);
		try {
			session = SessionSecurity.getUserSession(sessionId, org.getId());
			tokens = Factories.getSecurityTokenFactory().getSecurityTokens(sessionId, org.getId());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage()); 
			e.printStackTrace();
			
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage()); 
			e.printStackTrace();
		}
		
		if(tokens.length > 0){
			
			for(int i = 0; i < tokens.length; i++){
				//String[] pairs = tokens[i].getData().split("&&");
				SecurityBean bean = new SecurityBean();
				//SecurityFactory.getSecurityFactory().setSecretKey(bean, BinaryUtil.fromBase64(pairs[0].getBytes()), BinaryUtil.fromBase64(pairs[1].getBytes()), false);
				SecurityFactory.getSecurityFactory().importSecurityBean(bean, tokens[i].getData(), false);
				secs.add(getCryptoBean(bean,tokens[i].getGuid()));
			}
		}
		else{
			try{
				List<SecuritySpoolType> tokenTypes = new ArrayList<SecuritySpoolType>();
				for(int i = 0; i < 10;i++){
					SecurityBean bean = new SecurityBean();
					SecurityFactory.getSecurityFactory().generateSecretKey(bean);
					SecuritySpoolType tokenType = Factories.getSecurityTokenFactory().newSecurityToken(sessionId, org.getId());
					secs.add(getCryptoBean(bean,tokenType.getGuid()));
					
					tokenType.setOwnerId(session.getUserId());
					tokenType.setData(SecurityFactory.getSecurityFactory().serializeCipher(bean));
					//tokenType.setData(BinaryUtil.toBase64Str(bean.getCipherKey()) + "&&" + BinaryUtil.toBase64Str(bean.getCipherIV()));
					tokenTypes.add(tokenType);
				}
				if(Factories.getSecurityTokenFactory().addSecurityTokens(tokenTypes.toArray(new SecuritySpoolType[0])) == false){
					logger.error("Failed to persist tokens");
					secs.clear();
				}
			}
			catch(FactoryException fe){
				fe.printStackTrace();
				logger.error(fe.getMessage()); 
				secs.clear();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage()); 
				secs.clear();
				e.printStackTrace();
			}
		}
		
		///System.out.println(ServiceUtil.getSessionId(request));

		return secs.toArray(new CryptoBean[0]);
	}
	
	/// TODO: Add throttle to prevent BRUTE FORCE attempts to crack
	///
	@GET @Path("/validateApiToken/{token:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean validateApiToken(@PathParam("token") String token,@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHENTICATE, "validateCredential", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user == null) return false;
		boolean out_bool = false;
		if(token == null || token.length() == 0){
			AuditService.denyResult(audit, "Credential is missing");
			return out_bool;
		}

		DirectoryGroupType dir = ApiConnectionConfigurationService.getApiDirectory(user);
		if(dir == null){
			AuditService.denyResult(audit, "Invalid API Directory");
			return false;
		}

		CredentialType currentCred = CredentialService.getPrimaryCredential(dir,CredentialEnumType.TOKEN,true);
		if(currentCred == null){
			AuditService.denyResult(audit, "Object does not define a credential");
			return false;
		}
		if(CredentialService.validateTokenCredential(dir, currentCred, token)==true){
			AuditService.permitResult(audit, "Validated credential");
			out_bool = true;
			
		}
		else{
			AuditService.denyResult(audit, "Failed to validate credential");
			out_bool = false;
		}
		return out_bool;
	}
	@GET @Path("/validateMaterialToken/{type:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}/{token:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean validateMaterialToken(@PathParam("type") String type,@PathParam("token") String token,@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHENTICATE, "validateCredential", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		//UserType user = ServiceUtil.getUserFromSession(audit,request);
		//if(user == null) return false;
		boolean out_bool = false;
		if(token == null || token.length() == 0){
			AuditService.denyResult(audit, "Credential is missing");
			return out_bool;
		}
		try{
			Matcher m = uidTokenPattern.matcher(token);
			if(m.find() == false || m.groupCount() < 4){
				AuditService.denyResult(audit, "Credential is not in an expected token format");
				return false;
			}
			int orgId = Integer.parseInt(m.group(1));
			int userId = Integer.parseInt(m.group(2));
			int objId = Integer.parseInt(m.group(3));
			logger.info("Parsing " + orgId + " " + userId + " " + type + " " + objId);
			OrganizationType org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org == null){
				AuditService.denyResult(audit, "Invalid organization from id " + orgId);
				return false;
			}
			UserType targUser = Factories.getUserFactory().getById(userId, org.getId());
			if(targUser == null){
				AuditService.denyResult(audit, "Invalid user from id " + userId);
				return false;
			}
			FactoryEnumType ftype = FactoryEnumType.valueOf(type);
			if(ftype == FactoryEnumType.GROUP){
				BaseGroupType group = Factories.getGroupFactory().getGroupById(objId, org.getId());
				if(group == null){
					AuditService.denyResult(audit, "Invalid group from id " + objId);
					return false;
				}
				CredentialType currentCred = CredentialService.getPrimaryCredential(group);
				if(currentCred == null){
					AuditService.denyResult(audit, "Object does not define a credential");
					return false;
				}
				if(CredentialService.validatePasswordCredential(group, currentCred, token)==true){
					AuditService.permitResult(audit, "Validated credential");
					out_bool = true;
					
				}
				else{
					AuditService.denyResult(audit, "Failed to validate credential");
					return false;
				}
				
			}
			else{
				AuditService.denyResult(audit, "Unsupported token type " + type);
				return false;
			}

			
		}
		catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//CredentialService.validatePasswordCredential(updateUser, currentCred, new String(authReq.getCheckCredential(),"UTF-8"))==false
		return out_bool;
	}
	@POST @Path("/newPrimaryCredential") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean newPrimaryCredential(AuthenticationRequestType authReq,@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "newCredential", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		UserType owner = null;
		boolean out_bool = false;
		if(user == null) return out_bool;
		CredentialType newCred = null;
		try{
			NameIdType targetObject = null;
			
			/*
			 * To create a new user credential:
			 *    The authenticated user must be an account administrator
			 *    Or the current credential must be supplied
			 */
			/// TODO: Address the class name duplication 
			///
			boolean accountAdmin = org.cote.accountmanager.data.services.RoleService.isFactoryAdministrator(user,Factories.getAccountFactory());
			boolean dataAdmin = org.cote.accountmanager.data.services.RoleService.isFactoryAdministrator(user, Factories.getDataFactory());
			if(authReq.getSubjectType() == NameEnumType.USER){
				UserType updateUser = Factories.getUserFactory().getUserByName(authReq.getSubject(), user.getOrganizationId());
				if(updateUser == null){
					AuditService.denyResult(audit, "Target user " + authReq.getSubject() + " does not exist");
					return out_bool;
				}
				AuditService.targetAudit(audit, AuditEnumType.USER, updateUser.getName() + " (#" + updateUser.getId() + ")");
				/// If not account admin, then validate the current password
				if(accountAdmin == false){
					if(AuthorizationService.isMapOwner(user, updateUser) == false){
						AuditService.denyResult(audit, "Non account administrators are not premitted to change user passowrds that are not their own.");
						return out_bool;
					}
					CredentialType currentCred = CredentialService.getPrimaryCredential(updateUser);
					if(authReq.getCheckCredential() == null || authReq.getCheckCredential().length == 0){
						AuditService.denyResult(audit, "The current credential is required to create a new one.");
						return out_bool;
					}
					if(CredentialService.validatePasswordCredential(updateUser, currentCred, new String(authReq.getCheckCredential(),"UTF-8"))==false){
						AuditService.denyResult(audit, "Failed to validate current credential");
						return out_bool;
					}
					
				}
				targetObject = updateUser;
				owner = updateUser;
			}
			/*
			 * To create an object credential,
			 * the current user must be a data administrator
			 * Or the object owner
			 * Or have access to update the object and supply current password must be supplied
			 */
			else if(authReq.getSubjectType() == NameEnumType.GROUP){
				BaseGroupType updateGroup = Factories.getGroupFactory().getByUrn(authReq.getSubject());
				if(updateGroup == null){
					AuditService.denyResult(audit, "Target group " + authReq.getSubject() + " does not exist");
					return out_bool;
				}
				AuditService.targetAudit(audit, AuditEnumType.GROUP, updateGroup.getName() + " (#" + updateGroup.getId() + ")");
				/// If not account admin, then validate the current password
				if(dataAdmin == false && AuthorizationService.canChange(user, updateGroup) == false){

					CredentialType currentCred = CredentialService.getPrimaryCredential(updateGroup);
					if(authReq.getCheckCredential() == null || authReq.getCheckCredential().length == 0){
						AuditService.denyResult(audit, "The current credential is required to create a new one.");
						return out_bool;
					}
					if(CredentialService.validatePasswordCredential(updateGroup, currentCred, new String(authReq.getCheckCredential(),"UTF-8"))==false){
						AuditService.denyResult(audit, "Failed to validate current credential");
						return out_bool;
					}
				}
				targetObject = updateGroup;
				owner = Factories.getUserFactory().getById(updateGroup.getOwnerId(), updateGroup.getOrganizationId());
			}
			else{
				//logger.error("Subject type " + authReq.getSubjectType().toString() + " is unsupported");
				AuditService.denyResult(audit, "Subject type " + authReq.getSubjectType().toString() + " is unsupported");;
				return out_bool;
			}
			/// Create a new primary credential for target user
			newCred = CredentialService.newCredential(authReq.getCredentialType(), null, owner, targetObject, authReq.getCredential(), true, true, false);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(newCred != null){
			AuditService.permitResult(audit, "Created a new primary credential");
			out_bool = true;
		}
		else{
			AuditService.denyResult(audit, "Failed to create new primary credential");
		}
		return out_bool;

	}
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}