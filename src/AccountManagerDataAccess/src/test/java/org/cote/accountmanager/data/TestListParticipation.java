package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.junit.Test;

import bsh.Console;

public class TestListParticipation extends BaseDataAccessTest {

	

	private static String testAuthUser1 = "Auth User 1"; 
	private static String testAuthUser2 = "Auth User 2"; 
	private static String testAuthUser3 = "Auth User 3"; 
	private static String testAuthUser4 = "Auth User 4"; 

	@Test
	public void TestRoleMemberCount() {
		
		
		setupRoles();
		
		UserRoleType userHomeRole = null;
		UserRoleType roleRoot = null;
		UserRoleType branch1 = null;
		UserType user = getUser(testAuthUser1,"password1");
		
		try {
			userHomeRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(user, RoleEnumType.USER, user.getOrganizationId());
			
			roleRoot = getRole(user, "Root", RoleEnumType.USER, userHomeRole);
			branch1 = getRole(user, "Branch 1", RoleEnumType.USER, roleRoot);
			
			IParticipationFactory pFact = Factories.getParticipationFactory(FactoryEnumType.valueOf("ROLEPARTICIPATION"));
			assertNotNull("Factory is null", pFact);
			
			int count = pFact.countParticipations(new NameIdType[] {branch1}, ParticipantEnumType.USER);
			
			assertTrue("Expected count to be 3", count == 3);
			
			List<UserType> users = pFact.listParticipations(ParticipantEnumType.USER, new NameIdType[] {branch1}, 0L, 10, testUser.getOrganizationId());
			
			logger.info("Members: " + users.size());
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e);
		}
	}
	
	
	private void setupRoles(){
		UserType user = getUser(testAuthUser1,"password1");
		UserType user2 = getUser(testAuthUser2,"password1");
		UserType user3 = getUser(testAuthUser3,"password1");
		UserType user4 = getUser(testAuthUser4,"password1");
		

		
		UserRoleType userHomeRole = null;
		UserRoleType roleRoot = null;
		UserRoleType branch1 = null;

		try {
			EffectiveAuthorizationService.pendUpdate(Arrays.asList(user,user2,user3,user4));
			EffectiveAuthorizationService.rebuildPendingRoleCache();

			userHomeRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(user, RoleEnumType.USER, user.getOrganizationId());
			
			roleRoot = getRole(user, "Root", RoleEnumType.USER, userHomeRole);
			branch1 = getRole(user, "Branch 1", RoleEnumType.USER, roleRoot);
			
			resetRoleMembership(branch1,user2, true);
			resetRoleMembership(branch1,user3, true);
			resetRoleMembership(branch1,user4, true);

			
		} catch (ArgumentException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	
	
	/// Test, Remove if needed, Add, and assert
	private boolean resetRoleMembership(UserRoleType role, UserType user, boolean add) {
		boolean inRole = false;
		try {
			inRole = RoleService.getIsUserInRole(role, user);
			if(inRole && RoleService.removeUserFromRole(role, user)) {
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				inRole = RoleService.getIsUserInRole(role, user);
			}
			else {
				logger.warn("Failed to remove " + user.getUrn() + " from " + role.getUrn());
			}
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
