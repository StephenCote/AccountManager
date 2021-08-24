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
/*
 * 
 * TODO: These are all cookie-cutter factory files, more or less, that use my particular (preferred) style for handling bulk operations
 * BUT - these could also be generated instead of copy/pasted/tweaked, and could be refactored into a parent abstract class
 */

package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;



public class FactFactory extends NameIdGroupFactory {
	
	public FactFactory(){
		super();
		this.primaryTableName = "fact";
		this.tableNames.add(primaryTableName);
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.FACT;
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			// restrict any columns
		}
	}

	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		FactType fact = (FactType)obj;
		if(fact.getPopulated().booleanValue()) return;
		fact.setPopulated(true);
		updateToCache(fact);
	}
	
	
	public FactType newFact(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		FactType obj = new FactType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setFactType(FactEnumType.UNKNOWN);
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.FACT);
		obj.setFactoryType(FactoryEnumType.UNKNOWN);
		obj.setSourceDataType(SqlDataEnumType.UNKNOWN);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		FactType obj = (FactType)object;
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.FACTTYPE), obj.getFactType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.FACTORYTYPE), obj.getFactoryType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.FACTDATA), obj.getFactData());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			row.setCellValue(Columns.get(ColumnEnumType.SCORE), obj.getScore());
			row.setCellValue(Columns.get(ColumnEnumType.LOGICALORDER), obj.getLogicalOrder());
			row.setCellValue(Columns.get(ColumnEnumType.SOURCEURN), obj.getSourceUrn());
			row.setCellValue(Columns.get(ColumnEnumType.SOURCEURL), obj.getSourceUrl());
			row.setCellValue(Columns.get(ColumnEnumType.SOURCETYPE), obj.getSourceType());
			row.setCellValue(Columns.get(ColumnEnumType.SOURCEDATATYPE), obj.getSourceDataType().toString());
			
			if (insertRow(row)) return true;
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		FactType newObj = new FactType();
		newObj.setNameType(NameEnumType.FACT);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setFactType(FactEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.FACTTYPE))));
		newObj.setFactoryType(FactoryEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.FACTORYTYPE))));
		newObj.setScore(rset.getInt(Columns.get(ColumnEnumType.SCORE)));
		newObj.setFactData(rset.getString(Columns.get(ColumnEnumType.FACTDATA)));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		newObj.setSourceUrn(rset.getString(Columns.get(ColumnEnumType.SOURCEURN)));
		newObj.setSourceUrl(rset.getString(Columns.get(ColumnEnumType.SOURCEURL)));
		newObj.setSourceType(rset.getString(Columns.get(ColumnEnumType.SOURCETYPE)));
		newObj.setSourceDataType(SqlDataEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.SOURCEDATATYPE))));
		newObj.setLogicalOrder(rset.getInt(Columns.get(ColumnEnumType.LOGICALORDER)));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		FactType data = (FactType)object;
		removeFromCache(data);
		removeFromCache(data,getUrnCacheKey(data));
		return super.update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		FactType useMap = (FactType)map;
		fields.add(QueryFields.getFieldScore(useMap.getScore()));
		fields.add(QueryFields.getFieldFactData(useMap.getFactData()));
		fields.add(QueryFields.getFieldSourceUrn(useMap.getSourceUrn()));
		fields.add(QueryFields.getFieldSourceUrl(useMap.getSourceUrl()));
		fields.add(QueryFields.getFieldSourceType(useMap.getSourceType()));
		fields.add(QueryFields.getFieldSourceDataType(useMap.getSourceDataType()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldFactType(useMap.getFactType()));
		fields.add(QueryFields.getFieldFactoryType(useMap.getFactoryType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteFactsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteFactsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		FactType obj = (FactType)object;
		removeFromCache(obj);
		removeFromCache(obj,getUrnCacheKey(obj));
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteFactsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	public int deleteFactsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		return deleteFactsByIds(ids, group.getOrganizationId());
	}

	
	public List<FactType> getFacts(QueryField[] matches, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organizationId);
		return convertList(lst);

	}
	public List<FactType>  getFactList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<FactType> getFactListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
