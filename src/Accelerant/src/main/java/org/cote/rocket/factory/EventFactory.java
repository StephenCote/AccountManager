/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rocket.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.EventType;
import org.cote.propellant.objects.types.EventEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class EventFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.EVENT, EventFactory.class); }
	public EventFactory(){
		super();
		this.tableNames.add("event");
		this.hasParentId = true;
		factoryType = FactoryEnumType.EVENT;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("event")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getParentId() + "-" + t.getGroupId();
	}
	

	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		EventType event = (EventType)obj;
		event.getActors().clear();
		event.getObservers().clear();
		event.getInfluencers().clear();
		event.getOrchestrators().clear();
		event.getThings().clear();
		event.getEntryTraits().clear();
		event.getExitTraits().clear();
		event.getGroups().clear();
		
		for(EventType cevent : event.getChildEvents()){
			depopulate(cevent);
		}
		event.getChildEvents().clear();
		event.setPopulated(false);
		updateToCache(event);
		
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		EventType cobj = (EventType)obj;
		if(cobj.getPopulated() == true) return;
		cobj.getChildEvents().addAll(getChildEventList(cobj));
		cobj.getActors().addAll(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getActorsFromParticipation(cobj));
		cobj.getObservers().addAll(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getObserversFromParticipation(cobj));
		cobj.getInfluencers().addAll(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getInfluencersFromParticipation(cobj));
		cobj.getOrchestrators().addAll(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getOrchestratorsFromParticipation(cobj));
		cobj.getThings().addAll(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getThingsFromParticipation(cobj));
		cobj.getEntryTraits().addAll(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getEntryTraitsFromParticipation(cobj));
		cobj.getExitTraits().addAll(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getExitTraitsFromParticipation(cobj));
		cobj.getGroups().addAll(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getGroupsFromParticipation(cobj));
		cobj.setPopulated(true);
		updateToCache(cobj);
	}
	public EventType newEvent(UserType user, EventType parentEvent) throws ArgumentException{
		EventType event = newEvent(user, parentEvent.getGroupId());
		event.setParentId(parentEvent.getId());
		event.setStartDate(parentEvent.getStartDate());
		event.setEndDate(parentEvent.getEndDate());
		
		return event;
	}
	public EventType newEvent(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		EventType obj = new EventType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setEventType(EventEnumType.UNKNOWN);
		obj.setParentId(0L);
		Calendar now = Calendar.getInstance();
		XMLGregorianCalendar cal = CalendarUtil.getXmlGregorianCalendar(now.getTime()); 
		obj.setStartDate(cal);
		obj.setEndDate(cal);

		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.EVENT);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		EventType obj = (EventType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Event without a group");

		DataRow row = prepareAdd(obj, "event");
		try{
			row.setCellValue("startdate",obj.getStartDate());
			row.setCellValue("enddate",obj.getEndDate());
			row.setCellValue("locationid",(obj.getLocation() != null ? obj.getLocation().getId() : 0L));
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("eventtype", obj.getEventType().toString());
			row.setCellValue("groupid", obj.getGroupId());
			if (insertRow(row)){
				
				//EventType cobj = (bulkMode ? obj : (EventType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
				EventType cobj = null;
				if(bulkMode) cobj = obj;
				else if(obj.getParentId() > 0L){
					EventType parent = getById(obj.getParentId(),obj.getOrganizationId());
					if(parent == null) throw new FactoryException("Unable to update orphaned task without correcting the parent");
					cobj = getByNameInGroup(obj.getName(),obj.getParentId(),((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(),obj.getOrganizationId()));
				}
				else{
					cobj = (EventType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId()));
				}
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;
				for(int i = 0; i < obj.getThings().size();i++){
					part = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newThingParticipation(cobj,obj.getThings().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
					else ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getExitTraits().size();i++){
					part = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newExitTraitParticipation(cobj,obj.getExitTraits().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
					else ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
				}
				//logger.info("Event Entry Traits: " + obj.getEntryTraits().size());
				for(int i = 0; i < obj.getEntryTraits().size();i++){
					part = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newEntryTraitParticipation(cobj,obj.getEntryTraits().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
					else ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
				}

				for(int i = 0; i < obj.getActors().size();i++){
					part = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newActorParticipation(cobj,obj.getActors().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
					else ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getObservers().size();i++){
					part = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newObserverParticipation(cobj,obj.getObservers().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
					else ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getInfluencers().size();i++){
					part = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newInfluencerParticipation(cobj,obj.getInfluencers().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
					else ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getOrchestrators().size();i++){
					part = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newOrchestratorParticipation(cobj,obj.getOrchestrators().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
					else ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getGroups().size();i++){
					part = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newGroupParticipation(cobj,obj.getGroups().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
					else ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(part);
				}
				
				return true;

			}
		}
		catch(DataAccessException | ArgumentException dae){
			logger.error(FactoryException.LOGICAL_EXCEPTION,dae);
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		EventType newObj = new EventType();
		newObj.setNameType(NameEnumType.EVENT);
		super.read(rset, newObj);
		readGroup(rset, newObj);

		// TODO: set time, cost
		newObj.setStartDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("startdate")));
		newObj.setEndDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("enddate")));
		newObj.setDescription(rset.getString("description"));
		long locId = rset.getLong("locationid");
		if(locId > 0L) newObj.setLocation(((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getById(locId, newObj.getOrganizationId()));
		newObj.setEventType(EventEnumType.valueOf(rset.getString("eventtype")));

		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		EventType data = (EventType)object;
		boolean outBool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
			Set<Long> set = new HashSet<>();
			BaseParticipantType[] maps = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getActorParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getActors().size();i++){
				if(set.contains(data.getActors().get(i).getId())== false){
					((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newActorParticipation(data,data.getActors().get(i)));
				}
				else{
					set.remove(data.getActors().get(i).getId());
				}
			}
			((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

			set = new HashSet<>();
			maps = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getObserverParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getObservers().size();i++){
				if(set.contains(data.getObservers().get(i).getId())== false){
					((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newObserverParticipation(data,data.getObservers().get(i)));
				}
				else{
					set.remove(data.getObservers().get(i).getId());
				}
			}
			((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

			set = new HashSet<>();
			maps = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getInfluencerParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getInfluencers().size();i++){
				if(set.contains(data.getInfluencers().get(i).getId())== false){
					((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newInfluencerParticipation(data,data.getInfluencers().get(i)));
				}
				else{
					set.remove(data.getInfluencers().get(i).getId());
				}
			}
			((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

			set = new HashSet<>();
			maps = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getOrchestratorParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getOrchestrators().size();i++){
				if(set.contains(data.getOrchestrators().get(i).getId())== false){
					((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newOrchestratorParticipation(data,data.getOrchestrators().get(i)));
				}
				else{
					set.remove(data.getOrchestrators().get(i).getId());
				}
			}
			((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

			
			set = new HashSet<>();
			maps = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getThingParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getThings().size();i++){
				if(set.contains(data.getThings().get(i).getId())== false){
					((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newThingParticipation(data,data.getThings().get(i)));
				}
				else{
					set.remove(data.getThings().get(i).getId());
				}
			}
			((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

			
			set = new HashSet<>();
			maps = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getEntryTraitParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getEntryTraits().size();i++){
				if(set.contains(data.getEntryTraits().get(i).getId())== false){
					((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newEntryTraitParticipation(data,data.getEntryTraits().get(i)));
				}
				else{
					set.remove(data.getEntryTraits().get(i).getId());
				}
			}
			((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

			
			set = new HashSet<>();
			maps = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getExitTraitParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getExitTraits().size();i++){
				if(set.contains(data.getExitTraits().get(i).getId())== false){
					((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newExitTraitParticipation(data,data.getExitTraits().get(i)));
				}
				else{
					set.remove(data.getExitTraits().get(i).getId());
				}
			}
			((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

			set = new HashSet<>();
			maps = ((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).getGroupParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getGroups().size();i++){
				if(set.contains(data.getGroups().get(i).getId())== false){
					((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).add(((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).newGroupParticipation(data,data.getGroups().get(i)));
				}
				else{
					set.remove(data.getGroups().get(i).getId());
				}
			}
			((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());

			
			outBool = true;
			}
			catch(ArgumentException e){
				logger.error(e.getMessage());
			}
		}
		return outBool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		EventType useMap = (EventType)map;
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldEventType(useMap.getEventType()));
		fields.add(QueryFields.getFieldLocationId((useMap.getLocation() != null ? useMap.getLocation().getId() : 0L)));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteEventsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteEventsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		if(bulkMode) return true;
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteEventsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteEventsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteEventsByIds(ids, group.getOrganizationId());
	}
	
	public List<EventType> getChildEventList(EventType parent) throws FactoryException,ArgumentException{

		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		return getEventList(fields.toArray(new QueryField[0]), 0,0,parent.getOrganizationId());
	}
	
	public List<EventType>  getEventList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<EventType> getEventListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
