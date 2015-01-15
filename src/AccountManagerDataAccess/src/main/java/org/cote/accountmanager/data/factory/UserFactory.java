package org.cote.accountmanager.data.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.StatisticsType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;



public class UserFactory extends NameIdFactory {
	
	public UserFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = false;
		this.hasName = true;
		this.hasUrn = true;
		this.tableNames.add("users");
		this.factoryType = FactoryEnumType.USER;
	}
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		UserType user = (UserType)obj;
		if(user.getPopulated() == true || user.getDatabaseRecord() == false) return;
		user.setContactInformation(Factories.getContactInformationFactory().getContactInformationForUser(user));
		user.setHomeDirectory(Factories.getGroupFactory().getUserDirectory(user));
		user.setStatistics(Factories.getStatisticsFactory().getStatistics(user));
		user.setPopulated(true);
		updateToCache(user);
		return;
	}
	public boolean updateUser(UserType user) throws FactoryException{
		removeFromCache(user);
		boolean b =  update(user);
		
		/// 2014/09/10
		/// Contact information is updated along with the parent object because it's a foreign-keyed object that is not otherwise easily referenced
		///
		if(user.getContactInformation() != null){
			try {
				b = Factories.getContactInformationFactory().updateContactInformation(user.getContactInformation());
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				b = false;
			}
		}
		return b;
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		UserType t = (UserType)obj;
		return t.getName() + "-" + t.getAccountId() + "-" + t.getOrganization().getId();
	}

	public void updateUserToCache(UserType user) throws ArgumentException{
		updateToCache(user);
	}
	public void removeUserFromCache(UserType user){
		removeFromCache(user);
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("userid")){
			table.setRestrictUpdateColumn("userid", true);
		}
		if(table.getName().equalsIgnoreCase("password")){
			table.setRestrictSelectColumn("password", true);
		}
	}
	public boolean deleteUser(UserType user) throws FactoryException, ArgumentException
	{
		removeFromCache(user);
		int deleted = deleteById(user.getId(), user.getOrganization().getId());
		if (deleted > 0)
		{
			/// TODO
			/// Delete users for Account
			///
			Factories.getContactInformationFactory().deleteContactInformationByReferenceType(user);

			//Factories.getTagFactory().deleteTagsByAccount(account);
			Factories.getTagParticipationFactory().deleteUserParticipations(user);
			Factories.getRoleFactory().deleteRolesByUser(user);
			Factories.getDataFactory().deleteDataByUser(user);
			Factories.getGroupFactory().deleteGroupsByUser(user);
			Factories.getGroupParticipationFactory().deleteUserGroupParticipations(user);
			Factories.getRoleParticipationFactory().deleteUserParticipations(user);
			Factories.getDataParticipationFactory().deleteUserParticipations(user);
			//Factories.getGroupFactory();

/*
			Factory.DataFactoryInstance.DeleteDataByAccount(account);
			Factory.MessageQueueFactoryInstance.DeleteMessagesForAccount(account);
*/
		}
		return (deleted > 0);
	}
	public UserType newUserForAccount(String name, String hashed_password, AccountType account) throws FactoryException{
		UserType user = newUser(name, hashed_password, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, account.getOrganization());
		user.setAccountId(account.getId());
		return user;
	}
	public UserType newUserForAccount(String name, String hashed_password, AccountType account, UserEnumType type, UserStatusEnumType status) throws FactoryException{
		UserType user = newUser(name, hashed_password, type, status, account.getOrganization());
		user.setAccountId(account.getId());
		return user;
	}
	public UserType newUser(String name, String hashed_password, UserEnumType type, UserStatusEnumType status, OrganizationType organization)
	{
		UserType new_user = new UserType();
		new_user.setNameType(NameEnumType.USER);
		new_user.setUserStatus(status);
		new_user.setUserType(type);
		new_user.setOrganization(organization);
		new_user.setName(name);
		new_user.setPassword(hashed_password);
		new_user.setUserId(UUID.randomUUID().toString());
		return new_user;
	}
	
	public UserType getUserBySession(UserSessionType session) throws FactoryException, ArgumentException{
		if(!Factories.getSessionFactory().isValid(session)) return null;
		if(session.getOrganizationId() == null || session.getOrganizationId() <= 0) throw new FactoryException("Invalid organization id");
		if(session.getUserId() == null || session.getUserId() <= 0) throw new FactoryException("Invalid user id");
		UserType out_type = readCache(session.getUserId());
		if(out_type != null){
			System.out.println("*** getUserBySession: " + session.getUserId());
			return out_type;
		}
		List<NameIdType> types =  getById(session.getUserId(), session.getOrganizationId());
		if(types.size() > 0) return (UserType)types.get(0);
		return null;
	}
	
	public boolean getUserNameExists(String user_name, OrganizationType organization) throws FactoryException {
		return (getIdByName(user_name, organization.getId()) > 0);
	}
	public UserType getUserByName(String name, OrganizationType organization) throws FactoryException, ArgumentException
	{
		String key_name = name + "-0-" + organization.getId();
		UserType out_user = (UserType)readCache(key_name);
		if(out_user != null)
			return out_user;
		
		List<NameIdType> users = getByName(name, organization.getId());
		if (users.size() > 0)
		{
			out_user = (UserType)users.get(0);
			addToCache(out_user, key_name);
		}
		return out_user;
	}
	public boolean addUser(UserType new_user) throws FactoryException, ArgumentException
	{
		return addUser(new_user, false);
	}
	public boolean addUser(UserType new_user, boolean allot_contact_info) throws FactoryException, ArgumentException
	{
		if (new_user.getOrganization() == null || new_user.getOrganization().getId() <= 0) throw new FactoryException("Cannot add user to invalid organization");
		if(new_user.getPassword() == null || new_user.getPassword().length() < 42) throw new FactoryException("Password hash is not strong enough.  Must be at least 42 characters");;
		if (!bulkMode && getUserNameExists(new_user.getName(), new_user.getOrganization()))
		{
			throw new FactoryException("User name " + new_user.getName() + " already exists");
		}
		DataRow row = prepareAdd(new_user, "users");
		try{
			row.setCellValue("userstatus", new_user.getUserStatus().toString());
			row.setCellValue("usertype", new_user.getUserType().toString());
			row.setCellValue("password", new_user.getPassword());
			row.setCellValue("accountid", new_user.getAccountId());
			row.setCellValue("userid",new_user.getUserId());
			if (insertRow(row)){
				new_user = (bulkMode ? new_user : getUserByName(new_user.getName(), new_user.getOrganization()));
				if(new_user == null) throw new FactoryException("Failed to retrieve new user object");
				StatisticsType stats = Factories.getStatisticsFactory().newStatistics(new_user);
				DirectoryGroupType home_dir = Factories.getGroupFactory().newDirectoryGroup(new_user, new_user.getName(), Factories.getGroupFactory().getHomeDirectory(new_user.getOrganization()), new_user.getOrganization());
				if(bulkMode){
					
					BulkFactories.getBulkFactory().setDirty(FactoryEnumType.STATISTICS);
					BulkFactories.getBulkStatisticsFactory().addStatistics(stats);
					BulkFactories.getBulkFactory().setDirty(FactoryEnumType.GROUP);
					BulkFactories.getBulkGroupFactory().addGroup(home_dir);
					if(allot_contact_info){
						ContactInformationType cinfo = Factories.getContactInformationFactory().newContactInformation(new_user);
						if(new_user.getId() > 0){
							BulkFactories.getBulkFactory().setDirty(FactoryEnumType.CONTACTINFORMATION);
							BulkFactories.getBulkContactInformationFactory().addContactInformation(cinfo);
						}
						else{
							if(this.factoryType == FactoryEnumType.UNKNOWN) throw new FactoryException("Invalid Factory Type for Bulk Identifiers");
							String sessionId = BulkFactories.getBulkFactory().getSessionForBulkId(new_user.getId());
							if(sessionId == null){
								logger.error("Invalid bulk session id");
								throw new FactoryException("Invalid bulk session id");
							}
							logger.debug("Bulk id discovered.  User=" + new_user.getId() + ". Diverting to Bulk " + FactoryEnumType.CONTACTINFORMATION + " Operation");
							BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cinfo);

						}
					}
				}
				else{
					if(Factories.getStatisticsFactory().addStatistics(stats) == false) throw new FactoryException("Failed to add statistics to new user #" + new_user.getId());
					stats = Factories.getStatisticsFactory().getStatistics(new_user);
					if(stats == null) throw new FactoryException("Failed to retrieve statistics for user #" + new_user.getId());
					if(Factories.getGroupFactory().addGroup(home_dir) == false) throw new FactoryException("Failed to add home directory for #" + new_user.getId());
					if(allot_contact_info){
						ContactInformationType cinfo = Factories.getContactInformationFactory().newContactInformation(new_user);
						logger.debug("Adding cinfo for user in org " + new_user.getOrganization().getId());
						if(Factories.getContactInformationFactory().addContactInformation(cinfo) == false) throw new FactoryException("Failed to assign contact information for user #" + new_user.getId());
						cinfo = Factories.getContactInformationFactory().getContactInformationForUser(new_user);
						if(cinfo == null) throw new FactoryException("Failed to retrieve contact information for user #" + new_user.getId());
						new_user.setContactInformation(cinfo);
					}
					new_user.setStatistics(stats);
				}
				return true;
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		UserType new_user = new UserType();
		new_user.setDatabaseRecord(true);
		new_user.setNameType(NameEnumType.USER);
		new_user.setAccountId(rset.getLong("accountid"));
		new_user.setUserId(rset.getString("userid"));
		new_user.setUserStatus(UserStatusEnumType.valueOf(rset.getString("userstatus")));
		new_user.setUserType(UserEnumType.valueOf(rset.getString("usertype")));
		return super.read(rset, new_user);
	}
	public List<UserType>  getUserList(long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getUserList(new QueryField[] {  }, startRecord, recordCount, organization);
	}
	public List<UserType>  getUserList(ProcessingInstructionType instruction, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getUserList(new QueryField[] {  }, instruction, startRecord, recordCount,organization);
	}
	public List<UserType>  getUserList(QueryField[] fields, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getUserList(fields, instruction, startRecord,recordCount,organization);
	}
	public List<UserType>  getUserList(QueryField[] fields, ProcessingInstructionType instruction,long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getUserList(fields, instruction, organization);
	}
	public List<UserType> getUserList(QueryField[] fields, ProcessingInstructionType instruction, OrganizationType organization) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();

		List<NameIdType> UserList = getByField(fields, instruction, organization.getId());
		return convertList(UserList);

	}
	
	public List<UserType> getUserListByIds(int[] User_ids, OrganizationType organization) throws FactoryException, ArgumentException
	{
		StringBuffer buff = new StringBuffer();
		int deleted = 0;
		List<UserType> out_list = new ArrayList<UserType>();
		for (int i = 0; i < User_ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(User_ids[i]);
			if ((i > 0 || User_ids.length == 1) && ((i % 250 == 0) || i == User_ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
				match.setComparator(ComparatorEnumType.IN);
				List<UserType> tmp_User_list = getUserList(new QueryField[] { match }, null, organization);
				out_list.addAll(tmp_User_list);
				buff.delete(0,  buff.length());
			}
		}
		return out_list;
	}

	public List<UserType> searchUsers(String searchValue, long startRecord, int recordCount, OrganizationType org) throws FactoryException{
		
		ProcessingInstructionType instruction = null;
		if(startRecord >= 0 && recordCount >= 0){
			instruction = new ProcessingInstructionType();
			instruction.setOrderClause("name ASC");
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		
		List<QueryField> fields = buildSearchQuery(searchValue, org);
		return search(fields.toArray(new QueryField[0]), instruction,org);
	}
	
	
	/// User search uses a different query to join in contact information
	/// Otherwise, this could be the getPaginatedList method
	///
	/// public List<UserType> search(QueryField[] filters, OrganizationType organization){
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
		return searchByIdInView("userContact", filters,instruction,organization);
	}
		

}
