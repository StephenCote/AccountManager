package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.propellant.objects.ProjectType;
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
}
