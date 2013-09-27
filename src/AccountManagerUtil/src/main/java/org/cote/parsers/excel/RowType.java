package org.cote.parsers.excel;

import java.util.ArrayList;
import java.util.List;

public class RowType {
	private List<CellType> cells = null;
	
	public RowType(){
		cells = new ArrayList<CellType>();
	}
	public List<CellType> getCells() {
		return cells;
	}
	public CellType getCell(int index){
		if(index >= 0 && index < cells.size()) return cells.get(index);
		return null;
	}
	public CellType getCell(String columnName){
		CellType cell = null;
		for(int i = 0; i < cells.size();i++){
			CellType match = cells.get(i);
			if(match.getColumnName() != null && match.getColumnName().equalsIgnoreCase(columnName)){
				cell = match;
				break;
			}
		}
		return cell;
	}


}
