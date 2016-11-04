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

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.AddressParticipantType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.ContactParticipantType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;

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
	public GroupParticipantType newGroupParticipation(NameIdType obj, BaseGroupType group) throws ArgumentException,FactoryException {
		return newParticipation(obj, group, AuthorizationService.getViewObjectPermission(obj.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.GROUP);
	}
	
	public DataParticipantType newDataParticipation(NameIdType cycle, DataType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.DATA);
	}
	public List<DataType> getDataFromParticipations(DataParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException, ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getDataFactory().getDataList(new QueryField[]{ field }, true, startRecord, recordCount, organizationId);
	}
	public List<DataType> getDataFromParticipation(NameIdType participation) throws ArgumentException{
		List<DataType> items = new ArrayList<DataType>();
		try{
			DataParticipantType[] parts = getDataParticipations(participation).toArray(new DataParticipantType[0]);
			if(parts.length > 0){
				items = getDataFromParticipations(parts, 0, 0, participation.getOrganizationId());
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
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.DATA), sched.getOrganizationId()));
	}
	public DataParticipantType getDataParticipant(NameIdType cycle, DataType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.DATA);
	}

	public boolean deleteDataParticipant(NameIdType cycle, DataType sched) throws FactoryException, ArgumentException
	{
		DataParticipantType dp = getDataParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteDataParticipations(DataType sched) throws FactoryException,ArgumentException
	{

		List<DataParticipantType> dp = getDataParticipants(sched);
		return deleteParticipants(dp.toArray(new DataParticipantType[0]), sched.getOrganizationId());
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

		return delete(dp);
	}
	public boolean deleteContactParticipations(ContactType map) throws FactoryException, ArgumentException
	{

		List<ContactParticipantType> dp = getContactParticipants(map);
		return deleteParticipants(dp.toArray(new ContactParticipantType[0]), map.getOrganizationId());
	}
	public ContactParticipantType newContactParticipation(NameIdType obj, ContactType map) throws ArgumentException, FactoryException{
		return newContactParticipation(obj,map,AuthorizationService.getViewObjectPermission(obj.getOrganizationId()),AffectEnumType.GRANT_PERMISSION);
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
		return convertList(getByField(QueryFields.getFieldParticipantMatch(map, ParticipantEnumType.CONTACT), map.getOrganizationId()));	
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
				items = getContactsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		return items;
	}
	public List<ContactType> getContactsFromParticipations(ContactParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getContactFactory().getContactList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
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

			return delete(dp);
		}
		public boolean deleteAddressParticipations(AddressType map) throws FactoryException, ArgumentException
		{

			List<AddressParticipantType> dp = getAddressParticipants(map);
			return deleteParticipants(dp.toArray(new AddressParticipantType[0]), map.getOrganizationId());
		}
		public AddressParticipantType newAddressParticipation(NameIdType obj, AddressType map) throws ArgumentException, FactoryException{
			return newAddressParticipation(obj,map,AuthorizationService.getViewObjectPermission(obj.getOrganizationId()),AffectEnumType.GRANT_PERMISSION);
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
			return convertList(getByField(QueryFields.getFieldParticipantMatch(map, ParticipantEnumType.ADDRESS), map.getOrganizationId()));	
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
					items = getAddressesFromParticipations(parts, 0, 0, participation.getOrganizationId());
				}
			}
			catch(FactoryException fe){
				logger.error(fe.getMessage());
				fe.printStackTrace();
			}
			return items;
		}
		public List<AddressType> getAddressesFromParticipations(AddressParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
		{
			QueryField field = QueryFields.getFieldParticipantIds(list);
			return Factories.getAddressFactory().getAddressList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
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
