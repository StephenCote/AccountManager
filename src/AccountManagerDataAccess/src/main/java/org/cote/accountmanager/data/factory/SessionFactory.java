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

import org.apache.log4j.Logger;
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
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserSessionDataType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class SessionFactory extends FactoryBase {
	
	public static final Logger logger = Logger.getLogger(SessionFactory.class.getName());
	
	private int defaultPageSize = 10;
	private Map<String,Integer> typeIdMap = null;
	private List<UserSessionType> typeMap = null;
	private long cacheExpires = 0;
	private int cacheExpiry = 5;
	
	public SessionFactory(){
		super();
		this.scopeToOrganization = true;
		this.tableNames.add("session");
		this.tableNames.add("sessiondata");
		typeIdMap = Collections.synchronizedMap(new HashMap<String,Integer>());
		typeMap = new ArrayList<UserSessionType>();
	}
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		DataTable table = this.getDataTable("sessiondata");
		table.setBulkInsert(true);
		
	}
	public boolean clearSessions()
	{
		return clearSession(null);
	}

	public boolean clearSession(String session_id)
	{
		
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();

		try
		{
			clearSession(connection, session_id);
			clearSessionData(connection, session_id);
			connection.close();
			if(session_id != null) removeFromCache(session_id);
			else clearCache();
			out_bool = true;
		}
		catch (Exception sqe)
		{
			System.out.println(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out_bool;
	}
	public boolean clearSessionData(String session_id)
	{
		
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		
		try
		{
			clearSessionData(connection, session_id);
			connection.close();
			removeFromCache(session_id);
			out_bool = true;
		}
		catch (Exception sqe)
		{
			System.out.println(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out_bool;
	}
	private boolean clearSessionData(Connection connection, String session_id) throws SQLException, FactoryException{
		CONNECTION_TYPE connection_type = DBFactory.getConnectionType(connection);
		List<QueryField> fields = new ArrayList<QueryField>();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		if(session_id != null) fields.add(QueryFields.getFieldSessionId(session_id));
		String sql = (connection_type == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "") + "DELETE FROM sessiondata"
				+ (fields.size() > 0 ? " WHERE  " +  getQueryClause(fields.toArray(new QueryField[0]), token) : "")
				+ ((connection_type == CONNECTION_TYPE.MYSQL) ? " LIMIT 200 OFFSET 0" : "") + ";"
		;
		///  || connection_type == CONNECTION_TYPE.POSTGRES
		///
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
	private boolean clearSession(Connection connection, String session_id) throws SQLException, FactoryException{

		CONNECTION_TYPE connection_type = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		List<QueryField> fields = new ArrayList<QueryField>();
		if(session_id != null) fields.add(QueryFields.getFieldSessionId(session_id));
		
		String sql = (connection_type == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "") + "DELETE FROM session"
				+ (fields.size() > 0 ? " WHERE  " +  getQueryClause(fields.toArray(new QueryField[0]), token) : "")
				+ ((connection_type == CONNECTION_TYPE.MYSQL) ? " LIMIT 200 OFFSET 0" : "") + ";"
		///  || connection_type == CONNECTION_TYPE.POSTGRES
		///	No limit with PG DELETE
		;
		//System.out.println(sql);
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
	public UserSessionType newUserSession(String session_id){
		UserSessionType session = new UserSessionType();
		Calendar now = Calendar.getInstance();
 
		session.setSessionCreated(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		session.setSessionAccessed(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		now.add(Calendar.HOUR, 1);
		session.setSessionExpires(CalendarUtil.getXmlGregorianCalendar(now.getTime()));

		session.setSessionStatus(SessionStatusEnumType.UNKNOWN);
		session.setSessionId(session_id);
		return session;
	}
	public UserSessionType newUserSession(UserType user){
		return newUserSession(user, UUID.randomUUID().toString());
	}
	public UserSessionType newUserSession(UserType user, String session_id){
		UserSessionType session = newUserSession(session_id);
		if(user != null){
			session.setUserId(user.getId());
			session.setOrganizationId(user.getOrganization().getId());
		}
		return session;
	}
	
	public boolean addSession(UserSessionType new_session) throws FactoryException
	{
		if(isValid(new_session) == false) throw new FactoryException("Session does not contain valid data.");
		return insertRow(prepareAdd(new_session,"session"));
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("session") || table.getName().equalsIgnoreCase("sessiondata")){
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
		return update(map, null);
	}
	public boolean update(UserSessionType map, ProcessingInstructionType instruction) throws FactoryException
	{
		DataTable table = getDataTable("session");
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		List<QueryField> queryFields = new ArrayList<QueryField>();
		List<QueryField> updateFields = new ArrayList<QueryField>();

		queryFields.add(QueryFields.getFieldSessionId(map.getSessionId()));
		queryFields.add(QueryFields.getFieldOrganization(map.getOrganizationId()));
		setNameIdFields(updateFields, map);
		setFactoryFields(updateFields, map, instruction);
		String sql = getUpdateTemplate(table, updateFields.toArray(new QueryField[0]), token) + " WHERE " + getQueryClause(queryFields.toArray(new QueryField[0]), token);

		//System.out.println("Update String = " + sql);
		
		updateFields.addAll(queryFields);
		
		int updated = 0;
		try{
			PreparedStatement statement = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(updateFields.toArray(new QueryField[0]), statement);
			updated = statement.executeUpdate();
			removeFromCache(map);
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
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
		List<UserSessionDataType> filtered = new ArrayList<UserSessionDataType>();
		if(changes.size() == 0) return true;
		
		DataTable table = getDataTable("sessiondata");
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		StringBuilder buff = new StringBuilder();
		
		buff.append("DELETE FROM SessionData WHERE SessionId = " + token + " AND (");
		/// Why is this not a dictionary?
		///

		Hashtable changed = new Hashtable();
		
		int change_count = 0;
		for (int i = changes.size() - 1; i >=0; i--)
		{
			if(changed.containsKey(changes.get(i).getName())) continue;
			changed.put(changes.get(i).getName(), true);
			if (change_count > 0) buff.append(" OR ");
			buff.append("Name = " + token);
			filtered.add(changes.get(i));
			change_count++;
		}
		buff.append(")");
		try{
			PreparedStatement statement = connection.prepareStatement(buff.toString());
			DBFactory.setStatementParameter(statement, SqlDataEnumType.VARCHAR, map.getSessionId(), 1);
			for (int i = 0; i < change_count; i++)
			{
				DBFactory.setStatementParameter(statement, SqlDataEnumType.VARCHAR, filtered.get(i).getName(), (i + 2));
			}
			int removed = statement.executeUpdate();
			statement.close();
			connection.close();
			
			//System.out.println("Change Count:" + change_count);
			for(int i = 0; i < change_count; i++){
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
		}
		catch(SQLException sqe){
			sqe.printStackTrace();
		}
		catch(DataAccessException de){
			de.printStackTrace();
		}
		
		return true;
	}
	/// 2014/03/13 - made this synchronized due to race condition from initial session sending in multiple requests
	///
	public synchronized UserSessionType getCreateSession(String session_id, OrganizationType organization) throws FactoryException{
		if(session_id == null){
			logger.error("Session ID is null");
			return null;
		}
		if(organization == null){
			logger.error("Organization is null");
			return null;
		}
		UserSessionType session = getSession(session_id, organization);
		if(session != null) return session;
		session = this.newUserSession(session_id);
		session.setOrganizationId(organization.getId());
		if(addSession(session)) return session;
		return null;
	}
	public UserSessionType getSession(String session_id, OrganizationType organization) throws FactoryException
	{
		UserSessionType out_session = readCache(session_id);
		if(out_session != null) return out_session;
		ProcessingInstructionType instruction = getPagingInstruction(0);
		List<UserSessionType> sessions = getByField(new QueryField[]{QueryFields.getFieldSessionId(session_id)}, instruction, organization.getId());
		if(sessions.size() > 0){
			UserSessionType session = sessions.get(0);
			if(isValid(session) == false){
				System.out.println("Session is invalid; deleting session.");
				System.out.println("Session Id: " + session.getSessionId());
				System.out.println("Session Created: " + session.getSessionCreated().toString());
				System.out.println("Session Expires: " + session.getSessionExpires().toString());
				clearSession(session.getSessionId());
				removeFromCache(session);
				return null;
			}
			else{
				populateSessionData(session);
				addToCache(session);
				return session;
			}
			
		}
		return null;
	}
	public boolean populateSessionData(UserSessionType session) throws FactoryException{
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = getDataTable("sessiondata");
		QueryField[] fields = new QueryField[]{QueryFields.getFieldSessionId(session.getSessionId())};
		ProcessingInstructionType instruction = getPagingInstruction(0);
		instruction.setOrderClause("name ASC");
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, session.getOrganizationId());
		//System.out.println(sqlQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				UserSessionDataType obj = new UserSessionDataType(); 
				obj.setName(rset.getString("name"));
				obj.setValue(rset.getString("data"));
				//System.out.println("Fetching data: " + obj.getName());
				session.getSessionData().add(obj);
			}
			session.setDataSize(session.getSessionData().size());
			rset.close();
			
			out_bool = true;
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

		return out_bool;
	}
	protected List<UserSessionType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organization_id) throws FactoryException{
		List<UserSessionType> out_list = new ArrayList<UserSessionType>();
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		//if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");
		DataTable table = getDataTable("session");
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, organization_id);
		//System.out.println(sqlQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				UserSessionType obj = this.read(rset, instruction);
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
			/// throw new FactoryException("This is an artifact from java<->c#<->java conversions - should be an abstract class + interface, not an override");
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
	
	/// TODO: Find a better pattern for creating these slight variations of caching without having to duplicate the code
	public void updateSessionToCache(UserSessionType session){
		//String key_name = session.getSessionId();
		//System.out.println("Update user to cache: " + key_name);
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
		//synchronized(typeMap){

			//System.out.println("Remove from cache: " + obj.getSessionId() + " : " + typeIdMap.containsKey(obj.getSessionId()));
			if(typeIdMap.containsKey(obj.getSessionId())){
				int indexId = typeIdMap.get(obj.getSessionId());
				typeMap.set(indexId, null);
				typeIdMap.remove(obj.getSessionId());
			}
		//}
	}


	public <T> T readCache(String id){
		checkCacheExpires();
		if(typeIdMap.containsKey(id)){
			return (T)typeMap.get(typeIdMap.get(id));
		}
		return null;
	}

	public synchronized boolean addToCache(UserSessionType map){
		//System.out.println("Add to cache: " + (map == null ? "NULL" : map.getName()) + " AT " + key_name);
		//synchronized(typeMap){
			int length = typeMap.size();
			if(typeIdMap.containsKey(map.getSessionId())){
				return false;
			}
			typeMap.add(map);
			typeIdMap.put(map.getSessionId(), length);
		//}
		return true;
	}
}
