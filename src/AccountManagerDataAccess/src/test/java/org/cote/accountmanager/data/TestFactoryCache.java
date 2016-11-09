package org.cote.accountmanager.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.util.BeanUtil;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;
public class TestFactoryCache extends BaseDataAccessTest{
	
	public static final Logger logger = LogManager.getLogger(TestGroupFactory.class);
	private static String testDirGroupName = null;
	
	private static String testUserName1 = "TestSessionUser";

	private UserType sessionUser = null;
	private static String sessionId = null;
	
	
	/*
	@Test
	public void TestDirectDirectoryGroupCache(){
		DirectoryGroupType dgt = null;
		boolean success = false;
		
		try {

			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).clearCache();
			//dgt = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("CacheTest", sessionUser.getHomeDirectory(), sessionUser.getOrganization());
			dgt = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(sessionUser, "~/CacheTest", sessionUser.getOrganization());
			assertNotNull("Group is null",dgt);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dgt);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populateSubDirectories(dgt);
			dgt = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(dgt.getId(), dgt.getOrganization());
			assertNotNull("Group is null",dgt);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dgt);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populateSubDirectories(dgt);
			//dgt = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("CacheTest", sessionUser.getHomeDirectory(), sessionUser.getOrganization());
			dgt = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(sessionUser, "~/CacheTest", sessionUser.getOrganization());
			assertNotNull("Group is null",dgt);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dgt);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populateSubDirectories(dgt);
			
			assertNotNull("Group org is null",dgt.getOrganization());
			success = true;
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Bit not set", success);
	}
	*/
	
	@Test
	public void TestCacheNames(){
		sessionUser = testUser;
		DataType data = getTestData("Test data",sessionUser.getHomeDirectory());

		logger.info("Data Cache Key Name #1 = " + ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getCacheKeyName(data));
		logger.info("Group Cache Key Name #1 = " + ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCacheKeyName(sessionUser.getHomeDirectory()));
		List<DataType> datas = new ArrayList<DataType>();
		try {
			DataUtil.setValueString(data, UUID.randomUUID().toString());
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(data);
			datas = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataListByGroup(sessionUser.getHomeDirectory(), true, 0, 10, sessionUser.getOrganizationId());
		} catch (DataException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		data = getTestData("Test data",sessionUser.getHomeDirectory());
		
		logger.info("Data Cache Key Name #2 = " + ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getCacheKeyName(datas.get(0)));
	}
	
	private DataType getTestData(String name, DirectoryGroupType dir){
		DataType data = null;
		sessionUser = testUser;
		try{
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name,dir);
			if(data == null){
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(sessionUser, dir.getId());
				data.setName(name);
				data.setMimeType("text/plain");
				DataUtil.setValueString(data, "Example data");
				((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data);
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name,dir);
			}
		}
		catch(FactoryException fe){
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataException e) {
			
			logger.error("Error",e);
		}
		return data;
	}
	/*
	@Test
	public void TestDirectoryGroupCache(){
		DirectoryGroupType dgt = null;
		boolean success = false;
		
		try {
			
			dgt = getPath("~/CacheTest");
			assertNotNull("Group is null",dgt);
			dgt = getGroupById(dgt.getId());
			assertNotNull("Group is null",dgt);
			dgt = getPath("~/CacheTest");
			assertNotNull("Group is null",dgt);
			success = true;
		}
		 catch (FactoryException e) {
				
				logger.error("Error",e);
			} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Bit not set", success);
	}
	*/
	private DirectoryGroupType getGroupById(long parentId) throws FactoryException, ArgumentException{
		DirectoryGroupType bean = null;
		sessionUser = testUser;
		String sessionId = "unit test";
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "#" + parentId,AuditEnumType.SESSION,sessionId);
		UserType user = sessionUser;
		if(user == null) return bean;
		if(parentId <= 0){
			AuditService.denyResult(audit, "Invalid parent id: " + parentId);
			return bean;
		}

			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(parentId, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Id " + parentId + " doesn't exist in org " + user.getOrganizationId());
				return bean;
			}
			/*
			DirectoryGroupType cdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(name, dir, user.getOrganization());
			if(cdir == null){
				AuditService.denyResult(audit, "Group  " + name + " doesn't exist in parent " + dir.getName() + " (#" + dir.getId() + ") in org " +  user.getOrganization().getId());
				return bean;
			}
				
			if(!AuthorizationService.canView(user, cdir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to view group " + cdir.getName() + " (#" + cdir.getId() + ")");
				return bean;
			}
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(cdir);		
			*/	
			if(!AuthorizationService.canView(user, dir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);
			//for(int i = 0; i < dir.getSubDirectories().size();i++) ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir.getSubDirectories().get(i));
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			//bean = BeanUtil.getSanitizedGroup(dir,false);
			

		return bean;
	}
	private DirectoryGroupType getPath(String path) throws FactoryException, ArgumentException{
		DirectoryGroupType bean = null;
		sessionUser = testUser;
		if(path == null || path.length() == 0) path = "~";
		if(path.startsWith("~") == false && path.startsWith("/") == false) path = "/" + path;
		System.out.println("Path = '" + path + "'");
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.SESSION,"unit test");
		UserType user = sessionUser;
		if(user == null) return bean;

			DirectoryGroupType dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, path, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path");
				return bean;
			}
			if(AuthorizationService.canView(user, dir) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);	
			/// Work with a clone of the group because if it's cached, don't null out the cached copy's version
			dir = BeanUtil.getBean(DirectoryGroupType.class,dir);
			//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populateSubDirectories(dir);
			/*
			for(int i = 0; i < dir.getSubDirectories().size();i++){
				((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir.getSubDirectories().get(i));
			}
			*/
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			
			//bean = BeanUtil.getSanitizedGroup(dir,false);
	
		return bean;
	}

}