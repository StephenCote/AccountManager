package org.cote.accountmanager.data.services;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;

public class RoleService {
	public static final Logger logger = Logger.getLogger(RoleService.class.getName());

	
	public static boolean getIsGroupInRole(BaseRoleType role, UserGroupType group) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error("Role is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsGroupInRole(role, group, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsGroupInRole(BaseRoleType role, UserGroupType group, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error("Role is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return Factories.getRoleParticipationFactory().getIsGroupInRole(role, group,permission,affect_type);
	}
	public static boolean addGroupToRole(UserGroupType group, UserRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addGroupToRole(group, role, null, AffectEnumType.UNKNOWN);
	}

	public static boolean addGroupToRole(UserGroupType account, UserRoleType role, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
	{
		/// accommodate bulk inserts with a negative id - skip the check for the getGroupInRole, which will return true for bulk jobs
		///
		if (role.getId() < 0L || getIsGroupInRole(role, account) == false)
		{
			GroupParticipantType ap = Factories.getRoleParticipationFactory().newGroupRoleParticipation(role, account);
			if (Factories.getRoleParticipationFactory().addParticipant(ap))
			{
				EffectiveAuthorizationService.pendGroupUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeGroupFromRole(UserRoleType role, UserGroupType group) throws FactoryException, ArgumentException
	{
		if (Factories.getRoleParticipationFactory().deleteGroupRoleParticipants(role, group))
		{
			EffectiveAuthorizationService.pendGroupUpdate(group);
			return true;
		}
		return false;
	}

	public static boolean getIsUserInEffectiveRole(BaseRoleType role, UserType user) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error("Role is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsUserInEffectiveRole(role, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsUserInEffectiveRole(BaseRoleType role, UserType user, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error("Role is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return EffectiveAuthorizationService.getIsUserInEffectiveRole(role, user, permission, affect_type);
	}
	
	public static boolean getIsUserInRole(BaseRoleType role, UserType user) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error("Role is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsUserInRole(role, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsUserInRole(BaseRoleType role, UserType user, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error("Role is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return EffectiveAuthorizationService.getIsUserInEffectiveRole(role, user,permission,affect_type);
		//return Factories.getRoleParticipationFactory().getIsUserInRole(role, user,permission,affect_type);
	}
	public static boolean addUserToRole(UserType user, UserRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addUserToRole(user, role, null, AffectEnumType.UNKNOWN);
	}

	public static boolean addUserToRole(UserType account, UserRoleType role, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
	{
		/// accommodate bulk inserts with a negative id - skip the check for the getUserInRole, which will return true for bulk jobs
		///
		if (role.getId() < 0L || getIsUserInRole(role, account) == false)
		{
			UserParticipantType ap = Factories.getRoleParticipationFactory().newUserRoleParticipation(role, account);
			if (Factories.getRoleParticipationFactory().addParticipant(ap))
			{
				EffectiveAuthorizationService.pendUserUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeUserFromRole(UserRoleType role, UserType account) throws FactoryException, ArgumentException
	{
		if (Factories.getRoleParticipationFactory().deleteUserRoleParticipants(role, account))
		{
			EffectiveAuthorizationService.pendUserUpdate(account);
			return true;
		}
		return false;
	}
	/*
	public static boolean addUserRoleToGroup(UserRoleType role, BaseGroupType group, BasePermissionType permission, AffectEnumType affectType){
		BaseParticipantType bp = Factories.getGroupParticipationFactory().newRoleGroupParticipation(group, role, permission, affectType);
		Factories.getGroupParticipationFactory().addParticipant(bp);
	}
	*/
	
	public static PersonRoleType getCreatePersonRole(UserType role_owner, PersonType person, String role_name) throws DataAccessException, FactoryException, ArgumentException
	{
		PersonRoleType parent_role = Factories.getRoleFactory().getPersonRole(person);
		return getCreatePersonRole(role_owner, role_name, parent_role);
	}
	public static PersonRoleType getCreatePersonRole(UserType role_owner, String role_name, PersonRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return Factories.getRoleFactory().getCreatePersonRole(role_owner, role_name, Parent);
	}

	public static PersonRoleType getPersonRole(String role_name, PersonRoleType Parent, OrganizationType organization) throws FactoryException, ArgumentException{
		return Factories.getRoleFactory().getPersonRoleByName(role_name, Parent, organization);
	}
	public static boolean getIsPersonInRole(BaseRoleType role, PersonType person) throws ArgumentException, FactoryException{
		return getIsPersonInRole(role, person, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsPersonInRole(BaseRoleType role, PersonType person, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return Factories.getRoleParticipationFactory().getIsPersonInRole(role, person,permission,affect_type);
	}
	public static boolean addPersonToRole(PersonType person, PersonRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addPersonToRole(person, role, null, AffectEnumType.UNKNOWN);
	}
	public static UserRoleType getCreatePersonRole(UserType role_owner, String role_name) throws DataAccessException, FactoryException, ArgumentException
	{
		UserRoleType parent_role = Factories.getRoleFactory().getUserRole(role_owner);
		return getCreateUserRole(role_owner, role_name, parent_role);
	}
	
	public static AccountRoleType getCreateAccountRole(UserType role_owner, AccountType account, String role_name) throws DataAccessException, FactoryException, ArgumentException
	{
		AccountRoleType parent_role = Factories.getRoleFactory().getAccountRole(account);
		return getCreateAccountRole(role_owner, role_name, parent_role);
	}
	public static AccountRoleType getCreateAccountRole(UserType role_owner, String role_name, AccountRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return Factories.getRoleFactory().getCreateAccountRole(role_owner, role_name, Parent);
	}

	public static AccountRoleType getAccountRole(String role_name, AccountRoleType Parent, OrganizationType organization) throws FactoryException, ArgumentException{
		return Factories.getRoleFactory().getAccountRoleByName(role_name, Parent, organization);
	}
	public static boolean getIsAccountInRole(BaseRoleType role, AccountType account) throws ArgumentException, FactoryException{
		return getIsAccountInRole(role, account, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsAccountInRole(BaseRoleType role, AccountType account, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return Factories.getRoleParticipationFactory().getIsAccountInRole(role, account,permission,affect_type);
	}
	public static boolean addAccountToRole(AccountType account, AccountRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addAccountToRole(account, role, null, AffectEnumType.UNKNOWN);
	}
	public static UserRoleType getCreateAccountRole(UserType role_owner, String role_name) throws DataAccessException, FactoryException, ArgumentException
	{
		UserRoleType parent_role = Factories.getRoleFactory().getUserRole(role_owner);
		return getCreateUserRole(role_owner, role_name, parent_role);
	}
	public static UserRoleType getCreateUserRole(UserType role_owner, String role_name, UserRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return Factories.getRoleFactory().getCreateUserRole(role_owner, role_name, Parent);
	}

	public static UserRoleType getUserRole(String role_name, UserRoleType Parent, OrganizationType organization) throws FactoryException, ArgumentException{
		return Factories.getRoleFactory().getUserRoleByName(role_name, Parent, organization);
	}
	/// <summary>
	/// Adds an account participation to a role participation, with affect 
	/// </summary>
	/// <param name="role_admin"></param>
	/// <param name="account"></param>
	/// <param name="role"></param>
	/// <param name="permission"></param>
	/// <param name="affect_type"></param>
	/// <returns></returns>
	public static boolean addAccountToRole(AccountType account, AccountRoleType role, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
	{
		if (getIsAccountInRole(role, account) == false)
		{
			AccountParticipantType ap = Factories.getRoleParticipationFactory().newAccountRoleParticipation(role, account);
			if (Factories.getRoleParticipationFactory().addParticipant(ap))
			{
				EffectiveAuthorizationService.pendAccountUpdate(account);
				return true;
			}
			
		}
		return false;
	}
	public static boolean removeAccountFromRole(AccountRoleType role, AccountType account) throws FactoryException, ArgumentException
	{
		if (Factories.getRoleParticipationFactory().deleteAccountRoleParticipants(role, account))
		{
			EffectiveAuthorizationService.pendAccountUpdate(account);
			return true;
		}
		return false;
	}
	
	public static boolean addPersonToRole(PersonType person, PersonRoleType role, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
	{
		if (getIsPersonInRole(role, person) == false)
		{
			PersonParticipantType ap = Factories.getRoleParticipationFactory().newPersonRoleParticipation(role, person);
			if (Factories.getRoleParticipationFactory().addParticipant(ap))
			{
				EffectiveAuthorizationService.pendPersonUpdate(person);
				return true;
			}
		}
		return false;
	}
	public static boolean removePersonFromRole(PersonRoleType role, PersonType person) throws FactoryException, ArgumentException
	{
		if (Factories.getRoleParticipationFactory().deletePersonRoleParticipants(role, person))
		{
			EffectiveAuthorizationService.pendPersonUpdate(person);
			return true;
		}
		return false;
	}
	
	
		/// Access roles for viewing objects
		///
		public static UserRoleType getAccountUsersReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "AccountUsersReaders", null);
		}
		public static UserRoleType getAccountUsersReaderUserRole(OrganizationType org) throws  FactoryException, ArgumentException
		{
			return getUserRole("AccountUsersReaders", null,org);
		}
		public static UserRoleType getRoleReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "RoleReaders", null);
		}
		public static UserRoleType getRoleReaderUserRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getUserRole("RoleReaders", null,org);
		}
		public static UserRoleType getGroupReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "GroupReaders", null);
		}
		public static UserRoleType getGroupReaderUserRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getUserRole("GroupReaders", null,org);
		}
		public static UserRoleType getDataReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "DataReaders", null);
		}
		public static UserRoleType getDataReaderUserRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getUserRole("DataReaders", null,org);
		}
		public static UserRoleType getObjectReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "ObjectReaders", null);
		}
		public static UserRoleType getObjectReaderUserRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getUserRole("ObjectReaders", null,org);
		}
		
		public static AccountRoleType getAccountUsersReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "AccountUsersReaders", null);
		}
		public static AccountRoleType getAccountUsersReaderAccountRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getAccountRole("AccountUsersReaders", null,org);
		}
		public static AccountRoleType getRoleReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "RoleReaders", null);
		}
		public static AccountRoleType getRoleReaderAccountRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getAccountRole("RoleReaders", null,org);
		}
		public static AccountRoleType getGroupReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "GroupReaders", null);
		}
		public static AccountRoleType getGroupReaderAccountRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getAccountRole("GroupReaders", null,org);
		}
		public static AccountRoleType getDataReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "DataReaders", null);
		}
		public static AccountRoleType getDataReaderAccountRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getAccountRole("DataReaders", null,org);
		}
		public static AccountRoleType getObjectReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "ObjectReaders", null);
		}
		public static AccountRoleType getObjectReaderAccountRole(OrganizationType org) throws FactoryException, ArgumentException
		{
			return getAccountRole("ObjectReaders", null,org);
		}
	
		/// Users
		///
		public static UserRoleType getAccountUsersRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "AccountUsers", null);
		}
		public static UserRoleType getAccountUsersRole(OrganizationType org) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole("AccountUsers", null,org);
		}
		public static UserRoleType getAccountDevelopersUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "AccountDevelopers", null);
		}
		public static UserRoleType getAccountDevelopersUserRole(OrganizationType org) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole("AccountDevelopers", null,org);
		}
		public static UserRoleType getSystemAdministratorUserRole(OrganizationType org) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole("SystemAdministrators",null,org);
		}
		public static UserRoleType getSystemAdministratorUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "SystemAdministrators",null);
		}
		public static UserRoleType getDataAdministratorUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "DataAdministrators",null);
		}
		public static UserRoleType getDataAdministratorUserRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getUserRole("DataAdministrators", null, organization);
		}
		public static UserRoleType getObjectAdministratorUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "ObjectAdministrators",null);
		}
		public static UserRoleType getObjectAdministratorUserRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getUserRole("ObjectAdministrators", null, organization);
		}
		public static UserRoleType getAccountAdministratorUserRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getUserRole("AccountAdministrators", null,organization);
		}
		public static UserRoleType getAccountAdministratorUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "AccountAdministrators",null);
		}
		

		/// Account
		///
		public static AccountRoleType getAccountUsersAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "AccountUsers", null);
		}
		public static AccountRoleType getAccountDevelopersAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "AccountDevelopers", null);
		}
		public static AccountRoleType getSystemAdministratorAccountRole(OrganizationType org) throws DataAccessException, FactoryException, ArgumentException
		{
			return getAccountRole("SystemAdministrators",null, org);
		}
		public static AccountRoleType getSystemAdministratorAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "SystemAdministrators",null);
		}
		public static AccountRoleType getDataAdministratorAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "DataAdministrators",null);
		}
		public static AccountRoleType getDataAdministratorAccountRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getAccountRole("DataAdministrators", null, organization);
		}

		public static AccountRoleType getObjectAdministratorAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "ObjectAdministrators",null);
		}
		public static AccountRoleType getObjectAdministratorAccountRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getAccountRole("ObjectAdministrators", null, organization);
		}
		
		public static AccountRoleType getAccountAdministratorAccountRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getAccountRole("AccountAdministrators", null,organization);
		}
		public static AccountRoleType getAccountAdministratorAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "AccountAdministrators",null);
		}
		
		/// Person
		///
		public static PersonRoleType getAccountUsersPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "AccountUsers", null);
		}
		public static PersonRoleType getAccountDevelopersPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "AccountDevelopers", null);
		}
		public static PersonRoleType getSystemAdministratorPersonRole(OrganizationType org) throws DataAccessException, FactoryException, ArgumentException
		{
			return getPersonRole("SystemAdministrators",null, org);
		}
		public static PersonRoleType getSystemAdministratorPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "SystemAdministrators",null);
		}
		public static PersonRoleType getDataAdministratorPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "DataAdministrators",null);
		}
		public static PersonRoleType getDataAdministratorPersonRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getPersonRole("DataAdministrators", null, organization);
		}

		public static PersonRoleType getObjectAdministratorPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "ObjectAdministrators",null);
		}
		public static PersonRoleType getObjectAdministratorPersonRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getPersonRole("ObjectAdministrators", null, organization);
		}
		
		public static PersonRoleType getAccountAdministratorPersonRole(OrganizationType organization) throws FactoryException, ArgumentException
		{
			return getPersonRole("AccountAdministrators", null,organization);
		}
		public static PersonRoleType getAccountAdministratorPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "AccountAdministrators",null);
		}
		
}
