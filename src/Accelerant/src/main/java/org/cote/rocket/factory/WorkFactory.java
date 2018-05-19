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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.cote.accountmanager.data.util.LogicalTypeComparator;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.WorkType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;


public class WorkFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.WORK, WorkFactory.class); }
	public WorkFactory(){
		super();
		this.tableNames.add("work");
		factoryType = FactoryEnumType.WORK;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("work")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		WorkType work = (WorkType)obj;
		work.getArtifacts().clear();
		work.getResources().clear();
		work.getDependencies().clear();
		for(int i = 0; i < work.getTasks().size();i++){
			((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).depopulate(work.getTasks().get(i));
		}
		work.getTasks().clear();
		work.setPopulated(false);
		updateToCache(work);
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{

		WorkType work = (WorkType)obj;
		if(work.getPopulated()) return;

		work.getArtifacts().addAll(((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).getArtifactsFromParticipation(work));
		work.getResources().addAll(((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).getResourcesFromParticipation(work));
		work.getDependencies().addAll(((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).getDependenciesFromParticipation(work));
		work.getTasks().addAll(((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).getTasksFromParticipation(work));
		Collections.sort(work.getTasks(),new LogicalTypeComparator());
		work.setPopulated(true);
		updateToCache(work);
	}
	
	
	public WorkType newWork(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		WorkType obj = new WorkType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.WORK);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		WorkType obj = (WorkType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Work without a group");

		DataRow row = prepareAdd(obj, "work");
		try{
			row.setCellValue("logicalorder",obj.getLogicalOrder());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("groupid", obj.getGroupId());
			if (insertRow(row)){
				try{
					WorkType cobj = (bulkMode ? obj : (WorkType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					BulkFactories.getBulkFactory().setDirty(factoryType);
					BaseParticipantType part = null;

					for(int i = 0; i < obj.getResources().size();i++){
						part = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).newResourceParticipation(cobj,obj.getResources().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
						else ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
					}
	
					for(int i = 0; i < obj.getTasks().size();i++){
						part = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).newTaskParticipation(cobj,obj.getTasks().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
						else ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getDependencies().size();i++){
						part = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).newDependencyParticipation(cobj,obj.getDependencies().get(i)); 
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
						else ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getArtifacts().size();i++){
						part = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).newArtifactParticipation(cobj,obj.getArtifacts().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
						else ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
					}
	
					return true;
				}
				catch(ArgumentException ae){
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
		WorkType newObj = new WorkType();
		newObj.setNameType(NameEnumType.WORK);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setDescription(rset.getString("description"));
		newObj.setLogicalOrder(rset.getInt("logicalorder"));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		WorkType data = (WorkType)object;
		boolean outBool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
				Set<Long> set = new HashSet<>();
				BaseParticipantType part = null;
				List<Long> delIds = new ArrayList<>();
				BaseParticipantType[] maps = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).getResourceParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getResources().size();i++){
					if(set.contains(data.getResources().get(i).getId())== false){
						part = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).newResourceParticipation(data,data.getResources().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.WORKPARTICIPATION, part);
						else ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);

					}
					else{
						set.remove(data.getResources().get(i).getId());
					}
				}
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				///if(set.size() > 0) ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Tasks
				set.clear();
				maps = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).getTaskParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getTasks().size();i++){
					if(set.contains(data.getTasks().get(i).getId())== false){
						part = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).newTaskParticipation(data,data.getTasks().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.WORKPARTICIPATION, part);
						else ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);

					}
					else{
						set.remove(data.getTasks().get(i).getId());
					}
				}
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				///if(set.size() > 0) ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Dependencies
				set.clear();
				maps = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).getDependencyParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getDependencies().size();i++){
					if(set.contains(data.getDependencies().get(i).getId())== false){
						part = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).newDependencyParticipation(data,data.getDependencies().get(i)); 
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.WORKPARTICIPATION, part);
						else ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);

					}
					else{
						set.remove(data.getDependencies().get(i).getId());
					}
				}

				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				///if(set.size() > 0) ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Artifacts
				set.clear();
				maps = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).getArtifactParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getArtifacts().size();i++){
					if(set.contains(data.getArtifacts().get(i).getId())== false){
						part = ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).newArtifactParticipation(data,data.getArtifacts().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.WORKPARTICIPATION, part);
						else ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).add(part);
					}
					else{
						set.remove(data.getArtifacts().get(i).getId());
					}
				}

				//if(set.size() > 0) ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				if(!delIds.isEmpty()) ((WorkParticipationFactory)Factories.getFactory(FactoryEnumType.WORKPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(delIds.toArray(new Long[0])), data, data.getOrganizationId());
				
				
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
		WorkType useMap = (WorkType)map;
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteWorksByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteWorksByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteWorksByIds(long[] ids, long organizationId) throws FactoryException
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
	public int deleteWorksInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteWorksByIds(ids, group.getOrganizationId());
	}
	
	
	public List<WorkType>  getWorkList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<WorkType> getWorkListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
