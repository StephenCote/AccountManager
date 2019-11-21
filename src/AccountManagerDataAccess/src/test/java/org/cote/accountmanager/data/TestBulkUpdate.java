/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.FactoryException;
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
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} 

		
		*/
		
	}


}
	