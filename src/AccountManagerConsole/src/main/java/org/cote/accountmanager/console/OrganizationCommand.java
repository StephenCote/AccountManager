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

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.util.SecurityUtil;

public class OrganizationCommand {
	public static final Logger logger = Logger.getLogger(OrganizationCommand.class.getName());

	public static boolean deleteOrganization(String parentPath, String name, String adminPassword, boolean allowNoAuth){
		boolean out_bool = false;
		try{
			String orgPath = parentPath + (parentPath.endsWith("/") ? "" : "/") + name;
			OrganizationType org = Factories.getOrganizationFactory().findOrganization(orgPath);
			if(org != null){
				
				UserType adminUser = (allowNoAuth ? null : SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId()));
				if(allowNoAuth || adminUser != null){
					logger.warn("Deleting " + org.getName());
					Factories.getOrganizationFactory().deleteOrganization(org);
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
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
	
		return out_bool;
	}
	public static boolean addOrganization(String parentPath, String name, String parentAdminPassword, String newPassword,boolean allowNoAuth){
		boolean out_bool = false;
		try{
		OrganizationType org = Factories.getOrganizationFactory().findOrganization(parentPath);
		if(org != null){
			OrganizationType uOrg = org;
			if(uOrg.getName().equals("Global") && uOrg.getParentId().equals(0L)) uOrg = Factories.getSystemOrganization();
			UserType adminUser = (allowNoAuth ? Factories.getUserFactory().getUserByName("Admin", uOrg.getId()) : SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,parentAdminPassword, uOrg.getId()));
			if(adminUser != null){
				OrganizationType newOrg = Factories.getOrganizationFactory().addOrganization(name,OrganizationEnumType.PUBLIC,org);
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
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}

		return out_bool;
	}
	
}
