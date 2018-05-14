/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
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


// 2015/06/23 - Refactored to strip passwords off the user object
//
public class UserFactory extends NameIdFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.USER, UserFactory.class); }
	public UserFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = false;
		this.hasName = true;
		this.hasObjectId = true;
		this.hasUrn = true;
		this.tableNames.add("users");
		this.factoryType = FactoryEnumType.USER;
		
		systemRoleNameReader = "AccountUsersReaders";
		systemRoleNameAdministrator = "AccountAdministrators";
	}
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		UserType user = (UserType)obj;
		if(user.getPopulated() == true || user.getDatabaseRecord() == false) return;
		user.setContactInformation(((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).getContactInformationForUser(user));
		user.setHomeDirectory(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserDirectory(user));
		user.setStatistics(((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).getStatistics(user));
		user.setPopulated(true);
		updateToCache(user);
		return;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException{
		UserType user = (UserType)object;
		removeFromCache(user);
		/// Refactor into external credential capability 
		///
		//if(user.getPassword() != null && user.getPassword().length() < 42) throw new FactoryException("Password hash is not strong enough.  Must be at least 42 characters");
		boolean b =  super.update(user);
		
		/// 2014/09/10
		/// Contact information is updated along with the parent object because it's a foreign-keyed object that is not otherwise easily referenced
		///
		if(user.getContactInformation() != null){
			b = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).update(user.getContactInformation());
		}
		return b;
	}
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		UserType user = (UserType)map;
		/*
		if(user.getPassword() != null){
			fields.add(QueryFields.getFieldPassword(user.getPassword()));
			user.setPassword(null);
		}
		*/

	}
	@Override
	public <T> String getCacheKeyName(T obj){
		UserType t = (UserType)obj;
		return t.getName() + "-" + t.getAccountId() + "-" + t.getOrganizationId();
	}

	/*
	public void updateUserToCache(UserType user) throws ArgumentException{
		updateToCache(user);
	}
	public void removeUserFromCache(UserType user){
		removeFromCache(user);
	}
	*/
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("userid")){
			table.setRestrictUpdateColumn("userid", true);
		}
		/*
		if(table.getName().equalsIgnoreCase("password")){
			table.setRestrictSelectColumn("password", true);
		}
		*/
	}
	
	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		UserType user = (UserType)object;
		removeFromCache(user);
		int deleted = deleteById(user.getId(), user.getOrganizationId());
		if (deleted > 0)
		{
			/// TODO
			/// Delete users for Account
			///
			((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).deleteContactInformationByReferenceType(user);
			((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).deleteUserParticipations(user);
			((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).deleteRolesByUser(user);
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).deleteDataByUser(user);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteGroupsByUser(user);
			((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteUserGroupParticipations(user);
			((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteUserParticipations(user);
			((DataParticipationFactory)Factories.getFactory(FactoryEnumType.DATAPARTICIPATION)).deleteUserParticipations(user);
		}
		return (deleted > 0);
	}
	public UserType newUserForAccount(String name, AccountType account) throws FactoryException{
		UserType user = newUser(name, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, account.getOrganizationId());
		user.setAccountId(account.getId());
		return user;
	}
	public UserType newUserForAccount(String name, AccountType account, UserEnumType type, UserStatusEnumType status) throws FactoryException{
		UserType user = newUser(name, type, status, account.getOrganizationId());
		user.setAccountId(account.getId());
		return user;
	}
	public UserType newUser(String name, UserEnumType type, UserStatusEnumType status, long organizationId)
	{
		UserType new_user = new UserType();
		new_user.setNameType(NameEnumType.USER);
		new_user.setUserStatus(status);
		new_user.setUserType(type);
		//new_user.setOrganization(organization);
		new_user.setOrganizationId(organizationId);
		new_user.setName(name);
		new_user.setUserId(UUID.randomUUID().toString());
		return new_user;
	}
	
	public UserType getUserBySession(UserSessionType session) throws FactoryException, ArgumentException{
		if(!Factories.getSessionFactory().isValid(session)) return null;
		if(session.getOrganizationId() == null || session.getOrganizationId() <= 0) throw new FactoryException("Invalid organization id");
		if(session.getUserId() == null || session.getUserId() <= 0) throw new FactoryException("Invalid user id");
		UserType out_type = readCache(session.getUserId());
		if(out_type != null){
			logger.debug("Session " + session.getSessionId() + " --> user #" + session.getUserId());
			return out_type;
		}
		return getById(session.getUserId(), session.getOrganizationId());
	}
	
	public boolean getUserNameExists(String user_name, long organizationId) throws FactoryException {
		return (getIdByName(user_name, organizationId) > 0);
	}

	@Override
	public <T> boolean add(T object) throws FactoryException, ArgumentException
	{
		return add(object, false);
	}
	public <T> boolean add(T object, boolean allot_contact_info) throws FactoryException, ArgumentException
	{
		UserType new_user = (UserType)object;
		if (new_user.getOrganizationId() == null || new_user.getOrganizationId() <= 0) throw new FactoryException("Cannot add user to invalid organization");
		if (!bulkMode && getUserNameExists(new_user.getName(), new_user.getOrganizationId()))
		{
			throw new FactoryException("User name " + new_user.getName() + " already exists");
		}
		DataRow row = prepareAdd(new_user, "users");
		try{
			row.setCellValue("userstatus", new_user.getUserStatus().toString());
			row.setCellValue("usertype", new_user.getUserType().toString());
			row.setCellValue("accountid", new_user.getAccountId());
			row.setCellValue("userid",new_user.getUserId());
			if (insertRow(row)){
				new_user = (bulkMode ? new_user : getByName(new_user.getName(), new_user.getOrganizationId()));
				if(new_user == null) throw new FactoryException("Failed to retrieve new user object");
				StatisticsType stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).newStatistics(new_user);
				DirectoryGroupType home_dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(new_user, new_user.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getHomeDirectory(new_user.getOrganizationId()), new_user.getOrganizationId());
				String sessionId = null;
				if(bulkMode){
					BulkFactories.getBulkFactory().setDirty(FactoryEnumType.STATISTICS);
					((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.STATISTICS)).add(stats);
					BulkFactories.getBulkFactory().setDirty(FactoryEnumType.GROUP);
					BulkFactories.getBulkFactory().createBulkEntry(null,FactoryEnumType.GROUP,home_dir);
					if(allot_contact_info){
						ContactInformationType cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(new_user);
						if(new_user.getId() > 0L){
							sessionId = BulkFactories.getBulkFactory().getGlobalSessionId();
							BulkFactories.getBulkFactory().setDirty(FactoryEnumType.CONTACTINFORMATION);
							((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.CONTACTINFORMATION)).add(cinfo);
						}
						else{
							if(this.factoryType == FactoryEnumType.UNKNOWN) throw new FactoryException("Invalid Factory Type for Bulk Identifiers");
							sessionId = BulkFactories.getBulkFactory().getSessionForBulkId(new_user.getId());
							if(sessionId == null){
								logger.error("Invalid bulk session id");
								throw new FactoryException("Invalid bulk session id");
							}
							((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.GROUP)).add(home_dir);
							logger.debug("Bulk id discovered.  User=" + new_user.getId() + ". Diverting to Bulk " + FactoryEnumType.CONTACTINFORMATION + " Operation");
							BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cinfo);
						}
					}
				}
				else{
					if(((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).add(stats) == false) throw new FactoryException("Failed to add statistics to new user #" + new_user.getId());
					stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).getStatistics(new_user);
					if(stats == null) throw new FactoryException("Failed to retrieve statistics for user #" + new_user.getId());
					if(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).add(home_dir) == false) throw new FactoryException("Failed to add home directory for #" + new_user.getId());
					if(allot_contact_info){
						ContactInformationType cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(new_user);
						logger.debug("Adding cinfo for user in org " + new_user.getOrganizationId());
						if(((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).add(cinfo) == false) throw new FactoryException("Failed to assign contact information for user #" + new_user.getId());
						cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).getContactInformationForUser(new_user);
						if(cinfo == null) throw new FactoryException("Failed to retrieve contact information for user #" + new_user.getId());
						new_user.setContactInformation(cinfo);
					}
					new_user.setStatistics(stats);
				}
				/// re-read home dir to get the right id / bulk id
				///
				home_dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(new_user.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getHomeDirectory(new_user.getOrganizationId()), new_user.getOrganizationId());
				if(home_dir == null) throw new FactoryException("Missing home directory");
				((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).addDefaultUserGroups(new_user, home_dir, bulkMode,sessionId);
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
	
	public List<UserType>  getUserList(long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getUserList(new QueryField[] {  }, startRecord, recordCount, organizationId);
	}
	public List<UserType>  getUserList(ProcessingInstructionType instruction, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getUserList(new QueryField[] {  }, instruction, startRecord, recordCount,organizationId);
	}
	public List<UserType>  getUserList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getUserList(fields, instruction, startRecord,recordCount,organizationId);
	}
	public List<UserType>  getUserList(QueryField[] fields, ProcessingInstructionType instruction,long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getUserList(fields, instruction, organizationId);
	}
	public List<UserType> getUserList(QueryField[] fields, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();

		List<NameIdType> UserList = getByField(fields, instruction, organizationId);
		return convertList(UserList);

	}
	
	/*
	public List<UserType> getUserListByIds(int[] User_ids, long organizationId) throws FactoryException, ArgumentException
	{
		StringBuilder buff = new StringBuilder();
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
				List<UserType> tmp_User_list = getUserList(new QueryField[] { match }, null, organizationId);
				out_list.addAll(tmp_User_list);
				buff.delete(0,  buff.length());
			}
		}
		return out_list;
	}
	*/

	/// User search uses a different query to join in contact information
	/// Otherwise, this could be the paginateList method
	///
	/// public List<UserType> search(QueryField[] filters, long organizationId){
	@Override
	public List<QueryField> buildSearchQuery(String searchValue, long organizationId) throws FactoryException{
		
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
	public <T> List<T> search(QueryField[] filters, ProcessingInstructionType instruction, long organizationId){
		return searchByIdInView("userContact", filters,instruction,organizationId);
	}
		

}
