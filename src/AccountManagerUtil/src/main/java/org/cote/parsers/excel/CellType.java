package org.cote.parsers.excel;

public class CellType {
	private String cellValue = null;
	private String columnName = null;
	public CellType(){
		
	}
	public CellType(String val){
		this.cellValue = val;
	}
	public String getCellValue() {
		return cellValue;
	}
	public void setCellValue(String cellValue) {
		this.cellValue = cellValue;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
}
