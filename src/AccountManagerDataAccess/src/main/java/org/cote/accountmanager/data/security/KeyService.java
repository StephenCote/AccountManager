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
package org.cote.accountmanager.data.security;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.AsymmetricKeyFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.SymmetricKeyFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class KeyService {
	public static final Logger logger = LogManager.getLogger(KeyService.class);
	
	/// 2017/06/26 - TODO: Reconsider how key caching is handled
	///
	private static final Map<Long,SecurityBean> primaryUserSymmetricKeys = new HashMap<>();
	private static final Map<Long,SecurityBean> primaryOrganizationSymmetricKeys = new HashMap<>();
	private static final Map<String,SecurityBean> symmetricKeys = new HashMap<>();
	private static final int maximumCacheSize = 1000;
	private static final int maximumCacheAgeSeconds = 360;
	private static long lastSymmetricCacheRefresh = 0L;
	private static long lastOrgSymmetricCacheRefresh = 0L;
	private static long lastUserSymmetricCacheRefresh = 0L;
	
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
			sf.setSecretKey(bean, bean.getCipherKey(),  bean.getCipherIV(), false);
		}
		return bean;
	}
	public static String reportCacheSize(){
		return "Key Service Cache Report\n"
			+ "primaryUserSymmetricKeys\t" + primaryUserSymmetricKeys.keySet().size()
			+ "\nprimaryOrganizationSymmetricKeys\t" + primaryOrganizationSymmetricKeys.keySet().size()
			+ "\nsymmetricKeys\t" + symmetricKeys.keySet().size() + "\n"
		;
	}
	public static boolean deleteKeys(long organizationId){
		boolean out_bool = false;
		try {
			((SymmetricKeyFactory)Factories.getFactory(FactoryEnumType.SYMMETRICKEY)).deleteByOrganization(organizationId);
			((AsymmetricKeyFactory)Factories.getFactory(FactoryEnumType.ASYMMETRICKEY)).deleteByOrganization(organizationId);
			out_bool = true;
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return out_bool;
	}
	public static SecurityBean getSymmetricKeyById(long id,long organizationId){
		SecurityBean bean = null;
		try {
			SecurityType sec = ((SymmetricKeyFactory)Factories.getFactory(FactoryEnumType.SYMMETRICKEY)).getById(id, organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return bean;
	}
	public static void checkPrimaryOrganizationSymmetricKeyCache(){
		long now = System.currentTimeMillis();
		if(primaryOrganizationSymmetricKeys.size() > maximumCacheSize || lastOrgSymmetricCacheRefresh < (now - (maximumCacheAgeSeconds * 1000))){
			lastOrgSymmetricCacheRefresh = now;
			primaryOrganizationSymmetricKeys.clear();
			logger.debug("Clearing organization symmetric key cache");
		}
	}
	public static void checkPrimaryUserSymmetricKeyCache(){
		long now = System.currentTimeMillis();
		if(primaryUserSymmetricKeys.size() > maximumCacheSize || lastUserSymmetricCacheRefresh < (now - (maximumCacheAgeSeconds * 1000))){
			lastUserSymmetricCacheRefresh = now;
			primaryUserSymmetricKeys.clear();
			logger.debug("Clearing user symmetric key cache");
		}
	}
	public static void checkSymmetricKeyCache(){
		long now = System.currentTimeMillis();
		if(symmetricKeys.size() > maximumCacheSize || lastSymmetricCacheRefresh < (now - (maximumCacheAgeSeconds * 1000))){
			lastSymmetricCacheRefresh = now;
			symmetricKeys.clear();
			logger.debug("Clearing symmetric key cache");
		}
	}
	public static SecurityBean getSymmetricKeyByObjectId(String id,long organizationId){
		SecurityBean bean = null;
		checkSymmetricKeyCache();
		if(symmetricKeys.containsKey(id)) return symmetricKeys.get(id);
		try {
			SecurityType sec = ((SymmetricKeyFactory)Factories.getFactory(FactoryEnumType.SYMMETRICKEY)).getByObjectId(id, organizationId);
			if(sec != null){
				bean = promote(sec);
				symmetricKeys.put(id, bean);
			}
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return bean;
	}
	public static SecurityBean getPrimarySymmetricKey(long organizationId) {
		SecurityBean bean = null;
		checkPrimaryOrganizationSymmetricKeyCache();
		if(primaryOrganizationSymmetricKeys.containsKey(organizationId)) return primaryOrganizationSymmetricKeys.get(organizationId);
		try {
			SecurityType sec = ((SymmetricKeyFactory)Factories.getFactory(FactoryEnumType.SYMMETRICKEY)).getPrimaryOrganizationKey(organizationId);
			if(sec != null){
				bean = promote(sec);
				primaryOrganizationSymmetricKeys.put(organizationId, bean);
			}
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		return bean;
	}	
	public static SecurityBean getPrimarySymmetricKey(UserType user) {
		SecurityBean bean = null;
		checkPrimaryUserSymmetricKeyCache();
		if(primaryUserSymmetricKeys.containsKey(user.getId())) return primaryUserSymmetricKeys.get(user.getId());
		try {
			SecurityType sec = ((SymmetricKeyFactory)Factories.getFactory(FactoryEnumType.SYMMETRICKEY)).getPrimaryPersonalKey(user);
			if(sec != null){
				bean = promote(sec);
				primaryUserSymmetricKeys.put(user.getId(), bean);
			}
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			SecurityType sec = ((AsymmetricKeyFactory)Factories.getFactory(FactoryEnumType.ASYMMETRICKEY)).getById(id, organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return bean;
	}
	public static SecurityBean getAsymmetricKeyByObjectId(String id,long organizationId){
		SecurityBean bean = null;
		try {
			SecurityType sec = ((AsymmetricKeyFactory)Factories.getFactory(FactoryEnumType.ASYMMETRICKEY)).getByObjectId(id, organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return bean;
	}
	public static SecurityBean getPrimaryAsymmetricKey(long organizationId) {
		SecurityBean bean = null;
		try {
			SecurityType sec = ((AsymmetricKeyFactory)Factories.getFactory(FactoryEnumType.ASYMMETRICKEY)).getPrimaryOrganizationKey(organizationId);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		return bean;
	}	
	public static SecurityBean getPrimaryAsymmetricKey(UserType user) {
		SecurityBean bean = null;
		try {
			SecurityType sec = ((AsymmetricKeyFactory)Factories.getFactory(FactoryEnumType.ASYMMETRICKEY)).getPrimaryPersonalKey(user);
			if(sec != null) bean = promote(sec);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
				else if(((SymmetricKeyFactory)Factories.getFactory(FactoryEnumType.SYMMETRICKEY)).add(sec)){
					SecurityType secm = ((SymmetricKeyFactory)Factories.getFactory(FactoryEnumType.SYMMETRICKEY)).getByObjectId(sec.getObjectId(), sec.getOrganizationId());
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
					((SymmetricKeyFactory)Factories.getFactory(FactoryEnumType.SYMMETRICKEY)).update(lastPrimary);
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
				else if(((AsymmetricKeyFactory)Factories.getFactory(FactoryEnumType.ASYMMETRICKEY)).add(sec)){
					SecurityType secm = ((AsymmetricKeyFactory)Factories.getFactory(FactoryEnumType.ASYMMETRICKEY)).getByObjectId(sec.getObjectId(), sec.getOrganizationId());
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
					if(bulkSessionId != null) ((INameIdFactory)Factories.getBulkFactory(FactoryEnumType.ASYMMETRICKEY)).update(lastPrimary);
					else ((AsymmetricKeyFactory)Factories.getFactory(FactoryEnumType.ASYMMETRICKEY)).update(lastPrimary);
				}
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		//if(sec != null) sec.setPublicKey(null);
		return sec;
	}
}
