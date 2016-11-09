/*
 * WARNING: TestFactorySetup WILL ERASE EVERYTHING IN THE DATABASE
 * 
 * This unit test (fine, functional test for you pedantic blowhards) will connect to the database and rip it all down
 * This is what it's supposed to do - test the setup process
 * But that means if you run it, assuming the right database settings are provided, it will re-apply the database schema, which in turn will replace existing schema  
 */

package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.StreamUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
public class TestFactorySetup {
	public static final Logger logger = LogManager.getLogger(TestFactorySetup.class);

	private static String testAdminPassword = "password1";
	private static boolean tearDown = true;
	
	@Before
	public void setUp() throws Exception {

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
		
		String sqlFile = "/Users/Steve/Projects/Source/db/AM6_PG9_Schema.sql";
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
			logger.error("Error",sqe);
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
		} catch (NullPointerException | ArgumentException | DataAccessException | FactoryException e) {
			
			logger.error("Error",e);
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
			adminRole = RoleService.getAccountAdministratorUserRole(Factories.getPublicOrganization().getId());
			assertNotNull("Role is null", adminRole);
			rootAcct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName("Root", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getRootDirectory(Factories.getSystemOrganization().getId()));
			root = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Root", Factories.getSystemOrganization().getId());
			assertNotNull("Root is null", root);
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(root);
			assertTrue("Root not populated", root.getPopulated());
			admin = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", Factories.getPublicOrganization().getId());
			doc = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Document Control", Factories.getPublicOrganization().getId());
			assertNotNull("Admin is null", admin);
			assertNotNull("Doc Control is null", doc);
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(admin);
			assertTrue("Admin not populated", admin.getPopulated());
			assertTrue("Admin not in admin role", RoleService.getIsUserInRole(adminRole, admin));
			assertTrue("Root not in admin role", RoleService.getIsUserInRole(adminRole, root));
			assertFalse("Doc control is not an admin", RoleService.getIsUserInRole(adminRole, doc));
			
			List<UserType> users = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getUsersInRole(adminRole);
			logger.info(users.size() + " in role " + adminRole.getName());
			for(int i = 0; i < users.size(); i++){
				logger.info("#" + i + "- id: " + users.get(i).getId() + " " + users.get(i).getName());
			}
		
	} catch (NullPointerException | ArgumentException | FactoryException e) {
		
		logger.error("Error",e);
	}

		
	}
	

}
