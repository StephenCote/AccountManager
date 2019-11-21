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
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.MethodologyType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.WorkType;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class StageFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.STAGE, StageFactory.class); }
	public StageFactory(){
		super();
		this.tableNames.add("stage");
		factoryType = FactoryEnumType.STAGE;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("stage")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		StageType stage = (StageType)obj;
		
		if(stage.getWork() != null) ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).depopulate(stage.getWork());
		if(stage.getSchedule() != null) ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).depopulate(stage.getSchedule());
		if(stage.getBudget() != null) ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).depopulate(stage.getBudget());
		
		stage.setPopulated(false);
		updateToCache(stage);

	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		StageType stage = (StageType)obj;
		if(stage.getPopulated()) return;
		
		/// Stage doesn't have anything direct to populate,
		/// but use this to populate children
		
		stage.setPopulated(true);
		updateToCache(stage);
	}
	
	
	public StageType newStage(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		StageType obj = new StageType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.STAGE);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		StageType obj = (StageType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Stage without a group");

		DataRow row = prepareAdd(obj, "stage");
		try{
			row.setCellValue("scheduleid", (obj.getSchedule() != null ? obj.getSchedule().getId() : 0));
			row.setCellValue("workid", (obj.getWork() != null ? obj.getWork().getId() : 0));
			row.setCellValue("budgetid", (obj.getBudget() != null ? obj.getBudget().getId() : 0));
			row.setCellValue("methodologyid", (obj.getMethodology() != null ? obj.getMethodology().getId() : 0));
			row.setCellValue("logicalorder",obj.getLogicalOrder());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("groupid", obj.getGroupId());
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
		StageType newObj = new StageType();
		newObj.setNameType(NameEnumType.STAGE);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		long work_id = rset.getLong("workid");
		long budget_id = rset.getLong("budgetid");
		long methodology_id = rset.getLong("methodologyid");
		long schedule_id = rset.getLong("scheduleid");
		if(schedule_id > 0) newObj.setSchedule((ScheduleType)((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).getById(schedule_id, newObj.getOrganizationId()));
		if(work_id > 0) newObj.setWork((WorkType)((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).getById(work_id, newObj.getOrganizationId()));
		if(budget_id > 0) newObj.setBudget((BudgetType)((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).getById(budget_id, newObj.getOrganizationId()));
		if(methodology_id > 0) newObj.setMethodology((MethodologyType)((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).getById(methodology_id, newObj.getOrganizationId()));
		
		newObj.setDescription(rset.getString("description"));
		newObj.setLogicalOrder(rset.getInt("logicalorder"));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		NameIdType data = (NameIdType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		StageType useMap = (StageType)map;
		fields.add(QueryFields.getFieldBudgetId((useMap.getBudget() != null ? useMap.getBudget().getId() : 0)));
		fields.add(QueryFields.getFieldMethodologyId((useMap.getMethodology() != null ? useMap.getMethodology().getId() : 0)));
		fields.add(QueryFields.getFieldWorkId((useMap.getWork() != null ? useMap.getWork().getId() : 0)));
		fields.add(QueryFields.getFieldScheduleId((useMap.getSchedule() != null ? useMap.getSchedule().getId() : 0)));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteStagesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteStagesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteStagesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteStagesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteStagesByIds(ids, group.getOrganizationId());
	}
	
	
	public List<StageType>  getStageList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<StageType> getStageListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
