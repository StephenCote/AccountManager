package org.cote.accountmanager.data.security;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class KeyService {
	public static final Logger logger = Logger.getLogger(KeyService.class.getName());

	private static SecurityBean promote(SecurityType sec) throws ArgumentException{
		SecurityBean bean = new SecurityBean();
		bean.setAsymmetricCipherKeySpec(sec.getAsymmetricCipherKeySpec());
		bean.setAsymmetricKeyId(sec.getAsymmetricKeyId());
		bean.setCipherIV(sec.getCipherIV());
		bean.setCipherKey(sec.getCipherKey());
		bean.setCipherKeySpec(sec.getCipherKeySpec());
		bean.setCipherProvider(sec.getCipherProvider());
		bean.setEncryptCipherKey(sec.getEncryptCipherKey());
		bean.setGlobalKey(sec.getGlobalKey());
		bean.setHashProvider(sec.getHashProvider());
		bean.setId(sec.getId());
		bean.setKeySize(sec.getKeySize());
		bean.setNameType(NameEnumType.SECURITY);
		bean.setObjectId(sec.getObjectId());
		bean.setOrganization(sec.getOrganization());
		bean.setOrganizationKey(sec.getOrganizationKey());
		bean.setOwnerId(sec.getOwnerId());
		bean.setPreviousKeyId(sec.getPreviousKeyId());
		bean.setPrimaryKey(sec.getPrimaryKey());
		bean.setRandomSeedLength(sec.getRandomSeedLength());
		bean.setReverseEncrypt(sec.getReverseEncrypt());
		bean.setSymmetricCipherKeySpec(sec.getSymmetricCipherKeySpec());
		bean.setSymmetricKeyId(sec.getSymmetricKeyId());
		bean.setPrivateKeyBytes(sec.getPrivateKeyBytes());
		bean.setPublicKeyBytes(sec.getPublicKeyBytes());
		bean.setEncryptedCipherIV(sec.getEncryptedCipherIV());
		bean.setEncryptedCipherKey(sec.getEncryptedCipherKey());
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		if(bean.getPrivateKeyBytes() != null && bean.getPrivateKeyBytes().length > 0) sf.setPrivateKey(bean, bean.getPrivateKeyBytes());
		if(bean.getPublicKeyBytes() != null && bean.getPublicKeyBytes().length > 0) sf.setPublicKey(bean, bean.getPublicKeyBytes());
		
		/// If the key is encrypted, the key isn't otherwise provided, and a key reference is included, then look up that key 
		if(bean.getEncryptCipherKey() == true && bean.getPrivateKey() == null && bean.getAsymmetricKeyId().compareTo(0L) != 0){
			SecurityBean asymmKey = getAsymmetricKeyById(bean.getAsymmetricKeyId(),bean.getOrganization());
			if(asymmKey != null) bean.setPrivateKey(asymmKey.getPrivateKey());
		}
		
		if(bean.getEncryptCipherKey() == true && bean.getEncryptedCipherIV() != null && bean.getEncryptedCipherIV().length > 0 && bean.getEncryptedCipherKey() != null && bean.getEncryptedCipherKey().length > 0){
			if(bean.getPrivateKey() == null) throw new ArgumentException("Private key is needed to decrypt the cipher key");
			sf.setSecretKey(bean, bean.getEncryptedCipherKey(), bean.getEncryptedCipherIV(), true);
			/// clear out the private key after using it
			///
			bean.setPrivateKey(null);
		}
		else if(bean.getCipherIV() != null && bean.getCipherIV().length > 0 && bean.getCipherKey() != null && bean.getCipherKey().length > 0){
			sf.setSecretKey(bean, bean.getCipherIV(),  bean.getCipherKey(), false);
		}
		return bean;
	}
	public static boolean deleteKeys(OrganizationType org){
		boolean out_bool = false;
		try {
			Factories.getSymmetricKeyFactory().deleteKeys(org);
			Factories.getAsymmetricKeyFactory().deleteKeys(org);
			out_bool = true;
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return out_bool;
	}
	public static SecurityBean getSymmetricKeyById(long id,OrganizationType org){
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getSymmetricKeyFactory().getById(id, org);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bean;
	}
	public static SecurityBean getSymmetricKeyByObjectId(String id,OrganizationType org){
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getSymmetricKeyFactory().getKeyByObjectId(id, org);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bean;
	}
	public static SecurityBean getPrimarySymmetricKey(OrganizationType org) {
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getSymmetricKeyFactory().getPrimaryOrganizationKey(org);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bean;
	}	
	public static SecurityBean getPrimarySymmetricKey(UserType user) {
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getSymmetricKeyFactory().getPrimaryPersonalKey(user);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bean;
	}	
	public static SecurityBean newOrganizationSymmetricKey(OrganizationType org, boolean primaryKey) throws ArgumentException {
		return newOrganizationSymmetricKey(null,org,primaryKey);
	}
	public static SecurityBean newOrganizationSymmetricKey(SecurityBean asymmetricKey, OrganizationType org, boolean primaryKey) throws ArgumentException {
		return newSymmetricKey(null,asymmetricKey,null,org,primaryKey,true,false);
	}
	public static SecurityBean newPersonalSymmetricKey(SecurityBean asymmetricKey, UserType user, boolean primaryKey) throws ArgumentException {
		return newSymmetricKey(null,asymmetricKey,user,user.getOrganization(),primaryKey,false,false);
	}
	public static SecurityBean newPersonalSymmetricKey(UserType user, boolean primaryKey) throws ArgumentException {
		return newPersonalSymmetricKey(null,user,primaryKey);
	}
	public static SecurityBean getAsymmetricKeyById(long id,OrganizationType org){
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getAsymmetricKeyFactory().getById(id, org);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bean;
	}
	public static SecurityBean getAsymmetricKeyByObjectId(String id,OrganizationType org){
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getAsymmetricKeyFactory().getKeyByObjectId(id, org);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bean;
	}
	public static SecurityBean getPrimaryAsymmetricKey(OrganizationType org) {
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getAsymmetricKeyFactory().getPrimaryOrganizationKey(org);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bean;
	}	
	public static SecurityBean getPrimaryAsymmetricKey(UserType user) {
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getAsymmetricKeyFactory().getPrimaryPersonalKey(user);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bean;
	}	
	public static SecurityBean newOrganizationAsymmetricKey(OrganizationType org, boolean primaryKey) throws ArgumentException {
		return newOrganizationAsymmetricKey(null,org,primaryKey);
	}
	public static SecurityBean newOrganizationAsymmetricKey(SecurityBean asymmetricKey, OrganizationType org, boolean primaryKey) throws ArgumentException {
		return newAsymmetricKey(null,null,null,org,primaryKey,true,false);
	}
	public static SecurityBean newPersonalAsymmetricKey(SecurityBean asymmetricKey, UserType user, boolean primaryKey) throws ArgumentException {
		return newAsymmetricKey(null,null,user,user.getOrganization(),primaryKey,false,false);
	}
	public static SecurityBean newPersonalAsymmetricKey(UserType user, boolean primaryKey) throws ArgumentException {
		return newPersonalAsymmetricKey(null,user,primaryKey);
	}
	
	private static SecurityBean newSymmetricKey(String bulkSessionId, SecurityBean asymmetricKey, UserType owner, OrganizationType org, boolean primaryKey, boolean organizationKey, boolean globalKey) throws ArgumentException {
		SecurityBean sec = new SecurityBean();
		sec.setOrganizationKey(organizationKey);
		sec.setGlobalKey(globalKey);
		sec.setPrimaryKey(primaryKey);
		SecurityType lastPrimary = null;
		if(primaryKey){
			//logger.info("Checking for existing primary key");
			if(owner !=null) lastPrimary = getPrimarySymmetricKey(owner);
			else if(organizationKey) lastPrimary = getPrimarySymmetricKey(org);
			if(lastPrimary != null) sec.setPreviousKeyId(lastPrimary.getId());
			//else logger.info("No existing primary key found");
		}

		if(asymmetricKey != null){
			if(asymmetricKey.getPublicKey() == null) throw new ArgumentException("Public key was specified but is null");
			sec.setPublicKey(asymmetricKey.getPublicKey());
			sec.setEncryptCipherKey(true);
			sec.setAsymmetricKeyId(asymmetricKey.getId());
		}
		sec.setOrganization(org);
		sec.setOwnerId((owner != null ? owner.getId() : 0L));
		sec.setNameType(NameEnumType.SECURITY);

		try{
			if(
				SecurityFactory.getSecurityFactory().generateSecretKey(sec)
			){
				if(bulkSessionId != null) BulkFactories.getBulkFactory().createBulkEntry(bulkSessionId, FactoryEnumType.SYMMETRICKEY, sec);
				else if(Factories.getSymmetricKeyFactory().addSymmetricKey(sec)){
					SecurityType secm = Factories.getSymmetricKeyFactory().getKeyByObjectId(sec.getObjectId(), sec.getOrganization());
					if(secm != null) sec.setId(secm.getId());
					else{
						logger.error("Failed to retrieve key");
						sec = null;
					}
				}
				else{
					logger.error("Failed to persist key");
				}
				if(lastPrimary != null){
					lastPrimary.setPrimaryKey(false);
					Factories.getSymmetricKeyFactory().updateSymmetricKey(lastPrimary);
				}
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		//if(sec != null) sec.setPublicKey(null);
		return sec;
	}
	private static SecurityBean newAsymmetricKey(String bulkSessionId, SecurityBean symmetricKey, UserType owner, OrganizationType org, boolean primaryKey, boolean organizationKey, boolean globalKey) throws ArgumentException {
		SecurityBean sec = new SecurityBean();
		sec.setOrganizationKey(organizationKey);
		sec.setGlobalKey(globalKey);
		sec.setPrimaryKey(primaryKey);
		SecurityType lastPrimary = null;
		if(primaryKey){
			//logger.info("Checking for existing primary key");
			if(owner !=null) lastPrimary = getPrimaryAsymmetricKey(owner);
			else if(organizationKey) lastPrimary = getPrimaryAsymmetricKey(org);
			if(lastPrimary != null) sec.setPreviousKeyId(lastPrimary.getId());
			//else logger.info("No existing primary key found");
		}
		/*
		if(symmetricKey != null){
			if(symmetricKey.getPublicKey() == null) throw new ArgumentException("Secret key was specified but is null");
			sec.setSecretKey(symmetricKey.getSecretKey());
			sec.setEncryptCipherKey(true);
			sec.setSymmetricKeyId(symmetricKey.getId());
		}
		*/
		sec.setOrganization(org);
		sec.setOwnerId((owner != null ? owner.getId() : 0L));
		sec.setNameType(NameEnumType.SECURITY);

		try{
			if(
				SecurityFactory.getSecurityFactory().generateKeyPair(sec)
			){
				if(bulkSessionId != null) BulkFactories.getBulkFactory().createBulkEntry(bulkSessionId, FactoryEnumType.ASYMMETRICKEY, sec);
				else if(Factories.getAsymmetricKeyFactory().addAsymmetricKey(sec)){
					SecurityType secm = Factories.getAsymmetricKeyFactory().getKeyByObjectId(sec.getObjectId(), sec.getOrganization());
					if(secm != null) sec.setId(secm.getId());
					else{
						logger.error("Failed to retrieve key");
						sec = null;
					}
				}
				else{
					logger.error("Failed to persist key");
				}
				if(lastPrimary != null){
					lastPrimary.setPrimaryKey(false);
					Factories.getAsymmetricKeyFactory().updateAsymmetricKey(lastPrimary);
				}
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		//if(sec != null) sec.setPublicKey(null);
		return sec;
	}
}
