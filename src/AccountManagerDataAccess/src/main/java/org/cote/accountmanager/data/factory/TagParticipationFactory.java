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
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountTagType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonTagType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserTagType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

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
		return deleteParticipant(dp);

	}
	public boolean deletePersonParticipations(PersonType person) throws FactoryException, ArgumentException
	{

		List<PersonParticipantType> dp = getPersonParticipants(person);
		return deleteParticipants(dp.toArray(new PersonParticipantType[0]), person.getOrganization());
	}
	
	public boolean deleteUserTagParticipant(UserTagType tag, UserType user) throws FactoryException, ArgumentException
	{
		UserParticipantType dp = getUserParticipant(tag, user);
		if (dp == null) return true;
		return deleteParticipant(dp);

	}
	public boolean deleteUserParticipations(UserType user) throws FactoryException, ArgumentException
	{

		List<UserParticipantType> dp = getUserParticipants(user);
		return deleteParticipants(dp.toArray(new UserParticipantType[0]), user.getOrganization());
	}
	public boolean deleteAccountTagParticipant(AccountTagType tag, AccountType account) throws FactoryException, ArgumentException
	{
		AccountParticipantType dp = getAccountParticipant(tag, account);
		if (dp == null) return true;
		return deleteParticipant(dp);

	}
	public boolean deleteAccountParticipations(AccountType account) throws FactoryException, ArgumentException
	{

		List<AccountParticipantType> dp = getAccountParticipants(account);
		return deleteParticipants(dp.toArray(new AccountParticipantType[0]), account.getOrganization());
	}
	public boolean deleteDataTagParticipant(DataTagType tag, DataType data) throws FactoryException, ArgumentException
	{
		DataParticipantType dp = getDataParticipant(tag, data);
		if (dp == null) return true;
		return deleteParticipant(dp);

	}
	public boolean deleteDataParticipations(DataType data) throws FactoryException, ArgumentException
	{

		List<DataParticipantType> dp = getDataParticipants(data);
		return deleteParticipants(dp.toArray(new DataParticipantType[0]), data.getOrganization());
	}

	public DataParticipantType newDataTagParticipation(BaseTagType tag, DataType data) throws ArgumentException {
		DataParticipantType part = (DataParticipantType)newParticipant(tag, data, ParticipantEnumType.DATA,null,null);
		part.setOwnerId(data.getId());
		return part;
	}
	public AccountParticipantType newAccountTagParticipation(BaseTagType tag, AccountType account) throws ArgumentException
	{
		AccountParticipantType part = (AccountParticipantType)newParticipant(tag, account, ParticipantEnumType.ACCOUNT,null,null);
		part.setOwnerId(account.getId());
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
		part.setOwnerId(person.getId());
		return part;
	}

	public List<AccountTagType> getAccountTags(AccountType account) throws FactoryException, ArgumentException
	{
		List<AccountParticipantType> list = getAccountParticipants(account);
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().getAccountTags(field, account.getOrganization());
	}
	
	public List<UserTagType> getUserTags(UserType user) throws FactoryException, ArgumentException
	{
		List<UserParticipantType> list = getUserParticipants(user);
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().getUserTags(field, user.getOrganization());
	}
	public List<PersonTagType> getPersonTags(PersonType person) throws FactoryException, ArgumentException
	{
		List<PersonParticipantType> list = getPersonParticipants(person);
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().getPersonTags(field, person.getOrganization());
	}
	public List<DataTagType> getDataTags(DataType data) throws FactoryException, ArgumentException
	{
		List<DataParticipantType> list = getDataParticipants(data);
		QueryField field = QueryFields.getFieldParticipationIds(list.toArray(new BaseParticipantType[0]));
		return Factories.getTagFactory().getDataTags(field, data.getOrganization());
	}
	public List<PersonParticipantType> getPersonParticipants(PersonType person) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(person,ParticipantEnumType.PERSON), person.getOrganization().getId()));
	}
	public List<AccountParticipantType> getAccountParticipants(AccountType account) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(account,ParticipantEnumType.ACCOUNT), account.getOrganization().getId()));
	}
	public List<UserParticipantType> getUserParticipants(UserType user) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(user,ParticipantEnumType.USER), user.getOrganization().getId()));
	}
	public List<DataParticipantType> getDataParticipants(DataType data) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(data,ParticipantEnumType.DATA), data.getOrganization().getId()));
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
	public <T> List<T> getTagParticipations(BaseTagType[] tags, ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		
		///return convertList(getParticipations(tags,ParticipantEnumType.DATA));
		if(tags.length == 0) return new ArrayList<T>();

		OrganizationType org = tags[0].getOrganization();

		List<QueryField> matches = new ArrayList<QueryField>();
		matches.add(QueryFields.getFieldParticipantType(type));
		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < tags.length; i++)
		{
			if (i > 0) buff.append(",");
			buff.append(tags[i].getId());
		}

		// Get a list of participantids 
		//
		StringBuilder id_buff = new StringBuilder();
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		int id_count = 0;
		try{
			String sql = "SELECT participantid FROM " + dataTables.get(0).getName()
				+ " WHERE participationid IN (" + buff.toString() + ")"
				+ " AND participanttype = " + token
				+ " AND organizationid = " + token
				+ " GROUP BY participantid HAVING count(*) = " + token
			;
			/*
			System.out.println("SELECT participantid FROM " + dataTables.get(0).getName()
					+ " WHERE participationid IN (" + buff.toString() + ")"
					+ " AND participanttype = '" + type.toString() + "'"
					+ " AND organizationid = " + org.getId()
					+ " GROUP BY participantid HAVING count(*) = " + tags.length);
			*/
			PreparedStatement statement = connection.prepareStatement(sql);

			statement.setString(1, type.toString());
			statement.setLong(2, org.getId());
			statement.setInt(3, tags.length);
			ResultSet rset = statement.executeQuery();
		
			while (rset.next())
			{
				if (id_count > 0) id_buff.append(",");
				id_buff.append(rset.getLong(1));
				id_count++;
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
		
		if (id_count == 0) return new ArrayList<T>();

		QueryField match = new QueryField(SqlDataEnumType.BIGINT, "participantid", id_buff.toString());
		match.setComparator(ComparatorEnumType.IN);
		matches.add(match);
		QueryField match2 = new QueryField(SqlDataEnumType.BIGINT, "participationid", buff.toString());
		match2.setComparator(ComparatorEnumType.IN);
		matches.add(match2);

		return convertList(getByField(matches.toArray(new QueryField[0]),org.getId()));

	}

	public DataParticipantType getDataParticipant(BaseTagType tag,DataType data) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(data), QueryFields.getFieldParticipantType(ParticipantEnumType.DATA), QueryFields.getFieldParticipationId(tag)}, data.getOrganization().getId());
		if(list.size() == 0) return null;
		return (DataParticipantType)list.get(0);
	}

	public AccountParticipantType getAccountParticipant(BaseTagType tag, AccountType account) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(account), QueryFields.getFieldParticipantType(ParticipantEnumType.ACCOUNT), QueryFields.getFieldParticipationId(tag) }, account.getOrganization().getId());
		if (list.size() == 0) return null;
		return (AccountParticipantType)list.get(0);
	}
	public UserParticipantType getUserParticipant(BaseTagType tag, UserType user) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(user), QueryFields.getFieldParticipantType(ParticipantEnumType.USER), QueryFields.getFieldParticipationId(tag) }, user.getOrganization().getId());
		if (list.size() == 0) return null;
		return (UserParticipantType)list.get(0);
	}
	public PersonParticipantType getPersonParticipant(BaseTagType tag, PersonType person) throws FactoryException, ArgumentException
	{

		List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(person), QueryFields.getFieldParticipantType(ParticipantEnumType.PERSON), QueryFields.getFieldParticipationId(tag) }, person.getOrganization().getId());
		if (list.size() == 0) return null;
		return (PersonParticipantType)list.get(0);
	}
}
