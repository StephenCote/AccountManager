package org.cote.accountmanager.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.FactoryService;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestMessageSpool extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestMessageSpool.class.getName());

	@Test
	public void testInsertMessage(){
		assertNotNull("User is null", testUser);
		MessageSpoolType message = null;
		boolean add_message = false;
		try{
			message = Factories.getMessageFactory().newMessage(testUser);
			message.setName("testInsertMessage");
			add_message = Factories.getMessageFactory().addMessage(message);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Message was not added", add_message);
	}
	
	@Test
	public void testGetMessages(){
		assertNotNull("User is null", testUser);
		List<MessageSpoolType> messages = new ArrayList<MessageSpoolType>();
		try{
			messages = Factories.getMessageFactory().getMessagesFromUserGroup(SpoolNameEnumType.GENERAL, testUser);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Expected one or more messages", messages.size() > 0);
	}
	
	@Test
	public void testGetMessagesAfterNow(){
		Calendar now = Calendar.getInstance();
		//now.add(Calendar.HOUR, 7);
		assertNotNull("User is null", testUser);
		
		MessageSpoolType message = null;
		boolean add_message = false;
		List<MessageSpoolType> messages = new ArrayList<MessageSpoolType>();
		try{
			message = Factories.getMessageFactory().newMessage(testUser);
			message.setName("testMessageByDate");
			add_message = Factories.getMessageFactory().addMessage(message);
			assertTrue(add_message);
			XMLGregorianCalendar xCal = CalendarUtil.getXmlGregorianCalendar(now.getTime());
			Date xCalCheck = CalendarUtil.getDate(xCal);
			logger.info(xCal.toString() + " :: " + xCalCheck.toString());
			messages = Factories.getMessageFactory().getMessagesAfterDate(SpoolNameEnumType.GENERAL, xCal, 0, testUser.getOrganizationId());
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Expected 1 message, received " + messages.size() + " messages", messages.size() == 1);
	}
	
	@Test
	public void testUpdateMessage(){
		assertNotNull("User is null", testUser);
		List<MessageSpoolType> messages = new ArrayList<MessageSpoolType>();
		try{
			messages = Factories.getMessageFactory().getMessagesFromUserGroup(SpoolNameEnumType.GENERAL, testUser);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Expected one or more messages", messages.size() > 0);
		MessageSpoolType message = messages.get(0);
		message.setData("Example data".getBytes());
		message.setValueType(ValueEnumType.STRING);
		boolean updated = false;
		try{
			updated = Factories.getMessageFactory().update(message);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		assertTrue("Message was not updated", updated);
	}
	
	@Test
	public void testDeleteMessages(){
		assertNotNull("User is null", testUser);
		boolean deleted = false;
		try{
			deleted = Factories.getMessageFactory().deleteMessagesInGroup(SpoolNameEnumType.GENERAL, Factories.getMessageFactory().getUserMessagesGroup(testUser));
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Failed to deleted", deleted);
	}
	
	
}