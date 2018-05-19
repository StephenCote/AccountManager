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
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.types.EstimateEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;


public class EstimateFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.ESTIMATE, EstimateFactory.class); }
	public EstimateFactory(){
		super();
		this.tableNames.add("estimate");
		factoryType = FactoryEnumType.ESTIMATE;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("estimate")){
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
		EstimateType estimate = (EstimateType)obj;
		if(estimate.getPopulated()) return;
		estimate.setPopulated(true);
		updateToCache(estimate);
	}
	
	public EstimateType newEstimate(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		EstimateType obj = new EstimateType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setEstimateType(EstimateEnumType.UNKNOWN);
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.ESTIMATE);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		EstimateType obj = (EstimateType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Estimate without a group");

		DataRow row = prepareAdd(obj, "estimate");
		try{
			row.setCellValue("costid",(obj.getCost() != null ? obj.getCost().getId() : 0L));
			
			row.setCellValue("timeid",(obj.getTime() != null ? obj.getTime().getId() : 0L));
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("estimatetype", obj.getEstimateType().toString());
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
		EstimateType newObj = new EstimateType();
		newObj.setNameType(NameEnumType.ESTIMATE);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		
		newObj.setEstimateType(EstimateEnumType.valueOf(rset.getString("estimatetype")));
		newObj.setDescription(rset.getString("description"));
		long time_id = rset.getLong("timeid");
		long cost_id = rset.getLong("costid");
		if(time_id > 0L) newObj.setTime((TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getById(time_id, newObj.getOrganizationId()));
		if(cost_id > 0L) newObj.setCost((CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getById(cost_id, newObj.getOrganizationId()));
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
		EstimateType useMap = (EstimateType)map;
		fields.add(QueryFields.getFieldEstimateType(useMap.getEstimateType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		//if(useMap.getCost() != null)
		fields.add(QueryFields.getFieldCost(useMap.getCost()));
		//if(useMap.getTime() != null)
		fields.add(QueryFields.getFieldTime(useMap.getTime()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteEstimatesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteEstimatesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteEstimatesByIds(long[] ids, long organizationId) throws FactoryException
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
	public int deleteEstimatesInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteEstimatesByIds(ids, group.getOrganizationId());
	}
	
	
	public List<EstimateType>  getEstimateList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<EstimateType> getEstimateListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
