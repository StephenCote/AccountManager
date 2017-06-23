package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.services.VaultService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;
public class TestVaultService extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestVaultService.class);

	private boolean resetVault = true;

	@Test
	public void TestVaultData(){
		UserType qaUser = getUser("QA Vault User", "password");
		String vaultName = "QA Vault Keys";
		VaultService vs = new VaultService(qaUser,"./target/VaultExp",vaultName);

		String dataName = "QA Vault Data - " + UUID.randomUUID().toString();
		SecurityBean cipherBean = new SecurityBean();
		SecurityFactory.getSecurityFactory().generateSecretKey(cipherBean);
		try {

			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(qaUser, "VaultExamples", qaUser.getHomeDirectory(), qaUser.getOrganizationId());

			vs.initialize();
			if(resetVault){
				/// Cleanup anything already present
				vs.deleteVault();
				/// run initialize a second time after deleting the remnants
				///
				vs.initialize();
			}
			if(vs.getIsImproved() == false){
				boolean created = vs.createVault("password");
				logger.info("Created = " + created);
				assertTrue("Failed to create vault",created);
			}
			else vs.setPassword("password");
			String dataStr = "This is the vaulted data";
			DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(qaUser, dir.getId());
			data.setName(dataName);
			data.setMimeType("text/plain");
			DataUtil.setCipher(data, cipherBean);
			DataUtil.setPassword(data, "data password");
			vs.setVaultBytes(data, dataStr.getBytes());
					//vs.newVaultData(dataName,dir,"text/plain",dataStr.getBytes(), false);
			
			
			assertNotNull("Vault data was null",data);
			assertTrue("Failed to add data",((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data));
			
			vs.clearCache();
			
			DataType dataCheck = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, dir);
			assertNotNull("Data was null",dataCheck);

			DataUtil.setPassword(dataCheck, "data password");
			DataUtil.setCipher(dataCheck, cipherBean);
			logger.info("Vaulted data value: " + BinaryUtil.toBase64Str(DataUtil.getValue(dataCheck)));
			byte[] decBytes = vs.extractVaultData(dataCheck);
			logger.info("Extracted data value: " + new String(decBytes));
			
			byte[] newData = "Updated vault data".getBytes();
			DataUtil.setPassword(dataCheck, "data password");
			DataUtil.setCipher(dataCheck, cipherBean);
			dataCheck.setDescription("Updated description");
			vs.updateImprovedData(dataCheck, newData);
			//((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).updateData(dataCheck);
			logger.info("Making sure multi-enciphered data survives an update");
			DataType dataCheck2 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, dir);
			assertNotNull("Data was null",dataCheck2);

			DataUtil.setPassword(dataCheck2, "data password");
			DataUtil.setCipher(dataCheck2, cipherBean);
			logger.info("Vaulted data value: " + BinaryUtil.toBase64Str(DataUtil.getValue(dataCheck2)));
			byte[] decBytes2 = vs.extractVaultData(dataCheck2);
			logger.info("Extracted data value: " + new String(decBytes2));
			
			logger.info("Making sure a details-only data survives an update");
			DataType dataCheck3 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, true, dir);
			dataCheck3.setDescription("Updated description #2");
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(dataCheck3);

			logger.info("Making sure an unread detailed data survives an update");
			
			dataCheck3 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, false, dir);
			dataCheck3.setDescription("Updated description #3");
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(dataCheck3);
			
			dataCheck3 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, false, dir);
			DataUtil.setPassword(dataCheck3, "data password");
			DataUtil.setCipher(dataCheck3, cipherBean);
			logger.info("Vaulted data value: " + BinaryUtil.toBase64Str(DataUtil.getValue(dataCheck3)));
			byte[] decBytes3 = vs.extractVaultData(dataCheck3);
			logger.info("Extracted data value: " + new String(decBytes3));

			logger.info("Make sure vaulted data cannot be read outside of using the vault");
			DataType dataCheck4 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, false, dir);
			dataCheck4.setVaulted(false);
			DataUtil.setPassword(dataCheck4, "data password");
			DataUtil.setCipher(dataCheck4, cipherBean);
			
			String data4Str = new String(DataUtil.getValue(dataCheck4));
			assertFalse("Attempt to read data without the vault should have failed",data4Str.equals("Updated vault data"));
			//logger.info("Invalid data: '" + data4Str + "'");
			
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (UnsupportedEncodingException e) {
			
			logger.error("Error",e);
		} catch (DataException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
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
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (UnsupportedEncodingException e) {
			
			logger.error("Error",e);
		} catch (DataException e) {
			
			logger.error("Error",e);
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
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (UnsupportedEncodingException e) {
			
			logger.error("Error",e);
		} catch (DataException e) {
			
			logger.error("Error",e);
		}
	}
	*/
}
	