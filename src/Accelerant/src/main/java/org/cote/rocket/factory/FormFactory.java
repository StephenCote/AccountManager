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
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.NoteType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class FormFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.FORM, FormFactory.class); }
	public FormFactory(){
		super();
		this.tableNames.add("form");
		factoryType = FactoryEnumType.FORM;
	}

	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("form")){
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
		FormType form = (FormType)obj;
		if(form == null){
			logger.error("Form reference is null. This is likely due to an invalid template or subform reference.");
			return;
		}
		if(form.getPopulated() == true) return;

		form.getChildForms().addAll(((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).getFormsFromParticipation(form));
		try{
			if(form.getIsTemplate()){
				form.getElements().addAll(((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).getFormElementsFromParticipation(form));
			}
			else{
				if(form.getTemplate() != null){
					((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).populate(form.getTemplate());
					for(FormElementType fet : form.getTemplate().getElements()){
						form.getElements().add(((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).clone(fet));
					}
				}
				else{
					logger.warn("Form #" + form.getId() + " template reference is null");
				}
			}
			for(int i = 0; i < form.getElements().size();i++){
				if(form.getIsTemplate()) ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).populate(form.getElements().get(i));
				else form.getElements().get(i).getElementValues().addAll(((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getByFormElement(form,form.getElements().get(i)));
			}
		}
		catch(FactoryException fe){
			System.out.println(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		form.setPopulated(true);
		updateToCache(form);
	}
	public FormType newForm(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		FormType obj = new FormType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.FORM);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		FormType obj = (FormType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Form without a group");

		DataRow row = prepareAdd(obj, "form");
		try{
			if(obj.getTemplate() != null && obj.getTemplate().getId().compareTo(obj.getId())==0){
				System.out.println("Form cannot point to itself as a template");
				obj.setTemplate(null);
			}
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("istemplate", obj.getIsTemplate());
			row.setCellValue("isgrid",obj.getIsGrid());
			if(obj.getViewTemplate() != null) row.setCellValue("viewtemplateid", obj.getViewTemplate().getId());
			if(obj.getTemplate() != null) row.setCellValue("templateid", obj.getTemplate().getId());
			if (insertRow(row)){
				try{
					FormType cobj = (bulkMode ? obj : (FormType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					BulkFactories.getBulkFactory().setDirty(factoryType);
					BaseParticipantType part = null;

					for(int i = 0; i < obj.getElements().size();i++){
						part = ((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).newFormElementParticipation(cobj,obj.getElements().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.FORMPARTICIPATION)).add(part);
						else ((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getChildForms().size();i++){
						part = ((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).newFormParticipation(cobj,obj.getChildForms().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.FORMPARTICIPATION)).add(part);
						else ((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).add(part);
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
		FormType new_obj = new FormType();
		new_obj.setNameType(NameEnumType.FORM);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		new_obj.setDescription(rset.getString("description"));
		long view_id = rset.getLong("viewtemplateid");
		if(view_id > 0) new_obj.setViewTemplate((NoteType)((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).getById(view_id, new_obj.getOrganizationId()));
		long form_id = rset.getLong("templateid");
		if(form_id > 0) new_obj.setTemplate((FormType)getById(form_id,new_obj.getOrganizationId()));
		new_obj.setIsTemplate(rset.getBoolean("istemplate"));
		new_obj.setIsGrid(rset.getBoolean("isgrid"));
		return new_obj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		FormType data = (FormType)object;
		boolean out_bool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
			/// Elements
			Set<Long> set = new HashSet<Long>();
			BaseParticipantType[] maps = ((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).getFormElementParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getElements().size();i++){
				if(set.contains(data.getElements().get(i).getId())== false){
					((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).add(((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).newFormElementParticipation(data,data.getElements().get(i)));
				}
				else{
					set.remove(data.getElements().get(i).getId());
				}
			}
//			System.out.println("Net delete Element parts: " + set.size());
			((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// Forms
			set.clear();
			maps = ((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).getFormParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getChildForms().size();i++){
				if(set.contains(data.getChildForms().get(i).getId())== false){
					((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).add(((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).newFormParticipation(data,data.getChildForms().get(i)));
				}
				else{
					set.remove(data.getChildForms().get(i).getId());
				}
			}
//			System.out.println("Net delete Form parts: " + set.size());
			((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			
		
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
		FormType use_map = (FormType)map;
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroupId()));
		fields.add(QueryFields.getFieldIsTemplate(use_map.getIsTemplate()));
		fields.add(QueryFields.getFieldIsGrid(use_map.getIsGrid()));
		fields.add(QueryFields.getFieldTemplateId((use_map.getTemplate() != null ? use_map.getTemplate().getId() : 0)));
		if(use_map.getTemplate() != null && use_map.getTemplate().getId().compareTo(use_map.getId())==0){
			System.out.println("Form cannot point to itself as a template");
			use_map.setTemplate(null);
		}
		fields.add(QueryFields.getFieldViewTemplateId((use_map.getViewTemplate() != null ? use_map.getViewTemplate().getId() : 0)));
	}
	public int deleteFormsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteFormsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		if (deleted > 0){
			((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).deleteFormElementValuesByFormIds(new long[]{obj.getId()}, obj.getOrganizationId());
			((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).deleteParticipations(new long[]{obj.getId()}, obj.getOrganizationId());
			return true;
		}
		return false;
	}
	public int deleteFormsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).deleteFormElementValuesByFormIds(ids, organizationId);
			((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).deleteParticipations(ids, organizationId);
			
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteFormsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteFormsByIds(ids, group.getOrganizationId());
	}
	
	public List<FormType>  getFormListByGroup(DirectoryGroupType group, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, startRecord, recordCount, organizationId);
	}

	public List<FormType>  getFormList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<FormType> getFormListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
