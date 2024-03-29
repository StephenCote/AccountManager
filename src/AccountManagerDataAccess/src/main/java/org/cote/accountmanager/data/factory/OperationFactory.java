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
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;



public class OperationFactory extends NameIdGroupFactory {
	
	public OperationFactory(){
		super();
		this.primaryTableName = "operation";
		this.tableNames.add(primaryTableName);
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.OPERATION;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("operation")){
			/// restrict column names
		}
	}

	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		OperationType oper = (OperationType)obj;
		if(oper.getPopulated().booleanValue()) return;
		oper.setPopulated(true);
		updateToCache(oper);
	}
	
	
	public OperationType newOperation(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		OperationType obj = new OperationType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setOperationType(OperationEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.OPERATION);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		OperationType obj = (OperationType)object;
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.OPERATIONTYPE), obj.getOperationType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			row.setCellValue(Columns.get(ColumnEnumType.SCORE), obj.getScore());
			row.setCellValue(Columns.get(ColumnEnumType.LOGICALORDER), obj.getLogicalOrder());
			row.setCellValue(Columns.get(ColumnEnumType.OPERATION), obj.getOperation());
			
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
		OperationType newObj = new OperationType();
		newObj.setNameType(NameEnumType.OPERATION);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setOperationType(OperationEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.OPERATIONTYPE))));
		newObj.setScore(rset.getInt(Columns.get(ColumnEnumType.SCORE)));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		newObj.setOperation(rset.getString(Columns.get(ColumnEnumType.OPERATION)));
		newObj.setLogicalOrder(rset.getInt(Columns.get(ColumnEnumType.LOGICALORDER)));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		OperationType data = (OperationType)object;
		removeFromCache(data);
		removeFromCache(data,getUrnCacheKey(data));
		return super.update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		OperationType useMap = (OperationType)map;
		fields.add(QueryFields.getFieldScore(useMap.getScore()));
		fields.add(QueryFields.getFieldOperation(useMap.getOperation()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldOperationType(useMap.getOperationType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteOperationsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteOperationsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		OperationType obj = (OperationType)object;
		removeFromCache(obj);
		removeFromCache(obj,getUrnCacheKey(obj));
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteOperationsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	public int deleteOperationsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		return deleteOperationsByIds(ids, group.getOrganizationId());
	}
	public String getUrnCacheKey(OperationType operation){
		return getUrnCacheKey(operation.getUrn(),operation.getOrganizationId());
	}
	public String getUrnCacheKey(String urn, long organizationId){
		return urn + "-" + organizationId;
	}
	
	public List<OperationType> getOperations(QueryField[] matches, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organizationId);
		return convertList(lst);

	}
	public List<OperationType>  getOperationList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<OperationType> getOperationListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
