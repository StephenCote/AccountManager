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
import java.util.Arrays;
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
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.TicketType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.types.PriorityEnumType;
import org.cote.propellant.objects.types.SeverityEnumType;
import org.cote.propellant.objects.types.TicketStatusEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class TicketFactory extends NameIdGroupFactory {
	
	public TicketFactory(){
		super();
		this.tableNames.add("ticket");
		factoryType = FactoryEnumType.TICKET;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("ticket")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		TicketType ticket = (TicketType)obj;
		if(ticket.getPopulated()) return;

		ticket.getDependencies().addAll(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getDependenciesFromParticipation(ticket));
		ticket.getArtifacts().addAll(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getArtifactsFromParticipation(ticket));
		ticket.getNotes().addAll(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getNotesFromParticipation(ticket));
		ticket.getRequiredResources().addAll(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getResourcesFromParticipation(ticket));
		ticket.getAudit().addAll(Arrays.asList(Factories.getAuditFactory().getAuditByTarget(AuditEnumType.TICKET, ticket.getId().toString())));
		ticket.getForms().addAll(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getFormsFromParticipation(ticket));
		ticket.setPopulated(true);
		updateToCache(ticket);
	}
	
	public TicketType newTicket(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		TicketType obj = new TicketType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		Calendar now = Calendar.getInstance();
		obj.setTicketStatus(TicketStatusEnumType.UNKNOWN);
		obj.setPriority(PriorityEnumType.NORMAL);
		obj.setSeverity(SeverityEnumType.LOW);
		XMLGregorianCalendar cal = CalendarUtil.getXmlGregorianCalendar(now.getTime()); 
		obj.setCreatedDate(cal);
		obj.setModifiedDate(cal);
		obj.setDueDate(cal);
		obj.setClosedDate(cal);
		obj.setReopenedDate(cal);
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.TICKET);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		TicketType obj = (TicketType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Ticket without a group");

		DataRow row = prepareAdd(obj, "ticket");
		try{
			row.setCellValue("priority", obj.getPriority().toString());
			row.setCellValue("severity", obj.getSeverity().toString());
			row.setCellValue("ticketstatus", obj.getTicketStatus().toString());
			row.setCellValue("createddate",obj.getCreatedDate());
			row.setCellValue("modifieddate",obj.getModifiedDate());
			row.setCellValue("duedate",obj.getDueDate());
			row.setCellValue("closeddate",obj.getClosedDate());
			row.setCellValue("reopeneddate",obj.getReopenedDate());
			row.setCellValue("description",obj.getDescription());
			//row.setCellValue("wasreopened",obj.getWasReopened());
			if(obj.getAssignedResource() != null) row.setCellValue("assignedresourceid", obj.getAssignedResource().getId());
			if(obj.getEstimate() != null) row.setCellValue("estimateid", obj.getEstimate().getId());
			if(obj.getActualCost() != null) row.setCellValue("actualcostid", obj.getActualCost().getId());
			if(obj.getActualTime() != null) row.setCellValue("actualtimeid", obj.getActualTime().getId());
			
			row.setCellValue("groupid", obj.getGroupId());
			if (insertRow(row)){
				try{
					TicketType cobj = (bulkMode ? obj : (TicketType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId())));
					if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
					BulkFactories.getBulkFactory().setDirty(factoryType);
					BaseParticipantType part = null;
					for(int i = 0; i < obj.getNotes().size();i++){
						part = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newNoteParticipation(cobj,obj.getNotes().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
						else ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
					}
	
					for(int i = 0; i < obj.getRequiredResources().size();i++){
						part = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newResourceParticipation(cobj,obj.getRequiredResources().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
						else ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
					}

					for(int i = 0; i < obj.getDependencies().size();i++){
						part = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newDependencyParticipation(cobj,obj.getDependencies().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
						else ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
					}
					for(int i = 0; i < obj.getArtifacts().size();i++){
						part= ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newArtifactParticipation(cobj,obj.getArtifacts().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
						else ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
						
					}
					for(int i = 0; i < obj.getForms().size();i++){
						part = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newFormParticipation(cobj,obj.getForms().get(i));
						if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
						else ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(part);
					}
					return true;
				}
				catch(ArgumentException ae){
					logger.error(FactoryException.LOGICAL_EXCEPTION,ae);
					throw new FactoryException(ae.getMessage());
					
				}
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		TicketType newObj = new TicketType();
		newObj.setNameType(NameEnumType.TICKET);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setSeverity(SeverityEnumType.valueOf(rset.getString("severity")));
		newObj.setPriority(PriorityEnumType.valueOf(rset.getString("priority")));
		newObj.setTicketStatus(TicketStatusEnumType.valueOf(rset.getString("ticketstatus")));
		newObj.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		newObj.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		newObj.setDueDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("duedate")));
		newObj.setClosedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("closeddate")));
		newObj.setReopenedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("reopeneddate")));
		newObj.setDescription(rset.getString("description"));
		//newObj.setWasReopened(rset.getBoolean("wasreopened"));
		
		long assign_id = rset.getLong("assignedresourceid");
		if(assign_id > 0) newObj.setAssignedResource((ResourceType)((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).getById(assign_id, newObj.getOrganizationId()));
		
		long est_id = rset.getLong("estimateid");
		if(est_id > 0) newObj.setEstimate((EstimateType)((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getById(est_id, newObj.getOrganizationId()));
		
		long cost_id = rset.getLong("actualcostid");
		if(cost_id > 0) newObj.setActualCost((CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getById(cost_id, newObj.getOrganizationId()));
		
		long time_id = rset.getLong("actualtimeid");
		if(time_id > 0) newObj.setActualTime((TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getById(time_id, newObj.getOrganizationId()));
		
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		TicketType data = (TicketType)object;
		boolean outBool = false;
		removeFromCache(data);
		Calendar now = Calendar.getInstance();

		data.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		
		if(update(data, null)){
			try{
			/// Goals
			Set<Long> set = new HashSet<>();
			BaseParticipantType[] maps = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getNoteParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getNotes().size();i++){
				if(set.contains(data.getNotes().get(i).getId())== false){
					((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newNoteParticipation(data,data.getNotes().get(i)));
				}
				else{
					set.remove(data.getNotes().get(i).getId());
				}
			}
//			System.out.println("Net delete Note parts: " + set.size());
			((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// Resources
			set.clear();
			maps = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getResourceParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getRequiredResources().size();i++){
				if(set.contains(data.getRequiredResources().get(i).getId())== false){
					((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newResourceParticipation(data,data.getRequiredResources().get(i)));
				}
				else{
					set.remove(data.getRequiredResources().get(i).getId());
				}
			}
//			System.out.println("Net delete Resource parts: " + set.size());
			((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			
			/// Dependencies
			set.clear();
			maps = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getDependencyParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getDependencies().size();i++){
				if(set.contains(data.getDependencies().get(i).getId())== false){
					((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newDependencyParticipation(data,data.getDependencies().get(i)));
				}
				else{
					set.remove(data.getDependencies().get(i).getId());
				}
			}
//			System.out.println("Net delete Dependency parts: " + set.size());
			((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			/// Artifacts
			set.clear();
			maps = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getArtifactParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getArtifacts().size();i++){
				if(set.contains(data.getArtifacts().get(i).getId())== false){
					((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newArtifactParticipation(data,data.getArtifacts().get(i)));
				}
				else{
					set.remove(data.getArtifacts().get(i).getId());
				}
			}
//			System.out.println("Net delete Artifact parts: " + set.size());
			((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			
			/// Forms
			set.clear();
			maps = ((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).getFormParticipations(data).toArray(new BaseParticipantType[0]);
			for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
			
			for(int i = 0; i < data.getForms().size();i++){
				if(set.contains(data.getForms().get(i).getId())== false){
					((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newFormParticipation(data,data.getForms().get(i)));
				}
				else{
					set.remove(data.getForms().get(i).getId());
				}
			}
//			System.out.println("Net delete Form parts: " + set.size());
			((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
			
			
			
			outBool = true;
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}
		}
		return outBool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		TicketType useMap = (TicketType)map;
		fields.add(QueryFields.getFieldPriority(useMap.getPriority()));
		fields.add(QueryFields.getFieldSeverity(useMap.getSeverity()));
		fields.add(QueryFields.getFieldTicketStatus(useMap.getTicketStatus()));
		fields.add(QueryFields.getFieldDueDate(useMap.getDueDate()));
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));
		fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		fields.add(QueryFields.getFieldClosedDate(useMap.getClosedDate()));
		fields.add(QueryFields.getFieldReopenedDate(useMap.getReopenedDate()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldAssignedResourceId(useMap.getAssignedResource() != null ? useMap.getAssignedResource().getId() : 0L));
		fields.add(QueryFields.getFieldEstimateId(useMap.getEstimate() != null ? useMap.getEstimate().getId() : 0L));
		fields.add(QueryFields.getFieldActualCostId(useMap.getActualCost() != null ? useMap.getActualCost().getId() : 0L));
		fields.add(QueryFields.getFieldActualTimeId(useMap.getActualTime() != null ? useMap.getActualTime().getId() : 0L));
		
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteTicketsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteTicketsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteTicketsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).deleteParticipations(ids, organizationId);
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deleteTicketsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteTicketsByIds(ids, group.getOrganizationId());
	}
	
	
	public List<TicketType>  getTicketList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<TicketType> getTicketListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
