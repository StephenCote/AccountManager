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
import org.cote.accountmanager.objects.AccountPermissionType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DataPermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ObjectPermissionType;
import org.cote.accountmanager.objects.PersonPermissionType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.RolePermissionType;
import org.cote.accountmanager.objects.UserPermissionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;

public class PermissionFactory extends NameIdFactory {

	@Override
	public void registerProvider(){
		AuthorizationService.registerAuthorizationProviders(
				FactoryEnumType.PERMISSION,
				NameEnumType.PERMISSION,
				FactoryEnumType.PERMISSIONPARTICIPATION
			);
	}
	
	public PermissionFactory(){
		super();
		this.clusterByParent = true;
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.hasObjectId = true;
		this.hasUrn = true;
		this.primaryTableName = "permissions";
		this.tableNames.add(primaryTableName);
		this.factoryType = FactoryEnumType.PERMISSION;
		systemRoleNameReader = RoleService.ROLE_PERMISSION_READERS;
		systemRoleNameAdministrator = RoleService.ROLE_PERMISSION_ADMINISTRATORS;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// retrict column names
		}
	}
	
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		BasePermissionType perm = (BasePermissionType)obj;
		if(perm.getPopulated().booleanValue()) return;
		perm.setPopulated(true);
		updateToCache(perm);
	}
	

	@Override
	public <T> void normalize(T object) throws ArgumentException, FactoryException{
		super.normalize(object);
		if(object == null){
			throw new ArgumentException("Null object");
		}
		BasePermissionType obj = (BasePermissionType)object;
		if(obj.getParentPath() == null || obj.getParentPath().length() == 0 || obj.getPermissionType() == PermissionEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object parent path or type");	
		}
		if(obj.getParentId().compareTo(0L) != 0) return;
		BasePermissionType dir = null;
		try{
			dir = findPermission(obj.getPermissionType(), obj.getParentPath(), obj.getOrganizationId());
			if(dir == null){
				throw new ArgumentException("Invalid parent path '" + obj.getParentPath() + "'");
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
		BasePermissionType obj = (BasePermissionType)object;
		if(obj.getParentId().compareTo(0L) == 0){
			logger.warn("Root level permission does not have a path");
			return;	
		}
		if(obj.getParentPath() != null) return;
		BasePermissionType parent = getById(obj.getParentId(), obj.getOrganizationId());
		obj.setParentPath(getPermissionPath(parent));
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		BasePermissionType t = (BasePermissionType)obj;
		return t.getName() + "-" + t.getPermissionType().toString() + "-" + t.getParentId() + "-" + t.getOrganizationId();
	}
	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		BasePermissionType permission = (BasePermissionType)object;
		removeFromCache(permission);
		int deleted = deleteById(permission.getId(), permission.getOrganizationId());
		return (deleted > 0);
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		BasePermissionType permission = (BasePermissionType)object;
		removeFromCache(permission);
		return super.update(permission);
	}

	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		BasePermissionType newPermission = (BasePermissionType)object;
		if (newPermission.getOrganizationId() <= 0L) throw new FactoryException("Cannot add permission to invalid organization");
		DataRow row = prepareAdd(newPermission, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.PERMISSIONTYPE),newPermission.getPermissionType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.REFERENCEID),newPermission.getReferenceId());
		}
		catch(DataAccessException e){
			throw new FactoryException(e.getMessage());
		}
		return insertRow(row);
	}

	@SuppressWarnings("unchecked")
	public <T> T getPermissionById(long id, long organizationId)  throws FactoryException, ArgumentException
	{
		T outPerm = readCache(id);
		if (outPerm != null) return outPerm;

		BasePermissionType perm = getById(id, organizationId);
		if (perm != null)
		{
			addToCache(perm);
			return (T)perm;
		}
		return null;
	}
	
	public <T> T getPermissionByName(String name, PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException
	{
		return getPermissionByName(name, type, null, organizationId);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getByNameInParent(String name, String type, long parentId, long organizationId) throws FactoryException, ArgumentException
	{
		String keyName = name + "-" + type + "-" + parentId + "-" + organizationId;
		PermissionEnumType perType = PermissionEnumType.valueOf(type);
		T outPerm = readCache(keyName);
		if (outPerm != null) return outPerm;

		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldName(name));
		fields.add(QueryFields.getFieldParent(parentId));
		if (perType != PermissionEnumType.UNKNOWN) fields.add(QueryFields.getFieldPermissionType(perType));
		List<NameIdType> perms = getByField(fields.toArray(new QueryField[0]),organizationId);
		if (!perms.isEmpty())
		{
			addToCache(perms.get(0),keyName);
			return (T)perms.get(0);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public <T> T getPermissionByName(String name, PermissionEnumType type, BasePermissionType parent, long organizationId) throws FactoryException, ArgumentException
	{
		long parentId = 0;
		if (parent != null) parentId = parent.getId();

		String keyName = name + "-" + type.toString() + "-" + parentId + "-" + organizationId;
		T outPerm = readCache(keyName);
		if (outPerm != null) return outPerm;

		List<QueryField> Fields = new ArrayList<>();
		Fields.add(QueryFields.getFieldName(name));
		Fields.add(QueryFields.getFieldParent(parentId));
		if (type != PermissionEnumType.UNKNOWN) Fields.add(QueryFields.getFieldPermissionType(type));
		List<NameIdType> perms = getByField(Fields.toArray(new QueryField[0]),organizationId);
		if (!perms.isEmpty())
		{
			addToCache(perms.get(0),keyName);
			return (T)perms.get(0);
		}
		return null;
	}
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BasePermissionType useMap = (BasePermissionType)map;
		fields.add(QueryFields.getFieldPermissionType(useMap.getPermissionType()));
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));

	}
	
	public <T> T getRootPermission(PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(Factories.getAdminUser(organizationId),"Root",type,null,organizationId);
	}
	@SuppressWarnings("unchecked")
	public <T> T getHomePermission(PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(Factories.getAdminUser(organizationId),"Home",type,(T)getRootPermission(type,organizationId),organizationId);
	}
	@SuppressWarnings("unchecked")
	public <T> T getUserPermission(UserType user,PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(user,user.getName(),type,(T)getHomePermission(type,organizationId),organizationId);
	}

	@SuppressWarnings("unchecked")
	public <T> T getCreatePermission(UserType user, String name, PermissionEnumType type, T parent, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		T per = (T)getPermissionByName(name,type,(BasePermissionType)parent,organizationId);
		if(per == null){
			per = (T)newPermission(user,name,type,(BasePermissionType)parent,organizationId);
			if(add((BasePermissionType)per)){
				per = (T)getPermissionByName(name,type,(BasePermissionType)parent,organizationId);
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
			path = getPermissionPath((BasePermissionType)getPermissionById(per.getParentId(),per.getOrganizationId()));
		}
		if(per.getParentId().compareTo(0L) == 0 && per.getName().equals("Root")) return "";
		path = path + "/" + per.getName();
		return path;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(UserType user, String type, String path, long organizationId) throws FactoryException, ArgumentException
	{
		try {
			return (T)makePath(user,PermissionEnumType.valueOf(type),path,organizationId);
		} catch (DataAccessException e) {
			logger.error(FactoryException.TRACE_EXCEPTION, e);
		}
		return null;
	}
	public <T> T findPermission(PermissionEnumType type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(null, type, pathBase,organizationId);
	}
	public <T> T makePath(UserType user, PermissionEnumType type, DirectoryGroupType group) throws FactoryException, ArgumentException, DataAccessException{
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(group);
		return makePath(user, type, group.getPath(), group.getOrganizationId());
	}
	@Override
	public <T> T makePath(String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(null,type,pathBase,organizationId);
	}
	@Override
	public <T> T makePath(UserType user, String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return makePath(user,PermissionEnumType.valueOf(type), pathBase, organizationId);
	}
	@SuppressWarnings("unchecked")
	public <T> T makePath(UserType user, PermissionEnumType type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		String[] path = pathBase.split("/");
		BasePermissionType parent = null;
		T per = null;
		for(int i = 0; i < path.length;i++){
			String seg = path[i];
			if(seg.equals("")){
				parent = getRootPermission(type,  organizationId);
				continue;
			}
			if(seg.equals("Home") && parent != null && parent.getName().equals("Root") && parent.getParentId().compareTo(0L) == 0){
				parent = getHomePermission(type, organizationId);
				continue;
			}
			if(user != null) per = getCreatePermission(user, seg, type, (T)parent, organizationId);
			else per = getPermissionByName(seg,type,(BasePermissionType)parent,organizationId);
			if(per == null){
				logger.warn("Failed to find permission '" + seg + "' in " + (parent == null ? "Null Parent":parent.getName() + " " + parent.getPermissionType().toString()) + " from " + pathBase);
				return null;
			}
			parent = (BasePermissionType)per;
		}
		return per;
	}

	
	public BasePermissionType newPermission(UserType owner, String permissionName, PermissionEnumType type, BasePermissionType parent, long organizationId)
	{
		BasePermissionType newPerm = newPermission(permissionName, type, parent, organizationId);
		newPerm.setOwnerId((owner != null ? owner.getId() : 0L));
		return newPerm;
	}
	
	private BasePermissionType newPermission(String permissionName, PermissionEnumType type, BasePermissionType parent, long organizationId)
	{
		BasePermissionType newPerm = newPermission(permissionName, type, organizationId);
		newPerm.setParentId((parent != null ? parent.getId() : 0L));
		return newPerm;
	}
	
	private BasePermissionType newPermission(String permissionName, PermissionEnumType type, long organizationId)
	{
		BasePermissionType newPerm = newPermission(type);
		newPerm.setOrganizationId(organizationId);
		newPerm.setName(permissionName);
		return newPerm;
	}
	
	protected BasePermissionType newPermission(PermissionEnumType Type)
	{
		BasePermissionType newPerm = null;
		switch (Type)
		{
			case OBJECT:
				newPerm = new ObjectPermissionType();
				break;
			case APPLICATION:
				newPerm = new ApplicationPermissionType();
				break;
			case DATA:
				newPerm = new DataPermissionType();
				break;
			case ROLE:
				newPerm = new RolePermissionType();
				break;
			case ACCOUNT:
				newPerm = new AccountPermissionType();
				break;
			case USER:
				newPerm = new UserPermissionType();
				break;
			case PERSON:
				newPerm = new PersonPermissionType();
				break;

			default:
				newPerm = new BasePermissionType();
				break;
		}
		newPerm.setPermissionType(Type);
		newPerm.setNameType(NameEnumType.PERMISSION);
		return newPerm;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		BasePermissionType newPer = newPermission(PermissionEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.PERMISSIONTYPE))));
		newPer.setReferenceId(rset.getLong(Columns.get(ColumnEnumType.REFERENCEID)));
		return super.read(rset, newPer);
	}

	
	public <T> List<T> listInParent(String type, long parentId, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		PermissionEnumType perType = PermissionEnumType.valueOf(type);
		if(!perType.equals(PermissionEnumType.UNKNOWN)) fields.add(QueryFields.getFieldPermissionType(perType));
		fields.add(QueryFields.getFieldParent(parentId));
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause(Columns.get(ColumnEnumType.NAME) + " ASC");

		return getPermissionList(fields.toArray(new QueryField[0]), instruction, startRecord, recordCount, organizationId);		
	}
	
	public <T> List<T>  getPermissionList(PermissionEnumType type, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getPermissionList(null, type, startRecord, recordCount, organizationId);
	}	
	public <T> List<T>  getPermissionList(BasePermissionType parent, PermissionEnumType type, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		if(type != PermissionEnumType.UNKNOWN) fields.add(QueryFields.getFieldPermissionType(type));
		fields.add(QueryFields.getFieldParent((parent != null ? parent.getId() : 0L)));
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause(Columns.get(ColumnEnumType.NAME) + " ASC");

		return getPermissionList(fields.toArray(new QueryField[0]), instruction, startRecord, recordCount, organizationId);
	}
	public <T> List<T>  getPermissionList(QueryField[] fields, ProcessingInstructionType instruction,long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && !instruction.getPaginate().booleanValue())
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getPermissionList(fields, instruction, organizationId);
	}
	public <T> List<T> getPermissionList(QueryField[] fields, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();
		List<NameIdType> permissionList = getByField(fields, instruction, organizationId);
		return convertList(permissionList);
	}
	public int deletePermissionsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}


}
