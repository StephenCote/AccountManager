package org.cote.accountmanager.client.test;
/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.ClientSigningKeyResolver;
import org.cote.accountmanager.client.CommunityContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.AuthenticationUtil;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.client.util.ClientUtil;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApiClientConfigurationType;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.AuthenticationResponseEnumType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.junit.After;
import org.junit.Before;
public class BaseClientTest{
	public static final Logger logger = LogManager.getLogger(BaseClientTest.class);
	
	private String countryCodes = "CA,US,MX";
	private int epochCount = 50;
	private int populationSeedSize = 10000;
	private int locationSeedSize = 3;
	
	private static String clientReceiverOrganization = null;
	private static String clientReceiverName = null;
	private static String clientReceiverPassword = null;
	private static ClientContext clientReceiverContext = null;
	
	private static String testAdminName = "Admin";
	private static String testAdminPassword = null;
	private static String testUserName = null;
	private static String testUserOrganization = null;
	private static String testUserPassword = null;
	
	protected static String testCommunityName = null;
	protected static String testProjectName = null;
	protected static String testApplication1Name = null;
	protected static String testPerson1Name = null;
	protected static String testAccount1Name = null;
	protected static String testAccountGroup1Name = null;
	protected static UserType adminUser = null;
	protected static ClientContext testAdminContext = null;
	protected static UserType testUser = null;
	protected static ClientContext defaultContext = new ClientContext();
	protected static ClientContext testUserContext = null;
	private static String sessionId = null;
	protected static String serviceUrl = null;
	protected static String serviceName = null;
	private static Properties testProperties = null;
	

	@Before
	public void setUp() throws Exception {
		
		File cacheDir = new File("./cache");
		if(cacheDir.exists() == false) cacheDir.mkdirs();
		
		if(testProperties == null){
			testProperties = new Properties();
		
			try {
				InputStream fis = ClassLoader.getSystemResourceAsStream("./resource.properties"); 
				testProperties.load(fis);
				fis.close();
			} catch (IOException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				return;
			}
		}
		
		
		
		testUserOrganization = testProperties.getProperty("test.user1.organization");
		testUserName = testProperties.getProperty("test.user1.name");
		testUserPassword = testProperties.getProperty("test.user1.password");
		testAdminPassword = testProperties.getProperty("test.admin.password");
		testCommunityName = testProperties.getProperty("test.community.name");
		testProjectName = testProperties.getProperty("test.project.name");
		testApplication1Name = testProperties.getProperty("test.application1.name");
		testAccount1Name = testProperties.getProperty("test.account1.name");
		testAccountGroup1Name = testProperties.getProperty("test.accountgroup1.name");
		testPerson1Name = testProperties.getProperty("test.person1.name");
		testUserContext = new ClientContext();
		testAdminContext = new ClientContext();
		String serviceBase = testProperties.getProperty("service.url.base");
		String serviceApp = testProperties.getProperty("service.url.app");
		ClientUtil.setServer(serviceBase);
		ClientUtil.setAccountManagerApp(serviceApp);
		serviceUrl = serviceBase + serviceApp;
		serviceName = testProperties.getProperty("service.name");
		
		clientReceiverContext = new ClientContext();
		clientReceiverOrganization = testProperties.getProperty("client.receiver.organization");
		clientReceiverName = testProperties.getProperty("client.receiver.name");
		clientReceiverPassword = testProperties.getProperty("client.receiver.password");
		
		ApiClientConfigurationType api = AuthenticationUtil.getApiConfiguration(serviceUrl);
		CacheUtil.cache(defaultContext, serviceName, api);
		
		// logger.info("Names: " + testAdminName + ", " + testUserName);

		AuthenticationResponseType art = AuthenticationUtil.authenticate(clientReceiverContext, serviceName, clientReceiverOrganization, clientReceiverName, clientReceiverPassword);
		assertNotNull("Client receiver authentication response is null", art);
		if(art != null) {
			assertNotNull("Client receiver user is null.  This must be a user in the system organization.",art.getUser());
		}
		ClientSigningKeyResolver.setResolverContext(clientReceiverContext);

		
		
		art = AuthenticationUtil.authenticate(testAdminContext, serviceName, testUserOrganization, testAdminName, testAdminPassword);
		if(art != null) {
			adminUser = art.getUser();
			assertNotNull("Test admin is null",adminUser);
		}

		assertTrue("Community is not configured",configureCommunity(testAdminContext));
		art = AuthenticationUtil.authenticate(testUserContext, serviceName, testUserOrganization, testUserName, testUserPassword);
		assertNotNull("3 Admin token is null", testAdminContext.getAuthenticationCredential());

//		logger.info(JSONUtil.exportObject(testAdminContext));
		try {
			if(art == null || !art.getResponse().equals(AuthenticationResponseEnumType.AUTHENTICATED)) {
				UserType user = getCreateUser(testAdminContext, testUserName);
				assertNotNull("Test user object is null", user);
				if(addCredential(testAdminContext, user, testUserPassword)) {
					art = AuthenticationUtil.authenticate(testUserContext, serviceName, testUserOrganization, testUserName, testUserPassword);
				}
			}
			else {
				logger.debug("Art isn't null: " + JSONUtil.exportObject(art));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		if(art != null) {
			testUser = art.getUser();
			assertNotNull("Authentication user is null",testUser);
			//logger.info("TestUser = " + (testUser == null ? " null " : " not null "));
		}
		
		CacheUtil.clearCache(testUserContext);
		CacheUtil.clearCache(testAdminContext);
	}
	
	@After
	public void tearDown() throws Exception{

		/// SessionSecurity.logout(sessionId, testOrganization.getId());
	}
	
	protected CommunityContext prepareCommunityContext() {
		AM6Util.clearCache(testAdminContext, NameEnumType.UNKNOWN);
		
		assertNotNull("User context is null", testUserContext);
		assertNotNull("User context is null", testAdminContext);
		LifecycleType community = getCreateCommunity(testAdminContext, testCommunityName, false);
		
		assertNotNull("Community is null", community);

		CommunityContext cc = new CommunityContext(testAdminContext, testCommunityName);
		assertTrue("Failed to initialize community context", cc.initialize());
		
		ProjectType project = cc.getCreateProject(testProjectName);
		assertNotNull("Project is null", project);
		
		int eventCount = org.cote.accountmanager.client.Client.countEvents(testAdminContext, project);
		logger.info("Event count: " + eventCount);
		if(eventCount <= 0) {
			assertTrue("Failed to load country info", org.cote.accountmanager.client.Client.loadCommunityCountryInformation(testAdminContext, community, countryCodes));
			assertTrue("Failed to load project regions", org.cote.accountmanager.client.Client.loadProjectRegions(testAdminContext, community,project,locationSeedSize,populationSeedSize));
			assertTrue("Failed to evolve project regions", org.cote.accountmanager.client.Client.evolveProjectRegions(testAdminContext, community, project, epochCount));
		}

		AM6Util.enrollCommunitiesReader(testAdminContext, Boolean.class, testUserContext.getUser().getObjectId());
		AM6Util.enrollCommunityReader(testAdminContext, Boolean.class, community.getObjectId(), testUserContext.getUser().getObjectId());
		AM6Util.enrollCommunityProjectAdmin(testAdminContext, Boolean.class, community.getObjectId(), project.getObjectId(), testUserContext.getUser().getObjectId());

		LifecycleType checkL = AM6Util.getObject(testUserContext, LifecycleType.class, NameEnumType.LIFECYCLE, community.getObjectId());
		assertNotNull("Test user should be able to read the lifecycle",checkL);

		ProjectType checkP = AM6Util.getObject(testUserContext, ProjectType.class, NameEnumType.PROJECT, project.getObjectId());
		assertNotNull("Test user should be able to read the project",checkP);
		
		String personPath = AM6Util.getEncodedPath(project.getGroupPath() + "/Persons");
		
		DirectoryGroupType personDir = 	AM6Util.findObject(testUserContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", personPath);
		assertNotNull("Person directory is null", personDir);
		
		String objType = "USER";
		String objId = testUserContext.getUser().getObjectId();
		List<EntitlementType> ents = AM6Util.getEntitlements(testUserContext, new ArrayList<EntitlementType>().getClass(), objType, objId);
		logger.info("Entitlements: " + ents.size());
		
		List<BaseRoleType> commRoles = AM6Util.listCommunityRoles(testUserContext, new ArrayList<BaseRoleType>().getClass());
		logger.info("Role count: " + commRoles.size());
		
		DirectoryGroupType appDir = cc.getCreateApplication(testProjectName, testApplication1Name);

		assertNotNull("Application " + testApplication1Name + " is null", appDir);
		AccountType account = cc.getCreateAccount(testProjectName, testApplication1Name, testAccount1Name);
		assertNotNull("Account " + testAccount1Name + " is null",account);
		
		BaseGroupType group = cc.getCreateAccountGroup(testProjectName, testApplication1Name, testAccountGroup1Name);
		assertNotNull("AccountGroup " + testAccountGroup1Name + " is null",group);
		
		AM6Util.setMember(testAdminContext, Boolean.class, "GROUP", group.getObjectId(), "ACCOUNT", account.getObjectId(), false);
		boolean addMember = AM6Util.setMember(testAdminContext, Boolean.class, "GROUP", group.getObjectId(), "ACCOUNT", account.getObjectId(), true);
		assertTrue("Expected to add member", addMember);
		
		List<EntitlementType> ents2 = AM6Util.getEntitlements(testUserContext, new ArrayList<EntitlementType>().getClass(), "ACCOUNT", account.getObjectId());
		
		logger.info("Account Entitlements: " + ents2.size());
		assertTrue("Expected account to have only one entitlement", ents2.size() == 1);
		
		PersonType per = cc.getCreatePerson(testProjectName, testPerson1Name);
		assertNotNull("Person is null", per);

		assertTrue("Expected to adopt", cc.adopt(per, account));
		
		BaseRoleType testRole = cc.getCreateProjectRole(testProjectName, RoleEnumType.PERSON, "Test Role 1");
		assertNotNull("Role is null", testRole);
		
		BasePermissionType testPer = cc.getCreateApplicationPermission(testProjectName, testApplication1Name, "Test Per 2");
		assertNotNull("Permission is null", testPer);
		
		return cc;
		
	}
	
	protected boolean addCredential(ClientContext context, NameIdType object, String passwordCredential) {
		boolean added = false;
		AuthenticationRequestType art = new AuthenticationRequestType();
		art.setCredential(passwordCredential.getBytes());
		art.setCredentialType(CredentialEnumType.HASHED_PASSWORD);
		art.setSubject((object.getNameType().equals(NameEnumType.USER) ? object.getName() : object.getUrn()));
		added = AM6Util.addCredential(context, Boolean.class, object.getNameType(), object.getObjectId(), art);
		logger.info("Adding credential for object " + object.getUrn() + " : " + added);
		return added;
	}
	protected UserType getCreateUser(ClientContext adminContext, String name) {
		UserType checkUser = AM6Util.getObjectByName(adminContext, UserType.class,NameEnumType.USER, null, name, false);
		if(checkUser == null) {
			logger.info("User " + name + " is null.  Attempting to create");
			checkUser = new UserType();
			checkUser.setOrganizationPath(adminContext.getOrganizationPath());
			checkUser.setNameType(NameEnumType.USER);
			checkUser.setName(name);
			checkUser.setUserStatus(UserStatusEnumType.NORMAL);
			checkUser.setUserType(UserEnumType.DEVELOPMENT);
			checkUser.setAccountId(0L);
			//logger.info(JSONUtil.exportObject(checkUser));
			if(AM6Util.updateObject(adminContext, Boolean.class, checkUser)) {
				logger.info("Created user object");
				checkUser = AM6Util.getObjectByName(adminContext, UserType.class,NameEnumType.USER, null, name, false);
			}
			else {
				logger.error("Failed to create user object");
			}
		}
		return checkUser;
	}
	
	protected DirectoryGroupType getCreateDirectory(ClientContext context, DirectoryGroupType parent, String name) {

		if(parent == null) {
			parent = AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", "~");
		}
		assertNotNull("Parent directory is null",parent);
		//String testPath = AM6Util.getEncodedPath("~/TestData");

		logger.info("Attempt to find dir");
		DirectoryGroupType subDirectory = AM6Util.getObjectByName(context, DirectoryGroupType.class, NameEnumType.GROUP, parent.getObjectId(), name, false);
				//AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);
		
		if(subDirectory == null) {
			subDirectory = new DirectoryGroupType();
			subDirectory.setNameType(NameEnumType.GROUP);
			subDirectory.setGroupType(GroupEnumType.DATA);
			subDirectory.setName(name);
			subDirectory.setParentId(parent.getId());
			logger.info("Attempt to add dir");
			assertTrue("Failed to add directory",AM6Util.updateObject(context, Boolean.class, subDirectory));
			
			subDirectory = AM6Util.getObjectByName(context, DirectoryGroupType.class, NameEnumType.GROUP, parent.getObjectId(), name, false);
			//subDirectory = AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);
		}
		return subDirectory;
	}
	protected boolean configureCommunity(ClientContext context) {
		boolean configured = AM6Util.isCommunityCofigured(context, Boolean.class);
		if(configured == false) configured = AM6Util.configureCommunity(context, Boolean.class);
		assertTrue("Community is configured",configured);
		return configured;
	}
	protected LifecycleType getCreateCommunity(ClientContext context, String name, boolean reset) {

		LifecycleType lt = AM6Util.findCommunity(context, LifecycleType.class, name);
		if(lt != null && reset) {
			AM6Util.deleteCommunity(context, Boolean.class, lt.getObjectId());
			AM6Util.cleanupOrphans(context);
			AM6Util.clearCache(context, NameEnumType.UNKNOWN);
			lt = null;
			
		}
		if(lt == null && AM6Util.addCommunity(testAdminContext, Boolean.class, name)){
			lt = AM6Util.findCommunity(testAdminContext, LifecycleType.class, name);
			
		}
		assertNotNull("Community is null", lt);
		return lt;
	}
	
	protected ProjectType getCreateCommunityProject(ClientContext context, String communityId, String communityName, String name) {

		ProjectType lt = AM6Util.findCommunityProject(context, ProjectType.class, communityName, name);
		if(lt == null && AM6Util.addCommunityProject(testAdminContext, Boolean.class, communityId, name)){
			lt = AM6Util.findCommunityProject(testAdminContext, ProjectType.class, communityName, name);
			
		}
		assertNotNull("Community project is null", lt);
		return lt;
	}

	public String getTestScript(String fileName) {
		String data = null;
		try {
			BufferedInputStream fis = new BufferedInputStream(ClassLoader.getSystemResourceAsStream("./" + fileName)); 
			data = StreamUtil.streamToString(fis);
		} catch (IOException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return data;
	}
	
	public DataType getCreateData(ClientContext context, String subGroupName, String dataName, String dataContents) {
		DirectoryGroupType homeDirectory = AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", "~");
		assertNotNull("Couldn't Find Home Directory",homeDirectory);
		String testPath = AM6Util.getEncodedPath("~/" + subGroupName);

		DirectoryGroupType subDirectory = AM6Util.findObject(testUserContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);

		if(subDirectory == null) {
			subDirectory = new DirectoryGroupType();
			subDirectory.setNameType(NameEnumType.GROUP);
			subDirectory.setGroupType(GroupEnumType.DATA);
			subDirectory.setName(subGroupName);
			subDirectory.setParentId(homeDirectory.getId());
			assertTrue("Failed to add directory",AM6Util.updateObject(context, Boolean.class, subDirectory));
			subDirectory = AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);
		}
		assertNotNull("Couldn't find sub directory",subDirectory);
		// logger.info(JSONUtil.exportObject(subDirectory));
		DataType testData = AM6Util.getObjectByName(context, DataType.class, NameEnumType.DATA, subDirectory.getObjectId(), dataName, false);
		if(testData != null) {
			assertTrue("Failed to delete data", AM6Util.deleteObject(context, Boolean.class, NameEnumType.DATA, testData.getObjectId()));
			testData = null;
		}
		if(testData == null) {
			DataType data = new DataType();
			data.setNameType(NameEnumType.DATA);
			data.setMimeType("text/plain");
			try {
				DataUtil.setValue(data, dataContents.getBytes());
			} catch (DataException e) {
				logger.error(e);
			}
			assertTrue("Data contains no value", data.getDataBytesStore().length > 0);
			data.setName(dataName);
			data.setGroupPath(subDirectory.getPath());
			assertTrue("Failed to add data",AM6Util.updateObject(testUserContext, Boolean.class, data));
			testData = AM6Util.getObjectByName(context, DataType.class, NameEnumType.DATA, subDirectory.getObjectId(), dataName, false);

		}
		assertNotNull("Couldn't find test data",testData);

		return testData;
	}
	
}