package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDataAccess{
	public static final Logger logger = Logger.getLogger(TestDataAccess.class.getName());
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testDBConnection(){
		logger.info("Testing PG DB Connection");
		Connection connection = ConnectionFactory.getConnection("jdbc:postgresql://127.0.0.1:5432/devdb", "devuser","password","org.postgresql.Driver");
		assertNotNull("Connection is null", connection);
		
		boolean error = false;
		try{
			assertFalse("Connection is not open", connection.isClosed());
			connection.close();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			error = true;
		}
		assertFalse("Error executing connection command", error);
	}
	
	@Test
	public void testDBInsert(){
		logger.info("Testing PG DB Connection");
		Connection connection = ConnectionFactory.getConnection("jdbc:postgresql://127.0.0.1:5432/devdb", "devuser","password","org.postgresql.Driver");
		assertNotNull("Connection is null", connection);
		
		boolean error = false;

		
		String sql_insert = "INSERT into devtable (Name) VALUES (?);";
		int updated = 0;
		
		if(DBFactory.getTableExists(connection, "devtable") == false){
			String table = getSamplePGTable("devtable");
			DBFactory.executeStatement(connection, table);
		}
		
		try{
			PreparedStatement ps = connection.prepareStatement(sql_insert);
			ps.setString(1, "Dev Value " + System.currentTimeMillis());
			updated = ps.executeUpdate();
			ps.close();
		}
		catch(SQLException sqe){
			sqe.printStackTrace();
			error = true;
			logger.error(sqe.getMessage());
		}
		assertFalse("Error executing statement command", error);
		try{
			assertFalse("Connection is not open", connection.isClosed());
			connection.close();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			error = true;
		}
		assertFalse("Error executing connection command", error);
	}
	
	@Test
	public void testAddParameter(){
		logger.info("Testing PG DB Connection");
		Connection connection = ConnectionFactory.getConnection("jdbc:postgresql://127.0.0.1:5432/devdb", "devuser","password","org.postgresql.Driver");
		assertNotNull("Connection is null", connection);
		
		boolean error = false;


		try {
			DataTable table = DBFactory.getDataTable(connection, "devtable");
			assertTrue("Column length is 0", table.getColumnSize() > 0);
			DataRow newRow = table.newRow();
			table.getRows().add(newRow);
			newRow.setCellValue(0, 52);
			newRow.setCellValue(1, "Example");
			printTable(table);		
		} catch (DataAccessException e) {
			logger.info(e.getMessage());
			error = true;
		}
	
		/*
		for(int i = 0; i < table.getColumnSize(); i++){
			logger.info(table.getColumns().get(i));
		}
		
		String sql_insert = "INSERT into devtable (Name) VALUES (?);";
		int updated = 0;
		
		if(DBFactory.getTableExists(connection, "devtable") == false){
			String tableStr = getSamplePGTable("devtable");
			DBFactory.executeStatement(connection, tableStr);
		}
		
		try{
			PreparedStatement ps = connection.prepareStatement(sql_insert);
			
			ps.setString(1, "Dev Value " + System.currentTimeMillis());
			updated = ps.executeUpdate();
			ps.close();
		}
		catch(SQLException sqe){
			sqe.printStackTrace();
			error = true;
			logger.error(sqe.getMessage());
		}
		assertFalse("Error executing statement command", error);
		*/
		try{
			assertFalse("Connection is not open", connection.isClosed());
			connection.close();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			error = true;
		}
		assertFalse("Error executing connection command", error);
		
	}
	private void printTable(DataTable table){
		logger.info("TABLE " + table.getName());
		for(int r = 0; r < table.getRows().size();r++){
			DataRow row = (DataRow)table.getRows().get(r);
			for(int i = 0; i < table.getColumnSize();i++){
				logger.info("\tCOLUMN NAME: " + table.getColumns().get(i).getColumnName());
				try{
					String strValue = null;
					if(table.getColumns().get(i).getDataType() == SqlDataEnumType.VARCHAR){
						strValue = row.getCellValue(i);
					}
					else if(table.getColumns().get(i).getDataType() == SqlDataEnumType.INTEGER){
						Integer intVal = row.getCellValue(i);
						strValue = intVal.toString();
					}

					logger.info("\tCOLUMN VALUE: " + strValue);
				}
				catch(DataAccessException dae){
					logger.error(dae.toString());
					
				}
			}
		}
	}
	public String getSamplePGTable(String name){
		StringBuffer buff = new StringBuffer();

		buff.append("CREATE SEQUENCE " + name + "_id_seq;");
		buff.append("CREATE TABLE " + name + " (Id int NOT NULL DEFAULT nextval('" + name + "_id_seq'), Name varchar(128));");
		return buff.toString();
	}
}
