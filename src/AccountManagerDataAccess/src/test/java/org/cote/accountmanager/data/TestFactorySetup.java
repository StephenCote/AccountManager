/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
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
	private static Properties testProperties = null;
	
	@Before
	public void setUp() throws Exception {

		if(testProperties == null){
			testProperties = new Properties();
		
			try {
				InputStream fis = ClassLoader.getSystemResourceAsStream("./resource.properties"); 
						//new FileInputStream("./resource.properties");
				
				testProperties.load(fis);
				fis.close();
			} catch (IOException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				return;
			}
		}
		ConnectionFactory.setupConnectionFactory(testProperties);
	
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testFactorySetup() {
		testFactoryTearDown();
		testSetupAccountManager();
		testDefaultUsers();
	}
	private void testFactoryTearDown(){
		if(tearDown == false) return;
		
		String sqlFile = testProperties.getProperty("am6.schemaPath");
		String sqlFile2 = testProperties.getProperty("rocket.schemaPath");
		String sql = new String(StreamUtil.fileToBytes(sqlFile));
		String sql2 = new String(StreamUtil.fileToBytes(sqlFile2));
		boolean error = false;
		try{
			Connection connection = ConnectionFactory.getInstance().getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);
			statement.executeUpdate(sql2);
			connection.close();
		}
		catch(SQLException sqe){
			error = true;
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		assertFalse("Error occurred",error);
		
	}
	
	private void testSetupAccountManager(){
		if(tearDown == false) return;
		
		boolean setup = false;
		boolean error = false;
		try {
			Factories.recycleFactories();
			setup = FactoryDefaults.setupAccountManager("password1");
		} catch (NullPointerException | ArgumentException | DataAccessException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			error = true;
		}
		assertFalse("Error occurred", error);
		assertTrue("Account manager not setup", setup);
	}
	
	private void testDefaultUsers(){
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
		
		logger.error(FactoryException.LOGICAL_EXCEPTION,e);
	}

		
	}
	

}
