package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.RoleFactory;

public class BulkRoleFactory extends RoleFactory{
	public BulkRoleFactory(){
		super();
		bulkMode = true;
		sequenceName = "roles_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("role")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}