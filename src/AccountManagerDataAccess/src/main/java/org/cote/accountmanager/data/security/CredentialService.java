package org.cote.accountmanager.data.security;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.SecurityType;
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
			case HASHED_PASSWORD:
				out_bool = validateHashedPasswordCredential(credential, password);
				break;
			case LEGACY_PASSWORD:
				out_bool = validateLegacyPasswordCredential(credential, password);
				break;
			case ENCRYPTED_PASSWORD:
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
		cred.setOrganization(user.getOrganization());
		cred.setReferenceId(user.getId());
		cred.setReferenceType(FactoryEnumType.USER);
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
	public static boolean validateLegacyPasswordCredential(CredentialType credential, String password){
		boolean out_bool = false;
		if(credential.getReferenceType() != FactoryEnumType.USER){
			logger.error("Unsupported legacy type");
			return out_bool;
		}
		String passwordHash = new String(credential.getCredential());
		logger.info("Hash = " + passwordHash);
		try {
			List<NameIdType> users = Factories.getUserFactory().getList(new QueryField[]{QueryFields.getFieldId(credential.getReferenceId()), QueryFields.getFieldPassword(passwordHash)}, credential.getOrganization());
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
	public static boolean validateHashedPasswordCredential(CredentialType credential, String password){
		boolean out_bool = false;
		if(credential.getCredentialType() != CredentialEnumType.HASHED_PASSWORD){
			logger.error("Invalid credential type for a hashed password validation");
			return out_bool;
		}
		byte[] hash = credential.getCredential();
		if(hash.length == 0){
			logger.error("Credential hash is invalid");
			return out_bool;
		}
		if(credential.getEnciphered()){
			if(credential.getKeyId() == null){
				logger.error("Enciphered credential does not define a key");
				return out_bool;
			}
			SecurityBean bean = KeyService.getAsymmetricKeyByObjectId(credential.getKeyId(), credential.getOrganization());
			if(bean == null){
				logger.error("Credential key is invalid");
				return out_bool;
			}
			hash = SecurityUtil.decrypt(bean, hash);
			if(hash.length == 0){
				logger.error("Post-decrypted credential hash is invalid");
				return out_bool;
			}
		}
		try {
			byte[] matchHash = SecurityUtil.getDigest(password.getBytes("UTF-8"), credential.getSalt());
			out_bool = Arrays.areEqual(hash, matchHash);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_bool;
	}
	public static CredentialType newHashedPasswordCredential(UserType owner, NameIdType targetObject, String password, boolean primary){
		byte[] pwdBytes = new byte[0];
		try {
			pwdBytes = password.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newCredential(CredentialEnumType.HASHED_PASSWORD, null, owner, targetObject, pwdBytes, primary, true);
	}
	public static CredentialType newCredential(CredentialEnumType credType, String bulkSessionId, UserType owner, NameIdType targetObject, byte[] credBytes, boolean primary, boolean encrypted){
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
				SecurityBean bean = KeyService.newPersonalAsymmetricKey(owner, false);
				cred.setKeyId(bean.getObjectId());
				//logger.info("Bean has bytes: " + (bean.getPublicKeyBytes() != null) + " / and key = " + (bean.getPublicKey() != null));
				useCredBytes = SecurityUtil.encrypt(bean, useCredBytes);
				if(useCredBytes.length == 0) throw new FactoryException("Invalid encrypted credential");
				cred.setEnciphered(true);
			}
			
			cred.setCredential(useCredBytes);
			
			if(bulkSessionId != null) BulkFactories.getBulkFactory().createBulkEntry(bulkSessionId, FactoryEnumType.CREDENTIAL, cred);
			else if(Factories.getCredentialFactory().addCredential(cred)){
				cred = Factories.getCredentialFactory().getCredentialByObjectId(cred.getObjectId(),cred.getOrganization());
			}
			else{
				logger.error("Failed to add credential");
			}
			if(lastPrimary != null){
				lastPrimary.setPrimary(false);
				Factories.getCredentialFactory().updateCredential(lastPrimary);
			}

		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}  catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return cred;

	}
	
}
