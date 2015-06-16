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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cote.accountmanager.data.ArgumentException;
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
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class SpoolFactory extends FactoryBase {
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
		this.tableNames.add("spool");
		typeNameIdMap = Collections.synchronizedMap(new HashMap<String,String>());
		typeNameMap = Collections.synchronizedMap(new HashMap<String,Integer>());
		typeIdMap = Collections.synchronizedMap(new HashMap<String,Integer>());
		typeMap = new ArrayList<BaseSpoolType>();
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("spool")){
			table.setRestrictUpdateColumn("spoolguid", true);
		}
	}
	public DataRow prepareAdd(BaseSpoolType obj, String tableName) throws FactoryException{
		DataTable table = getDataTable(tableName);
		if(table == null) throw new FactoryException("Table doesn't exist:" + tableName);
		DataRow row = table.newRow();
		try{
			row.setCellValue("spoolguid", obj.getGuid());
			row.setCellValue("spoolbucketname", obj.getSpoolBucketName().toString());
			row.setCellValue("spoolbuckettype",obj.getSpoolBucketType().toString());
			row.setCellValue("spoolcreated", obj.getCreated());
			row.setCellValue("spoolexpiration", obj.getExpiration());
			row.setCellValue("spoolexpires", obj.getExpires());
			row.setCellValue("spoolname", obj.getName());
			if(obj.getData() != null) row.setCellValue("spooldata", obj.getData());
			row.setCellValue("spoolstatus", obj.getSpoolStatus());
			row.setCellValue("spoolownerid", obj.getOwnerId());
			if(obj.getGroup() != null) row.setCellValue("groupid", obj.getGroup().getId());
			if(scopeToOrganization) row.setCellValue("organizationid", obj.getOrganization().getId());
			row.setCellValue("spoolvaluetype", obj.getValueType().toString());
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return row;
	}
	

	protected List<BaseSpoolType> getByField(QueryField field, long organization_id) throws FactoryException, ArgumentException
	{
		return getByField(field, null, organization_id);
	}
	protected List<BaseSpoolType> getByField(QueryField field, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException
	{
		return getByField(new QueryField[]{field}, instruction, organization_id);
	}
	protected List<BaseSpoolType> getByField(QueryField[] fields, long organization_id) throws FactoryException, ArgumentException
	{
		return getByField(fields, null, organization_id);
	}
	protected List<BaseSpoolType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException{
		List<BaseSpoolType> out_list = new ArrayList<BaseSpoolType>();
		if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = this.dataTables.get(0);
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, organization_id);
		System.out.println(sqlQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				BaseSpoolType obj = this.read(rset, instruction);
				out_list.add(obj);
			}
			rset.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out_list;
	}	
	
	protected BaseSpoolType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		throw new FactoryException("This is an artifact from java<->c#<->java conversions - should be an abstract class + interface, not an override");
	}

	protected BaseSpoolType read(ResultSet rset, BaseSpoolType obj) throws SQLException, FactoryException, ArgumentException
	{
		obj.setGuid(rset.getString("spoolguid"));
		obj.setOwnerId(rset.getLong("spoolownerid"));
		obj.setOrganization(Factories.getOrganizationFactory().getOrganizationById(rset.getLong("organizationid")));
		obj.setGroup(Factories.getGroupFactory().getDirectoryById(rset.getLong("groupid"), obj.getOrganization()));

		obj.setSpoolBucketName(SpoolNameEnumType.valueOf(rset.getString("spoolbucketname")));
		obj.setSpoolBucketType(SpoolBucketEnumType.valueOf(rset.getString("spoolbuckettype")));
		obj.setValueType(ValueEnumType.valueOf(rset.getString("spoolvaluetype")));
		obj.setName(rset.getString("spoolname"));
		obj.setData(rset.getString("spooldata"));
		obj.setCreated(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("spoolcreated")));
		obj.setExpiration(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("spoolexpiration")));
		obj.setExpires(rset.getBoolean("spoolexpires"));
		obj.setSpoolStatus(rset.getInt("spoolstatus"));
		
		return obj;
	}
	
	public boolean update(BaseSpoolType map) throws FactoryException
	{
		return update(map, null);
	}
	public boolean update(BaseSpoolType map, ProcessingInstructionType instruction) throws FactoryException
	{
		DataTable table = dataTables.get(0);
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		List<QueryField> queryFields = new ArrayList<QueryField>();
		List<QueryField> updateFields = new ArrayList<QueryField>();

		queryFields.add(QueryFields.getFieldSpoolGuid(map));
		queryFields.add(QueryFields.getFieldOrganization(map.getOrganization().getId()));
		setNameIdFields(updateFields, map);
		setFactoryFields(updateFields, map, instruction);
		String sql = getUpdateTemplate(table, updateFields.toArray(new QueryField[0]), token) + " WHERE " + getQueryClause(queryFields.toArray(new QueryField[0]), token);

		// System.out.println("Update String = " + sql);
		
		updateFields.addAll(queryFields);
		
		int updated = 0;
		try{
			PreparedStatement statement = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(updateFields.toArray(new QueryField[0]), statement);
			updated = statement.executeUpdate();
		}
		catch(SQLException sqe){
			sqe.printStackTrace();
		}
		
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (updated > 0);
	}
	public void setFactoryFields(List<QueryField> fields, BaseSpoolType map, ProcessingInstructionType instruction){
		fields.add(QueryFields.getFieldSpoolBucketName(map.getSpoolBucketName()));
		fields.add(QueryFields.getFieldSpoolBucketType(map.getSpoolBucketType()));
		fields.add(QueryFields.getFieldSpoolData(map.getData()));
		fields.add(QueryFields.getFieldSpoolValueType(map.getValueType()));
		fields.add(QueryFields.getFieldSpoolExpiration(map.getExpiration()));
		fields.add(QueryFields.getFieldSpoolExpires(map.getExpires()));
		fields.add(QueryFields.getFieldGroup((map.getGroup() != null ? map.getGroup().getId() : 0)));
		fields.add(QueryFields.getFieldSpoolStatus(map.getSpoolStatus()));
	}
	private void setNameIdFields(List<QueryField> fields, BaseSpoolType map){
			fields.add(QueryFields.getFieldSpoolName(map));
			fields.add(QueryFields.getFieldSpoolOwner(map));
			if(scopeToOrganization) fields.add(QueryFields.getFieldOrganization(map.getOrganization().getId()));
	}

	
	public ProcessingInstructionType getPagingInstruction(long startIndex)
	{
		return getPagingInstruction(startIndex, defaultPageSize);
	}
	public ProcessingInstructionType getPagingInstruction(long startIndex, int recordCount)
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();

		instruction.setOrderClause("spoolcreated ASC,spoolname ASC");
		instruction.setPaginate(true);
		instruction.setRecordCount(recordCount);
		instruction.setStartIndex(startIndex);
		return instruction;
	}
	public ProcessingInstructionType getPagingInstruction()
	{
		return getPagingInstruction(0, defaultPageSize);
	}
	
	/// TODO: Stop duplicating this - it's centralized in NameIdFactory
	/// But the factories coming off of FactoryBase
	/// And the caching for those factories varies slightly between types
	
	
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
	protected boolean haveCacheId(int id){
		return typeIdMap.containsKey(id);
	}
	protected void removeFromCache(BaseSpoolType obj){
		removeFromCache(obj, obj.getName());
	}
	protected void removeFromCache(BaseSpoolType obj, String key_name){
		synchronized(typeMap){
			if(key_name == null){
				Iterator<String> keys = typeNameMap.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					if(typeNameMap.get(key).equals(obj.getGuid())){
						key_name = key;
						break;
					}
				}
			}
			//System.out.println("Remove from cache: " + key_name + ":" + typeNameMap.containsKey(key_name) + " and " + typeIdMap.containsKey(obj.getGuid()));
			if(typeNameMap.containsKey(key_name) && typeIdMap.containsKey(obj.getGuid())){
				int indexId = typeIdMap.get(obj.getGuid());
				typeNameMap.remove(key_name);
				typeMap.set(indexId, null);
				typeIdMap.remove(obj.getGuid());
				typeNameIdMap.remove(obj.getGuid());
			}
		}
	}
	public <T> T readCache(String name){
		checkCacheExpires();
		if(typeNameMap.containsKey(name)){
			return (T)typeMap.get(typeNameMap.get(name));
		}
		return null;
	}

	public <T> T readCache(int id){
		checkCacheExpires();
		if(typeIdMap.containsKey(id)){
			return (T)typeMap.get(typeIdMap.get(id));
		}
		return null;
	}
	public boolean addToCache(BaseSpoolType map){
		return addToCache(map, map.getName());
	}
	public boolean addToCache(BaseSpoolType map, String key_name){
		//System.out.println("Add to cache: " + (map == null ? "NULL" : map.getName()) + " AT " + key_name);
		synchronized(typeMap){
			int length = typeMap.size();
			if(typeNameMap.containsKey(key_name) || typeIdMap.containsKey(map.getGuid())){
				return false;
			}
			typeMap.add(map);
			typeNameMap.put(key_name, length);
			typeIdMap.put(map.getGuid(), length);
			typeNameIdMap.put(map.getGuid(), map.getName());
		}
		return true;
	}
	
}
