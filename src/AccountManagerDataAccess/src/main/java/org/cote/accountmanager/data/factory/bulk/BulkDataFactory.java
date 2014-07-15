package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.objects.types.FactoryEnumType;



public class BulkDataFactory extends DataFactory{
	public BulkDataFactory(){
		super();
		bulkMode = true;
		sequenceName = "data_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("data")){
			table.setBulkInsert(bulkMode);
		}
	}
	
	
}