package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.FunctionParticipationFactory;


public class BulkFunctionParticipationFactory extends FunctionParticipationFactory{
	public BulkFunctionParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "functionparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("functionparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}