package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.services.ServiceUtil;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(BaseDataAccessTest.class.getName());
	private static String testUserName = "RocketQAUser";
	protected static UserType testUser = null;
	private static String sessionId = null;

	private static String testUserName2 = "RocketQAUser2";
	protected static UserType testUser2 = null;
	private static String sessionId2 = null;

	
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
		sessionId = UUID.randomUUID().toString();
		sessionId2 = UUID.randomUUID().toString();
		
		try{
			testUser = SessionSecurity.login(sessionId, testUserName, SecurityUtil.getSaltedDigest("password1"), Factories.getDevelopmentOrganization());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		if(testUser == null){
			UserType new_user = Factories.getUserFactory().newUser(testUserName, SecurityUtil.getSaltedDigest("password1"), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
			if(Factories.getUserFactory().addUser(new_user,  true)){
				testUser = SessionSecurity.login(sessionId, testUserName, SecurityUtil.getSaltedDigest("password1"), Factories.getDevelopmentOrganization());
			}
		}
		
		try{
			testUser2 = SessionSecurity.login(sessionId2, testUserName2, SecurityUtil.getSaltedDigest("password1"), Factories.getDevelopmentOrganization());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		if(testUser2 == null){
			UserType new_user = Factories.getUserFactory().newUser(testUserName2, SecurityUtil.getSaltedDigest("password1"), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
			if(Factories.getUserFactory().addUser(new_user,  true)){
				testUser2 = SessionSecurity.login(sessionId2, testUserName2, SecurityUtil.getSaltedDigest("password1"), Factories.getDevelopmentOrganization());
			}
		}
		//logger.info("Setup session: " + sessionId);
	}
	
	@After
	public void tearDown() throws Exception{
		//logger.info("Cleanup session: " + sessionId);
		SessionSecurity.logout(sessionId,  Factories.getDevelopmentOrganization());
	}
	
	

	public DataType newTextData(String name, String value, UserType owner, DirectoryGroupType dir){
		DataType data = null;
		try{
			data = Factories.getDataFactory().newData(owner, dir);
			data.setName(name);
			data.setMimeType("text/plain");
			DataUtil.setValueString(data, value);
			Factories.getDataFactory().addData(data);
			data = Factories.getDataFactory().getDataByName(name, dir);
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
		}
		return data;
	}


	protected DataType getData(UserType user, String data_name){
		
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());

		
		DataType data = null;
		try{
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(user,"examples", user.getHomeDirectory(), user.getOrganization());
			data = Factories.getDataFactory().getDataByName(data_name,  dir);
			if(data == null){
				data = Factories.getDataFactory().newData(user,  dir);
				data.setName(data_name);
				data.setMimeType("text/plain");
				DataUtil.setValueString(data, "Example Data");
				Factories.getDataFactory().addData(data);
				data = Factories.getDataFactory().getDataByName(data_name,  dir);
			}
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	public DataTagType getTag(String tag_name){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		DataTagType tag = null;
		try {
			tag = Factories.getTagFactory().getDataTagByName(tag_name, Factories.getDevelopmentOrganization());
			if(tag == null){
				tag = Factories.getTagFactory().newDataTag(tag_name, Factories.getDevelopmentOrganization());
				Factories.getTagFactory().addTag(tag);
				tag = Factories.getTagFactory().getDataTagByName(tag_name, Factories.getDevelopmentOrganization());
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tag;
	}
	
	public UserRoleType getRole(UserType owner, String roleName, UserRoleType parent){
		UserRoleType role = null;
		try {
			role = Factories.getRoleFactory().getRoleByName(roleName, parent, owner.getOrganization());
			if(role == null){
				role = Factories.getRoleFactory().newUserRole(owner, roleName, parent);
				Factories.getRoleFactory().addRole(role);
				role = Factories.getRoleFactory().getUserRoleByName(roleName,parent, owner.getOrganization());
			}
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
		
		return role;
	}
	
	public UserType getUser(String user_name, String password){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		
		UserType user = null;
		try {
			user = Factories.getUserFactory().getUserByName(user_name, Factories.getDevelopmentOrganization());
			if(user == null){
				user = Factories.getUserFactory().newUser(user_name, SecurityUtil.getSaltedDigest(password), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
				Factories.getUserFactory().addUser(user);
				user = Factories.getUserFactory().getUserByName(user_name, Factories.getDevelopmentOrganization());
			}
			Factories.getUserFactory().populate(user);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}
	
	public <T> T getCreatePermission(UserType user, String name, PermissionEnumType type, T parent, OrganizationType org){
		T per = null;
		try {
			per = (T)Factories.getPermissionFactory().getPermissionByName(name, type, (BasePermissionType)parent,org);
			if(per == null){
				per = (T)Factories.getPermissionFactory().newPermission(user, name, type, (BasePermissionType)parent, org);
				if(Factories.getPermissionFactory().addPermission((BasePermissionType)per)){
					per = (T)Factories.getPermissionFactory().getPermissionByName(name, type, (BasePermissionType)parent,org);
				}
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return per;
	}
	
}