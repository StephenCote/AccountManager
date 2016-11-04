package org.cote.accountmanager.data.security;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class KeyService {
	public static final Logger logger = Logger.getLogger(KeyService.class.getName());

	public static SecurityBean promote(SecurityType sec) throws ArgumentException{
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
		bean.setOrganizationId(sec.getOrganizationId());
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
			SecurityBean asymmKey = getAsymmetricKeyById(bean.getAsymmetricKeyId(),bean.getOrganizationId());
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
	public static boolean deleteKeys(long organizationId){
		boolean out_bool = false;
		try {
			Factories.getSymmetricKeyFactory().deleteByOrganization(organizationId);
			Factories.getAsymmetricKeyFactory().deleteByOrganization(organizationId);
			out_bool = true;
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
		
		return out_bool;
	}
	public static SecurityBean getSymmetricKeyById(long id,long organizationId){
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getSymmetricKeyFactory().getById(id, organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
		
		return bean;
	}
	public static SecurityBean getSymmetricKeyByObjectId(String id,long organizationId){
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getSymmetricKeyFactory().getByObjectId(id, organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
		
		return bean;
	}
	public static SecurityBean getPrimarySymmetricKey(long organizationId) {
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getSymmetricKeyFactory().getPrimaryOrganizationKey(organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}

		return bean;
	}	
	public static SecurityBean getPrimarySymmetricKey(UserType user) {
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getSymmetricKeyFactory().getPrimaryPersonalKey(user);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}

		return bean;
	}	
	public static SecurityBean newOrganizationSymmetricKey(long organizationId, boolean primaryKey) throws ArgumentException {
		return newOrganizationSymmetricKey(null,null,organizationId,primaryKey);
	}
	public static SecurityBean newOrganizationSymmetricKey(String bulkSessionId, SecurityBean asymmetricKey, long organizationId, boolean primaryKey) throws ArgumentException {
		return newSymmetricKey(bulkSessionId,asymmetricKey,null,organizationId,primaryKey,true,false);
	}
	public static SecurityBean newPersonalSymmetricKey(String bulkSessionId, SecurityBean asymmetricKey, UserType user, boolean primaryKey) throws ArgumentException {
		return newSymmetricKey(bulkSessionId,asymmetricKey,user,user.getOrganizationId(),primaryKey,false,false);
	}
	public static SecurityBean newPersonalSymmetricKey(UserType user, boolean primaryKey) throws ArgumentException {
		return newPersonalSymmetricKey(null,null,user,primaryKey);
	}
	public static SecurityBean getAsymmetricKeyById(long id,long organizationId){
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getAsymmetricKeyFactory().getById(id, organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
		
		return bean;
	}
	public static SecurityBean getAsymmetricKeyByObjectId(String id,long organizationId){
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getAsymmetricKeyFactory().getByObjectId(id, organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
		
		return bean;
	}
	public static SecurityBean getPrimaryAsymmetricKey(long organizationId) {
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getAsymmetricKeyFactory().getPrimaryOrganizationKey(organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}

		return bean;
	}	
	public static SecurityBean getPrimaryAsymmetricKey(UserType user) {
		SecurityBean bean = null;
		try {
			SecurityType sec = Factories.getAsymmetricKeyFactory().getPrimaryPersonalKey(user);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}

		return bean;
	}	
	public static SecurityBean newOrganizationAsymmetricKey(long organizationId, boolean primaryKey) throws ArgumentException {
		return newOrganizationAsymmetricKey(null,null,organizationId,primaryKey);
	}
	public static SecurityBean newOrganizationAsymmetricKey(String bulkSessionId, SecurityBean symmetricKey, long organizationId, boolean primaryKey) throws ArgumentException {
		return newAsymmetricKey(bulkSessionId,symmetricKey,null,organizationId,primaryKey,true,false);
	}
	public static SecurityBean newPersonalAsymmetricKey(String bulkSessionId, SecurityBean symmetricKey, UserType user, boolean primaryKey) throws ArgumentException {
		return newAsymmetricKey(bulkSessionId,symmetricKey,user,user.getOrganizationId(),primaryKey,false,false);
	}
	public static SecurityBean newPersonalAsymmetricKey(UserType user, boolean primaryKey) throws ArgumentException {
		return newPersonalAsymmetricKey(null,null,user,primaryKey);
	}
	
	private static SecurityBean newSymmetricKey(String bulkSessionId, SecurityBean asymmetricKey, UserType owner, long organizationId, boolean primaryKey, boolean organizationKey, boolean globalKey) throws ArgumentException {
		SecurityBean sec = new SecurityBean();
		sec.setOrganizationKey(organizationKey);
		sec.setGlobalKey(globalKey);
		sec.setPrimaryKey(primaryKey);
		SecurityType lastPrimary = null;
		if(primaryKey){
			//logger.info("Checking for existing primary key");
			if(owner !=null) lastPrimary = getPrimarySymmetricKey(owner);
			else if(organizationKey) lastPrimary = getPrimarySymmetricKey(organizationId);
			if(lastPrimary != null) sec.setPreviousKeyId(lastPrimary.getId());
			//else logger.info("No existing primary key found");
		}

		if(asymmetricKey != null){
			if(asymmetricKey.getSecretKey() == null) throw new ArgumentException("Secret key was specified but is null");
			//throw new ArgumentException("Not implemented");
			
			sec.setPublicKey(asymmetricKey.getPublicKey());
			sec.setEncryptCipherKey(true);
			sec.setAsymmetricKeyId(asymmetricKey.getId());
			
		}
		sec.setOrganizationId(organizationId);
		sec.setOwnerId((owner != null ? owner.getId() : 0L));
		sec.setNameType(NameEnumType.SECURITY);

		try{
			if(
				SecurityFactory.getSecurityFactory().generateSecretKey(sec)
			){
				if(bulkSessionId != null) BulkFactories.getBulkFactory().createBulkEntry(bulkSessionId, FactoryEnumType.SYMMETRICKEY, sec);
				else if(Factories.getSymmetricKeyFactory().add(sec)){
					SecurityType secm = Factories.getSymmetricKeyFactory().getByObjectId(sec.getObjectId(), sec.getOrganizationId());
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
					Factories.getSymmetricKeyFactory().update(lastPrimary);
				}
			}
		}
		catch(FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
		//if(sec != null) sec.setPublicKey(null);
		return sec;
	}
	private static SecurityBean newAsymmetricKey(String bulkSessionId, SecurityBean symmetricKey, UserType owner, long organizationId, boolean primaryKey, boolean organizationKey, boolean globalKey) throws ArgumentException {
		SecurityBean sec = new SecurityBean();
		sec.setOrganizationKey(organizationKey);
		sec.setGlobalKey(globalKey);
		sec.setPrimaryKey(primaryKey);
		SecurityType lastPrimary = null;
		if(primaryKey){
			//logger.info("Checking for existing primary key");
			if(owner !=null) lastPrimary = getPrimaryAsymmetricKey(owner);
			else if(organizationKey) lastPrimary = getPrimaryAsymmetricKey(organizationId);
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
		sec.setOrganizationId(organizationId);
		sec.setOwnerId((owner != null ? owner.getId() : 0L));
		sec.setNameType(NameEnumType.SECURITY);

		try{
			if(
				SecurityFactory.getSecurityFactory().generateKeyPair(sec)
			){
				if(bulkSessionId != null){
					BulkFactories.getBulkFactory().createBulkEntry(bulkSessionId, FactoryEnumType.ASYMMETRICKEY, sec);
				}
				else if(Factories.getAsymmetricKeyFactory().add(sec)){
					SecurityType secm = Factories.getAsymmetricKeyFactory().getByObjectId(sec.getObjectId(), sec.getOrganizationId());
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
					if(bulkSessionId != null) BulkFactories.getBulkAsymmetricKeyFactory().update(lastPrimary);
					else Factories.getAsymmetricKeyFactory().update(lastPrimary);
				}
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
		} 
		//if(sec != null) sec.setPublicKey(null);
		return sec;
	}
}
