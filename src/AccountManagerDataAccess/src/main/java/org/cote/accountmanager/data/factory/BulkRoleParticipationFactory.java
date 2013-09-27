package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;

public class BulkRoleParticipationFactory extends RoleParticipationFactory{
	public BulkRoleParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "roleparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("roleparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
}