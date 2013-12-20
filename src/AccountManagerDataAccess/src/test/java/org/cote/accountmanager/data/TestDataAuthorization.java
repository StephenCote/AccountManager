package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.DataService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.FactoryService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.ServiceUtil;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.TagEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.SecurityUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDataAuthorization extends BaseDataAccessTest {
	
	

	private void switchPermissions(UserType owner, UserType user, DataType data, boolean enabled){
		try {
			AuthorizationService.switchData(owner, user, data, AuthorizationService.getViewDataPermission(owner.getOrganization()),enabled);
			AuthorizationService.switchData(owner, user, data, AuthorizationService.getEditDataPermission(owner.getOrganization()),enabled);
			AuthorizationService.switchData(owner, user, data, AuthorizationService.getCreateDataPermission(owner.getOrganization()),enabled);
			AuthorizationService.switchData(owner, user, data, AuthorizationService.getDeleteDataPermission(owner.getOrganization()),enabled);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncryptedData(){
		UserType user = getUser("testuser1","password");
		
		SecurityBean bean = OrganizationSecurity.getSecurityBean(Factories.getDevelopmentOrganization());
		
		try{
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateUserDirectory(user, "CryptoData");
			DataType data = Factories.getDataFactory().newData(user, dir);
			String d1name = UUID.randomUUID().toString();
			String d2name = UUID.randomUUID().toString();
			DataUtil.setPassword(data, "My special password");
			data.setEncipher(true);
			data.setMimeType("text/plain");
			
			DataUtil.setCipher(data,bean);
			
			DataUtil.setValue(data, "This is the example short text".getBytes());
			data.setName(d1name);
			Factories.getDataFactory().addData(data);
			
			DataType cdata = Factories.getDataFactory().getDataByName(d1name, dir);
			assertNotNull("Data is null",cdata);
			DataUtil.setCipher(cdata,bean);
			DataUtil.setPassword(cdata, "My special password");
			logger.info("Data Value: " + (new String(DataUtil.getValue(cdata))));
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			user2 = Factories.getUserFactory().getUserByName("TestUser2", Factories.getPublicOrganization());
			EffectiveAuthorizationService.rebuildGroupRoleCache(Factories.getPublicOrganization());
			assertNotNull("User is null",user2);
			dir = Factories.getGroupFactory().findGroup(null, "/Home/TestUser1/GalleryHome/.thumbnail", Factories.getPublicOrganization());
			assertNotNull("Dir is null",dir);
			data = Factories.getDataFactory().getDataByName("2355.jpg 128x128", dir);
			assertNotNull("Data is null",data);
			assertTrue("User not authorized to view data",AuthorizationService.canViewData(user2, data));
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	/*
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			ugroup = Factories.getGroupFactory().getCreateUserGroup(user1, "UnitAuthGroup", null, user1.getOrganization());
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
			
			AuthorizationService.switchData(user2, user1, data3, AuthorizationService.getEditDataPermission(data3.getOrganization()), false);
			EffectiveAuthorizationService.rebuildUserRoleCache(Arrays.asList(new UserType[]{user1,user2}), user1.getOrganization());
			logger.info("Check default permissions - only owner and data admin can change data in a given org");
			assertTrue(AuthorizationService.canChangeData(user1, data1));
			assertTrue(AuthorizationService.canChangeData(user2, data3));
			assertFalse(AuthorizationService.canChangeData(user1, data3));
			logger.info("Give user #1 permission to change user #2's data");
			AuthorizationService.switchData(user2, user1, data3, AuthorizationService.getEditDataPermission(data3.getOrganization()), true);
			//EffectiveAuthorizationService.rebuildUserRoleCache(Arrays.asList(new UserType[]{user1,user2}), user1.getOrganization());
			//EffectiveAuthorizationService.rebuildDataRoleCache(data3);
			EffectiveAuthorizationService.clearCache(data3);
			assertTrue(AuthorizationService.canChangeData(user1, data3));
			logger.info("Remove user #1 permission to change user #2's data");
			AuthorizationService.switchData(user2, user1, data3, AuthorizationService.getEditDataPermission(data3.getOrganization()), false);
			//EffectiveAuthorizationService.rebuildUserRoleCache(Arrays.asList(new UserType[]{user1,user2}), user1.getOrganization());
			//EffectiveAuthorizationService.rebuildDataRoleCache(data3);
			EffectiveAuthorizationService.clearCache(data3);
			assertFalse("User #1 can still read User #2's data",AuthorizationService.canChangeData(user1, data3));
		}
		catch(FactoryException fe){
			fe.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		}
		assertFalse("Error occurred", error);
	}
	*/
	/*
	@Test
	public void testDataTags(){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		UserType user1 = getUser("testuser1","password1");
		UserType user2 = getUser("testuser2","password1");
		DataType data1 = getData(user1, "testdata1");
		DataType data2 = getData(user1, "testdata2");
		DataType data3 = getData(user2, "testdata3");
		DataTagType tag1 = getTag("tag1");
		DataTagType tag2 = getTag("tag2");
		try {
			assertTrue("Unable to tag data", AuthorizationService.switchData(user1, tag1, data1, true));
			assertTrue("Unable to tag data", AuthorizationService.switchData(user1, tag1, data2, true));
			assertTrue("Unable to tag data", AuthorizationService.switchData(user2, tag1, data3, true));
			assertTrue("Unable to tag data", AuthorizationService.switchData(user1, tag2, data1, true));
			assertTrue("Unable to tag data", AuthorizationService.switchData(user2, tag2, data3, true));
			
			List<DataParticipantType> parts = Factories.getTagParticipationFactory().convertList(Factories.getTagParticipationFactory().getParticipations(new DataTagType[]{tag1}, ParticipantEnumType.DATA));
			assertTrue("Unexpected count", parts.size() == 3);
			logger.info("Parts = " + parts.size());
			List<DataType> data_list = DataService.getDataForTag(tag1, Factories.getDevelopmentOrganization());
			assertTrue("Unexpected count", data_list.size() == 3);
			logger.info("Data for parts = " + data_list.size());
			
			parts = Factories.getTagParticipationFactory().convertList(Factories.getTagParticipationFactory().getParticipations(new DataTagType[]{tag2}, ParticipantEnumType.DATA));
			assertTrue("Unexpected count", parts.size() == 2);
			logger.info("Parts = " + parts.size());
			data_list = DataService.getDataForTag(tag2, Factories.getDevelopmentOrganization());
			assertTrue("Unexpected count", data_list.size() == 2);
			logger.info("Data for parts = " + data_list.size());

			parts = Factories.getTagParticipationFactory().convertList(Factories.getTagParticipationFactory().getTagParticipations(new DataTagType[]{tag1,tag2}, ParticipantEnumType.DATA));
			logger.info("Parts = " + parts.size());
			logger.info("Perf Note/Bug: getTagParticipations returns N instances of participant ids instead of just 1.  This doesn't affect the result, but does add duplicate entries to the query.");
			//assertTrue("Unexpected count", parts.size() == 4);

			data_list = DataService.getDataForTags(new DataTagType[]{tag1,tag2}, Factories.getDevelopmentOrganization());
			logger.info("Data for parts = " + data_list.size());
			assertTrue("Unexpected count", data_list.size() == 2);

			assertTrue("Unable to tag data", AuthorizationService.switchData(user2, tag2, data3, false));
			data_list = DataService.getDataForTags(new DataTagType[]{tag1,tag2}, Factories.getDevelopmentOrganization());
			logger.info("Data for parts = " + data_list.size());
			assertTrue("Unexpected count", data_list.size() == 1);	
			

			
			//Factories.getTagParticipationFactory().GetDataFromParticipations(list, detailsOnly, startRecord, recordCount, organization)
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	*/
}
