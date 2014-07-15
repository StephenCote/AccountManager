package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.TagFactory;

public class BulkTagFactory extends TagFactory{
	public BulkTagFactory(){
		super();
		bulkMode = true;
		sequenceName = "tags_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("tag")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}