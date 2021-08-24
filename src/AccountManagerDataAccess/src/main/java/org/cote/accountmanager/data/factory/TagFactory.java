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
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountTagType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.GroupTagType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonTagType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserTagType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.TagEnumType;

public class TagFactory extends NameIdGroupFactory {
	public TagFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
		this.hasName = true;
		this.hasObjectId = true;
		this.hasUrn = true;
		this.primaryTableName = "tags";
		this.tableNames.add(primaryTableName);
		factoryType = FactoryEnumType.TAG;
		
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			// column restrictions
		}

	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		BaseTagType t = (BaseTagType)obj;
		return t.getName() + "-" + t.getTagType().toString() + "-" + t.getGroupId();
	}
	
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		BaseTagType tag = (BaseTagType)object;
		removeFromCache(tag, null);
		return super.update(tag);
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		BaseTagType tag = (BaseTagType)object;
		removeFromCache(tag);
		int deleted = deleteById(tag.getId(), tag.getOrganizationId());
		((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).deleteParticipations(tag);
		return (deleted > 0);
	}
	
	/// Like data, tags are owned by users, not accounts
	///
	public int deleteTagsByUser(UserType map) throws FactoryException, ArgumentException
	{
		List<NameIdType> tags = getByField(new QueryField[] { QueryFields.getFieldOwner(map.getId()) }, map.getOrganizationId());
		List<Long> tagIds = new ArrayList<>();
		for (int i = 0; i < tags.size(); i++)
		{
			tagIds.add(tags.get(i).getId());
			removeFromCache(tags.get(i));
		}
		return deleteTagsByIds(convertLongList(tagIds), map.getOrganizationId());
	}
	public int deleteTagsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		BaseTagType newTag = (BaseTagType)object;
		if (newTag.getOrganizationId() <= 0L) throw new FactoryException("Cannot add tag to invalid organization");
		DataRow row = prepareAdd(newTag, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), newTag.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.TAGTYPE), newTag.getTagType().toString());
		}
		catch(DataAccessException e){
			throw new FactoryException(e.getMessage());
		}
		return insertRow(row);
	}

	public DataTagType getPersonTagByName(String name, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		return getTagByName(name, TagEnumType.PERSON, group);
	}
	public DataTagType getAccountTagByName(String name, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		return getTagByName(name, TagEnumType.ACCOUNT, group);
	}
	public DataTagType getUserTagByName(String name, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		return getTagByName(name, TagEnumType.USER, group);
	}
	public DataTagType getGroupTagByName(String name, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		return getTagByName(name, TagEnumType.GROUP, group);
	}

	public DataTagType getDataTagByName(String name, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		return getTagByName(name, TagEnumType.DATA, group);
	}
	@SuppressWarnings("unchecked")
	public <T> T getTagByName(String name, TagEnumType type, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		String keyName = name + "-" + type + "-" + group.getId();
		T outTag = readCache(keyName);
		if (outTag != null) return outTag;

		List<NameIdType> tags = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldGroup(group.getId()),QueryFields.getFieldTagType(type)}, group.getOrganizationId());
		if (!tags.isEmpty())
		{
			addToCache(tags.get(0),keyName);
			return (T)tags.get(0);
		}
		return null;
	}

	@Override
	public <T> List<T> listInGroup(BaseGroupType group, long startRecord, int recordCount,long organizationId) throws FactoryException, ArgumentException{
		return listInGroup(group, TagEnumType.UNKNOWN, null,startRecord, recordCount,organizationId);
	}
	public <T> List<T> listInGroup(BaseGroupType group, TagEnumType type, QueryField match, long startRecord, int recordCount,long organizationId) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		if(group != null) fields.add(QueryFields.getFieldGroup(group.getId()));
		if(type != TagEnumType.UNKNOWN) fields.add(QueryFields.getFieldTagType(type));
		if(match != null) fields.add(match);
		return paginateList(fields.toArray(new QueryField[0]),startRecord,recordCount,organizationId);
	}

	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BaseTagType useMap = (BaseTagType)map;
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldTagType(useMap.getTagType()));
	}

	@SuppressWarnings("unchecked")
	public <T> T newTag(UserType owner,String tagName, TagEnumType type, long groupId) throws ArgumentException
	{
		BaseTagType newTag = newTag(type);
		newTag.setOwnerId(owner.getId());
		newTag.setGroupId(groupId);
		newTag.setOrganizationId(owner.getOrganizationId());
		newTag.setName(tagName);
		return (T)newTag;
	}
	protected BaseTagType newTag(TagEnumType type) throws ArgumentException
	{

		BaseTagType newTag = null;
		switch (type)
		{
			case DATA:
				newTag = new DataTagType();
				break;
			case ACCOUNT:
				newTag = new AccountTagType();
				break;
			case USER:
				newTag = new UserTagType();
				break;
			case PERSON:
				newTag = new PersonTagType();
				break;
			case GROUP:
				newTag = new GroupTagType();
				break;
			default:
				throw new ArgumentException("Invalid tag type: " + type);
		}
		newTag.setNameType(NameEnumType.TAG);
		newTag.setTagType(type);
		return newTag;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		BaseTagType newTag = null;
		try{
			newTag = newTag(TagEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.TAGTYPE))));
			super.read(rset, newTag);
			readGroup(rset,newTag);
		}
		catch(ArgumentException ae){
			throw new FactoryException(ae.getMessage());
		}
		return newTag;
	}

	public List<DataType> getForTag(BaseTagType tag, long organizationId) throws FactoryException, ArgumentException{
		return getForTags(FactoryEnumType.valueOf(tag.getTagType().toString()),new BaseTagType[]{tag}, 0L,0, organizationId);
	}
	public <T> List<T> getForTags(FactoryEnumType type, BaseTagType[] tags, long startRecord, int recordCount, long organizationId) throws FactoryException, ArgumentException{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setPaginate(true);
		instruction.setStartIndex(startRecord);
		instruction.setRecordCount(recordCount);
		List<DataParticipantType> parts = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).getTagParticipations(tags, instruction,ParticipantEnumType.DATA);
		if(parts.isEmpty()) return new ArrayList<>();
		/// Don't apply pagination to the secondary query because it's already been paginated from the parts list
		///
		return ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).getListFromParticipations(type, parts.toArray(new BaseParticipantType[0]), true, 0, 0, organizationId);
	
	}

}
