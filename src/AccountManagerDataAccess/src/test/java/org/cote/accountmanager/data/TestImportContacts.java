/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cote.accountmanager.data.factory.AddressFactory;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.parsers.excel.RowType;
import org.cote.parsers.excel.SheetType;
import org.cote.parsers.excel.TikaShredder;
import org.cote.parsers.excel.WorkbookType;
import org.junit.Test;

public class TestImportContacts extends BaseDataAccessTest{

	@Test
	public void importRoleSourceData(){
		String content = TikaShredder.getExcelAsString("testData/roles.xlsx");
		WorkbookType wb = TikaShredder.shredTikaContent(content,true);
		SheetType sheet = wb.getSheets().get(0);
		logger.info(sheet.getSheetName());
		DirectoryGroupType dir = null;
		BaseRoleType parentRole = null;
		BaseGroupType parentGroup = null;
		Map<String,Set<String>> userGroup = new HashMap<String,Set<String>>();
		Map<String,Set<String>> userRole = new HashMap<String,Set<String>>();
		try {
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(testUser, "TestPersons");
			parentRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateUserRole(testUser, "TestUserRoles", null);
			parentGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserGroup(testUser,"TestUserGroups",testUser.getHomeDirectory(),testUser.getOrganizationId());
			
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();

			for(int i = 1; i < sheet.getRows().size();i++){
				RowType row = sheet.getRows().get(i);
				if(row.getCells().size() < 3){
					logger.info("Skipping unexpected cell count");
					continue;
				}
				String roleName = row.getCell(0).getCellValue();
				String name = row.getCell(1).getCellValue();
				String groupName = row.getCell(2).getCellValue();
				if(name == null || name.length() == 0){
					logger.warn("Skipping null person entry");
					continue;
				}
				PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(name, dir);
				UserType user = null;
				if(person == null){
					person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser, dir.getId());
					person.setName(name);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, person);
					user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(name, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, testUser.getOrganizationId());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, user);
					CredentialService.newCredential(CredentialEnumType.HASHED_PASSWORD, sessionId, user, user, "password".getBytes("UTF-8"), true, true, false);

					person.getUsers().add(user);
				}
				else{
					user = person.getUsers().get(0);
				}
				UserRoleType role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRoleByName(roleName, parentRole, parentRole.getOrganizationId());
				if(role == null){
					role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).newUserRole(testUser, roleName, parentRole);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLE, role);
				}
				UserGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserGroupByName(groupName, parentGroup,parentGroup.getOrganizationId());
				if(group == null){
					group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newUserGroup(testUser, groupName, parentGroup, parentGroup.getOrganizationId());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, group);
				}
				if(userGroup.containsKey(name) == false){
					userGroup.put(name, new HashSet<String>());
				}
				if(userRole.containsKey(name) == false){
					userRole.put(name, new HashSet<String>());
				}
				if(userGroup.get(name).contains(groupName) == false){
					BaseParticipantType bpt = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newUserGroupParticipation(group, user);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, bpt);
					userGroup.get(name).add(groupName);
				}
				if(userRole.get(name).contains(roleName) == false){
					BaseParticipantType bpt = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newUserRoleParticipation(role, user);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLEPARTICIPATION, bpt);
					userRole.get(name).add(roleName);
				}
				
				
			}
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);			
			
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (UnsupportedEncodingException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	private void importPersonRow(String sessionId,DirectoryGroupType pDir, RowType row){
		
		String lastName = row.getCell("LastName").getCellValue();
		String firstName = row.getCell("FirstName").getCellValue();
		String child = row.getCell("Children").getCellValue();
		String address = row.getCell("Address").getCellValue();
		String city = row.getCell("City").getCellValue();
		String state = row.getCell("State").getCellValue();
		String zip = row.getCell("Zip").getCellValue();
		String phone = row.getCell("Phone").getCellValue();
		String email = row.getCell("e-mail").getCellValue();
		
		logger.info(lastName + ":" + firstName + ":" + child + ":" + address + ":" + city + ":" + state + ":" + zip + ":" + phone);
		try{
			PersonType new_person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir.getId());
			new_person.setName(firstName + " " + lastName);
			new_person.setFirstName(firstName);
			new_person.setLastName(lastName);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON,new_person);
			
			ContactInformationType cit = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(new_person);
			cit.setOwnerId(testUser.getId());

			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION,cit);
			
			new_person.setContactInformation(cit);
			
			AddressType addr = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(testUser, pDir.getId());
			addr.setName(firstName + " " + lastName + " Address");
			addr.setLocationType(LocationEnumType.HOME);
			addr.setAddressLine1(address);
			addr.setCity(city);
			addr.setState(state);
			addr.setPostalCode(zip);
			addr.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS,addr);
			
			cit.getAddresses().add(addr);
			
			ContactType contact = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(testUser, pDir.getId());
			contact.setName(firstName + " " + lastName + " Email");
			contact.setContactType(ContactEnumType.EMAIL);
			contact.setLocationType(LocationEnumType.HOME);
			contact.setContactValue(email);
			contact.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT,contact);
			cit.getContacts().add(contact);
			
			contact = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(testUser, pDir.getId());
			contact.setName(firstName + " " + lastName + " Phone");
			contact.setContactType(ContactEnumType.PHONE);
			contact.setLocationType(LocationEnumType.HOME);
			contact.setContactValue(phone);
			contact.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT,contact);
			cit.getContacts().add(contact);
		}
		catch (ArgumentException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		

	}
	
}