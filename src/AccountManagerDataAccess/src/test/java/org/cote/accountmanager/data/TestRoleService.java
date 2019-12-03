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
package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.junit.Test;

public class TestRoleService extends BaseDataAccessTest{
	
	private static String testAuthUser1 = "Auth User 1"; 
	private static String testAuthUser2 = "Auth User 2"; 
	private static String testAuthUser3 = "Auth User 3"; 
	private static String testAuthUser4 = "Auth User 4"; 

	@Test
	public void testSystemRoles(){
		boolean isDataAdmin = false;
		try {
			isDataAdmin =RoleService.isFactoryAdministrator(testUser, ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)));
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse("User should not be a data admin",isDataAdmin);
	}
	
	@Test
	public void testEffectiveRoles(){
		UserType user = getUser(testAuthUser1,"password1");
		UserType user2 = getUser(testAuthUser2,"password1");
		UserType user3 = getUser(testAuthUser3,"password1");
		UserType user4 = getUser(testAuthUser4,"password1");
		

		
		UserRoleType userHomeRole = null;
		UserRoleType roleRoot = null;
		UserRoleType branch1 = null;
		UserRoleType branch2 = null;
		UserRoleType leaf1 = null;
		UserRoleType leaf2 = null;
		UserRoleType leaf3 = null;
		UserRoleType leaf4 = null;
		
		DirectoryGroupType authGroup = this.getGroup(user4, "Auth Data", GroupEnumType.DATA, user.getHomeDirectory());
		DataType authData = this.getCreateTextData(user, "Auth Data 1", "Demo data", authGroup);
		try {
			EffectiveAuthorizationService.pendUpdate(Arrays.asList(user,user2,user3,user4));
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			userHomeRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(user, RoleEnumType.USER, user.getOrganizationId());
			roleRoot = getRole(user, "Root", RoleEnumType.USER, userHomeRole);
			branch1 = getRole(user, "Branch 1", RoleEnumType.USER, roleRoot);
			branch2 = getRole(user, "Branch 2", RoleEnumType.USER, roleRoot);
			leaf1 = getRole(user, "Leaf 1", RoleEnumType.USER, branch1);
			leaf2 = getRole(user, "Leaf 2", RoleEnumType.USER, branch1);
			leaf3 = getRole(user, "Leaf 3", RoleEnumType.USER, branch2);
			leaf4 = getRole(user, "Leaf 4", RoleEnumType.USER, branch2);
			
			AuthorizationService.authorizeType(user, leaf1, authGroup, true, false, false, false);
			AuthorizationService.authorizeType(user, leaf2, authGroup, true, true, false, false);
			
			AuthorizationService.authorizeType(user, leaf3, authData, true, false, false, false);
			AuthorizationService.authorizeType(user, leaf4, authData, true, true, false, false);
			
			resetRoleMembership(branch1,user2, true);
			resetRoleMembership(branch2,user3, true);
			resetRoleMembership(leaf4,user4, true);
			
			testEffectiveRoleMembership(branch1,user2, true);
			testEffectiveRoleMembership(leaf1,user2, true);
			testEffectiveRoleMembership(leaf2,user2, true);
			testEffectiveRoleMembership(branch2,user3, true);
			testEffectiveRoleMembership(leaf3,user3, true);
			testEffectiveRoleMembership(leaf4,user3, true);
			testEffectiveRoleMembership(leaf4,user4, true);
			
			/// user 2 can view and edit group and data
			/// user 3 can view and edit data but not view/edit group
			/// user 4 can edit data, but not view/edit group
			
			testAuthorization(user2, authGroup, true, true, false, false);
			testAuthorization(user2, authData, true, true, false, false);
			testAuthorization(user3, authData, true, true, false, false);
			testAuthorization(user3, authGroup, false, false, false, false);
			testAuthorization(user4, authData, true, true, false, false);
			testAuthorization(user4, authGroup, false, false, false, false);
			
			resetRoleMembership(branch1,user2, false);
			resetRoleMembership(branch2,user3, false);
			resetRoleMembership(leaf4,user4, false);
			
			testEffectiveRoleMembership(branch1,user2, false);
			testEffectiveRoleMembership(leaf1,user2, false);
			testEffectiveRoleMembership(leaf2,user2, false);
			testEffectiveRoleMembership(branch2,user3, false);
			testEffectiveRoleMembership(leaf3,user3, false);
			testEffectiveRoleMembership(leaf4,user3, false);
			testEffectiveRoleMembership(leaf4,user4, false);
			
		} catch (ArgumentException | FactoryException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		//assertFalse("User should not be a data admin",isDataAdmin);
	}
	
	private void testAuthorization(NameIdType actor, NameIdType object, boolean canView, boolean canEdit, boolean canDelete, boolean canCreate) {

		String errStr = "Failed to authorize " + actor.getUrn() + " (#" + actor.getId() + ") access to " + object.getUrn() + " (#" + object.getId() + ") to ";
		boolean error = false;
		try {
			/// EffectiveAuthorizationService.clearCache();
			/// logger.info(JSONUtil.exportObject(EffectiveAuthorizationService.getObjectMap()));
			/// logger.info("Cache Report: " + EffectiveAuthorizationService.reportCacheSize());
			if(canView) assertTrue(errStr + "view",AuthorizationService.canView(actor, object));
			if(canEdit) assertTrue(errStr + "view",AuthorizationService.canChange(actor, object));
			if(canDelete) assertTrue(errStr + "view",AuthorizationService.canDelete(actor, object));
			if(canCreate) assertTrue(errStr + "view",AuthorizationService.canCreate(actor, object));
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e);
			error = true;
		}
		assertFalse("A logical error occurred", error);
	}
	
	private boolean testEffectiveRoleMembership(UserRoleType role, UserType user, boolean active) {
		boolean inEffectiveRole = false;
		try {
			inEffectiveRole = RoleService.getIsUserInEffectiveRole(role, user);
		} catch (ArgumentException | FactoryException e) {
			logger.error(e);
		}
		assertTrue(user.getUrn() + " should " + (active ? "" : "not") + " be in " + role.getUrn(), (active == inEffectiveRole));
		return inEffectiveRole;
	}
	
	/// Test, Remove if needed, Add, and assert
	private boolean resetRoleMembership(UserRoleType role, UserType user, boolean add) {
		boolean inRole = false;
		try {
			inRole = RoleService.getIsUserInRole(role, user);
			/// logger.info("Check 1 " + user.getUrn() + " in " + role.getUrn() + " " + inRole);
			if(inRole && RoleService.removeUserFromRole(role, user)) {
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				inRole = RoleService.getIsUserInRole(role, user);
				/// logger.info("Check 2 " + user.getUrn() + " in " + role.getUrn() + " " + inRole);
			}
			else {
				logger.warn("Failed to remove " + user.getUrn() + " from " + role.getUrn());
			}
			/// logger.info("Check 3 " + user.getUrn() + " in " + role.getUrn() + " " + inRole);
			assertFalse("User " + user.getUrn() + " shouldn't be in role " + role.getUrn(), inRole);
			
			if(!add) return true;
			
			if(!inRole && RoleService.addUserToRole(user, role)) {
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				inRole = RoleService.getIsUserInRole(role, user);
			}
		}
		catch(FactoryException | ArgumentException | DataAccessException e) {
			logger.error(e);
		}
		assertTrue("Expected user " + user.getUrn() + " should be a member of role " + role.getUrn(), inRole);
		return inRole;
	}
		
}