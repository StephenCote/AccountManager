package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.PolicyFactory;

public class BulkPolicyFactory extends PolicyFactory{
	public BulkPolicyFactory(){
		super();
		bulkMode = true;
		sequenceName = "policy_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("policy")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}