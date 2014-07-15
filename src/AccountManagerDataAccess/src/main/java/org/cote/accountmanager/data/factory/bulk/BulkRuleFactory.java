package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.RuleFactory;

public class BulkRuleFactory extends RuleFactory{
	public BulkRuleFactory(){
		super();
		bulkMode = true;
		sequenceName = "rule_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("rule")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}