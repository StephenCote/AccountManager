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
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.ContactInformationEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;


public class ContactFactory extends NameIdGroupFactory {
	
	public ContactFactory(){
		super();
		this.hasParentId=false;
		this.tableNames.add("contacts");
		factoryType = FactoryEnumType.CONTACT;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getGroup().getId();
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("contacts")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	public void populate(ContactType person) throws FactoryException,ArgumentException{
		if(person.getPopulated() == true) return;

		person.setPopulated(true);
		updateToCache(person);
	}
	public ContactType newContact(UserType user, ContactType parentContact) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		
		ContactType obj = newContact(user,parentContact.getGroup());
		
		return obj;
	}
	public ContactType newContact(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getId().equals(0L)) throw new ArgumentException("Invalid owner");
		ContactType obj = new ContactType();
		
		obj.setLocationType(LocationEnumType.UNKNOWN);
		obj.setContactType(ContactEnumType.UNKNOWN);

		obj.setOrganization(group.getOrganization());
		obj.setOwnerId(user.getId());
		obj.setGroup(group);
		obj.setNameType(NameEnumType.CONTACT);
		return obj;
	}
	
	public boolean addContact(ContactType obj) throws FactoryException
	{

		if (obj.getGroup() == null) throw new FactoryException("Cannot add new Contact without a group");

		DataRow row = prepareAdd(obj, "contacts");


		try{
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("locationtype",obj.getLocationType().toString());
			row.setCellValue("contacttype",obj.getContactType().toString());
			row.setCellValue("contactvalue",obj.getContactValue());
			row.setCellValue("groupid", obj.getGroup().getId());
			row.setCellValue("preferred", obj.getPreferred());
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
		ContactType new_obj = new ContactType();
		new_obj.setNameType(NameEnumType.CONTACT);
		super.read(rset, new_obj);

		long group_id = rset.getLong("groupid");
		new_obj.setGroup(Factories.getGroupFactory().getDirectoryById(group_id, new_obj.getOrganization()));
		new_obj.setPreferred(rset.getBoolean("preferred"));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setLocationType(LocationEnumType.valueOf(rset.getString("locationtype")));
		new_obj.setContactType(ContactEnumType.valueOf(rset.getString("contacttype")));
		new_obj.setContactValue(rset.getString("contactvalue"));
		
		return new_obj;
	}
	public boolean updateContact(ContactType data) throws FactoryException, DataAccessException
	{	
		removeFromCache(data);
		return update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ContactType use_map = (ContactType)map;
		fields.add(QueryFields.getFieldPreferred(use_map.getPreferred()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldLocationType(use_map.getLocationType()));
		fields.add(QueryFields.getFieldContactType(use_map.getContactType()));
		fields.add(QueryFields.getFieldContactValue(use_map.getContactValue()));
	}
	public int deleteContactsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deleteContactsByIds(ids, user.getOrganization());
	}

	public boolean deleteContact(ContactType obj) throws FactoryException
	{
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		return (deleted > 0);
	}
	public int deleteContactsByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			/*
			Factories.getContactInformationFactory().deleteContactInformationByReferenceIds(ids,organization.getId());
			Factories.getContactParticipationFactory().deleteParticipations(ids, organization);

			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deleteContactsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deleteContactsByIds(ids, group.getOrganization());
	}
/*	
	public List<ContactType> getChildContactList(ContactType parent) throws FactoryException,ArgumentException{

		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroup(parent.getGroup().getId()));
		return getContactList(fields.toArray(new QueryField[0]), 0,0,parent.getOrganization());

	}
*/
	public List<ContactType>  getContactList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<ContactType> getContactListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
	
	public List<ContactType> searchContacts(String searchValue, int startRecord, int recordCount, DirectoryGroupType dir) throws FactoryException{
	
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
	
	
	/// Contact search uses a different query to join in contact information
	/// Otherwise, this could be the getPaginatedList method
	///
	/// public List<ContactType> search(QueryField[] filters, OrganizationType organization){
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
