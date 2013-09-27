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
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonTagType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserTagType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.TagEnumType;

public class TagFactory extends NameIdFactory {
	public TagFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
		this.hasName = true;
		this.tableNames.add("tags");
		factoryType = FactoryEnumType.TAG;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("xxx")){
			///table.setRestrictUpdateColumn("xxx", true);
		}

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
		row.setCellValue("tagtype", new_tag.getTagType().toString());
		return insertRow(row);
	}

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
	public DataTagType getDataTagByName(String name, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getTagByName(name, TagEnumType.DATA, organization);
	}
	public <T> T getTagByName(String name, TagEnumType type, OrganizationType organization) throws FactoryException, ArgumentException
	{
		String key_name = type + "-" + name + "-" + organization.getId();
		T out_tag = readCache(key_name);
		if (out_tag != null) return out_tag;

		List<NameIdType> tags = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldTagType(type)}, organization.getId());
		if (tags.size() > 0)
		{
			addToCache(tags.get(0),key_name);
			return (T)tags.get(0);
		}
		return null;
	}

	public List<PersonTagType> getPersonTags(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { QueryFields.getFieldTagType(TagEnumType.PERSON) }, organization.getId()));

	}
	public List<PersonTagType> getPersonTags(QueryField match, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { match, QueryFields.getFieldTagType(TagEnumType.PERSON) }, organization.getId()));
	}
	public List<UserTagType> getUserTags(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { QueryFields.getFieldTagType(TagEnumType.USER) }, organization.getId()));

	}
	public List<UserTagType> getUserTags(QueryField match, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { match, QueryFields.getFieldTagType(TagEnumType.USER) }, organization.getId()));
	}

	
	public List<AccountTagType> getAccountTags(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { QueryFields.getFieldTagType(TagEnumType.ACCOUNT) }, organization.getId()));

	}
	public List<AccountTagType> getAccountTags(QueryField match, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { match, QueryFields.getFieldTagType(TagEnumType.ACCOUNT) }, organization.getId()));
	}

	public List<DataTagType> getDataTags(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { QueryFields.getFieldTagType(TagEnumType.DATA) }, organization.getId()));

	}
	public List<DataTagType> getDataTags(QueryField match, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { match, QueryFields.getFieldTagType(TagEnumType.DATA) }, organization.getId()));
	}
	
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BaseTagType use_map = (BaseTagType)map;
		fields.add(QueryFields.getFieldTagType(use_map.getTagType()));
	}
	public DataTagType newDataTag(String tag_name, OrganizationType organization) throws ArgumentException{
		return newTag(tag_name, TagEnumType.DATA, organization);
	}
	public AccountTagType newAccountTag(String tag_name, OrganizationType organization) throws ArgumentException{
		return newTag(tag_name, TagEnumType.ACCOUNT, organization);
	}
	public UserTagType newUserTag(String tag_name, OrganizationType organization) throws ArgumentException{
		return newTag(tag_name, TagEnumType.USER, organization);
	}
	public <T> T newTag(String tag_name, TagEnumType Type, OrganizationType organization) throws ArgumentException
	{
		BaseTagType new_tag = newTag(Type);
		new_tag.setOrganization(organization);
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
			default:
				throw new ArgumentException("Invalid tag type: " + Type);
		}
		new_tag.setTagType(Type);
		return new_tag;
	}
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		BaseTagType new_tag = null;
		try{
			new_tag = newTag(TagEnumType.valueOf(rset.getString("tagtype")));
		}
		catch(ArgumentException ae){
			throw new FactoryException(ae.getMessage());
		}
		return super.read(rset, new_tag);
	}


}
