package org.cote.accountmanager.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;

public class CommunityContext {
	public static final Logger logger = LogManager.getLogger(CommunityContext.class.getName());
	private String communityId = null;
	private String communityName = null;
	private LifecycleType community = null;
	private Map<String,ProjectType> projects = null;
	private Map<String,Map<String,BaseRoleType>> roles = null;
	private Map<String,Map<String,DirectoryGroupType>> applications = null;
	private Map<String,Map<String,DirectoryGroupType>> dirs = null;
	private Map<String,Map<String,PersonType>> persons = null;
	private Map<String,Map<String,AccountType>> accounts = null;
	private Map<String,Map<String,BaseGroupType>> groups = null;
	private Map<String,Map<String,BasePermissionType>> permissions = null;
	private Map<String, Map<String,BasePermissionType>> appPermissionBases = null;
	private ClientContext communityAdminContext = null;
	private boolean initialized = false;
	public CommunityContext(ClientContext adminContext) {
		projects = new HashMap<>();
		roles = new HashMap<>();
		applications = new HashMap<>();
		persons = new HashMap<>();
		accounts = new HashMap<>();
		groups = new HashMap<>();
		dirs = new HashMap<>();
		permissions = new HashMap<>();
		appPermissionBases = new HashMap<>();
		communityAdminContext = adminContext;
		
	}
	public CommunityContext(ClientContext adminContext, String communityName) {
		this(adminContext);
		this.communityName = communityName;
	}

	public boolean clearCache() {
		return CacheUtil.clearCache(communityAdminContext);
	}
	public boolean createCommunity(String communityName) {
		LifecycleType lifecycle = AM6Util.findCommunity(communityAdminContext, LifecycleType.class, communityName);
		if(lifecycle != null) {
			return  false;
		}
		if(AM6Util.addCommunity(communityAdminContext, Boolean.class, communityName)) {
			lifecycle = AM6Util.findCommunity(communityAdminContext, LifecycleType.class, communityName);
			if(lifecycle != null) {
				communityId = lifecycle.getObjectId();
				community = lifecycle;
				return true;
			}
			else {
				logger.error("Community was created, but failed to retrieve");
				return false;
			}
		}
		return false;
	}
	/*
	public <T> T getApplicationPermission(String name,PermissionEnumType type, DirectoryGroupType dir){
		T per = null;
		try {
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(dir);
			String perPath = dir.getPath() + "/" + name;
			per = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(testUser, type, perPath, dir.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return per;
	}



	 */
	public <T> T getCreateProjectRole(String projectName, RoleEnumType type, String name) {
		T role = getProjectRole(projectName, type, name);
		if(role != null) return role;
		return createProjectRole(projectName, type, name);
	}
	public <T> T createProjectRole(String projectName, RoleEnumType type, String name) {
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}
		ProjectType project  = getProject(projectName);
		if(project == null) {
			logger.error("Null project for " + projectName);
			return null;
		}
		BaseRoleType parentRole = AM6Util.getCommunityProjectRoleBase(communityAdminContext, BaseRoleType.class,  project.getObjectId());
		if(parentRole == null) {
			logger.error("Failed to retrieve role base for project " + projectName);
			return null;
		}
		BaseRoleType role = new BaseRoleType();
		role.setName(name);
		role.setRoleType(type);
		role.setParentId(parentRole.getId());
		//role.setParentPath(parentRole.getParentPath() + "/" + parentRole.getName());
		role.setNameType(NameEnumType.ROLE);
		boolean created = AM6Util.updateObject(communityAdminContext, Boolean.class, role);
		if(!created) {
			logger.error("Failed to create role " + name);
			return null;
		}
		return getProjectRole(projectName, type, name);
	}
	public <T> T getProjectRole(String projectName, RoleEnumType type, String name){
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}

		if(roles.containsKey(projectName) && roles.get(projectName).containsKey(name)) {
			return (T)roles.get(projectName).get(name);
		}
		ProjectType project  = getProject(projectName);
		if(project == null) {
			logger.error("Null project for " + projectName);
			return null;
		}
		BaseRoleType parentRole = AM6Util.getCommunityProjectRoleBase(communityAdminContext, BaseRoleType.class,  project.getObjectId());
		if(parentRole == null) {
			logger.error("Failed to retrieve role base for project " + projectName);
			return null;
		}

		BaseRoleType role = AM6Util.getObjectByName(communityAdminContext, BaseRoleType.class, NameEnumType.ROLE, parentRole.getObjectId(), name, true);
		if(role != null) {
			if(!roles.containsKey(projectName)) roles.put(projectName, new HashMap<>());
			roles.get(projectName).put(name, role);
		}
		return (T)role;
	}
	
	public boolean adopt(PersonType person, AccountType account) {
		boolean adopt = false;

		if(!person.getPopulated()) {
			logger.warn("Person object is not populated.  This will result in any unpopulated values being detached from this object.");
			return false;
		}
		for(AccountType acct : person.getAccounts()) {
			if(acct.getObjectId().contentEquals(account.getObjectId())) {
				adopt = true;
				break;
			}
		}
		if(!adopt) {
			person.getAccounts().add(account);
			adopt = AM6Util.updateObject(communityAdminContext, Boolean.class, person);
		}
		return adopt;
	}
	public AccountType getCreateAccount(String projectName, String applicationName, String accountName) {
		AccountType acct = getAccount(projectName, applicationName, accountName);
		if(acct == null) acct = createAccount(projectName, applicationName, accountName);
		return acct;
	}
	public AccountType createAccount(String projectName, String applicationName, String accountName) {

		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}	

		DirectoryGroupType app = getApplication(projectName, applicationName);
		if(app == null) {
			logger.error("Null or inaccessible application '" + applicationName + "'");
			return null;
		}
		AccountType newAccount = new AccountType();
		newAccount.setGroupPath(app.getPath());
		newAccount.setNameType(NameEnumType.ACCOUNT);
		newAccount.setAccountType(AccountEnumType.SYSTEM);
		newAccount.setAccountStatus(AccountStatusEnumType.RESTRICTED);
		newAccount.setName(accountName);
		boolean created = AM6Util.updateObject(communityAdminContext, Boolean.class, newAccount);
		if(!created) {
			logger.error("Failed to created community project application account '" + applicationName + "'");
			return null;
		}
		return getAccount(projectName, applicationName, accountName);
	}
	public AccountType getAccount(String projectName, String applicationName, String accountName) {
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}
		DirectoryGroupType application = getApplication(projectName, applicationName);
		if(application == null) {
			logger.error("Application is null or not accessible");
			return null;
		}
		String appObjId = application.getObjectId();
		if(accounts.containsKey(appObjId) && accounts.get(appObjId).containsKey(accountName)) {
			return accounts.get(appObjId).get(accountName);
		}
		
		
		AccountType account = AM6Util.getObjectByName(communityAdminContext, AccountType.class, NameEnumType.ACCOUNT, appObjId, accountName, false);
		if(account != null) {
			if(!accounts.containsKey(appObjId)) accounts.put(appObjId,new HashMap<>());
			accounts.get(appObjId).put(accountName,account);
		}
		return account;
	}
	public BaseGroupType getCreateAccountGroup(String projectName, String applicationName, String groupName) {
		BaseGroupType acct = getAccountGroup(projectName, applicationName, groupName);
		if(acct == null) acct = createAccountGroup(projectName, applicationName, groupName);
		return acct;
	}
	public BaseGroupType createAccountGroup(String projectName, String applicationName, String groupName) {

		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}	

		DirectoryGroupType app = getApplication(projectName, applicationName);
		if(app == null) {
			logger.error("Null or inaccessible application '" + applicationName + "'");
			return null;
		}
		BaseGroupType newGroup = new BaseGroupType();
		/// newGroup.setPath(app.getPath());
		newGroup.setParentId(app.getId());
		newGroup.setNameType(NameEnumType.GROUP);
		newGroup.setGroupType(GroupEnumType.ACCOUNT);
		newGroup.setName(groupName);
		logger.info("Creating group:\n" + JSONUtil.exportObject(newGroup));
		boolean created = AM6Util.updateObject(communityAdminContext, Boolean.class, newGroup);
		if(!created) {
			logger.error("Failed to created community project application group '" + groupName + "'");
			return null;
		}
		return getAccountGroup(projectName, applicationName, groupName);
	}
	
	public DirectoryGroupType getProjectDirectory(String projectName, String projectGroupName) {
		ProjectType project = getProject(projectName);
		String path = AM6Util.getEncodedPath(project.getGroupPath() + "/" + projectGroupName);
		
		String cache_key = CacheUtil.getCacheKeyName("GROUP-" + communityName + "-" + projectName + "-" + projectGroupName);
		DirectoryGroupType grp = CacheUtil.readCache(communityAdminContext, cache_key, DirectoryGroupType.class);
		if(grp == null) {
			grp = AM6Util.findObject(communityAdminContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", path);
			if(grp == null) {
				logger.error("Failed to find project group for " + path);
				return null;
			}
			CacheUtil.cache(communityAdminContext, cache_key, grp);
		}
		return grp;
	}
	
	public BaseGroupType getAccountGroup(String projectName, String applicationName, String groupName) {
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}
		DirectoryGroupType application = getApplication(projectName, applicationName);
		if(application == null) {
			logger.error("Application is null or not accessible");
			return null;
		}
		String appObjId = application.getObjectId();
		if(groups.containsKey(appObjId) && groups.get(appObjId).containsKey(groupName)) {
			return groups.get(appObjId).get(groupName);
		}

		BaseGroupType group = AM6Util.findObject(communityAdminContext, AccountGroupType.class, NameEnumType.GROUP, "UNKNOWN", application.getPath() + "/" + groupName);
		if(group != null) {
			if(!groups.containsKey(appObjId)) groups.put(appObjId,new HashMap<>());
			groups.get(appObjId).put(groupName,group);
		}
		return group;
	}
	
	/// Bug - there seems to be a bug where the permission path (and I assume role path) is being emitted
	/// within the factory find layer, behind the authorization, which can lead to an alternate permission branch being emitted with an UKNOWN type
	/// Hence the manual path unwinding being done here for the moment.
	public BasePermissionType getApplicationPermissionBase(String projectName, String applicationName) {
		if(appPermissionBases.containsKey(projectName) && appPermissionBases.get(projectName).containsKey(applicationName)) {
			return appPermissionBases.get(projectName).get(applicationName);
		}
		if(!appPermissionBases.containsKey(projectName)) appPermissionBases.put(projectName,new HashMap<>());
		ProjectType project = getProject(projectName);
		if(project == null) {
			logger.error("Failed to find project " + projectName);
			return null;
		}
		String cache_key = CacheUtil.getCacheKeyName("PERMISSION-" + communityName + "-" + projectName);
		BasePermissionType per1 = CacheUtil.readCache(communityAdminContext, cache_key, BasePermissionType.class);
		if(per1 == null) {
			per1 = AM6Util.getCommunityProjectPermissionBase(communityAdminContext, BasePermissionType.class, project.getObjectId());
		
			if(per1 == null) {
				logger.error("Failed to find project permission base for " + projectName);
				return null;
			}
			CacheUtil.cache(communityAdminContext, cache_key, per1);
		}
		cache_key = CacheUtil.getCacheKeyName("PERMISSION-" + communityName + "-" + projectName + "-Applications");
		BasePermissionType per2 = CacheUtil.readCache(communityAdminContext, cache_key, BasePermissionType.class);
		if(per2 == null) {
			per2 = AM6Util.getObjectByName(communityAdminContext, BasePermissionType.class, NameEnumType.PERMISSION, per1.getObjectId(), "Applications", true);
			if(per2 == null) {
				logger.error("Failed to find applications permission base within " + projectName);
				return null;
			}
			CacheUtil.cache(communityAdminContext, cache_key, per2);
		}
		
		cache_key = CacheUtil.getCacheKeyName("PERMISSION-" + communityName + "-" + projectName + "-Applications-" + applicationName);
		BasePermissionType per = CacheUtil.readCache(communityAdminContext, cache_key, BasePermissionType.class);
		if(per == null) {
			per = AM6Util.getObjectByName(communityAdminContext, BasePermissionType.class, NameEnumType.PERMISSION, per2.getObjectId(), applicationName, true);
			if(per == null) {
				logger.error("Failed to find application permission base for " + applicationName);
				return null;
			}
			CacheUtil.cache(communityAdminContext, cache_key, per);
		}
		if(per != null) {
			appPermissionBases.get(projectName).put(applicationName,per);
		}
		return per;
		
		
	}
	public BasePermissionType getCreateApplicationPermission(String projectName, String applicationName, String permissionName) {
		BasePermissionType per = getApplicationPermission(projectName, applicationName, permissionName);
		if(per == null) per = createApplicationPermission(projectName, applicationName, permissionName);
		return per;
	}
	public BasePermissionType createApplicationPermission(String projectName, String applicationName, String permissionName) {

		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}	
		ProjectType project  = getProject(projectName);
		if(project == null) {
			logger.error("Null project for " + projectName);
			return null;
		}
		DirectoryGroupType app = getApplication(projectName, applicationName);
		if(app == null) {
			logger.error("Null or inaccessible application '" + applicationName + "'");
			return null;
		}
		/// BasePermissionType per = AM6Util.findObject(communityAdminContext, BasePermissionType.class, NameEnumType.PERMISSION, "UNKNOWN", app.getPath());
		BasePermissionType per = getApplicationPermissionBase(projectName, applicationName);
		
		if(per == null) {
			logger.error("Failed to find application base permission");
			return null;
		}
		logger.info("********** Parent: " + per.getParentPath());
		BasePermissionType newPer = new BasePermissionType();
		newPer.setParentId(per.getId());
		newPer.setParentPath(per.getParentPath() + "/" + per.getName());
		newPer.setNameType(NameEnumType.PERMISSION);
		newPer.setPermissionType(PermissionEnumType.APPLICATION);

		newPer.setName(permissionName);

		boolean created = AM6Util.updateObject(communityAdminContext, Boolean.class, newPer);
		if(!created) {
			logger.error("Failed to created community project application permission '" + permissionName + "'");
			return null;
		}
		return getApplicationPermission(projectName, applicationName, permissionName);
	}
	public BasePermissionType getApplicationPermission(String projectName, String applicationName, String permissionName) {
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}
		ProjectType project  = getProject(projectName);
		if(project == null) {
			logger.error("Null project for " + projectName);
			return null;
		}
		DirectoryGroupType application = getApplication(projectName, applicationName);
		if(application == null) {
			logger.error("Application is null or not accessible");
			return null;
		}
		String appObjId = application.getObjectId();
		if(permissions.containsKey(appObjId) && permissions.get(appObjId).containsKey(permissionName)) {
			return permissions.get(appObjId).get(permissionName);
		}
		
		/// BasePermissionType permission = AM6Util.findObject(communityAdminContext, BasePermissionType.class, NameEnumType.PERMISSION, "APPLICATION", application.getPath() + "/" + permissionName);
		String cacheKey = CacheUtil.getCacheKeyName("PERMISSION-" + communityName + "-" + projectName + "-" + applicationName + "-" + permissionName);
		BasePermissionType permission = CacheUtil.readCache(communityAdminContext, cacheKey, BasePermissionType.class);
		if(permission == null) {
			BasePermissionType per = getApplicationPermissionBase(projectName, applicationName);
			permission = AM6Util.getObjectByName(communityAdminContext, BasePermissionType.class, NameEnumType.PERMISSION, per.getObjectId(), permissionName, true);
			if(permission != null) {
				CacheUtil.cache(communityAdminContext, cacheKey, permission);
			}
		}
		if(permission != null) {
			if(!permissions.containsKey(appObjId)) permissions.put(appObjId,new HashMap<>());
			permissions.get(appObjId).put(permissionName,permission);
		}
		return permission;
	}
	
	public PersonType getCreatePerson(String projectName, String personName) {
		PersonType per = getPerson(projectName, personName);
		if(per == null) per = createPerson(projectName, personName);
		return per;
	}
	public PersonType createPerson(String projectName, String personName) {

		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}	
		ProjectType proj = getProject(projectName);
		if(proj == null) {
			logger.error("Null or inaccessible project '" + projectName + "'");
			return null;
		}
		
		PersonType newPerson = new PersonType();
		newPerson.setName(personName);
		newPerson.setGroupPath(proj.getGroupPath() + "/Persons");
		newPerson.setNameType(NameEnumType.PERSON);
		boolean created = AM6Util.updateObject(communityAdminContext, Boolean.class, newPerson);
		if(!created) {
			logger.error("Failed to created community project person '" + personName + "'");
			return null;
		}
		return getPerson(projectName, personName);
	}
	public PersonType getPerson(String projectName, String personName) {
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}
		if(persons.containsKey(projectName) && persons.get(projectName).containsKey(personName)) {
			return persons.get(projectName).get(personName);
		}
		ProjectType proj = getProject(projectName);
		if(proj == null) {
			logger.error("Null or inaccessible project '" + projectName + "'");
			return null;
		}
		if(personName == null) {
			logger.error("Null person name");
			return null;
		}
		DirectoryGroupType perDir = AM6Util.findObject(communityAdminContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", proj.getGroupPath() + "/Persons");
		if(perDir == null) {
			logger.error("Person directory is null");
			return null;
		}
		PersonType person = AM6Util.getObjectByName(communityAdminContext, PersonType.class, NameEnumType.PERSON, perDir.getObjectId(), personName, false);
		if(person != null) {
			if(!persons.containsKey(projectName)) persons.put(projectName,new HashMap<>());
			persons.get(projectName).put(personName,person);
		}
		return person;
	}
	
	public DirectoryGroupType getCreateApplication(String projectName, String applicationName) {
		DirectoryGroupType app = getApplication(projectName, applicationName);
		if(app == null) app = createApplication(projectName, applicationName);
		return app;
	}
	public DirectoryGroupType createApplication(String projectName, String applicationName) {

		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}	
		ProjectType proj = getProject(projectName);
		if(proj == null) {
			logger.error("Null or inaccessible project '" + projectName + "'");
			return null;
		}
		boolean created = AM6Util.createCommunityApplication(communityAdminContext, Boolean.class, communityId, proj.getObjectId(), applicationName);
		if(!created) {
			logger.error("Failed to created community project application '" + applicationName + "'");
			return null;
		}
		return getApplication(projectName, applicationName);
	}
	public DirectoryGroupType getApplication(String projectName, String applicationName) {
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}
		if(applications.containsKey(projectName) && applications.get(projectName).containsKey(applicationName)) {
			return applications.get(projectName).get(applicationName);
		}

		if(applicationName == null) {
			logger.error("Null application name");
			return null;
		}
		String cacheKey = CacheUtil.getCacheKeyName("GROUP-" + communityName + "-" + projectName + "-Applications-" + applicationName);
		DirectoryGroupType appDir = CacheUtil.readCache(communityAdminContext, cacheKey, DirectoryGroupType.class);
		if(appDir == null) {
			ProjectType proj = getProject(projectName);
			if(proj == null) {
				logger.error("Null or inaccessible project '" + projectName + "'");
				return null;
			}
			appDir = AM6Util.getCommunityApplication(communityAdminContext, DirectoryGroupType.class, communityId, proj.getObjectId(), applicationName);
			if(appDir != null) {
				CacheUtil.cache(communityAdminContext, cacheKey, appDir);
			}
		}
		if(appDir != null) {
			if(!applications.containsKey(projectName)) applications.put(projectName,new HashMap<>());
			applications.get(projectName).put(applicationName,appDir);
		}
		return appDir;
	}
	public ProjectType getCreateProject(String projectName) {
		ProjectType pt = getProject(projectName);
		if(pt == null) pt = createProject(projectName);
		return pt;
	}
	public ProjectType createProject(String projectName) {
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}
		boolean created = AM6Util.addCommunityProject(communityAdminContext, Boolean.class, communityId, projectName);
		if(!created) {
			logger.error("Failed to created community project '" + projectName + "'");
			return null;
		}
		return getProject(projectName);
	}
	public ProjectType getProject(String projectName) {
		if(!initialized) {
			logger.error("Community is not initialized");
			return null;
		}
		if(projects.containsKey(projectName)) {
			return projects.get(projectName);
		}
		String cacheKey = CacheUtil.getCacheKeyName("PROJECT-" + communityName + "-" + projectName);
		ProjectType lt = CacheUtil.readCache(communityAdminContext, cacheKey, ProjectType.class);
		if(lt == null) {
			lt = AM6Util.findCommunityProject(communityAdminContext, ProjectType.class, communityName, projectName);
		
			if(lt == null) {
				logger.error("Project '" + projectName + "' was not accessible or does not exist");
				return null;
			}
			CacheUtil.cache(communityAdminContext, cacheKey, lt);
		}
		if(lt != null) {
			projects.put(projectName, lt);
		}
		
		return lt;
	}
	public boolean initialize() {
		boolean outBool = false;
		if(initialized) return true;
		if(communityName == null) {
			logger.warn("Community name is null");
			return outBool;
		}
		if(community!=null) {
			initialized = true;
			return true;
		}
		// clearCache();
		String cacheKey = "LIFECYCLE-" + communityName;
		community = CacheUtil.readCache(communityAdminContext, communityName, LifeCycle.class);
		if(community == null) {
			community = AM6Util.findCommunity(communityAdminContext, LifecycleType.class, communityName);
			if(community != null) {
				CacheUtil.cache(communityAdminContext, cacheKey, community); 
			}
			//community = AM6Util.getObject(communityAdminContext, LifecycleType.class, NameEnumType.LIFECYCLE, communityId);
		}
		if(community != null) {
			outBool = true;
			communityId = community.getObjectId();
			initialized = true;
			logger.info("Community context for " + communityName + " initialized");
		}
		
		return outBool;
	}

	public String getCommunityName() {
		return communityName;
	}
	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}
	public boolean isInitialized() {
		return initialized;
	}

	public String getCommunityId() {
		return communityId;
	}

	public void setCommunityId(String communityId) {
		this.communityId = communityId;
	}

	public LifecycleType getCommunity() {
		return community;
	}

	public void setCommunity(LifecycleType community) {
		this.community = community;
	}

	public Map<String, ProjectType> getProjects() {
		return projects;
	}

	public void setProjects(Map<String, ProjectType> projects) {
		this.projects = projects;
	}
	
	public static boolean enrollReaderInCommunityProject(ClientContext adminContext, String userId, String communityId, String projectId) {
		
		return
		(
			AM6Util.enrollCommunitiesReader(adminContext, Boolean.class, userId)
			&& AM6Util.enrollCommunityReader(adminContext, Boolean.class, communityId, userId)
			&& AM6Util.enrollCommunityProjectReader(adminContext, Boolean.class, communityId, projectId, userId)
		);
	}
	public static boolean enrollAdminInCommunityProject(ClientContext adminContext, String userId, String communityId, String projectId) {
		
		return
		(
			AM6Util.enrollCommunitiesReader(adminContext, Boolean.class, userId)
			&& AM6Util.enrollCommunityReader(adminContext, Boolean.class, communityId, userId)
			&& AM6Util.enrollCommunityProjectAdmin(adminContext, Boolean.class, communityId, projectId, userId)
		);
	}
}
