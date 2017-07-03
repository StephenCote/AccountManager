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
package org.cote.rocket.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
public class LifecycleFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.LIFECYCLE, LifecycleFactory.class); }
	public LifecycleFactory(){
		super();
		this.tableNames.add("lifecycle");
		factoryType = FactoryEnumType.LIFECYCLE;
		this.hasObjectId = true;
	}

	@Override
	public void registerProvider(){
		AuthorizationService.registerAuthorizationProviders(
				FactoryEnumType.LIFECYCLE,
				NameEnumType.LIFECYCLE,
				FactoryEnumType.LIFECYCLEPARTICIPATION
			);
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("lifecycle")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}

	public LifecycleType newLifecycle(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		LifecycleType obj = new LifecycleType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.LIFECYCLE);
		obj.setObjectId(UUID.randomUUID().toString());
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		LifecycleType obj = (LifecycleType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new lifecycle without a group");

		DataRow row = prepareAdd(obj, "lifecycle");
		try{
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("groupid", obj.getGroupId());
			if (insertRow(row)){
				LifecycleType cobj = (bulkMode ? obj : (LifecycleType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < obj.getSchedules().size();i++){
					part = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newScheduleParticipation(cobj,obj.getSchedules().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(part);
					else ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getBudgets().size();i++){
					part = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newBudgetParticipation(cobj,obj.getBudgets().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(part);
					else ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getGoals().size();i++){
					part = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newGoalParticipation(cobj,obj.getGoals().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(part);
					else ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getProjects().size();i++){
					part = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newProjectParticipation(cobj,obj.getProjects().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(part);
					else ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(part);
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
	public void repopulate(LifecycleType cycle) throws FactoryException, ArgumentException{
		depopulate(cycle);
		populate(cycle);
	}
	
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		LifecycleType cycle = (LifecycleType)obj;
		cycle.getSchedules().clear();
		cycle.getBudgets().clear();
		cycle.getGoals().clear();
		cycle.getProjects().clear();
		cycle.setPopulated(false);
		updateToCache(cycle);
		
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		LifecycleType cycle = (LifecycleType)obj;

		if(cycle.getPopulated()) return;
		cycle.getSchedules().addAll(((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).getSchedulesFromParticipation(cycle));
		cycle.getBudgets().addAll(((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).getBudgetsFromParticipation(cycle));
		cycle.getGoals().addAll(((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).getGoalsFromParticipation(cycle));
		cycle.getProjects().addAll(((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).getProjectsFromParticipation(cycle));
		
		cycle.setPopulated(true);
		updateToCache(cycle);
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		LifecycleType new_obj = new LifecycleType();
		new_obj.setNameType(NameEnumType.LIFECYCLE);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		new_obj.setDescription(rset.getString("description"));
		return new_obj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		LifecycleType data = (LifecycleType)object;

		boolean out_bool = false;
		if(data.getPopulated() == false){
			logger.warn("Updating unpopulated lifecycle '" + data.getName() + "' may result in unexpected data loss");
		}
		removeFromCache(data);
		if(update(data, null)){
			try{
				
				/// Budgets
				///
				Set<Long> set = new HashSet<Long>();
				BaseParticipantType[] maps = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).getBudgetParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getBudgets().size();i++){
					if(set.contains(data.getBudgets().get(i).getId())== false){
						((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newBudgetParticipation(data,data.getBudgets().get(i)));
					}
					else{
						set.remove(data.getBudgets().get(i).getId());
					}
				}
//				System.out.println("Net delete budget parts: " + set.size());
				((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Goals
				set.clear();
				maps = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).getGoalParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getGoals().size();i++){
					if(set.contains(data.getGoals().get(i).getId())== false){
						((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newGoalParticipation(data,data.getGoals().get(i)));
					}
					else{
						set.remove(data.getGoals().get(i).getId());
					}
				}
//				System.out.println("Net delete goal parts: " + set.size());
				((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

				/// Schedules
				set.clear();
				maps = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).getScheduleParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getSchedules().size();i++){
					if(set.contains(data.getSchedules().get(i).getId())== false){
						((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newScheduleParticipation(data,data.getSchedules().get(i)));
					}
					else{
						set.remove(data.getSchedules().get(i).getId());
					}
				}
//				System.out.println("Net delete Schedule parts: " + set.size());
				((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

				/// Projects
				set.clear();
				maps = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).getProjectParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getProjects().size();i++){
					if(set.contains(data.getProjects().get(i).getId())== false){
						((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newProjectParticipation(data,data.getProjects().get(i)));
					}
					else{
						set.remove(data.getProjects().get(i).getId());
					}
				}
//				System.out.println("Net delete Project parts: " + set.size());
				((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

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
		LifecycleType use_map = (LifecycleType)map;
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroupId()));
	}
	public int deleteLifecyclesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteLifecyclesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		//int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		int deleted = deleteLifecyclesByIds(new long[]{obj.getId()},obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteLifecyclesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).deleteParticipations(ids, organizationId);
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteLifecyclesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteLifecyclesByIds(ids, group.getOrganizationId());
	}
	
	public List<LifecycleType>  getLifecycleListByGroup(DirectoryGroupType group, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, startRecord, recordCount, organizationId);
	}

	public List<LifecycleType>  getLifecycleList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<LifecycleType> getLifecycleListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
