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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import org.cote.accountmanager.data.util.LogicalTypeComparator;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.types.TaskStatusEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class TaskFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.TASK, TaskFactory.class); }
	public TaskFactory(){
		super();
		this.hasParentId=true;
		this.tableNames.add("task");
		factoryType = FactoryEnumType.TASK;
		this.clusterByParent = true;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getParentId() + "-" + t.getGroupId();
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("task")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		TaskType task = (TaskType)obj;
		task.getArtifacts().clear();
		task.getRequirements().clear();
		task.getWork().clear();
		task.getActualCost().clear();
		task.getActualTime().clear();
		task.getResources().clear();
		task.getDependencies().clear();
		for(int i = 0; i < task.getChildTasks().size();i++){
			depopulate(task.getChildTasks().get(i));
		}
		task.getChildTasks().clear();
		task.setPopulated(false);
		updateToCache(task);
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		TaskType task = (TaskType)obj;
		if(task.getPopulated()) return;
		/*
		if(task.getEstimateId() > 0){
			task.setEstimate((EstimateType)((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getById(task.getEstimateId(), task.getOrganizationId()));
		}
		*/
		
		task.getArtifacts().addAll(((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getArtifactsFromParticipation(task));
		task.getRequirements().addAll(((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getRequirementsFromParticipation(task));
		task.getWork().addAll(((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getWorkFromParticipation(task));
		task.getNotes().addAll(((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getNotesFromParticipation(task));
		task.getActualTime().addAll(((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getTimesFromParticipation(task));
		task.getActualCost().addAll(((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getCostsFromParticipation(task));
		task.getResources().addAll(((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getResourcesFromParticipation(task));
		task.getDependencies().addAll(((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getDependenciesFromParticipation(task));
		task.getChildTasks().addAll(getChildTaskList(task));
		Collections.sort(task.getChildTasks(),new LogicalTypeComparator());
		task.setPopulated(true);
		updateToCache(task);
	}
	public TaskType newTask(UserType user, TaskType parentTask) throws ArgumentException
	{
		TaskType obj = newTask(user,parentTask.getGroupId());
		obj.setParentId(parentTask.getId());
		return obj;
	}
	public TaskType newTask(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		TaskType obj = new TaskType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setTaskStatus(TaskStatusEnumType.UNKNOWN);
		obj.setGroupId(groupId);
		Calendar now = Calendar.getInstance();
		XMLGregorianCalendar xcal = CalendarUtil.getXmlGregorianCalendar(now.getTime());
		obj.setStartDate(xcal);
		obj.setCreatedDate(xcal);
		obj.setModifiedDate(xcal);
		obj.setCompletedDate(xcal);
		obj.setDueDate(xcal);
		obj.setNameType(NameEnumType.TASK);
		
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		TaskType obj = (TaskType)object;

		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Task without a group");

		DataRow row = prepareAdd(obj, "task");
		try{
			if(obj.getEstimate() != null) row.setCellValue("estimateid", obj.getEstimate().getId());
			row.setCellValue("logicalorder",obj.getLogicalOrder());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("taskstatus", obj.getTaskStatus().toString());
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("startdate",obj.getStartDate());
			row.setCellValue("createddate",obj.getCreatedDate());
			row.setCellValue("modifieddate",obj.getModifiedDate());
			row.setCellValue("completeddate",obj.getCompletedDate());
			row.setCellValue("duedate",obj.getDueDate());

			if (insertRow(row)){
				try{
					TaskType cobj = null;
					if(bulkMode) cobj = obj;
					else if(obj.getParentId() > 0L){
						TaskType parent = getById(obj.getParentId(),obj.getOrganizationId());
						if(parent == null) throw new FactoryException("Unable to update orphaned task without correcting the parent");
						cobj = getByNameInGroup(obj.getName(),obj.getParentId(),((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(),obj.getOrganizationId()));
						//(bulkMode ? obj : obj.getParentId() > 0L ? (TaskType)getByNameInGroup(obj.getName(),obj.get);
					}
					else{
						cobj = (TaskType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId()));
					}
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					
					logger.info("Adding initial " + obj.getResources().size() + " resources on new task " + obj.getName() + " with id " + cobj.getId());
					
					BaseParticipantType part = null;
					if(bulkMode) BulkFactories.getBulkFactory().setDirty(FactoryEnumType.TASKPARTICIPATION);
					for(int i = 0; i < obj.getActualCost().size();i++){
						part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newCostParticipation(cobj,obj.getActualCost().get(i));
						if(bulkMode)((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
						else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getActualTime().size();i++){
						part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newTimeParticipation(cobj,obj.getActualTime().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
						else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
					}
					
					for(int i = 0; i < obj.getWork().size();i++){
						part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newWorkParticipation(cobj,obj.getWork().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
						else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getNotes().size();i++){
						part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newNoteParticipation(cobj,obj.getNotes().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
						else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
					}
	
					for(int i = 0; i < obj.getResources().size();i++){
						part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newResourceParticipation(cobj,obj.getResources().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
						else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
					}
	
					for(int i = 0; i < obj.getRequirements().size();i++){
						part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newRequirementParticipation(cobj,obj.getRequirements().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
						else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getDependencies().size();i++){
						part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newDependencyParticipation(cobj,obj.getDependencies().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
						else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getArtifacts().size();i++){
						part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newArtifactParticipation(cobj,obj.getArtifacts().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
						else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
					}

					return true;
				}
				catch(ArgumentException ae){
					logger.error(FactoryException.LOGICAL_EXCEPTION,ae);
					throw new FactoryException(ae.getMessage());
					
				}
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
		TaskType newObj = new TaskType();
		newObj.setNameType(NameEnumType.TASK);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		long estimate_id = rset.getLong("estimateid");
		if(estimate_id > 0) newObj.setEstimate((EstimateType)((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getById(estimate_id, newObj.getOrganizationId()));
		newObj.setTaskStatus(TaskStatusEnumType.valueOf(rset.getString("TaskStatus")));
		newObj.setDescription(rset.getString("description"));
		newObj.setLogicalOrder(rset.getInt("logicalorder"));
		newObj.setStartDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("startdate")));
		newObj.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		newObj.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		newObj.setCompletedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("completeddate")));
		newObj.setDueDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("duedate")));

		
		return newObj;
	}

	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		TaskType data = (TaskType)object;
		boolean outBool = false;
		removeBranchFromCache(data);
		//logger.info("Updating Task: " + data.getName());
		if(update(data, null)){
			try{
			/// Goals
			Set<Long> set = new HashSet<>();
			List<Long> delIds = new ArrayList<>();
			
			BaseParticipantType part = null;
			if(bulkMode) BulkFactories.getBulkFactory().setDirty(FactoryEnumType.TASKPARTICIPATION);

			BaseParticipantType[] maps = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getWorkParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			for(int i = 0; i < data.getWork().size();i++){
				if(set.contains(data.getWork().get(i).getId())== false){
					part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newWorkParticipation(data,data.getWork().get(i));
					if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.TASKPARTICIPATION, part);
					else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
				}
				else{
					set.remove(data.getWork().get(i).getId());
				}
			}
			delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
			//if(set.size() > 0) ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// ActualTimes
			set.clear();
			maps = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getTimeParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			for(int i = 0; i < data.getActualTime().size();i++){
				if(set.contains(data.getActualTime().get(i).getId())== false){
					part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newTimeParticipation(data,data.getActualTime().get(i));
					if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.TASKPARTICIPATION, part);
					else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
				}
				else{
					set.remove(data.getActualTime().get(i).getId());
				}
			}
			delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
			//if(set.size() > 0) ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// ActualCosts
			set.clear();
			maps = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getCostParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getActualCost().size();i++){
				if(set.contains(data.getActualCost().get(i).getId())== false){
					part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newCostParticipation(data,data.getActualCost().get(i));
					if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.TASKPARTICIPATION, part);
					else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);
				}
				else{
					set.remove(data.getActualCost().get(i).getId());
				}
			}
			delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
			
//			System.out.println("Net delete ActualCost parts: " + set.size());
			//if(set.size() > 0)  ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// Notes
			set.clear();
			maps = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getNoteParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getNotes().size();i++){
				if(set.contains(data.getNotes().get(i).getId())== false){
					part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newNoteParticipation(data,data.getNotes().get(i));
					if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.TASKPARTICIPATION, part);
					else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);

				}
				else{
					set.remove(data.getNotes().get(i).getId());
				}
			}
//			System.out.println("Net delete Note parts: " + set.size());
			delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
			//if(set.size() > 0) ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// Resources
			set.clear();
			maps = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getResourceParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getResources().size();i++){
				if(set.contains(data.getResources().get(i).getId())== false){
					part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newResourceParticipation(data,data.getResources().get(i));
					if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.TASKPARTICIPATION, part);
					else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);

				}
				else{
					set.remove(data.getResources().get(i).getId());
				}
			}
			delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
			//if(set.size() > 0) ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// Requirements
			set.clear();
			maps = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getRequirementParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getRequirements().size();i++){
				if(set.contains(data.getRequirements().get(i).getId())== false){
					part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newRequirementParticipation(data,data.getRequirements().get(i));
					if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.TASKPARTICIPATION, part);
					else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);

				}
				else{
					set.remove(data.getRequirements().get(i).getId());
				}
			}
			
			delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
			//if(set.size() > 0) ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// Dependencies
			set.clear();
			maps = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getDependencyParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getDependencies().size();i++){
				if(set.contains(data.getDependencies().get(i).getId())== false){
					part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newDependencyParticipation(data,data.getDependencies().get(i));
					if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.TASKPARTICIPATION, part);
					else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);

				}
				else{
					set.remove(data.getDependencies().get(i).getId());
				}
			}

			delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
			//if(set.size() > 0) ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// Artifacts
			set.clear();
			maps = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).getArtifactParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getArtifacts().size();i++){
				if(set.contains(data.getArtifacts().get(i).getId())== false){
					part = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newArtifactParticipation(data,data.getArtifacts().get(i));
					if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.TASKPARTICIPATION, part);
					else ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(part);

				}
				else{
					set.remove(data.getArtifacts().get(i).getId());
				}
			}
			delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
			//if(set.size() > 0) ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			if(!delIds.isEmpty()) ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(delIds.toArray(new Long[0])), data, data.getOrganizationId());
			
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
		TaskType useMap = (TaskType)map;
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));
		fields.add(QueryFields.getFieldStartDate(useMap.getStartDate()));
		//fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		Calendar now = Calendar.getInstance();
		fields.add(QueryFields.getFieldModifiedDate(CalendarUtil.getXmlGregorianCalendar(now.getTime())));

		fields.add(QueryFields.getFieldCompletedDate(useMap.getCompletedDate()));
		fields.add(QueryFields.getFieldDueDate(useMap.getDueDate()));

		fields.add(QueryFields.getFieldEstimateId((useMap.getEstimate() != null ? useMap.getEstimate().getId() : 0)));
		fields.add(QueryFields.getFieldTaskStatus(useMap.getTaskStatus()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteTasksByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteTasksByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeBranchFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		if (deleted > 0){
			((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipations(new long[]{obj.getId()}, obj.getOrganizationId());
			return true;
		}
		return false;
	}
	public int deleteTasksByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).deleteParticipations(ids, organizationId);
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteTasksInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteTasksByIds(ids, group.getOrganizationId());
	}
	public List<TaskType> getChildTaskList(TaskType parent) throws FactoryException,ArgumentException{

		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		//fields.add(QueryFields.getFieldGroup(parent.getGroupId()));
		return getTaskList(fields.toArray(new QueryField[0]), 0,0,parent.getOrganizationId());
	}
	
	public List<TaskType>  getTaskList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<TaskType> getTaskListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
