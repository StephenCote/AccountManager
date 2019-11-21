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
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;


public class ContactFactory extends NameIdGroupFactory {
	
	public ContactFactory(){
		super();
		this.hasParentId=false;
		this.hasUrn = true;
		this.hasObjectId = true;
		this.primaryTableName = "contacts";
		this.tableNames.add(primaryTableName);
		
		factoryType = FactoryEnumType.CONTACT;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getGroupId();
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){

		}
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		ContactType contact = (ContactType)obj;

		if(contact.getPopulated()) return;
		contact.setPopulated(true);
		updateToCache(contact);
		
	}
	public ContactType newContact(UserType user, ContactType parentContact) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		
		return newContact(user,parentContact.getGroupId());

	}
	public ContactType newContact(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || user.getId().equals(0L)) throw new ArgumentException("Invalid owner");
		ContactType obj = new ContactType();
		
		obj.setLocationType(LocationEnumType.UNKNOWN);
		obj.setContactType(ContactEnumType.UNKNOWN);
		obj.setDescription("");
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.CONTACT);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws FactoryException
	{
		ContactType obj = (ContactType)object;
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Contact without a group");

		DataRow row = prepareAdd(obj, "contacts");


		try{
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("locationtype",obj.getLocationType().toString());
			row.setCellValue("contacttype",obj.getContactType().toString());
			row.setCellValue("contactvalue",obj.getContactValue());
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("preferred", obj.getPreferred());
			if(insertRow(row)) return true;
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} 
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		ContactType newObj = new ContactType();
		newObj.setNameType(NameEnumType.CONTACT);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setPreferred(rset.getBoolean("preferred"));
		newObj.setDescription(rset.getString("description"));
		newObj.setLocationType(LocationEnumType.valueOf(rset.getString("locationtype")));
		newObj.setContactType(ContactEnumType.valueOf(rset.getString("contacttype")));
		newObj.setContactValue(rset.getString("contactvalue"));
		
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{	
		ContactType data = (ContactType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ContactType useMap = (ContactType)map;
		fields.add(QueryFields.getFieldPreferred(useMap.getPreferred()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldLocationType(useMap.getLocationType()));
		fields.add(QueryFields.getFieldContactType(useMap.getContactType()));
		fields.add(QueryFields.getFieldContactValue(useMap.getContactValue()));
	}
	public int deleteContactsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteContactsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		ContactType obj = (ContactType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteContactsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	public int deleteContactsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		return deleteContactsByIds(ids, group.getOrganizationId());
	}

	public List<ContactType>  getContactList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ContactType> getContactListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
	
	public List<ContactType> searchContacts(String searchValue, long startRecord, int recordCount, DirectoryGroupType dir) throws FactoryException{
	
		ProcessingInstructionType instruction = null;
		if(startRecord >= 0 && recordCount >= 0){
			instruction = new ProcessingInstructionType();
			instruction.setOrderClause("name ASC");
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		
		List<QueryField> fields = buildSearchQuery(searchValue, dir.getOrganizationId());
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		return search(fields.toArray(new QueryField[0]), instruction, dir.getOrganizationId());
	}
	
	
	/// Contact search uses a different query to join in contact information
	/// Otherwise, this could be the paginateList method
	///
	@Override
	public List<QueryField> buildSearchQuery(String searchValue, long organizationId) throws FactoryException{
		
		searchValue = searchValue.replaceAll("\\*","%");
		
		List<QueryField> filters = new ArrayList<>();
		QueryField searchFilters = new QueryField(SqlDataEnumType.NULL,"searchgroup",null);
		searchFilters.setComparator(ComparatorEnumType.GROUP_OR);
		QueryField nameFilter = new QueryField(SqlDataEnumType.VARCHAR,"name",searchValue);
		nameFilter.setComparator(ComparatorEnumType.LIKE);
		searchFilters.getFields().add(nameFilter);
		QueryField firstNameFilter = new QueryField(SqlDataEnumType.VARCHAR,"firstname",searchValue);
		firstNameFilter.setComparator(ComparatorEnumType.LIKE);
		searchFilters.getFields().add(firstNameFilter);
		filters.add(searchFilters);
		return filters;
	}
	
	@Override
	public <T> List<T> search(QueryField[] filters, ProcessingInstructionType instruction, long organizationId){
		return searchByIdInView("personContact", filters,instruction,organizationId);
	}


	
}
