package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;

public class BulkStatisticsFactory extends StatisticsFactory{
	public BulkStatisticsFactory(){
		super();
		bulkMode = true;
		sequenceName = "statistics_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("statistics")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}