/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.io.BulkInsertUtil;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;

public abstract class FactoryBase {
	public static final Logger logger = LogManager.getLogger(FactoryBase.class);
	
	protected FactoryEnumType factoryType = FactoryEnumType.UNKNOWN;
	
	protected Map<String,Integer> dataTableMap = null;
	protected List<DataTable> dataTables = null;
	private DBFactory.CONNECTION_TYPE connectionType = DBFactory.CONNECTION_TYPE.UNKNOWN;
	protected List<String> tableNames = null;
	protected boolean scopeToOrganization = true;
	protected boolean canJoinToAttribute = false;
	private boolean initialized = false;
	protected String primaryTableName = null;
	protected String secondaryTableName = null;
	protected boolean bulkMode = false;
	private int batchSize = 250;
	private static boolean enableSchemaCache = false;
	private static String schemaCachePath = null;
	
	/// PostGres sequence name
	///
	protected String sequenceName = "orgid_id_seq";
	
	private Map<String,List<Long>> bulkMap = null;
	
	public static void setEnableSchemaCache(boolean enable){
		enableSchemaCache = enable;
	}
	public static void setSchemaCachePath(String path){
		schemaCachePath = path;
	}
	
	public FactoryBase()
	{
		dataTableMap = Collections.synchronizedMap(new HashMap<String,Integer>());
		tableNames = new ArrayList<>();
		dataTables = new ArrayList<>();
		bulkMap = new HashMap<>();
	}
	public FactoryEnumType getFactoryType(){
		return factoryType;
	}
	protected void addBulkId(String sessionId, long id){
		if(!bulkMap.containsKey(sessionId)) bulkMap.put(sessionId, new ArrayList<Long>());
		bulkMap.get(sessionId).add(id);
	}
	public Map<String,List<Long>> getBulkMap(){
		return bulkMap;
	}
	
	public void registerProvider(){
		
	}

	

	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	protected boolean insertRow(DataRow row){
		boolean outBool = false;

		if(!row.getTable().getBulkInsert()){
			try {
				outBool = BulkInsertUtil.insert(row);
			} catch (FactoryException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
			/// If the table is individual updates, then dump any rows accumulated into the table row set
			/// 
			row.getTable().getRows().clear();
		}
		else{
			if(bulkMode) row.getTable().getRows().add(row);
			outBool = true;
		}
		return outBool;
	}
	public boolean getBulkMode(){
		return bulkMode;
	}
	public String getDataTable(){
		return getDataTables().get(0).getName();
	}
	public void writeSpool(){
		writeSpool(getDataTable());
	}
	
	public void writeSpool(String tableName)
	{
		DataTable table = getDataTable(tableName);
		if (table.getBulkInsert())
		{
			synchronized(table){
				if(!BulkInsertUtil.insertBulk(table,batchSize)){
					logger.error("Error writing bulk spool.  NOTE: The bulk data set with the error will be cleared from memory.");
				}
				table.getRows().clear();
			}
		}
	}

	public boolean isInitialized() {
		return initialized;
	}
	public DataTable getDataTable(String name){
		if(!dataTableMap.containsKey(name))return null;
		return dataTables.get(dataTableMap.get(name));
	}
	public List<DataTable> getDataTables() {
		return dataTables;
	}
	
	public boolean isScopeToOrganization() {
		return scopeToOrganization;
	}
	protected String getSelectIdTemplate(DataTable table, ProcessingInstructionType instruction){
		return table.getSelectIdTemplate();
	}
	protected String getSelectNameTemplate(DataTable table, ProcessingInstructionType instruction){
		return table.getSelectNameTemplate();
	}

	protected String getSelectTemplate(DataTable table, ProcessingInstructionType instruction){
		if(instruction != null && instruction.getJoinAttribute()) return table.getSelectFullAttributeTemplate();
		else return table.getSelectFullTemplate();
	}
	public void initialize(Connection connection) throws FactoryException{
		dataTableMap.clear();
		dataTables.clear();
		int len = tableNames.size();
		boolean error = false;
		connectionType = DBFactory.getConnectionType(connection);
		if(len == 0){
			throw new FactoryException("At least one table must be specified");
		}
		
		for(int i = 0; i < len; i++){
			String tableName = tableNames.get(i);
			
			DataTable table = null;
			
			String schemaCacheFile = schemaCachePath + "/schema." + (bulkMode ? "bulk." : "") + tableName + ".json";
			
			if(enableSchemaCache && (new File(schemaCacheFile)).exists()){
				table = JSONUtil.importObject(FileUtil.getFileAsString(schemaCacheFile), DataTable.class);
				dataTableMap.put(tableName, i);
				dataTables.add(table);
				continue;
			}
			
			try{
				table = DBFactory.getDataTable(connection, tableName);
			}
			catch(DataAccessException dae){
				logger.error(dae.getMessage());
				error = true;
			}
			finally{
				if(error) break;
			}

			String alias = tableName.substring(0, 1);

			configureTableRestrictions(table);
			dataTableMap.put(tableName, i);
			dataTables.add(table);
			
			StringBuilder buff = new StringBuilder();
			StringBuilder aliasBuff = new StringBuilder();
			String lockHint = DBFactory.getNoLockHint(connectionType);

			buff.append("SELECT #TOP# ");
			aliasBuff.append("SELECT #TOP# ");
			int scount = 0;
			for (int c = 0; c < table.getColumnSize(); c++)
			{
				DataColumnType column = table.getColumns().get(c);
				if (table.getCanSelectColumn(column.getColumnName()))
				{
					if (scount > 0){
						buff.append(",");
						aliasBuff.append(",");
					}
					buff.append(column.getColumnName());
					aliasBuff.append(alias + "." + column.getColumnName());
					scount++;
				}
			}

			String attributeClause = " INNER JOIN Attribute ATR on ATR.referenceId = " + alias + ".id AND ATR.referenceType = '" + factoryType.toString() + "' AND ATR.organizationId = " + alias + ".organizationId";
			String tableClause = " FROM " + tableName + lockHint;
			table.setSelectFullTemplate(buff.toString() + " #PAGE# " + tableClause);
			table.setSelectFullAttributeTemplate(aliasBuff.toString() + " #PAGE# FROM " + tableName + " " + alias + " " + lockHint + attributeClause);
			table.setSelectIdTemplate("SELECT id" + tableClause);
			table.setSelectAggregateTemplate("SELECT %AGGREGATE%" + tableClause);
			table.setSelectNameTemplate("SELECT name" + tableClause);
			
			if(enableSchemaCache){
				logger.info("Caching schema to " + schemaCacheFile);
				FileUtil.emitFile(schemaCacheFile,JSONUtil.exportObject(table));
			}

		
		}
		
		if(!error) initialized = true;
	}
	public String getUpdateTemplate(DataTable table, QueryField[] updateFields, String token) throws FactoryException {
		if(updateFields.length == 0) throw new FactoryException("Empty field list");
		StringBuilder buff = new StringBuilder();
		buff.append("UPDATE " + table.getName() +" SET ");
		for(int i = 0; i < updateFields.length; i++){
			if(i > 0) buff.append(", ");
			buff.append(updateFields[i].getName() + "=" + token);
		}
		return buff.toString();
	}
	

	protected String[] getNamesByField(QueryField[] fields, long organizationId) throws FactoryException
	{
		List<String> outInts = new ArrayList<>();

		if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");
		Connection connection = ConnectionFactory.getInstance().getConnection();

		DataTable table = this.dataTables.get(0);
		
		String selectString = getSelectNameTemplate(table, null);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, null, organizationId);
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			rset = statement.executeQuery();
			while(rset.next()){
				String name = rset.getString(1);
				outInts.add(name);
			}
			rset.close();
			
		} catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outInts.toArray(new String[0]);

	}
	
	
	protected <T> long[] getIdByField(String fieldName, SqlDataEnumType fieldType, T fieldValue, long organizationId) throws FactoryException
	{
		QueryField field = new QueryField(fieldType, fieldName, fieldValue);
		field.setComparator(ComparatorEnumType.EQUALS);
		return getIdByField(new QueryField[]{field}, organizationId);
	}
	protected long[] getIdByField(QueryField[] fields, long organizationId) throws FactoryException
	{
		List<Long> outInts = new ArrayList<>();

		if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");
		Connection connection = ConnectionFactory.getInstance().getConnection();
		DataTable table = this.dataTables.get(0);
		
		String selectString = getSelectIdTemplate(table, null);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, null, organizationId);
		ResultSet rset = null;
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			rset = statement.executeQuery();
			while(rset.next()){
				long id = rset.getLong(1);
				outInts.add(id);

			}
			rset.close();
			
		} catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return ArrayUtils.toPrimitive(outInts.toArray(new Long[outInts.size()]));
	}
	
	
	public int getCountByField(DataTable table, QueryField[] fields, long organizationId) throws FactoryException
	{
		return getAggregateByField(table, "count(*)", fields, organizationId);
	}
	private int getAggregateByField(DataTable table, String aggregateExpression, QueryField[] fields, long organizationId) throws FactoryException
	{
		int outCount = 0;

		String selectString = table.getSelectAggregateTemplate().replace("%AGGREGATE%", aggregateExpression);
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, null, organizationId);
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			rset = statement.executeQuery();
			if(rset.next()){
				outCount = rset.getInt(1);
			}
			rset.close();
			
		} catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			throw new FactoryException(e.getMessage());
		} 
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		
		return outCount;
	}
	
	
	protected int deleteById(long[] id) throws FactoryException
	{
		return deleteByBigIntField("id", id, 0);
	}
	protected int deleteById(long id) throws FactoryException
	{
		return deleteById(new long[] { id });
	}
	protected int deleteById(long id, long organizationId) throws FactoryException
	{
		return deleteByBigIntField("id",new long[] { id }, organizationId);
	}
	protected int deleteById(long[] id, long organizationId) throws FactoryException
	{
		return deleteByBigIntField("id", id, organizationId);
	}
	protected int deleteByBigIntField(String fieldName, long[] list, long organizationId) throws FactoryException
	{
		if (scopeToOrganization && organizationId <= 0L) throw new FactoryException("Cannot invoke without organization id");
		if (list.length == 0) return 0;
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		PreparedStatement statement = null;
		DataTable table = this.dataTables.get(0);

		int deletedRecords = 0;
		try {
			String sql = String.format("DELETE FROM %s WHERE %s = ?",table.getName(), fieldName);
			statement = connection.prepareStatement(sql);
			for (int i = 0; i < list.length; i++)
			{
				statement.setLong(1, list[i]);
				statement.addBatch();
				if((i > 0 || list.length ==1 ) && ((i % 250 == 0) || i == list.length - 1)){
					int[] del = statement.executeBatch();
					for(int d = 0; d < del.length; d++) deletedRecords += del[d];
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try {
				if(statement != null) statement.close();

				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}

		return deletedRecords;
	}
	protected int deleteByField(QueryField[] fields, long organizationId) throws FactoryException
	{
		Connection connection = ConnectionFactory.getInstance().getConnection();
		DataTable table = this.dataTables.get(0);
		String deleteQuery = String.format("DELETE FROM %s",table.getName());
		String sqlQuery = assembleQueryString(deleteQuery, fields, connectionType, null, organizationId);
		int deletedRecords = 0;
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			deletedRecords = statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		

		return deletedRecords;
	}

	public static String getQueryClause(ProcessingInstructionType instruction, QueryField[] fields, String paramToken){
		return getQueryClause(instruction, fields, "AND", paramToken);
	}
	public static String getQueryClause(ProcessingInstructionType instruction, QueryField[] fields, String joinType, String paramToken)
	{
		StringBuilder matchBuff = new StringBuilder();
		int len = fields.length;
		String alias = "";
		boolean haveAlias = false;
		if(instruction != null && instruction.getJoinAttribute()){
			haveAlias = true;
			alias = instruction.getTableAlias() + ".";
		}
		for (int i = 0; i < len; i++)
		{
			if(fields[i] == null) continue;
			
			String fieldName = fields[i].getName();
			ComparatorEnumType fieldComp = fields[i].getComparator();
			if(haveAlias && fieldName.indexOf(".") == -1){
				fieldName = alias + fieldName;
			}
			if (i > 0) matchBuff.append(" " + joinType + " ");
			if(fieldComp == ComparatorEnumType.GROUP_AND || fieldComp == ComparatorEnumType.GROUP_OR){
				String useType = (fieldComp == ComparatorEnumType.GROUP_AND ? "AND" : "OR");
				
				if(!fields[i].getFields().isEmpty()){
					matchBuff.append("(" + getQueryClause(instruction, fields[i].getFields().toArray(new QueryField[0]),useType,paramToken) + ")");
				}
			}
			else if (fieldComp == ComparatorEnumType.EQUALS)
			{
				matchBuff.append(fieldName + " = " + paramToken);
			}
			else if (fieldComp == ComparatorEnumType.NOT_EQUALS)
			{
				matchBuff.append(String.format("NOT %s = %s", fieldName, paramToken));
			}
			else if (fieldComp == ComparatorEnumType.LIKE)
			{
				// WARN: This is a SQL Injection Point -- need to fix or be sure to restrict
				// At the moment, assuming it's a string value
				//
				matchBuff.append(String.format("%s LIKE %s", fieldName, paramToken));
				continue;
			}
			else if (fieldComp == ComparatorEnumType.IN || fieldComp == ComparatorEnumType.NOT_IN)
			{
				// WARN: This is a SQL Injection Point -- need to fix or be sure to restrict
				// At the moment, assuming it's a string value
				//
				String notStr = (fieldComp == ComparatorEnumType.NOT_IN ? " NOT " : "");
				matchBuff.append(String.format("%s %s IN (%s)",fieldName, notStr, (String)fields[i].getValue()));
				continue;
			}
			else if (fieldComp == ComparatorEnumType.GREATER_THAN || fieldComp == ComparatorEnumType.GREATER_THAN_OR_EQUALS)
			{
				matchBuff.append(fieldName + " >" + (fieldComp == ComparatorEnumType.GREATER_THAN_OR_EQUALS ? "=" : "") + " " + paramToken);
			}
			else if (fieldComp == ComparatorEnumType.LESS_THAN || fieldComp == ComparatorEnumType.LESS_THAN_OR_EQUALS)
			{
				matchBuff.append(fieldName + " <" + (fieldComp == ComparatorEnumType.LESS_THAN_OR_EQUALS ? "=" : "") + " " + paramToken);
			}
			else{
				logger.error("Unhandled Comparator: " + fields[i].getComparator());
			}

		}
		return matchBuff.toString();
	}
	
	
	protected void configureTableRestrictions(DataTable table){
		
	}
	

	
	
	public static int[] convertIntList(List<Integer> list){
		return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
	}
	public static long[] convertLongList(List<Long> list){
		return ArrayUtils.toPrimitive(list.toArray(new Long[list.size()]));
	}
	
	@SuppressWarnings("unchecked")
	public static <Y,T> List<T> convertList( List<Y> inList)  
	{  
		List<T> outList = new ArrayList<>();
		int len = inList.size();
		try{
	    for (int i = 0; i < len; i++)
	    {  
	    	outList.add((T)inList.get(i));
	    } 
		}
		catch(ClassCastException cce){
			logger.error(FactoryException.LOGICAL_EXCEPTION,cce);
			logger.error(cce.getMessage());
		}
		return outList;
	}  
	protected String assembleQueryString(String selectString, QueryField[] fields, CONNECTION_TYPE connectionType, ProcessingInstructionType instruction, long organizationId){
		String tableAlias = "";
		if(instruction != null && instruction.getJoinAttribute()){
			instruction.setTableAlias(getDataTables().get(0).getName().substring(0, 1));
			tableAlias = instruction.getTableAlias() + ".";
		}
		
		String pagePrefix = DBFactory.getPaginationPrefix(instruction, connectionType);
		String pageSuffix = DBFactory.getPaginationSuffix(instruction, connectionType);
		String pageField = DBFactory.getPaginationField(instruction, connectionType);
		String paramToken = DBFactory.getParamToken(connectionType);
		String queryClause = getQueryClause(instruction, fields, paramToken);

		String modSelectString = selectString
				.replaceAll("#TOP#", (instruction != null && instruction.getTopCount() > 0 ? "TOP " + instruction.getTopCount() : ""))
				.replaceAll("#PAGE#", pageField)
		;
		String queryClauseCond = (queryClause.length() == 0 ? " " : " AND ");
		return pagePrefix + modSelectString + " WHERE " + queryClause
			+ (scopeToOrganization && organizationId > 0L ? queryClauseCond + tableAlias + "organizationid=" + organizationId : "")
			+ (instruction != null && instruction.getGroupClause() != null ? " GROUP BY " + instruction.getGroupClause() : "")
			+ (instruction != null && instruction.getHavingClause() != null ? " HAVING " + instruction.getHavingClause() : "")
			+ pageSuffix
		;

	}
	
	protected List<Long> getNextIds(int count) throws FactoryException{
		List<Long> ids = new ArrayList<>();
		if(sequenceName == null || sequenceName.length() == 0) throw new FactoryException("Sequence name is null");
		String query = "SELECT nextval('" + sequenceName + "') FROM generate_series(1," + count + ")";
		Connection connection = ConnectionFactory.getInstance().getConnection();
		ResultSet rset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			rset = statement.executeQuery(query);
			while(rset.next()){
				ids.add(rset.getLong(1));
			}
			rset.close();
			statement.close();
		} catch (SQLException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return ids;
	}


}

