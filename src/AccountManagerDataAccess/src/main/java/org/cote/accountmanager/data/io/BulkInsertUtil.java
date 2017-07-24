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
package org.cote.accountmanager.data.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataCell;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.DataRowType;

public class BulkInsertUtil {
	/// Non-transactional insert of a single row
	public static final Logger logger = LogManager.getLogger(BulkInsertUtil.class);
	public static BulkInsertMeta getInsertTemplate(Connection connection, DataRow row) throws FactoryException
	{
		
		StringBuffer buff = new StringBuffer();
		StringBuffer nameBuff = new StringBuffer();
		StringBuffer valBuff = new StringBuffer();
		int cellCount = row.getCells().size();
		int insCount = 0;
		List<DataCell> insCells = new ArrayList<DataCell>();
		for(int i = 0; i < cellCount; i++){
			DataCell cell = (DataCell)row.getCells().get(i);
			
			if(cell.isDirty() == false){
				//logger.info("Skipping non-dirty cell " + cell.getColumnName());
				continue;
			}
			if(insCount > 0){
				nameBuff.append(",");
				valBuff.append(",");
			}
			nameBuff.append(cell.getColumnName());
			valBuff.append("?");
			//logger.info("Proto cell: " + cell.getColumnName());
			insCells.add(cell);
			insCount++;
		}
		buff.append("INSERT INTO " + row.getTable().getName() + " (" + nameBuff.toString() + ") VALUES (" + valBuff.toString() + ");");
		BulkInsertMeta meta = new BulkInsertMeta();
		meta.setInsertTemplate(buff.toString());
		meta.getQueryCells().addAll(insCells);
		meta.setParameterCount(insCount);
		return meta;
	}
	/*
	public static BulkInsertMeta getUpdateTemplate(Connection connection, DataRow row) throws FactoryException
	{
		StringBuffer buff = new StringBuffer();
		StringBuffer valBuff = new StringBuffer();
		int cellCount = row.getCells().size();
		int insCount = 0;
		List<DataCell> insCells = new ArrayList<DataCell>();
		for(int i = 0; i < cellCount; i++){
			DataCell cell = (DataCell)row.getCells().get(i);
			
			if(cell.isDirty() == false){
				//logger.info("Skipping non-dirty cell " + cell.getColumnName());
				continue;
			}
			if(insCount > 0){

				valBuff.append(",");
			}
			valBuff.append(cell.getColumnName() + " = ?");
			insCells.add(cell);
			insCount++;
		}
		buff.append("UPDATE " + row.getTable().getName() + " SET " + valBuff.toString());
		BulkInsertMeta meta = new BulkInsertMeta();
		meta.setUpdateTemplate(buff.toString());
		meta.getQueryCells().addAll(insCells);
		meta.setParameterCount(insCount);
		return meta;
	}
	*/
	public static PreparedStatement getInsertStatement(Connection connection, DataRow row) throws FactoryException{
		BulkInsertMeta insStatement = getInsertTemplate(connection, row);
		//logger.info(insStatement.getParameterCount() + ":" + insStatement.getInsertTemplate());
		PreparedStatement ps = null;
		try{
			ps = connection.prepareStatement(insStatement.getInsertTemplate());
			for(int i = 0; i < insStatement.getParameterCount(); i++){
				
				DBFactory.setPreparedStatementValue(ps, insStatement.getQueryCells().get(i), i+1);
			}
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error("Error",sqe);
			throw new FactoryException(sqe.getMessage());
		}
		return ps;
	}


	public static boolean insert(DataRow row) throws FactoryException{
		Connection connection = ConnectionFactory.getInstance().getConnection();
		PreparedStatement insStatement = getInsertStatement(connection, row);

		int updated = 0;
		try{
			updated = insStatement.executeUpdate();
			insStatement.close();
		}
		catch(SQLException sqe){
			
			logger.error(sqe.getMessage());
			//logger.error(getInsertTemplate(connection, row).getInsertTemplate());
			throw new FactoryException("Failed to insert: " + sqe.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error("Error",e);
				throw new FactoryException(e.getMessage());
			}
		}
		return (updated > 0);
	}
	
	/// Transactional insert across 1 or more tables of 1 or more rows
	
	public static boolean insertBulk(DataTable table, int maxBatchSize){
		boolean out_bool = false;
		if(table.getRows().size() == 0){
			//logger.info("Pending rows to insert are empty");
			return true;
		}
		//long startInsert = System.currentTimeMillis();
		Connection connection = ConnectionFactory.getInstance().getConnection();
		DataRow firstRow = (DataRow)table.getRows().get(0);
		//logger.info("Insert bulk rows: " + table.getRows().size());
		
		
		/// TODO - something is amiss with th
		
		//int maxBatchSize = 250;
		int batch = 0;
		int rLen = 0;
		BulkInsertMeta insStatement = null;
		try {

			boolean lastCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			insStatement = getInsertTemplate(connection, firstRow);
			rLen = table.getRows().size();
			//logger.info("BULK INSERT - " + rLen + " : " + insStatement.getParameterCount() + " : " + insStatement.getInsertTemplate());
			long start = System.currentTimeMillis();
			PreparedStatement statement = connection.prepareStatement(insStatement.getInsertTemplate());
			for(int r = 0; r < rLen; r++){
				DataRowType row = table.getRows().get(r);
				for(int i = 0; i < insStatement.getParameterCount(); i++){
					DataCell mCell = insStatement.getQueryCells().get(i);
					int iCol = table.getColumnIndex(mCell.getColumnName());
					DataCell iCell = (DataCell)row.getCells().get(iCol);
					//insStatement.getCells().get(i)
					DBFactory.setPreparedStatementValue(statement, iCell, i+1);
				
				}
				statement.addBatch();
				if(batch++ >= maxBatchSize){
					//logger.info("Execute batch: " + batch);
					statement.executeBatch();
					statement.clearBatch();
					batch=0;
				}
				
			}
			if(batch > 0){
				logger.debug("Execute last batch: " + batch);
				statement.executeBatch();
				statement.clearBatch();
			}
			statement.close();
			connection.commit();
			connection.setAutoCommit(lastCommit);
			long stop = System.currentTimeMillis();
			logger.debug("Inserted " + table.getRows().size() + " rows in " + (stop - start) + "ms.");
			out_bool = true;
		}
		catch(NullPointerException npe){
			if(insStatement != null) logger.error("Null pointer in: BULK INSERT - " + rLen + " : " + insStatement.getParameterCount() + " : " + insStatement.getInsertTemplate());
			logger.error(npe.getMessage());
			logger.error("Error",npe);
		}
		catch(DataAccessException dae){
			logger.error(dae.getMessage());
			logger.error("Error",dae);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		}
		catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
			while(( e = e.getNextException()) != null){
				logger.error("Error",e);
			}
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error("Error",e);
			}
		}
		//logger.info("Bulk Write Time: " + (System.currentTimeMillis() - startInsert));
		return out_bool;
	}
}
