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
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
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
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.ScheduleType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;

public class ScheduleFactory extends NameIdGroupFactory {
	
	public ScheduleFactory(){
		super();
		this.primaryTableName = "schedule";
		this.tableNames.add(primaryTableName);
		factoryType = FactoryEnumType.SCHEDULE;
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("schedule")){
			/// restrict columns
		}
	}

	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		ScheduleType sched = (ScheduleType)obj;
		if(sched.getPopulated().booleanValue()) return;
		sched.getBudgets().addAll(((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).getBudgetsFromParticipation(sched));
		sched.getGoals().addAll(((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).getGoalsFromParticipation(sched));
		sched.setPopulated(true);
		updateToCache(sched);
	}
	
	public ScheduleType newSchedule(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		ScheduleType obj = new ScheduleType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		Calendar now = Calendar.getInstance();
		XMLGregorianCalendar cal = CalendarUtil.getXmlGregorianCalendar(now.getTime()); 
		obj.setStartTime(cal);
		obj.setEndTime(cal);
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.SCHEDULE);
		return obj;
	}
	

	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ScheduleType obj = (ScheduleType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Schedule without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.STARTTIME),obj.getStartTime());
			row.setCellValue(Columns.get(ColumnEnumType.ENDTIME),obj.getEndTime());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			if (insertRow(row)){
				ScheduleType cobj = (bulkMode ? obj : (ScheduleType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < obj.getBudgets().size();i++){
					part = ((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).newBudgetParticipation(cobj,obj.getBudgets().get(i));
						
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).add(part);
					else ((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).add(part);
				}

				for(int i = 0; i < obj.getGoals().size();i++){
					part = ((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).newGoalParticipation(cobj,obj.getGoals().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).add(part);
					((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).add(part);
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
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		ScheduleType newObj = new ScheduleType();
		newObj.setNameType(NameEnumType.SCHEDULE);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setStartTime(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.STARTTIME))));
		newObj.setEndTime(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.ENDTIME))));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ScheduleType data = (ScheduleType)object;
		
		boolean outBool = false;
		
		removeFromCache(data);
		if(update(data, null)){
			try{
				
				/// Budgets
				///
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).getBudgetParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getBudgets().size();i++){
					if(!set.contains(data.getBudgets().get(i).getId())){
						((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).add(((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).newBudgetParticipation(data,data.getBudgets().get(i)));
					}
					else{
						set.remove(data.getBudgets().get(i).getId());
					}
				}
				((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Goals
				set.clear();
				maps = ((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).getGoalParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getGoals().size();i++){
					if(!set.contains(data.getGoals().get(i).getId())){
						((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).add(((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).newGoalParticipation(data,data.getGoals().get(i)));
					}
					else{
						set.remove(data.getGoals().get(i).getId());
					}
				}
				((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

				
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
		ScheduleType useMap = (ScheduleType)map;
		fields.add(QueryFields.getFieldStartTime(useMap.getStartTime()));
		fields.add(QueryFields.getFieldEndTime(useMap.getEndTime()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteSchedulesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteSchedulesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).deleteParticipations(obj);
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteSchedulesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((ScheduleParticipationFactory)Factories.getFactory(FactoryEnumType.SCHEDULEPARTICIPATION)).deleteParticipations(ids, organizationId);
		}
		return deleted;
	}
	public int deleteSchedulesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteSchedulesByIds(ids, group.getOrganizationId());
	}
	
	
	public List<ScheduleType>  getScheduleList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ScheduleType> getScheduleListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
