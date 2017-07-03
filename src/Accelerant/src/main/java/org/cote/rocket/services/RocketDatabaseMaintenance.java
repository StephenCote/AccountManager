package org.cote.rocket.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.services.ThreadService;

public class RocketDatabaseMaintenance extends ThreadService {
		
		public static final Logger logger = LogManager.getLogger(RocketDatabaseMaintenance.class);
		private int spoolFlushDelay = 300000;
		
		public RocketDatabaseMaintenance(){
			super();
			this.setThreadDelay(spoolFlushDelay);
		}
		public void execute(){
			Connection connection = ConnectionFactory.getInstance().getConnection();
			long start_cleanup = System.currentTimeMillis();
			try{
				
				String sql = "SELECT * FROM cleanup_rocket_orphans();";
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet rset = statement.executeQuery();
				rset.close();
				statement.close();
			}
			catch(SQLException sqe){
				logger.error(sqe.getMessage());
				logger.error("Error",sqe);
			}
			finally{
				try {
					connection.close();
				} catch (SQLException e) {
					
					logger.error("Error",e);
				}
			}
			logger.info("Cleaned up orphan data in " + (System.currentTimeMillis() - start_cleanup));
		}
}
