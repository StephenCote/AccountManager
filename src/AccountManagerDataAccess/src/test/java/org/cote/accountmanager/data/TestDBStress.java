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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.PersonService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.junit.Test;


public class TestDBStress extends BaseDataAccessTest{

	@Test
	public void testOrgLoad(){
		int bukOrganizations = 5;
		int bulkUserCount = 1000;
		
		String testOrgAdminPassword = "password";
		logger.info("Creating bulk test organization");
		OrganizationType bulkOrg = getCreateOrganization(Factories.getDevelopmentOrganization(), "Stress Organization", testOrgAdminPassword);
		assertNotNull("Org is null", bulkOrg);
		logger.info("Creating unit test group org");
		OrganizationType groupOrg = getCreateOrganization(bulkOrg, "Stress Organization - " + System.currentTimeMillis(), testOrgAdminPassword);
		assertNotNull("Org is null", groupOrg);
		for(int i = 0; i < bukOrganizations; i++) {
			logger.info("Creating unit test org");
			OrganizationType instanceOrg = getCreateOrganization(bulkOrg, "Instance Organization " + (i + 1), testOrgAdminPassword);

			logger.info("Creating bulk user base");

			
			try{
				String sessionId = BulkFactories.getBulkFactory().newBulkSession();
				long average = 0L;
				for(int x = 0; x < bulkUserCount; x++) {
					long startTime = System.currentTimeMillis();
					String guid = UUID.randomUUID().toString();
					String userName = "BulkUser-" + guid;
					AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Bulk test", AuditEnumType.PERSON, userName);
					AuditService.targetAudit(audit, AuditEnumType.USER, userName);
					boolean created = PersonService.createUserAsPerson(audit, sessionId, userName, "password", userName + "@test.com", UserEnumType.DEVELOPMENT, UserStatusEnumType.NORMAL, instanceOrg.getId());
					if(created) AuditService.permitResult(audit, "Bulk test user created");
					else AuditService.invalidateResult(audit, "Failed to create test user");
					assertTrue("Failed to create new person user",created);
					long stopTime = System.currentTimeMillis();
					average += (stopTime - startTime);
					
				}
				logger.info("Average time to bulk prepare " + bulkUserCount + " bulk user-persons: " + (average / bulkUserCount) + " ms");
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
			}
			catch(FactoryException | ArgumentException | DataAccessException e) {
				logger.error(e);
			}
			
			
		}
		
		/*
		Connection connection = null;
		for(int i = 0; i < 50; i++){
			connection = ConnectionFactory.getInstance().getConnection();
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		*/
	}
	
	public OrganizationType getCreateOrganization(OrganizationType parentOrg, String orgName, String adminPassword){
		boolean error = false;
		OrganizationType new_org = null;
		try {
			OrganizationFactory org_factory = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
			new_org = org_factory.getByNameInParent(orgName,parentOrg.getId(), 0L);
			if(new_org != null) {
				return new_org;
			}
					
			new_org = new OrganizationType();
			new_org.setName(orgName);
			new_org.setNameType(NameEnumType.ORGANIZATION);
			new_org.setOrganizationType(OrganizationEnumType.DEVELOPMENT);
			
			new_org.setParentId(parentOrg.getId());

			if(org_factory.add(new_org)){
				new_org = org_factory.getByNameInParent(orgName,parentOrg.getId(), 0L);
				FactoryDefaults.setupOrganization(new_org, adminPassword);
			}
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			error = true;
		} 
		logger.info("Added " + orgName + " as " + new_org.getId());
		assertFalse("An error occurred", error);
		assertNotNull("New organization is null", new_org);
		return new_org;
	}

	@Test
	public void testConnectionRoundRobin(){
		Connection connection = null;
		for(int i = 0; i < 50; i++){
			connection = ConnectionFactory.getInstance().getConnection();
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
	}
	
	@Test
	public void testFactoryFlood(){
		for(int i = 0; i < 50; i++){
			AuditType audit = Factories.getAuditFactory().newAudit();
			audit.setAuditActionType(ActionEnumType.ADD);
			audit.setAuditSourceType(AuditEnumType.DATA);
			audit.setAuditTargetType(AuditEnumType.DATA);
			audit.setAuditResultType(ResponseEnumType.INFO);
			try {
				Factories.getAuditFactory().addAudit(audit);
			} catch (FactoryException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
			Factories.getAuditFactory().flushSpool();
		}
	}
}
