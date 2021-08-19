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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;
public class TestOrganizationFactory extends BaseDataAccessTest{
	private static String testOrgName = null;
	public static final Logger logger = LogManager.getLogger(TestOrganizationFactory.class);
	private static String testOrgPassword = null;
	private static String sessionId = null;
	
	@Test
	public void runTests(){
		testAddOrganization();
		testGetOrganization();
		testSetupOrganization();
		testCredentialSalted();
		testOrganizationCipher();
		testUpdateOrganization();
		testAddOrphanOrganization();
		testDeleteOrganization();
	}
	
	public void testAddOrganization(){
		boolean error = false;
	
		assertFalse("An error occurred", error);
		testOrgName = "Example " + System.currentTimeMillis();
		OrganizationType newOrg = new OrganizationType();
		newOrg.setName(testOrgName);
		newOrg.setNameType(NameEnumType.ORGANIZATION);
		newOrg.setOrganizationType(OrganizationEnumType.DEVELOPMENT);
		
		logger.info("Id: " + newOrg.getId());
		logger.info("Ref Id: " + newOrg.getReferenceId());
		try {
			OrganizationFactory org_factory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
			OrganizationType devOrg = Factories.getDevelopmentOrganization();
			newOrg.setParentId(devOrg.getId());

			if(org_factory.add(newOrg)){
				newOrg = org_factory.getByNameInParent(testOrgName,devOrg.getId(), 0L);
				KeyService.newOrganizationAsymmetricKey(newOrg.getId(), true);
				KeyService.newOrganizationSymmetricKey(newOrg.getId(), true);
			}
		} catch (FactoryException | ArgumentException e) {
			error = true;
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		logger.info("Added " + testOrgName + " as " + newOrg.getId());
		assertFalse("An error occurred", error);
	}
	
	public void testAddOrphanOrganization(){
		boolean error = false;

		String orgName = "Example " + System.currentTimeMillis();
		OrganizationType newOrg = new OrganizationType();
		newOrg.setName(orgName);
		newOrg.setNameType(NameEnumType.ORGANIZATION);
		newOrg.setOrganizationType(OrganizationEnumType.DEVELOPMENT);

		OrganizationType parentOrg = null;
		

		try {
			OrganizationFactory org_factory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
			OrganizationType devOrg = Factories.getDevelopmentOrganization();

			parentOrg = org_factory.getByNameInParent(testOrgName, devOrg.getId(),0L);
			assertNotNull("Test organization " + testOrgName + " is null in " + devOrg.getUrn(),parentOrg);
			newOrg.setParentId(parentOrg.getId());

			if(org_factory.add(newOrg)){
				newOrg = org_factory.getByNameInParent(orgName, parentOrg.getId(), 0L);
				assertNotNull("New organization is null",newOrg);
			}
			else{
				logger.error("Failed to add new organization " + testOrgName);
				newOrg = null;
			}
		} catch (FactoryException | ArgumentException e) {
			error = true;
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		logger.info("Added " + testOrgName + " as " + newOrg.getId());
		assertFalse("An error occurred", error);
	}
	
	public void testGetOrganization(){
		boolean error = false;
		
		OrganizationType newOrg = null;
		try{
			logger.info("Read clean: " + testOrgName + " in " + Factories.getDevelopmentOrganization().getId());
			newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
			assertNotNull("Get organization " + testOrgName + "->" + Factories.getDevelopmentOrganization().getId() + " by name was null", newOrg);
			logger.info("Read from cache by id: " + newOrg.getId());
			newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(newOrg.getId());
			assertNotNull("Get organization from cache by id was null", newOrg);
			logger.info("Read from cache by name and parent");
			newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
			assertNotNull("Get oranization from cache by name was null null", newOrg);
		}
		catch(FactoryException | ArgumentException e) {
			error = true;
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse(FactoryException.LOGICAL_EXCEPTION,error);

		
		logger.info("Id: " + newOrg.getId());
		logger.info("Ref Id: " + newOrg.getReferenceId());
		
	}
	
	public void testSetupOrganization(){
		boolean error = false;
		testOrgPassword = "Password - " + UUID.randomUUID().toString();
		OrganizationType newOrg = null;
		try{
			newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
			assertNotNull("Get organization " + testOrgName + "->" + Factories.getDevelopmentOrganization().getId() + " by name was null", newOrg);
			assertTrue("Failed to setup new organization", FactoryDefaults.setupOrganization(newOrg, testOrgPassword));
		}
		catch(FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse(FactoryException.LOGICAL_EXCEPTION,error);
	}
	
	public void testCredentialSalted(){
		boolean error = false;
		OrganizationType newOrg = null;
		sessionId = UUID.randomUUID().toString();
		try{
			newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
			assertNotNull("Get organization " + testOrgName + "->" + Factories.getDevelopmentOrganization().getId() + " by name was null", newOrg);
			UserType adminUser = SessionSecurity.login(sessionId, "Admin", CredentialEnumType.HASHED_PASSWORD,testOrgPassword, newOrg.getId());
			assertNotNull("Failed to authenticate as admin user", adminUser);
			CredentialType cred = CredentialService.getPrimaryCredential(adminUser);
			assertNotNull("Failed to retrieve credential object for admin user");
			logger.info(JSONUtil.exportObject(cred));
			
			
			assertTrue("Expected the password to validate against the credential", CredentialService.validatePasswordCredential(adminUser, cred, testOrgPassword));
			/// This is effectively the same as the Auth
			SecurityBean bean = KeyService.getAsymmetricKeyByObjectId(cred.getKeyId(), cred.getOrganizationId());
			assertNotNull("Cipher is null", bean);
			byte[] credBytes = SecurityUtil.decrypt(bean, cred.getCredential());
			byte[] pwdBytes = SecurityUtil.getDigest(testOrgPassword.getBytes("UTF-8"), cred.getSalt());
			String digest = new String(pwdBytes,"UTF-8");
			assertTrue("Password bytes don't match",Arrays.areEqual(credBytes, pwdBytes));
			logger.info("Comparing from bytes: " + (new String(credBytes,"UTF-8") + " == " + new String(pwdBytes,"UTF-8")));
			logger.info("Comparing " + digest + " == " + (new String(credBytes,"UTF-8")));
			assertTrue("Expected the salted hashed password to match: " + digest + " == " + (new String(credBytes,"UTF-8")), digest.equals(new String(credBytes,"UTF-8")));
			
			SessionSecurity.logout(adminUser);

		}
		catch(FactoryException | ArgumentException | UnsupportedEncodingException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse(FactoryException.LOGICAL_EXCEPTION,error);
	}
	
	
	public void testOrganizationCipher(){
		boolean error = false;

		
		OrganizationType newOrg = null;
		try{
			logger.info("Read clean");
			newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse(FactoryException.LOGICAL_EXCEPTION,error);
		assertNotNull("Org is null", newOrg);
		

		SecurityBean bean = KeyService.getPrimarySymmetricKey(newOrg.getId()); 

		String test_data = "This is some test data.";
		byte[] enc = SecurityUtil.encipher(bean, test_data.getBytes());
		assertTrue("Enciphered data is empty or null",enc != null && enc.length > 0);
		try{
			((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).clearCache();

			newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
		}
		catch(FactoryException | ArgumentException e) {
			error = true;
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		bean = KeyService.getPrimaryAsymmetricKey(newOrg.getId());

		byte[] dec = SecurityUtil.decipher(bean, enc);
		logger.info("Decrypted: " + (new String(dec)));
		logger.info("Bean: " + (bean == null ? "Null":"Retrieved"));
		assertNotNull("Bean is null", bean);
	}

	public void testUpdateOrganization(){
		boolean updated = false;
		boolean error = false;
		String newName = "Updated Example - " + System.currentTimeMillis();
		try{
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
			assertNotNull("Org " + testOrgName + " is null in parent " + Factories.getDevelopmentOrganization().getUrn(),org);
			org.setName(newName);
			updated = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).update(org);
			if(updated){
				testOrgName = newName;
			}

		}
		catch(FactoryException | ArgumentException e) {
			error = true;
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse("An error occurred", error);
		assertTrue("Organization was not updated", updated);
	}

	public void testDeleteOrganization(){
		boolean deleted = false;
		boolean error = false;
		try{
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);

			deleted = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(org);
		}
		catch(FactoryException fe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse("An error occurred", error);
		assertTrue("Did not delete org", deleted);
		logger.info("Deleted organizations " + testOrgName);
	}

}