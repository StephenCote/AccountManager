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
package org.cote.accountmanager.console;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.factory.FactoryDefaults;

public class RocketSetupAction {
	public static final Logger logger = LogManager.getLogger(SetupAction.class);
	public static boolean setupRocket(String adminPassword, String schemaFile) throws FactoryException{
		boolean out_bool = false;
		boolean error = false;
		logger.info("Setting up Rocket");
		if(adminPassword == null || adminPassword.length() == 0){
			logger.error("Invalid admin password");
			return out_bool;
		}
		if(schemaFile == null || schemaFile.length() == 0){
			logger.error("Invalid schema file");
			return out_bool;
		}

		String sql = new String(StreamUtil.fileToBytes(schemaFile));
		if(sql == null || sql.length() == 0){
			logger.error("Invalid schema file: " + schemaFile);
			return out_bool;
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

		if(error == true) return out_bool;
		//Factories.coolDown();
		//Factories.warmUp();
		/// Find and delete the Accelerant and Rocket organizations if they exist
		///
		Factories.recycleFactories();
		try {
			OrganizationType aOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant");
			OrganizationType rOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			if(rOrg != null){
				//logger.error("Organization should not exist.  This is most likely a caching or schema bug.");
				//return false;
				logger.info("Deleting existing organization: " + rOrg.getUrn());
				((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(rOrg);
			}
			if(aOrg != null){
				//logger.error("Organization should not exist.  This is most likely a caching or schema bug.");
				//return false;
				logger.info("Deleting existing organization: " + aOrg.getUrn());
				((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(aOrg);
			}
		} catch (FactoryException | ArgumentException e1) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e1);
			return false;
		}

		//Factories.recycleFactories();

		
		Factories.cleanupOrphans();
		Factories.clearCaches();
		Factories.recycleFactories();
		/*
		Factories.cleanupOrphans();
		Factories.clearCaches();
		Factories.recycleFactories();
		*/
		//Factories.warmUp();

		/// Invoking getIsSetup will create /Accelerant organization if it doesn't exist
		///
		if(FactoryDefaults.getIsSetup() == true){
			logger.error("Internal state is not correctly cleaned up");
			return false;
		}

		Factories.recycleFactories();
		Factories.clearCaches();
		
		GroupFactory oFact1 = org.cote.accountmanager.data.Factories.getFactory(FactoryEnumType.GROUP);
		if(oFact1.getBulkMode()){
			logger.error("Factory is incorrectly reported as being in bulk mode");
			return out_bool;
		}

		
		OrganizationType aOrg = FactoryDefaults.getAccelerantOrganization();
		if(aOrg == null){
			logger.error("Accelerant organization is null");
			return false;
		}			
		logger.info("Configuring " + aOrg.getId() + ":" + aOrg.getUrn());
		try {

			out_bool = org.cote.accountmanager.data.factory.FactoryDefaults.setupOrganization(FactoryDefaults.getAccelerantOrganization(), adminPassword);
		} catch (ArgumentException | DataAccessException | FactoryException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		
		if(Rocket.getIsSetup() == true){
			logger.error("Internal rocket state is not correctly cleaned up");
			return false;
		}
		if(out_bool && Rocket.getIsSetup() == false){
			
			try {
				out_bool = org.cote.accountmanager.data.factory.FactoryDefaults.setupOrganization(Rocket.getRocketOrganization(),adminPassword);
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
		return out_bool;
	}
}
