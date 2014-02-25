package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
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
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.StatisticsType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;

public class AccountFactory extends NameIdFactory {
	
	
	public AccountFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = false;
		this.hasName = true;
		this.tableNames.add("accounts");
		this.factoryType = FactoryEnumType.ACCOUNT;
		
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("accountid")){
			table.setRestrictUpdateColumn("accountid", true);
		}

	}
	@Override
	public <T> String getCacheKeyName(T obj){
		AccountType t = (AccountType)obj;
		return t.getName() + "-" + t.getParentId() + "-" + t.getOrganization().getId();
	}
	public boolean updateAccount(AccountType account) throws FactoryException{
		removeAccountFromCache(account);
		return update(account);
	}
	protected void updateAccountToCache(AccountType account) throws ArgumentException{
		String key_name = getCacheKeyName(account);
		System.out.println("Update account to cache: " + key_name);
		if(this.haveCacheId(account.getId())) removeAccountFromCache(account);
		addToCache(account, key_name);
	}
	protected void removeAccountFromCache(AccountType account){
		String key_name = getCacheKeyName(account);
		System.out.println("Remove account from cache: " + key_name);
		removeFromCache(account, key_name);
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
	public AccountType newAccount(String name, AccountEnumType type, AccountStatusEnumType status, OrganizationType organization)
	{
		return newAccount(name, null, type, status, organization);
	}
	public AccountType newAccount(String name, AccountType parentAccount, AccountEnumType type, AccountStatusEnumType status, OrganizationType organization)
	{
		AccountType new_account = new AccountType();
		new_account.setNameType(NameEnumType.ACCOUNT);
		if(parentAccount != null) new_account.setParentId(parentAccount.getId());
		new_account.setAccountStatus(status);
		new_account.setAccountType(type);
		new_account.setOrganization(organization);
		new_account.setName(name);
		new_account.setAccountId(UUID.randomUUID().toString());
		return new_account;
	}
	public boolean getAccountNameExists(String name, OrganizationType organization) throws FactoryException {
		return getAccountNameExists(name, null, organization);
	}
	public boolean getAccountNameExists(String name, AccountType parent, OrganizationType organization) throws FactoryException {
		return (getIdByField(new QueryField[]{QueryFields.getFieldName(name),QueryFields.getFieldParent((parent != null ? parent.getId() : 0))}, organization.getId()).length > 0);
	}

	public AccountType getAccountByName(String name, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getAccountByName(name, null, organization);
	}
	
	public AccountType getAccountByName(String name, AccountType parent, OrganizationType organization) throws FactoryException, ArgumentException
		{

		String key_name = name + "-" + (parent != null ? parent.getId() : 0) + "-" + organization.getId();
		AccountType out_user = (AccountType)readCache(key_name);
		if(out_user != null) return out_user;
		List<NameIdType> users = getByField(new QueryField[] { QueryFields.getFieldName(name), QueryFields.getFieldParent(( parent != null ? parent.getId() : 0)) }, organization.getId());
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
		if (!bulkMode && getAccountNameExists(new_account.getName(), (new_account.getParentId() != 0L ?  (AccountType)getById(new_account.getParentId(), new_account.getOrganization()) : null), new_account.getOrganization()))
		{
			throw new FactoryException("Account name " + new_account.getName() + " already exists");
		}
		DataRow row = prepareAdd(new_account, "accounts");
		try{
			row.setCellValue("accountstatus", new_account.getAccountStatus().toString());
			row.setCellValue("accounttype", new_account.getAccountType().toString());
			row.setCellValue("accountid", new_account.getAccountId());
			row.setCellValue("referenceid", new_account.getReferenceId());
			if (insertRow(row)){

				new_account = (bulkMode ? new_account : getAccountByName(new_account.getName(), (new_account.getParentId() != 0L ?  (AccountType)getById(new_account.getParentId(), new_account.getOrganization()) : null), new_account.getOrganization()));
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
		new_account.setDatabaseRecord(true);
		new_account.setNameType(NameEnumType.ACCOUNT);
		new_account.setAccountId(rset.getString("accountid"));
		new_account.setReferenceId(rset.getLong("referenceid"));
		new_account.setAccountStatus(AccountStatusEnumType.valueOf(rset.getString("accountstatus")));
		new_account.setAccountType(AccountEnumType.valueOf(rset.getString("accounttype")));
		return super.read(rset, new_account);
	}
	/*
	public List<AccountType> getAccountList(QueryField[] fields, OrganizationType organization) throws FactoryException
	{

		List<NameIdType> account_list = getByField(fields, organization.getId());
		return convertList(account_list);
	}
	*/
	public List<AccountType>  getAccountList(int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getAccountList(new QueryField[] {  }, startRecord, recordCount, organization);
	}
	public List<AccountType>  getAccountList(ProcessingInstructionType instruction, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getAccountList(new QueryField[] {  }, instruction, startRecord, recordCount,organization);
	}
	public List<AccountType>  getAccountList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getAccountList(fields, instruction, startRecord,recordCount,organization);
	}
	public List<AccountType>  getAccountList(QueryField[] fields, ProcessingInstructionType instruction,int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getAccountList(fields, instruction, organization);
	}
	public List<AccountType> getAccountList(QueryField[] fields, ProcessingInstructionType instruction, OrganizationType organization) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();

		List<NameIdType> AccountList = getByField(fields, instruction, organization.getId());
		return convertList(AccountList);

	}
	
	public List<AccountType> getAccountListByIds(int[] Account_ids, OrganizationType organization) throws FactoryException, ArgumentException
	{
		StringBuffer buff = new StringBuffer();
		int deleted = 0;
		List<AccountType> out_list = new ArrayList<AccountType>();
		for (int i = 0; i < Account_ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(Account_ids[i]);
			if ((i > 0 || Account_ids.length == 1) && ((i % 250 == 0) || i == Account_ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
				match.setComparator(ComparatorEnumType.IN);
				List<AccountType> tmp_Account_list = getAccountList(new QueryField[] { match }, null, organization);
				out_list.addAll(tmp_Account_list);
				buff.delete(0,  buff.length());
			}
		}
		return out_list;
	}
}
