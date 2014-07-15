package org.cote.accountmanager.data.factory.bulk;

import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.DataParticipationFactory;
import org.cote.accountmanager.objects.types.FactoryEnumType;



public class BulkDataParticipationFactory extends DataParticipationFactory{
	public BulkDataParticipationFactory(){
		super();
		bulkMode = true;
		sequenceName = "dataparticipation_id_seq";
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("dataparticipation")){
			table.setBulkInsert(bulkMode);
		}
	}
}