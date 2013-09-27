package org.cote.accountmanager.data.factory;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;



public class NameIdGroupFactory extends NameIdFactory{

	public NameIdGroupFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getParentId() + "-" + t.getGroup().getId();
	}
	
	public int getCount(DirectoryGroupType group) throws FactoryException
	{
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldGroup(group.getId()));
		if(this.hasParentId){
			fields.add(QueryFields.getFieldParent(0));
		}
		return getCountByField(this.getDataTables().get(0), fields.toArray(new QueryField[0]), group.getOrganization().getId());
	}
	
	public <T> T getByName(String name, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException{
		return getByName(name, 0, parentGroup);
	}
	public <T> T getByName(String name, int parentId, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException{
		String key_name = name + "-" + parentId + "-" + parentGroup.getId();
		T out_data = readCache(key_name);
		if (out_data != null) return out_data;

		List<NameIdType> obj_list = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldGroup(parentGroup.getId()) }, parentGroup.getOrganization().getId());

		if (obj_list.size() > 0)
		{
			logger.debug("NGF BEGIN Add to Cache");
			addToCache(obj_list.get(0),key_name);
			logger.debug("NGF END Add to Cache");
			out_data = (T)obj_list.get(0);
		}
		else{
			//logger.info("No results for " + name + " in " + parentGroup.getId());
		}
		return out_data;
	}
	


	public <T> List<T>  getListByGroup(DirectoryGroupType group, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldGroup(group.getId()));
		if(hasParentId){
			fields.add(QueryFields.getFieldParent(0));
		}
		return getPaginatedList(fields.toArray(new QueryField[0]), startRecord, recordCount, organization);
	}

	
}