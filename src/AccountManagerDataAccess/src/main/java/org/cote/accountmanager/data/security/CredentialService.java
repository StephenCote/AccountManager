package org.cote.accountmanager.data.security;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.SecurityUtil;

public class CredentialService {
	public static final Logger logger = Logger.getLogger(CredentialService.class.getName());
	public static CredentialType getPrimaryCredential(NameIdType obj) {
		return getPrimaryCredential(obj,CredentialEnumType.UNKNOWN,false);
	}
	public static CredentialType getActivePrimaryCredential(NameIdType obj, CredentialEnumType credType) {
		return getPrimaryCredential(obj, credType, true);
	}
	public static CredentialType getPrimaryCredential(NameIdType obj, CredentialEnumType credType, boolean requireActive) {
		CredentialType cred = null;
		try {
			cred = Factories.getCredentialFactory().getPrimaryCredential(obj,credType,requireActive);

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cred;
	}
	public static boolean validateTokenCredential(NameIdType object, CredentialType credential, String token){
		return validatePasswordCredential(object, credential, token);
	}
	public static boolean validatePasswordCredential(NameIdType object, CredentialType credential, String password){
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHENTICATE, "Validate hashed password", AuditEnumType.valueOf(object.getNameType().toString()), object.getName() + " (#" + object.getId() + ")");
		AuditService.targetAudit(audit, AuditEnumType.CREDENTIAL, credential.getCredentialType().toString() + "(# " + credential.getId() + ")");
		boolean out_bool = false;
		/// If the credential reference type and id don't match the object, error out
		///
		if(object.getId().compareTo(credential.getReferenceId()) != 0 || FactoryEnumType.valueOf(object.getNameType().toString()) != credential.getReferenceType()){
			AuditService.denyResult(audit, "Specified credential does not match the specified object");
			return out_bool;
		}
		switch(credential.getCredentialType()){
			case TOKEN:
			case HASHED_PASSWORD:
			case ENCRYPTED_PASSWORD:
				out_bool = comparePasswordCredential(credential, password);
				break;
			case LEGACY_PASSWORD:
				out_bool = compareLegacyPasswordCredential(credential, password);
				break;
			default:
				logger.error("Not implemented");
				break;
		}
		if(out_bool) AuditService.validateResult(audit, "Validated");
		else AuditService.invalidateResult(audit, "Not validated");
		return out_bool;
		
	}
	
	/// throw-away for migration
	///
	public static CredentialType newLegacyPasswordCredential(UserType user, String password, boolean isPasswordHashed){
		CredentialType cred = new CredentialType();
		cred.setCredentialType(CredentialEnumType.LEGACY_PASSWORD);
		cred.setNameType(NameEnumType.CREDENTIAL);
		cred.setOrganizationId(user.getOrganizationId());
		cred.setReferenceId(user.getId());
		cred.setReferenceType(FactoryEnumType.USER);
		cred.setVaulted(false);
		try {
			cred.setCredential((isPasswordHashed ? password.getBytes("UTF-8") : SecurityUtil.getSaltedDigest(password).getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cred;
	}
	/// Legacy password mechanism was to hash the password first, then lookup the user by the username and the hash
	/// New system is to obtain the credential for the object, then run it through the validator
	/// This is throw-away to migrate off the legacy system - just take the hashed password in, lookup the user, and call it good
	///
	public static boolean compareLegacyPasswordCredential(CredentialType credential, String password){
		boolean out_bool = false;
		if(credential.getReferenceType() != FactoryEnumType.USER){
			logger.error("Unsupported legacy type");
			return out_bool;
		}
		String passwordHash = new String(credential.getCredential());
		logger.info("Hash = " + passwordHash);
		try {
			List<NameIdType> users = Factories.getUserFactory().list(new QueryField[]{QueryFields.getFieldId(credential.getReferenceId()), QueryFields.getFieldPassword(passwordHash)}, credential.getOrganizationId());
			if (users.size() == 1){
				out_bool = true;
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return out_bool;
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
		if(cred.length == 0){
			logger.error("Credential value is empty");
			return outBytes;
		}
		if(credential.getEnciphered()){
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
		boolean out_bool = false;
		if(credential.getCredentialType() != CredentialEnumType.HASHED_PASSWORD && credential.getCredentialType() != CredentialEnumType.ENCRYPTED_PASSWORD && credential.getCredentialType() != CredentialEnumType.TOKEN){
			logger.error("Invalid credential type for a the specified validation");
			return out_bool;
		}
		byte[] cred = extractCredential(credential);

		if(cred.length == 0){
			logger.error("Credential is invalid");
			return out_bool;
		}

		try {
			byte[] pwdBytes = password.getBytes("UTF-8");
			if(credential.getCredentialType() == CredentialEnumType.HASHED_PASSWORD){
				byte[] matchHash = SecurityUtil.getDigest(pwdBytes, credential.getSalt());
				out_bool = Arrays.areEqual(cred, matchHash);
			}
			else if(credential.getCredentialType() == CredentialEnumType.ENCRYPTED_PASSWORD || credential.getCredentialType() == CredentialEnumType.TOKEN){
				out_bool = Arrays.areEqual(cred, pwdBytes);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_bool;
	}
	public static CredentialType newHashedPasswordCredential(UserType owner, NameIdType targetObject, String password, boolean primary, boolean vaulted){
		return newHashedPasswordCredential(null,owner,targetObject,password,primary, vaulted);
	}
	public static CredentialType newHashedPasswordCredential(String bulkSessionId, UserType owner, NameIdType targetObject, String password, boolean primary, boolean vaulted){

		byte[] pwdBytes = new byte[0];
		try {
			pwdBytes = password.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newCredential(CredentialEnumType.HASHED_PASSWORD, bulkSessionId, owner, targetObject, pwdBytes, primary, true, vaulted);
	}
	
	public static CredentialType newTokenCredential(UserType owner, NameIdType targetObject, String token, boolean primary){
		return newTokenCredential(null,owner,targetObject,token,primary);
	}
	public static CredentialType newTokenCredential(String bulkSessionId, UserType owner, NameIdType targetObject, String token, boolean primary){

		byte[] pwdBytes = new byte[0];
		try {
			pwdBytes = token.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newCredential(CredentialEnumType.TOKEN, bulkSessionId, owner, targetObject, pwdBytes, primary, true, false);
	}
	
	/// TODO: At the moment, a bulk credential requires a synchronous key insertion
	///
	///
	public static CredentialType newCredential(CredentialEnumType credType, String bulkSessionId, UserType owner, NameIdType targetObject, byte[] credBytes, boolean primary, boolean encrypted, boolean vaulted){
		CredentialType cred = null;
		CredentialType lastPrimary = null;
		byte[] useCredBytes = credBytes;
		try {

			cred = Factories.getCredentialFactory().newCredential(owner, targetObject);
			cred.setPrimary(primary);
			if(primary){
				lastPrimary = Factories.getCredentialFactory().getPrimaryCredential(targetObject,credType,false);
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
			if(encrypted){
				SecurityBean bean = KeyService.newPersonalAsymmetricKey(bulkSessionId,null,owner, false);
				cred.setKeyId(bean.getObjectId());
				//logger.info("Bean has bytes: " + (bean.getPublicKeyBytes() != null) + " / and key = " + (bean.getPublicKey() != null));
				useCredBytes = SecurityUtil.encrypt(bean, useCredBytes);
				if(useCredBytes.length == 0) throw new FactoryException("Invalid encrypted credential");
				cred.setEnciphered(true);
			}
			
			cred.setCredential(useCredBytes);
			
			if(bulkSessionId != null) BulkFactories.getBulkFactory().createBulkEntry(bulkSessionId, FactoryEnumType.CREDENTIAL, cred);
			else if(Factories.getCredentialFactory().add(cred)){
				cred = Factories.getCredentialFactory().getByObjectId(cred.getObjectId(),cred.getOrganizationId());
			}
			else{
				logger.error("Failed to add credential");
			}
			if(lastPrimary != null){
				lastPrimary.setPrimary(false);
				Factories.getCredentialFactory().update(lastPrimary);
			}

		} catch (ArgumentException | FactoryException e) {
			logger.error(e.getMessage());
		} 
		
		return cred;

	}
	
}
