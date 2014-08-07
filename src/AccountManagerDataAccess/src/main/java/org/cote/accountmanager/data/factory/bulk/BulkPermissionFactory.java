package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.PermissionFactory;

public class BulkPermissionFactory extends PermissionFactory{
	public BulkPermissionFactory(){
		super();
		bulkMode = true;
		sequenceName = "permissions_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("permission")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}