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
package org.cote.accountmanager.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class ConnectionFactory {
	public static final Logger logger = Logger.getLogger(ConnectionFactory.class.getName());
	private String driverClassName = null;
	private String userName = null;
	private String userPassword = null;
	private String url = null;
	private String jndiDataSource = null;
	private CONNECTION_TYPE connectionType = CONNECTION_TYPE.UNKNOWN;
	private boolean checkDriver = true;
	
	private static ConnectionFactory Singleton = null;
	
	private static DataSource ds = null;
	public static ConnectionFactory getInstance(){
		if(Singleton == null){ 
			Singleton = new ConnectionFactory();
		}
		return Singleton;
	}
	
	public ConnectionFactory(){
		
	}
	
	public boolean isCheckDriver() {
		return checkDriver;
	}

	public void setCheckDriver(boolean checkDriver) {
		this.checkDriver = checkDriver;
	}

	public Connection getConnection(){
		//System.out.println("Get Connection: " + connectionType);
		if(connectionType == CONNECTION_TYPE.SINGLE) return getConnection(url, userName, userPassword, driverClassName);
		else if(connectionType == CONNECTION_TYPE.DS) return getDSConnection(jndiDataSource, driverClassName);
		return null;
	}
	public String getJndiDataSource() {
		return jndiDataSource;
	}

	public void setJndiDataSource(String jndiDataSource) {
		this.jndiDataSource = jndiDataSource;
	}

	
	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public CONNECTION_TYPE getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(CONNECTION_TYPE connectionType) {
		this.connectionType = connectionType;
	}

	private static Map<String,Boolean> driverCheck = Collections.synchronizedMap(new HashMap<String,Boolean>());
	public static enum CONNECTION_TYPE{
		UNKNOWN,
		DS,
		SINGLE
	};
	/*
	public static Connection getPostGres91Connection(String url, String userName, String password){
		return getConnection(url, userName, password, "org.postgresql.Driver");
	}
	public static Connection getPostGres91DSConnection(String jndiDS){
		return getDSConnection(jndiDS,"org.postgresql.Driver");
	}
	*/
	/*
	private static Connection getConnection(String url, String userName, String password, String driverClassName){
		return getSingleConnection(url, userName, password, "org.postgresql.Driver");
	}
	*/
	private static boolean tryDriver(String driverClassName){
		boolean out_bool = false;
		
		if(ConnectionFactory.getInstance().isCheckDriver() == false || (driverCheck.containsKey(driverClassName) && driverCheck.get(driverClassName).booleanValue() == true)){
			return true;
		}
	    try {
			Class.forName(driverClassName);
			out_bool = true;
		} catch (ClassNotFoundException e) {
			
			System.out.println("Class Not Found for '" + driverClassName + '"');
			/// logger.error(e.getStackTrace());
		}
	    driverCheck.put(driverClassName, out_bool);
		return out_bool;
	}
	public static Connection getConnection(String url, String userName, String password, String driverClassName)  {   
		  
		if(tryDriver(driverClassName) == false){
			return null;
		}
		Connection connection = null;
		try {
			/*
			Properties properties = new Properties();
			properties.put("user",     "devuser");
			properties.put("password", "password");
			properties.put("ssl",      "true");
			properties.put("sslfactory","org.postgresql.ssl.NonValidatingFactory");
			*/
		
			connection = DriverManager.getConnection(
					url,
					//properties);
					userName, password);
		}
		catch (SQLException e) {
 			System.out.println("Connection Failed! Check output console");
			logger.error(e.getStackTrace());
		}
		return connection;
	}
	public static Connection getDSConnection(String jndiDS, String driverClassName)  {   
		Connection out_connection = null;
		if(ds == null){
			if(tryDriver(driverClassName) == false){
				return null;
			}
		    String dsFile = "java:/" + jndiDS;   
		    
			try {
				InitialContext ctx = new InitialContext();
				ds = (DataSource) ctx.lookup(dsFile);
				//System.out.println("AutoCommit=" + out_connection.getAutoCommit());
			}
			catch (NamingException e) {
				
				logger.error(e.getStackTrace());
			}
		}
		try{
			out_connection = ds.getConnection();

		}
		catch(SQLException sqe){
			logger.error(sqe.getStackTrace());
		}
 
  
	    return out_connection;  
	}  
	
	
}