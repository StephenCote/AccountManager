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
/*
 * 
 * TODO: These are all cookie-cutter factory files, more or less, that use my particular (preferred) style for handling bulk operations
 * BUT - these could also be generated instead of copy/pasted/tweaked, and could be refactored into a parent abstract class
 */

package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;



public class PatternFactory extends NameIdGroupFactory {
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.PATTERN, PatternFactory.class); }
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
		if(pattern.getFactUrn() != null) pattern.setFact((FactType)Factories.getNameIdFactory(FactoryEnumType.FACT).getByUrn(pattern.getFactUrn()));
		if(pattern.getMatchUrn() != null) pattern.setMatch((FactType)Factories.getNameIdFactory(FactoryEnumType.FACT).getByUrn(pattern.getMatchUrn()));
		if(pattern.getOperationUrn() != null) pattern.setOperation((OperationType)Factories.getNameIdFactory(FactoryEnumType.OPERATION).getByUrn(pattern.getOperationUrn()));
		pattern.setPopulated(true);
		updateToCache(pattern);
	}
	
	
	public PatternType newPattern(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		PatternType obj = new PatternType();
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setPatternType(PatternEnumType.UNKNOWN);
		obj.setComparator(ComparatorEnumType.UNKNOWN);
		obj.setNameType(NameEnumType.PATTERN);
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		PatternType obj = (PatternType)object;
		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Fact without a group");

		DataRow row = prepareAdd(obj, "pattern");
		try{
			row.setCellValue("patterntype", obj.getPatternType().toString());
			row.setCellValue("groupid", obj.getGroupId());
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
		PatternType newObj = new PatternType();
		newObj.setNameType(NameEnumType.PATTERN);
		super.read(rset, newObj);
		readGroup(rset, newObj);
		newObj.setPatternType(PatternEnumType.valueOf(rset.getString("patterntype")));
		//newObj.setUrn(rset.getString("urn"));
		newObj.setScore(rset.getInt("score"));
		newObj.setFactUrn(rset.getString("facturn"));
		newObj.setOperationUrn(rset.getString("operationurn"));
		newObj.setMatchUrn(rset.getString("matchurn"));
		newObj.setComparator(ComparatorEnumType.valueOf(rset.getString("comparator")));
		newObj.setDescription(rset.getString("description"));
		newObj.setLogicalOrder(rset.getInt("logicalorder"));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		PatternType data = (PatternType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		PatternType useMap = (PatternType)map;
		//fields.add(QueryFields.getFieldUrn(useMap.getUrn()));
		fields.add(QueryFields.getFieldScore(useMap.getScore()));
		fields.add(QueryFields.getFieldFactUrn(useMap.getFactUrn()));
		fields.add(QueryFields.getFieldMatchUrn(useMap.getMatchUrn()));
		fields.add(QueryFields.getFieldOperationUrn(useMap.getOperationUrn()));
		fields.add(QueryFields.getFieldComparatorType(useMap.getComparator()));
		fields.add(QueryFields.getFieldLogicalOrder(useMap.getLogicalOrder()));
		fields.add(QueryFields.getFieldPatternType(useMap.getPatternType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));
	}
	public int deletePatternsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deletePatternsByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		PatternType obj = (PatternType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deletePatternsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			/*
			Factories.getPatternParticipationFactory().deleteParticipations(ids, organizationId);
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deletePatternsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deletePatternsByIds(ids, group.getOrganizationId());
	}
	public List<FactType> getPatterns(QueryField[] matches, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> lst = getByField(matches, organizationId);
		return convertList(lst);

	}
	
	public List<PatternType>  getPatternList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<PatternType> getPatternListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
