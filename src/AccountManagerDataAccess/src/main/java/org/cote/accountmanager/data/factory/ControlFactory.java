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
package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class ControlFactory extends NameIdFactory {
	private DatatypeFactory dtFactory = null;

	public ControlFactory(){
		super();
		this.hasOwnerId = true;
		this.hasParentId=false;
		this.hasUrn = false;
		this.hasName = false;
		this.hasObjectId = true;
		this.aggressiveKeyFlush = false;
		this.useThreadSafeCollections = false;
		this.scopeToOrganization = true;
		this.primaryTableName = "control";
		this.tableNames.add(primaryTableName);

		factoryType = FactoryEnumType.CONTROL;
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		ControlType cit = (ControlType)map;
		if(cit.getReferenceId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getReferenceId());
			if(tmpId.compareTo(0L) > 0) cit.setReferenceId(tmpId);
		}
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		ControlType t = (ControlType)obj;
		return t.getControlType().toString() + "-" + t.getControlAction().toString() + "-" + t.getReferenceType().toString() + "-" + t.getReferenceId();
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){

		}
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		ControlType obj = (ControlType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	
	public ControlType newControl(UserType owner, NameIdType targetObject) throws ArgumentException
	{
		if (owner == null || owner.getId().compareTo(0L)==0) throw new ArgumentException("Invalid owner");
		if(targetObject.getNameType() == NameEnumType.UNKNOWN) throw new ArgumentException("Invalid target object");
		/// It's ok if targetObject has no ID - this will be used to indicate its organization level
		/// This is needed for create operations of a given type
		///
		ControlType cred = new ControlType();
		cred.setNameType(NameEnumType.CREDENTIAL);
		cred.setControlType(ControlEnumType.UNKNOWN);
		cred.setControlAction(ControlActionEnumType.UNKNOWN);
		cred.setOrganizationId(owner.getOrganizationId());
		cred.setOwnerId(owner.getId());
		cred.setReferenceType(FactoryEnumType.valueOf(targetObject.getNameType().toString()));
		cred.setReferenceId(targetObject.getId());
		
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		cred.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		cred.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.YEAR, 5);
		cred.setExpiryDate(dtFactory.newXMLGregorianCalendar(cal));
		
		return cred;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		ControlType obj = (ControlType)object;
		DataRow row = prepareAdd(obj, "control");
		try{

			row.setCellValue("controlid",obj.getControlId());
			row.setCellValue("referencetype",obj.getReferenceType().toString());
			row.setCellValue("referenceid",obj.getReferenceId());
			row.setCellValue("createddate", obj.getCreatedDate());
			row.setCellValue("modifieddate", obj.getModifiedDate());
			row.setCellValue("expirationdate", obj.getExpiryDate());
			row.setCellValue("controltype",obj.getControlType().toString());
			row.setCellValue("controlaction",obj.getControlAction().toString());

			
			if(insertRow(row)) return true;
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} 
		return false;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		ControlType newCred = new ControlType();
		newCred.setNameType(NameEnumType.CREDENTIAL);
		super.read(rset, newCred);
		
		newCred.setReferenceId(rset.getLong("referenceid"));
		newCred.setReferenceType(FactoryEnumType.fromValue(rset.getString("referencetype")));
		newCred.setControlId(rset.getLong("controlid"));
		newCred.setControlType(ControlEnumType.fromValue(rset.getString("controltype")));
		newCred.setControlAction(ControlActionEnumType.fromValue(rset.getString("controlaction")));

		newCred.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		newCred.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		newCred.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));

		
		return newCred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ControlType useMap = (ControlType)map;
		fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(useMap.getExpiryDate()));
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(useMap.getReferenceType()));
		fields.add(QueryFields.getFieldControlId(useMap.getControlId()));
		fields.add(QueryFields.getFieldControlType(useMap.getControlType()));
		fields.add(QueryFields.getFieldControlAction(useMap.getControlAction()));
	}
	public ControlType getControlByObjectId(String id, long organizationId) throws FactoryException, ArgumentException{
		List<NameIdType> sec = getByObjectId(id, organizationId);
		if(!sec.isEmpty()) return (ControlType)sec.get(0);
		return null;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ControlType data = (ControlType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	public List<ControlType> getControlsForType(NameIdType obj,boolean includeGlobal,boolean onlyGlobal) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		// allow id of 0 for global control checks
		if(obj == null || obj.getNameType() == NameEnumType.UNKNOWN || obj.getOrganizationId() == null) throw new ArgumentException("Invalid object reference");
		fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		
		QueryField referenceIdFilters = new QueryField(SqlDataEnumType.NULL,"referenceid",null);
		referenceIdFilters.setComparator(ComparatorEnumType.GROUP_OR);
		if(!onlyGlobal) referenceIdFilters.getFields().add(QueryFields.getFieldReferenceId(obj.getId()));
		if(includeGlobal || onlyGlobal) referenceIdFilters.getFields().add(QueryFields.getFieldReferenceId(0L));
		
		fields.add(referenceIdFilters);
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(0L);
		pi.setRecordCount(2);
		
		return list(fields.toArray(new QueryField[0]), pi, obj.getOrganizationId());
	}
	public boolean deleteControlsForType(NameIdType obj) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		// allow id of 0 for global controls
		if(obj == null || obj.getNameType() == NameEnumType.UNKNOWN || obj.getOrganizationId() == null) throw new ArgumentException("Invalid object reference");
		fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		
		fields.add(QueryFields.getFieldReferenceId(obj.getId()));

		return (this.deleteByField(fields.toArray(new QueryField[0]), obj.getOrganizationId()) > 0);
	}

	
}
