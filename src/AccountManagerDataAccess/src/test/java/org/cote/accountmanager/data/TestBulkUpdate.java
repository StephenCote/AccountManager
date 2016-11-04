package org.cote.accountmanager.data;

import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestBulkUpdate extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestBulkUpdate.class.getName());
	
	protected AccountType getAccount(UserType owner, String name){
		
		DirectoryGroupType rootDir = null;
		AccountType qaAccount = null;
		try{
			rootDir = Factories.getGroupFactory().getHomeDirectory(owner.getOrganizationId());
			if(rootDir == null){
				logger.error("Null directory");
				return null;
			}
			qaAccount = Factories.getAccountFactory().getAccountByName(name, rootDir);
	
			if(qaAccount == null){
				qaAccount = Factories.getAccountFactory().newAccount(owner,name, AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, rootDir.getId());
				Factories.getAccountFactory().add(qaAccount);
				qaAccount = Factories.getAccountFactory().getAccountByName(name, rootDir);
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(fe.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
		}
		return qaAccount;
	}
	
	
	@Test
	public void TestBulkDataUpdate(){
		
		AccountType qaAccount1 = getAccount(testUser,"QA Account 1");
		AccountType qaAccount2 = getAccount(testUser,"QA Account 2");
		AccountType qaAccount3 = getAccount(testUser,"QA Account 3");
		
		DataType qaData1 = getData(testUser,"QA Data 1");
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
		}
		catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (DataAccessException e) {
			
			logger.error(e.getStackTrace());
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
			logger.error(fe.getStackTrace());
		} 

		
		*/
		
	}


}
	