package org.cote.accountmanager.data.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;



public abstract class NameIdFactory extends FactoryBase {
	public static final Logger logger = Logger.getLogger(NameIdFactory.class.getName());
	/// id :: name
	private Map<Long, String> typeNameIdMap = null;
	/// cacheKey :: index
	private Map<String,Integer> typeNameMap = null;
	/// id :: index
	private Map<Long,Integer> typeIdMap = null;
	private List<NameIdType> typeMap = null;
	private long cacheExpires = 0;
	private int cacheExpiry = 5;
	protected boolean hasOwnerId = true;
	protected boolean hasParentId = true;
	protected boolean hasName = true;
	protected boolean hasObjectId = false;
	protected boolean hasUrn = false;
	
	public NameIdFactory(){
		super();
		typeNameIdMap = Collections.synchronizedMap(new HashMap<Long,String>());
		typeNameMap = Collections.synchronizedMap(new HashMap<String,Integer>());
		typeIdMap = Collections.synchronizedMap(new HashMap<Long,Integer>());
		typeMap = new ArrayList<NameIdType>();
		
		/// Invoke clear cache to set the expiration
		clearCache();
	}
	
	public <T> T clone(T source) throws FactoryException{
		throw new FactoryException("Method must be overriden.  Yes, this could be abstract, but not every factory needs it.");
	}
		
	
	public void mapBulkIds(NameIdType map){
		long tmpId = 0;
		if(hasOwnerId && map.getOwnerId() < 0L){
			tmpId = BulkFactories.getBulkFactory().getMappedId(map.getOwnerId());
			if(tmpId > 0) map.setOwnerId(tmpId);
		}
		if(hasParentId && map.getParentId() < 0L){
			tmpId = BulkFactories.getBulkFactory().getMappedId(map.getParentId());
			logger.debug("Map parentId " + map.getParentId() + " to " + tmpId);
			if(tmpId > 0) map.setParentId(tmpId);
		}
	}

	/*
	public String getUpdateTemplate(DataTable table, ProcessingInstructionType instruction){
		return table.getUpdateFullTemplate();
	}
	*/
	public boolean update(NameIdType map) throws FactoryException
	{
		return update(map, null);
	}
	
	public boolean update(NameIdType map, ProcessingInstructionType instruction) throws FactoryException
	{
		DataTable table = dataTables.get(0);
		if(instruction == null) instruction = new ProcessingInstructionType();
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		List<QueryField> queryFields = new ArrayList<QueryField>();
		List<QueryField> updateFields = new ArrayList<QueryField>();
		
		//+ " WHERE id = " + token
		//		+ (scopeToOrganization ? " AND organizationid = " + token : "")
		//;
		
		queryFields.add(QueryFields.getFieldId(map));
		if(scopeToOrganization){
			queryFields.add(QueryFields.getFieldOrganization(map));
		}
		setNameIdFields(updateFields, map);
		setFactoryFields(updateFields, map, instruction);
		String sql = getUpdateTemplate(table, updateFields.toArray(new QueryField[0]), token) + " WHERE " + getQueryClause(queryFields.toArray(new QueryField[0]), token);

		//logger.info("Update String = " + sql);
		
		updateFields.addAll(queryFields);
		
		int updated = 0;
		try{
			PreparedStatement statement = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(updateFields.toArray(new QueryField[0]), statement);
			updated = statement.executeUpdate();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return (updated > 0);
	}

	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		
	}

	private void setNameIdFields(List<QueryField> fields, NameIdType map){
			if(hasObjectId) fields.add(QueryFields.getFieldObjectId(map));
			if(hasName) fields.add(QueryFields.getFieldName(map));
			if(hasOwnerId) fields.add(QueryFields.getFieldOwner(map));
			if(hasParentId) fields.add(QueryFields.getFieldParent(map));
			if(hasUrn){
				map.setUrn(UrnUtil.getUrn(map));
				if(map.getUrn() == null){
					logger.error("Urn value is null for object " + map.getNameType().toString() + " " + map.getName());
				}
				fields.add(QueryFields.getFieldUrn(map));
			}
				//fields.add(QueryFields.getFieldUrn(map));
			if(scopeToOrganization) fields.add(QueryFields.getFieldOrganization(map));
	}
	public DataRow prepareAdd(NameIdType obj, String tableName) throws FactoryException{
		DataTable table = getDataTable(tableName);
		if(table == null) throw new FactoryException("Table doesn't exist:" + tableName);
		/// If the factory specifies the object should have an object id, then auto generate it if it doesn't exist
		///
		if(hasObjectId && obj.getObjectId() == null) obj.setObjectId(UUID.randomUUID().toString());
		
		/// If the factory specifies the object should have an urn, then auto generate it if it doesn't exist
		///
		if(hasUrn){
			obj.setUrn(UrnUtil.getUrn(obj));
			if(obj.getUrn() == null){
				throw new FactoryException("Urn value is null for object " + obj.getNameType().toString() + " " + obj.getName());
			}
		}
		
		if(bulkMode && obj.getId() < 0){
			throw new FactoryException("Object id is invalid for bulk mode insert: " + obj.getId());
		}
		DataRow row = table.newRow();
		try{
			if(bulkMode && obj.getId() > 0) row.setCellValue("id", obj.getId());
			if(hasObjectId) row.setCellValue("objectid", obj.getObjectId());
			if(hasName) row.setCellValue("name", obj.getName());
			if(hasUrn) row.setCellValue("urn", obj.getUrn());
			if(hasParentId) row.setCellValue("parentid",obj.getParentId());
			if(hasOwnerId) row.setCellValue("ownerid",obj.getOwnerId());
			if (scopeToOrganization) row.setCellValue("organizationid", obj.getOrganization().getId());
		}
		catch(DataAccessException dae){
			logger.error(dae.getMessage());
			throw new FactoryException(dae.getMessage());
		}
		return row;
	}
	public <T> T getById(long id, OrganizationType org) throws FactoryException, ArgumentException
	{
		T out_obj = readCache(id);
		if (out_obj != null) return out_obj;

		List<NameIdType> obj_list = getByField(new QueryField[] { QueryFields.getFieldId(id) }, org.getId());

		if (obj_list.size() > 0)
		{
			
			String key_name = getCacheKeyName(obj_list.get(0));
			addToCache(obj_list.get(0),key_name);
			out_obj = (T)obj_list.get(0);
		}
		return out_obj;
	}
	protected List<NameIdType> getByName(String name, long organization_id) throws FactoryException, ArgumentException
	{
		if (!hasName) throw new FactoryException("Table " + dataTables.get(0).getName() + " Does not define a Name");
		return getByField(QueryFields.getFieldName(name), organization_id);
	}
	protected long getIdByName(String name, long organization_id) throws FactoryException
	{
		if (!hasName) throw new FactoryException("Table " + dataTables.get(0).getName() + " does not define a Name");
		long[] ids = getIdByField("name", SqlDataEnumType.VARCHAR, name, organization_id);
		if (ids.length > 0) return ids[0];
		return 0;
	}
	protected List<NameIdType> getByField(QueryField field, long organization_id) throws FactoryException, ArgumentException{
		return getByField(field, null, organization_id);
	}
	protected List<NameIdType> getByField(QueryField field, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException{
		return getByField(new QueryField[]{field}, instruction, organization_id);
	}
	protected List<NameIdType> getByField(QueryField[] fields, long organization_id) throws FactoryException, ArgumentException{
		return getByField(fields, null, organization_id);
	}
	protected List<NameIdType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException{
		List<NameIdType> out_list = new ArrayList<NameIdType>();

		if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = this.dataTables.get(0);
		String selectString = getSelectTemplate(table, instruction);
		/*
		String pagePrefix = DBFactory.getPaginationPrefix(instruction, connectionType);
		String pageSuffix = DBFactory.getPaginationSuffix(instruction, connectionType);
		String pageField = DBFactory.getPaginationField(instruction, connectionType);
		String paramToken = DBFactory.getParamToken(connectionType);
		String queryClause = getQueryClause(fields, paramToken);
		
		selectString = selectString.replaceAll("#TOP#", (instruction != null && instruction.getTopCount() > 0 ? "TOP " + instruction.getTopCount() : ""));
		selectString = selectString.replaceAll("#PAGE#", pageField);
		
		String sqlQuery = pagePrefix + selectString + " WHERE " + queryClause
			+ (scopeToOrganization ? (queryClause.length() == 0 ? " " : " AND ") + "organizationid=" + organization_id : "")
			+ (instruction != null && instruction.getGroupClause() != null ? " GROUP BY " + instruction.getGroupClause() : "")
			+ (instruction != null && instruction.getHavingClause() != null ? " HAVING " + instruction.getHavingClause() : "")
			+ pageSuffix
		;
		*/
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, organization_id);
		//logger.info("Query Clause=" + queryClause);
		//logger.info("SQL=" + sqlQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				NameIdType obj = this.read(rset, instruction);
				//logger.info("Read object: " + (obj == null ? "NULL" : obj.getName()));
				out_list.add(obj);
			}
			rset.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return out_list;
	}
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		throw new FactoryException("This is an artifact from java<->c#<->java conversions - should be an abstract class + interface, not an override");
	}

	protected NameIdType read(ResultSet rset, NameIdType obj) throws SQLException, FactoryException, ArgumentException
	{
		obj.setId(rset.getLong("id"));
		if(obj.getNameType() == null) obj.setNameType(NameEnumType.UNKNOWN);
		if(hasObjectId) obj.setObjectId(rset.getString("objectid"));
		if(hasName) obj.setName(rset.getString("name"));
		if(hasUrn) obj.setUrn(rset.getString("urn"));
		if(hasOwnerId) obj.setOwnerId(rset.getLong("ownerid"));
		else obj.setOwnerId(0L);
		if(hasParentId) obj.setParentId(rset.getLong("parentid"));
		else obj.setParentId(0L);
		if(scopeToOrganization){
			OrganizationType org = Factories.getOrganizationFactory().getOrganizationById(rset.getLong("organizationid"));
			obj.setOrganization(org);
		}

		return obj;
	}
	protected List<NameIdType> getById(long id, long organization_id) throws FactoryException, ArgumentException
	{
		return getByField(QueryFields.getFieldId(id), organization_id);
	}
	
	public List<QueryField> buildSearchQuery(String searchValue, OrganizationType organization) throws FactoryException{
		throw new FactoryException("Abstract implementation");
	}
	
	public <T> List<T> search(QueryField[] fields, ProcessingInstructionType pi, OrganizationType organization) throws FactoryException, ArgumentException{
		return getList(fields, pi, organization);
	}
	protected <T> List<T> searchByIdInView(String viewName, QueryField[] filters, ProcessingInstructionType instruction, OrganizationType organization){
		return searchByIdInView(viewName, "id", filters, instruction, organization);
	}
	protected <T> List<T> searchByIdInView(String viewName, String idCol, QueryField[] filters, ProcessingInstructionType instruction, OrganizationType organization){

		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String sqlQuery = assembleQueryString("SELECT " + idCol + " FROM " + viewName, filters, connectionType, instruction, organization.getId());
		logger.debug("Query=" + sqlQuery);
		List<Long> ids = new ArrayList<Long>();
		List<T> objs = new ArrayList<T>();
		
		try{
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(filters, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				ids.add(rset.getLong(idCol));
			}
			rset.close();
			
			/// don't paginate the subsequent search for ids because it was already filtered.
			/// Create a new instruction and just copy the order clause
			///
			ProcessingInstructionType pi2 = null;
			if(instruction != null){
				pi2 = new ProcessingInstructionType();
				pi2.setOrderClause(instruction.getOrderClause());
			}
			objs = getListByIds(ArrayUtils.toPrimitive(ids.toArray(new Long[0])),pi2,organization);
			logger.info("Retrieved " + objs.size() + " from " + ids.size() + " ids");
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//return search(fields, instruction, organization);
		return objs;
	}
	public <T> List<T> getList(QueryField[] fields, ProcessingInstructionType pi, OrganizationType organization) throws FactoryException, ArgumentException
	{

		List<NameIdType> user_list = getByField(fields,pi, organization.getId());
		return convertList(user_list);
	}	
	public <T> List<T> getList(QueryField[] fields, OrganizationType organization) throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getList(fields, instruction, organization);
	}
	public <T> List<T>  getPaginatedList(QueryField[] fields, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		instruction.setPaginate(false);
		return getPaginatedList(fields, instruction, startRecord, recordCount, organization);
	}
	public <T> List<T>  getPaginatedList(QueryField[] fields, ProcessingInstructionType instruction, int startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		if (instruction != null && startRecord >= 0 && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getList(fields, instruction, organization);
	}
	public <T> List<T> getListByIds(long[] ids, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getListByIds(ids, null, organization);
	}
	public <T> List<T> getListByIds(long[] ids, ProcessingInstructionType instruction, OrganizationType organization) throws FactoryException, ArgumentException
	{

		StringBuffer buff = new StringBuffer();
		
		List<NameIdType> out_list = new ArrayList<NameIdType>();
		for (int i = 0; i < ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(ids[i]);
			if ((i > 0 || ids.length == 1) && ((i % 250 == 0) || i == ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
				match.setComparator(ComparatorEnumType.IN);
				List<NameIdType> tmp_data_list = getByField(new QueryField[] { match }, instruction, organization.getId());
				out_list.addAll(tmp_data_list);
				buff.delete(0,  buff.length());
			}
		}
		return convertList(out_list);
	}
	
	public <T> T getByUrn(String urn){
		logger.debug("Reading object by urn " + urn);
		T obj = readCache(urn);
		if(obj != null){
			return obj;
		}
		try {
			//OrganizationType organization = UrnUtil.getOrganization(urn);
			//List<T> objs = convertList(getByField(new QueryField[]{QueryFields.getFieldUrn(urn)},organization.getId()));
			List<T> objs = convertList(getByField(new QueryField[]{QueryFields.getFieldUrn(urn)},0L));
			if(objs.size() >= 1){
				obj = objs.get(0);
				addToCache((NameIdType)obj, getUrnCacheKey(obj));
				addToCache((NameIdType)obj);
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return obj;
	}
	
	public void expireCache(){
		cacheExpires = 0;
	}
	
	
	public void clearCache(){
		typeNameIdMap.clear();
		typeNameMap.clear();
		typeIdMap.clear();
		typeMap.clear();
		cacheExpires = System.currentTimeMillis() + (cacheExpiry * 60000);
	}
	
	protected void checkCacheExpires(){
		if(cacheExpires <= System.currentTimeMillis()){
			logger.debug("Expire cache");
			clearCache();
		}
	}
	public boolean haveCacheId(long id){
		return typeIdMap.containsKey(id);
	}
	public void removeBranchFromCache(NameIdType obj){
		removeFromCache(obj);
		if(!hasParentId || obj.getParentId() == 0L) return;
		try {
			NameIdType parent = getById(obj.getParentId(),obj.getOrganization());
			if(parent != null) removeBranchFromCache(parent);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void removeFromCache(NameIdType obj){
		removeFromCache(obj, getCacheKeyName(obj));
	}
	public void removeFromCache(NameIdType obj, String key_name){
		synchronized(typeMap){
			if(key_name == null){
				//logger.info("Remove null key");
				Iterator<String> keys = typeNameMap.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					if(typeMap.contains(typeNameMap.get(key)) && typeMap.get(typeNameMap.get(key)).getId() == obj.getId()){
						key_name = key;
						break;
					}
				}
			}
			//logger.debug("Remove from cache: " + key_name + ":" + typeNameMap.containsKey(key_name) + " and " + typeIdMap.containsKey(obj.getId()));
			if(typeNameMap.containsKey(key_name) && typeIdMap.containsKey(obj.getId())){
				int indexId = typeIdMap.get(obj.getId());
				typeNameMap.remove(key_name);
				typeMap.set(indexId, null);
				typeIdMap.remove(obj.getId());
				typeNameIdMap.remove(obj.getId());
			}
		}
	}
	public <T> T readCache(String name){
		checkCacheExpires();
		//logger.debug("Check cache for key '" + name + "'");
		if(typeNameMap.containsKey(name)){
			//logger.debug("Found cache for key '" + name + "'");
			return (T)typeMap.get(typeNameMap.get(name));
		}
		return null;
	}

	public <T> T readCache(long id){
		//logger.debug("Check cache for id '" + id + "'");
		checkCacheExpires();
		if(typeIdMap.containsKey(id)){
			//logger.debug("Found cache for " + this.factoryType + " id " + id);
			return (T)typeMap.get(typeIdMap.get(id));
		}
		return null;
	}
	public <T> String getUrnCacheKey(T obj){
		return ((NameIdType)obj).getUrn();
	}
	public <T> String getCacheKeyName(T obj){
		return ((NameIdType)obj).getName();
	}
	protected void updateToCache(NameIdType obj) throws ArgumentException{
		String key_name = getCacheKeyName(obj);
		//logger.info("Update to cache: " + key_name);
		if(this.haveCacheId(obj.getId())) removeFromCache(obj);
		addToCache(obj, key_name);
	}

	public boolean addToCache(NameIdType map) throws ArgumentException{
		return addToCache(map, map.getName());
	}
	public synchronized boolean addToCache(NameIdType map, String key_name) throws ArgumentException{
		if(map == null) return false;
		if(key_name == null) throw new ArgumentException("Key name is null");
		//checkCacheExpires();
		//logger.debug("Add to cache: " + (map == null ? "NULL" : map.getNameType() + " " + map.getName()) + " AT " + key_name);
		//synchronized(typeMap){
			int length = typeMap.size();
			if(typeNameMap.containsKey(key_name) || typeIdMap.containsKey(map.getId())){
				return false;
			}
			//logger.debug("Cached " + map.getNameType() + " " + map.getName() + " (#" + map.getId() + ") with key '" + key_name + "'");
			typeMap.add(map);
			typeNameMap.put(key_name, length);
			typeIdMap.put(map.getId(), length);
			typeNameIdMap.put(map.getId(), map.getName());
		//}
		return true;
	}
	protected boolean isValid(NameIdType map)
	{
		if (map == null || map.getId() <= 0 || map.getOrganization() == null) return false;
		return true;
	}
	public int getCount(long organization_id) throws FactoryException
	{
		return getCountByField(this.getDataTables().get(0), new QueryField[]{}, organization_id);
	}
	public int getCountInParent(NameIdType parent) throws FactoryException
	{
		return getCountByField(this.getDataTables().get(0), new QueryField[]{QueryFields.getFieldParent(parent.getId())}, parent.getOrganization().getId());
	}

}
