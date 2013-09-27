package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;



public class BulkContactInformationParticipationFactory extends ContactInformationParticipationFactory{
	public BulkContactInformationParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "contactinformationparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("contactinformationparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
	
}