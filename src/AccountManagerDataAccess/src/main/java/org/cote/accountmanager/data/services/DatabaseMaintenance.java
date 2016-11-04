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
package org.cote.accountmanager.data.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.services.ThreadService;

public class DatabaseMaintenance extends ThreadService {
	
	public static final Logger logger = Logger.getLogger(DatabaseMaintenance.class.getName());
	private int spoolFlushDelay = 300000;
	
	public DatabaseMaintenance(){
		super();
		this.setThreadDelay(spoolFlushDelay);
	}
	public void execute(){
		Connection connection = ConnectionFactory.getInstance().getConnection();
		long start_cleanup = System.currentTimeMillis();
		try{
			
			String sql = "SELECT * FROM cleanup_orphans();";
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet rset = statement.executeQuery();
			rset.close();
			statement.close();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(sqe.getStackTrace());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(e.getStackTrace());
			}
		}
		logger.info("Cleaned up orphan data in " + (System.currentTimeMillis() - start_cleanup));
	}
	
}
