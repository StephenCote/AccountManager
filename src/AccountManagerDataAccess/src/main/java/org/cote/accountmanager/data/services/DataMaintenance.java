package org.cote.accountmanager.data.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.objects.types.RetentionEnumType;

public class DataMaintenance {
	
	public static final Logger logger = Logger.getLogger(DataMaintenance.class.getName());
	
	public static boolean cleanupExpiredAudits(RetentionEnumType retentionType){
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connection_type = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		boolean out_bool = false;
		try{
			String sql = (connection_type == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "") + "DELETE FROM audit WHERE auditretentiontype = " + token + " AND auditexpiresdate <= " + token  + (connection_type == CONNECTION_TYPE.MYSQL? " LIMIT 200 " : "") + ";";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, retentionType.toString());
			statement.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			int affected = statement.executeUpdate();
			logger.debug("Removed " + affected + " expired audit entries");
			while (affected > 0)
			{
				affected = statement.executeUpdate();
				logger.debug("Removed " + affected + " expired audit entries");
			}
			statement.close();
			out_bool = true;
		}
		catch(SQLException sqe){
			System.out.println(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out_bool;
	}
	public static boolean cleanupSessions(){
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connection_type = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		boolean out_bool = false;
		try{
			String sql = (connection_type == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "") + "DELETE FROM session WHERE sessionexpiration <= " + token  + (connection_type == CONNECTION_TYPE.MYSQL ? " LIMIT 200 " : "") + ";";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			int affected = statement.executeUpdate();
			while (affected > 0)
			{
				affected = statement.executeUpdate();
			}
			statement.close();
			sql = (connection_type == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "") + "DELETE FROM sessiondata WHERE expiration <= " + token + (connection_type == CONNECTION_TYPE.MYSQL ? " LIMIT 200 " : "") + ";";
			statement = connection.prepareStatement(sql);
			statement.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			affected = statement.executeUpdate();
			while (affected > 0)
			{
				affected = statement.executeUpdate();
			}
			
			out_bool = true;
		}
		catch(SQLException sqe){
			System.out.println(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out_bool;
	}
}
