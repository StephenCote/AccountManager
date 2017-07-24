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
package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.junit.After;
import org.junit.Test;

public class TestDataRow{
	public static final Logger logger = LogManager.getLogger(TestDataRow.class);


	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAddSampleData(){
		DataTable table = getSampleTableSchema();
		DataRow row = table.newRow();
		boolean error = false;
		assertNotNull("Table pointer in row is null", row.getTable());
		logger.info("Row Count: " + table.getRows().size());
		logger.info("Cell Count: " + row.getCells().size());
		try{
			logger.info("id index = " + table.getColumnIndex("id"));
			row.setCellValue("id", 1);
			row.setCellValue("name", "Example 1");
			row.setCellValue("time", (new Date()));
			row.setCellValue("data","Example example".getBytes());
		}
		catch(DataAccessException dae){
			logger.error(dae.getMessage());
			error = true;
		}
		assertFalse("An error occurred",error);
	}
	@Test
	public void testGetSampleData(){
		DataTable table = getSampleTableSchema();
		DataRow row = table.addNewRow();
		boolean error = false;
		try{
			row.setCellValue("id", 1);
			row.setCellValue("name", "Example 1");
			row.setCellValue("time", (Calendar.getInstance().getTime()));
			row.setCellValue("data","Example example".getBytes());
		}
		catch(DataAccessException dae){
			logger.error(dae.getMessage());
			error = true;
		}
		try{
			logger.info("Accessing integer data");
			Integer id = row.getCellValue("id");
			logger.info("Accessing string data");
			String name = row.getCellValue("name");
			logger.info("Accessing date data");
			Date date = row.getCellValue("time");
			logger.info("Accessing byte[] data");
			byte[] data = row.getCellValue("data");

		}
		catch(DataAccessException dae){
			logger.error(dae.getMessage());
			error = true;
		}
		assertFalse("An error occurred",error);
		printTable(table);
	}	
	private DataTable getSampleTableSchema(){
		DataTable out_table = new DataTable();
		out_table.setName("Example");
		out_table.addColumn("id", 0, 0, SqlDataEnumType.INTEGER);
		out_table.addColumn("name", 1, 127, SqlDataEnumType.VARCHAR);
		out_table.addColumn("time", 2, 0, SqlDataEnumType.TIMESTAMP);
		out_table.addColumn("data", 3, 1024, SqlDataEnumType.BLOB);
		return out_table;
	}

	private void printTable(DataTable table){
		logger.info("TABLE " + table.getName());
		for(int r = 0; r < table.getRows().size();r++){
			DataRow row = (DataRow)table.getRows().get(r);
			for(int i = 0; i < table.getColumnSize();i++){
				DataColumnType column = table.getColumns().get(i);
				logger.info("\tCOLUMN NAME: " + column.getColumnName());
				try{
					String strValue = null;
					if(column.getDataType() == SqlDataEnumType.VARCHAR){
						strValue = row.getCellValue(i);
					}
					else if(column.getDataType() == SqlDataEnumType.INTEGER){
						Integer intVal = row.getCellValue(i);
						strValue = intVal.toString();
					}
					else if(column.getDataType() == SqlDataEnumType.TIME || column.getDataType() == SqlDataEnumType.TIMESTAMP || column.getDataType() == SqlDataEnumType.DATE){
						Date dateVal = row.getCellValue(i);
						strValue = dateVal.toString();
					}
					else{
						strValue = "[" + column.getDataType().toString() + "]";
					}

					logger.info("\tCOLUMN VALUE: " + strValue);
				}
				catch(DataAccessException dae){
					logger.error(dae.toString());
					
				}
			}
		}
	}

}
