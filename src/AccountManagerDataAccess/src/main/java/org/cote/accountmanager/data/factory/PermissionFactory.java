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
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;

public class PermissionFactory extends NameIdFactory {
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.PERMISSION, PermissionFactory.class); }
	public PermissionFactory(){
		super();
		this.clusterByParent = true;
		this.scopeToOrganization = true;
		this.hasParentId = true;
		this.hasOwnerId = true;
		this.hasName = true;
		this.hasObjectId = true;
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
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		BasePermissionType perm = (BasePermissionType)obj;
		if(perm.getPopulated()) return;
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
		return update(permission);
	}

	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		BasePermissionType new_permission = (BasePermissionType)object;
		if (new_permission.getOrganizationId() <= 0L) throw new FactoryException("Cannot add permission to invalid organization");
		DataRow row = prepareAdd(new_permission, "permissions");
		try{
			row.setCellValue("permissiontype",new_permission.getPermissionType().toString());
			row.setCellValue("referenceid",new_permission.getReferenceId());
		}
		catch(DataAccessException e){
			throw new FactoryException(e.getMessage());
		}
		return insertRow(row);
	}

	public <T> T getPermissionById(long id, long organizationId)  throws FactoryException, ArgumentException
	{
		T out_perm = readCache(id);
		if (out_perm != null) return out_perm;

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
	@Override
	public <T> T getByNameInParent(String name, String type, long parent_id, long organizationId) throws FactoryException, ArgumentException
	{
		String key_name = name + "-" + type + "-" + parent_id + "-" + organizationId;
		PermissionEnumType perType = PermissionEnumType.valueOf(type);
		T out_perm = readCache(key_name);
		if (out_perm != null) return out_perm;

		List<QueryField> Fields = new ArrayList<QueryField>();
		Fields.add(QueryFields.getFieldName(name));
		Fields.add(QueryFields.getFieldParent(parent_id));
		if (perType != PermissionEnumType.UNKNOWN) Fields.add(QueryFields.getFieldPermissionType(perType));
		List<NameIdType> perms = getByField(Fields.toArray(new QueryField[0]),organizationId);
		if (perms.size() > 0)
		{
			addToCache(perms.get(0),key_name);
			return (T)perms.get(0);
		}
		return null;
	}
	public <T> T getPermissionByName(String name, PermissionEnumType type, BasePermissionType parent, long organizationId) throws FactoryException, ArgumentException
	{
		long parent_id = 0;
		if (parent != null) parent_id = parent.getId();

		String key_name = name + "-" + type.toString() + "-" + parent_id + "-" + organizationId;
		T out_perm = readCache(key_name);
		if (out_perm != null) return out_perm;

		//List<NameIdType> perms = getByNameInGroup(name,organizationId);
		List<QueryField> Fields = new ArrayList<QueryField>();
		Fields.add(QueryFields.getFieldName(name));
		Fields.add(QueryFields.getFieldParent(parent_id));
		if (type != PermissionEnumType.UNKNOWN) Fields.add(QueryFields.getFieldPermissionType(type));
		List<NameIdType> perms = getByField(Fields.toArray(new QueryField[0]),organizationId);
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
	
	public <T> T getRootPermission(PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(null,"Root",type,null,organizationId);
	}
	public <T> T getHomePermission(PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(null,"Home",type,(T)getRootPermission(type,organizationId),organizationId);
	}
	public <T> T getUserPermission(UserType user,PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return getCreatePermission(user,user.getName(),type,(T)getHomePermission(type,organizationId),organizationId);
	}

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
	@Override
	public <T> T find(UserType user, String type, String path, long organizationId) throws FactoryException, ArgumentException
	{
		try {
			return (T)findPermission(PermissionEnumType.valueOf(type),path,organizationId);
		} catch (DataAccessException e) {
			logger.error("Trace", e);
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

	
	public BasePermissionType newPermission(UserType owner, String permission_name, PermissionEnumType type, BasePermissionType parent, long organizationId)
	{
		BasePermissionType new_perm = newPermission(permission_name, type, parent, organizationId);
		new_perm.setOwnerId((owner != null ? owner.getId() : 0L));
		return new_perm;
	}
	
	public BasePermissionType newPermission(String permission_name, PermissionEnumType type, BasePermissionType parent, long organizationId)
	{
		BasePermissionType new_perm = newPermission(permission_name, type, organizationId);
		new_perm.setParentId((parent != null ? parent.getId() : 0L));
		return new_perm;
	}
	
	public BasePermissionType newPermission(String permission_name, PermissionEnumType type, long organizationId)
	{
		BasePermissionType new_perm = newPermission(type);
		new_perm.setOrganizationId(organizationId);
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

	
	public <T> List<T> listInParent(String type, long parentId, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		PermissionEnumType perType = PermissionEnumType.valueOf(type);
		if(perType.equals(PermissionEnumType.UNKNOWN) == false) fields.add(QueryFields.getFieldPermissionType(perType));
		fields.add(QueryFields.getFieldParent(parentId));
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");

		return getPermissionList(fields.toArray(new QueryField[0]), instruction, startRecord, recordCount, organizationId);		
	}
	
	public <T> List<T>  getPermissionList(PermissionEnumType type, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getPermissionList(null, type, startRecord, recordCount, organizationId);
	}	
	public <T> List<T>  getPermissionList(BasePermissionType parent, PermissionEnumType type, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<QueryField>();
		if(type != PermissionEnumType.UNKNOWN) fields.add(QueryFields.getFieldPermissionType(type));
		fields.add(QueryFields.getFieldParent((parent != null ? parent.getId() : 0L)));
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");

		return getPermissionList(fields.toArray(new QueryField[0]), instruction, startRecord, recordCount, organizationId);
	}
	public <T> List<T>  getPermissionList(QueryField[] fields, ProcessingInstructionType instruction,long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0 && recordCount > 0 && instruction.getPaginate() == false)
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
		List<NameIdType> PermissionList = getByField(fields, instruction, organizationId);
		return convertList(PermissionList);
	}
	public int deletePermissionsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}


}
