package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressParticipantType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ContactParticipantType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.RoleParticipantType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;


public abstract class ParticipationFactory extends NameIdFactory {
	public static final Logger logger = Logger.getLogger(ParticipationFactory.class.getName());
	protected ParticipationEnumType participationType = ParticipationEnumType.UNKNOWN;
	protected boolean haveAffect = false;

	
	public ParticipationFactory(ParticipationEnumType type, String table_name){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
		this.hasName = false;
		this.participationType = type;
		this.tableNames.add(table_name);
	}

	@Override
	public <T> String getCacheKeyName(T obj){
		BaseParticipantType participant = (BaseParticipantType)obj;
		return participant.getParticipationId() + "-" + this.participationType + "-" + participant.getParticipantId() + "-" + participant.getParticipantType() + "-" + participant.getAffectId() + "-" + participant.getAffectType() + "-" + participant.getOrganization().getId();
	}
	public boolean deleteParticipations(NameIdType source) throws FactoryException
	{
		int count = deleteByField(new QueryField[] { QueryFields.getFieldParticipationId(source) }, source.getOrganization().getId());
		return (count > 0);
	}
	public boolean deleteParticipations(long[] ids, OrganizationType organization) throws FactoryException
	{
		return deleteParts(ids, "ParticipationId", null, organization);
	}

	public boolean deleteParticipantsForParticipation(long[] ids, NameIdType participation, OrganizationType organization) throws FactoryException
	{
		QueryField query = new QueryField(SqlDataEnumType.BIGINT, "ParticipationId",participation.getId());
		query.setComparator(ComparatorEnumType.EQUALS);
		return deleteParts(ids, "ParticipantId", query, organization);
	}	
	
	public boolean deleteParticipants(long[] ids, OrganizationType organization) throws FactoryException
	{
		return deleteParts(ids, "ParticipantId", null, organization);
	}
	protected boolean deleteParts(long[] ids, String field_name, QueryField query, OrganizationType organization) throws FactoryException
	{
		StringBuffer buff = new StringBuffer();
		int deleted = 0;
		for (int i = 0; i < ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(ids[i]);
			if ((i > 0 || ids.length == 1) && ((i % 250 == 0) || i == ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, field_name, buff.toString());
				List<QueryField> matches = new ArrayList<QueryField>();
				match.setComparator(ComparatorEnumType.IN);
				matches.add(match);
				if(query != null) matches.add(query);
				deleted += deleteByField(matches.toArray(new QueryField[0]), organization.getId());
				buff.delete(0, buff.length());
			}
		}

		return (deleted > 0);
	}
	public boolean deleteParticipants(BaseParticipantType[] list, OrganizationType organization)  throws FactoryException
	{

		List<Long> ids = new ArrayList<Long>();
		for (int i = 0; i < list.length; i++)
		{
			ids.add(list[i].getId());
			removeParticipantFromCache(list[i]);
		}
		return (deleteById(convertLongList(ids), organization.getId()) > 0);
	}
	public boolean deleteParticipant(BaseParticipantType participant) throws FactoryException
	{
		int deleted = deleteById(participant.getId(), participant.getOrganization().getId());
		removeParticipantFromCache(participant);
		return (deleted > 0);
	}
	public boolean updateParticipant(BaseParticipantType participant)  throws FactoryException
	{
		removeParticipantFromCache(participant);
		return update(participant);
	}
	protected void updateParticipantToCache(BaseParticipantType participant) throws ArgumentException{
		//String key_name = participant.getParticipationId() + "-" + this.participationType + "-" + participant.getParticipantId() + "-" + participant.getParticipantType() + "-" + participant.getAffectId() + "-" + participant.getAffectType() + "-" + participant.getOrganization().getId();
		String key_name = getCacheKeyName(participant);
		logger.debug("Update participant to cache: " + key_name);
		if(this.haveCacheId(participant.getId())) removeFromCache(participant);
		addToCache(participant, key_name);
	}

	protected void removeParticipantFromCache(BaseParticipantType participant){
		//String key_name = participant.getParticipationId() + "-" + this.participationType + "-" + participant.getParticipantId() + "-" + participant.getParticipantType() + "-" + participant.getAffectId() + "-" + participant.getAffectType() + "-" + participant.getOrganization().getId();
		String key_name = getCacheKeyName(participant);
		logger.debug("Remove participant from cache: " + key_name);
		
		removeFromCache(participant, key_name);
	}
	public boolean addParticipant(BaseParticipantType participant) throws FactoryException, DataAccessException
	{
		if (participant.getOrganization() == null || participant.getOrganization().getId() <= 0) throw new FactoryException("Cannot add participant to invalid organization");
		if (participant.getParticipantId().compareTo(0L)==0 || participant.getParticipationId().compareTo(0L)==0 || participant.getOwnerId() <= 0)
		{
			logger.warn("Participant Id: " + participant.getParticipantId());
			logger.warn("Participation Id: " + participant.getParticipationId());
			logger.warn("Owner Id: " + participant.getOwnerId());
			throw new FactoryException("Participant could not be added due to missing data.  Participant must have: 1) Participant Id, 2) Participation Id, and 3) Owner id");
			
		}
		/*
		if(participant.getParticipantId() > 0 && participant.getParticipationId() > 0){
			String sessionId = BulkFactories.getBulkFactory().getSessionForPersistentId(FactoryEnumType.fromValue(participationType.toString()),participant.getParticipationId());
			if(sessionId == null){
				logger.error("Invalid bulk session id from key '" + (participationType.toString() + "-" + participant.getParticipationId()) + "'");
				throw new FactoryException("Invalid bulk session id from key '" + (participationType.toString() + "-" + participant.getParticipationId()) + "'");
			}
			logger.info("Bulk participation discovered.  Participant=" + participant.getParticipantId() + " / Participation=" + participant.getParticipationId() + ". Writing to Bulk " + factoryType + " Session " + sessionId);
			try {
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, factoryType, participant);
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		*/
		if(participant.getParticipantId() < 0 || participant.getParticipationId() < 0){
			if(this.factoryType == FactoryEnumType.UNKNOWN) throw new FactoryException("Invalid Factory Type for Bulk Identifiers");
			String sessionId = BulkFactories.getBulkFactory().getSessionForBulkId(participant.getParticipantId() < 0 ? participant.getParticipantId() : participant.getParticipationId());
			if(sessionId == null){
				logger.error("Invalid bulk session id");
				throw new FactoryException("Invalid bulk session id");
			}
			logger.debug("Bulk id discovered.  Participant=" + participant.getParticipantId() + " / Participation=" + participant.getParticipationId() + ". Diverting to Bulk " + factoryType + " Operation");
			try {
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, factoryType, participant);
				//BulkFactories.getBulkFactory().setDirty(factoryType);
				return true;
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			} 
			return false;
		}
		if(bulkMode){
			//logger.debug("Set the factory for a dirty write ");
			BulkFactories.getBulkFactory().setDirty(factoryType);
		}
		DataTable dt = dataTables.get(0);
		DataRow row = prepareAdd(participant, dt.getName());
		row.setCellValue("participationid", participant.getParticipationId());
		row.setCellValue("participantid", participant.getParticipantId());
		row.setCellValue("participanttype", participant.getParticipantType().toString());
		
		if (haveAffect)
		{
			row.setCellValue("affecttype", participant.getAffectType().toString());
			row.setCellValue("affectid", participant.getAffectId());
		}
		/// Bulk insert note: prepareAdd and insertRow won't add the row to the local table row cache, so it must be added manually
		/// bulkMode is excluded because there is different behavior betweer using direct bulk insert and the bulk insert factory
		///
		if(!bulkMode && dt.getBulkInsert()) dt.getRows().add(row);
		
		return insertRow(row);

	}
	protected List<BaseRoleType> getRoleListFromParticipations(RoleParticipantType[] list, OrganizationType organization) throws FactoryException, ArgumentException
	{
	
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return  Factories.getRoleFactory().getRoles(new QueryField[] { field }, organization);
		//return new ArrayList<BaseRoleType>();

	}
	protected <T> List<T> getGroupListFromParticipations(BaseParticipantType[] list, OrganizationType organization) throws FactoryException, ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getGroupFactory().getList(new QueryField[]{ field }, organization);
	}
	protected <T> List<T> getUserListFromParticipations(BaseParticipantType[] list, OrganizationType organization) throws FactoryException, ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getUserFactory().getList(new QueryField[]{ field }, organization);
	}	
	protected <T> List<T> getAccountListFromParticipations(BaseParticipantType[] list, OrganizationType organization) throws FactoryException, ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getAccountFactory().getList(new QueryField[]{ field }, organization);
	}
/*
	protected List<AccountType> getAccountsFromParticipations(AccountParticipantType[] list, OrganizationType organization) throws FactoryException
	{
		QueryField field = QueryFields.getFieldParticipationList(list, organization);
		return Factories.getAccountFactory().getList(new QueryField[]{ field }, organization);
	}
*/
	public List<DataType> getDataListFromParticipations(DataParticipantType[] list, boolean detailsOnly, int startRecord, int recordCount, OrganizationType organization) throws FactoryException, ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getDataFactory().getDataList(new QueryField[]{ field }, detailsOnly, startRecord, recordCount, organization);
		/*
		return Factory.DataFactoryInstance.GetDataList(new QueryField[] { match }, DetailsOnly, StartRecord, RecordCount,organization);
		*/
		//return new ArrayList<DataType>();
	}
	public List<NameIdType> getParticipants(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participant_type,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException
	{
		QueryField[] fields = QueryFields.getFieldParticipantsMatch(participation, participant, participant_type, permission, affect_type);
		
		return getByField(fields, participation.getOrganization().getId());


	}
	public List<NameIdType> getParticipations(NameIdType[] maps, ParticipantEnumType participant_type)  throws FactoryException, ArgumentException
	{

		if (maps.length == 0) return new ArrayList<NameIdType>();
		OrganizationType org = maps[0].getOrganization();

		List<QueryField> matches = new ArrayList<QueryField>();
		matches.add(QueryFields.getFieldParticipantType(participant_type));
		//StringBuffer buff = new StringBuffer();
		List<Long> ids = new ArrayList<Long>();
		for (int i = 0; i < maps.length; i++)
		{
			ids.add(maps[i].getId());
		}

		QueryField match = new QueryField(SqlDataEnumType.VARCHAR, "participationid", QueryFields.getFilteredLongList(convertLongList(ids)));
		match.setComparator(ComparatorEnumType.IN);
		matches.add(match);
		return getByField(matches.toArray(new QueryField[0]), org.getId());
	}
	public <T> T getParticipant(NameIdType participation, NameIdType participant, ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		
		T out_participant = null;
		if (participation == null || participant == null || participation.getId() <= 0 || participant.getId() <= 0 || type == ParticipantEnumType.UNKNOWN) throw new ArgumentException("getParticipant: Invalid parameters");

		String key_name = participation.getId() + "-" + this.participationType + "-" + participant.getId() + "-" + type + "-" + participation.getOrganization().getId();
		out_participant = (T)readCache(key_name);
		boolean add_to_cache = false;
		if (out_participant == null)
		{
			List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(participant), QueryFields.getFieldParticipantType(type), QueryFields.getFieldParticipationId(participation)}, participation.getOrganization().getId());
			if (list.size() == 0) return null;
			out_participant = (T)list.get(0);
			add_to_cache = true;
		}
		
		if (add_to_cache){
			/// logger.info("Add to cache: " + key_name);
			addToCache((NameIdType)out_participant, key_name);
		}
		return out_participant;

	}
	public <T> T getParticipant(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participant_type,
		BasePermissionType permission,
		AffectEnumType affect_type
	)  throws FactoryException, ArgumentException
	{
		T out_participant = null;
		if (participation == null || participant == null || participation.getId().compareTo(0L)==0 || participant.getId().compareTo(0L)==0 || participant_type == ParticipantEnumType.UNKNOWN){
				throw new ArgumentException("getParticipant: Invalid parameters.  " + (participation == null ? " Null Participation." : "") + (participant == null ? " Null participant." : "") + (participant_type == ParticipantEnumType.UNKNOWN ? " UNKNOWN Participant Type":""));
		}

		String key_name = participation.getId() + "-" + this.participationType + "-" + participant.getId() + "-" + participant_type + "-" + (permission != null ? permission.getId() : "0") + "-" + affect_type + "-" + participation.getOrganization().getId();
		out_participant = (T)readCache(key_name);
		boolean add_to_cache = false;
		
		/// Moving the negative part id check down is to support bulk operations
		///
		if (out_participant == null && participation.getId() > 0 && participant.getId() > 0)
		{
			List<NameIdType> list = getParticipants(participation, participant, participant_type, permission, affect_type);
			if (list.size() == 0) return null;
			out_participant = (T)list.get(0);
			add_to_cache = true;
		}
		
		if (add_to_cache){
			/// logger.info("Add to cache: " + key_name);
			addToCache((NameIdType)out_participant, key_name);
		}
		return out_participant;
	}

	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BaseParticipantType use_map = (BaseParticipantType)map;
		fields.add(QueryFields.getFieldParticipantId(use_map));
		fields.add(QueryFields.getFieldParticipantType(use_map.getParticipantType()));
		fields.add(QueryFields.getFieldParticipationId(use_map));
		if(haveAffect){
			fields.add(QueryFields.getFieldAffectId(use_map));
			fields.add(QueryFields.getFieldAffectType(use_map));
		}
	}



	protected BaseParticipantType newParticipant(String participant_name, ParticipantEnumType type, OrganizationType organization) throws ArgumentException
	{
		return newParticipant(participant_name, type, null, organization);
	}
	protected BaseParticipantType newParticipant(String participant_name, ParticipantEnumType type, BaseParticipantType Parent, OrganizationType organization) throws ArgumentException
	{
		BaseParticipantType new_participant = newParticipant(type);
		new_participant.setOrganization(organization);
		return new_participant;
	}

	protected BaseParticipantType newParticipant(ParticipantEnumType type) throws ArgumentException
	{
		BaseParticipantType new_participant = null;
		switch (type)
		{
			case ADDRESS:
				new_participant = new AddressParticipantType();
				break;
			case CONTACT:
				new_participant = new ContactParticipantType();
				break;
			case DEPENDENTPERSON:
				new_participant = new PersonParticipantType();
				break;
			case PERSON:
				new_participant = new PersonParticipantType();
				break;
			case DATA:
				new_participant = new DataParticipantType();
				break;
			case ACCOUNT:
				new_participant = new AccountParticipantType();
				break;
			case ROLE:
				new_participant = new RoleParticipantType();
				break;
			case USER:
				new_participant = new UserParticipantType();
				break;
			case GROUP:
				new_participant = new GroupParticipantType();
				break;
			default:
				throw new ArgumentException("Invalid participant type: " + type.toString());
				//new_participant = new BaseParticipantType();
				//break;
		}
		new_participant.setNameType(NameEnumType.PARTICIPANT);
		new_participant.setAffectType(AffectEnumType.UNKNOWN);
		new_participant.setParticipantType(type);
		return new_participant;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		
		BaseParticipantType new_part = null;
		try{
			new_part = newParticipant(ParticipantEnumType.valueOf(rset.getString("participanttype")));
		}
		catch(ArgumentException ae){
			logger.error(ae.getMessage());
			throw new FactoryException(ae.getMessage());
		}
		new_part.setParticipantId(rset.getLong("participantid"));
		new_part.setParticipationId(rset.getLong("participationid"));
		new_part.setAffectType(AffectEnumType.UNKNOWN);
		if(haveAffect){
			new_part.setAffectType(AffectEnumType.valueOf(rset.getString("affecttype")));
			new_part.setAffectId(rset.getLong("affectid"));
		}
		return super.read(rset, new_part);
	}
	

	public BaseParticipantType newParticipant(
		NameIdType participation,
		//ParticipationType participation_type,
		NameIdType participant,
		ParticipantEnumType participant_type,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException
	{
		
		if (participation == null || participant == null || participation.getId().compareTo(0L)==0 || participant.getId().compareTo(0L)==0 || participant_type == ParticipantEnumType.UNKNOWN){
			logger.warn("Invalid " + participationType + " Participant Parameters");
			if(participation == null) logger.warn("\tNull participation");
			else if(participant == null) logger.warn("\tNull participant");
			else{

				logger.warn("participantType=" + participant_type);
				logger.warn("participantId=" + participant.getId());
				logger.warn("participationId=" + participation.getId());
			}
			throw new ArgumentException("Invalid parameters");
		}
		BaseParticipantType out_participant = newParticipant(participant_type);
		out_participant.setNameType(NameEnumType.PARTICIPANT);
		out_participant.setParticipationType(this.participationType);
		out_participant.setParticipationId(participation.getId());
		out_participant.setParticipantId(participant.getId());
		out_participant.setOwnerId(participation.getOwnerId());
		out_participant.setOrganization(participation.getOrganization());
		if (permission != null) out_participant.setAffectId(permission.getId());
		out_participant.setAffectType(affect_type);

		return out_participant;
	}


	
}
