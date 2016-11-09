/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.StatisticsType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;


public class AccountFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.ACCOUNT, AccountFactory.class); }
	public AccountFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.hasUrn = true;
		this.hasObjectId = true;
		this.tableNames.add("accounts");
		this.factoryType = FactoryEnumType.ACCOUNT;
		systemRoleNameAdministrator = "AccountAdministrators";
 
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("accounts")){
			table.setRestrictUpdateColumn("accountid", true);
		}

	}
	
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		AccountType account = (AccountType)obj;
		if(account.getPopulated() == true || account.getDatabaseRecord() == false) return;
		account.setContactInformation(((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).getContactInformationForAccount(account));
		if(account.getContactInformation() != null) ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).populate(account.getContactInformation());
		account.setStatistics(((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).getStatistics(account));
		account.setPopulated(true);
		updateAccountToCache(account);
		return;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getParentId() + "-" + ((NameIdDirectoryGroupType)obj).getGroupId();
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{	
		AccountType account = (AccountType)object;

		removeAccountFromCache(account);
		boolean b = super.update(account);
		/// 2014/09/10
		/// Contact information is updated along with the parent object because it's a foreign-keyed object that is not otherwise easily referenced
		///
		if(account.getContactInformation() != null){

				b = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).update(account.getContactInformation());

		}
		return b;
	}
	protected void updateAccountToCache(AccountType account) throws ArgumentException{
		updateToCache(account);
	}
	protected void removeAccountFromCache(AccountType account){
		removeFromCache(account);
	}
	public int deleteAccountsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteAccountsByIds(ids, group.getOrganizationId());
	}
	public int deleteAccountsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			/*
			((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).deleteContactInformationByReferenceIds(ids,organizationId);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		AccountType account = (AccountType)object;
		removeFromCache(account);
		int deleted = deleteById(account.getId(), account.getOrganizationId());
		if (deleted > 0)
		{
			/// TODO
			/// Delete users for Account
			///
			((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).deleteContactInformationByReferenceType(account);

			//((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).deleteTagsByAccount(account);
			((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).deleteAccountParticipations(account);
			/// ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).deleteRolesByOwner(account);
			//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteGroupsByOwner(account);
			((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteAccountGroupParticipations(account);
			((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteAccountParticipations(account);
			((DataParticipationFactory)Factories.getFactory(FactoryEnumType.DATAPARTICIPATION)).deleteAccountParticipations(account);
			//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));

/*
			Factory.DataFactoryInstance.DeleteDataByAccount(account);
			Factory.MessageQueueFactoryInstance.DeleteMessagesForAccount(account);
*/
		}
		return (deleted > 0);
	}

	public AccountType newAccount(UserType owner, String name, AccountType parentAccount, AccountEnumType type, AccountStatusEnumType status){
		AccountType account = newAccount(owner, name, type, status, parentAccount.getGroupId());
		account.setParentId(parentAccount.getId());
		return account;
	}
	public AccountType newAccount(UserType owner, String name, AccountEnumType type, AccountStatusEnumType status, long groupId)
	{
		return newAccount(owner,name,type,status,groupId,(owner != null ? owner.getOrganizationId() : 0L));
	}
	/// TODO: Refactor order in which account is created.  At the moment, a null user is allowed primarily to support initial organization setup
	public AccountType newAccount(UserType owner, String name, AccountEnumType type, AccountStatusEnumType status, long groupId, long organizationId)
	{
		if(organizationId <= 0L){
			logger.error("Invalid organization id for new acount '" + name + "' = " + organizationId);
		}
		AccountType new_account = new AccountType();
		new_account.setNameType(NameEnumType.ACCOUNT);
		if(owner != null) new_account.setOwnerId(owner.getId());
		new_account.setAccountStatus(status);
		new_account.setAccountType(type);
		new_account.setOrganizationId(organizationId);
		new_account.setName(name);
		new_account.setGroupId(groupId);
		new_account.setAccountId(UUID.randomUUID().toString());
		return new_account;
	}
	public boolean getAccountNameExists(String name, DirectoryGroupType group) throws FactoryException {
		return (getIdByField(new QueryField[]{QueryFields.getFieldName(name),QueryFields.getFieldGroup(group.getId())}, group.getOrganizationId()).length > 0);
		//return getAccountNameExists(name, null, organization);
	}
	public boolean getAccountNameExists(String name, AccountType parent) throws FactoryException {
		return (getIdByField(new QueryField[]{QueryFields.getFieldName(name),QueryFields.getFieldParent(parent.getId()),QueryFields.getFieldGroup(parent.getGroupId())}, parent.getOrganizationId()).length > 0);
	}

	public AccountType getAccountByName(String name, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		return getAccountByName(name, group, null);
	}
	
	public AccountType getAccountByName(String name, DirectoryGroupType group, AccountType parent) throws FactoryException, ArgumentException
		{

		String key_name = name + "-" + (parent != null ? parent.getId() : 0) + "-" + group.getId();
		AccountType out_user = (AccountType)readCache(key_name);
		if(out_user != null) return out_user;
		List<NameIdType> users = getByField(new QueryField[] { QueryFields.getFieldName(name), QueryFields.getFieldParent(( parent != null ? parent.getId() : 0)), QueryFields.getFieldGroup(group.getId())},group.getOrganizationId());
		if (users.size() > 0)
		{
			out_user = (AccountType)users.get(0);
			addToCache(out_user, key_name);
		}
		return out_user;
	}
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		return add(object, false);
	}
	public <T> boolean add(T object, boolean allot_contact_info) throws FactoryException, ArgumentException
	{
		AccountType new_account = (AccountType)object;
		if (new_account.getOrganizationId() == null || new_account.getOrganizationId() <= 0) throw new FactoryException("Cannot add contact information to invalid organization");
		/*
		if (!bulkMode && getAccountNameExists(new_account.getName(), (new_account.getParentId() != 0L ?  (AccountType)getById(new_account.getParentId(), new_account.getOrganizationId()) : null), new_account.getOrganizationId()))
		{
			throw new FactoryException("Account name " + new_account.getName() + " already exists");
		}
		*/
		DataRow row = prepareAdd(new_account, "accounts");
		try{
			row.setCellValue("accountstatus", new_account.getAccountStatus().toString());
			row.setCellValue("accounttype", new_account.getAccountType().toString());
			row.setCellValue("accountid", new_account.getAccountId());
			row.setCellValue("referenceid", new_account.getReferenceId());
			row.setCellValue("groupid", new_account.getGroupId());
			if (insertRow(row)){

				new_account = (bulkMode ? new_account : getAccountByName(new_account.getName(), (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(new_account.getGroupId(),new_account.getOrganizationId()), (new_account.getParentId() != 0L ?  (AccountType)getById(new_account.getParentId(),new_account.getOrganizationId()) : null)));
				if(new_account == null) throw new FactoryException("Failed to retrieve new account object");
				StatisticsType stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).newStatistics(new_account);
				if(bulkMode){
					BulkFactories.getBulkFactory().setDirty(FactoryEnumType.STATISTICS);
					((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.STATISTICS)).add(stats);
					if(allot_contact_info){
						ContactInformationType cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(new_account);
						if(new_account.getId() > 0){
							BulkFactories.getBulkFactory().setDirty(FactoryEnumType.CONTACTINFORMATION);
							((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.CONTACTINFORMATION)).add(cinfo);
						}
						else{
							if(this.factoryType == FactoryEnumType.UNKNOWN) throw new FactoryException("Invalid Factory Type for Bulk Identifiers");
							String sessionId = BulkFactories.getBulkFactory().getSessionForBulkId(new_account.getId());
							if(sessionId == null){
								logger.error("Invalid bulk session id");
								throw new FactoryException("Invalid bulk session id");
							}
							logger.debug("Bulk id discovered.  User=" + new_account.getId() + ". Diverting to Bulk " + FactoryEnumType.CONTACTINFORMATION + " Operation");
							BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cinfo);

						}
					}
				}
				else{
					if(((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).add(stats) == false) throw new FactoryException("Failed to add statistics to new account #" + new_account.getId());
					stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).getStatistics(new_account);
					if(stats == null) throw new FactoryException("Failed to retrieve statistics for account #" + new_account.getId());
					new_account.setStatistics(stats);
					if(allot_contact_info){
						ContactInformationType cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(new_account);
						System.out.println("Adding cinfo for account in org " + new_account.getOrganizationId());
						if(((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).add(cinfo) == false) throw new FactoryException("Failed to assign contact information for account #" + new_account.getId());
						cinfo = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).getContactInformationForAccount(new_account);
						if(cinfo == null) throw new FactoryException("Failed to retrieve contact information for account #" + new_account.getId());
						new_account.setContactInformation(cinfo);
					}
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
		AccountType new_account = new AccountType();
		new_account.setNameType(NameEnumType.ACCOUNT);
		super.read(rset, new_account);
		readGroup(rset, new_account);
		new_account.setDatabaseRecord(true);
		new_account.setAccountId(rset.getString("accountid"));
		new_account.setReferenceId(rset.getLong("referenceid"));
		new_account.setAccountStatus(AccountStatusEnumType.valueOf(rset.getString("accountstatus")));
		new_account.setAccountType(AccountEnumType.valueOf(rset.getString("accounttype")));
		return new_account;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		AccountType use_map = (AccountType)map;
		fields.add(QueryFields.getFieldAccountId(use_map.getAccountId()));
		fields.add(QueryFields.getFieldAccountStatus(use_map.getAccountStatus()));
		fields.add(QueryFields.getFieldAccountType(use_map.getAccountType()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroupId()));
		fields.add(QueryFields.getFieldReferenceId(use_map.getReferenceId()));
	}
	
	/*
	public List<AccountType> getAccountList(QueryField[] fields, long organizationId) throws FactoryException
	{

		List<NameIdType> account_list = getByField(fields, organizationId);
		return convertList(account_list);
	}
	*/
	
	public List<AccountType> getChildAccountList(AccountType parent) throws FactoryException,ArgumentException{

		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroup(parent.getGroupId()));
		return getAccountList(fields.toArray(new QueryField[0]), 0,0,parent.getOrganizationId());

	}
	public List<AccountType>  getAccountList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<AccountType> getAccountListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
