package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Rocket;
import org.cote.rocket.util.CommunityProjectUtil;
import org.junit.Test;

public class TestCommunities extends BaseAccelerantTest {
	
	/*
	 /// NOTE: This test will take a little bit to run as it creates a new organization and then configures it for a community
	 /// A lot of the time lost is in the serial organization configuration process which includes cache resets and authZ rebuilds
	 ///
	  
	@Test
	public void TestNewCommunity(){
		String newOrgName = "QA Org " + UUID.randomUUID().toString();
		logger.info("Creating new test organization: " + newOrgName);
		OrganizationType org = getCreateOrganization(newOrgName,"password");
		assertNotNull("New organization is null", org);
		ICommunityProvider provider = getProvider();
		UserType adminUser = null;
		try {
			adminUser = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName("Admin", org.getId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		assertNotNull("Admin user is null");
		boolean configured = provider.configureCommunity(adminUser);
		assertTrue("Failed to configure community", configured);
	}
	*/
	
	/*
	
	@Test
	public void TestNewCommunityProject(){
		String newProjectName = "Project-" + UUID.randomUUID().toString();
		DirectoryGroupType pDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Projects", testUser);
		assertNotNull("Directory is null", pDir);
		ProjectType newProject = new ProjectType();
		newProject.setName(newProjectName);
		newProject.setGroupPath(pDir.getPath());
		boolean saved = CommunityProjectUtil.saveCommunityProject(newProject, testUser);
		assertTrue("Project was not saved", saved);
	}
	*/
	
	private UserType getCommunityUser(OrganizationType org, String name) {
		UserType user = null;

		try {
			user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(name, org.getId());
			if(user == null) {
				user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(name, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, org.getId());
				if(Factories.getNameIdFactory(FactoryEnumType.USER).add(user) == false){
					logger.error("Failed to create user");
					return null;
				}
				user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(name, org.getId());

			}
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return user;
	}
	private UserType getCommunityAdminUser(OrganizationType org) {
		UserType adminUser = null;
		try {
			adminUser = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName("Admin", org.getId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return adminUser;
	}
	private OrganizationType getCommunityTestOrganization(String name){
		String newOrgName = "QA Org " + name;
		logger.info("Creating new test organization: " + newOrgName);
		OrganizationType org = getCreateOrganization(newOrgName,"password");
		assertNotNull("New organization is null", org);
		ICommunityProvider provider = getProvider();
		UserType adminUser = getCommunityAdminUser(org);

		assertNotNull("Admin user is null",adminUser);
		
		boolean configured = provider.isCommunityConfigured(org.getId());
		if(!configured) {
			logger.info("Community is not configured.  Attempting to configure");
			configured = provider.configureCommunity(adminUser);
		}
		else {
			logger.info("Organization is already configured for community use");
		}
		assertTrue("Failed to configure community", configured);
		return org;
	}
	
	@Test
	public void TestNewCommunityLifecycleProject(){
		OrganizationType testCommunityOrg = getCommunityTestOrganization("TestCommunities");
		
		String testId = UUID.randomUUID().toString();
		String newProjectName = "Project-" + testId;
		String newProjectName2 = "Project 2-" + testId;
		String newLifecycleName = "Lifecycle-Q1";// + UUID.randomUUID().toString();
		
		ICommunityProvider provider = getProvider();
		UserType adminUser = getCommunityAdminUser(testCommunityOrg);
		UserType user = getCommunityUser(testCommunityOrg, "Test Project User 1");
		
		
		boolean createCommunity = false;
		boolean createProject = false;
		boolean createProject2 = false;
		try {
				
				LifecycleType lt = provider.getCommunity(adminUser, newLifecycleName);
				if(lt == null) {
					createCommunity = provider.createCommunity(adminUser, newLifecycleName);
					assertTrue("Failed to create community", createCommunity);
					lt = provider.getCommunity(adminUser, newLifecycleName);
				}
				assertNotNull("Failed to find community", lt);
				
				ProjectType pj = provider.getCommunityProject(adminUser, newLifecycleName, newProjectName);
				if(pj == null) {
					createProject = provider.createCommunityProject(adminUser, lt.getObjectId(), newProjectName);
					assertTrue("Failed to create community project", createProject);
					pj = provider.getCommunityProject(adminUser, newLifecycleName, newProjectName);
				}
				assertNotNull("Failed to find community project", lt);
				
				provider.enrollAdminInCommunity(adminUser, lt.getObjectId(), user.getObjectId());
				
				ProjectType pj2 = provider.getCommunityProject(adminUser, newLifecycleName, newProjectName2);
				if(pj2 == null) {
					createProject2 = provider.createCommunityProject(user, lt.getObjectId(), newProjectName2);
					assertTrue("Failed to create community project", createProject2);
					pj2 = provider.getCommunityProject(user, newLifecycleName, newProjectName2);
				}
				assertNotNull("Failed to find community project", lt);
				
		}
		catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		/*
		DirectoryGroupType pDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Projects", testUser);
		assertNotNull("Directory is null", pDir);
		ProjectType newProject = new ProjectType();
		newProject.setName(newProjectName);
		newProject.setGroupPath(pDir.getPath());
		boolean saved = CommunityProjectUtil.saveCommunityProject(newProject, testUser);
		assertTrue("Project was not saved", saved);
		*/
	}
}
