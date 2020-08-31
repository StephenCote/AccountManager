package org.cote.accountmanager.client.util;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.JSONUtil;

public class AM6Util {
	public static final Logger logger = LogManager.getLogger(AM6Util.class);
	
	
	private static String cacheUri = "/cache";
	private static String resourceUri = "/resource";
	private static String principalUri = "/principal";
	private static String searchUri = "/search";
	private static String makeUri = "/make";
	private static String listUri = "/list";
	private static String authZUri = "/authorization";
	private static String commUri = "/community";
	private static String credUri = "/credential";
	private static String polUri = "/policy";
	private static String tokenUri = "/token";
	private static String apprUri = "/approval";
	
	public static <T> T make(ClientContext context, Class<T> cls, NameEnumType nameType, String objectType, String path) {
		return makeFind(context, cls, nameType, objectType, path, true);
	}

	
	private static <T> T makeFind(ClientContext context, Class<T> cls, NameEnumType nameType, String objectType, String path, boolean make) {
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + (make ? makeUri : searchUri) + "/" + nameType.toString() + "/" + (objectType != null ? objectType : "UNKNOWN") + "/" + path);
		return getEntity(context, cls,webResource);
	}

	
	public static int count(ClientContext context, NameEnumType nameType, String objectId){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + listUri + "/" + nameType.toString() + "/" + objectId + "/count");
		return getEntity(context, Integer.class,webResource);
	}

	public static <T> T list(ClientContext context, Class<T> cls, NameEnumType nameType, String objectId, long startIndex, int recordCount){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + listUri + "/" + nameType.toString() + "/" + objectId + "/" + Long.toString(startIndex) + "/" + Integer.toString(recordCount));
		return getEntity(context, cls,webResource);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getPrincipal(ClientContext context){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + principalUri + "/");
		return (T)getEntity(context, UserType.class, webResource);
	}
	
	public static <T> T updateObject(ClientContext context, Class<T> cls, NameIdType object){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + resourceUri + "/" + object.getNameType().toString());
		logger.info("Update URI: " + ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + resourceUri + "/" + object.getNameType().toString());
		// logger.info(JSONUtil.exportObject(context));
		return postEntity(context, cls,webResource,object);
	}
	
	public static <T> T deleteObject(ClientContext context, Class<T> cls, NameEnumType nameType, String objectId){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + resourceUri + "/" + nameType.toString() + "/" + objectId);
		return deleteEntity(context, cls,webResource);
	}
	
	public static boolean clearCache(ClientContext context, NameEnumType nameType) {
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + cacheUri + "/" + (nameType != NameEnumType.UNKNOWN ? "clear/" + nameType.toString() : "clearAll"));
		return getEntity(context, Boolean.class,webResource);
	}
	public static <T> T findObject(ClientContext context, Class<T> cls, NameEnumType nameType, String objectType, String path){
		return makeFind(context, cls, nameType, objectType, path, false);
	}
	public static <T> T getObject(ClientContext context, Class<T> cls, NameEnumType nameType, String objectId){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + resourceUri + "/" + nameType.toString() + "/" + objectId);
		return getEntity(context, cls,webResource);
	}
	public static <T> T getObjectByName(ClientContext context, Class<T> cls, NameEnumType nameType, String objectId, String name, boolean useObjectParent){
		WebTarget webResource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + resourceUri + "/" + nameType.toString() + (useObjectParent  ? "/parent" : "") + "/" +  objectId + "/" + name.replace(" ", "%20"));
		logger.info("Get Resource: " + webResource.getUri());
		return getEntity(context, cls,webResource);
	}
	public static <T> T getEntity(ClientContext context, Class<T> cls, WebTarget resource){
		return getEntity(context, cls, resource, MediaType.APPLICATION_JSON_TYPE, 200);
	}
	public static <T> T getEntity(ClientContext context, Class<T> cls, WebTarget resource, MediaType responseType, int successStatus){
		Response response = ClientUtil.getRequestBuilder(context, resource).accept(responseType).get(Response.class);

		T out_obj = null;
		/// logger.info("Received status: " + response.getStatus());
		if(response.getStatus() == successStatus){
			try {
				out_obj = response.readEntity(cls);
			}
			catch(Exception e) {
				/// currently passing nulls back as a 200 status since, so the response shows success but the entity is null
				/// at the moment, sinking the error
				logger.warn(e.getMessage());
				/// logger.error("Trace: " + e.getStackTrace());
			}
		}
		else {
			logger.warn("Received response: " + response.getStatus());
		}
		/// logger.info("Received entity: " + out_obj);
		if(out_obj == null && cls.equals(Boolean.class)) out_obj = (T)Boolean.FALSE;
		return out_obj;
	}
	
	public static <T,U> T postEntity(ClientContext context, Class<T> cls, WebTarget resource, Object object){
		return postEntity(context, cls, resource, object, MediaType.APPLICATION_JSON_TYPE, 200);
	}
	public static <T> T postEntity(ClientContext context, Class<T> cls, WebTarget resource, Object object, MediaType responseType, int successStatus){
		Response response = ClientUtil.getRequestBuilder(context, resource).accept(responseType).post(Entity.entity(object, MediaType.APPLICATION_JSON_TYPE));

		T out_obj = null;
		if(response != null) {
			if(response.getStatus() == successStatus){
				out_obj = response.readEntity(cls);
			}
			else {
				logger.warn("Received response: " + response.getStatus());
			}
		}
		else {
			logger.warn("Null response");
		}
		return out_obj;
	}
	
	public static <T> T deleteEntity(ClientContext context, Class<T> cls, WebTarget resource){
		return deleteEntity(context, cls, resource, MediaType.APPLICATION_JSON_TYPE, 200);
	}
	public static <T> T deleteEntity(ClientContext context, Class<T> cls, WebTarget resource, MediaType responseType, int successStatus){
		Response response = ClientUtil.getRequestBuilder(context, resource).accept(responseType).delete(Response.class);

		T out_obj = null;
		if(response.getStatus() == successStatus){
			out_obj = response.readEntity(cls);
		}
		else {
			logger.warn("Received response: " + response.getStatus());
		}
		return out_obj;
	}
	public static <T> T addCredential(ClientContext context, Class<T> cls, NameEnumType objectType, String objectId, AuthenticationRequestType art) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + credUri + "/" + objectType.toString() + "/" + objectId);
		return postEntity(context, cls, resource, art);
	}
	public static <T> T deleteCommunity(ClientContext context, Class<T> cls, String communityId) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/" + communityId);
		return deleteEntity(context, cls, resource);
	}
	public static <T> T deleteCommunityProject(ClientContext context, Class<T> cls, String projectId) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/project/" + projectId);
		return deleteEntity(context, cls, resource);
	}
	public static <T> T findCommunityProject(ClientContext context, Class<T> cls, String communityName, String name) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/find/" + communityName.replace(" ", "%20") + "/" + name.replace(" ", "%20"));
		return getEntity(context, cls, resource);
	}
	public static <T> T addCommunityProject(ClientContext context, Class<T> cls, String communityId, String name) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/new/" + communityId + "/" + name.replace(" ", "%20"));
		return getEntity(context, cls, resource);
	}
	public static <T> T findCommunity(ClientContext context, Class<T> cls, String name) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/find/" + name.replace(" ", "%20"));
		return getEntity(context, cls, resource);
	}
	public static <T> T addCommunity(ClientContext context, Class<T> cls, String name) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/new/" + name.replace(" ", "%20"));
		return getEntity(context, cls, resource);
	}
	public static <T> T configureCommunity(ClientContext context, Class<T> cls) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/configure");
		return getEntity(context, cls, resource);
	}
	public static <T> T enrollCommunityAdmin(ClientContext context, Class<T> cls, String communityId, String userId) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/enroll/admin/" + communityId + "/" + userId);
		return getEntity(context, cls, resource);
	}
	public static <T> T configureCommunityTraits(ClientContext context, Class<T> cls, String communityId) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/geo/traits/LIFECYCLE/" + communityId);
		return getEntity(context, cls, resource);
	}
	public static <T> T configureCommunityCountryInfo(ClientContext context, Class<T> cls, String communityId) {
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/geo/countryInfo/LIFECYCLE/" + communityId);
		return getEntity(context, cls, resource);
	}
	public static <T> T configureAdmin1Codes(ClientContext context, Class<T> cls, String communityId) {
		// http://127.0.0.1:8080/AccountManagerService/rest/community/geo/admin1Codes/LIFECYCLE/$objectId"
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/geo/admin1Codes/LIFECYCLE/" + communityId);
		return getEntity(context, cls, resource);
	}
	public static <T> T configureAdmin2Codes(ClientContext context, Class<T> cls, String communityId) {
		// http://127.0.0.1:8080/AccountManagerService/rest/community/geo/admin1Codes/LIFECYCLE/$objectId"
		WebTarget resource = ClientUtil.getResource(ClientUtil.getServer() + ClientUtil.getAccountManagerApp() + commUri + "/geo/admin2Codes/LIFECYCLE/" + communityId);
		return getEntity(context, cls, resource);
	}
	
	public static String getEncodedPath(String path) {
		return ("B64-" + BinaryUtil.toBase64Str(path)).replace("=","%3D");
	}
}
