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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AddressParticipantType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ContactParticipantType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.FactParticipantType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;
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
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;


public abstract class ParticipationFactory extends NameIdFactory implements IParticipationFactory {
	public static final Logger logger = LogManager.getLogger(ParticipationFactory.class);
	protected ParticipationEnumType participationType = ParticipationEnumType.UNKNOWN;
	protected boolean haveAffect = false;
	protected String permissionPrefix = null;
	protected PermissionEnumType defaultPermissionType = PermissionEnumType.OBJECT;

	public ParticipationFactory(ParticipationEnumType type, String tableName){
		super();
		this.isParticipation = true;
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
		this.hasName = false;
		this.participationType = type;
		this.primaryTableName = tableName;
		this.tableNames.add(tableName);
	}
	
	public PermissionEnumType getDefaultPermissionType() {
		return defaultPermissionType;
	}

	public String getPermissionPrefix(){
		return permissionPrefix;
	}


	public String[] getDefaultPermissions(){
		if(permissionPrefix == null){
			logger.warn("Permission prefix for " + participationType.toString() + " is not defined");
			return new String[0];
		}
		String[] names = new String[AuthorizationService.getDefaultPermissionBase().length];
		for(int i = 0; i < names.length;i++){
			names[i] = permissionPrefix + AuthorizationService.getDefaultPermissionBase()[i];
		}
		return names;
	}


	@Override
	public <T> String getCacheKeyName(T obj){
		BaseParticipantType participant = (BaseParticipantType)obj;
		return participant.getParticipationId() + "-" + this.participationType + "-" + participant.getParticipantId() + "-" + participant.getParticipantType() + "-" + participant.getAffectId() + "-" + participant.getAffectType() + "-" + participant.getOrganizationId();
	}
	public boolean deleteParticipations(NameIdType source) throws FactoryException
	{
		int count = deleteByField(new QueryField[] { QueryFields.getFieldParticipationId(source) }, source.getOrganizationId());
		return (count > 0);
	}
	public boolean deleteParticipationsByAffects(NameIdType source, long[] permissions) throws FactoryException{
		StringBuilder buff = new StringBuilder();
		int count = 0;
		for (int i = 0; i < permissions.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(permissions[i]);
			if ((i > 0 || permissions.length == 1) && ((i % BulkFactories.bulkQueryLimit == 0) || i == permissions.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "affectid", buff.toString());
				match.setComparator(ComparatorEnumType.ANY);
				count += deleteByField(new QueryField[] { QueryFields.getFieldParticipationId(source),match }, source.getOrganizationId());
				buff.delete(0,  buff.length());
			}
		}
		return (count > 0);
	}
	public boolean deleteParticipationsByAffect(NameIdType source,BasePermissionType permission) throws FactoryException
	{
		int count = deleteByField(new QueryField[] { QueryFields.getFieldParticipationId(source),QueryFields.getFieldAffectId(permission) }, source.getOrganizationId());
		return (count > 0);
	}
	public boolean deleteParticipations(long[] ids, long organizationId) throws FactoryException
	{
		return deleteParts(ids, "ParticipationId", null, organizationId);
	}
	public boolean deleteParticipantsWithAffect(long[] participationIds, long organizationId) throws FactoryException
	{
		QueryField query = new QueryField(SqlDataEnumType.VARCHAR, "AffectType",AffectEnumType.GRANT_PERMISSION.toString());
		query.setComparator(ComparatorEnumType.EQUALS);
		return deleteParts(participationIds, "ParticipationId", query, organizationId);
	}
	public boolean deleteParticipantsForParticipation(long[] ids, NameIdType participation, long organizationId) throws FactoryException
	{
		QueryField query = new QueryField(SqlDataEnumType.BIGINT, "ParticipationId",participation.getId());
		query.setComparator(ComparatorEnumType.EQUALS);
		return deleteParts(ids, "ParticipantId", query, organizationId);
	}
	
	
	public boolean deleteParticipants(long[] ids, long organizationId) throws FactoryException
	{
		return deleteParts(ids, "ParticipantId", null, organizationId);
	}
	protected boolean deleteParts(long[] ids, String field_name, QueryField query, long organizationId) throws FactoryException
	{
		StringBuilder buff = new StringBuilder();
		int deleted = 0;
		for (int i = 0; i < ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(ids[i]);
			if ((i > 0 || ids.length == 1) && ((i % BulkFactories.bulkQueryLimit == 0) || i == ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, field_name, buff.toString());
				List<QueryField> matches = new ArrayList<>();
				match.setComparator(ComparatorEnumType.ANY);
				matches.add(match);
				if(query != null) matches.add(query);
				deleted += deleteByField(matches.toArray(new QueryField[0]), organizationId);
				buff.delete(0, buff.length());
			}
		}

		return (deleted > 0);
	}
	public boolean deleteParticipants(BaseParticipantType[] list, long organizationId)  throws FactoryException
	{

		List<Long> ids = new ArrayList<>();
		for (int i = 0; i < list.length; i++)
		{
			ids.add(list[i].getId());
			removeParticipantFromCache(list[i]);
		}
		return (deleteById(convertLongList(ids), organizationId) > 0);
	}
	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		BaseParticipantType participant = (BaseParticipantType)object;
		if(bulkMode) return true;
		int deleted = deleteById(participant.getId(), participant.getOrganizationId());
		removeParticipantFromCache(participant);
		return (deleted > 0);
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		BaseParticipantType participant = (BaseParticipantType)object;
		removeParticipantFromCache(participant);
		return super.update(participant);
	}
	protected void updateParticipantToCache(BaseParticipantType participant) throws ArgumentException{
		//String keyName = participant.getParticipationId() + "-" + this.participationType + "-" + participant.getParticipantId() + "-" + participant.getParticipantType() + "-" + participant.getAffectId() + "-" + participant.getAffectType() + "-" + participant.getOrganizationId();
		String keyName = getCacheKeyName(participant);
		logger.debug("Update participant to cache: " + keyName);
		if(this.haveCacheId(participant.getId())) removeFromCache(participant);
		addToCache(participant, keyName);
	}

	protected void removeParticipantFromCache(BaseParticipantType participant){
		//String keyName = participant.getParticipationId() + "-" + this.participationType + "-" + participant.getParticipantId() + "-" + participant.getParticipantType() + "-" + participant.getAffectId() + "-" + participant.getAffectType() + "-" + participant.getOrganizationId();
		String keyName = getCacheKeyName(participant);
		logger.debug("Remove participant from cache: " + keyName);
		
		removeFromCache(participant, keyName);
	}
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		BaseParticipantType participant = (BaseParticipantType)object;
		if (participant.getOrganizationId() <= 0L) throw new FactoryException("Cannot add participant to invalid organization");
		if (participant.getParticipantId().compareTo(0L)==0 || participant.getParticipationId().compareTo(0L)==0 || participant.getOwnerId().compareTo(0L)==0)
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
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
			return true;
		}
		*/
		if(participant.getParticipantId() < 0L || participant.getParticipationId() < 0L || participant.getAffectId() < 0L){
			if(this.factoryType == FactoryEnumType.UNKNOWN) throw new FactoryException("Invalid Factory Type for Bulk Identifiers");
			/// One of the numbers is from a bulk session - find that bulk session
			///
			String sessionId = BulkFactories.getBulkFactory().getSessionForBulkId(participant.getParticipantId() < 0L ? participant.getParticipantId() : (participant.getParticipationId() < 0L ? participant.getParticipationId() : participant.getAffectId()));
			if(sessionId == null){
				logger.error("Invalid bulk session id");
				throw new FactoryException("Invalid bulk session id");
			}
			/// logger.debug("Bulk id discovered.  Participant=" + participant.getParticipantId() + " / Participation=" + participant.getParticipationId() + ". Diverting to Bulk " + factoryType + " Operation");
			try {
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, factoryType, participant);
				//BulkFactories.getBulkFactory().setDirty(factoryType);
				return true;
			} catch (ArgumentException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			} 
			return false;
		}
		if(bulkMode){
			//logger.debug("Set the factory for a dirty write ");
			BulkFactories.getBulkFactory().setDirty(factoryType);
		}
		DataTable dt = dataTables.get(0);
		DataRow row = prepareAdd(participant, dt.getName());
		try{
			row.setCellValue("participationid", participant.getParticipationId());
			row.setCellValue("participantid", participant.getParticipantId());
			row.setCellValue("participanttype", participant.getParticipantType().toString());
			if (haveAffect)
			{
				row.setCellValue("affecttype", participant.getAffectType().toString());
				row.setCellValue("affectid", participant.getAffectId());
			}
		}
		catch(DataAccessException e){
			throw new FactoryException(e.getMessage());
		}

		/// Bulk insert note: prepareAdd and insertRow won't add the row to the local table row cache, so it must be added manually
		/// bulkMode is excluded because there is different behavior betweer using direct bulk insert and the bulk insert factory
		///
		if(!bulkMode && dt.getBulkInsert()) dt.getRows().add(row);
		
		return insertRow(row);

	}
	protected List<BaseRoleType> getRoleListFromParticipations(RoleParticipantType[] list, long organizationId) throws FactoryException, ArgumentException
	{
	
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return  Factories.getNameIdFactory(FactoryEnumType.ROLE).list(new QueryField[] { field }, organizationId);
		//return new ArrayList<BaseRoleType>();

	}
	protected List<FactType> getFactListFromParticipations(FactParticipantType[] list, long organizationId) throws FactoryException, ArgumentException
	{
	
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return  Factories.getNameIdFactory(FactoryEnumType.FACT).list(new QueryField[] { field }, organizationId);
		//return new ArrayList<BaseFactType>();

	}
	protected <T> List<T> getGroupListFromParticipations(BaseParticipantType[] list, long organizationId) throws FactoryException, ArgumentException
	{
		if(list.length == 0) return new ArrayList<T>();
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).list(new QueryField[]{ field }, organizationId);
	}
	protected <T> List<T> getUserListFromParticipations(BaseParticipantType[] list, long organizationId) throws FactoryException, ArgumentException
	{
		if(list.length == 0) return new ArrayList<T>();
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getNameIdFactory(FactoryEnumType.USER).list(new QueryField[]{ field }, organizationId);
	}	
	protected <T> List<T> getAccountListFromParticipations(BaseParticipantType[] list, long organizationId) throws FactoryException, ArgumentException
	{
		if(list.length == 0) return new ArrayList<T>();
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getNameIdFactory(FactoryEnumType.ACCOUNT).list(new QueryField[]{ field }, organizationId);
	}
	protected <T> List<T> getPersonListFromParticipations(BaseParticipantType[] list, long organizationId) throws FactoryException, ArgumentException
	{
		if(list.length == 0) return new ArrayList<T>();
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getNameIdFactory(FactoryEnumType.PERSON).list(new QueryField[]{ field }, organizationId);
	}
	protected <T> List<T> getDataListFromParticipations(BaseParticipantType[] list, long organizationId) throws FactoryException, ArgumentException
	{
		if(list.length == 0) return new ArrayList<T>();
		QueryField field = QueryFields.getFieldParticipantIds(list);
		ProcessingInstructionType pit = new ProcessingInstructionType();
		pit.setAlternateQuery(true);
		return Factories.getNameIdFactory(FactoryEnumType.DATA).list(new QueryField[]{ field }, pit, organizationId);
	}

	public <T> List<T> getListFromParticipations(FactoryEnumType fType, BaseParticipantType[] list, boolean alternateQuery, long startRecord, int recordCount, long organizationId) throws FactoryException, ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		INameIdFactory iFact = (INameIdFactory)Factories.getFactory(fType);
		if(fType == FactoryEnumType.DATA) return convertList(((DataFactory)iFact).getDataList(new QueryField[]{ field }, alternateQuery, startRecord, recordCount, organizationId));
		return iFact.paginateList(new QueryField[]{ field },startRecord, recordCount, organizationId);
	}
	public List<NameIdType> getParticipants(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participantType,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws FactoryException, ArgumentException
	{
		QueryField[] fields = QueryFields.getFieldParticipantsMatch(participation, participant, participantType, permission, affectType);
		
		return getByField(fields, participation.getOrganizationId());


	}
	public List<NameIdType> getParticipations(NameIdType[] maps, ParticipantEnumType participantType)  throws FactoryException, ArgumentException
	{

		if (maps.length == 0) return new ArrayList<NameIdType>();
		long org = maps[0].getOrganizationId();

		List<QueryField> matches = new ArrayList<>();
		matches.add(QueryFields.getFieldParticipantType(participantType));
		//StringBuilder buff = new StringBuilder();
		List<Long> ids = new ArrayList<>();
		for (int i = 0; i < maps.length; i++)
		{
			ids.add(maps[i].getId());
		}

		QueryField match = new QueryField(SqlDataEnumType.VARCHAR, "participationid", QueryFields.getFilteredLongList(convertLongList(ids)));
		match.setComparator(ComparatorEnumType.ANY);
		matches.add(match);
		return getByField(matches.toArray(new QueryField[0]), org);
	}
	@SuppressWarnings("unchecked")
	public <T> T getParticipant(NameIdType participation, NameIdType participant, ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		
		T out_participant = null;
		if (participation == null || participant == null || participation.getId() <= 0 || participant.getId() <= 0 || type == ParticipantEnumType.UNKNOWN) throw new ArgumentException("getParticipant: Invalid parameters");

		String keyName = participation.getId() + "-" + this.participationType + "-" + participant.getId() + "-" + type + "-" + participation.getOrganizationId();
		out_participant = (T)readCache(keyName);
		boolean add_to_cache = false;
		if (out_participant == null)
		{
			List<NameIdType> list = getByField(new QueryField[] { QueryFields.getFieldParticipantId(participant), QueryFields.getFieldParticipantType(type), QueryFields.getFieldParticipationId(participation)}, participation.getOrganizationId());
			if (list.isEmpty()) return null;
			out_participant = (T)list.get(0);
			add_to_cache = true;
		}
		
		if (add_to_cache){
			addToCache((NameIdType)out_participant, keyName);
		}
		return out_participant;

	}
	@SuppressWarnings("unchecked")
	public <T> T getParticipant(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participantType,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws FactoryException, ArgumentException
	{
		T out_participant = null;
		if (participation == null || participant == null || participation.getId().compareTo(0L)==0 || participant.getId().compareTo(0L)==0 || participantType == ParticipantEnumType.UNKNOWN){
				
				throw new ArgumentException("getParticipant: Invalid parameters.  "
					+ (participation == null ? " Null Participation." : "") + (participant == null ? " Null participant." : "") + (participantType == ParticipantEnumType.UNKNOWN ? " UNKNOWN Participant Type":"")
					+ (participation != null && participation.getId().compareTo(0L)==0 ? " Participation id is 0." : "")
					+ (participant != null && participant.getId().compareTo(0L)==0 ? " Participant id is 0." : "")
				);
		}

		String keyName = participation.getId() + "-" + this.participationType + "-" + participant.getId() + "-" + participantType + "-" + (permission != null ? permission.getId() : "0") + "-" + affectType + "-" + participation.getOrganizationId();
		out_participant = (T)readCache(keyName);
		boolean add_to_cache = false;
		
		/// Moving the negative part id check down is to support bulk operations
		///
		if (out_participant == null && participation.getId() > 0 && participant.getId() > 0)
		{
			List<NameIdType> list = getParticipants(participation, participant, participantType, permission, affectType);
			if (list.isEmpty()) return null;
			out_participant = (T)list.get(0);
			add_to_cache = true;
		}
		
		if (add_to_cache){
			addToCache((NameIdType)out_participant, keyName);
		}
		return out_participant;
	}

	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		BaseParticipantType useMap = (BaseParticipantType)map;
		fields.add(QueryFields.getFieldParticipantId(useMap));
		fields.add(QueryFields.getFieldParticipantType(useMap.getParticipantType()));
		fields.add(QueryFields.getFieldParticipationId(useMap));
		if(haveAffect){
			fields.add(QueryFields.getFieldAffectId(useMap));
			fields.add(QueryFields.getFieldAffectType(useMap));
		}
	}



	protected BaseParticipantType newParticipant(String participant_name, ParticipantEnumType type, long organizationId) throws ArgumentException
	{
		return newParticipant(participant_name, type, null, organizationId);
	}
	protected BaseParticipantType newParticipant(String participant_name, ParticipantEnumType type, BaseParticipantType Parent, long organizationId) throws ArgumentException
	{
		BaseParticipantType new_participant = newParticipant(type);
		//new_participant.setOrganization(organization);
		new_participant.setOrganizationId(organizationId);
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
		ParticipantEnumType participantType,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws ArgumentException
	{
		
		if (participation == null || participant == null || participation.getId().compareTo(0L)==0 || participant.getId().compareTo(0L)==0 || participantType == ParticipantEnumType.UNKNOWN){
			logger.warn("Invalid " + participationType + " Participant Parameters");
			if(participation == null) logger.warn("\tNull participation");
			else if(participant == null) logger.warn("\tNull participant");
			else{

				logger.warn("participantType=" + participantType);
				logger.warn("participantId=" + participant.getId());
				logger.warn("participationId=" + participation.getId());
			}
			throw new ArgumentException("Invalid parameters");
		}
		BaseParticipantType out_participant = newParticipant(participantType);
		out_participant.setNameType(NameEnumType.PARTICIPANT);
		out_participant.setParticipationType(this.participationType);
		out_participant.setParticipationId(participation.getId());
		out_participant.setParticipantId(participant.getId());
		out_participant.setOwnerId(participation.getOwnerId());
		//out_participant.setOrganization(participation.getOrganization());
		out_participant.setOrganizationId(participation.getOrganizationId());
		if (permission != null) out_participant.setAffectId(permission.getId());
		out_participant.setAffectType(affectType);

		return out_participant;
	}


	
}
