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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;



public class RuleFactory extends NameIdGroupFactory {
	
	public RuleFactory(){
		super();
		this.tableNames.add("rule");
		this.hasObjectId = true;
		factoryType = FactoryEnumType.RULE;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("rule")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	public void populate(RuleType rule) throws FactoryException,ArgumentException{
		if(rule.getPopulated()) return;
		/*
		rule.getArtifacts().addAll(Factories.getRuleParticipationFactory().getArtifactsFromParticipation(rule));
		rule.getDependencies().addAll(Factories.getRuleParticipationFactory().getDependenciesFromParticipation(rule));
		rule.getCases().addAll(Factories.getRuleParticipationFactory().getCasesFromParticipation(rule));
		rule.getRequirements().addAll(Factories.getRuleParticipationFactory().getRequirementsFromParticipation(rule));
		rule.getRules().addAll(Factories.getRuleParticipationFactory().getRulesFromParticipation(rule));
		*/
		rule.getPatterns().addAll(Factories.getRuleParticipationFactory().getPatternsFromParticipation(rule));
		rule.setPopulated(true);
		updateToCache(rule);
	}
	
	
	public RuleType newRule(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		RuleType obj = new RuleType();
		obj.setOrganization(group.getOrganization());
		obj.setOwnerId(user.getId());
		obj.setGroup(group);
		obj.setRuleType(RuleEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.RULE);
		return obj;
	}
	
	public boolean addRule(RuleType obj) throws FactoryException
	{
		if (obj.getGroup() == null) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "rule");
		try{
			row.setCellValue("ruletype", obj.getRuleType().toString());
			row.setCellValue("condition", obj.getCondition().toString());
			row.setCellValue("groupid", obj.getGroup().getId());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("urn", obj.getUrn());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
			if (insertRow(row)){
				RuleType cobj = (bulkMode ? obj : (RuleType)getByName(obj.getName(), obj.getGroup()));
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < obj.getPatterns().size();i++){
					part = Factories.getRuleParticipationFactory().newPatternParticipation(cobj,obj.getPatterns().get(i));
					if(bulkMode) BulkFactories.getBulkRuleParticipationFactory().addParticipant(part);
					else Factories.getRuleParticipationFactory().addParticipant(part);
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
		RuleType new_obj = new RuleType();
		new_obj.setNameType(NameEnumType.MODEL);
		super.read(rset, new_obj);
		new_obj.setRuleType(RuleEnumType.valueOf(rset.getString("ruletype")));
		new_obj.setCondition(ConditionEnumType.valueOf(rset.getString("condition")));
		new_obj.setUrn(rset.getString("urn"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setLogicalOrder(rset.getInt("logicalorder"));
		long group_id = rset.getLong("groupid");
		new_obj.setGroup(Factories.getGroupFactory().getDirectoryById(group_id, new_obj.getOrganization()));
		return new_obj;
	}
	public boolean updateRule(RuleType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		boolean out_bool = false;
		if(update(data, null)){
			try{
				
				Set<Long> set = new HashSet<Long>();
				BaseParticipantType[] maps = Factories.getRuleParticipationFactory().getFactParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getPatterns().size();i++){
					if(set.contains(data.getPatterns().get(i).getId())== false){
						Factories.getRuleParticipationFactory().addParticipant(Factories.getRuleParticipationFactory().newPatternParticipation(data,data.getPatterns().get(i)));
					}
					else{
						set.remove(data.getPatterns().get(i).getId());
					}
				}
				Factories.getRuleParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganization());
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
		RuleType use_map = (RuleType)map;
		fields.add(QueryFields.getFieldUrn(use_map.getUrn()));
		fields.add(QueryFields.getFieldLogicalOrder(use_map.getLogicalOrder()));
		fields.add(QueryFields.getFieldRuleType(use_map.getRuleType()));
		fields.add(QueryFields.getFieldCondition(use_map.getCondition()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
	}
	public int deleteRulesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deleteRulesByIds(ids, user.getOrganization());
	}

	public boolean deleteRule(RuleType obj) throws FactoryException
	{
		removeFromCache(obj);
		//int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		int deleted = deleteRulesByIds(new long[]{obj.getId()},obj.getOrganization());
		return (deleted > 0);
	}
	public int deleteRulesByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			Factories.getRuleParticipationFactory().deleteParticipations(ids, organization);
			/*
			Factories.getRuleParticipationFactory().deleteParticipations(ids, organization);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deleteRulesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deleteRulesByIds(ids, group.getOrganization());
	}
	public List<FactType> getRules(QueryField[] matches, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organization.getId());
		return convertList(lst);

	}
	
	public List<RuleType>  getRuleList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<RuleType> getRuleListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
}
