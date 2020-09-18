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
package org.cote.accountmanager.data.security;

import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.CredentialFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.VaultService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.SecurityUtil;

public class CredentialService {
	public static final Logger logger = LogManager.getLogger(CredentialService.class);
	private static final VaultService vaultService = new VaultService();
	private CredentialService(){
		
	}
	
	public static CredentialType getPrimaryCredential(NameIdType obj) {
		return getPrimaryCredential(obj,CredentialEnumType.UNKNOWN,false);
	}
	public static CredentialType getActivePrimaryCredential(NameIdType obj, CredentialEnumType credType) {
		return getPrimaryCredential(obj, credType, true);
	}
	public static CredentialType getPrimaryCredential(NameIdType obj, CredentialEnumType credType, boolean requireActive) {
		CredentialType cred = null;
		try {
			cred = ((CredentialFactory)Factories.getFactory(FactoryEnumType.CREDENTIAL)).getPrimaryCredential(obj,credType,requireActive);

		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		return cred;
	}
	public static boolean validateTokenCredential(NameIdType object, CredentialType credential, String token){
		return validatePasswordCredential(object, credential, token);
	}
	public static boolean validatePasswordCredential(NameIdType object, CredentialType credential, String password){
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHENTICATE, "Validate hashed password", AuditEnumType.valueOf(object.getNameType().toString()), object.getName() + " (#" + object.getId() + ")");
		AuditService.targetAudit(audit, AuditEnumType.CREDENTIAL, credential.getCredentialType().toString() + "(# " + credential.getId() + ")");
		boolean outBool = false;
		/// If the credential reference type and id don't match the object, error out
		///
		if(object.getId().compareTo(credential.getReferenceId()) != 0 || FactoryEnumType.valueOf(object.getNameType().toString()) != credential.getReferenceType()){
			AuditService.denyResult(audit, "Specified credential does not match the specified object");
			return outBool;
		}
		switch(credential.getCredentialType()){
			case TOKEN:
			case HASHED_PASSWORD:
			case ENCRYPTED_PASSWORD:
				outBool = comparePasswordCredential(credential, password);
				break;
			case LEGACY_PASSWORD:
				AuditService.denyResult(audit, "Legacy password credential is no longer supported");
				break;
			default:
				logger.error("Not implemented");
				break;
		}
		if(outBool) AuditService.validateResult(audit, "Validated");
		else AuditService.invalidateResult(audit, "Not validated");
		return outBool;
		
	}
	
	public static byte[] decryptCredential(CredentialType credential){
		AuditType audit = AuditService.beginAudit(ActionEnumType.OPEN, "Decrypted credential", AuditEnumType.CREDENTIAL, credential.getObjectId());
		AuditService.targetAudit(audit, AuditEnumType.CREDENTIAL, credential.getCredentialType().toString() + "(# " + credential.getId() + ")");

		byte[] outBytes = new byte[0];
		if(credential.getCredentialType() != CredentialEnumType.ENCRYPTED_IDENTITY && credential.getCredentialType() != CredentialEnumType.ENCRYPTED_PASSWORD){
			AuditService.denyResult(audit, "Credential type " + credential.getCredentialType().toString() + " is not supported");
			return outBytes;
		}
		outBytes = extractCredential(credential);
		if(outBytes.length > 0){
			AuditService.permitResult(audit, "Extracted credential");
		}
		else{
			AuditService.denyResult(audit, "Failed to extract credential");
		}
		return outBytes;
	}
	private static byte[] extractCredential(CredentialType credential){

		byte[] outBytes = new byte[0];
		byte[] cred = credential.getCredential();
		if(credential.getVaulted()) {
			UserType owner = null;
			try {
				owner = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getById(credential.getOwnerId(), credential.getOrganizationId());
				if(owner == null) {
					logger.error("Credential owner is invalid");
					return outBytes;
				}
				VaultBean vault = vaultService.getVaultByUrn(owner, credential.getVaultId());
				if(vault == null) {
					logger.error("Vault " + credential.getVaultId() + " is invalid");
					return outBytes;
				}
				outBytes = vaultService.extractVaultData(vault, credential);
			} catch (FactoryException | ArgumentException | DataException e) {
				logger.error("Error extracting credential value: " + e.getMessage());
			}

		}
		else if(credential.getEnciphered()){

			if(cred.length == 0){
				logger.error("Credential value is empty");
				return outBytes;
			}
			if(credential.getKeyId() == null){
				logger.error("Enciphered credential does not define a key");
				return outBytes;
			}
			SecurityBean bean = KeyService.getAsymmetricKeyByObjectId(credential.getKeyId(), credential.getOrganizationId());
			if(bean == null){
				logger.error("Credential key is invalid");
				return outBytes;
			}
			cred = SecurityUtil.decrypt(bean, cred);
			if(cred.length == 0){
				logger.error("Post-decrypted credential is invalid");
				return outBytes;
			}
			outBytes = cred;
		}
		else{
			outBytes = cred;
		}
		return outBytes;
	}
	
	public static boolean comparePasswordCredential(CredentialType credential, String password){
		boolean outBool = false;
		if(credential.getCredentialType() != CredentialEnumType.HASHED_PASSWORD && credential.getCredentialType() != CredentialEnumType.ENCRYPTED_PASSWORD && credential.getCredentialType() != CredentialEnumType.TOKEN){
			logger.error("Invalid credential type for a the specified validation");
			return outBool;
		}
		byte[] cred = extractCredential(credential);

		if(cred.length == 0){
			logger.error("Credential is invalid");
			return outBool;
		}

		try {
			byte[] pwdBytes = password.getBytes("UTF-8");
			if(credential.getCredentialType() == CredentialEnumType.HASHED_PASSWORD){
				byte[] matchHash = SecurityUtil.getDigest(pwdBytes, credential.getSalt());
				outBool = Arrays.areEqual(cred, matchHash);
			}
			else if(credential.getCredentialType() == CredentialEnumType.ENCRYPTED_PASSWORD || credential.getCredentialType() == CredentialEnumType.TOKEN){
				outBool = Arrays.areEqual(cred, pwdBytes);
			}
		} catch (UnsupportedEncodingException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBool;
	}
	public static CredentialType newHashedPasswordCredential(UserType owner, NameIdType targetObject, String password, boolean primary){
		return newHashedPasswordCredential(null,owner,targetObject,password,primary, null);
	}

	public static CredentialType newHashedPasswordCredential(UserType owner, NameIdType targetObject, String password, boolean primary, String vaultId){
		return newHashedPasswordCredential(null,owner,targetObject,password,primary, vaultId);
	}
	public static CredentialType newHashedPasswordCredential(String bulkSessionId, UserType owner, NameIdType targetObject, String password, boolean primary, String vaultId){

		byte[] pwdBytes = new byte[0];
		try {
			pwdBytes = password.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return newCredential(CredentialEnumType.HASHED_PASSWORD, bulkSessionId, owner, targetObject, pwdBytes, primary, true, vaultId);
	}
	
	public static CredentialType newTokenCredential(UserType owner, NameIdType targetObject, String token, boolean primary){
		return newTokenCredential(null,owner,targetObject,token,primary);
	}
	public static CredentialType newTokenCredential(String bulkSessionId, UserType owner, NameIdType targetObject, String token, boolean primary){

		byte[] pwdBytes = new byte[0];
		try {
			pwdBytes = token.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return newCredential(CredentialEnumType.TOKEN, bulkSessionId, owner, targetObject, pwdBytes, primary, true, null);
	}
	
	/// TODO: At the moment, a bulk credential requires a synchronous key insertion
	///
	///
	public static CredentialType newCredential(CredentialEnumType credType, String bulkSessionId, UserType owner, NameIdType targetObject, byte[] credBytes, boolean primary, boolean encrypted){
		return newCredential(credType, bulkSessionId, owner, targetObject, credBytes, primary, true, encrypted, null);
	}

	public static CredentialType newCredential(CredentialEnumType credType, String bulkSessionId, UserType owner, NameIdType targetObject, byte[] credBytes, boolean primary, boolean encrypted, String vaultId){
		return newCredential(credType, bulkSessionId, owner, targetObject, credBytes, primary, true, encrypted, vaultId);
	}
	public static CredentialType newCredential(CredentialEnumType credType, String bulkSessionId, UserType owner, NameIdType targetObject, byte[] credBytes, boolean primary, boolean unsetPrimary, boolean encrypted, String vaultId){

		CredentialType cred = null;
		CredentialType lastPrimary = null;
		byte[] useCredBytes = credBytes;
		try {

			cred = ((CredentialFactory)Factories.getFactory(FactoryEnumType.CREDENTIAL)).newCredential(owner, targetObject);
			cred.setPrimary(primary);
			if(primary && unsetPrimary){
				lastPrimary = ((CredentialFactory)Factories.getFactory(FactoryEnumType.CREDENTIAL)).getPrimaryCredential(targetObject,credType,false);
				if(lastPrimary != null){
					cred.setPreviousCredentialId(lastPrimary.getId());
				}
			}
			cred.setCredentialType(credType);
			if(credType == CredentialEnumType.HASHED_PASSWORD || credType == CredentialEnumType.SALT){
				cred.setSalt(SecurityUtil.getRandomSalt());
			}
			if(credType == CredentialEnumType.HASHED_PASSWORD){
				useCredBytes = SecurityUtil.getDigest(useCredBytes, cred.getSalt());
				if(useCredBytes.length == 0) throw new FactoryException("Invalid hashed credential");
			}
			
			/// NOTE: 'keyId' can be used for either being enciphered or vaulted, but not both
			/// By comparison, DataType has various key reference support while CredentialType just has Key and Vault, and Vault uses both
			///

			if(vaultId != null) {
				VaultBean vault = vaultService.getVaultByUrn(owner, vaultId);
				if(vault == null) {
					logger.error("Vault " + vaultId + " could not be accessed");
					return null;
				}
				vaultService.setVaultBytes(vault, cred, useCredBytes);
			}
			else{
				if(encrypted){
			
					SecurityBean bean = KeyService.newPersonalAsymmetricKey(bulkSessionId,null,owner, false);
					cred.setKeyId(bean.getObjectId());
					//logger.info("Bean has bytes: " + (bean.getPublicKeyBytes() != null) + " / and key = " + (bean.getPublicKey() != null));
					useCredBytes = SecurityUtil.encrypt(bean, useCredBytes);
	
					if(useCredBytes.length == 0) throw new FactoryException("Invalid encrypted credential");
					cred.setEnciphered(true);
				}
				cred.setCredential(useCredBytes);
			}

			if(bulkSessionId != null) BulkFactories.getBulkFactory().createBulkEntry(bulkSessionId, FactoryEnumType.CREDENTIAL, cred);
			else if(((CredentialFactory)Factories.getFactory(FactoryEnumType.CREDENTIAL)).add(cred)){
				cred = ((CredentialFactory)Factories.getFactory(FactoryEnumType.CREDENTIAL)).getByObjectId(cred.getObjectId(),cred.getOrganizationId());
			}
			else{
				logger.error("Failed to add credential");
			}
			if(lastPrimary != null){
				lastPrimary.setPrimary(false);
				((CredentialFactory)Factories.getFactory(FactoryEnumType.CREDENTIAL)).update(lastPrimary);
			}

		} catch (ArgumentException | FactoryException | UnsupportedEncodingException | DataException e) {
			logger.error(e.getMessage());
		} 
		
		return cred;

	}
	
}
