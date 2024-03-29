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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.junit.Test;

public class TestBulkUser extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestBulkUser.class);
	
	@Test
	public void TestBulkUser(){
		boolean success = false;
		String guid = UUID.randomUUID().toString();
		try{
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			UserType new_user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser("BulkUser-" + guid, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, new_user);
			//SecurityType asymKey = KeyService.newPersonalAsymmetricKey(sessionId,null,new_user,false);
			//SecurityType symKey = KeyService.newPersonalSymmetricKey(sessionId,null,new_user,false);
			CredentialService.newCredential(CredentialEnumType.HASHED_PASSWORD,sessionId,new_user, new_user, "password1".getBytes(StandardCharsets.UTF_8), true, true);

			logger.info("Retrieving Bulk User");
			UserType check = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("BulkUser-" + guid, new_user.getOrganizationId());
			if(check == null){
				logger.error(Factories.getNameIdFactory(FactoryEnumType.USER).getCacheReport());
			}
			assertNotNull("Failed user cache check",check);
			
			logger.info("Retrieving User By Id");
			check = Factories.getNameIdFactory(FactoryEnumType.USER).getById(new_user.getId(), new_user.getOrganizationId());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			success = true;
		}
		catch(FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		assertTrue("Success bit is false",success);
		// Now try to authenticate as the new bulk loaded user
		UserType chkUser = null;
		try {
			 chkUser = SessionSecurity.login("BulkUser-" + guid, CredentialEnumType.HASHED_PASSWORD, "password1", Factories.getDevelopmentOrganization().getId());
			 assertNotNull("Unable to authenticate as new user",chkUser);
			 SessionSecurity.logout(chkUser);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}

}