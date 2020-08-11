package org.cote.accountmanager.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.CacheUtil;
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
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.JSONUtil;

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
	private Map<NameEnumType,Map<String,Integer>> countMap = new HashMap<NameEnumType,Map<String,Integer>>();

	private String defaultContextId = CacheUtil.cleanPattern.matcher(BinaryUtil.toBase64Str("0000")).replaceAll("");
	private String contextId = defaultContextId;
	public ClientContext() {
		
	}
	public ClientContext(UserType usr) {
		this.applyContext(usr);
	}
	public void clearContext(){
		user = null;
		//sessionId = null;
		//organizationId = 0L;
		//sessionStatus = SessionStatusEnumType.UNKNOWN;
		authenticationStatus = AuthenticationResponseEnumType.UNKNOWN;
		authenticationCredential = null;
		currentDirectory = null;
		homeDirectory = null;
		contextId = defaultContextId;
	}
	public String getContextId() {
		return contextId;
	}
	public String getOrganizationPath() {
		return organizationPath;
	}

	public void setOrganizationPath(String organizationPath) {
		ClientContext.organizationPath = organizationPath;
	}

	public ApiClientConfigurationType getApiConfiguration() {
		return apiConfiguration;
	}

	public void setApiConfiguration(ApiClientConfigurationType apiConfiguration) {
		ClientContext.apiConfiguration = apiConfiguration;
	}

	public AuthenticationResponseEnumType getAuthenticationStatus() {
		return authenticationStatus;
	}

	public void setAuthenticationStatus(AuthenticationResponseEnumType authenticationStatus) {
		ClientContext.authenticationStatus = authenticationStatus;
	}

	public CredentialType getAuthenticationCredential() {
		return authenticationCredential;
	}

	public void setAuthenticationCredential(CredentialType authenticationCredential) {
		ClientContext.authenticationCredential = authenticationCredential;
	}

	public void setCount(NameEnumType type, String path, int count){
		if(countMap.containsKey(type) == false) countMap.put(type, new HashMap<String,Integer>());
		countMap.get(type).put(path, count);
	}
	public int getCount(NameEnumType type, String path){
		if(haveCount(type,path)) return countMap.get(type).get(path);
		return 0;
	}
	public boolean haveCount(NameEnumType type, String path){
		if(countMap.containsKey(type) && countMap.get(type).containsKey(path)) return true;
		return false;
	}

	public NameEnumType getContextObjectType() {
		return contextObjectType;
	}


	public void setContextObjectType(NameEnumType contextObjectType) {
		ClientContext.contextObjectType = contextObjectType;
	}


	public DirectoryGroupType getHomeDirectory() {
		return homeDirectory;
	}

	public void setHomeDirectory(DirectoryGroupType homeDirectory) {
		ClientContext.homeDirectory = homeDirectory;
	}


	public void applyContext(UserType usr){
		user = usr;
		if(usr == null) {
			contextId = defaultContextId;
			return;
		}
		/// logger.info(JSONUtil.exportObject(usr));
		organizationPath = usr.getOrganizationPath();
		if(user.getHomeDirectory() != null){
			homeDirectory = user.getHomeDirectory();
		}
		else{
			homeDirectory = AM6Util.findObject(this, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", "~");
		}
		currentDirectory = homeDirectory;
		contextId = CacheUtil.cleanPattern.matcher(BinaryUtil.toBase64Str(user.getUrn())).replaceAll("");
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
	public UserType getUser() {
		return user;
	}
	public void setUser(UserType user) {
		ClientContext.user = user;
	}
	public DirectoryGroupType getCurrentDirectory() {
		return currentDirectory;
	}
	public void setCurrentDirectory(DirectoryGroupType currentDirectory) {
		ClientContext.currentDirectory = currentDirectory;
	}

}
