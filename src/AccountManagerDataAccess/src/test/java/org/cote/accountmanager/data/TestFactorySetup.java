package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.services.FactoryService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.StreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFactorySetup {
	public static final Logger logger = Logger.getLogger(TestFactorySetup.class.getName());

	private static String testAdminPassword = SecurityUtil.getSaltedDigest("password1");
	private static boolean tearDown = true;
	
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
		logger.info("Setup");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testFactoryTearDown(){
		if(tearDown == false) return;
		
		String sqlFile = "/Users/Steve/Projects/Source/db/AM4_PG9_Schema.txt";
		String sql = new String(StreamUtil.fileToBytes(sqlFile));
		boolean error = false;
		try{
			Connection connection = ConnectionFactory.getInstance().getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);
			connection.close();
		}
		catch(SQLException sqe){
			error = true;
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		assertFalse("Error occurred",error);
		
	}
	
	@Test
	public void testSetupAccountManager(){
		if(tearDown == false) return;
		
		boolean setup = false;
		boolean error = false;
		try {
			setup = FactoryDefaults.setupAccountManager("password1");
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		}
		assertFalse("Error occurred", error);
		assertTrue("Account manager not setup", setup);
	}
	
	@Test
	public void testDefaultUsers(){
		AccountType rootAcct = null;
		AccountType adminAcct = null;
		UserType root = null;
		UserType admin = null;
		UserType doc = null;
		UserRoleType adminRole = null;
		try{
			adminRole = RoleService.getAccountAdministratorUserRole(Factories.getPublicOrganization());
			assertNotNull("Role is null", adminRole);
			rootAcct = Factories.getAccountFactory().getAccountByName("Root", Factories.getGroupFactory().getRootDirectory(Factories.getSystemOrganization()));
			root = Factories.getUserFactory().getUserByName("Root", Factories.getSystemOrganization());
			assertNotNull("Root is null", root);
			Factories.getUserFactory().populate(root);
			assertTrue("Root not populated", root.getPopulated());
			admin = Factories.getUserFactory().getUserByName("Admin", Factories.getPublicOrganization());
			doc = Factories.getUserFactory().getUserByName("Document Control", Factories.getPublicOrganization());
			assertNotNull("Admin is null", admin);
			assertNotNull("Doc Control is null", doc);
			Factories.getUserFactory().populate(admin);
			assertTrue("Admin not populated", admin.getPopulated());
			assertTrue("Admin not in admin role", RoleService.getIsUserInRole(adminRole, admin));
			assertTrue("Root not in admin role", RoleService.getIsUserInRole(adminRole, root));
			assertFalse("Doc control is not an admin", RoleService.getIsUserInRole(adminRole, doc));
			
			List<UserType> users = Factories.getRoleParticipationFactory().getUsersInRole(adminRole);
			logger.info(users.size() + " in role " + adminRole.getName());
			for(int i = 0; i < users.size(); i++){
				logger.info("#" + i + "- id: " + users.get(i).getId() + " " + users.get(i).getName());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		
	}
	

}
