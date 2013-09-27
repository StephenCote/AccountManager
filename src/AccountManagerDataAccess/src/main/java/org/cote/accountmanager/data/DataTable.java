package org.cote.accountmanager.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.DataRowType;
import org.cote.accountmanager.objects.DataTableType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class DataTable extends DataTableType {
	private Map<String,Integer> columnMap = Collections.synchronizedMap(new HashMap<String,Integer>());
	private static Map<String,Boolean> restrictSelectMap = new HashMap<String, Boolean>();
	private static Map<String, Boolean> restrictUpdateMap = new HashMap<String, Boolean>();

	public void setRestrictUpdateColumn(String column_name, boolean restricted){
		restrictUpdateMap.put(column_name,  restricted);
	}
	public void setRestrictSelectColumn(String column_name, boolean restricted){
		restrictSelectMap.put(column_name,  restricted);
	}
	public boolean getCanSelectColumn(String column_name)
	{
		if(restrictSelectMap.containsKey(column_name) && restrictSelectMap.get(column_name).booleanValue() == true) return false;
		return true;
	}
	
	public boolean getCanUpdateColumn(String column_name)
	{
		if(restrictUpdateMap.containsKey(column_name) && restrictUpdateMap.get(column_name).booleanValue() == true) return false;
		return true;
	}
	
	public int getColumnIndex(String columnName) throws DataAccessException{
		if(columnMap.containsKey(columnName) == false) throw new DataAccessException("Invalid column '" + columnName + "'");
		return getColumnMap().get(columnName).intValue();
	}
	public Map<String, Integer> getColumnMap() {
		return columnMap;
	}
	public DataColumnType addColumn(String name, int index, int size, SqlDataEnumType type){
		DataColumnType col = new DataColumnType();
		col.setColumnIndex(index);
		col.setColumnName(name);
		col.setColumnSize(size);
		col.setDataType(type);
		columnMap.put(name, index);
		this.getColumns().add(col);
		this.setColumnSize(this.getColumns().size());
		return col;
	}
	public DataRow addNewRow(){
		DataRow row = newRow();
		this.getRows().add(row);
		return row;
	}
	public DataRow newRow(){
		DataRow row = new DataRow();
		row.setTable(this);
		int colLen = this.getColumns().size();
		for(int i = 0; i < colLen; i++){
			DataCell cell = new DataCell();
			DataColumnType col = this.getColumns().get(i);
			cell.setColumnIndex(col.getColumnIndex());
			cell.setColumnName(col.getColumnName());
			cell.setDataType(col.getDataType());
			row.getCells().add(cell);
		}
		//this.getRows().add(row);
		return row;
	}
}
