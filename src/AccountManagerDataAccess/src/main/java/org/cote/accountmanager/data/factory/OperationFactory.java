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

import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;



public class OperationFactory extends NameIdGroupFactory {
	
	public OperationFactory(){
		super();
		this.tableNames.add("operation");
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.OPERATION;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("operation")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	public void populate(OperationType cycle) throws FactoryException,ArgumentException{
		if(cycle.getPopulated()) return;
		/*
		cycle.getArtifacts().addAll(Factories.getOperationParticipationFactory().getArtifactsFromParticipation(cycle));
		cycle.getDependencies().addAll(Factories.getOperationParticipationFactory().getDependenciesFromParticipation(cycle));
		cycle.getCases().addAll(Factories.getOperationParticipationFactory().getCasesFromParticipation(cycle));
		cycle.getRequirements().addAll(Factories.getOperationParticipationFactory().getRequirementsFromParticipation(cycle));
		cycle.getOperations().addAll(Factories.getOperationParticipationFactory().getOperationsFromParticipation(cycle));
		*/
		cycle.setPopulated(true);
		updateToCache(cycle);
	}
	
	
	public OperationType newOperation(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		OperationType obj = new OperationType();
		obj.setOrganization(group.getOrganization());
		obj.setOwnerId(user.getId());
		obj.setGroup(group);
		obj.setOperationType(OperationEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.OPERATION);
		return obj;
	}
	
	public boolean addOperation(OperationType obj) throws FactoryException
	{
		if (obj.getGroup() == null) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "operation");
		try{
			row.setCellValue("operationtype", obj.getOperationType().toString());
			row.setCellValue("groupid", obj.getGroup().getId());
			row.setCellValue("description", obj.getDescription());
			//row.setCellValue("urn", obj.getUrn());
			row.setCellValue("score", obj.getScore());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
			row.setCellValue("operation", obj.getOperation());
			
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
		OperationType new_obj = new OperationType();
		new_obj.setNameType(NameEnumType.OPERATION);
		super.read(rset, new_obj);
		new_obj.setOperationType(OperationEnumType.valueOf(rset.getString("operationtype")));
		//new_obj.setUrn(rset.getString("urn"));
		new_obj.setScore(rset.getInt("score"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setOperation(rset.getString("operation"));
		new_obj.setLogicalOrder(rset.getInt("logicalorder"));
		long group_id = rset.getLong("groupid");
		new_obj.setGroup(Factories.getGroupFactory().getDirectoryById(group_id, new_obj.getOrganization()));
		return new_obj;
	}
	public boolean updateOperation(OperationType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		removeFromCache(data,getUrnCacheKey(data));
		return update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		OperationType use_map = (OperationType)map;
		//fields.add(QueryFields.getFieldUrn(use_map.getUrn()));
		fields.add(QueryFields.getFieldScore(use_map.getScore()));
		fields.add(QueryFields.getFieldOperation(use_map.getOperation()));
		fields.add(QueryFields.getFieldLogicalOrder(use_map.getLogicalOrder()));
		fields.add(QueryFields.getFieldOperationType(use_map.getOperationType()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
	}
	public int deleteOperationsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deleteOperationsByIds(ids, user.getOrganization());
	}

	public boolean deleteOperation(OperationType obj) throws FactoryException
	{
		removeFromCache(obj);
		removeFromCache(obj,getUrnCacheKey(obj));
		int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		return (deleted > 0);
	}
	public int deleteOperationsByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			/*
			Factories.getOperationParticipationFactory().deleteParticipations(ids, organization);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deleteOperationsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deleteOperationsByIds(ids, group.getOrganization());
	}
	public String getUrnCacheKey(OperationType operation){
		return getUrnCacheKey(operation.getUrn(),operation.getOrganization());
	}
	public String getUrnCacheKey(String urn, OrganizationType org){
		return urn + "-" + org.getId();
	}
	public OperationType getByUrn(String urn, OrganizationType organization){
		OperationType operation = readCache(getUrnCacheKey(urn, organization));
		if(operation != null){
			return operation;
		}
		try {
			List<OperationType> operations = getOperations(new QueryField[]{QueryFields.getFieldUrn(urn)},organization);
			if(operations.size() >= 1){
				operation = operations.get(0);
				addToCache(operation, getUrnCacheKey(operation));
				addToCache(operation);
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
		return operation;
	}
	public List<OperationType> getOperations(QueryField[] matches, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organization.getId());
		return convertList(lst);

	}
	public List<OperationType>  getOperationList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<OperationType> getOperationListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
}
