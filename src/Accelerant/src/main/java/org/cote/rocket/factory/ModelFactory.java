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
import org.cote.propellant.objects.ModelType;
import org.cote.propellant.objects.types.ModelEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class ModelFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.MODEL, ModelFactory.class); }
	public ModelFactory(){
		super();
		this.tableNames.add("model");
		factoryType = FactoryEnumType.MODEL;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("model")){
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
		ModelType model = (ModelType)obj;
		if(model.getPopulated()) return;
		model.getArtifacts().addAll(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getArtifactsFromParticipation(model));
		model.getDependencies().addAll(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getDependenciesFromParticipation(model));
		model.getCases().addAll(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getCasesFromParticipation(model));
		model.getRequirements().addAll(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getRequirementsFromParticipation(model));
		model.getModels().addAll(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getModelsFromParticipation(model));
		model.setPopulated(true);
		updateToCache(model);
	}
	
	
	public ModelType newModel(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		ModelType obj = new ModelType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setModelType(ModelEnumType.UNKNOWN);
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.MODEL);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ModelType obj = (ModelType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Model without a group");

		DataRow row = prepareAdd(obj, "model");
		try{
			row.setCellValue("modeltype", obj.getModelType().toString());
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("description", obj.getDescription());
			if (insertRow(row)){
				ModelType cobj = (bulkMode ? obj : (ModelType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;
				
				for(int i = 0; i < obj.getCases().size();i++){
					part = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newCaseParticipation(cobj,obj.getCases().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
					else ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getRequirements().size();i++){
					part = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newRequirementParticipation(cobj,obj.getRequirements().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
					else ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getDependencies().size();i++){
					part = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newDependencyParticipation(cobj,obj.getDependencies().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
					else ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getArtifacts().size();i++){
					part = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newArtifactParticipation(cobj,obj.getArtifacts().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
					else ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getModels().size();i++){
					part = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newModelParticipation(cobj,obj.getModels().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
					else ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(part);
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
		ModelType newObj = new ModelType();
		newObj.setNameType(NameEnumType.MODEL);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setModelType(ModelEnumType.valueOf(rset.getString("modeltype")));
		newObj.setDescription(rset.getString("description"));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ModelType data = (ModelType)object;
		boolean outBool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
				/// Cases
				///
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getCaseParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getCases().size();i++){
					if(set.contains(data.getCases().get(i).getId())== false){
						((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newCaseParticipation(data,data.getCases().get(i)));
					}
					else{
						set.remove(data.getCases().get(i).getId());
					}
				}
//				System.out.println("Net delete Case parts: " + set.size());
				((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				
				/// Requirements
				///
				set.clear();
				maps = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getRequirementParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getRequirements().size();i++){
					if(set.contains(data.getRequirements().get(i).getId())== false){
						((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newRequirementParticipation(data,data.getRequirements().get(i)));
					}
					else{
						set.remove(data.getRequirements().get(i).getId());
					}
				}
//				System.out.println("Net delete Requirement parts: " + set.size());
				((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Dependencies
				///
				set.clear();
				maps = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getDependencyParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getDependencies().size();i++){
					if(set.contains(data.getDependencies().get(i).getId())== false){
						((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newDependencyParticipation(data,data.getDependencies().get(i)));
					}
					else{
						set.remove(data.getDependencies().get(i).getId());
					}
				}
//				System.out.println("Net delete Dependencie parts: " + set.size());
				((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Artifacts
				///
				set.clear();
				maps = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getArtifactParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getArtifacts().size();i++){
					if(set.contains(data.getArtifacts().get(i).getId())== false){
						((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newArtifactParticipation(data,data.getArtifacts().get(i)));
					}
					else{
						set.remove(data.getArtifacts().get(i).getId());
					}
				}
//				System.out.println("Net delete Artifact parts: " + set.size());
				((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				/// Models
				///
				set.clear();
				maps = ((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).getModelParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getModels().size();i++){
					if(set.contains(data.getModels().get(i).getId())== false){
						((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).add(((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).newModelParticipation(data,data.getModels().get(i)));
					}
					else{
						set.remove(data.getModels().get(i).getId());
					}
				}
//				System.out.println("Net delete Model parts: " + set.size());
				((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				outBool = true;
				
			}
			catch(ArgumentException e){
				throw new FactoryException(e.getMessage());
			}
		}
		return outBool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ModelType useMap = (ModelType)map;
		fields.add(QueryFields.getFieldModelType(useMap.getModelType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteModelsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteModelsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteModelsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((ModelParticipationFactory)Factories.getFactory(FactoryEnumType.MODELPARTICIPATION)).deleteParticipations(ids, organizationId);
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteModelsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteModelsByIds(ids, group.getOrganizationId());
	}
	
	
	public List<ModelType>  getModelList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ModelType> getModelListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
