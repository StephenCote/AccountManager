package org.cote.rest.services;

import java.util.HashMap;
import java.util.Map;
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
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.AuthenticationResponseEnumType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
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

