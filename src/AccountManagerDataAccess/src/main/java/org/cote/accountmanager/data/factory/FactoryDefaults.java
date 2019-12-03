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
package org.cote.accountmanager.data.factory;

import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.security.ApiConnectionConfigurationService;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
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
	
	public static final String ROOT_USER_NAME = "Root";
	public static final String ADMIN_USER_NAME = "Admin";
	public static final String FEEDBACK_USER_NAME = "FeedbackUser";
	public static final String VAULT_USER_NAME = "VaultUser";
	public static final String DOCUMENT_CONTROL_USER_NAME = "Document Control";
	
	public static final Logger logger = LogManager.getLogger(FactoryDefaults.class);
	
	
	
	protected static final String[] DEFAULT_APPLICATION_PERMISSIONS = new String[]{
		"ApplicationView",
		"ApplicationEdit",
		"ApplicationDelete",
		"ApplicationCreate",
		"ApplicationExecute"
	};
	protected static final String[] DEFAULT_OBJECT_PERMISSIONS = new String[]{
		"ObjectView",
		"ObjectEdit",
		"ObjectDelete",
		"ObjectCreate",
		"ObjectExecute"
	};
	protected static final String[] DEFAULT_ACCOUNT_PERMISSIONS = new String[]{
		"AccountView",
		"AccountEdit",
		"AccountDelete",
		"AccountCreate"
	};
	
	private FactoryDefaults(){
		
	}
	
	/// Changing this to be a random string
	public static boolean setupAccountManager(String rootPassword) throws ArgumentException, DataAccessException, FactoryException
	{
		// 2016/07/27 - Bug: Because the factory starts automatically, it will throw an error
		// it also means it has to be reset again before running setup or it will fail again because all the data was just nuked by reloading the database schema
		//
		Factories.recycleFactories();
		
		logger.info("Begin Setup Account Manager");
		
		Factories.clearCaches();

		
		logger.info("Create default organizations");
		
		setupOrganizations();

		logger.info("Create root account");
		
		AccountFactory aFact = Factories.getFactory(FactoryEnumType.ACCOUNT);
		UserFactory uFact = Factories.getFactory(FactoryEnumType.USER);
		AccountType rootAccount = aFact.getAccountByName(ROOT_USER_NAME, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", Factories.getSystemOrganization().getId()));
		if(rootAccount == null){
			rootAccount = aFact.newAccount(null,ROOT_USER_NAME, AccountEnumType.SYSTEM, AccountStatusEnumType.RESTRICTED, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", Factories.getSystemOrganization().getId()).getId(),Factories.getSystemOrganization().getId());
			if (!aFact.add(rootAccount)) throw new FactoryException("Unable to add root account");
			rootAccount = aFact.getAccountByName(ROOT_USER_NAME, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", Factories.getSystemOrganization().getId()));
			
			UserType rootUser = uFact.newUserForAccount(ROOT_USER_NAME, rootAccount, UserEnumType.SYSTEM, UserStatusEnumType.RESTRICTED);
			if (!uFact.add(rootUser)) throw new FactoryException("Unable to add root user");
			rootUser = uFact.getByName(ROOT_USER_NAME, rootAccount.getOrganizationId());
			if (rootUser == null) throw new FactoryException("Failed to retrieve to add root user");
			
			rootAccount.setOwnerId(rootUser.getId());
			if(!aFact.update(rootAccount)) throw new FactoryException("Failed to update root account");
			/// 2015/06/23 - New Credential System
			/// I intentionally left the credential operation decoupled from object creation
			///
			CredentialType cred = CredentialService.newHashedPasswordCredential(rootUser, rootUser, rootPassword, true);
			if(cred == null) throw new FactoryException("Failed to persist root credential");
		}
		setupOrganization(Factories.getDevelopmentOrganization(), rootPassword);
		setupOrganization(Factories.getSystemOrganization(), rootPassword);
		setupOrganization(Factories.getPublicOrganization(), rootPassword);
		
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
		AccountType adminAccount = aFact.newAccount(null,ADMIN_USER_NAME, AccountEnumType.SYSTEM, AccountStatusEnumType.RESTRICTED, agroup.getId(),organization.getId());
		if (!aFact.add(adminAccount)) throw new FactoryException("Unable to add admin account");
		adminAccount = aFact.getAccountByName(ADMIN_USER_NAME, agroup);

		UserType adminUser = uFact.newUserForAccount(ADMIN_USER_NAME, adminAccount, UserEnumType.SYSTEM, UserStatusEnumType.RESTRICTED);
		if (!uFact.add(adminUser)) throw new FactoryException("Unable to add admin user");
		adminUser = uFact.getByName(ADMIN_USER_NAME, organization.getId());
		
		adminAccount.setOwnerId(adminUser.getId());
		if(!aFact.update(adminAccount)) throw new FactoryException("Failed to update admin account");
		
		/// 2015/06/23 - New Credential System
		/// I intentionally left the credential operation decoupled from object creation
		///
		CredentialType cred = CredentialService.newHashedPasswordCredential(adminUser, adminUser, admin_password, true);
		if(cred == null) throw new FactoryException("Failed to persist credential");

		// Create the document control user
		//
		UserType dcUser = uFact.newUserForAccount(DOCUMENT_CONTROL_USER_NAME, adminAccount, UserEnumType.SYSTEM, UserStatusEnumType.RESTRICTED);
		if (!uFact.add(dcUser)) return false;
		dcUser = uFact.getByName(DOCUMENT_CONTROL_USER_NAME, organization.getId());

		/// 2015/06/23 - New Credential System
		/// I intentionally left the credential operation decoupled from object creation
		///
		cred = CredentialService.newHashedPasswordCredential(dcUser, dcUser, UUID.randomUUID().toString(), true);
		if(cred == null) throw new FactoryException("Failed to persist credential");
		
		if(dcUser.getId() <= 0 || adminUser.getId() <= 0){
			logger.error("Cache error.  A temporary object was returned when a persisted object was expected");
			return false;
		}
		

		
		/// Feedback user
		///
		UserType fbUser = uFact.newUserForAccount(FEEDBACK_USER_NAME, adminAccount, UserEnumType.SYSTEM, UserStatusEnumType.RESTRICTED);
		if (!uFact.add(fbUser)) return false;
		fbUser = uFact.getByName(FEEDBACK_USER_NAME, organization.getId());
		cred = CredentialService.newHashedPasswordCredential(fbUser, fbUser, UUID.randomUUID().toString(), true);
		if(cred == null) throw new FactoryException("Failed to persist credential");
		if(fbUser.getId() <= 0 || adminUser.getId() <= 0){
			logger.error("Cache error.  A temporary object was returned when a persisted object was expected");
			return false;
		}
		
		/// Vault user
		///
		UserType vlUser = uFact.newUserForAccount(VAULT_USER_NAME, adminAccount, UserEnumType.SYSTEM, UserStatusEnumType.RESTRICTED);
		if (!uFact.add(vlUser)) return false;
		vlUser = uFact.getByName(VAULT_USER_NAME, organization.getId());
		cred = CredentialService.newHashedPasswordCredential(vlUser, vlUser, UUID.randomUUID().toString(), true);
		if(cred == null) throw new FactoryException("Failed to persist credential");
		if(vlUser.getId() <= 0 || adminUser.getId() <= 0){
			logger.error("Cache error.  A temporary object was returned when a persisted object was expected");
			return false;
		}
		
		// Create default permission sets
		//
		for (int i = 0; i < DEFAULT_ACCOUNT_PERMISSIONS.length; i++)
		{
			pFact.add(
				pFact.newPermission(adminUser, DEFAULT_ACCOUNT_PERMISSIONS[i], PermissionEnumType.ACCOUNT, null, organization.getId())
			);
		}

		for (int i = 0; i < DEFAULT_OBJECT_PERMISSIONS.length; i++)
		{
			pFact.add(
					pFact.newPermission(adminUser, DEFAULT_OBJECT_PERMISSIONS[i], PermissionEnumType.OBJECT, null, organization.getId())
			);
		}
		for (int i = 0; i < DEFAULT_APPLICATION_PERMISSIONS.length; i++)
		{
			pFact.add(
					pFact.newPermission(adminUser, DEFAULT_APPLICATION_PERMISSIONS[i], PermissionEnumType.APPLICATION, null, organization.getId())
			);
		}
		/// 2016/05/18 - Moved default permission construction into the Participation Factories
		/// Account is left out at the moment
		///
		createPermissionsForAuthorizationFactories(adminUser, organization.getId());

		// Request the person roles to create them
		//
		PersonRoleType personAdminRole = RoleService.getAccountAdministratorPersonRole(adminUser);
		PersonRoleType personDataAdminRole = RoleService.getDataAdministratorPersonRole(adminUser);
		PersonRoleType personObjectAdminRole = RoleService.getObjectAdministratorPersonRole(adminUser);
		PersonRoleType personSystemAdminRole = RoleService.getSystemAdministratorPersonRole(adminUser);
		PersonRoleType personUserAdminRole = RoleService.getAccountUsersPersonRole(adminUser);
		if(personAdminRole == null || personDataAdminRole == null || personObjectAdminRole == null || personSystemAdminRole == null || personUserAdminRole == null){
			logger.error("Failed to retrieve one or more person roles");
		}
		// Add admin account and root account to Administrators and Users account roles
		//
		AccountType rootAccount = aFact.getAccountByName(ROOT_USER_NAME,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", Factories.getSystemOrganization().getId()));
		AccountRoleType accountAdminRole = RoleService.getAccountAdministratorAccountRole(adminUser);
		AccountRoleType dataAdminRole = RoleService.getDataAdministratorAccountRole(adminUser);
		AccountRoleType objectAdminRole = RoleService.getObjectAdministratorAccountRole(adminUser);
		AccountRoleType systemAdminRole = RoleService.getSystemAdministratorAccountRole(adminUser);
		AccountRoleType usersRole = RoleService.getAccountUsersAccountRole(adminUser);
		if(usersRole == null){
			logger.error("User role is null");
		}
		RoleService.addAccountToRole(rootAccount, objectAdminRole);
		RoleService.addAccountToRole(rootAccount,accountAdminRole);
		RoleService.addAccountToRole(rootAccount,dataAdminRole);
		RoleService.addAccountToRole(rootAccount,systemAdminRole);
		RoleService.addAccountToRole(adminAccount, objectAdminRole);
		RoleService.addAccountToRole(adminAccount, accountAdminRole);
		RoleService.addAccountToRole(adminAccount, dataAdminRole);
		RoleService.addAccountToRole(adminAccount,systemAdminRole);
		
		// Add admin user and root user to Administrators and Users user roles
		//
		UserType rootUser = uFact.getByName(ROOT_USER_NAME,Factories.getSystemOrganization().getId());
		UserRoleType userAdminRole = RoleService.getAccountAdministratorUserRole(adminUser);
		UserRoleType userDataAdminRole = RoleService.getDataAdministratorUserRole(adminUser);
		UserRoleType userObjectAdminRole = RoleService.getObjectAdministratorUserRole(adminUser);
		UserRoleType userSystemAdminRole = RoleService.getSystemAdministratorUserRole(adminUser);
		UserRoleType usersUsersRole = RoleService.getAccountUsersRole(adminUser);
		if(usersUsersRole == null){
			logger.error("Failed to retrieve users users role");
		}

		RoleService.getAccountUsersReaderAccountRole(adminUser);
		RoleService.getPermissionReaderAccountRole(adminUser);
		RoleService.getPermissionAdministratorAccountRole(adminUser);
		RoleService.getRoleReaderAccountRole(adminUser);
		RoleService.getDataReaderAccountRole(adminUser);
		RoleService.getGroupReaderAccountRole(adminUser);
		RoleService.getObjectReaderAccountRole(adminUser);
		RoleService.getApiUserUserRole(adminUser);
		UserRoleType usersUsersReadersRole = RoleService.getAccountUsersReaderUserRole(adminUser);
		RoleService.getArticleAuthorUserRole(adminUser);
		RoleService.getRoleReaderUserRole(adminUser);
		RoleService.getPermissionReaderUserRole(adminUser);
		RoleService.getPermissionAdministratorUserRole(adminUser);
		RoleService.getDataReaderUserRole(adminUser);
		RoleService.getGroupReaderUserRole(adminUser);
		RoleService.getObjectReaderUserRole(adminUser);

		RoleService.addUserToRole(rootUser,userAdminRole);
		RoleService.addUserToRole(rootUser,userDataAdminRole);
		RoleService.addUserToRole(rootUser,userObjectAdminRole);
		RoleService.addUserToRole(rootUser,userSystemAdminRole);
		RoleService.addUserToRole(adminUser, userAdminRole);
		RoleService.addUserToRole(adminUser, userDataAdminRole);
		RoleService.addUserToRole(adminUser, userObjectAdminRole);
		RoleService.addUserToRole(adminUser,userSystemAdminRole);
		
		RoleFactory rFact = Factories.getFactory(FactoryEnumType.ROLE);
		rFact.addDefaultRoles(organization.getId());
		
		DirectoryGroupType rDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getRootDirectory(organization.getId());
		DirectoryGroupType hDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getHomeDirectory(organization.getId());

		/// Update home and root directory ownership to the Admin user
		///
		rDir.setOwnerId(adminUser.getId());
		hDir.setOwnerId(adminUser.getId());
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).update(rDir);
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).update(hDir);
		
		DirectoryGroupType pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(adminUser, "Persons", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getRootDirectory(organization.getId()), organization.getId());
		DirectoryGroupType cDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(adminUser, "Contacts", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getRootDirectory(organization.getId()), organization.getId());

		
		
		AuthorizationService.authorizeType(adminUser, usersUsersReadersRole, pDir, true, false, false, false);
		AuthorizationService.authorizeType(adminUser, usersUsersReadersRole, cDir, true, false, false, false);
		AuthorizationService.authorizeType(adminUser, userAdminRole, pDir, true, true, false, true);
		AuthorizationService.authorizeType(adminUser, userAdminRole, cDir, true, true, false, true);
		
		EffectiveAuthorizationService.rebuildPendingRoleCache();
		
		KeyService.newOrganizationAsymmetricKey(organization.getId(), true);
		KeyService.newOrganizationSymmetricKey(organization.getId(), true);
		
		UserType apiUser = ApiConnectionConfigurationService.getApiUser(organization.getId());
		if(apiUser == null){
			logger.error("Failed to retrieve API user");
		}
		return true;
	}
	
	public static void createPermissionsForAuthorizationFactories(UserType owner, long organizationId) throws FactoryException{
		Map<FactoryEnumType, FactoryEnumType> factories = AuthorizationService.getAuthorizationFactories();
		if(factories.keySet().isEmpty()){
			logger.error("No factories registered with authorization service");
		}
		PermissionFactory pfact = Factories.getFactory(FactoryEnumType.PERMISSION);
		for(FactoryEnumType factType : factories.keySet()){
			IParticipationFactory fact = Factories.getFactory(factories.get(factType));
			String[] permissionNames = fact.getDefaultPermissions();
			logger.debug("Processing " + permissionNames.length + " for " + fact.getFactoryType().toString() + " Factory");
			for (int i = 0; i < permissionNames.length; i++)
			{
				try{
					pfact.add(
						pfact.newPermission(owner, permissionNames[i], fact.getDefaultPermissionType(), null, organizationId)
				);
				logger.debug("Added permission " + permissionNames[i] + " to organization #" + organizationId);
				}
				catch(FactoryException | ArgumentException e){
					logger.error(e.getMessage());
				}
			}
		}
	}

	private static void setupOrganizations() throws FactoryException
	{

		/// requesting org factory forces population of default orgs
		///
		OrganizationFactory orgFact = Factories.getFactory(FactoryEnumType.ORGANIZATION);
		if(orgFact == null){
			logger.error("Failed to retrieve organization factory");
		}
	}
}
