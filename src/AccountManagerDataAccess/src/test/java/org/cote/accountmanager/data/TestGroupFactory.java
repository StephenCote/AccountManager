package org.cote.accountmanager.data;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestGroupFactory extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestGroupFactory.class.getName());
	private static String testDirGroupName = null;

	
	@Test
	public void testFindUserDir(){
		DirectoryGroupType dir = null;
		try {
			dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(testUser, GroupEnumType.DATA, "~", testUser.getOrganization());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull("~ path is null",dir);
	}
	@Test
	public void testAddDirectoryGroup(){
		testDirGroupName = "Example - " + System.currentTimeMillis();
		OrganizationFactory orgFactory = Factories.getOrganizationFactory();
		OrganizationType dev = Factories.getDevelopmentOrganization();
		assertNotNull("Development organization is null", dev);
		boolean error = false;
		boolean addDir = false;
		try{
			DirectoryGroupType parentDir = Factories.getGroupFactory().getDirectoryByName("Root", dev);
			assertNotNull("Root dir is null", parentDir);
			DirectoryGroupType dir = Factories.getGroupFactory().newDirectoryGroup(testDirGroupName, parentDir, dev);
			//DirectoryGroupType newDir = null;
			addDir = Factories.getGroupFactory().addGroup(dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("Error occurred", error);
		assertTrue("Directory not added", addDir);
	}
	
	@Test
	public void testUpdateGroup(){
		String newTestDirGroupName = "Changed Example - " + System.currentTimeMillis();
		OrganizationFactory orgFactory = Factories.getOrganizationFactory();
		OrganizationType dev = Factories.getDevelopmentOrganization();
		assertNotNull("Development organization is null", dev);
		boolean error = false;
		boolean editDir = false;
		try{
			DirectoryGroupType parentDir = Factories.getGroupFactory().getDirectoryByName("Root", dev);
			assertNotNull("Root dir is null", parentDir);
			DirectoryGroupType dir = Factories.getGroupFactory().getDirectoryByName(testDirGroupName, parentDir, parentDir.getOrganization());
			assertNotNull("Directory is null", dir);
			dir.setName(newTestDirGroupName);
			editDir = Factories.getGroupFactory().updateGroup(dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("Error occurred", error);
		assertTrue("Directory not changed", editDir);
		testDirGroupName = newTestDirGroupName;
	}
	
	@Test
	public void testGetGroupPath(){
		OrganizationFactory orgFactory = Factories.getOrganizationFactory();
		OrganizationType dev = Factories.getDevelopmentOrganization();
		assertNotNull("Development organization is null", dev);
		boolean error = false;
		boolean editDir = false;
		String path = null;
		try{
			DirectoryGroupType parentDir = Factories.getGroupFactory().getDirectoryByName("Root", dev);
			assertNotNull("Root dir is null", parentDir);
			DirectoryGroupType dir = Factories.getGroupFactory().getDirectoryByName(testDirGroupName, parentDir, parentDir.getOrganization());
			assertNotNull("Directory is null", dir);
			path = Factories.getGroupFactory().getPath(dir);

		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("Error occurred", error);
		assertNotNull("Path is null", path);
		logger.info("Path=" + path);
	}
	
	@Test
	public void testMakePath(){
		OrganizationFactory orgFactory = Factories.getOrganizationFactory();
		OrganizationType dev = Factories.getDevelopmentOrganization();
		boolean error = false;
		boolean path = false;
		boolean cleanUp = false;
		try {
			DirectoryGroupType dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(null,  GroupEnumType.DATA,"/Example1", dev);
			if(dir != null){
				logger.info("Cleanup example path");
				cleanUp = Factories.getGroupFactory().deleteDirectoryGroup(dir);
				assertTrue("Unable to cleanup directory path", cleanUp);
				dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(null, GroupEnumType.DATA, "/Example1", dev);
				assertNull("Directory was not fully removed", dir);
			}
			else{
				logger.info("Did not find example path");
			}
			path = Factories.getGroupFactory().makePath(null, "/Example1/Example2/Example3/Example4", dev);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("Error occurred", error);
		assertTrue("Directory path not made", path);
	}
}