package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.MessageFactory;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.junit.Test;
public class TestMessageSpool extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestMessageSpool.class);

	@Test
	public void testInsertMessage(){
		assertNotNull("User is null", testUser);
		MessageSpoolType message = null;
		boolean add_message = false;
		try{
			message = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).newMessage(testUser);
			message.setName("testInsertMessage");
			add_message = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).addMessage(message);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Message was not added", add_message);
	}
	
	@Test
	public void testGetMessages(){
		assertNotNull("User is null", testUser);
		List<MessageSpoolType> messages = new ArrayList<MessageSpoolType>();
		try{
			messages = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessagesFromUserGroup(SpoolNameEnumType.GENERAL, testUser);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
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
			message = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).newMessage(testUser);
			message.setName("testMessageByDate");
			add_message = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).addMessage(message);
			assertTrue(add_message);
			XMLGregorianCalendar xCal = CalendarUtil.getXmlGregorianCalendar(now.getTime());
			Date xCalCheck = CalendarUtil.getDate(xCal);
			logger.info(xCal.toString() + " :: " + xCalCheck.toString());
			messages = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessagesAfterDate(SpoolNameEnumType.GENERAL, xCal, 0, testUser.getOrganizationId());
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Expected 1 message, received " + messages.size() + " messages", messages.size() == 1);
	}
	
	@Test
	public void testUpdateMessage(){
		assertNotNull("User is null", testUser);
		List<MessageSpoolType> messages = new ArrayList<MessageSpoolType>();
		try{
			messages = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessagesFromUserGroup(SpoolNameEnumType.GENERAL, testUser);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Expected one or more messages", messages.size() > 0);
		MessageSpoolType message = messages.get(0);
		message.setData("Example data".getBytes());
		message.setValueType(ValueEnumType.STRING);
		boolean updated = false;
		try{
			updated = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).update(message);
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
			deleted = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).deleteMessagesInGroup(SpoolNameEnumType.GENERAL, ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getUserMessagesGroup(testUser));
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Failed to deleted", deleted);
	}
	
	
}