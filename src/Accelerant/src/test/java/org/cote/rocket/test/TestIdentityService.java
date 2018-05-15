/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.IdentityServiceFactory;
import org.cote.rocket.services.IdentityService;
import org.cote.rocket.util.DataGeneratorData;
import org.cote.rocket.util.DataGeneratorUtil;
import org.junit.Test;

/*
 * 2017/10/16 - AUTHORIZATION NOTE
 * TODO: Community authorization for lifecycles will get bridged to direct authorization checks due to the Policy Extension being applied on initial setup
 * This means that authorization checks targeting the lifecycle itself versus its containment group will fail for anyone other than the owner because the authorization was not set
 */
public class TestIdentityService extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestIdentityService.class);
	private static boolean createNewOrganization = false;
	private static ICommunityProvider provider = null;
	
	private OrganizationType getOrganization(String newOrgName){
		OrganizationType newOrg = testOrganization;
		if(createNewOrganization == true){
			try {
				
				newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).addOrganization(newOrgName, OrganizationEnumType.DEVELOPMENT, testOrganization);
				FactoryDefaults.setupOrganization(newOrg, SecurityUtil.getSaltedDigest("password"));
			} catch (FactoryException | ArgumentException | DataAccessException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return newOrg;
	}
	
	private ICommunityProvider getProvider(){
		if(provider != null) return provider;
		String pcls =testProperties.getProperty("factories.community");
		try {
			logger.info("Initializing community provider " + pcls);
			Class cls = Class.forName(pcls);
			ICommunityProvider f = (ICommunityProvider)cls.newInstance();
			provider = f;
			provider.setRandomizeSeedPopulation(false);
			provider.setOrganizePersonManagement(true);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.error(FactoryException.TRACE_EXCEPTION, e);
		}
		
		return provider;
	}
	
	private ProjectType getProviderCommunityProject(UserType user, LifecycleType community, String projectName,boolean cleanup){
		ProjectType proj = null;
		UserType admin = getAdminUser(user.getOrganizationId());
		proj = provider.getCommunityProject(admin, community.getName(), projectName);
		//logger.info(JSONUtil.exportObject(user));
		if(proj != null && cleanup){
			provider.deleteCommunityProject(admin, proj.getObjectId());
			proj = null;
		}
		if(proj == null){
			assertTrue("Failed to create community",provider.createCommunityProject(admin, community.getObjectId(), projectName));
			proj = provider.getCommunityProject(admin, community.getName(), projectName);
			assertNotNull("Community project is null",proj);
		}
		return proj;
	}
	
	private LifecycleType getProviderCommunity(UserType user, String communityName, boolean cleanup){
		LifecycleType lf = null;
		UserType admin = getAdminUser(user.getOrganizationId());
		lf = provider.getCommunity(admin, communityName);
		//logger.info(JSONUtil.exportObject(user));
		if(lf != null && cleanup){
			provider.deleteCommunity(admin, lf.getObjectId());
			lf = null;
		}
		if(lf == null){
			assertTrue("Failed to create community",provider.createCommunity(admin, communityName));
			lf = provider.getCommunity(admin, communityName);
			assertNotNull("Community is null",lf);
			assertTrue("Failed to enroll admin",provider.enrollAdminInCommunity(admin,lf.getObjectId(), user.getObjectId()));
		}
		return lf;
	}
	
	private boolean reloadTraits(UserType user, LifecycleType lf){
		ICommunityProvider provider = getProvider();
		boolean outBool = false;
		UserType admin = getAdminUser(user.getOrganizationId());
		DirectoryGroupType tdir = BaseService.findGroup(admin, GroupEnumType.DATA, lf.getGroupPath() + "/Traits");
		assertNotNull("Directory is null", tdir);
		int ct = BaseService.countByGroup(AuditEnumType.TRAIT, tdir, user);
		if(ct == 0){
			outBool = provider.importLocationTraits(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"), "featureCodes_en.txt");
		}
		else{
			outBool = true;
		}
		return outBool;
	}
	private boolean loadProjectRegion(UserType user, LifecycleType lf, ProjectType proj, int locationCount, int populationSize){
		ICommunityProvider provider = getProvider();
		boolean outBool = false;
		UserType admin = getAdminUser(user.getOrganizationId());
		DirectoryGroupType tdir = BaseService.findGroup(admin, GroupEnumType.DATA, proj.getGroupPath() + "/Persons");
		assertNotNull("Directory is null", tdir);
		int ct = BaseService.countByGroup(AuditEnumType.PERSON, tdir, user);
		if(ct == 0){
			logger.info("Loading project region and person seed data.");
			outBool = (
					provider.generateCommunityProjectRegion(admin, lf.getObjectId(), proj.getObjectId(), locationCount, populationSize, testProperties.getProperty("data.generator.dictionary"),testProperties.getProperty("data.generator.names"))
				);
		}
		else{
			outBool = true;
		}
		return outBool;
	}
	private boolean reloadCountryInfo(UserType user, LifecycleType lf){
		ICommunityProvider provider = getProvider();
		boolean outBool = false;
		UserType admin = getAdminUser(user.getOrganizationId());
		DirectoryGroupType tdir = BaseService.findGroup(admin, GroupEnumType.DATA, lf.getGroupPath() + "/Locations");
		assertNotNull("Directory is null", tdir);
		int ct = BaseService.countByGroup(AuditEnumType.LOCATION, tdir, user);
		if(ct == 0){
			logger.info("Loading community location data.  Note: This may take a while.");
			outBool = (
				provider.importLocationCountryInfo(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"), "countryInfo.txt")
				&& provider.importLocationAdmin1Codes(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"), "admin1CodesASCII.txt")
				&& provider.importLocationAdmin2Codes(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"),  "admin2Codes.txt")
				&& provider.importLocationCountryData(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"), "US,CA,MX","alternateNames.txt")
			);
		}
		else{
			outBool = true;
		}
		return outBool;
	}
	
	@Test
	public void TestDataGenerator(){
		ICommunityProvider provider = getProvider();
 
		int locationSize = 3;
		int seedSize = 1000;
		String communityName =  "Data Generator Q1";
		String projectName = "Data Project Q1";
		LifecycleType lf = getProviderCommunity(testUser, communityName,false);
		
		assertNotNull("Community is null",lf);
		/*
		try {
			//EffectiveAuthorizationService.rebuildEntitlementsCache();
			assertTrue("Test User is not authorized to change object",AuthorizationService.canChange(testUser, lf));
		} catch (ArgumentException | FactoryException e) {
			logger.error("Failed authorization check");
		}
		*/
		assertTrue("Failed to reload traits",reloadTraits(testUser,lf));
		assertTrue("Failed to reload cinfo",reloadCountryInfo(testUser,lf));
		//provider.generateCommunityProjectRegion(testUser, lf.getObjectId(), projectId, locationSize, seedSize, dictionaryPath, namesPath)
		
		ProjectType p1 = getProviderCommunityProject(testUser, lf, projectName,true);
		
		assertNotNull("Project is null",p1);
		
		assertTrue("Failed to load project region",loadProjectRegion(testUser, lf, p1, locationSize, seedSize));
		
		assertTrue("Failed to generate project application",provider.generateCommunityProjectApplication(testUser, lf.getObjectId(), p1.getObjectId(), "Application - " + UUID.randomUUID().toString(), true, true, 25, 25, 1.0, testProperties.getProperty("data.generator.dictionary"),testProperties.getProperty("data.generator.names")));
		
		BasePermissionType[] permTest = DataGeneratorData.randomApplicationPermissions(25, 25);
		assertTrue("Expected exact number of permissions, but received " + permTest.length,permTest.length == 25);
		
		/*
		AuditType audit = AuditService.beginAudit(ActionEnumType.EXECUTE, "Unit Test",AuditEnumType.USER, testUser.getUrn());

		
		
		DataGeneratorUtil dutil = getGenerator(audit, testUser, lf.getName(), p1.getName(), lf.getGroupPath() + "/Locations", lf.getGroupPath() + "/Traits", testProperties.getProperty("data.generator.dictionary"),testProperties.getProperty("data.generator.names"));
		boolean init = false;
		try {
			init = dutil.initialize();
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		assertTrue("Failed to init generator", init);
		
		DirectoryGroupType ldir = BaseService.findGroup(testUser, GroupEnumType.DATA, lf.getGroupPath() + "/Locations");
		assertNotNull("Locations path is null", ldir);
		List<LocationType> locations = dutil.getRandomType(testUser, FactoryEnumType.LOCATION, ldir, 2);
		assertTrue("Expected at least one location",locations.size() > 0);
		AddressType addr1 = dutil.randomAddress(locations.get(0));
		assertNotNull("Address is null",addr1);
		logger.info(JSONUtil.exportObject(addr1));
		*/
		//provider.generateCommunityProjectRegion(testUser, communityId, projectId, locationSize, seedSize, testProperties.getProperty("data.generator.dictionary"), testProperties.getProperty("data.generator.names"));
	}
	/*
	@Test
	public void TestSetup(){
		
		OrganizationType newOrg = getOrganization("Organization -" +  UUID.randomUUID().toString());

		assertNotNull("Org is null",newOrg);
		logger.info("Testing with organization " + newOrg.getName());
		
		IdentityServiceFactory.setAdminCredential("password");
		IdentityService is = IdentityServiceFactory.getIdentityService(newOrg);
		
		assertTrue("Service is not initialized",is.initialize());
		ProjectType proj = is.getProject("Test Project 1");
		
		if(proj != null){
			is.deleteProjectContents(proj, true, true);
			Factories.clearCaches();
			proj = null;
		}
		
		if(proj == null) proj = is.createProject("Test Project 1");
		assertNotNull("Project is null",proj);
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		List<DirectoryGroupType> apps = new ArrayList<DirectoryGroupType>();
		
		DirectoryGroupType appRoot = is.getApplicationsRoot(proj);
		
		try {
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(appRoot);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(appRoot);
			
			DirectoryGroupType pd = is.getPersonsRoot(proj);
			List<PersonType> persons = new ArrayList<PersonType>();
			for(int p = 0; p < 20; p++){
				PersonType p1 = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(is.getAdminUser(), pd.getId());
				p1.setName("Person " + (p+1));
				persons.add(p1);
			}
			
			is.importPersons(sessionId,proj,persons);
			BulkFactories.getBulkFactory().write(sessionId);

			sessionId = BulkFactories.getBulkFactory().newBulkSession();
			for(int i = 0; i < 20; i++){
				DirectoryGroupType d = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(is.getAdminUser(),"Application " + (i + 1), appRoot, newOrg.getId());
				d.setName("Application " + (i + 1));
				apps.add(d);
			}
			is.importApplications(sessionId, proj, apps);
			BulkFactories.getBulkFactory().write(sessionId);

			sessionId = BulkFactories.getBulkFactory().newBulkSession();
			for(int i = 0; i < 20; i++){
				DirectoryGroupType d = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Application " + (i + 1), appRoot, newOrg.getId());
				List<BasePermissionType> perms = new ArrayList<BasePermissionType>();
				List<AccountType> accounts = new ArrayList<AccountType>();
				for(int p = 0; p < 50; p++){
					ApplicationPermissionType ap = (ApplicationPermissionType)((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).newPermission(is.getAdminUser(),"Permission " + (p + 1), PermissionEnumType.APPLICATION, null,newOrg.getId());
					perms.add(ap);
					
				}
				for(int a = 0; a < 20; a++){
					AccountType a1 = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount(is.getAdminUser(), "Account " + (a + 1), AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, d.getId());
					a1.getAttributes().add(Factories.getAttributeFactory().newAttribute(a1, "owner", "Person " + (i+1)));
					accounts.add(a1);
				}
				is.importAccounts(sessionId, proj, d, accounts);
				is.importPermissions(sessionId, proj, d, perms);
				for(int v = 0; v < perms.size();v++){
					Set<String> am = new HashSet<String>(); 
					List<AccountType> aL = new ArrayList<AccountType>();
					int len = (new Random()).nextInt(accounts.size());
					for(int r = 0; r < len; r++){
						int ia = (new Random()).nextInt(accounts.size());
						if(am.contains(accounts.get(ia).getName())) continue;
						am.add(accounts.get(ia).getName());
						aL.add(accounts.get(ia));
					}
					is.importPermissionMembers(sessionId, proj, d, perms.get(v), aL, true);
				}
			}
			/// Note, for the initial import, the permissions should be in the same session,
			/// But for subsequent imports, it must be individual because there's a mix/match of existing and new accounts
			/// And the new accounts will have the bulk ids, allowing the updates to be bulk-added, while the existing accounts won't
			///
			
			BulkFactories.getBulkFactory().write(sessionId);
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			//EffectiveAuthorizationService.rebuildEntitlementsCache();
		} catch (ArgumentException | FactoryException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
	}
	*/
	
}