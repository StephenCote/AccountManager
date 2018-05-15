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
package org.cote.accountmanager.console;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.PersonService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
public class UserCommand {
public static final Logger logger = LogManager.getLogger(UserCommand.class);
	
	public static boolean addUser(String orgPath, String name, String adminPassword, String newPassword, String email){
		boolean out_bool = false;
		try{
		OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(orgPath);
		if(org != null){
			UserType adminUser = SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId());
			if(adminUser != null){
				AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, UserCommand.class.getName(), AuditEnumType.USER, "Admin");
				AuditService.targetAudit(audit, AuditEnumType.USER, name);
				out_bool = PersonService.createUserAsPerson(audit, name, newPassword, email, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, org.getId());
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
		} 

		return out_bool;
	}
	
}
