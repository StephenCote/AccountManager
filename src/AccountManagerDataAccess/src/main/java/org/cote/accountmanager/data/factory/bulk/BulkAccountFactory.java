package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.objects.types.FactoryEnumType;



public class BulkAccountFactory extends AccountFactory{
	public BulkAccountFactory(){
		super();
		bulkMode = true;
		sequenceName = "accounts_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("accounts")){
			table.setBulkInsert(bulkMode);
		}
	}
	
	
}