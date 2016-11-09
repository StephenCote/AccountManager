package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;
public class TestDataFactory extends BaseDataAccessTest {
	public static final Logger logger = LogManager.getLogger(TestDataFactory.class);
	private static String testShortDataName = null;
	private static String testLongDataName = null;
	private static String testEncLongDataName = null;

	@Test
	public void testSearchByAttribute(){
		boolean error = false;
		DirectoryGroupType dir = null;
		try{
			logger.info("Creating demo data with bunk attribute");
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserDirectory(testUser);
			String dataName = UUID.randomUUID().toString();
			DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(testUser, dir.getId());
			data.setName(dataName);
			data.setMimeType("text/plain");
			DataUtil.setValue(data, "This is the test data".getBytes());
			assertTrue(((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data));
			
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, dir);
			data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "demo", "value"));
			Factories.getAttributeFactory().addAttributes(data);

			logger.info("Finding demo data based on directory and attribute");
			ProcessingInstructionType instruction = new ProcessingInstructionType();
			instruction.setJoinAttribute(true);
			List<QueryField> fields = new ArrayList<>();
			fields.add(QueryFields.getFieldGroup(dir.getId()));
			fields.add(QueryFields.getStringField("ATR.name", "demo"));
			assertTrue(instruction.getJoinAttribute());
			List<NameIdType> dataList = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getByField(fields.toArray(new QueryField[0]), instruction,testUser.getOrganizationId());
			logger.info("Data: " + dataList.size());
		}
		catch(FactoryException fe){
			logger.error("Error",fe);
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataException e) {
			
			logger.error("Error",e);
		}
	}
	
	/// TODO: All of these tests need to be refactored - they are contingent on test order and the static names
	/*
	
	@Test
	public void testArtifacts(){
		OrganizationFactory of = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());

			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			if(dir == null){
				dir = gf.newDirectoryGroup("Test", rootDir, Factories.getDevelopmentOrganization().getId());
				gf.addGroup(dir);
				dir = gf.getDirectoryByName("Test", rootDir, Factories.getDevelopmentOrganization().getId());
			}
		}
		catch(FactoryException fe){
			logger.error("Error",fe);
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}

	}

	@Test
	public void testDataType(){

		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			DataType new_data = df.newData(testUser,  dir.getId());
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
			
			logger.error("Error",e);
		}
		
	
		
	}

	@Test
	public void testAddData(){
		DataType new_data = null;
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		String data_name = "Example - " + System.currentTimeMillis();
		testShortDataName = data_name;

		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			new_data = df.newData(testUser, dir.getId());
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
			
			logger.error("Error",e);
		}

		assertFalse("Factory error", error);
		//OrganizationType devOrg = 
	}
	

	@Test
	public void testAddByteData(){
		DataType new_data = null;
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		String data_name = "Example - " + System.currentTimeMillis();
		testLongDataName = data_name;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			new_data = df.newData(testUser, dir.getId());
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
			
			logger.error("Error",e);
		}

		assertFalse("Factory error", error);
		//OrganizationType devOrg = 
	}
	
	@Test
	public void testAddBigByteData(){
		DataType new_data = null;
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		String data_name = "Example - " + System.currentTimeMillis();
		testEncLongDataName = data_name;
		SecurityBean bean = KeyService.getPrimarySymmetricKey(Factories.getDevelopmentOrganization().getId());
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			new_data = df.newData(testUser, dir.getId());
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
			
			logger.error("Error",e);
		}

		assertFalse("Factory error", error);
		//OrganizationType devOrg = 
	}
	
	@Test
	public void testGetShortDataByName(){
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			logger.info("Looking for " + testShortDataName + " in " + dir.getId());
			data = df.getDataByName(testShortDataName, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} 
		assertFalse("An error occurred", error);
		assertNotNull("Data is null", data);
		
	}
	
	@Test
	public void testGetLongEncDataByName(){
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			logger.info("Looking for " + testEncLongDataName + " in " + dir.getId());
			data = df.getDataByName(testEncLongDataName, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} 
		assertFalse("An error occurred", error);
		assertNotNull("Data is null", data);
	}
	
	@Test
	public void testGetDataListByGroup(){
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		List<DataType> data = new ArrayList<DataType>();
		boolean updated = false;
		try {
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			data = df.getDataListByGroup(dir, true, 0, 0, Factories.getDevelopmentOrganization().getId());
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("An error occurred", error);
		assertTrue("No data returned", data.size() > 0);
		logger.info("Found " + data.size() + " data items");
	}
	@Test
	public void testUpdateLongDataByName(){
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		boolean updated = false;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			logger.info("Looking for " + testLongDataName + " in " + dir.getId());
			data = df.getDataByName(testLongDataName, dir);
			assertNotNull("Test data is null",data);
			DataUtil.setValue(data,  "New Example Value".getBytes());
			updated = df.updateData(data);
			data = df.getDataByName(testLongDataName, dir);
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (DataException e) {
			
			logger.error("Error",e);
			logger.error(e.getMessage());
			error = true;
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} 
		assertFalse("An error occurred", error);
		assertTrue("Data not updated", updated);
		assertTrue("Data value does not match", data.getSize() == 17);
		
		logger.info("Updated data value to have a size of: " + data.getSize());

	}
	@Test
	public void testUpdateShortDataByName(){
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		boolean updated = false;
		try{
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());

			
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
			
			logger.error("Error",e);
			logger.error(e.getMessage());
			error = true;
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} 
		assertFalse("An error occurred", error);
		assertTrue("Data not updated", updated);
		assertTrue("Data value does not match", data.getShortData().equals("New Example Value"));
		
		logger.info("Updated data value to: " + data.getShortData());

	}
	
	@Test
	public void testDeleteData(){
		GroupFactory gf = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
		DataFactory df = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
		boolean error = false;
		DirectoryGroupType dir = null;
		DirectoryGroupType rootDir = null;
		DataType data = null;
		boolean deleted = false;
		try {
			rootDir = gf.getDirectoryByName("Root", Factories.getDevelopmentOrganization().getId());
			dir = gf.getDirectoryByName("Test",rootDir, Factories.getDevelopmentOrganization().getId());
			data = df.getDataByName(testLongDataName, dir);
			assertTrue("Failed to delete long data", df.deleteData(data));
			data = df.getDataByName(testEncLongDataName, dir);
			assertTrue("Failed to delete long enc data", df.deleteData(data));
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			logger.error(e.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("An error occurred", error);
		
	}
	*/
}
