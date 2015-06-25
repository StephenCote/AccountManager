package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.cote.accountmanager.data.security.CredentialService;
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
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.parsers.excel.RowType;
import org.cote.parsers.excel.SheetType;
import org.cote.parsers.excel.TikaShredder;
import org.cote.parsers.excel.WorkbookType;
import org.junit.Test;


public class TestImportContacts extends BaseDataAccessTest{
	/*
	@Test
	public void TestDataFile(){
		String content = TikaShredder.getExcelAsString("/Users/Steve/Desktop/Address.xlsx");
		assertNotNull("Content is null",content);
		assertTrue("Content is empty",content.length() > 0);
		WorkbookType wb = TikaShredder.shredTikaContent(content,true);
		assertNotNull("Workbook is null",wb);
		assertTrue("There are no sheets",wb.getSheets().size() > 0);
		logger.info("Working with " + wb.getSheets().size() + " sheets");
		SheetType sheet = wb.getSheets().get(0);
		assertTrue("There are no rows",sheet.getRows().size() > 0);
		
		RowType row = sheet.getRows().get(0);
		assertTrue("There are no cells",row.getCells().size() > 0);
		
		DirectoryGroupType pDir = null;
		try{
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons-" + UUID.randomUUID().toString(), testUser.getHomeDirectory(), testUser.getOrganization());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			for(int i = 0; i < sheet.getRows().size();i++){
				importPersonRow(sessionId,pDir,sheet.getRows().get(i));	
			}
				
		
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);

		
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		}  catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e){
			logger.error("Unknown Exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		
	}
	*/
	@Test
	public void importRoleSourceData(){
		String content = TikaShredder.getExcelAsString("/Users/Steve/Downloads/Roles_20130905.xlsx");
		WorkbookType wb = TikaShredder.shredTikaContent(content,true);
		SheetType sheet = wb.getSheets().get(1);
		logger.info(sheet.getSheetName());
		DirectoryGroupType dir = null;
		BaseRoleType parentRole = null;
		BaseGroupType parentGroup = null;
		Map<String,Set<String>> userGroup = new HashMap<String,Set<String>>();
		Map<String,Set<String>> userRole = new HashMap<String,Set<String>>();
		try {
			dir = Factories.getGroupFactory().getCreateUserDirectory(testUser, "TestPersons");
			parentRole = Factories.getRoleFactory().getCreateUserRole(testUser, "TestUserRoles", null);
			parentGroup = Factories.getGroupFactory().getCreateUserGroup(testUser,"TestUserGroups",testUser.getHomeDirectory(),testUser.getOrganization());
			
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
				PersonType person = Factories.getPersonFactory().getByName(name, dir);
				UserType user = null;
				if(person == null){
					person = Factories.getPersonFactory().newPerson(testUser, dir);
					person.setName(name);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, person);
					user = Factories.getUserFactory().newUser(name, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, testUser.getOrganization());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, user);
					CredentialService.newCredential(CredentialEnumType.HASHED_PASSWORD, sessionId, user, user, "password".getBytes("UTF-8"), true, true);

					person.getUsers().add(user);
				}
				else{
					user = person.getUsers().get(0);
				}
				UserRoleType role = Factories.getRoleFactory().getUserRoleByName(roleName, parentRole, parentRole.getOrganization());
				if(role == null){
					role = Factories.getRoleFactory().newUserRole(testUser, roleName, parentRole);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLE, role);
				}
				UserGroupType group = Factories.getGroupFactory().getUserGroupByName(groupName, parentGroup,parentGroup.getOrganization());
				if(group == null){
					group = Factories.getGroupFactory().newUserGroup(testUser, groupName, parentGroup, parentGroup.getOrganization());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, group);
				}
				if(userGroup.containsKey(name) == false){
					userGroup.put(name, new HashSet<String>());
				}
				if(userRole.containsKey(name) == false){
					userRole.put(name, new HashSet<String>());
				}
				if(userGroup.get(name).contains(groupName) == false){
					BaseParticipantType bpt = Factories.getGroupParticipationFactory().newUserGroupParticipation(group, user);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, bpt);
					userGroup.get(name).add(groupName);
				}
				if(userRole.get(name).contains(roleName) == false){
					BaseParticipantType bpt = Factories.getRoleParticipationFactory().newUserRoleParticipation(role, user);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLEPARTICIPATION, bpt);
					userRole.get(name).add(roleName);
				}
				
				
			}
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);			
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			PersonType new_person = Factories.getPersonFactory().newPerson(testUser,pDir);
			new_person.setName(firstName + " " + lastName);
			new_person.setFirstName(firstName);
			new_person.setLastName(lastName);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON,new_person);
			
			ContactInformationType cit = Factories.getContactInformationFactory().newContactInformation(new_person);
			cit.setOwnerId(testUser.getId());

			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION,cit);
			
			new_person.setContactInformation(cit);
			
			AddressType addr = Factories.getAddressFactory().newAddress(testUser, pDir);
			addr.setName(firstName + " " + lastName + " Address");
			addr.setLocationType(LocationEnumType.HOME);
			addr.setAddressLine1(address);
			addr.setCity(city);
			addr.setState(state);
			addr.setPostalCode(zip);
			addr.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS,addr);
			
			cit.getAddresses().add(addr);
			
			ContactType contact = Factories.getContactFactory().newContact(testUser, pDir);
			contact.setName(firstName + " " + lastName + " Email");
			contact.setContactType(ContactEnumType.EMAIL);
			contact.setLocationType(LocationEnumType.HOME);
			contact.setContactValue(email);
			contact.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT,contact);
			cit.getContacts().add(contact);
			
			contact = Factories.getContactFactory().newContact(testUser, pDir);
			contact.setName(firstName + " " + lastName + " Phone");
			contact.setContactType(ContactEnumType.PHONE);
			contact.setLocationType(LocationEnumType.HOME);
			contact.setContactValue(phone);
			contact.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT,contact);
			cit.getContacts().add(contact);
		}
		catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

	}
	
}