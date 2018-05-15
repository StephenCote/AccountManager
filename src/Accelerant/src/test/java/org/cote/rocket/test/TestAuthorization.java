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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Factories;
import org.cote.rocket.RocketSecurity;
import org.junit.Test;
public class TestAuthorization extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestAuthorization.class);

	
	@Test
	public void TestGroupRoleAuth(){
		LifecycleType lc = getTestLifecycle(testUser,"QA Lifecycle");
		assertNotNull("Lifecycle is null",lc);
		ProjectType proj = getTestProject(testUser, lc, "QA Project");
		UserGroupType group = null;
		UserGroupType devGroup = null;
		boolean succeeded = false;

		try {
			group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserGroup(testUser, "UserGroup", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId()), proj.getOrganizationId());
			devGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserGroup(testUser, "DevGroup", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId()), proj.getOrganizationId());
			assertNotNull("Group is null", group);
			GroupService.addUserToGroup(testUser, group);
			/*
			String guid=UUID.randomUUID().toString();
			String bulkSessionId = BulkFactories.getBulkFactory().newBulkSession();
			for(int i = 0; i < 100;i++){
				UserType tuser = addBulkUser(bulkSessionId,"BulkUser-" + guid + "-" + (i + 1),"password1");//getUser("BulkUser-" + guid + "-" + (i+1),"password1");
				GroupService.addUserToGroup(tuser, group);
				GroupService.addUserToGroup(tuser, devGroup);
				RoleService.addUserToRole(tuser, RocketSecurity.getProjectUserRole(proj));
			}
			BulkFactories.getBulkFactory().write(bulkSessionId);
			*/
			RoleService.addGroupToRole(devGroup, RocketSecurity.getProjectDeveloperRole(proj));
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			assertTrue("Test user is not in group",GroupService.getIsUserInGroup(group, testUser));
			UserRoleType readerRole = RocketSecurity.getProjectUserRole(proj);
			assertTrue("Role can't read project directory",AuthorizationService.canView(readerRole, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId())));
			
			GroupService.removeUserFromGroup(group,testUser2);
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			logger.info("*** MARK ROLE RIGHT CHECK");
			assertTrue("Test User 1 (Reader Role) can't read project group via role",AuthorizationService.canView(readerRole,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId())));
			assertFalse("Test User 2 can read project group",AuthorizationService.canView(testUser2, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId())));
			
			RoleService.addGroupToRole(group, readerRole);
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			//assertTrue("Failed to add group of users to role",add);
			assertTrue("Group is not a member of the role",RoleService.getIsGroupInRole(readerRole, group));
			
			// Add Test User 3 directly as a reader of the project group
			AuthorizationService.authorize(testUser, testUser3, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId()), AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP,testUser3.getOrganizationId()), true);
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			assertTrue("Test User 3 can't read project group",AuthorizationService.canView(testUser3, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId())));
			
			// Add Test User 2 to the group that is attached to the role
			GroupService.addUserToGroup(testUser2, group);
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			assertTrue("Test User 2 isn't in the group",GroupService.getIsUserInGroup(group, testUser2));
			logger.info("Check if Test User #2 (#" + testUser2.getId() + ") can read the group (#" + proj.getGroupId() + ") by being in a user group (#" + group.getId() + ")  attached to the role (#" + readerRole.getId() + ") with access rights");
			
			//assertTrue("Test User 2 can't read project group",AuthorizationService.canView(testUser2, proj.getGroup()));
			
			
			//RoleService.addAccountToRole(role_admin, account, role)
			
			succeeded = true;
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Success bit is false",succeeded);
		
		
	}
	
	/*
	@Test
	public void TestRocketLifecycleAuthorization(){
		LifecycleType lc = getTestLifecycle(testUser,"QA Lifecycle");
		assertNotNull("Lifecycle is null");
		try {
			assertTrue("User 1 is not the lifecycle admin",RoleService.getIsUserInRole(RocketSecurity.getLifecycleAdminRole(lc),testUser));
			assertTrue("User 1 cannot read lifecycle",RocketSecurity.canReadLifecycle(testUser, lc));
			assertFalse("User 2 shouldn't be able to read lifecycle",RocketSecurity.canReadLifecycle(testUser2, lc));
			assertFalse("User 3 shouldn't be able to read lifecycle",RocketSecurity.canReadLifecycle(testUser3, lc));
			assertTrue("User 1 cannot change lifecycle",RocketSecurity.canChangeLifecycle(testUser, lc));
			assertFalse("User 2 shouldn't be able to change lifecycle",RocketSecurity.canReadLifecycle(testUser2, lc));
			assertFalse("User 3 shouldn't be able to change lifecycle",RocketSecurity.canChangeLifecycle(testUser3, lc));

		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	
	
	@Test
	public void TestGetDefaultValues(){
		boolean check = false;
		try{

			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			UserType admin = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", org);
			BaseRoleType rocketRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName("RocketRoles", org);
			assertNotNull("Role is null",rocketRole);
			BaseRoleType adminRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName("AdminRole", rocketRole, org);
			assertNotNull("Role is null",adminRole);
			UserType user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("steve", org);
			DirectoryGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user,"/Rocket", user.getOrganizationId());
			assertNotNull("Group is null",group);
			
			boolean switched = AuthorizationService.switchGroup(admin, adminRole, group, AuthorizationService.getViewGroupPermission(org), true);
			logger.info("Switched permission: " + switched);
			check = AuthorizationService.canView(user, group);
			logger.info("User is authorized = " + check);
			check = AuthorizationService.canView(adminRole, group);
			logger.info("Role is authorized = " + check);
			logger.info("Check role group permission = " + (AuthorizationService.checkGroupPermissions(adminRole, group, new BasePermissionType[] { AuthorizationService.getViewGroupPermission(group.getOrganizationId()) })));			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("User can't view group",check);
	}
	
	*/
	
	
	/*
	@Test
	public void TestRoles(){

		OrganizationType org=null;
		UserType testUser = null;
		String sessionId = UUID.randomUUID().toString();
		String testUserName = "QA User 1";
		try {
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			//Rocket.configureApplicationEnvironment(org, SecurityUtil.getSaltedDigest("password1"));
			try{
				testUser = SessionSecurity.login(sessionId, testUserName, SecurityUtil.getSaltedDigest("password1"), org);
			}
			catch(FactoryException fe2){
				///
			}
			if(testUser == null){
				UserType new_user = Factories.getNameIdFactory(FactoryEnumType.USER).newUser(testUserName, SecurityUtil.getSaltedDigest("password1"), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, org);
				if(Factories.getNameIdFactory(FactoryEnumType.USER).addUser(new_user,  true)){
					testUser = SessionSecurity.login(sessionId, testUserName, SecurityUtil.getSaltedDigest("password1"), org);
				}
			}
			UserRoleType rocketUserRole = RocketSecurity.getUserRole(org);
			UserRoleType userReaderRole = RoleService.getAccountUsersReaderUserRole(org);
			UserRoleType roleReaderRole = RoleService.getRoleReaderUserRole(org);
			assertNotNull("Rocket role is null", rocketUserRole);
			assertNotNull("User reader role is null", userReaderRole);
			assertNotNull("Role reader role is null", roleReaderRole);
			
			/// Cleanup any leftovers
			///
			RoleService.removeUserFromRole(rocketUserRole,testUser);
			RoleService.removeUserFromRole(userReaderRole,testUser);
			RoleService.removeUserFromRole(roleReaderRole,testUser);
			
			
			assertTrue("Failed to add user to role",RoleService.addUserToRole(testUser, rocketUserRole));
			assertTrue("Failed to add user to role",RoleService.addUserToRole(testUser, userReaderRole));
			assertTrue("Failed to add user to role",RoleService.addUserToRole(testUser, roleReaderRole));
			boolean isAuth = AuthorizationService.isAccountReaderInOrganization(testUser, org);
			
			List<UserRoleType> roles = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getUserRoles(testUser);
			assertTrue("Roles list is empty",roles.size() > 0);
			
			RoleService.removeUserFromRole(rocketUserRole,testUser);
			RoleService.removeUserFromRole(userReaderRole,testUser);
			RoleService.removeUserFromRole(roleReaderRole,testUser);
			

		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

	}
	*/
	
}
