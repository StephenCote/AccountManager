package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.objects.types.FactoryEnumType;



public class BulkUserFactory extends UserFactory{
	public BulkUserFactory(){
		super();
		bulkMode = true;
		sequenceName = "users_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("users")){
			table.setBulkInsert(bulkMode);
		}
	}
	
	
}