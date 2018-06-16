package org.cote.accountmanager.data.services;



import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;

import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.util.ContactParser;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.services.ThreadService;

import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.data.factory.*;

/// TODO: Still has hardcoded org references to /FirstContact
public class DataImportJob extends ThreadService {
	
	public static final Logger logger = LogManager.getLogger(DataImportJob.class);
	private int spoolFlushDelay = 10000;
	
	public DataImportJob(){
		super();
		this.setThreadDelay(spoolFlushDelay);
	}
	public void execute(){
		QueryField orField = new QueryField(SqlDataEnumType.UNKNOWN,null,null);
		orField.setComparator(ComparatorEnumType.GROUP_OR);
		QueryField field = QueryFields.getFieldName("%FirstContactTemplate.xlsx");
		field.setComparator(ComparatorEnumType.LIKE);
		QueryField cfield = QueryFields.getFieldName("%FirstContactTemplate.csv");
		cfield.setComparator(ComparatorEnumType.LIKE);
		orField.addField(field);
		orField.addField(cfield);
		try{
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/FirstContact");
			List<DataType> dataList = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataList(new QueryField[]{orField}, true, 0, 0, org.getId());
			logger.debug("Running data import for " + dataList.size() + " items");
			for(int i = 0; i < dataList.size();i++){
				DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataById(dataList.get(i).getId(), org.getId());
				if(data == null){
					logger.warn("Null data for id " + dataList.get(i).getId());
					continue;
				}
				UserType user = (UserType)Factories.getNameIdFactory(FactoryEnumType.USER).getById(data.getOwnerId(), org.getId());
				if(user == null){
					logger.warn("Null user for id " + data.getOwnerId());
					continue;
				}
				int cont = ContactParser.parseContactData(user, data);
				logger.info("Imported " + cont + " contacts for " + user.getName() + " from " + data.getName());
				((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).delete(data);
			}
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
	}
	
}