package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.junit.Test;
public class TestGroupFactory extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestGroupFactory.class);
	private static String testDirGroupName = null;

	
	@Test
	public void testFindUserDir(){
		DirectoryGroupType dir = null;
		try {
			dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(testUser, GroupEnumType.DATA, "~", testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("~ path is null",dir);
	}
	@Test
	public void testAddDirectoryGroup(){
		testDirGroupName = "Example - " + System.currentTimeMillis();
		OrganizationFactory orgFactory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
		OrganizationType dev = Factories.getDevelopmentOrganization();
		assertNotNull("Development organization is null", dev);
		boolean error = false;
		boolean addDir = false;
		try{
			DirectoryGroupType parentDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", dev.getId());
			assertNotNull("Root dir is null", parentDir);
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(testDirGroupName, parentDir, dev.getId());
			//DirectoryGroupType newDir = null;
			addDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).add(dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);
		assertTrue("Directory not added", addDir);
	}
	
	@Test
	public void testUpdateGroup(){
		String newTestDirGroupName = "Changed Example - " + System.currentTimeMillis();
		OrganizationFactory orgFactory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
		OrganizationType dev = Factories.getDevelopmentOrganization();
		assertNotNull("Development organization is null", dev);
		boolean error = false;
		boolean editDir = false;
		try{
			DirectoryGroupType parentDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", dev.getId());
			assertNotNull("Root dir is null", parentDir);
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(testDirGroupName, parentDir, parentDir.getOrganizationId());
			assertNotNull("Directory is null", dir);
			dir.setName(newTestDirGroupName);
			editDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).update(dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);
		assertTrue("Directory not changed", editDir);
		testDirGroupName = newTestDirGroupName;
	}
	
	@Test
	public void testGetGroupPath(){
		OrganizationFactory orgFactory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
		OrganizationType dev = Factories.getDevelopmentOrganization();
		assertNotNull("Development organization is null", dev);
		boolean error = false;
		boolean editDir = false;
		String path = null;
		try{
			DirectoryGroupType parentDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", dev.getId());
			assertNotNull("Root dir is null", parentDir);
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(testDirGroupName, parentDir, parentDir.getOrganizationId());
			assertNotNull("Directory is null", dir);
			path = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getPath(dir);

		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);
		assertNotNull("Path is null", path);
		logger.info("Path=" + path);
	}
	
	@Test
	public void testMakePath(){
		OrganizationFactory orgFactory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
		OrganizationType dev = Factories.getDevelopmentOrganization();
		boolean error = false;
		boolean path = false;
		boolean cleanUp = false;
		try {
			DirectoryGroupType dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null,  GroupEnumType.DATA,"/Example1", dev.getId());
			if(dir != null){
				logger.info("Cleanup example path");
				cleanUp = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup(dir);
				assertTrue("Unable to cleanup directory path", cleanUp);
				dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA, "/Example1", dev.getId());
				assertNull("Directory was not fully removed", dir);
			}
			else{
				logger.info("Did not find example path");
			}
			path = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).makePath(null, GroupEnumType.DATA,"/Example1/Example2/Example3/Example4", dev.getId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			logger.error(e.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);
		assertTrue("Directory path not made", path);
	}
}