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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.junit.Test;

public class TestGroupAuthorization extends BaseDataAccessTest {
	
	
	private static String testAuthUser1 = "Auth User 1"; 
	private static String testAuthUser2 = "Auth User 2"; 
	private static String testAuthUser3 = "Auth User 3"; 
	// private static String testAuthUser4 = "Auth User 4"; 
	// private static String testAuthRole1 = "Auth Role 1";
	// private static String testAuthRole2 = "Auth Role 2";
	private static String testAuthGroup1 = "Auth Group 1";
	private static String testAuthGroup2 = "Auth Group 2";
	
	
	@Test
	public void testUserReadGroupEntitlementAccessLongForm(){
		UserType user1 = this.getUser(testAuthUser1, "password1");
		UserType user2 = this.getUser(testAuthUser2, "password1");
		UserType user3 = this.getUser(testAuthUser3, "password1");
		assertNotNull("User 1 is null", user1);
		assertNotNull("User 2 is null", user2);
		DirectoryGroupType group1 = this.getGroup(user1, testAuthGroup1, GroupEnumType.DATA, user1.getHomeDirectory());
		DirectoryGroupType group2 = this.getGroup(user2, testAuthGroup2, GroupEnumType.DATA, user2.getHomeDirectory());
		//DirectoryGroupType group3 = this.getGroup(user3, testAuthGroup3, GroupEnumType.DATA, user3.getHomeDirectory());
		assertNotNull("Group 1 is null", group1);
		assertNotNull("Group 2 is null", group2);
		
		//AuthorizationService.authorize(admin, actor, object, permission, enable)
		boolean setAuthZ = false;
		boolean deAuthZ = false;
		boolean notAuthZ = false;
		boolean isAuthZ = false;
		try {
			setAuthZ = AuthorizationService.authorize(user1, user2, group1, AuthorizationService.getViewPermissionForMapType(group1.getNameType(), group1.getOrganizationId()), true);
			isAuthZ = AuthorizationService.isAuthorized(user2, group1, AuthorizationService.PERMISSION_VIEW, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(group1.getNameType(), group1.getOrganizationId())});
			deAuthZ = AuthorizationService.authorize(user1, user2, group1, AuthorizationService.getViewPermissionForMapType(group1.getNameType(), group1.getOrganizationId()), false);
			notAuthZ = AuthorizationService.isAuthorized(user2,group1, AuthorizationService.PERMISSION_VIEW, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(group1.getNameType(), group1.getOrganizationId())});

		} catch (FactoryException | DataAccessException | ArgumentException e) {
			logger.error(e);
		}
		assertTrue("Failed to set authorization bit", setAuthZ);
		assertTrue("User2 unable to view group1",isAuthZ);
		assertTrue("Failed to unset authorization bit", deAuthZ);
		assertFalse("User2 still able to view group1",notAuthZ);
		
	}
	
	
	@Test
	public void testUserReadGroupEntitlementAccessShortForm(){
		UserType user1 = this.getUser(testAuthUser1, "password1");
		UserType user2 = this.getUser(testAuthUser2, "password1");
		/// UserType user3 = this.getUser(testAuthUser3, "password1");
		assertNotNull("User 1 is null", user1);
		assertNotNull("User 2 is null", user2);
		DirectoryGroupType group1 = this.getGroup(user1, testAuthGroup1, GroupEnumType.DATA, user1.getHomeDirectory());
		DirectoryGroupType group2 = this.getGroup(user2, testAuthGroup2, GroupEnumType.DATA, user2.getHomeDirectory());
		assertNotNull("Group 1 is null", group1);
		assertNotNull("Group 2 is null", group2);
		
		boolean setAuthZ = false;
		boolean deAuthZ = false;
		boolean notAuthZ = false;
		boolean isAuthZ = false;
		try {
			setAuthZ = AuthorizationService.authorizeType(user1, user2, group1, true, false, false, false);
			isAuthZ = AuthorizationService.canView(user2, group1);
			deAuthZ = AuthorizationService.authorizeType(user1, user2, group1, false, false, false, false);
			notAuthZ = AuthorizationService.canView(user2, group1);

		} catch (FactoryException | DataAccessException | ArgumentException e) {
			logger.error(e);
		}
		assertTrue("Failed to set authorization bit", setAuthZ);
		assertTrue("User2 unable to view group1",isAuthZ);
		assertTrue("Failed to unset authorization bit", deAuthZ);
		assertFalse("User2 still able to view group1",notAuthZ);
		
	}
	
}
