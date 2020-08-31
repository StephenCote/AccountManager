package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.client.Client;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.junit.Test;

public class TestGameSetup extends BaseClientTest {
	
	
	private String countryCodes = "GB,FR";
	private int epochEvolutions = 12;
	private int epochCount = 20;
	public int countPeople(ClientContext context, NameIdDirectoryGroupType parent)
	{
		return Client.countObjects(context, parent, NameEnumType.PERSON, "Persons");
	}
	public int countTraits(ClientContext context, NameIdDirectoryGroupType parent)
	{
		return Client.countObjects(context, parent, NameEnumType.TRAIT, "Traits");
	}
	public int countLocations(ClientContext context, NameIdDirectoryGroupType parent)
	{
		return Client.countObjects(context, parent, NameEnumType.LOCATION, "Locations");
	}
	public int countEvents(ClientContext context, NameIdDirectoryGroupType parent)
	{
		return Client.countObjects(context, parent, NameEnumType.EVENT, "Events", false);
	}
	public boolean evolveProjectRegions(ClientContext context, LifecycleType community, ProjectType project, int epochCount) {
		boolean outBool = false;
		int ecount = countEvents(context, project);
		logger.info("Epoch size: " + ecount);
		if(ecount == 0) {
			logger.error("Epoch count must be 1 (by loading project region)");
			return outBool;
		}
		outBool = AM6Util.evolveCommunityProjectRegion(context,  Boolean.class, community.getObjectId(), project.getObjectId(), epochCount, epochEvolutions);
		return outBool;
	}
	public boolean loadProjectRegions(ClientContext context, LifecycleType community, ProjectType project, int locationSize, int seedSize) {
		boolean outBool = false;
		int pcount = countPeople(context, project);
		logger.info("People count: " + pcount);
		if(pcount == 0) {
			boolean loadRegion = AM6Util.configureCommunityProjectRegion(context, Boolean.class, community.getObjectId(), project.getObjectId(), locationSize, seedSize);
			if(!loadRegion) {
				logger.error("Failed to configure community region");
				return false;
			}
		}
		outBool = true;
		return outBool;
	}
	public boolean loadCommunityCountryInformation(ClientContext context, LifecycleType community) {
		boolean outBool = false;
		
		int tcount = countTraits(context, community);
		logger.info("Trait count: " + tcount);
		if(tcount == 0) {
			boolean traits = AM6Util.configureCommunityTraits(context, Boolean.class, community.getObjectId());
			if(!traits) {
				logger.error("Failed to configure traits");
				return false;
			}
		}
		int lcount = countLocations(context, community);
		if(lcount == 0) {
			
			/// Note: These are all synchronous calls, so there will likely be connection timeouts for large volumes of data
			/// These need to be made async calls
			
			logger.info("Configuring country data in community " + community.getName());
			boolean cinfo = AM6Util.configureCommunityCountryInfo(context, Boolean.class, community.getObjectId());
			if(!cinfo) {
				logger.error("Failed to configure country information");
				return false;
			}
			logger.info("Configuring administrative codes in community " + community.getName());
			boolean admin1Codes = AM6Util.configureAdmin1Codes(context, Boolean.class, community.getObjectId());
			if(!admin1Codes) {
				logger.error("Failed to configure admin1codes");
				return false;
			}	
			logger.info("Configuring additional administrative codes in community " + community.getName());
			boolean admin2Codes = AM6Util.configureAdmin2Codes(context, Boolean.class, community.getObjectId());
			if(!admin2Codes) {
				logger.error("Failed to configure admin2codes");
				return false;
			}
			logger.info("Configuring country data " + countryCodes + " in community " + community.getName());
			boolean countryData = AM6Util.configureCommunityCountryData(testAdminContext, Boolean.class, community.getObjectId(), countryCodes);
			if(!countryData) {
				logger.error("Failed to load country data");
				return false;
			}
			outBool = true;
		}
		else {
			outBool = true;
		}
		return outBool;
	}
	
	@Test
	public void TestGameProject() {
		assertNotNull("User context is null", testUserContext);
		assertNotNull("User context is null", testAdminContext);
		LifecycleType community = getCreateCommunity(testAdminContext, testCommunityName, false);
		assertNotNull("Community is null", community);
		
		/// AM6Util.enrollCommunityAdmin(testAdminContext, Boolean.class, community.getObjectId(), testUser.getObjectId());
		
		ProjectType project = getCreateCommunityProject(testAdminContext, community.getObjectId(), testCommunityName, testProjectName);
		assertNotNull("Project is null", project);
		
		assertTrue("Failed to load country info", loadCommunityCountryInformation(testAdminContext, community));
		assertTrue("Failed to load project regions", loadProjectRegions(testAdminContext, community,project,3,250));
		assertTrue("Failed to evolve project regions", evolveProjectRegions(testAdminContext, community, project, epochCount));

	}
}
