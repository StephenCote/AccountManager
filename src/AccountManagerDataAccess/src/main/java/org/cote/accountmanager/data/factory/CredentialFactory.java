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
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
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

		this.tableNames.add("credential");

		factoryType = FactoryEnumType.CREDENTIAL;
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public boolean deleteCredential(CredentialType obj) throws FactoryException
	{
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		return (deleted > 0);
	}

	public CredentialType newCredential(UserType owner, NameIdType targetObject) throws ArgumentException
	{
		if (owner == null || owner.getId().compareTo(0L)==0) throw new ArgumentException("Invalid owner");
		if(targetObject.getId() == 0L || targetObject.getNameType() == NameEnumType.UNKNOWN) throw new ArgumentException("Invalid target object");
		
		CredentialType cred = new CredentialType();
		cred.setNameType(NameEnumType.CREDENTIAL);
		cred.setCredentialType(CredentialEnumType.UNKNOWN);
		cred.setOwnerId(owner.getId());
		cred.setReferenceType(FactoryEnumType.valueOf(targetObject.getNameType().toString()));
		cred.setReferenceId(targetObject.getId());
		cred.setOrganization(targetObject.getOrganization());
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
	    //cal.add(GregorianCalendar.YEAR, 1);
	    
		cred.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		cred.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.YEAR, 1);
		cred.setExpiryDate(dtFactory.newXMLGregorianCalendar(cal));

		
		return cred;
	}
	
	public boolean addCredential(CredentialType obj) throws FactoryException
	{

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
	public CredentialType getCredentialByObjectId(String id, OrganizationType org) throws FactoryException, ArgumentException{
		List<NameIdType> sec = getByObjectId(id, org.getId());
		if(sec.size() > 0) return (CredentialType)sec.get(0);
		return null;
	}
	public boolean updateCredential(CredentialType data) throws FactoryException, DataAccessException
	{	
		removeFromCache(data);
		return update(data, null);
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
		
		return getList(fields.toArray(new QueryField[0]), pi, obj.getOrganization());
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
		List<NameIdType> recs = this.getByField(fields.toArray(new QueryField[0]), obj.getOrganization().getId());
		if(recs.size() > 0) outsec = (CredentialType)recs.get(0);
		return outsec;
	}
	
}
