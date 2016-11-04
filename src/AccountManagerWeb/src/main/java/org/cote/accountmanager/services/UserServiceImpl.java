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
package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;



public class UserServiceImpl  {
	public static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
	public static final String defaultDirectory = "~/Users";

	public static boolean delete(UserType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.USER, bean, request);
	}
	
	public static boolean add(UserType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.USER, bean, request);
	}
	public static boolean update(UserType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.USER, bean, request);
	}
	public static UserType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.USER, name, request);
	}
	public static UserType readByOrganizationId(long orgId, String name,HttpServletRequest request){
		return BaseService.readByNameInOrganization(AuditEnumType.USER, orgId, name, request);
	}	
	public static UserType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.USER, id, request);
	}
	public static int count(long orgId, HttpServletRequest request){
		return BaseService.countByOrganization(AuditEnumType.USER, orgId, request);
	}
	
	public static List<UserType> getList(UserType user, long organizationId, long startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.USER, user, path, startRecord, recordCount);
		

		List<UserType> out_obj = new ArrayList<UserType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All users",AuditEnumType.USER,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.USER, "All users");
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
		if(organizationId == 0L){
			AuditService.denyResult(audit,  "Organization is null");
			return null;
		}
		try {
			//AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				RoleService.isFactoryAdministrator(user, Factories.getAccountFactory()) == true
				||
				RoleService.isFactoryReader(user, Factories.getAccountFactory()) == true
			){
				AuditService.permitResult(audit, "Access authorized to list users");
				out_obj = getList(startRecord,recordCount,organizationId);
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list users.");
				return out_obj;
			}
			
		} catch (ArgumentException e1) {
			
			logger.error(e1.getStackTrace());
		} catch (FactoryException e1) {
			
			logger.error(e1.getStackTrace());
		} 

		return out_obj;
		
	}
	private static List<UserType> getList(long startRecord, int recordCount, long organizationId) throws ArgumentException, FactoryException {

		return Factories.getUserFactory().getUserList(startRecord, recordCount, organizationId);
		
	}
	
	
	
}
