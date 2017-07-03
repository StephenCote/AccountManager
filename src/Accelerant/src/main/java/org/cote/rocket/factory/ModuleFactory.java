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
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.ModuleType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.types.ModuleEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class ModuleFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.MODULE, ModuleFactory.class); }
	public ModuleFactory(){
		super();
		this.tableNames.add("module");
		factoryType = FactoryEnumType.MODULE;
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		ModuleType module = (ModuleType)obj;
		if(module.getPopulated()) return;
		module.getArtifacts().addAll(((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).getArtifactsFromParticipation(module));
		module.getWork().addAll(((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).getWorkFromParticipation(module));
		module.setPopulated(true);
		updateToCache(module);
	}
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("module")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	
	public ModuleType newModule(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		ModuleType obj = new ModuleType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setModuleType(ModuleEnumType.UNKNOWN);
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.MODULE);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ModuleType obj = (ModuleType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Module without a group");

		DataRow row = prepareAdd(obj, "module");
		try{
			if(obj.getActualCost() != null) row.setCellValue("costid",obj.getActualCost().getId());
			if(obj.getActualTime() != null) row.setCellValue("timeid",obj.getActualTime().getId());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("moduletype", obj.getModuleType().toString());
			row.setCellValue("groupid", obj.getGroupId());
			if (insertRow(row)){
				try{
					ModuleType cobj = (bulkMode ? obj : (ModuleType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					BulkFactories.getBulkFactory().setDirty(factoryType);
					BaseParticipantType part = null;
					for(int i = 0; i < obj.getWork().size();i++){
						part = ((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).newWorkParticipation(cobj,obj.getWork().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.MODULEPARTICIPATION)).add(part);
						else ((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).add(part);
					}

					for(int i = 0; i < obj.getArtifacts().size();i++){
						part = ((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).newArtifactParticipation(cobj,obj.getArtifacts().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.MODULEPARTICIPATION)).add(part);
						else ((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).add(part);
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
		ModuleType new_obj = new ModuleType();
		new_obj.setNameType(NameEnumType.MODULE);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		new_obj.setModuleType(ModuleEnumType.valueOf(rset.getString("moduletype")));
		new_obj.setDescription(rset.getString("description"));
		long time_id = rset.getLong("timeid");
		long cost_id = rset.getLong("costid");
		if(time_id > 0) new_obj.setActualTime((TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getById(time_id, new_obj.getOrganizationId()));
		if(cost_id > 0) new_obj.setActualCost((CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getById(cost_id, new_obj.getOrganizationId()));
		return new_obj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ModuleType data = (ModuleType)object;
		boolean out_bool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
				Set<Long> set = new HashSet<Long>();
				BaseParticipantType[] maps = ((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).getWorkParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getWork().size();i++){
					if(set.contains(data.getWork().get(i).getId())== false){
						((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).add(((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).newWorkParticipation(data,data.getWork().get(i)));
					}
					else{
						set.remove(data.getWork().get(i).getId());
					}
				}
//				System.out.println("Net delete Work parts: " + set.size());
				((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				
				/// Artifacts
				set.clear();
				maps = ((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).getArtifactParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getArtifacts().size();i++){
					if(set.contains(data.getArtifacts().get(i).getId())== false){
						((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).add(((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).newArtifactParticipation(data,data.getArtifacts().get(i)));
					}
					else{
						set.remove(data.getArtifacts().get(i).getId());
					}
				}
//				System.out.println("Net delete Artifact parts: " + set.size());
				((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}
			out_bool = true;
		}
		return out_bool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ModuleType use_map = (ModuleType)map;
		fields.add(QueryFields.getFieldModuleType(use_map.getModuleType()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldCost(use_map.getActualCost()));
		fields.add(QueryFields.getFieldTime(use_map.getActualTime()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroupId()));
	}
	public int deleteModulesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteModulesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteModulesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((ModuleParticipationFactory)Factories.getFactory(FactoryEnumType.MODULEPARTICIPATION)).deleteParticipations(ids, organizationId);
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteModulesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteModulesByIds(ids, group.getOrganizationId());
	}
	
	
	public List<ModuleType>  getModuleList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ModuleType> getModuleListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
