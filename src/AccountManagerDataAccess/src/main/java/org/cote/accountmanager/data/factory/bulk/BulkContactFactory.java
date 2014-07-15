package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.ContactFactory;

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