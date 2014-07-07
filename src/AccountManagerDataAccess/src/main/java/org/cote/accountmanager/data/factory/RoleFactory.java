package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class RoleFactory extends NameIdFactory {
	public RoleFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.tableNames.add("roles");
		factoryType = FactoryEnumType.ROLE;
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		BaseRoleType role = (BaseRoleType)obj;
		return role.getRoleType().toString() + "-" + role.getName() + "-" + role.getParentId() + "-" + role.getOrganization().getId();
	}
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("groups")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	protected void addDefaultRoles(OrganizationType organization) throws DataAccessException, FactoryException, ArgumentException
	{
		addDefaultAccountRoles(organization);
		addDefaultUserRoles(organization);
	}
	public void addDefaultAccountRoles(OrganizationType organization) throws DataAccessException, FactoryException, ArgumentException
	{
		UserType admin = Factories.getUserFactory().getUserByName("Admin", organization);
		
		AccountRoleType root_role = newAccountRole(admin,"Root");
		addRole(root_role);
		root_role = getAccountRoleByName("Root", organization);

		AccountRoleType home_role = getCreateAccountRole(admin,"Home", root_role);

	}
	public void addDefaultUserRoles(OrganizationType organization) throws DataAccessException, FactoryException, ArgumentException
	{
		UserType admin = Factories.getUserFactory().getUserByName("Admin", organization);
		
		UserRoleType root_role = newUserRole(admin,"Root");
		addRole(root_role);
		root_role = getUserRoleByName("Root", organization);

		UserRoleType home_role = getCreateUserRole(admin,"Home", root_role);

	}

	protected void removeRoleFromCache(BaseRoleType role){
		//String key_name = role.getRoleType().toString() + "-" + role.getName() + "-" + role.getParentId() + "-" + role.getOrganization().getId();
		String key_name = getCacheKeyName(role);
		removeFromCache(role, key_name);
	}
	public boolean deleteRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		if(role == null) return false;
		List<BaseRoleType> childRoles = getRoleList(new QueryField[]{QueryFields.getFieldParent(role.getId())}, null, 0, 0, role.getOrganization());
		//System.out.println("Remove " + childRoles.size() + " children of role #" + role.getId());
		for(int i = childRoles.size() -1; i >=0; i--) deleteRole(childRoles.get(i));
		removeRoleFromCache(role);
		int deleted = deleteById(role.getId(), role.getOrganization().getId());
		Factories.getRoleParticipationFactory().deleteParticipations(role);
		return (deleted > 0);
	}
	public int deleteRolesByUser(NameIdType map) throws FactoryException, ArgumentException
	{
		/// QueryFields.getFieldRoleType(RoleEnumType.valueOf(map.getNameType().toString()))
		List<BaseRoleType> roles = getRoles(new QueryField[]{QueryFields.getFieldOwner(map)}, map.getOrganization());
		List<Long> role_ids = new ArrayList<Long>();
		for (int i = 0; i < roles.size(); i++)
		{
			role_ids.add(roles.get(i).getId());
			removeRoleFromCache(roles.get(i));
		}
		
		return deleteRolesByIds(convertLongList(role_ids), map.getOrganization());
	}
	public int deleteRolesByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			Factories.getRoleParticipationFactory().deleteParticipations(ids, organization);
		}
		return deleted;
	}
	public boolean updateRole(BaseRoleType role) throws FactoryException
	{
		removeFromCache(role, null);
		return update(role);
	}

	public boolean addRole(BaseRoleType new_role) throws FactoryException, DataAccessException
	{
		if (new_role.getOrganization() == null || new_role.getOrganization().getId() <= 0 || new_role.getOwnerId() <= 0) throw new FactoryException("Cannot add role to invalid organization, or without an OwnerId");
		DataRow row = prepareAdd(new_role, "roles");
		row.setCellValue("roletype", new_role.getRoleType().toString());
		row.setCellValue("referenceid", new_role.getReferenceId());
		return insertRow(row);
	}
	public AccountRoleType getRootAccountRoleType(OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getAccountRoleByName("Root", organization);
	}
	public AccountRoleType getHomeAccountRole(OrganizationType organization) throws FactoryException, ArgumentException
	{

		return getAccountRoleByName("Home", getRootAccountRoleType(organization), organization);
	}
	public AccountRoleType getAccountRole(AccountType account)  throws FactoryException, ArgumentException
	{

		return getAccountRoleByName(account.getName(), getHomeAccountRole(account.getOrganization()), account.getOrganization());
	}

	public AccountRoleType getCreateAccountRole(UserType role_owner, String role_name, BaseRoleType Parent) throws FactoryException, DataAccessException, ArgumentException
	{
		AccountRoleType role = getAccountRoleByName(role_name, Parent, role_owner.getOrganization());
		if (role == null)
		{
			role = newAccountRole(role_owner, role_name, Parent);
			if (addRole(role))
			{
				role = getAccountRoleByName(role_name, Parent, role_owner.getOrganization());
			}
			else role = null;
		}
		return role;
	}
	public UserRoleType getRootUserRoleType(OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getUserRoleByName("Root", organization);
	}
	public UserRoleType getHomeUserRole(OrganizationType organization) throws FactoryException, ArgumentException
	{

		return getUserRoleByName("Home", getRootUserRoleType(organization), organization);
	}
	public UserRoleType getUserRole(UserType account)  throws FactoryException, ArgumentException
	{

		return getUserRoleByName(account.getName(), getHomeUserRole(account.getOrganization()), account.getOrganization());
	}
	public UserRoleType getCreateUserRole(UserType role_owner, String role_name, BaseRoleType Parent) throws FactoryException, DataAccessException, ArgumentException
	{
		UserRoleType role = getUserRoleByName(role_name, Parent, role_owner.getOrganization());
		if (role == null)
		{
			role = newUserRole(role_owner, role_name, Parent);
			if (addRole(role))
			{
				role = getUserRoleByName(role_name, Parent, role_owner.getOrganization());
			}
			else role = null;
		}
		return role;
	}
	public <T> T getRoleById(long id, OrganizationType organization) throws FactoryException, ArgumentException
	{
		NameIdType out_role = readCache(id);
		if (out_role != null) return (T)out_role;

		List<NameIdType> roles = getById(id, organization.getId());
		if (roles.size() > 0)
		{
			BaseRoleType role = (BaseRoleType)roles.get(0);
			//String key_name = role.getRoleType() + "-" + role.getName() + "-" + role.getParentId() + "-" + role.getOrganization().getId();
			String key_name = getCacheKeyName(role);
			addToCache(roles.get(0), key_name);
			return (T)roles.get(0);
		}
		return null;
	}
	
	public AccountRoleType getAccountRoleByName(String name, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getAccountRoleByName(name, null, organization);
	}
	public AccountRoleType getAccountRoleByName(String name, BaseRoleType Parent, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getRoleByName(name, Parent, RoleEnumType.ACCOUNT, organization);
	}
	public UserRoleType getUserRoleByName(String name, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getUserRoleByName(name, null, organization);
	}
	public UserRoleType getUserRoleByName(String name, BaseRoleType Parent, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getRoleByName(name, Parent, RoleEnumType.USER, organization);
	}
	public <T> T getRoleByName(String name, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getRoleByName(name, null, organization);
	}

	public <T> T getRoleByName(String name, BaseRoleType Parent, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getRoleByName(name, Parent, RoleEnumType.UNKNOWN, organization);
	}
	public <T> T getRoleByName(String name, BaseRoleType Parent, RoleEnumType role_type, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		if(name == null || organization == null) throw new ArgumentException((name == null ? "Name" : "Organization") + " is null");
		if(Parent != null && Parent.getId() == null) throw new ArgumentException("Parent id is null");
		long parent_id = 0;
		if (Parent != null) parent_id = Parent.getId();
		String key_name = role_type.toString() + "-" + name + "-" + parent_id + "-" + organization.getId();
		NameIdType out_role = readCache(key_name);
		if (out_role != null){
//			System.out.println("Reading Cache: " + key_name);
			return (T)out_role;
		}

		List<QueryField> Fields = new ArrayList<QueryField>();
		Fields.add(QueryFields.getFieldName(name));
		Fields.add(QueryFields.getFieldParent(parent_id));
		if (role_type != RoleEnumType.UNKNOWN) Fields.add(QueryFields.getFieldRoleType(role_type));
		List<NameIdType> roles = getByField(Fields.toArray(new QueryField[0]),organization.getId());
		if (roles.size() > 0)
		{
			addToCache(roles.get(0), key_name);
			return (T)roles.get(0);
		}
		return null;
	}
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BaseRoleType use_map = (BaseRoleType)map;
		fields.add(QueryFields.getFieldRoleType(use_map.getRoleType()));
		fields.add(QueryFields.getFieldReferenceId(use_map.getReferenceId()));
	}

	public AccountRoleType newAccountRole(UserType user, String role_name) throws FactoryException
	{
		return newAccountRole(user, role_name, null);
	}
	public AccountRoleType newAccountRole(UserType user, String role_name, BaseRoleType ParentRole) throws FactoryException
	{
		return newRoleType(RoleEnumType.ACCOUNT, user, role_name, ParentRole);
	}
	public UserRoleType newUserRole(UserType user, String role_name) throws FactoryException
	{
		return newUserRole(user, role_name, null);
	}
	public UserRoleType newUserRole(UserType user, String role_name, BaseRoleType ParentRole) throws FactoryException
	{
		return newRoleType(RoleEnumType.USER, user, role_name, ParentRole);
	}
	public <T> T newRoleType(RoleEnumType type, UserType owner, String role_name, BaseRoleType parentRole) throws FactoryException
	{
		if (owner == null) throw new FactoryException("Role cannot be created without a valid owner");
		BaseRoleType new_role = newRole(type);
		new_role.setOrganization(owner.getOrganization());
		new_role.setOwnerId(owner.getId());
		new_role.setName(role_name);
		if (parentRole != null) new_role.setParentId(parentRole.getId());
		return (T)new_role;
	}

	protected <T> T newRole(RoleEnumType Type) throws FactoryException
	{
		BaseRoleType new_role = null;
		switch (Type)
		{
			case ACCOUNT:
				new_role = new AccountRoleType();
				break;
			case USER:
				new_role = new UserRoleType();
				break;
			default:
				throw new FactoryException("Invalid role type: " + Type.toString());
				//new_role = new BaseRoleType();

		}
		new_role.setNameType(NameEnumType.ROLE);
		new_role.setRoleType(Type);
		return (T)new_role;
	}
	public List<AccountRoleType> getAccountRoles(OrganizationType organization)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { QueryFields.getFieldRoleType(RoleEnumType.ACCOUNT) }, organization.getId());
		return convertList(roles);

	}
	public List<AccountRoleType> getAccountRoles(QueryField match, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { match, QueryFields.getFieldRoleType(RoleEnumType.ACCOUNT) }, organization.getId());
		return convertList(roles);

	}
	public List<UserRoleType> getUserRoles(OrganizationType organization)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { QueryFields.getFieldRoleType(RoleEnumType.USER) }, organization.getId());
		return convertList(roles);

	}
	public List<UserRoleType> getUserRoles(QueryField match, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { match, QueryFields.getFieldRoleType(RoleEnumType.USER) }, organization.getId());
		return convertList(roles);

	}
	public List<BaseRoleType> getRoles(QueryField match, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getRoles(new QueryField[] { match }, organization);
	}
	public List<BaseRoleType> getRoles(QueryField[] matches, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(matches, organization.getId());
		return convertList(roles);

	}
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		BaseRoleType role = newRole(RoleEnumType.valueOf(rset.getString("roletype")));
		role.setReferenceId(rset.getLong("referenceid"));
		//role.setRoleType(RoleEnumType.valueOf(rset.getString("roletype")));
		return super.read(rset, role);
	}
	
	public List<BaseRoleType>  getRoleList(int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getRoleList(new QueryField[] { QueryFields.getFieldParent(0)  }, startRecord, recordCount, organization);
	}
	public List<BaseRoleType>  getRoleList(BaseRoleType parentRole, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getRoleList(new QueryField[] { QueryFields.getFieldParent(parentRole.getId()) }, startRecord, recordCount, organization);
	}
	public List<BaseRoleType>  getRoleList(ProcessingInstructionType instruction, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getRoleList(new QueryField[] { QueryFields.getFieldParent(0)  }, instruction, startRecord, recordCount,organization);
	}
	public List<BaseRoleType>  getRoleList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getRoleList(fields, instruction, startRecord,recordCount,organization);
	}
	public List<BaseRoleType>  getRoleList(QueryField[] fields, ProcessingInstructionType instruction,int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getRoleList(fields, instruction, organization);
	}
	public List<BaseRoleType> getRoleList(QueryField[] fields, ProcessingInstructionType instruction, OrganizationType organization) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();

		List<NameIdType> RoleList = getByField(fields, instruction, organization.getId());
		return convertList(RoleList);

	}
	
	public List<BaseRoleType> getRoleListByIds(int[] Role_ids, OrganizationType organization) throws FactoryException, ArgumentException
	{
		StringBuffer buff = new StringBuffer();
		int deleted = 0;
		List<BaseRoleType> out_list = new ArrayList<BaseRoleType>();
		for (int i = 0; i < Role_ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(Role_ids[i]);
			if ((i > 0 || Role_ids.length == 1) && ((i % 250 == 0) || i == Role_ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
				match.setComparator(ComparatorEnumType.IN);
				List<BaseRoleType> tmp_Role_list = getRoleList(new QueryField[] { match }, null, organization);
				out_list.addAll(tmp_Role_list);
				buff.delete(0,  buff.length());
			}
		}
		return out_list;
	}
}

