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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;


public abstract class NameIdFactory extends FactoryBase implements INameIdFactory{
	public static final Logger logger = Logger.getLogger(NameIdFactory.class.getName());
	private Map<Long, String> typeNameIdMap = null;
	private Map<String,Integer> typeNameMap = null;
	private Map<Long,Integer> typeIdMap = null;
	private Map<String,Integer> typeObjectIdMap = null;
	private List<NameIdType> typeMap = null;
	private long cacheExpires = 0;
	private int cacheExpiry = 5;
	protected boolean hasOwnerId = true;
	protected boolean hasParentId = true;
	protected boolean hasName = true;
	protected boolean hasObjectId = false;
	protected boolean hasUrn = false;
	protected boolean clusterByGroup = false;
	protected boolean clusterByParent = false;
	protected boolean isParticipation = false;
	
	/// 2016/05/31 - Switching the default of aggressive flush to 'false' until the reproduction is identified again
	/// The cleanup routine wasn't actually doing anything for all the time it wasted.
	/// If enabled, the performance hit is drastic on large collections.
	///

	protected boolean aggressiveKeyFlush = false;
	protected boolean useThreadSafeCollections = true;
	
	protected boolean hasAuthorization = false;
	
	protected String systemRoleNameReader = null;
	protected String systemRoleNameAdministrator = null;
	
	/*
	protected NameEnumType[] authorizationActors = new NameEnumType[]{NameEnumType.ACCOUNT, NameEnumType.PERSON, NameEnumType.USER};
	*/
	/// 2014/12/26
	/// bit indicating to use the urn from the database
	/// this will be disabled because when tree structures are moved,
	/// it would require a reevalutation for everything below that branch
	/// At the moment, the NameIdGroupFactory read operation for group needs to be refactored so the urn can be successfully rebuilt while the object is being reconstituted,
	/// without having to make changes to every factory
	protected boolean usePersistedUrn = true;
	
	public NameIdFactory(){
		super();
		setUseThreadSafeCollections(true);
	}
	

	public boolean isClusterByGroup() {
		return clusterByGroup;
	}


	public void setClusterByGroup(boolean clusterByGroup) {
		this.clusterByGroup = clusterByGroup;
	}


	public boolean isClusterByParent() {
		return clusterByParent;
	}


	public void setClusterByParent(boolean clusterByParent) {
		this.clusterByParent = clusterByParent;
	}


	public boolean isParticipation() {
		return isParticipation;
	}


	public void setParticipation(boolean isParticipation) {
		this.isParticipation = isParticipation;
	}


	public String getSystemRoleNameReader() {
		return systemRoleNameReader;
	}

	public String getSystemRoleNameAdministrator() {
		return systemRoleNameAdministrator;
	}


	public boolean isUseThreadSafeCollections() {
		return useThreadSafeCollections;
	}

	public boolean isAggressiveKeyFlush() {
		return aggressiveKeyFlush;
	}

	public void setAggressiveKeyFlush(boolean aggressiveKeyFlush) {
		this.aggressiveKeyFlush = aggressiveKeyFlush;
	}

	public void setUseThreadSafeCollections(boolean useThreadSafeCollections) {
		this.useThreadSafeCollections = useThreadSafeCollections;
		if(useThreadSafeCollections){
			typeNameIdMap = Collections.synchronizedMap(new HashMap<Long,String>());
			typeNameMap = Collections.synchronizedMap(new HashMap<String,Integer>());
			typeIdMap = Collections.synchronizedMap(new HashMap<Long,Integer>());
			typeObjectIdMap = Collections.synchronizedMap(new HashMap<String,Integer>());

			typeMap = Collections.synchronizedList(new ArrayList<NameIdType>());
		}
		else{
			typeNameIdMap = new HashMap<Long,String>();
			typeNameMap = new HashMap<String,Integer>();
			typeIdMap = new HashMap<Long,Integer>();
			typeObjectIdMap = new HashMap<String,Integer>();
			typeMap = new ArrayList<NameIdType>();
		}
		/// Invoke clear cache to set the expiration
		clearCache();
	}

	public <T> boolean add(T object) throws ArgumentException, FactoryException{
		throw new FactoryException("Add method must be overriden for " + this.factoryType.toString() + ".");
	}
	
	public <T> boolean delete(T object) throws ArgumentException, FactoryException{
		throw new FactoryException("Delete method must be overriden for " + this.factoryType.toString() + ".");
	}
	
	public int deleteByOrganization(long organizationId) throws FactoryException, ArgumentException
	{
		return deleteByField(new QueryField[] {  }, organizationId);
	}
	
	public int deleteByOwner(UserType owner) throws FactoryException, ArgumentException
	{
		return deleteByField(new QueryField[] { QueryFields.getFieldOwner(owner.getId()) }, owner.getOrganizationId());
	}
	public <T> List<T> listByOwner(UserType user) throws FactoryException, ArgumentException
	{
		return list(new QueryField[]{QueryFields.getFieldOwner(user.getId())},user.getOrganizationId());
	}
	public <T> T clone(T source) throws FactoryException{
		throw new FactoryException("Clone method must be overriden for " + this.factoryType.toString() + ".  Yes, this could be abstract, but not every factory needs it.");
	}
	public <T> void normalize(T object) throws ArgumentException, FactoryException{
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdType obj = (NameIdType)object;
		if(obj.getNameType() == NameEnumType.ORGANIZATION){
			logger.warn("Skip normalization of organization type.  Moving organizations is not currently supported.");
			return;
		}
		if(obj.getNameType() == NameEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object type");	
		}
		if(obj.getOrganizationPath() == null || obj.getOrganizationPath().length() == 0){
			logger.debug("Organization path not defined");
			return;
		}
		OrganizationType org = Factories.getOrganizationFactory().findOrganization(obj.getOrganizationPath());
		if(org == null){
			throw new ArgumentException("Invalid organization path '" + obj.getOrganizationPath() + "'");
		}
		obj.setOrganizationId(org.getId());
	}
	public <T> void denormalize(T object) throws ArgumentException, FactoryException{
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdType obj = (NameIdType)object;
		if((obj.getNameType() != NameEnumType.ORGANIZATION && obj.getOrganizationId().compareTo(0L) == 0) || obj.getNameType() == NameEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object organization or type");	
		}
		if(obj.getOrganizationPath() == null){

			if(obj.getNameType() == NameEnumType.ORGANIZATION){
				obj.setOrganizationPath(Factories.getOrganizationFactory().getOrganizationPath(obj.getId()));
			}
			else{
				obj.setOrganizationPath(Factories.getOrganizationFactory().getOrganizationPath(obj.getOrganizationId()));
			}
		}
	}
	public <T> void populate(T object) throws FactoryException,ArgumentException{
		logger.debug("Populate method should be overriden for " + this.factoryType.toString() + ".  Yes, this could be abstract, but not every factory needs it.");
		NameIdType obj = (NameIdType)object;
		if(obj.getPopulated() == false){
			obj.setPopulated(true);
			updateToCache(obj);
		}
	}
	public <T> void depopulate(T object) throws FactoryException,ArgumentException{
		logger.debug("Depopulate method should be overriden for " + this.factoryType.toString() + ".  Yes, this could be abstract, but not every factory needs it.");
		NameIdType obj = (NameIdType)object;
		obj.setPopulated(false);
		updateToCache(obj);
	}	
	public void mapBulkIds(NameIdType map){
		long tmpId = 0L;
		if(hasOwnerId && map.getOwnerId() < 0L){
			tmpId = BulkFactories.getBulkFactory().getMappedId(map.getOwnerId());
			if(tmpId > 0L) map.setOwnerId(tmpId);
		}
		if(hasParentId && map.getParentId() < 0L){
			tmpId = BulkFactories.getBulkFactory().getMappedId(map.getParentId());
			//logger.debug("Map parentId " + map.getParentId() + " to " + tmpId);
			if(tmpId > 0L) map.setParentId(tmpId);
		}
	}

	public <T >boolean update(T obj) throws FactoryException
	{
		return update(obj, null);
	}
	
	public <T> boolean update(T obj, ProcessingInstructionType instruction) throws FactoryException
	{
		if(this.bulkMode == true){
			return true;
		}
		NameIdType map = (NameIdType)obj;
		DataTable table = dataTables.get(0);
		if(instruction == null)
			instruction = new ProcessingInstructionType();

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		List<QueryField> queryFields = new ArrayList<QueryField>();
		List<QueryField> updateFields = new ArrayList<QueryField>();

		queryFields.add(QueryFields.getFieldId(map));
		if(scopeToOrganization){
			queryFields.add(QueryFields.getFieldOrganization(map.getOrganizationId()));
		}
		setNameIdFields(updateFields, map);
		setFactoryFields(updateFields, map, instruction);
		String sql = getUpdateTemplate(table, updateFields.toArray(new QueryField[0]), token) + " WHERE " + getQueryClause(instruction,queryFields.toArray(new QueryField[0]), token);
		
		updateFields.addAll(queryFields);
		
		int updated = 0;
		try{
			PreparedStatement statement = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(updateFields.toArray(new QueryField[0]), statement);
			updated = statement.executeUpdate();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error("Trace",sqe);
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error("Trace",e);
			}
		}		
		return (updated > 0);
	}
	public <T> boolean updateBulk(List<T> map) throws FactoryException
	{
		return updateBulk(map, null);
	}
	public <T> boolean updateBulk(List<T> map, ProcessingInstructionType instruction) throws FactoryException
	{
		if(this.bulkMode == false) throw new FactoryException("Factory is not configured for bulk operation");
		DataTable table = dataTables.get(0);
		if(instruction == null) instruction = new ProcessingInstructionType();
		if(map.isEmpty()){
			return false;
		}

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		List<QueryField> queryFields = new ArrayList<QueryField>();
		List<QueryField> updateFields = new ArrayList<QueryField>();
		
		int maxBatchSize = 250;
		int batch = 0;
		int updated = 0;
		try{
			boolean lastCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			queryFields.add(QueryFields.getFieldId((NameIdType)map.get(0)));
			if(scopeToOrganization){
				queryFields.add(QueryFields.getFieldOrganization(((NameIdType)map.get(0)).getOrganizationId()));
			}

			setNameIdFields(updateFields, (NameIdType)map.get(0));
			setFactoryFields(updateFields, (NameIdType)map.get(0), instruction);
			String sql = null;
			PreparedStatement statement = null;
			for(int i = 0; i < map.size(); i++){
				
				queryFields.clear();
				updateFields.clear();
				queryFields.add(QueryFields.getFieldId((NameIdType)map.get(i)));
				if(scopeToOrganization){
					queryFields.add(QueryFields.getFieldOrganization(((NameIdType)map.get(i)).getOrganizationId()));
				}
				setNameIdFields(updateFields, (NameIdType)map.get(i));
				setFactoryFields(updateFields, (NameIdType)map.get(i), instruction);
				if(i == 0){
					sql = getUpdateTemplate(table, updateFields.toArray(new QueryField[0]), token) + " WHERE " + getQueryClause(instruction,queryFields.toArray(new QueryField[0]), token);
					statement = connection.prepareStatement(sql);
				}
				updateFields.addAll(queryFields);
				DBFactory.setStatementParameters(updateFields.toArray(new QueryField[0]), statement);
				statement.addBatch();
				updated++;
				if(batch++ >= maxBatchSize){
					logger.debug("Execute bulk update batch: " + batch);
					statement.executeBatch();
					statement.clearBatch();
					batch=0;
				}
				

				
			}
			if(batch > 0){
				logger.debug("Execute last bulk update batch: " + batch);
				statement.executeBatch();
				statement.clearBatch();
				batch=0;
			}
			connection.commit();
			connection.setAutoCommit(lastCommit);
			
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error("Trace",sqe);
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error("Trace",e);
			}
		}		
		return (updated > 0);
	}

	public <T> boolean deleteBulk(List<T> map, ProcessingInstructionType instruction) throws FactoryException
	{
		if(this.bulkMode == false) throw new FactoryException("Factory is not configured for bulk operation");
		DataTable table = dataTables.get(0);
		if(instruction == null) instruction = new ProcessingInstructionType();
		if(map.isEmpty()){
			return false;
		}

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		List<QueryField> queryFields = new ArrayList<QueryField>();
		
		int maxBatchSize = 500;
		int batch = 0;
		int deleted = 0;
		try{
			boolean lastCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			/*
			queryFields.add(QueryFields.getFieldId((NameIdType)map.get(0)));
			if(scopeToOrganization){
				queryFields.add(QueryFields.getFieldOrganization(((NameIdType)map.get(0)).getOrganizationId()));
			}
			*/
			String sql = null;
			PreparedStatement statement = null;
			for(int i = 0; i < map.size(); i++){
				
				queryFields.clear();
				queryFields.add(QueryFields.getFieldId((NameIdType)map.get(i)));
				if(scopeToOrganization){
					queryFields.add(QueryFields.getFieldOrganization(((NameIdType)map.get(i)).getOrganizationId()));
				}
				if(i == 0){
					sql = "DELETE FROM " + table.getName() + " WHERE " + getQueryClause(instruction,queryFields.toArray(new QueryField[0]), token);
					statement = connection.prepareStatement(sql);
				}

				DBFactory.setStatementParameters(queryFields.toArray(new QueryField[0]), statement);
				statement.addBatch();
				deleted++;
				if(batch++ >= maxBatchSize){
					logger.debug("Execute bulk update batch: " + batch);
					statement.executeBatch();
					statement.clearBatch();
					batch=0;
				}
				

				
			}
			if(batch > 0){
				logger.debug("Execute last bulk update batch: " + batch);
				statement.executeBatch();
				statement.clearBatch();
				batch=0;
			}
			connection.commit();
			connection.setAutoCommit(lastCommit);
			
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error("Trace",sqe);
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error("Trace",e);
			}
		}		
		return (deleted > 0);
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
			if(scopeToOrganization) fields.add(QueryFields.getFieldOrganization(map.getOrganizationId()));
	}
	protected DataRow prepareAdd(NameIdType obj, String tableName) throws FactoryException{
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
				throw new FactoryException("Generated urn value is null for object " + obj.getNameType().toString() + " " + obj.getName());
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
			if (scopeToOrganization) row.setCellValue("organizationid", obj.getOrganizationId());
		}
		catch(DataAccessException dae){
			logger.error(this.factoryType.toString() + ": " + dae.getMessage());
			throw new FactoryException(dae.getMessage());
		}
		return row;
	}
	public <T> T getByNameInParent(String name, long parent_id, long organization_id) throws FactoryException, ArgumentException
	{

		String key_name = name + "-" + parent_id ;

		T obj = readCache(key_name);
		if (obj != null)
			return obj;

		List<NameIdType> objs = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldParent(parent_id) }, organization_id);

		if (objs.isEmpty() == false)
		{
			addToCache(objs.get(0),key_name);
			obj = (T)objs.get(0);
		}
		return obj;
	}
	public <T> T getByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		if(hasName == false) throw new FactoryException("Factory does not support object name");
		T out_obj = readCache(name);
		if (out_obj != null)
			return out_obj;

		List<NameIdType> obj_list = getByField(new QueryField[] { QueryFields.getFieldName(name) }, organizationId);

		if (obj_list.isEmpty() == false)
		{
			
			String key_name = getCacheKeyName(obj_list.get(0));
			addToCache(obj_list.get(0),key_name);
			out_obj = (T)obj_list.get(0);
		}
		return out_obj;
	}
	public <T> List<T> listByName(String name, long organization_id) throws FactoryException, ArgumentException
	{
		if (!hasName) throw new FactoryException("Table " + dataTables.get(0).getName() + " Does not define a Name");
		return convertList(getByField(QueryFields.getFieldName(name), organization_id));
	}

	protected long getIdByName(String name, long organization_id) throws FactoryException
	{
		if (!hasName) throw new FactoryException("Table " + dataTables.get(0).getName() + " does not define a Name");
		long[] ids = getIdByField("name", SqlDataEnumType.VARCHAR, name, organization_id);
		if (ids.length > 0) return ids[0];
		return 0;
	}
	
	public List<NameIdType> getByField(QueryField field, long organization_id) throws FactoryException, ArgumentException{
		return getByField(field, null, organization_id);
	}
	public List<NameIdType> getByField(QueryField field, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException{
		return getByField(new QueryField[]{field}, instruction, organization_id);
	}
	
	public List<NameIdType> getByField(QueryField[] fields, long organization_id) throws FactoryException, ArgumentException{
		return getByField(fields, null, organization_id);
	}
	public List<NameIdType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException{
		List<NameIdType> out_list = new ArrayList<NameIdType>();

		if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");

		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = this.dataTables.get(0);
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, organization_id);
		//logger.debug(sqlQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				NameIdType obj = this.read(rset, instruction);
				out_list.add(obj);
			}
			rset.close();
			
		} catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error("Trace",e);
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error("Trace",e);
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
		//logger.info("Reading object " + obj.getId());
		if(obj.getNameType() == null) obj.setNameType(NameEnumType.UNKNOWN);
		if(hasObjectId) obj.setObjectId(rset.getString("objectid"));
		if(hasName) obj.setName(rset.getString("name"));
		if(hasOwnerId) obj.setOwnerId(rset.getLong("ownerid"));
		else obj.setOwnerId(0L);
		if(hasParentId) obj.setParentId(rset.getLong("parentid"));
		else obj.setParentId(0L);
		if(scopeToOrganization){
			long org_id = rset.getLong("organizationid");
			obj.setOrganizationId(org_id);
			//obj.setOrganization(Factories.getOrganizationFactory().getOrganizationById(org_id));
			obj.setOrganizationPath(Factories.getOrganizationFactory().getOrganizationPath(obj.getOrganizationId()));
		}
		if(hasUrn && usePersistedUrn == true){
			obj.setUrn(rset.getString("urn"));
		}

		return obj;
	}
	public <T> T getByObjectId(String id, long organizationId) throws FactoryException, ArgumentException
	{
		if(hasObjectId == false) throw new FactoryException("Factory does not support object id");
		T out_obj = readCache(id);
		if (out_obj != null)
			return out_obj;

		List<NameIdType> obj_list = getByField(new QueryField[] { QueryFields.getFieldObjectId(id) }, organizationId);

		if (obj_list.isEmpty() == false)
		{
			
			String key_name = getCacheKeyName(obj_list.get(0));
			addToCache(obj_list.get(0),key_name);
			out_obj = (T)obj_list.get(0);
		}
		return out_obj;
	}
	public <T> T getById(long id, long organizationId) throws FactoryException, ArgumentException
	{
		T out_obj = readCache(id);
		if (out_obj != null)
			return out_obj;

		List<NameIdType> obj_list = getByField(new QueryField[] { QueryFields.getFieldId(id) }, organizationId);

		if (obj_list.isEmpty() == false)
		{
			
			String key_name = getCacheKeyName(obj_list.get(0));
			addToCache(obj_list.get(0),key_name);
			out_obj = (T)obj_list.get(0);
		}
		return out_obj;
	}
	
	public List<QueryField> buildSearchQuery(String searchValue, long organizationId) throws FactoryException{
		searchValue = searchValue.replaceAll("\\*","%");
		
		List<QueryField> filters = new ArrayList<>();
		QueryField search_filters = new QueryField(SqlDataEnumType.NULL,"searchgroup",null);
		search_filters.setComparator(ComparatorEnumType.GROUP_OR);
		QueryField name_filter = new QueryField(SqlDataEnumType.VARCHAR,"name",searchValue);
		name_filter.setComparator(ComparatorEnumType.LIKE);
		search_filters.getFields().add(name_filter);
		QueryField description_filter = new QueryField(SqlDataEnumType.VARCHAR,"description",searchValue);
		description_filter.setComparator(ComparatorEnumType.LIKE);
		search_filters.getFields().add(description_filter);
		filters.add(search_filters);
		return filters;
	}
	
	public <T> List<T> search(String searchValue, long startRecord, int recordCount, long org) throws FactoryException, ArgumentException{
		ProcessingInstructionType instruction = null;
		if(startRecord >= 0 && recordCount >= 0){
			instruction = new ProcessingInstructionType();
			instruction.setOrderClause("name ASC");
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		List<QueryField> fields = buildSearchQuery(searchValue, org);
		return search(fields.toArray(new QueryField[0]), instruction,org);
	}

	public <T> List<T> search(QueryField[] fields, ProcessingInstructionType pi, long organizationId) throws FactoryException, ArgumentException{

		return list(fields, pi, organizationId);
	}
	
	protected <T> List<T> searchByIdInView(String viewName, QueryField[] filters, ProcessingInstructionType instruction, long organizationId){
		return searchByIdInView(viewName, "id", filters, instruction, organizationId);
	}
	protected <T> List<T> searchByIdInView(String viewName, String idCol, QueryField[] filters, ProcessingInstructionType instruction, long organizationId){

		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String sqlQuery = assembleQueryString("SELECT " + idCol + " FROM " + viewName, filters, connectionType, instruction, organizationId);

		List<Long> ids = new ArrayList<>();
		List<T> objs = new ArrayList<>();
		
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
			objs = listByIds(ArrayUtils.toPrimitive(ids.toArray(new Long[0])),pi2,organizationId);
			logger.info("Retrieved " + objs.size() + " from " + ids.size() + " ids");
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error("Trace",sqe);
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error("Trace",e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Trace",e);
		}
		finally{
			
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error("Trace",e);
			}
		}
		return objs;
	}
	public <T> List<T> list(QueryField[] fields, ProcessingInstructionType pi, long organizationId) throws FactoryException, ArgumentException
	{

		List<NameIdType> user_list = getByField(fields,pi, organizationId);
		return convertList(user_list);
	}	
	public <T> List<T> list(QueryField[] fields, long organizationId) throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return list(fields, instruction, organizationId);
	}
	public <T> List<T>  paginateList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		instruction.setPaginate(false);
		return paginateList(fields, instruction, startRecord, recordCount, organizationId);
	}
	public <T> List<T>  paginateList(QueryField[] fields, ProcessingInstructionType instruction, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		if (instruction != null && startRecord >= 0L && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return list(fields, instruction, organizationId);
	}
	public <T> List<T> listByIds(long[] ids, long organizationId) throws FactoryException, ArgumentException
	{
		return listByIds(ids, null, organizationId);
	}
	public <T> List<T> listByIds(long[] ids, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException
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
				List<NameIdType> tmp_data_list = getByField(new QueryField[] { match }, instruction, organizationId);
				out_list.addAll(tmp_data_list);
				buff.delete(0,  buff.length());
			}
		}
		return convertList(out_list);
	}
	
	public <T> T getByUrn(String urn){
		//logger.debug("Reading object by urn " + urn);
		T obj = readCache(urn);
		if(obj != null){
			return obj;
		}
		try {
			//long organizationId = UrnUtil.getOrganization(urn);
			//List<T> objs = convertList(getByField(new QueryField[]{QueryFields.getFieldUrn(urn)},organizationId));
			List<T> objs = convertList(getByField(new QueryField[]{QueryFields.getFieldUrn(urn)},0L));
			if(objs.isEmpty() == false){
				obj = objs.get(0);
				addToCache((NameIdType)obj, getUrnCacheKey(obj));
				addToCache((NameIdType)obj);
			}
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error("Trace",e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Trace",e);
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
		typeObjectIdMap.clear();
		typeMap.clear();
		cacheExpires = System.currentTimeMillis() + (cacheExpiry * 60000);
	}
	
	protected void checkCacheExpires(){
		if(cacheExpires <= System.currentTimeMillis()){
			logger.debug("Expire cache");
			clearCache();
		}
	}
	public boolean haveCacheId(String id){
		return typeObjectIdMap.containsKey(id);
	}
	public boolean haveCacheId(long id){
		return typeIdMap.containsKey(id);
	}
	public void removeBranchFromCache(NameIdType obj){
		removeFromCache(obj);
		if(!hasParentId || obj.getParentId().compareTo(0L) == 0) return;
		try {
			NameIdType parent = getById(obj.getParentId(),obj.getOrganizationId());
			if(parent != null) removeBranchFromCache(parent);
		} catch (FactoryException e) {
			
			logger.error("Trace",e);
		} catch (ArgumentException e) {
			
			logger.error("Trace",e);
		}
	}
	public void removeFromCache(NameIdType obj){
		removeFromCache(obj, getCacheKeyName(obj));
	}

	
	public void removeFromCache(NameIdType obj, String key_name){
		synchronized(typeMap){
			
			if(key_name != null) typeNameMap.remove(key_name);
			
			/// 2015/01/12
			/// Aggressive flush is an expensive process
			/// It's useful for mixed bulk updates, such as adds + modifies
			///	A dirty condition happens when a new object with no parent, or a bulk parent id is added.  Once the parentid is assigned, the old key continues to exist and the cache gets corrupted.
			/// But it's also slower.  By default it's enabled for use in a Web setting where there are multiple concurent threads and dirty data
			/// But should be disabled for the fastest possible bulk operations - the bulk operations add and remove objects to the factory cache
			/// so this can greatly slow down large operations.
			///
			/// 2015/01/13 - Why not just make a list of keys per object id and not loop through the whole array?
			/// 2016/05/31 - The actual use of the keyName isn't specified after finding it, so this whole section, for aggressive as it is, is a waste
			///	The original issue persists, but the cleanup operation isn't right.  Switching the default to 'false' until the reproduction is identified again
			///
			if(aggressiveKeyFlush == true){
				//Iterator<String> keys = typeNameMap.keySet().iterator();
				
				NameIdType objC = null;
				//while(keys.hasNext()){
				
				for (Entry<String,Integer> entry : typeNameMap.entrySet()) {
					String key = entry.getKey();
					Integer index = entry.getValue();
					
					if((objC = typeMap.get(index)) != null && objC.getId() == obj.getId()){
						typeNameMap.remove(key);
						key_name = key;
						break;
					}
					
				}
				
			}
			
			//logger.debug("Remove from cache: " + key_name + ":" + typeNameMap.containsKey(key_name) + " and " + typeIdMap.containsKey(obj.getId()));
			//if(typeNameMap.containsKey(key_name) && typeIdMap.containsKey(obj.getId())){
			if(typeIdMap.containsKey(obj.getId())){
				int indexId = typeIdMap.get(obj.getId());
				//typeNameMap.remove(key_name);
				typeMap.set(indexId, null);
				typeIdMap.remove(obj.getId());
				if(hasObjectId) typeObjectIdMap.remove(obj.getObjectId());
				typeNameIdMap.remove(obj.getId());
			}
		}
	}
	
	public String[] getCacheKeys(){
		return typeNameMap.keySet().toArray(new String[0]);
	}
	
	public <T> T readCache(String name){
		checkCacheExpires();
		if(typeNameMap.containsKey(name)){
			return (T)typeMap.get(typeNameMap.get(name));
		}
		else if(typeObjectIdMap.containsKey(name)){
			return (T)typeMap.get(typeObjectIdMap.get(name));
		}
		return null;
	}

	public <T> T readCache(long id){

		checkCacheExpires();
		if(typeIdMap.containsKey(id)){
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
	public boolean updateToCache(NameIdType obj) throws ArgumentException{
		return updateToCache(obj, getCacheKeyName(obj));
	}
	public boolean updateToCache(NameIdType obj,String key_name) throws ArgumentException{
		if(this.haveCacheId(obj.getId())) removeFromCache(obj,key_name);
		return addToCache(obj, key_name);
	}

	public boolean addToCache(NameIdType map) throws ArgumentException{

		return addToCache(map,getCacheKeyName(map));
	}
	public boolean addToCache(NameIdType map, String key_name) throws ArgumentException{
		if(key_name == null) throw new ArgumentException("Key name is null");
		if(map == null){
			logger.error("Map with key '" + key_name + "' is null");
			return false;
		}
		int length = typeMap.size();
		if(typeNameMap.containsKey(key_name) || typeIdMap.containsKey(map.getId())){
			return false;
		}
		typeMap.add(map);
		typeNameMap.put(key_name, length);
		typeIdMap.put(map.getId(), length);
		if(hasObjectId) typeObjectIdMap.put(map.getObjectId(), length);
		typeNameIdMap.put(map.getId(), map.getName());

		return true;
	}
	protected boolean isValid(NameIdType map)
	{
		if (map == null || map.getId() <= 0L || map.getOrganizationId() <= 0L) return false;
		return true;
	}
	public int countInOrganization(long organization_id) throws FactoryException
	{
		return getCountByField(this.getDataTables().get(0), new QueryField[]{}, organization_id);
	}
	public <T> int countInParent(T parent) throws FactoryException
	{
		NameIdType obj = (NameIdType)parent;
		return getCountByField(this.getDataTables().get(0), new QueryField[]{QueryFields.getFieldParent(obj.getId())}, obj.getOrganizationId());
	}
	
}
