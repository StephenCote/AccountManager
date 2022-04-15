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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public abstract class NameIdFactory extends FactoryBase implements INameIdFactory{
	public static final Logger logger = LogManager.getLogger(NameIdFactory.class);
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
	protected boolean isVaulted = false;
	/// 2016/05/31 - Switching the default of aggressive flush to 'false' until the reproduction is identified again
	/// The cleanup routine wasn't actually doing anything for all the time it wasted.
	/// If enabled, the performance hit is drastic on large collections.
	///

	protected boolean aggressiveKeyFlush = false;
	protected boolean useThreadSafeCollections = true;
	
	protected boolean hasAuthorization = false;
	
	protected String systemRoleNameReader = null;
	protected String systemRoleNameWriter = null;
	protected String systemRoleNameAdministrator = null;
	
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
	

	public boolean isVaulted() {
		return isVaulted;
	}


	public void setVaulted(boolean isVaulted) {
		this.isVaulted = isVaulted;
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
			typeNameIdMap = Collections.synchronizedMap(new HashMap<>());
			typeNameMap = Collections.synchronizedMap(new HashMap<>());
			typeIdMap = Collections.synchronizedMap(new HashMap<>());
			typeObjectIdMap = Collections.synchronizedMap(new HashMap<>());

			typeMap = Collections.synchronizedList(new ArrayList<NameIdType>());
		}
		else{
			typeNameIdMap = new HashMap<>();
			typeNameMap = new HashMap<>();
			typeIdMap = new HashMap<>();
			typeObjectIdMap = new HashMap<>();
			typeMap = new ArrayList<>();
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
		throw new FactoryException("Clone method must be overriden for " + this.factoryType.toString());
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
		OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(obj.getOrganizationPath());
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
				obj.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(obj.getId()));
			}
			else{
				obj.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(obj.getOrganizationId()));
			}
		}
	}
	public <T> void populate(T object) throws FactoryException,ArgumentException{
		logger.debug("Populate method should be overriden for " + this.factoryType.toString() + ".  Yes, this could be abstract, but not every factory needs it.");
		NameIdType obj = (NameIdType)object;
		if(!obj.getPopulated()){
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
		long tmpId;
		if(hasOwnerId && map.getOwnerId() < 0L){
			tmpId = BulkFactories.getBulkFactory().getMappedId(map.getOwnerId());
			if(tmpId > 0L) map.setOwnerId(tmpId);
		}
		if(hasParentId && map.getParentId() < 0L){
			tmpId = BulkFactories.getBulkFactory().getMappedId(map.getParentId());
			if(tmpId > 0L) map.setParentId(tmpId);
		}
	}

	public <T >boolean update(T obj) throws FactoryException
	{
		return update(obj, null);
	}
	
	public <T> boolean update(T obj, ProcessingInstructionType instruction) throws FactoryException
	{
		if(this.bulkMode){
			return true;
		}
		NameIdType map = (NameIdType)obj;
		DataTable table = dataTables.get(0);
		if(instruction == null)
			instruction = new ProcessingInstructionType();

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		List<QueryField> queryFields = new ArrayList<>();
		List<QueryField> updateFields = new ArrayList<>();

		queryFields.add(QueryFields.getFieldId(map));
		if(scopeToOrganization){
			queryFields.add(QueryFields.getFieldOrganization(map.getOrganizationId()));
		}
		setNameIdFields(updateFields, map);
		setFactoryFields(updateFields, map, instruction);
		String sql = getUpdateTemplate(table, updateFields.toArray(new QueryField[0]), token) + " WHERE " + getQueryClause(instruction,queryFields.toArray(new QueryField[0]), token);
		
		updateFields.addAll(queryFields);
		
		int updated = 0;
		PreparedStatement statement = null;
		try{
			statement = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(updateFields.toArray(new QueryField[0]), statement);
			updated = statement.executeUpdate();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,sqe);
		}
		finally{
			try {
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.TRACE_EXCEPTION,e);
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
		if(!this.bulkMode) throw new FactoryException("Factory is not configured for bulk operation");
		DataTable table = dataTables.get(0);
		if(instruction == null) instruction = new ProcessingInstructionType();
		if(map.isEmpty()){
			return false;
		}

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		List<QueryField> queryFields = new ArrayList<>();
		List<QueryField> updateFields = new ArrayList<>();
		
		int maxBatchSize = 250;
		int batch = 0;
		int updated = 0;
		PreparedStatement statement = null;
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
			logger.error(FactoryException.TRACE_EXCEPTION,sqe);
		}
		finally{
			try {
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.TRACE_EXCEPTION,e);
			}
		}		
		return (updated > 0);
	}

	public <T> boolean deleteBulk(List<T> map, ProcessingInstructionType instruction) throws FactoryException
	{
		if(!this.bulkMode) throw new FactoryException("Factory is not configured for bulk operation");
		DataTable table = dataTables.get(0);
		if(instruction == null) instruction = new ProcessingInstructionType();
		if(map.isEmpty()){
			return false;
		}

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		List<QueryField> queryFields = new ArrayList<>();
		
		int maxBatchSize = 500;
		int batch = 0;
		int deleted = 0;
		PreparedStatement statement = null;
		try{
			boolean lastCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			String sql = null;

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
			logger.error(FactoryException.TRACE_EXCEPTION,sqe);
		}
		finally{
			try {
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.TRACE_EXCEPTION,e);
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
			if(isVaulted) {
				fields.add(QueryFields.getFieldKeyId(map.getKeyId()));
				fields.add(QueryFields.getFieldVaultId(map.getVaultId()));
				fields.add(QueryFields.getFieldVaulted(map.getVaulted()));
				fields.add(QueryFields.getFieldEnciphered(map.getEnciphered()));
			}
			if(hasUrn){
				map.setUrn(UrnUtil.getUrn(map));
				if(map.getUrn() == null){
					logger.error("Urn value is null for object " + map.getNameType().toString() + " " + map.getName());
				}
				fields.add(QueryFields.getFieldUrn(map));
			}
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
			if(bulkMode && obj.getId() > 0) row.setCellValue(Columns.get(ColumnEnumType.ID), obj.getId());
			if(hasObjectId) row.setCellValue(Columns.get(ColumnEnumType.OBJECTID), obj.getObjectId());
			if(hasName) row.setCellValue(Columns.get(ColumnEnumType.NAME), obj.getName());
			if(hasUrn) row.setCellValue(Columns.get(ColumnEnumType.URN), obj.getUrn());
			if(hasParentId) row.setCellValue(Columns.get(ColumnEnumType.PARENTID),obj.getParentId());
			if(hasOwnerId) row.setCellValue(Columns.get(ColumnEnumType.OWNERID),obj.getOwnerId());
			if (scopeToOrganization) row.setCellValue(Columns.get(ColumnEnumType.ORGANIZATIONID), obj.getOrganizationId());
			if(isVaulted) {
				row.setCellValue(Columns.get(ColumnEnumType.VAULTID),obj.getVaultId());
				row.setCellValue(Columns.get(ColumnEnumType.KEYID), obj.getKeyId());
				row.setCellValue(Columns.get(ColumnEnumType.ISVAULTED), obj.getVaulted());
				row.setCellValue(Columns.get(ColumnEnumType.ISENCIPHERED), obj.getEnciphered());
			}
		}
		catch(DataAccessException dae){
			logger.error(this.factoryType.toString() + ": " + dae.getMessage());
			throw new FactoryException(dae.getMessage());
		}
		return row;
	}
	public <T> List<T> listInParent(long parentId, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException{
		if(clusterByParent) {
			List<QueryField> fields = new ArrayList<>();
			fields.add(QueryFields.getFieldParent(parentId));
			return paginateList(fields.toArray(new QueryField[0]), startRecord, recordCount, organizationId);
		}
		else throw new FactoryException("Method not supported for this factory type");
	}
	public <T> List<T> listInParent(String type, long parentId, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException{
		if(type == null || type.equals("UNKNOWN")) {
			return listInParent(parentId, startRecord, recordCount, organizationId);
		}
		else throw new FactoryException("Method must be overridden by implementing class to define type enum");
	}
	public <T> T getByNameInParent(String name, String type, long parentId, long organizationId) throws FactoryException, ArgumentException
	{
		throw new FactoryException("Method must be overridden by implementing class to define type enum");
	}
	@SuppressWarnings("unchecked")
	public <T> T getByNameInParent(String name, long parentId, long organizationId) throws FactoryException, ArgumentException
	{

		String keyName = name + "-" + parentId ;

		T obj = readCache(keyName);
		if (obj != null)
			return obj;

		List<NameIdType> objs = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldParent(parentId) }, organizationId);

		if (!objs.isEmpty())
		{
			addToCache(objs.get(0),keyName);
			obj = (T)objs.get(0);
		}
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		if(!hasName) throw new FactoryException("Factory does not support object name");
		/// This method is primarily used for looking up users by name (whereas organizations, roles, and permissions are usually discovered by parent)
		/// The remaining factories are based on group memembership
		/// 
		String keyName = name + "-0-" + organizationId;
		T outObj = readCache(keyName);
		if (outObj != null)
			return outObj;

		List<NameIdType> objList = getByField(new QueryField[] { QueryFields.getFieldName(name) }, organizationId);

		if (!objList.isEmpty())
		{
			
			keyName = getCacheKeyName(objList.get(0));
			addToCache(objList.get(0),keyName);
			outObj = (T)objList.get(0);
		}
		return outObj;
	}
	public <T> List<T> listByName(String name, long organizationId) throws FactoryException, ArgumentException
	{
		if (!hasName) throw new FactoryException("Table " + dataTables.get(0).getName() + " Does not define a Name");
		return convertList(getByField(QueryFields.getFieldName(name), organizationId));
	}

	protected long getIdByName(String name, long organizationId) throws FactoryException
	{
		if (!hasName) throw new FactoryException("Table " + dataTables.get(0).getName() + " does not define a Name");
		long[] ids = getIdByField(Columns.get(ColumnEnumType.NAME), SqlDataEnumType.VARCHAR, name, organizationId);
		if (ids.length > 0) return ids[0];
		return 0;
	}
	
	public List<NameIdType> getByField(QueryField field, long organizationId) throws FactoryException, ArgumentException{
		return getByField(field, null, organizationId);
	}
	public List<NameIdType> getByField(QueryField field, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException{
		return getByField(new QueryField[]{field}, instruction, organizationId);
	}
	
	public List<NameIdType> getByField(QueryField[] fields, long organizationId) throws FactoryException, ArgumentException{
		return getByField(fields, null, organizationId);
	}
	public List<NameIdType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException{
		List<NameIdType> outList = new ArrayList<>();

		if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");

		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = this.dataTables.get(0);
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, organizationId);

		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				NameIdType obj = this.read(rset, instruction);
				outList.add(obj);
			}
			rset.close();
			
		} catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.TRACE_EXCEPTION,e);
			}
		}
		
		return outList;
	}
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		throw new FactoryException("This is an artifact from java<->c#<->java conversions - should be an abstract class + interface, not an override");
	}

	protected NameIdType read(ResultSet rset, NameIdType obj) throws SQLException, FactoryException, ArgumentException
	{
		obj.setId(rset.getLong(Columns.get(ColumnEnumType.ID)));
		if(obj.getNameType() == null) obj.setNameType(NameEnumType.UNKNOWN);
		if(hasObjectId) obj.setObjectId(rset.getString(Columns.get(ColumnEnumType.OBJECTID)));
		if(hasName) obj.setName(rset.getString(Columns.get(ColumnEnumType.NAME)));
		if(hasOwnerId) obj.setOwnerId(rset.getLong(Columns.get(ColumnEnumType.OWNERID)));
		else obj.setOwnerId(0L);
		if(hasParentId) obj.setParentId(rset.getLong(Columns.get(ColumnEnumType.PARENTID)));
		else obj.setParentId(0L);
		if(scopeToOrganization){
			long orgId = rset.getLong(Columns.get(ColumnEnumType.ORGANIZATIONID));
			obj.setOrganizationId(orgId);
			obj.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(obj.getOrganizationId()));
		}
		if(isVaulted) {
			obj.setVaultId(rset.getString(Columns.get(ColumnEnumType.VAULTID)));
			obj.setKeyId(rset.getString(Columns.get(ColumnEnumType.KEYID)));
			obj.setVaulted(rset.getBoolean(Columns.get(ColumnEnumType.ISVAULTED)));
			obj.setEnciphered(rset.getBoolean(Columns.get(ColumnEnumType.ISENCIPHERED)));
		}
		if(hasUrn && usePersistedUrn){
			obj.setUrn(rset.getString(Columns.get(ColumnEnumType.URN)));
		}

		return obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getByObjectId(String id, long organizationId) throws FactoryException, ArgumentException
	{
		if(!hasObjectId) throw new FactoryException("Factory does not support object id");
		T outObj = readCache(id);
		if (outObj != null)
			return outObj;

		List<NameIdType> objList = getByField(new QueryField[] { QueryFields.getFieldObjectId(id) }, organizationId);

		if (objList.isEmpty() == false)
		{
			
			String keyName = getCacheKeyName(objList.get(0));
			addToCache(objList.get(0),keyName);
			outObj = (T)objList.get(0);
		}
		return outObj;
	}
	@SuppressWarnings("unchecked")
	public <T> T getById(long id, long organizationId) throws FactoryException, ArgumentException
	{
		T outObj = readCache(id);
		if (outObj != null)
			return outObj;

		List<NameIdType> objList = getByField(new QueryField[] { QueryFields.getFieldId(id) }, organizationId);

		if (!objList.isEmpty())
		{
			
			String keyName = getCacheKeyName(objList.get(0));
			addToCache(objList.get(0),keyName);
			outObj = (T)objList.get(0);
		}
		return outObj;
	}
	
	public List<QueryField> buildSearchQuery(String inSearchValue, long organizationId) throws FactoryException{
		String searchValue = inSearchValue.replaceAll("\\*","%");
		
		List<QueryField> filters = new ArrayList<>();
		QueryField searchFilters = new QueryField(SqlDataEnumType.NULL,"searchgroup",null);
		searchFilters.setComparator(ComparatorEnumType.GROUP_OR);
		QueryField nameFilter = new QueryField(SqlDataEnumType.VARCHAR,Columns.get(ColumnEnumType.NAME),searchValue);
		nameFilter.setComparator(ComparatorEnumType.LIKE);
		searchFilters.getFields().add(nameFilter);
		QueryField descriptionFilter = new QueryField(SqlDataEnumType.VARCHAR,"description",searchValue);
		descriptionFilter.setComparator(ComparatorEnumType.LIKE);
		searchFilters.getFields().add(descriptionFilter);
		filters.add(searchFilters);
		return filters;
	}
	
	public <T> List<T> search(String searchValue, long startRecord, int recordCount, long org) throws FactoryException, ArgumentException{
		ProcessingInstructionType instruction = null;
		if(startRecord >= 0 && recordCount >= 0){
			instruction = new ProcessingInstructionType();
			instruction.setOrderClause(Columns.get(ColumnEnumType.NAME) + " ASC");
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
		return searchByIdInView(viewName, Columns.get(ColumnEnumType.ID), filters, instruction, organizationId);
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
			logger.debug("Retrieved " + objs.size() + " from " + ids.size() + " ids");
		}
		catch(SQLException | FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		finally{
			
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.TRACE_EXCEPTION,e);
			}
		}
		return objs;
	}
	public <T> List<T> list(QueryField[] fields, ProcessingInstructionType pi, long organizationId) throws FactoryException, ArgumentException
	{

		List<NameIdType> userList = getByField(fields,pi, organizationId);
		return convertList(userList);
	}	
	public <T> List<T> list(QueryField[] fields, long organizationId) throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause(Columns.get(ColumnEnumType.NAME) + " ASC");
		return list(fields, instruction, organizationId);
	}
	public <T> List<T>  paginateList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause(Columns.get(ColumnEnumType.NAME) + " ASC");
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

		StringBuilder buff = new StringBuilder();
		
		List<NameIdType> outList = new ArrayList<>();
		for (int i = 0; i < ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(ids[i]);
			if ((i > 0 || ids.length == 1) && ((i % BulkFactories.bulkQueryLimit == 0) || i == ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, Columns.get(ColumnEnumType.ID), buff.toString());
				match.setComparator(ComparatorEnumType.ANY);
				List<NameIdType> tmpDataList = getByField(new QueryField[] { match }, instruction, organizationId);
				outList.addAll(tmpDataList);
				buff.delete(0,  buff.length());
			}
		}
		return convertList(outList);
	}
	
	public <T> T getByUrn(String urn){
		T obj = readCache(urn);
		if(obj != null){
			return obj;
		}
		try {
			List<T> objs = convertList(getByField(new QueryField[]{QueryFields.getFieldUrn(urn)},0L));
			if(objs.isEmpty() == false){
				obj = objs.get(0);
				addToCache((NameIdType)obj, getUrnCacheKey(obj));
				addToCache((NameIdType)obj);
			}
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return obj;
	}
	
	public void expireCache(){
		cacheExpires = 0;
	}
	
	public String reportCacheSize(){
		if(factoryType == FactoryEnumType.UNKNOWN){
			logger.warn(getDataTables().get(0).getName() + " doesn't define a factory type");
		}
		return this.factoryType.toString() + " Cache Report\n"
			+ "typeNameIdMap\t" + typeNameIdMap.keySet().size()
			+ "\ntypeNameMap\t" + typeNameMap.keySet().size()
			+ "\ntypeIdMap\t" + typeIdMap.keySet().size()
			+ "\ntypeObjectIdMap\t" + typeObjectIdMap.keySet().size()
			+ "\ntypeMap\t" + typeMap.size() + "\n"
		;
	}
	public synchronized void clearCache(){
		typeNameIdMap.clear();
		typeNameMap.clear();
		typeIdMap.clear();
		typeObjectIdMap.clear();
		typeMap.clear();
		cacheExpires = System.currentTimeMillis() + (cacheExpiry * 60000);
	}
	
	protected synchronized void checkCacheExpires(){
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
		} catch (FactoryException| ArgumentException e) {
			
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
	}
	public void removeFromCache(NameIdType obj){
		removeFromCache(obj, getCacheKeyName(obj));
	}

	
	public void removeFromCache(NameIdType obj, String keyName){
		synchronized(typeMap){
			
			if(keyName != null) typeNameMap.remove(keyName);
			
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
			if(aggressiveKeyFlush){
				NameIdType objC = null;
				for (Entry<String,Integer> entry : typeNameMap.entrySet()) {
					String key = entry.getKey();
					Integer index = entry.getValue();
					
					if((objC = typeMap.get(index)) != null && objC.getId().equals(obj.getId())){
						typeNameMap.remove(key);
						break;
					}
					
				}
				
			}
			
			if(typeIdMap.containsKey(obj.getId())){
				int indexId = typeIdMap.get(obj.getId());
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
	
	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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
	public synchronized boolean updateToCache(NameIdType obj,String keyName) throws ArgumentException{
		if(this.haveCacheId(obj.getId()) || (keyName != null && typeNameMap.containsKey(keyName))) removeFromCache(obj,keyName);
		return addToCache(obj, keyName);
	}

	public boolean addToCache(NameIdType map) throws ArgumentException{

		return addToCache(map,getCacheKeyName(map));
	}
	public synchronized boolean addToCache(NameIdType map, String keyName) throws ArgumentException{

		if(keyName == null) throw new ArgumentException("Key name is null");
		if(map == null){
			logger.error("Map with key '" + keyName + "' is null");
			return false;
		}
		int length = typeMap.size();
		if(typeNameMap.containsKey(keyName) || typeIdMap.containsKey(map.getId())){
			return false;
		}
		typeMap.add(map);
		typeNameMap.put(keyName, length);
		typeIdMap.put(map.getId(), length);
		if(hasObjectId) typeObjectIdMap.put(map.getObjectId(), length);
		typeNameIdMap.put(map.getId(), map.getName());
		return true;
	}
	
	public String getCacheReport(){
		StringBuilder buff = new StringBuilder();
		for(String key : typeNameMap.keySet()){
			buff.append(key + ": " + typeNameMap.get(key) + "\n");
		}
		return buff.toString();
	}
	
	protected boolean isValid(NameIdType map)
	{
		if (map == null || map.getId() <= 0L || map.getOrganizationId() <= 0L) return false;
		return true;
	}
	public int countInOrganization(long organizationId) throws FactoryException
	{
		return getCountByField(this.getDataTables().get(0), new QueryField[]{}, organizationId);
	}
	public <T> int countInParent(T parent) throws FactoryException
	{
		NameIdType obj = (NameIdType)parent;
		return getCountByField(this.getDataTables().get(0), new QueryField[]{QueryFields.getFieldParent(obj.getId())}, obj.getOrganizationId());
	}
	public <T> T makePath(String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return null;
	}
	public <T> T makePath(UserType user, String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException{
		return null;
	}
	public <T> T find(String path) throws FactoryException,ArgumentException
	{
		return find(null,null,path,0L);
	}
	public <T> T find(String type, String path, long organizationId) throws FactoryException, ArgumentException
	{
		return find(null,type,path,organizationId);
	}
	public <T> T find(UserType user, String type, String path, long organizationId) throws FactoryException, ArgumentException
	{
		logger.error("Invocation of abstract method");
		return null;
	}
	
}
