package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.client.util.AM6Util;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.junit.Test;

public class TestGameSetup extends BaseClientTest {
	
	@Test
	public void TestGameProject() {
		assertNotNull("User context is null", testUserContext);
		assertNotNull("User context is null", testAdminContext);
		LifecycleType community = getCreateCommunity(testAdminContext, testCommunityName);
		assertNotNull("Community is null", community);
		
		AM6Util.enrollCommunityAdmin(testAdminContext, Boolean.class, community.getObjectId(), testUser.getObjectId());
		
		ProjectType project = getCreateCommunityProject(testAdminContext, community.getObjectId(), testCommunityName, testProjectName);
		assertNotNull("Project is null", project);
		
		boolean traits = AM6Util.configureCommunityTraits(testAdminContext, Boolean.class, community.getObjectId());
		assertTrue("Failed to configure traits", traits);

		boolean cinfo = AM6Util.configureCommunityCountryInfo(testAdminContext, Boolean.class, community.getObjectId());
		assertTrue("Failed to configure country information", cinfo);
		
		boolean admin1Codes = AM6Util.configureAdmin1Codes(testAdminContext, Boolean.class, community.getObjectId());
		assertTrue("Failed to configure admin1codes", admin1Codes);		
		/*

		
		boolean admin2Codes = AM6Util.configureAdmin2Codes(testAdminContext, Boolean.class, community.getObjectId());
		assertTrue("Failed to configure admin2codes", admin2Codes);
		*/
	}
}
