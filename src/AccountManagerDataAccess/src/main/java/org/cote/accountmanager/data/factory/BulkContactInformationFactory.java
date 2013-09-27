package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.objects.types.FactoryEnumType;



public class BulkContactInformationFactory extends ContactInformationFactory{
	public BulkContactInformationFactory(){
		super();
		bulkMode = true;
		sequenceName = "contactinformation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("contactinformation")){
			table.setBulkInsert(bulkMode);
		}
	}
	
	
}