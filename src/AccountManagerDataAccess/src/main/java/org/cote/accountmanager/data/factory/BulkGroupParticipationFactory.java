package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;

public class BulkGroupParticipationFactory extends GroupParticipationFactory{
	public BulkGroupParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "groupparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("groupparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}