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
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class RoleService {
	public static final Logger logger = Logger.getLogger(RoleService.class.getName());

	public static boolean getIsMemberInEffectiveRole(NameIdType member,BaseRoleType role) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		switch(member.getNameType()){
			case ACCOUNT:
				out_bool = getIsAccountInEffectiveRole(role, (AccountType)member);
				break;
			case USER:
				out_bool = getIsUserInEffectiveRole(role, (UserType)member);
				break;
			case PERSON:
				out_bool = getIsPersonInEffectiveRole(role, (PersonType)member);
				break;
		}
		return out_bool;
	}
	public static boolean getIsMemberInRole(NameIdType member,BaseRoleType role) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		switch(member.getNameType()){
			case ACCOUNT:
				out_bool = getIsAccountInRole(role, (AccountType)member);
				break;
			case USER:
				out_bool = getIsUserInRole(role, (UserType)member);
				break;
			case PERSON:
				out_bool = getIsPersonInRole(role, (PersonType)member);
				break;
		}
		return out_bool;
	}
	
	public static BaseRoleType getDataSystemRoleForMemberType(NameEnumType memberType, long organizationId) throws FactoryException, ArgumentException{
		BaseRoleType role = null;
		switch(memberType){
			case USER:
				role = RoleService.getDataAdministratorUserRole(organizationId);
				break;
			case ACCOUNT:
				role = RoleService.getDataAdministratorAccountRole(organizationId);
				break;
			case PERSON:
				role = RoleService.getDataAdministratorPersonRole(organizationId);
				break;
			default:
				break;
		}
		return role;
	}
	public static BaseRoleType getAccountSystemRoleForMemberType(NameEnumType memberType, long organizationId) throws FactoryException, ArgumentException{
		BaseRoleType role = null;
		switch(memberType){
			case USER:
				role = RoleService.getAccountAdministratorUserRole(organizationId);
				break;
			case ACCOUNT:
				role = RoleService.getAccountAdministratorAccountRole(organizationId);
				break;
			case PERSON:
				role = RoleService.getAccountAdministratorPersonRole(organizationId);
				break;
			default:
				break;
		}
		return role;
	}
	public static BaseRoleType getObjectSystemRoleForMemberType(NameEnumType memberType, long organizationId) throws FactoryException, ArgumentException{
		BaseRoleType role = null;
		switch(memberType){
			case USER:
				role = RoleService.getObjectAdministratorUserRole(organizationId);
				break;
			case ACCOUNT:
				role = RoleService.getObjectAdministratorAccountRole(organizationId);
				break;
			case PERSON:
				role = RoleService.getObjectAdministratorPersonRole(organizationId);
				break;
			default:
				break;
		}
		return role;
	}
	
	public static boolean isMemberActor(NameIdType member){
		boolean out_bool = false;
		if(member == null) return out_bool;
		if(
				member.getNameType() == NameEnumType.PERSON
				|| member.getNameType() == NameEnumType.ACCOUNT
				|| member.getNameType() == NameEnumType.USER
		){
			out_bool = true;
		}
		return out_bool;
	}
	
	public static BaseRoleType getSystemRoleForMemberByMapType(NameIdType object,NameIdType member) throws FactoryException, ArgumentException{
		BaseRoleType role = null;
		if(isMemberActor(member) == false) return role;
		switch(object.getNameType()){
			case PERMISSION:
			case ACCOUNT:
			case USER:
			case PERSON:
				role = RoleService.getAccountSystemRoleForMemberType(member.getNameType(), object.getOrganizationId());
				break;
			case GROUP:
			case DATA:
				role = RoleService.getDataSystemRoleForMemberType(member.getNameType(), object.getOrganizationId());
				break;
			default:
				break;
		}
		return role;
	}
	
	public static boolean getIsGroupInRole(BaseRoleType role, BaseGroupType group) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error("Role is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsGroupInRole(role, group, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsGroupInRole(BaseRoleType role, BaseGroupType group, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
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
	public static boolean addGroupToRole(BaseGroupType group, BaseRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addGroupToRole(group, role, null, AffectEnumType.UNKNOWN);
	}

	public static boolean addGroupToRole(BaseGroupType account, BaseRoleType role, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
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
	public static boolean removeGroupFromRole(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
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

	public static PersonRoleType getPersonRole(String role_name, PersonRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return Factories.getRoleFactory().getPersonRoleByName(role_name, Parent, organizationId);
	}
	public static boolean getIsPersonInEffectiveRole(BaseRoleType role, PersonType user) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error("Role is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsPersonInEffectiveRole(role, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsPersonInEffectiveRole(BaseRoleType role, PersonType user, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error("Role is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return EffectiveAuthorizationService.getIsPersonInEffectiveRole(role, user, permission, affect_type);
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

	public static AccountRoleType getAccountRole(String role_name, AccountRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return Factories.getRoleFactory().getAccountRoleByName(role_name, Parent, organizationId);
	}
	public static boolean getIsAccountInEffectiveRole(BaseRoleType role, AccountType user) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error("Role is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsAccountInEffectiveRole(role, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsAccountInEffectiveRole(BaseRoleType role, AccountType user, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error("Role is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return EffectiveAuthorizationService.getIsAccountInEffectiveRole(role, user, permission, affect_type);
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

	public static UserRoleType getUserRole(String role_name, UserRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return Factories.getRoleFactory().getUserRoleByName(role_name, Parent, organizationId);
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
		public static UserRoleType getAccountUsersReaderUserRole(long organizationId) throws  FactoryException, ArgumentException
		{
			return getUserRole("AccountUsersReaders", null,organizationId);
		}
		public static UserRoleType getRoleReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "RoleReaders", null);
		}
		public static UserRoleType getRoleReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("RoleReaders", null,organizationId);
		}
		public static UserRoleType getPermissionReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "PermissionReaders", null);
		}
		public static UserRoleType getPermissionReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("PermissionReaders", null,organizationId);
		}
		public static UserRoleType getGroupReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "GroupReaders", null);
		}
		public static UserRoleType getGroupReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("GroupReaders", null,organizationId);
		}
		public static UserRoleType getDataReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "DataReaders", null);
		}
		public static UserRoleType getDataReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("DataReaders", null,organizationId);
		}
		public static UserRoleType getObjectReaderUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "ObjectReaders", null);
		}
		public static UserRoleType getObjectReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("ObjectReaders", null,organizationId);
		}
		public static UserRoleType getApiUserUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "ApiUsers", null);
		}
		public static UserRoleType getApiUserUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("ApiUsers", null,organizationId);
		}
		
		public static AccountRoleType getAccountUsersReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "AccountUsersReaders", null);
		}
		public static AccountRoleType getAccountUsersReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("AccountUsersReaders", null,organizationId);
		}
		public static AccountRoleType getRoleReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "RoleReaders", null);
		}
		public static AccountRoleType getRoleReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("RoleReaders", null,organizationId);
		}
		public static AccountRoleType getPermissionReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "PermissionReaders", null);
		}
		public static AccountRoleType getPermissionReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("PermissionReaders", null,organizationId);
		}
		public static AccountRoleType getGroupReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "GroupReaders", null);
		}
		public static AccountRoleType getGroupReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("GroupReaders", null,organizationId);
		}
		public static AccountRoleType getDataReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "DataReaders", null);
		}
		public static AccountRoleType getDataReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("DataReaders", null,organizationId);
		}
		public static AccountRoleType getObjectReaderAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "ObjectReaders", null);
		}
		public static AccountRoleType getObjectReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("ObjectReaders", null,organizationId);
		}
	
		/// Users
		///
		public static UserRoleType getAccountUsersRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "AccountUsers", null);
		}
		public static UserRoleType getAccountUsersRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole("AccountUsers", null,organizationId);
		}
		public static UserRoleType getAccountDevelopersUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "AccountDevelopers", null);
		}
		public static UserRoleType getAccountDevelopersUserRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole("AccountDevelopers", null,organizationId);
		}
		public static UserRoleType getSystemAdministratorUserRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole("SystemAdministrators",null,organizationId);
		}
		public static UserRoleType getSystemAdministratorUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "SystemAdministrators",null);
		}
		public static UserRoleType getDataAdministratorUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "DataAdministrators",null);
		}
		public static UserRoleType getDataAdministratorUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("DataAdministrators", null, organizationId);
		}
		public static UserRoleType getObjectAdministratorUserRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(role_owner, "ObjectAdministrators",null);
		}
		public static UserRoleType getObjectAdministratorUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("ObjectAdministrators", null, organizationId);
		}
		public static UserRoleType getAccountAdministratorUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole("AccountAdministrators", null,organizationId);
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
		public static AccountRoleType getSystemAdministratorAccountRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getAccountRole("SystemAdministrators",null, organizationId);
		}
		public static AccountRoleType getSystemAdministratorAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "SystemAdministrators",null);
		}
		public static AccountRoleType getDataAdministratorAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "DataAdministrators",null);
		}
		public static AccountRoleType getDataAdministratorAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("DataAdministrators", null, organizationId);
		}

		public static AccountRoleType getObjectAdministratorAccountRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(role_owner, "ObjectAdministrators",null);
		}
		public static AccountRoleType getObjectAdministratorAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("ObjectAdministrators", null, organizationId);
		}
		
		public static AccountRoleType getAccountAdministratorAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole("AccountAdministrators", null,organizationId);
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
		public static PersonRoleType getSystemAdministratorPersonRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getPersonRole("SystemAdministrators",null, organizationId);
		}
		public static PersonRoleType getSystemAdministratorPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "SystemAdministrators",null);
		}
		public static PersonRoleType getDataAdministratorPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "DataAdministrators",null);
		}
		public static PersonRoleType getDataAdministratorPersonRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getPersonRole("DataAdministrators", null, organizationId);
		}

		public static PersonRoleType getObjectAdministratorPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "ObjectAdministrators",null);
		}
		public static PersonRoleType getObjectAdministratorPersonRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getPersonRole("ObjectAdministrators", null, organizationId);
		}
		
		public static PersonRoleType getAccountAdministratorPersonRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getPersonRole("AccountAdministrators", null,organizationId);
		}
		public static PersonRoleType getAccountAdministratorPersonRole(UserType role_owner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(role_owner, "AccountAdministrators",null);
		}
		
}
