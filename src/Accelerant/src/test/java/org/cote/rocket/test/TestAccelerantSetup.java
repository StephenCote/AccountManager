/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rocket.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.rocket.Rocket;
import org.cote.rocket.factory.FactoryDefaults;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
public class TestAccelerantSetup {
	public static final Logger logger = LogManager.getLogger(TestAccelerantSetup.class);

	private static String testAdminPassword ="password1";
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
	
	public void testFactoryTearDown(){
		if(tearDown == false) return;
		
		String sqlFile = "/Users/Steve/Projects/Source/db/AM4_PG9_Schema.txt";
		String sql = new String(StreamUtil.fileToBytes(sqlFile));

		String sqlFile2 = "/Users/Steve/Projects/Source/db/Rocket_PG9_Schema.txt";
		String sql2 = new String(StreamUtil.fileToBytes(sqlFile2));
		assertTrue("Schema File #1 is Null or Empty",sql != null && sql.length() > 0);
		assertTrue("Schema File #2 is Null or Empty",sql2 != null && sql2.length() > 0);
		
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
	
	@Test
	public void testSetupAccountManager(){
		if(tearDown == false) return;
		testFactoryTearDown();
		boolean setup = false;
		boolean error = false;
		try {
			setup = org.cote.accountmanager.data.factory.FactoryDefaults.setupAccountManager("password1");
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			error = true;
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			error = true;
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		
	}
	
	@Test
	public void testRocketSetup(){
		boolean setup = false;
		try {
			org.cote.accountmanager.data.factory.FactoryDefaults.setupOrganization(FactoryDefaults.getAccelerantOrganization(), "password1");
			org.cote.accountmanager.data.factory.FactoryDefaults.setupOrganization(Rocket.getRocketOrganization(),"password1");
			Rocket.configureApplicationEnvironment(Factories.getDevelopmentOrganization().getId(), "password1");
			setup = true;
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Rocket environment was not setup", setup);
	}

}
