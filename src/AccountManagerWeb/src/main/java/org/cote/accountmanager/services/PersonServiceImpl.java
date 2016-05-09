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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;

public class PersonServiceImpl  {
	public static final Logger logger = Logger.getLogger(PersonServiceImpl.class.getName());
	public static final String defaultDirectory = "~/Persons";

	public static boolean delete(PersonType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.PERSON, bean, request);
	}
	
	public static boolean add(PersonType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.PERSON, bean, request);
	}
	public static boolean update(PersonType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.PERSON, bean, request);
	}
	public static PersonType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.PERSON, name, request);
	}
	public static PersonType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.PERSON, groupId, name, request);
	}	
	public static PersonType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.PERSON, id, request);
	}
	
	public static List<PersonType> getGroupList(UserType user, String path, long startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.PERSON, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.PERSON, groupId, request);
	}	
	
	public static PersonType readPersonForUser(UserType user, HttpServletRequest request){
		PersonType person = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "User #" + user.getId(),AuditEnumType.USER,user.getName());
		UserType contUser = ServiceUtil.getUserFromSession(audit, request);
		if(contUser == null) return null;
		try{
			if(user.getId().compareTo(contUser.getId()) != 0 && AuthorizationService.isAccountAdministratorInOrganization(contUser, user.getOrganizationId()) == false){
				AuditService.denyResult(audit, "Not authorized to read user");
			}
			else{
				person = Factories.getPersonFactory().getPersonByUser(user);
				if(person == null){
					AuditService.denyResult(audit, "Global person does not exist for user");
					return person;
				}
				if(AuthorizationService.isMapOwner(contUser, person) == false && AuthorizationService.canViewGroup(contUser, Factories.getGroupFactory().getGroupById(person.getGroupId(),person.getOrganizationId())) == false){
					AuditService.denyResult(audit, "Not authorized to read person");
					person = null;
				}
				else{
					Factories.getPersonFactory().populate(person);
					/// populating group here due to a UI issue preventing most users from populating it due to the group being restricted
					/// This prevents the path from being exposed, and any subsequent update action moves the object to the default group
					/// even though the user is authorized to update in place
					///
					//Factories.getGroupFactory().populate(person.getGroup());
					AuditService.permitResult(audit, "Permitted to read person");
				}
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return person;
	}
}
