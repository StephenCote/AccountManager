/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.junit.Test;

public class TestSecuritySpool extends BaseDataAccessTest {
	public static final Logger logger = LogManager.getLogger(TestSecuritySpool.class);
	
	private static String referenceId = UUID.randomUUID().toString();
	private static String referenceId2 = UUID.randomUUID().toString();
	
	
	@Test
	public void testTokens(){
		testInsertToken();
		testGetToken();
		testUpdateToken();
		testDeleteToken();
	}
	public void testInsertToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		boolean add_token = false;
		try{
			token = Factories.getSecurityTokenFactory().generateSecurityToken(referenceId, testUser.getOrganizationId());
			add_token = (token != null);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("token was not added", add_token);
	}


	public void testGetToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		logger.info("Token: " + referenceId);
		try{
			token = Factories.getSecurityTokenFactory().getSecurityToken(referenceId, testUser.getOrganizationId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("Expected one token", token);
	}
	

	public void testUpdateToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		try{
			token = Factories.getSecurityTokenFactory().getSecurityToken(referenceId, testUser.getOrganizationId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("Expected one token", token);
		token.setData("Example data".getBytes());
		boolean updated = false;
		try{
			updated = Factories.getSecurityTokenFactory().update(token);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		assertTrue("token was not updated", updated);
	}
	
	public void testDeleteToken(){
		assertNotNull("User is null", testUser);
		boolean deleted = false;
		try{
			deleted = Factories.getSecurityTokenFactory().deleteTokens(referenceId, testUser.getOrganizationId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		assertTrue("Failed to deleted", deleted);
	}
	/*
	@Test
	public void testInsertTokens(){
		List<SecuritySpoolType> tokenTypes = new ArrayList<SecuritySpoolType>();
		boolean error = false;
		assertFalse("Table cache is not cleaned up", Factories.getSecurityTokenFactory().getDataTable("spool").getRows().size() > 0);
		try{
		for(int i = 0; i < 10;i++){
			SecurityBean bean = new SecurityBean();
			SecurityFactory.getSecurityFactory().generateSecretKey(bean);
			SecuritySpoolType tokenType = Factories.getSecurityTokenFactory().newSecurityToken(referenceId2, Factories.getPublicOrganization());
			tokenType.setData(BinaryUtil.toBase64Str(bean.getCipherKey()) + "&&" + BinaryUtil.toBase64Str(bean.getCipherIV()));
			tokenTypes.add(tokenType);
		}
		if(Factories.getSecurityTokenFactory().addSecurityTokens(tokenTypes.toArray(new SecuritySpoolType[0])) == false){
			System.out.println("Failed to persist tokens");
		}
		}
		catch(FactoryException fe){
			error = true;
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		assertFalse("An error occurred", error);
	}
	*/
	
}