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

import java.util.Arrays;
import java.util.UUID;

import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.ServiceUtil;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;
public class TestDataAuthorization extends BaseDataAccessTest {
	
	
	
	@Test
	public void testEncryptedData(){
		UserType user = getUser("testuser1","password");
		
		SecurityBean bean = KeyService.getPrimarySymmetricKey(Factories.getDevelopmentOrganization().getId());
		//logger.info("Security Bean: " + JSONUtil.exportObject((SecurityType)bean));
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(user, "CryptoData");
			DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(user, dir.getId());
			String d1name = UUID.randomUUID().toString();
			String d2name = UUID.randomUUID().toString();
			DataUtil.setPassword(data, "My special password");
			data.setEncipher(true);
			data.setMimeType("text/plain");
			
			DataUtil.setCipher(data,bean);
			
			DataUtil.setValue(data, "This is the example short text".getBytes());
			data.setName(d1name);
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data);
			
			DataType cdata = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(d1name, dir);
			assertNotNull("Data is null",cdata);
			DataUtil.setCipher(cdata,bean);
			DataUtil.setPassword(cdata, "My special password");
			logger.info("Data Value: " + (new String(DataUtil.getValue(cdata))));
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		} catch (DataException e) {
			
			logger.error("Error",e);
		}
	}
	/*
	@Test
	public void testSharedDataAuth(){
		UserType user2 = null;
		DirectoryGroupType dir = null;
		DataType data = null;
		boolean auth = false;
		try {
			user2 = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("TestUser2", Factories.getPublicOrganization());
			EffectiveAuthorizationService.rebuildGroupRoleCache(Factories.getPublicOrganization());
			assertNotNull("User is null",user2);
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, "/Home/TestUser1/GalleryHome/.thumbnail", Factories.getPublicOrganization());
			assertNotNull("Dir is null",dir);
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName("2355.jpg 128x128", dir);
			assertNotNull("Data is null",data);
			assertTrue("User not authorized to view data",AuthorizationService.canViewData(user2, data));
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	*/
	/*
	 * NOTE: This test was written for the reverse-RBAC method, which is not configured by default, so it will fail.
	@Test
	public void testDataRoles(){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		UserType user1 = getUser("testuser1","password1");
		UserType user2 = getUser("testuser2","password1");
		UserType user3 = getUser("testuser3","password1");
		UserType user4 = getUser("testuser4","password1");
		DataType data1 = getData(user1, "testdata1");
		DataType data2 = getData(user1, "testdata2");
		DataType data3 = getData(user2, "testdata3");
		boolean error = false;
		try {
			EffectiveAuthorizationService.rebuildUserRoleCache(Arrays.asList(new UserType[]{user1,user2,user3}), user1.getOrganization());
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
			error = true;
		}
		assertFalse("There was an error",error);
		
		logger.info("Strip off existing permissions");
		switchPermissions(user1,user1,data1,false);
		switchPermissions(user1,user1,data2,false);
		switchPermissions(user1,user1,data3,false);

		switchPermissions(user1,user2,data1,false);
		switchPermissions(user1,user2,data2,false);
		switchPermissions(user1,user2,data3,false);
		
		switchPermissions(user1,user3,data1,false);
		switchPermissions(user1,user3,data2,false);
		switchPermissions(user1,user3,data3,false);

		
		UserRoleType rootRole = getRole(user1,"Unit Role - Root",null);
		assertNotNull("Base role is null",rootRole);
		UserRoleType roleChildDel = getRole(user1,"Unit Child Role - Delete",rootRole);
		assertNotNull("Child delete role is null",roleChildDel);
		UserRoleType roleChildAdd = getRole(user1,"Unit Child Role - Add",roleChildDel);
		assertNotNull("Child add role is null",roleChildAdd);
		UserRoleType roleChildMod = getRole(user1, "Unit Child Role - Mod",roleChildAdd);
		assertNotNull("Child role is null",roleChildMod);
		UserRoleType roleChildView = getRole(user1,"Unit Child Role - View",roleChildMod);
		assertNotNull("Child role is null",roleChildView);
		
		///DirectoryGroupType dir = data1.getGroup();
		UserGroupType ugroup = null;
		
		try {
			ugroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserGroup(user1, "UnitAuthGroup", null, user1.getOrganization());
			GroupService.addUserToGroup(user3, ugroup);
			RoleService.addGroupToRole(ugroup, roleChildAdd);
			boolean switched = AuthorizationService.switchData(user1, rootRole, data1, AuthorizationService.getViewDataPermission(user1.getOrganization()), true);
			assertTrue("Failed to switch data - view",switched);

			switched = AuthorizationService.switchData(user1, roleChildView, data1, AuthorizationService.getViewDataPermission(user1.getOrganization()), true);
			assertTrue("Failed to switch data - view",switched);
			switched = AuthorizationService.switchData(user1, roleChildAdd, data1, AuthorizationService.getCreateDataPermission(user1.getOrganization()), true);
			assertTrue("Failed to switch data - add",switched);
			switched = AuthorizationService.switchData(user1, roleChildMod, data1, AuthorizationService.getEditDataPermission(user1.getOrganization()), true);
			assertTrue("Failed to switch data - mod",switched);
			switched = AuthorizationService.switchData(user1, roleChildDel, data1, AuthorizationService.getDeleteDataPermission(user1.getOrganization()), true);
			assertTrue("Failed to switch data - del",switched);
			//AuthorizationService.switchData(user1, roleChildAddDel, data1, AuthorizationService.getCreateDataPermission(user1.getOrganization()), true);
			//AuthorizationService.switchData(user1, roleChildAddDel, data1, AuthorizationService.getDeleteDataPermission(user1.getOrganization()), true);
			//AuthorizationService.switchData(user1, user3, data1, AuthorizationService.getViewDataPermission(user1.getOrganization()), true);
			//AuthorizationService.switchData(user1, user3, data1, AuthorizationService.getEditDataPermission(user1.getOrganization()), true);
			//AuthorizationService.switchData(user1, user4, data1, AuthorizationService.getViewDataPermission(user1.getOrganization()), true);
			
			//AuthorizationService.switchData(user1, roleParent, data1, AuthorizationService.getViewDataPermission(user1.getOrganization()), true);
			//AuthorizationService.switchData(user1, roleChildAdd, data1, AuthorizationService.getCreateDataPermission(user1.getOrganization()), true);
			//AuthorizationService.switchData(user1, roleChildMod, data1, AuthorizationService.getEditDataPermission(user1.getOrganization()), true);
			//AuthorizationService.switchData(user1, roleChildDel, data1, AuthorizationService.getDeleteDataPermission(user1.getOrganization()), true);
			
			RoleService.addUserToRole(user2,rootRole);
			RoleService.addUserToRole(user3,roleChildView);
			
			/// The role cache must be rebuild in order to see indirect role relationships
			///
			//EffectiveAuthorizationService.rebuildUserRoleCache(Arrays.asList(user2,user3),user2.getOrganization());
			EffectiveAuthorizationService.rebuildPendingRoleCache();

			assertTrue("User 2 should have the view role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildView, user2));
			assertTrue("User 2 should have the mod role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildMod, user2));
			assertTrue("User 2 should have the add role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildAdd, user2));
			assertTrue("User 2 should have the del role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildDel, user2));
			assertTrue("User 3 should have the mod role from indirect group membership",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildMod, user3));
			assertTrue("User 3 should have the add role from direct group membership",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildAdd, user3));
			assertFalse("User 3 should not have the del role",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildDel, user3));
			

			///// Cache cache cache check
			assertTrue("User 2 should have the view role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildView, user2));
			assertTrue("User 2 should have the mod role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildMod, user2));
			assertTrue("User 2 should have the add role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildAdd, user2));
			assertTrue("User 2 should have the del role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildDel, user2));
			assertTrue("User 3 should have the mod role from indirect group membership",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildMod, user3));
			assertTrue("User 3 should have the add role from direct group membership",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildAdd, user3));
			assertFalse("User 3 should not have the del role",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildDel, user3));

			EffectiveAuthorizationService.clearCache(roleChildDel);
			/// Shouldn't be cached anymore
			///
			assertTrue("User 2 should have the del role from the bottom",EffectiveAuthorizationService.getIsUserInEffectiveRole(roleChildDel, user2));

			
			//assertTrue("User 2 cann't view the data",AuthorizationService.canViewData(user2, data1));
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
	}
	*/
	@Test
	public void testDataPermissions(){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		UserType user1 = getUser("testuser1","password1");
		UserType user2 = getUser("testuser2","password1");
		DataType data1 = getData(user1, "testdata1");
		DataType data2 = getData(user1, "testdata2");
		DataType data3 = getData(user2, "testdata3");
		assertNotNull("Data #1 is null",data1);
		assertNotNull("Data #2 is null",data2);
		assertNotNull("Data #3 is null",data3);
		assertNotNull("User #1 is null",user1);
		assertNotNull("User #2 is null",user2);
		
		boolean error = false;
		try {
			
			 /// Cleanup any leftover permission checks
			
			AuthorizationService.authorize(user2, user1, data3, AuthorizationService.getEditPermissionForMapType(NameEnumType.DATA, data3.getOrganizationId()), false);
			EffectiveAuthorizationService.rebuildUserRoleCache(Arrays.asList(new UserType[]{user1,user2}), user1.getOrganizationId());
			logger.info("Check default permissions - only owner and data admin can change data in a given org");
			assertTrue(AuthorizationService.canChange(user1, data1));
			assertTrue(AuthorizationService.canChange(user2, data3));
			assertFalse(AuthorizationService.canChange(user1, data3));
			logger.info("Give user #1 permission to change user #2's data");
			AuthorizationService.authorize(user2, user1, data3, AuthorizationService.getEditPermissionForMapType(NameEnumType.DATA, data3.getOrganizationId()), true);
			//EffectiveAuthorizationService.rebuildUserRoleCache(Arrays.asList(new UserType[]{user1,user2}), user1.getOrganization());
			//EffectiveAuthorizationService.rebuildDataRoleCache(data3);
			EffectiveAuthorizationService.clearCache(data3);
			assertTrue(AuthorizationService.canChange(user1, data3));
			logger.info("Remove user #1 permission to change user #2's data");
			AuthorizationService.authorize(user2, user1, data3, AuthorizationService.getEditPermissionForMapType(NameEnumType.DATA, data3.getOrganizationId()), false);
			//EffectiveAuthorizationService.rebuildUserRoleCache(Arrays.asList(new UserType[]{user1,user2}), user1.getOrganization());
			//EffectiveAuthorizationService.rebuildDataRoleCache(data3);
			EffectiveAuthorizationService.clearCache(data3);
			assertFalse("User #1 can still read User #2's data",AuthorizationService.canChange(user1, data3));
		}
		catch(FactoryException fe){
			logger.error("Error",fe);
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
			error = true;
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
			error = true;
		}
		assertFalse("Error occurred", error);
	}
	
	
}
