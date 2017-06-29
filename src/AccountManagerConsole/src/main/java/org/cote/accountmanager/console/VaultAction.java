package org.cote.accountmanager.console;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.services.VaultService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.VaultType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

public class VaultAction {
	public static final Logger logger = LogManager.getLogger(VaultAction.class);
	
	public static void configureVaultCredential(String organizationPath, String adminPassword, String filePath, String credential){
		logger.info("Configure vault credential for organization " + organizationPath);
		if(credential.equalsIgnoreCase("random")){
			logger.info("Generating random credential");
			credential = UUID.randomUUID().toString();
		
		}
		try{
			
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(organizationPath);
			if(org == null){
				logger.error("Null organization");
				return;
			}
			
			UserType adminUser = SessionSecurity.login(FactoryDefaults.ADMIN_USER_NAME, CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId());
			if(adminUser == null){
				logger.error("Null admin");
				return;
			}
			
			UserType vaultUser = ((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).getByName("VaultUser", org.getId());
			if(vaultUser == null){
				logger.error("Null vault user");
				return;
			}
			((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).populate(vaultUser);
			VaultService service = new VaultService();
			
			service.createProtectedCredentialFile(vaultUser, filePath, credential.getBytes());
			
			SessionSecurity.logout(adminUser);
		}
		catch(ArgumentException | FactoryException e){
			logger.error(e);
		}

	}
	public static void deleteVault(UserType user, String vaultUrn){
		VaultService service = new VaultService();
		logger.info("Delete vault " + vaultUrn);
		VaultBean vault = service.getVaultByUrn(user, vaultUrn);
		if(vault == null){
			logger.error("Vault is null");
			return;
		}
		boolean deleted = false;
		try{
			deleted = service.deleteVault(vault);
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e);
		}
		
		if(deleted){
			logger.info("Deleted vault");
		}
		else{
			logger.error("Failed to delete vault");
		}
		
	}
	public static void deleteVault(String organizationPath, String adminPassword, String vaultName, String vaultPath){
		VaultService service = new VaultService();
		logger.info("Delete vault " + vaultName + " for organization " + organizationPath);
		try{
			
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(organizationPath);
			if(org == null){
				logger.error("Null organization");
				return;
			}
			
			UserType adminUser = SessionSecurity.login(FactoryDefaults.ADMIN_USER_NAME, CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId());
			if(adminUser == null){
				logger.error("Null admin");
				return;
			}
			
			UserType vaultUser = ((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).getByName("VaultUser", org.getId());
			if(vaultUser == null){
				logger.error("Null vault user");
				return;
			}
			((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).populate(vaultUser);
			VaultBean vault = service.loadVault(vaultPath, vaultName, true);
			if(vault == null){
				logger.error("Failed to load the vault");
				return;
			}
			// Provide a blank credential since this instance is only being used to delete
			//
			service.initialize(vault, new CredentialType());
			if(service.deleteVault(vault)){
				logger.info("Deleted vault " + vaultName);
			}
			else{
				logger.error("Failed to deleted vault " + vaultName);
			}
			
			
			
			SessionSecurity.logout(adminUser);
		}
		catch(ArgumentException | FactoryException e){
			logger.error(e);
		}

	}
	
	public static void listVaults(UserType owner){
		VaultService service = new VaultService();
		
		try {
			List<VaultType> vaults = service.listVaultsByOwner(owner);
			for(VaultType vault : vaults) logger.info(vault.getVaultName() + " (" + vault.getVaultDataUrn() + ")");
		} catch (FactoryException | ArgumentException | DataException e) {
			logger.error(e);
		}
		
	}
	
	public static void createVault(UserType owner, String vaultName, String vaultPath, String credentialPath){
		VaultService service = new VaultService();
		try {
			((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).populate(owner);
			
			if(service.createProtectedCredentialFile(owner, credentialPath, UUID.randomUUID().toString().getBytes()) == false){
				logger.error("Failed to create credential file");
				return;
			}
			CredentialType cred = service.loadProtectedCredential(credentialPath);
			if(cred == null){
				logger.error("Failed to load credential file");
				return;
			}
			VaultBean vault = service.newVault(owner, vaultPath, vaultName);
			service.setProtectedCredentialPath(vault, credentialPath);
			if(service.createVault(vault, cred) == false){
				logger.error("Failed to create vault");
				return;
			}
			VaultBean chkVault =  service.loadVault(vaultPath, vaultName, true);
			if(chkVault == null){
				logger.error("Failed to restore vault");
				return;
			}
			service.initialize(vault, cred);
			SecurityBean key = service.getVaultKey(vault);
			if(key == null){
				logger.error("Failed to extract and decrypt vault key");
				return;
			} 
			logger.info("Created vault " + vault.getVaultDataUrn());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		
	}

	public static void createVault(String organizationPath, String adminPassword, String vaultName, String vaultPath, String credentialPath){
		VaultService service = new VaultService();
		logger.info("Create vault " + vaultName + " for organization " + organizationPath);
				try{
			
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(organizationPath);
			if(org == null){
				logger.error("Null organization");
				return;
			}
			
			UserType adminUser = SessionSecurity.login(FactoryDefaults.ADMIN_USER_NAME, CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId());
			if(adminUser == null){
				logger.error("Null admin");
				return;
			}
			
			UserType vaultUser = ((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).getByName("VaultUser", org.getId());
			if(vaultUser == null){
				logger.error("Null vault user");
				return;
			}
			((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).populate(vaultUser);
			CredentialType cred = service.loadProtectedCredential(credentialPath);
			if(cred == null){
				logger.error("Failed to load the credential");
				return;
			}
			
			VaultBean vault = service.newVault(vaultUser, vaultPath, vaultName);
			if(service.createVault(vault, cred)){
				logger.info("Created vault at " + vault.getVaultKeyPath());
			}
			else{
				logger.error("Failed to create vault");
			}
			
			SessionSecurity.logout(adminUser);
		}
		catch(ArgumentException | FactoryException e){
			logger.error(e);
		}

	}

}
