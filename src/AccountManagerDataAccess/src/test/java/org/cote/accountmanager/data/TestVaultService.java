package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.VaultService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;

public class TestVaultService extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestVaultService.class.getName());
	
	private AccountType getAccount(String name){
		
		AccountType qaAccount = null;
		try{
			qaAccount = Factories.getAccountFactory().getAccountByName(name, Factories.getDevelopmentOrganization());
	
			if(qaAccount == null){
				qaAccount = Factories.getAccountFactory().newAccount(name, AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
				Factories.getAccountFactory().addAccount(qaAccount);
				qaAccount = Factories.getAccountFactory().getAccountByName(name, Factories.getDevelopmentOrganization());
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
	public void TestVaultData(){
		UserType qaUser = getUser("QA Vault User", "password");
		String vaultName = "QA Vault Keys";
		VaultService vs = new VaultService(qaUser,"./VaultExp",vaultName);
		String dataName = "QA Vault Data - " + UUID.randomUUID().toString();
		SecurityBean cipherBean = new SecurityBean();
		SecurityFactory.getSecurityFactory().generateSecretKey(cipherBean);
		try {
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(qaUser, "VaultExamples", qaUser.getHomeDirectory(), qaUser.getOrganization());
			vs.initialize();
			if(vs.getIsImproved() == false) vs.createVault("password");
			else vs.setPassword("password");
			String dataStr = "This is the vaulted data";
			DataType data = Factories.getDataFactory().newData(qaUser, dir);
			data.setName(dataName);
			data.setMimeType("text/plain");
			DataUtil.setCipher(data, cipherBean);
			DataUtil.setPassword(data, "data password");
			vs.setVaultBytes(data, dataStr.getBytes());
					//vs.newVaultData(dataName,dir,"text/plain",dataStr.getBytes(), false);
			
			
			assertNotNull("Vault data was null",data);
			assertTrue("Failed to add data",Factories.getDataFactory().addData(data));
			
			vs.clearCache();
			
			DataType dataCheck = Factories.getDataFactory().getDataByName(dataName, dir);
			assertNotNull("Data was null",dataCheck);

			DataUtil.setPassword(dataCheck, "data password");
			DataUtil.setCipher(dataCheck, cipherBean);
			logger.info("Vaulted data value: " + new String(DataUtil.getValue(dataCheck)));
			byte[] decBytes = vs.extractVaultData(dataCheck);
			logger.info("Extracted data value: " + new String(decBytes));
			
			byte[] newData = "Updated vault data".getBytes();
			DataUtil.setPassword(dataCheck, "data password");
			DataUtil.setCipher(dataCheck, cipherBean);
			dataCheck.setDescription("Updated description");
			vs.updateImprovedData(dataCheck, newData);
			//Factories.getDataFactory().updateData(dataCheck);
			logger.info("Making sure multi-enciphered data survives an update");
			DataType dataCheck2 = Factories.getDataFactory().getDataByName(dataName, dir);
			assertNotNull("Data was null",dataCheck2);

			DataUtil.setPassword(dataCheck2, "data password");
			DataUtil.setCipher(dataCheck2, cipherBean);
			logger.info("Vaulted data value: " + new String(DataUtil.getValue(dataCheck2)));
			byte[] decBytes2 = vs.extractVaultData(dataCheck2);
			logger.info("Extracted data value: " + new String(decBytes2));
			
			logger.info("Making sure a details-only data survives an update");
			DataType dataCheck3 = Factories.getDataFactory().getDataByName(dataName, true, dir);
			dataCheck3.setDescription("Updated description #2");
			Factories.getDataFactory().updateData(dataCheck3);

			logger.info("Making sure an unread detailed data survives an update");
			
			dataCheck3 = Factories.getDataFactory().getDataByName(dataName, false, dir);
			dataCheck3.setDescription("Updated description #3");
			Factories.getDataFactory().updateData(dataCheck3);
			
			dataCheck3 = Factories.getDataFactory().getDataByName(dataName, false, dir);
			DataUtil.setPassword(dataCheck3, "data password");
			DataUtil.setCipher(dataCheck3, cipherBean);
			logger.info("Vaulted data value: " + new String(DataUtil.getValue(dataCheck3)));
			byte[] decBytes3 = vs.extractVaultData(dataCheck3);
			logger.info("Extracted data value: " + new String(decBytes3));

			
			
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	@Test
	public void TestVaultActiveKey(){
		UserType qaUser = getUser("QA Vault User", "password");
		String vaultName = "QA Vault Keys";
		VaultService vs = new VaultService(qaUser,"./VaultExp",vaultName);
		try {
			
			vs.initialize();
			if(vs.getIsImproved() == false) vs.createVault("password");
			else vs.setPassword("password");
			
			assertTrue("Failed to create new active key",vs.newActiveKey());
			
			String dataToEnc = "Blah blah blah";
			String activeKeyId = vs.getActiveKeyId();
			byte[] enc = vs.encipher(dataToEnc.getBytes());
			logger.info("Encrypted " + dataToEnc + " with key " + activeKeyId);
			vs.clearCache();
			logger.info("Resetting Active Key to test cipher protection");
			vs.setActiveKey(activeKeyId);
			byte[] dec = vs.decipher(enc);
			String dataWasEnc = new String(dec);
			logger.info("Was encrypted: " + dataWasEnc);
			
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	/*
	@Test
	public void TestCreateVault(){
		UserType qaUser = getUser("QA Vault User", "password");
		String vaultName = "QA Vault - " + UUID.randomUUID().toString();
		String vaultName2 = "QA Pwd Vault - " + UUID.randomUUID().toString();
		
		VaultService vs = new VaultService(qaUser,"./VaultExp",vaultName);
		try {
			vs.initialize();
			assertFalse("Vault should not be marked as improved",vs.getIsImproved());
			assertTrue("Failed to create vault",vs.createVault());
			assertTrue("Vault should be marked as improved",vs.getIsImproved());
			
			VaultService vs2 = new VaultService(qaUser,"./VaultExp",vaultName);
			vs2.initialize();
			
			assertTrue("Vault should be marked as improved",vs2.getIsImproved());
			
			VaultService vs3 = new VaultService(qaUser,"./VaultExp",vaultName2);
			vs3.initialize();
			vs3.createVault("password");
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}
	