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
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountTagType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.GroupTagType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonTagType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserTagType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
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
		this.hasUrn = true;
		this.tableNames.add("tags");
		factoryType = FactoryEnumType.TAG;
		
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("tags")){
			///table.setRestrictUpdateColumn("xxx", true);
		}

	}
	@Override
	public <T> String getCacheKeyName(T obj){
		BaseTagType t = (BaseTagType)obj;
		return t.getName() + "-" + t.getTagType().toString() + "-" + t.getGroup().getId();
	}
	public boolean updateTag(BaseTagType tag) throws FactoryException
	{
		removeFromCache(tag, null);
		return update(tag);
	}

	public boolean deleteTag(BaseTagType tag) throws FactoryException
	{

		removeFromCache(tag);
		int deleted = deleteById(tag.getId(), tag.getOrganization().getId());
		Factories.getTagParticipationFactory().deleteParticipations(tag);
		return (deleted > 0);
	}
	
	/// Like data, tags are owned by users, not accounts
	///
	public int deleteTagsByUser(UserType map) throws FactoryException, ArgumentException
	{
		List<NameIdType> tags = getByField(new QueryField[] { QueryFields.getFieldOwner(map.getId()) }, map.getOrganization().getId());
		List<Long> tag_ids = new ArrayList<Long>();
		for (int i = 0; i < tags.size(); i++)
		{
			tag_ids.add(tags.get(i).getId());
			removeFromCache(tags.get(i));
		}
		return deleteTagsByIds(convertLongList(tag_ids), map.getOrganization());
	}
	public int deleteTagsByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			//Factory.TagParticipationFactoryInstance.DeleteParticipations(ids, organization);
		}
		return deleted;
	}
	public boolean addTag(BaseTagType new_tag) throws DataAccessException, FactoryException
	{
		if (new_tag.getOrganization() == null || new_tag.getOrganization().getId() <= 0) throw new FactoryException("Cannot add tag to invalid organization");
		DataRow row = prepareAdd(new_tag, "tags");
		row.setCellValue("groupid", new_tag.getGroup().getId());
		row.setCellValue("tagtype", new_tag.getTagType().toString());
		return insertRow(row);
	}
	/*
	public <T> T getTagById(int id, OrganizationType organization) throws FactoryException, ArgumentException
	{
		T out_tag = readCache(id);
		if (out_tag != null) return out_tag;

		List<NameIdType> tags = getById(id, organization.getId());
		if (tags.size() > 0)
		{
			BaseTagType tag = (BaseTagType)tags.get(0);
			String key_name = tag.getTagType() + "-" + tag.getName() + "-" + tag.getOrganization().getId();
			addToCache(tag,key_name);
			return (T)tag;
		}
		return null;
	}
	*/
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
	public <T> T getTagByName(String name, TagEnumType type, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		String key_name = name + "-" + type + "-" + group.getId();
		T out_tag = readCache(key_name);
		if (out_tag != null) return out_tag;

		List<NameIdType> tags = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldGroup(group.getId()),QueryFields.getFieldTagType(type)}, group.getOrganization().getId());
		if (tags.size() > 0)
		{
			addToCache(tags.get(0),key_name);
			return (T)tags.get(0);
		}
		return null;
	}

	public <T> List<T> listTags(DirectoryGroupType group, long startRecord, int recordCount,OrganizationType org) throws FactoryException, ArgumentException{
		return listTags(group, TagEnumType.UNKNOWN, null,startRecord, recordCount,org);
	}
	public <T> List<T> listTags(DirectoryGroupType group, TagEnumType type, QueryField match, long startRecord, int recordCount,OrganizationType org) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		if(group != null) fields.add(QueryFields.getFieldGroup(group.getId()));
		if(type != TagEnumType.UNKNOWN) fields.add(QueryFields.getFieldTagType(type));
		if(match != null) fields.add(match);
		return getPaginatedList(fields.toArray(new QueryField[0]),startRecord,recordCount,org);
	}

	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BaseTagType use_map = (BaseTagType)map;
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
		fields.add(QueryFields.getFieldTagType(use_map.getTagType()));
	}
	public DataTagType newDataTag(UserType owner,String tag_name, DirectoryGroupType group) throws ArgumentException{
		return newTag(owner,tag_name, TagEnumType.DATA, group);
	}
	public AccountTagType newAccountTag(UserType owner,String tag_name, DirectoryGroupType group) throws ArgumentException{
		return newTag(owner,tag_name, TagEnumType.ACCOUNT, group);
	}
	public UserTagType newUserTag(UserType owner,String tag_name, DirectoryGroupType group) throws ArgumentException{
		return newTag(owner,tag_name, TagEnumType.USER, group);
	}
	public UserTagType newPersonTag(UserType owner,String tag_name, DirectoryGroupType group) throws ArgumentException{
		return newTag(owner,tag_name, TagEnumType.PERSON, group);
	}
	public UserTagType newGroupTag(UserType owner,String tag_name, DirectoryGroupType group) throws ArgumentException{
		return newTag(owner,tag_name, TagEnumType.GROUP, group);
	}


	public <T> T newTag(UserType owner,String tag_name, TagEnumType Type, DirectoryGroupType group) throws ArgumentException
	{
		BaseTagType new_tag = newTag(Type);
		new_tag.setOwnerId(owner.getId());
		new_tag.setGroup(group);
		new_tag.setOrganization(group.getOrganization());
		new_tag.setName(tag_name);
		return (T)new_tag;
	}
	protected BaseTagType newTag(TagEnumType Type) throws ArgumentException
	{

		BaseTagType new_tag = null;
		switch (Type)
		{
			case DATA:
				new_tag = new DataTagType();
				break;
			case ACCOUNT:
				new_tag = new AccountTagType();
				break;
			case USER:
				new_tag = new UserTagType();
				break;
			case PERSON:
				new_tag = new PersonTagType();
				break;
			case GROUP:
				new_tag = new GroupTagType();
				break;
			default:
				throw new ArgumentException("Invalid tag type: " + Type);
		}
		new_tag.setNameType(NameEnumType.TAG);
		new_tag.setTagType(Type);
		return new_tag;
	}
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		BaseTagType new_tag = null;
		try{
			new_tag = newTag(TagEnumType.valueOf(rset.getString("tagtype")));
			super.read(rset, new_tag);
			readGroup(rset,new_tag);
		}
		catch(ArgumentException ae){
			throw new FactoryException(ae.getMessage());
		}
		return new_tag;
	}

	public List<DataType> getDataForTag(BaseTagType tag, OrganizationType organization) throws FactoryException, ArgumentException{
		return getDataForTags(new BaseTagType[]{tag}, 0L,0, organization);
	}
	public List<DataType> getDataForTags(BaseTagType[] tags, long startRecord, int recordCount, OrganizationType organization) throws FactoryException, ArgumentException{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setPaginate(true);
		instruction.setStartIndex(startRecord);
		instruction.setRecordCount(recordCount);
		List<DataParticipantType> parts = Factories.getTagParticipationFactory().getTagParticipations(tags, instruction,ParticipantEnumType.DATA);
		if(parts.size() == 0) return new ArrayList<DataType>();
		/// Don't apply pagination to the secondary query because it's already been paginated from the parts list
		///
		return Factories.getTagParticipationFactory().getDataListFromParticipations(parts.toArray(new DataParticipantType[0]), true, 0, 0, organization);

		//Factories.getTagParticipationFactory().getTagParticipations(tags);
		//List<Core.Tools.AccountManager.Map.DataParticipant> dps = Core.Tools.AccountManager.Factory.TagParticipationFactoryInstance.GetTagParticipations(tags.ToArray());

		//if (dps.Count > 0) active_data_list = Core.Tools.AccountManager.Factory.TagParticipationFactoryInstance.GetDataFromParticipations(dps.ToArray(), true, 0, 0, product.Organization).ToArray();
		//else active_data_list = new Core.Tools.AccountManager.Map.Data[0];
	
	}

}
