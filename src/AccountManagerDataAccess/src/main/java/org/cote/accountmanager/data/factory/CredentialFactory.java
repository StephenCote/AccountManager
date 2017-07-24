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
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;


public class CredentialFactory extends NameIdFactory {
	private DatatypeFactory dtFactory = null;
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.CREDENTIAL, CredentialFactory.class); }
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

		this.tableNames.add("credential");

		factoryType = FactoryEnumType.CREDENTIAL;
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error("Error",e);
		}
	}
	
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		CredentialType cit = (CredentialType)map;
		Long tmpId = 0L;
		if(cit.getReferenceId().compareTo(0L) < 0){
			tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getReferenceId());
			if(tmpId.compareTo(0L) > 0) cit.setReferenceId(tmpId.longValue());
		}
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		CredentialType t = (CredentialType)obj;
		return t.getCredentialType().toString() + "-" + t.getReferenceType().toString() + "-" + t.getReferenceId();
	}
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("credential")){
			/// table.setRestrictSelectColumn("logicalid", true);
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
		cred.setOrganizationId(targetObject.getOrganizationId());
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
	    //cal.add(GregorianCalendar.YEAR, 1);
	    
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
			row.setCellValue("hashprovider",obj.getHashProvider());
			row.setCellValue("referencetype",obj.getReferenceType().toString());
			row.setCellValue("referenceid",obj.getReferenceId());
			row.setCellValue("previouscredentialid",obj.getPreviousCredentialId());
			row.setCellValue("nextcredentialid",obj.getNextCredentialId());
			row.setCellValue("vaultid",obj.getVaultId());
			row.setCellValue("keyid", obj.getKeyId());
			row.setCellValue("isvaulted", obj.getVaulted());
			row.setCellValue("isenciphered", obj.getEnciphered());
			row.setCellValue("createddate", obj.getCreatedDate());
			row.setCellValue("modifieddate", obj.getModifiedDate());
			row.setCellValue("expirationdate", obj.getExpiryDate());
			row.setCellValue("credential", obj.getCredential());
			row.setCellValue("salt", obj.getSalt());
			row.setCellValue("credentialtype",obj.getCredentialType().toString());
			row.setCellValue("primarycredential", obj.getPrimary());
			
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
		CredentialType new_cred = new CredentialType();
		new_cred.setNameType(NameEnumType.CREDENTIAL);
		super.read(rset, new_cred);
		
		new_cred.setHashProvider(rset.getString("hashprovider"));
		new_cred.setNextCredentialId(rset.getLong("nextcredentialid"));
		new_cred.setPreviousCredentialId(rset.getLong("previouscredentialid"));
		new_cred.setReferenceId(rset.getLong("referenceid"));
		new_cred.setReferenceType(FactoryEnumType.fromValue(rset.getString("referencetype")));
		new_cred.setCredentialType(CredentialEnumType.fromValue(rset.getString("credentialtype")));
		new_cred.setVaultId(rset.getString("vaultid"));
		new_cred.setKeyId(rset.getString("keyid"));
		new_cred.setVaulted(rset.getBoolean("isvaulted"));
		new_cred.setEnciphered(rset.getBoolean("isenciphered"));
		new_cred.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		new_cred.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		new_cred.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));
		new_cred.setSalt(rset.getBytes("salt"));
		new_cred.setCredential(rset.getBytes("credential"));
		
		return new_cred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		CredentialType use_map = (CredentialType)map;
		fields.add(QueryFields.getFieldHashProvider(use_map.getHashProvider()));
		fields.add(QueryFields.getFieldPrimaryCredential(use_map.getPrimary()));
		fields.add(QueryFields.getFieldModifiedDate(use_map.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(use_map.getExpiryDate()));
		fields.add(QueryFields.getFieldCreatedDate(use_map.getCreatedDate()));
		fields.add(QueryFields.getFieldPreviousCredentialId(use_map.getPreviousCredentialId()));
		fields.add(QueryFields.getFieldNextCredentialId(use_map.getNextCredentialId()));
		fields.add(QueryFields.getFieldReferenceId(use_map.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(use_map.getReferenceType()));
		fields.add(QueryFields.getFieldCredentialType(use_map.getCredentialType()));
		fields.add(QueryFields.getFieldKeyId(use_map.getKeyId()));
		fields.add(QueryFields.getFieldVaultId(use_map.getVaultId()));
		fields.add(QueryFields.getFieldVaulted(use_map.getVaulted()));
		fields.add(QueryFields.getFieldEnciphered(use_map.getEnciphered()));

		fields.add(QueryFields.getFieldSalt(use_map.getSalt()));
		fields.add(QueryFields.getFieldCredential(use_map.getCredential()));
	}
	/*
	public CredentialType getCredentialByObjectId(String id, long organizationId) throws FactoryException, ArgumentException{
		
		List<NameIdType> sec = getByObjectId(id, organizationId);
		if(sec.size() > 0) return (CredentialType)sec.get(0);
		return null;
	}
	*/
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		CredentialType data = (CredentialType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	public List<CredentialType> getCredentialsForType(NameIdType obj) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
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
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		fields.add(QueryFields.getFieldReferenceId(obj.getId()));
		fields.add(QueryFields.getFieldPrimaryCredential(true));
		
		if(requireActive){
			QueryField expiry = QueryFields.getFieldExpirationDate(CalendarUtil.getXmlGregorianCalendar(new Date()));
			expiry.setComparator(ComparatorEnumType.GREATER_THAN);
			fields.add(expiry);
		}
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setOrderClause("createddate DESC");
		pi.setPaginate(true);
		pi.setStartIndex(0L);
		pi.setRecordCount(1);
		if(credType != CredentialEnumType.UNKNOWN) fields.add(QueryFields.getFieldCredentialType(credType));
		CredentialType outsec = null;
		List<NameIdType> recs = this.getByField(fields.toArray(new QueryField[0]), obj.getOrganizationId());
		if(recs.size() > 0) outsec = (CredentialType)recs.get(0);
		return outsec;
	}
	
}
