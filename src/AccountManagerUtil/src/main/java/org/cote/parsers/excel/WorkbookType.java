package org.cote.parsers.excel;

import java.util.ArrayList;
import java.util.List;

public class WorkbookType {
	private List<SheetType> sheets = null;
	public WorkbookType(){
		sheets = new ArrayList<SheetType>();
	}
	public List<SheetType> getSheets() {
		return sheets;
	}
	
}
