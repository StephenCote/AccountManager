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
package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.junit.Test;

public class TestUserFactory extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestUserFactory.class);
	private static String testUserName1 = "Example-" + UUID.randomUUID().toString();
	private static String testUserName2 = "Example-" + UUID.randomUUID().toString();
	private static String testUserPassword = "password1";
	
	
	@Test
	public void testAddUser(){
		OrganizationFactory of = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
		UserType user1 = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).newUser(testUserName1, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
		UserType user2 = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).newUser(testUserName2, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
		boolean add = false;
		boolean error = false;
		logger.info(testUserPassword + ":" + testUserPassword.length());
		try{
			add = (
				((UserFactory)Factories.getFactory(FactoryEnumType.USER)).add(user1, true)
				&&
				((UserFactory)Factories.getFactory(FactoryEnumType.USER)).add(user2, true)
			);
		}
		catch(ArgumentException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
			error = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
			error = true;
		}
		assertFalse("Error occurred", error);
		assertTrue("Did not add user", add);
	}
	@Test
	public void testGetUser(){
		UserType user = null;

		boolean error = false;

		try{
			user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(testUserName1,Factories.getDevelopmentOrganization().getId());
			((UserFactory)Factories.getFactory(FactoryEnumType.USER)).populate(user);
			assertTrue(user.getPopulated());
		}

		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);

	}
	
	@Test
	public void testUserRoles(){
		UserType user1 = null;

		UserType admin = null;
		UserRoleType devRole = null;

		boolean error = false;

		try{
			admin = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName("Admin", Factories.getDevelopmentOrganization().getId());
			assertNotNull(admin);
			devRole = RoleService.getCreateUserRole(admin, "Dev Role", null);
			assertNotNull(devRole);
			user1 = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(testUserName1,Factories.getDevelopmentOrganization().getId());
			((UserFactory)Factories.getFactory(FactoryEnumType.USER)).populate(user1);
			assertTrue("Did not populate user", user1.getPopulated());
			/// moved addUserToRole outside of the FactoryService
			assertTrue("Did not add user to role", RoleService.addUserToRole(user1, devRole));
			assertTrue("Could not find user in role", RoleService.getIsUserInRole(devRole, user1));
			assertTrue("Could not remove user from role", RoleService.removeUserFromRole(devRole, user1));
			assertFalse("User still in role cache",RoleService.getIsUserInRole(devRole, user1));
		}

		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
			error = true;
		} catch (DataAccessException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
			error = true;
		} catch (ArgumentException e) {
			
			error = true;
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);

	}
	@Test
	public void testUserAuthorization(){
		UserType user1 = null;
		UserType user2 = null;
		UserType admin = null;
		UserRoleType devRole = null;

		boolean error = false;

		try{
			admin = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName("Admin", Factories.getDevelopmentOrganization().getId());
			assertNotNull(admin);
			AccountRoleType adminRole = RoleService.getDataAdministratorAccountRole(Factories.getDevelopmentOrganization().getId());
			assertNotNull("Admin role is null", adminRole);
			//assertTrue("Admin should be a data administrator in its own organization", AuthorizationService.isDataAdministratorInMapOrganization(admin, admin.getOrganization()));
			devRole = RoleService.getCreateUserRole(admin, "Dev Role", null);
			assertNotNull(devRole);
			user1 = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(testUserName1,Factories.getDevelopmentOrganization().getId());
			user2 = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(testUserName2,Factories.getDevelopmentOrganization().getId());
			((UserFactory)Factories.getFactory(FactoryEnumType.USER)).populate(user1);
			((UserFactory)Factories.getFactory(FactoryEnumType.USER)).populate(user2);
			assertTrue("Did not populate user", user1.getPopulated() && user2.getPopulated());
			assertTrue("User #1 should be able to change their group.", AuthorizationService.canChange(user1, user1.getHomeDirectory()));
			assertTrue("User #2 should be able to change their group.", AuthorizationService.canChange(user2, user2.getHomeDirectory()));
			assertFalse("User #1 should not be able to change User #2's group.", AuthorizationService.canChange(user1, user2.getHomeDirectory()));
			
			AuthorizationService.authorize(user2, user1, user2.getHomeDirectory(), AuthorizationService.getEditPermissionForMapType(NameEnumType.GROUP, user2.getOrganizationId()), true);
			assertTrue("User #1 should now be able to change User #2's group.", AuthorizationService.canChange(user1, user2.getHomeDirectory()));

			AuthorizationService.authorize(user2, user1, user2.getHomeDirectory(), AuthorizationService.getEditPermissionForMapType(NameEnumType.GROUP, user2.getOrganizationId()), false);
			assertFalse("User #1 should no longer be able to change User #2's group.", AuthorizationService.canChange(user1, user2.getHomeDirectory()));

			//assertTrue("Admin user should be able to change user #1 group.", AuthorizationService.canChange(admin, user1.getHomeDirectory()));
			
		}

		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
			error = true;
		} catch (DataAccessException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
			error = true;
		} catch (ArgumentException e) {
			
			error = true;
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);

	}

}