package org.cote.accountmanager.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.objects.ApiClientConfigurationType;
import org.cote.accountmanager.objects.AuthenticationResponseEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.BinaryUtil;

public class ClientContext {
	public static final Logger logger = LogManager.getLogger(ClientContext.class);
	private NameEnumType contextObjectType = NameEnumType.UNKNOWN;
	private UserType user = null;
	private DirectoryGroupType currentDirectory = null;
	private DirectoryGroupType homeDirectory = null;
	private AuthenticationResponseEnumType authenticationStatus = AuthenticationResponseEnumType.UNKNOWN;
	private CredentialType authenticationCredential = null;
	private ApiClientConfigurationType apiConfiguration = null;
	private String organizationPath = null;
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
	public void setContextObjectType(NameEnumType contextObjectType) {
		this.contextObjectType = contextObjectType;
	}
	public void setUser(UserType user) {
		this.user = user;
	}
	public void setCurrentDirectory(DirectoryGroupType currentDirectory) {
		this.currentDirectory = currentDirectory;
	}
	public void setHomeDirectory(DirectoryGroupType homeDirectory) {
		this.homeDirectory = homeDirectory;
	}
	public void setOrganizationPath(String organizationPath) {
		this.organizationPath = organizationPath;
	}
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}
	public String getContextId() {
		return contextId;
	}
	public String getDefaultContextId() {
		return defaultContextId;
	}
	public String getOrganizationPath() {
		return organizationPath;
	}


	public ApiClientConfigurationType getApiConfiguration() {
		return apiConfiguration;
	}

	public void setApiConfiguration(ApiClientConfigurationType apiConfiguration) {
		apiConfiguration = apiConfiguration;
	}

	public AuthenticationResponseEnumType getAuthenticationStatus() {
		return authenticationStatus;
	}

	public void setAuthenticationStatus(AuthenticationResponseEnumType authenticationStatus) {
		authenticationStatus = authenticationStatus;
	}

	public CredentialType getAuthenticationCredential() {
		return authenticationCredential;
	}



	public void setAuthenticationCredential(CredentialType authenticationCredential) {
		this.authenticationCredential = authenticationCredential;
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


	public DirectoryGroupType getHomeDirectory() {
		return homeDirectory;
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

	public DirectoryGroupType getCurrentDirectory() {
		return currentDirectory;
	}

}
