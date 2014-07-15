package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.AddressFactory;

public class BulkAddressFactory extends AddressFactory{
	public BulkAddressFactory(){
		super();
		bulkMode = true;
		sequenceName = "addresses_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("addresses")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}