package org.cote.accountmanager.data.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.parsers.excel.RowType;
import org.cote.parsers.excel.SheetType;
import org.cote.parsers.excel.TikaShredder;
import org.cote.parsers.excel.WorkbookType;

public class ContactParser {
	public static final Logger logger = LogManager.getLogger(ContactParser.class);
	public static int parseContactData(UserType user,DataType data){
		int ret = 0;
		Map<String,Integer> columnMap = new HashMap<>();
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(DataUtil.getValue(data));
			if(data.getMimeType().equals("text/csv")){
				logger.info("Parsing CSV content");
				List<PersonType> persons = parseCsv(user, data);
				ret = persons.size();
				importPersons(user,persons);
			}
			else{
				logger.warn("TIKA BUG - Empty cell values aren't handled well/correctly in Tika, which makes this problematic");
				String content = TikaShredder.getExcelAsString(data.getName(),bais);
				if(content == null || content.length() == 0){
					logger.error("Null content");
					return 0;
				}

				WorkbookType wb = TikaShredder.shredTikaContent(content,false);
				if(wb == null || wb.getSheets().isEmpty()){
					logger.error("Failed to parse workbook");
					return 0;
				}
				SheetType sheet = wb.getSheets().get(0);
				/// 2018/07/31 - Change minimum row count to support only one row.  This was originally coded as requiring at least two contacts
				///
				if(sheet.getRows().size() < 4){
					logger.error("Unexpected row count: " + sheet.getRows().size());
					return 0;
				}
				mapColumns(sheet.getRows().get(2),columnMap);
				List<PersonType> persons = parseSheet(user,columnMap,sheet,3);
				logger.info("Parsed " + persons.size() + " persons");
				ret = persons.size();
				importPersons(user,persons);
			}
			logger.info("Imported " + ret + " persons");
		} catch (DataException | ArgumentException | FactoryException | DataAccessException | IOException e) {
			logger.error(e);
		}

		return ret;
	}
	private static void importPersons(UserType user, List<PersonType> persons) throws ArgumentException, FactoryException, DataAccessException{
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		for(int i = 0; i < persons.size();i++){
			PersonType person = persons.get(i);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, person);
			ContactInformationType mcit = person.getContactInformation();
			ContactInformationType cit = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(person);
			cit.setOwnerId(user.getId());

			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cit);
			
			person.setContactInformation(cit);
			for(int c = 0; c < mcit.getContacts().size();c++){
			
				ContactType ct = mcit.getContacts().get(c);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT, ct);
				cit.getContacts().add(ct);
			}
		}
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);

	}
	private static List<PersonType> parseCsv(UserType user, DataType data) throws DataException, IOException, ArgumentException, FactoryException{
		List<PersonType> persons = new ArrayList<PersonType>();
		DirectoryGroupType homeDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserDirectory(user);
		DirectoryGroupType pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Persons", homeDir, user.getOrganizationId());
		if(pdir != null){
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).deletePersonsInGroup(pdir);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup(pdir);
		}
		pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Persons", homeDir, user.getOrganizationId());
		DirectoryGroupType cdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Contacts", homeDir, user.getOrganizationId());
		if(cdir != null){
			((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).deleteContactsInGroup(cdir);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup(cdir);
		}
		cdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Contacts", homeDir, user.getOrganizationId());
		
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(new String[]{"First Name","Middle Name","Last Name","Birthdate","Home E-Mail","Home Phone Number","Cell Phone Number","Work E-Mail","Work Phone Number","Relationship"});
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(DataUtil.getValue(data))));
		CSVParser csvFileParser = new CSVParser(reader, csvFileFormat);
		List<CSVRecord> csvRecords = csvFileParser.getRecords(); 
		for (int i = 1; i < csvRecords.size(); i++) {
			CSVRecord record = csvRecords.get(i);
			String firstName = record.get("First Name");
			String middleName = record.get("Middle Name");
			String lastName = record.get("Last Name");
			String email = record.get("Home E-Mail");
			String workEmail = record.get("Work E-Mail");
			String homePhone = record.get("Home Phone Number");
			String workPhone = record.get("Work Phone Number");
			String cellPhone = record.get("Cell Phone Number");
			String birthDate = record.get("Birthdate");
			PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(user, pdir.getId());
			if(firstName != null && firstName.length() > 0){
				person.setFirstName(firstName.trim());
			}
			if(middleName != null && middleName.length() > 0){
				person.setMiddleName(middleName.trim());
			}
			if(lastName != null && lastName.length() > 0){
				person.setLastName(lastName.trim());
			}
			if(birthDate != null && birthDate.length() > 0){
				person.setBirthDate(CalendarUtil.getXmlGregorianCalendar(CalendarUtil.importDateFromString(birthDate,"MM/dd/yyyy")));
			}
			if(person.getFirstName() == null && person.getLastName() == null){
				logger.info("Skipping empty name at #" + i);
			}
			person.setName((person.getFirstName() != null ? person.getFirstName() + (person.getMiddleName() != null ? " " + person.getMiddleName() : "") : "") + (person.getFirstName() != null && person.getLastName() != null ? " " : "") + (person.getLastName() != null ? person.getLastName() : ""));

			ContactInformationType cit= ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(person);
			cit.setOwnerId(user.getId());
			person.setContactInformation(cit);
			if(email != null && email.length() > 0){

				ContactType em = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(user, cdir.getId());
				em.setContactType(ContactEnumType.EMAIL);
				em.setLocationType(LocationEnumType.HOME);
				em.setName(person.getName() + " Home Email");
				em.setDescription("");
				em.setContactValue(email.trim());
				person.getContactInformation().getContacts().add(em);
				
			}
			if(workEmail != null && workEmail.length() > 0){

				ContactType em = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(user, cdir.getId());
				em.setContactType(ContactEnumType.EMAIL);
				em.setLocationType(LocationEnumType.WORK);
				em.setName(person.getName() + " Work Email");
				em.setDescription("");
				em.setContactValue(workEmail.trim());
				person.getContactInformation().getContacts().add(em);
				
			}
			if(homePhone != null && homePhone.length() > 0){
					ContactType phone = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(user, cdir.getId());
					phone.setContactType(ContactEnumType.PHONE);
					phone.setLocationType(LocationEnumType.HOME);
					phone.setName(person.getName() + " Home Phone");
					phone.setDescription("");
					phone.setContactValue(homePhone.trim());
					person.getContactInformation().getContacts().add(phone);
				
			}
			if(cellPhone != null && cellPhone.length() > 0){
				ContactType phone = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(user, cdir.getId());
				phone.setContactType(ContactEnumType.PHONE);
				phone.setLocationType(LocationEnumType.MOBILE);
				phone.setName(person.getName() + " Cell Phone");
				phone.setDescription("");
				phone.setContactValue(cellPhone.trim());
				person.getContactInformation().getContacts().add(phone);
			
			}
			if(workPhone != null && workPhone.length() > 0){
				ContactType phone = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(user, cdir.getId());
				phone.setContactType(ContactEnumType.PHONE);
				phone.setLocationType(LocationEnumType.WORK);
				phone.setName(person.getName() + " Work Phone");
				phone.setDescription("");
				phone.setContactValue(workPhone.trim());
				person.getContactInformation().getContacts().add(phone);
			
			}
			persons.add(person);
			
		}
		csvFileParser.close();
		return persons;
	}
	private static List<PersonType> parseSheet(UserType user, Map<String,Integer> map,SheetType sheet, int startRow ) throws ArgumentException, FactoryException{
		List<PersonType> persons = new ArrayList<PersonType>();
		int len = sheet.getRows().size();
		int minSize = map.keySet().size();
		DirectoryGroupType homeDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserDirectory(user);
		DirectoryGroupType pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Persons", homeDir, user.getOrganizationId());
		if(pdir != null){
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).deletePersonsInGroup(pdir);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup(pdir);
		}
		pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Persons", homeDir, user.getOrganizationId());
		DirectoryGroupType cdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Contacts", homeDir, user.getOrganizationId());
		if(cdir != null){
			((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).deleteContactsInGroup(cdir);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup(cdir);
		}
		cdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Contacts", homeDir, user.getOrganizationId());
		
		for(int i = startRow; i < len; i++){
			RowType row = sheet.getRows().get(i);
			int cellSize = row.getCells().size();
			
			for(int c = 0; c < cellSize;c++){
				logger.info(c + " '" + row.getCells().get(c).getCellValue() + "'");
			}
			/*
			if(row.getCells().size() < minSize){
				logger.info("Skip row #" + i + " with " + row.getCells().size() + " not " + minSize );
				continue;
			}
			*/
			int firstCell = (map.containsKey("First Name") ? map.get("First Name") : -1);
			int lastCell = (map.containsKey("Last Name") ? map.get("Last Name") : -1);
			int emailCell = (map.containsKey("E-Mail") ? map.get("E-Mail") : -1);
			int homePhoneCell = (map.containsKey("Home Phone Number") ? map.get("Home Phone Number") : -1);
			
			PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(user, pdir.getId());
			if(firstCell >= 0 && cellSize > firstCell){
				person.setFirstName(row.getCells().get(firstCell).getCellValue());
			}
			if(lastCell >= 0 && cellSize > lastCell){
				person.setLastName(row.getCells().get(lastCell).getCellValue());
			}
			if(person.getFirstName() == null && person.getLastName() == null){
				logger.info("Skipping empty name at #" + i);
			}
			person.setName((person.getFirstName() != null ? person.getFirstName()  : "") + (person.getFirstName() != null && person.getLastName() != null ? " " : "") + (person.getLastName() != null ? person.getLastName() : ""));
			logger.info(person.getName() + " " + cellSize + ", " + emailCell + ", " + homePhoneCell);
			ContactInformationType cit= ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(person);
			cit.setOwnerId(user.getId());
			person.setContactInformation(cit);
			if(emailCell >= 0 && cellSize > emailCell){
				String em = row.getCells().get(emailCell).getCellValue().trim();
				if(em.length() > 0){
					ContactType email = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(user, cdir.getId());
					email.setContactType(ContactEnumType.EMAIL);
					email.setLocationType(LocationEnumType.HOME);
					email.setName(person.getName() + " Home Email");
					email.setContactValue(em);
					person.getContactInformation().getContacts().add(email);
				}
			}
			if(homePhoneCell >= 0 && cellSize > homePhoneCell){
				String em = row.getCells().get(homePhoneCell).getCellValue().trim();
				if(em.length() > 0){
					ContactType phone = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(user, cdir.getId());
					phone.setContactType(ContactEnumType.PHONE);
					phone.setLocationType(LocationEnumType.HOME);
					phone.setName(person.getName() + " Home Phone");
					phone.setContactValue(em);
					person.getContactInformation().getContacts().add(phone);
				}
			}
			persons.add(person);

		}
		return persons;
	}
	private static void mapColumns(RowType row,Map<String,Integer> map){
		logger.info("Cells = " + row.getCells().size());
		int len = row.getCells().size();
		for(int i = 0; i < len; i++){
			logger.info("Mapping '" + row.getCells().get(i).getCellValue() + "' to " + i);
			String val = row.getCells().get(i).getCellValue();
			map.put(val, i);
		}
	}
	
}
