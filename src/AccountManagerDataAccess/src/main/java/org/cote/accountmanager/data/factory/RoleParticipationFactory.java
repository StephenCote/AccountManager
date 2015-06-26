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

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.RoleParticipantType;

public class RoleParticipationFactory extends ParticipationFactory {
	public RoleParticipationFactory(){
		super(ParticipationEnumType.ROLE, "roleparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.ROLEPARTICIPATION;
	}
	public boolean deleteRoleRoleParticipant(BaseRoleType role, BaseRoleType child_role) throws FactoryException, ArgumentException
	{
		RoleParticipantType dp = getRoleRoleParticipant(role, child_role);
		if (dp == null) return true;
		return deleteParticipant(dp);

	}
	public boolean deleteRoleParticipations(BaseRoleType child_role) throws FactoryException, ArgumentException
	{

		List<RoleParticipantType> dp = getRoleRoleParticipants(child_role);
		return deleteParticipants(dp.toArray(new RoleParticipantType[0]), child_role.getOrganization());
	}

	public RoleParticipantType newRoleRoleParticipation(BaseRoleType role, BaseRoleType child_role) throws ArgumentException
	{
		return (RoleParticipantType)newParticipant(role, child_role, ParticipantEnumType.ROLE, null, AffectEnumType.UNKNOWN);
	}
	
	public List<BaseRoleType> getRoleRoles(BaseRoleType child_role)  throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> list = getRoleRoleParticipants(child_role);

		if (list.size() == 0) return new ArrayList<BaseRoleType>();
/*
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < list.size(); i++)
		{
			if (i > 0) buff.append(",");
			buff.append(list.get(i).getParticipationId());
		}

		QueryField match = new QueryField(SqlDataEnumType.INTEGER, "id", buff.toString());
		match.setComparator(ComparatorEnumType.IN);
*/
		QueryField match = QueryFields.getFieldParticipationIds(list.toArray(new RoleParticipantType[0]));
		return Factories.getRoleFactory().getRoles(match, child_role.getOrganization());
	}
	public List<BaseRoleType> getRolesInRole(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> ap = getRoleRoleParticipations(role);
		return getRoleListFromParticipations(ap.toArray(new RoleParticipantType[0]), role.getOrganization());
	}
	public List<RoleParticipantType> getRoleRoleParticipants(BaseRoleType child_role)  throws FactoryException, ArgumentException
	{
		List<QueryField> matches = new ArrayList<QueryField>();
		matches.add(QueryFields.getFieldParticipantType(ParticipantEnumType.ROLE));
		matches.add(QueryFields.getFieldParticipantId(child_role));
		List<NameIdType> dtlist = getByField(matches.toArray(new QueryField[0]), child_role.getOrganization().getId());
		return convertList(dtlist);
	}
	public List<RoleParticipantType> getRoleRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<QueryField> matches = new ArrayList<QueryField>();
		matches.add(QueryFields.getFieldParticipantType(ParticipantEnumType.ROLE));
		matches.add(QueryFields.getFieldParticipationId(role));
		List<NameIdType> list = getByField(matches.toArray(new QueryField[0]), role.getOrganization().getId());
		return convertList(list);
	}

	public RoleParticipantType getRoleRoleParticipant(BaseRoleType role, BaseRoleType child_role) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(child_role), QueryFields.getFieldParticipantType(ParticipantEnumType.ROLE), QueryFields.getFieldParticipationId(role) }, role.getOrganization().getId());
		if (list.size() == 0) return null;
		return (RoleParticipantType)list.get(0);
	}
	public boolean getIsRoleInRole(BaseRoleType role, BaseRoleType child_role) throws FactoryException, ArgumentException
	{
		return (getRoleRoleParticipant(role, child_role) != null);
	}

	// User
	//
	
	public boolean deleteUserRoleParticipant(UserRoleType role, UserType account)  throws ArgumentException, FactoryException
	{
		return deleteUserRoleParticipant(role, account, null,AffectEnumType.UNKNOWN);
	}
	public boolean deleteUserRoleParticipant(UserRoleType role, UserType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		UserParticipantType dp = getUserRoleParticipant(role, account, permission, affect_type);
		if (dp == null) return true;

		removeParticipantFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deleteUserRoleParticipants(UserRoleType role, UserType account) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> dp = getUserRoleParticipants(role, account);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), account.getOrganization());
	}

	public boolean deleteUserParticipations(UserType account)  throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserRoleParticipants(account);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), account.getOrganization());
	}

	public UserParticipantType newUserRoleParticipation(BaseRoleType role, UserType account) throws ArgumentException
	{
		return newUserRoleParticipation(role, account, null, AffectEnumType.UNKNOWN);
	}
	public UserParticipantType newUserRoleParticipation(BaseRoleType role, UserType account,BasePermissionType permission,AffectEnumType affect_type) throws ArgumentException
	{
		return (UserParticipantType)newParticipant(role, account, ParticipantEnumType.USER, permission, affect_type);
	}


	public List<UserRoleType> getUserRoles(UserType account) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> list = getUserRoleParticipants(account);
		if(list.size() == 0) return new ArrayList<UserRoleType>();
		QueryField match = QueryFields.getFieldParticipationIds(list.toArray(new UserParticipantType[0]));
		return Factories.getRoleFactory().getUserRoles(match, account.getOrganization());
	}
	public List<UserType> getUsersInRole(UserRoleType role) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> ap = getUserRoleParticipations(role);
		return getUserListFromParticipations(ap.toArray(new UserParticipantType[0]), role.getOrganization());
	}
	public List<UserParticipantType> getUserRoleParticipants(UserType account) throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipantMatch(account,ParticipantEnumType.USER), account.getOrganization().getId());
		return convertList(dtlist);
	}
	public List<UserParticipantType> getUserRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipationMatch(role,ParticipantEnumType.USER), role.getOrganization().getId());
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
		AffectEnumType affect_type
	)  throws FactoryException, ArgumentException
	{
		List<NameIdType> list = getParticipants(role, account, ParticipantEnumType.USER, permission, affect_type);
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
		AffectEnumType affect_type
	)  throws ArgumentException, FactoryException
	{
		return getParticipant(role, account, ParticipantEnumType.USER, permission, affect_type);
	}
	public boolean getIsUserInRole(BaseRoleType role, UserType account)  throws ArgumentException, FactoryException
	{
		return (getUserRoleParticipant(role, account) != null);
	}
	public boolean getIsUserInRole(BaseRoleType role, UserType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		return (getUserRoleParticipant(role, account,permission, affect_type) != null);
	}
	
	// Person
	//

	public boolean deletePersonRoleParticipant(PersonRoleType role, PersonType person)  throws ArgumentException, FactoryException
	{
		return deletePersonRoleParticipant(role, person, null,AffectEnumType.UNKNOWN);
	}
	public boolean deletePersonRoleParticipant(PersonRoleType role, PersonType person, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		PersonParticipantType dp = getPersonRoleParticipant(role, person, permission, affect_type);
		if (dp == null) return true;

		removeParticipantFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deletePersonRoleParticipants(PersonRoleType role, PersonType person) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> dp = getPersonRoleParticipants(role, person);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), person.getOrganization());
	}

	public boolean deletePersonParticipations(PersonType person)  throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getPersonRoleParticipants(person);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), person.getOrganization());
	}

	public PersonParticipantType newPersonRoleParticipation(BaseRoleType role, PersonType person) throws ArgumentException
	{
		return newPersonRoleParticipation(role, person, null, AffectEnumType.UNKNOWN);
	}
	public PersonParticipantType newPersonRoleParticipation(BaseRoleType role, PersonType person,BasePermissionType permission,AffectEnumType affect_type) throws ArgumentException
	{
		return (PersonParticipantType)newParticipant(role, person, ParticipantEnumType.PERSON, permission, affect_type);
	}


	public List<PersonRoleType> getPersonRoles(PersonType person) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> list = getPersonRoleParticipants(person);
		QueryField match = QueryFields.getFieldParticipationIds(list.toArray(new PersonParticipantType[0]));
		return Factories.getRoleFactory().getPersonRoles(match, person.getOrganization());
	}
	public List<PersonType> getPersonsInRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> ap = getPersonRoleParticipations(role);
		return getPersonListFromParticipations(ap.toArray(new PersonParticipantType[0]), role.getOrganization());
	}
	public List<PersonParticipantType> getPersonRoleParticipants(PersonType person) throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipantMatch(person,ParticipantEnumType.PERSON), person.getOrganization().getId());
		return convertList(dtlist);
	}
	public List<PersonParticipantType> getPersonRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipationMatch(role,ParticipantEnumType.PERSON), role.getOrganization().getId());
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
		AffectEnumType affect_type
	)  throws FactoryException, ArgumentException
	{
		List<NameIdType> list = getParticipants(role, person, ParticipantEnumType.PERSON, permission, affect_type);
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
		AffectEnumType affect_type
	)  throws ArgumentException, FactoryException
	{
		return getParticipant(role, person, ParticipantEnumType.PERSON, permission, affect_type);
	}
	public boolean getIsPersonInRole(BaseRoleType role, PersonType person)  throws ArgumentException, FactoryException
	{
		return (getPersonRoleParticipant(role, person) != null);
	}
	public boolean getIsPersonInRole(BaseRoleType role, PersonType person, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		return (getPersonRoleParticipant(role, person,permission, affect_type) != null);
	}
	
	// Account
	//

	public boolean deleteAccountRoleParticipant(AccountRoleType role, AccountType account)  throws ArgumentException, FactoryException
	{
		return deleteAccountRoleParticipant(role, account, null,AffectEnumType.UNKNOWN);
	}
	public boolean deleteAccountRoleParticipant(AccountRoleType role, AccountType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		AccountParticipantType dp = getAccountRoleParticipant(role, account, permission, affect_type);
		if (dp == null) return true;

		removeParticipantFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deleteAccountRoleParticipants(AccountRoleType role, AccountType account) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> dp = getAccountRoleParticipants(role, account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganization());
	}

	public boolean deleteAccountParticipations(AccountType account)  throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountRoleParticipants(account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganization());
	}

	public AccountParticipantType newAccountRoleParticipation(BaseRoleType role, AccountType account) throws ArgumentException
	{
		return newAccountRoleParticipation(role, account, null, AffectEnumType.UNKNOWN);
	}
	public AccountParticipantType newAccountRoleParticipation(BaseRoleType role, AccountType account,BasePermissionType permission,AffectEnumType affect_type) throws ArgumentException
	{
		return (AccountParticipantType)newParticipant(role, account, ParticipantEnumType.ACCOUNT, permission, affect_type);
	}


	public List<AccountRoleType> getAccountRoles(AccountType account) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> list = getAccountRoleParticipants(account);
		QueryField match = QueryFields.getFieldParticipationIds(list.toArray(new AccountParticipantType[0]));
		return Factories.getRoleFactory().getAccountRoles(match, account.getOrganization());
	}
	public List<AccountType> getAccountsInRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> ap = getAccountRoleParticipations(role);
		return getAccountListFromParticipations(ap.toArray(new AccountParticipantType[0]), role.getOrganization());
	}
	public List<AccountParticipantType> getAccountRoleParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipantMatch(account,ParticipantEnumType.ACCOUNT), account.getOrganization().getId());
		return convertList(dtlist);
	}
	public List<AccountParticipantType> getAccountRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipationMatch(role,ParticipantEnumType.ACCOUNT), role.getOrganization().getId());
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
		AffectEnumType affect_type
	)  throws FactoryException, ArgumentException
	{
		List<NameIdType> list = getParticipants(role, account, ParticipantEnumType.ACCOUNT, permission, affect_type);
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
		AffectEnumType affect_type
	)  throws ArgumentException, FactoryException
	{
		return getParticipant(role, account, ParticipantEnumType.ACCOUNT, permission, affect_type);
	}
	public boolean getIsAccountInRole(BaseRoleType role, AccountType account)  throws ArgumentException, FactoryException
	{
		return (getAccountRoleParticipant(role, account) != null);
	}
	public boolean getIsAccountInRole(BaseRoleType role, AccountType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		return (getAccountRoleParticipant(role, account,permission, affect_type) != null);
	}
	
	/// Group
	public boolean deleteGroupRoleParticipant(BaseRoleType role, BaseGroupType group)  throws ArgumentException, FactoryException
	{
		return deleteGroupRoleParticipant(role, group, null,AffectEnumType.UNKNOWN);
	}
	public boolean deleteGroupRoleParticipant(BaseRoleType role, BaseGroupType group, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		GroupParticipantType dp = getGroupRoleParticipant(role, group, permission, affect_type);
		if (dp == null) return true;

		removeParticipantFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deleteGroupRoleParticipants(BaseRoleType role, BaseGroupType account) throws FactoryException, ArgumentException
	{
		List<GroupParticipantType> dp = getGroupRoleParticipants(role, account);
		return deleteParticipants(dp.toArray(new GroupParticipantType[0]), account.getOrganization());
	}

	public boolean deleteGroupParticipations(UserGroupType group)  throws FactoryException, ArgumentException
	{

		List<GroupParticipantType> dp = getGroupRoleParticipants(group);
		return deleteParticipants(dp.toArray(new GroupParticipantType[0]), group.getOrganization());
	}

	public GroupParticipantType newGroupRoleParticipation(BaseRoleType role, BaseGroupType group) throws ArgumentException
	{
		return newGroupRoleParticipation(role, group, null, AffectEnumType.UNKNOWN);
	}
	public GroupParticipantType newGroupRoleParticipation(BaseRoleType role, BaseGroupType group,BasePermissionType permission,AffectEnumType affect_type) throws ArgumentException
	{
		return (GroupParticipantType)newParticipant(role, group, ParticipantEnumType.GROUP, permission, affect_type);
	}


	public List<UserGroupType> getGroupsInRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<GroupParticipantType> ap = getGroupRoleParticipations(role);
		return getGroupListFromParticipations(ap.toArray(new GroupParticipantType[0]), role.getOrganization());
	}
	public List<GroupParticipantType> getGroupRoleParticipants(BaseGroupType group) throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipantMatch(group,ParticipantEnumType.GROUP), group.getOrganization().getId());
		return convertList(dtlist);
	}
	public List<GroupParticipantType> getGroupRoleParticipations(BaseRoleType role)  throws FactoryException, ArgumentException
	{
		List<NameIdType> dtlist = getByField(QueryFields.getFieldParticipationMatch(role,ParticipantEnumType.GROUP), role.getOrganization().getId());
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
		AffectEnumType affect_type
	)  throws FactoryException, ArgumentException
	{
		List<NameIdType> list = getParticipants(role, account, ParticipantEnumType.GROUP, permission, affect_type);
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
		AffectEnumType affect_type
	)  throws ArgumentException, FactoryException
	{
		return getParticipant(role, account, ParticipantEnumType.GROUP, permission, affect_type);
	}
	public boolean getIsGroupInRole(BaseRoleType role, BaseGroupType account)  throws ArgumentException, FactoryException
	{
		return (getGroupRoleParticipant(role, account) != null);
	}
	public boolean getIsGroupInRole(BaseRoleType role, BaseGroupType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		return (getGroupRoleParticipant(role, account,permission, affect_type) != null);
	}

}
