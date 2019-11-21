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
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserParticipantType;
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

	public boolean deleteTagParticipations(NameIdType object) throws FactoryException, ArgumentException
	{

		List<BaseParticipantType> dp = getTagParticipants(object);
		return deleteParticipants(dp.toArray(new BaseParticipantType[0]), object.getOrganizationId());
	}

	public BaseParticipantType newTagParticipation(BaseTagType tag, NameIdType object) throws ArgumentException
	{
		BaseParticipantType part = newParticipant(tag, object, ParticipantEnumType.valueOf(object.getNameType().toString()),null,null);
		part.setOwnerId(object.getOwnerId());
		return part;
	}

	public List<BaseTagType> getTags(NameIdType object) throws FactoryException, ArgumentException
	{
		List<BaseParticipantType> list = getTagParticipants(object);
		if(list.isEmpty()) return new ArrayList<>();
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).listInGroup(null, TagEnumType.valueOf(object.getNameType().toString()), field, 0, 0,object.getOrganizationId());
	}

	public <T> List<T> getTagParticipants(NameIdType object) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(object,ParticipantEnumType.valueOf(object.getNameType().toString())), object.getOrganizationId()));
	}
	public <T> List<T> getTagParticipations(BaseTagType tag, ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		return convertList(getParticipations(new BaseTagType[]{tag}, type));
	}
	
	/// Tag participations for a set of tags is currently an inner set where the specified
	/// array of tags must match all participants, while the default is to match a participant to any one of the specified participations
	///
	///
	@SuppressWarnings("unchecked")
	public <T> List<T> getTagParticipations(BaseTagType[] tags, ProcessingInstructionType instruction,ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		
		List<T> outList = new ArrayList<T>();
		if(tags.length == 0) return outList;
		if(instruction == null) instruction = new ProcessingInstructionType();
		long org = tags[0].getOrganizationId();
		
		
		List<QueryField> matches = new ArrayList<>();
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
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		PreparedStatement statement = null;
		ResultSet rset = null;
		try{
			String sql = assembleQueryString("SELECT participantid FROM " + dataTables.get(0).getName(),matches.toArray(new QueryField[0]),connectionType,instruction,org);

			logger.info("Tag Sql for type " + type.toString() + " and length " + tags.length + ": " + sql);
			statement = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(matches.toArray(new QueryField[0]), statement);
			rset = statement.executeQuery();
		
			while (rset.next())
			{
				BaseParticipantType bpt = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).newParticipant(type);
				bpt.setOrganizationId(org);
				bpt.setParticipantId(rset.getLong(1));
				outList.add((T)bpt);
			}
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
			throw new FactoryException(sqe.getMessage());
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}

		return outList;

	}
	public int countTagParticipations(BaseTagType[] tags, ParticipantEnumType type) throws FactoryException
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

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		String partType =  (type != ParticipantEnumType.UNKNOWN? " AND participanttype = " + token : "");
		PreparedStatement statement = null;
		ResultSet rset = null;
		try{
			String sql = String.format("SELECT count(participantid) FROM (SELECT participantid FROM %s WHERE participationid IN (%s) %s AND organizationid = %s GROUP BY participantid HAVING count(*) = %s ORDER BY participantid) as tc",this.primaryTableName,buff.toString(),partType,token,token);

			statement = connection.prepareStatement(sql);
			int paramCount = 1;
			if(type != ParticipantEnumType.UNKNOWN) statement.setString(paramCount++, type.toString());
			statement.setLong(paramCount++, org);
			statement.setInt(paramCount++, tags.length);
			rset = statement.executeQuery();
		
			if (rset.next())
			{
				count = rset.getInt(1);
			}
			
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
			throw new FactoryException(sqe.getMessage());
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return count;

	}
	public GroupParticipantType getGroupParticipant(BaseTagType tag,BaseGroupType group) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(group), QueryFields.getFieldParticipantType(ParticipantEnumType.GROUP), QueryFields.getFieldParticipationId(tag)}, group.getOrganizationId());
		if(list.isEmpty()) return null;
		return (GroupParticipantType)list.get(0);
	}
	public DataParticipantType getDataParticipant(BaseTagType tag,DataType data) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(data), QueryFields.getFieldParticipantType(ParticipantEnumType.DATA), QueryFields.getFieldParticipationId(tag)}, data.getOrganizationId());
		if(list.isEmpty()) return null;
		return (DataParticipantType)list.get(0);
	}

	public AccountParticipantType getAccountParticipant(BaseTagType tag, AccountType account) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(account), QueryFields.getFieldParticipantType(ParticipantEnumType.ACCOUNT), QueryFields.getFieldParticipationId(tag) }, account.getOrganizationId());
		if (list.isEmpty()) return null;
		return (AccountParticipantType)list.get(0);
	}
	public UserParticipantType getUserParticipant(BaseTagType tag, UserType user) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(user), QueryFields.getFieldParticipantType(ParticipantEnumType.USER), QueryFields.getFieldParticipationId(tag) }, user.getOrganizationId());
		if (list.isEmpty()) return null;
		return (UserParticipantType)list.get(0);
	}
	public PersonParticipantType getPersonParticipant(BaseTagType tag, PersonType person) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(person), QueryFields.getFieldParticipantType(ParticipantEnumType.PERSON), QueryFields.getFieldParticipationId(tag) }, person.getOrganizationId());
		if (list.isEmpty()) return null;
		return (PersonParticipantType)list.get(0);
	}
}
