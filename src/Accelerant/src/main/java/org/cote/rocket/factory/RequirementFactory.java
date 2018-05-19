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
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.NoteType;
import org.cote.propellant.objects.RequirementType;
import org.cote.propellant.objects.types.PriorityEnumType;
import org.cote.propellant.objects.types.RequirementEnumType;
import org.cote.propellant.objects.types.RequirementStatusEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;


public class RequirementFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.REQUIREMENT, RequirementFactory.class); }
	public RequirementFactory(){
		super();
		this.tableNames.add("requirement");
		factoryType = FactoryEnumType.REQUIREMENT;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("requirement")){
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
		RequirementType req = (RequirementType)obj;
		if(req.getPopulated() == true) return;
		
		req.setPopulated(true);
		updateToCache(req);
	}
	
	public RequirementType newRequirement(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		RequirementType obj = new RequirementType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setRequirementType(RequirementEnumType.UNKNOWN);
		obj.setRequirementStatus(RequirementStatusEnumType.UNKNOWN);
		obj.setPriority(PriorityEnumType.UNKNOWN);
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.REQUIREMENT);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		RequirementType obj = (RequirementType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Requirement without a group");

		DataRow row = prepareAdd(obj, "requirement");
		try{
			row.setCellValue("requirementtype", obj.getRequirementType().toString());
			row.setCellValue("requirementstatus", obj.getRequirementStatus().toString());
			row.setCellValue("groupid", obj.getGroupId());
			if(obj.getDescription() != null) row.setCellValue("description", obj.getDescription());
			if(obj.getRequirementId() != null) row.setCellValue("requirementid",obj.getRequirementId());
			if(obj.getNote() != null) row.setCellValue("noteid", obj.getNote().getId());
			else row.setCellValue("noteid",0);
			if(obj.getForm() != null) row.setCellValue("formid",obj.getForm().getId());
			else row.setCellValue("formid",0);
			row.setCellValue("priority",obj.getPriority().toString());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
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
		RequirementType newObj = new RequirementType();
		newObj.setNameType(NameEnumType.REQUIREMENT);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setRequirementType(RequirementEnumType.valueOf(rset.getString("requirementtype")));
		newObj.setRequirementStatus(RequirementStatusEnumType.valueOf(rset.getString("requirementstatus")));
		newObj.setDescription(rset.getString("description"));
		newObj.setPriority(PriorityEnumType.valueOf(rset.getString("priority")));
		newObj.setLogicalOrder(rset.getInt("logicalorder"));
		newObj.setRequirementId(rset.getString("requirementid"));
		long noteId = rset.getLong("noteid");
		if(noteId > 0) newObj.setNote((NoteType)((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).getById(noteId,  newObj.getOrganizationId()));
		long formId = rset.getLong("formid");
		if(formId > 0) newObj.setForm((FormType)((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getById(formId, newObj.getOrganizationId()));
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
		RequirementType useMap = (RequirementType)map;
		fields.add(QueryFields.getFieldRequirementType(useMap.getRequirementType()));
		fields.add(QueryFields.getFieldRequirementStatusType(useMap.getRequirementStatus()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldPriority(useMap.getPriority()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldRequirementId(useMap.getRequirementId()));
		fields.add(QueryFields.getBigIntField("noteid", (useMap.getNote() != null ? useMap.getNote().getId() : 0)));
		fields.add(QueryFields.getBigIntField("formid", (useMap.getForm() != null ? useMap.getForm().getId() : 0)));
	}
	public int deleteRequirementsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteRequirementsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteRequirementsByIds(long[] ids, long organizationId) throws FactoryException
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
	public int deleteRequirementsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteRequirementsByIds(ids, group.getOrganizationId());
	}
	
	
	public List<RequirementType>  getRequirementList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<RequirementType> getRequirementListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
