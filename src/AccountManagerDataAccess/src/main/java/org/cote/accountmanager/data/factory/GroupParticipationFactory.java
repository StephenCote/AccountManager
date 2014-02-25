package org.cote.accountmanager.data.factory;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
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
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.RoleParticipantType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;

public class GroupParticipationFactory extends ParticipationFactory {
	public GroupParticipationFactory(){
		super(ParticipationEnumType.GROUP, "groupparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.GROUPPARTICIPATION;
	}
	
	public boolean deleteRoleGroupParticipant(BaseGroupType group, BaseRoleType role) throws ArgumentException, FactoryException
	{
		return deleteRoleGroupParticipant(group, role, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteRoleGroupParticipant(BaseGroupType group, BaseRoleType role, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(group.getGroupType() != GroupEnumType.ACCOUNT && group.getGroupType() != GroupEnumType.USER) throw new FactoryException("Can only delete user and account group participants");
		RoleParticipantType dp = getRoleGroupParticipant(group, role, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
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
		return deleteParticipant(dp);
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
		return deleteParticipant(dp);
	}
	public boolean deleteRoleGroupParticipants(BaseGroupType group, BaseRoleType role) throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> dp = getRoleGroupParticipants(group,role);
		if (dp.size() == 0) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), role.getOrganization());
	}
	public boolean deleteAccountGroupParticipants(AccountGroupType group, AccountType account) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> dp = getAccountGroupParticipants(group, account);
		if (dp.size() == 0) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), group.getOrganization());
	}
	public boolean deleteUserGroupParticipants(UserGroupType group, UserType user) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> dp = getUserGroupParticipants(group, user);
		if (dp.size() == 0) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), group.getOrganization());
	}	
	public boolean deleteRoleGroupParticipations(BaseRoleType role) throws FactoryException, ArgumentException
	{

		List<RoleParticipantType> dp = getRoleGroupParticipants(role);
		if (dp.size() == 0) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), role.getOrganization());
	}
	public boolean deleteAccountGroupParticipations(AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountGroupParticipants(account);
		if (dp.size() == 0) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), account.getOrganization());
	}
	public boolean deleteUserGroupParticipations(UserType user) throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserGroupParticipants(user);
		if (dp.size() == 0) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), user.getOrganization());
	}
	public boolean deleteDataGroupParticipations(DataType data) throws FactoryException, ArgumentException
	{

		List<DataParticipantType> dp = getDataGroupParticipants(data);
		if (dp.size() == 0) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), data.getOrganization());
	}
	public boolean deleteDataGroupParticipationsForAccount(AccountType account) throws FactoryException
	{
		long[] data_ids = ParticipationUtil.getDataFromGroupForAccount(account);
		///return deleteParts(data_ids, "participantid", account.getOrganization());
		return deleteParticipants(data_ids, account.getOrganization());
	}
	public boolean deleteDataGroupParticipants(BucketGroupType group, DataType data) throws FactoryException, ArgumentException
	{
		List<DataParticipantType> dp = getDataGroupParticipants(group, data);
		if (dp.size() == 0) return true;
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), data.getOrganization());
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
	public RoleParticipantType newRoleGroupParticipation(
		BaseGroupType group, 
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException
	{
		return (RoleParticipantType)newParticipant(group, role, ParticipantEnumType.ROLE, permission, affect_type);
	}

	public List<DataType> getDataForAccount(BaseGroupType group, AccountType account) throws FactoryException, ArgumentException
	{
		long[] data_ids = ParticipationUtil.getDataFromGroupForAccount(group, account);
		List<DataType> out_list = new ArrayList<DataType>();
		if(data_ids.length == 0) return out_list;
		return Factories.getDataFactory().getDataListByIds(data_ids, true, account.getOrganization());

	}
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
		return convertList(getByField(fields, data.getOrganization().getId()));
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
		return convertList(getByField(QueryFields.getFieldParticipantMatch(User, ParticipantEnumType.USER), User.getOrganization().getId()));
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
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.ACCOUNT), account.getOrganization().getId()));
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
		return getAccountListFromParticipations(ap.toArray(new UserParticipantType[0]), group.getOrganization());
	}

	public List<UserType> getUsersInGroup(UserGroupType group) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> ap = getUserGroupParticipations(group);
		return getUserListFromParticipations(ap.toArray(new UserParticipantType[0]), group.getOrganization());
	}
	public List<BaseRoleType> getRolesInGroup(BaseGroupType group) throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> ap = getRoleGroupParticipations(group);
		return getRoleListFromParticipations(ap.toArray(new RoleParticipantType[0]), group.getOrganization());
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
		return convertList(getByField(QueryFields.getFieldParticipantMatch(role, ParticipantEnumType.ROLE), role.getOrganization().getId()));
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
