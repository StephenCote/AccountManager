package org.cote.accountmanager.data.factory;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.RoleParticipantType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;

public class DataParticipationFactory extends ParticipationFactory {
	public DataParticipationFactory(){
		super(ParticipationEnumType.DATA, "dataparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.DATAPARTICIPATION;
	}
	
	public boolean deleteRoleDataParticipant(DataType data, BaseRoleType role) throws ArgumentException, FactoryException
	{
		return deleteRoleDataParticipant(data, role, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteRoleDataParticipant(DataType data, BaseRoleType role, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		RoleParticipantType dp = getRoleDataParticipant(data, role, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}

	public boolean deleteRoleParticipations(AccountRoleType role) throws FactoryException, ArgumentException
	{

		List<RoleParticipantType> dp = getRoleParticipants(role);
		return deleteParticipants(dp.toArray(new RoleParticipantType[0]), role.getOrganization());
	}

	public boolean deleteAccountDataParticipant(DataType data, AccountType account) throws ArgumentException, FactoryException
	{
		return deleteAccountDataParticipant(data, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteAccountDataParticipant(DataType data, AccountType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		AccountParticipantType dp = getAccountDataParticipant(data, account, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}

	public boolean deleteAccountParticipations(AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountParticipants(account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganization());
	}
	
	public boolean deleteUserDataParticipant(DataType data, UserType user) throws ArgumentException, FactoryException
	{
		return deleteUserDataParticipant(data, user, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteUserDataParticipant(DataType data, UserType user, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		UserParticipantType dp = getUserDataParticipant(data, user, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}

	public boolean deleteUserParticipations(UserType account) throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserParticipants(account);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), account.getOrganization());
	}

	public RoleParticipantType newRoleDataParticipation(
		DataType data,
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affect_type

	) throws ArgumentException
	{
		return newDataParticipation(data, role, permission, affect_type, ParticipantEnumType.ROLE);
	}
	public AccountParticipantType newAccountDataParticipation(
		DataType data,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affect_type

	) throws ArgumentException
	{
		return newDataParticipation(data, account, permission, affect_type, ParticipantEnumType.ACCOUNT);
	}
	public UserParticipantType newUserDataParticipation(
			DataType data,
			BaseRoleType role,
			BasePermissionType permission,
			AffectEnumType affect_type

		) throws ArgumentException
		{
			return newDataParticipation(data, role, permission, affect_type, ParticipantEnumType.USER);
		}
	public <T> T newDataParticipation(
		DataType data, 
		NameIdType map,
		BasePermissionType permission,
		AffectEnumType affect_type,
		ParticipantEnumType participant_type
	) throws ArgumentException
	{
		return (T)newParticipant(data, map, participant_type, permission, affect_type);

	}

	public List<RoleParticipantType> getRoleParticipants(BaseRoleType role) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(role, ParticipantEnumType.ROLE),role.getOrganization().getId()));
	}
	public List<AccountParticipantType> getAccountParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.ACCOUNT), account.getOrganization().getId()));	
	}
	public List<UserParticipantType> getUserParticipants(UserType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.USER), account.getOrganization().getId()));	
	}
	public List<AccountParticipantType> getAccountParticipations(DataType data) throws FactoryException, ArgumentException
	{
		return getDataAccountParticipations(new DataType[] { data });
	}
	public List<RoleParticipantType> getRoleParticipations(DataType data) throws FactoryException, ArgumentException
	{
		return getDataRoleParticipations(new DataType[] { data });
	}
	public List<UserParticipantType> getUserParticipations(DataType data) throws FactoryException, ArgumentException
	{
		return getDataUserParticipations(new DataType[] { data });
	}
	public List<AccountParticipantType> getDataAccountParticipations(DataType[] data) throws FactoryException, ArgumentException
	{
		return convertList(getDataParticipations(data, ParticipantEnumType.ACCOUNT));
	}
	public List<UserParticipantType> getDataUserParticipations(DataType[] data) throws FactoryException, ArgumentException
	{
		return convertList(getDataParticipations(data, ParticipantEnumType.USER));
	}
	public List<RoleParticipantType> getDataRoleParticipations(DataType[] data) throws FactoryException, ArgumentException
	{
		return convertList(getDataParticipations(data, ParticipantEnumType.ROLE));
	}
	public List<NameIdType> getDataParticipations(DataType[] data, ParticipantEnumType participant_type) throws FactoryException, ArgumentException
	{

		if (data.length == 0) return new ArrayList<NameIdType>();
		OrganizationType org = data[0].getOrganization();

		List<QueryField> matches = new ArrayList<QueryField>();
		matches.add(QueryFields.getFieldParticipantType(participant_type));

		matches.add(QueryFields.getFieldParticipationIds(data));
		return getByField(matches.toArray(new QueryField[0]), org.getId());

	}

	public List<RoleParticipantType> getRoleDataParticipants(
		DataType data,
		BaseRoleType role
	) throws FactoryException, ArgumentException
	{
		return getRoleDataParticipants(data, role, null, AffectEnumType.UNKNOWN);
	}
	public List<RoleParticipantType> getRoleDataParticipants(
		DataType data,
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(data, role, ParticipantEnumType.ROLE, permission,affect_type));
	}
	public RoleParticipantType getRoleDataParticipant(DataType data, BaseRoleType role) throws ArgumentException, FactoryException
	{
		return getRoleDataParticipant(data, role, null, AffectEnumType.UNKNOWN);
	}
	public RoleParticipantType getRoleDataParticipant(
		DataType data,
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException, FactoryException
	{
		return getParticipant(data, role, ParticipantEnumType.ROLE, permission, affect_type);
	}




	public List<AccountParticipantType> getAccountDataParticipants(
		DataType data,
		AccountType account
	) throws FactoryException, ArgumentException
	{
		return getAccountDataParticipants(data, account, null, AffectEnumType.UNKNOWN);
	}
	public List<AccountParticipantType> getAccountDataParticipants(
		DataType data,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(data, account, ParticipantEnumType.ACCOUNT, permission, affect_type));
	}
	
	public AccountParticipantType getAccountDataParticipant(DataType data, AccountType account) throws ArgumentException, FactoryException
	{
		return getAccountDataParticipant(data, account, null, AffectEnumType.UNKNOWN);
	}
	public AccountParticipantType getAccountDataParticipant(
		DataType data,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException, FactoryException
	{
		return getParticipant(data, account, ParticipantEnumType.ACCOUNT, permission, affect_type);

	}
	
	public List<UserParticipantType> getUserDataParticipants(
			DataType data,
			UserType user
		) throws FactoryException, ArgumentException
	{
		return getUserDataParticipants(data, user, null, AffectEnumType.UNKNOWN);
	}
	public List<UserParticipantType> getUserDataParticipants(
		DataType data,
		UserType user,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(data, user, ParticipantEnumType.USER, permission,affect_type));
	}
	public UserParticipantType getUserDataParticipant(DataType data, UserType user) throws ArgumentException, FactoryException
	{
		return getUserDataParticipant(data, user, null, AffectEnumType.UNKNOWN);
	}
	public UserParticipantType getUserDataParticipant(
		DataType data,
		UserType user,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException, FactoryException
	{
		return getParticipant(data, user, ParticipantEnumType.USER, permission, affect_type);
	}
}
