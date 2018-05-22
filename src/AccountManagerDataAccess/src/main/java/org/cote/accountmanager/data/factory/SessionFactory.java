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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserSessionDataType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class SessionFactory extends FactoryBase {
	
	public static final Logger logger = LogManager.getLogger(SessionFactory.class);
	
	private int defaultPageSize = 10;
	private Map<String,Integer> typeIdMap = null;
	private List<UserSessionType> typeMap = null;
	private long cacheExpires = 0;
	private int cacheExpiry = 5;

	public SessionFactory(){
		super();
		this.scopeToOrganization = true;
		this.primaryTableName = "session";
		this.secondaryTableName = "sessiondata";

		this.tableNames.add(primaryTableName);
		this.tableNames.add(secondaryTableName);
		typeIdMap = Collections.synchronizedMap(new HashMap<String,Integer>());
		typeMap = new ArrayList<>();
	}
	
	@Override
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		DataTable table = this.getDataTable(secondaryTableName);
		table.setBulkInsert(true);
		
	}
	public boolean clearSessions()
	{
		return clearSession(null);
	}

	public boolean clearSession(String sessionId)
	{
		
		boolean outBool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();

		try
		{
			clearSession(connection, sessionId);
			clearSessionData(connection, sessionId);
			if(sessionId != null) removeFromCache(sessionId);
			else clearCache();
			outBool = true;
		}
		catch (Exception sqe)
		{
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outBool;
	}
	public boolean clearSessionData(String sessionId)
	{
		
		boolean outBool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		
		try
		{
			clearSessionData(connection, sessionId);
			connection.close();
			removeFromCache(sessionId);
			outBool = true;
		}
		catch (Exception sqe)
		{
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outBool;
	}
	private boolean clearSessionData(Connection connection, String sessionId) throws SQLException, FactoryException{
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		List<QueryField> fields = new ArrayList<>();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		if(sessionId != null) fields.add(QueryFields.getFieldSessionId(sessionId));
		String limit1 = (connectionType == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "");
		String limit2 = ((connectionType == CONNECTION_TYPE.MYSQL) ? " LIMIT 200 OFFSET 0" : "");
		String where = (!fields.isEmpty() ? " WHERE  " +  getQueryClause(null,fields.toArray(new QueryField[0]), token) : "");
		String sql = String.format("%s DELETE FROM %s %s %s;",limit1,secondaryTableName,where,limit2);
		PreparedStatement statement = connection.prepareStatement(sql);
		DBFactory.setStatementParameters(fields.toArray(new QueryField[0]), statement);
		
		int affected = statement.executeUpdate();
		while (affected > 0)
		{
			affected = statement.executeUpdate();
		}
		statement.close();
		return true;

	}
	private boolean clearSession(Connection connection, String sessionId) throws SQLException, FactoryException{

		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		List<QueryField> fields = new ArrayList<>();
		if(sessionId != null) fields.add(QueryFields.getFieldSessionId(sessionId));
		String limit1 = (connectionType == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "");
		String limit2 = ((connectionType == CONNECTION_TYPE.MYSQL) ? " LIMIT 200 OFFSET 0" : "");
		String where = (!fields.isEmpty() ? " WHERE  " +  getQueryClause(null,fields.toArray(new QueryField[0]), token) : "");
		String sql = String.format("%s DELETE FROM %s %s %s;", limit1, primaryTableName, where, limit2);
		PreparedStatement statement = connection.prepareStatement(sql);
		DBFactory.setStatementParameters(fields.toArray(new QueryField[0]), statement);
		
		int affected = statement.executeUpdate();
		while (affected > 0)
		{
			affected = statement.executeUpdate();
		}
		statement.close();
		return true;

	}
	public UserSessionType newUserSession(){
		return newUserSession(UUID.randomUUID().toString());
	}
	public UserSessionType newUserSession(String sessionId){
		UserSessionType session = new UserSessionType();
		Calendar now = Calendar.getInstance();
 
		session.setSessionCreated(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		session.setSessionAccessed(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		now.add(Calendar.HOUR, 1);
		session.setSessionExpires(CalendarUtil.getXmlGregorianCalendar(now.getTime()));

		session.setSessionStatus(SessionStatusEnumType.UNKNOWN);
		session.setSessionId(sessionId);
		return session;
	}
	public UserSessionType newUserSession(UserType user){
		return newUserSession(user, UUID.randomUUID().toString());
	}
	public UserSessionType newUserSession(UserType user, String sessionId){
		UserSessionType session = newUserSession(sessionId);
		if(user != null){
			session.setUserId(user.getId());
			session.setOrganizationId(user.getOrganizationId());
		}
		return session;
	}
	
	public boolean addSession(UserSessionType new_session) throws FactoryException
	{
		if(isValid(new_session) == false) throw new FactoryException("Session does not contain valid data.");
		return insertRow(prepareAdd(new_session,"session"));
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName) || table.getName().equalsIgnoreCase(secondaryTableName)){
			table.setRestrictUpdateColumn("sessionid", true);
			table.setRestrictUpdateColumn("organizationid", true);
		}
	}
	public DataRow prepareAdd(UserSessionType obj, String tableName) throws FactoryException{
		DataTable table = getDataTable(tableName);
		if(table == null) throw new FactoryException("Table doesn't exist:" + tableName);
		DataRow row = table.newRow();
		try{
			
			row.setCellValue("userid", obj.getUserId());
			row.setCellValue("sessionid", obj.getSessionId());
			if(obj.getSecurityId() != null) row.setCellValue("securityid", obj.getSecurityId());
			row.setCellValue("sessioncreated", obj.getSessionCreated());
			row.setCellValue("sessionaccessed", obj.getSessionAccessed());
			row.setCellValue("sessionexpiration", obj.getSessionExpires());
			row.setCellValue("sessionstatus", obj.getSessionStatus().toString());
			row.setCellValue("sessiondatasize", obj.getDataSize());
			row.setCellValue("organizationid", obj.getOrganizationId());

		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return row;
	}
	public DataRow prepareAdd(UserSessionType obj, UserSessionDataType data, String tableName) throws FactoryException{
		DataTable table = getDataTable(tableName);
		if(table == null) throw new FactoryException("Table doesn't exist:" + tableName);
		DataRow row = table.newRow();
		try{
			row.setCellValue("userid", obj.getUserId());
			row.setCellValue("sessionid", obj.getSessionId());
			row.setCellValue("expiration", obj.getSessionExpires());
			row.setCellValue("data", data.getValue());
			row.setCellValue("name", data.getName());
			row.setCellValue("organizationid", obj.getOrganizationId());	
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return row;
	}
	public boolean isValid(UserSessionType session)
	{
		if (session != null)
		{
			if (
				// if session expiration is 0, then session will last until all sessions are cleared
				//
				CalendarUtil.getTimeSpanFromNow(session.getSessionExpires()) >= 0
			)
			{
				return true;
			}
		}
		return false;
	}
	

	public boolean update(UserSessionType map) throws FactoryException
	{
		return update(map, null, false);
	}
	public boolean update(UserSessionType map, ProcessingInstructionType instruction, boolean recover) throws FactoryException
	{
		DataTable table = getDataTable("session");
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		List<QueryField> queryFields = new ArrayList<>();
		List<QueryField> updateFields = new ArrayList<>();

		queryFields.add(QueryFields.getFieldSessionId(map.getSessionId()));
		queryFields.add(QueryFields.getFieldOrganization(map.getOrganizationId()));
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
			removeFromCache(map);
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
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
		if(recover == false && updated <= 0){
			logger.error("Session Error Detected.  Attempting to Recover "  + map.getSessionId() + " in organization id " + map.getOrganizationId());
			Factories.getSessionFactory().clearSession(map.getSessionId());
			return addSession(map);
		}
		return (updated > 0);
	}
	public void setFactoryFields(List<QueryField> fields, UserSessionType map, ProcessingInstructionType instruction){
		
		fields.add(QueryFields.getFieldUserId(map.getUserId()));
		fields.add(QueryFields.getFieldSessionId(map.getSessionId()));
		fields.add(QueryFields.getFieldSecurityId(map.getSecurityId()));
		fields.add(QueryFields.getFieldSessionCreated(map.getSessionCreated()));
		fields.add(QueryFields.getFieldSessionExpiration(map.getSessionExpires()));
		fields.add(QueryFields.getFieldSessionAccessed(map.getSessionAccessed()));
		fields.add(QueryFields.getFieldSessionStatus(map.getSessionStatus()));
		fields.add(QueryFields.getFieldSessionDataSize(map.getDataSize()));
	}
	private void setNameIdFields(List<QueryField> fields, UserSessionType map){
			if(scopeToOrganization) fields.add(QueryFields.getFieldOrganization(map.getOrganizationId()));
	}
	
	
	public boolean updateData(UserSessionType map) throws FactoryException
	{
		ProcessingInstructionType instruction = getPagingInstruction(0);
		instruction.setOrderClause("name ASC");
		return updateData(map, instruction);
	}
	public boolean updateData(UserSessionType map, ProcessingInstructionType instruction) throws FactoryException
	{
		List<UserSessionDataType> changes = map.getChangeSessionData();
		List<UserSessionDataType> filtered = new ArrayList<>();
		if(changes.isEmpty()) return true;
		
		DataTable table = getDataTable(secondaryTableName);
		boolean outBool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		StringBuilder buff = new StringBuilder();
		
		buff.append("DELETE FROM SessionData WHERE SessionId = " + token + " AND (");
		/// Why is this not a dictionary?
		///

		Map<String,Boolean> changed = new HashMap<>();
		
		int changeCount = 0;
		for (int i = changes.size() - 1; i >=0; i--)
		{
			if(changed.containsKey(changes.get(i).getName())) continue;
			changed.put(changes.get(i).getName(), true);
			if (changeCount > 0) buff.append(" OR ");
			buff.append("Name = " + token);
			filtered.add(changes.get(i));
			changeCount++;
		}
		buff.append(")");
		PreparedStatement statement = null;
		try{
			statement = connection.prepareStatement(buff.toString());
			DBFactory.setStatementParameter(statement, SqlDataEnumType.VARCHAR, map.getSessionId(), 1);
			for (int i = 0; i < changeCount; i++)
			{
				DBFactory.setStatementParameter(statement, SqlDataEnumType.VARCHAR, filtered.get(i).getName(), (i + 2));
			}
			statement.executeUpdate();

			

			for(int i = 0; i < changeCount; i++){
				UserSessionDataType data = filtered.get(i);
				if(data.getValue() == null) continue;
				DataRow new_row = table.addNewRow();
				new_row.setCellValue("userid", map.getUserId());
				new_row.setCellValue("sessionid", map.getSessionId());
				new_row.setCellValue("expiration", map.getSessionExpires());
				new_row.setCellValue("name", data.getName());
				new_row.setCellValue("data", data.getValue());
				new_row.setCellValue("organizationid", map.getOrganizationId());
				insertRow(new_row);
			}
			writeSpool("sessiondata");
			removeFromCache(map);
			outBool = true;
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		catch(DataAccessException de){
			logger.error(FactoryException.LOGICAL_EXCEPTION,de);
		}
		finally{
			try {
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e);
			}
			
		}
		return outBool;
	}
	/// 2014/03/13 - made this synchronized due to race condition from initial session sending in multiple requests
	///
	public synchronized UserSessionType getCreateSession(String sessionId, long organizationId) throws FactoryException{
		if(sessionId == null){
			logger.error("Session ID is null");
			return null;
		}
		if(organizationId <= 0L){
			logger.error("Organization is null");
			return null;
		}
		UserSessionType session = getSession(sessionId, organizationId);
		if(session != null) return session;
		session = this.newUserSession(sessionId);
		session.setOrganizationId(organizationId);
		if(addSession(session)) return session;
		return null;
	}
	public UserSessionType getSession(String sessionId, long organizationId) throws FactoryException
	{
		UserSessionType outSession = readCache(sessionId);
		if(outSession != null) return outSession;
		ProcessingInstructionType instruction = getPagingInstruction(0);
		List<UserSessionType> sessions = getByField(new QueryField[]{QueryFields.getFieldSessionId(sessionId)}, instruction, organizationId);
		UserSessionType session = null;
		if(!sessions.isEmpty()){
			session = sessions.get(0);
			if(isValid(session) == false){
				logger.error("Session is invalid; deleting session.");
				logger.error("Session Id: " + session.getSessionId());
				logger.error("Session Created: " + session.getSessionCreated().toString());
				logger.error("Session Expires: " + session.getSessionExpires().toString());
				clearSession(session.getSessionId());
				removeFromCache(session);
				session = null;
			}
			else{
				populateSessionData(session);
				addToCache(session);
			}
			
		}
		return session;
	}
	public boolean populateSessionData(UserSessionType session) throws FactoryException{
		boolean outBool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = getDataTable(secondaryTableName);
		QueryField[] fields = new QueryField[]{QueryFields.getFieldSessionId(session.getSessionId())};
		ProcessingInstructionType instruction = getPagingInstruction(0);
		instruction.setOrderClause("name ASC");
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, session.getOrganizationId());

		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				UserSessionDataType obj = new UserSessionDataType(); 
				obj.setName(rset.getString("name"));
				obj.setValue(rset.getString("data"));

				session.getSessionData().add(obj);
			}
			session.setDataSize(session.getSessionData().size());
			rset.close();
			
			outBool = true;
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

		return outBool;
	}
	protected List<UserSessionType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organizationId) throws FactoryException{
		List<UserSessionType> outList = new ArrayList<>();
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = getDataTable(primaryTableName);
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, organizationId);

		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				UserSessionType obj = this.read(rset, instruction);
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
	
	public ProcessingInstructionType getPagingInstruction(long startIndex)
	{
		return getPagingInstruction(startIndex, defaultPageSize);
	}
	public ProcessingInstructionType getPagingInstruction(long startIndex, int recordCount)
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();

		instruction.setOrderClause("sessioncreated ASC");
		instruction.setPaginate(true);
		instruction.setRecordCount(recordCount);
		instruction.setStartIndex(startIndex);
		return instruction;
	}
	public ProcessingInstructionType getPagingInstruction()
	{
		return getPagingInstruction(0, defaultPageSize);
	}
	
	protected UserSessionType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException
	{
			UserSessionType session = new UserSessionType();
			return read(rset, session);
	}
	protected UserSessionType read(ResultSet rset, UserSessionType obj) throws SQLException, FactoryException
	{
		obj.setDataSize(rset.getInt("sessiondatasize"));
		obj.setOrganizationId(rset.getLong("organizationid"));
		obj.setSecurityId(rset.getString("securityid"));
		obj.setSessionAccessed(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("sessionaccessed")));
		obj.setSessionCreated(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("sessioncreated")));
		obj.setSessionExpires(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("sessionexpiration")));
		obj.setSessionStatus(SessionStatusEnumType.valueOf(rset.getString("sessionstatus")));
		obj.setUserId(rset.getLong("userid"));
		obj.setSessionId(rset.getString("sessionid"));
		
		
		
		return obj;
	}
	
	public void updateSessionToCache(UserSessionType session){
		logger.info("Updating Session To Cache: " + session.getSessionId() + " / Remove: " + (haveCacheId(session.getSessionId()) ? "true":"false"));
		if(this.haveCacheId(session.getSessionId())) removeFromCache(session.getSessionId());
		addToCache(session);
	}
	public void clearCache(){
		typeIdMap.clear();
		typeMap.clear();
		cacheExpires = System.currentTimeMillis() + (cacheExpiry * 60000);
	}
	
	protected void checkCacheExpires(){
		if(cacheExpires <= System.currentTimeMillis()){
			clearCache();
		}
	}
	public boolean haveCacheId(String id){
		return typeIdMap.containsKey(id);
	}
	public void removeFromCache(String sessionId){
		UserSessionType obj = readCache(sessionId);
		if(obj != null) removeFromCache(obj);
	}
	public synchronized void removeFromCache(UserSessionType obj){
		if(typeIdMap.containsKey(obj.getSessionId())){
			int indexId = typeIdMap.get(obj.getSessionId());
			typeMap.set(indexId, null);
			typeIdMap.remove(obj.getSessionId());
		}
	}


	@SuppressWarnings("unchecked")
	public <T> T readCache(String id){
		checkCacheExpires();
		if(typeIdMap.containsKey(id)){
			return (T)typeMap.get(typeIdMap.get(id));
		}
		return null;
	}

	public synchronized boolean addToCache(UserSessionType map){
		int length = typeMap.size();
		if(typeIdMap.containsKey(map.getSessionId())){
			return false;
		}
		typeMap.add(map);
		typeIdMap.put(map.getSessionId(), length);
		return true;
	}
}
