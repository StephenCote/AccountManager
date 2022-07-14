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
package org.cote.rocket.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.util.LogicalTypeComparator;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.ProcessType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class ProcessFactory extends NameIdGroupFactory {
	
	public ProcessFactory(){
		super();
		this.primaryTableName = "process";
		this.tableNames.add(primaryTableName);
		factoryType = FactoryEnumType.PROCESS;
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// restrict columns
		}
	}

	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		ProcessType process = (ProcessType)obj;
		if(process.getPopulated().booleanValue()) return;
		process.setPopulated(true);
		process.getBudgets().addAll(((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).getBudgetsFromParticipation(process));
		process.getSteps().addAll(((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).getProcessStepsFromParticipation(process));
		Collections.sort(process.getSteps(),new LogicalTypeComparator());
		
		updateToCache(process);
	}
	
	
	public ProcessType newProcess(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		ProcessType obj = new ProcessType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.PROCESS);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ProcessType obj = (ProcessType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Process without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.ITERATES),obj.getIterates());
			row.setCellValue(Columns.get(ColumnEnumType.LOGICALORDER),obj.getLogicalOrder());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			if (insertRow(row)){
				ProcessType cobj = (bulkMode ? obj : (ProcessType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < obj.getBudgets().size();i++){
					part = ((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).newBudgetParticipation(cobj,obj.getBudgets().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PROCESSPARTICIPATION)).add(part);
					else ((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).add(part);
				}

				for(int i = 0; i < obj.getSteps().size();i++){
					part = ((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).newProcessStepParticipation(cobj,obj.getSteps().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PROCESSPARTICIPATION)).add(part);
					else ((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).add(part);
				}
				return true;
			}
		}
		catch(DataAccessException | ArgumentException ae){
			throw new FactoryException(ae.getMessage());
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		ProcessType newObj = new ProcessType();
		newObj.setNameType(NameEnumType.PROCESS);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setIterates(rset.getBoolean(Columns.get(ColumnEnumType.ITERATES)));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		newObj.setLogicalOrder(rset.getInt(Columns.get(ColumnEnumType.LOGICALORDER)));

		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ProcessType data = (ProcessType)object;
		boolean outBool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
				
				/// Budgets
				///
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).getBudgetParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getBudgets().size();i++){
					if(!set.contains(data.getBudgets().get(i).getId())){
						((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).add(((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).newBudgetParticipation(data,data.getBudgets().get(i)));
					}
					else{
						set.remove(data.getBudgets().get(i).getId());
					}
				}
				((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// ProcessSteps
				set.clear();
				maps = ((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).getProcessStepParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getSteps().size();i++){
					if(!set.contains(data.getSteps().get(i).getId())){
						((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).add(((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).newProcessStepParticipation(data,data.getSteps().get(i)));
					}
					else{
						set.remove(data.getSteps().get(i).getId());
					}
				}
				((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
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
		ProcessType useMap = (ProcessType)map;
		fields.add(QueryFields.getFieldIterates(useMap.getIterates()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteProcesssByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteProcesssByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteProcesssByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).deleteParticipations(ids, organizationId);
		}
		return deleted;
	}
	public int deleteProcesssInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteProcesssByIds(ids, group.getOrganizationId());
	}
	
	
	public List<ProcessType>  getProcessList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ProcessType> getProcessListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
