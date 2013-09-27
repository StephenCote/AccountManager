package org.cote.parsers.excel;

import java.util.ArrayList;
import java.util.List;

public class SheetType {
	private List<RowType> rows = null;
	private String sheetName = null;
	public SheetType(){
		rows = new ArrayList<RowType>();
	}
	public List<RowType> getRows() {
		return rows;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	
	
}
