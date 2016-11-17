package org.cote.accountmanager.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.JSONUtil;
import org.cote.accountmanager.objects.ApiClientConfigurationType;
import org.cote.accountmanager.objects.AuthenticationResponseEnumType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;

public class ClientContext {
	public static final Logger logger = LogManager.getLogger(ClientContext.class);
	private static NameEnumType contextObjectType = NameEnumType.UNKNOWN;
	private static UserType user = null;
	private static DirectoryGroupType currentDirectory = null;
	private static DirectoryGroupType homeDirectory = null;
	private static AuthenticationResponseEnumType authenticationStatus = AuthenticationResponseEnumType.UNKNOWN;
	private static CredentialType authenticationCredential = null;
	private static ApiClientConfigurationType apiConfiguration = null;
	private static String organizationPath = null;
	//private static long organizationId = 0L;
	//private static String sessionId = null;
	//private static SessionStatusEnumType sessionStatus = SessionStatusEnumType.UNKNOWN;
	//private static OrganizationType currentOrganization = null;
	private static Map<NameEnumType,Map<String,Integer>> countMap = new HashMap<NameEnumType,Map<String,Integer>>();
	public static void clearContext(){
		user = null;
		//sessionId = null;
		//organizationId = 0L;
		//sessionStatus = SessionStatusEnumType.UNKNOWN;
		authenticationStatus = AuthenticationResponseEnumType.UNKNOWN;
		authenticationCredential = null;
		currentDirectory = null;
		homeDirectory = null;
	}

	public static String getOrganizationPath() {
		return organizationPath;
	}

	public static void setOrganizationPath(String organizationPath) {
		ClientContext.organizationPath = organizationPath;
	}

	public static ApiClientConfigurationType getApiConfiguration() {
		return apiConfiguration;
	}

	public static void setApiConfiguration(ApiClientConfigurationType apiConfiguration) {
		ClientContext.apiConfiguration = apiConfiguration;
	}

	public static AuthenticationResponseEnumType getAuthenticationStatus() {
		return authenticationStatus;
	}

	public static void setAuthenticationStatus(AuthenticationResponseEnumType authenticationStatus) {
		ClientContext.authenticationStatus = authenticationStatus;
	}

	public static CredentialType getAuthenticationCredential() {
		return authenticationCredential;
	}

	public static void setAuthenticationCredential(CredentialType authenticationCredential) {
		ClientContext.authenticationCredential = authenticationCredential;
	}

	public static void setCount(NameEnumType type, String path, int count){
		if(countMap.containsKey(type) == false) countMap.put(type, new HashMap<String,Integer>());
		countMap.get(type).put(path, count);
	}
	public static int getCount(NameEnumType type, String path){
		if(haveCount(type,path)) return countMap.get(type).get(path);
		return 0;
	}
	public static boolean haveCount(NameEnumType type, String path){
		if(countMap.containsKey(type) && countMap.get(type).containsKey(path)) return true;
		return false;
	}

	public static NameEnumType getContextObjectType() {
		return contextObjectType;
	}


	public static void setContextObjectType(NameEnumType contextObjectType) {
		ClientContext.contextObjectType = contextObjectType;
	}


	public static DirectoryGroupType getHomeDirectory() {
		return homeDirectory;
	}

	public static void setHomeDirectory(DirectoryGroupType homeDirectory) {
		ClientContext.homeDirectory = homeDirectory;
	}


	public static void applyContext(UserType usr){
		user = usr;
		logger.info(JSONUtil.exportObject(usr));
		organizationPath = usr.getOrganizationPath();
		if(user.getHomeDirectory() != null){
			homeDirectory = user.getHomeDirectory();
		}
		else{
			homeDirectory = AM6Util.findObject(DirectoryGroupType.class, NameEnumType.GROUP, "DATA", "~");
		}
		currentDirectory = homeDirectory;
			//organizationId = usr.getOrganizationId();
		//currentOrganization = null;
		//OrganizationUtil.readById(organizationId);
		//applyContext(user.getSession());
	}
	/*
	public static void applyContext(UserSessionType session){
		if(session != null){
			sessionId = session.getSessionId();
			sessionStatus = session.getSessionStatus();
			if(sessionStatus == SessionStatusEnumType.AUTHENTICATED){
				/ *
				DirectoryGroupType homeDir = GroupUtil.getHomeDirectory();
				if(homeDir == null) logger.error("Failed to retrieve home directory");
				setCurrentDirectory(GroupUtil.getHomeDirectory());
				* /
			}
		}
		else{
			//sessionId = null;
			sessionStatus = sessionStatus.UNKNOWN;
		}

	}
	*/
	public static UserType getUser() {
		return user;
	}
	public static void setUser(UserType user) {
		ClientContext.user = user;
	}
	public static DirectoryGroupType getCurrentDirectory() {
		return currentDirectory;
	}
	public static void setCurrentDirectory(DirectoryGroupType currentDirectory) {
		ClientContext.currentDirectory = currentDirectory;
	}

}
