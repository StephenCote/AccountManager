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
import java.util.Collections;
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
import org.cote.accountmanager.data.util.LogicalTypeComparator;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;


public class FunctionFactory extends NameIdGroupFactory {
	
	public FunctionFactory(){
		super();
		this.primaryTableName = "function";
		this.tableNames.add(primaryTableName);
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.FUNCTION;
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			// restrict column names
		}
	}

	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		FunctionType func = (FunctionType)obj;
		if(func.getPopulated().booleanValue()) return;
		func.getFacts().addAll(((FunctionParticipationFactory)Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).getFunctionFactsFromParticipation(func));
		Collections.sort(func.getFacts(),new LogicalTypeComparator());
		func.setPopulated(true);
		updateToCache(func);
	}
	
	
	public FunctionType newFunction(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		FunctionType obj = new FunctionType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setFunctionType(FunctionEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.FUNCTION);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		FunctionType obj = (FunctionType)object;
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.FUNCTIONTYPE), obj.getFunctionType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			row.setCellValue(Columns.get(ColumnEnumType.SCORE), obj.getScore());
			row.setCellValue(Columns.get(ColumnEnumType.LOGICALORDER), obj.getLogicalOrder());
			row.setCellValue(Columns.get(ColumnEnumType.SOURCEURN), obj.getFunctionData().getUrn());
			if(obj.getFunctionData() == null){
				row.setCellValue(Columns.get(ColumnEnumType.SOURCEURL), obj.getSourceUrl());
			}
			if (insertRow(row)){
				FunctionType cobj = (bulkMode ? obj : (FunctionType)getByNameInGroup(obj.getName(), obj.getGroupId(),obj.getOrganizationId()));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < obj.getFacts().size();i++){
					part = ((FunctionParticipationFactory)Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).newFunctionFactParticipation(cobj,obj.getFacts().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).add(part);
					else ((FunctionParticipationFactory)Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).add(part);
				}
				return true;
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} catch (ArgumentException e) {
			
			throw new FactoryException(e.getMessage());
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		FunctionType newObj = new FunctionType();
		newObj.setNameType(NameEnumType.FUNCTION);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setFunctionType(FunctionEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.FUNCTIONTYPE))));
		newObj.setScore(rset.getInt(Columns.get(ColumnEnumType.SCORE)));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		newObj.setSourceUrn(rset.getString(Columns.get(ColumnEnumType.SOURCEURN)));
		newObj.setSourceUrl(rset.getString(Columns.get(ColumnEnumType.SOURCEURL)));
		newObj.setLogicalOrder(rset.getInt(Columns.get(ColumnEnumType.LOGICALORDER)));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		FunctionType data = (FunctionType)object;
		removeFromCache(data);
		boolean outBool = false;
		if(super.update(data, null)){
			try{
				
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((FunctionParticipationFactory)Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).getFunctionFactParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getFacts().size();i++){
					if(!set.contains(data.getFacts().get(i).getId())){
						((FunctionParticipationFactory)Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).add(((FunctionParticipationFactory)Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).newFunctionFactParticipation(data,data.getFacts().get(i)));
					}
					else{
						set.remove(data.getFacts().get(i).getId());
					}
				}
				((FunctionParticipationFactory)Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				outBool = true;
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}
		}
		return outBool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		FunctionType useMap = (FunctionType)map;
		fields.add(QueryFields.getFieldScore(useMap.getScore()));
		fields.add(QueryFields.getFieldSourceUrn(useMap.getSourceUrn()));
		fields.add(QueryFields.getFieldSourceUrl(useMap.getSourceUrl()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldFunctionType(useMap.getFunctionType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteFunctionsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteFunctionsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		FunctionType obj = (FunctionType)object;
		removeFromCache(obj);
		int deleted = deleteFunctionsByIds(new long[]{obj.getId()},obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteFunctionsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((FunctionParticipationFactory)Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION)).deleteParticipations(ids, organizationId);
		}
		return deleted;
	}
	public int deleteFunctionsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteFunctionsByIds(ids, group.getOrganizationId());
	}
	public List<FactType> getFunctions(QueryField[] matches, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organizationId);
		return convertList(lst);

	}
	
	public List<FunctionType>  getFunctionList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<FunctionType> getFunctionListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
