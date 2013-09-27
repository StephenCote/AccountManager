package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.DataTableType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.SecurityUtil;

public class TestDataFactory{
	public static final Logger logger = Logger.getLogger(TestDataFactory.class.getName());
	private static String testShortDataName = null;
	private static String testLongDataName = null;
	private static String testEncLongDataName = null;
	private static String testUserName1 = "DebugDataUser";
	private UserType dataUser = null;
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		try{
			dataUser = Factories.getUserFactory().getUserByName(testUserName1,Factories.getDevelopmentOrganization());
			if(dataUser == null){
				UserType new_user = Factories.getUserFactory().newUser(testUserName1, SecurityUtil.getSaltedDigest("password1"), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
				if(Factories.getUserFactory().addUser(new_user,  false)){
					dataUser = Factories.getUserFactory().getUserByName(testUserName1,Factories.getDevelopmentOrganization());
				}
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
	}

	@After
	public void tearDown() throws Exception {
	}
	@Test
	public void testArtifacts(){
		OrganizationFactory of = Factories.getOrganizationFactory();
		GroupFactory gf = Factories.getGroupFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());

			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			if(dir == null){
				dir = gf.newDirectoryGroup("Test", rootDir, Factories.getDevelopmentOrganization());
				gf.addGroup(dir);
				dir = gf.getDirectoryByName("Test", rootDir, Factories.getDevelopmentOrganization());
			}
		}
		catch(FactoryException fe){
			fe.printStackTrace();
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testDataType(){

		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			DataType new_data = df.newData(dataUser,  dir);
			logger.info("BEGIN DATATYPE");
			logger.info(new_data.getName());
			logger.info(new_data.getCreatedDate().toString());
			logger.info(new_data.getModifiedDate().toString());
			logger.info(new_data.getExpiryDate().toString());
			logger.info("END DATATYPE");
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		
	}

	@Test
	public void testAddData(){
		DataType new_data = null;
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		String data_name = "Example - " + System.currentTimeMillis();
		testShortDataName = data_name;

		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			/*
			new_data = df.getDataByName(testShortDataName, dir);
			if(new_data != null) df.deleteData(new_data);
			*/
			new_data = df.newData(dataUser, dir);
			new_data.setName(data_name);
			new_data.setMimeType("text/plain");
			DataUtil.setValueString(new_data, "This is the example text.");
			boolean addData = df.addData(new_data);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (DataException e) {
			logger.error(e.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertFalse("Factory error", error);
		//OrganizationType devOrg = 
	}
	

	@Test
	public void testAddByteData(){
		DataType new_data = null;
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		String data_name = "Example - " + System.currentTimeMillis();
		testLongDataName = data_name;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			new_data = df.newData(dataUser, dir);
			new_data.setName(data_name);
			new_data.setMimeType("text/plain");
			DataUtil.setValue(new_data, "This is the example text.".getBytes());
			boolean addData = df.addData(new_data);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (DataException e) {
			logger.error(e.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertFalse("Factory error", error);
		//OrganizationType devOrg = 
	}
	
	@Test
	public void testAddBigByteData(){
		DataType new_data = null;
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		String data_name = "Example - " + System.currentTimeMillis();
		testEncLongDataName = data_name;
		SecurityBean bean = OrganizationSecurity.getSecurityBean(Factories.getDevelopmentOrganization());
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			new_data = df.newData(dataUser, dir);
			DataUtil.setCipher(new_data, bean);
			new_data.setEncipher(true);
			new_data.setName(data_name);
			new_data.setMimeType("text/plain");
			
			String bigData = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
			DataUtil.setValue(new_data, bigData.getBytes());
			boolean addData = df.addData(new_data);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (DataException e) {
			logger.error(e.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertFalse("Factory error", error);
		//OrganizationType devOrg = 
	}
	
	@Test
	public void testGetShortDataByName(){
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			logger.info("Looking for " + testShortDataName + " in " + dir.getId());
			data = df.getDataByName(testShortDataName, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		/*
		catch (DataException e) {
			logger.error(e.getMessage());
			error = true;
			
		}*/
		assertFalse("An error occurred", error);
		assertNotNull("Data is null", data);
		
	}
	
	@Test
	public void testGetLongEncDataByName(){
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			logger.info("Looking for " + testEncLongDataName + " in " + dir.getId());
			data = df.getDataByName(testEncLongDataName, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		/*
		catch (DataException e) {
			logger.error(e.getMessage());
			error = true;
			
		}*/
		assertFalse("An error occurred", error);
		assertNotNull("Data is null", data);
	}
	
	@Test
	public void testGetDataListByGroup(){
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		List<DataType> data = new ArrayList<DataType>();
		boolean updated = false;
		try {
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			data = df.getDataListByGroup(dir, true, 0, 0, Factories.getDevelopmentOrganization());
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("An error occurred", error);
		assertTrue("No data returned", data.size() > 0);
		logger.info("Found " + data.size() + " data items");
	}
	@Test
	public void testUpdateLongDataByName(){
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		boolean updated = false;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
			logger.info("Looking for " + testLongDataName + " in " + dir.getId());
			data = df.getDataByName(testLongDataName, dir);
			DataUtil.setValue(data,  "New Example Value".getBytes());
			updated = df.updateData(data);
			data = df.getDataByName(testLongDataName, dir);
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			error = true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		/*
		catch (DataException e) {
			logger.error(e.getMessage());
			error = true;
			
		}*/
		assertFalse("An error occurred", error);
		assertTrue("Data not updated", updated);
		assertTrue("Data value does not match", data.getSize() == 17);
		
		logger.info("Updated data value to have a size of: " + data.getSize());

	}
	@Test
	public void testUpdateShortDataByName(){
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		boolean updated = false;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());

			
			logger.info("Looking for " + testShortDataName + " in " + dir.getId());
			data = df.getDataByName(testShortDataName, dir);
			data.setMimeType("test/example");
			data.setDimensions("foo.bar");
			data.setDescription(null);
			DataUtil.setValueString(data,  "New Example Value");
			updated = df.updateData(data);
			data = df.getDataByName(testShortDataName, dir);
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			error = true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		/*
		catch (DataException e) {
			logger.error(e.getMessage());
			error = true;
			
		}*/
		assertFalse("An error occurred", error);
		assertTrue("Data not updated", updated);
		assertTrue("Data value does not match", data.getShortData().equals("New Example Value"));
		
		logger.info("Updated data value to: " + data.getShortData());

	}
	
	@Test
	public void testDeleteData(){
		GroupFactory gf = Factories.getGroupFactory();
		DataFactory df = Factories.getDataFactory();
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		boolean deleted = false;
		try {
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization());
/*
			data = df.getDataByName(testShortDataName, dir);
			assertTrue("Failed to delete short data", df.deleteData(data));
*/
			data = df.getDataByName(testLongDataName, dir);
			assertTrue("Failed to delete long data", df.deleteData(data));
			data = df.getDataByName(testEncLongDataName, dir);
			assertTrue("Failed to delete long enc data", df.deleteData(data));
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("An error occurred", error);
		
	}
}
