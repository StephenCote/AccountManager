package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.client.Client;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.CommunityContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.junit.Test;

public class TestCommunityProjectAccess extends BaseClientTest {

	private String countryCodes = "CA,US,MX";
	private int epochCount = 50;
	private int populationSeedSize = 10000;
	private int locationSeedSize = 3;
	
	@Test
	public void TestCommunityDataExport() {
		
		AM6Util.clearCache(testAdminContext, NameEnumType.UNKNOWN);
		
		assertNotNull("User context is null", testUserContext);
		assertNotNull("User context is null", testAdminContext);
		LifecycleType community = getCreateCommunity(testAdminContext, testCommunityName, false);
		
		
		assertNotNull("Community is null", community);

		CommunityContext cc = new CommunityContext(testAdminContext, testCommunityName);
		assertTrue("Failed to initialize community context", cc.initialize());
		
		ProjectType project = cc.getCreateProject(testProjectName);
		assertNotNull("Project is null", project);
		
		int eventCount = Client.countEvents(testAdminContext, project);
		logger.info("Event count: " + eventCount);
		if(eventCount <= 0) {
			assertTrue("Failed to load country info", Client.loadCommunityCountryInformation(testAdminContext, community, countryCodes));
			assertTrue("Failed to load project regions", Client.loadProjectRegions(testAdminContext, community,project,locationSeedSize,populationSeedSize));
			assertTrue("Failed to evolve project regions", Client.evolveProjectRegions(testAdminContext, community, project, epochCount));
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
				
		/*
				AM6Util.getCommunityApplication(testUserContext, DirectoryGroupType.class, community.getObjectId(), project.getObjectId(), testApplication1Name);
		if(appDir == null) {
			boolean created = AM6Util.createCommunityApplication(testUserContext, Boolean.class, community.getObjectId(), project.getObjectId(), testApplication1Name);
			assertTrue("Failed to create application " + testApplication1Name,created);
			appDir = AM6Util.getCommunityApplication(testUserContext, DirectoryGroupType.class, community.getObjectId(), project.getObjectId(), testApplication1Name);
		}
		*/
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

		
		/*
		int perCount = Client.countPeople(testUserContext, project);
		logger.info("Person count: " + perCount);
		
		long startRecord = 0L;
		int recordCount = 100;
		
		int pages = 0;
		if(perCount > 0) pages = (int)Math.nextUp(perCount/recordCount);
		logger.info("Page count: " + pages);
		*/
		
	}
}
