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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.ContactInformationEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class ContactInformationFactory extends NameIdFactory {
	
	
	public ContactInformationFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasName = false;
		this.hasUrn = false;
		this.hasObjectId = true;
		this.primaryTableName = "contactinformation";
		this.tableNames.add(primaryTableName);
		this.factoryType = FactoryEnumType.CONTACTINFORMATION;
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// Restrict any additional columns
		}
	}
	
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		ContactInformationType cinfo = (ContactInformationType)obj;
		if(cinfo.getPopulated().booleanValue() || cinfo.getId().equals(0L)) return;
		cinfo.getContacts().addAll(((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).getContactsFromParticipation(cinfo));
		cinfo.getAddresses().addAll(((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).getAddressesFromParticipation(cinfo));
		cinfo.setPopulated(true);
		
		updateToCache(cinfo);
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		ContactInformationType t = (ContactInformationType)obj;
		return t.getReferenceId() + "-" + t.getContactInformationType().toString() + "-" + t.getOrganizationId();
	}

	public boolean deleteContactInformationByReferenceType(NameIdType map) throws FactoryException
	{
		return deleteContactInformationByReferenceIds(new long[]{map.getId()},map.getOrganizationId());
	}
	public boolean deleteContactInformationByReferenceIds(long[] ids, long organizationId) throws FactoryException{
		
		int deleted = deleteByBigIntField(Columns.get(ColumnEnumType.REFERENCEID),ids,organizationId);
		return (deleted > 0);
	}
	
	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		ContactInformationType cinfo = (ContactInformationType)object;
		int deleted = deleteById(cinfo.getId(), cinfo.getOrganizationId());
		if(deleted > 0){
			((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).deleteParticipations(new long[]{cinfo.getId()},cinfo.getOrganizationId());
		}
		return (deleted > 0);
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{	
		ContactInformationType cinfo = (ContactInformationType)object;
		boolean outBool = false;
		removeFromCache(cinfo);
		if(super.update(cinfo)){
			try{
				/// Contacts
				///
				BaseParticipantType part = null;
				List<Long> delIds = new ArrayList<>();
				if(bulkMode) BulkFactories.getBulkFactory().setDirty(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION);

				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).getContactParticipations(cinfo).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < cinfo.getContacts().size();i++){
					if(!set.contains(cinfo.getContacts().get(i).getId())){
						part = ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).newContactParticipation(cinfo,cinfo.getContacts().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.CONTACTINFORMATIONPARTICIPATION, part);
						else ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).add(part);
					}
					else{
						set.remove(cinfo.getContacts().get(i).getId());
					}
				}
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				set.clear();
				maps = ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).getAddressParticipations(cinfo).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < cinfo.getAddresses().size();i++){
					if(!set.contains(cinfo.getAddresses().get(i).getId())){
						part = ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).newAddressParticipation(cinfo,cinfo.getAddresses().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.CONTACTINFORMATIONPARTICIPATION, part);
						else ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).add(part);
					}
					else{
						set.remove(cinfo.getAddresses().get(i).getId());
					}
				}
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				if(!delIds.isEmpty()) ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(delIds.toArray(new Long[0])), cinfo, cinfo.getOrganizationId());
				outBool = true;
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}

		}
		return outBool;
	}
	
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		ContactInformationType cit = (ContactInformationType)map;
		long tmpId = 0;
		if(cit.getReferenceId() < 0){
			tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getReferenceId());
			if(tmpId > 0) cit.setReferenceId(tmpId);
		}
	}
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		ContactInformationType newInfo = (ContactInformationType)object;
		if (newInfo.getReferenceId().equals(0L)) throw new FactoryException("Cannot add information without a corresponding reference id");
		if (newInfo.getOrganizationId() == null || newInfo.getOrganizationId() <= 0) throw new FactoryException("Cannot add contact information to invalid organization");
		if(newInfo.getOwnerId().equals(0L)) throw new FactoryException("Contact Information must now have an owner id");
		DataRow row = prepareAdd(newInfo, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.REFERENCEID),newInfo.getReferenceId());
			row.setCellValue(Columns.get(ColumnEnumType.CONTACTINFORMATIONTYPE), newInfo.getContactInformationType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION),newInfo.getDescription());
			if (insertRow(row)){
				ContactInformationType cobj = (bulkMode ? newInfo : getContactInformationByReferenceId(newInfo.getReferenceId(),newInfo.getContactInformationType(),newInfo.getOrganizationId()));
				if(cobj == null) throw new FactoryException("Failed to retrieve new user cobject");

				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < newInfo.getContacts().size();i++){
					part = ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).newContactParticipation(cobj,newInfo.getContacts().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).add(part);
					else ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).add(part);
				}
				
				for(int i = 0; i < newInfo.getAddresses().size();i++){
					part = ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).newAddressParticipation(cobj,newInfo.getAddresses().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).add(part);
					else ((ContactInformationParticipationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION)).add(part);
				}
				
				return true;
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return false;
	}
	
	public ContactInformationType getContactInformationForPerson(PersonType map) throws FactoryException, ArgumentException
	{
		return getContactInformationByReferenceId(map.getId(), ContactInformationEnumType.PERSON, map.getOrganizationId());
	}
	public ContactInformationType getContactInformationForUser(UserType map) throws FactoryException, ArgumentException
	{
		return getContactInformationByReferenceId(map.getId(), ContactInformationEnumType.USER, map.getOrganizationId());
	}
	public ContactInformationType getContactInformationForAccount(AccountType map) throws FactoryException, ArgumentException
	{
		return getContactInformationByReferenceId(map.getId(), ContactInformationEnumType.ACCOUNT, map.getOrganizationId());
	}
	public ContactInformationType getContactInformationByReferenceId(long referenceId, ContactInformationEnumType type, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> cinfo = getByField(new QueryField[]{QueryFields.getFieldReferenceId(referenceId),QueryFields.getFieldContactInformationType(type)},organizationId);
		if (!cinfo.isEmpty()) return (ContactInformationType)cinfo.get(0);
		return null;
	}

	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ContactInformationType useMap = (ContactInformationType)map;
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));
		fields.add(QueryFields.getFieldContactInformationType(useMap.getContactInformationType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
	}
	public ContactInformationType newContactInformation(PersonType map)
	{
		ContactInformationType cit = newContactInformation(map,ContactInformationEnumType.PERSON);
		cit.setOwnerId(map.getOwnerId());
		return cit;
	}
	public ContactInformationType newContactInformation(AccountType map)
	{
		ContactInformationType cit = newContactInformation(map,ContactInformationEnumType.ACCOUNT);
		cit.setOwnerId(map.getOwnerId());
		return cit;
	}	
	public ContactInformationType newContactInformation(UserType map)
	{
		ContactInformationType cit = newContactInformation(map,ContactInformationEnumType.USER);
		cit.setOwnerId(map.getId());
		return cit;
	}
	protected ContactInformationType newContactInformation(NameIdType map, ContactInformationEnumType type)
	{
		ContactInformationType cinfo = new ContactInformationType();
		cinfo.setReferenceId(map.getId());
		cinfo.setOrganizationId(map.getOrganizationId());
		cinfo.setContactInformationType(type);
		cinfo.setNameType(NameEnumType.CONTACTINFORMATION);
		return cinfo;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		ContactInformationType cinfo = new ContactInformationType();
		cinfo.setContactInformationType(ContactInformationEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.CONTACTINFORMATIONTYPE))));
		cinfo.setReferenceId(rset.getLong(Columns.get(ColumnEnumType.REFERENCEID)));
		cinfo.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		cinfo.setNameType(NameEnumType.CONTACTINFORMATION);
		return super.read(rset, cinfo);
	}


}
