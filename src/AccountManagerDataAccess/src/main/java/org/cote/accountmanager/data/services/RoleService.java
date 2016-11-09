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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
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
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;

public class RoleService {
	public static final Logger logger = LogManager.getLogger(RoleService.class);
	
	public static final String ROLE_SYSTEM_ADMINISTRATOR = "SystemAdministrators";
	public static final String ROLE_DATA_ADMINISTRATOR = "DataAdministrators";
	public static final String ROLE_DATA_READER = "DataReaders";
	public static final String ROLE_ACCOUNT_ADMINISTRATOR = "AccountAdministrators";
	public static final String ROLE_ACCOUNT_USERS_READER = "AccountUsersReaders";	
	public static final String ROLE_ACCOUNT_USERS = "AccountUsers";
	public static final String ROLE_API_USERS = "ApiUsers";
	public static final String ROLE_PERMISSION_READERS = "PermissionReaders";
	public static final String ROLE_ROLE_READERS = "RoleReaders";
	public static final String ROLE_GROUP_READERS = "GroupReaders";
	
	public static final String[] SYSTEM_ROLES = new String[]{
		ROLE_SYSTEM_ADMINISTRATOR, ROLE_DATA_ADMINISTRATOR, ROLE_DATA_READER,
		ROLE_ACCOUNT_ADMINISTRATOR, ROLE_ACCOUNT_USERS, ROLE_API_USERS,
		ROLE_PERMISSION_READERS, ROLE_ROLE_READERS, ROLE_GROUP_READERS
	};
	
	private static boolean isFactoryRoleMember(NameIdType actor, String roleName, long organizationId) throws ArgumentException, FactoryException{
		if(!isMemberActor(actor)){
			logger.debug("Actor " + (actor == null ? " is null" : actor.getNameType() + " is not a valid member"));
			return false;
		}
		if(roleName == null){
			logger.warn("Factory role is null");
			return false;
		}

		BaseRoleType role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName(roleName, null, RoleEnumType.valueOf(actor.getNameType().toString()),organizationId);
		if(role == null){
			logger.warn("Role '" + roleName + "' does not exist for type " + actor.getNameType());
			return false;
		}
		return getIsMemberInEffectiveRole(actor,role);
	}
	public static boolean isFactoryAdministrator(NameIdType actor, NameIdFactory factory, long organizationId) throws ArgumentException, FactoryException{
		return isFactoryRoleMember(actor,factory.getSystemRoleNameAdministrator(),organizationId);
	}
	public static boolean isFactoryReader(NameIdType actor, NameIdFactory factory, long organizationId) throws ArgumentException, FactoryException{
		return isFactoryRoleMember(actor,factory.getSystemRoleNameReader(),organizationId);
	}

	public static boolean isFactoryAdministrator(NameIdType actor, NameIdFactory factory) throws ArgumentException, FactoryException{
		return isFactoryRoleMember(actor,factory.getSystemRoleNameAdministrator(),actor.getOrganizationId());
	}
	public static boolean isFactoryReader(NameIdType actor, NameIdFactory factory) throws ArgumentException, FactoryException{
		return isFactoryRoleMember(actor,factory.getSystemRoleNameReader(),actor.getOrganizationId());
	}

	
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
				// || member.getNameType() == NameEnumType.ROLE
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
		return ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getIsGroupInRole(role, group,permission,affect_type);
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
			GroupParticipantType ap = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newGroupRoleParticipation(role, account);
			if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).add(ap))
			{
				// EffectiveAuthorizationService.pendRoleUpdate(role);
				EffectiveAuthorizationService.pendUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeGroupFromRole(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
	{
		if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteGroupRoleParticipants(role, group))
		{
			// EffectiveAuthorizationService.pendRoleUpdate(role);
			EffectiveAuthorizationService.pendUpdate(group);
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
		//return ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getIsUserInRole(role, user,permission,affect_type);
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
			UserParticipantType ap = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newUserRoleParticipation(role, account);
			if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).add(ap))
			{
				EffectiveAuthorizationService.pendUserUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeUserFromRole(UserRoleType role, UserType account) throws FactoryException, ArgumentException
	{
		if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteUserRoleParticipants(role, account))
		{
			EffectiveAuthorizationService.pendUserUpdate(account);
			return true;
		}
		return false;
	}
	/*
	public static boolean addUserRoleToGroup(UserRoleType role, BaseGroupType group, BasePermissionType permission, AffectEnumType affectType){
		BaseParticipantType bp = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newRoleGroupParticipation(group, role, permission, affectType);
		((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).add(bp);
	}
	*/
	
	public static PersonRoleType getCreatePersonRole(UserType role_owner, PersonType person, String role_name) throws DataAccessException, FactoryException, ArgumentException
	{
		PersonRoleType parent_role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getPersonRole(person);
		return getCreatePersonRole(role_owner, role_name, parent_role);
	}
	public static PersonRoleType getCreatePersonRole(UserType role_owner, String role_name, PersonRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreatePersonRole(role_owner, role_name, Parent);
	}

	public static PersonRoleType getPersonRole(String role_name, PersonRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getPersonRoleByName(role_name, Parent, organizationId);
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
		return ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getIsPersonInRole(role, person,permission,affect_type);
	}
	public static boolean addPersonToRole(PersonType person, PersonRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addPersonToRole(person, role, null, AffectEnumType.UNKNOWN);
	}
	public static UserRoleType getCreatePersonRole(UserType role_owner, String role_name) throws DataAccessException, FactoryException, ArgumentException
	{
		UserRoleType parent_role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(role_owner);
		return getCreateUserRole(role_owner, role_name, parent_role);
	}
	
	public static AccountRoleType getCreateAccountRole(UserType role_owner, AccountType account, String role_name) throws DataAccessException, FactoryException, ArgumentException
	{
		AccountRoleType parent_role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getAccountRole(account);
		return getCreateAccountRole(role_owner, role_name, parent_role);
	}
	public static AccountRoleType getCreateAccountRole(UserType role_owner, String role_name, AccountRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateAccountRole(role_owner, role_name, Parent);
	}

	public static AccountRoleType getAccountRole(String role_name, AccountRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getAccountRoleByName(role_name, Parent, organizationId);
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
		return ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getIsAccountInRole(role, account,permission,affect_type);
	}
	public static boolean addAccountToRole(AccountType account, AccountRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addAccountToRole(account, role, null, AffectEnumType.UNKNOWN);
	}
	public static UserRoleType getCreateAccountRole(UserType role_owner, String role_name) throws DataAccessException, FactoryException, ArgumentException
	{
		UserRoleType parent_role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(role_owner);
		return getCreateUserRole(role_owner, role_name, parent_role);
	}
	public static UserRoleType getCreateUserRole(UserType role_owner, String role_name, UserRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateUserRole(role_owner, role_name, Parent);
	}

	public static UserRoleType getUserRole(String role_name, UserRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRoleByName(role_name, Parent, organizationId);
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
			AccountParticipantType ap = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newAccountRoleParticipation(role, account);
			if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).add(ap))
			{
				EffectiveAuthorizationService.pendAccountUpdate(account);
				return true;
			}
			
		}
		return false;
	}
	public static boolean removeAccountFromRole(AccountRoleType role, AccountType account) throws FactoryException, ArgumentException
	{
		if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteAccountRoleParticipants(role, account))
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
			PersonParticipantType ap = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newPersonRoleParticipation(role, person);
			if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).add(ap))
			{
				EffectiveAuthorizationService.pendPersonUpdate(person);
				return true;
			}
		}
		return false;
	}
	public static boolean removePersonFromRole(PersonRoleType role, PersonType person) throws FactoryException, ArgumentException
	{
		if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deletePersonRoleParticipants(role, person))
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
