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
import org.cote.accountmanager.objects.AccountPermissionType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataPermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ObjectPermissionType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonPermissionType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.RolePermissionType;
import org.cote.accountmanager.objects.UserPermissionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;

public class PermissionFactory extends NameIdFactory {
	public PermissionFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.hasUrn = true;
		this.tableNames.add("permissions");
		this.factoryType = FactoryEnumType.PERMISSION;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("permissions")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		BasePermissionType t = (BasePermissionType)obj;
		return t.getName() + "-" + t.getPermissionType().toString() + "-" + t.getParentId() + "-" + t.getOrganization().getId();
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

	public <T> T getPermissionById(long id, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		T out_perm = readCache(id);
		if (out_perm != null) return out_perm;

		List<NameIdType> perms = getById(id, organization.getId());
		if (perms.size() > 0)
		{
			addToCache(perms.get(0));
			return (T)perms.get(0);
		}
		return null;
	}
	
	public <T> T getPermissionByName(String name, PermissionEnumType type, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermissionByName(name, type, null, organization);
	}
	
	public <T> T getPermissionByName(String name, PermissionEnumType type, BasePermissionType parent, OrganizationType organization) throws FactoryException, ArgumentException
	{
		String key_name = name + "-" + type.toString() + "-" + (parent != null ? parent.getId() : "0") + "-" + organization.getId();
		T out_perm = readCache(key_name);
		if (out_perm != null) return out_perm;

		//List<NameIdType> perms = getByName(name,organization.getId());
		List<NameIdType> perms = getByField(new QueryField[] { QueryFields.getFieldName(name), QueryFields.getFieldParent(( parent != null ? parent.getId() : 0))},organization.getId());
		if (perms.size() > 0)
		{
			addToCache(perms.get(0),key_name);
			return (T)perms.get(0);
		}
		return null;
	}
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BasePermissionType use_map = (BasePermissionType)map;
		fields.add(QueryFields.getFieldPermissionType(use_map.getPermissionType()));
		fields.add(QueryFields.getFieldReferenceId(use_map.getReferenceId()));

	}
	
	public <T> T getRootPermission(PermissionEnumType type, OrganizationType org) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(null,"Root",type,null,org);
	}
	public <T> T getHomePermission(PermissionEnumType type, OrganizationType org) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(null,"Home",type,(T)getRootPermission(type,org),org);
	}
	public <T> T getUserPermission(UserType user,PermissionEnumType type, OrganizationType org) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(user,user.getName(),type,(T)getHomePermission(type,org),org);
	}

	public <T> T getCreatePermission(UserType user, String name, PermissionEnumType type, T parent, OrganizationType org) throws FactoryException, ArgumentException, DataAccessException{
		T per = (T)getPermissionByName(name,type,(BasePermissionType)parent,org);
		if(per == null){
			per = (T)newPermission(user,name,type,(BasePermissionType)parent,org);
			if(addPermission((BasePermissionType)per)){
				per = (T)getPermissionByName(name,type,(BasePermissionType)parent,org);
			}
		}
		return per;
		
	}
	
	/// Create a permission hierarchy that mirrors a group hierarchy
	///
	public String getPermissionPath(BasePermissionType per) throws FactoryException, ArgumentException{
		String path = "";
		/// Note: Skip 'Global' Permission, which is always 1L
		/// (always == until it's not, but it's never been not because it must be setup first)
		if(per.getParentId() > 1L){
			path = getPermissionPath((BasePermissionType)getPermissionById(per.getParentId(),per.getOrganization()));
		}
		path = path + "/" + per.getName();
		return path;
	}

	public <T> T findPermission(PermissionEnumType type, String pathBase, OrganizationType org) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(null, type, pathBase,org);
	}
	public <T> T makePath(UserType user, PermissionEnumType type, DirectoryGroupType group) throws FactoryException, ArgumentException, DataAccessException{
		Factories.getGroupFactory().populate(group);
		return makePath(user, type, group.getPath(), group.getOrganization());
	}
	public <T> T makePath(UserType user, PermissionEnumType type, String pathBase, OrganizationType org) throws FactoryException, ArgumentException, DataAccessException{
		String[] path = pathBase.split("/");
		BasePermissionType parent = null;
		T per = null;
		for(int i = 0; i < path.length;i++){
			String seg = path[i];
			if(seg.equals("")){
				parent = getRootPermission(type,  org);
				continue;
			}
			if(seg.equals("Home") && parent != null && parent.getName().equals("Root") && parent.getParentId() == 0L){
				parent = getHomePermission(type, org);
				continue;
			}
			if(user != null) per = getCreatePermission(user, seg, type, (T)parent, org);
			else per = getPermissionByName(seg,type,(BasePermissionType)parent,org);
			if(per == null){
				logger.warn("Failed to find permission '" + seg + "' in " + (parent == null ? "Null Parent":parent.getName()));
				return null;
			}
			parent = (BasePermissionType)per;
		}
		return per;
	}

	
	public BasePermissionType newPermission(UserType owner, String permission_name, PermissionEnumType type, BasePermissionType parent, OrganizationType organization)
	{
		BasePermissionType new_perm = newPermission(permission_name, type, parent, organization);
		new_perm.setOwnerId((owner != null ? owner.getId() : 0L));
		return new_perm;
	}
	
	public BasePermissionType newPermission(String permission_name, PermissionEnumType type, BasePermissionType parent, OrganizationType organization)
	{
		BasePermissionType new_perm = newPermission(permission_name, type, organization);
		new_perm.setParentId((parent != null ? parent.getId() : 0L));
		return new_perm;
	}
	
	public BasePermissionType newPermission(String permission_name, PermissionEnumType type, OrganizationType organization)
	{
		BasePermissionType new_perm = newPermission(type);
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
			case PERSON:
				new_perm = new PersonPermissionType();
				break;

			default:
				new_perm = new BasePermissionType();
				break;
		}
		new_perm.setPermissionType(Type);
		new_perm.setNameType(NameEnumType.PERMISSION);
		return new_perm;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		BasePermissionType new_per = newPermission(PermissionEnumType.valueOf(rset.getString("permissiontype")));
		new_per.setReferenceId(rset.getLong("referenceid"));
		return super.read(rset, new_per);
	}

	public <T> List<T>  getPermissionList(PermissionEnumType type, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getPermissionList(null, type, startRecord, recordCount, organization);
	}	
	public <T> List<T>  getPermissionList(BasePermissionType parent, PermissionEnumType type, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<QueryField>();
		if(type != PermissionEnumType.UNKNOWN) fields.add(QueryFields.getFieldPermissionType(type));
		fields.add(QueryFields.getFieldParent((parent != null ? parent.getId() : 0L)));
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");

		return getPermissionList(fields.toArray(new QueryField[0]), instruction, startRecord, recordCount, organization);
	}
	public <T> List<T>  getPermissionList(QueryField[] fields, ProcessingInstructionType instruction,int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getPermissionList(fields, instruction, organization);
	}
	public <T> List<T> getPermissionList(QueryField[] fields, ProcessingInstructionType instruction, OrganizationType organization) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();
		List<NameIdType> PermissionList = getByField(fields, instruction, organization.getId());
		return convertList(PermissionList);
	}
	public int deletePermissionsByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		return deleteById(ids, organization.getId());
	}


}
