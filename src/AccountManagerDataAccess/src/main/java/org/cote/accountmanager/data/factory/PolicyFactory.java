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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.util.LogicalTypeComparator;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;



public class PolicyFactory extends NameIdGroupFactory {
	private DatatypeFactory dtFactory = null;
	

	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.POLICY, PolicyFactory.class); }
	public PolicyFactory(){
		super();
		this.tableNames.add("policy");
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.POLICY;
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	@Override
	public void registerProvider(){
		AuthorizationService.registerAuthorizationProviders(
				FactoryEnumType.POLICY,
				NameEnumType.POLICY,
				FactoryEnumType.POLICYPARTICIPATION
			);
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
		PolicyParticipationFactory ppFact = Factories.getFactory(FactoryEnumType.POLICYPARTICIPATION);
		policy.getRules().addAll(ppFact.getRulesFromParticipation(policy));
		Collections.sort(policy.getRules(),new LogicalTypeComparator());
		policy.setPopulated(true);
		updateToCache(policy);
	}
	
	
	public PolicyType newPolicy(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
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
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		PolicyType obj = (PolicyType)object;
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
				PolicyParticipationFactory ppFact = Factories.getFactory(FactoryEnumType.POLICYPARTICIPATION);
				for(int i = 0; i < obj.getRules().size();i++){
					part = ppFact.newRuleParticipation(cobj,obj.getRules().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.POLICYPARTICIPATION)).add(part);
					else ppFact.add(part);
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
		PolicyType newObj = new PolicyType();
		newObj.setNameType(NameEnumType.POLICY);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		//newObj.setUrn(rset.getString("urn"));
		newObj.setScore(rset.getInt("score"));
		newObj.setDescription(rset.getString("description"));
		newObj.setLogicalOrder(rset.getInt("logicalorder"));
		newObj.setEnabled(rset.getBoolean("enabled"));
		newObj.setDecisionAge(rset.getLong("decisionage"));
		newObj.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		newObj.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		newObj.setExpiresDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));
		newObj.setCondition(ConditionEnumType.fromValue(rset.getString("condition")));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		PolicyType data = (PolicyType)object;
		removeFromCache(data);
		removeFromCache(data,getUrnCacheKey(data));
		data.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(new Date()));
		boolean outBool = false;
		if(super.update(data, null)){
			try{
				PolicyParticipationFactory ppFact = Factories.getFactory(FactoryEnumType.POLICYPARTICIPATION);
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ppFact.getRuleParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getRules().size();i++){
					if(set.contains(data.getRules().get(i).getId())== false){
						ppFact.add(ppFact.newRuleParticipation(data,data.getRules().get(i)));
					}
					else{
						set.remove(data.getRules().get(i).getId());
					}
				}
				ppFact.deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
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
		PolicyType useMap = (PolicyType)map;
		fields.add(QueryFields.getFieldCondition(useMap.getCondition()));
		//fields.add(QueryFields.getFieldUrn(useMap.getUrn()));
		fields.add(QueryFields.getFieldScore(useMap.getScore()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldEnabled(useMap.getEnabled()));
		//fields.add(QueryFields.getFieldCreatedDate(useMap.getCreated()));
		fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(useMap.getExpiresDate()));
		fields.add(QueryFields.getFieldDecisionAge(useMap.getDecisionAge()));

	}
	public int deletePoliciesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deletePoliciesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		PolicyType obj = (PolicyType)object;
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
			Factories.getParticipationFactory(FactoryEnumType.POLICYPARTICIPATION).deleteParticipations(ids, organizationId);
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
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<PolicyType> getPolicyListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
