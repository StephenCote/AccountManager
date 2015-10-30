package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;

public class TestBulkSession extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestBulkSession.class.getName());
	
	/*
	@Test
	public void TestBulkData(){
		try{
			Factories.getUserFactory().populate(testUser);
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(testUser, "BulkData", testUser.getHomeDirectory(), testUser.getOrganization());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			long start = System.currentTimeMillis();
			for(int i = 0; i < 10;i++){
				DataType data = Factories.getDataFactory().newData(testUser, dir);
				data.setName(UUID.randomUUID().toString());
				data.setMimeType("text/plain");
				DataUtil.setValueString(data, UUID.randomUUID().toString());
				Factories.getDataFactory().addData(data);
				data = Factories.getDataFactory().getDataByName(data.getName(),dir);
				AuthorizationService.switchData(testUser, testUser2,data,AuthorizationService.getViewDataPermission(testUser.getOrganization()), true);
			}
			long stop = System.currentTimeMillis();
			logger.info("Time to loop individual: " + (stop - start) + "ms");
			start = System.currentTimeMillis();
			Map<String,Long> bulkNameId = new HashMap<String,Long>();
			for(int i = 0; i < 10; i++){
				DataType data = Factories.getDataFactory().newData(testUser, dir);
				data.setName(UUID.randomUUID().toString());
				data.setMimeType("text/plain");
				DataUtil.setValueString(data, UUID.randomUUID().toString());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.DATA, data);
				assertTrue("Data id (" + data.getId() + " not set to bulk entry",data.getId() < 0);
				DataType checkData = Factories.getDataFactory().getDataByName(data.getName(),false,data.getGroup());
				assertNotNull("Failed cache check for bulk data object by name",checkData);
				checkData = Factories.getDataFactory().getDataById(data.getId(), data.getOrganization());
				assertNotNull("Failed cache check for bulk data object by id",checkData);
				logger.info("Data Bulk entry id=" + data.getId());
				
				AuthorizationService.switchData(testUser,testUser2,data,AuthorizationService.getViewDataPermission(testUser.getOrganization()),true);
				//bulkNameId.put(data.getName(), data.getId());
			}
			stop = System.currentTimeMillis();
			logger.info("Time to loop bulk: " + (stop - start) + "ms");
			BulkFactories.getBulkFactory().write(sessionId);
			logger.info("Time to write: " + (System.currentTimeMillis() - start) + "ms");
			BulkFactories.getBulkFactory().close(sessionId);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	@Test
	public void TestBulkRole(){
		try{
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			Factories.getUserFactory().populate(testUser);
			UserRoleType baseRole = Factories.getRoleFactory().newUserRole(testUser, "BulkRoleParent");
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLE, baseRole);
			UserRoleType childRole = Factories.getRoleFactory().newUserRole(testUser, "ChildRole",baseRole);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLE, childRole);
			
			logger.info("Retrieving Bulk Role Parent");
			UserRoleType check = Factories.getRoleFactory().getUserRoleByName("BulkRoleParent", testUser.getOrganizationId());
			assertNotNull("Failed role cache check",check);
			
			logger.info("Retrieving Child Role By Parent");
			check = Factories.getRoleFactory().getUserRoleByName("ChildRole", check, testUser.getOrganizationId());
			assertNotNull("Failed role+parent cache check",check);
			
			BulkFactories.getBulkFactory().close(sessionId);
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		}  catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	public void TestBulkGroup(){
		try{
			Factories.getUserFactory().populate(testUser);
			UserRoleType baseRole = Factories.getRoleFactory().getCreateUserRole(testUser, "BulkRoleParent", null);
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(testUser, "BulkGroup", testUser.getHomeDirectory(), testUser.getOrganization());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			List<String> debugRoles = new ArrayList<String>();
			for(int i = 0; i < 10;i++){
				String name = UUID.randomUUID().toString();
				UserRoleType role = Factories.getRoleFactory().newUserRole(testUser, name,baseRole);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLE, role);
				assertTrue("Role id (" + role.getId() + ") not set to bulk entry",role.getId() < 0);
				BaseRoleType checkRole = Factories.getRoleFactory().getUserRoleByName(role.getName(),baseRole,baseRole.getOrganization());
				assertNotNull("Failed cache check for bulk role object by name",checkRole);
				checkRole = Factories.getRoleFactory().getRoleById(role.getId(),baseRole.getOrganization());
				assertNotNull("Failed cache check for bulk role object by name",checkRole);
				debugRoles.add(name);
				
				RoleService.addUserToRole(testUser, role);
				RoleService.addUserToRole(testUser2, role);
			}
			
			long start = System.currentTimeMillis();
			Map<String,Long> bulkNameId = new HashMap<String,Long>();
			for(int i = 0; i < 10; i++){
				DirectoryGroupType group = Factories.getGroupFactory().newDirectoryGroup(testUser,UUID.randomUUID().toString(), dir, testUser.getOrganization());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, group);
				assertTrue("Group id (" + group.getId() + " not set to bulk entry",group.getId() < 0);
				DirectoryGroupType checkGroup = Factories.getGroupFactory().getDirectoryByName(group.getName(),dir,group.getOrganization());
				assertNotNull("Failed cache check for bulk group object by name",checkGroup);
				checkGroup = Factories.getGroupFactory().getDirectoryById(group.getId(), group.getOrganization());
				assertNotNull("Failed cache check for bulk group object by id",checkGroup);
				logger.info("Group Bulk entry id=" + group.getId());

				AuthorizationService.switchGroup(testUser,testUser2,group,AuthorizationService.getViewDataPermission(testUser.getOrganization()),true);
				for(int r = 0; r < debugRoles.size();r++){
					AuthorizationService.switchGroup(testUser,(UserRoleType)Factories.getRoleFactory().getUserRoleByName(debugRoles.get(r),baseRole,baseRole.getOrganization()),group,AuthorizationService.getViewDataPermission(testUser.getOrganization()),true);
				}
				//bulkNameId.put(data.getName(), data.getId());
			}
			long stop = System.currentTimeMillis();
			logger.info("Time to loop bulk: " + (stop - start) + "ms");
			BulkFactories.getBulkFactory().write(sessionId);
			logger.info("Time to write: " + (System.currentTimeMillis() - start) + "ms");
			BulkFactories.getBulkFactory().close(sessionId);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}