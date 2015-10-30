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
/*
 * 
 * TODO: These are all cookie-cutter factory files, more or less, that use my particular (preferred) style for handling bulk operations
 * BUT - these could also be generated instead of copy/pasted/tweaked, and could be refactored into a parent abstract class
 */

package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.util.LogicalTypeComparator;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;



public class PolicyFactory extends NameIdGroupFactory {
	private DatatypeFactory dtFactory = null;
	
	public PolicyFactory(){
		super();
		this.tableNames.add("policy");
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.POLICY;
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("policy")){
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
		PolicyType policy = (PolicyType)obj;
		if(policy.getPopulated()) return;
		policy.getRules().addAll(Factories.getPolicyParticipationFactory().getRulesFromParticipation(policy));
		Collections.sort(policy.getRules(),new LogicalTypeComparator());
		policy.setPopulated(true);
		updateToCache(policy);
	}
	
	
	public PolicyType newPolicy(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		PolicyType obj = new PolicyType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.POLICY);
		obj.setCondition(ConditionEnumType.UNKNOWN);
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		obj.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		obj.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.YEAR, 1);
		obj.setExpiresDate(dtFactory.newXMLGregorianCalendar(cal));

		return obj;
	}
	
	public boolean addPolicy(PolicyType obj) throws FactoryException
	{
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "policy");
		try{
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("description", obj.getDescription());
			//row.setCellValue("urn", obj.getUrn());
			
			row.setCellValue("score", obj.getScore());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
			row.setCellValue("createddate", obj.getCreatedDate());
			row.setCellValue("modifieddate", obj.getModifiedDate());
			row.setCellValue("expirationdate", obj.getExpiresDate());
			row.setCellValue("decisionage", obj.getDecisionAge());
			row.setCellValue("enabled", obj.getEnabled());
			row.setCellValue("condition", obj.getCondition().toString());

			if (insertRow(row)){
				PolicyType cobj = (bulkMode ? obj : (PolicyType)getByNameInGroup(obj.getName(), obj.getGroupId(),obj.getOrganizationId()));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < obj.getRules().size();i++){
					part = Factories.getPolicyParticipationFactory().newRuleParticipation(cobj,obj.getRules().get(i));
					if(bulkMode) BulkFactories.getBulkPolicyParticipationFactory().addParticipant(part);
					else Factories.getPolicyParticipationFactory().addParticipant(part);
				}
				return true;
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		PolicyType new_obj = new PolicyType();
		new_obj.setNameType(NameEnumType.POLICY);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		//new_obj.setUrn(rset.getString("urn"));
		new_obj.setScore(rset.getInt("score"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setLogicalOrder(rset.getInt("logicalorder"));
		new_obj.setEnabled(rset.getBoolean("enabled"));
		new_obj.setDecisionAge(rset.getLong("decisionage"));
		new_obj.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		new_obj.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		new_obj.setExpiresDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));
		new_obj.setCondition(ConditionEnumType.fromValue(rset.getString("condition")));
		return new_obj;
	}
	public boolean updatePolicy(PolicyType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		removeFromCache(data,getUrnCacheKey(data));
		data.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(new Date()));
		boolean out_bool = false;
		if(update(data, null)){
			try{
				
				Set<Long> set = new HashSet<Long>();
				BaseParticipantType[] maps = Factories.getPolicyParticipationFactory().getRuleParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getRules().size();i++){
					if(set.contains(data.getRules().get(i).getId())== false){
						Factories.getPolicyParticipationFactory().addParticipant(Factories.getPolicyParticipationFactory().newRuleParticipation(data,data.getRules().get(i)));
					}
					else{
						set.remove(data.getRules().get(i).getId());
					}
				}
				Factories.getPolicyParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
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
		PolicyType use_map = (PolicyType)map;
		fields.add(QueryFields.getFieldCondition(use_map.getCondition()));
		//fields.add(QueryFields.getFieldUrn(use_map.getUrn()));
		fields.add(QueryFields.getFieldScore(use_map.getScore()));
		fields.add(QueryFields.getFieldLogicalOrder(use_map.getLogicalOrder()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroupId()));
		fields.add(QueryFields.getFieldEnabled(use_map.getEnabled()));
		//fields.add(QueryFields.getFieldCreatedDate(use_map.getCreated()));
		fields.add(QueryFields.getFieldModifiedDate(use_map.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(use_map.getExpiresDate()));
		fields.add(QueryFields.getFieldDecisionAge(use_map.getDecisionAge()));

	}
	public int deletePoliciesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deletePoliciesByIds(ids, user.getOrganizationId());
	}

	public boolean deletePolicy(PolicyType obj) throws FactoryException
	{
		removeFromCache(obj);
		removeFromCache(obj,getUrnCacheKey(obj));
		//int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		int deleted = deletePoliciesByIds(new long[]{obj.getId()},obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deletePoliciesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			Factories.getPolicyParticipationFactory().deleteParticipations(ids, organizationId);
			/*
			Factories.getPolicyParticipationFactory().deleteParticipations(ids, organization);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deletePoliciesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deletePoliciesByIds(ids, group.getOrganizationId());
	}

	public List<PolicyType> getPolicies(QueryField[] matches, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organizationId);
		return convertList(lst);

	}
	
	public List<PolicyType>  getPolicyList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organizationId);
	}
	public List<PolicyType> getPolicyListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organizationId);
	}
	
}
