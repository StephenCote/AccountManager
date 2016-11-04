package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.junit.Test;

public class TestUserFactory extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestUserFactory.class.getName());
	private static String testUserName1 = "Example-" + UUID.randomUUID().toString();
	private static String testUserName2 = "Example-" + UUID.randomUUID().toString();
	private static String testUserPassword = "password1";
	
	
	@Test
	public void testAddUser(){
		OrganizationFactory of = Factories.getOrganizationFactory();
		UserType user1 = Factories.getUserFactory().newUser(testUserName1, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
		UserType user2 = Factories.getUserFactory().newUser(testUserName2, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
		boolean add = false;
		boolean error = false;
		logger.info(testUserPassword + ":" + testUserPassword.length());
		try{
			add = (
				Factories.getUserFactory().add(user1, true)
				&&
				Factories.getUserFactory().add(user2, true)
			);
		}
		catch(ArgumentException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			error = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
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
			user = Factories.getUserFactory().getByName(testUserName1,Factories.getDevelopmentOrganization().getId());
			Factories.getUserFactory().populate(user);
			assertTrue(user.getPopulated());
		}

		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			admin = Factories.getUserFactory().getByName("Admin", Factories.getDevelopmentOrganization().getId());
			assertNotNull(admin);
			devRole = RoleService.getCreateUserRole(admin, "Dev Role", null);
			assertNotNull(devRole);
			user1 = Factories.getUserFactory().getByName(testUserName1,Factories.getDevelopmentOrganization().getId());
			Factories.getUserFactory().populate(user1);
			assertTrue("Did not populate user", user1.getPopulated());
			/// moved addUserToRole outside of the FactoryService
			assertTrue("Did not add user to role", RoleService.addUserToRole(user1, devRole));
			assertTrue("Could not find user in role", RoleService.getIsUserInRole(devRole, user1));
			assertTrue("Could not remove user from role", RoleService.removeUserFromRole(devRole, user1));
			assertFalse("User still in role cache",RoleService.getIsUserInRole(devRole, user1));
		}

		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			error = true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			error = true;
			e.printStackTrace();
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
			admin = Factories.getUserFactory().getByName("Admin", Factories.getDevelopmentOrganization().getId());
			assertNotNull(admin);
			AccountRoleType adminRole = RoleService.getDataAdministratorAccountRole(Factories.getDevelopmentOrganization().getId());
			assertNotNull("Admin role is null", adminRole);
			//assertTrue("Admin should be a data administrator in its own organization", AuthorizationService.isDataAdministratorInMapOrganization(admin, admin.getOrganization()));
			devRole = RoleService.getCreateUserRole(admin, "Dev Role", null);
			assertNotNull(devRole);
			user1 = Factories.getUserFactory().getByName(testUserName1,Factories.getDevelopmentOrganization().getId());
			user2 = Factories.getUserFactory().getByName(testUserName2,Factories.getDevelopmentOrganization().getId());
			Factories.getUserFactory().populate(user1);
			Factories.getUserFactory().populate(user2);
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
			fe.printStackTrace();
			error = true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			error = true;
			e.printStackTrace();
		}
		assertFalse("Error occurred", error);

	}

}