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
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.StatisticsType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;


public class AccountFactory extends NameIdGroupFactory {
	
	
	public AccountFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.hasUrn = true;
		this.tableNames.add("accounts");
		this.factoryType = FactoryEnumType.ACCOUNT;
		
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
		account.setContactInformation(Factories.getContactInformationFactory().getContactInformationForAccount(account));
		if(account.getContactInformation() != null) Factories.getContactInformationFactory().populate(account.getContactInformation());
		account.setStatistics(Factories.getStatisticsFactory().getStatistics(account));
		account.setPopulated(true);
		updateAccountToCache(account);
		return;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getParentId() + "-" + ((NameIdDirectoryGroupType)obj).getGroup().getId();
	}
	public boolean updateAccount(AccountType account) throws FactoryException{
		removeAccountFromCache(account);
		boolean b = update(account);
		/// 2014/09/10
		/// Contact information is updated along with the parent object because it's a foreign-keyed object that is not otherwise easily referenced
		///
		if(account.getContactInformation() != null){
			try {
				b = Factories.getContactInformationFactory().updateContactInformation(account.getContactInformation());
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				b = false;
			}
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
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deleteAccountsByIds(ids, group.getOrganization());
	}
	public int deleteAccountsByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			/*
			Factories.getContactInformationFactory().deleteContactInformationByReferenceIds(ids,organization.getId());
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public boolean deleteAccount(AccountType account) throws FactoryException, ArgumentException
	{
		removeFromCache(account);
		int deleted = deleteById(account.getId(), account.getOrganization().getId());
		if (deleted > 0)
		{
			/// TODO
			/// Delete users for Account
			///
			Factories.getContactInformationFactory().deleteContactInformationByReferenceType(account);

			//Factories.getTagFactory().deleteTagsByAccount(account);
			Factories.getTagParticipationFactory().deleteAccountParticipations(account);
			/// Factories.getRoleFactory().deleteRolesByOwner(account);
			//Factories.getGroupFactory().deleteGroupsByOwner(account);
			Factories.getGroupParticipationFactory().deleteAccountGroupParticipations(account);
			Factories.getRoleParticipationFactory().deleteAccountParticipations(account);
			Factories.getDataParticipationFactory().deleteAccountParticipations(account);
			//Factories.getGroupFactory();

/*
			Factory.DataFactoryInstance.DeleteDataByAccount(account);
			Factory.MessageQueueFactoryInstance.DeleteMessagesForAccount(account);
*/
		}
		return (deleted > 0);
	}

	public AccountType newAccount(UserType owner, String name, AccountType parentAccount, AccountEnumType type, AccountStatusEnumType status){
		AccountType account = newAccount(owner, name, type, status, parentAccount.getGroup());
		account.setParentId(parentAccount.getId());
		return account;
	}
	public AccountType newAccount(UserType owner, String name, AccountEnumType type, AccountStatusEnumType status, DirectoryGroupType group)
	{
		AccountType new_account = new AccountType();
		new_account.setNameType(NameEnumType.ACCOUNT);
		if(owner != null) new_account.setOwnerId(owner.getId());
		new_account.setAccountStatus(status);
		new_account.setAccountType(type);
		new_account.setOrganization(group.getOrganization());
		new_account.setName(name);
		new_account.setGroup(group);
		new_account.setAccountId(UUID.randomUUID().toString());
		return new_account;
	}
	public boolean getAccountNameExists(String name, DirectoryGroupType group) throws FactoryException {
		return (getIdByField(new QueryField[]{QueryFields.getFieldName(name),QueryFields.getFieldGroup(group.getId())}, group.getOrganization().getId()).length > 0);
		//return getAccountNameExists(name, null, organization);
	}
	public boolean getAccountNameExists(String name, AccountType parent) throws FactoryException {
		return (getIdByField(new QueryField[]{QueryFields.getFieldName(name),QueryFields.getFieldParent(parent.getId()),QueryFields.getFieldGroup(parent.getGroup().getId())}, parent.getOrganization().getId()).length > 0);
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
		List<NameIdType> users = getByField(new QueryField[] { QueryFields.getFieldName(name), QueryFields.getFieldParent(( parent != null ? parent.getId() : 0)), QueryFields.getFieldGroup(group.getId())},group.getOrganization().getId());
		if (users.size() > 0)
		{
			out_user = (AccountType)users.get(0);
			addToCache(out_user, key_name);
		}
		return out_user;
	}
	public boolean addAccount(AccountType new_account) throws FactoryException, ArgumentException
	{
		return addAccount(new_account, false);
	}
	public boolean addAccount(AccountType new_account, boolean allot_contact_info) throws FactoryException, ArgumentException
	{
		if (new_account.getOrganization() == null || new_account.getOrganization().getId() <= 0) throw new FactoryException("Cannot add contact information to invalid organization");
		/*
		if (!bulkMode && getAccountNameExists(new_account.getName(), (new_account.getParentId() != 0L ?  (AccountType)getById(new_account.getParentId(), new_account.getOrganization()) : null), new_account.getOrganization()))
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
			row.setCellValue("groupid", new_account.getGroup().getId());
			if (insertRow(row)){

				new_account = (bulkMode ? new_account : getAccountByName(new_account.getName(), new_account.getGroup(), (new_account.getParentId() != 0L ?  (AccountType)getById(new_account.getParentId(),new_account.getOrganization().getId()) : null)));
				if(new_account == null) throw new FactoryException("Failed to retrieve new account object");
				StatisticsType stats = Factories.getStatisticsFactory().newStatistics(new_account);
				if(bulkMode){
					BulkFactories.getBulkFactory().setDirty(FactoryEnumType.STATISTICS);
					BulkFactories.getBulkStatisticsFactory().addStatistics(stats);
					if(allot_contact_info){
						ContactInformationType cinfo = Factories.getContactInformationFactory().newContactInformation(new_account);
						if(new_account.getId() > 0){
							BulkFactories.getBulkFactory().setDirty(FactoryEnumType.CONTACTINFORMATION);
							BulkFactories.getBulkContactInformationFactory().addContactInformation(cinfo);
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
					if(Factories.getStatisticsFactory().addStatistics(stats) == false) throw new FactoryException("Failed to add statistics to new account #" + new_account.getId());
					stats = Factories.getStatisticsFactory().getStatistics(new_account);
					if(stats == null) throw new FactoryException("Failed to retrieve statistics for account #" + new_account.getId());
					new_account.setStatistics(stats);
					if(allot_contact_info){
						ContactInformationType cinfo = Factories.getContactInformationFactory().newContactInformation(new_account);
						System.out.println("Adding cinfo for account in org " + new_account.getOrganization().getId());
						if(Factories.getContactInformationFactory().addContactInformation(cinfo) == false) throw new FactoryException("Failed to assign contact information for account #" + new_account.getId());
						cinfo = Factories.getContactInformationFactory().getContactInformationForAccount(new_account);
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
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
		fields.add(QueryFields.getFieldReferenceId(use_map.getReferenceId()));
	}
	
	/*
	public List<AccountType> getAccountList(QueryField[] fields, OrganizationType organization) throws FactoryException
	{

		List<NameIdType> account_list = getByField(fields, organization.getId());
		return convertList(account_list);
	}
	*/
	
	public List<AccountType> getChildAccountList(AccountType parent) throws FactoryException,ArgumentException{

		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroup(parent.getGroup().getId()));
		return getAccountList(fields.toArray(new QueryField[0]), 0,0,parent.getOrganization());

	}
	public List<AccountType>  getAccountList(QueryField[] fields, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<AccountType> getAccountListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
}
