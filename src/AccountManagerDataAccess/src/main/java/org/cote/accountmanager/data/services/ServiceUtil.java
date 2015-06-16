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
package org.cote.accountmanager.data.services;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;

public class ServiceUtil {
	public static final Logger logger = Logger.getLogger(FactoryService.class.getName());
	public static boolean isFactorySetup(){
		boolean out_bool = false;
		AccountType rootAcct = null;
		AccountType adminAcct = null;
		UserType root = null;
		UserType admin = null;
		UserType doc = null;
		UserRoleType adminRole = null;
		UserRoleType dataRole = null;
		try{
			dataRole = RoleService.getDataAdministratorUserRole(Factories.getPublicOrganization());
			adminRole = RoleService.getAccountAdministratorUserRole(Factories.getPublicOrganization());
			if(dataRole == null){
				logger.error("Data role in public org is null");
				return out_bool;
			}
			if(adminRole == null){
				logger.error("Admin role in public org is null");
				return out_bool;
			}
			rootAcct = Factories.getAccountFactory().getAccountByName("Root", Factories.getGroupFactory().getDirectoryByName("Root", Factories.getSystemOrganization()));
			root = Factories.getUserFactory().getUserByName("Root", Factories.getSystemOrganization());
			if(rootAcct == null || root == null){
				logger.error("Root account or root user is null");
				return out_bool;
			}
			
			Factories.getUserFactory().populate(root);
			
			admin = Factories.getUserFactory().getUserByName("Admin", Factories.getPublicOrganization());
			doc = Factories.getUserFactory().getUserByName("Document Control", Factories.getPublicOrganization());
			if(admin == null || doc == null){
				logger.error("Admin or Document Control user in public org was null");
				return out_bool;
			}
			Factories.getUserFactory().populate(admin);
			boolean adminHasRole = RoleService.getIsUserInRole(adminRole, admin);
			boolean rootHasRole = RoleService.getIsUserInRole(adminRole, root);
			boolean docHasRole = RoleService.getIsUserInRole(dataRole, doc);
			/* 2014/07/14 - see below comment || docHasRole == false */
			if(adminHasRole == false || rootHasRole == false ){
				logger.warn("Required organization users not found in administration roles.");
				logger.info("Admin " + admin.getId() + " Has Role " + adminRole.getId() + " = " + adminHasRole);
				logger.info("Root " + root.getId() + " Has Role " + adminRole.getId() + " = " + rootHasRole);
				logger.info("Doc " + doc.getId() + " Has Role " + dataRole.getId() + " = " + docHasRole);
				logger.warn("Attempting to rebuild role cache.");
				EffectiveAuthorizationService.rebuildUserRoleCache(Factories.getPublicOrganization());
				EffectiveAuthorizationService.rebuildUserRoleCache(Factories.getSystemOrganization());
				
				/// 2014/07/14 - Removed check for doc control in data admin role per prior authorization changes where doc control is delegated permission instead of receiving carte blanche data admin rigths
				///  || RoleService.getIsUserInRole(dataRole, doc) == false
				if(RoleService.getIsUserInRole(adminRole, admin) == false || RoleService.getIsUserInRole(adminRole, root) == false){
					logger.error("Root, Admin, or Document Control were not in the admin role in the public org.");
					return out_bool;
				}
			}
			out_bool = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return out_bool;
	}
}
