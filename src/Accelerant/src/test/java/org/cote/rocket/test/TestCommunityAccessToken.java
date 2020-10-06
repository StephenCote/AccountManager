package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.security.TokenService;
import org.cote.accountmanager.data.security.TokenUtil;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.data.services.UserService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.RocketSecurity;
import org.junit.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public class TestCommunityAccessToken extends BaseAccelerantTest {
	public static final Logger logger = LogManager.getLogger(TestCommunityAccessToken.class);
	private static String communityName =  "Test Community Access Token Lifecycle";
	private static String projectName = "Test Community Access Token Project";
	private static String testApplicationName = "Access Token Application";
	private static String testTokenPersonName = "Test Token User";
	private static boolean cleanupProject = false;
	
	@Test
	public void TestTokenGeneration() {
		String userToken1 = TokenService.getJWTToken(testUser, testUser);
		assertNotNull("User token is null", userToken1);
		Claims valid = TokenService.validateJWTToken(userToken1);
		logger.info("Valid: '" + valid.getSubject() + "'");
	}
	
	@Test
	public void TestApplicationAccess(){
		
		Factories.cleanupOrphans();
		
		UserType linkedUser = this.getUser(testTokenPersonName, "password", testUser.getOrganizationId());
		assertNotNull("User is null", linkedUser);
		ICommunityProvider provider = getProvider();

		LifecycleType lf = getProviderCommunity(testUser, communityName,false);
		assertNotNull("Lifecycle is null", lf);
		
		ProjectType p1 = getProviderCommunityProject(testUser, lf, projectName,cleanupProject);
		assertNotNull("Project is null",p1);

		/// Note: the current community role structure is not a straight hierachy, so the role hierarchy computation won't count, for example, a project admin role being effective if in a community admin role
		/*
		boolean isAdmin = false;
		try {
			isAdmin = RoleService.getIsUserInEffectiveRole(RocketSecurity.getProjectAdminRole(p1),testUser);
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertTrue("User should be considered an administrator", isAdmin);
		*/
		/// BUG: Note, because the role structure doesn't inherit, certain admin capabilities that depend on role membership won't work if not directly in the role
		/// because the community role entitlements are assigned to the project, versus using role inheritence
		assertTrue("Failed to enroll reader",provider.enrollReaderInCommunityProject(getAdminUser(testUser.getOrganizationId()), linkedUser.getObjectId(), lf.getObjectId(),p1.getObjectId()));
		
		
		DirectoryGroupType app1 = provider.getCommunityProjectApplication(testUser, lf.getObjectId(), p1.getObjectId(), testApplicationName);
		if(app1 == null) {
			assertTrue("Failed to create application", provider.createCommunityProjectApplication(testUser, lf.getObjectId(), p1.getObjectId(), testApplicationName));
		}
		app1 = provider.getCommunityProjectApplication(testUser, lf.getObjectId(), p1.getObjectId(), testApplicationName);
		assertNotNull("Application is null", app1);

		AccountType a1 = getLinkedAppAccount(p1, testUser, linkedUser, app1, testTokenPersonName);
		AccountGroupType ag1 = getAppGroup(testUser, app1, "Capability 1");
		boolean authZ = false;
		List<BaseRoleType> roles = new ArrayList<>();
		List<EntitlementType> linkedUserEntitlements = new ArrayList<>();
		List<EntitlementType> testUserEntitlements = new ArrayList<>();
		
		
		/// Need to update test for authorization considerations - testUser can't read linkedUser, and linkedUser is attached to a person owned by testUser
		/// So, to get the linked person for linked user, the context user (testUser) must be able to read both
		///
		List<PersonType> linkedPersons = UserService.readPersonsForUser(linkedUser, linkedUser, false);
		assertTrue("Linked Person is null", linkedPersons.size() > 0);
		PersonType linkedPerson = linkedPersons.get(0);
		try {
			EffectiveAuthorizationService.rebuildRoleCache(linkedUser);
			//AuthorizationService.switchParticipant(testUser, a1, ag1, true);
			BaseService.setMember(testUser, AuditEnumType.GROUP, ag1.getObjectId(), AuditEnumType.ACCOUNT, a1.getObjectId(), true);
			//BaseService.is
			assertTrue("Account should be a member of the group", GroupService.getIsAccountInGroup(ag1, a1));
			roles = EffectiveAuthorizationService.getEffectiveRoles(linkedPerson);
			
			linkedUserEntitlements = BaseService.aggregateEntitlementsForMember(linkedUser, linkedUser);
			testUserEntitlements = BaseService.aggregateEntitlementsForMember(testUser, testUser);
					//EffectiveAuthorizationService.getEffectiveMemberEntitlements(app1, linkedUser, new BasePermissionType[0], false);
			authZ = true;
		} catch (NullPointerException | FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertTrue("Authorization check should have passed", authZ);
		assertTrue("Expected some entitlements", linkedUserEntitlements.size() > 0);
		/// 20 is arbitrary here, but if the match condition were null (aka - a bug) then all groups could be returned
		assertTrue("Didn't expect EVERY entitlement", linkedUserEntitlements.size() < 20);
		List<BaseGroupType> objs = BaseService.listForMember(AuditEnumType.GROUP, testUser, a1, FactoryEnumType.ACCOUNT);
		assertTrue("Expected at least 1 group", objs.size() > 0);
		// logger.info("Linked user has " + roles.size() + " roles");
		
		logger.info("Test user entitlements has " + testUserEntitlements.size() + " entitlements");
		for(EntitlementType ent : testUserEntitlements) {
			logger.info("Entitlement: " + ent.getEntitlementType() + " " + ent.getEntitlementName() + " " + ent.getEntitlementId());
		}
		
		logger.info("Linked user entitlements has " + linkedUserEntitlements.size() + " entitlements");
		for(EntitlementType ent : linkedUserEntitlements) {
			logger.info("Entitlement: " + ent.getEntitlementType() + " " + ent.getEntitlementName() +  " " + ent.getEntitlementId());
		}
		
		SecurityBean tokenBean1 = TokenUtil.getJWTSecurityBean(linkedUser);
		SecurityBean tokenBean2 = TokenUtil.getJWTSecurityBean(testUser);
		
		assertNotNull("Token Bean 1 is null", tokenBean1);
		assertNotNull("Token Bean 2 is null", tokenBean2);
		
		String token1 = TokenService.getJWTToken(testUser,  a1);
		assertNotNull("Token is null",token1);
		
		Claims valid = TokenService.validateJWTToken(token1);
		assertNotNull("Token validation is null", valid);
		logger.info("Valid :'" + valid.getSubject() + "'");
		
		Jws<Claims> claims = TokenService.extractJWTClaims(token1);
		assertNotNull("Claims are null", claims);
		//claims.getBody()
		
	}
	private AccountGroupType getAppGroup(UserType owner, DirectoryGroupType app, String groupName) {
		logger.info("Find: " + app.getPath() + "/" + groupName);
		// AccountGroupType grp1 = BaseService.readByName(AuditEnumType.GROUP, app.getId(), groupName, owner);
		AccountGroupType grp1 = BaseService.readByNameInParent(AuditEnumType.GROUP, app, groupName, GroupEnumType.ACCOUNT.toString(), owner);
		if(grp1 == null) {

			try {
				grp1 = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newAccountGroup(owner, groupName, app, app.getOrganizationId());
				((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).add(grp1);
				grp1 = BaseService.readByName(AuditEnumType.GROUP, app.getId(), groupName, owner);
			} catch (ArgumentException | FactoryException e) {
				logger.error(e);
			}
			
		}
		
		return grp1;
		//return BaseService.makeFind(AuditEnumType.GROUP, "ACCOUNT", app.getPath() + "/" + groupName, owner);
	}
	private AccountType getLinkedAppAccount(ProjectType project, UserType owner, UserType linkedUser, DirectoryGroupType app, String accountName) {
		AccountType a1 = BaseService.readByName(AuditEnumType.ACCOUNT, app, accountName, testUser);
		if(a1 == null) {
			try {
				a1 = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount(testUser, accountName, AccountEnumType.DEVELOPMENT, AccountStatusEnumType.RESTRICTED, app.getId());

				((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).add(a1);
				a1 = BaseService.readByName(AuditEnumType.ACCOUNT, app, accountName, testUser);
			}
			catch(ArgumentException | FactoryException f) {
				logger.error(f.getMessage());
				a1 = null;
			}
		}
		assertNotNull("Account is null", a1);
		
		DirectoryGroupType personsDir = RocketSecurity.getProjectDirectory(testUser, project, "Persons");
		PersonType per = BaseService.readByName(AuditEnumType.PERSON, personsDir, accountName, testUser);
		if(per == null) {
			try {
				per = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser, personsDir.getId());
				per.setName(accountName);
				per.getUsers().add(linkedUser);
				per.getAccounts().add(a1);
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).add(per);
				per = BaseService.readByName(AuditEnumType.PERSON, personsDir, accountName, testUser);
			}
			catch(ArgumentException | FactoryException f) {
				logger.error(f.getMessage());
				per = null;
			}
		}
		
		assertNotNull("Person is null", per);
		return a1;
	}
}
