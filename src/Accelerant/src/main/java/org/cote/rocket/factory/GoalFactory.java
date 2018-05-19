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
package org.cote.rocket.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.GoalType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.types.GoalEnumType;
import org.cote.propellant.objects.types.PriorityEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;
public class GoalFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.GOAL, GoalFactory.class); }
	public GoalFactory(){
		super();
		this.tableNames.add("goal");
		factoryType = FactoryEnumType.GOAL;
	}

	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("goal")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		GoalType goal = (GoalType)obj;
		goal.getDependencies().clear();	
		goal.getRequirements().clear();
		goal.getCases().clear();
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		GoalType goal = (GoalType)obj;
		if(goal.getPopulated() == true) return;
		goal.getDependencies().addAll(((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).getDependenciesFromParticipation(goal));
		goal.getRequirements().addAll(((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).getRequirementsFromParticipation(goal));
		goal.getCases().addAll(((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).getCasesFromParticipation(goal));
		goal.setPopulated(true);
		updateToCache(goal);
	}
	
	public GoalType newGoal(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		GoalType obj = new GoalType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setGoalType(GoalEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.GOAL);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		GoalType obj = (GoalType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Goal without a group");

		DataRow row = prepareAdd(obj, "goal");
		try{
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("goaltype", obj.getGoalType().toString());
			
			row.setCellValue("logicalorder",obj.getLogicalOrder());
			row.setCellValue("priority",obj.getPriority().toString());
			if(obj.getSchedule() != null) row.setCellValue("scheduleid", obj.getSchedule().getId());
			if(obj.getAssigned() != null) row.setCellValue("resourceid", obj.getAssigned().getId());
			if(obj.getBudget() != null) row.setCellValue("budgetid", obj.getBudget().getId());
			if (insertRow(row)){

				try{
					GoalType cobj = (bulkMode ? obj : (GoalType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					BulkFactories.getBulkFactory().setDirty(factoryType);
					BaseParticipantType part = null;
					for(int i = 0; i < obj.getDependencies().size();i++){
						part = ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).newDependencyParticipation(cobj,obj.getDependencies().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.GOALPARTICIPATION)).add(part);
						else ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getRequirements().size();i++){
						part = ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).newRequirementParticipation(cobj,obj.getRequirements().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.GOALPARTICIPATION)).add(part);
						else ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getCases().size();i++){
						part = ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).newCaseParticipation(cobj,obj.getCases().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.GOALPARTICIPATION)).add(part);
						else ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).add(part);
					}
				}
				catch(ArgumentException ae){
					throw new FactoryException(ae.getMessage());
				}
				return true;
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	
	
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		GoalType newObj = new GoalType();
		newObj.setNameType(NameEnumType.GOAL);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setDescription(rset.getString("description"));
		newObj.setGoalType(GoalEnumType.valueOf(rset.getString("goaltype")));
		
		newObj.setLogicalOrder(rset.getInt("logicalorder"));
		newObj.setPriority(PriorityEnumType.fromValue(rset.getString("priority")));
		
		long schedule_id = rset.getLong("scheduleid");
		if(schedule_id > 0L) newObj.setSchedule((ScheduleType)((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).getById(schedule_id, newObj.getOrganizationId()));

		long budget_id = rset.getLong("budgetid");
		if(budget_id > 0L) newObj.setBudget((BudgetType)((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).getById(budget_id, newObj.getOrganizationId()));

		long resource_id = rset.getLong("resourceid");
		if(resource_id > 0L) newObj.setAssigned((ResourceType)((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).getById(resource_id, newObj.getOrganizationId()));


		
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		GoalType data = (GoalType)object;
		boolean outBool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).getDependencyParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getDependencies().size();i++){
					if(set.contains(data.getDependencies().get(i).getId())== false){
						((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).add(((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).newDependencyParticipation(data,data.getDependencies().get(i)));
					}
					else{
						set.remove(data.getDependencies().get(i).getId());
					}
				}
				((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

				set.clear();
				maps = ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).getRequirementParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getRequirements().size();i++){
					if(set.contains(data.getRequirements().get(i).getId())== false){
						((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).add(((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).newRequirementParticipation(data,data.getRequirements().get(i)));
					}
					else{
						set.remove(data.getRequirements().get(i).getId());
					}
				}
				((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				set.clear();
				maps = ((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).getCaseParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getCases().size();i++){
					if(set.contains(data.getCases().get(i).getId())== false){
						((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).add(((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).newCaseParticipation(data,data.getCases().get(i)));
					}
					else{
						set.remove(data.getCases().get(i).getId());
					}
				}
				((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				
				
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}
			outBool = true;
		}
		return outBool;

	}
	/*
	protected <T,U> void deltaParticipation(T obj, ParticipantEnumType parType, List<U> list){
		Set<Long> set = new HashSet<>();
		NameIdType parObj = (NameIdType)obj;
		BaseParticipationFactory bpf = Factories.getFactory(this.factoryType);
		BaseParticipantType[] maps = convertList( bpf.getParticipations(new NameIdType[]{parObj}, parType)).toArray(new BaseParticipantType[0]);
		for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
		
		for(int i = 0; i < data.getDependencies().size();i++){
			if(set.contains(data.getDependencies().get(i).getId())== false){
				((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).add(((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).newDependencyParticipation(data,data.getDependencies().get(i)));
			}
			else{
				set.remove(data.getDependencies().get(i).getId());
			}
		}
		((GoalParticipationFactory)Factories.getFactory(FactoryEnumType.GOALPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

	}
	*/
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		GoalType useMap = (GoalType)map;
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldGoalType(useMap.getGoalType()));
		
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldPriority(useMap.getPriority()));
		
		fields.add(QueryFields.getFieldScheduleId(useMap.getSchedule() != null ? useMap.getSchedule().getId() : 0L));
		fields.add(QueryFields.getFieldBudgetId(useMap.getBudget() != null ? useMap.getBudget().getId() : 0L));
		fields.add(QueryFields.getFieldResourceId(useMap.getAssigned() != null ? useMap.getAssigned().getId() : 0L));
	}
	public int deleteGoalsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteGoalsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteGoalsByIds(long[] ids, long organizationId) throws FactoryException
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
	public int deleteGoalsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteGoalsByIds(ids, group.getOrganizationId());
	}
	
	public List<GoalType>  getGoalListByGroup(DirectoryGroupType group, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, startRecord, recordCount, organizationId);
	}

	public List<GoalType>  getGoalList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<GoalType> getGoalListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
