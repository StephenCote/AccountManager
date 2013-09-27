package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class MessageFactory extends SpoolFactory {
	public MessageFactory()
	{
		super();
	}

	public boolean deleteMessage(MessageSpoolType message) throws FactoryException
	{
		removeFromCache(message);
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldSpoolGuid(message) }, message.getOrganization().getId());
		return (deleted > 0);
	}
	public boolean deleteMessagesForUser(UserType user) throws FactoryException
	{

		clearCache();
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldSpoolOwner(user.getOwnerId()) }, user.getOrganization().getId());
		return (deleted > 0);
	}
	public boolean deleteMessagesInGroup(SpoolNameEnumType queue, DirectoryGroupType group) throws FactoryException
	{

		clearCache();
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldSpoolBucketName(queue), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE), QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
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
		return newMessage(queue, owner, getUserMessagesGroup(owner));
	}
	public MessageSpoolType newMessage(SpoolNameEnumType queue, UserType owner, DirectoryGroupType group)
	{
		MessageSpoolType new_message = new MessageSpoolType();
		new_message.setGuid(UUID.randomUUID().toString());
		new_message.setSpoolBucketName(queue);
		new_message.setOwnerId(owner.getId());
		new_message.setOrganization(owner.getOrganization());
		new_message.setGroup(group);
		new_message.setSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE);
		new_message.setValueType(ValueEnumType.UNKNOWN);
		new_message.setCreated(CalendarUtil.getXmlGregorianCalendar(Calendar.getInstance().getTime()));
		Date expDate = Calendar.getInstance().getTime();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		new_message.setExpiration(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
		new_message.setExpires(true);
		new_message.setSpoolStatus(0);
		return new_message;
	}
	
	public DirectoryGroupType getUserMessagesGroup(UserType user) throws FactoryException, ArgumentException
	{
		return Factories.getGroupFactory().getCreateUserDirectory(user, ".messages");
	}

	public List<MessageSpoolType> getMessagesAfterDate(SpoolNameEnumType queue, XMLGregorianCalendar startDate, int startIndex, OrganizationType organization) throws FactoryException, ArgumentException
	{
		QueryField dateField = QueryFields.getFieldSpoolCreated(startDate);
		dateField.setComparator(ComparatorEnumType.GREATER_THAN_OR_EQUALS);
		return getMessages(new QueryField[] { QueryFields.getFieldSpoolBucketName(queue), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE), dateField }, startIndex, organization);
	}
	public MessageSpoolType getMessageByName(SpoolNameEnumType queue, String name, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		List<BaseSpoolType> messages = getByField(new QueryField[] { QueryFields.getFieldSpoolBucketName(queue), QueryFields.getFieldName(name), QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		if (messages.size() == 0) return null;
		return (MessageSpoolType)messages.get(0);
	}
	public MessageSpoolType getMessageByGuid(String guid, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<BaseSpoolType> messages = getByField(new QueryField[] { QueryFields.getFieldSpoolGuid(guid) }, organization.getId());
		if (messages.size() == 0) return null;
		return (MessageSpoolType)messages.get(0);
	}
	public List<MessageSpoolType> getMessages(SpoolNameEnumType queue, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getMessages(queue, 1, organization);
	}
	public List<MessageSpoolType> getMessages(SpoolNameEnumType queue, int startIndex, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getMessages(new QueryField[] { QueryFields.getFieldSpoolBucketName(queue), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE) },startIndex,organization);
	}
	public List<MessageSpoolType> getMessagesFromUserGroup(SpoolNameEnumType queue, UserType user) throws FactoryException, ArgumentException
	{
		return getMessagesFromUserGroup(queue, user, getUserMessagesGroup(user));
	}
	public List<MessageSpoolType> getMessagesFromUserGroup(SpoolNameEnumType queue, UserType user, DirectoryGroupType group) throws FactoryException, ArgumentException
	{
		return getMessages(new QueryField[]{QueryFields.getFieldSpoolBucketName(queue),QueryFields.getFieldGroup(group.getId()),QueryFields.getFieldSpoolOwner(user.getId())}, 0, user.getOrganization());
	}
	public List<MessageSpoolType> getMessages(QueryField[] fields, int startIndex, OrganizationType organization) throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = getPagingInstruction(startIndex);
		List<BaseSpoolType> messages = getByField(fields, instruction, organization.getId());
		return convertList(messages);
	}


	protected BaseSpoolType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
			MessageSpoolType new_message = new MessageSpoolType();
			return super.read(rset, new_message);
	}


	public boolean addMessage(MessageSpoolType new_message) throws FactoryException
	{
		if(isValid(new_message) == false) throw new FactoryException("Message does not contain valid data.");
		return insertRow(prepareAdd(new_message,"spool"));
	}

	public boolean isValid(BaseSpoolType message)
	{
		if (
			message == null
			|| message.getOrganization() == null
			|| message.getOrganization().getId() <= 0
			|| message.getGroup() == null
			|| message.getGroup().getId() <= 0
			|| message.getSpoolBucketName() == null
			|| message.getSpoolBucketType() == null
			|| message.getName() == null
			|| message.getGuid() == null
			|| message.getOwnerId() <= 0
		) return false;
		return true;
	}

}
