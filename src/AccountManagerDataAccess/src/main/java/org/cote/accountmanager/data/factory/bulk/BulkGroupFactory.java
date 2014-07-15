package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.GroupFactory;

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