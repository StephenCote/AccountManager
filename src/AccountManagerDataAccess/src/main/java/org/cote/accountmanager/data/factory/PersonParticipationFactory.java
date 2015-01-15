package org.cote.accountmanager.data.factory;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.AccountParticipantType;

import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;


public class PersonParticipationFactory extends ParticipationFactory {
	public PersonParticipationFactory(){
		super(ParticipationEnumType.PERSON, "personparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.PERSONPARTICIPATION;
	}
	
	public boolean deletePartnerPersonParticipant(PersonType person, PersonType account) throws ArgumentException, FactoryException
	{
		return deletePartnerPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deletePartnerPersonParticipant(PersonType person, PersonType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		PersonParticipantType dp = getPartnerPersonParticipant(person, account, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deletePersonParticipations(PersonType account) throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getPersonParticipants(account);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), account.getOrganization());
	}
	
	public boolean deleteDependentPersonParticipant(PersonType person, PersonType account) throws ArgumentException, FactoryException
	{
		return deleteDependentPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteDependentPersonParticipant(PersonType person, PersonType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		PersonParticipantType dp = getDependentPersonParticipant(person, account, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deleteDependentParticipations(PersonType account) throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getDependentParticipants(account);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), account.getOrganization());
	}
	
	public boolean deleteDataPersonParticipant(PersonType person, DataType account) throws ArgumentException, FactoryException
	{
		return deleteDataPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteDataPersonParticipant(PersonType person, DataType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		DataParticipantType dp = getDataPersonParticipant(person, account, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deleteDataParticipations(DataType account) throws FactoryException, ArgumentException
	{

		List<DataParticipantType> dp = getDataParticipants(account);
		return deleteParticipants(dp.toArray(new DataParticipantType[0]), account.getOrganization());
	}
	
	public boolean deleteAccountPersonParticipant(PersonType person, AccountType account) throws ArgumentException, FactoryException
	{
		return deleteAccountPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteAccountPersonParticipant(PersonType person, AccountType account, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		AccountParticipantType dp = getAccountPersonParticipant(person, account, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deleteAccountParticipations(AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountParticipants(account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganization());
	}
	
	public boolean deleteUserPersonParticipant(PersonType person, UserType user) throws ArgumentException, FactoryException
	{
		return deleteUserPersonParticipant(person, user, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteUserPersonParticipant(PersonType person, UserType user, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		UserParticipantType dp = getUserPersonParticipant(person, user, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}

	public boolean deleteUserParticipations(UserType account) throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserParticipants(account);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), account.getOrganization());
	}
	public PersonParticipantType newPartnerPersonParticipation(PersonType person, PersonType partner) throws ArgumentException, FactoryException{
		return newPartnerPersonParticipation(person,partner,AuthorizationService.getViewObjectPermission(person.getOrganization()),AffectEnumType.GRANT_PERMISSION);
	}
	public PersonParticipantType newPartnerPersonParticipation(
			PersonType person,
			PersonType account,
			BasePermissionType permission,
			AffectEnumType affect_type

		) throws ArgumentException
		{
			return newPersonParticipation(person, account, permission, affect_type, ParticipantEnumType.PERSON);
		}
	public PersonParticipantType newDependentPersonParticipation(PersonType person, PersonType dependent) throws ArgumentException, FactoryException{
		return newDependentPersonParticipation(person,dependent,AuthorizationService.getViewObjectPermission(person.getOrganization()),AffectEnumType.GRANT_PERMISSION);
	}
	public PersonParticipantType newDependentPersonParticipation(
			PersonType person,
			PersonType account,
			BasePermissionType permission,
			AffectEnumType affect_type

		) throws ArgumentException
		{
			return newPersonParticipation(person, account, permission, affect_type, ParticipantEnumType.DEPENDENTPERSON);
		}
	public DataParticipantType newDataPersonParticipation(PersonType person, DataType data) throws ArgumentException, FactoryException{
		return newDataPersonParticipation(person,data,AuthorizationService.getViewObjectPermission(person.getOrganization()),AffectEnumType.GRANT_PERMISSION);
	}
	public DataParticipantType newDataPersonParticipation(
			PersonType person,
			DataType account,
			BasePermissionType permission,
			AffectEnumType affect_type

		) throws ArgumentException
		{
			return newPersonParticipation(person, account, permission, affect_type, ParticipantEnumType.DATA);
		}
	public AccountParticipantType newAccountPersonParticipation(PersonType person, AccountType data) throws ArgumentException, FactoryException{
		return newAccountPersonParticipation(person,data,AuthorizationService.getViewObjectPermission(person.getOrganization()),AffectEnumType.GRANT_PERMISSION);
	}

	public AccountParticipantType newAccountPersonParticipation(
		PersonType person,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affect_type

	) throws ArgumentException
	{
		return newPersonParticipation(person, account, permission, affect_type, ParticipantEnumType.ACCOUNT);
	}
	public UserParticipantType newUserPersonParticipation(PersonType person, UserType data) throws ArgumentException, FactoryException{
		return newUserPersonParticipation(person,data,AuthorizationService.getViewObjectPermission(person.getOrganization()),AffectEnumType.GRANT_PERMISSION);
	}

	public UserParticipantType newUserPersonParticipation(
			PersonType person,
			UserType role,
			BasePermissionType permission,
			AffectEnumType affect_type

		) throws ArgumentException
		{
			return newPersonParticipation(person, role, permission, affect_type, ParticipantEnumType.USER);
		}
	public <T> T newPersonParticipation(
		PersonType person, 
		NameIdType map,
		BasePermissionType permission,
		AffectEnumType affect_type,
		ParticipantEnumType participant_type
	) throws ArgumentException
	{
		return (T)newParticipant(person, map, participant_type, permission, affect_type);

	}

	
	public List<PersonParticipantType> getPersonParticipants(PersonType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.PERSON), account.getOrganization().getId()));	
	}
	
	public List<PersonParticipantType> getDependentParticipants(PersonType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.DEPENDENTPERSON), account.getOrganization().getId()));	
	}
	
	public List<DataParticipantType> getDataParticipants(DataType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.DATA), account.getOrganization().getId()));	
	}
	
	public List<AccountParticipantType> getAccountParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.ACCOUNT), account.getOrganization().getId()));	
	}
	public List<UserParticipantType> getUserParticipants(UserType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account, ParticipantEnumType.USER), account.getOrganization().getId()));	
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
		return convertList(getPersonParticipations(person, ParticipantEnumType.PERSON));
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
		List<PersonType> items = new ArrayList<PersonType>();
		try{
			PersonParticipantType[] parts = getPartnerParticipations(participation).toArray(new PersonParticipantType[0]);
			if(parts.length > 0){
				items = getPersonsFromParticipations(parts, 0, 0, participation.getOrganization());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		return items;
	}
	public List<PersonType> getDependentsFromParticipation(PersonType participation) throws ArgumentException{
		List<PersonType> items = new ArrayList<PersonType>();
		try{
			PersonParticipantType[] parts = getDependentParticipations(participation).toArray(new PersonParticipantType[0]);
			if(parts.length > 0){
				items = getPersonsFromParticipations(parts, 0, 0, participation.getOrganization());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		return items;
	}
	public List<DataType> getDatasFromParticipation(PersonType participation) throws ArgumentException{
		List<DataType> items = new ArrayList<DataType>();
		try{
			DataParticipantType[] parts = getDataParticipations(participation).toArray(new DataParticipantType[0]);
			if(parts.length > 0){
				items = getDatasFromParticipations(parts, 0, 0, participation.getOrganization());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		return items;
	}
	public List<AccountType> getAccountsFromParticipation(PersonType participation) throws ArgumentException{
		List<AccountType> items = new ArrayList<AccountType>();
		try{
			AccountParticipantType[] parts = getAccountParticipations(participation).toArray(new AccountParticipantType[0]);
			if(parts.length > 0){
				items = getAccountsFromParticipations(parts, 0, 0, participation.getOrganization());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		return items;
	}
	public List<UserType> getUsersFromParticipation(PersonType participation) throws ArgumentException{
		List<UserType> items = new ArrayList<UserType>();
		try{
			UserParticipantType[] parts = getUserParticipations(participation).toArray(new UserParticipantType[0]);
			if(parts.length > 0){
				items = getUsersFromParticipations(parts, 0, 0, participation.getOrganization());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		return items;
	}
	public List<PersonType> getPersonsFromParticipations(PersonParticipantType[] list, long startRecord, int recordCount, OrganizationType organization) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getPersonFactory().getPersonList(new QueryField[]{ field }, startRecord, recordCount, organization);
	}

	public List<AccountType> getAccountsFromParticipations(AccountParticipantType[] list, long startRecord, int recordCount, OrganizationType organization) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getAccountFactory().getAccountList(new QueryField[]{ field }, startRecord, recordCount, organization);
	}
	public List<UserType> getUsersFromParticipations(UserParticipantType[] list, long startRecord, int recordCount, OrganizationType organization) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getUserFactory().getUserList(new QueryField[]{ field }, startRecord, recordCount, organization);
	}
	public List<DataType> getDatasFromParticipations(DataParticipantType[] list, long startRecord, int recordCount, OrganizationType organization) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getDataFactory().getDataList(new QueryField[]{ field }, true,startRecord, recordCount, organization);
	}	
	public List<NameIdType> getPersonParticipations(PersonType[] person, ParticipantEnumType participant_type) throws FactoryException, ArgumentException
	{
		/*
		if (person.length == 0) return new ArrayList<NameIdType>();
		OrganizationType org = person[0].getOrganization();

		List<QueryField> matches = new ArrayList<QueryField>();
		matches.add(QueryFields.getFieldParticipantType(participant_type));

		matches.add(QueryFields.getFieldParticipationIds(person));
		return getByField(matches.toArray(new QueryField[0]), org.getId());
		*/
		return getParticipations(person,participant_type);
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
			AffectEnumType affect_type
		) throws FactoryException, ArgumentException
		{
			return convertList(getParticipants(person, account, ParticipantEnumType.PERSON, permission, affect_type));
		}
		
		public PersonParticipantType getPartnerPersonParticipant(PersonType person, PersonType account) throws ArgumentException, FactoryException
		{
			return getPartnerPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
		}
		public PersonParticipantType getPartnerPersonParticipant(
			PersonType person,
			PersonType account,
			BasePermissionType permission,
			AffectEnumType affect_type
		) throws ArgumentException, FactoryException
		{
			return getParticipant(person, account, ParticipantEnumType.PERSON, permission, affect_type);

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
				AffectEnumType affect_type
			) throws FactoryException, ArgumentException
			{
				return convertList(getParticipants(person, account, ParticipantEnumType.DEPENDENTPERSON, permission, affect_type));
			}
			
			public PersonParticipantType getDependentPersonParticipant(PersonType person, PersonType account) throws ArgumentException, FactoryException
			{
				return getDependentPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
			}
			public PersonParticipantType getDependentPersonParticipant(
				PersonType person,
				PersonType account,
				BasePermissionType permission,
				AffectEnumType affect_type
			) throws ArgumentException, FactoryException
			{
				return getParticipant(person, account, ParticipantEnumType.DEPENDENTPERSON, permission, affect_type);

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
					AffectEnumType affect_type
				) throws FactoryException, ArgumentException
				{
					return convertList(getParticipants(person, account, ParticipantEnumType.DATA, permission, affect_type));
				}
				
				public DataParticipantType getDataPersonParticipant(PersonType person, DataType account) throws ArgumentException, FactoryException
				{
					return getDataPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
				}
				public DataParticipantType getDataPersonParticipant(
					PersonType person,
					DataType account,
					BasePermissionType permission,
					AffectEnumType affect_type
				) throws ArgumentException, FactoryException
				{
					return getParticipant(person, account, ParticipantEnumType.DATA, permission, affect_type);

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
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(person, account, ParticipantEnumType.ACCOUNT, permission, affect_type));
	}
	
	public AccountParticipantType getAccountPersonParticipant(PersonType person, AccountType account) throws ArgumentException, FactoryException
	{
		return getAccountPersonParticipant(person, account, null, AffectEnumType.UNKNOWN);
	}
	public AccountParticipantType getAccountPersonParticipant(
		PersonType person,
		AccountType account,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException, FactoryException
	{
		return getParticipant(person, account, ParticipantEnumType.ACCOUNT, permission, affect_type);

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
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		return convertList(getParticipants(person, user, ParticipantEnumType.USER, permission,affect_type));
	}
	public UserParticipantType getUserPersonParticipant(PersonType person, UserType user) throws ArgumentException, FactoryException
	{
		return getUserPersonParticipant(person, user, null, AffectEnumType.UNKNOWN);
	}
	public UserParticipantType getUserPersonParticipant(
		PersonType person,
		UserType user,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException, FactoryException
	{
		return getParticipant(person, user, ParticipantEnumType.USER, permission, affect_type);
	}
}
