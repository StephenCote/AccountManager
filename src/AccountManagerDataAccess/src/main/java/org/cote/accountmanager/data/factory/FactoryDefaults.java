/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.factory;

import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.security.ApiConnectionConfigurationService;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;

public class FactoryDefaults {
	
	public static final Logger logger = LogManager.getLogger(FactoryDefaults.class);
	
	protected static String[] default_application_permissions = new String[]{
		"ApplicationView",
		"ApplicationEdit",
		"ApplicationDelete",
		"ApplicationCreate"
	};
	protected static String[] default_object_permissions = new String[]{
		"ObjectView",
		"ObjectEdit",
		"ObjectDelete",
		"ObjectCreate"
	};
	protected static String[] default_account_permissions = new String[]{
		"AccountView",
		"AccountEdit",
		"AccountDelete",
		"AccountCreate"
	};
	/*
	protected static String[] default_person_permissions = new String[]{
		"AccountView",
		"AccountEdit",
		"AccountDelete",
		"AccountCreate"
	};
	
	protected static String[] default_data_permissions = new String[]{
			"DataView",
			"DataEdit",
			"DataDelete",
			"DataCreate"
	};
	protected static String[] default_role_permissions = new String[]{
			"RoleView",
			"RoleEdit",
			"RoleCreate",
			"RoleDelete"
	};
	protected static String[] default_group_permissions = new String[]{
			"GroupView",
			"GroupEdit",
			"GroupCreate",
			"GroupDelete"
	};
	*/
	/// Changing this to be a random string
	/// private static String DocumentControlPassword = "$%##@austh@#$09au$gks'<>";
	
	public static boolean setupAccountManager(String root_password) throws ArgumentException, DataAccessException, FactoryException
	{
		// 2016/07/27 - Bug: Because the factory starts automatically, it will throw an error
		// it also means it has to be reset again before running setup or it will fail again because all the data was just nuked by reloading the database schema
		//
		Factories.recycleFactories();
		
		logger.info("Begin Setup Account Manager");
		
		Factories.clearCaches();
		
		//String root_hash = SecurityUtil.getSaltedDigest(root_password);
		
		logger.info("Create default organizations");
		
		setupOrganizations();

		logger.info("Create root account");
		
		AccountFactory aFact = Factories.getFactory(FactoryEnumType.ACCOUNT);
		UserFactory uFact = Factories.getFactory(FactoryEnumType.USER);
		AccountType root_account = aFact.getAccountByName("Root", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", Factories.getSystemOrganization().getId()));
		if(root_account == null){
			root_account = aFact.newAccount(null,"Root", AccountEnumType.SYSTEM, AccountStatusEnumType.RESTRICTED, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", Factories.getSystemOrganization().getId()).getId(),Factories.getSystemOrganization().getId());
			if (!aFact.add(root_account)) throw new FactoryException("Unable to add root account");
	
			UserType root_user = uFact.newUserForAccount("Root", root_account, UserEnumType.SYSTEM, UserStatusEnumType.RESTRICTED);
			if (!uFact.add(root_user)) throw new FactoryException("Unable to add root user");
			root_user = uFact.getByName("Root", root_account.getOrganizationId());
			if (root_user == null) throw new FactoryException("Failed to retrieve to add root user");
			/// 2015/06/23 - New Credential System
			/// I intentionally left the credential operation decoupled from object creation
			///
			CredentialType cred = CredentialService.newHashedPasswordCredential(root_user, root_user, root_password, true,false);
			if(cred == null) throw new FactoryException("Failed to persist root credential");
		}
		setupOrganization(Factories.getDevelopmentOrganization(), root_password);
		setupOrganization(Factories.getSystemOrganization(), root_password);
		setupOrganization(Factories.getPublicOrganization(), root_password);
		
		logger.info("End Setup Account Manager");
		
		return true;
	}

	public static boolean setupOrganization(OrganizationType organization, String admin_password) throws ArgumentException, DataAccessException, FactoryException
	{

		if(organization == null){
			throw new ArgumentException("Organization is null");
		}

		
		AccountFactory aFact = Factories.getFactory(FactoryEnumType.ACCOUNT);
		UserFactory uFact = Factories.getFactory(FactoryEnumType.USER);
		PermissionFactory pFact = Factories.getFactory(FactoryEnumType.PERMISSION);

		logger.info("Configure " + organization.getName() + " organization");
		DirectoryGroupType agroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", organization.getId());
		if(agroup == null){
			throw new FactoryException("Root directory is null");
		}
		// Create administration user
		//
		AccountType admin_account = aFact.newAccount(null,"Admin", AccountEnumType.SYSTEM, AccountStatusEnumType.RESTRICTED, agroup.getId(),organization.getId());
		if (!aFact.add(admin_account)) throw new FactoryException("Unable to add admin account");
		admin_account = aFact.getAccountByName("Admin", agroup);

		UserType admin_user = uFact.newUserForAccount("Admin", admin_account, UserEnumType.SYSTEM, UserStatusEnumType.RESTRICTED);
		if (!uFact.add(admin_user)) throw new FactoryException("Unable to add admin user");
		admin_user = uFact.getByName("Admin", organization.getId());
		/// 2015/06/23 - New Credential System
		/// I intentionally left the credential operation decoupled from object creation
		///
		CredentialType cred = CredentialService.newHashedPasswordCredential(admin_user, admin_user, admin_password, true, false);
		if(cred == null) throw new FactoryException("Failed to persist credential");

		// Create the document control user
		//
		UserType dc_user = uFact.newUserForAccount("Document Control", admin_account, UserEnumType.SYSTEM, UserStatusEnumType.RESTRICTED);
		if (uFact.add(dc_user) == false) return false;
		dc_user = uFact.getByName("Document Control", organization.getId());

		/// 2015/06/23 - New Credential System
		/// I intentionally left the credential operation decoupled from object creation
		///
		cred = CredentialService.newHashedPasswordCredential(dc_user, dc_user, UUID.randomUUID().toString(), true, false);
		if(cred == null) throw new FactoryException("Failed to persist credential");
		
		if(dc_user.getId() <= 0 || admin_user.getId() <= 0){
			logger.error("Cache error.  A temporary object was returned when a persisted object was expected");
			return false;
		}

		// Create default permission sets
		//
		for (int i = 0; i < default_account_permissions.length; i++)
		{
			pFact.add(
				pFact.newPermission(default_account_permissions[i], PermissionEnumType.ACCOUNT, organization.getId())
			);
		}

		for (int i = 0; i < default_object_permissions.length; i++)
		{
			pFact.add(
					pFact.newPermission(default_object_permissions[i], PermissionEnumType.OBJECT, organization.getId())
			);
		}
		for (int i = 0; i < default_application_permissions.length; i++)
		{
			pFact.add(
					pFact.newPermission(default_application_permissions[i], PermissionEnumType.APPLICATION, organization.getId())
			);
		}
		/// 2016/05/18 - Moved default permission construction into the Participation Factories
		/// TODO: Account is left out at the moment
		///
		createPermissionsForAuthorizationFactories(organization.getId());

		// Request the person roles to create them
		//
		PersonRoleType person_admin_role = RoleService.getAccountAdministratorPersonRole(admin_user);
		PersonRoleType per_data_admin_role = RoleService.getDataAdministratorPersonRole(admin_user);
		PersonRoleType per_obj_admin_role = RoleService.getObjectAdministratorPersonRole(admin_user);
		PersonRoleType per_sys_admin_role = RoleService.getSystemAdministratorPersonRole(admin_user);
		PersonRoleType per_users_role = RoleService.getAccountUsersPersonRole(admin_user);
		
		// Add admin account and root account to Administrators and Users account roles
		//
		AccountType root_account = aFact.getAccountByName("Root",((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", Factories.getSystemOrganization().getId()));
		AccountRoleType account_admin_role = RoleService.getAccountAdministratorAccountRole(admin_user);
		AccountRoleType data_admin_role = RoleService.getDataAdministratorAccountRole(admin_user);
		AccountRoleType obj_admin_role = RoleService.getObjectAdministratorAccountRole(admin_user);
		AccountRoleType sys_admin_role = RoleService.getSystemAdministratorAccountRole(admin_user);
		AccountRoleType users_role = RoleService.getAccountUsersAccountRole(admin_user);

		RoleService.addAccountToRole(root_account, obj_admin_role);
		RoleService.addAccountToRole(root_account,account_admin_role);
		RoleService.addAccountToRole(root_account,data_admin_role);
		RoleService.addAccountToRole(root_account,sys_admin_role);
		RoleService.addAccountToRole(admin_account, obj_admin_role);
		RoleService.addAccountToRole(admin_account, account_admin_role);
		RoleService.addAccountToRole(admin_account, data_admin_role);
		RoleService.addAccountToRole(admin_account,sys_admin_role);
		
		// Add admin user and root user to Administrators and Users user roles
		//
		UserType root_user = uFact.getByName("Root",Factories.getSystemOrganization().getId());
		UserRoleType user_admin_role = RoleService.getAccountAdministratorUserRole(admin_user);
		UserRoleType user_data_admin_role = RoleService.getDataAdministratorUserRole(admin_user);
		UserRoleType user_obj_admin_role = RoleService.getObjectAdministratorUserRole(admin_user);
		UserRoleType user_sys_admin_role = RoleService.getSystemAdministratorUserRole(admin_user);
		UserRoleType user_users_role = RoleService.getAccountUsersRole(admin_user);
		
		RoleService.getAccountUsersReaderAccountRole(admin_user);
		RoleService.getPermissionReaderAccountRole(admin_user);
		RoleService.getRoleReaderAccountRole(admin_user);
		RoleService.getDataReaderAccountRole(admin_user);
		RoleService.getGroupReaderAccountRole(admin_user);
		RoleService.getObjectReaderAccountRole(admin_user);
		RoleService.getApiUserUserRole(admin_user);
		RoleService.getAccountUsersReaderUserRole(admin_user);
		RoleService.getRoleReaderUserRole(admin_user);
		RoleService.getPermissionReaderUserRole(admin_user);
		RoleService.getDataReaderUserRole(admin_user);
		RoleService.getGroupReaderUserRole(admin_user);
		RoleService.getObjectReaderUserRole(admin_user);

		/// 2014/03/03 - Document control shouldn't be an admin, it should be delegated as needed
		///
		/// RoleService.addUserToRole(dc_user, user_data_admin_role);
		/// RoleService.addUserToRole(dc_user, user_obj_admin_role);
		RoleService.addUserToRole(root_user,user_admin_role);
		RoleService.addUserToRole(root_user,user_data_admin_role);
		RoleService.addUserToRole(root_user,user_obj_admin_role);
		RoleService.addUserToRole(root_user,user_sys_admin_role);
		RoleService.addUserToRole(admin_user, user_admin_role);
		RoleService.addUserToRole(admin_user, user_data_admin_role);
		RoleService.addUserToRole(admin_user, user_obj_admin_role);
		RoleService.addUserToRole(admin_user,user_sys_admin_role);
		
		RoleFactory rFact = Factories.getFactory(FactoryEnumType.ROLE);
		rFact.addDefaultRoles(organization.getId());
		
		EffectiveAuthorizationService.rebuildPendingRoleCache();
		
		KeyService.newOrganizationAsymmetricKey(organization.getId(), true);
		KeyService.newOrganizationSymmetricKey(organization.getId(), true);
		
		UserType apiUser = ApiConnectionConfigurationService.getApiUser(organization.getId());
		
		return true;
	}
	
	public static void createPermissionsForAuthorizationFactories(long organizationId){
		Map<FactoryEnumType, FactoryEnumType> factories = AuthorizationService.getAuthorizationFactories();
		if(factories.keySet().size() == 0){
			logger.error("No factories registered with authorization service");
		}
		PermissionFactory pfact = Factories.getFactory(FactoryEnumType.PERMISSION);
		for(FactoryEnumType factType : factories.keySet()){
			IParticipationFactory fact = Factories.getFactory(factories.get(factType));
			String[] permissionNames = fact.getDefaultPermissions();
			logger.info("Processing " + permissionNames.length + " for " + fact.getFactoryType().toString() + " Factory");
			for (int i = 0; i < permissionNames.length; i++)
			{
				try{
					pfact.add(
							pfact.newPermission(permissionNames[i], fact.getDefaultPermissionType(), organizationId)
				);
				logger.info("Added permission " + permissionNames[i] + " to organization #" + organizationId);
				}
				catch(FactoryException | ArgumentException e){
					logger.error(e.getMessage());
				}
			}
		}
	}

	private static void setupOrganizations()
	{

		/// requesting org factory forces population of default orgs
		///
		OrganizationFactory org_fact = Factories.getFactory(FactoryEnumType.ORGANIZATION);
	}
}
