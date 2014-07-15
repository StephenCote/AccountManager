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
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
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
	
	public void populate(PolicyType policy) throws FactoryException,ArgumentException{
		if(policy.getPopulated()) return;
		/*
		policy.getArtifacts().addAll(Factories.getPolicyParticipationFactory().getArtifactsFromParticipation(policy));
		policy.getDependencies().addAll(Factories.getPolicyParticipationFactory().getDependenciesFromParticipation(policy));
		policy.getCases().addAll(Factories.getPolicyParticipationFactory().getCasesFromParticipation(policy));
		policy.getRequirements().addAll(Factories.getPolicyParticipationFactory().getRequirementsFromParticipation(policy));
		policy.getPolicys().addAll(Factories.getPolicyParticipationFactory().getPolicysFromParticipation(policy));
		*/
		policy.getRules().addAll(Factories.getPolicyParticipationFactory().getRulesFromParticipation(policy));

		policy.setPopulated(true);
		updateToCache(policy);
	}
	
	
	public PolicyType newPolicy(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		PolicyType obj = new PolicyType();
		obj.setOrganization(group.getOrganization());
		obj.setOwnerId(user.getId());
		obj.setGroup(group);
		obj.setNameType(NameEnumType.POLICY);
		
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		obj.setCreated(dtFactory.newXMLGregorianCalendar(cal));
		obj.setModified(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.YEAR, 1);
		obj.setExpires(dtFactory.newXMLGregorianCalendar(cal));

		return obj;
	}
	
	public boolean addPolicy(PolicyType obj) throws FactoryException
	{
		if (obj.getGroup() == null) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "policy");
		try{
			row.setCellValue("groupid", obj.getGroup().getId());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("urn", obj.getUrn());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
			row.setCellValue("createddate", obj.getCreated());
			row.setCellValue("modifieddate", obj.getModified());
			row.setCellValue("expirationdate", obj.getExpires());
			row.setCellValue("decisionage", obj.getDecisionAge());
			row.setCellValue("enabled", obj.getEnabled());

			if (insertRow(row)){
				PolicyType cobj = (bulkMode ? obj : (PolicyType)getByName(obj.getName(), obj.getGroup()));
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
		new_obj.setNameType(NameEnumType.MODEL);
		super.read(rset, new_obj);
		new_obj.setUrn(rset.getString("urn"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setLogicalOrder(rset.getInt("logicalorder"));
		new_obj.setEnabled(rset.getBoolean("enabled"));
		new_obj.setDecisionAge(rset.getLong("decisionage"));
		new_obj.setCreated(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		new_obj.setModified(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		new_obj.setExpires(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));

		long group_id = rset.getLong("groupid");
		new_obj.setGroup(Factories.getGroupFactory().getDirectoryById(group_id, new_obj.getOrganization()));
		return new_obj;
	}
	public boolean updatePolicy(PolicyType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		removeFromCache(data,getUrnCacheKey(data));
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
				Factories.getPolicyParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganization());
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
		fields.add(QueryFields.getFieldUrn(use_map.getUrn()));
		fields.add(QueryFields.getFieldLogicalOrder(use_map.getLogicalOrder()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
	}
	public int deletePoliciesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deletePoliciesByIds(ids, user.getOrganization());
	}

	public boolean deletePolicy(PolicyType obj) throws FactoryException
	{
		removeFromCache(obj);
		removeFromCache(obj,getUrnCacheKey(obj));
		//int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		int deleted = deletePoliciesByIds(new long[]{obj.getId()},obj.getOrganization());
		return (deleted > 0);
	}
	public int deletePoliciesByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			Factories.getPolicyParticipationFactory().deleteParticipations(ids, organization);
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
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deletePoliciesByIds(ids, group.getOrganization());
	}
	public String getUrnCacheKey(PolicyType policy){
		return getUrnCacheKey(policy.getUrn(),policy.getOrganization());
	}
	public String getUrnCacheKey(String urn, OrganizationType org){
		return urn + "-" + org.getId();
	}
	public PolicyType getByUrn(String urn, OrganizationType organization){
		PolicyType policy = readCache(getUrnCacheKey(urn, organization));
		if(policy != null){
			return policy;
		}
		try {
			List<PolicyType> policys = getPolicies(new QueryField[]{QueryFields.getFieldUrn(urn)},organization);
			if(policys.size() >= 1){
				policy = policys.get(0);
				addToCache(policy, getUrnCacheKey(policy));
				addToCache(policy);
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return policy;
	}
	public List<PolicyType> getPolicies(QueryField[] matches, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organization.getId());
		return convertList(lst);

	}
	
	public List<PolicyType>  getPolicyList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<PolicyType> getPolicyListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
}
