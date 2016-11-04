/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.util.StreamUtil;

public class SetupAction {
	public static final Logger logger = Logger.getLogger(SetupAction.class.getName());
	public static boolean setupAccountManager(String rootPassword, String schemaFile){
		boolean out_bool = false;
		boolean error = false;
		
		if(rootPassword == null || rootPassword.length() == 0){
			logger.error("Invalid root password");
			return out_bool;
		}
		if(schemaFile == null || schemaFile.length() == 0){
			logger.error("Invalid schema file");
			return out_bool;
		}
		
		/*
		OrganizationType pubOrg = Factories.getPublicOrganization();
		
		if(pubOrg != null && Factories.isSetup(pubOrg)){
			logger.error("Account Manager is at least partially configured.  Drop the user table to force a reset.");
			return out_bool;
		}
		*/
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
			logger.error(sqe.getStackTrace());
		}
		finally{
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					
					logger.error(e.getStackTrace());
				}
			}
		}

		if(error == true) return out_bool;
		// 2016/07/27 - Bug: Because the factory starts automatically, it will throw an error
		// it also means it has to be reset again before running setup or it will fail again because all the data was just nuked by reloading the database schema
		// 
		try {
			Factories.recycleFactories();
			if(FactoryDefaults.setupAccountManager(rootPassword)){
				out_bool = true;
			}
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		} catch (DataAccessException e) {
			
			logger.error(e.getStackTrace());
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		}

		return out_bool;
	}
	
}
