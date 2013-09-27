package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;

public class BulkContactFactory extends ContactFactory{
	public BulkContactFactory(){
		super();
		bulkMode = true;
		sequenceName = "contacts_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("contacts")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}