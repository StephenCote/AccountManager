package org.cote.accountmanager.data.factory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountPermissionType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataPermissionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ObjectPermissionType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.RolePermissionType;
import org.cote.accountmanager.objects.UserPermissionType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;

public class PermissionFactory extends NameIdFactory {
	public PermissionFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.tableNames.add("permissions");
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("groups")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	public boolean deletePermission(BasePermissionType permission) throws FactoryException
	{
		removeFromCache(permission);
		int deleted = deleteById(permission.getId(), permission.getOrganization().getId());
		return (deleted > 0);
	}
	public boolean updatePermission(BasePermissionType permission) throws FactoryException
	{
		return update(permission);
	}

	public boolean addPermission(BasePermissionType new_permission) throws DataAccessException, FactoryException
	{
		if (new_permission.getOrganization() == null || new_permission.getOrganization().getId() <= 0) throw new FactoryException("Cannot add permission to invalid organization");
		DataRow row = prepareAdd(new_permission, "permissions");
		row.setCellValue("permissiontype",new_permission.getPermissionType().toString());
		row.setCellValue("referenceid",new_permission.getReferenceId());
		return insertRow(row);
	}

	public BasePermissionType getPermissionById(int id, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		BasePermissionType out_perm = readCache(id);
		if (out_perm != null) return out_perm;

		List<NameIdType> perms = getById(id, organization.getId());
		if (perms.size() > 0)
		{
			addToCache(perms.get(0));
			return (BasePermissionType)perms.get(0);
		}
		return null;
	}

	public BasePermissionType getPermissionByName(String name, OrganizationType organization) throws FactoryException, ArgumentException
	{
		String key_name = name + "-" + organization.getId();
		BasePermissionType out_perm = readCache(key_name);
		if (out_perm != null) return out_perm;

		List<NameIdType> perms = getByName(name,organization.getId());
		if (perms.size() > 0)
		{
			addToCache(perms.get(0),key_name);
			return (BasePermissionType)perms.get(0);
		}
		return null;
	}
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BasePermissionType use_map = (BasePermissionType)map;
		fields.add(QueryFields.getFieldPermissionType(use_map.getPermissionType()));
		fields.add(QueryFields.getFieldReferenceId(use_map.getReferenceId()));

	}

	public BasePermissionType newPermission(String permission_name, PermissionEnumType Type, OrganizationType organization)
	{
		BasePermissionType new_perm = newPermission(Type);
		new_perm.setOrganization(organization);
		new_perm.setName(permission_name);
		return new_perm;
	}
	protected BasePermissionType newPermission(PermissionEnumType Type)
	{
		BasePermissionType new_perm = null;
		switch (Type)
		{
			case OBJECT:
				new_perm = new ObjectPermissionType();
				break;
			case APPLICATION:
				new_perm = new ApplicationPermissionType();
				break;
			case DATA:
				new_perm = new DataPermissionType();
				break;
			case ROLE:
				new_perm = new RolePermissionType();
				break;
			case ACCOUNT:
				new_perm = new AccountPermissionType();
				break;
			case USER:
				new_perm = new UserPermissionType();
				break;
			default:
				new_perm = new BasePermissionType();
				break;
		}
		new_perm.setPermissionType(Type);
		return new_perm;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		BasePermissionType new_per = newPermission(PermissionEnumType.valueOf(rset.getString("permissiontype")));
		new_per.setReferenceId(rset.getLong("referenceid"));

		return super.read(rset, new_per);
	}
	

}
