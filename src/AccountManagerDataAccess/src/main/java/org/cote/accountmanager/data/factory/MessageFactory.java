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
package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.SpoolStatusEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class MessageFactory extends SpoolFactory {

	public MessageFactory()
	{
		super();
		this.factoryType = FactoryEnumType.MESSAGE;
	}

	public boolean deleteMessage(MessageSpoolType message) throws FactoryException
	{
		removeFromCache(message);
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldObjectId(message.getObjectId()) }, message.getOrganizationId());
		return (deleted > 0);
	}
	public boolean deleteMessagesForUser(UserType user) throws FactoryException
	{

		clearCache();
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldOwner(user.getOwnerId()) }, user.getOrganizationId());
		return (deleted > 0);
	}
	public boolean deleteMessagesInGroup(SpoolNameEnumType queue, DirectoryGroupType group) throws FactoryException
	{

		clearCache();
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldSpoolBucketName(queue), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE), QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		return (deleted > 0);
	}

	public boolean updateMessage(MessageSpoolType message) throws FactoryException
	{
		return update(message);
	}

	public MessageSpoolType newMessage(UserType owner) throws FactoryException, ArgumentException
	{
		return newMessage(SpoolNameEnumType.GENERAL,owner);
	}
	public MessageSpoolType newMessage(SpoolNameEnumType queue, UserType owner) throws FactoryException, ArgumentException
	{
		return newMessage(queue, owner, getUserMessagesGroup(owner).getId());
	}
	public MessageSpoolType newMessage(SpoolNameEnumType queue, UserType owner, long groupId) throws ArgumentException
	{
		MessageSpoolType newMessage = (MessageSpoolType)newSpoolEntry(SpoolBucketEnumType.MESSAGE_QUEUE);
		newMessage.setSpoolBucketName(queue);
		newMessage.setOwnerId(owner.getId());
		newMessage.setOrganizationId(owner.getOrganizationId());
		newMessage.setGroupId(groupId);
		newMessage.setSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE);
		newMessage.setValueType(ValueEnumType.UNKNOWN);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		newMessage.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
		newMessage.setExpires(true);
		return newMessage;
	}
	
	public DirectoryGroupType getUserMessagesGroup(UserType user) throws FactoryException, ArgumentException
	{
		return ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(user, ".messages");
	}

	public List<MessageSpoolType> getMessagesAfterDate(SpoolNameEnumType queue, XMLGregorianCalendar startDate, long startIndex, long organizationId) throws FactoryException, ArgumentException
	{
		QueryField dateField = QueryFields.getFieldCreatedDate(startDate);
		dateField.setComparator(ComparatorEnumType.GREATER_THAN_OR_EQUALS);
		return getMessages(new QueryField[] { QueryFields.getFieldSpoolBucketName(queue), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE), dateField }, startIndex, defaultPageSize, organizationId);
	}
	public MessageSpoolType getMessageByName(SpoolNameEnumType queue, String name, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		List<BaseSpoolType> messages = getByField(new QueryField[] { QueryFields.getFieldSpoolBucketName(queue), QueryFields.getFieldName(name), QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		if (messages.isEmpty()) return null;
		return (MessageSpoolType)messages.get(0);
	}
	public MessageSpoolType getMessageByObjectId(String guid, long organizationId) throws FactoryException, ArgumentException
	{
		List<BaseSpoolType> messages = getByField(new QueryField[] { QueryFields.getFieldObjectId(guid) }, organizationId);
		if (messages.isEmpty()) return null;
		return (MessageSpoolType)messages.get(0);
	}
	public List<MessageSpoolType> getMessages(SpoolNameEnumType queue, long organizationId) throws FactoryException, ArgumentException
	{
		return getMessages(queue, 1, organizationId);
	}
	public List<MessageSpoolType> getMessages(SpoolNameEnumType queue, long startIndex, long organizationId) throws FactoryException, ArgumentException
	{
		return getMessages(new QueryField[] { QueryFields.getFieldSpoolBucketName(queue), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE) }, startIndex, defaultPageSize, organizationId);
	}
	public List<MessageSpoolType> getMessagesFromUserGroup(SpoolNameEnumType queue, UserType user) throws FactoryException, ArgumentException
	{
		return getMessagesFromUserGroup(null,queue, SpoolStatusEnumType.UNKNOWN, null,user);
	}
	public List<MessageSpoolType> getMessagesFromUserGroup(String name, SpoolNameEnumType queue, SpoolStatusEnumType status,MessageSpoolType parentMessage,UserType user) throws FactoryException, ArgumentException
	{
		return getMessagesFromUserGroup(name, queue, status, user, parentMessage,getUserMessagesGroup(user));
	}
	public List<MessageSpoolType> getMessagesFromUserGroup(String name, SpoolNameEnumType queue, SpoolStatusEnumType status, UserType user, MessageSpoolType parentMessage,DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		/*
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldSpoolBucketName(queue));
		fields.add(QueryFields.getFieldGroup(group.getId()));
		fields.add(QueryFields.getFieldOwner(user.getId()));
		if(name != null) fields.add(QueryFields.getFieldName(name));
		if(status != SpoolStatusEnumType.UNKNOWN) fields.add(QueryFields.getFieldSpoolStatus(status));
		if(parentMessage != null) fields.add(QueryFields.getFieldParentObjectId(parentMessage.getObjectId()));
		*/
		List<QueryField> fields = getUserGroupQueryFields(user, SpoolBucketEnumType.UNKNOWN, queue, status, group, (parentMessage != null ? parentMessage.getObjectId() : null), name);
		return getMessages(fields.toArray(new QueryField[0]), 0, defaultPageSize, user.getOrganizationId());
	}
	public List<MessageSpoolType> getMessages(QueryField[] fields, long startIndex, int recordCount, long organizationId) throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = getPagingInstruction(startIndex, recordCount);
		List<BaseSpoolType> messages = getByField(fields, instruction, organizationId);
		return convertList(messages);
	}
	public List<QueryField> getUserGroupQueryFields(
			UserType user,
			SpoolBucketEnumType spoolBucket,
			SpoolNameEnumType spoolType,
			SpoolStatusEnumType spoolStatus,
			DirectoryGroupType spoolGroup,
			String ParentObjectId,
			String name
	){
		List<QueryField> fields = new ArrayList<>();
		if(spoolBucket != SpoolBucketEnumType.UNKNOWN) fields.add(QueryFields.getFieldSpoolBucketType(spoolBucket));
		if(spoolType != SpoolNameEnumType.UNKNOWN) fields.add(QueryFields.getFieldSpoolBucketName(spoolType));
		fields.add(QueryFields.getFieldGroup(spoolGroup.getId()));
		if(user != null) fields.add(QueryFields.getFieldOwner(user.getId()));
		if(name != null) fields.add(QueryFields.getFieldName(name));
		if(spoolStatus != SpoolStatusEnumType.UNKNOWN) fields.add(QueryFields.getFieldSpoolStatus(spoolStatus));
		if(ParentObjectId != null) fields.add(QueryFields.getFieldParentObjectId(ParentObjectId));
		return fields;
		
	}
	public List<QueryField> getQueryFields(
			UserType owner,
			SpoolBucketEnumType spoolBucket,
			SpoolNameEnumType spoolType,
			SpoolStatusEnumType spoolStatus,
			DirectoryGroupType spoolGroup,
			String name,
			FactoryEnumType referenceType,
			long referenceId,
			FactoryEnumType recipientType,
			long recipientId,
			FactoryEnumType transportType,
			long transportId
	){
		List<QueryField> fields = new ArrayList<>();
		if(spoolBucket != SpoolBucketEnumType.UNKNOWN) fields.add(QueryFields.getFieldSpoolBucketType(spoolBucket));
		if(spoolType != SpoolNameEnumType.UNKNOWN) fields.add(QueryFields.getFieldSpoolBucketName(spoolType));
		if(spoolStatus != SpoolStatusEnumType.UNKNOWN) fields.add(QueryFields.getFieldSpoolStatus(spoolStatus));
		if(referenceType != FactoryEnumType.UNKNOWN) fields.add(QueryFields.getFieldReferenceType(referenceType));
		if(referenceId != 0L) fields.add(QueryFields.getFieldReferenceId(referenceId));
		if(recipientType != FactoryEnumType.UNKNOWN) fields.add(QueryFields.getFieldRecipientType(recipientType));
		if(recipientId != 0L) fields.add(QueryFields.getFieldRecipientId(recipientId));
		if(transportType != FactoryEnumType.UNKNOWN) fields.add(QueryFields.getFieldTransportType(transportType));
		if(transportId != 0L) fields.add(QueryFields.getFieldTransportId(transportId));
		if(owner != null) fields.add(QueryFields.getFieldOwner(owner.getId()));
		if(name != null) fields.add(QueryFields.getFieldName(name));
		if(spoolStatus != SpoolStatusEnumType.UNKNOWN) fields.add(QueryFields.getFieldSpoolStatus(spoolStatus));
		if(spoolGroup != null) fields.add(QueryFields.getFieldGroup(spoolGroup.getId()));
		return fields;
	}

	public List<MessageSpoolType> getMessages(
			UserType owner,
			SpoolBucketEnumType spoolBucket,
			SpoolNameEnumType spoolType,
			SpoolStatusEnumType spoolStatus,
			DirectoryGroupType spoolGroup,
			String name,
			FactoryEnumType referenceType,
			long referenceId,
			FactoryEnumType recipientType,
			long recipientId,
			FactoryEnumType transportType,
			long transportId,
			long organizationId
	) throws FactoryException, ArgumentException{
		List<QueryField> fields = getQueryFields(owner, spoolBucket, spoolType, spoolStatus, spoolGroup, name, referenceType, referenceId, recipientType, recipientId, transportType, transportId);
		
		return getMessages(fields.toArray(new QueryField[0]), 0, defaultPageSize, organizationId);
	}
	
	public <T> List<T> listMessages(QueryField[] fields, long startIndex, int recordCount, long organizationId) throws FactoryException, ArgumentException {
		return convertList(getMessages(fields, startIndex, recordCount, organizationId));
	}
	
	public int countMessages(QueryField[] fields, long organizationId) throws FactoryException{
		return this.getCountByField(getDataTable(primaryTableName), fields, organizationId);
	}
	
	@Override
	protected BaseSpoolType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
			MessageSpoolType newMessage = new MessageSpoolType();
			newMessage.setNameType(NameEnumType.MESSAGE);
			return super.read(rset, newMessage);
	}


	public boolean addMessage(MessageSpoolType newMessage) throws FactoryException
	{
		if(!isValid(newMessage)) throw new FactoryException("Message does not contain valid data.");
		return insertRow(prepareAdd(newMessage,this.primaryTableName));
	}

	public boolean isValid(BaseSpoolType message)
	{
		return !(
			message == null
			|| message.getOrganizationId() <= 0L
			|| message.getGroupId().compareTo(0L) == 0
			|| message.getSpoolBucketName() == null
			|| message.getSpoolBucketType() == null
			|| message.getName() == null
			|| message.getObjectId() == null
			|| message.getOwnerId() <= 0L
		);
	}

}
