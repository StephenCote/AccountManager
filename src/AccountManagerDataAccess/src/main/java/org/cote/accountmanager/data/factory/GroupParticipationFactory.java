/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BucketGroupType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.RoleParticipantType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;

public class GroupParticipationFactory extends ParticipationFactory {
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.GROUPPARTICIPATION, GroupParticipationFactory.class); }
	public GroupParticipationFactory(){
		super(ParticipationEnumType.GROUP, "groupparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.GROUPPARTICIPATION;
		permissionPrefix = "Group";
		defaultPermissionType = PermissionEnumType.GROUP;
	}
	
	public boolean deleteRoleGroupParticipant(BaseGroupType group, BaseRoleType role) throws ArgumentException, FactoryException
	{
		return deleteRoleGroupParticipant(group, role, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteRoleGroupParticipant(BaseGroupType group, BaseRoleType role, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(group.getGroupType() != GroupEnumType.PERSON && group.getGroupType() != GroupEnumType.ACCOUNT && group.getGroupType() != GroupEnumType.USER) throw new FactoryException("Can only delete user and account group participants");
		RoleParticipantType dp = getRoleGroupParticipant(group, role, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}
	public boolean deletePersonGroupParticipant(BaseGroupType group, PersonType person) throws FactoryException, ArgumentException
	{
		return deletePersonGroupParticipant(group, person, null, AffectEnumType.UNKNOWN);
	}
	public boolean deletePersonGroupParticipant(BaseGroupType group, PersonType person, BasePermissionType permission, AffectEnumType affect_type) throws FactoryException, ArgumentException
	{
		PersonParticipantType dp = getGroupParticipant(group, person, ParticipantEnumType.PERSON, permission, affect_type);
		if (dp == null) return true;
		removeFromCache(dp);
		return delete(dp);
	}
	public boolean deleteAccountGroupParticipant(BaseGroupType group, AccountType account) throws FactoryException, ArgumentException
	{
		return deleteAccountGroupParticipant(group, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteAccountGroupParticipant(BaseGroupType group, AccountType account, BasePermissionType permission, AffectEnumType affect_type) throws FactoryException, ArgumentException
	{
		AccountParticipantType dp = getGroupParticipant(group, account, ParticipantEnumType.ACCOUNT, permission, affect_type);
		if (dp == null) return true;
		removeFromCache(dp);
		return delete(dp);
	}
	public boolean deleteUserGroupParticipant(BaseGroupType group, UserType user) throws FactoryException, ArgumentException
	{
		return deleteUserGroupParticipant(group, user, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteUserGroupParticipant(BaseGroupType group, UserType user, BasePermissionType permission, AffectEnumType affect_type) throws FactoryException, ArgumentException
	{
		AccountParticipantType dp = getGroupParticipant(group, user, ParticipantEnumType.ACCOUNT, permission, affect_type);
		if (dp == null) return true;
		removeFromCache(dp);
		return delete(dp);
	}
	public boolean deleteRoleGroupParticipants(BaseGroupType group, BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> dp = getRoleGroupParticipants(group,role);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), role.getOrganizationId());
	}
	public boolean deleteGroupGroupParticipants(BaseGroupType group, BaseGroupType member) throws FactoryException, ArgumentException
	{

		List<GroupParticipantType> dp = getGroupGroupParticipants(group, member);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), group.getOrganizationId());
	}
	public boolean deleteAccountGroupParticipants(AccountGroupType group, AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountGroupParticipants(group, account);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), group.getOrganizationId());
	}
	public boolean deletePersonGroupParticipants(PersonGroupType group, PersonType person) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> dp = getPersonGroupParticipants(group, person);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), group.getOrganizationId());
	}

	public boolean deleteUserGroupParticipants(UserGroupType group, UserType user) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> dp = getUserGroupParticipants(group, user);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), group.getOrganizationId());
	}	
	public boolean deleteRoleGroupParticipations(BaseRoleType role) throws FactoryException, ArgumentException
	{

		List<RoleParticipantType> dp = getRoleGroupParticipants(role);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), role.getOrganizationId());
	}
	public boolean deletePersonGroupParticipations(PersonType person) throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getPersonGroupParticipants(person);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), person.getOrganizationId());
	}
	public boolean deleteAccountGroupParticipations(AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountGroupParticipants(account);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), account.getOrganizationId());
	}
	public boolean deleteUserGroupParticipations(UserType user) throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserGroupParticipants(user);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), user.getOrganizationId());
	}
	public boolean deleteDataGroupParticipations(DataType data) throws FactoryException, ArgumentException
	{

		List<DataParticipantType> dp = getDataGroupParticipants(data);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), data.getOrganizationId());
	}
	/*
	/// TODO: Deprecated
	public boolean deleteDataGroupParticipationsForAccount(AccountType account) throws FactoryException
	{
		long[] data_ids = ParticipationUtil.getDataFromGroupForAccount(account);
		///return deleteParts(data_ids, "participantid", account.getOrganizationId());
		return deleteParticipants(data_ids, account.getOrganizationId());
	}
	*/
	public boolean deleteDataGroupParticipants(BucketGroupType group, DataType data) throws FactoryException, ArgumentException
	{
		List<DataParticipantType> dp = getDataGroupParticipants(group, data);
		if (dp.isEmpty()) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), data.getOrganizationId());
	}

	public DataParticipantType newDataGroupParticipation(
		BaseGroupType group,
		DataType data
	) throws ArgumentException
	{
		if (group.getGroupType() != GroupEnumType.BUCKET) throw new ArgumentException("Cannot create data/group participation on a non-bucket group");
		return (DataParticipantType)newParticipant(group, data, ParticipantEnumType.DATA, null, AffectEnumType.AGGREGATE);
	}
	public DataParticipantType newDataGroupParticipation(
		BaseGroupType group,
		DataType data,
		BasePermissionType permission,
		AffectEnumType affect_type
		) throws ArgumentException
	{
		if (group.getGroupType() != GroupEnumType.BUCKET) throw new ArgumentException("Cannot create data/group participation on a non-bucket group");
		return (DataParticipantType)newParticipant(group, data, ParticipantEnumType.DATA, permission, affect_type);
	}
	public UserParticipantType newUserGroupParticipation(
			BaseGroupType group,
			UserType User
		) throws ArgumentException
		{
			/// TODO: Verify business case for this restriction
			if (group.getGroupType() != GroupEnumType.USER && group.getGroupType() != GroupEnumType.ACCOUNT) throw new ArgumentException("Cannot create User/group participation to non-User group");
			return (UserParticipantType)newParticipant(group, User, ParticipantEnumType.USER, null, AffectEnumType.AGGREGATE);
		}
	public UserParticipantType newUserGroupParticipation(
		BaseGroupType group, 
		UserType User,
		BasePermissionType permission,
		AffectEnumType affect_type
		) throws ArgumentException
	{
		/// TODO: Verify business case for this restriction
		if (group.getGroupType() == GroupEnumType.USER || group.getGroupType() == GroupEnumType.ACCOUNT) throw new ArgumentException("Cannot create User/group participation with permission affecting an User group");
		return (UserParticipantType)newParticipant(group, User, ParticipantEnumType.USER, permission, affect_type);
	}
	public GroupParticipantType newGroupGroupParticipation(
			BaseGroupType group,
			BaseGroupType memberGroup
		) throws ArgumentException
		{
			return (GroupParticipantType)newParticipant(group, memberGroup, ParticipantEnumType.GROUP, null, AffectEnumType.AGGREGATE);
		}
		public GroupParticipantType newGroupGroupParticipation(
			BaseGroupType group, 
			BaseGroupType memberGroup,
			BasePermissionType permission,
			AffectEnumType affect_type
			) throws ArgumentException
		{
			return (GroupParticipantType)newParticipant(group, memberGroup, ParticipantEnumType.GROUP, permission, affect_type);
		}
	public AccountParticipantType newAccountGroupParticipation(
		BaseGroupType group,
		AccountType account
	) throws ArgumentException
	{
		/// TODO: Verify business case for this restriction
		if (group.getGroupType() != GroupEnumType.ACCOUNT && group.getGroupType() != GroupEnumType.USER) throw new ArgumentException("Cannot create account/group participation to non-account group");
		return (AccountParticipantType)newParticipant(group, account, ParticipantEnumType.ACCOUNT, null, AffectEnumType.AGGREGATE);
	}
	public AccountParticipantType newAccountGroupParticipation(
		BaseGroupType group, 
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affect_type
		) throws ArgumentException
	{
		/// TODO: Verify business case for this restriction
		if (group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.USER) throw new ArgumentException("Cannot create account/group participation with permission affecting an account group");
		return (AccountParticipantType)newParticipant(group, account, ParticipantEnumType.ACCOUNT, permission, affect_type);
	}
	public PersonParticipantType newPersonGroupParticipation(
			BaseGroupType group,
			PersonType person
		) throws ArgumentException
		{
			/// TODO: Verify business case for this restriction
			if (group.getGroupType() != GroupEnumType.PERSON && group.getGroupType() != GroupEnumType.USER) throw new ArgumentException("Cannot create person/group participation to non-person group");
			return (PersonParticipantType)newParticipant(group, person, ParticipantEnumType.PERSON, null, AffectEnumType.AGGREGATE);
		}
		public PersonParticipantType newPersonGroupParticipation(
			BaseGroupType group, 
			PersonType person,
			BasePermissionType permission,
			AffectEnumType affect_type
			) throws ArgumentException
		{
			/// TODO: Verify business case for this restriction
			if (group.getGroupType() == GroupEnumType.PERSON || group.getGroupType() == GroupEnumType.USER) throw new ArgumentException("Cannot create person/group participation with permission affecting an person group");
			return (PersonParticipantType)newParticipant(group, person, ParticipantEnumType.PERSON, permission, affect_type);
		}
	public RoleParticipantType newRoleGroupParticipation(
		BaseGroupType group, 
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException
	{
		return (RoleParticipantType)newParticipant(group, role, ParticipantEnumType.ROLE, permission, affect_type);
	}
/*
	public List<DataType> getDataForPerson(BaseGroupType group, PersonType person) throws FactoryException, ArgumentException
	{
		long[] data_ids = ParticipationUtil.getDataFromGroupForPerson(group, person);
		List<DataType> out_list = new ArrayList<DataType>();
		if(data_ids.length == 0) return out_list;
		return ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataListByIds(data_ids, true, person.getOrganizationId());

	}
	public List<DataType> getDataForAccount(BaseGroupType group, AccountType account) throws FactoryException, ArgumentException
	{
		long[] data_ids = ParticipationUtil.getDataFromGroupForAccount(group, account);
		List<DataType> out_list = new ArrayList<DataType>();
		if(data_ids.length == 0) return out_list;
		return ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataListByIds(data_ids, true, account.getOrganizationId());
	}
*/
	public boolean getIsDataInGroup(BaseGroupType group, DataType data) throws ArgumentException, FactoryException
	{
		return (getDataGroupParticipant(group, data) != null);
	}
	public boolean getIsDataInGroup(BaseGroupType group, DataType data, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return (getDataGroupParticipant(group, data, permission, affect_type) != null);
	}
	public List<DataParticipantType> getDataGroupParticipations(BaseGroupType group) throws FactoryException, ArgumentException
	{
		return getDataGroupParticipations(new BaseGroupType[] { group });
	}
	public List<DataParticipantType> getDataGroupParticipations(BaseGroupType[] groups) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(groups, ParticipantEnumType.DATA));
	}


	public List<DataParticipantType> getDataGroupParticipants(DataType data) throws FactoryException, ArgumentException
	{
		QueryField[] fields = QueryFields.getFieldParticipantMatch(data,  ParticipantEnumType.DATA);
		return convertList(getByField(fields, data.getOrganizationId()));
	}


	public List<DataParticipantType> getDataGroupParticipants(
		BaseGroupType group,
		DataType data
	) throws FactoryException, ArgumentException
	{
		return getDataGroupParticipants(group, data, null, AffectEnumType.UNKNOWN);
	}
	public List<DataParticipantType> getDataGroupParticipants(
		BaseGroupType group,
		DataType data,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(group, data, ParticipantEnumType.DATA, permission, affect_type));
	}
	public DataParticipantType getDataGroupParticipant(BaseGroupType group, DataType data) throws ArgumentException, FactoryException
	{
		return getDataGroupParticipant(group, data, null, AffectEnumType.UNKNOWN);
	}
	public DataParticipantType getDataGroupParticipant(
		BaseGroupType group,
		DataType data,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException, FactoryException
	{
		return getParticipant(group, data, ParticipantEnumType.DATA, permission, affect_type);
	}

	
	public boolean getIsUserInGroup(BaseGroupType group, UserType user) throws ArgumentException, FactoryException
	{
		return (getUserGroupParticipant(group, user) != null);
	}
	public boolean getIsUserInGroup(BaseGroupType group, UserType user, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return (getGroupParticipant(group, user, ParticipantEnumType.USER, permission, affect_type) != null);
	}
	
	public List<UserParticipantType> getUserGroupParticipations(BaseGroupType group) throws FactoryException, ArgumentException
	{
		return getUserGroupParticipations(new BaseGroupType[] { group });
	}
	public List<UserParticipantType> getUserGroupParticipations(BaseGroupType[] groups) throws FactoryException, ArgumentException
	{
		return convertList(getParticipations(groups, ParticipantEnumType.USER));
	}
	public List<UserParticipantType> getUserGroupParticipants(UserType User) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(User, ParticipantEnumType.USER), User.getOrganizationId()));
	}

	public List<UserParticipantType> getUserGroupParticipants(
		BaseGroupType group,
		UserType User
	) throws FactoryException, ArgumentException
	{
		return getUserGroupParticipants(group, User, null, AffectEnumType.UNKNOWN);
	}
	public List<UserParticipantType> getUserGroupParticipants(
		BaseGroupType group,
		UserType User,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		
		return convertList(getParticipants(group, User, ParticipantEnumType.USER, permission, affect_type));
	}
	public UserParticipantType getUserGroupParticipant(BaseGroupType group, UserType User) throws ArgumentException, FactoryException
	{
		return getGroupParticipant(group, User, ParticipantEnumType.USER, null, AffectEnumType.UNKNOWN);
	}

	public boolean getIsGroupInGroup(BaseGroupType group, BaseGroupType member) throws ArgumentException, FactoryException
	{
		return (getGroupGroupParticipant(group, member) != null);
	}
	public boolean getIsGroupInGroup(BaseGroupType group, BaseGroupType member, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return (getGroupParticipant(group, member, ParticipantEnumType.GROUP, permission, affect_type) != null);
	}

	public List<GroupParticipantType> getGroupGroupParticipations(BaseGroupType group) throws FactoryException, ArgumentException
	{
		return getGroupGroupParticipations(new BaseGroupType[] { group });
	}
	public List<GroupParticipantType> getGroupGroupParticipations(BaseGroupType[] groups) throws FactoryException, ArgumentException
	{
		return convertList(getParticipations(groups, ParticipantEnumType.GROUP));
	}


	public List<GroupParticipantType> getGroupGroupParticipants(BaseGroupType member) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(member, ParticipantEnumType.GROUP), member.getOrganizationId()));
	}


	public List<GroupParticipantType> getGroupGroupParticipants(
		BaseGroupType group,
		BaseGroupType member
	) throws FactoryException, ArgumentException
	{
		return getGroupGroupParticipants(group, member, null, AffectEnumType.UNKNOWN);
	}
	public List<GroupParticipantType> getGroupGroupParticipants(
		BaseGroupType group,
		BaseGroupType member,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		
		return convertList(getParticipants(group, member, ParticipantEnumType.GROUP, permission, affect_type));
	}
	public GroupParticipantType getGroupGroupParticipant(BaseGroupType group, BaseGroupType member) throws ArgumentException, FactoryException
	{
		return getGroupParticipant(group, member, ParticipantEnumType.GROUP, null, AffectEnumType.UNKNOWN);
	}
	
	
	public boolean getIsAccountInGroup(BaseGroupType group, AccountType account) throws ArgumentException, FactoryException
	{
		return (getAccountGroupParticipant(group, account) != null);
	}
	public boolean getIsAccountInGroup(BaseGroupType group, AccountType account, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return (getGroupParticipant(group, account, ParticipantEnumType.ACCOUNT, permission, affect_type) != null);
	}

	public List<AccountParticipantType> getAccountGroupParticipations(BaseGroupType group) throws FactoryException, ArgumentException
	{
		return getAccountGroupParticipations(new BaseGroupType[] { group });
	}
	public List<AccountParticipantType> getAccountGroupParticipations(BaseGroupType[] groups) throws FactoryException, ArgumentException
	{
		return convertList(getParticipations(groups, ParticipantEnumType.ACCOUNT));
	}


	public List<AccountParticipantType> getAccountGroupParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.ACCOUNT), account.getOrganizationId()));
	}


	public List<AccountParticipantType> getAccountGroupParticipants(
		BaseGroupType group,
		AccountType account
	) throws FactoryException, ArgumentException
	{
		return getAccountGroupParticipants(group, account, null, AffectEnumType.UNKNOWN);
	}
	public List<AccountParticipantType> getAccountGroupParticipants(
		BaseGroupType group,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		
		return convertList(getParticipants(group, account, ParticipantEnumType.ACCOUNT, permission, affect_type));
	}
	public AccountParticipantType getAccountGroupParticipant(BaseGroupType group, AccountType account) throws ArgumentException, FactoryException
	{
		return getGroupParticipant(group, account, ParticipantEnumType.ACCOUNT, null, AffectEnumType.UNKNOWN);
	}
	
	public boolean getIsPersonInGroup(BaseGroupType group, PersonType person) throws ArgumentException, FactoryException
	{
		return (getPersonGroupParticipant(group, person) != null);
	}
	public boolean getIsPersonInGroup(BaseGroupType group, PersonType person, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return (getGroupParticipant(group, person, ParticipantEnumType.PERSON, permission, affect_type) != null);
	}

	public List<PersonParticipantType> getPersonGroupParticipations(BaseGroupType group) throws FactoryException, ArgumentException
	{
		return getPersonGroupParticipations(new BaseGroupType[] { group });
	}
	public List<PersonParticipantType> getPersonGroupParticipations(BaseGroupType[] groups) throws FactoryException, ArgumentException
	{
		return convertList(getParticipations(groups, ParticipantEnumType.PERSON));
	}


	public List<PersonParticipantType> getPersonGroupParticipants(PersonType person) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(person, ParticipantEnumType.PERSON), person.getOrganizationId()));
	}


	public List<PersonParticipantType> getPersonGroupParticipants(
		BaseGroupType group,
		PersonType person
	) throws FactoryException, ArgumentException
	{
		return getPersonGroupParticipants(group, person, null, AffectEnumType.UNKNOWN);
	}
	public List<PersonParticipantType> getPersonGroupParticipants(
		BaseGroupType group,
		PersonType person,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		
		return convertList(getParticipants(group, person, ParticipantEnumType.PERSON, permission, affect_type));
	}
	public PersonParticipantType getPersonGroupParticipant(BaseGroupType group, PersonType person) throws ArgumentException, FactoryException
	{
		return getGroupParticipant(group, person, ParticipantEnumType.PERSON, null, AffectEnumType.UNKNOWN);
	}
	
	public <T> T getGroupParticipant(
			BaseGroupType group,
			NameIdType map,
			ParticipantEnumType type,
			BasePermissionType permission,
			AffectEnumType affect_type
		) throws ArgumentException, FactoryException
	{
		return getParticipant(group, map, type, permission, affect_type);
	}
	public List<AccountType> getAccountsInGroup(AccountGroupType group) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> ap = getAccountGroupParticipations(group);
		return getAccountListFromParticipations(ap.toArray(new AccountParticipantType[0]), group.getOrganizationId());
	}
	public List<PersonType> getPersonsInGroup(PersonGroupType group) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> ap = getPersonGroupParticipations(group);
		return getPersonListFromParticipations(ap.toArray(new PersonParticipantType[0]), group.getOrganizationId());
	}

	public List<UserType> getUsersInGroup(UserGroupType group) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> ap = getUserGroupParticipations(group);
		return getUserListFromParticipations(ap.toArray(new UserParticipantType[0]), group.getOrganizationId());
	}
	public List<BaseRoleType> getRolesInGroup(BaseGroupType group) throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> ap = getRoleGroupParticipations(group);
		return getRoleListFromParticipations(ap.toArray(new RoleParticipantType[0]), group.getOrganizationId());
	}	
	public List<RoleParticipantType> getRoleGroupParticipations(BaseGroupType group) throws FactoryException, ArgumentException
	{
		return getRoleGroupParticipations(new BaseGroupType[] { group });
	}
	public List<RoleParticipantType> getRoleGroupParticipations(BaseGroupType[] groups) throws FactoryException, ArgumentException
	{
		return convertList(getParticipations(groups, ParticipantEnumType.ROLE));

	}


	public List<RoleParticipantType> getRoleGroupParticipants(
		BaseGroupType group,
		BaseRoleType role
	) throws FactoryException, ArgumentException
	{
		return getRoleGroupParticipants(group, role, null, AffectEnumType.UNKNOWN);
	}
	public List<RoleParticipantType> getRoleGroupParticipants(
		BaseGroupType group,
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(group, role, ParticipantEnumType.ROLE, permission, affect_type));
	}

	public List<RoleParticipantType> getRoleGroupParticipants(BaseRoleType role) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(role, ParticipantEnumType.ROLE), role.getOrganizationId()));
	}

	public RoleParticipantType getRoleGroupParticipant(BaseGroupType group, BaseRoleType role) throws ArgumentException, FactoryException
	{
		return getRoleGroupParticipant(group, role, null, AffectEnumType.UNKNOWN);
	}
	public RoleParticipantType getRoleGroupParticipant(
		BaseGroupType group,
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException, FactoryException
	{
		return getParticipant(group, role, ParticipantEnumType.ROLE, permission, affect_type);
	}
}
