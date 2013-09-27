package org.cote.accountmanager.data;

import org.cote.accountmanager.objects.DataRowType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

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
