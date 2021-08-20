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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.SpoolStatusEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public abstract class SpoolFactory extends FactoryBase {
	private Map<String, String> typeNameIdMap = null;
	private Map<String,Integer> typeNameMap = null;
	private Map<String,Integer> typeIdMap = null;
	private List<BaseSpoolType> typeMap = null;
	private long cacheExpires = 0;
	private int cacheExpiry = 5;
	private int defaultPageSize = 10;
	
	public SpoolFactory(){
		super();
		this.scopeToOrganization = true;
		this.primaryTableName = "spool";
		this.tableNames.add(primaryTableName);
		typeNameIdMap = Collections.synchronizedMap(new HashMap<>());
		typeNameMap = Collections.synchronizedMap(new HashMap<>());
		typeIdMap = Collections.synchronizedMap(new HashMap<>());
		typeMap = new ArrayList<>();
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			table.setRestrictUpdateColumn("spoolguid", true);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T newSpoolEntry(SpoolBucketEnumType type) throws ArgumentException{
		BaseSpoolType spoolItem = null;
		switch(type){
			case MESSAGE_QUEUE: spoolItem = new MessageSpoolType();break;
			case SECURITY_TOKEN: spoolItem = new SecuritySpoolType();break;
			default: throw new ArgumentException("Unsupported spool type: " + type.toString());
		}
		spoolItem.setSpoolBucketType(type);
		spoolItem.setGuid(UUID.randomUUID().toString());
		spoolItem.setOwnerId(0L);
		spoolItem.setValueType(ValueEnumType.UNKNOWN);
		spoolItem.setCreated(CalendarUtil.getXmlGregorianCalendar(Calendar.getInstance().getTime()));
		spoolItem.setExpiration(spoolItem.getCreated());
		spoolItem.setExpires(true);
		spoolItem.setSpoolStatus(SpoolStatusEnumType.UNKNOWN);
		spoolItem.setRecipientId(0L);
		spoolItem.setRecipientType(FactoryEnumType.UNKNOWN);
		spoolItem.setReferenceId(0L);
		spoolItem.setReferenceType(FactoryEnumType.UNKNOWN);
		spoolItem.setTransportId(0L);
		spoolItem.setTransportType(FactoryEnumType.UNKNOWN);
		spoolItem.setCredentialId(0L);
		spoolItem.setCurrentLevel(0);
		spoolItem.setEndLevel(0);
		spoolItem.setGroupId(0L);
		
		return (T)spoolItem;
	}
	
	public DataRow prepareAdd(BaseSpoolType obj, String tableName) throws FactoryException{
		DataTable table = getDataTable(tableName);
		if(table == null) throw new FactoryException("Table doesn't exist:" + tableName);
		DataRow row = table.newRow();
		try{
			row.setCellValue("guid", obj.getGuid());
			row.setCellValue("parentguid", obj.getParentGuid());
			row.setCellValue("spoolbucketname", obj.getSpoolBucketName().toString());
			row.setCellValue("spoolbuckettype",obj.getSpoolBucketType().toString());
			row.setCellValue("createddate", obj.getCreated());
			row.setCellValue("expirationdate", obj.getExpiration());
			row.setCellValue("expires", obj.getExpires());
			row.setCellValue("name", obj.getName());
			if(obj.getData() != null) row.setCellValue("spooldata", obj.getData());
			row.setCellValue("spoolstatus", obj.getSpoolStatus().toString());
			row.setCellValue("ownerid", obj.getOwnerId());
			row.setCellValue("groupid", obj.getGroupId());
			if(scopeToOrganization) row.setCellValue("organizationid", obj.getOrganizationId());
			row.setCellValue("spoolvaluetype", obj.getValueType().toString());
			row.setCellValue("credentialid", obj.getCredentialId());
			row.setCellValue("referenceid", obj.getReferenceId());
			row.setCellValue("referencetype", obj.getReferenceType().toString());
			row.setCellValue("recipientid", obj.getRecipientId());
			row.setCellValue("recipienttype", obj.getRecipientType().toString());
			row.setCellValue("transportid", obj.getTransportId());
			row.setCellValue("transporttype", obj.getTransportType().toString());
			row.setCellValue("currentlevel", obj.getCurrentLevel());
			row.setCellValue("endlevel", obj.getEndLevel());
			row.setCellValue("classification", obj.getClassification());
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return row;
	}
	

	protected List<BaseSpoolType> getByField(QueryField field, long organizationId) throws FactoryException, ArgumentException
	{
		return getByField(field, null, organizationId);
	}
	protected List<BaseSpoolType> getByField(QueryField field, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException
	{
		return getByField(new QueryField[]{field}, instruction, organizationId);
	}
	protected List<BaseSpoolType> getByField(QueryField[] fields, long organizationId) throws FactoryException, ArgumentException
	{
		return getByField(fields, null, organizationId);
	}
	protected List<BaseSpoolType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException{
		List<BaseSpoolType> outList = new ArrayList<>();
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
				BaseSpoolType obj = this.read(rset, instruction);
				outList.add(obj);
			}
			rset.close();
			
		} catch (SQLException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outList;
	}	
	
	protected BaseSpoolType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		throw new FactoryException("This is an artifact from java<->c#<->java conversions - should be an abstract class + interface, not an override");
	}

	protected BaseSpoolType read(ResultSet rset, BaseSpoolType obj) throws SQLException
	{
		obj.setGuid(rset.getString("guid"));
		obj.setParentGuid(rset.getString("parentguid"));
		obj.setOwnerId(rset.getLong("ownerid"));
		obj.setOrganizationId(rset.getLong("organizationid"));
		obj.setGroupId(rset.getLong("groupid"));

		obj.setSpoolBucketName(SpoolNameEnumType.valueOf(rset.getString("spoolbucketname")));
		obj.setSpoolBucketType(SpoolBucketEnumType.valueOf(rset.getString("spoolbuckettype")));
		obj.setValueType(ValueEnumType.valueOf(rset.getString("spoolvaluetype")));
		obj.setSpoolStatus(SpoolStatusEnumType.valueOf(rset.getString("spoolstatus")));
		obj.setName(rset.getString("name"));
		obj.setData(rset.getBytes("spooldata"));
		obj.setCreated(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		obj.setExpiration(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));
		obj.setExpires(rset.getBoolean("expires"));
		obj.setReferenceId(rset.getLong("referenceid"));
		obj.setReferenceType(FactoryEnumType.valueOf(rset.getString("referencetype")));
		obj.setRecipientId(rset.getLong("recipientid"));
		obj.setRecipientType(FactoryEnumType.valueOf(rset.getString("recipienttype")));
		obj.setTransportId(rset.getLong("transportid"));
		obj.setTransportType(FactoryEnumType.valueOf(rset.getString("transporttype")));
		obj.setCredentialId(rset.getLong("credentialid"));
		obj.setCurrentLevel(rset.getInt("currentlevel"));
		obj.setEndLevel(rset.getInt("endlevel"));
		obj.setClassification(rset.getString("classification"));
		
		return obj;
	}
	
	public boolean update(BaseSpoolType map) throws FactoryException
	{
		return update(map, null);
	}
	public boolean update(BaseSpoolType map, ProcessingInstructionType instruction) throws FactoryException
	{
		DataTable table = dataTables.get(0);
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		List<QueryField> queryFields = new ArrayList<>();
		List<QueryField> updateFields = new ArrayList<>();

		queryFields.add(QueryFields.getFieldGuid(map.getGuid()));
		queryFields.add(QueryFields.getFieldOrganization(map.getOrganizationId()));
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
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		
		try {
			connection.close();
		} catch (SQLException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return (updated > 0);
	}
	public void setFactoryFields(List<QueryField> fields, BaseSpoolType map, ProcessingInstructionType instruction){
		fields.add(QueryFields.getFieldParentGuid(map.getParentGuid()));
		fields.add(QueryFields.getFieldSpoolBucketName(map.getSpoolBucketName()));
		fields.add(QueryFields.getFieldSpoolBucketType(map.getSpoolBucketType()));
		fields.add(QueryFields.getFieldSpoolData(map.getData()));
		fields.add(QueryFields.getFieldSpoolValueType(map.getValueType()));
		fields.add(QueryFields.getFieldExpirationDate(map.getExpiration()));
		fields.add(QueryFields.getFieldExpires(map.getExpires()));
		fields.add(QueryFields.getFieldGroup(map.getGroupId()));
		fields.add(QueryFields.getFieldSpoolStatus(map.getSpoolStatus()));
		fields.add(QueryFields.getFieldCredentialId(map.getCredentialId()));
		fields.add(QueryFields.getFieldReferenceId(map.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(map.getReferenceType()));
		fields.add(QueryFields.getFieldRecipientId(map.getRecipientId()));
		fields.add(QueryFields.getFieldRecipientType(map.getRecipientType()));
		fields.add(QueryFields.getFieldTransportId(map.getTransportId()));
		fields.add(QueryFields.getFieldTransportType(map.getTransportType()));
		fields.add(QueryFields.getFieldCurrentLevel(map.getCurrentLevel()));
		fields.add(QueryFields.getFieldEndLevel(map.getEndLevel()));
		fields.add(QueryFields.getFieldClassification(map.getClassification()));
	}
	private void setNameIdFields(List<QueryField> fields, BaseSpoolType map){
			fields.add(QueryFields.getFieldName(map.getName()));
			fields.add(QueryFields.getFieldOwner(map.getOwnerId()));
			if(scopeToOrganization) fields.add(QueryFields.getFieldOrganization(map.getOrganizationId()));
	}

	
	public ProcessingInstructionType getPagingInstruction(long startIndex)
	{
		return getPagingInstruction(startIndex, defaultPageSize);
	}
	public ProcessingInstructionType getPagingInstruction(long startIndex, int recordCount)
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();

		instruction.setOrderClause("createddate ASC,name ASC");
		instruction.setPaginate(true);
		instruction.setRecordCount(recordCount);
		instruction.setStartIndex(startIndex);
		return instruction;
	}
	public ProcessingInstructionType getPagingInstruction()
	{
		return getPagingInstruction(0, defaultPageSize);
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
			clearCache();
		}
	}

	protected void removeFromCache(BaseSpoolType obj){
		removeFromCache(obj, obj.getName());
	}
	protected void removeFromCache(BaseSpoolType obj, String keyName){
		synchronized(typeMap){
			if(keyName == null){
				Iterator<String> keys = typeNameMap.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					if(key.equals(obj.getGuid())){
						keyName = typeNameIdMap.get(key);
						break;
					}
				}
			}
			if(typeNameMap.containsKey(keyName) && typeIdMap.containsKey(obj.getGuid())){
				int indexId = typeIdMap.get(obj.getGuid());
				typeNameMap.remove(keyName);
				typeMap.set(indexId, null);
				typeIdMap.remove(obj.getGuid());
				typeNameIdMap.remove(obj.getGuid());
			}
		}
	}
	@SuppressWarnings("unchecked")
	public <T> T readCache(String name){
		checkCacheExpires();
		if(typeNameMap.containsKey(name)){
			return (T)typeMap.get(typeNameMap.get(name));
		}
		return null;
	}

	public boolean addToCache(BaseSpoolType map){
		return addToCache(map, map.getName());
	}
	public boolean addToCache(BaseSpoolType map, String keyName){
		synchronized(typeMap){
			int length = typeMap.size();
			if(typeNameMap.containsKey(keyName) || typeIdMap.containsKey(map.getGuid())){
				return false;
			}
			typeMap.add(map);
			typeNameMap.put(keyName, length);
			typeIdMap.put(map.getGuid(), length);
			typeNameIdMap.put(map.getGuid(), map.getName());
		}
		return true;
	}
	
}
