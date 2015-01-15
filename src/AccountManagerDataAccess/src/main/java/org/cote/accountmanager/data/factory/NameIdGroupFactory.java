package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;



public class NameIdGroupFactory extends NameIdFactory{
	public static final Logger logger = Logger.getLogger(NameIdGroupFactory.class.getName());
	public NameIdGroupFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
		this.hasUrn = true;
	}
	
	protected NameIdType readGroup(ResultSet rset, NameIdType obj) throws SQLException, FactoryException, ArgumentException
	{
		super.read(rset, obj);
		long group_id = rset.getLong("groupid");
		NameIdDirectoryGroupType dobj = (NameIdDirectoryGroupType)obj;
		dobj.setGroup(Factories.getGroupFactory().getDirectoryById(group_id, dobj.getOrganization()));
		dobj.setGroupId(group_id);
		//logger.info("Reading group " + group_id);
		return obj;
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		if(t.getGroup() == null){
			logger.error("ORPHAN " + t.getNameType() + " OBJECT #" + t.getId());
		}
		///logger.info("CKN: " + t.getParentId() + (t.getGroup() == null ? "NULL GROUP" : t.getGroup().getId()));
		return t.getName() + "-" + t.getParentId() + "-" + (t.getGroup() == null ? "ORPHAN" : t.getGroup().getId());
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
		
		List<QueryField> fields = buildSearchQuery(searchValue, dir.getOrganization());
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		return search(fields.toArray(new QueryField[0]), instruction, dir.getOrganization());
	}
	
	public <T> T getByName(String name, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException{
		return getByName(name, 0, parentGroup);
	}
	public <T> T getByName(String name, long parentId, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException{
		String key_name = name + "-" + parentId + "-" + parentGroup.getId();
		T out_data = readCache(key_name);
		if (out_data != null) return out_data;
		List<QueryField> fields = new ArrayList<QueryField>();
		if(hasParentId) fields.add(QueryFields.getFieldParent(parentId));
		fields.add(QueryFields.getFieldName(name));
		fields.add(QueryFields.getFieldGroup(parentGroup.getId()));
		
		List<NameIdType> obj_list = getByField(fields.toArray(new QueryField[0]), parentGroup.getOrganization().getId());

		if (obj_list.size() > 0)
		{
			/// logger.debug("NGF BEGIN Add to Cache");
			addToCache(obj_list.get(0),key_name);
			/// logger.debug("NGF END Add to Cache");
			out_data = (T)obj_list.get(0);
		}
		else{
			//logger.info("No results for " + name + " in " + parentGroup.getId());
		}
		return out_data;
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

	public <T> List<T>  getListByGroup(DirectoryGroupType group, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldGroup(group.getId()));
		if(hasParentId){
			fields.add(QueryFields.getFieldParent(0));
		}
		return getPaginatedList(fields.toArray(new QueryField[0]), startRecord, recordCount, organization);
	}

	
}