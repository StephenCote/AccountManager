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
package org.cote.accountmanager.data.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

public class ServiceUtil {
	public static final Logger logger = LogManager.getLogger(ServiceUtil.class);
	public static boolean isFactorySetup(){
		boolean outBool = false;
		AccountType rootAcct = null;
		UserType root = null;
		UserType admin = null;
		UserType doc = null;
		UserRoleType adminRole = null;
		UserRoleType dataRole = null;
		try{
			dataRole = RoleService.getDataAdministratorUserRole(Factories.getPublicOrganization().getId());
			adminRole = RoleService.getAccountAdministratorUserRole(Factories.getPublicOrganization().getId());
			if(dataRole == null){
				logger.error("Data role in public org is null");
				return outBool;
			}
			if(adminRole == null){
				logger.error("Admin role in public org is null");
				return outBool;
			}
			rootAcct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName("Root", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Root", Factories.getSystemOrganization().getId()));
			root = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Root", Factories.getSystemOrganization().getId());
			if(rootAcct == null || root == null){
				logger.error("Root account or root user is null");
				return outBool;
			}
			
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(root);
			
			admin = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", Factories.getPublicOrganization().getId());
			doc = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Document Control", Factories.getPublicOrganization().getId());
			if(admin == null || doc == null){
				logger.error("Admin or Document Control user in public org was null");
				return outBool;
			}
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(admin);
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
				EffectiveAuthorizationService.rebuildRoleCache(Factories.getPublicOrganization().getId());
				EffectiveAuthorizationService.rebuildRoleCache(Factories.getSystemOrganization().getId());
				
				/// 2014/07/14 - Removed check for doc control in data admin role per prior authorization changes where doc control is delegated permission instead of receiving carte blanche data admin rigths
				///  || RoleService.getIsUserInRole(dataRole, doc) == false
				if(RoleService.getIsUserInRole(adminRole, admin) == false || RoleService.getIsUserInRole(adminRole, root) == false){
					logger.error("Root, Admin, or Document Control were not in the admin role in the public org.");
					return outBool;
				}
			}
			outBool = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBool;
	}
}
