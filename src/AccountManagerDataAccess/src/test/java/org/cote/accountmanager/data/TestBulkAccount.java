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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestBulkAccount extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestBulkAccount.class);
	
	private AccountType getAccount(UserType owner, String name){
		
		DirectoryGroupType rootDir = null;
		AccountType qaAccount = null;
		try{
			rootDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getRootDirectory(owner.getOrganizationId());
			qaAccount = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(name, rootDir);
	
			if(qaAccount == null){
				qaAccount = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount(owner,name, AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, rootDir.getId());
				((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).add(qaAccount);
				qaAccount = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(name, rootDir);
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return qaAccount;
	}
	
	@Test
	public void TestAccountAuthZ(){
		AccountType qaAccount1 = getAccount(testUser,"QA Account 1");
		AccountType qaAccount2 = getAccount(testUser,"QA Account 2");
		AccountType qaAccount3 = getAccount(testUser,"QA Account 3");
		DirectoryGroupType dir = null;
		AccountRoleType art = null;
		AccountRoleType readerRole = null;
		AccountRoleType writerRole = null;
		AccountRoleType deleterRole = null;
		AccountRoleType adminRole = null;
		/*
		AccountRoleType parentArt = null;
		AccountRoleType child1Art = null;
		AccountRoleType child2Art = null;
		AccountRoleType grandChild1Art = null;
		*/
		try{
			
			String qaDirName = "QA Dir - Static";// + UUID.randomUUID().toString();
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, qaDirName, testUser.getHomeDirectory(), Factories.getDevelopmentOrganization().getId());
			art = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateAccountRole(testUser, "QA Roles", null);
			readerRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateAccountRole(testUser, "QA Reader Role", art);
			writerRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateAccountRole(testUser, "QA Writer Role", readerRole);
			deleterRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateAccountRole(testUser, "QA Deleter Role", writerRole);
			adminRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateAccountRole(testUser, "QA Admin Role", deleterRole);
			UserType adminUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", Factories.getDevelopmentOrganization().getId());
			
			/// Put qaAccount1 in readerRole Role
			AuthorizationService.authorizeType(adminUser, qaAccount1, readerRole, true, false, false, false);

			/// Put qaAccount2 in the writerRole Role
			AuthorizationService.authorizeType(adminUser, qaAccount2, writerRole, true, false, false, false);

			/// Put qaAccount3 in the deleterRole Role
			AuthorizationService.authorizeType(adminUser, qaAccount3, deleterRole, true, false, false, false);

			
			/// Specify that  role can view the group
			AuthorizationService.authorizeType(adminUser,readerRole,dir,true,false,false,false);
			AuthorizationService.authorizeType(adminUser,writerRole,dir,false,true,false,false);
			AuthorizationService.authorizeType(adminUser,deleterRole,dir,false,false,true,false);

			EffectiveAuthorizationService.rebuildPendingRoleCache();
			
			boolean canReaderRoleRead =  AuthorizationService.canView(readerRole, dir);
			boolean canReaderRoleWrite =  AuthorizationService.canChange(readerRole, dir);
			boolean canUser1Read = AuthorizationService.canView(qaAccount1,dir);
			boolean canUser1Write = AuthorizationService.canChange(qaAccount1,dir);
			boolean canUser1Delete = AuthorizationService.canDelete(qaAccount1,dir);
			boolean isUser1InReaderRole = EffectiveAuthorizationService.getIsActorInEffectiveRole(readerRole, qaAccount1);
			
			logger.info("#1 Can Role View (should be true): " + canReaderRoleRead);
			logger.info("#1 Can Role Write (should be false): " + canReaderRoleWrite);
			logger.info("#1 Can Account View (should be true): " + canUser1Read);
			logger.info("#2 Can Account Write (should be false): " + canUser1Write);
			logger.info("#2 Can Account Delete (should be false): " + canUser1Delete);
			logger.info("#1 In Effective Role (should be true): " + isUser1InReaderRole);
			
			boolean canWriterRoleRead =  AuthorizationService.canView(writerRole, dir);
			boolean canWriterRoleWrite =  AuthorizationService.canChange(writerRole, dir);
			boolean canUser2Read = AuthorizationService.canView(qaAccount2,dir);
			boolean canUser2Write = AuthorizationService.canChange(qaAccount2,dir);
			boolean canUser2Delete = AuthorizationService.canDelete(qaAccount2,dir);
			boolean isUser2InReaderRole = EffectiveAuthorizationService.getIsActorInEffectiveRole(readerRole, qaAccount2);
			boolean canWriterRoleDelete =  AuthorizationService.canDelete(writerRole, dir);
			
			logger.info("#2 Can Role View: (should be true): " + canWriterRoleRead);
			logger.info("#2 Can Role Write (should be true): " + canWriterRoleWrite);
			logger.info("#2 Can Role Delete (should be false): " + canWriterRoleDelete);
			logger.info("#1 Can Account View (should be true): " + canUser2Read);
			logger.info("#2 Can Account Write (should be true): " + canUser2Write);
			logger.info("#2 Can Account Delete (should be false): " + canUser2Delete);
			logger.info("#2 In Effective Role (should be true): " + isUser2InReaderRole);

			boolean canDeleterRoleRead =  AuthorizationService.canView(deleterRole, dir);
			boolean canDeleterRoleWrite =  AuthorizationService.canChange(deleterRole, dir);
			boolean canDeleterRoleDelete =  AuthorizationService.canDelete(deleterRole, dir);
			boolean canUser3Read = AuthorizationService.canView(qaAccount3,dir);
			boolean canUser3Write = AuthorizationService.canChange(qaAccount3,dir);

			boolean canUser3Delete = AuthorizationService.canDelete(qaAccount3,dir);
			boolean isUser3InReaderRole = EffectiveAuthorizationService.getIsActorInEffectiveRole(readerRole, qaAccount3);

			logger.info("#3 Can Role View: (should be true): " + canDeleterRoleRead);
			logger.info("#3 Can Role Write (should be true): " + canDeleterRoleWrite);
			logger.info("#3 Can Role Delete (should be true): " + canDeleterRoleDelete);
			logger.info("#3 Can Account View (should be true): " + canUser3Read);
			logger.info("#3 Can Account Write (should be true): " + canUser3Write);

			logger.info("#3 Can Account Delete (should be true): " + canUser3Delete);
			logger.info("#3 In Effective Role (should be true): " + isUser3InReaderRole);

			

		
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("QA Account is null",qaAccount1);
		assertNotNull("QA Dir si null",dir);
		assertNotNull("QA Role is null",art);
		
		
		
	}
	/*
	@Test
	public void TestBulkAccount(){
		boolean success = false;
		try{
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			AccountType new_account = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount("BulkAccount-" + guid, AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ACCOUNT, new_account);
			
			new_account = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount("Bulk Child 1", new_account,AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ACCOUNT, new_account);
			
			logger.info("Retrieving Bulk User");
			AccountType check = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName("BulkAccount-" + guid, new_account.getOrganization());
			assertNotNull("Failed user cache check",check);
			
			logger.info("Retrieving User By Id");
			check = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getById(new_account.getId(), new_account.getOrganization());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			success = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}  catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Success bit is false",success);
	}
	*/

}
	