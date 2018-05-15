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
import org.cote.accountmanager.exceptions.FactoryException;
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
		this.primaryTableName = "users";
		this.tableNames.add(this.primaryTableName);
		this.factoryType = FactoryEnumType.USER;
		
		systemRoleNameReader = "AccountUsersReaders";
		systemRoleNameAdministrator = "AccountAdministrators";
	}
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		UserType user = (UserType)obj;
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
	public <T> String getCacheKeyName(T obj){
		UserType t = (UserType)obj;
		return t.getName() + "-" + t.getAccountId() + "-" + t.getOrganizationId();
	}


	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(this.primaryTableName)){
			table.setRestrictUpdateColumn("userid", true);
		}
	}
	
	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		UserType user = (UserType)object;
		removeFromCache(user);
		int deleted = deleteById(user.getId(), user.getOrganizationId());
		if (deleted > 0)
		{
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
		UserType newUser = new UserType();
		newUser.setNameType(NameEnumType.USER);
		newUser.setUserStatus(status);
		newUser.setUserType(type);
		newUser.setOrganizationId(organizationId);
		newUser.setName(name);
		newUser.setUserId(UUID.randomUUID().toString());
		return newUser;
	}
	
	public UserType getUserBySession(UserSessionType session) throws FactoryException, ArgumentException{
		if(!Factories.getSessionFactory().isValid(session)) return null;
		if(session.getOrganizationId() == null || session.getOrganizationId() <= 0) throw new FactoryException("Invalid organization id");
		if(session.getUserId() == null || session.getUserId() <= 0) throw new FactoryException("Invalid user id");
		UserType outType = readCache(session.getUserId());
		if(outType != null){
			logger.debug("Session " + session.getSessionId() + " --> user #" + session.getUserId());
			return outType;
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
	public <T> boolean add(T object, boolean allotContactInfo) throws FactoryException, ArgumentException
	{
		UserType newUser = (UserType)object;
		if (newUser.getOrganizationId() == null || newUser.getOrganizationId() <= 0) throw new FactoryException("Cannot add user to invalid organization");
		if (!bulkMode && getUserNameExists(newUser.getName(), newUser.getOrganizationId()))
		{
			throw new FactoryException("User name " + newUser.getName() + " already exists");
		}
		DataRow row = prepareAdd(newUser, "users");
		try{
			row.setCellValue("userstatus", newUser.getUserStatus().toString());
			row.setCellValue("usertype", newUser.getUserType().toString());
			row.setCellValue("accountid", newUser.getAccountId());
			row.setCellValue("userid",newUser.getUserId());
			if (insertRow(row)){
				newUser = (bulkMode ? newUser : getByName(newUser.getName(), newUser.getOrganizationId()));
				if(newUser == null) throw new FactoryException("Failed to retrieve new user object");
				StatisticsType stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).newStatistics(newUser);
				DirectoryGroupType homeDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(newUser, newUser.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getHomeDirectory(newUser.getOrganizationId()), newUser.getOrganizationId());
				String sessionId = null;
				if(bulkMode){
					BulkFactories.getBulkFactory().setDirty(FactoryEnumType.STATISTICS);
					((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.STATISTICS)).add(stats);
					BulkFactories.getBulkFactory().setDirty(FactoryEnumType.GROUP);
					BulkFactories.getBulkFactory().createBulkEntry(null,FactoryEnumType.GROUP,homeDir);
					if(allotContactInfo){
						ContactInformationType cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(newUser);
						if(newUser.getId() > 0L){
							sessionId = BulkFactories.getBulkFactory().getGlobalSessionId();
							BulkFactories.getBulkFactory().setDirty(FactoryEnumType.CONTACTINFORMATION);
							((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.CONTACTINFORMATION)).add(cinfo);
						}
						else{
							if(this.factoryType == FactoryEnumType.UNKNOWN) throw new FactoryException("Invalid Factory Type for Bulk Identifiers");
							sessionId = BulkFactories.getBulkFactory().getSessionForBulkId(newUser.getId());
							if(sessionId == null){
								logger.error("Invalid bulk session id");
								throw new FactoryException("Invalid bulk session id");
							}
							((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.GROUP)).add(homeDir);
							logger.debug("Bulk id discovered.  User=" + newUser.getId() + ". Diverting to Bulk " + FactoryEnumType.CONTACTINFORMATION + " Operation");
							BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cinfo);
						}
					}
				}
				else{
					if(((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).add(stats) == false) throw new FactoryException("Failed to add statistics to new user #" + newUser.getId());
					stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).getStatistics(newUser);
					if(stats == null) throw new FactoryException("Failed to retrieve statistics for user #" + newUser.getId());
					if(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).add(homeDir) == false) throw new FactoryException("Failed to add home directory for #" + newUser.getId());
					if(allotContactInfo){
						ContactInformationType cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(newUser);
						logger.debug("Adding cinfo for user in org " + newUser.getOrganizationId());
						if(((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).add(cinfo) == false) throw new FactoryException("Failed to assign contact information for user #" + newUser.getId());
						cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).getContactInformationForUser(newUser);
						if(cinfo == null) throw new FactoryException("Failed to retrieve contact information for user #" + newUser.getId());
						newUser.setContactInformation(cinfo);
					}
					newUser.setStatistics(stats);
				}
				/// re-read home dir to get the right id / bulk id
				///
				homeDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(newUser.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getHomeDirectory(newUser.getOrganizationId()), newUser.getOrganizationId());
				if(homeDir == null) throw new FactoryException("Missing home directory");
				((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).addDefaultUserGroups(newUser, homeDir, bulkMode,sessionId);
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
		UserType newUser = new UserType();
		newUser.setDatabaseRecord(true);
		newUser.setNameType(NameEnumType.USER);
		newUser.setAccountId(rset.getLong("accountid"));
		newUser.setUserId(rset.getString("userid"));
		newUser.setUserStatus(UserStatusEnumType.valueOf(rset.getString("userstatus")));
		newUser.setUserType(UserEnumType.valueOf(rset.getString("usertype")));
		return super.read(rset, newUser);
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

		List<NameIdType> userList = getByField(fields, instruction, organizationId);
		return convertList(userList);

	}
	
	/// User search uses a different query to join in contact information
	/// Otherwise, this could be the paginateList method
	///
	@Override
	public List<QueryField> buildSearchQuery(String inSearchValue, long organizationId) throws FactoryException{
		
		String searchValue = inSearchValue.replaceAll("\\*","%");
		
		List<QueryField> filters = new ArrayList<>();
		QueryField searchFilters = new QueryField(SqlDataEnumType.NULL,"searchgroup",null);
		searchFilters.setComparator(ComparatorEnumType.GROUP_OR);
		QueryField nameFilter = new QueryField(SqlDataEnumType.VARCHAR,"name",searchValue);
		nameFilter.setComparator(ComparatorEnumType.LIKE);
		searchFilters.getFields().add(nameFilter);
		QueryField firstNameFilter = new QueryField(SqlDataEnumType.VARCHAR,"firstname",searchValue);
		firstNameFilter.setComparator(ComparatorEnumType.LIKE);
		searchFilters.getFields().add(firstNameFilter);
		filters.add(searchFilters);
		return filters;
	}
	
	@Override
	public <T> List<T> search(QueryField[] filters, ProcessingInstructionType instruction, long organizationId){
		return searchByIdInView("userContact", filters,instruction,organizationId);
	}
		

}
