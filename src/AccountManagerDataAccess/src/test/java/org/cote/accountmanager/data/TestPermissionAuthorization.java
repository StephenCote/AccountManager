package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.junit.Test;

public class TestPermissionAuthorization extends BaseDataAccessTest {
	private static String testAuthUser1 = "Auth User 1"; 
	private static String testAuthUser2 = "Auth User 2"; 
	private static String testAuthUser3 = "Auth User 3"; 
	private static String testAuthUser4 = "Auth User 4"; 
	
	@Test
	public void testAuthorizePermissions(){
		

		UserType user = getUser(testAuthUser1,"password1");
		UserType user2 = getUser(testAuthUser2,"password1");
		UserType user3 = getUser(testAuthUser3,"password1");
		UserType user4 = getUser(testAuthUser4,"password1");
		UserGroupType userGroup = getGroup(user, "Friends", GroupEnumType.USER, user.getHomeDirectory());
		BaseRoleType userHomeRole = null;
		BasePermissionType userHomePerm = null;
		BasePermissionType userPerm = null;
		BasePermissionType userPermChild = null;

		UserRoleType userRole = null;
		boolean inRole = false;
		boolean inRole2 = false;
		boolean inGroup = false;
		try {
			userHomePerm = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getUserPermission(user, PermissionEnumType.USER, user.getOrganizationId());
			userHomeRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(user, RoleEnumType.USER, user.getOrganizationId());
			userRole = getRole(user, "Friends", RoleEnumType.USER, userHomeRole);
			userPerm = getPermission(user, "Friend Benefit", PermissionEnumType.USER,userHomePerm);
			userPermChild = getPermission(user, "Friend Child Benefit", PermissionEnumType.USER, userPerm);
			UserRoleType brt = RoleService.getAccountUsersReaderUserRole(user.getOrganizationId());
			UserRoleType prt = RoleService.getPermissionReaderUserRole(user.getOrganizationId());
			
			RoleService.removeUserFromRole(brt, user);
			RoleService.removeUserFromRole(prt, user3);
			
			GroupService.removeUserFromGroup(userGroup, user4);
			
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			
			inRole = RoleService.getIsUserInRole(brt, user);
			//assertFalse("User shouldn't be in reader role", inRole);
			if(!inRole && RoleService.addUserToRole(user, brt)) {
				/// logger.info("Reset role membership");
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				inRole = RoleService.getIsUserInRole(brt, user);
				
			}
			
			inRole2 = RoleService.getIsUserInRole(prt, user3);
			assertFalse("User 3 shouldn't be in permission reader role", inRole2);
			if(!inRole2 && RoleService.addUserToRole(user3, prt)) {
				/// logger.info("Reset role membership");
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				inRole2 = RoleService.getIsUserInRole(prt, user3);
				
			}
			
			inGroup = GroupService.getIsUserInGroup(userGroup, user4);
			assertFalse("User 4 shouldn't be in user group", inGroup);
			if(!inGroup && GroupService.addUserToGroup(user4, userGroup)) {
				GroupService.getIsUserInGroup(userGroup, user4);
			}

		} catch (FactoryException | ArgumentException | DataAccessException e) {
			logger.error(e);
		}

		assertTrue("User 1 not in user readers role", inRole);
		assertTrue("User 3 not in permission readers role", inRole2);

		
		logger.info("Reset role membership");
		boolean inRole3 = false;
		try {
			inRole3 = RoleService.getIsUserInRole(userRole, user2);
		} catch (ArgumentException | FactoryException e1) {
			logger.error(e1);
		}
		logger.info("User #2 starting in role: " + inRole3);
		boolean setMem =  BaseService.setMember(user, AuditEnumType.ROLE, userRole.getObjectId(), AuditEnumType.USER, user2.getObjectId(), false);
		logger.info("User #2 removed from role: " + setMem);
		setMem = BaseService.setMember(user, AuditEnumType.ROLE, userRole.getObjectId(), AuditEnumType.USER, user2.getObjectId(), true);
		assertTrue("Failed to set user " + user2.getUrn()  + " member to role " + userRole.getUrn(), setMem);
		
		logger.info("Reset group membership");
		
		setMem =  BaseService.setMember(user, AuditEnumType.ROLE, userRole.getObjectId(), AuditEnumType.GROUP, userGroup.getObjectId(), false);
		setMem = BaseService.setMember(user, AuditEnumType.ROLE, userRole.getObjectId(), AuditEnumType.GROUP, userGroup.getObjectId(), true);
		assertTrue("Failed to set user group " + userGroup.getUrn() + " to role " + userRole.getUrn(), setMem);

		
		assertNotNull("User perm is null", userPerm);
		
		boolean roleAuthZ = BaseService.authorizeRole(AuditEnumType.PERMISSION, user.getOrganizationId(), userRole.getId(), userPerm, true, true, false, false,user);
		assertTrue("Failed to authorize role to view permission", roleAuthZ);
		
		logger.info("Check that User #1 has full control over permission object");
		boolean canView = false;
		boolean canExec = false;
		boolean canChange = false;
		boolean canDelete = false;
		boolean canCreate = false;
		
		boolean canView2 = false;
		boolean canExec2 = false;
		boolean canChange2 = false;
		boolean canDelete2 = false;
		boolean canCreate2 = false;
		
		boolean canView3 = false;
		boolean canExec3 = false;
		boolean canChange3 = false;
		boolean canDelete3 = false;
		boolean canCreate3 = false;
		
		try {
			canView = AuthorizationService.canView(user, userPerm);
			canExec = AuthorizationService.canExecute(user, userPerm);
			canChange = AuthorizationService.canChange(user, userPerm);
			canDelete = AuthorizationService.canDelete(user, userPerm);
			canCreate = AuthorizationService.canCreate(user, userPerm);
			
			canView2 = AuthorizationService.canView(user2, userPerm);
			canExec2 = AuthorizationService.canExecute(user2, userPerm);
			canChange2 = AuthorizationService.canChange(user2, userPerm);
			canDelete2 = AuthorizationService.canDelete(user2, userPerm);
			canCreate2 = AuthorizationService.canCreate(user2, userPerm);
			
			canView3 = AuthorizationService.canView(user3, userPerm);
			canExec3 = AuthorizationService.canExecute(user3, userPerm);
			canChange3 = AuthorizationService.canChange(user3, userPerm);
			canDelete3 = AuthorizationService.canDelete(user3, userPerm);
			canCreate3 = AuthorizationService.canCreate(user3, userPerm);
			
		} catch (ArgumentException | FactoryException e) {
			logger.error(e);
		}
		assertTrue("User #1 failed expected ownership entitlement", canView && canExec && canChange && canDelete && canCreate);
		
		logger.info("User #2: " + canView2 + " / " + canChange2 + " / " + canCreate2 + " / " + canDelete2 + " / " + canExec2);
		assertTrue("User #2 failed expected read and change entitlement", canView2 && canChange2);
		assertFalse("User #2 has more rights than expected",  canExec2 || canDelete2 || canCreate2);
		
		logger.info("User #3: " + canView3 + " / " + canChange3 + " / " + canCreate3 + " / " + canDelete3 + " / " + canExec3);
		assertTrue("User #3 failed expected readership entitlement", canView3);
		assertFalse("User #3 has more rights than expected",  canExec3 || canChange3 || canDelete3 || canCreate3);
		
		int count2 = BaseService.countInParent(AuditEnumType.PERMISSION, userPerm, user2);
		assertTrue("Expected to be able to count", count2 > 0);
		
		List<BasePermissionType> perms = BaseService.listByParentObjectId(AuditEnumType.PERMISSION, "USER", userPerm.getObjectId(), 0L, 0, user2);
		assertTrue("Expected to be able to list", perms.size() > 0);
	}
	
}
