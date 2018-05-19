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
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.util.StreamUtil;

public class SetupAction {
	public static final Logger logger = LogManager.getLogger(SetupAction.class);
	public static boolean setupAccountManager(String rootPassword, String schemaFile){
		boolean outBool = false;
		boolean error = false;
		
		if(rootPassword == null || rootPassword.length() == 0){
			logger.error("Invalid root password");
			return outBool;
		}
		if(schemaFile == null || schemaFile.length() == 0){
			logger.error("Invalid schema file");
			return outBool;
		}
		
		/*
		OrganizationType pubOrg = Factories.getPublicOrganization();
		
		if(pubOrg != null && Factories.isSetup(pubOrg)){
			logger.error("Account Manager is at least partially configured.  Drop the user table to force a reset.");
			return outBool;
		}
		*/
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

		if(error == true) return outBool;


		
		try {
			Factories.coolDown();
			Factories.warmUp();
			if(FactoryDefaults.setupAccountManager(rootPassword)){
				outBool = true;
			}
		} catch (ArgumentException | DataAccessException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		return outBool;
	}
	
}
