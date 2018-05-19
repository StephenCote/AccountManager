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
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;


public class AddressFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.ADDRESS, AddressFactory.class); }
	public AddressFactory(){
		super();
		this.hasParentId=false;
		this.hasUrn = true;
		this.hasObjectId = true;
		this.primaryTableName = "addresses";
		this.tableNames.add(this.primaryTableName);
		factoryType = FactoryEnumType.ADDRESS;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getGroupId();
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(this.primaryTableName)){

		}
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		AddressType addr = (AddressType)obj;
		if(addr.getPopulated() == true) return;

		addr.setPopulated(true);
		updateToCache(addr);
	}
	public AddressType newAddress(UserType user, AddressType parentAddress) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		
		return newAddress(user,parentAddress.getGroupId());
	}
	public AddressType newAddress(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		AddressType obj = new AddressType();
		obj.setLocationType(LocationEnumType.UNKNOWN);
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.ADDRESS);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		AddressType obj = (AddressType)object;
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Address without a group");

		DataRow row = prepareAdd(obj, "addresses");


		try{
			row.setCellValue("preferred", obj.getPreferred());
			row.setCellValue("addressline1",obj.getAddressLine1());
			row.setCellValue("addressline2",obj.getAddressLine2());
			row.setCellValue("city",obj.getCity());
			row.setCellValue("country",obj.getCountry());
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("postalcode",obj.getPostalCode());
			row.setCellValue("state",obj.getState());
			row.setCellValue("region",obj.getRegion());
			row.setCellValue("locationtype",obj.getLocationType().toString());
			row.setCellValue("groupid", obj.getGroupId());
			
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
		AddressType newObj = new AddressType();
		newObj.setNameType(NameEnumType.ADDRESS);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setPreferred(rset.getBoolean("preferred"));
		newObj.setDescription(rset.getString("description"));
		newObj.setAddressLine1(rset.getString("addressline1"));
		newObj.setAddressLine2(rset.getString("addressline2"));
		newObj.setCity(rset.getString("city"));
		newObj.setCountry(rset.getString("country"));
		newObj.setPostalCode(rset.getString("postalcode"));
		newObj.setState(rset.getString("state"));
		newObj.setRegion(rset.getString("region"));
		newObj.setLocationType(LocationEnumType.valueOf(rset.getString("locationtype")));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{	
		AddressType data = (AddressType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		AddressType useMap = (AddressType)map;
		fields.add(QueryFields.getFieldPreferred(useMap.getPreferred()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldAddressLine1(useMap.getAddressLine1()));
		fields.add(QueryFields.getFieldAddressLine2(useMap.getAddressLine2()));
		fields.add(QueryFields.getFieldCity(useMap.getCity()));
		fields.add(QueryFields.getFieldState(useMap.getState()));
		fields.add(QueryFields.getFieldRegion(useMap.getRegion()));
		fields.add(QueryFields.getFieldLocationType(useMap.getLocationType()));
		fields.add(QueryFields.getFieldCountry(useMap.getCountry()));
		fields.add(QueryFields.getFieldPostalCode(useMap.getPostalCode()));
	}
	public int deleteAddresssByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteAddresssByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		AddressType obj = (AddressType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteAddresssByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);

	}
	public int deleteAddresssInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		return deleteAddresssByIds(ids, group.getOrganizationId());
	}

	public List<AddressType>  getAddressList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<AddressType> getAddressListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
	
	public List<AddressType> searchAddresss(String searchValue, long startRecord, int recordCount, DirectoryGroupType dir) throws FactoryException{
	
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
	
	
	/// Address search uses a different query to join in contact information
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
