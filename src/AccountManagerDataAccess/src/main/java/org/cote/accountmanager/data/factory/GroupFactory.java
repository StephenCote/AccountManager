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
import java.util.Collections;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BucketGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class GroupFactory  extends NameIdFactory {
	
	protected static final String[] DEFAULT_DIRECTORY_NAMES = new String[]{"Roles","Data","Contacts","Addresses","Persons","Accounts"};
	
	@Override
	public void registerProvider(){
		AuthorizationService.registerAuthorizationProviders(
				FactoryEnumType.GROUP,
				NameEnumType.GROUP,
				FactoryEnumType.GROUPPARTICIPATION
			);
	}

	public GroupFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasUrn = true;
		this.hasObjectId = true;
		this.primaryTableName = "groups";
		this.tableNames.add(primaryTableName);
		this.clusterByParent = true;
		factoryType = FactoryEnumType.GROUP;
		systemRoleNameAdministrator = RoleService.ROLE_DATA_ADMINISTRATOR;
		systemRoleNameReader = RoleService.ROLE_GROUP_READERS;

	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			
		}
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		BaseGroupType t = (BaseGroupType)obj;
		return t.getName() + "-" + t.getGroupType().toString() + "-" + t.getParentId() + "-" + t.getOrganizationId();
	}

	protected void addDefaultGroups(long organizationId)  throws FactoryException, ArgumentException
	{
		addDefaultDirectoryGroups(organizationId);
	}
	protected void addDefaultUserGroups(UserType user, DirectoryGroupType hDir, boolean isBulk, String sessionId) throws FactoryException, ArgumentException{

		for(int i = 0; i < DEFAULT_DIRECTORY_NAMES.length;i++){
			DirectoryGroupType ddir = newDirectoryGroup(user,DEFAULT_DIRECTORY_NAMES[i], hDir, user.getOrganizationId());
			if(isBulk) BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, ddir);
			else add(ddir);
		}
		
	}
	protected void addDefaultDirectoryGroups(long organizationId) throws FactoryException, ArgumentException
	{
		logger.info("Adding default groups to organization " + organizationId);
		if(organizationId == 0L) throw new FactoryException("Invalid organization");
		DirectoryGroupType rootDir = newDirectoryGroup("Root", null, organizationId);
		
		add(rootDir);
		rootDir = getDirectoryByName("Root", organizationId);

		DirectoryGroupType homeDir = newDirectoryGroup("Home", rootDir, organizationId);
		add(homeDir);
	}
	public UserGroupType newUserGroup(String groupName, BaseGroupType parent, long organizationId)
	{
		return newUserGroup(null, groupName, parent, organizationId);
	}
	public UserGroupType newUserGroup(UserType owner, String groupName, BaseGroupType parent, long organizationId)
	{
		if (parent != null) clearGroupCache(parent, false);
		return (UserGroupType)newGroup(owner, groupName, GroupEnumType.USER, parent, organizationId);
	}
	public PersonGroupType newPersonGroup(UserType owner, String groupName, BaseGroupType parent, long organizationId) 
	{
		if (parent != null) clearGroupCache(parent, false);
		return (PersonGroupType)newGroup(owner, groupName, GroupEnumType.PERSON, parent, organizationId);
	}
	public AccountGroupType newAccountGroup(UserType owner, String groupName, BaseGroupType parent, long organizationId)
	{
		if (parent != null) clearGroupCache(parent, false);
		return (AccountGroupType)newGroup(owner, groupName, GroupEnumType.ACCOUNT, parent, organizationId);
	}	
	public DirectoryGroupType newDirectoryGroup(String groupName, BaseGroupType parent, long organizationId)
	{
		return newDirectoryGroup(null, groupName, parent, organizationId);
	}
	public DirectoryGroupType newDirectoryGroup(UserType owner, String groupName, BaseGroupType parent, long organizationId) 
	{
		if (parent != null) clearGroupCache(parent, false);
		return (DirectoryGroupType)newGroup(owner, groupName, GroupEnumType.DATA, parent, organizationId);
	}
	
	public BaseGroupType newGroup(String groupName, long organizationId)
	{
		return newGroup(null,groupName, GroupEnumType.UNKNOWN, null, organizationId);
	}


	public BaseGroupType newGroup(String groupName, GroupEnumType groupType, long organizationId)
	{
		return newGroup(null, groupName, groupType, null, organizationId);
	}
	public BaseGroupType newGroup(UserType owner, String groupName, GroupEnumType groupType, long organizationId)
	{
		return newGroup(owner, groupName, groupType, null, organizationId);
	}
	public BaseGroupType newGroup(UserType owner, String groupName, GroupEnumType groupType, BaseGroupType parent, long organizationId)
	{
		BaseGroupType newGroup = newGroup(groupType);
		if(owner != null) newGroup.setOwnerId(owner.getId());
		newGroup.setOrganizationId(organizationId);
		newGroup.setName(groupName);
		if (parent != null) newGroup.setParentId(parent.getId());
		return newGroup;
	}
	protected BaseGroupType newGroup(GroupEnumType groupType)
	{
		BaseGroupType newGroup = null;
		switch (groupType)
		{
			case BUCKET:
				newGroup = new BucketGroupType();
				break;
			case DATA:
				newGroup = new DirectoryGroupType();
				break;
			case ACCOUNT:
				newGroup = new AccountGroupType();
				break;
			case USER:
				newGroup = new UserGroupType();
				break;
			case PERSON:
				newGroup = new PersonGroupType();
				break;
			default:
				newGroup = new BaseGroupType();
				break;
		}
		newGroup.setGroupType(groupType);
		newGroup.setNameType(NameEnumType.GROUP);
		return newGroup;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		BaseGroupType group = (BaseGroupType)object;
		if (group.getOrganizationId() == null || group.getOrganizationId() <= 0) throw new FactoryException("Cannot add group without Organization.");
		
		/// 2018/05/25 - Why is 'Lifecycles' restricted here?
		///
		if(group.getName().equals("Lifecycles") && group.getParentId().compareTo(0L) == 0) throw new ArgumentException("Invalid parent id");
		try{
			DataRow row = prepareAdd(group, "groups");
			row.setCellValue("grouptype", group.getGroupType().toString());
			row.setCellValue("referenceid", group.getReferenceId());
			if (insertRow(row))
			{
				if(!bulkMode) clearGroupParentCache(group);
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
		BaseGroupType newGroup = newGroup(GroupEnumType.valueOf(rset.getString("grouptype")));
		newGroup.setReferenceId(rset.getLong("referenceid"));
		return super.read(rset, newGroup);
	}
	
	public boolean deleteDirectoryGroup(DirectoryGroupType directory) throws FactoryException, ArgumentException
	{
		if (directory == null) return true;
		populate(directory);
		DirectoryGroupType[] subDirs = getDirectoryGroups(directory).toArray(new DirectoryGroupType[0]);
		for (int i = subDirs.length - 1; i >= 0; i--) deleteDirectoryGroup(subDirs[i]);
		DataFactory dFact = Factories.getFactory(FactoryEnumType.DATA);
		dFact.deleteDataInGroup(directory);
		return delete(directory);
	}
	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		BaseGroupType group = (BaseGroupType)object;
		List<BaseGroupType> subGroups = new ArrayList<>();
		try {
			subGroups = this.getListByParent(GroupEnumType.UNKNOWN, group, 0L, 0, group.getOrganizationId());
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		for(BaseGroupType sub_group : subGroups) delete(sub_group); 
		int deleted = deleteById(group.getId(), group.getOrganizationId());
		clearGroupCache(group, true);
		return (deleted > 0);
	}
	public int deleteGroupsByUser(UserType map) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(map.getId())}, map.getOrganizationId());
		return deleteGroupsByIds(ids, map.getOrganizationId());
	}
	public int deleteGroupsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteParticipations(ids, organizationId);
		}
		return deleted;
	}
	public UserGroupType getUserGroupByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		return (UserGroupType)getGroupByName(name, GroupEnumType.USER, null, organizationId);
	}
	public UserGroupType getUserGroupByName(String name, BaseGroupType parent, long organizationId) throws FactoryException, ArgumentException
	{
		return (UserGroupType)getGroupByName(name, GroupEnumType.USER, parent, organizationId);
	}	
	public DirectoryGroupType getDirectoryByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		return (DirectoryGroupType)getGroupByName(name, GroupEnumType.DATA, null, organizationId);
	}
	public DirectoryGroupType getDirectoryByName(String name, DirectoryGroupType parent, long organizationId) throws FactoryException, ArgumentException
	{
		return (DirectoryGroupType)getGroupByName(name, GroupEnumType.DATA, parent, organizationId);
	}
	public PersonGroupType getPersonByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		return (PersonGroupType)getGroupByName(name, GroupEnumType.PERSON, null, organizationId);
	}
	public PersonGroupType getPersonByName(String name, PersonGroupType parent, long organizationId) throws FactoryException, ArgumentException
	{
		return (PersonGroupType)getGroupByName(name, GroupEnumType.PERSON, parent, organizationId);
	}
	public AccountGroupType getAccountByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		return (AccountGroupType)getGroupByName(name, GroupEnumType.ACCOUNT, null, organizationId);
	}
	public AccountGroupType getAccountByName(String name, AccountGroupType parent, long organizationId) throws FactoryException, ArgumentException
	{
		return (AccountGroupType)getGroupByName(name, GroupEnumType.ACCOUNT, parent, organizationId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getByNameInParent(String name, String type, long parentId, long organizationId) throws FactoryException, ArgumentException
	{
		String keyName = name + "-" + type + "-" + parentId + "-" + organizationId;
		GroupEnumType groupType = GroupEnumType.valueOf(type);
		NameIdType outGroup = readCache(keyName);
		if (outGroup != null) return (T)outGroup;
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldName(name));
		fields.add(QueryFields.getFieldParent(parentId));
		if(groupType != GroupEnumType.UNKNOWN) fields.add(QueryFields.getFieldGroupType(groupType));
		
		List<NameIdType> groups = getByField(fields.toArray(new QueryField[0]), organizationId);

		if (!groups.isEmpty())
		{
			addToCache(groups.get(0),keyName);
			return (T)groups.get(0);
		}
		return null;
	}
	public BaseGroupType getGroupByName(String name, GroupEnumType groupType, BaseGroupType parent, long organizationId) throws FactoryException, ArgumentException
	{
		String keyName = name + "-" + groupType + "-" + (parent == null ? 0 : parent.getId()) + "-" + organizationId;
	
		NameIdType outGroup = readCache(keyName);
		if (outGroup != null) return (BaseGroupType)outGroup;
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldName(name));
		fields.add(QueryFields.getFieldParent(( parent != null ? parent.getId() : 0)));
		if(groupType != GroupEnumType.UNKNOWN) fields.add(QueryFields.getFieldGroupType(groupType));
		
		List<NameIdType> groups = getByField(fields.toArray(new QueryField[0]), organizationId);

		if (!groups.isEmpty())
		{
			addToCache(groups.get(0),keyName);
			return (BaseGroupType)groups.get(0);
		}
		return null;
	}

	public List<UserGroupType> getUserGroups(UserGroupType parent) throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroupType(GroupEnumType.USER.toString()));
		return getUserGroups(fields, parent.getOrganizationId());
	}
	public List<UserGroupType> getUserGroups(List<QueryField> fields, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> inList = getByField(fields.toArray(new QueryField[0]), organizationId);
		return convertList(inList);

	}

	public UserGroupType getUserGroupById(long id, long organizationId) throws ArgumentException
	{
		return (UserGroupType)getGroupById(id, GroupEnumType.USER, organizationId);
	}
	
	
	public List<DirectoryGroupType> getDirectoryGroups(DirectoryGroupType parent) throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroupType(GroupEnumType.DATA.toString()));
		return getDirectoryGroups(fields, parent.getOrganizationId());
	}
	public List<DirectoryGroupType> getDirectoryGroups(List<QueryField> fields, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> inList = getByField(fields.toArray(new QueryField[0]), organizationId);
		return convertList(inList);

	}
	
	public DirectoryGroupType getDirectoryById(long id, long organizationId) throws ArgumentException
	{
		return (DirectoryGroupType)getGroupById(id, GroupEnumType.DATA, organizationId);
	}
	
	
	
	public List<PersonGroupType> getPersonGroups(PersonGroupType parent) throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroupType(GroupEnumType.PERSON.toString()));
		return getPersonGroups(fields, parent.getOrganizationId());
	}
	public List<PersonGroupType> getPersonGroups(List<QueryField> fields, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> inList = getByField(fields.toArray(new QueryField[0]), organizationId);
		return convertList(inList);

	}
	
	public PersonGroupType getPersonById(long id, long organizationId) throws ArgumentException
	{
		return (PersonGroupType)getGroupById(id, GroupEnumType.PERSON, organizationId);
	}
	
	
	
	public List<AccountGroupType> getAccountGroups(AccountGroupType parent) throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroupType(GroupEnumType.ACCOUNT.toString()));
		return getAccountGroups(fields, parent.getOrganizationId());
	}
	public List<AccountGroupType> getAccountGroups(List<QueryField> fields, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> inList = getByField(fields.toArray(new QueryField[0]), organizationId);
		return convertList(inList);

	}
	
	public AccountGroupType getAccountById(long id, long organizationId) throws ArgumentException
	{
		return (AccountGroupType)getGroupById(id, GroupEnumType.ACCOUNT, organizationId);
	}
	
	
	public BaseGroupType getGroupById(long id, long organizationId) throws ArgumentException
	{
		return getGroupById(id, GroupEnumType.UNKNOWN, organizationId);
	}
	public BaseGroupType getGroupById(long id, GroupEnumType groupType, long organizationId) throws ArgumentException{

		NameIdType outGroup = readCache(id);
		if (outGroup != null) return (BaseGroupType)outGroup;
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldId(id));
		if(groupType != GroupEnumType.UNKNOWN) fields.add(QueryFields.getFieldGroupType(groupType));
		List<NameIdType> groups = null;
		try {
			groups = getByField(fields.toArray(new QueryField[0]), organizationId);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if (groups != null && !groups.isEmpty())
		{
			BaseGroupType group = (BaseGroupType)groups.get(0);
			String keyName = group.getName() + "-" + group.getGroupType().toString() + "-" + group.getParentId() + "-" + organizationId;
			addToCache(group,keyName);
			return (BaseGroupType)groups.get(0);
		}
		return null;
	}
	
	protected void clearGroupParentCache(BaseGroupType group) throws ArgumentException
	{
		if (group == null) return;
		if (group.getParentId() > 0 && haveCacheId(group.getParentId()))
		{
			clearGroupCache(getGroupById(group.getParentId(), group.getOrganizationId()), false);
		}
	}
	protected void clearGroupCache(BaseGroupType group, boolean clearParent) {
		if (group == null) return;
		if (clearParent)
			try {
				clearGroupParentCache(group);
			} catch (ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		String keyName = group.getName() + "-" + group.getGroupType().toString() + "-" + group.getParentId() + "-" + group.getOrganizationId();
		
		removeFromCache(group, keyName);
	}

	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		BaseGroupType group = (BaseGroupType)object;
		try {
			clearGroupParentCache(group);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return super.update(group);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BaseGroupType useGroup = (BaseGroupType)map;
		fields.add(QueryFields.getFieldGroupType(useGroup.getGroupType().toString()));
		fields.add(QueryFields.getFieldReferenceId(useGroup.getReferenceId()));
	}
	
	@Override
	public <T> void denormalize(T object) throws ArgumentException, FactoryException{
		super.denormalize(object);
		if(object == null){
			throw new ArgumentException("Null object");
		}
		
		BaseGroupType obj = (BaseGroupType)object;
		if(obj.getPath() != null) return;
		obj.setPath(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getPath(obj));
	}
	
	
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		BaseGroupType group = (BaseGroupType)obj;
		boolean valid = isValid(group);
		if (group.getPopulated()) return;
	
        group.setPopulated(true);
        if(valid) updateToCache(group);
	}

	public DirectoryGroupType getRootDirectory(long organizationId) throws FactoryException, ArgumentException
	{
		return getDirectoryByName("Root", organizationId);
	}
	public DirectoryGroupType getHomeDirectory(long organizationId) throws FactoryException, ArgumentException
	{

		return getDirectoryByName("Home", getRootDirectory(organizationId),organizationId);
	}
	public DirectoryGroupType getPersonsDirectory(long organizationId) throws FactoryException, ArgumentException
	{

		return getDirectoryByName("Persons", getRootDirectory(organizationId),organizationId);
	}
	public DirectoryGroupType getUserDirectory(UserType user) throws FactoryException, ArgumentException
	{

		return getDirectoryByName(user.getName(), getHomeDirectory(user.getOrganizationId()),user.getOrganizationId());
	}
	public DirectoryGroupType getCreateUserDirectory(UserType user, String directoryName) throws FactoryException, ArgumentException
	{
		return getCreateDirectory(user, directoryName, getUserDirectory(user), user.getOrganizationId());
	}
	public DirectoryGroupType getCreateDirectory(UserType owner, String directoryName, DirectoryGroupType parent, long organizationId) throws FactoryException, ArgumentException
	{
		if(parent != null) populate(parent);
		DirectoryGroupType vdir = getDirectoryByName(directoryName, parent, organizationId);
		if (vdir == null)
		{
			vdir = newDirectoryGroup(owner, directoryName, parent, organizationId);
			if (add(vdir))
			{
				vdir = getDirectoryByName(directoryName, parent, organizationId);
			}
			else vdir = null;
		}
		return vdir;
	}
	public UserGroupType getCreateUserGroup(UserType owner, String directoryName, BaseGroupType parent, long organizationId) throws FactoryException, ArgumentException
	{
		UserGroupType vdir = getUserGroupByName(directoryName, parent, organizationId);
		if (vdir == null)
		{
			vdir = newUserGroup(owner, directoryName, parent, organizationId);
			if (add(vdir))
			{
				vdir = getUserGroupByName(directoryName, parent, organizationId);
			}
			else vdir = null;
		}
		return vdir;
	}
	public DirectoryGroupType getCreatePath(UserType user, String path, long organizationId) throws FactoryException, ArgumentException
	{
		DirectoryGroupType dir = (DirectoryGroupType)findGroup(user, GroupEnumType.DATA, path, organizationId);
		if(dir == null) logger.debug("Make path: " + path + " in organization " + organizationId + " relative to user " + user.getName());
		if(dir == null && makePath(user, GroupEnumType.DATA,path, organizationId)){
			dir = (DirectoryGroupType)findGroup(user, GroupEnumType.DATA, path, organizationId);
		}
		return dir;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(UserType user, String type, String path, long organizationId) throws FactoryException, ArgumentException
	{
		return (T)findGroup(user,GroupEnumType.valueOf(type), path, organizationId);
	}
	
	public BaseGroupType findGroup(UserType user, GroupEnumType groupType, String path, long organizationId) throws FactoryException, ArgumentException
	{
		
		BaseGroupType outDir = null;
		if (path == null || path.length() == 0) throw new FactoryException("Invalid path");

		String[] paths = path.split("/");

		BaseGroupType nestedGroup = null;

		String name = null;
		logger.info("Find group by path: '" + path + "'");
		if (paths.length == 0 || path.equals("/"))
		{
			return getRootDirectory(organizationId);
		}
		if(paths.length == 0) throw new FactoryException("Invalid path list from '" + path + "'");

		for(int i = 0; i < paths.length; i++)
		{
			name = paths[i];
			
			if (name.length() == 0 && i == 0)
			{
				nestedGroup = getRootDirectory(organizationId);
				if (paths.length == 1)
				{
					break;
				}
				// name = paths[++i];
				i++;
			}
			else if (name.equals("~") && user != null)
			{
				Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
				if(user.getHomeDirectory() == null) throw new FactoryException("User home directory not populated");
				nestedGroup = user.getHomeDirectory();
				if (paths.length == 1) break;
				// name = paths[++i];
				i++;
			}
			nestedGroup = getGroupByName(paths[i], groupType,nestedGroup, organizationId);
		}
		outDir = nestedGroup;

		return outDir;
	}
	
	@Override
	public <T> T makePath(String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(null,type,pathBase,organizationId);
	}
	
	@Override
	public <T> T makePath(UserType user, String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		if(makePath(user,GroupEnumType.valueOf(type), pathBase, organizationId)){
			return find(user,type,pathBase,organizationId);
		}
		return null;
	}
	public boolean makePath(UserType user, GroupEnumType groupType, String path, long organizationId) throws FactoryException, ArgumentException
	{

		boolean ret = false;
		if (path == null || path.length() == 0) throw new FactoryException("Invalid path value");

		// Check if the path exists
		//
		DirectoryGroupType checkGroup = (DirectoryGroupType)findGroup(user, groupType, path, organizationId);
		if (checkGroup != null) return false;

		String[] paths = path.split("/");
		if (paths.length == 0 || path.equals("/"))
		{
			return false;
		}
		DirectoryGroupType nestedGroup = null;
		DirectoryGroupType refGroup = null;
		String name =  null;
		for (int i = 0; i < paths.length; i++)
		{
			name = paths[i];
			if (name.length() == 0 && i == 0)
			{
				refGroup = getRootDirectory(organizationId);
				if (paths.length == 1)
				{
					throw new FactoryException("Invalid group name in makepath");
				}
				name = paths[++i];
			}
			else if (name.equals("~") && user != null)
			{
				Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
				refGroup = user.getHomeDirectory();
				if (paths.length == 1)
				{
					throw new FactoryException("MakeDirectoryPath: Cannot create user home directory.  This can only be created through setAccountDefaults");
				}
				name = paths[++i];
			}

			if (refGroup == null)
			{
				logger.error("Invalid reference group for path section: '" + name + "' at index " + i);
				throw new FactoryException("MakeDirectoryPath: Invalid directory reference id");
			}

			nestedGroup = getDirectoryByName(name, refGroup, organizationId);
			logger.debug("Pathing " + name + " - " + (nestedGroup == null ? "NEW" : nestedGroup.getId()));
			if (nestedGroup == null)
			{
				nestedGroup = newDirectoryGroup(user, name, refGroup, organizationId);
				if (add(nestedGroup))
				{
					nestedGroup = getDirectoryByName(name, refGroup, organizationId);
					refGroup = nestedGroup;
					ret = true;
				}
				else
				{
					throw new FactoryException("MakeDirectoryPath: Unable to create directory: '" + name + "'");
				}
			}
			else
			{
				refGroup = nestedGroup;
				ret = true;
			}

		}

		return ret;
	}
	public String getPath(long leafGroupId, long organizationId) throws FactoryException, ArgumentException
	{
		return getPath(leafGroupId, organizationId,true);
	}
	public String getPath(long leafGroupId, long organizationId,boolean populate) throws FactoryException, ArgumentException
	{

		BaseGroupType group = getGroupById(leafGroupId,organizationId);
		return getPath(group, populate);
	}
	public String getPath(BaseGroupType leafGroup) throws FactoryException, ArgumentException
	{
		return getPath(leafGroup, true);
	}
	protected String getPath(BaseGroupType leafGroup, boolean populate) throws FactoryException, ArgumentException
	{
		if(leafGroup == null) return null;
		List<BaseGroupType> groupList = new ArrayList<>();
		groupList.add(leafGroup);
		if(populate) populate(leafGroup);
		while (leafGroup.getParentId() != null && leafGroup.getParentId() > 0L)
		{
			BaseGroupType parentGroup = getById(leafGroup.getParentId(),leafGroup.getOrganizationId());
			groupList.add(parentGroup);
			leafGroup = parentGroup;
			populate(leafGroup);
		}
		Collections.reverse(groupList);
		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < groupList.size(); i++)
		{
			BaseGroupType group = groupList.get(i);
			if (group.getName().equals("Root") && group.getParentId() == 0) continue;
			buff.append("/" + group.getName());
		}
		return buff.toString();
	}
	public int getCount(BaseGroupType group) throws FactoryException
	{
		return getCountByField(this.getDataTables().get(0), new QueryField[]{QueryFields.getFieldParent(group.getId())}, group.getOrganizationId());
	}
	
	@Override
	public <T> List<T> listInParent(String type, long parentId, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parentId));
		GroupEnumType groupType = GroupEnumType.valueOf(type);
		if(groupType != GroupEnumType.UNKNOWN) fields.add(QueryFields.getFieldGroupType(groupType));
		return getList(fields.toArray(new QueryField[0]), startRecord, recordCount, organizationId);

	}

	public <T> List<T>  getListByParent(GroupEnumType groupType, BaseGroupType parent, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		if(groupType != GroupEnumType.UNKNOWN) fields.add(QueryFields.getFieldGroupType(groupType));
		return getList(fields.toArray(new QueryField[0]), startRecord, recordCount, organizationId);
	}
	
	public <T> List<T>  getListByParent(GroupEnumType groupType, BaseGroupType parent, ProcessingInstructionType instruction, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		if(groupType != GroupEnumType.UNKNOWN) fields.add(QueryFields.getFieldGroupType(groupType));
		return getList(fields.toArray(new QueryField[0]), instruction, startRecord, recordCount,organizationId);
	}
	public <T> List<T>  getList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getList(fields, instruction, startRecord,recordCount,organizationId);
	}
	public <T> List<T>  getList(QueryField[] fields, ProcessingInstructionType instruction,long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && !instruction.getPaginate())
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getList(fields, instruction, organizationId);
	}
	public <T> List<T> getList(QueryField[] fields, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();

		List<NameIdType> dataList = getByField(fields, instruction, organizationId);
		return convertList(dataList);
	}
	

}
