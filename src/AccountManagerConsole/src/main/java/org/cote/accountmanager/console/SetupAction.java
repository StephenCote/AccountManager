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
import org.cote.accountmanager.objects.OrganizationType;
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
			sqe.printStackTrace();
		}
		finally{
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if(error == true) return out_bool;
		
		try {
			if(FactoryDefaults.setupAccountManager(rootPassword)){
				out_bool = true;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return out_bool;
	}
	
}
