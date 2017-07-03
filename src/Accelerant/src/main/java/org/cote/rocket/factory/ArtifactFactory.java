/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.types.ArtifactEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.query.QueryFields;


public class ArtifactFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.ARTIFACT, ArtifactFactory.class); }
	public ArtifactFactory(){
		super();
		this.tableNames.add("artifact");
		factoryType = FactoryEnumType.ARTIFACT;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("artifact")){
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
		ArtifactType artifact = (ArtifactType)obj;
		if(artifact.getPopulated() == true) return;
		List<NameIdType> arts = new ArrayList<NameIdType>();
		if(artifact.getNextTransitionId() > 0L){
			arts = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getById(artifact.getNextTransitionId(), artifact.getOrganizationId());
			if(arts.size() > 0) artifact.setNextTransition((ArtifactType)arts.get(0));
			else throw new FactoryException("Artifact next transition id is out of sync.");
		}
		if(artifact.getPreviousTransitionId() > 0L){
			arts = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getById(artifact.getPreviousTransitionId(), artifact.getOrganizationId());
			if(arts.size() > 0) artifact.setPreviousTransition((ArtifactType)arts.get(0));
			else throw new FactoryException("Artifact previous transition id is out of sync.");
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
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
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
		Long tmpId = 0L;
		if(ait.getArtifactDataId().compareTo(0L) < 0){
			tmpId = BulkFactories.getBulkFactory().getMappedId(ait.getArtifactDataId());
			if(tmpId.compareTo(0L) > 0) ait.setArtifactDataId(tmpId.longValue());
		}
	}
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		ArtifactType obj = (ArtifactType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Artifact without a group");

		DataRow row = prepareAdd(obj, "artifact");
		try{
			row.setCellValue("artifactdataid", obj.getArtifactDataId());
			row.setCellValue("previoustransitionid",obj.getPreviousTransitionId());
			row.setCellValue("nexttransitionid",obj.getNextTransitionId());
			row.setCellValue("artifacttype", obj.getArtifactType().toString());
			row.setCellValue("groupid", obj.getGroupId());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("createddate", obj.getCreatedDate());
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
		ArtifactType new_obj = new ArtifactType();
		new_obj.setNameType(NameEnumType.ARTIFACT);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		new_obj.setArtifactDataId(rset.getLong("artifactdataid"));
		new_obj.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		new_obj.setArtifactType(ArtifactEnumType.valueOf(rset.getString("artifacttype")));
		new_obj.setNextTransitionId(rset.getLong("nexttransitionid"));
		new_obj.setPreviousTransitionId(rset.getLong("previoustransitionid"));
		new_obj.setDescription(rset.getString("description"));
		return new_obj;
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
		ArtifactType use_map = (ArtifactType)map;
		fields.add(QueryFields.getFieldArtifactDataId(use_map.getArtifactDataId()));
		fields.add(QueryFields.getFieldArtifactType(use_map.getArtifactType()));
		fields.add(QueryFields.getFieldCreatedDate(use_map.getCreatedDate()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldPreviousTransitionId(use_map.getPreviousTransitionId()));
		fields.add(QueryFields.getFieldNextTransitionId(use_map.getNextTransitionId()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroupId()));
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
	public int deleteArtifactsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
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
