package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;

public class BulkPersonFactory extends PersonFactory{
	public BulkPersonFactory(){
		super();
		bulkMode = true;
		sequenceName = "persons_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("persons")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}