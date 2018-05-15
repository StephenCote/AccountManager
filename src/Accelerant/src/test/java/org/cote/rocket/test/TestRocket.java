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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.RocketModel;
import org.cote.rocket.RocketSecurity;
import org.junit.Test;
public class TestRocket extends BaseAccelerantTest {

	public static final Logger logger = LogManager.getLogger(TestRocket.class);
	private static String lifecycleName = null;
	private static int lifecycleId = 0;
	private static String testLifecycleName = "Demo Lifecycle 1";
	private static String testProjectName = "Demo Project 1";
	private static boolean resetTestProject = true;
	private void deleteTestLifecycle(){
		LifecycleType lc = getTestLifecycle();
		if(lc != null){
			logger.info("Cleaning up test lifecycle: id=" + lc.getId() + " / Name=" + lc.getName() + " / Group=" + lc.getGroupId() + " / Org=" + lc.getOrganizationId());
			Rocket.deleteLifecycle(lc);
		}
	}
	private void deleteTestProject(){
		

		LifecycleType lc = getTestLifecycle();
		ProjectType proj = getTestProject(lc);
		if(lc != null){
			if(proj != null){
				logger.info("Cleaning up test project: id=" + proj.getId() + " / Name=" + proj.getName() + " / Group=" + proj.getGroupId() + " / Org=" + proj.getOrganizationId());
				Rocket.deleteProject(proj);

			}
			logger.info("Cleaning up test lifecycle: id=" + lc.getId() + " / Name=" + lc.getName() + " / Group=" + lc.getGroupId() + " / Org=" + lc.getOrganizationId());
			Rocket.deleteLifecycle(lc);
		}
		
	}
	
	private LifecycleType getTestLifecycle(){
		LifecycleType lc = null;
		
		try {
			lc = Rocket.getLifecycle(testLifecycleName, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return lc;
	}
	private ProjectType getTestProject(LifecycleType lc){
		ProjectType proj = null;
		try {
			proj = Rocket.getProject(testProjectName, lc, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return proj;
	}
	/*
	@Test
	public void TestGetLifecycle(){
		LifecycleType lc = null;
		DirectoryGroupType dir = null;
		try {
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, "/Lifecycles/" + testLifecycleName, testUser.getOrganizationId());
			lc = Rocket.getLifecycle(testLifecycleName, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		logger.info("Group: " + (dir == null ? true : false));
		assertNotNull("Test lifecycle directory is null", dir);
		assertNotNull("Test lifecycle is null", lc);
	}
	*/
	@Test
	public void TestCreateRocketLifecycle(){
		LifecycleType lc = getTestLifecycle();

		if(lc != null){
			logger.info("Using existing test lifecycle: id=" + lc.getId() + " / Name=" + lc.getName() + " / Group=" + lc.getGroupId() + " / Org=" + lc.getOrganizationId());
			return;
			// deleteTestLifecycle();
			// lc = getTestLifecycle();
		}
		assertNull("Test lifecycle already exists", lc);
		logger.info("Creating new Lifecycle");
		try {
			lc = Rocket.createLifecycle(testUser, testLifecycleName);
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			String[] groupKeys = new String[0];
			try{
				groupKeys = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCacheKeys();
				for(int i = 0; i < groupKeys.length;i++) logger.info("ROCKET KEY " + groupKeys[i]);
				groupKeys = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCacheKeys();
				for(int i = 0; i < groupKeys.length;i++) logger.info("AM5 KEY " + groupKeys[i]);
			}
			catch(FactoryException f1){
				logger.error(f1);
			}
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("Test lifecycle is null", lc);
	}

	@Test
	public void TestCreateProject(){
		LifecycleType lc = getTestLifecycle();
		assertNotNull("Test lifecycle is null", lc);
		ProjectType proj = getTestProject(lc);
		if(proj != null){
			if(resetTestProject == false){
				logger.info("Using existing test project: id=" + proj.getId() + " / Name=" + proj.getName() + " / Group=" + proj.getGroupId() + " / Org=" + proj.getOrganizationId());
				return;
			}
			else{
				logger.info("Cleaning up test project: id=" + proj.getId() + " / Name=" + proj.getName() + " / Group=" + proj.getGroupId() + " / Org=" + proj.getOrganizationId());
				Rocket.deleteProject(proj);
				proj = null;
			}
		}
		try {
			proj = Rocket.createProject(testUser, lc, testProjectName);
			RocketModel.addDefaults(testUser, proj.getGroupId());
			RocketModel.addWaterfallArtifacts(testUser, proj);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("Test project is null", proj);
	}
	
	@Test
	public void TestLifecycleRights(){
		LifecycleType lc = getTestLifecycle();
		assertNotNull("Test lifecycle is null", lc);
		boolean in_role = false;
		boolean in_role2 = false;
		boolean in_role3 = false;
		try {
			in_role = RoleService.getIsUserInRole(RocketSecurity.getLifecycleAdminRole(lc),testUser);
			in_role2 = RoleService.getIsUserInRole(RocketSecurity.getLifecycleAdminRole(lc),testUser2);
			in_role3 = RoleService.getIsUserInRole(RocketSecurity.getLifecycleDeveloperRole(lc),testUser3);
			if(in_role3 == false){
				RoleService.addUserToRole(testUser3, RocketSecurity.getLifecycleDeveloperRole(lc));
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				in_role3 = RoleService.getIsUserInRole(RocketSecurity.getLifecycleDeveloperRole(lc),testUser3);
			}
			assertTrue("Test user is not in the lifecycle admin role", in_role);
			assertFalse("Test user #2 is in the lifecycle admin role", in_role2);
			assertTrue("Test user #3 is not in the lifecycle developers role",  in_role3);
			List<DirectoryGroupType> dirs = Rocket.getLifecycleGroups(lc);
			assertTrue("Lifecycle directories not found",dirs.size() > 0);
			UserType[] users = new UserType[]{testUser,testUser3};
			for(int u = 0; u < users.length;u++){
				for(int i = 0; i < dirs.size();i++){
					//logger.info("Checking " + users[u].getName() + "'s rights on " + dirs.get(i).getName());
					boolean readGroup = AuthorizationService.canView(users[u], dirs.get(i));
					boolean editGroup = AuthorizationService.canChange(users[u], dirs.get(i));
					boolean deleteGroup = AuthorizationService.canDelete(users[u],dirs.get(i));
					boolean createGroup = AuthorizationService.canCreate(users[u],dirs.get(i));
					assertTrue("User " + users[u].getName() + " in role can't read lifecycle directory group " + dirs.get(i).getName(),readGroup);
					assertTrue("User " + users[u].getName() + " in role can't edit lifecycle directory group " + dirs.get(i).getName(),editGroup);
					assertTrue("User " + users[u].getName() + " in role can't delete lifecycle directory group " + dirs.get(i).getName(),deleteGroup);
					assertTrue("User " + users[u].getName() + " in role can't create lifecycle directory group " + dirs.get(i).getName(),createGroup);
				}
			}
			
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

	}
	
	
	@Test
	public void TestProjectRights(){
		LifecycleType lc = getTestLifecycle();
		assertNotNull("Test lifecycle is null", lc);
		ProjectType proj = getTestProject(lc);
		assertNotNull("Test project is null", proj);
		boolean in_role = false;
		boolean in_role2 = false;
		boolean in_role3 = false;
		try {
			setupProjectUsers();
			UserRoleType adminRole = RocketSecurity.getProjectAdminRole(proj);
			assertNotNull("Admin Role Is Null", adminRole);
			
			//EffectiveAuthorizationService.rebuildUserRoleCache(proj.getOrganizationId());
			//EffectiveAuthorizationService.rebuildRoleRoleCache(proj.getOrganizationId());
			
			in_role = RoleService.getIsUserInRole(adminRole,testUser);
			in_role2 = RoleService.getIsUserInRole(adminRole,testUser2);
			in_role3 = RoleService.getIsUserInRole(RocketSecurity.getProjectDeveloperRole(proj),testUser3);
			assertTrue("Test user (#" + testUser.getId() + ") is not in the project admin role (#" + adminRole.getId() + ")", in_role);
			assertFalse("Test user #2 is in the project admin role", in_role2);
			assertFalse("Test user #3 is in the project dev role", in_role3);
			
			List<DirectoryGroupType> dirs = Rocket.getProjectGroups(proj);
			assertTrue("Project directories not found",dirs.size() > 0);
			UserType[] users = new UserType[]{testUser,testUser2,testUser3};
			
			//Factories.clearCaches();
			for(int u = 0; u < users.length;u++){
				for(int i = 0; i < dirs.size();i++){
					//logger.info("Checking " + users[u].getName() + "'s rights on " + dirs.get(i).getName());
					//EffectiveAuthorizationService.rebuildGroupRoleCache(dirs.get(i));
					boolean readGroup = AuthorizationService.canView(users[u], dirs.get(i));
					boolean editGroup = AuthorizationService.canChange(users[u], dirs.get(i));
					boolean deleteGroup = AuthorizationService.canDelete(users[u],dirs.get(i));
					boolean createGroup = AuthorizationService.canCreate(users[u],dirs.get(i));
					assertTrue("User " + users[u].getName() + " in role can't read project directory group " + dirs.get(i).getName(),readGroup);
					assertTrue("User " + users[u].getName() + " in role can't edit project directory group " + dirs.get(i).getName(),editGroup);
					assertTrue("User " + users[u].getName() + " in role can't delete project directory group " + dirs.get(i).getName(),deleteGroup);
					assertTrue("User " + users[u].getName() + " in role can't create project directory group " + dirs.get(i).getName(),createGroup);
				}
			}
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Test user (#" + testUser.getId() + ") is not in the project admin role", in_role);
		assertFalse("Test user #2 is in the project admin role", in_role2);
	}
	
	private void setupProjectUsers(){
		LifecycleType lc = getTestLifecycle();
		assertNotNull("Test lifecycle is null", lc);
		ProjectType proj = getTestProject(lc);
		assertNotNull("Test project is null", proj);

		boolean in_role = false;

		try {
			in_role = RoleService.getIsUserInRole(RocketSecurity.getProjectDeveloperRole(proj),testUser2);
			if(in_role == false){
				RoleService.addUserToRole(testUser2, RocketSecurity.getProjectDeveloperRole(proj));
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				in_role = RoleService.getIsUserInRole(RocketSecurity.getProjectDeveloperRole(proj),testUser2);
			}
			

		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Test user is not in the project admin role", in_role);
	}
}
