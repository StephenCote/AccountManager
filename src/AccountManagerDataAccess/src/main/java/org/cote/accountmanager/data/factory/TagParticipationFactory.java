/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountTagType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.GroupTagType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonTagType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserTagType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.TagEnumType;

public class TagParticipationFactory extends ParticipationFactory {
	public TagParticipationFactory(){
		super(ParticipationEnumType.TAG, "tagparticipation");
		this.haveAffect = false;
		factoryType = FactoryEnumType.TAG;
	}
	public boolean deletePersonTagParticipant(PersonTagType tag, PersonType person) throws FactoryException, ArgumentException
	{
		PersonParticipantType dp = getPersonParticipant(tag, person);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deletePersonParticipations(PersonType person) throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getPersonParticipants(person);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), person.getOrganizationId());
	}
	
	public boolean deleteUserTagParticipant(UserTagType tag, UserType user) throws FactoryException, ArgumentException
	{
		UserParticipantType dp = getUserParticipant(tag, user);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteUserParticipations(UserType user) throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserParticipants(user);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), user.getOrganizationId());
	}
	public boolean deleteAccountTagParticipant(AccountTagType tag, AccountType account) throws FactoryException, ArgumentException
	{
		AccountParticipantType dp = getAccountParticipant(tag, account);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteAccountParticipations(AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountParticipants(account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganizationId());
	}
	public boolean deleteDataTagParticipant(DataTagType tag, DataType data) throws FactoryException, ArgumentException
	{
		DataParticipantType dp = getDataParticipant(tag, data);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteDataParticipations(DataType data) throws FactoryException, ArgumentException
	{

		List<DataParticipantType> dp = getDataParticipants(data);
		return deleteParticipants(dp.toArray(new DataParticipantType[0]), data.getOrganizationId());
	}
	public boolean deleteGroupTagParticipant(GroupTagType tag, BaseGroupType group) throws FactoryException, ArgumentException
	{
		GroupParticipantType dp = getGroupParticipant(tag, group);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteGroupParticipations(BaseGroupType group) throws FactoryException, ArgumentException
	{

		List<GroupParticipantType> dp = getGroupParticipants(group);
		return deleteParticipants(dp.toArray(new GroupParticipantType[0]), group.getOrganizationId());
	}

	public DataParticipantType newDataTagParticipation(BaseTagType tag, DataType data) throws ArgumentException {
		DataParticipantType part = (DataParticipantType)newParticipant(tag, data, ParticipantEnumType.DATA,null,null);
		part.setOwnerId(data.getOwnerId());
		return part;
	}
	public AccountParticipantType newAccountTagParticipation(BaseTagType tag, AccountType account) throws ArgumentException
	{
		AccountParticipantType part = (AccountParticipantType)newParticipant(tag, account, ParticipantEnumType.ACCOUNT,null,null);
		part.setOwnerId(account.getOwnerId());
		return part;
	}
	public UserParticipantType newUserTagParticipation(BaseTagType tag, UserType user) throws ArgumentException
	{
		UserParticipantType part = (UserParticipantType)newParticipant(tag, user, ParticipantEnumType.USER,null,null);
		part.setOwnerId(user.getId());
		return part;
	}
	public PersonParticipantType newPersonTagParticipation(BaseTagType tag, PersonType person) throws ArgumentException
	{
		PersonParticipantType part = (PersonParticipantType)newParticipant(tag, person, ParticipantEnumType.PERSON,null,null);
		part.setOwnerId(person.getOwnerId());
		return part;
	}
	public GroupParticipantType newGroupTagParticipation(BaseTagType tag, BaseGroupType group) throws ArgumentException
	{
		GroupParticipantType part = (GroupParticipantType)newParticipant(tag, group, ParticipantEnumType.GROUP,null,null);
		part.setOwnerId(group.getOwnerId());
		return part;
	}
	public List<BaseTagType> getAccountTags(AccountType account) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> list = getAccountParticipants(account);
		if(list.size() == 0) return new ArrayList<BaseTagType>();
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().listInGroup(null, TagEnumType.ACCOUNT, field, 0, 0,account.getOrganizationId());
	}
	
	public List<BaseTagType> getUserTags(UserType user) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> list = getUserParticipants(user);
		if(list.size() == 0) return new ArrayList<BaseTagType>();
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().listInGroup(null, TagEnumType.USER, field, 0, 0,user.getOrganizationId());
	}
	public List<BaseTagType> getPersonTags(PersonType person) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> list = getPersonParticipants(person);
		if(list.size() == 0) return new ArrayList<BaseTagType>();
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().listInGroup(null, TagEnumType.PERSON, field, 0, 0,person.getOrganizationId());
	}
	public List<BaseTagType> getDataTags(DataType data) throws FactoryException, ArgumentException
	{
		List<DataParticipantType> list = getDataParticipants(data);
		if(list.size() == 0) return new ArrayList<BaseTagType>();
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().listInGroup(null, TagEnumType.DATA, field, 0, 0,data.getOrganizationId());
	}
	public List<BaseTagType> getGroupTags(BaseGroupType group) throws FactoryException, ArgumentException
	{
		List<GroupParticipantType> list = getGroupParticipants(group);
		if(list.size() == 0) return new ArrayList<BaseTagType>();
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().listInGroup(null, TagEnumType.GROUP, field, 0, 0,group.getOrganizationId());
	}
	public List<PersonParticipantType> getPersonParticipants(PersonType person) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(person,ParticipantEnumType.PERSON), person.getOrganizationId()));
	}
	public List<AccountParticipantType> getAccountParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account,ParticipantEnumType.ACCOUNT), account.getOrganizationId()));
	}
	public List<UserParticipantType> getUserParticipants(UserType user) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(user,ParticipantEnumType.USER), user.getOrganizationId()));
	}
	public List<DataParticipantType> getDataParticipants(DataType data) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(data,ParticipantEnumType.DATA), data.getOrganizationId()));
	}
	public List<GroupParticipantType> getGroupParticipants(BaseGroupType group) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(group,ParticipantEnumType.GROUP), group.getOrganizationId()));
	}
	public <T> List<T> getTagParticipations(BaseTagType tag, ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		//return getTagParticipations(new BaseTagType[] { tag });
		return convertList(getParticipations(new BaseTagType[]{tag}, type));
	}
	
	/// Tag participations for a set of tags is currently an inner set where the specified
	/// array of tags must match all participants, while the default is to match a participant to any one of the specified participations
	///
	/// TODO: The OOP query interface for AccountManager isn't as dynamic as the QueryBuilder API used in the monitoring code base,
	/// So it's not convenient to assemble a complex group clause and having the query automatically constructed.
	/// Therefore, this is done inline for AM, but if the QueryBuilder API is used (which brings with it a more complex/noisy construction)
	/// Then this can be replaced.
	///
	public <T> List<T> getTagParticipations(BaseTagType[] tags, ProcessingInstructionType instruction,ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		
		List<T> out_list = new ArrayList<T>();
		///return convertList(getParticipations(tags,ParticipantEnumType.DATA));
		if(tags.length == 0) return out_list;
		if(instruction == null) instruction = new ProcessingInstructionType();
		long org = tags[0].getOrganizationId();
		
		
		List<QueryField> matches = new ArrayList<QueryField>();
		if(type != ParticipantEnumType.UNKNOWN) matches.add(QueryFields.getFieldParticipantType(type));
		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < tags.length; i++)
		{
			if (i > 0) buff.append(",");
			buff.append(tags[i].getId());
		}
		QueryField field = new QueryField(SqlDataEnumType.BIGINT,"participationid",buff.toString());
		field.setComparator(ComparatorEnumType.IN);
		matches.add(field);
		instruction.setHavingClause("count(participantid) = " + tags.length);
		instruction.setGroupClause("participantid");
		instruction.setOrderClause("participantid");
		// Get a list of participantids 
		//
		//StringBuilder id_buff = new StringBuilder();
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		//String token = DBFactory.getParamToken(connectionType);
		//int id_count = 0;
		try{
			String sql = assembleQueryString("SELECT participantid FROM " + dataTables.get(0).getName(),matches.toArray(new QueryField[0]),connectionType,instruction,org);
			/*
			String sql = "SELECT participantid FROM " + dataTables.get(0).getName()
				+ " WHERE participationid IN (" + buff.toString() + ")"
				+ " AND participanttype = " + token
				+ " AND organizationid = " + token
				+ " GROUP BY participantid HAVING count(*) = " + token
				+ " ORDER BY participantid"
			;
			*/
			logger.info("Tag Sql for type " + type.toString() + " and length " + tags.length + ": " + sql);
			PreparedStatement statement = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(matches.toArray(new QueryField[0]), statement);
			/*
			statement.setString(1, type.toString());
			statement.setLong(2, org.getId());
			statement.setInt(3, tags.length);
			*/
			ResultSet rset = statement.executeQuery();
		
			while (rset.next())
			{
				BaseParticipantType bpt = Factories.getTagParticipationFactory().newParticipant(type);
				bpt.setOrganizationId(org);
				//bpt.setParticipationId(rset.getLong(1));
				bpt.setParticipantId(rset.getLong(1));
				out_list.add((T)bpt);
				//logger.info(bpt.getParticipantId());
				
				/*
				if (id_count > 0) id_buff.append(",");
				id_buff.append(rset.getLong(1));
				id_count++;
				*/
			}
			rset.close();
			
		}
		catch(SQLException sqe){
			sqe.printStackTrace();
			throw new FactoryException(sqe.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return out_list;

	}
	public int countTagParticipations(BaseTagType[] tags, ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		int count = 0;
		if(tags.length == 0) return count;

		long org = tags[0].getOrganizationId();

		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < tags.length; i++)
		{
			if (i > 0) buff.append(",");
			buff.append(tags[i].getId());
		}

		StringBuilder id_buff = new StringBuilder();
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		int id_count = 0;
		try{
			String sql = "SELECT count(participantid) FROM (SELECT participantid FROM " + dataTables.get(0).getName()
				+ " WHERE participationid IN (" + buff.toString() + ")"
				+ (type != ParticipantEnumType.UNKNOWN? " AND participanttype = " + token : "")
				+ " AND organizationid = " + token
				+ " GROUP BY participantid HAVING count(*) = " + token
				+ "ORDER BY participantid) as tc"
			;

			PreparedStatement statement = connection.prepareStatement(sql);
			int paramCount = 1;
			if(type != ParticipantEnumType.UNKNOWN) statement.setString(paramCount++, type.toString());
			statement.setLong(paramCount++, org);
			statement.setInt(paramCount++, tags.length);
			ResultSet rset = statement.executeQuery();
		
			if (rset.next())
			{
				count = rset.getInt(1);
			}
			rset.close();
			
		}
		catch(SQLException sqe){
			sqe.printStackTrace();
			throw new FactoryException(sqe.getMessage());
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return count;

	}
	public GroupParticipantType getGroupParticipant(BaseTagType tag,BaseGroupType group) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(group), QueryFields.getFieldParticipantType(ParticipantEnumType.GROUP), QueryFields.getFieldParticipationId(tag)}, group.getOrganizationId());
		if(list.size() == 0) return null;
		return (GroupParticipantType)list.get(0);
	}
	public DataParticipantType getDataParticipant(BaseTagType tag,DataType data) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(data), QueryFields.getFieldParticipantType(ParticipantEnumType.DATA), QueryFields.getFieldParticipationId(tag)}, data.getOrganizationId());
		if(list.size() == 0) return null;
		return (DataParticipantType)list.get(0);
	}

	public AccountParticipantType getAccountParticipant(BaseTagType tag, AccountType account) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(account), QueryFields.getFieldParticipantType(ParticipantEnumType.ACCOUNT), QueryFields.getFieldParticipationId(tag) }, account.getOrganizationId());
		if (list.size() == 0) return null;
		return (AccountParticipantType)list.get(0);
	}
	public UserParticipantType getUserParticipant(BaseTagType tag, UserType user) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(user), QueryFields.getFieldParticipantType(ParticipantEnumType.USER), QueryFields.getFieldParticipationId(tag) }, user.getOrganizationId());
		if (list.size() == 0) return null;
		return (UserParticipantType)list.get(0);
	}
	public PersonParticipantType getPersonParticipant(BaseTagType tag, PersonType person) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(person), QueryFields.getFieldParticipantType(ParticipantEnumType.PERSON), QueryFields.getFieldParticipationId(tag) }, person.getOrganizationId());
		if (list.size() == 0) return null;
		return (PersonParticipantType)list.get(0);
	}
}
