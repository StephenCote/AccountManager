package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.RuleParticipationFactory;


public class BulkRuleParticipationFactory extends RuleParticipationFactory{
	public BulkRuleParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "ruleparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("ruleparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}