package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.PatternFactory;

public class BulkPatternFactory extends PatternFactory{
	public BulkPatternFactory(){
		super();
		bulkMode = true;
		sequenceName = "pattern_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("pattern")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}