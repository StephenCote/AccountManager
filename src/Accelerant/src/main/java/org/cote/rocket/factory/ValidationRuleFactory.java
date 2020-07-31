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
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.ValidationRuleType;
import org.cote.propellant.objects.types.ValidationEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;
public class ValidationRuleFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.VALIDATIONRULE, ValidationRuleFactory.class); }
	public ValidationRuleFactory(){
		super();
		this.hasParentId = false;
		this.tableNames.add("validationrule");
		factoryType = FactoryEnumType.VALIDATIONRULE;
	}

	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("validationrule")){
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

		ValidationRuleType rule = (ValidationRuleType)obj;
		if(rule.getPopulated()) return;
		rule.getRules().addAll(((ValidationRuleParticipationFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).getValidationRulesFromParticipation(rule));
		rule.setPopulated(true);
		updateToCache(rule);
	}
	
	public ValidationRuleType newValidationRule(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		ValidationRuleType obj = new ValidationRuleType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setValidationType(ValidationEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.VALIDATIONRULE);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ValidationRuleType obj = (ValidationRuleType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new ValidationRule without a group");

		DataRow row = prepareAdd(obj, "validationrule");
		try{
			row.setCellValue("allownull", obj.getAllowNull());
			row.setCellValue("expression", obj.getExpression());
			row.setCellValue("errormessage", obj.getErrorMessage());
			row.setCellValue("isruleset", obj.getIsRuleSet());
			row.setCellValue("isreplacementrule", obj.getIsReplacementRule());
			row.setCellValue("replacementvalue", obj.getReplacementValue());
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("expression", obj.getExpression());
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("comparison", obj.getComparison());
			row.setCellValue("validationtype", obj.getValidationType().toString());
			if (insertRow(row)){
				try{
					ValidationRuleType cobj = (bulkMode ? obj : (ValidationRuleType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					BulkFactories.getBulkFactory().setDirty(factoryType);
					BaseParticipantType part = null;
					for(int i = 0; i < obj.getRules().size();i++){
						part = ((ValidationRuleParticipationFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).newValidationRuleParticipation(cobj,obj.getRules().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).add(part);
						else ((ValidationRuleParticipationFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).add(part);
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
		ValidationRuleType newObj = new ValidationRuleType();
		newObj.setNameType(NameEnumType.VALIDATIONRULE);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setAllowNull(rset.getBoolean("allownull"));
		newObj.setDescription(rset.getString("description"));
		newObj.setValidationType(ValidationEnumType.valueOf(rset.getString("validationtype")));
		newObj.setExpression(rset.getString("expression"));
		newObj.setIsRuleSet(rset.getBoolean("isruleset"));
		newObj.setIsReplacementRule(rset.getBoolean("isreplacementrule"));
		newObj.setReplacementValue(rset.getString("replacementvalue"));
		newObj.setComparison(rset.getBoolean("comparison"));
		newObj.setErrorMessage(rset.getString("errormessage"));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ValidationRuleType data = (ValidationRuleType)object;
		boolean outBool = false;
		removeFromCache(data);
		
		if(update(data, null)){
			try{
			/// Rules
			Set<Long> set = new HashSet<>();
			BaseParticipantType[] maps = ((ValidationRuleParticipationFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).getValidationRuleParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getRules().size();i++){
				if(set.contains(data.getRules().get(i).getId())== false){
					((ValidationRuleParticipationFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).add(((ValidationRuleParticipationFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).newValidationRuleParticipation(data,data.getRules().get(i)));
				}
				else{
					set.remove(data.getRules().get(i).getId());
				}
			}
//			System.out.println("Net delete ValidationRule parts: " + set.size());
			((ValidationRuleParticipationFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).clearCache();
			((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).clearCache();
			
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
		ValidationRuleType useMap = (ValidationRuleType)map;
		fields.add(QueryFields.getFieldAllowNull(useMap.getAllowNull()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldValidationType(useMap.getValidationType()));
		fields.add(QueryFields.getFieldExpression(useMap.getExpression()));
		fields.add(QueryFields.getFieldIsRuleSet(useMap.getIsRuleSet()));
		fields.add(QueryFields.getFieldIsReplacementRule(useMap.getIsReplacementRule()));
		fields.add(QueryFields.getFieldReplacementValue(useMap.getReplacementValue()));
		fields.add(QueryFields.getFieldComparison(useMap.getComparison()));
		fields.add(QueryFields.getFieldErrorMessage(useMap.getErrorMessage()));
	}
	public int deleteValidationRulesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteValidationRulesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteValidationRulesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((ValidationRuleParticipationFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULEPARTICIPATION)).deleteParticipations(ids, organizationId);
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteValidationRulesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteValidationRulesByIds(ids, group.getOrganizationId());
	}
	
	public List<ValidationRuleType>  getValidationRuleListByGroup(DirectoryGroupType group, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, startRecord, recordCount, organizationId);
	}

	public List<ValidationRuleType>  getValidationRuleList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ValidationRuleType> getValidationRuleListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
