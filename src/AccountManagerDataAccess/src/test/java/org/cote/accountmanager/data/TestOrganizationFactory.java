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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;
public class TestOrganizationFactory extends BaseDataAccessTest{
	private static String testOrgName = null;
	public static final Logger logger = LogManager.getLogger(TestOrganizationFactory.class);


	@Test
	public void runTests(){
		testAddOrganization();
		testGetOrganization();
		testOrganizationCipher();
		testUpdateOrganization();
		testAddOrphanOrganization();
		testDeleteOrganization();
	}
	
	public void testAddOrganization(){
		boolean error = false;
	
		assertFalse("An error occurred", error);
		testOrgName = "Example " + System.currentTimeMillis();
		OrganizationType new_org = new OrganizationType();
		new_org.setName(testOrgName);
		new_org.setNameType(NameEnumType.ORGANIZATION);
		new_org.setOrganizationType(OrganizationEnumType.DEVELOPMENT);
		
		logger.info("Id: " + new_org.getId());
		logger.info("Ref Id: " + new_org.getReferenceId());
		try {
			OrganizationFactory org_factory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
			OrganizationType devOrg = Factories.getDevelopmentOrganization();
			new_org.setParentId(devOrg.getId());

			if(org_factory.add(new_org)){
				new_org = org_factory.getByNameInParent(testOrgName,devOrg.getId(), 0L);
				KeyService.newOrganizationAsymmetricKey(new_org.getId(), true);
				KeyService.newOrganizationSymmetricKey(new_org.getId(), true);
			}
		} catch (FactoryException e2) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e2);
			logger.error(e2.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		logger.info("Added " + testOrgName + " as " + new_org.getId());
		assertFalse("An error occurred", error);
	}
	
	public void testAddOrphanOrganization(){
		boolean error = false;

		String orgName = "Example " + System.currentTimeMillis();
		OrganizationType new_org = new OrganizationType();
		new_org.setName(orgName);
		new_org.setNameType(NameEnumType.ORGANIZATION);
		new_org.setOrganizationType(OrganizationEnumType.DEVELOPMENT);

		OrganizationType parentOrg = null;
		

		try {
			OrganizationFactory org_factory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
			OrganizationType devOrg = Factories.getDevelopmentOrganization();

			parentOrg = org_factory.getByNameInParent(testOrgName, devOrg.getId(),0L);
			assertNotNull("Test organization " + testOrgName + " is null in " + devOrg.getUrn(),parentOrg);
			new_org.setParentId(parentOrg.getId());

			if(org_factory.add(new_org)){
				new_org = org_factory.getByNameInParent(orgName, parentOrg.getId(), 0L);
				assertNotNull("New organization is null",new_org);
			}
			else{
				logger.error("Failed to add new organization " + testOrgName);
				new_org = null;
			}
		} catch (FactoryException e2) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e2);
			logger.error(e2.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		logger.info("Added " + testOrgName + " as " + new_org.getId());
		assertFalse("An error occurred", error);
	}
	
	public void testGetOrganization(){
		boolean error = false;
		
		OrganizationType new_org = null;
		try{
			logger.info("Read clean: " + testOrgName + " in " + Factories.getDevelopmentOrganization().getId());
			new_org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
			assertNotNull("Get organization " + testOrgName + "->" + Factories.getDevelopmentOrganization().getId() + " by name was null", new_org);
			logger.info("Read from cache by id: " + new_org.getId());
			new_org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(new_org.getId());
			assertNotNull("Get organization from cache by id was null", new_org);
			logger.info("Read from cache by name and parent");
			new_org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
			assertNotNull("Get oranization from cache by name was null null", new_org);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse(FactoryException.LOGICAL_EXCEPTION,error);

		
		logger.info("Id: " + new_org.getId());
		logger.info("Ref Id: " + new_org.getReferenceId());
		
	}
	
	
	public void testOrganizationCipher(){
		boolean error = false;

		
		OrganizationType new_org = null;
		try{
			logger.info("Read clean");
			new_org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse(FactoryException.LOGICAL_EXCEPTION,error);
		assertNotNull("Org is null", new_org);
		

		SecurityBean bean = KeyService.getPrimarySymmetricKey(new_org.getId()); 
				//OrganizationSecurity.getSecurityBean(new_org);
		String test_data = "This is some test data.";
		byte[] enc = SecurityUtil.encipher(bean, test_data.getBytes());
		assertTrue("Enciphered data is empty or null",enc != null && enc.length > 0);
		try{
			((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).clearCache();

			new_org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByNameInParent(testOrgName, Factories.getDevelopmentOrganization().getId(),0L);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		bean = KeyService.getPrimaryAsymmetricKey(new_org.getId());
				//OrganizationSecurity.getSecurityBean(new_org);
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
		catch(FactoryException fe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			
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