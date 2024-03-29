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
import java.util.Calendar;
import java.util.List;

import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.types.ArtifactEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;


public class ArtifactFactory extends NameIdGroupFactory {
	
	public ArtifactFactory(){
		super();
		this.primaryTableName = "artifact";
		this.tableNames.add(primaryTableName);
		factoryType = FactoryEnumType.ARTIFACT;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// restrict columns
		}
	}
	
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		ArtifactType artifact = (ArtifactType)obj;
		if(artifact.getPopulated().booleanValue()) return;

		if(artifact.getNextTransitionId() > 0L){
			ArtifactType art = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getById(artifact.getNextTransitionId(), artifact.getOrganizationId());
			artifact.setNextTransition(art);
		}
		if(artifact.getPreviousTransitionId() > 0L){
			ArtifactType art = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getById(artifact.getPreviousTransitionId(), artifact.getOrganizationId());
			artifact.setPreviousTransition(art);
		}
		/*
		if(artifact.getArtifactDataId() > 0){
.			artifact.setArtifactData(((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataById(artifact.getArtifactDataId(), artifact.getOrganizationId()));
		}
		*/
		artifact.setPopulated(true);
		updateToCache(artifact);
	}
	
	public ArtifactType newArtifact(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		ArtifactType obj = new ArtifactType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setArtifactType(ArtifactEnumType.UNKNOWN);
		Calendar now = Calendar.getInstance();
		obj.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		obj.setPreviousTransitionId((long)0);
		obj.setNextTransitionId((long)0);
		obj.setArtifactDataId((long)0);
		obj.setNameType(NameEnumType.ARTIFACT);
		obj.setGroupId(groupId);

		return obj;
	}
	
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		ArtifactType ait = (ArtifactType)map;
		if(ait.getArtifactDataId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(ait.getArtifactDataId());
			if(tmpId.compareTo(0L) > 0) ait.setArtifactDataId(tmpId);
		}
	}
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ArtifactType obj = (ArtifactType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Artifact without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.ARTIFACTDATAID), obj.getArtifactDataId());
			row.setCellValue(Columns.get(ColumnEnumType.PREVIOUSTRANSITIONID),obj.getPreviousTransitionId());
			row.setCellValue(Columns.get(ColumnEnumType.NEXTTRANSITIONID),obj.getNextTransitionId());
			row.setCellValue(Columns.get(ColumnEnumType.ARTIFACTTYPE), obj.getArtifactType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			row.setCellValue(Columns.get(ColumnEnumType.CREATEDDATE), obj.getCreatedDate());
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
		ArtifactType newObj = new ArtifactType();
		newObj.setNameType(NameEnumType.ARTIFACT);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setArtifactDataId(rset.getLong(Columns.get(ColumnEnumType.ARTIFACTDATAID)));
		newObj.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.CREATEDDATE))));
		newObj.setArtifactType(ArtifactEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.ARTIFACTTYPE))));
		newObj.setNextTransitionId(rset.getLong(Columns.get(ColumnEnumType.NEXTTRANSITIONID)));
		newObj.setPreviousTransitionId(rset.getLong(Columns.get(ColumnEnumType.PREVIOUSTRANSITIONID)));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
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
		ArtifactType useMap = (ArtifactType)map;
		fields.add(QueryFields.getFieldArtifactDataId(useMap.getArtifactDataId()));
		fields.add(QueryFields.getFieldArtifactType(useMap.getArtifactType()));
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldPreviousTransitionId(useMap.getPreviousTransitionId()));
		fields.add(QueryFields.getFieldNextTransitionId(useMap.getNextTransitionId()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteArtifactsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteArtifactsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteArtifactsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	public int deleteArtifactsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		return deleteArtifactsByIds(ids, group.getOrganizationId());
	}
	
	
	public List<ArtifactType>  getArtifactList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<ArtifactType> getArtifactListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
