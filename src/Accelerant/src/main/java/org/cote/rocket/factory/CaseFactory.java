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
import org.cote.propellant.objects.CaseType;
import org.cote.propellant.objects.types.CaseEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;


public class CaseFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.CASE, CaseFactory.class); }
	public CaseFactory(){
		super();
		this.primaryTableName = "usecase";
		this.tableNames.add(primaryTableName);
		factoryType = FactoryEnumType.CASE;
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
		CaseType cobj = (CaseType)obj;
		if(cobj.getPopulated().booleanValue()) return;

		cobj.getActors().addAll(((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).getResourcesFromParticipation(cobj));
		cobj.getPrerequisites().addAll(((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).getArtifactsFromParticipation(cobj));
		cobj.getSequence().addAll(((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).getTasksFromParticipation(cobj));
		cobj.getDiagrams().addAll(((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).getDataFromParticipation(cobj));
		cobj.setPopulated(true);
		updateToCache(cobj);
	}
	
	public CaseType newCase(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		CaseType obj = new CaseType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setCaseType(CaseEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.CASE);
		obj.setGroupId(groupId);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		CaseType obj = (CaseType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Case without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.CASETYPE), obj.getCaseType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			if (insertRow(row)){
				CaseType cobj = (bulkMode ? obj : (CaseType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;
				for(int i = 0; i < obj.getActors().size();i++){
					part = ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).newResourceParticipation(cobj,obj.getActors().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.CASEPARTICIPATION)).add(part);
					else ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getPrerequisites().size();i++){
					part = ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).newArtifactParticipation(cobj,obj.getPrerequisites().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.CASEPARTICIPATION)).add(part);
					else ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getSequence().size();i++){
					part = ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).newTaskParticipation(cobj,obj.getSequence().get(i)); 
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.CASEPARTICIPATION)).add(part);
					else ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getDiagrams().size();i++){
					part = ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).newDataParticipation(cobj,obj.getDiagrams().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.CASEPARTICIPATION)).add(part);
					else ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).add(part);
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
		CaseType newObj = new CaseType();
		newObj.setNameType(NameEnumType.CASE);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setCaseType(CaseEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.CASETYPE))));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		CaseType data = (CaseType)object;
		boolean outBool = false;
		removeFromCache(data);
		if(update(data, null)){
			/// Cases
			///
			try{
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).getResourceParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getActors().size();i++){
					if(set.contains(data.getActors().get(i).getId())== false){
						((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).add(((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).newResourceParticipation(data,data.getActors().get(i)));
					}
					else{
						set.remove(data.getActors().get(i).getId());
					}
				}
//				System.out.println("Net delete Case parts: " + set.size());
				((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				set = new HashSet<>();
				maps = ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).getArtifactParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getPrerequisites().size();i++){
					if(!set.contains(data.getPrerequisites().get(i).getId())){
						((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).add(((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).newArtifactParticipation(data,data.getPrerequisites().get(i)));
					}
					else{
						set.remove(data.getPrerequisites().get(i).getId());
					}
				}

				((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

				set = new HashSet<>();
				maps = ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).getTaskParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getSequence().size();i++){
					if(!set.contains(data.getSequence().get(i).getId())){
						((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).add(((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).newTaskParticipation(data,data.getSequence().get(i)));
					}
					else{
						set.remove(data.getSequence().get(i).getId());
					}
				}
				System.out.println("Net delete Case parts: " + set.size());
				((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

				set = new HashSet<>();
				maps = ((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).getDataParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getDiagrams().size();i++){
					if(!set.contains(data.getDiagrams().get(i).getId())){
						((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).add(((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).newDataParticipation(data,data.getDiagrams().get(i)));
					}
					else{
						set.remove(data.getDiagrams().get(i).getId());
					}
				}

				((CaseParticipationFactory)Factories.getFactory(FactoryEnumType.CASEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

				
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
		CaseType useMap = (CaseType)map;
		fields.add(QueryFields.getFieldCaseType(useMap.getCaseType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteCasesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteCasesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteCasesByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	public int deleteCasesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteCasesByIds(ids, group.getOrganizationId());
	}
	
	
	public List<CaseType>  getCaseList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<CaseType> getCaseListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
