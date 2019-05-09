package org.cote.accountmanager.data.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.MessageFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.security.ApiClientConfigurationBean;
import org.cote.accountmanager.data.security.ApiConnectionConfigurationService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AlertEnumType;
import org.cote.accountmanager.objects.ApiServiceEnumType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.QueueLevelEnumType;
import org.cote.accountmanager.objects.QueueStatusEnumType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.SpoolStatusEnumType;
import org.cote.accountmanager.util.DataUtil;

public class FirstContactMessageService {
	
	public static final Logger logger = LogManager.getLogger(FirstContactMessageService.class);
	/*
	 * Message Level controls how deep the communication chain will be built before forking into the remainder
	 * If set to none, then no chain is built and all messages sent to an ALERT are sent at the REMAINDER level
	 * Sending everything to REMAINDER is not desirable as that effectively spams the message
	 * By building up communication chains, this allows each tier to vet the information before moving the message forward
	 */
	public static QueueLevelEnumType messageLevel = QueueLevelEnumType.SECONDARY;
	public static MessageSpoolType newEvent(UserType user, AlertEnumType alert) throws FactoryException, ArgumentException{

		return newMessage(user,alert, QueueLevelEnumType.EVENT, messageLevel,null,null,null,null);
	}
	private static MessageSpoolType newMessage(UserType user, AlertEnumType alert, QueueLevelEnumType currentLevel,QueueLevelEnumType endLevel,MessageSpoolType parentMessage,PersonType person,ContactType contact,DataType data) throws FactoryException, ArgumentException{
		MessageSpoolType message = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).newMessage(user);
		if(parentMessage != null){
			message.setParentGuid(parentMessage.getGuid());
		}
		if(person != null && contact != null){
			message.setRecipientId(person.getId());
			message.setRecipientType(FactoryEnumType.PERSON);
			message.setTransportId(contact.getId());
			message.setTransportType(FactoryEnumType.CONTACT);
		}
		if(data != null){
			message.setReferenceId(data.getId());
			message.setReferenceType(FactoryEnumType.DATA);
		}
		message.setName(getQueueLevelName(alert, currentLevel));
		message.setSpoolBucketName((currentLevel == QueueLevelEnumType.EVENT ? SpoolNameEnumType.QUEUE : SpoolNameEnumType.MESSAGE));
		message.setSpoolStatus(SpoolStatusEnumType.QUEUED);
		message.setClassification(alert.toString());
		message.setCurrentLevel(getQueueLevelValue(currentLevel));
		message.setEndLevel(getQueueLevelValue(endLevel));
		((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).addMessage(message);
		return message;
	}
	public static List<MessageSpoolType> getEvents(UserType user, AlertEnumType aType, SpoolStatusEnumType status, long organizationId) throws FactoryException, ArgumentException{
		return getMessagesAtLevel(user, aType, null, QueueLevelEnumType.EVENT,false,SpoolNameEnumType.QUEUE,status,new SpoolStatusEnumType[0],organizationId);
	}

	/// Returns messages at the specified level for the specified parent with a given status, and optionally not including the filter statuses
	/// NOTE
	///		SpoolFactory includes a hard coded pagination on all get requests
	///		This needs to be refactored.
	public static List<MessageSpoolType> getMessagesAtLevel(
			UserType user,
			AlertEnumType aType,
			MessageSpoolType parentMessage,
			QueueLevelEnumType level,
			boolean filterLevelVal,
			SpoolNameEnumType spoolType,
			SpoolStatusEnumType status,
			SpoolStatusEnumType[] filterNotStatus,
			long organizationId
	) throws FactoryException, ArgumentException{
		
		int levelVal = getQueueLevelValue(level);
		DirectoryGroupType messageGroup = null;
		List<QueryField> fields = new ArrayList<QueryField>();
		if(user != null){
			messageGroup = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getUserMessagesGroup(user);
			fields.add(QueryFields.getFieldGroup(messageGroup.getId()));
		}
		
		fields.add(QueryFields.getFieldSpoolBucketName(spoolType));
		
		if(filterLevelVal) fields.add(QueryFields.getFieldCurrentLevel(levelVal));
		if(user != null) fields.add(QueryFields.getFieldOwner(user.getId()));
		fields.add(QueryFields.getFieldName(getQueueLevelName(aType, level)));
		if(status != SpoolStatusEnumType.UNKNOWN){
			fields.add(QueryFields.getFieldSpoolStatus(status));
		}
		if(filterNotStatus.length > 0){
			for(int i = 0; i < filterNotStatus.length;i++){
				QueryField statusField = QueryFields.getFieldSpoolStatus(filterNotStatus[i]);
				statusField.setComparator(ComparatorEnumType.NOT_EQUALS);
				fields.add(statusField);
			}
		}
		if(parentMessage != null) fields.add(QueryFields.getFieldParentGuid(parentMessage.getGuid()));
		return ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessages(fields.toArray(new QueryField[0]), 0, organizationId);
		
	}
	/// Returns messages at the specified level for the specified parent with a given status, and optionally not including the filter statuses
	/// NOTE
	///		SpoolFactory includes a hard coded pagination on all get requests
	///		This needs to be refactored.
	public static List<MessageSpoolType> getMessagesForContact(
			UserType user,
			MessageSpoolType parentMessage,
			ContactType contact,
			SpoolStatusEnumType status
	) throws FactoryException, ArgumentException{
		
		DirectoryGroupType messageGroup = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getUserMessagesGroup(user);
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldSpoolBucketName(SpoolNameEnumType.MESSAGE));
		fields.add(QueryFields.getFieldGroup(messageGroup.getId()));
		fields.add(QueryFields.getFieldTransportId(contact.getId()));
		fields.add(QueryFields.getFieldTransportType(FactoryEnumType.CONTACT));
		fields.add(QueryFields.getFieldOwner(user.getId()));
		if(status != SpoolStatusEnumType.UNKNOWN){
			fields.add(QueryFields.getFieldSpoolStatus(status));
		}

		if(parentMessage != null) fields.add(QueryFields.getFieldParentGuid(parentMessage.getGuid()));
		return ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessages(fields.toArray(new QueryField[0]), 0, user.getOrganizationId());
		
	}
	
	/// 2015/07/10 - The Spool API needs to be updated.  The API is older in that it forces pagination, and thereby sorts by created date in ascending order
	/// So latest is only the last entry of the first page.
	///
	public static MessageSpoolType getLatestEvent(UserType user, AlertEnumType aType) throws FactoryException, ArgumentException{
		List<MessageSpoolType> messages = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessagesFromUserGroup(getQueueLevelName(aType, QueueLevelEnumType.EVENT),SpoolNameEnumType.QUEUE,SpoolStatusEnumType.UNKNOWN, null,user);
		int size = messages.size();
		if(size == 0) return null;
		return messages.get(size-1);
	}
	public static boolean deleteMessage(MessageSpoolType message) throws FactoryException{
		return ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).deleteMessage(message);
	}
	public static boolean pruneEventQueue(UserType user, AlertEnumType aType) throws FactoryException, ArgumentException{

		List<MessageSpoolType> messages = getEvents(user, aType,SpoolStatusEnumType.UNKNOWN,user.getOrganizationId());
		int len = (messages.size() - 1);
		for(int i = 0; i < len;i++){
			((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).deleteMessage(messages.get(i));
		}
		return (len > 0);

	}
	public static QueueLevelEnumType getQueueLevelFromValue(int level){
		QueueLevelEnumType outVal = QueueLevelEnumType.UNKNOWN;
		switch(level){
			case 0:
				outVal = QueueLevelEnumType.NONE;
				break;
			case 1: outVal = QueueLevelEnumType.EVENT;break;
			case 2: outVal = QueueLevelEnumType.PRIMARY;break;
			case 3: outVal = QueueLevelEnumType.SECONDARY;break;
			case 4: outVal = QueueLevelEnumType.TERTIARY;break;
			case 5: outVal = QueueLevelEnumType.QUARTERNARY;break;
			case 6: outVal = QueueLevelEnumType.REMAINDER;break;
			default:
				logger.error("Unhandled level: " + level);
				outVal = QueueLevelEnumType.UNKNOWN;
				break;
		}
		return outVal;
	}
	public static int getQueueLevelValue(QueueLevelEnumType level){
		int outVal = 0;
		switch(level){
			case NONE:
				outVal = 0;
				break;
			case EVENT: outVal = 1;break;
			case PRIMARY: outVal = 2;break;
			case SECONDARY: outVal = 3;break;
			case TERTIARY: outVal = 4;break;
			case QUARTERNARY: outVal = 5;break;
			case REMAINDER: outVal = 6;break;
			case UNKNOWN:
			default:
				logger.error("Unhandled level: " + level.toString());
				outVal = -1;
				break;
		}
		return outVal;
	}
	public static String getQueueLevelName(AlertEnumType aType, QueueLevelEnumType type){
		String outName = null;
		switch(type){
			case EVENT: outName = "Event Queue";break;
			case PRIMARY: outName = "Primary Contact Message";break;
			case SECONDARY: outName = "Secondary Contact Message";break;
			case TERTIARY: outName = "Tertiary Contact Message";break;
			case QUARTERNARY: outName = "Quarternary Contact Message";break;
			case REMAINDER: outName = "Remainder Contact Message";break;
			case UNKNOWN:
			default:
				break;
		}
		if(outName != null) outName = aType.toString() + " " + outName;
		return outName;
	}
	public static QueueStatusEnumType getQueueStatus(UserType user, AlertEnumType aType, QueueLevelEnumType level){
		QueueStatusEnumType outType = QueueStatusEnumType.UNKNOWN;
		try {
			String name = getQueueLevelName(aType,level);
			SpoolNameEnumType type = (level == QueueLevelEnumType.EVENT ? SpoolNameEnumType.QUEUE : SpoolNameEnumType.MESSAGE);
			List<MessageSpoolType> messages = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessagesFromUserGroup(name,type,SpoolStatusEnumType.QUEUED, null,user);
			int size = messages.size();
			if(size == 0){
				outType = QueueStatusEnumType.EMPTY;
			}
			else{
				switch(level){
					case EVENT:
						if(size == 1){
							outType = QueueStatusEnumType.POPULATED;
						}
						else{
							logger.warn("There is more than one queued alert of type " + aType.toString());
							outType = QueueStatusEnumType.DIRTY;
						}
						break;
					default:
						logger.error("TODO: Identity queue status for level: " + level.toString());
						break;
				}
			}
			for(int i = 0; i < messages.size(); i++){
				logger.info(messages.get(i).getName() + " " + messages.get(i).getCreated());
			}
		} catch (FactoryException | ArgumentException e) {
			outType = QueueStatusEnumType.ERROR;
			e.printStackTrace();
		}
		return outType;
	}
	
	/*
	 * The alert notification structure is:
	 * USER->Alert Queue
	 * 	Alert Queue -> Primary Message Block
	 * 		Primary Message Block -> Secondary Message Block
	 * 			Secondary Message Block -> Tertiary Message Block
	 * 				Tertiary Message Block -> Quarternary Message Block
	 * 					Quarternary Message Block -> Remainder Block
	 *
	 *	Only Primary and Secondary are named by role participation.  Tertiary, Quarternary, and Remainder are created by a random sample.
	 *  
	 *  Message Processing can happen in two modes:
	 *  1) Spam all primary contact for every person, then supplemental contacts until acknowledgement is received
	 *  2) Send all primary contact for persons in the primary contact role, then proceed through the hierarchy
	 */
	
	public static boolean canCreateNotifications(MessageSpoolType message){
		boolean outBool = false;

		if(message.getSpoolBucketName() == SpoolNameEnumType.QUEUE
			&&
			(
				message.getSpoolStatus() == SpoolStatusEnumType.QUEUED
				||
				message.getSpoolStatus() == SpoolStatusEnumType.SPOOLING
			)
		){
			outBool = true;
		}
		return outBool;
	}

	public static boolean createNotifications(MessageSpoolType message) {
		
		boolean outBool = false;
		boolean error = false;
		DataType messageTemplate = null;
		DirectoryGroupType dir = null;
		if(!canCreateNotifications(message)){
			logger.warn("Message is not in a queued or spooling status.");
			return outBool;
		}
		
		AlertEnumType alert = (message.getClassification() != null ? AlertEnumType.valueOf(message.getClassification()) : AlertEnumType.UNKNOWN);
		QueueLevelEnumType useLevel = QueueLevelEnumType.UNKNOWN;
		QueueLevelEnumType currentLevel = getQueueLevelFromValue(message.getCurrentLevel());
		QueueLevelEnumType endLevel = getQueueLevelFromValue(message.getEndLevel());
		if(alert == AlertEnumType.UNKNOWN || currentLevel == QueueLevelEnumType.UNKNOWN || endLevel == QueueLevelEnumType.UNKNOWN){
			logger.error("Invalid message levels for alert " + alert.toString() + ".  Current level is " + currentLevel.toString() + " and end level is " + endLevel.toString());
			return outBool;
		}

		try{
			
			UserType owner = Factories.getNameIdFactory(FactoryEnumType.USER).getById(message.getOwnerId(), message.getOrganizationId());
			if(owner == null){
				logger.error("Null owner for message " + message.getName());
				return outBool;
			}

			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Data", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserDirectory(owner), owner.getOrganizationId()) ;
			if(dir == null){
				logger.error("Null data directory");
				return outBool;
			}
			
			useLevel = (endLevel == QueueLevelEnumType.REMAINDER ? endLevel : getQueueLevelFromValue(message.getCurrentLevel() + 1));
			
			
			List<MessageSpoolType> pendingMessages = getMessagesAtLevel(owner, alert, message, currentLevel, true, SpoolNameEnumType.MESSAGE,SpoolStatusEnumType.QUEUED, new SpoolStatusEnumType[]{},owner.getOrganizationId());
			logger.info("Check for messages at level " + currentLevel.toString() + " : " + pendingMessages.size());
			if(!pendingMessages.isEmpty()){
				logger.error("No new messages will be created because message " + message.getName() + " queue at level " + currentLevel.toString() + " is pending transmission.");
				return outBool;
			}

			/// Take the queued message, build up the primary message list,
			/// And move it into a transmitted state
			///
			if(message.getCurrentLevel() < message.getEndLevel()){
				message.setCurrentLevel(getQueueLevelValue(useLevel));
				List<PersonType> persons = getPersonsAtLevel(owner,useLevel);
				logger.info("Preparing " + message.getName() + " queue for " + persons.size() + " recipients");
				message.setSpoolStatus(SpoolStatusEnumType.SPOOLING);
				for(int i = 0; i < persons.size();i++){
					PersonType person = persons.get(i);
					if(person.getContactInformation() == null){
						logger.error("Person " + person.getName() + " does not define a contact information object");
						continue;
					}
					ContactType contact = getNextContactType(owner, message, person);
					if(contact == null){
						logger.error("Person " + person.getName() + " does not define any contact information");
						continue;
					}
					String sctype = contact.getContactType().toString();
					String templateName = alert.toString() + " " + sctype.substring(0,1) + sctype.substring(1,sctype.length()).toLowerCase();
					messageTemplate = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(templateName, false, dir);
					if(messageTemplate == null){
						logger.error("Data Template " + templateName + " not found");
						continue;
					}
					logger.info(contact.getLocationType().toString() + " " + contact.getContactType().toString() + " " + contact.getContactValue());
					newMessage(owner,alert, useLevel, useLevel,message,person,contact,messageTemplate);
				}
				outBool = true;

			}
			// remainder
			else{
				logger.info("TODO: Manager remainder");
			}
		}
		catch(FactoryException | DataAccessException | ArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			error = true;
		}
		finally{
			try {
				if(error){
					message.setSpoolStatus(SpoolStatusEnumType.ERROR);
				}
				((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).updateMessage(message);
			} catch (FactoryException e) {
				logger.error(e.getMessage());
			}
		}

		
		return outBool;
		
	}
	public static boolean canProcessNotifications(MessageSpoolType message){
		boolean outBool = false;

		if(message.getSpoolBucketName() == SpoolNameEnumType.QUEUE
			&&
			(
				message.getSpoolStatus() == SpoolStatusEnumType.SPOOLING
				||
				message.getSpoolStatus() == SpoolStatusEnumType.SPOOLED
			)
		){
			outBool = true;
		}
		return outBool;
	}
	public static boolean processNotifications(MessageSpoolType message) {
		
		boolean outBool = false;
		boolean error = false;
		
		if(canProcessNotifications(message) == false){
			logger.warn("Message is not in a queued or spooling status.");
			return outBool;
		}
		
		AlertEnumType alert = (message.getClassification() != null ? AlertEnumType.valueOf(message.getClassification()) : AlertEnumType.UNKNOWN);
		QueueLevelEnumType currentLevel = getQueueLevelFromValue(message.getCurrentLevel());
		if(alert == AlertEnumType.UNKNOWN || currentLevel == QueueLevelEnumType.UNKNOWN){
			logger.error("Invalid message levels for alert " + alert.toString() + ".  Current level is " + currentLevel.toString());
			return outBool;
		}

		try{
			UserType owner = Factories.getNameIdFactory(FactoryEnumType.USER).getById(message.getOwnerId(), message.getOrganizationId());
			if(owner == null){
				logger.error("Null owner for message " + message.getName());
				return outBool;
			}

			List<MessageSpoolType> pendingMessages = getMessagesAtLevel(owner, alert, message, currentLevel, true, SpoolNameEnumType.MESSAGE,SpoolStatusEnumType.QUEUED, new SpoolStatusEnumType[]{},owner.getOrganizationId());
			logger.info("Check for messages at level " + currentLevel.toString() + " : " + pendingMessages.size());
			if(pendingMessages.isEmpty()){
				logger.error("No new messages will be processed.");
				return outBool;
			}
			Map<ContactEnumType,List<ContactType>> contactMap = new HashMap<>();
			int invalidEntries = 0;
			for(int i = 0; i < pendingMessages.size(); i++){
				MessageSpoolType pendingMessage = pendingMessages.get(i);
				SpoolStatusEnumType newState = SpoolStatusEnumType.TRANSMITTING;
				if(pendingMessage.getTransportType() != FactoryEnumType.CONTACT || pendingMessage.getTransportId().compareTo(0L) <= 0){
					logger.error("Invalid transport value: " + pendingMessage.getTransportType().toString() + " " + pendingMessage.getTransportId().toString());
					newState = SpoolStatusEnumType.ERROR;
					continue;
				}
				if(pendingMessage.getReferenceType() != FactoryEnumType.DATA || pendingMessage.getReferenceId().compareTo(0L) <= 0){
					logger.error("Invalid reference value: " + pendingMessage.getReferenceType().toString() + " " + pendingMessage.getReferenceId().toString());
					newState = SpoolStatusEnumType.ERROR;
					continue;					
				}
				if(pendingMessage.getRecipientType() != FactoryEnumType.PERSON || pendingMessage.getRecipientId().compareTo(0L) <= 0){
					logger.error("Invalid recipient value: " + pendingMessage.getRecipientType().toString() + " " + pendingMessage.getRecipientId().toString());
					newState = SpoolStatusEnumType.ERROR;
					continue;					
				}
				PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getById(pendingMessage.getRecipientId(), owner.getOrganizationId());
				if(person == null){
					logger.error("Null transport for: " + pendingMessage.getRecipientType().toString() + " " + pendingMessage.getRecipientId().toString());
					newState = SpoolStatusEnumType.ERROR;
					continue;
				}
				
				ContactType contact = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).getById(pendingMessage.getTransportId(), owner.getOrganizationId());
				if(contact == null){
					logger.error("Null transport for: " + pendingMessage.getTransportType().toString() + " " + pendingMessage.getTransportId().toString());
					newState = SpoolStatusEnumType.ERROR;
					continue;
				}
				DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getById(pendingMessage.getReferenceId(), owner.getOrganizationId());
				if(data == null){
					logger.error("Null reference for: " + pendingMessage.getReferenceType().toString() + " " + pendingMessage.getReferenceId().toString());
					newState = SpoolStatusEnumType.ERROR;
					continue;
				}
				if(newState == SpoolStatusEnumType.TRANSMITTING){
					/// Update to reflect temporary state
					///
					pendingMessage.setSpoolStatus(newState);
					((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).updateMessage(pendingMessage);
					if(contactMap.containsKey(contact.getContactType())==false) contactMap.put(contact.getContactType(), new ArrayList<ContactType>());
					contactMap.get(contact.getContactType()).add(contact);
					
					if(transmitToContact(owner,message,pendingMessage,person,contact,data)){
						newState = SpoolStatusEnumType.TRANSMITTED;
					}
					else{
						newState = SpoolStatusEnumType.ERROR;
					}
					/// Update to reflect end state
					///
					pendingMessage.setSpoolStatus(newState);
					((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).updateMessage(pendingMessage);
					
					
					
				} // end for
				/*
				if(contactMap.containsKey(ContactEnumType.EMAIL)){
					EmailService.sendEmail(contacts, title, content)
				}
				else{
					logger.info("No Email Messages.");
				}
				
				if(contactMap.containsKey(ContactEnumType.PHONE)){
					logger.info("TODO: Implement Phone/Cell TRANSMIT");
				}
				else{
					logger.info("No Phone/Cell Messages.");
				}
				*/
				if(newState == SpoolStatusEnumType.ERROR){
					invalidEntries++;
				}
				else{
					outBool = true;
				}
				
			} // end for
			if(invalidEntries > 0){
				logger.error("Invalid entries detected.  Entries moved into ERROR state.");
			}
		}
		catch(FactoryException | DataException | ArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			error = true;
		}
		finally{
			try {
				if(error){
					message.setSpoolStatus(SpoolStatusEnumType.ERROR);
				}
				((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).updateMessage(message);
			} catch (FactoryException e) {
				logger.error(e.getMessage());
			}
		}

		
		return outBool;
		
	}
	protected static ContactType getContactForPersonUser(UserType user,ContactEnumType contactType) throws FactoryException, ArgumentException{
		PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getPersonByUser(user);
		if(person == null) return null;
		((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
		return ContactService.getPreferredContact(person.getContactInformation(), contactType, LocationEnumType.UNKNOWN);
	}
	protected static boolean transmitToContact(UserType owner, MessageSpoolType message, MessageSpoolType pendingMessage, PersonType person, ContactType contact, DataType data) throws DataException, FactoryException, ArgumentException{
		boolean outBool = false;
		logger.info("TRANSMIT " + contact.getContactType().toString() + " " + contact.getContactValue());
		if(contact.getContactType() == ContactEnumType.EMAIL){
			String title = "First Alert " + message.getClassification() + " Notification";
			String content = DataUtil.getValueString(data);
			
			content = content.replaceAll("\\$\\{person\\.name\\}", owner.getName());
			content = content.replaceAll("\\$\\{contact\\.name\\}", person.getName());
			
			ContactType ownerContact = getContactForPersonUser(owner,ContactEnumType.EMAIL);
			if(ownerContact == null){
				logger.error("Owner does not define an email address");
				return outBool;
			}
			ApiClientConfigurationBean apiConfig = ApiConnectionConfigurationService.getApiClientConfiguration(ApiServiceEnumType.EMAIL,ApiConnectionConfigurationService.getApiEmailConfigName(),owner.getOrganizationId());
			outBool = EmailService.sendEmail(apiConfig,ownerContact,new ContactType[]{contact}, title, content);
		}
		else{
			logger.info("TODO: Implement TRANSMIT for " + contact.getContactType().toString());
		}
		return outBool;
	}
	/// Check if a message of the given status was already created for the alert message, for the given contact
	///
	public static boolean getTransmittedToContact(UserType owner, MessageSpoolType message, ContactType contact, SpoolStatusEnumType status){
		boolean outBool = false;
		try {
			List<MessageSpoolType> messages = getMessagesForContact(owner, message, contact,status);
			if(!messages.isEmpty()){
				logger.warn("Transmission redundancy warning: Found " + messages.size() + " message(s) using contact " + contact.getName() + " for message " + message.getName());
				outBool = true;
			}
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return outBool;
	}
	public static ContactType getNextContactType(UserType user, MessageSpoolType message, PersonType person){
		ContactType contact = null;

		ContactType mobileContact = ContactService.getPreferredMobileContact(person.getContactInformation());
		ContactType emailContact = ContactService.getPreferredEmailContact(person.getContactInformation());
		ContactType phoneContact = ContactService.getPreferredPhoneContact(person.getContactInformation());
		logger.info(person.getName() + " -- Phone=" + (phoneContact == null?"No":"Yes") + "/Cell=" + (mobileContact == null?"No":"Yes") + "/Email=" + (emailContact == null?"No":"Yes"));
		/// Refactor this to pull all messages for the person for this message, and then check the contact use there
		///
		if((phoneContact == null && mobileContact == null) || (emailContact.getPreferred() && !getTransmittedToContact(user,message,emailContact,SpoolStatusEnumType.UNKNOWN))) contact = emailContact;
		else if(mobileContact == null || (phoneContact != null && phoneContact.getPreferred() && !getTransmittedToContact(user,message,phoneContact,SpoolStatusEnumType.UNKNOWN))) contact = phoneContact;
		else if(!getTransmittedToContact(user,message,mobileContact,SpoolStatusEnumType.UNKNOWN)) contact = mobileContact;
		if(contact == null){
			logger.warn("No contact was found for " + person.getName());
		}
		
		return contact;
	}
	public static List<PersonType> getPersonsAtLevel(UserType user, QueueLevelEnumType level) throws ArgumentException, FactoryException, DataAccessException{
		List<PersonType> persons = new ArrayList<PersonType>();
		PersonRoleType role = null;
		switch(level){
			case PRIMARY:
				role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreatePersonRole(user, "Primary Contacts", ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(user));
				if(role == null){
					throw new ArgumentException("User is not configured for expected roles");
				}
				persons = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getPersonsInRole(role));
				break;
			case SECONDARY:
				role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreatePersonRole(user, "Secondary Contacts", ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(user));
				if(role == null){
					throw new ArgumentException("User is not configured for expected roles");
				}
				persons = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getPersonsInRole(role));
				break;
			case TERTIARY:
			case QUARTERNARY:
				logger.warn("Level " + level.toString() + " not implemented");
				break;
			case REMAINDER:
				/// Everyone else who wasn't already chosen
				/// This is basically a list of people for the current message set who don't already have a message
				///
				logger.warn("Remainder not implemented");
				break;
			default:
				throw new ArgumentException("Invalid level: " + level.toString());
		}
		return persons;
	}
	
}
