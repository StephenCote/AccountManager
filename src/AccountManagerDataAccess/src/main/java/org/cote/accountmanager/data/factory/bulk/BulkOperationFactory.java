package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.OperationFactory;

public class BulkOperationFactory extends OperationFactory{
	public BulkOperationFactory(){
		super();
		bulkMode = true;
		sequenceName = "operation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("operation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}