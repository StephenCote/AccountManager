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

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
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
import org.cote.accountmanager.objects.RoleParticipantType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;

public class RoleParticipationFactory extends ParticipationFactory {
	public RoleParticipationFactory(){
		super(ParticipationEnumType.ROLE, "roleparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.ROLEPARTICIPATION;
		permissionPrefix = "Role";
		defaultPermissionType = PermissionEnumType.ROLE;
	}
	public boolean deleteRoleRoleParticipant(BaseRoleType role, BaseRoleType childRole) throws FactoryException, ArgumentException
	{
		RoleParticipantType dp = getRoleRoleParticipant(role, childRole);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteRoleParticipations(BaseRoleType childRole) throws FactoryException, ArgumentException
	{

		List<RoleParticipantType> dp = getRoleRoleParticipants(childRole);
		return deleteParticipants(dp.toArray(new RoleParticipantType[0]), childRole.getOrganizationId());
	}

	public RoleParticipantType newRoleRoleParticipation(BaseRoleType role, BaseRoleType childRole) throws ArgumentException
	{
		return (RoleParticipantType)newParticipant(role, childRole, ParticipantEnumType.ROLE, null, AffectEnumType.UNKNOWN);
	}
	
	public List<BaseRoleType> getRoleRoles(BaseRoleType childRole)  throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> list = getRoleRoleParticipants(childRole);

		if (list.isEmpty()) return new ArrayList<>();
		QueryField match = QueryFields.getFieldParticipationIds(list.toArray(new RoleParticipantType[0]));
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoles(match, childRole.getOrganizationId());
	}
	public List<BaseRoleType> getRolesInRole(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> ap = getRoleRoleParticipations(role);
		return getRoleListFromParticipations(ap.toArray(new RoleParticipantType[0]), role.getOrganizationId());
	}
	public List<RoleParticipantType> getRoleRoleParticipants(BaseRoleType childRole)  throws FactoryException, ArgumentException
	{
		List<QueryField> matches = new ArrayList<>();
		matches.add(QueryFields.getFieldParticipantType(ParticipantEnumType.ROLE));
		matches.add(QueryFields.getFieldParticipantId(childRole));
		List<NameIdType> dtlist = getByField(matches.toArray(new QueryField[0]), childRole.getOrganizationId());
		return convertList(dtlist);
	}
	public List<RoleParticipantType> getRoleRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<QueryField> matches = new ArrayList<>();
		matches.add(QueryFields.getFieldParticipantType(ParticipantEnumType.ROLE));
		matches.add(QueryFields.getFieldParticipationId(role));
		List<NameIdType> list = getByField(matches.toArray(new QueryField[0]), role.getOrganizationId());
		return convertList(list);
	}

	public RoleParticipantType getRoleRoleParticipant(BaseRoleType role, BaseRoleType childRole) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(childRole), QueryFields.getFieldParticipantType(ParticipantEnumType.ROLE), QueryFields.getFieldParticipationId(role) }, role.getOrganizationId());
		if (list.isEmpty()) return null;
		return (RoleParticipantType)list.get(0);
	}
	public boolean getIsRoleInRole(BaseRoleType role, BaseRoleType childRole) throws FactoryException, ArgumentException
	{
		return (getRoleRoleParticipant(role, childRole) != null);
	}

	// User
	//
	
	public boolean deleteUserRoleParticipant(UserRoleType role, UserType account)  throws ArgumentException, FactoryException
	{
		return deleteUserRoleParticipant(role, account, null,AffectEnumType.UNKNOWN);
	}
	public boolean deleteUserRoleParticipant(UserRoleType role, UserType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		UserParticipantType dp = getUserRoleParticipant(role, account, permission, affectType);
		if (dp == null) return true;

		removeParticipantFromCache(dp);

		return delete(dp);
	}
	public boolean deleteUserRoleParticipants(UserRoleType role, UserType account) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> dp = getUserRoleParticipants(role, account);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), account.getOrganizationId());
	}

	public boolean deleteUserParticipations(UserType account)  throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserRoleParticipants(account);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), account.getOrganizationId());
	}

	public UserParticipantType newUserRoleParticipation(BaseRoleType role, UserType account) throws ArgumentException
	{
		return newUserRoleParticipation(role, account, null, AffectEnumType.UNKNOWN);
	}
	public UserParticipantType newUserRoleParticipation(BaseRoleType role, UserType account,BasePermissionType permission,AffectEnumType affectType) throws ArgumentException
	{
		return (UserParticipantType)newParticipant(role, account, ParticipantEnumType.USER, permission, affectType);
	}


	public List<UserRoleType> getUserRoles(UserType account) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> list = getUserRoleParticipants(account);
		if(list.isEmpty()) return new ArrayList<>();
		QueryField match = QueryFields.getFieldParticipationIds(list.toArray(new UserParticipantType[0]));
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRoles(match, account.getOrganizationId());
	}
	public List<UserType> getUsersInRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> ap = getUserRoleParticipations(role);
		return getUserListFromParticipations(ap.toArray(new UserParticipantType[0]), role.getOrganizationId());
	}
	public List<UserParticipantType> getUserRoleParticipants(UserType account) throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipantMatch(account,ParticipantEnumType.USER), account.getOrganizationId());
		return convertList(dtlist);
	}
	public List<UserParticipantType> getUserRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipationMatch(role,ParticipantEnumType.USER), role.getOrganizationId());
		return convertList(dtlist);
	}

	public List<UserParticipantType> getUserRoleParticipants(
		BaseRoleType role, 
		UserType account
	)  throws FactoryException, ArgumentException
	{
		return getUserRoleParticipants(role, account, null, AffectEnumType.UNKNOWN);
	}
	public List<UserParticipantType> getUserRoleParticipants(
		BaseRoleType role,
		UserType account,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws FactoryException, ArgumentException
	{
		List<NameIdType> list = getParticipants(role, account, ParticipantEnumType.USER, permission, affectType);
		return convertList(list);

	}
	public UserParticipantType getUserRoleParticipant(BaseRoleType role, UserType account)  throws ArgumentException, FactoryException
	{
		return getUserRoleParticipant(role, account, null, AffectEnumType.UNKNOWN);
	} 
	public UserParticipantType getUserRoleParticipant(
		BaseRoleType role,
		UserType account,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws ArgumentException, FactoryException
	{
		return getParticipant(role, account, ParticipantEnumType.USER, permission, affectType);
	}
	public boolean getIsUserInRole(BaseRoleType role, UserType account)  throws ArgumentException, FactoryException
	{
		return (getUserRoleParticipant(role, account) != null);
	}
	public boolean getIsUserInRole(BaseRoleType role, UserType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		return (getUserRoleParticipant(role, account,permission, affectType) != null);
	}
	
	// Person
	//

	public boolean deletePersonRoleParticipant(PersonRoleType role, PersonType person)  throws ArgumentException, FactoryException
	{
		return deletePersonRoleParticipant(role, person, null,AffectEnumType.UNKNOWN);
	}
	public boolean deletePersonRoleParticipant(PersonRoleType role, PersonType person, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		PersonParticipantType dp = getPersonRoleParticipant(role, person, permission, affectType);
		if (dp == null) return true;

		removeParticipantFromCache(dp);

		return delete(dp);
	}
	public boolean deletePersonRoleParticipants(PersonRoleType role, PersonType person) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> dp = getPersonRoleParticipants(role, person);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), person.getOrganizationId());
	}

	public boolean deletePersonParticipations(PersonType person)  throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getPersonRoleParticipants(person);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), person.getOrganizationId());
	}

	public PersonParticipantType newPersonRoleParticipation(BaseRoleType role, PersonType person) throws ArgumentException
	{
		return newPersonRoleParticipation(role, person, null, AffectEnumType.UNKNOWN);
	}
	public PersonParticipantType newPersonRoleParticipation(BaseRoleType role, PersonType person,BasePermissionType permission,AffectEnumType affectType) throws ArgumentException
	{
		return (PersonParticipantType)newParticipant(role, person, ParticipantEnumType.PERSON, permission, affectType);
	}


	public List<PersonRoleType> getPersonRoles(PersonType person) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> list = getPersonRoleParticipants(person);
		if(list.isEmpty()) return new ArrayList<>();
		QueryField match = QueryFields.getFieldParticipationIds(list.toArray(new PersonParticipantType[0]));
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getPersonRoles(match, person.getOrganizationId());
	}
	public List<PersonType> getPersonsInRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> ap = getPersonRoleParticipations(role);
		return getPersonListFromParticipations(ap.toArray(new PersonParticipantType[0]), role.getOrganizationId());
	}
	public List<PersonParticipantType> getPersonRoleParticipants(PersonType person) throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipantMatch(person,ParticipantEnumType.PERSON), person.getOrganizationId());
		return convertList(dtlist);
	}
	public List<PersonParticipantType> getPersonRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipationMatch(role,ParticipantEnumType.PERSON), role.getOrganizationId());
		return convertList(dtlist);
	}

	public List<PersonParticipantType> getPersonRoleParticipants(
		BaseRoleType role, 
		PersonType person
	)  throws FactoryException, ArgumentException
	{
		return getPersonRoleParticipants(role, person, null, AffectEnumType.UNKNOWN);
	}
	public List<PersonParticipantType> getPersonRoleParticipants(
		BaseRoleType role,
		PersonType person,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws FactoryException, ArgumentException
	{
		List<NameIdType> list = getParticipants(role, person, ParticipantEnumType.PERSON, permission, affectType);
		return convertList(list);

	}
	public PersonParticipantType getPersonRoleParticipant(BaseRoleType role, PersonType person)  throws ArgumentException, FactoryException
	{
		return getPersonRoleParticipant(role, person, null, AffectEnumType.UNKNOWN);
	} 
	public PersonParticipantType getPersonRoleParticipant(
		BaseRoleType role,
		PersonType person,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws ArgumentException, FactoryException
	{
		return getParticipant(role, person, ParticipantEnumType.PERSON, permission, affectType);
	}
	public boolean getIsPersonInRole(BaseRoleType role, PersonType person)  throws ArgumentException, FactoryException
	{
		return (getPersonRoleParticipant(role, person) != null);
	}
	public boolean getIsPersonInRole(BaseRoleType role, PersonType person, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		return (getPersonRoleParticipant(role, person,permission, affectType) != null);
	}
	
	// Account
	//

	public boolean deleteAccountRoleParticipant(AccountRoleType role, AccountType account)  throws ArgumentException, FactoryException
	{
		return deleteAccountRoleParticipant(role, account, null,AffectEnumType.UNKNOWN);
	}
	public boolean deleteAccountRoleParticipant(AccountRoleType role, AccountType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		AccountParticipantType dp = getAccountRoleParticipant(role, account, permission, affectType);
		if (dp == null) return true;

		removeParticipantFromCache(dp);

		return delete(dp);
	}
	public boolean deleteAccountRoleParticipants(AccountRoleType role, AccountType account) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> dp = getAccountRoleParticipants(role, account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganizationId());
	}

	public boolean deleteAccountParticipations(AccountType account)  throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountRoleParticipants(account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganizationId());
	}

	public AccountParticipantType newAccountRoleParticipation(BaseRoleType role, AccountType account) throws ArgumentException
	{
		return newAccountRoleParticipation(role, account, null, AffectEnumType.UNKNOWN);
	}
	public AccountParticipantType newAccountRoleParticipation(BaseRoleType role, AccountType account,BasePermissionType permission,AffectEnumType affectType) throws ArgumentException
	{
		return (AccountParticipantType)newParticipant(role, account, ParticipantEnumType.ACCOUNT, permission, affectType);
	}


	public List<AccountRoleType> getAccountRoles(AccountType account) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> list = getAccountRoleParticipants(account);
		if(list.isEmpty()) return new ArrayList<>();
		QueryField match = QueryFields.getFieldParticipationIds(list.toArray(new AccountParticipantType[0]));
		return ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getAccountRoles(match, account.getOrganizationId());
	}
	public List<AccountType> getAccountsInRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> ap = getAccountRoleParticipations(role);
		return getAccountListFromParticipations(ap.toArray(new AccountParticipantType[0]), role.getOrganizationId());
	}
	public List<AccountParticipantType> getAccountRoleParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipantMatch(account,ParticipantEnumType.ACCOUNT), account.getOrganizationId());
		return convertList(dtlist);
	}
	public List<AccountParticipantType> getAccountRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipationMatch(role,ParticipantEnumType.ACCOUNT), role.getOrganizationId());
		return convertList(dtlist);
	}

	public List<AccountParticipantType> getAccountRoleParticipants(
		BaseRoleType role, 
		AccountType account
	)  throws FactoryException, ArgumentException
	{
		return getAccountRoleParticipants(role, account, null, AffectEnumType.UNKNOWN);
	}
	public List<AccountParticipantType> getAccountRoleParticipants(
		BaseRoleType role,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws FactoryException, ArgumentException
	{
		List<NameIdType> list = getParticipants(role, account, ParticipantEnumType.ACCOUNT, permission, affectType);
		return convertList(list);

	}
	public AccountParticipantType getAccountRoleParticipant(BaseRoleType role, AccountType account)  throws ArgumentException, FactoryException
	{
		return getAccountRoleParticipant(role, account, null, AffectEnumType.UNKNOWN);
	} 
	public AccountParticipantType getAccountRoleParticipant(
		BaseRoleType role,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws ArgumentException, FactoryException
	{
		return getParticipant(role, account, ParticipantEnumType.ACCOUNT, permission, affectType);
	}
	public boolean getIsAccountInRole(BaseRoleType role, AccountType account)  throws ArgumentException, FactoryException
	{
		return (getAccountRoleParticipant(role, account) != null);
	}
	public boolean getIsAccountInRole(BaseRoleType role, AccountType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		return (getAccountRoleParticipant(role, account,permission, affectType) != null);
	}
	
	/// Group
	public boolean deleteGroupRoleParticipant(BaseRoleType role, BaseGroupType group)  throws ArgumentException, FactoryException
	{
		return deleteGroupRoleParticipant(role, group, null,AffectEnumType.UNKNOWN);
	}
	public boolean deleteGroupRoleParticipant(BaseRoleType role, BaseGroupType group, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		GroupParticipantType dp = getGroupRoleParticipant(role, group, permission, affectType);
		if (dp == null) return true;

		removeParticipantFromCache(dp);

		return delete(dp);
	}
	public boolean deleteGroupRoleParticipants(BaseRoleType role, BaseGroupType account) throws FactoryException, ArgumentException
	{
		List<GroupParticipantType> dp = getGroupRoleParticipants(role, account);
		return deleteParticipants(dp.toArray(new GroupParticipantType[0]), account.getOrganizationId());
	}

	public boolean deleteGroupParticipations(UserGroupType group)  throws FactoryException, ArgumentException
	{

		List<GroupParticipantType> dp = getGroupRoleParticipants(group);
		return deleteParticipants(dp.toArray(new GroupParticipantType[0]), group.getOrganizationId());
	}

	public GroupParticipantType newGroupRoleParticipation(BaseRoleType role, BaseGroupType group) throws ArgumentException
	{
		return newGroupRoleParticipation(role, group, null, AffectEnumType.UNKNOWN);
	}
	public GroupParticipantType newGroupRoleParticipation(BaseRoleType role, BaseGroupType group,BasePermissionType permission,AffectEnumType affectType) throws ArgumentException
	{
		return (GroupParticipantType)newParticipant(role, group, ParticipantEnumType.GROUP, permission, affectType);
	}


	public List<UserGroupType> getGroupsInRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<GroupParticipantType> ap = getGroupRoleParticipations(role);
		return getGroupListFromParticipations(ap.toArray(new GroupParticipantType[0]), role.getOrganizationId());
	}
	public List<GroupParticipantType> getGroupRoleParticipants(BaseGroupType group) throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipantMatch(group,ParticipantEnumType.GROUP), group.getOrganizationId());
		return convertList(dtlist);
	}
	public List<GroupParticipantType> getGroupRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipationMatch(role,ParticipantEnumType.GROUP), role.getOrganizationId());
		return convertList(dtlist);
	}

	public List<GroupParticipantType> getGroupRoleParticipants(
		BaseRoleType role, 
		BaseGroupType account
	)  throws FactoryException, ArgumentException
	{
		return getGroupRoleParticipants(role, account, null, AffectEnumType.UNKNOWN);
	}
	public List<GroupParticipantType> getGroupRoleParticipants(
		BaseRoleType role,
		BaseGroupType account,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws FactoryException, ArgumentException
	{
		List<NameIdType> list = getParticipants(role, account, ParticipantEnumType.GROUP, permission, affectType);
		return convertList(list);

	}
	public GroupParticipantType getGroupRoleParticipant(BaseRoleType role, BaseGroupType account)  throws ArgumentException, FactoryException
	{
		return getGroupRoleParticipant(role, account, null, AffectEnumType.UNKNOWN);
	} 
	public GroupParticipantType getGroupRoleParticipant(
		BaseRoleType role,
		BaseGroupType account,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws ArgumentException, FactoryException
	{
		return getParticipant(role, account, ParticipantEnumType.GROUP, permission, affectType);
	}
	public boolean getIsGroupInRole(BaseRoleType role, BaseGroupType account)  throws ArgumentException, FactoryException
	{
		return (getGroupRoleParticipant(role, account) != null);
	}
	public boolean getIsGroupInRole(BaseRoleType role, BaseGroupType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		return (getGroupRoleParticipant(role, account,permission, affectType) != null);
	}

}
