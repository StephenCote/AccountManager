package org.cote.accountmanager.data.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.ContactInformationEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;


public class AddressFactory extends NameIdGroupFactory {
	
	public AddressFactory(){
		super();
		this.hasParentId=false;
		this.tableNames.add("addresses");
		factoryType = FactoryEnumType.ADDRESS;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getGroup().getId();
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("addresses")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	public void populate(AddressType person) throws FactoryException,ArgumentException{
		if(person.getPopulated() == true) return;

		person.setPopulated(true);
		updateToCache(person);
	}
	public AddressType newAddress(UserType user, AddressType parentAddress) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		
		AddressType obj = newAddress(user,parentAddress.getGroup());
		
		return obj;
	}
	public AddressType newAddress(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		AddressType obj = new AddressType();
		obj.setLocationType(LocationEnumType.UNKNOWN);
		obj.setOrganization(group.getOrganization());
		obj.setOwnerId(user.getId());
		obj.setGroup(group);
		obj.setNameType(NameEnumType.ADDRESS);
		return obj;
	}
	
	public boolean addAddress(AddressType obj) throws FactoryException
	{

		if (obj.getGroup() == null) throw new FactoryException("Cannot add new Address without a group");

		DataRow row = prepareAdd(obj, "addresses");


		try{
			row.setCellValue("preferred", obj.getPreferred());
			row.setCellValue("addressline1",obj.getAddressLine1());
			row.setCellValue("addressline2",obj.getAddressLine2());
			row.setCellValue("city",obj.getCity());
			row.setCellValue("country",obj.getCountry());
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("postalcode",obj.getPostalCode());
			row.setCellValue("state",obj.getState());
			row.setCellValue("region",obj.getRegion());
			row.setCellValue("locationtype",obj.getLocationType().toString());
			row.setCellValue("groupid", obj.getGroup().getId());
			
			if(insertRow(row)) return true;
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} 
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		AddressType new_obj = new AddressType();
		new_obj.setNameType(NameEnumType.ADDRESS);
		super.read(rset, new_obj);

		long group_id = rset.getLong("groupid");
		new_obj.setGroup(Factories.getGroupFactory().getDirectoryById(group_id, new_obj.getOrganization()));
		new_obj.setPreferred(rset.getBoolean("preferred"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setAddressLine1(rset.getString("addressline1"));
		new_obj.setAddressLine2(rset.getString("addressline2"));
		new_obj.setCity(rset.getString("city"));
		new_obj.setCountry(rset.getString("country"));
		new_obj.setPostalCode(rset.getString("postalcode"));
		new_obj.setState(rset.getString("state"));
		new_obj.setRegion(rset.getString("region"));
		new_obj.setLocationType(LocationEnumType.valueOf(rset.getString("locationtype")));
		return new_obj;
	}
	public boolean updateAddress(AddressType data) throws FactoryException, DataAccessException
	{	
		removeFromCache(data);
		return update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		AddressType use_map = (AddressType)map;
		fields.add(QueryFields.getFieldPreferred(use_map.getPreferred()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldAddressLine1(use_map.getAddressLine1()));
		fields.add(QueryFields.getFieldAddressLine2(use_map.getAddressLine2()));
		fields.add(QueryFields.getFieldCity(use_map.getCity()));
		fields.add(QueryFields.getFieldState(use_map.getState()));
		fields.add(QueryFields.getFieldRegion(use_map.getRegion()));
		fields.add(QueryFields.getFieldLocationType(use_map.getLocationType()));
		fields.add(QueryFields.getFieldCountry(use_map.getCountry()));
		fields.add(QueryFields.getFieldPostalCode(use_map.getPostalCode()));
	}
	public int deleteAddresssByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deleteAddresssByIds(ids, user.getOrganization());
	}

	public boolean deleteAddress(AddressType obj) throws FactoryException
	{
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		return (deleted > 0);
	}
	public int deleteAddresssByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			/*
			Factories.getContactInformationFactory().deleteContactInformationByReferenceIds(ids,organization.getId());
			Factories.getAddressParticipationFactory().deleteParticipations(ids, organization);

			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deleteAddresssInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deleteAddresssByIds(ids, group.getOrganization());
	}
/*	
	public List<AddressType> getChildAddressList(AddressType parent) throws FactoryException,ArgumentException{

		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroup(parent.getGroup().getId()));
		return getAddressList(fields.toArray(new QueryField[0]), 0,0,parent.getOrganization());

	}
*/
	public List<AddressType>  getAddressList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<AddressType> getAddressListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
	
	public List<AddressType> searchAddresss(String searchValue, int startRecord, int recordCount, DirectoryGroupType dir) throws FactoryException{
	
		ProcessingInstructionType instruction = null;
		if(startRecord >= 0 && recordCount >= 0){
			instruction = new ProcessingInstructionType();
			instruction.setOrderClause("name ASC");
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		
		List<QueryField> fields = buildSearchQuery(searchValue, dir.getOrganization());
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		return search(fields.toArray(new QueryField[0]), instruction, dir.getOrganization());
	}
	
	
	/// Address search uses a different query to join in contact information
	/// Otherwise, this could be the getPaginatedList method
	///
	/// public List<AddressType> search(QueryField[] filters, OrganizationType organization){
	@Override
	public List<QueryField> buildSearchQuery(String searchValue, OrganizationType organization) throws FactoryException{
		
		searchValue = searchValue.replaceAll("\\*","%");
		
		List<QueryField> filters = new ArrayList<QueryField>();
		QueryField search_filters = new QueryField(SqlDataEnumType.NULL,"searchgroup",null);
		search_filters.setComparator(ComparatorEnumType.GROUP_OR);
		QueryField name_filter = new QueryField(SqlDataEnumType.VARCHAR,"name",searchValue);
		name_filter.setComparator(ComparatorEnumType.LIKE);
		search_filters.getFields().add(name_filter);
		QueryField first_name_filter = new QueryField(SqlDataEnumType.VARCHAR,"firstname",searchValue);
		first_name_filter.setComparator(ComparatorEnumType.LIKE);
		search_filters.getFields().add(first_name_filter);
		filters.add(search_filters);
		return filters;
	}
	
	@Override
	public <T> List<T> search(QueryField[] filters, ProcessingInstructionType instruction, OrganizationType organization){
		return searchByIdInView("personContact", filters,instruction,organization);

/*
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String sqlQuery = assembleQueryString("SELECT id FROM personContact", filters, connectionType, instruction, organization.getId());
		logger.info("Query=" + sqlQuery);
		List<Long> ids = new ArrayList<Long>();
		List<T> persons = new ArrayList<T>();
		
		try{
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(filters, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				ids.add(rset.getLong("id"));
			}
			rset.close();
			
			/// don't paginate the subsequent search for ids because it was already filtered.
			/// Create a new instruction and just copy the order clause
			///
			ProcessingInstructionType pi2 = new ProcessingInstructionType();
			pi2.setOrderClause(instruction.getOrderClause());
			persons = getListByIds(ArrayUtils.toPrimitive(ids.toArray(new Long[0])),pi2,organization);
			logger.info("Retrieved " + persons.size() + " from " + ids.size() + " ids");
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//return search(fields, instruction, organization);
		return persons;
*/
	}


	
}
