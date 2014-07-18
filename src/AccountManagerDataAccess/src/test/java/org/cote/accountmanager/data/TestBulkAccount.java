package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.log4j.Logger;

import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.SecurityUtil;

import org.junit.Test;

public class TestBulkAccount extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestBulkAccount.class.getName());
	
	private AccountType getAccount(String name){
		
		DirectoryGroupType rootDir = null;
		AccountType qaAccount = null;
		try{
			rootDir = Factories.getGroupFactory().getRootDirectory(Factories.getDevelopmentOrganization());
			qaAccount = Factories.getAccountFactory().getAccountByName(name, rootDir);
	
			if(qaAccount == null){
				qaAccount = Factories.getAccountFactory().newAccount(name, AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, rootDir);
				Factories.getAccountFactory().addAccount(qaAccount);
				qaAccount = Factories.getAccountFactory().getAccountByName(name, rootDir);
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return qaAccount;
	}
	
	@Test
	public void TestAccountAuthZ(){
		AccountType qaAccount1 = getAccount("QA Account 1");
		AccountType qaAccount2 = getAccount("QA Account 2");
		AccountType qaAccount3 = getAccount("QA Account 3");
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
			dir = Factories.getGroupFactory().getCreateDirectory(testUser, qaDirName, testUser.getHomeDirectory(), Factories.getDevelopmentOrganization());
			art = Factories.getRoleFactory().getCreateAccountRole(testUser, "QA Roles", null);
			readerRole = Factories.getRoleFactory().getCreateAccountRole(testUser, "QA Reader Role", art);
			writerRole = Factories.getRoleFactory().getCreateAccountRole(testUser, "QA Writer Role", readerRole);
			deleterRole = Factories.getRoleFactory().getCreateAccountRole(testUser, "QA Deleter Role", writerRole);
			adminRole = Factories.getRoleFactory().getCreateAccountRole(testUser, "QA Admin Role", deleterRole);
			UserType adminUser = Factories.getUserFactory().getUserByName("Admin", Factories.getDevelopmentOrganization());
			
			/// Put qaAccount1 in readerRole Role
			AuthorizationService.authorizeAccountType(adminUser, qaAccount1, readerRole, true, false, false, false);

			/// Put qaAccount2 in the writerRole Role
			AuthorizationService.authorizeAccountType(adminUser, qaAccount2, writerRole, true, false, false, false);

			/// Put qaAccount3 in the deleterRole Role
			AuthorizationService.authorizeAccountType(adminUser, qaAccount3, deleterRole, true, false, false, false);

			
			/// Specify that  role can view the group
			AuthorizationService.authorizeRoleType(adminUser,readerRole,dir,true,false,false,false);
			AuthorizationService.authorizeRoleType(adminUser,writerRole,dir,false,true,false,false);
			AuthorizationService.authorizeRoleType(adminUser,deleterRole,dir,false,false,true,false);

			EffectiveAuthorizationService.rebuildPendingRoleCache();
			
			boolean canReaderRoleRead =  AuthorizationService.canViewGroup(readerRole, dir);
			boolean canReaderRoleWrite =  AuthorizationService.canChangeGroup(readerRole, dir);
			boolean canUser1Read = AuthorizationService.canViewGroup(qaAccount1,dir);
			boolean canUser1Write = AuthorizationService.canChangeGroup(qaAccount1,dir);
			boolean canUser1Delete = AuthorizationService.canDeleteGroup(qaAccount1,dir);
			boolean isUser1InReaderRole = EffectiveAuthorizationService.getIsAccountInEffectiveRole(readerRole, qaAccount1);
			
			logger.info("#1 Can Role View (should be true): " + canReaderRoleRead);
			logger.info("#1 Can Role Write (should be false): " + canReaderRoleWrite);
			logger.info("#1 Can Account View (should be true): " + canUser1Read);
			logger.info("#2 Can Account Write (should be false): " + canUser1Write);
			logger.info("#2 Can Account Delete (should be false): " + canUser1Delete);
			logger.info("#1 In Effective Role (should be true): " + isUser1InReaderRole);
			
			boolean canWriterRoleRead =  AuthorizationService.canViewGroup(writerRole, dir);
			boolean canWriterRoleWrite =  AuthorizationService.canChangeGroup(writerRole, dir);
			boolean canUser2Read = AuthorizationService.canViewGroup(qaAccount2,dir);
			boolean canUser2Write = AuthorizationService.canChangeGroup(qaAccount2,dir);
			boolean canUser2Delete = AuthorizationService.canDeleteGroup(qaAccount2,dir);
			boolean isUser2InReaderRole = EffectiveAuthorizationService.getIsAccountInEffectiveRole(readerRole, qaAccount2);
			boolean canWriterRoleDelete =  AuthorizationService.canDeleteGroup(writerRole, dir);
			
			logger.info("#2 Can Role View: (should be true): " + canWriterRoleRead);
			logger.info("#2 Can Role Write (should be true): " + canWriterRoleWrite);
			logger.info("#2 Can Role Delete (should be false): " + canWriterRoleDelete);
			logger.info("#1 Can Account View (should be true): " + canUser2Read);
			logger.info("#2 Can Account Write (should be true): " + canUser2Write);
			logger.info("#2 Can Account Delete (should be false): " + canUser2Delete);
			logger.info("#2 In Effective Role (should be true): " + isUser2InReaderRole);

			boolean canDeleterRoleRead =  AuthorizationService.canViewGroup(deleterRole, dir);
			boolean canDeleterRoleWrite =  AuthorizationService.canChangeGroup(deleterRole, dir);
			boolean canDeleterRoleDelete =  AuthorizationService.canDeleteGroup(deleterRole, dir);
			boolean canUser3Read = AuthorizationService.canViewGroup(qaAccount3,dir);
			boolean canUser3Write = AuthorizationService.canChangeGroup(qaAccount3,dir);

			boolean canUser3Delete = AuthorizationService.canDeleteGroup(qaAccount3,dir);
			boolean isUser3InReaderRole = EffectiveAuthorizationService.getIsAccountInEffectiveRole(readerRole, qaAccount3);

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
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
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
			AccountType new_account = Factories.getAccountFactory().newAccount("BulkAccount-" + guid, AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ACCOUNT, new_account);
			
			new_account = Factories.getAccountFactory().newAccount("Bulk Child 1", new_account,AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ACCOUNT, new_account);
			
			logger.info("Retrieving Bulk User");
			AccountType check = Factories.getAccountFactory().getAccountByName("BulkAccount-" + guid, new_account.getOrganization());
			assertNotNull("Failed user cache check",check);
			
			logger.info("Retrieving User By Id");
			check = Factories.getAccountFactory().getById(new_account.getId(), new_account.getOrganization());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			success = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}  catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertTrue("Success bit is false",success);
	}
	*/

}
	