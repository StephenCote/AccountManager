package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.client.Client;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.junit.Test;

public class TestGameSetup extends BaseClientTest {
	
	
	private String countryCodes = "GB,FR";

	private int epochCount = 50;
	
	
	@Test
	public void TestGameProject() {
		assertNotNull("User context is null", testUserContext);
		assertNotNull("User context is null", testAdminContext);
		LifecycleType community = getCreateCommunity(testAdminContext, testCommunityName, false);
		assertNotNull("Community is null", community);
		
		/// AM6Util.enrollCommunityAdmin(testAdminContext, Boolean.class, community.getObjectId(), testUser.getObjectId());
		
		ProjectType project = getCreateCommunityProject(testAdminContext, community.getObjectId(), testCommunityName, testProjectName);
		assertNotNull("Project is null", project);
		
		assertTrue("Failed to load country info", Client.loadCommunityCountryInformation(testAdminContext, community, countryCodes));
		assertTrue("Failed to load project regions", Client.loadProjectRegions(testAdminContext, community,project,3,250));
		assertTrue("Failed to evolve project regions", Client.evolveProjectRegions(testAdminContext, community, project, epochCount));

	}
}
