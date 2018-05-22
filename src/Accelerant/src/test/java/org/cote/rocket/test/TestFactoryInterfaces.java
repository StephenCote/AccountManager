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
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
//import org.cote.accountmanager.data.Factories;
import org.cote.rocket.factory.FactoryDefaults;
import org.junit.Before;
import org.junit.Test;

public class TestFactoryInterfaces {
	public static final Logger logger = LogManager.getLogger(TestFactoryInterfaces.class);
	
	@Before
	public void setUp() throws Exception {

		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		//org.cote.accountmanager.service.util.ServiceUtil.useAccountManagerSession = false;
	}
	
	public void tearDownRocket(){
		String sqlFile = "/Users/Steve/Projects/Source/db/Rocket_PG9_Schema.sql";
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
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		assertFalse("Error occurred",error);
		
	}
	
	
	@Test
	public void TestRocketFactoryInterfaces(){
		//tearDownRocket();
		//org.cote.accountmanager.data.Factories.coolDown();
		boolean setup = false;
		try{
			org.cote.rocket.Factories.prepare();
			org.cote.rocket.Factories.warmUp();
			
			OrganizationFactory oFact1 = org.cote.accountmanager.data.Factories.getFactory(FactoryEnumType.ORGANIZATION);
			OrganizationFactory oFact2 = org.cote.rocket.Factories.getFactory(FactoryEnumType.ORGANIZATION);
			assertTrue("Factories should be the same instance",oFact1.equals(oFact2));
			assertFalse("Factories shouldn't be in bulk mode", oFact1.getBulkMode() || oFact2.getBulkMode());
			
			setup = setupRocket("password","/Users/Steve/Projects/Source/db/Rocket_PG9_Schema.sql");
		}
		catch(FactoryException e){
			logger.error(e);
		}
		assertTrue("Failed to setup",setup);
		/*
		logger.info("Registered " + org.cote.accountmanager.data.Factories.getFactoryClasses().size() + "/" + org.cote.rocket.Factories.getFactoryClasses().size() + " factories");
		org.cote.accountmanager.data.Factories.warmUp();
		
		
		try {
			OrganizationType aOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant");
			OrganizationType rOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			if(rOrg != null) ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(rOrg);
			if(aOrg != null) ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(aOrg);
			Factories.clearCaches();
		} catch (FactoryException | ArgumentException e1) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e1);
		}
		
		assertFalse("Factory should not be setup",FactoryDefaults.getIsSetup());
		
		Factories.recycleFactories();
		Factories.clearCaches();
		
		assertFalse("Factory should not be setup",FactoryDefaults.getIsSetup());

		boolean setup = false;
		
		try {
			setup = org.cote.accountmanager.data.factory.FactoryDefaults.setupOrganization(FactoryDefaults.getAccelerantOrganization(), "password");
		} catch (ArgumentException | DataAccessException | FactoryException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Factories were not setup",setup);
		*/
	}
	
	public static boolean setupRocket(String adminPassword, String schemaFile){
		boolean outBool = false;
		boolean error = false;
		
		if(adminPassword == null || adminPassword.length() == 0){
			logger.error("Invalid admin password");
			return outBool;
		}
		if(schemaFile == null || schemaFile.length() == 0){
			logger.error("Invalid schema file");
			return outBool;
		}

		String sql = new String(StreamUtil.fileToBytes(schemaFile));
		if(sql == null || sql.length() == 0){
			logger.error("Invalid schema file: " + schemaFile);
			return outBool;
		}
		Connection connection = null;
		try{
			connection = ConnectionFactory.getInstance().getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);
			connection.close();
		}
		catch(SQLException sqe){
			error = true;
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					
					logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				}
			}
		}

		if(error) return outBool;
		
		
		/// Find and delete the Accelerant and Rocket organizations if they exist
		///
		
		try {
			OrganizationType aOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant");
			OrganizationType rOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			if(rOrg != null) ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(rOrg);
			if(aOrg != null) ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(aOrg);
			Factories.clearCaches();
		} catch (FactoryException e1) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e1);
		} catch (ArgumentException e1) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e1);
		}
		/// Invoking getIsSetup will create /Accelerant organization if it doesn't exist
		///
		if(FactoryDefaults.getIsSetup()){
			logger.error("Internal state is not correctly cleaned up");
			return false;
		}
		try{
			Factories.recycleFactories();
			Factories.clearCaches();
		}
		catch(FactoryException f){
			logger.error(f);
		}
		OrganizationType aOrg = FactoryDefaults.getAccelerantOrganization();
		if(aOrg == null){
			logger.error("Accelerant organization is null");
			return false;
		}			
		logger.info("Configuring " + aOrg.getId() + ":" + aOrg.getUrn());
		try {

			outBool = org.cote.accountmanager.data.factory.FactoryDefaults.setupOrganization(FactoryDefaults.getAccelerantOrganization(), adminPassword);
		} catch (ArgumentException | DataAccessException | FactoryException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		
		if(Rocket.getIsSetup()){
			logger.error("Internal rocket state is not correctly cleaned up");
			return false;
		}
		if(outBool && Rocket.getIsSetup() == false){
			
			try {
				outBool = org.cote.accountmanager.data.factory.FactoryDefaults.setupOrganization(Rocket.getRocketOrganization(),adminPassword);
			} catch (ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			} catch (DataAccessException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			} catch (FactoryException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		Factories.clearCaches();
		
		
		if(Rocket.getIsSetup()){
			try {
				Rocket.configureApplicationEnvironment(adminPassword);
			}  catch (DataAccessException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			} catch (FactoryException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			} catch (ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outBool;
	}
	
}
