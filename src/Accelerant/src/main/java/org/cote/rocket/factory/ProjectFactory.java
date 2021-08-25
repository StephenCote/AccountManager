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
import java.util.UUID;

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
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;


public class ProjectFactory extends NameIdGroupFactory {
	
	public ProjectFactory(){
		super();
		this.primaryTableName = "project";
		this.tableNames.add(primaryTableName);
		factoryType = FactoryEnumType.PROJECT;
		this.hasObjectId = true;
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// restrict columns
		}
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		ProjectType project = (ProjectType)obj;
		project.getBlueprints().clear();
		project.getRequirements().clear();
		project.getDependencies().clear();
		project.getArtifacts().clear();
		project.getModules().clear();
		for(int i = 0; i < project.getStages().size();i++){
			((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).depopulate(project.getStages().get(i));
		}
		project.getStages().clear();
		project.setPopulated(false);
		updateToCache(project);
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		ProjectType project = (ProjectType)obj;
		if(project.getPopulated().booleanValue()) return;
		
		project.getBlueprints().addAll(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getModelsFromParticipation(project));
		project.getRequirements().addAll(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getRequirementsFromParticipation(project));
		project.getDependencies().addAll(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getDependenciesFromParticipation(project));
		project.getArtifacts().addAll(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getArtifactsFromParticipation(project));
		project.getModules().addAll(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getModulesFromParticipation(project));
		project.getStages().addAll(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getStagesFromParticipation(project));
		Collections.sort(project.getStages(),new LogicalTypeComparator());
		project.setPopulated(true);
		updateToCache(project);
	}
	
	public ProjectType newProject(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		ProjectType obj = new ProjectType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setNameType(NameEnumType.PROJECT);
		obj.setGroupId(groupId);
		obj.setObjectId(UUID.randomUUID().toString());
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ProjectType obj = (ProjectType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Project without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			if(obj.getSchedule() != null) row.setCellValue(Columns.get(ColumnEnumType.SCHEDULEID), obj.getSchedule().getId()); 
			if (insertRow(row)){
				try{
					ProjectType cobj = (bulkMode ? obj : (ProjectType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					BulkFactories.getBulkFactory().setDirty(factoryType);
					BaseParticipantType part = null;

					for(int i = 0; i < obj.getRequirements().size();i++){
						part = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newRequirementParticipation(cobj,obj.getRequirements().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
						else ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getBlueprints().size();i++){
						part = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newModelParticipation(cobj,obj.getBlueprints().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
						else ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
					}

					for(int i = 0; i < obj.getModules().size();i++){
						part = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newModuleParticipation(cobj,obj.getModules().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
						else ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
					}
					
					for(int i = 0; i < obj.getStages().size();i++){
						part = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newStageParticipation(cobj,obj.getStages().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
						else ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getDependencies().size();i++){
						part = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newDependencyParticipation(cobj,obj.getDependencies().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
						else ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getArtifacts().size();i++){
						part = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newArtifactParticipation(cobj,obj.getArtifacts().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
						((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(part);
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
		ProjectType newObj = new ProjectType();
		newObj.setNameType(NameEnumType.PROJECT);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		long scheduleId = rset.getLong(Columns.get(ColumnEnumType.SCHEDULEID));

		if(scheduleId > 0L) newObj.setSchedule((ScheduleType)((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).getById(scheduleId, newObj.getOrganizationId()));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ProjectType data = (ProjectType)object;
		boolean outBool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getModuleParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getModules().size();i++){
					if(!set.contains(data.getModules().get(i).getId())){
						((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newModuleParticipation(data,data.getModules().get(i)));
					}
					else{
						set.remove(data.getModules().get(i).getId());
					}
				}
				((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Stages
				set.clear();
				maps = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getStageParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getStages().size();i++){
					if(!set.contains(data.getStages().get(i).getId())){
						((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newStageParticipation(data,data.getStages().get(i)));
					}
					else{
						set.remove(data.getStages().get(i).getId());
					}
				}
				((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

				/// Blueprints
				set.clear();
				maps = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getModelParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getBlueprints().size();i++){
					if(!set.contains(data.getBlueprints().get(i).getId())){
						((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newModelParticipation(data,data.getBlueprints().get(i)));
					}
					else{
						set.remove(data.getBlueprints().get(i).getId());
					}
				}
				((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Requirements
				set.clear();
				maps = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getRequirementParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getRequirements().size();i++){
					if(!set.contains(data.getRequirements().get(i).getId())){
						((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newRequirementParticipation(data,data.getRequirements().get(i)));
					}
					else{
						set.remove(data.getRequirements().get(i).getId());
					}
				}
				((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Dependencies
				set.clear();
				maps = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getDependencyParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getDependencies().size();i++){
					if(!set.contains(data.getDependencies().get(i).getId())){
						((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newDependencyParticipation(data,data.getDependencies().get(i)));
					}
					else{
						set.remove(data.getDependencies().get(i).getId());
					}
				}
				((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Artifacts
				set.clear();
				maps = ((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).getArtifactParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getArtifacts().size();i++){
					if(!set.contains(data.getArtifacts().get(i).getId())){
						((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).add(((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).newArtifactParticipation(data,data.getArtifacts().get(i)));
					}
					else{
						set.remove(data.getArtifacts().get(i).getId());
					}
				}
				((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}
			outBool = true;
		}
		return outBool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ProjectType useMap = (ProjectType)map;
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldScheduleId(useMap.getSchedule() != null ? useMap.getSchedule().getId() : 0L));
	}
	public int deleteProjectsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteProjectsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteProjectsByIds(new long[]{obj.getId()}, obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteProjectsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((ProjectParticipationFactory)Factories.getFactory(FactoryEnumType.PROJECTPARTICIPATION)).deleteParticipations(ids, organizationId);
		}
		return deleted;
	}
	public int deleteProjectsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteProjectsByIds(ids, group.getOrganizationId());
	}
	
	
	public List<ProjectType>  getProjectList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ProjectType> getProjectListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
