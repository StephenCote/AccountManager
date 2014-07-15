package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.TagParticipationFactory;

public class BulkTagParticipationFactory extends TagParticipationFactory{
	public BulkTagParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "tagparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("tagparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}