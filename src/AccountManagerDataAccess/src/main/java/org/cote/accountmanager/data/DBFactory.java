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

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class DBFactory {
	public static final Logger logger = LogManager.getLogger(DBFactory.class);
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
			if(fields[i] == null)
				continue;
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
					if(value == null)
						statement.setNull(index, Types.VARCHAR);
					else
						statement.setString(index,  (String)value);
					break;
				case INTEGER:
					if(value != null)
						statement.setInt(index,  ((Integer)value).intValue());
					else{
						logger.warn("Null int detected.  If this is for an id field, the probable cause is that a bulk insert session includes both bulk and dirty writes of the same factory type");
						statement.setNull(index, Types.BIGINT);
					}

					break;
				case BIGINT:
					
					if(value != null)
						statement.setLong(index,  ((Long)value).longValue());
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
			logger.error(e.getMessage());
			logger.error(e);
			throw new FactoryException(e.getMessage());
		}
	}
	public static void setPreparedStatementValue(PreparedStatement ps, DataCell cell, int index) throws FactoryException{

		try {
			if(cell.getValue() == null && cell.getDataType().equals(SqlDataEnumType.INTEGER)){
				logger.warn("Null integer value detected for cell " + cell.getColumnName());
			}
			setStatementParameter(ps, cell.getDataType(), cell.getValue(), index);
		} catch (DataAccessException e) {
			logger.error(e);
		}

	}
	
	public static boolean isInstructionReadyForPagination(ProcessingInstructionType instruction)
	{
		return (
				instruction != null
				&& instruction.getPaginate()
				&& instruction.getOrderClause() != null
				&& instruction.getStartIndex().compareTo(0L) >= 0
				&& instruction.getRecordCount() > 0
			);
	}
	public static String getPaginationPrefix(ProcessingInstructionType instruction, CONNECTION_TYPE connectionType)
	{
		if (!isInstructionReadyForPagination(instruction))
			return "";

		String outPrefix = "";
		switch (connectionType)
		{
			case SQL:
				outPrefix = "SELECT * FROM (";
				break;
			case ORACLE:
			case POSTGRES:
			case MYSQL:
				break;
			default:
				break;
		}
		return outPrefix;
	}
	public static String getPaginationField(ProcessingInstructionType instruction, CONNECTION_TYPE connectionType)
	{
		if (!isInstructionReadyForPagination(instruction))
			return "";
		String outField = "";
		switch (connectionType)
		{
			case SQL:
				outField = ",ROW_NUMBER() OVER (ORDER BY " + instruction.getOrderClause() + ") AS ROW_NUM";
				break;
			case ORACLE:
			case POSTGRES:				
			case MYSQL:
				break;
			default:
				break;
		}
		return outField;
	}
	public static String getPaginationSuffix(ProcessingInstructionType instruction, CONNECTION_TYPE connectionType)
	{
		String orderClause = (instruction != null && instruction.getOrderClause() != null ? " ORDER BY " + instruction.getOrderClause() : "");
		if (!isInstructionReadyForPagination(instruction))
			return orderClause;
		String outSuffix = "";
		switch (connectionType)
		{
			case SQL:
				outSuffix = ") as RowCursor WHERE ROW_NUM >= " + instruction.getStartIndex() + " AND ROW_NUM < " + (instruction.getStartIndex() + instruction.getRecordCount()) + orderClause;
				break;
			case ORACLE:
			case POSTGRES:					
			case MYSQL:
				outSuffix = orderClause + " LIMIT " + instruction.getRecordCount() + " OFFSET " + instruction.getStartIndex();
				break;
			default:
				break;
		}
		return outSuffix;
	}
	public static long getAdjustedStartRecord(long startRecord, CONNECTION_TYPE connectionType)
	{
		if (connectionType == CONNECTION_TYPE.SQL)
			return (startRecord + 1);
		return startRecord;
	}
	
	public static String getParamToken(CONNECTION_TYPE connectionType)
	{

		if (connectionType == CONNECTION_TYPE.SQL)
			return "@";
		else if(connectionType == CONNECTION_TYPE.POSTGRES || connectionType == CONNECTION_TYPE.MYSQL || connectionType == CONNECTION_TYPE.ORACLE)
			return "?";
		return "";
		
	}
	public static String getNoLockHint(CONNECTION_TYPE connectionType)
	{

		if (connectionType == CONNECTION_TYPE.SQL)
			return " with (nolock) ";
		else if (connectionType == CONNECTION_TYPE.MYSQL || connectionType == CONNECTION_TYPE.ORACLE)
			return "";
		return "";

	}
	public static CONNECTION_TYPE getConnectionType(Connection connection){
		CONNECTION_TYPE outType = CONNECTION_TYPE.UNKNOWN;
		String dbName = null;
		try {
			dbName = connection.getMetaData().getDatabaseProductName();
		} catch (SQLException e) {

			logger.error(e);
		}
		if(dbName != null && dbName.matches("^(?i)postgresql$"))
			outType = CONNECTION_TYPE.POSTGRES;
		return outType;
	}

	public static boolean dropTable(Connection connection, String tableName){
		int update = 0;
		CONNECTION_TYPE connectionType = getConnectionType(connection);
		Statement stat = null;
		try{
			stat = connection.createStatement();
			StringBuilder buff = new StringBuilder();
			buff.append("DROP TABLE IF EXISTS " + tableName + ";");
			if(connectionType == CONNECTION_TYPE.POSTGRES){
				buff.append(" DROP SEQUENCE IF EXISTS " + tableName + "_id_seq;");
			}
			update = stat.executeUpdate(buff.toString());
			stat.close();
		}
		catch(SQLException sqe){
			logger.error(sqe);
		}
		finally{
			try {
				if(stat != null) stat.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return (update != 0);
	}
	public static boolean getTableExists(Connection connection, String tableName){
		boolean exists = false;
		DatabaseMetaData dmd = null;
		try{
			dmd = connection.getMetaData();
			ResultSet rset = dmd.getTables(null, null, tableName,null);
			if(rset == null){
				return false;
			}

			ResultSetMetaData rsetMD = rset.getMetaData();
			while(rset.next() && !exists){
				int count=rsetMD.getColumnCount();
				for(int i = 1;i <= count; i++){
					String columnName = rsetMD.getColumnName(i);
					if(columnName.equalsIgnoreCase("TABLE_NAME")){
						String compareName = rset.getString(i);
						if(compareName != null && compareName.equalsIgnoreCase(tableName)){
							exists = true;
						}
						break;
					}
				}
			}
			rset.close();
		}
		catch(SQLException sqe){
			logger.error("Trace:",sqe);
		}
		return exists;
	}
	public static int executeStatement(Connection connection, String statement){
		int update = 0;
		Statement stat = null;
		try{
			stat = connection.createStatement();
			update = stat.executeUpdate(statement);

		}
		catch(SQLException sqe){
			logger.error(sqe);
		}
		finally{
			try {
				if(stat != null) stat.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return update;
	}
	
	public static DataTable getDataTable(Connection connection, String tableName) throws DataAccessException{
		DataTable outTable = new DataTable();
		outTable.setName(tableName);
		int colIndex = 0;
		CONNECTION_TYPE connectionType = getConnectionType(connection);
		try{
			DatabaseMetaData meta = connection.getMetaData();
			
			ResultSet rset = meta.getColumns(null, null, tableName, null);
			while(rset.next()){
				String colName = rset.getString("COLUMN_NAME");
				String colType = rset.getString("TYPE_NAME");
				int colSize = rset.getInt("COLUMN_SIZE");
				
				SqlDataEnumType dataType = SqlTypeUtil.translateSqlType(connectionType, colType);
				outTable.addColumn(colName,  colIndex++, colSize, dataType);
			}
			rset.close();
		}
		catch(SQLException sqe){
			throw new DataAccessException(sqe.getMessage());
		}
		outTable.setColumnSize(colIndex);
		return outTable;
	}

}
