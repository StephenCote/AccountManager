package org.cote.accountmanager.client.util;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class AM6Util {
	public static final Logger logger = LogManager.getLogger(AM6Util.class);
	
	public static <T> T getPrincipal(){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + "/principal/");
		return (T)getEntity(UserType.class,webResource);
	}
	
	
	public static <T> T findObject(Class<T> cls, NameEnumType nameType, String objectType, String path){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + "/search/" + nameType.toString() + "/" + (objectType != null ? objectType : "UNKNOWN") + "/" + path);
		return getEntity(cls,webResource);
	}
	public static <T> T getObject(Class<T> cls, NameEnumType nameType, String objectId){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + "/resource/" + nameType.toString() + "/" + objectId);
		return getEntity(cls,webResource);
	}
	public static <T> T getEntity(Class<T> cls, WebTarget resource){
		return getEntity(cls, resource, MediaType.APPLICATION_JSON_TYPE, 200);
	}
	public static <T> T getEntity(Class<T> cls, WebTarget resource, MediaType responseType, int successStatus){
		Response response = ClientUtil.getRequestBuilder(resource).accept(responseType).get(Response.class);

		T out_obj = null;
		if(response.getStatus() == successStatus){
			out_obj = response.readEntity(cls);
		}
		return out_obj;
	}
	/*
	public static <T> T getObject(String objectId, Class cls){
		T obj = null;
		
		return obj;
	}
	*/
}
