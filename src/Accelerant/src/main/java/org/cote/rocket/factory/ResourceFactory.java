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
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.types.ResourceEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;


public class ResourceFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.RESOURCE, ResourceFactory.class); }
	public ResourceFactory(){
		super();
		this.tableNames.add("resource");
		factoryType = FactoryEnumType.RESOURCE;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("resource")){
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
		ResourceType rec = (ResourceType)obj;
		if(rec.getPopulated() == true) return;
		if(rec.getResourceDataId() > 0){
			if(rec.getResourceType() == ResourceEnumType.USER){
				rec.setResourceData((UserType)Factories.getNameIdFactory(FactoryEnumType.USER).getById(rec.getResourceDataId(), rec.getOrganizationId()));
			}
		}
		
		rec.setPopulated(true);
		updateToCache(rec);
	}
	public ResourceType newResource(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		ResourceType obj = new ResourceType();
		obj.setName(user.getName());
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setResourceType(ResourceEnumType.UNKNOWN);
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.RESOURCE);
		return obj;
	}

	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ResourceType obj = (ResourceType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Resource without a group");

		DataRow row = prepareAdd(obj, "resource");
		try{
			//if(obj.getResourceData() != null) row.setCellValue("resourceid", obj.getResourceData().getId());
			row.setCellValue("estimateid", (obj.getEstimate() != null ? obj.getEstimate().getId() : 0L));
			row.setCellValue("scheduleid", (obj.getSchedule() != null ? obj.getSchedule().getId() : 0L));
			row.setCellValue("resourceid", obj.getResourceDataId());
			row.setCellValue("utilization",obj.getUtilization());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("resourcetype", obj.getResourceType().toString());
			row.setCellValue("groupid", obj.getGroupId());
			if (insertRow(row)) return true;
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		ResourceType newObj = new ResourceType();

		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setNameType(NameEnumType.RESOURCE);
		newObj.setResourceDataId(rset.getLong("resourceid"));
		newObj.setUtilization(rset.getDouble("utilization"));
		newObj.setResourceType(ResourceEnumType.valueOf(rset.getString("ResourceType")));
		newObj.setDescription(rset.getString("description"));
		long estimate_id = rset.getLong("estimateid");
		if(estimate_id > 0L) newObj.setEstimate((EstimateType)((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getById(estimate_id, newObj.getOrganizationId()));
		long schedule_id = rset.getLong("scheduleid");
		if(schedule_id > 0L) newObj.setSchedule((ScheduleType)((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).getById(schedule_id, newObj.getOrganizationId()));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		NameIdType data = (NameIdType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ResourceType useMap = (ResourceType)map;
		fields.add(QueryFields.getFieldResourceId(useMap.getResourceDataId()));
		fields.add(QueryFields.getFieldResourceType(useMap.getResourceType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldUtilization(useMap.getUtilization()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
		fields.add(QueryFields.getFieldEstimateId((useMap.getEstimate() != null ? useMap.getEstimate().getId() : 0L)));
		fields.add(QueryFields.getFieldScheduleId((useMap.getSchedule() != null ? useMap.getSchedule().getId() : 0L)));
	}
	public int deleteResourcesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteResourcesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteResourcesByIds(long[] ids, long organizationId) throws FactoryException
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
	public int deleteResourcesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteResourcesByIds(ids, group.getOrganizationId());
	}
	
	
	public List<ResourceType>  getResourceList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ResourceType> getResourceListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
