package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.SecurityUtil;


import org.cote.accountmanager.util.BeanUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFactoryCache extends BaseDataAccessTest{
	
	public static final Logger logger = Logger.getLogger(TestGroupFactory.class.getName());
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

			Factories.getGroupFactory().clearCache();
			//dgt = Factories.getGroupFactory().getDirectoryByName("CacheTest", sessionUser.getHomeDirectory(), sessionUser.getOrganization());
			dgt = Factories.getGroupFactory().findGroup(sessionUser, "~/CacheTest", sessionUser.getOrganization());
			assertNotNull("Group is null",dgt);
			Factories.getGroupFactory().populate(dgt);
			Factories.getGroupFactory().populateSubDirectories(dgt);
			dgt = Factories.getGroupFactory().getDirectoryById(dgt.getId(), dgt.getOrganization());
			assertNotNull("Group is null",dgt);
			Factories.getGroupFactory().populate(dgt);
			Factories.getGroupFactory().populateSubDirectories(dgt);
			//dgt = Factories.getGroupFactory().getDirectoryByName("CacheTest", sessionUser.getHomeDirectory(), sessionUser.getOrganization());
			dgt = Factories.getGroupFactory().findGroup(sessionUser, "~/CacheTest", sessionUser.getOrganization());
			assertNotNull("Group is null",dgt);
			Factories.getGroupFactory().populate(dgt);
			Factories.getGroupFactory().populateSubDirectories(dgt);
			
			assertNotNull("Group org is null",dgt.getOrganization());
			success = true;
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Bit not set", success);
	}
	*/
	
	@Test
	public void TestCacheNames(){
		sessionUser = testUser;
		DataType data = getTestData("Test data",sessionUser.getHomeDirectory());

		logger.info("Data Cache Key Name #1 = " + Factories.getDataFactory().getCacheKeyName(data));
		logger.info("Group Cache Key Name #1 = " + Factories.getGroupFactory().getCacheKeyName(sessionUser.getHomeDirectory()));
		List<DataType> datas = new ArrayList<DataType>();
		try {
			DataUtil.setValueString(data, UUID.randomUUID().toString());
			Factories.getDataFactory().updateData(data);
			datas = Factories.getDataFactory().getDataListByGroup(sessionUser.getHomeDirectory(), true, 0, 10, sessionUser.getOrganizationId());
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data = getTestData("Test data",sessionUser.getHomeDirectory());
		
		logger.info("Data Cache Key Name #2 = " + Factories.getDataFactory().getCacheKeyName(datas.get(0)));
	}
	
	private DataType getTestData(String name, DirectoryGroupType dir){
		DataType data = null;
		sessionUser = testUser;
		try{
			data = Factories.getDataFactory().getDataByName(name,dir);
			if(data == null){
				data = Factories.getDataFactory().newData(sessionUser, dir.getId());
				data.setName(name);
				data.setMimeType("text/plain");
				DataUtil.setValueString(data, "Example data");
				Factories.getDataFactory().addData(data);
				data = Factories.getDataFactory().getDataByName(name,dir);
			}
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

			DirectoryGroupType dir = Factories.getGroupFactory().getById(parentId, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Id " + parentId + " doesn't exist in org " + user.getOrganizationId());
				return bean;
			}
			/*
			DirectoryGroupType cdir = Factories.getGroupFactory().getDirectoryByName(name, dir, user.getOrganization());
			if(cdir == null){
				AuditService.denyResult(audit, "Group  " + name + " doesn't exist in parent " + dir.getName() + " (#" + dir.getId() + ") in org " +  user.getOrganization().getId());
				return bean;
			}
				
			if(!AuthorizationService.canViewGroup(user, cdir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to view group " + cdir.getName() + " (#" + cdir.getId() + ")");
				return bean;
			}
			Factories.getGroupFactory().populate(cdir);		
			*/	
			if(!AuthorizationService.canViewGroup(user, dir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			Factories.getGroupFactory().populate(dir);
			//for(int i = 0; i < dir.getSubDirectories().size();i++) Factories.getGroupFactory().populate(dir.getSubDirectories().get(i));
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

			DirectoryGroupType dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.DATA, path, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path");
				return bean;
			}
			if(AuthorizationService.canViewGroup(user, dir) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			Factories.getGroupFactory().populate(dir);	
			/// Work with a clone of the group because if it's cached, don't null out the cached copy's version
			dir = BeanUtil.getBean(DirectoryGroupType.class,dir);
			//Factories.getGroupFactory().populateSubDirectories(dir);
			/*
			for(int i = 0; i < dir.getSubDirectories().size();i++){
				Factories.getGroupFactory().populate(dir.getSubDirectories().get(i));
			}
			*/
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			
			//bean = BeanUtil.getSanitizedGroup(dir,false);
	
		return bean;
	}

}