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

import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.RoleParticipantType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;

public class DataParticipationFactory extends ParticipationFactory {
	public DataParticipationFactory(){
		super(ParticipationEnumType.DATA, "dataparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.DATAPARTICIPATION;
		permissionPrefix = "Data";
		defaultPermissionType = PermissionEnumType.DATA;
	}
	
	public boolean deleteRoleDataParticipant(DataType data, BaseRoleType role) throws ArgumentException, FactoryException
	{
		return deleteRoleDataParticipant(data, role, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteRoleDataParticipant(DataType data, BaseRoleType role, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
	{
		RoleParticipantType dp = getRoleDataParticipant(data, role, permission, affectType);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}

	public boolean deleteRoleParticipations(AccountRoleType role) throws FactoryException, ArgumentException
	{

		List<RoleParticipantType> dp = getRoleParticipants(role);
		return deleteParticipants(dp.toArray(new RoleParticipantType[0]), role.getOrganizationId());
	}

	public boolean deleteAccountDataParticipant(DataType data, AccountType account) throws ArgumentException, FactoryException
	{
		return deleteAccountDataParticipant(data, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteAccountDataParticipant(DataType data, AccountType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		AccountParticipantType dp = getAccountDataParticipant(data, account, permission, affectType);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}

	public boolean deleteAccountParticipations(AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountParticipants(account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganizationId());
	}
	
	public boolean deleteUserDataParticipant(DataType data, UserType user) throws ArgumentException, FactoryException
	{
		return deleteUserDataParticipant(data, user, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteUserDataParticipant(DataType data, UserType user, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		UserParticipantType dp = getUserDataParticipant(data, user, permission, affectType);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}

	public boolean deleteUserParticipations(UserType account) throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserParticipants(account);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), account.getOrganizationId());
	}

	public RoleParticipantType newRoleDataParticipation(
		DataType data,
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affectType

	) throws ArgumentException
	{
		return newDataParticipation(data, role, permission, affectType, ParticipantEnumType.ROLE);
	}
	public AccountParticipantType newAccountDataParticipation(
		DataType data,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affectType

	) throws ArgumentException
	{
		return newDataParticipation(data, account, permission, affectType, ParticipantEnumType.ACCOUNT);
	}
	public UserParticipantType newUserDataParticipation(
			DataType data,
			BaseRoleType role,
			BasePermissionType permission,
			AffectEnumType affectType

		) throws ArgumentException
		{
			return newDataParticipation(data, role, permission, affectType, ParticipantEnumType.USER);
		}
	
	@SuppressWarnings("unchecked")
	public <T> T newDataParticipation(
		DataType data, 
		NameIdType map,
		BasePermissionType permission,
		AffectEnumType affectType,
		ParticipantEnumType participantType
	) throws ArgumentException
	{
		return (T)newParticipant(data, map, participantType, permission, affectType);

	}
	public List<BaseRoleType> getRolesForData(DataType data) throws FactoryException, ArgumentException
	{
		List<RoleParticipantType> ap = getRoleParticipations(data);
		return getRoleListFromParticipations(ap.toArray(new RoleParticipantType[0]), data.getOrganizationId());
	}	
	public List<RoleParticipantType> getRoleParticipants(BaseRoleType role) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(role, ParticipantEnumType.ROLE),role.getOrganizationId()));
	}
	public List<AccountParticipantType> getAccountParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.ACCOUNT), account.getOrganizationId()));	
	}
	public List<UserParticipantType> getUserParticipants(UserType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.USER), account.getOrganizationId()));	
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
	public List<NameIdType> getDataParticipations(DataType[] data, ParticipantEnumType participantType) throws FactoryException, ArgumentException
	{

		if (data.length == 0) return new ArrayList<>();
		long org = data[0].getOrganizationId();

		List<QueryField> matches = new ArrayList<>();
		matches.add(QueryFields.getFieldParticipantType(participantType));

		matches.add(QueryFields.getFieldParticipationIds(data));
		return getByField(matches.toArray(new QueryField[0]), org);

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
		AffectEnumType affectType
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(data, role, ParticipantEnumType.ROLE, permission,affectType));
	}
	public RoleParticipantType getRoleDataParticipant(DataType data, BaseRoleType role) throws ArgumentException, FactoryException
	{
		return getRoleDataParticipant(data, role, null, AffectEnumType.UNKNOWN);
	}
	public RoleParticipantType getRoleDataParticipant(
		DataType data,
		BaseRoleType role,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws ArgumentException, FactoryException
	{
		return getParticipant(data, role, ParticipantEnumType.ROLE, permission, affectType);
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
		AffectEnumType affectType
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(data, account, ParticipantEnumType.ACCOUNT, permission, affectType));
	}
	
	public AccountParticipantType getAccountDataParticipant(DataType data, AccountType account) throws ArgumentException, FactoryException
	{
		return getAccountDataParticipant(data, account, null, AffectEnumType.UNKNOWN);
	}
	public AccountParticipantType getAccountDataParticipant(
		DataType data,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws ArgumentException, FactoryException
	{
		return getParticipant(data, account, ParticipantEnumType.ACCOUNT, permission, affectType);

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
		AffectEnumType affectType
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(data, user, ParticipantEnumType.USER, permission,affectType));
	}
	public UserParticipantType getUserDataParticipant(DataType data, UserType user) throws ArgumentException, FactoryException
	{
		return getUserDataParticipant(data, user, null, AffectEnumType.UNKNOWN);
	}
	public UserParticipantType getUserDataParticipant(
		DataType data,
		UserType user,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws ArgumentException, FactoryException
	{
		return getParticipant(data, user, ParticipantEnumType.USER, permission, affectType);
	}
}
