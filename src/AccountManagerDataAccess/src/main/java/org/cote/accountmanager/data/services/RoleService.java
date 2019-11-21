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
package org.cote.accountmanager.data.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.exceptions.FactoryException;
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
	
	public static final String ROLE_OBJECT_ADMINISTRATOR = "ObjectAdministrators";
	public static final String ROLE_OBJECT_READERS = "ObjectReaders";
	public static final String ROLE_SYSTEM_ADMINISTRATOR = "SystemAdministrators";
	public static final String ROLE_DATA_ADMINISTRATOR = "DataAdministrators";
	public static final String ROLE_DATA_READERS = "DataReaders";
	public static final String ROLE_ACCOUNT_ADMINISTRATOR = "AccountAdministrators";
	public static final String ROLE_ACCOUNT_DEVELOPERS = "AccountDevelopers";
	public static final String ROLE_ACCOUNT_USERS_READERS = "AccountUsersReaders";	
	public static final String ROLE_ACCOUNT_USERS = "AccountUsers";
	public static final String ROLE_API_USERS = "ApiUsers";
	public static final String ROLE_ARTICLE_AUTHORS = "ArticleAuthors";
	public static final String ROLE_PERMISSION_READERS = "PermissionReaders";
	public static final String ROLE_ROLE_READERS = "RoleReaders";
	public static final String ROLE_ROLE_ADMINISTRATORS = "RoleAdministrators";
	public static final String ROLE_PERMISSION_ADMINISTRATORS = "PermissionAdministrators";
	public static final String ROLE_GROUP_READERS = "GroupReaders";

	
	protected static final String[] SYSTEM_ROLE_NAMES = new String[]{
		ROLE_SYSTEM_ADMINISTRATOR, ROLE_DATA_ADMINISTRATOR, ROLE_DATA_READERS,ROLE_ARTICLE_AUTHORS,
		ROLE_ACCOUNT_ADMINISTRATOR, ROLE_ACCOUNT_DEVELOPERS, ROLE_ACCOUNT_USERS, ROLE_ACCOUNT_USERS_READERS, ROLE_API_USERS,ROLE_PERMISSION_ADMINISTRATORS,
		ROLE_PERMISSION_READERS, ROLE_ROLE_READERS, ROLE_ROLE_ADMINISTRATORS, ROLE_GROUP_READERS, ROLE_OBJECT_READERS, ROLE_OBJECT_ADMINISTRATOR
	};
	
	protected static final Map<Long,List<BaseRoleType>> SYSTEM_ROLE_OBJECTS = new HashMap<>();
	
	public static List<BaseRoleType> getSystemRoles(long organizationId){
		List<BaseRoleType> outList = new ArrayList<>();
		if(!SYSTEM_ROLE_OBJECTS.containsKey(organizationId)){
			try{
				outList.addAll(Arrays.asList(
					RoleService.getAccountAdministratorPersonRole(organizationId),
					RoleService.getDataAdministratorPersonRole(organizationId),
					RoleService.getObjectAdministratorPersonRole(organizationId),
					RoleService.getSystemAdministratorPersonRole(organizationId),
					RoleService.getAccountUsersPersonRole(organizationId),
					RoleService.getAccountAdministratorAccountRole(organizationId),
					RoleService.getDataAdministratorAccountRole(organizationId),
					RoleService.getObjectAdministratorAccountRole(organizationId),
					RoleService.getSystemAdministratorAccountRole(organizationId),
					RoleService.getAccountUsersAccountRole(organizationId),
					
					RoleService.getAccountAdministratorUserRole(organizationId),
					RoleService.getDataAdministratorUserRole(organizationId),
					RoleService.getObjectAdministratorUserRole(organizationId),
					RoleService.getSystemAdministratorUserRole(organizationId),
					RoleService.getAccountUsersRole(organizationId),
		
					RoleService.getAccountUsersReaderAccountRole(organizationId),
					RoleService.getPermissionReaderAccountRole(organizationId),
					RoleService.getPermissionAdministratorAccountRole(organizationId),
					RoleService.getRoleReaderAccountRole(organizationId),
					RoleService.getDataReaderAccountRole(organizationId),
					RoleService.getGroupReaderAccountRole(organizationId),
					RoleService.getObjectReaderAccountRole(organizationId),
					RoleService.getApiUserUserRole(organizationId),
					RoleService.getAccountUsersReaderUserRole(organizationId),
					RoleService.getRoleReaderUserRole(organizationId),
					RoleService.getPermissionReaderUserRole(organizationId),
					RoleService.getPermissionAdministratorUserRole(organizationId),
					RoleService.getDataReaderUserRole(organizationId),
					RoleService.getGroupReaderUserRole(organizationId),
					RoleService.getObjectReaderUserRole(organizationId),
					RoleService.getArticleAuthorUserRole(organizationId)
				));
				
				SYSTEM_ROLE_OBJECTS.put(organizationId, outList);
			}
			catch(ArgumentException | FactoryException | DataAccessException e){
				logger.error(e);
			}

		}
		else{
			outList = SYSTEM_ROLE_OBJECTS.get(organizationId);
		}
		return outList;
	}
	
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
		boolean outBool = false;
		switch(member.getNameType()){
			case ACCOUNT:
				outBool = getIsAccountInEffectiveRole(role, (AccountType)member);
				break;
			case USER:
				outBool = getIsUserInEffectiveRole(role, (UserType)member);
				break;
			case PERSON:
				outBool = getIsPersonInEffectiveRole(role, (PersonType)member);
				break;
			default:
				logger.error(String.format(FactoryException.UNHANDLED_TYPE, member.getNameType().toString()));
				break;
		}
		return outBool;
	}
	public static boolean getIsMemberInRole(NameIdType member,BaseRoleType role) throws ArgumentException, FactoryException{
		boolean outBool = false;
		switch(member.getNameType()){
			case ACCOUNT:
				outBool = getIsAccountInRole(role, (AccountType)member);
				break;
			case USER:
				outBool = getIsUserInRole(role, (UserType)member);
				break;
			case PERSON:
				outBool = getIsPersonInRole(role, (PersonType)member);
				break;
			default:
				logger.error(String.format(FactoryException.UNHANDLED_TYPE, member.getNameType().toString()));
				break;
		}
		return outBool;
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
				logger.error(String.format(FactoryException.UNHANDLED_TYPE, memberType.toString()));
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
				logger.error(String.format(FactoryException.UNHANDLED_TYPE, memberType.toString()));
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
				logger.error(String.format(FactoryException.UNHANDLED_TYPE, memberType.toString()));
				break;
		}
		return role;
	}
	
	public static boolean isMemberActor(NameIdType member){
		boolean outBool = false;
		if(member == null) return outBool;
		if(
				member.getNameType() == NameEnumType.PERSON
				|| member.getNameType() == NameEnumType.ACCOUNT
				|| member.getNameType() == NameEnumType.USER
				// || member.getNameType() == NameEnumType.ROLE
		){
			outBool = true;
		}
		return outBool;
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
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsGroupInRole(role, group, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsGroupInRole(BaseRoleType role, BaseGroupType group, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getIsGroupInRole(role, group,permission,affectType);
	}
	public static boolean addGroupToRole(BaseGroupType group, BaseRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addGroupToRole(group, role, null, AffectEnumType.UNKNOWN);
	}

	public static boolean addGroupToRole(BaseGroupType account, BaseRoleType role, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, DataAccessException, FactoryException
	{
		/// accommodate bulk inserts with a negative id - skip the check for the getGroupInRole, which will return true for bulk jobs
		///
		if (role.getId() < 0L || !getIsGroupInRole(role, account))
		{
			GroupParticipantType ap = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newGroupRoleParticipation(role, account);
			if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).add(ap))
			{				EffectiveAuthorizationService.pendUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeGroupFromRole(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
	{
		if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteGroupRoleParticipants(role, group))
		{
			EffectiveAuthorizationService.pendUpdate(group);
			return true;
		}
		return false;
	}

	public static boolean getIsUserInEffectiveRole(BaseRoleType role, UserType user) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsUserInEffectiveRole(role, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsUserInEffectiveRole(BaseRoleType role, UserType user, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return EffectiveAuthorizationService.getIsActorInEffectiveRole(role, user, permission, affectType);
	}
	
	public static boolean getIsUserInRole(BaseRoleType role, UserType user) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsUserInRole(role, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsUserInRole(BaseRoleType role, UserType user, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return EffectiveAuthorizationService.getIsActorInEffectiveRole(role, user,permission,affectType);
	}
	public static boolean addUserToRole(UserType user, UserRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addUserToRole(user, role, null, AffectEnumType.UNKNOWN);
	}

	public static boolean addUserToRole(UserType account, UserRoleType role, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, DataAccessException, FactoryException
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
	
	public static PersonRoleType getCreatePersonRole(UserType roleOwner, PersonType person, String roleName) throws DataAccessException, FactoryException, ArgumentException
	{
		PersonRoleType parentRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getPersonRole(person);
		return getCreatePersonRole(roleOwner, roleName, parentRole);
	}
	public static PersonRoleType getCreatePersonRole(UserType roleOwner, String roleName, PersonRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreatePersonRole(roleOwner, roleName, Parent);
	}

	public static PersonRoleType getPersonRole(String roleName, PersonRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getPersonRoleByName(roleName, Parent, organizationId);
	}
	public static boolean getIsPersonInEffectiveRole(BaseRoleType role, PersonType user) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsPersonInEffectiveRole(role, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsPersonInEffectiveRole(BaseRoleType role, PersonType user, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return EffectiveAuthorizationService.getIsActorInEffectiveRole(role, user, permission, affectType);
	}
	public static boolean getIsPersonInRole(BaseRoleType role, PersonType person) throws ArgumentException, FactoryException{
		return getIsPersonInRole(role, person, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsPersonInRole(BaseRoleType role, PersonType person, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
	{
		return ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getIsPersonInRole(role, person,permission,affectType);
	}
	public static boolean addPersonToRole(PersonType person, PersonRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addPersonToRole(person, role, null, AffectEnumType.UNKNOWN);
	}
	public static UserRoleType getCreatePersonRole(UserType roleOwner, String roleName) throws DataAccessException, FactoryException, ArgumentException
	{
		UserRoleType parentRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(roleOwner);
		return getCreateUserRole(roleOwner, roleName, parentRole);
	}
	
	public static AccountRoleType getCreateAccountRole(UserType roleOwner, AccountType account, String roleName) throws DataAccessException, FactoryException, ArgumentException
	{
		AccountRoleType parentRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getAccountRole(account);
		return getCreateAccountRole(roleOwner, roleName, parentRole);
	}
	public static AccountRoleType getCreateAccountRole(UserType roleOwner, String roleName, AccountRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateAccountRole(roleOwner, roleName, Parent);
	}

	public static AccountRoleType getAccountRole(String roleName, AccountRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getAccountRoleByName(roleName, Parent, organizationId);
	}
	public static boolean getIsAccountInEffectiveRole(BaseRoleType role, AccountType user) throws ArgumentException, FactoryException{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(role.getId() < 0L ) return true;
		return getIsAccountInEffectiveRole(role, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsAccountInEffectiveRole(BaseRoleType role, AccountType user, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
	{
		if(role == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE,  NameEnumType.ROLE.toString()));
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(role.getId() < 0L ) return true;
		return EffectiveAuthorizationService.getIsActorInEffectiveRole(role, user, permission, affectType);
	}
	public static boolean getIsAccountInRole(BaseRoleType role, AccountType account) throws ArgumentException, FactoryException{
		return getIsAccountInRole(role, account, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsAccountInRole(BaseRoleType role, AccountType account, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
	{
		return ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getIsAccountInRole(role, account,permission,affectType);
	}
	public static boolean addAccountToRole(AccountType account, AccountRoleType role) throws ArgumentException, DataAccessException, FactoryException
	{
		return addAccountToRole(account, role, null, AffectEnumType.UNKNOWN);
	}
	public static UserRoleType getCreateAccountRole(UserType roleOwner, String roleName) throws DataAccessException, FactoryException, ArgumentException
	{
		UserRoleType parentRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(roleOwner);
		return getCreateUserRole(roleOwner, roleName, parentRole);
	}
	public static UserRoleType getCreateUserRole(UserType roleOwner, String roleName, UserRoleType Parent) throws DataAccessException, FactoryException, ArgumentException
	{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateUserRole(roleOwner, roleName, Parent);
	}

	public static UserRoleType getUserRole(String roleName, UserRoleType Parent, long organizationId) throws FactoryException, ArgumentException{
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRoleByName(roleName, Parent, organizationId);
	}
	/// <summary>
	/// Adds an account participation to a role participation, with affect 
	/// </summary>
	/// <param name="role_admin"></param>
	/// <param name="account"></param>
	/// <param name="role"></param>
	/// <param name="permission"></param>
	/// <param name="affectType"></param>
	/// <returns></returns>
	public static boolean addAccountToRole(AccountType account, AccountRoleType role, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, DataAccessException, FactoryException
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
	
	public static boolean addPersonToRole(PersonType person, PersonRoleType role, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, DataAccessException, FactoryException
	{
		if (getIsPersonInRole(role, person) == false)
		{
			PersonParticipantType ap = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newPersonRoleParticipation(role, person);
			if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).add(ap))
			{
				EffectiveAuthorizationService.pendUpdate(person);
				return true;
			}
		}
		return false;
	}
	public static boolean removePersonFromRole(PersonRoleType role, PersonType person) throws FactoryException, ArgumentException
	{
		if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deletePersonRoleParticipants(role, person))
		{
			EffectiveAuthorizationService.pendUpdate(person);
			return true;
		}
		return false;
	}
	
	
		/// Access roles for viewing objects
		///
		public static UserRoleType getAccountUsersReaderUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_ACCOUNT_USERS_READERS, null);
		}
		public static UserRoleType getAccountUsersReaderUserRole(long organizationId) throws  FactoryException, ArgumentException
		{
			return getUserRole(ROLE_ACCOUNT_USERS_READERS, null,organizationId);
		}
		public static UserRoleType getRoleReaderUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_ROLE_READERS, null);
		}
		public static UserRoleType getRoleReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_ROLE_READERS, null,organizationId);
		}
		public static UserRoleType getPermissionReaderUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_PERMISSION_READERS, null);
		}
		public static UserRoleType getPermissionReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_PERMISSION_READERS, null,organizationId);
		}
		
		public static UserRoleType getPermissionAdministratorUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_PERMISSION_ADMINISTRATORS,null);
		}
		public static UserRoleType getPermissionAdministratorUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_PERMISSION_ADMINISTRATORS, null, organizationId);
		}
		
		public static UserRoleType getGroupReaderUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_GROUP_READERS, null);
		}
		public static UserRoleType getGroupReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_GROUP_READERS, null,organizationId);
		}
		public static UserRoleType getDataReaderUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_DATA_READERS, null);
		}
		public static UserRoleType getDataReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_DATA_READERS, null,organizationId);
		}
		public static UserRoleType getObjectReaderUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_OBJECT_READERS, null);
		}
		public static UserRoleType getObjectReaderUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_OBJECT_READERS, null,organizationId);
		}
		public static UserRoleType getApiUserUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_API_USERS, null);
		}
		public static UserRoleType getApiUserUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_API_USERS, null,organizationId);
		}
		public static UserRoleType getArticleAuthorUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_ARTICLE_AUTHORS, null);
		}
		public static UserRoleType getArticleAuthorUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_ARTICLE_AUTHORS, null,organizationId);
		}
		public static AccountRoleType getAccountUsersReaderAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_ACCOUNT_USERS_READERS, null);
		}
		public static AccountRoleType getAccountUsersReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_ACCOUNT_USERS_READERS, null,organizationId);
		}
		public static AccountRoleType getRoleReaderAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_ROLE_READERS, null);
		}
		public static AccountRoleType getRoleReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_ROLE_READERS, null,organizationId);
		}
		public static AccountRoleType getPermissionReaderAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_PERMISSION_READERS, null);
		}
		public static AccountRoleType getPermissionReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_PERMISSION_READERS, null,organizationId);
		}
		
		public static AccountRoleType getPermissionAdministratorAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_PERMISSION_ADMINISTRATORS,null);
		}
		public static AccountRoleType getPermissionAdministratorAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_PERMISSION_ADMINISTRATORS, null, organizationId);
		}
		
		public static AccountRoleType getGroupReaderAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_GROUP_READERS, null);
		}
		public static AccountRoleType getGroupReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_GROUP_READERS, null,organizationId);
		}
		public static AccountRoleType getDataReaderAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_DATA_READERS, null);
		}
		public static AccountRoleType getDataReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_DATA_READERS, null,organizationId);
		}
		public static AccountRoleType getObjectReaderAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_OBJECT_READERS, null);
		}
		public static AccountRoleType getObjectReaderAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_OBJECT_READERS, null,organizationId);
		}
	
		/// Users
		///
		public static UserRoleType getAccountUsersRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_ACCOUNT_USERS, null);
		}
		public static UserRoleType getAccountUsersRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole(ROLE_ACCOUNT_USERS, null,organizationId);
		}
		public static UserRoleType getAccountDevelopersUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_ACCOUNT_DEVELOPERS, null);
		}
		public static UserRoleType getAccountDevelopersUserRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole(ROLE_ACCOUNT_DEVELOPERS, null,organizationId);
		}
		public static UserRoleType getSystemAdministratorUserRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getUserRole(ROLE_SYSTEM_ADMINISTRATOR,null,organizationId);
		}
		public static UserRoleType getSystemAdministratorUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_SYSTEM_ADMINISTRATOR,null);
		}
		public static UserRoleType getDataAdministratorUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_DATA_ADMINISTRATOR,null);
		}
		public static UserRoleType getDataAdministratorUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_DATA_ADMINISTRATOR, null, organizationId);
		}
		public static UserRoleType getObjectAdministratorUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_OBJECT_ADMINISTRATOR,null);
		}
		public static UserRoleType getObjectAdministratorUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_OBJECT_ADMINISTRATOR, null, organizationId);
		}
		public static UserRoleType getAccountAdministratorUserRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getUserRole(ROLE_ACCOUNT_ADMINISTRATOR, null,organizationId);
		}
		public static UserRoleType getAccountAdministratorUserRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateUserRole(roleOwner, ROLE_ACCOUNT_ADMINISTRATOR,null);
		}
		

		/// Account
		///
		public static AccountRoleType getAccountUsersAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_ACCOUNT_USERS, null);
		}
		public static AccountRoleType getAccountUsersAccountRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_ACCOUNT_USERS, null, organizationId);
		}
		public static AccountRoleType getAccountDevelopersAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_ACCOUNT_DEVELOPERS, null);
		}
		public static AccountRoleType getSystemAdministratorAccountRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_SYSTEM_ADMINISTRATOR,null, organizationId);
		}
		public static AccountRoleType getSystemAdministratorAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_SYSTEM_ADMINISTRATOR,null);
		}
		public static AccountRoleType getDataAdministratorAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_DATA_ADMINISTRATOR,null);
		}
		public static AccountRoleType getDataAdministratorAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_DATA_ADMINISTRATOR, null, organizationId);
		}

		public static AccountRoleType getObjectAdministratorAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_OBJECT_ADMINISTRATOR,null);
		}
		public static AccountRoleType getObjectAdministratorAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_OBJECT_ADMINISTRATOR, null, organizationId);
		}
		
		public static AccountRoleType getAccountAdministratorAccountRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getAccountRole(ROLE_ACCOUNT_ADMINISTRATOR, null,organizationId);
		}
		public static AccountRoleType getAccountAdministratorAccountRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreateAccountRole(roleOwner, ROLE_ACCOUNT_ADMINISTRATOR,null);
		}
		
		/// Person
		///
		public static PersonRoleType getAccountUsersPersonRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(roleOwner, ROLE_ACCOUNT_USERS, null);
		}
		public static PersonRoleType getAccountUsersPersonRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getPersonRole(ROLE_ACCOUNT_USERS, null, organizationId);
		}
		public static PersonRoleType getAccountDevelopersPersonRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(roleOwner, ROLE_ACCOUNT_DEVELOPERS, null);
		}
		public static PersonRoleType getSystemAdministratorPersonRole(long organizationId) throws DataAccessException, FactoryException, ArgumentException
		{
			return getPersonRole(ROLE_SYSTEM_ADMINISTRATOR,null, organizationId);
		}
		public static PersonRoleType getSystemAdministratorPersonRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(roleOwner, ROLE_SYSTEM_ADMINISTRATOR,null);
		}
		public static PersonRoleType getDataAdministratorPersonRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(roleOwner, ROLE_DATA_ADMINISTRATOR,null);
		}
		public static PersonRoleType getDataAdministratorPersonRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getPersonRole(ROLE_DATA_ADMINISTRATOR, null, organizationId);
		}

		public static PersonRoleType getObjectAdministratorPersonRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(roleOwner, ROLE_OBJECT_ADMINISTRATOR,null);
		}
		public static PersonRoleType getObjectAdministratorPersonRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getPersonRole(ROLE_OBJECT_ADMINISTRATOR, null, organizationId);
		}
		
		public static PersonRoleType getAccountAdministratorPersonRole(long organizationId) throws FactoryException, ArgumentException
		{
			return getPersonRole(ROLE_ACCOUNT_ADMINISTRATOR, null,organizationId);
		}
		public static PersonRoleType getAccountAdministratorPersonRole(UserType roleOwner) throws DataAccessException, FactoryException, ArgumentException
		{
			return getCreatePersonRole(roleOwner, ROLE_ACCOUNT_ADMINISTRATOR,null);
		}
		
		public static boolean switchActorInRole(NameIdType actor, BaseRoleType role, boolean add) throws ArgumentException, DataAccessException, FactoryException{
			boolean outBool = false;
			if(actor.getNameType() != NameEnumType.GROUP && RoleEnumType.fromValue(actor.getNameType().toString()) != role.getRoleType()){
				logger.warn("Role type " + role.getRoleType() + " mismatched with actor type " + actor.getNameType());
				return false;
			}
			switch(actor.getNameType()){
				case PERSON:
					if(add) outBool = RoleService.addPersonToRole((PersonType)actor, (PersonRoleType)role);
					else outBool = RoleService.removePersonFromRole((PersonRoleType)role,(PersonType)actor);
					break;
				case ACCOUNT:
					if(add) outBool = RoleService.addAccountToRole((AccountType)actor, (AccountRoleType)role);
					else outBool = RoleService.removeAccountFromRole((AccountRoleType)role,(AccountType)actor);
					break;
				case USER:
					if(add) outBool = RoleService.addUserToRole((UserType)actor, (UserRoleType)role);
					else outBool = RoleService.removeUserFromRole((UserRoleType)role,(UserType)actor);
					break;
				case GROUP:
					if(add) outBool = RoleService.addGroupToRole((BaseGroupType)actor, role);
					else outBool = RoleService.removeGroupFromRole(role,(BaseGroupType)actor);
					break;
				default:
					logger.error(String.format(FactoryException.UNHANDLED_TYPE,actor.getNameType().toString()));
					break;
				}

			return outBool;
		}

		
}
