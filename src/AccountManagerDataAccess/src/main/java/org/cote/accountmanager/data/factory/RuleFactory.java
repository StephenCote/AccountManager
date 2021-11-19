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
/*
 * 
 * TODO: These are all cookie-cutter factory files, more or less, that use my particular (preferred) style for handling bulk operations
 * BUT - these could also be generated instead of copy/pasted/tweaked, and could be refactored into a parent abstract class
 */

package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.util.LogicalTypeComparator;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;



public class RuleFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.RULE, RuleFactory.class); }
	public RuleFactory(){
		super();
		this.primaryTableName = "rule";
		this.tableNames.add(primaryTableName);
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.RULE;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// restrict columns
		}
	}

	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		RuleType rule = (RuleType)obj;
		if(rule.getPopulated()) return;
		RuleParticipationFactory rpFact = Factories.getFactory(FactoryEnumType.RULEPARTICIPATION);
		rule.getPatterns().addAll(rpFact.getPatternsFromParticipation(rule));
		rule.getRules().addAll(rpFact.getRulesFromParticipation(rule));
		Collections.sort(rule.getPatterns(),new LogicalTypeComparator());
		rule.setPopulated(true);
		updateToCache(rule);
	}
	
	
	public RuleType newRule(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		RuleType obj = new RuleType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setRuleType(RuleEnumType.UNKNOWN);
		obj.setCondition(ConditionEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.RULE);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		RuleType obj = (RuleType)object;
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.RULETYPE), obj.getRuleType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.CONDITION), obj.getCondition().toString());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			row.setCellValue(Columns.get(ColumnEnumType.SCORE), obj.getScore());
			row.setCellValue(Columns.get(ColumnEnumType.LOGICALORDER), obj.getLogicalOrder());
			if (insertRow(row)){
				
				RuleType cobj = (bulkMode ? obj : (RuleType)getByNameInGroup(obj.getName(), obj.getGroupId(),obj.getOrganizationId()));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;
				RuleParticipationFactory rpFact = Factories.getFactory(FactoryEnumType.RULEPARTICIPATION);
				for(int i = 0; i < obj.getRules().size();i++){
					part = rpFact.newRuleParticipation(cobj,obj.getRules().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.RULEPARTICIPATION)).add(part);
					else rpFact.add(part);
				}
				for(int i = 0; i < obj.getPatterns().size();i++){
					part = rpFact.newPatternParticipation(cobj,obj.getPatterns().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.RULEPARTICIPATION)).add(part);
					else rpFact.add(part);
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
		RuleType newObj = new RuleType();
		newObj.setNameType(NameEnumType.RULE);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setRuleType(RuleEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.RULETYPE))));
		newObj.setCondition(ConditionEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.CONDITION))));
		newObj.setScore(rset.getInt(Columns.get(ColumnEnumType.SCORE)));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		newObj.setLogicalOrder(rset.getInt(Columns.get(ColumnEnumType.LOGICALORDER)));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		RuleType data = (RuleType)object;
		removeFromCache(data);
		boolean outBool = false;
		if(update(data, null)){
			try{
				RuleParticipationFactory rpFact = Factories.getFactory(FactoryEnumType.RULEPARTICIPATION);
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = rpFact.getPatternParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getPatterns().size();i++){
					if(set.contains(data.getPatterns().get(i).getId())== false){
						rpFact.add(rpFact.newPatternParticipation(data,data.getPatterns().get(i)));
					}
					else{
						set.remove(data.getPatterns().get(i).getId());
					}
				}
				
				maps = rpFact.getRuleParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getRules().size();i++){
					if(set.contains(data.getRules().get(i).getId())== false){
						rpFact.add(rpFact.newRuleParticipation(data,data.getRules().get(i)));
					}
					else{
						set.remove(data.getRules().get(i).getId());
					}
				}

				
				rpFact.deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				outBool = true;
			}
			catch(ArgumentException  ae){
				throw new FactoryException(ae.getMessage());
			}
		}
		return outBool;
	
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		RuleType useMap = (RuleType)map;
		fields.add(QueryFields.getFieldScore(useMap.getScore()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldRuleType(useMap.getRuleType()));
		fields.add(QueryFields.getFieldCondition(useMap.getCondition()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteRulesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteRulesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		RuleType obj = (RuleType)object;
		removeFromCache(obj);
		//int deleted = deleteById(obj.getId(), obj.getOrganizationId().getId());
		int deleted = deleteRulesByIds(new long[]{obj.getId()},obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteRulesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			Factories.getParticipationFactory(FactoryEnumType.RULEPARTICIPATION).deleteParticipations(ids, organizationId);
		}
		return deleted;
	}
	public int deleteRulesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteRulesByIds(ids, group.getOrganizationId());
	}
	public List<FactType> getRules(QueryField[] matches, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organizationId);
		return convertList(lst);

	}
	
	public List<RuleType>  getRuleList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<RuleType> getRuleListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
