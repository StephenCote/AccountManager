package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.FactFactory;

public class BulkFactFactory extends FactFactory{
	public BulkFactFactory(){
		super();
		bulkMode = true;
		sequenceName = "fact_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("fact")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}