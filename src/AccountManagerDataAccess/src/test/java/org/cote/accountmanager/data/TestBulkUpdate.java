package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestBulkUpdate extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestBulkUpdate.class);
	
	protected AccountType getAccount(UserType owner, String name){
		
		DirectoryGroupType rootDir = null;
		AccountType qaAccount = null;
		try{
			rootDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getHomeDirectory(owner.getOrganizationId());
			if(rootDir == null){
				logger.error("Null directory");
				return null;
			}
			qaAccount = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(name, rootDir);
	
			if(qaAccount == null){
				qaAccount = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount(owner,name, AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, rootDir.getId());
				((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).add(qaAccount);
				qaAccount = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(name, rootDir);
			}
		}
		catch(FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		}
		return qaAccount;
	}
	
	
	@Test
	public void TestBulkDataUpdate(){
		
		AccountType qaAccount1 = getAccount(testUser,"QA Account 1");
		AccountType qaAccount2 = getAccount(testUser,"QA Account 2");
		AccountType qaAccount3 = getAccount(testUser,"QA Account 3");
		
		DataType qaData1 = getData(testUser,"QA Data 1");
		qaData1.getAttributes().add(Factories.getAttributeFactory().newAttribute(qaData1, "Test Attribute 1", "Test Value 1"));
		try {
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(qaData1);
			Factories.getAttributeFactory().updateAttributes(qaData1);
			qaData1 = getData(testUser,"QA Data 1");
			Factories.getAttributeFactory().populateAttributes(qaData1);
			String attrVal = Factories.getAttributeFactory().getAttributeValueByName(qaData1, "Test Attribute 1");
			assertTrue("Attribute is null",attrVal != null && attrVal.length() > 0);
			
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DataType qaData2 = getData(testUser,"QA Data 2");
		DataType qaData3 = getData(testUser,"QA Data 3");

		qaData1.setDescription(UUID.randomUUID().toString());
		qaData2.setDescription(UUID.randomUUID().toString());
		qaData2.setDescription(UUID.randomUUID().toString());
		
		qaAccount1.setReferenceId((new Random()).nextLong());
		qaAccount2.setReferenceId((new Random()).nextLong());
		qaAccount3.setReferenceId((new Random()).nextLong());
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		try{
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.ACCOUNT, qaAccount1);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.ACCOUNT, qaAccount2);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.ACCOUNT, qaAccount3);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.DATA, qaData1);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.DATA, qaData2);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.DATA, qaData3);
			BulkFactories.getBulkFactory().write(sessionId);
			
			
			qaData1 = getData(testUser,"QA Data 1");
			Factories.getAttributeFactory().populateAttributes(qaData1);
			String attrVal = Factories.getAttributeFactory().getAttributeValueByName(qaData1, "Test Attribute 1");
			assertTrue("Attribute is null",attrVal != null && attrVal.length() > 0);
			
		}
		catch (ArgumentException | FactoryException | DataAccessException e) {
			
			logger.error("Error",e);
		} 

		/*
		List<AccountType> accts = new ArrayList<AccountType>();

		accts.add(qaAccount1);
		accts.add(qaAccount2);
		accts.add(qaAccount3);
		try{
			BulkFactories.getBulkAccountFactory().updateBulk(accts);


		
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		} 

		
		*/
		
	}


}
	