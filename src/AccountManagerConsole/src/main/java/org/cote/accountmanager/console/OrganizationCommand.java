/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.console;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.util.KeyStoreUtil;
import org.cote.accountmanager.data.factory.OrganizationFactory;

public class OrganizationCommand {
	public static final Logger logger = LogManager.getLogger(OrganizationCommand.class);
	public static boolean setOrganizationCertificate(String organizationPath, String sslPath, String alias, char[] password, String adminPassword){
		boolean out_bool = false;
		try{
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(organizationPath);
			if(org == null){
				logger.error("Null organization");
				return false;
			}
			UserType adminUser = SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId());
			if(adminUser == null){
				logger.error("Unable to authenticate");
				return false;
			}
			//logger.info("Admin org: " + adminUser.getOrganizationId());
			//logger.debug("Bug: Organization objects don't have an organization, only parents, which breaks);
			OpenSSLAction sslAction = new OpenSSLAction(null,sslPath);
			byte[] certificate = sslAction.getCertificate(alias, true);
			CredentialType cred = CredentialService.newCredential(CredentialEnumType.CERTIFICATE, null, adminUser, org, certificate, true, false, false);
			if(cred == null){
				logger.error("Failed to create certificate credential");
				return out_bool;
			}
			CredentialType cred2 = CredentialService.newCredential(CredentialEnumType.ENCRYPTED_PASSWORD, null, adminUser, cred, (new String(password)).getBytes(), true, true, false);
			if(cred2 == null){
				logger.error("Failed to create encrypted credential for certificate password");
				return out_bool;
			}
			Factories.getAttributeFactory().populateAttributes(org);
			AttributeType aliasAttr = Factories.getAttributeFactory().getAttributeByName(org, "certificate.alias");
			if(aliasAttr != null) org.getAttributes().remove(aliasAttr);
			org.getAttributes().add(Factories.getAttributeFactory().newAttribute(org, "certificate.alias", alias));
			Factories.getAttributeFactory().updateAttributes(org);
			out_bool = true;
			
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
		}
		return out_bool;
	}
	public static boolean testOrganizationCertificate(String organizationPath, String sslPath, String adminPassword){
		boolean out_bool = false;
		try{
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(organizationPath);
			if(org == null){
				logger.error("Null organization");
				return false;
			}
			UserType adminUser = SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId());
			if(adminUser == null){
				logger.error("Unable to authenticate");
				return false;
			}
			Factories.getAttributeFactory().populateAttributes(org);
			AttributeType aliasAttr = Factories.getAttributeFactory().getAttributeByName(org, "certificate.alias");
			String alias = null;
			if(aliasAttr == null || aliasAttr.getValues().size() == 0) alias = "1";
			else alias = aliasAttr.getValues().get(0);
			
			CredentialType cred = CredentialService.getPrimaryCredential(org, CredentialEnumType.CERTIFICATE, true);
			if(cred == null){
				logger.error("Failed to retrieve certificate");
				return false;
			}
			CredentialType cred2 = CredentialService.getPrimaryCredential(cred, CredentialEnumType.ENCRYPTED_PASSWORD, true);
			if(cred2 == null){
				logger.error("Failed to retrieve certificate password");
				return false;
			}
			byte[] p12 = (cred.getEnciphered() ? CredentialService.decryptCredential(cred) : cred.getCredential());
			char[] p12pass = (new String((cred2.getEnciphered() ? CredentialService.decryptCredential(cred2) : cred2.getCredential()))).toCharArray();
			logger.info("P12 P: " + (new String(p12pass)));
			KeyStore store = KeyStoreUtil.getKeyStore(p12, p12pass);
			if(store == null){
				logger.error("Failed to open PKCS12 data");
				return false;
			}
			
			Key privateKey = KeyStoreUtil.getKey(store, p12pass, alias, p12pass);
			if(privateKey == null){
				logger.error("Failed to extract private key");
				return false;
			}
			Certificate cert = KeyStoreUtil.getCertificate(store, p12pass, alias);
			if(cert == null){
				logger.error("Failed to extract certificate for alias '" + alias + "'");
				return false;
			}
			logger.info("Retrieved private and public keys");
			out_bool = true;
			
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
		}
		return out_bool;
	}
	public static boolean deleteOrganization(String parentPath, String name, String adminPassword, boolean allowNoAuth){
		boolean out_bool = false;
		try{
			String orgPath = parentPath + (parentPath.endsWith("/") ? "" : "/") + name;
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(orgPath);
			if(org != null){
				
				UserType adminUser = (allowNoAuth ? null : SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId()));
				if(allowNoAuth || adminUser != null){
					logger.warn("Deleting " + org.getName());
					((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(org);
					SessionSecurity.logout(adminUser);
				}
				else{
					logger.error("Failed to login as admin user.");
				}
			}
			else{
				logger.error("Organization '" +orgPath + "' doesn't exist");
			}
		}
		
		catch(ArgumentException ae){
			logger.error(ae.getMessage());
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
		}
	
		return out_bool;
	}
	public static boolean addOrganization(String parentPath, String name, String parentAdminPassword, String newPassword,boolean allowNoAuth){
		boolean out_bool = false;
		try{
		OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(parentPath);
		if(org != null){
			OrganizationType uOrg = org;
			if(uOrg.getName().equals("Global") && uOrg.getParentId().equals(0L)) uOrg = Factories.getSystemOrganization();
			UserType adminUser = (allowNoAuth ? Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", uOrg.getId()) : SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,parentAdminPassword, uOrg.getId()));
			if(adminUser != null){
				OrganizationType newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).addOrganization(name,OrganizationEnumType.PUBLIC,org);
				if(newOrg != null && FactoryDefaults.setupOrganization(newOrg, newPassword)){
					logger.info("Created organization " + name + " in " + org.getName());
					UserType adminUser2 = SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,newPassword, newOrg.getId());
					if(adminUser2 != null){
						logger.info("Verified new administrator user");
						SessionSecurity.logout(adminUser2);
						out_bool = true;
					}
					else{
						logger.error("Unable to verify new administrator user");
					}
				}
				SessionSecurity.logout(adminUser);
			}
			else{
				logger.error("Failed to login as admin user.");
			}
		}
		else{
			logger.error("Organization was not found");
		}
		}
		catch(ArgumentException ae){
			logger.error(ae.getMessage());
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
		} catch (DataAccessException e) {
			
			logger.error(e.getMessage());
		}

		return out_bool;
	}
	
}
