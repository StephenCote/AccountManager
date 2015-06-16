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
/*
 * 
 * TODO: These are all cookie-cutter factory files, more or less, that use my particular (preferred) style for handling bulk operations
 * BUT - these could also be generated instead of copy/pasted/tweaked, and could be refactored into a parent abstract class
 */

package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;



public class PatternFactory extends NameIdGroupFactory {
	
	public PatternFactory(){
		super();
		this.tableNames.add("pattern");
		this.hasObjectId = true;
		this.hasUrn = true;
		factoryType = FactoryEnumType.PATTERN;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("pattern")){
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
		PatternType pattern = (PatternType)obj;
		if(pattern.getPopulated()) return;
		pattern.setFact(null);
		pattern.setMatch(null);
		pattern.setOperation(null);
		if(pattern.getFactUrn() != null) pattern.setFact((FactType)Factories.getFactFactory().getByUrn(pattern.getFactUrn()));
		if(pattern.getMatchUrn() != null) pattern.setMatch((FactType)Factories.getFactFactory().getByUrn(pattern.getMatchUrn()));
		if(pattern.getOperationUrn() != null) pattern.setOperation(Factories.getOperationFactory().getByUrn(pattern.getOperationUrn(), pattern.getOrganization()));
		pattern.setPopulated(true);
		updateToCache(pattern);
	}
	
	
	public PatternType newPattern(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		PatternType obj = new PatternType();
		obj.setOrganization(group.getOrganization());
		obj.setOwnerId(user.getId());
		obj.setGroup(group);
		obj.setPatternType(PatternEnumType.UNKNOWN);
		obj.setComparator(ComparatorEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.PATTERN);
		return obj;
	}
	
	public boolean addPattern(PatternType obj) throws FactoryException
	{
		if (obj.getGroup() == null) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "pattern");
		try{
			row.setCellValue("patterntype", obj.getPatternType().toString());
			row.setCellValue("groupid", obj.getGroup().getId());
			row.setCellValue("description", obj.getDescription());
			//row.setCellValue("urn", obj.getUrn());
			row.setCellValue("score", obj.getScore());
			row.setCellValue("facturn", obj.getFactUrn());
			row.setCellValue("operationurn", obj.getOperationUrn());
			row.setCellValue("matchurn", obj.getMatchUrn());
			row.setCellValue("comparator", obj.getComparator().toString());
			row.setCellValue("logicalorder", obj.getLogicalOrder());
			
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
		PatternType new_obj = new PatternType();
		new_obj.setNameType(NameEnumType.PATTERN);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		new_obj.setPatternType(PatternEnumType.valueOf(rset.getString("patterntype")));
		//new_obj.setUrn(rset.getString("urn"));
		new_obj.setScore(rset.getInt("score"));
		new_obj.setFactUrn(rset.getString("facturn"));
		new_obj.setOperationUrn(rset.getString("operationurn"));
		new_obj.setMatchUrn(rset.getString("matchurn"));
		new_obj.setComparator(ComparatorEnumType.valueOf(rset.getString("comparator")));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setLogicalOrder(rset.getInt("logicalorder"));
		return new_obj;
	}
	public boolean updatePattern(PatternType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		return update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		PatternType use_map = (PatternType)map;
		//fields.add(QueryFields.getFieldUrn(use_map.getUrn()));
		fields.add(QueryFields.getFieldScore(use_map.getScore()));
		fields.add(QueryFields.getFieldFactUrn(use_map.getFactUrn()));
		fields.add(QueryFields.getFieldMatchUrn(use_map.getMatchUrn()));
		fields.add(QueryFields.getFieldOperationUrn(use_map.getOperationUrn()));
		fields.add(QueryFields.getFieldComparatorType(use_map.getComparator()));
		fields.add(QueryFields.getFieldLogicalOrder(use_map.getLogicalOrder()));
		fields.add(QueryFields.getFieldPatternType(use_map.getPatternType()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
	}
	public int deletePatternsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deletePatternsByIds(ids, user.getOrganization());
	}

	public boolean deletePattern(PatternType obj) throws FactoryException
	{
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		return (deleted > 0);
	}
	public int deletePatternsByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			/*
			Factories.getPatternParticipationFactory().deleteParticipations(ids, organization);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deletePatternsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		/// TODO: Delete participations
		///
		return deletePatternsByIds(ids, group.getOrganization());
	}
	public List<FactType> getPatterns(QueryField[] matches, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organization.getId());
		return convertList(lst);

	}
	
	public List<PatternType>  getPatternList(QueryField[] fields, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organization);
	}
	public List<PatternType> getPatternListByIds(long[] ids, OrganizationType organization) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organization);
	}
	
}
