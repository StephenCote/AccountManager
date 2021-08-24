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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class CredentialFactory extends NameIdFactory {
	private DatatypeFactory dtFactory = null;
	public CredentialFactory(){
		super();
		this.hasOwnerId = true;
		this.hasParentId=false;
		this.hasUrn = false;
		this.hasName = false;
		this.hasObjectId = true;
		this.aggressiveKeyFlush = false;
		this.useThreadSafeCollections = false;
		this.scopeToOrganization = true;
		this.isVaulted = true;
		this.primaryTableName = "credential";
		this.tableNames.add(primaryTableName);

		factoryType = FactoryEnumType.CREDENTIAL;
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		CredentialType cit = (CredentialType)map;
		if(cit.getReferenceId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getReferenceId());
			if(tmpId.compareTo(0L) > 0) cit.setReferenceId(tmpId);
		}
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		CredentialType t = (CredentialType)obj;
		return t.getCredentialType().toString() + "-" + t.getReferenceType().toString() + "-" + t.getReferenceId();
	}
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// Restrict any columns
		}
	}
	
	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		CredentialType obj = (CredentialType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}

	public CredentialType newCredential(UserType owner, NameIdType targetObject) throws ArgumentException
	{
		if (owner == null || owner.getId().compareTo(0L)==0) throw new ArgumentException("Invalid owner");
		if(targetObject.getId().compareTo(0L) == 0 || targetObject.getNameType() == NameEnumType.UNKNOWN) throw new ArgumentException("Invalid target object");
		
		CredentialType cred = new CredentialType();
		cred.setNameType(NameEnumType.CREDENTIAL);
		cred.setCredentialType(CredentialEnumType.UNKNOWN);
		cred.setOwnerId(owner.getId());
		cred.setReferenceType(FactoryEnumType.valueOf(targetObject.getNameType().toString()));
		cred.setReferenceId(targetObject.getId());
		cred.setOrganizationId((targetObject.getNameType() == NameEnumType.ORGANIZATION ? targetObject.getId() : targetObject.getOrganizationId()));
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
	    
		cred.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		cred.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.YEAR, 1);
		cred.setExpiryDate(dtFactory.newXMLGregorianCalendar(cal));

		
		return cred;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		CredentialType obj = (CredentialType)object;

		DataRow row = prepareAdd(obj, "credential");


		try{
			row.setCellValue(Columns.get(ColumnEnumType.HASHPROVIDER),obj.getHashProvider());
			row.setCellValue(Columns.get(ColumnEnumType.REFERENCETYPE),obj.getReferenceType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.REFERENCEID),obj.getReferenceId());
			row.setCellValue(Columns.get(ColumnEnumType.PREVIOUSCREDENTIALID),obj.getPreviousCredentialId());
			row.setCellValue(Columns.get(ColumnEnumType.NEXTCREDENTIALID),obj.getNextCredentialId());
			row.setCellValue(Columns.get(ColumnEnumType.CREATEDDATE), obj.getCreatedDate());
			row.setCellValue(Columns.get(ColumnEnumType.MODIFIEDDATE), obj.getModifiedDate());
			row.setCellValue(Columns.get(ColumnEnumType.EXPIRATIONDATE), obj.getExpiryDate());
			row.setCellValue(Columns.get(ColumnEnumType.CREDENTIAL), obj.getCredential());
			row.setCellValue(Columns.get(ColumnEnumType.SALT), obj.getSalt());
			row.setCellValue(Columns.get(ColumnEnumType.CREDENTIALTYPE),obj.getCredentialType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.PRIMARYCREDENTIAL), obj.getPrimary());
			
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
		CredentialType newCred = new CredentialType();
		newCred.setNameType(NameEnumType.CREDENTIAL);
		super.read(rset, newCred);
		
		newCred.setHashProvider(rset.getString(Columns.get(ColumnEnumType.HASHPROVIDER)));
		newCred.setNextCredentialId(rset.getLong(Columns.get(ColumnEnumType.NEXTCREDENTIALID)));
		newCred.setPreviousCredentialId(rset.getLong(Columns.get(ColumnEnumType.PREVIOUSCREDENTIALID)));
		newCred.setReferenceId(rset.getLong(Columns.get(ColumnEnumType.REFERENCEID)));
		newCred.setReferenceType(FactoryEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.REFERENCETYPE))));
		newCred.setCredentialType(CredentialEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.CREDENTIALTYPE))));
		newCred.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.CREATEDDATE))));
		newCred.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.MODIFIEDDATE))));
		newCred.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.EXPIRATIONDATE))));
		newCred.setSalt(rset.getBytes(Columns.get(ColumnEnumType.SALT)));
		newCred.setCredential(rset.getBytes(Columns.get(ColumnEnumType.CREDENTIAL)));
		newCred.setPrimary(rset.getBoolean(Columns.get(ColumnEnumType.PRIMARYCREDENTIAL)));
		
		return newCred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		CredentialType useMap = (CredentialType)map;
		fields.add(QueryFields.getFieldHashProvider(useMap.getHashProvider()));
		fields.add(QueryFields.getFieldPrimaryCredential(useMap.getPrimary()));
		fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(useMap.getExpiryDate()));
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));
		fields.add(QueryFields.getFieldPreviousCredentialId(useMap.getPreviousCredentialId()));
		fields.add(QueryFields.getFieldNextCredentialId(useMap.getNextCredentialId()));
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(useMap.getReferenceType()));
		fields.add(QueryFields.getFieldCredentialType(useMap.getCredentialType()));

		fields.add(QueryFields.getFieldSalt(useMap.getSalt()));
		fields.add(QueryFields.getFieldCredential(useMap.getCredential()));
	}

	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		CredentialType data = (CredentialType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	public List<CredentialType> getCredentialsForType(NameIdType obj) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		if(obj == null || obj.getNameType() == NameEnumType.UNKNOWN || obj.getId().compareTo(0L) == 0) throw new ArgumentException("Invalid object reference");
		fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		fields.add(QueryFields.getFieldReferenceId(obj.getId()));
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(0L);
		pi.setRecordCount(2);
		
		return list(fields.toArray(new QueryField[0]), pi, obj.getOrganizationId());
	}
	
	public CredentialType getActivePrimaryCredential(NameIdType obj,CredentialEnumType credType) throws FactoryException, ArgumentException{
		return getPrimaryCredential(obj, credType, true);
	}
	
	public CredentialType getPrimaryCredential(NameIdType obj,CredentialEnumType credType,boolean requireActive) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		fields.add(QueryFields.getFieldReferenceId(obj.getId()));
		fields.add(QueryFields.getFieldPrimaryCredential(true));
		
		if(requireActive){
			QueryField expiry = QueryFields.getFieldExpirationDate(CalendarUtil.getXmlGregorianCalendar(new Date()));
			expiry.setComparator(ComparatorEnumType.GREATER_THAN);
			fields.add(expiry);
		}
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setOrderClause(Columns.get(ColumnEnumType.CREATEDDATE) + " DESC");
		pi.setPaginate(true);
		pi.setStartIndex(0L);
		pi.setRecordCount(1);
		if(credType != CredentialEnumType.UNKNOWN) fields.add(QueryFields.getFieldCredentialType(credType));
		CredentialType outsec = null;

		List<NameIdType> recs = this.getByField(fields.toArray(new QueryField[0]), obj.getOrganizationId());
		if(!recs.isEmpty()) outsec = (CredentialType)recs.get(0);
		return outsec;
	}
	
}
