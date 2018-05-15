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
package org.cote.accountmanager.data.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.types.RetentionEnumType;

public class DataMaintenance {
	
	public static final Logger logger = LogManager.getLogger(DataMaintenance.class);
	
	public static boolean cleanupExpiredAudits(RetentionEnumType retentionType){
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		boolean outBool = false;
		PreparedStatement statement = null;
		
		try{
			String limit1 = (connectionType == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "");
			String limit2 = (connectionType == CONNECTION_TYPE.MYSQL? " LIMIT 200 " : "");
			String sql = String.format("%sDELETE FROM audit WHERE auditretentiontype = %s AND auditexpiresdate <= %s %s;",limit1,token,token,limit2);
			statement = connection.prepareStatement(sql);
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
			outBool = true;
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outBool;
	}
	public static boolean cleanupSessions(){
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		boolean outBool = false;
		PreparedStatement statement = null;
		try{
			String limit1 = (connectionType == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "");
			String limit2 = (connectionType == CONNECTION_TYPE.MYSQL ? " LIMIT 200 " : "");
			String sql = String.format("%sDELETE FROM session WHERE sessionexpiration <= %s%s;",limit1,token,limit2);
			statement = connection.prepareStatement(sql);
			statement.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			int affected = statement.executeUpdate();
			while (affected > 0)
			{
				affected = statement.executeUpdate();
			}
			statement.close();
			sql = String.format("%sDELETE FROM sessiondata WHERE expiration <= %s%s;",limit1,token,limit2);
			statement = connection.prepareStatement(sql);
			statement.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			affected = statement.executeUpdate();
			while (affected > 0)
			{
				affected = statement.executeUpdate();
			}
			
			outBool = true;
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outBool;
	}
}
