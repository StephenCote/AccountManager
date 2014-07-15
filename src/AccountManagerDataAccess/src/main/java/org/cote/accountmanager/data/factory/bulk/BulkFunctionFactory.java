package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.FunctionFactory;

public class BulkFunctionFactory extends FunctionFactory{
	public BulkFunctionFactory(){
		super();
		bulkMode = true;
		sequenceName = "function_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("function")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}