package org.cote.rest.services;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.AuthenticationResponseEnumType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.jaas.AM5SigningKeyResolver;
import org.cote.jaas.TokenUtil;

import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Path("/token")
public class TokenService {
	public static final Logger logger = LogManager.getLogger(TokenService.class);

	private static SchemaBean schemaBean = null;
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

	@POST @Path("/credential/{type:[A-Za-z]+}/{objectId:[A-Za-z0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean newPrimaryCredential(@PathParam("type") String objectType, @PathParam("objectId") String objectId,AuthenticationRequestType authReq,@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "newCredential", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditEnumType type = AuditEnumType.valueOf(objectType);
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
			//boolean accountAdmin = org.cote.accountmanager.data.services.AuthorizationService.is
			boolean accountAdmin = RoleService.isFactoryAdministrator(user,((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)));
			boolean dataAdmin = RoleService.isFactoryAdministrator(user, ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)));
			if(type == AuditEnumType.USER){
				UserType updateUser = BaseService.readByObjectId(AuditEnumType.valueOf(objectType), objectId, request);
				//UserType updateUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(authReq.getSubject(), user.getOrganizationId());
				if(updateUser == null){
					AuditService.denyResult(audit, "Target user " + authReq.getSubject() + " does not exist");
					return out_bool;
				}
				AuditService.targetAudit(audit, AuditEnumType.USER, updateUser.getName() + " (#" + updateUser.getId() + ")");
				/// If not account admin, then validate the current password
				if(accountAdmin == false){
					if(org.cote.accountmanager.data.services.AuthorizationService.isMapOwner(user, updateUser) == false){
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
			else if(type == AuditEnumType.GROUP){
				BaseGroupType updateGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getByUrn(authReq.getSubject());
				if(updateGroup == null){
					AuditService.denyResult(audit, "Target group " + authReq.getSubject() + " does not exist");
					return out_bool;
				}
				AuditService.targetAudit(audit, AuditEnumType.GROUP, updateGroup.getName() + " (#" + updateGroup.getId() + ")");
				/// If not account admin, then validate the current password
				if(dataAdmin == false && org.cote.accountmanager.data.services.AuthorizationService.canChange(user, updateGroup) == false){

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
				owner = Factories.getNameIdFactory(FactoryEnumType.USER).getById(updateGroup.getOwnerId(), updateGroup.getOrganizationId());
			}
			else{
				AuditService.denyResult(audit, "Subject type " + authReq.getSubjectType().toString() + " is unsupported");;
				return out_bool;
			}
			/// Create a new primary credential for target user
			byte[] credByte = (new String(authReq.getCredential())).trim().getBytes("UTF-8");
			newCred =
					//CredentialService.newHashedPasswordCredential(owner, targetObject, new String(authReq.getCredential(),"UTF-8"), true, false);
					
					CredentialService.newCredential(authReq.getCredentialType(), null, owner, targetObject, credByte, true, true, false);
		}
		catch(FactoryException | ArgumentException | UnsupportedEncodingException e) {
			logger.error("Error",e);
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

	
	@POST
	@Path("/jwt/authenticate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginToResponse(AuthenticationRequestType authnRequest, @Context HttpServletRequest request, @Context HttpServletResponse response){
		AuthenticationResponseType outResp = new AuthenticationResponseType();
		outResp.setResponse(AuthenticationResponseEnumType.NOT_AUTHENTICATED);
		//authnRequest.
		String outToken = null;		
		if(authnRequest != null && authnRequest.getCredentialType().equals(CredentialEnumType.TOKEN) || authnRequest.getCredentialType().equals(CredentialEnumType.HASHED_PASSWORD)){
			if(authnRequest.getOrganizationPath() == null) authnRequest.setOrganizationPath("/Public");
			OrganizationFactory oFact = Factories.getFactory(FactoryEnumType.ORGANIZATION);
			try {
				OrganizationType org = oFact.find(authnRequest.getOrganizationPath());
				if(org != null){
					byte[] creds = authnRequest.getCredential();
					String credStr = (new String(authnRequest.getCredential())).trim();

					logger.info("Validating: " + authnRequest.getSubject() + ":" + credStr + ":" + creds.length);
					logger.info(JSONUtil.exportObject(authnRequest));
					UserType user = SessionSecurity.login(authnRequest.getSubject(), authnRequest.getCredentialType(), credStr,org.getId());
					if(user != null){
						outToken = getJWTToken(user);
						outResp.setMessage(outToken);
						outResp.setResponse(AuthenticationResponseEnumType.AUTHENTICATED);
					}
				}
			} catch (FactoryException | ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			logger.error("Unknown credential type");
		}
		return Response.status(200).entity(outResp).build();
	}
	
	
	@POST
	@Path("/jwt/authenticate/token")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginToToken(AuthenticationRequestType authnRequest, @Context HttpServletRequest request, @Context HttpServletResponse response){
		AuthenticationResponseType authnResp = (AuthenticationResponseType)loginToResponse(authnRequest, request, response).getEntity();
		return Response.status(200).entity(authnResp.getMessage()).build();
	}
	
	@POST
	@Path("/jwt/validate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validatePostJWT(AuthenticationRequestType authRequest, @Context HttpServletRequest request){
		AuthenticationResponseType outResp = new AuthenticationResponseType();
		outResp.setResponse(AuthenticationResponseEnumType.NOT_AUTHENTICATED);
		String subjectUrn = validateJWTToken(new String(authRequest.getCredential()));
		
		if(subjectUrn != null){
			outResp.setResponse(AuthenticationResponseEnumType.AUTHENTICATED);
			outResp.setMessage(new String(authRequest.getCredential()));
		}
		
		return Response.status(200).entity(outResp).build();
	}
	
	@GET
	@Path("/jwt/validate/{token:[A-Za-z0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateGetJWT(@PathParam("token") String token, @Context HttpServletRequest request){
		String subjectUrn = validateJWTToken(token);
		return Response.status(200).entity((subjectUrn != null)).build();
	}
	
	@GET
	@Path("/jwt")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJWT(@Context HttpServletRequest request){
		String outToken = null;
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user != null){
			outToken = getJWTToken(user);
		}
		return Response.status(200).entity(outToken).build();
	}
	
	
	private String validateJWTToken(String token){
		logger.info("Validting token: '" + token + "'");
		return Jwts.parser().setSigningKeyResolver(new AM5SigningKeyResolver()).parseClaimsJws(token).getBody().getSubject();
	}
	
	
	
	private String getJWTToken(UserType user){
		
		SecurityBean bean = TokenUtil.getJWTSecurityBean(user);
		if(bean == null){
			logger.error("Null security bean");
			return null;
		}
		if(bean.getSecretKey() == null){
			logger.error("Null secret key");
			logger.error(JSONUtil.exportObject(bean));
			return null;
		}
		
		//Key key = MacProvider.generateKey();
		Map<String,Object> claims = new HashMap<>();
		claims.put("objectId", user.getObjectId());
		claims.put("organizationPath", user.getOrganizationPath());
		return Jwts.builder()
		  .setClaims(claims)
		  .setSubject(user.getName())
		  .setId(user.getUrn())
		  .compressWith(CompressionCodecs.GZIP)
		  .signWith(SignatureAlgorithm.HS512, bean.getSecretKey())
		  .compact();
	}
	
}

