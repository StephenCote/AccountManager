/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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

import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
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
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class RoleFactory extends NameIdFactory {
	
	@Override
	public void registerProvider(){
		AuthorizationService.registerAuthorizationProviders(
				FactoryEnumType.ROLE,
				NameEnumType.ROLE,
				FactoryEnumType.ROLEPARTICIPATION
			);
	}
	
	public RoleFactory(){
		super();
		this.clusterByParent = true;
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.hasUrn = true;
		this.hasObjectId = true;
		this.primaryTableName = "roles";
		this.tableNames.add(primaryTableName);
		
		factoryType = FactoryEnumType.ROLE;
		systemRoleNameReader = RoleService.ROLE_ROLE_READERS;
		systemRoleNameAdministrator = RoleService.ROLE_ROLE_ADMINISTRATORS;
	}
	
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		BaseRoleType role = (BaseRoleType)obj;
		if(role.getPopulated().booleanValue()) return;
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
			logger.debug("Root level role does not have a path");
			return;	
		}
		if(obj.getParentPath() != null) return;
		BaseRoleType parent = getRoleById(obj.getParentId(), obj.getOrganizationId());
		obj.setParentPath(getRolePath(parent));
	}
	
	
	@Override
	public <T> String getCacheKeyName(T obj){
		BaseRoleType role = (BaseRoleType)obj;
		return role.getRoleType().toString() + "-" + role.getName() + "-" + role.getParentId() + "-" + role.getOrganizationId();
	}
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// restrict columns
		}
	}
	protected void addDefaultRoles(long organizationId) throws DataAccessException, FactoryException, ArgumentException
	{
		addDefaultAccountRoles(organizationId);
		addDefaultUserRoles(organizationId);
	}
	public void addDefaultPersonRoles(long organizationId) throws DataAccessException, FactoryException, ArgumentException
	{
		UserType admin = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(FactoryDefaults.ADMIN_USER_NAME, organizationId);
		
		PersonRoleType rootRole = newPersonRole(admin,FactoryDefaults.ROOT_OBJECT_NAME);
		add(rootRole);
		rootRole = getPersonRoleByName(FactoryDefaults.ROOT_OBJECT_NAME, organizationId);

		getCreatePersonRole(admin,FactoryDefaults.HOME_OBJECT_NAME, rootRole);

	}
	public void addDefaultAccountRoles(long organizationId) throws DataAccessException, FactoryException, ArgumentException
	{
		UserType admin = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(FactoryDefaults.ADMIN_USER_NAME, organizationId);
		
		AccountRoleType rootRole = newAccountRole(admin,FactoryDefaults.ROOT_OBJECT_NAME);
		add(rootRole);
		rootRole = getAccountRoleByName(FactoryDefaults.ROOT_OBJECT_NAME, organizationId);

		getCreateAccountRole(admin,FactoryDefaults.HOME_OBJECT_NAME, rootRole);

	}
	public void addDefaultUserRoles(long organizationId) throws DataAccessException, FactoryException, ArgumentException
	{
		UserType admin = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(FactoryDefaults.ADMIN_USER_NAME, organizationId);
		
		UserRoleType rootRole = newUserRole(admin,FactoryDefaults.ROOT_OBJECT_NAME);
		add(rootRole);
		rootRole = getUserRoleByName(FactoryDefaults.ROOT_OBJECT_NAME, organizationId);

		getCreateUserRole(admin,FactoryDefaults.HOME_OBJECT_NAME, rootRole);

	}

	protected void removeRoleFromCache(BaseRoleType role){
		String keyName = getCacheKeyName(role);
		removeFromCache(role, keyName);
	}
	
	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		BaseRoleType role = (BaseRoleType)object;
		if(role == null) return false;
		List<BaseRoleType> childRoles = new ArrayList<>();
		try {
			childRoles = getRoleList(new QueryField[]{QueryFields.getFieldParent(role.getId())}, null, 0, 0, role.getOrganizationId());
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		for(int i = childRoles.size() -1; i >=0; i--) delete(childRoles.get(i));
		removeRoleFromCache(role);
		int deleted = deleteById(role.getId(), role.getOrganizationId());
		((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteParticipations(role);
		return (deleted > 0);
	}
	public int deleteRolesByUser(NameIdType map) throws FactoryException, ArgumentException
	{
		List<BaseRoleType> roles = getRoles(new QueryField[]{QueryFields.getFieldOwner(map)}, map.getOrganizationId());
		List<Long> roleIds = new ArrayList<>();
		for (int i = 0; i < roles.size(); i++)
		{
			roleIds.add(roles.get(i).getId());
			removeRoleFromCache(roles.get(i));
		}
		
		return deleteRolesByIds(convertLongList(roleIds), map.getOrganizationId());
	}
	public int deleteRolesByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteParticipations(ids, organizationId);
		}
		return deleted;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		BaseRoleType role = (BaseRoleType)object;
		removeFromCache(role, null);
		return super.update(role);
	}

	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		BaseRoleType newRole = (BaseRoleType)object;
		if (newRole.getOrganizationId() == null || newRole.getOrganizationId() <= 0 || newRole.getOwnerId() <= 0) throw new FactoryException("Cannot add role to invalid organization, or without an OwnerId");
		DataRow row = prepareAdd(newRole, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.ROLETYPE), newRole.getRoleType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.REFERENCEID), newRole.getReferenceId());
		}
		catch(DataAccessException e){
			throw new FactoryException(e.getMessage());
		}
		return insertRow(row);
	}
	
	
	public <T> T getRootRole(RoleEnumType type, long organizationId) throws FactoryException, ArgumentException {
		return getCreateRole(Factories.getAdminUser(organizationId),FactoryDefaults.ROOT_OBJECT_NAME,type,null,organizationId);
	}
	@SuppressWarnings("unchecked")
	public <T> T getHomeRole(RoleEnumType type, long organizationId) throws FactoryException, ArgumentException {
		return getCreateRole(Factories.getAdminUser(organizationId),FactoryDefaults.HOME_OBJECT_NAME,type,(T)getRootRole(type,organizationId),organizationId);
	}
	@SuppressWarnings("unchecked")
	public <T> T getUserRole(UserType user,RoleEnumType type, long organizationId) throws FactoryException, ArgumentException {
		return getCreateRole(user,user.getName(),type,(T)getHomeRole(type,organizationId),organizationId);
	}

	public <T> T getCreateRole(UserType user, String name, RoleEnumType type, T parent, long organizationId) throws FactoryException, ArgumentException {

		T per = getRoleByName(name,(BaseRoleType)parent,type,organizationId);
		if(per == null){
			logger.info("Role " + type + " " + name + " doesn't exist.  Attempting to create.");
			per = newRoleType(type,user,name,(BaseRoleType)parent);
			if(add((BaseRoleType)per)){
				per = getRoleByName(name,(BaseRoleType)parent,type,organizationId);
			}
		}
		return per;
		
	}
	
	
	/// NOTE: The following methods mimic the preceding behavior for named types
	/// These need to be refactored out as dupes
	///
	
	public PersonRoleType getRootPersonRoleType(long organizationId)  throws FactoryException, ArgumentException
	{
		return getPersonRoleByName(FactoryDefaults.ROOT_OBJECT_NAME, organizationId);
	}
	public PersonRoleType getHomePersonRole(long organizationId) throws FactoryException, ArgumentException
	{

		return getPersonRoleByName(FactoryDefaults.HOME_OBJECT_NAME, getRootPersonRoleType(organizationId), organizationId);
	}
	public PersonRoleType getPersonRole(PersonType account)  throws FactoryException, ArgumentException
	{

		return getPersonRoleByName(account.getName(), getHomePersonRole(account.getOrganizationId()), account.getOrganizationId());
	}

	public PersonRoleType getCreatePersonRole(UserType roleOwner, String roleName, BaseRoleType parent) throws FactoryException, ArgumentException
	{
		PersonRoleType role = getPersonRoleByName(roleName, parent, roleOwner.getOrganizationId());
		if (role == null)
		{
			role = newPersonRole(roleOwner, roleName, parent);
			if (add(role))
			{
				role = getPersonRoleByName(roleName, parent, roleOwner.getOrganizationId());
			}
			else role = null;
		}
		return role;
	}
	public AccountRoleType getRootAccountRoleType(long organizationId)  throws FactoryException, ArgumentException
	{
		return getAccountRoleByName(FactoryDefaults.ROOT_OBJECT_NAME, organizationId);
	}
	public AccountRoleType getHomeAccountRole(long organizationId) throws FactoryException, ArgumentException
	{

		return getAccountRoleByName(FactoryDefaults.HOME_OBJECT_NAME, getRootAccountRoleType(organizationId), organizationId);
	}
	public AccountRoleType getAccountRole(AccountType account)  throws FactoryException, ArgumentException
	{

		return getAccountRoleByName(account.getName(), getHomeAccountRole(account.getOrganizationId()), account.getOrganizationId());
	}

	public AccountRoleType getCreateAccountRole(UserType roleOwner, String roleName, BaseRoleType parent) throws FactoryException, ArgumentException
	{
		AccountRoleType role = getAccountRoleByName(roleName, parent, roleOwner.getOrganizationId());
		if (role == null)
		{
			role = newAccountRole(roleOwner, roleName, parent);
			if (add(role))
			{
				role = getAccountRoleByName(roleName, parent, roleOwner.getOrganizationId());
			}
			else role = null;
		}
		return role;
	}
	public UserRoleType getRootUserRoleType(long organizationId)  throws FactoryException, ArgumentException
	{
		return getUserRoleByName(FactoryDefaults.ROOT_OBJECT_NAME, organizationId);
	}
	public UserRoleType getHomeUserRole(long organizationId) throws FactoryException, ArgumentException
	{

		return getUserRoleByName(FactoryDefaults.HOME_OBJECT_NAME, getRootUserRoleType(organizationId), organizationId);
	}
	public UserRoleType getUserRole(UserType account)  throws FactoryException, ArgumentException
	{

		return getUserRoleByName(account.getName(), getHomeUserRole(account.getOrganizationId()), account.getOrganizationId());
	}
	public UserRoleType getCreateUserRole(UserType roleOwner, String roleName, BaseRoleType parent) throws FactoryException, ArgumentException
	{
		UserRoleType role = getUserRoleByName(roleName, parent, roleOwner.getOrganizationId());
		if (role == null)
		{
			role = newUserRole(roleOwner, roleName, parent);
			if (add(role))
			{
				role = getUserRoleByName(roleName, parent, roleOwner.getOrganizationId());
			}
			else role = null;
		}
		return role;
	}
	@SuppressWarnings("unchecked")
	public <T> T getRoleById(long id, long organizationId) throws FactoryException, ArgumentException
	{
		NameIdType outRole = readCache(id);
		if (outRole != null) return (T)outRole;

		BaseRoleType role = getById(id, organizationId);
		if (role != null)
		{
			String keyName = getCacheKeyName(role);
			addToCache(role, keyName);
			return (T)role;
		}
		return null;
	}
	public PersonRoleType getPersonRoleByName(String name, long organizationId)  throws FactoryException, ArgumentException
	{
		return getPersonRoleByName(name, null, organizationId);
	}
	public PersonRoleType getPersonRoleByName(String name, BaseRoleType parent, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleByName(name, parent, RoleEnumType.PERSON, organizationId);
	}
	
	public AccountRoleType getAccountRoleByName(String name, long organizationId)  throws FactoryException, ArgumentException
	{
		return getAccountRoleByName(name, null, organizationId);
	}
	public AccountRoleType getAccountRoleByName(String name, BaseRoleType parent, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleByName(name, parent, RoleEnumType.ACCOUNT, organizationId);
	}
	public UserRoleType getUserRoleByName(String name, long organizationId)  throws FactoryException, ArgumentException
	{
		return getUserRoleByName(name, null, organizationId);
	}
	public UserRoleType getUserRoleByName(String name, BaseRoleType parent, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleByName(name, parent, RoleEnumType.USER, organizationId);
	}
	public <T> T getRoleByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		return getRoleByName(name, null, organizationId);
	}

	public <T> T getRoleByName(String name, BaseRoleType parent, long organizationId) throws FactoryException, ArgumentException
	{
		return getRoleByName(name, parent, RoleEnumType.UNKNOWN, organizationId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getByNameInParent(String name, String type, long parentId, long organizationId) throws FactoryException, ArgumentException
	{
		if(name == null || organizationId <= 0L) throw new ArgumentException((name == null ? "Name" : "Organization") + " is null");
		String keyName = type + "-" + name + "-" + parentId + "-" + organizationId;
		RoleEnumType roleType = RoleEnumType.valueOf(type);
		NameIdType outRole = readCache(keyName);
		if (outRole != null){
			return (T)outRole;
		}

		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldName(name));
		fields.add(QueryFields.getFieldParent(parentId));
		if (roleType != RoleEnumType.UNKNOWN) fields.add(QueryFields.getFieldRoleType(roleType));
		List<NameIdType> roles = getByField(fields.toArray(new QueryField[0]),organizationId);
		if (!roles.isEmpty())
		{
			addToCache(roles.get(0), keyName);
			return (T)roles.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRoleByName(String name, BaseRoleType parent, RoleEnumType roleType, long organizationId)  throws FactoryException, ArgumentException
	{
		if(name == null || organizationId <= 0L) throw new ArgumentException((name == null ? "Name" : "Organization") + " is null");
		if(parent != null && parent.getId() == null) throw new ArgumentException("Parent id is null");
		long parentId = 0;
		if (parent != null) parentId = parent.getId();
		String keyName = roleType.toString() + "-" + name + "-" + parentId + "-" + organizationId;
		NameIdType outRole = readCache(keyName);
		if (outRole != null){
			return (T)outRole;
		}

		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldName(name));
		fields.add(QueryFields.getFieldParent(parentId));
		if (roleType != RoleEnumType.UNKNOWN) fields.add(QueryFields.getFieldRoleType(roleType));
		List<NameIdType> roles = getByField(fields.toArray(new QueryField[0]),organizationId);
		if (!roles.isEmpty())
		{
			addToCache(roles.get(0), keyName);
			return (T)roles.get(0);
		}
		return null;
	}
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BaseRoleType useMap = (BaseRoleType)map;
		fields.add(QueryFields.getFieldRoleType(useMap.getRoleType()));
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));
	}

	
	public PersonRoleType newPersonRole(UserType user, String roleName) throws FactoryException
	{
		return newPersonRole(user, roleName, null);
	}
	public PersonRoleType newPersonRole(UserType user, String roleName, BaseRoleType parentRole) throws FactoryException
	{
		return newRoleType(RoleEnumType.PERSON, user, roleName, parentRole);
	}

	public AccountRoleType newAccountRole(UserType user, String roleName) throws FactoryException
	{
		return newAccountRole(user, roleName, null);
	}
	public AccountRoleType newAccountRole(UserType user, String roleName, BaseRoleType parentRole) throws FactoryException
	{
		return newRoleType(RoleEnumType.ACCOUNT, user, roleName, parentRole);
	}
	public UserRoleType newUserRole(UserType user, String roleName) throws FactoryException
	{
		return newUserRole(user, roleName, null);
	}
	public UserRoleType newUserRole(UserType user, String roleName, BaseRoleType parentRole) throws FactoryException
	{
		return newRoleType(RoleEnumType.USER, user, roleName, parentRole);
	}
	@SuppressWarnings("unchecked")
	public <T> T newRoleType(RoleEnumType type, UserType owner, String roleName, BaseRoleType parentRole) throws FactoryException
	{
		if (owner == null) throw new FactoryException("Role '" + roleName + "' cannot be created without a valid owner");
		BaseRoleType newRole = newRole(type);
		newRole.setOrganizationId(owner.getOrganizationId());
		newRole.setOwnerId(owner.getId());
		newRole.setName(roleName);
		if (parentRole != null) newRole.setParentId(parentRole.getId());
		return (T)newRole;
	}

	@SuppressWarnings("unchecked")
	protected <T> T newRole(RoleEnumType type) throws FactoryException
	{
		BaseRoleType newRole = null;
		switch (type)
		{
			case ACCOUNT:
				newRole = new AccountRoleType();
				break;
			case USER:
				newRole = new UserRoleType();
				break;
			case PERSON:
				newRole = new PersonRoleType();
				break;
			default:
				throw new FactoryException("Invalid role type: " + type.toString());


		}
		newRole.setNameType(NameEnumType.ROLE);
		newRole.setRoleType(type);
		return (T)newRole;
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
		BaseRoleType role = newRole(RoleEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.ROLETYPE))));
		role.setReferenceId(rset.getLong(Columns.get(ColumnEnumType.REFERENCEID)));
		return super.read(rset, role);
	}
	
	public List<BaseRoleType>  getRoleList(long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleList(RoleEnumType.UNKNOWN, null, startRecord, recordCount, organizationId);
	}
	public List<BaseRoleType>  getRoleList(BaseRoleType parentRole, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getRoleList(RoleEnumType.UNKNOWN, parentRole, startRecord, recordCount, organizationId);
	}
	@Override
	public <T> List<T> listInParent(String type, long parentId, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parentId));
		RoleEnumType roleType = RoleEnumType.valueOf(type);
		if(roleType != RoleEnumType.UNKNOWN) fields.add(QueryFields.getFieldRoleType(roleType));
		return getRoleList(fields.toArray(new QueryField[0]), startRecord, recordCount, organizationId);

	}
	public <T> List<T>  getRoleList(RoleEnumType type, BaseRoleType parent, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		if(type != RoleEnumType.UNKNOWN) fields.add(QueryFields.getFieldRoleType(type));
		fields.add(QueryFields.getFieldParent((parent != null ? parent.getId() : 0L)));
		return getRoleList(fields.toArray(new QueryField[0]), startRecord, recordCount, organizationId);
	}

	public <T> List<T>  getRoleList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause(Columns.get(ColumnEnumType.NAME) + " ASC");
		return getRoleList(fields, instruction, startRecord,recordCount,organizationId);
	}
	public <T> List<T>  getRoleList(QueryField[] fields, ProcessingInstructionType instruction,long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
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
	public <T> List<T> getRoleList(QueryField[] fields, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();

		List<NameIdType> roleList = getByField(fields, instruction, organizationId);
		return convertList(roleList);

	}
	
	public List<BaseRoleType> getRoleListByIds(long[] roleIds, long organizationId) throws FactoryException, ArgumentException
	{
		StringBuilder buff = new StringBuilder();
		List<BaseRoleType> outList = new ArrayList<>();
		for (int i = 0; i < roleIds.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(roleIds[i]);
			if ((i > 0 || roleIds.length == 1) && ((i % BulkFactories.bulkQueryLimit == 0) || i == roleIds.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
				match.setComparator(ComparatorEnumType.ANY);
				List<BaseRoleType> tmpRoleList = getRoleList(new QueryField[] { match }, null, organizationId);
				outList.addAll(tmpRoleList);
				buff.delete(0,  buff.length());
			}
		}
		return outList;
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
		if(role.getParentId().compareTo(0L) == 0 && role.getName().equals(FactoryDefaults.ROOT_OBJECT_NAME)) return "";
		path = path + "/" + role.getName();
		return path;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(UserType user, String type, String path, long organizationId) throws FactoryException, ArgumentException
	{
		try {
			return (T)makePath(user,RoleEnumType.valueOf(type),path,organizationId);
		} catch (DataAccessException e) {
			logger.error(FactoryException.TRACE_EXCEPTION, e);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(String type, String path, long organizationId) throws FactoryException, ArgumentException
	{
		try {
			return (T)findRole(RoleEnumType.valueOf(type),path,organizationId);
		} catch (DataAccessException e) {
			logger.error(FactoryException.TRACE_EXCEPTION, e);
		}
		return null;
	}
	
	public <T> T findRole(RoleEnumType type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(null, type, pathBase,organizationId);
	}
	public <T> T makePath(UserType user, RoleEnumType type, DirectoryGroupType group) throws FactoryException, ArgumentException, DataAccessException{
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(group);
		return makePath(user, type, group.getPath(), group.getOrganizationId());
	}
	
	@Override
	public <T> T makePath(String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(null,type,pathBase,organizationId);
	}
	@Override
	public <T> T makePath(UserType user, String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(user,RoleEnumType.valueOf(type), pathBase, organizationId);
	}
	
	@SuppressWarnings("unchecked")
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
			if(seg.equals(FactoryDefaults.HOME_OBJECT_NAME) && parent != null && parent.getName().equals(FactoryDefaults.ROOT_OBJECT_NAME) && parent.getParentId().compareTo(0L) == 0){
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

