package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.PolicyParticipationFactory;


public class BulkPolicyParticipationFactory extends PolicyParticipationFactory{
	public BulkPolicyParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "policyparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("policyparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}