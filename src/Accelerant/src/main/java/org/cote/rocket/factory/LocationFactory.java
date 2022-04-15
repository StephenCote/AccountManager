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
package org.cote.rocket.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.types.GeographyEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;

public class LocationFactory extends NameIdGroupFactory {
	
	public LocationFactory(){
		super();
		this.primaryTableName = "location";
		this.tableNames.add(primaryTableName);
		this.hasParentId = true;
		factoryType = FactoryEnumType.LOCATION;
		this.clusterByParent = true;
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// restrict columns
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
		LocationType location = (LocationType)obj;
		location.getBorders().clear();
		location.getBoundaries().clear();
		for(LocationType cloc : location.getChildLocations()){
			depopulate(cloc);
		}
		location.getChildLocations().clear();
		location.setPopulated(false);
		removeFromCache(location);
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		LocationType cobj = (LocationType)obj;
		if(cobj.getPopulated().booleanValue()) return;
		cobj.getChildLocations().addAll(getChildLocationList(cobj));
		cobj.getBoundaries().addAll(((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).getBoundariesFromParticipation(cobj));
		cobj.getBorders().addAll(((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).getBordersFromParticipation(cobj));
		cobj.setPopulated(true);
		updateToCache(cobj);
	}
	public LocationType newLocation(UserType user, LocationType parent) throws ArgumentException{
		LocationType location = newLocation(user, parent.getGroupId());
		location.setParentId(parent.getId());
		return location;
	}
	public LocationType newLocation(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		LocationType obj = new LocationType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGeographyType(GeographyEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.LOCATION);
		obj.setGroupId(groupId);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		LocationType obj = (LocationType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Location without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.GEOGRAPHYTYPE), obj.getGeographyType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.CLASSIFICATION), obj.getClassification());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			if (insertRow(row)){
				LocationType cobj = null;
				if(bulkMode) cobj = obj;
				else if(obj.getParentId() > 0L){
					LocationType parent = getById(obj.getParentId(),obj.getOrganizationId());
					if(parent == null) throw new FactoryException("Unable to update orphaned task without correcting the parent");
					cobj = getByNameInGroup(obj.getName(),obj.getParentId(),((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(),obj.getOrganizationId()));
				}
				else{
					cobj = (LocationType)getByNameInGroup(obj.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(obj.getGroupId(), obj.getOrganizationId()));
				}
				if(cobj == null) throw new DataAccessException("Failed to retrieve new object");
				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;
				for(int i = 0; i < obj.getBoundaries().size();i++){
					part = ((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).newBoundaryParticipation(cobj,obj.getBoundaries().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.LOCATIONPARTICIPATION)).add(part);
					else ((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).add(part);
				}
				for(int i = 0; i < obj.getBorders().size();i++){
					part = ((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).newBorderParticipation(cobj,obj.getBorders().get(i));
					if(bulkMode) ((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.LOCATIONPARTICIPATION)).add(part);
					else ((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).add(part);
				}
				
				return true;
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} catch (ArgumentException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		LocationType newObj = new LocationType();
		newObj.setNameType(NameEnumType.LOCATION);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setClassification(rset.getString(Columns.get(ColumnEnumType.CLASSIFICATION)));
		newObj.setGeographyType(GeographyEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.GEOGRAPHYTYPE))));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		LocationType data = (LocationType)object;
		boolean outBool = false;
		removeFromCache(data);
		if(update(data, null)){
			/// Locations
			///
			try{
				Set<Long> set = new HashSet<>();
				BaseParticipantType[] maps = ((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).getBorderParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getBorders().size();i++){
					if(!set.contains(data.getBorders().get(i).getId())){
						((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).add(((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).newBorderParticipation(data,data.getBorders().get(i)));
					}
					else{
						set.remove(data.getBorders().get(i).getId());
					}
				}
				((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				
				set = new HashSet<>();
				maps = ((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).getBoundaryParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getBoundaries().size();i++){
					if(!set.contains(data.getBoundaries().get(i).getId())){
						((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).add(((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).newBoundaryParticipation(data,data.getBoundaries().get(i)));
					}
					else{
						set.remove(data.getBoundaries().get(i).getId());
					}
				}
				((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				outBool = true;
			}
			catch(ArgumentException e){
				throw new FactoryException(e.getMessage());
			}
		}
		return outBool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		LocationType useMap = (LocationType)map;
		fields.add(QueryFields.getFieldGeographyType(useMap.getGeographyType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldClassification(useMap.getClassification()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteLocationsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteLocationsByIds(ids, user.getOrganizationId());
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
	public int deleteLocationsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	public int deleteLocationsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteLocationsByIds(ids, group.getOrganizationId());
	}
	public List<LocationType> getChildLocationList(LocationType parent) throws FactoryException,ArgumentException{

		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		return getLocationList(fields.toArray(new QueryField[0]), 0,0,parent.getOrganizationId());
	}
	
	public List<LocationType>  getLocationList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<LocationType> getLocationListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
