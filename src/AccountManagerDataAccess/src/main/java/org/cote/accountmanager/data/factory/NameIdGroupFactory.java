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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;




public abstract class NameIdGroupFactory extends NameIdFactory implements INameIdGroupFactory{
	public static final Logger logger = LogManager.getLogger(NameIdGroupFactory.class);
	public NameIdGroupFactory(){
		super();
		this.clusterByGroup = true;
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
		this.hasUrn = true;
		this.hasObjectId = true;
		systemRoleNameAdministrator = RoleService.ROLE_DATA_ADMINISTRATOR;
		systemRoleNameReader = RoleService.ROLE_DATA_READERS;
	}
	
	@Override
	public <T> void normalize(T object) throws ArgumentException, FactoryException{
		super.normalize(object);
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdDirectoryGroupType obj = (NameIdDirectoryGroupType)object;
		
		/// 2017/02/07 - Why is this returning out?
		///
		
		if(obj.getGroupPath() == null || obj.getGroupPath().length() == 0){
			logger.debug("Group path not defined");
			return;
		}
		BaseGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null,GroupEnumType.DATA, obj.getGroupPath(), obj.getOrganizationId());
		if(dir == null){
			throw new ArgumentException("Invalid group path '" + obj.getGroupPath() + "' in organization '" + obj.getOrganizationPath() + "' #" + obj.getOrganizationId());
		}
		obj.setGroupId(dir.getId());
	}
	
	@Override
	public <T> void denormalize(T object) throws ArgumentException, FactoryException{
		super.denormalize(object);
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdDirectoryGroupType obj = (NameIdDirectoryGroupType)object;
		if(obj.getGroupId() != null && obj.getGroupId().compareTo(0L) == 0){
			throw new ArgumentException("Invalid object group");	
		}
		if(obj.getGroupPath() != null) return;
		BaseGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(obj.getGroupId(), obj.getOrganizationId());
		obj.setGroupPath(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getPath(dir));
	}
	
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		NameIdDirectoryGroupType dir = (NameIdDirectoryGroupType)map;
		if(dir.getGroupId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(dir.getGroupId());
			if(tmpId.compareTo(0L) > 0) dir.setGroupId(tmpId);
		}
	}
	
	protected NameIdType readGroup(ResultSet rset, NameIdType obj) throws SQLException, FactoryException, ArgumentException
	{
		super.read(rset, obj);
		long groupId = rset.getLong("groupid");
		NameIdDirectoryGroupType dobj = (NameIdDirectoryGroupType)obj;
		
		dobj.setGroupId(groupId);
		dobj.setGroupPath(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getPath(groupId,obj.getOrganizationId()));
		return obj;
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		if(t.getGroupId().compareTo(0L) == 0){
			logger.error("ORPHAN " + t.getNameType() + " OBJECT #" + t.getId());
		}
		return t.getName() + "-" + t.getParentId() + "-" + (t.getGroupId().compareTo(0L) == 0 ? "ORPHAN" : t.getGroupId());
	}

	public <T> List<T> search(String searchValue, long startRecord, int recordCount, DirectoryGroupType dir) throws FactoryException, ArgumentException{
		ProcessingInstructionType instruction = null;
		if(startRecord >= 0 && recordCount >= 0){
			instruction = new ProcessingInstructionType();
			instruction.setOrderClause("name ASC");
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		
		List<QueryField> fields = buildSearchQuery(searchValue, dir.getOrganizationId());
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		return search(fields.toArray(new QueryField[0]), instruction, dir.getOrganizationId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getByNameInParent(String name, String type, NameIdDirectoryGroupType parentObj) throws FactoryException, ArgumentException
	{
		String keyName = name + "-" + "-" + parentObj.getGroupId() + "-" + parentObj.getOrganizationId();
		NameIdType outObj = readCache(keyName);
		if (outObj != null) return (T)outObj;
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldName(name));
		fields.add(QueryFields.getFieldParent(parentObj.getId()));
		
		List<NameIdType> objs = getByField(fields.toArray(new QueryField[0]), parentObj.getOrganizationId());

		if (!objs.isEmpty())
		{
			addToCache(objs.get(0),keyName);
			return (T)objs.get(0);
		}
		return null;
	}
	
	public <T> T getByNameInGroup(String name, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException{
		return getByNameInGroup(name, 0, parentGroup);
	}
	public <T> T getByNameInGroup(String name, long parentId, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException{
		return getByNameInGroup(name, parentId, parentGroup.getId(), parentGroup.getOrganizationId());
	}
	public <T> T getByNameInGroup(String name, long parentGroupId, long organizationId) throws FactoryException, ArgumentException{
		return getByNameInGroup(name, 0L, parentGroupId, organizationId);
	}
	@SuppressWarnings("unchecked")
	public <T> T getByNameInGroup(String name, long parentId, long parentGroupId, long organizationId) throws FactoryException, ArgumentException{
		String keyName = name + "-" + parentId + "-" + parentGroupId;
		T outData = readCache(keyName);
		if (outData != null) return outData;
		List<QueryField> fields = new ArrayList<>();
		if(hasParentId) fields.add(QueryFields.getFieldParent(parentId));
		fields.add(QueryFields.getFieldName(name));
		fields.add(QueryFields.getFieldGroup(parentGroupId));
		
		List<NameIdType> objList = getByField(fields.toArray(new QueryField[0]), organizationId);

		if (!objList.isEmpty())
		{
			addToCache(objList.get(0),keyName);
			outData = (T)objList.get(0);
		}

		return outData;
	}
	
	public int countInGroup(BaseGroupType group) throws FactoryException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldGroup(group.getId()));
		if(this.hasParentId){
			fields.add(QueryFields.getFieldParent(0));
		}
		return getCountByField(this.getDataTables().get(0), fields.toArray(new QueryField[0]), group.getOrganizationId());
	}
	public long[] getIdsInGroup(BaseGroupType group) throws FactoryException{
		return getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
	}
	public String[] getNamesInGroup(BaseGroupType group) throws FactoryException{
		return getNamesByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
	}
	public <T> List<T>  listInGroup(BaseGroupType group, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldGroup(group.getId()));
		if(hasParentId){
			fields.add(QueryFields.getFieldParent(0));
		}
		return paginateList(fields.toArray(new QueryField[0]), startRecord, recordCount, organizationId);
	}

	
}