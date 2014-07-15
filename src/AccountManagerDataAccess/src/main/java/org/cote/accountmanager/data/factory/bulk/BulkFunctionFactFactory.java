package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.FunctionFactFactory;

public class BulkFunctionFactFactory extends FunctionFactFactory{
	public BulkFunctionFactFactory(){
		super();
		bulkMode = true;
		sequenceName = "functionfact_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("functionfact")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}