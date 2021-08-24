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
import java.util.Calendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.LevelEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;
import org.cote.accountmanager.objects.types.RetentionEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class AuditFactory extends FactoryBase {
	private int defaultPageSize = 10;
	
	public AuditFactory(){
		super();
		this.scopeToOrganization = false;
		this.primaryTableName = "audit";
		this.tableNames.add(primaryTableName);
	}
	
	@Override
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		DataTable table = this.getDataTable(primaryTableName);
		table.setBulkInsert(true);
		
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.ID), true);
		}
	}
	
	public AuditType newAudit(){

		AuditType audit = new AuditType();
		Calendar now = Calendar.getInstance();
		XMLGregorianCalendar cal = CalendarUtil.getXmlGregorianCalendar(now.getTime()); 
		audit.setAuditDate(cal);
		audit.setAuditResultDate(audit.getAuditDate());
		now.add(Calendar.MONTH,3);
		audit.setAuditExpiresDate(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		audit.setAuditActionType(ActionEnumType.UNKNOWN);
		audit.setAuditResultType(ResponseEnumType.UNKNOWN);
		audit.setAuditRetentionType(RetentionEnumType.UNKNOWN);
		audit.setAuditSourceType(AuditEnumType.UNKNOWN);
		audit.setAuditTargetType(AuditEnumType.UNKNOWN);
		audit.setAuditLevelType(LevelEnumType.INFO);

		return audit;
	}

	public void flushSpool(){
		this.writeSpool("audit");
	}
	public boolean addAudit(AuditType new_audit) throws FactoryException
	{
		DataRow row = prepareAdd(new_audit, primaryTableName);
		getDataTable(primaryTableName).getRows().add(row);
		return true;
	}
	

	public DataRow prepareAdd(AuditType obj, String tableName) throws FactoryException{
		DataTable table = getDataTable(tableName);
		if(table == null) throw new FactoryException("Table doesn't exist:" + tableName);
		DataRow row = table.newRow();
		try{
			
			row.setCellValue(Columns.get(ColumnEnumType.AUDITACTIONSOURCE), obj.getAuditActionSource());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITLEVELTYPE), obj.getAuditLevelType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITACTIONTYPE), obj.getAuditActionType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITDATE), obj.getAuditDate());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITRESULTDATE), obj.getAuditResultDate());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITEXPIRESDATE), obj.getAuditExpiresDate());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITRESULTDATA), obj.getAuditResultData());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITRESULTTYPE), obj.getAuditResultType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITRETENTIONTYPE), obj.getAuditRetentionType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITSOURCEDATA), obj.getAuditSourceData());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITSOURCETYPE), obj.getAuditSourceType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITTARGETDATA), obj.getAuditTargetData());
			row.setCellValue(Columns.get(ColumnEnumType.AUDITTARGETTYPE), obj.getAuditTargetType().toString());
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return row;
	}
	
	public boolean isValid(AuditType audit)
	{
		return (audit != null && audit.getAuditActionType() != null && audit.getAuditResultType() != null && audit.getAuditRetentionType() != null && audit.getAuditTargetType() != null && audit.getAuditDate() != null && audit.getAuditExpiresDate() != null);
	}
	public boolean deleteAudit(AuditType audit) throws FactoryException
	{
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldId(audit.getId()) }, 0);
		return (deleted > 0);
	}
	public AuditType[] getAuditBySourceAndTarget(AuditEnumType sourceType, String sourceData,AuditEnumType targetType, String targetData) throws FactoryException
	{
		ProcessingInstructionType instruction = getPagingInstruction(0);
		return getByField(new QueryField[]{QueryFields.getFieldAuditSourceType(sourceType),QueryFields.getFieldAuditSourceData(sourceData),QueryFields.getFieldAuditTargetType(targetType),QueryFields.getFieldAuditTargetData(targetData)}, instruction).toArray(new AuditType[0]);
	}
	public AuditType[] getAuditBySource(AuditEnumType sourceType, String sourceData) throws FactoryException
	{
		ProcessingInstructionType instruction = getPagingInstruction(0);
		return getByField(new QueryField[]{QueryFields.getFieldAuditSourceType(sourceType),QueryFields.getFieldAuditSourceData(sourceData)}, instruction).toArray(new AuditType[0]);
	}
	public AuditType[] getAuditByTarget(AuditEnumType targetType, String targetData) throws FactoryException
	{
		ProcessingInstructionType instruction = getPagingInstruction(0);
		return getByField(new QueryField[]{QueryFields.getFieldAuditTargetType(targetType),QueryFields.getFieldAuditTargetData(targetData)}, instruction).toArray(new AuditType[0]);
	}
	protected List<AuditType> getByField(QueryField[] fields, ProcessingInstructionType instruction) throws FactoryException{
		List<AuditType> outList = new ArrayList<AuditType>();
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = getDataTable(primaryTableName);
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, 0);
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			rset = statement.executeQuery();
			while(rset.next()){
				AuditType obj = this.read(rset, instruction);
				outList.add(obj);
			}

		} catch (SQLException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			throw new FactoryException(e.getMessage());
		}
		finally{
			try{
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				connection.close();
			}
			catch(SQLException e){
				logger.error(e);
			}
		}
		
		return outList;
	}
	
	public ProcessingInstructionType getPagingInstruction(long startIndex)
	{
		return getPagingInstruction(startIndex, defaultPageSize);
	}
	public ProcessingInstructionType getPagingInstruction(long startIndex, int recordCount)
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();

		instruction.setOrderClause(Columns.get(ColumnEnumType.AUDITDATE) + " ASC");
		instruction.setPaginate(true);
		instruction.setRecordCount(recordCount);
		instruction.setStartIndex(startIndex);
		return instruction;
	}
	public ProcessingInstructionType getPagingInstruction()
	{
		return getPagingInstruction(0, defaultPageSize);
	}
	
	protected AuditType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException
	{
			AuditType audit = new AuditType();
			return read(rset, audit);
	}
	protected AuditType read(ResultSet rset, AuditType obj) throws SQLException
	{
		obj.setId(rset.getLong(Columns.get(ColumnEnumType.ID)));
		obj.setAuditLevelType(LevelEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.AUDITLEVELTYPE))));
		obj.setAuditDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.AUDITDATE))));
		obj.setAuditResultDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.AUDITRESULTDATE))));
		obj.setAuditExpiresDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.AUDITEXPIRESDATE))));
		obj.setAuditActionType(ActionEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.AUDITACTIONTYPE))));
		obj.setAuditActionSource(rset.getString(Columns.get(ColumnEnumType.AUDITACTIONSOURCE)));
		obj.setAuditResultType(ResponseEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.AUDITRESULTTYPE))));
		obj.setAuditResultData(rset.getString(Columns.get(ColumnEnumType.AUDITRESULTDATA)));
		obj.setAuditRetentionType(RetentionEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.AUDITRETENTIONTYPE))));
		obj.setAuditSourceType(AuditEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.AUDITSOURCETYPE))));
		obj.setAuditSourceData(rset.getString(Columns.get(ColumnEnumType.AUDITSOURCEDATA)));
		obj.setAuditTargetType(AuditEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.AUDITTARGETTYPE))));
		obj.setAuditTargetData(rset.getString(Columns.get(ColumnEnumType.AUDITTARGETDATA)));
		return obj;
	}
	
	
}
