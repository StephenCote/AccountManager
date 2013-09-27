package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;

public class BulkGroupFactory extends GroupFactory{
	public BulkGroupFactory(){
		super();
		bulkMode = true;
		sequenceName = "groups_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("groups")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}