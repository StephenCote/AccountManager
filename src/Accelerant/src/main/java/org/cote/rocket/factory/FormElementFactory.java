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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.NoteType;
import org.cote.propellant.objects.ValidationRuleType;
import org.cote.propellant.objects.types.ElementEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class FormElementFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.FORMELEMENT, FormElementFactory.class); }
	public FormElementFactory(){
		super();
		this.tableNames.add("formelement");
		factoryType = FactoryEnumType.FORMELEMENT;
	}

	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("formelement")){
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
		FormElementType formElement = (FormElementType)obj;
		if(formElement.getPopulated()) return;
		formElement.getElementValues().clear();
		formElement.getElementValues().addAll(((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).getFormElementValuesFromParticipation(formElement));
		formElement.setPopulated(true);
		updateToCache(formElement);
	}
	
	
	@Override
	public <T> T clone(T source) throws FactoryException{
		FormElementType el = (FormElementType)source;
		FormElementType outEl = new FormElementType();
		outEl.setOwnerId(el.getOwnerId());
		outEl.setOrganizationId(el.getOrganizationId());
		outEl.setNameType(el.getNameType());
		outEl.setId(el.getId());
		outEl.setName(el.getName());
		outEl.setDescription(el.getDescription());
		outEl.setParentId(el.getParentId());
		outEl.setElementLabel(el.getElementLabel());
		outEl.setElementName(el.getElementName());
		outEl.setElementType(el.getElementType());
		outEl.setValidationRule(el.getValidationRule());
		outEl.getElementValues().addAll(el.getElementValues());
		outEl.setElementTemplate(el.getElementTemplate());
		return (T)outEl;
	}
	public FormElementType newFormElement(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		//if(form == null || form.getId() <= 0) throw new ArgumentException("Invalid form");
		FormElementType obj = new FormElementType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		//obj.setForm(form);
		obj.setElementType(ElementEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.FORMELEMENT);
		return obj;
	}
	public FormType getDefaultValuesForm(FormElementType obj) throws FactoryException, ArgumentException{
		FormType form = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getByNameInGroup("DefaultElementValues", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(),obj.getOrganizationId()));
		if(form == null){
			UserType user = Factories.getNameIdFactory(FactoryEnumType.USER).getById(obj.getOwnerId(), obj.getOrganizationId());
			form = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).newForm(user, obj.getGroupId());
			form.setName("DefaultElementValues");
			form.setIsGrid(false);
			form.setIsTemplate(true);
			if(((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).add(form)){
				form = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getByNameInGroup("DefaultElementValues", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(),obj.getOrganizationId()));
			}
		}
		return form;
	}
	/// Add or update form element values
	/// Add a participation between the form element and the value
	/// Set the value to the Defaults form
	
	/// ONLY update from default element values for the defaultelementvalues
	/// This is not the place to update form element values where the form is not holding default template value
	///
	public void updateDefaultFormElementValues(FormElementType formElement) throws FactoryException, DataAccessException, ArgumentException{
		FormType form = getDefaultValuesForm(formElement);
		if(form == null) throw new DataAccessException("Failed to retrieve default values form");
		
		int add_count =0;
		int rem_count = 0;
		/// Create a map of existing values
		Set<Long> valSet = new HashSet<>();
		Map<Long,FormElementValueType> valMap = new HashMap<Long,FormElementValueType>();
		if(formElement.getId() > 0){
			List<FormElementValueType> values = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getByFormElement(form, formElement);
			for(int i = 0; i< values.size();i++){
				valSet.add(values.get(i).getId());
				valMap.put(values.get(i).getId(), values.get(i));
				
			}
		}
		/// For each value:
		for(int i = 0; i < formElement.getElementValues().size();i++){
			FormElementValueType val = formElement.getElementValues().get(i);
			FormElementValueType valCheck = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getByNameInGroup(val.getName(), form, formElement);
			if(valCheck != null) val = valCheck;
			/// Remove value from purge list if present in current element
			///
			if(valSet.contains(val.getId())){
				valSet.remove(val.getId());
			}
			/// if value is new, then add it and create a participation for it
			if(val.getId().compareTo(0L)==0){
				FormElementValueType nval = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).newFormElementValue((UserType)Factories.getNameIdFactory(FactoryEnumType.USER).getById(formElement.getOwnerId(), formElement.getOrganizationId()), form, formElement);
				nval.setName(val.getName());
				nval.setIsBinary(val.getIsBinary());
				nval.setTextValue(val.getTextValue());
				nval.setBinaryId(val.getBinaryId());
				if(((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).add(nval)){
					add_count++;
					val = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getByNameInGroup(val.getName(),form, formElement);
					/// replace the value with the new version
					///
					//formElement.getElementValues().set(i,val);
					((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).add(((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).newFormElementValueParticipation(formElement,val));
				}
				
			}
			/// Otherwise, updated the value
			else{
				((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).update(val);
			}
		}
		/// Purge remaining values
		long[] ids = ArrayUtils.toPrimitive(valSet.toArray(new Long[0]));
		((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).deleteFormElementValuesByIds(ids, formElement.getOrganizationId());
		if(ids.length > 0) rem_count++;
		
		// Re-populate element:
		// Note: The values are coming from the default form, not the participation
		//
		formElement.setPopulated(false);
		formElement.getElementValues().clear();
		formElement.getElementValues().addAll(((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getByFormElement(form, formElement));
		
		if(add_count > 0 || rem_count > 0){
			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).clearCache();
			((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).clearCache();
			((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).clearCache();
			((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).clearCache();
			((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).clearCache();
		}

	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		FormElementType obj = (FormElementType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new FormElement without a group");

		DataRow row = prepareAdd(obj, "formelement");
		try{
			//row.setCellValue("formid", obj.getForm().getId());
			row.setCellValue("elementtype",obj.getElementType().toString());
			row.setCellValue("elementname",obj.getElementName());
			row.setCellValue("elementlabel",obj.getElementLabel());
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("groupid", obj.getGroupId());
			if(obj.getElementTemplate() != null) row.setCellValue("elementtemplateid", obj.getElementTemplate().getId());
			if(obj.getValidationRule() != null) row.setCellValue("validationruleid",obj.getValidationRule().getId());

			if (insertRow(row)){
				
				try{
					/// For templates only: If there are any form values, then get/create an element form to store the values against
					/// these will be used for default values;
					
					FormElementType cobj = (bulkMode ? obj : (FormElementType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					/// todo: why on 'add' are values being duplicated?  
					/// ans: values are being updated to the cache copy,
					/// and the populate doesn't clear the array before copying values back in
					
					cobj.getElementValues().clear();
					cobj.getElementValues().addAll(obj.getElementValues());
					updateDefaultFormElementValues(cobj);
					///System.out.println("Adding element values #" + cobj.getElementValues().size());

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
		FormElementType newObj = new FormElementType();
		newObj.setNameType(NameEnumType.FORMELEMENT);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setElementLabel(rset.getString("elementlabel"));
		newObj.setElementName(rset.getString("elementname"));
		long view_id = rset.getLong("elementtemplateid");
		if(view_id > 0) newObj.setElementTemplate((NoteType)((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).getById(view_id, newObj.getOrganizationId()));

		//newObj.setForm((FormType)((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getById(rset.getInt("formid"),newObj.getOrganizationId()));
		long rule_id = rset.getLong("validationruleid");
		if(rule_id > 0) newObj.setValidationRule((ValidationRuleType)((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).getById(rule_id,newObj.getOrganizationId()));
		newObj.setDescription(rset.getString("description"));
		newObj.setElementType(ElementEnumType.valueOf(rset.getString("elementtype")));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		FormElementType data = (FormElementType)object;
		boolean outBool = false;
		removeFromCache(data);

		if(update(data, null)){
			try{
				
				/// Resync any element values
				/// 
				updateDefaultFormElementValues(data);
				
				/// Values
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).getFormElementValueParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getElementValues().size();i++){
					if(set.contains(data.getElementValues().get(i).getId())== false){
						((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).add(((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).newFormElementValueParticipation(data,data.getElementValues().get(i)));
					}
					else{
						set.remove(data.getElementValues().get(i).getId());
					}
				}
//				System.out.println("Net delete Element parts: " + set.size());
				((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
			
				outBool = true;
			}
			catch(ArgumentException | DataAccessException ae){
				throw new FactoryException(ae.getMessage());
			}
		}
		return outBool;
	}
	/* 
	public <T> T getByNameInGroup(String name, FormType form) throws FactoryException{

		String keyName = name + "-" + form.getGroupId();
		T out_data = readCache(keyName);
		if (out_data != null) return out_data;

		List<NameIdType> obj_list = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldGroup(form.getGroupId()),QueryFields.getFieldFormId(form.getId()) }, form.getOrganizationId());

		if (obj_list.size() > 0)
		{
			addToCache(obj_list.get(0),keyName);
			out_data = (T)obj_list.get(0);
		}
		else{
			System.out.println("No results for " + name + " in form " + form.getId());
		}
		return out_data;
	}
	*/
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		FormElementType useMap = (FormElementType)map;
		fields.add(QueryFields.getFieldValidationRuleId((useMap.getValidationRule() != null ? useMap.getValidationRule().getId() : 0)));
		fields.add(QueryFields.getFieldElementName(useMap.getElementName()));
		fields.add(QueryFields.getFieldElementLabel(useMap.getElementLabel()));
		fields.add(QueryFields.getFieldElementType(useMap.getElementType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldElementTemplateId((useMap.getElementTemplate() != null ? useMap.getElementTemplate().getId() : 0)));
		//fields.add(QueryFields.getFieldFormId(useMap.getForm().getId()));
	}
	public int deleteFormElementsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteFormElementsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		if(deleted > 0){
			((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).deleteFormElementValuesByElementIds(new long[]{obj.getId()}, obj.getOrganizationId());
			((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).deleteParticipations(new long[]{obj.getId()}, obj.getOrganizationId());
			return true;
		}
		return false;
	}
	public int deleteFormElementsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			//((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).get
			//((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).getform

			((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).deleteFormElementValuesByElementIds(ids, organizationId);
			((FormElementParticipationFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTPARTICIPATION)).deleteParticipations(ids, organizationId);

			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteFormElementsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteFormElementsByIds(ids, group.getOrganizationId());
	}
	
	public List<FormElementType>  getFormElementListByGroup(DirectoryGroupType group, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, startRecord, recordCount, organizationId);
	}

	public List<FormElementType>  getFormElementList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<FormElementType> getFormElementListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
