package org.cote.accountmanager.data.services;

import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;

public class PersonService {
	public static final Logger logger = Logger.getLogger(PersonService.class.getName());
	
	public static ContactType getPreferredEmailContact(PersonType person){
		return ContactService.getPreferredEmailContact(person.getContactInformation());
	}
	
	
	public static boolean createRegisteredUserAsPerson(AuditType audit, String userName, String password, String email,OrganizationType org){
		return createUserAsPerson(audit, userName, password,email,UserEnumType.NORMAL,UserStatusEnumType.REGISTERED,org);
	}
	public static boolean createUserAsPerson(AuditType audit, String userName, String password, String email,UserEnumType userType,UserStatusEnumType userStatus,OrganizationType org){
		boolean out_bool = false;
		try{
			if(Factories.getUserFactory().getUserNameExists(userName, org)){
				logger.error("User name '" + userName + "' is already used in organization " + org.getName());
				return false;
			}

			/// TODO - change this to just get the persons directory from the GroupFactory
			///
			UserType adminUser = Factories.getUserFactory().getUserByName("Admin", org);
			DirectoryGroupType pDir = Factories.getGroupFactory().getCreateDirectory(adminUser, "Persons", Factories.getGroupFactory().getRootDirectory(org), org);
			DirectoryGroupType cDir = Factories.getGroupFactory().getCreateDirectory(adminUser, "Contacts", Factories.getGroupFactory().getRootDirectory(org), org);
			
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			/// The logic is inverted here because the design is/was to have users own objects (all ownerId values point to users)
			/// And a user must own the person object, but because this person belongs to the user,
			/// the user is created first, then the person is added, but the person becomes the primary point of entry
			/// And this user is the primary owner.  Subsequent users attached to this person become dependent on the first, which owns the person
			///
			/// Since the Person structure is meant to capture basic identity information which may not be attached to a user,
			/// I'm leaving it as-is for the moment.  If it's going to be any more robust, it may as well just be an LDAP,
			/// And making an LDAP isn't the goal.
			///
			UserType newUser = Factories.getUserFactory().newUser(userName,  password, userType, userStatus, org);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, newUser);
			
			PersonType newPerson = Factories.getPersonFactory().newPerson(newUser,pDir);
			newPerson.setName(userName);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, newPerson);
			newPerson.getUsers().add(newUser);
			
			ContactInformationType cit = Factories.getContactInformationFactory().newContactInformation(newPerson);
			cit.setOwnerId(newUser.getId());
			
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cit);
			newPerson.setContactInformation(cit);
			if(email != null){
				ContactType ct = Factories.getContactFactory().newContact(newUser, cDir);
				ct.setName(newPerson.getName() + " Registration Email");
				ct.setPreferred(true);
				ct.setContactType(ContactEnumType.EMAIL);
				ct.setLocationType(LocationEnumType.HOME);
				ct.setContactValue(email);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT, ct);
				cit.getContacts().add(ct);
			}
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			out_bool = true;
			AuditService.permitResult(audit, "Created user '" + userName + "' (#" + newUser.getId() + ")");
		}
		catch(ArgumentException e){
			AuditService.denyResult(audit, "Failed to add user: " + e.getMessage());
			logger.error("Error creating user " + userName + ": " + e.getMessage());
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			AuditService.denyResult(audit, "Failed to add user: " + e.getMessage());
			logger.error("Error creating user " + userName + ": " + e.getMessage());
			e.printStackTrace();

		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			AuditService.denyResult(audit, "Failed to add user: " + e.getMessage());
			logger.error("Error creating user " + userName + ": " + e.getMessage());
			e.printStackTrace();

		}
		return out_bool;
	}
	
}
