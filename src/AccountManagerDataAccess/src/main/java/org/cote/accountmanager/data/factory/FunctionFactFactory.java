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
import org.cote.accountmanager.objects.FunctionFactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;



public class FunctionFactFactory extends NameIdGroupFactory {
	
	public FunctionFactFactory(){
		super();
		this.tableNames.add("functionfact");
		this.hasObjectId = true;
		factoryType = FactoryEnumType.FUNCTIONFACT;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("functionfact")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	public void populate(FunctionFactType cycle) throws FactoryException,ArgumentException{
		if(cycle.getPopulated()) return;
		/*
		cycle.getArtifacts().addAll(Factories.getFunctionFactParticipationFactory().getArtifactsFromParticipation(cycle));
		cycle.getDependencies().addAll(Factories.getFunctionFactParticipationFactory().getDependenciesFromParticipation(cycle));
		cycle.getCases().addAll(Factories.getFunctionFactParticipationFactory().getCasesFromParticipation(cycle));
		cycle.getRequirements().addAll(Factories.getFunctionFactParticipationFactory().getRequirementsFromParticipation(cycle));
		cycle.getFunctionFunctionFacts().addAll(Factories.getFunctionFactParticipationFactory().getFunctionFunctionFactsFromParticipation(cycle));
		*/
		cycle.setPopulated(true);
		updateToCache(cycle);
	}
	
	
	public FunctionFactType newFunctionFact(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		FunctionFactType obj = new FunctionFactType();
		obj.setOrganization(group.getOrganization());
		obj.setOwnerId(user.getId());
		obj.setGroup(group);
		obj.setNameType(NameEnumType.FUNCTIONFACT);
		return obj;
	}
	
	public boolean addFunctionFact(FunctionFactType obj) throws FactoryException
	{
		if (obj.getGroup() == null) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "functionfact");
		try{

			row.setCellValue("groupid", obj.getGroup().getId());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("urn", obj.getUrn());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
			row.setCellValue("functionurn", obj.getFunctionUrn());
			row.setCellValue("facturn", obj.getFactUrn());
			
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
		FunctionFactType new_obj = new FunctionFactType();
		new_obj.setNameType(NameEnumType.MODEL);
		super.read(rset, new_obj);

		new_obj.setUrn(rset.getString("urn"));
		new_obj.setFunctionUrn(rset.getString("functionurn"));
		new_obj.setFactUrn(rset.getString("facturn"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setLogicalOrder(rset.getInt("logicalorder"));
		long group_id = rset.getLong("groupid");
		new_obj.setGroup(Factories.getGroupFactory().getDirectoryById(group_id, new_obj.getOrganization()));
		return new_obj;
	}
	public boolean updateFunctionFact(FunctionFactType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		return update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		FunctionFactType use_map = (FunctionFactType)map;
		fields.add(QueryFields.getFieldUrn(use_map.getUrn()));
		fields.add(QueryFields.getFieldFunctionUrn(use_map.getFunctionUrn()));
		fields.add(QueryFields.getFieldFactUrn(use_map.getFactUrn()));
		fields.add(QueryFields.getFieldLogicalOrder(use_map.getLogicalOrder()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
	}
	public int deleteFunctionFactsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deleteFunctionFactsByIds(ids, user.getOrganization());
	}

	public boolean deleteFunctionFact(FunctionFactType obj) throws FactoryException
	{
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		return (deleted > 0);
	}
	public int deleteFunctionFactsByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			/*
			Factories.getFunctionFactParticipationFactory().deleteParticipations(ids, organization);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deleteFunctionFactsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deleteFunctionFactsByIds(ids, group.getOrganization());
	}
	
	public List<FactType> getFunctionFacts(QueryField[] matches, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organization.getId());
		return convertList(lst);

	}
	public List<FunctionFactType>  getFunctionFactList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<FunctionFactType> getFunctionFactListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
}
