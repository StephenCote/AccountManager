package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonPermissionType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.junit.Test;

public class TestPermissions extends BaseDataAccessTest{
	
	

	

	
	@Test
	public void TestPersonPermissions(){
		boolean reset = false;
		DirectoryGroupType app1 = getApplication("Application 1");
		PersonRoleType roleP = getApplicationRole("Role #1",RoleEnumType.PERSON,app1);
		AccountRoleType roleP2 = getApplicationRole("Role #2",RoleEnumType.ACCOUNT,app1);
		ApplicationPermissionType per1 = getApplicationPermission("Permission #1",PermissionEnumType.APPLICATION,app1);
		ApplicationPermissionType per2 = getApplicationPermission("Permission #2",PermissionEnumType.APPLICATION,app1);
		ApplicationPermissionType per3 = getApplicationPermission("Permission #3",PermissionEnumType.APPLICATION,app1);
		ApplicationPermissionType per4 = getApplicationPermission("Permission #4",PermissionEnumType.APPLICATION,app1);
		if(reset){
			try {
				
				Factories.getGroupFactory().deleteDirectoryGroup(app1);
				Factories.getPermissionFactory().deletePermission(per1);
				Factories.getPermissionFactory().deletePermission(per2);
				Factories.getPermissionFactory().deletePermission(per3);
				Factories.getPermissionFactory().deletePermission(per4);
			} catch (FactoryException | ArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Factories.cleanupOrphans();
			app1 = getApplication("Application 1");
			per1 = getApplicationPermission("Permission #1",PermissionEnumType.APPLICATION,app1);
			per2 = getApplicationPermission("Permission #2",PermissionEnumType.APPLICATION,app1);
			per3 = getApplicationPermission("Permission #3",PermissionEnumType.APPLICATION,app1);
			per4 = getApplicationPermission("Permission #4",PermissionEnumType.APPLICATION,app1);
		}
		BasePermissionType perc1 = null;
		try{
			PersonPermissionType per5 = Factories.getPermissionFactory().getPermissionByName("Permission #5", PermissionEnumType.PERSON, per1, per1.getOrganizationId());
			if(reset && per5 != null){
				Factories.getPermissionFactory().deletePermission(per5);
				per5 = null;
			}
			if(per5 == null){
				per5 = (PersonPermissionType)Factories.getPermissionFactory().newPermission(testUser2, "Permission #5", PermissionEnumType.PERSON, per1, per1.getOrganizationId());
				Factories.getPermissionFactory().addPermission(per5);
				per5 = Factories.getPermissionFactory().getPermissionByName("Permission #5", PermissionEnumType.PERSON, per1, per1.getOrganizationId());
			}
			Factories.getPermissionFactory().denormalize(per5);
			assertNotNull("Permission is null",per5);

			//Factories.getPermissionFactory().denormalize(per1);
			/// Find permission by path
			/// TODO: This path lookup is not valid for mixed types because the parent path resolution might not find the correct parent (eg: testuser ACCOUNT vs testuser PERSON types, etc)
			/*
			logger.info("Looking for '" + per5.getParentPath() + "/" + per5.getName() + "' from " + Factories.getPermissionFactory().getPermissionPath(per5));
			perc1 = Factories.getPermissionFactory().findPermission(PermissionEnumType.UNKNOWN, per5.getParentPath() + "/" + per5.getName(), per5.getOrganizationId());
			assertNotNull("Permission #1 Check (" + per5.getParentPath() + "/" + per5.getName() + ") was null",perc1);
			*/
			
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			logger.error(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PersonType acct1 = getApplicationPerson("Person #1", app1);
		PersonType acct2 = getApplicationPerson("Person #2", app1);
		PersonType acct3 = getApplicationPerson("Person #3", app1);
		PersonType acct4 = getApplicationPerson("Person #4", app1);
		PersonType acct5 = getApplicationPerson("Person #5", app1);
		AccountType pacct4 = getApplicationAccount("Account #4", app1);
		AccountType pacct5 = getApplicationAccount("Account #5", app1);
		
		assertNotNull("Person is null",acct1);
		boolean havePerm = false;
		/// Try giving account #1 permission #1 to application #1 (a directory group in AM)
		try {
			Factories.getPersonFactory().populate(acct4);
			Factories.getPersonFactory().populate(acct5);
			if(acct4.getAccounts().size() == 0){
				acct4.getAccounts().add(pacct4);
				Factories.getPersonFactory().updatePerson(acct4);
			}
			if(acct5.getAccounts().size() == 0){
				acct5.getAccounts().add(pacct5);
				Factories.getPersonFactory().updatePerson(acct5);
			}
			assertTrue("User can't view the permission", AuthorizationService.canView(testUser,per1));
			AuthorizationService.authorize(testUser, acct1, app1, per1, true);
			EffectiveAuthorizationService.rebuildCache();
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct1,app1, new BasePermissionType[] { per1 } );
			assertTrue("Person #1 should have the permission",havePerm);
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct2,app1, new BasePermissionType[] { per1 } );
			assertFalse("Person #2 should not have the permission", havePerm);
			AuthorizationService.authorize(testUser, roleP, app1, per2, true);
			RoleService.addPersonToRole(acct3, roleP);
			EffectiveAuthorizationService.rebuildCache();
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct3,app1, new BasePermissionType[] { per1 } );
			assertFalse("Person #3 should not have the permission", havePerm);
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct3,app1, new BasePermissionType[] { per2 } );
			assertTrue("Person #3 should have the permission", havePerm);
			
			AuthorizationService.authorize(testUser, pacct4, app1, per3, true);
			EffectiveAuthorizationService.rebuildCache();
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct4,app1, new BasePermissionType[] { per3 } );
			assertTrue("Person #4 should have the permission because their account has the permission",havePerm);
			
			AuthorizationService.authorize(testUser, roleP2, app1, per4, true);
			RoleService.addAccountToRole(pacct5, roleP2);
			EffectiveAuthorizationService.rebuildCache();
			
			havePerm = EffectiveAuthorizationService.getIsAccountInEffectiveRole(roleP2, pacct5);
			assertTrue("Person #5 should be in role " + roleP2.getName(),havePerm);
			
			havePerm = EffectiveAuthorizationService.getIsPersonInEffectiveRole(roleP2, acct5);
			assertTrue("PENDING: Person #5 should be in role " + roleP2.getName() + " because their account is in that role",havePerm);
			
			havePerm = EffectiveAuthorizationService.getIsPersonInEffectiveRole(roleP, acct3);
			assertTrue("Person #3 should be in role " + roleP.getName(),havePerm);

			havePerm = EffectiveAuthorizationService.getIsPersonInEffectiveRole(roleP, acct1);
			assertFalse("Person #1 should not be in role " + roleP.getName(),havePerm);
			
			havePerm = EffectiveAuthorizationService.getIsPersonInEffectiveRole(roleP, acct2);
			assertFalse("Person #2 should not be in role " + roleP.getName(),havePerm);
			
			
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
/*	
	@Test
	public void TestAccountPermissions(){
		DirectoryGroupType app1 = getApplication("Application #1");
		AccountRoleType roleP = getApplicationRole("Role #1",RoleEnumType.ACCOUNT,app1);
		ApplicationPermissionType per1 = getApplicationPermission("Permission #1",PermissionEnumType.APPLICATION,app1);
		ApplicationPermissionType per2 = getApplicationPermission("Permission #2",PermissionEnumType.APPLICATION,app1);
		assertNotNull("Permission is null",per1);
		AccountType acct1 = getApplicationAccount("Account #1", app1);
		AccountType acct2 = getApplicationAccount("Account #2", app1);
		AccountType acct3 = getApplicationAccount("Account #3", app1);
		AccountType acct4 = getApplicationAccount("Account #4", app1);
		assertNotNull("Account is null",acct1);
		boolean havePerm = false;
		/// Try giving account #1 permission #1 to application #1 (a directory group in AM)
		try {
			AuthorizationService.authorize(testUser, acct1, app1, per1, true);
			EffectiveAuthorizationService.rebuildCache();
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct1,app1, new BasePermissionType[] { per1 } );
			assertTrue("Account #1 should have the permission",havePerm);
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct2,app1, new BasePermissionType[] { per1 } );
			assertFalse("Account #2 should not have the permission", havePerm);
			AuthorizationService.authorize(testUser, roleP, app1, per2, true);
			RoleService.addAccountToRole(acct3, roleP);
			EffectiveAuthorizationService.rebuildCache();
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct3,app1, new BasePermissionType[] { per1 } );
			assertFalse("Account #3 should not have the permission", havePerm);
			havePerm = EffectiveAuthorizationService.getGroupAuthorization(acct3,app1, new BasePermissionType[] { per2 } );
			assertTrue("Account #3 should have the permission", havePerm);
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
	/*
	@Test
	public void TestBulkPermissions(){
		String appName = UUID.randomUUID().toString();
		DirectoryGroupType app1 = getApplication("Application " + appName);
		try {
			ApplicationPermissionType perB = Factories.getPermissionFactory().makePath(testUser, PermissionEnumType.APPLICATION, app1);
			
			String sess = BulkFactories.getBulkFactory().newBulkSession();
			for(int i = 0; i < 50; i++){
				ApplicationPermissionType p = (ApplicationPermissionType)Factories.getPermissionFactory().newPermission(testUser, "Permission " + appName + " " + (i+1), PermissionEnumType.APPLICATION,perB, testUser.getOrganizationId());
				BulkFactories.getBulkFactory().createBulkEntry(sess, FactoryEnumType.PERMISSION, p);
				
				/// Test that the bulk entry is 'discoverable'
				ApplicationPermissionType p2 = Factories.getPermissionFactory().getPermissionByName("Permission " + appName + " " + (i+1), PermissionEnumType.APPLICATION, perB, testUser.getOrganizationId());
				assertNotNull("Bulk cached permission not available",p2);
			}
			BulkFactories.getBulkFactory().write(sess);
			
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
	/*
	@Test
	public void TestPermissionHierarchy(){
		ObjectPermissionType rootPer = getCreatePermission(testUser,"PermissionRoot", PermissionEnumType.OBJECT, null,testUser.getOrganizationId());
		assertNotNull("Permission is null", rootPer);
		ObjectPermissionType childPer1 = getCreatePermission(testUser,"Child1", PermissionEnumType.OBJECT, rootPer,testUser.getOrganizationId());
		ObjectPermissionType childPer2 = getCreatePermission(testUser,"Child2", PermissionEnumType.OBJECT, rootPer,testUser.getOrganizationId());
		ObjectPermissionType subPer = getCreatePermission(testUser,"Sub", PermissionEnumType.OBJECT, rootPer,testUser.getOrganizationId());
		ObjectPermissionType childPer1a = getCreatePermission(testUser,"Child1", PermissionEnumType.OBJECT, subPer,testUser.getOrganizationId());
		ObjectPermissionType childPer2a = getCreatePermission(testUser,"Child2", PermissionEnumType.OBJECT, subPer,testUser.getOrganizationId());
		
		
		rootPer.getAttributes().add(Factories.getAttributeFactory().newAttribute(rootPer, "DemoAttr", "Demo value"));
		Factories.getAttributeFactory().updateAttributes(rootPer);
		
		List<ObjectPermissionType> per = new ArrayList<ObjectPermissionType>();
		List<ObjectPermissionType> per2 = new ArrayList<ObjectPermissionType>();
		try {
			per = Factories.getPermissionFactory().getPermissionList(PermissionEnumType.OBJECT, 0, 10, testUser.getOrganizationId());
			per2 = Factories.getPermissionFactory().getPermissionList(rootPer, PermissionEnumType.OBJECT, 0, 10, testUser.getOrganizationId());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/// Just check greater than zero off the root since there are default permissions there
		///
		assertTrue("List is empty",per.size() > 0);
		assertTrue("List size " + per2.size() + " should be three",per2.size() == 3);
		logger.info("Permission size = " + per.size());
		for(int i = 0; i < per.size();i++) logger.info("\t" + per.get(i).getName());
	}
	*/
	
}