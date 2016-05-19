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

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class RoleFactory extends NameIdFactory {
	static{
		AuthorizationService.registerAuthorizationProviders(
				FactoryEnumType.ROLE,
				NameEnumType.ROLE,
				Factories.getRoleParticipationFactory()
			);
	}
	
	
	public RoleFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.hasUrn = true;
		this.tableNames.add("roles");
		factoryType = FactoryEnumType.ROLE;
		systemRoleNameReader = "RoleReaders";
		systemRoleNameAdministrator = "RoleAdministrators";
	}
	
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		BaseRoleType role = (BaseRoleType)obj;
		if(role.getPopulated()) return;
		role.setPopulated(true);
		updateToCache(role);
	}
	
	@Override
	public <T> void normalize(T object) throws ArgumentException, FactoryException{
		super.normalize(object);
		if(object == null){
			throw new ArgumentException("Null object");
		}
		BaseRoleType obj = (BaseRoleType)object;
		if(obj.getParentId().compareTo(0L) != 0) return;
		if(obj.getParentPath() == null || obj.getParentPath().length() == 0 || obj.getRoleType() == RoleEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object parent path or type");	
		}
		BaseRoleType dir = null;
		try{
			dir = findRole(obj.getRoleType(), obj.getParentPath(), obj.getOrganizationId());
			if(dir == null){
				throw new ArgumentException("Invalid group path '" + obj.getParentPath() + "'");
			}
			obj.setParentId(dir.getId());
		}
		catch(DataAccessException e){
			throw new FactoryException(e.getMessage());
		}
	}
	
	@Override
	public <T> void denormalize(T object) throws ArgumentException, FactoryException{
		super.denormalize(object);
		if(object == null){
			throw new ArgumentException("Null object");
		}
		BaseRoleType obj = (BaseRoleType)object;
		if(obj.getParentId().compareTo(0L) == 0){
			logger.warn("Root level role does not have a path");
			return;	
		}
		if(obj.getParentPath() != null) return;
		BaseRoleType parent = Factories.getRoleFactory().getRoleById(obj.getParentId(), obj.getOrganizationId());
		obj.setParentPath(Factories.getRoleFactory().getRolePath(parent));
	}
	
	
	@Override
	public <T> String getCacheKeyName(T obj){
		BaseRoleType role = (BaseRoleType)obj;
		return role.getRoleType().toString() + "-" + role.getName() + "-" + role.getParentId() + "-" + role.getOrganizationId();
	}
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("groups")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	protected void addDefaultRoles(long organizationId) throws DataAccessException, FactoryException, ArgumentException
	{
		addDefaultAccountRoles(organizationId);
		addDefaultUserRoles(organizationId);
	}
	public void addDefaultPersonRoles(long organizationId) throws DataAccessException, FactoryException, ArgumentException
	{
		UserType admin = Factories.getUserFactory().getUserByName("Admin", organizationId);
		
		PersonRoleType root_role = newPersonRole(admin,"Root");
		addRole(root_role);
		root_role = getPersonRoleByName("Root", organizationId);

		PersonRoleType home_role = getCreatePersonRole(admin,"Home", root_role);

	}
	public void addDefaultAccountRoles(long organizationId) throws DataAccessException, FactoryException, ArgumentException
	{
		UserType admin = Factories.getUserFactory().getUserByName("Admin", organizationId);
		
		AccountRoleType root_role = newAccountRole(admin,"Root");
		addRole(root_role);
		root_role = getAccountRoleByName("Root", organizationId);

		AccountRoleType home_role = getCreateAccountRole(admin,"Home", root_role);

	}
	public void addDefaultUserRoles(long organizationId) throws DataAccessException, FactoryException, ArgumentException
	{
		UserType admin = Factories.getUserFactory().getUserByName("Admin", organizationId);
		
		UserRoleType root_role = newUserRole(admin,"Root");
		addRole(root_role);
		root_role = getUserRoleByName("Root", organizationId);

		UserRoleType home_role = getCreateUserRole(admin,"Home", root_role);

	}

	protected void removeRoleFromCache(BaseRoleType role){
		//String key_name = role.getRoleType().toString() + "-" + role.getName() + "-" + role.getParentId() + "-" + role.getOrganizationId();
		String key_name = getCacheKeyName(role);
		removeFromCache(role, key_name);
	}
	public boolean deleteRole(BaseRoleType role) throws FactoryException, ArgumentException
	{
		if(role == null) return false;
		List<BaseRoleType> childRoles = getRoleList(new QueryField[]{QueryFields.getFieldParent(role.getId())}, null, 0, 0, role.getOrganizationId());
		//System.out.println("Remove " + childRoles.size() + " children of role #" + role.getId());
		for(int i = childRoles.size() -1; i >=0; i--) deleteRole(childRoles.get(i));
		removeRoleFromCache(role);
		int deleted = deleteById(role.getId(), role.getOrganizationId());
		Factories.getRoleParticipationFactory().deleteParticipations(role);
		return (deleted > 0);
	}
	public int deleteRolesByUser(NameIdType map) throws FactoryException, ArgumentException
	{
		/// QueryFields.getFieldRoleType(RoleEnumType.valueOf(map.getNameType().toString()))
		List<BaseRoleType> roles = getRoles(new QueryField[]{QueryFields.getFieldOwner(map)}, map.getOrganizationId());
		List<Long> role_ids = new ArrayList<Long>();
		for (int i = 0; i < roles.size(); i++)
		{
			role_ids.add(roles.get(i).getId());
			removeRoleFromCache(roles.get(i));
		}
		
		return deleteRolesByIds(convertLongList(role_ids), map.getOrganizationId());
	}
	public int deleteRolesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			Factories.getRoleParticipationFactory().deleteParticipations(ids, organizationId);
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
		if (new_role.getOrganizationId() == null || new_role.getOrganizationId() <= 0 || new_role.getOwnerId() <= 0) throw new FactoryException("Cannot add role to invalid organization, or without an OwnerId");
		DataRow row = prepareAdd(new_role, "roles");
		row.setCellValue("roletype", new_role.getRoleType().toString());
		row.setCellValue("referenceid", new_role.getReferenceId());
		return insertRow(row);
	}
	
	
	public <T> T getRootRole(RoleEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreateRole(Factories.getAdminUser(organizationId),"Root",type,null,organizationId);
	}
	public <T> T getHomeRole(RoleEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreateRole(Factories.getAdminUser(organizationId),"Home",type,(T)getRootRole(type,organizationId),organizationId);
	}
	public <T> T getUserRole(UserType user,RoleEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreateRole(user,user.getName(),type,(T)getHomeRole(type,organizationId),organizationId);
	}

	public <T> T getCreateRole(UserType user, String name, RoleEnumType type, T parent, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		T per = (T)getRoleByName(name,(BaseRoleType)parent,type,organizationId);
		if(per == null){
			logger.info("Role " + type + " " + name + " doesn't exist.  Attempting to create.");
			per = (T)newRoleType(type,user,name,(BaseRoleType)parent);
			if(addRole((BaseRoleType)per)){
				per = (T)getRoleByName(name,(BaseRoleType)parent,type,organizationId);
			}
		}
		return per;
		
	}
	
	
	/// NOTE: The following methods mimic the preceding behavior for named types
	/// These need to be refactored out as dupes
	///
	
	public PersonRoleType getRootPersonRoleType(long organizationId)  throws FactoryException, ArgumentException
	{
		return getPersonRoleByName("Root", organizationId);
	}
	public PersonRoleType getHomePersonRole(long organizationId) throws FactoryException, ArgumentException
	{

		return getPersonRoleByName("Home", getRootPersonRoleType(organizationId), organizationId);
	}
	public PersonRoleType getPersonRole(PersonType account)  throws FactoryException, ArgumentException
	{

		return getPersonRoleByName(account.getName(), getHomePersonRole(account.getOrganizationId()), account.getOrganizationId());
	}

	public PersonRoleType getCreatePersonRole(UserType role_owner, String role_name, BaseRoleType Parent) throws FactoryException, DataAccessException, ArgumentException
	{
		PersonRoleType role = getPersonRoleByName(role_name, Parent, role_owner.getOrganizationId());
		if (role == null)
		{
			role = newPersonRole(role_owner, role_name, Parent);
			if (addRole(role))
			{
				role = getPersonRoleByName(role_name, Parent, role_owner.getOrganizationId());
			}
			else role = null;
		}
		return role;
	}
	public AccountRoleType getRootAccountRoleType(long organizationId)  throws FactoryException, ArgumentException
	{
		return getAccountRoleByName("Root", organizationId);
	}
	public AccountRoleType getHomeAccountRole(long organizationId) throws FactoryException, ArgumentException
	{

		return getAccountRoleByName("Home", getRootAccountRoleType(organizationId), organizationId);
	}
	public AccountRoleType getAccountRole(AccountType account)  throws FactoryException, ArgumentException
	{

		return getAccountRoleByName(account.getName(), getHomeAccountRole(account.getOrganizationId()), account.getOrganizationId());
	}

	public AccountRoleType getCreateAccountRole(UserType role_owner, String role_name, BaseRoleType Parent) throws FactoryException, DataAccessException, ArgumentException
	{
		AccountRoleType role = getAccountRoleByName(role_name, Parent, role_owner.getOrganizationId());
		if (role == null)
		{
			role = newAccountRole(role_owner, role_name, Parent);
			if (addRole(role))
			{
				role = getAccountRoleByName(role_name, Parent, role_owner.getOrganizationId());
			}
			else role = null;
		}
		return role;
	}
	public UserRoleType getRootUserRoleType(long organizationId)  throws FactoryException, ArgumentException
	{
		return getUserRoleByName("Root", organizationId);
	}
	public UserRoleType getHomeUserRole(long organizationId) throws FactoryException, ArgumentException
	{

		return getUserRoleByName("Home", getRootUserRoleType(organizationId), organizationId);
	}
	public UserRoleType getUserRole(UserType account)  throws FactoryException, ArgumentException
	{

		return getUserRoleByName(account.getName(), getHomeUserRole(account.getOrganizationId()), account.getOrganizationId());
	}
	public UserRoleType getCreateUserRole(UserType role_owner, String role_name, BaseRoleType Parent) throws FactoryException, DataAccessException, ArgumentException
	{
		UserRoleType role = getUserRoleByName(role_name, Parent, role_owner.getOrganizationId());
		if (role == null)
		{
			role = newUserRole(role_owner, role_name, Parent);
			if (addRole(role))
			{
				role = getUserRoleByName(role_name, Parent, role_owner.getOrganizationId());
			}
			else role = null;
		}
		return role;
	}
	public <T> T getRoleById(long id, long organizationId) throws FactoryException, ArgumentException
	{
		NameIdType out_role = readCache(id);
		if (out_role != null) return (T)out_role;

		BaseRoleType role = getById(id, organizationId);
		if (role != null)
		{
			String key_name = getCacheKeyName(role);
			addToCache(role, key_name);
			return (T)role;
		}
		return null;
	}
	public PersonRoleType getPersonRoleByName(String name, long organizationId)  throws FactoryException, ArgumentException
	{
		return getPersonRoleByName(name, null, organizationId);
	}
	public PersonRoleType getPersonRoleByName(String name, BaseRoleType Parent, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleByName(name, Parent, RoleEnumType.PERSON, organizationId);
	}
	
	public AccountRoleType getAccountRoleByName(String name, long organizationId)  throws FactoryException, ArgumentException
	{
		return getAccountRoleByName(name, null, organizationId);
	}
	public AccountRoleType getAccountRoleByName(String name, BaseRoleType Parent, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleByName(name, Parent, RoleEnumType.ACCOUNT, organizationId);
	}
	public UserRoleType getUserRoleByName(String name, long organizationId)  throws FactoryException, ArgumentException
	{
		return getUserRoleByName(name, null, organizationId);
	}
	public UserRoleType getUserRoleByName(String name, BaseRoleType Parent, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleByName(name, Parent, RoleEnumType.USER, organizationId);
	}
	public <T> T getRoleByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		return getRoleByName(name, null, organizationId);
	}

	public <T> T getRoleByName(String name, BaseRoleType Parent, long organizationId) throws FactoryException, ArgumentException
	{
		return getRoleByName(name, Parent, RoleEnumType.UNKNOWN, organizationId);
	}
	public <T> T getRoleByName(String name, BaseRoleType Parent, RoleEnumType role_type, long organizationId)  throws FactoryException, ArgumentException
	{
		if(name == null || organizationId <= 0L) throw new ArgumentException((name == null ? "Name" : "Organization") + " is null");
		if(Parent != null && Parent.getId() == null) throw new ArgumentException("Parent id is null");
		long parent_id = 0;
		if (Parent != null) parent_id = Parent.getId();
		String key_name = role_type.toString() + "-" + name + "-" + parent_id + "-" + organizationId;
		NameIdType out_role = readCache(key_name);
		if (out_role != null){
//			System.out.println("Reading Cache: " + key_name);
			return (T)out_role;
		}

		List<QueryField> Fields = new ArrayList<QueryField>();
		Fields.add(QueryFields.getFieldName(name));
		Fields.add(QueryFields.getFieldParent(parent_id));
		if (role_type != RoleEnumType.UNKNOWN) Fields.add(QueryFields.getFieldRoleType(role_type));
		List<NameIdType> roles = getByField(Fields.toArray(new QueryField[0]),organizationId);
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

	
	public PersonRoleType newPersonRole(UserType user, String role_name) throws FactoryException
	{
		return newPersonRole(user, role_name, null);
	}
	public PersonRoleType newPersonRole(UserType user, String role_name, BaseRoleType ParentRole) throws FactoryException
	{
		return newRoleType(RoleEnumType.PERSON, user, role_name, ParentRole);
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
		if (owner == null) throw new FactoryException("Role '" + role_name + "' cannot be created without a valid owner");
		BaseRoleType new_role = newRole(type);
		new_role.setOrganizationId(owner.getOrganizationId());
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
			case PERSON:
				new_role = new PersonRoleType();
				break;
			default:
				throw new FactoryException("Invalid role type: " + Type.toString());
				//new_role = new BaseRoleType();

		}
		new_role.setNameType(NameEnumType.ROLE);
		new_role.setRoleType(Type);
		return (T)new_role;
	}
	public List<PersonRoleType> getPersonRoles(long organizationId)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { QueryFields.getFieldRoleType(RoleEnumType.PERSON) }, organizationId);
		return convertList(roles);

	}
	public List<PersonRoleType> getPersonRoles(QueryField match, long organizationId)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { match, QueryFields.getFieldRoleType(RoleEnumType.PERSON) }, organizationId);
		return convertList(roles);

	}
	public List<AccountRoleType> getAccountRoles(long organizationId)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { QueryFields.getFieldRoleType(RoleEnumType.ACCOUNT) }, organizationId);
		return convertList(roles);

	}
	public List<AccountRoleType> getAccountRoles(QueryField match, long organizationId)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { match, QueryFields.getFieldRoleType(RoleEnumType.ACCOUNT) }, organizationId);
		return convertList(roles);

	}

	public List<UserRoleType> getUserRoles(long organizationId)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { QueryFields.getFieldRoleType(RoleEnumType.USER) }, organizationId);
		return convertList(roles);

	}
	public List<UserRoleType> getUserRoles(QueryField match, long organizationId)  throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(new QueryField[] { match, QueryFields.getFieldRoleType(RoleEnumType.USER) }, organizationId);
		return convertList(roles);

	}
	public List<BaseRoleType> getRoles(QueryField match, long organizationId) throws FactoryException, ArgumentException
	{
		return getRoles(new QueryField[] { match }, organizationId);
	}
	public List<BaseRoleType> getRoles(QueryField[] matches, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> roles = getByField(matches, organizationId);
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
	
	public List<BaseRoleType>  getRoleList(long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		//return getRoleList(new QueryField[] { QueryFields.getFieldParent(0)  }, startRecord, recordCount, organizationId);
		return getRoleList(RoleEnumType.UNKNOWN, null, startRecord, recordCount, organizationId);
	}
	public List<BaseRoleType>  getRoleList(BaseRoleType parentRole, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		//return getRoleList(new QueryField[] { QueryFields.getFieldParent(parentRole.getId()) }, startRecord, recordCount, organizationId);
		return getRoleList(RoleEnumType.UNKNOWN, parentRole, startRecord, recordCount, organizationId);
	}
	public List<BaseRoleType>  getRoleList(RoleEnumType type, BaseRoleType parent, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<QueryField>();
		if(type != RoleEnumType.UNKNOWN) fields.add(QueryFields.getFieldRoleType(type));
		fields.add(QueryFields.getFieldParent((parent != null ? parent.getId() : 0L)));
		return getRoleList(fields.toArray(new QueryField[0]), startRecord, recordCount, organizationId);
	}
	/*
	public List<BaseRoleType>  getRoleList(ProcessingInstructionType instruction, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleList(new QueryField[] { QueryFields.getFieldParent(0)  }, instruction, startRecord, recordCount,organizationId);
	}
	*/
	public List<BaseRoleType>  getRoleList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getRoleList(fields, instruction, startRecord,recordCount,organizationId);
	}
	public List<BaseRoleType>  getRoleList(QueryField[] fields, ProcessingInstructionType instruction,long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getRoleList(fields, instruction, organizationId);
	}
	public List<BaseRoleType> getRoleList(QueryField[] fields, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();

		List<NameIdType> RoleList = getByField(fields, instruction, organizationId);
		return convertList(RoleList);

	}
	
	public List<BaseRoleType> getRoleListByIds(long[] Role_ids, long organizationId) throws FactoryException, ArgumentException
	{
		StringBuffer buff = new StringBuffer();
		List<BaseRoleType> out_list = new ArrayList<BaseRoleType>();
		for (int i = 0; i < Role_ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(Role_ids[i]);
			if ((i > 0 || Role_ids.length == 1) && ((i % 250 == 0) || i == Role_ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
				match.setComparator(ComparatorEnumType.IN);
				List<BaseRoleType> tmp_Role_list = getRoleList(new QueryField[] { match }, null, organizationId);
				out_list.addAll(tmp_Role_list);
				buff.delete(0,  buff.length());
			}
		}
		return out_list;
	}
	
	/// Create a role hierarchy that mirrors a group hierarchy
	///
	public String getRolePath(BaseRoleType role) throws FactoryException, ArgumentException{
		String path = "";
		/// Note: Skip 'Global' Role, which is always 1L
		/// (always == until it's not, but it's never been not because it must be setup first)
		if(role.getParentId() > 1L){
			path = getRolePath((BaseRoleType)getRoleById(role.getParentId(),role.getOrganizationId()));
		}
		if(role.getParentId().compareTo(0L) == 0 && role.getName().equals("Root")) return "";
		path = path + "/" + role.getName();
		return path;
	}
	public <T> T findRole(RoleEnumType type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(null, type, pathBase,organizationId);
	}
	public <T> T makePath(UserType user, RoleEnumType type, DirectoryGroupType group) throws FactoryException, ArgumentException, DataAccessException{
		Factories.getGroupFactory().populate(group);
		return makePath(user, type, group.getPath(), group.getOrganizationId());
	}
	public <T> T makePath(UserType user, RoleEnumType type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		String[] path = pathBase.split("/");
		BaseRoleType parent = null;
		T per = null;
		for(int i = 0; i < path.length;i++){
			String seg = path[i];
			if(seg.equals("")){
				parent = getRootRole(type,  organizationId);
				continue;
			}
			if(seg.equals("Home") && parent != null && parent.getName().equals("Root") && parent.getParentId().compareTo(0L) == 0){
				parent = getHomeRole(type, organizationId);
				continue;
			}
			if(user != null) per = getCreateRole(user, seg, type, (T)parent, organizationId);
			else per = getRoleByName(seg, (BaseRoleType)parent, type, organizationId);
			if(per == null) throw new ArgumentException("Failed to find role '" + seg + "' in " + (parent == null ? "Null Parent":parent.getName()) + " from path " + pathBase);
			parent = (BaseRoleType)per;
		}
		return per;
	}
	
}

