package org.cote.accountmanager.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.DataTableType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class DBFactory {
	public static final Logger logger = Logger.getLogger(DBFactory.class.getName());
	public enum CONNECTION_TYPE{
		UNKNOWN,
		SQL,
		ORACLE,
		MYSQL,
		POSTGRES
	}
	public static CONNECTION_TYPE defaultConnectionType = CONNECTION_TYPE.UNKNOWN;
	
	public static CONNECTION_TYPE getDefaultConnectionType() {
		return defaultConnectionType;
	}
	public static void setDefaultConnectionType(
			CONNECTION_TYPE defaultConnectionType) {
		DBFactory.defaultConnectionType = defaultConnectionType;
	}
	public static void setStatementParameters(QueryField[] fields, PreparedStatement statement) throws FactoryException{
		setStatementParameters(fields, 1, statement);
	}
	public static int setStatementParameters(QueryField[] fields, int startMarker, PreparedStatement statement) throws FactoryException{
		int len = fields.length;
		int paramMarker = startMarker;
		for(int i = 0; i < len; i++){
			if(fields[i] == null) continue;
			if(fields[i].getComparator() == ComparatorEnumType.GROUP_AND || fields[i].getComparator() == ComparatorEnumType.GROUP_OR){
				paramMarker = setStatementParameters(fields[i].getFields().toArray(new QueryField[0]), paramMarker, statement);
			}
			else if (
				fields[i].getComparator() == ComparatorEnumType.EQUALS
				|| fields[i].getComparator() == ComparatorEnumType.NOT_EQUALS	
				|| fields[i].getComparator() == ComparatorEnumType.GREATER_THAN
				|| fields[i].getComparator() == ComparatorEnumType.GREATER_THAN_OR_EQUALS	
				|| fields[i].getComparator() == ComparatorEnumType.LESS_THAN
				|| fields[i].getComparator() == ComparatorEnumType.LESS_THAN_OR_EQUALS
				|| fields[i].getComparator() == ComparatorEnumType.LIKE

			){
				//System.out.println(fields[i].getDataType() + ":" + fields[i].getValue());
				setStatementParameter(statement, fields[i].getDataType(), fields[i].getValue(), paramMarker++);
			}
		}
		return paramMarker;
	}
	public static <T> void setStatementParameter(PreparedStatement statement, SqlDataEnumType dataType, T value, int index) throws FactoryException{
		if(index <= 0){
			throw new FactoryException("Prepared Statement index is 1-based, not 0-based, and the index must be greater than or equal to 1");
		}		
		try{
			switch(dataType){
				case BLOB:
					statement.setBytes(index, (byte[])value);
					break;
				case TEXT:
				case VARCHAR:
					//System.out.println("Str=" + (String)value);
					if(value == null) statement.setNull(index, Types.VARCHAR);
					else statement.setString(index,  (String)value);
					break;
				case INTEGER:
					if(value != null) statement.setInt(index,  ((Integer)value).intValue());
					else{
						logger.warn("Null int detected.  If this is for an id field, the probable cause is that a bulk insert session includes both bulk and dirty writes of the same factory type");
						statement.setNull(index, Types.BIGINT);
					}

					break;
				case BIGINT:
					
					if(value != null) statement.setLong(index,  ((Long)value).longValue());
					else{
						
						logger.warn("Null bigint detected.  If this is for an id field, the probable cause is that a bulk insert session includes both bulk and dirty writes of the same factory type");
						statement.setNull(index, Types.BIGINT);
					}
					
					break;
				case DOUBLE:
					statement.setDouble(index,  ((Double)value).doubleValue());
					break;
				case BOOLEAN:
					statement.setBoolean(index, ((Boolean)value).booleanValue());
					break;
				case DATE:
					statement.setDate(index, new Date(((XMLGregorianCalendar)value).toGregorianCalendar().getTimeInMillis()));
					break;
				case TIMESTAMP:
					statement.setTimestamp(index, new Timestamp(((XMLGregorianCalendar)value).toGregorianCalendar().getTimeInMillis()));
					break;
				default:
					throw new FactoryException("Unhandled data type:" + dataType + " for index " + index);
			}
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new FactoryException(e.getMessage());
		}
	}
	public static void setPreparedStatementValue(PreparedStatement ps, DataCell cell, int index) throws FactoryException{

		try {
			if(cell.getValue() == null && cell.getDataType().equals(SqlDataEnumType.INTEGER)){
				logger.warn("Null integer value detected for cell " + cell.getColumnName());
			}
		//	System.out.println(cell.getColumnName() + ":" + cell.getDataType() + ":" + cell.getValue());
			setStatementParameter(ps, cell.getDataType(), cell.getValue(), index);
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static boolean isInstructionReadyForPagination(ProcessingInstructionType instruction)
	{
		return (instruction != null && instruction.getPaginate() && instruction.getOrderClause() != null && instruction.getStartIndex() >= 0 && instruction.getRecordCount() > 0);
	}
	public static String getPaginationPrefix(ProcessingInstructionType instruction, CONNECTION_TYPE connectionType)
	{
		if (!isInstructionReadyForPagination(instruction)) return "";

		String out_prefix = "";
		switch (connectionType)
		{
			case SQL:
				out_prefix = "SELECT * FROM (";
				break;
			case ORACLE:
			case POSTGRES:
			case MYSQL:
				break;
		}
		return out_prefix;
	}
	public static String getPaginationField(ProcessingInstructionType instruction, CONNECTION_TYPE connectionType)
	{
		if (!isInstructionReadyForPagination(instruction)) return "";
		String out_field = "";
		switch (connectionType)
		{
			case SQL:
				out_field = ",ROW_NUMBER() OVER (ORDER BY " + instruction.getOrderClause() + ") AS ROW_NUM";
				break;
			case ORACLE:
			case POSTGRES:				
			case MYSQL:
				break;
		}
		return out_field;
	}
	public static String getPaginationSuffix(ProcessingInstructionType instruction, CONNECTION_TYPE connectionType)
	{
		String order_clause = (instruction != null && instruction.getOrderClause() != null ? " ORDER BY " + instruction.getOrderClause() : "");
		if (!isInstructionReadyForPagination(instruction)) return order_clause;
		String out_suffix = "";
		switch (connectionType)
		{
			case SQL:
				out_suffix = ") as RowCursor WHERE ROW_NUM >= " + instruction.getStartIndex() + " AND ROW_NUM < " + (instruction.getStartIndex() + instruction.getRecordCount()) + order_clause;
				break;
			case ORACLE:
			case POSTGRES:					
			case MYSQL:
				out_suffix = order_clause + " LIMIT " + instruction.getRecordCount() + " OFFSET " + instruction.getStartIndex();
				break;
		}
		return out_suffix;
	}
	public static int getAdjustedStartRecord(int startRecord, CONNECTION_TYPE connectionType)
	{
		if (connectionType == CONNECTION_TYPE.SQL) return (startRecord + 1);
		return startRecord;
	}
	
	public static String getParamToken(CONNECTION_TYPE connection_type)
	{

		if (connection_type == CONNECTION_TYPE.SQL) return "@";
		else if(connection_type == CONNECTION_TYPE.POSTGRES || connection_type == CONNECTION_TYPE.MYSQL || connection_type == CONNECTION_TYPE.ORACLE) return "?";
		return "";
		
	}
	public static String getNoLockHint(CONNECTION_TYPE connection_type)
	{

		if (connection_type == CONNECTION_TYPE.SQL) return " with (nolock) ";
		else if (connection_type == CONNECTION_TYPE.MYSQL || connection_type == CONNECTION_TYPE.ORACLE) return "";
		return "";

	}
	public static CONNECTION_TYPE getConnectionType(Connection connection){
		CONNECTION_TYPE out_type = CONNECTION_TYPE.UNKNOWN;
		String dbName = null;
		try {
			dbName = connection.getMetaData().getDatabaseProductName();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dbName != null && dbName.matches("^(?i)postgresql$")) out_type = CONNECTION_TYPE.POSTGRES;
		//System.out.println("DBName: " + dbName + ":" + out_type);
		return out_type;
	}

	public static boolean dropTable(Connection connection, String table_name){
		int update = 0;
		CONNECTION_TYPE connectionType = getConnectionType(connection);
		try{
			Statement stat = connection.createStatement();
			StringBuffer buff = new StringBuffer();
			buff.append("DROP TABLE IF EXISTS " + table_name + ";");
			if(connectionType == CONNECTION_TYPE.POSTGRES){
				buff.append(" DROP SEQUENCE IF EXISTS " + table_name + "_id_seq;");
			}
			update = stat.executeUpdate(buff.toString());
			stat.close();
		}
		catch(SQLException sqe){
			sqe.printStackTrace();
		}
		return (update != 0);
	}
	public static boolean getTableExists(Connection connection, String table_name){
		boolean exists = false;
		DatabaseMetaData dmd = null;
		try{
			dmd = connection.getMetaData();
			ResultSet rset = dmd.getTables(null, null, table_name,null);
			if(rset == null){
				return false;
			}

			ResultSetMetaData rsetMD = rset.getMetaData();
			while(rset.next() && !exists){
				int count=rsetMD.getColumnCount();
				for(int i = 1;i <= count; i++){
					String column_name = rsetMD.getColumnName(i);
					if(column_name.equalsIgnoreCase("TABLE_NAME")){
						String compare_name = rset.getString(i);
						if(compare_name != null && compare_name.equalsIgnoreCase(table_name)){
							exists = true;
						}
						break;
					}
				}
			}
			rset.close();
		}
		catch(SQLException sqe){
			System.out.println(sqe.toString());
		}
		return exists;
	}
	public static int executeStatement(Connection connection, String statement){
		int update = 0;
		try{
			Statement stat = connection.createStatement();
			update = stat.executeUpdate(statement);
			stat.close();
		}
		catch(SQLException sqe){
			sqe.printStackTrace();
		}
		return update;
	}
	
	public static DataTable getDataTable(Connection connection, String tableName) throws DataAccessException{
		DataTable out_table = new DataTable();
		out_table.setName(tableName);
		int colIndex = 0;
		CONNECTION_TYPE connectionType = getConnectionType(connection);
		try{
			DatabaseMetaData meta = connection.getMetaData();
			
			ResultSet rset = meta.getColumns(null, null, tableName, null);
			//logger.debug("Analyzing Table Schema: " + tableName);
			while(rset.next()){
				//DataColumnType col = new DataColumnType();
				String colName = rset.getString("COLUMN_NAME");
				String colType = rset.getString("TYPE_NAME");
				int colSize = rset.getInt("COLUMN_SIZE");
				
				SqlDataEnumType dataType = SqlTypeUtil.translateSqlType(connectionType, colType);
				//logger.debug("\tColumn name: " + colName + " / Column type: " + colType + "->" + dataType);
				out_table.addColumn(colName,  colIndex++, colSize, dataType);
				/*
				col.setColumnName(colName);
				col.setDataType(SqlDataEnumType.valueOf(colType));
				col.setColumnSize(colSize);
				col.setColumnIndex(colIndex++);
				out_table.getColumns().add(col);
				*/
			}
			rset.close();
		}
		catch(SQLException sqe){
			throw new DataAccessException(sqe.getMessage());
		}
		out_table.setColumnSize(colIndex);
		return out_table;
		///return columns.toArray(new String[]{});
	}

}
