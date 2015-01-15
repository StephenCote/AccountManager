package org.cote.accountmanager.data.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.AddressParticipantType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.ContactParticipantType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;



public abstract class BaseParticipationFactory extends ParticipationFactory {
	public static final Logger logger = Logger.getLogger(BaseParticipationFactory.class.getName());
	
	public BaseParticipationFactory(ParticipationEnumType type, String tableName){
		super(type, tableName);
	}
	
	public <T> T newParticipation(
			NameIdType cycle, 
			NameIdType map,
			BasePermissionType permission,
			AffectEnumType affect_type,
			ParticipantEnumType participant_type
		) throws ArgumentException
	{
		return (T)newParticipant(cycle, map, participant_type, permission, affect_type);
	}
	
	@Override
	protected BaseParticipantType newParticipant(ParticipantEnumType type) throws ArgumentException
	{
		return super.newParticipant(type);
	}
	
	
	public DataParticipantType newDataParticipation(NameIdType cycle, DataType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganization()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.DATA);
	}
	public List<DataType> getDataFromParticipations(DataParticipantType[] list, long startRecord, int recordCount, OrganizationType organization) throws FactoryException, ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getDataFactory().getDataList(new QueryField[]{ field }, true, startRecord, recordCount, organization);
	}
	public List<DataType> getDataFromParticipation(NameIdType participation) throws ArgumentException{
		List<DataType> items = new ArrayList<DataType>();
		try{
			DataParticipantType[] parts = getDataParticipations(participation).toArray(new DataParticipantType[0]);
			if(parts.length > 0){
				items = getDataFromParticipations(parts, 0, 0, participation.getOrganization());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		return items;
	}
	public List<DataParticipantType> getDataParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.DATA));
	}
	public List<DataParticipantType> getDataParticipants(DataType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.DATA), sched.getOrganization().getId()));
	}
	public DataParticipantType getDataParticipant(NameIdType cycle, DataType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.DATA);
	}

	public boolean deleteDataParticipant(NameIdType cycle, DataType sched) throws FactoryException, ArgumentException
	{
		DataParticipantType dp = getDataParticipant(cycle, sched);
		if (dp == null) return true;
		return deleteParticipant(dp);

	}	
	public boolean deleteDataParticipations(DataType sched) throws FactoryException,ArgumentException
	{

		List<DataParticipantType> dp = getDataParticipants(sched);
		return deleteParticipants(dp.toArray(new DataParticipantType[0]), sched.getOrganization());
	}	

	
	public boolean deleteContactParticipant(NameIdType obj, ContactType map) throws ArgumentException, FactoryException
	{
		return deleteContactPersonParticipant(obj, map, null, AffectEnumType.UNKNOWN);
	}
	public boolean deleteContactPersonParticipant(NameIdType obj, ContactType map, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
	{
		ContactParticipantType dp = getContactParticipant(obj, map, permission, affect_type);
		if (dp == null) return true;

		removeFromCache(dp);

		return deleteParticipant(dp);
	}
	public boolean deleteContactParticipations(ContactType map) throws FactoryException, ArgumentException
	{

		List<ContactParticipantType> dp = getContactParticipants(map);
		return deleteParticipants(dp.toArray(new ContactParticipantType[0]), map.getOrganization());
	}
	public ContactParticipantType newContactParticipation(NameIdType obj, ContactType map) throws ArgumentException, FactoryException{
		return newContactParticipation(obj,map,AuthorizationService.getViewObjectPermission(obj.getOrganization()),AffectEnumType.GRANT_PERMISSION);
	}

	public ContactParticipantType newContactParticipation(
		NameIdType obj,
		ContactType map,
		BasePermissionType permission,
		AffectEnumType affect_type

	) throws ArgumentException
	{
		return newParticipation(obj, map, permission, affect_type, ParticipantEnumType.CONTACT);
	}
	public List<ContactParticipantType> getContactParticipants(ContactType map) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(map, ParticipantEnumType.CONTACT), map.getOrganization().getId()));	
	}
	public List<ContactParticipantType> getContactParticipations(NameIdType obj) throws FactoryException, ArgumentException
	{
		return getContactParticipations(new NameIdType[] { obj });
	}
	public List<ContactParticipantType> getContactParticipations(NameIdType[] objs) throws FactoryException, ArgumentException
	{
		return convertList(getParticipations(objs, ParticipantEnumType.CONTACT));
	}

	public List<ContactType> getContactsFromParticipation(NameIdType participation) throws ArgumentException{
		List<ContactType> items = new ArrayList<ContactType>();
		try{
			ContactParticipantType[] parts = getContactParticipations(participation).toArray(new ContactParticipantType[0]);
			if(parts.length > 0){
				items = getContactsFromParticipations(parts, 0, 0, participation.getOrganization());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		return items;
	}
	public List<ContactType> getContactsFromParticipations(ContactParticipantType[] list, long startRecord, int recordCount, OrganizationType organization) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getContactFactory().getContactList(new QueryField[]{ field }, startRecord, recordCount, organization);
	}
	public List<ContactParticipantType> getContactParticipants(
			NameIdType obj,
			ContactType map
		) throws FactoryException, ArgumentException
		{
			return getContactParticipants(obj, map, null, AffectEnumType.UNKNOWN);
		}
		public List<ContactParticipantType> getContactParticipants(
			NameIdType obj,
			ContactType map,
			BasePermissionType permission,
			AffectEnumType affect_type
		) throws FactoryException, ArgumentException
		{
			return convertList(getParticipants(obj, map, ParticipantEnumType.CONTACT, permission, affect_type));
		}
		
		public ContactParticipantType getContactParticipant(NameIdType obj, ContactType map) throws ArgumentException, FactoryException
		{
			return getContactParticipant(obj, map, null, AffectEnumType.UNKNOWN);
		}
		public ContactParticipantType getContactParticipant(
			NameIdType obj,
			ContactType map,
			BasePermissionType permission,
			AffectEnumType affect_type
		) throws ArgumentException, FactoryException
		{
			return getParticipant(obj, map, ParticipantEnumType.CONTACT, permission, affect_type);

		}

		
		public boolean deleteAddressParticipant(NameIdType obj, AddressType map) throws ArgumentException, FactoryException
		{
			return deleteAddressPersonParticipant(obj, map, null, AffectEnumType.UNKNOWN);
		}
		public boolean deleteAddressPersonParticipant(NameIdType obj, AddressType map, BasePermissionType permission, AffectEnumType affect_type)  throws ArgumentException, FactoryException
		{
			AddressParticipantType dp = getAddressParticipant(obj, map, permission, affect_type);
			if (dp == null) return true;

			removeFromCache(dp);

			return deleteParticipant(dp);
		}
		public boolean deleteAddressParticipations(AddressType map) throws FactoryException, ArgumentException
		{

			List<AddressParticipantType> dp = getAddressParticipants(map);
			return deleteParticipants(dp.toArray(new AddressParticipantType[0]), map.getOrganization());
		}
		public AddressParticipantType newAddressParticipation(NameIdType obj, AddressType map) throws ArgumentException, FactoryException{
			return newAddressParticipation(obj,map,AuthorizationService.getViewObjectPermission(obj.getOrganization()),AffectEnumType.GRANT_PERMISSION);
		}

		public AddressParticipantType newAddressParticipation(
			NameIdType obj,
			AddressType map,
			BasePermissionType permission,
			AffectEnumType affect_type

		) throws ArgumentException
		{
			return newParticipation(obj, map, permission, affect_type, ParticipantEnumType.ADDRESS);
		}
		public List<AddressParticipantType> getAddressParticipants(AddressType map) throws FactoryException, ArgumentException
		{
			return convertList(getByField(QueryFields.getFieldParticipantMatch(map, ParticipantEnumType.ADDRESS), map.getOrganization().getId()));	
		}
		public List<AddressParticipantType> getAddressParticipations(NameIdType obj) throws FactoryException, ArgumentException
		{
			return getAddressParticipations(new NameIdType[] { obj });
		}
		public List<AddressParticipantType> getAddressParticipations(NameIdType[] objs) throws FactoryException, ArgumentException
		{
			return convertList(getParticipations(objs, ParticipantEnumType.ADDRESS));
		}

		public List<AddressType> getAddressesFromParticipation(NameIdType participation) throws ArgumentException{
			List<AddressType> items = new ArrayList<AddressType>();
			try{
				AddressParticipantType[] parts = getAddressParticipations(participation).toArray(new AddressParticipantType[0]);
				if(parts.length > 0){
					items = getAddressesFromParticipations(parts, 0, 0, participation.getOrganization());
				}
			}
			catch(FactoryException fe){
				logger.error(fe.getMessage());
				fe.printStackTrace();
			}
			return items;
		}
		public List<AddressType> getAddressesFromParticipations(AddressParticipantType[] list, long startRecord, int recordCount, OrganizationType organization) throws FactoryException,ArgumentException
		{
			QueryField field = QueryFields.getFieldParticipantIds(list);
			return Factories.getAddressFactory().getAddressList(new QueryField[]{ field }, startRecord, recordCount, organization);
		}
		public List<AddressParticipantType> getAddressParticipants(
				NameIdType obj,
				AddressType map
			) throws FactoryException, ArgumentException
			{
				return getAddressParticipants(obj, map, null, AffectEnumType.UNKNOWN);
			}
			public List<AddressParticipantType> getAddressParticipants(
				NameIdType obj,
				AddressType map,
				BasePermissionType permission,
				AffectEnumType affect_type
			) throws FactoryException, ArgumentException
			{
				return convertList(getParticipants(obj, map, ParticipantEnumType.ADDRESS, permission, affect_type));
			}
			
			public AddressParticipantType getAddressParticipant(NameIdType obj, AddressType map) throws ArgumentException, FactoryException
			{
				return getAddressParticipant(obj, map, null, AffectEnumType.UNKNOWN);
			}
			public AddressParticipantType getAddressParticipant(
				NameIdType obj,
				AddressType map,
				BasePermissionType permission,
				AffectEnumType affect_type
			) throws ArgumentException, FactoryException
			{
				return getParticipant(obj, map, ParticipantEnumType.ADDRESS, permission, affect_type);

			}

	
	
}
