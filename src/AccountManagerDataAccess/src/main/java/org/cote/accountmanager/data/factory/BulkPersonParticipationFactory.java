package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;

public class BulkPersonParticipationFactory extends PersonParticipationFactory{
	public BulkPersonParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "personparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("personparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}