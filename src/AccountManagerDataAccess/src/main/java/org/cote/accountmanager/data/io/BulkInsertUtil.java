package org.cote.accountmanager.data.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataCell;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.ParticipationFactory;
import org.cote.accountmanager.objects.DataRowType;

public class BulkInsertUtil {
	/// Non-transactional insert of a single row
	public static final Logger logger = Logger.getLogger(BulkInsertUtil.class.getName());
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
			sqe.printStackTrace();
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
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
				throw new FactoryException(e.getMessage());
			}
		}
		return (updated > 0);
	}
	
	/// Transactional insert across 1 or more tables of 1 or more rows
	
	public static boolean insertBulk(DataTable table){
		boolean out_bool = false;
		if(table.getRows().size() == 0){
			//logger.info("Pending rows to insert are empty");
			return true;
		}
		Connection connection = ConnectionFactory.getInstance().getConnection();
		DataRow firstRow = (DataRow)table.getRows().get(0);
		//logger.info("Insert bulk rows: " + table.getRows().size());
		
		
		/// TODO - something is amiss with th
		
		int maxBatchSize = 250;
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
			npe.printStackTrace();
		}
		catch(DataAccessException dae){
			logger.error(dae.getMessage());
			dae.printStackTrace();
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			while(( e = e.getNextException()) != null){
				e.printStackTrace();
			}
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return out_bool;
	}
}
