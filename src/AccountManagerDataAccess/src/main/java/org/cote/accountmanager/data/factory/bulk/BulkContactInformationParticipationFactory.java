package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.ContactInformationParticipationFactory;



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