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

import org.cote.accountmanager.objects.DataRowType;

public class DataRow extends DataRowType {
	private DataTable table = null;
	
	public DataTable getTable() {
		return table;
	}
	public void setTable(DataTable table) {
		this.table = table;
	}
	public DataCell getCell(String columnName) throws DataAccessException{
		return getCell(table.getColumnIndex(columnName));
	}
	public DataCell getCell(int columnIndex) throws DataAccessException{
		if(this.table == null){
			throw new DataAccessException("Null table reference");
		}
		if(columnIndex < 0 || columnIndex >= this.table.getColumnSize()){
			throw new DataAccessException("Invalid column index");
		}
		return (DataCell)this.getCells().get(columnIndex);

	}
	public <T> void setCellValue(String columnName, T value) throws DataAccessException{
		setCellValue(table.getColumnIndex(columnName), value);
	}
	public <T> void setCellValue(int columnIndex, T value) throws DataAccessException{
		DataCell cell = getCell(columnIndex);
		if(cell == null){
			throw new DataAccessException("Null cell for index " + columnIndex);
		}
		cell.setValue(value);
	}
	public <T> T getCellValue(String columnName) throws DataAccessException{
		return getCellValue(table.getColumnIndex(columnName));
	}
	public <T> T getCellValue(int columnIndex) throws DataAccessException{
		DataCell cell = getCell(columnIndex);
		return cell.getValue();
	}
	/*
	public String getString(int columnIndex) throws DataAccessException{
		String out_value = null;
		DataCell cell = getCell(columnIndex);
		try{
			out_value = (String)cell.getCellData();
		}
		catch(ClassCastException cce){
			throw new DataAccessException(cce.getMessage());
		}
		return out_value;
	}
	public String getString(String columnName) throws DataAccessException{
		return getString(table.getColumnIndex(columnName));
	}
	public void setString(int columnIndex, String value) throws DataAccessException {
		if(columnIndex >= this.table.getColumnSize()){
			throw new DataAccessException("Invalid column index");
		}
		DataCell cell = (DataCell)this.getCells().get(columnIndex);
		cell.setDataType(SqlDataEnumType.VARCHAR);
		setCellValue(cell, value);
	}
	public void setString(String columnName, String value) throws DataAccessException {
		setString(table.getColumnIndex(columnName), value);
	}
	private void setCellValue(DataCell cell, Object value){
		cell.setCellData(value);
		cell.setDirty(true);
	}
	*/
}
