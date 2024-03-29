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
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;


public class PersonParticipationFactory extends ParticipationFactory {
	public PersonParticipationFactory(){
		super(ParticipationEnumType.PERSON, "personparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.PERSONPARTICIPATION;
		permissionPrefix = "Person";
		defaultPermissionType = PermissionEnumType.PERSON;
	}
	
	public boolean deletePartnerPersonParticipant(PersonType person, PersonType account) throws ArgumentException, FactoryException
	{
		return deletePartnerPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deletePartnerPersonParticipant(PersonType person, PersonType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		PersonParticipantType dp = getPartnerPersonParticipant(person, account, permission, affectType);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}
	public boolean deletePersonParticipations(PersonType account) throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getPersonParticipants(account);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), account.getOrganizationId());
	}
	
	public boolean deleteDependentPersonParticipant(PersonType person, PersonType account) throws ArgumentException, FactoryException
	{
		return deleteDependentPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteDependentPersonParticipant(PersonType person, PersonType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		PersonParticipantType dp = getDependentPersonParticipant(person, account, permission, affectType);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}
	public boolean deleteDependentParticipations(PersonType account) throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getDependentParticipants(account);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), account.getOrganizationId());
	}
	
	public boolean deleteDataPersonParticipant(PersonType person, DataType account) throws ArgumentException, FactoryException
	{
		return deleteDataPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteDataPersonParticipant(PersonType person, DataType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		DataParticipantType dp = getDataPersonParticipant(person, account, permission, affectType);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}
	public boolean deleteDataParticipations(DataType account) throws FactoryException, ArgumentException
	{

		List<DataParticipantType> dp = getDataParticipants(account);
		return deleteParticipants(dp.toArray(new DataParticipantType[0]), account.getOrganizationId());
	}
	
	public boolean deleteAccountPersonParticipant(PersonType person, AccountType account) throws ArgumentException, FactoryException
	{
		return deleteAccountPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteAccountPersonParticipant(PersonType person, AccountType account, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		AccountParticipantType dp = getAccountPersonParticipant(person, account, permission, affectType);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}
	public boolean deleteAccountParticipations(AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountParticipants(account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganizationId());
	}
	
	public boolean deleteUserPersonParticipant(PersonType person, UserType user) throws ArgumentException, FactoryException
	{
		return deleteUserPersonParticipant(person, user, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteUserPersonParticipant(PersonType person, UserType user, BasePermissionType permission, AffectEnumType affectType)  throws ArgumentException, FactoryException
	{
		UserParticipantType dp = getUserPersonParticipant(person, user, permission, affectType);
		if (dp == null) return true;

		removeFromCache(dp);

		return delete(dp);
	}

	public boolean deleteUserParticipations(UserType account) throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserParticipants(account);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), account.getOrganizationId());
	}
	public PersonParticipantType newPartnerPersonParticipation(PersonType person, PersonType partner) throws ArgumentException, FactoryException{
		return newPartnerPersonParticipation(person,partner,AuthorizationService.getViewObjectPermission(person.getOrganizationId()),AffectEnumType.GRANT_PERMISSION);
	}
	public PersonParticipantType newPartnerPersonParticipation(
			PersonType person,
			PersonType account,
			BasePermissionType permission,
			AffectEnumType affectType

		) throws ArgumentException
		{
			return newPersonParticipation(person, account, permission, affectType, ParticipantEnumType.PERSON);
		}
	public PersonParticipantType newDependentPersonParticipation(PersonType person, PersonType dependent) throws ArgumentException, FactoryException{
		return newDependentPersonParticipation(person,dependent,AuthorizationService.getViewObjectPermission(person.getOrganizationId()),AffectEnumType.GRANT_PERMISSION);
	}
	public PersonParticipantType newDependentPersonParticipation(
			PersonType person,
			PersonType account,
			BasePermissionType permission,
			AffectEnumType affectType

		) throws ArgumentException
		{
			return newPersonParticipation(person, account, permission, affectType, ParticipantEnumType.DEPENDENTPERSON);
		}
	public DataParticipantType newDataPersonParticipation(PersonType person, DataType data) throws ArgumentException, FactoryException{
		return newDataPersonParticipation(person,data,AuthorizationService.getViewObjectPermission(person.getOrganizationId()),AffectEnumType.GRANT_PERMISSION);
	}
	public DataParticipantType newDataPersonParticipation(
			PersonType person,
			DataType account,
			BasePermissionType permission,
			AffectEnumType affectType

		) throws ArgumentException
		{
			return newPersonParticipation(person, account, permission, affectType, ParticipantEnumType.DATA);
		}
	public AccountParticipantType newAccountPersonParticipation(PersonType person, AccountType data) throws ArgumentException, FactoryException{
		return newAccountPersonParticipation(person,data,AuthorizationService.getViewObjectPermission(person.getOrganizationId()),AffectEnumType.GRANT_PERMISSION);
	}

	public AccountParticipantType newAccountPersonParticipation(
		PersonType person,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affectType

	) throws ArgumentException
	{
		return newPersonParticipation(person, account, permission, affectType, ParticipantEnumType.ACCOUNT);
	}
	public UserParticipantType newUserPersonParticipation(PersonType person, UserType data) throws ArgumentException, FactoryException{
		return newUserPersonParticipation(person,data,AuthorizationService.getViewObjectPermission(person.getOrganizationId()),AffectEnumType.GRANT_PERMISSION);
	}

	public UserParticipantType newUserPersonParticipation(
			PersonType person,
			UserType role,
			BasePermissionType permission,
			AffectEnumType affectType

		) throws ArgumentException
		{
			return newPersonParticipation(person, role, permission, affectType, ParticipantEnumType.USER);
		}
	@SuppressWarnings("unchecked")
	public <T> T newPersonParticipation(
		PersonType person, 
		NameIdType map,
		BasePermissionType permission,
		AffectEnumType affectType,
		ParticipantEnumType participantType
	) throws ArgumentException
	{
		return (T)newParticipant(person, map, participantType, permission, affectType);

	}

	
	public List<PersonParticipantType> getPersonParticipants(PersonType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.PERSON), account.getOrganizationId()));	
	}
	
	public List<PersonParticipantType> getDependentParticipants(PersonType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.DEPENDENTPERSON), account.getOrganizationId()));	
	}
	
	public List<DataParticipantType> getDataParticipants(DataType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.DATA), account.getOrganizationId()));	
	}
	
	public List<AccountParticipantType> getAccountParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.ACCOUNT), account.getOrganizationId()));	
	}
	public List<UserParticipantType> getUserParticipants(UserType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.USER), account.getOrganizationId()));	
	}
	public List<PersonParticipantType> getPartnerParticipations(PersonType person) throws FactoryException, ArgumentException
	{
		return getPartnerPersonParticipations(new PersonType[] { person });
	}
	public List<PersonParticipantType> getDependentParticipations(PersonType person) throws FactoryException, ArgumentException
	{
		return getDependentPersonParticipations(new PersonType[] { person });
	}
	public List<DataParticipantType> getDataParticipations(PersonType person) throws FactoryException, ArgumentException
	{
		return getPersonDataParticipations(new PersonType[] { person });
	}

	public List<AccountParticipantType> getAccountParticipations(PersonType person) throws FactoryException, ArgumentException
	{
		return getPersonAccountParticipations(new PersonType[] { person });
	}
	public List<UserParticipantType> getUserParticipations(PersonType person) throws FactoryException, ArgumentException
	{
		return getPersonUserParticipations(new PersonType[] { person });
	}
	public List<PersonParticipantType> getPartnerPersonParticipations(PersonType[] person) throws FactoryException, ArgumentException
	{
		return convertList(getPersonParticipations(person, ParticipantEnumType.PERSON));
	}
	public List<PersonParticipantType> getDependentPersonParticipations(PersonType[] person) throws FactoryException, ArgumentException
	{
		return convertList(getPersonParticipations(person, ParticipantEnumType.DEPENDENTPERSON));
	}
	public List<DataParticipantType> getPersonDataParticipations(PersonType[] person) throws FactoryException, ArgumentException
	{
		return convertList(getPersonParticipations(person, ParticipantEnumType.DATA));
	}
	public List<AccountParticipantType> getPersonAccountParticipations(PersonType[] person) throws FactoryException, ArgumentException
	{
		return convertList(getPersonParticipations(person, ParticipantEnumType.ACCOUNT));
	}
	public List<UserParticipantType> getPersonUserParticipations(PersonType[] person) throws FactoryException, ArgumentException
	{
		return convertList(getPersonParticipations(person, ParticipantEnumType.USER));
	}
	
	public List<PersonType> getPartnersFromParticipation(PersonType participation) throws ArgumentException{
		List<PersonType> items = new ArrayList<>();
		try{
			PersonParticipantType[] parts = getPartnerParticipations(participation).toArray(new PersonParticipantType[0]);
			if(parts.length > 0){
				items = getPersonsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<PersonType> getDependentsFromParticipation(PersonType participation) throws ArgumentException{
		List<PersonType> items = new ArrayList<>();
		try{
			PersonParticipantType[] parts = getDependentParticipations(participation).toArray(new PersonParticipantType[0]);
			if(parts.length > 0){
				items = getPersonsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<DataType> getDatasFromParticipation(PersonType participation) throws ArgumentException{
		List<DataType> items = new ArrayList<>();
		try{
			DataParticipantType[] parts = getDataParticipations(participation).toArray(new DataParticipantType[0]);
			if(parts.length > 0){
				items = getDatasFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<AccountType> getAccountsFromParticipation(PersonType participation) throws ArgumentException{
		List<AccountType> items = new ArrayList<>();
		try{
			AccountParticipantType[] parts = getAccountParticipations(participation).toArray(new AccountParticipantType[0]);
			if(parts.length > 0){
				items = getAccountsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<UserType> getUsersFromParticipation(PersonType participation) throws ArgumentException{
		List<UserType> items = new ArrayList<>();
		try{
			UserParticipantType[] parts = getUserParticipations(participation).toArray(new UserParticipantType[0]);
			if(parts.length > 0){
				items = getUsersFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<PersonType> getPersonsFromParticipations(PersonParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getPersonList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}

	public List<AccountType> getAccountsFromParticipations(AccountParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<UserType> getUsersFromParticipations(UserParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getUserList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<DataType> getDatasFromParticipations(DataParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataList(new QueryField[]{ field }, true,startRecord, recordCount, organizationId);
	}	
	public List<NameIdType> getPersonParticipations(PersonType[] person, ParticipantEnumType participantType) throws FactoryException, ArgumentException
	{
		return getParticipations(person,participantType);
	}

	
	public List<PersonParticipantType> getPartnerPersonParticipants(
			PersonType person,
			PersonType account
		) throws FactoryException, ArgumentException
		{
			return getPartnerPersonParticipants(person, account, null, AffectEnumType.UNKNOWN);
		}
		public List<PersonParticipantType> getPartnerPersonParticipants(
			PersonType person,
			PersonType account,
			BasePermissionType permission,
			AffectEnumType affectType
		) throws FactoryException, ArgumentException
		{
			return convertList(getParticipants(person, account, ParticipantEnumType.PERSON, permission, affectType));
		}
		
		public PersonParticipantType getPartnerPersonParticipant(PersonType person, PersonType account) throws ArgumentException, FactoryException
		{
			return getPartnerPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
		}
		public PersonParticipantType getPartnerPersonParticipant(
			PersonType person,
			PersonType account,
			BasePermissionType permission,
			AffectEnumType affectType
		) throws ArgumentException, FactoryException
		{
			return getParticipant(person, account, ParticipantEnumType.PERSON, permission, affectType);

		}
	
		public List<PersonParticipantType> getDependentPersonParticipants(
				PersonType person,
				PersonType account
			) throws FactoryException, ArgumentException
			{
				return getDependentPersonParticipants(person, account, null, AffectEnumType.UNKNOWN);
			}
			public List<PersonParticipantType> getDependentPersonParticipants(
				PersonType person,
				PersonType account,
				BasePermissionType permission,
				AffectEnumType affectType
			) throws FactoryException, ArgumentException
			{
				return convertList(getParticipants(person, account, ParticipantEnumType.DEPENDENTPERSON, permission, affectType));
			}
			
			public PersonParticipantType getDependentPersonParticipant(PersonType person, PersonType account) throws ArgumentException, FactoryException
			{
				return getDependentPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
			}
			public PersonParticipantType getDependentPersonParticipant(
				PersonType person,
				PersonType account,
				BasePermissionType permission,
				AffectEnumType affectType
			) throws ArgumentException, FactoryException
			{
				return getParticipant(person, account, ParticipantEnumType.DEPENDENTPERSON, permission, affectType);

			}
			
			public List<DataParticipantType> getDataPersonParticipants(
					PersonType person,
					DataType account
				) throws FactoryException, ArgumentException
				{
					return getDataPersonParticipants(person, account, null, AffectEnumType.UNKNOWN);
				}
				public List<DataParticipantType> getDataPersonParticipants(
					PersonType person,
					DataType account,
					BasePermissionType permission,
					AffectEnumType affectType
				) throws FactoryException, ArgumentException
				{
					return convertList(getParticipants(person, account, ParticipantEnumType.DATA, permission, affectType));
				}
				
				public DataParticipantType getDataPersonParticipant(PersonType person, DataType account) throws ArgumentException, FactoryException
				{
					return getDataPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
				}
				public DataParticipantType getDataPersonParticipant(
					PersonType person,
					DataType account,
					BasePermissionType permission,
					AffectEnumType affectType
				) throws ArgumentException, FactoryException
				{
					return getParticipant(person, account, ParticipantEnumType.DATA, permission, affectType);

				}

	public List<AccountParticipantType> getAccountPersonParticipants(
		PersonType person,
		AccountType account
	) throws FactoryException, ArgumentException
	{
		return getAccountPersonParticipants(person, account, null, AffectEnumType.UNKNOWN);
	}
	public List<AccountParticipantType> getAccountPersonParticipants(
		PersonType person,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(person, account, ParticipantEnumType.ACCOUNT, permission, affectType));
	}
	
	public AccountParticipantType getAccountPersonParticipant(PersonType person, AccountType account) throws ArgumentException, FactoryException
	{
		return getAccountPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public AccountParticipantType getAccountPersonParticipant(
		PersonType person,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws ArgumentException, FactoryException
	{
		return getParticipant(person, account, ParticipantEnumType.ACCOUNT, permission, affectType);

	}
	
	public List<UserParticipantType> getUserPersonParticipants(
			PersonType person,
			UserType user
		) throws FactoryException, ArgumentException
	{
		return getUserPersonParticipants(person, user, null, AffectEnumType.UNKNOWN);
	}
	public List<UserParticipantType> getUserPersonParticipants(
		PersonType person,
		UserType user,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(person, user, ParticipantEnumType.USER, permission,affectType));
	}
	public UserParticipantType getUserPersonParticipant(PersonType person, UserType user) throws ArgumentException, FactoryException
	{
		return getUserPersonParticipant(person, user, null, AffectEnumType.UNKNOWN);
	}
	public UserParticipantType getUserPersonParticipant(
		PersonType person,
		UserType user,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws ArgumentException, FactoryException
	{
		return getParticipant(person, user, ParticipantEnumType.USER, permission, affectType);
	}
}
