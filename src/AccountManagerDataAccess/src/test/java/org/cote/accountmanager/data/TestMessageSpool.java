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
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
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
		logger.info("Test insert message");
		assertNotNull("User is null", testUser);
		MessageSpoolType message = null;
		boolean add_message = false;
		try{
			logger.info("Bulk mode: " + ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getBulkMode());

			message = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).newMessage(testUser);
			message.setName("testInsertMessage");
			add_message = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).addMessage(message);
		}
		catch(FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Message was not added", add_message);
	}
	
	@Test
	public void testGetMessages(){
		testInsertMessage();
		logger.info("Test get message");
		assertNotNull("User is null", testUser);
		List<MessageSpoolType> messages = new ArrayList<>();
		try{
			messages = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessagesFromUserGroup(SpoolNameEnumType.GENERAL, testUser);
		}
		catch(FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Expected one or more messages", messages.size() > 0);
	}
	
	@Test
	public void testGetMessagesAfterNow(){
		testInsertMessage();
		logger.info("Test get message after time");
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
		catch(FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Expected 1 message, received " + messages.size() + " messages", messages.size() == 1);
	}
	
	@Test
	public void testUpdateMessage(){
		testInsertMessage();
		logger.info("Test update message");
		assertNotNull("User is null", testUser);
		List<MessageSpoolType> messages = new ArrayList<MessageSpoolType>();
		try{
			messages = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getMessagesFromUserGroup(SpoolNameEnumType.GENERAL, testUser);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
		testInsertMessage();
		logger.info("Test delete message");
		assertNotNull("User is null", testUser);
		boolean deleted = false;
		try{
			deleted = ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).deleteMessagesInGroup(SpoolNameEnumType.GENERAL, ((MessageFactory)Factories.getFactory(FactoryEnumType.MESSAGE)).getUserMessagesGroup(testUser));
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Failed to deleted", deleted);
	}
	
	
}