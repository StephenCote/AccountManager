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
package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.StatisticsType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.StatisticsEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class StatisticsFactory extends NameIdFactory {
	
	
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.STATISTICS, StatisticsFactory.class); }
	public StatisticsFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = false;
		this.hasName = false;
		this.tableNames.add("statistics");
		this.factoryType = FactoryEnumType.STATISTICS;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("statistics")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	

	public boolean deleteStatisticsByReferenceType(NameIdType map) throws FactoryException
	{
		int deleted = deleteByBigIntField("referenceid",new long[]{map.getId()},map.getOrganizationId());
		return (deleted > 0);
	}
	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		StatisticsType cinfo = (StatisticsType)object;
		int deleted = deleteById(cinfo.getId(), cinfo.getOrganizationId());
		return (deleted > 0);
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		StatisticsType cinfo = (StatisticsType)object;
		return super.update(cinfo);
	}

	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		StatisticsType new_info = (StatisticsType)object;
		if (new_info.getReferenceId().compareTo(0L) == 0) throw new FactoryException("Cannot add statistics without a corresponding reference id");
		if (new_info.getOrganizationId() <= 0L) throw new FactoryException("Cannot add statistics to invalid organization");

		DataRow row = prepareAdd(new_info, "statistics");
		try{
			row.setCellValue("referenceid",new_info.getReferenceId());
			row.setCellValue("createddate",new_info.getCreatedDate());
			row.setCellValue("modifieddate",new_info.getModifiedDate());
			row.setCellValue("accesseddate",new_info.getAccessedDate());
			row.setCellValue("expirationdate",new_info.getExpirationDate());
			row.setCellValue("statisticstype", new_info.getStatisticsType().toString());
			if (insertRow(row)) return true;
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	

	public StatisticsType getStatistics(UserType map) throws FactoryException, ArgumentException
	{
		return getStatisticsByReferenceId(map.getId(), StatisticsEnumType.USER, map.getOrganizationId());
	}
	public StatisticsType getStatistics(AccountType map) throws FactoryException, ArgumentException
	{
		return getStatisticsByReferenceId(map.getId(), StatisticsEnumType.ACCOUNT, map.getOrganizationId());
	}
	public StatisticsType getStatistics(DataType map) throws FactoryException, ArgumentException
	{
		return getStatisticsByReferenceId(map.getId(), StatisticsEnumType.DATA, map.getOrganizationId());
	}
	public StatisticsType getStatisticsByReferenceId(long reference_id, StatisticsEnumType type, long organizationId) throws FactoryException, ArgumentException
	{
		List<NameIdType> cinfo = getByField(new QueryField[]{QueryFields.getFieldReferenceId(reference_id),QueryFields.getFieldStatisticsType(type)},organizationId);
		if (cinfo.size() > 0) return (StatisticsType)cinfo.get(0);
		return null;
	}

	
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		StatisticsType useMap = (StatisticsType)map;
		fields.add(QueryFields.getFieldAccessedDate(useMap.getAccessedDate()));
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));
		fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(useMap.getExpirationDate()));
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));
		fields.add(QueryFields.getFieldStatisticsType(useMap.getStatisticsType()));
		
	}
	public StatisticsType newStatistics(AccountType map)
	{
		StatisticsType stats = newStatistics(map,StatisticsEnumType.ACCOUNT);
		stats.setReferenceId(map.getId());
		return stats;
	}	
	public StatisticsType newStatistics(UserType map)
	{
		StatisticsType stats = newStatistics(map,StatisticsEnumType.USER);
		stats.setReferenceId(map.getId());
		return stats;
	}
	public StatisticsType newStatistics(DataType map)
	{
		StatisticsType stats = newStatistics(map,StatisticsEnumType.DATA);
		stats.setReferenceId(map.getId());
		return stats;
	}
	protected StatisticsType newStatistics(NameIdType map, StatisticsEnumType type)
	{
		StatisticsType cinfo = new StatisticsType();

		cinfo.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(new Date()));
		cinfo.setAccessedDate(cinfo.getCreatedDate());
		cinfo.setModifiedDate(cinfo.getCreatedDate());
		cinfo.setExpirationDate(cinfo.getCreatedDate());
		//cinfo.setOrganization(map.getOrganizationId());
		cinfo.setOrganizationId(map.getOrganizationId());
		cinfo.setStatisticsType(type);
		return cinfo;
	}
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		StatisticsType cinfo = new StatisticsType();
		cinfo.setStatisticsType(StatisticsEnumType.valueOf(rset.getString("statisticstype")));
		cinfo.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		cinfo.setAccessedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("accesseddate")));
		cinfo.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		cinfo.setExpirationDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));
		cinfo.setReferenceId(rset.getLong("referenceid"));
		return super.read(rset, cinfo);
	}


}
