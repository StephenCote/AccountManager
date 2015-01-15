package org.cote.accountmanager.data.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.LevelEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;
import org.cote.accountmanager.objects.types.RetentionEnumType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class AuditFactory extends FactoryBase {
	private int defaultPageSize = 10;
	
	public AuditFactory(){
		super();
		this.scopeToOrganization = false;
		this.tableNames.add("audit");
	}
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		DataTable table = this.getDataTable("audit");
		table.setBulkInsert(true);
		
	}
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("audit")){
			table.setRestrictUpdateColumn("id", true);
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
		DataRow row = prepareAdd(new_audit,"audit");
		getDataTable("audit").getRows().add(row);
		return true;
	}
	

	public DataRow prepareAdd(AuditType obj, String tableName) throws FactoryException{
		DataTable table = getDataTable(tableName);
		if(table == null) throw new FactoryException("Table doesn't exist:" + tableName);
		DataRow row = table.newRow();
		try{
			
			row.setCellValue("auditactionsource", obj.getAuditActionSource());
			row.setCellValue("auditleveltype", obj.getAuditLevelType().toString());
			row.setCellValue("auditactiontype", obj.getAuditActionType().toString());
			row.setCellValue("auditdate", obj.getAuditDate());
			row.setCellValue("auditresultdate", obj.getAuditResultDate());
			row.setCellValue("auditexpiresdate", obj.getAuditExpiresDate());
			row.setCellValue("auditresultdata", obj.getAuditResultData());
			row.setCellValue("auditresulttype", obj.getAuditResultType().toString());
			row.setCellValue("auditretentiontype", obj.getAuditRetentionType().toString());
			row.setCellValue("auditsourcedata", obj.getAuditSourceData());
			row.setCellValue("auditsourcetype", obj.getAuditSourceType().toString());
			row.setCellValue("audittargetdata", obj.getAuditTargetData());
			row.setCellValue("audittargettype", obj.getAuditTargetType().toString());
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
		List<AuditType> out_list = new ArrayList<AuditType>();
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		//if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");
		DataTable table = getDataTable("audit");
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, 0);
		System.out.println(sqlQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				AuditType obj = this.read(rset, instruction);
				out_list.add(obj);
			}
			rset.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FactoryException(e.getMessage());
		}
		return out_list;
	}
	
	public ProcessingInstructionType getPagingInstruction(long startIndex)
	{
		return getPagingInstruction(startIndex, defaultPageSize);
	}
	public ProcessingInstructionType getPagingInstruction(long startIndex, int recordCount)
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();

		instruction.setOrderClause("auditdate ASC");
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
			/// throw new FactoryException("This is an artifact from java<->c#<->java conversions - should be an abstract class + interface, not an override");
			AuditType audit = new AuditType();
			return read(rset, audit);
	}
	protected AuditType read(ResultSet rset, AuditType obj) throws SQLException, FactoryException
	{
		obj.setId(rset.getLong("id"));
		obj.setAuditLevelType(LevelEnumType.valueOf(rset.getString("auditleveltype")));
		obj.setAuditDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("auditdate")));
		obj.setAuditResultDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("auditresultdate")));
		obj.setAuditExpiresDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("auditexpiresdate")));
		obj.setAuditActionType(ActionEnumType.valueOf(rset.getString("auditactiontype")));
		obj.setAuditActionSource(rset.getString("auditactionsource"));
		obj.setAuditResultType(ResponseEnumType.valueOf(rset.getString("auditresulttype")));
		obj.setAuditResultData(rset.getString("auditresultdata"));
		obj.setAuditRetentionType(RetentionEnumType.valueOf(rset.getString("auditretentiontype")));
		obj.setAuditSourceType(AuditEnumType.valueOf(rset.getString("auditsourcetype")));
		obj.setAuditSourceData(rset.getString("auditsourcedata"));
		obj.setAuditTargetType(AuditEnumType.valueOf(rset.getString("audittargettype")));
		obj.setAuditTargetData(rset.getString("audittargetdata"));
		return obj;
	}
	
	
}
