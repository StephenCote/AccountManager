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
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;



public class FactFactory extends NameIdGroupFactory {
	
	public FactFactory(){
		super();
		this.tableNames.add("fact");
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.FACT;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("fact")){
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
		FactType fact = (FactType)obj;
		if(fact.getPopulated()) return;
		fact.setPopulated(true);
		updateToCache(fact);
	}
	
	
	public FactType newFact(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		FactType obj = new FactType();
		obj.setOrganization(group.getOrganization());
		obj.setOwnerId(user.getId());
		obj.setFactType(FactEnumType.UNKNOWN);
		obj.setGroup(group);
		obj.setNameType(NameEnumType.FACT);
		obj.setFactoryType(FactoryEnumType.UNKNOWN);
		obj.setSourceDataType(SqlDataEnumType.UNKNOWN);
		return obj;
	}
	
	public boolean addFact(FactType obj) throws FactoryException
	{
		if (obj.getGroup() == null) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "fact");
		try{
			row.setCellValue("facttype", obj.getFactType().toString());
			row.setCellValue("factorytype", obj.getFactoryType().toString());
			row.setCellValue("groupid", obj.getGroup().getId());
			row.setCellValue("factdata", obj.getFactData());
			row.setCellValue("description", obj.getDescription());
			//row.setCellValue("urn", obj.getUrn());
			row.setCellValue("score", obj.getScore());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
			row.setCellValue("sourceurn", obj.getSourceUrn());
			row.setCellValue("sourceurl", obj.getSourceUrl());
			row.setCellValue("sourcetype", obj.getSourceType());
			row.setCellValue("sourcedatatype", obj.getSourceDataType().toString());
			
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
		FactType new_obj = new FactType();
		new_obj.setNameType(NameEnumType.FACT);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		new_obj.setFactType(FactEnumType.valueOf(rset.getString("facttype")));
		new_obj.setFactoryType(FactoryEnumType.valueOf(rset.getString("factorytype")));
		//new_obj.setUrn(rset.getString("urn"));
		new_obj.setScore(rset.getInt("score"));
		new_obj.setFactData(rset.getString("factdata"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setSourceUrn(rset.getString("sourceurn"));
		new_obj.setSourceUrl(rset.getString("sourceurl"));
		new_obj.setSourceType(rset.getString("sourcetype"));
		new_obj.setSourceDataType(SqlDataEnumType.valueOf(rset.getString("sourcedatatype")));
		new_obj.setLogicalOrder(rset.getInt("logicalorder"));
		return new_obj;
	}
	public boolean updateFact(FactType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		removeFromCache(data,getUrnCacheKey(data));
		return update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		FactType use_map = (FactType)map;
		//fields.add(QueryFields.getFieldUrn(use_map.getUrn()));
		fields.add(QueryFields.getFieldScore(use_map.getScore()));
		fields.add(QueryFields.getFieldFactData(use_map.getFactData()));
		fields.add(QueryFields.getFieldSourceUrn(use_map.getSourceUrn()));
		fields.add(QueryFields.getFieldSourceUrl(use_map.getSourceUrl()));
		fields.add(QueryFields.getFieldSourceType(use_map.getSourceType()));
		fields.add(QueryFields.getFieldSourceDataType(use_map.getSourceDataType()));
		fields.add(QueryFields.getFieldLogicalOrder(use_map.getLogicalOrder()));
		fields.add(QueryFields.getFieldFactType(use_map.getFactType()));
		fields.add(QueryFields.getFieldFactoryType(use_map.getFactoryType()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
	}
	public int deleteFactsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deleteFactsByIds(ids, user.getOrganization());
	}

	public boolean deleteFact(FactType obj) throws FactoryException
	{
		removeFromCache(obj);
		removeFromCache(obj,getUrnCacheKey(obj));
		int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		return (deleted > 0);
	}
	public int deleteFactsByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			/*
			Factories.getFactParticipationFactory().deleteParticipations(ids, organization);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deleteFactsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deleteFactsByIds(ids, group.getOrganization());
	}

	
	public List<FactType> getFacts(QueryField[] matches, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organization.getId());
		return convertList(lst);

	}
	public List<FactType>  getFactList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<FactType> getFactListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
}
