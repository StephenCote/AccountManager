package org.cote.accountmanager.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.ClientContext;

import org.cote.accountmanager.client.services.rest.SchemaBean;
import org.cote.accountmanager.client.services.rest.ServiceSchemaMethod;
import org.cote.accountmanager.client.services.rest.ServiceSchemaMethodParameter;

import org.glassfish.jersey.client.ClientConfig;

import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


public class ClientUtil {
	
	private static ArrayList<NewCookie> cookies = new ArrayList<NewCookie>();
	
	private static Client client = null;
	private static String server = "http://localhost:8080";
	private static String accountManagerApp = "/AccountManagerService/rest";
	private static String cachePath = "./cache";
	private static Map<String,SchemaBean> schemas = new HashMap<String,SchemaBean>();
	public static final Logger logger = LogManager.getLogger(ClientUtil.class);

	public static void clearCookies(){
		cookies.clear();
	}
	public static void setCookies(Map<String,NewCookie> in_cookies){
		clearCookies();
		for(String ck : in_cookies.keySet()){
			//logger.info("Receive Cookie: " + ck + "=" + in_cookies.get(ck).getValue());
			cookies.add(in_cookies.get(ck));
		}
		
	}
	
	public static String getCachePath(){
		return cachePath;
	}
	public static String getServer() {
		return server;
	}



	public static void setServer(String server) {
		ClientUtil.server = server;
	}



	public static String getAccountManagerApp() {
		return accountManagerApp;
	}



	public static void setAccountManagerApp(String accountManagerApp) {
		ClientUtil.accountManagerApp = accountManagerApp;
	}




	public static Client getClient(){
		if(client != null) return client;
		client = ClientBuilder.newClient().register(JacksonFeature.class);
		

		return client;
	}
	
	public static SchemaBean getSchema(String app, String service){
		if(schemas.containsKey(app + service)) return schemas.get(app + service);
		Response response = getResponse(ClientUtil.getServer() + app + service + "/smd");
		SchemaBean bean = null;
		if(response.getStatus() == 200){
			bean = response.readEntity(SchemaBean.class);
			schemas.put(app + service, bean);
		}
		return bean;
	}
	
	public static WebTarget getResource(String path){
		WebTarget resource = getClient().target(path);

		return resource;
	}
	public static Builder getRequestBuilder(WebTarget resource){
		Builder b = resource.request();
		for(NewCookie ck : cookies){
			//logger.info("Send Cookie: " + ck.getName() + "=" + ck.getValue());
			b.cookie(ck.getName(),ck.getValue());
		}
		if(ClientContext.getAuthenticationCredential() != null){
			b.header("Authorization", "Bearer " + new String(ClientContext.getAuthenticationCredential().getCredential()));
		}
		return b;
	}
	public static Response getResponse(String appUrl){
		return getResponse(appUrl,MediaType.APPLICATION_JSON_TYPE);
	}
	public static Response getResponse(String appUrl,MediaType type){
		WebTarget webResource = getResource(appUrl);
		
		
		return webResource.request().accept(type).get(Response.class);
	}
	
	public static List<String> printServiceSchema(String app, String name){
		SchemaBean userSchema = ClientUtil.getSchema(app,"/" + name);
		List<String> outSchema = new ArrayList<String>();
		if(userSchema == null){
			logger.error("Schema is null");
			return null;
		}

		for(int i = 0; i < userSchema.getMethods().size();i++){
			ServiceSchemaMethod meth = userSchema.getMethods().get(i);
			StringBuffer buff = new StringBuffer();
			buff.append("(");
			for(int p = 0; p < meth.getParameters().size(); p++){
				ServiceSchemaMethodParameter param = meth.getParameters().get(p);
				if(p > 0) buff.append(", ");
				buff.append(param.getType() + " " + param.getName());
			}
			buff.append(")");
			outSchema.add((meth.getReturnValue() != null ? meth.getReturnValue().getType() : "void") + " " + meth.getName() + buff.toString());
		}
		return outSchema;
	}
	
}
