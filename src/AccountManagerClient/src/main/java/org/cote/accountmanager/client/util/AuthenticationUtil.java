package org.cote.accountmanager.client.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.objects.ApiClientConfigurationType;
import org.cote.accountmanager.objects.ApiServiceEnumType;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.AuthenticationResponseEnumType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.util.JSONUtil;

public class AuthenticationUtil {
	//private static Map<String,String> sessionMap = new HashMap<String,String>();
	public static final Logger logger = LogManager.getLogger(AuthenticationUtil.class);
	public static String accessToken(ClientContext context){
		context.clearContext();
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + "/crypto/accessToken");
		Response response = ClientUtil.getRequestBuilder(context, webResource).accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
		if(response.getStatus() == 200){
			return response.readEntity(String.class);
		}
		return null;
		
	}
	
	public static ApiClientConfigurationType getApiConfiguration(String restService){
		ApiClientConfigurationType cfg = new ApiClientConfigurationType();
		cfg.setServiceUrl(restService);
		cfg.setServiceType(ApiServiceEnumType.REST);
		return cfg;
	}
	
	public static boolean logout(ClientContext context){
		context.clearContext();
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + "/user/logout");
		Response response = ClientUtil.getRequestBuilder(context, webResource).accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
		if(response.getStatus() == 200){
			return true;
		}
		return false;
	}

	public static AuthenticationResponseType authenticate(ClientContext context, String server, String orgPath, String name, String password){
		AuthenticationResponseType authResp = null;
		context.clearContext();
		ClientContext defaultContext = new ClientContext();
		String cacheKey = "Token::" + server + "::" + orgPath + "::" + name;
		String userKey = "User::" + server + "::" + orgPath + "::" + name;
		ApiClientConfigurationType api = CacheUtil.readCache(defaultContext, server, ApiClientConfigurationType.class);
		if(api == null){
			logger.error("API configuration does not exist.");
			return null;
		}

		CredentialType token = CacheUtil.readCache(context, cacheKey, CredentialType.class);
		if(token == null && password == null){
			logger.error("Credential or access token not defined");
			return null;
		}
		/*
		if(token != null){
			logger.info("Attempt to parse");
			Jwt jwt = Jwts.parser().parse(new String(token.getCredential()));
		}
		*/
		String path = ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + "/token/jwt";
//		String resourcePath = ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + "/resource";
		try{
			
			AuthenticationRequestType req = new AuthenticationRequestType();
			
			if(token != null){
				req.setCredentialType(CredentialEnumType.TOKEN);
				req.setCredential(token.getCredential());
				path += "/validate";
			}
			else{
				req.setCredentialType(CredentialEnumType.HASHED_PASSWORD);
				req.setOrganizationPath(orgPath);
				req.setSubject(name);
				req.setCredential(password.getBytes("UTF-8"));
				path += "/authenticate";
			}
			WebTarget webResource = ClientUtil.getResource(path);
			/// logger.info("Authentication Url: " + path + "\n" + JSONUtil.exportObject(req));
			
			Response response = webResource.request().accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE),Response.class);
	
			if (response.getStatus() == 200) {
				//ClientUtil.setCookies(response.getCookies());
				authResp = response.readEntity(AuthenticationResponseType.class);
				if(authResp != null){
					// logger.info("Authentication Response: " + JSONUtil.exportObject(authResp));
					if(authResp.getResponse().equals(AuthenticationResponseEnumType.AUTHENTICATED)){
						if(token == null && authResp.getMessage() != null){
							logger.info("Caching token");
							token = new CredentialType();
							token.setCredential(authResp.getMessage().getBytes());
							token.setCredentialType(CredentialEnumType.TOKEN);
							CacheUtil.cache(context, cacheKey, token);
						}
						
						context.setAuthenticationCredential(token);
						context.setAuthenticationStatus(authResp.getResponse());
						context.setApiConfiguration(api);
						UserType user = CacheUtil.readCache(context, userKey, UserType.class);
						if(user == null){
							user = AM6Util.getPrincipal(context);
							if(user == null){
								logger.error("Authenticated user object with key '" + userKey + "' is null");
							}
						}
						else {
							logger.debug("Reading principle from cache for " + userKey);
						}
						context.applyContext(user);
						authResp.setUser(user);
						// logger.info("User - " + (user == null ? "No" : "Yes"));
						/*

						*/
						//sessionMap.put(name, authResp.getSessionId());
						//ClientContext.applyContext(authResp.getUser());
					}
					else{
						logger.warn("Not authenticated");
					}
				}
				else{
					logger.error("Authentication response was unexpected");
				}
				//cookies.clear();
				//cookies.addAll(response.getCookies());

				//response.get
				//logger.info(response.getType());
			}
			else{
				logger.error("Authentication response is " + response.getStatus());
			}
		}
		catch (UnsupportedEncodingException e) {
			
			logger.error("Authentication Error",e);
		}
		return authResp;
	}
}
