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
package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.util.DataGeneratorData;
import org.junit.Test;

/*
 * 2017/10/16 - AUTHORIZATION NOTE
 * TODO: Community authorization for lifecycles will get bridged to direct authorization checks due to the Policy Extension being applied on initial setup
 * This means that authorization checks targeting the lifecycle itself versus its containment group will fail for anyone other than the owner because the authorization was not set
 */
public class TestIdentityService extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestIdentityService.class);
	private static String communityName =  "Data Generator Q1";
	private static String projectName = "Data Project Q2";
	
	@Test
	public void TestDataGenerator(){
		ICommunityProvider provider = getProvider();
 
		int locationSize = 3;
		int seedSize = 1000;

		LifecycleType lf = getProviderCommunity(testUser, communityName,false);
		
		assertNotNull("Community is null",lf);
		assertTrue("Failed to reload traits",reloadTraits(testUser,lf));
		assertTrue("Failed to reload cinfo",reloadCountryInfo(testUser,lf));
		
		ProjectType p1 = getProviderCommunityProject(testUser, lf, projectName,true);
		
		assertNotNull("Project is null",p1);
		
		assertTrue("Failed to load project region",loadProjectRegion(testUser, lf, p1, locationSize, seedSize));

		assertTrue("Failed to load project region",evolveProjectRegion(testUser, lf, p1, 1, 12));
		
		assertTrue("Failed to generate project application",getProvider().generateCommunityProjectApplication(testUser, lf.getObjectId(), p1.getObjectId(), "Application - " + UUID.randomUUID().toString(), true, true, 25, 25, 1.0, testProperties.getProperty("data.generator.dictionary"),testProperties.getProperty("data.generator.names")));
		
		BasePermissionType[] permTest = DataGeneratorData.randomApplicationPermissions(25, 25);
		assertTrue("Expected exact number of permissions, but received " + permTest.length,permTest.length == 25);
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