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
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.util.LogicalTypeComparator;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;


public class FunctionFactory extends NameIdGroupFactory {
	
	public FunctionFactory(){
		super();
		this.tableNames.add("function");
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.FUNCTION;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("function")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		FunctionType func = (FunctionType)obj;
		if(func.getPopulated()) return;
		func.getFacts().addAll(Factories.getFunctionParticipationFactory().getFunctionFactsFromParticipation(func));
		Collections.sort(func.getFacts(),new LogicalTypeComparator());
		func.setPopulated(true);
		updateToCache(func);
	}
	
	
	public FunctionType newFunction(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		FunctionType obj = new FunctionType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setFunctionType(FunctionEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.FUNCTION);
		return obj;
	}
	
	public boolean addFunction(FunctionType obj) throws FactoryException
	{
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "function");
		try{
			row.setCellValue("functiontype", obj.getFunctionType().toString());
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("score", obj.getScore());
			//row.setCellValue("urn", obj.getUrn());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
			if(obj.getFunctionData() != null){
				row.setCellValue("sourceurn", obj.getFunctionData().getUrn());
				//row.setCellValue("sourceurl", obj.getSourceUrl());				
			}
			else{
				row.setCellValue("sourceurn", obj.getSourceUrn());
				row.setCellValue("sourceurl", obj.getSourceUrl());
			}
			if (insertRow(row)){
				FunctionType cobj = (bulkMode ? obj : (FunctionType)getByNameInGroup(obj.getName(), obj.getGroupId(),obj.getOrganizationId()));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < obj.getFacts().size();i++){
					part = Factories.getFunctionParticipationFactory().newFunctionFactParticipation(cobj,obj.getFacts().get(i));
					if(bulkMode) BulkFactories.getBulkFunctionParticipationFactory().addParticipant(part);
					else Factories.getFunctionParticipationFactory().addParticipant(part);
				}
				return true;
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			throw new FactoryException(e.getMessage());
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		FunctionType new_obj = new FunctionType();
		new_obj.setNameType(NameEnumType.FUNCTION);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		new_obj.setFunctionType(FunctionEnumType.valueOf(rset.getString("functiontype")));
		//new_obj.setUrn(rset.getString("urn"));
		new_obj.setScore(rset.getInt("score"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setSourceUrn(rset.getString("sourceurn"));
		new_obj.setSourceUrl(rset.getString("sourceurl"));
		new_obj.setLogicalOrder(rset.getInt("logicalorder"));
		return new_obj;
	}
	public boolean updateFunction(FunctionType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		boolean out_bool = false;
		if(update(data, null)){
			try{
				
				Set<Long> set = new HashSet<Long>();
				BaseParticipantType[] maps = Factories.getFunctionParticipationFactory().getFunctionFactParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getFacts().size();i++){
					if(set.contains(data.getFacts().get(i).getId())== false){
						Factories.getFunctionParticipationFactory().addParticipant(Factories.getFunctionParticipationFactory().newFunctionFactParticipation(data,data.getFacts().get(i)));
					}
					else{
						set.remove(data.getFacts().get(i).getId());
					}
				}
				Factories.getFunctionParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				out_bool = true;
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}
		}
		return out_bool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		FunctionType use_map = (FunctionType)map;
		//fields.add(QueryFields.getFieldUrn(use_map.getUrn()));
		fields.add(QueryFields.getFieldScore(use_map.getScore()));
		fields.add(QueryFields.getFieldSourceUrn(use_map.getSourceUrn()));
		fields.add(QueryFields.getFieldSourceUrl(use_map.getSourceUrl()));
		fields.add(QueryFields.getFieldLogicalOrder(use_map.getLogicalOrder()));
		fields.add(QueryFields.getFieldFunctionType(use_map.getFunctionType()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroupId()));
	}
	public int deleteFunctionsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteFunctionsByIds(ids, user.getOrganizationId());
	}

	public boolean deleteFunction(FunctionType obj) throws FactoryException
	{
		removeFromCache(obj);
		//int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		int deleted = deleteFunctionsByIds(new long[]{obj.getId()},obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteFunctionsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			Factories.getFunctionParticipationFactory().deleteParticipations(ids, organizationId);
			/*
			Factories.getFunctionParticipationFactory().deleteParticipations(ids, organizationId);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
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
		return getPaginatedList(fields, startRecord, recordCount, organizationId);
	}
	public List<FunctionType> getFunctionListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organizationId);
	}
	
}
