package org.cote.accountmanager.data.factory;

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

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.io.BulkInsertUtil;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public abstract class FactoryBase {
	public static final Logger logger = Logger.getLogger(FactoryBase.class.getName());
	
	protected FactoryEnumType factoryType = FactoryEnumType.UNKNOWN;
	
	//private DataTable dataTable = null;
	protected Map<String,Integer> dataTableMap = null;
	/// protected Map<String,List<DataRow>> bulkSpool = null;
	protected List<DataTable> dataTables = null;
	private DBFactory.CONNECTION_TYPE connectionType = DBFactory.CONNECTION_TYPE.UNKNOWN;
	protected List<String> tableNames = null;
	protected boolean scopeToOrganization = true;
	///protected boolean spoolAdd = false;
	private boolean initialized = false;
	private int addCounter = 0;
	protected boolean bulkMode = false;
	
	/// PostGres sequence name
	///
	protected String sequenceName = null;
	
	private Map<String,List<Long>> bulkMap = null;
	
	public FactoryBase()
	{
		dataTableMap = Collections.synchronizedMap(new HashMap<String,Integer>());
		tableNames = new ArrayList<String>();
		dataTables = new ArrayList<DataTable>();
		bulkMap = new HashMap<String,List<Long>>();
		/// bulkSpool = new HashMap<String, List<DataRow>>();
	}
	public FactoryEnumType getFactoryType(){
		return factoryType;
	}
	protected void addBulkId(String sessionId, long id){
		if(bulkMap.containsKey(sessionId) == false) bulkMap.put(sessionId, new ArrayList<Long>());
		bulkMap.get(sessionId).add(id);
	}
	public Map<String,List<Long>> getBulkMap(){
		return bulkMap;
	}
	public void Destroy()
	{

	}
	

	
	protected boolean insertRow(DataRow row){
		boolean out_bool = false;
		if(row.getTable().getBulkInsert() == false){
			addCounter = 0;
			try {
				out_bool = BulkInsertUtil.insert(row);
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			/// If the table is individual updates, then dump any rows accumulated into the table row set
			/// 
			row.getTable().getRows().clear();
		}
		else{
			/// TODO: Refactor - this is currently being handled manually - describe the issue for not handling it here
			///
			if(bulkMode) row.getTable().getRows().add(row);
			out_bool = true;
		}
		return out_bool;
	}

	public void writeSpool(String tableName)
	{
		DataTable table = getDataTable(tableName);
		if (table.getBulkInsert() == true)
		{
			/// addCounter = 0;
			synchronized(table){
				//logger.info("Writing bulk spool");
				if(BulkInsertUtil.insertBulk(table) == false){
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
		if(dataTableMap.containsKey(name)==false)return null;
		return dataTables.get(dataTableMap.get(name));
	}
	public List<DataTable> getDataTables() {
		return dataTables;
	}
	/*

	public String getTableName() {
		return tableName;
	}
	*/
	
	public boolean isScopeToOrganization() {
		return scopeToOrganization;
	}
	protected String getSelectIdTemplate(DataTable table, ProcessingInstructionType instruction){
		return table.getSelectIdTemplate();
	}
	protected String getSelectTemplate(DataTable table, ProcessingInstructionType instruction){
		return table.getSelectFullTemplate();
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
			try{
				table = DBFactory.getDataTable(connection, tableName);
			}
			catch(DataAccessException dae){
				logger.error(dae.getMessage());
				error = true;
			}
			if(error){
				break;
			}
			configureTableRestrictions(table);
			dataTableMap.put(tableName, i);
			dataTables.add(table);
			
			/// TODO - table needs to be configured per factory instance for select/update restrictions
			/// this should be a callout to a decorator here

			StringBuffer buff = new StringBuffer();
			//StringBuffer ubuff = new StringBuffer();

			String lock_hint = DBFactory.getNoLockHint(connectionType);
			String token = DBFactory.getParamToken(connectionType);

			buff.append("SELECT #TOP# ");
			//ubuff.append("UPDATE " + tableName + " SET ");
			int ucount = 0;
			int scount = 0;
			for (int c = 0; c < table.getColumnSize(); c++)
			{
				DataColumnType column = table.getColumns().get(c);
				if (table.getCanSelectColumn(column.getColumnName()))
				{
					if (scount > 0) buff.append(",");
					buff.append(column.getColumnName());
					scount++;
				}
				/*
				if (table.getCanUpdateColumn(column.getColumnName()))
				{
					if (ucount > 0) ubuff.append(",");
					ubuff.append(column.getColumnName() + " = " + token + column.getColumnName());
					ucount++;
				}
				*/
			}
			String table_clause = " FROM " + tableName + lock_hint;
			table.setSelectFullTemplate(buff.toString() + " #PAGE# " + table_clause);
			table.setSelectIdTemplate("SELECT id" + table_clause);
			table.setSelectAggregateTemplate("SELECT %AGGREGATE%" + table_clause);
			table.setSelectNameTemplate("SELECT name" + table_clause);
			//table.setUpdateFullTemplate(ubuff.toString());

		
		}
		
		if(error == false) initialized = true;
	}
	
	public String getUpdateTemplate(DataTable table, QueryField[] updateFields, String token) throws FactoryException {
		if(updateFields.length == 0) throw new FactoryException("Empty field list");
		StringBuffer buff = new StringBuffer();
		buff.append("UPDATE " + table.getName() +" SET ");
		for(int i = 0; i < updateFields.length; i++){
			if(i > 0) buff.append(", ");
			buff.append(updateFields[i].getName() + "=" + token);
		}
		return buff.toString();
	}
	protected <T> long[] getIdByField(String field_name, SqlDataEnumType field_type, T field_value, long organization_id) throws FactoryException
	{
		QueryField field = new QueryField(field_type, field_name, field_value);
		field.setComparator(ComparatorEnumType.EQUALS);
		return getIdByField(new QueryField[]{field}, organization_id);
	}
	protected long[] getIdByField(QueryField[] fields, long organization_id) throws FactoryException
	{
		List<Long> out_ints = new ArrayList<Long>();

		if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = this.dataTables.get(0);
		
		String selectString = getSelectIdTemplate(table, null);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, null, organization_id);

		//logger.info("SQL=" + sqlQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				long id = rset.getLong(1);
				out_ints.add(id);
				//logger.info("ID=" + id);
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
		return ArrayUtils.toPrimitive(out_ints.toArray(new Long[out_ints.size()]));

		//return out_ints.toArray(new int[]);
		//return new int[0];
	}
	
	
	public int getCountByField(DataTable table, QueryField[] fields, long organization_id) throws FactoryException
	{
		return getAggregateByField(table, "count(*)", fields, organization_id);
	}
	private int getAggregateByField(DataTable table, String aggregate_expression, QueryField[] fields, long organization_id) throws FactoryException
	{
		int out_count = 0;

		String select_string = table.getSelectAggregateTemplate().replace("%AGGREGATE%", aggregate_expression);
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String sqlQuery = assembleQueryString(select_string, fields, connectionType, null, organization_id);

		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			if(rset.next()){
				out_count = rset.getInt(1);
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
		
		return out_count;
	}
	
	
	protected int deleteById(long[] id) throws FactoryException
	{
		if (scopeToOrganization) throw new FactoryException("Cannot invoke without organization id");
		return deleteByBigIntField("id", id, 0);
	}
	protected int deleteById(long id) throws FactoryException
	{
		return deleteById(new long[] { id });
	}
	protected int deleteById(long id, long organization_id) throws FactoryException
	{
		return deleteByBigIntField("id",new long[] { id }, organization_id);
	}
	protected int deleteById(long[] id, long organization_id) throws FactoryException
	{
		return deleteByBigIntField("id", id, organization_id);
	}
	protected int deleteByBigIntField(String field_name, long[] list, long organization_id)
	{
		if (list.length == 0) return 0;
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		DataTable table = this.dataTables.get(0);

		int deleted_records = 0;
		try {
			String sql = "DELETE FROM " + table.getName() + " WHERE id = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			for (int i = 0; i < list.length; i++)
			{
				statement.setLong(1, list[i]);
				statement.addBatch();
				if((i > 0 || list.length ==1 ) && ((i % 250 == 0) || i == list.length - 1)){
					int[] del = statement.executeBatch();
					for(int d = 0; d < del.length; d++) deleted_records += del[d];
				}
			}
			statement.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			// TODO Auto-generated catch block
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

		return deleted_records;
	}
	protected int deleteByField(QueryField[] fields, long organization_id) throws FactoryException
	{
		Connection connection = ConnectionFactory.getInstance().getConnection();
		DataTable table = this.dataTables.get(0);
		String deleteQuery = "DELETE FROM " + table.getName();
		String sqlQuery = assembleQueryString(deleteQuery, fields, connectionType, null, organization_id);
		//logger.info("Query Clause=" + queryClause);
		//logger.info("SQL=" + sqlQuery);
		int deleted_records = 0;
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			deleted_records = statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e) {
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
		

		return deleted_records;
	}
	/// TODO: IS NULL not yet handled
	///

	protected String getQueryClause(QueryField[] fields, String paramToken){
		return getQueryClause(fields, "AND", paramToken);
	}
	protected String getQueryClause(QueryField[] fields, String joinType, String paramToken)
	{
		StringBuffer match_buff = new StringBuffer();
		int len = fields.length;
		for (int i = 0; i < len; i++)
		{
			if(fields[i] == null) continue;
			///logger.info(i + ": " + fields[i].getName() + " " + fields[i].getComparator());
			if (i > 0) match_buff.append(" " + joinType + " ");
			if(fields[i].getComparator() == ComparatorEnumType.GROUP_AND || fields[i].getComparator() == ComparatorEnumType.GROUP_OR){
				String useType = (fields[i].getComparator() == ComparatorEnumType.GROUP_AND ? "AND" : "OR");
				
				if(fields[i].getFields().size() > 0){
					match_buff.append("(" + getQueryClause(fields[i].getFields().toArray(new QueryField[0]),useType,paramToken) + ")");
				}
			}
			else if (fields[i].getComparator() == ComparatorEnumType.EQUALS)
			{
				match_buff.append(fields[i].getName() + " = " + paramToken);
			}
			else if (fields[i].getComparator() == ComparatorEnumType.NOT_EQUALS)
			{
				match_buff.append("NOT " + fields[i].getName() + " = " + paramToken);
			}
			else if (fields[i].getComparator() == ComparatorEnumType.LIKE)
			{
				// TODO: This is a SQL Injection Point -- need to fix or be sure to restrict
				// At the moment, assuming it's a string value
				//
				//match_buff.append(fields[i].getName() + " LIKE '" + fields[i].getValue() + "'");
				match_buff.append(fields[i].getName() + " LIKE " + paramToken);
				continue;
			}
			else if (fields[i].getComparator() == ComparatorEnumType.IN || fields[i].getComparator() == ComparatorEnumType.NOT_IN)
			{
				// TODO: This is a SQL Injection Point -- need to fix or be sure to restrict
				// At the moment, assuming it's a string value
				//
				//match_buff.Append(Fields[i].Name + " IN (" + GetQueryParam(Fields[i].Name) + ")");
				String not_str = (fields[i].getComparator() == ComparatorEnumType.NOT_IN ? " NOT " : "");
				match_buff.append(fields[i].getName() + " " + not_str + "IN (" + (String)fields[i].getValue() + ")");
				continue;
			}
			else if (fields[i].getComparator() == ComparatorEnumType.GREATER_THAN || fields[i].getComparator() == ComparatorEnumType.GREATER_THAN_OR_EQUALS)
			{
				match_buff.append(fields[i].getName() + " >" + (fields[i].getComparator() == ComparatorEnumType.GREATER_THAN_OR_EQUALS ? "=" : "") + " " + paramToken);
			}
			else if (fields[i].getComparator() == ComparatorEnumType.LESS_THAN || fields[i].getComparator() == ComparatorEnumType.LESS_THAN_OR_EQUALS)
			{
				match_buff.append(fields[i].getName() + " <" + (fields[i].getComparator() == ComparatorEnumType.LESS_THAN_OR_EQUALS ? "=" : "") + " " + paramToken);
			}
			else{
				logger.error("Unhandled Comparator: " + fields[i].getComparator());
			}

			///AddParameter(col, Fields[i].DbType, Fields[i].Name, Fields[i].Value);
		}
		return match_buff.toString();
	}
	
	
	protected void configureTableRestrictions(DataTable table){
		
	}
	

	
	
	public static int[] convertIntList(List<Integer> list){
		return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
	}
	public static long[] convertLongList(List<Long> list){
		return ArrayUtils.toPrimitive(list.toArray(new Long[list.size()]));
	}
	public static <Y,T> List<T> convertList( List<Y> inList)  
	{  
		List<T> outList = new ArrayList<T>();
		int len = inList.size();
		try{
	    for (int i = 0; i < len; i++)
	    {  
	    	outList.add((T)inList.get(i));
	    } 
		}
		catch(ClassCastException cce){
			cce.printStackTrace();
			logger.error(cce.getMessage());
		}
		return outList;
	}  
	protected String assembleQueryString(String selectString, QueryField[] fields, CONNECTION_TYPE connectionType, ProcessingInstructionType instruction, long organization_id){
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
		
		return sqlQuery;
	}
	
	protected List<Long> getNextIds(int count) throws FactoryException{
		List<Long> ids = new ArrayList<Long>();
		if(sequenceName == null || sequenceName.length() == 0) throw new FactoryException("Sequence name is null");
		String query = "SELECT nextval('" + sequenceName + "') FROM generate_series(1," + count + ")";
		Connection connection = ConnectionFactory.getInstance().getConnection();
		try {
			Statement statement = connection.createStatement();
			ResultSet rset = statement.executeQuery(query);
			while(rset.next()){
				ids.add(rset.getLong(1));
			}
			rset.close();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ids;
	}


}

