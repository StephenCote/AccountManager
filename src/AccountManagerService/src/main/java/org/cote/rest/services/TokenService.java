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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.services.VaultService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.AuthenticationResponseEnumType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
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

	private static VaultService vaultService = new VaultService();
	private static SchemaBean schemaBean = null;
	private String tokenizerVaultUrn = null;
	private String getTokenizerVaultUrn(){
		if(tokenizerVaultUrn == null){
			tokenizerVaultUrn = context.getInitParameter("tokenizer.vault.urn");
		}
		return tokenizerVaultUrn;
	}
	@Context
	ServletContext context;
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	

	@RolesAllowed({"api","user","admin"})
	@GET
	@Path("/resource/{tokenId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getTokenizedResource(@PathParam("tokenId") String tokenId, @Context HttpServletRequest request){
		String outToken = null;
		UserType user = ServiceUtil.getUserFromSession(request);
		DataType tokenData = BaseService.readByObjectId(AuditEnumType.DATA, tokenId, request);

		if(tokenData == null){
			logger.error("Failed to retrieve token data with id '" + tokenId + "'");
		}
		else{
			/// TODO: There's a bug/caveat to reading vaulted data - by using BaseService, TypeSanitizer's postFetch for data will decrypt, decipher, and decompress the data and return a volatile copy with the decrypted value
			/// But the meta data still indicates it's enciphered
			try {
				/// 2017/09/14
				/// TODO: there appears to be an issue with the way vaulted data is being extracted through TypeSanitizer - it's not being decrypted for some reason (even though the call is being made).
				/// This isn't consistent with the behavior through GenericResource which does make use of TypeSanitizer, so there's either an obvious glaring issue, a caching issue, or I'm missing something (obvious)
				VaultBean vaultBean = vaultService.getVaultByUrn(user, tokenData.getVaultId());
				outToken = new String(vaultService.extractVaultData(vaultBean, tokenData));
			}
			catch (DataException | FactoryException | ArgumentException e) {
				logger.info(e.getMessage());
			}
		}
		return Response.status(outToken == null || outToken.length() == 0 ? 500 : 200).entity(outToken).build();
	}
	
	@RolesAllowed({"api","user","admin"})
	@POST
	@Path("/resource")
	@Produces(MediaType.TEXT_PLAIN)
	public Response tokenizeResource(String json, @Context HttpServletRequest request){
		String outToken = null;
		UserType user = ServiceUtil.getUserFromSession(request);

		DirectoryGroupType dir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/TokenizedResources", request);
		if(dir == null){
			logger.error("Failed to find ./TokenizedResources path");
		}
		else{
			try {
				BaseService.normalize(user, dir);
				String vaultUrn = getTokenizerVaultUrn();
				VaultBean vaultBean = (vaultUrn == null || vaultUrn.length() == 0 ? null : vaultService.getVaultByUrn(user, vaultUrn));
				if(vaultBean == null){
					logger.error("Invalid vault urn: '" + vaultUrn + "'");
				}
				else{
					DataType tokenData = new DataType();
					String dataName = UUID.randomUUID().toString();
					tokenData.setName(dataName);
					tokenData.setMimeType("text/plain");
					tokenData.setGroupPath(dir.getPath());
					if(vaultBean.getActiveKeyId() == null) vaultService.newActiveKey(vaultBean);
					vaultService.setVaultBytes(vaultBean, tokenData, json.getBytes());
					if(BaseService.add(AuditEnumType.DATA, tokenData, request)){
						tokenData = BaseService.readByName(AuditEnumType.DATA, dir, dataName, request);
						if(tokenData != null){
							outToken = tokenData.getObjectId();
						}
						else{
							logger.error("Failed to find tokenized data with name '" + dataName + "'");
						}
					}
					else{
						logger.error("Failed to add tokenized data with name '" + dataName + "'");
					}
				}
			} catch (UnsupportedEncodingException | DataException | FactoryException | ArgumentException e) {
				logger.error(e.getMessage());
			}
			
		}
		
		return Response.status(outToken == null || outToken.length() == 0 ? 500 : 200).entity(outToken).build();
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
			try {
				OrganizationFactory oFact = Factories.getFactory(FactoryEnumType.ORGANIZATION);

				OrganizationType org = oFact.find(authnRequest.getOrganizationPath());
				if(org != null){
					byte[] creds = authnRequest.getCredential();
					String credStr = (new String(authnRequest.getCredential())).trim();

					logger.info("Validating credential for '" + authnRequest.getSubject() + "'");
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
		logger.info("Validating token: '" + token + "'");
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

