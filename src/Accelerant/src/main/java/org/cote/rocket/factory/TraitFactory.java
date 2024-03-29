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
import org.cote.propellant.objects.TraitType;
import org.cote.propellant.objects.types.AlignmentEnumType;
import org.cote.propellant.objects.types.TraitEnumType;
import org.cote.rocket.query.QueryFields;


public class TraitFactory extends NameIdGroupFactory {
	
	public TraitFactory(){
		super();
		this.primaryTableName = "trait";
		this.tableNames.add(primaryTableName);
		factoryType = FactoryEnumType.TRAIT;
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// restrict columns
		}
	}

	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		TraitType cobj = (TraitType)obj;
		if(cobj.getPopulated().booleanValue()) return;

		cobj.setPopulated(true);
		updateToCache(cobj);
	}
	
	public TraitType newTrait(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		TraitType obj = new TraitType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setTraitType(TraitEnumType.UNKNOWN);
		obj.setAlignmentType(AlignmentEnumType.NEUTRAL);
		obj.setScore(0);
		obj.setNameType(NameEnumType.TRAIT);
		obj.setGroupId(groupId);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		TraitType obj = (TraitType)object;
		if (obj.getGroupId() == null) throw new FactoryException("Cannot add new Trait without a group");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.TRAITTYPE), obj.getTraitType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.GROUPID), obj.getGroupId());
			row.setCellValue(Columns.get(ColumnEnumType.SCORE), obj.getScore());
			row.setCellValue(Columns.get(ColumnEnumType.ALIGNMENT), obj.getAlignmentType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			if (insertRow(row)){
				return true;
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
		TraitType newObj = new TraitType();
		newObj.setNameType(NameEnumType.TRAIT);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setTraitType(TraitEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.TRAITTYPE))));
		newObj.setAlignmentType(AlignmentEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.ALIGNMENT))));
		newObj.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		newObj.setScore(rset.getInt(Columns.get(ColumnEnumType.SCORE)));
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
		TraitType useMap = (TraitType)map;
		fields.add(QueryFields.getFieldTraitType(useMap.getTraitType()));
		fields.add(QueryFields.getFieldAlignmentType(useMap.getAlignmentType()));
		fields.add(QueryFields.getFieldScore(useMap.getScore()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deleteTraitsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteTraitsByIds(ids, user.getOrganizationId());
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
	public int deleteTraitsByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	public int deleteTraitsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deleteTraitsByIds(ids, group.getOrganizationId());
	}
	
	
	public List<TraitType>  getTraitList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<TraitType> getTraitListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
