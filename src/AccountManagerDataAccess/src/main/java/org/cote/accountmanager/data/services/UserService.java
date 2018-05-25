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
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;

public class UserService {
	
	public static final Logger logger = LogManager.getLogger(UserService.class);
		
	public static PersonType readPersonForUser(UserType user, UserType contUser){
		PersonType person = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "Find person for " + user.getName(),AuditEnumType.USER,user.getUrn());
		try{
			if(user.getId().compareTo(contUser.getId()) != 0 && !BaseService.canViewType(AuditEnumType.USER, user, contUser)){
				AuditService.denyResult(audit, "Not authorized to read user");
			}
			else{
				person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getPersonByUser(user);
				if(person == null){
					AuditService.denyResult(audit, "Global person does not exist for user");
					return person;
				}
				if(!BaseService.canViewType(AuditEnumType.PERSON, user, person)){
					AuditService.denyResult(audit, "Not authorized to read person");
					person = null;
				}
				else{
					((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
					AuditService.permitResult(audit, "Permitted to read person");
				}
			}
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
		
		return person;
	}
	public static DataType getProfile(UserType user){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "getProfile",AuditEnumType.SESSION,"Anonymous");
		AuditService.targetAudit(audit, AuditEnumType.DATA, "{profile}");
		return getProfile(user,audit);
	}
	protected static void addDefaultProfileAttributes(DataType data){
		data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "blog.title", "My blog title"));
		data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "blog.subtitle", "My blog subtitle"));
		data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "blog.author", "My pen name"));
		data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "blog.signature", "My signature"));
		Factories.getAttributeFactory().addAttributes(data);
	}
	public static DataType getProfile(UserType user, AuditType audit){
		DataType data = null;
		try{
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(".profile", false, user.getHomeDirectory());
			if(data == null){
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(user, user.getHomeDirectory().getId());
				data.setMimeType("text/plain");
				data.setName(".profile");
				if(((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data)){
					data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(".profile",false,user.getHomeDirectory());
					addDefaultProfileAttributes(data);
				}
			}
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
			AuditService.denyResult(audit,e.getMessage());
		}
		
		if(data != null){
			AuditService.targetAudit(audit, AuditEnumType.DATA,data.getUrn());
			Factories.getAttributeFactory().populateAttributes(data);
			AuditService.permitResult(audit,"Returning " + user.getName() + " profile data");
		}
		else{
			AuditService.denyResult(audit,"Unable to retrieve profile information");
		}
		return data;
	}

	public static ContactType getPreferredEmailContact(UserType user){
		return ContactService.getPreferredEmailContact(user.getContactInformation());
	}
	
}
