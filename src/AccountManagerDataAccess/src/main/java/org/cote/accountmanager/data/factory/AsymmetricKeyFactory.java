package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class AsymmetricKeyFactory extends NameIdFactory {
	public AsymmetricKeyFactory(){
		super();
		this.hasOwnerId = true;
		this.hasParentId = false;
		this.hasUrn = false;
		this.hasName = false;
		this.hasObjectId = true;
		this.aggressiveKeyFlush = false;
		this.useThreadSafeCollections = false;
		this.scopeToOrganization = true;

		this.tableNames.add("asymmetrickeys");

		factoryType = FactoryEnumType.ASYMMETRICKEY;
	}

	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("asymmetrickeys")){
			table.setRestrictUpdateColumn("cipherprovider",true);
			table.setRestrictUpdateColumn("cipherkeyspec",true);
			table.setRestrictUpdateColumn("asymmetriccipherkeyspec",true);
			table.setRestrictUpdateColumn("hashprovider",true);
			table.setRestrictUpdateColumn("seedlength",true);
			table.setRestrictUpdateColumn("publickey",true);
			table.setRestrictUpdateColumn("privatekey",true);
			table.setRestrictUpdateColumn("ownerid",true);
			table.setRestrictUpdateColumn("organizationkey",true);
			table.setRestrictUpdateColumn("globalkey",true);

		}
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		SecurityType t = (SecurityType)obj;
		return t.getObjectId() + "-" + t.getId();
	}

	public boolean deleteAsymmetricKey(SecurityType obj) throws FactoryException
	{
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	
	public boolean addAsymmetricKey(SecurityType obj) throws FactoryException
	{

		DataRow row = prepareAdd(obj, "asymmetrickeys");


		try{

			row.setCellValue("globalkey",obj.getGlobalKey());
			row.setCellValue("primarykey",obj.getPrimaryKey());
			row.setCellValue("organizationkey",obj.getOrganizationKey());
			row.setCellValue("cipherprovider",obj.getCipherProvider());
			row.setCellValue("cipherkeyspec",obj.getCipherKeySpec());
			row.setCellValue("asymmetriccipherkeyspec",obj.getAsymmetricCipherKeySpec());
			row.setCellValue("hashprovider",obj.getHashProvider());
			row.setCellValue("seedlength",obj.getRandomSeedLength());
			row.setCellValue("previouskeyid",obj.getPreviousKeyId());
			row.setCellValue("symmetrickeyid",obj.getSymmetricKeyId());
			row.setCellValue("publickey",obj.getPublicKeyBytes());
			row.setCellValue("privatekey",obj.getPrivateKeyBytes());

			
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
		SecurityType new_cred = new SecurityType();
		new_cred.setNameType(NameEnumType.SECURITY);
		super.read(rset, new_cred);
		new_cred.setGlobalKey(rset.getBoolean("globalkey"));
		new_cred.setOrganizationKey(rset.getBoolean("organizationkey"));
		new_cred.setPrimaryKey(rset.getBoolean("primarykey"));
		new_cred.setHashProvider(rset.getString("hashprovider"));
		new_cred.setRandomSeedLength(rset.getLong("seedlength"));
		new_cred.setPreviousKeyId(rset.getLong("previouskeyid"));
		new_cred.setSymmetricKeyId(rset.getLong("symmetrickeyid"));
		new_cred.setCipherProvider(rset.getString("cipherprovider"));
		new_cred.setCipherKeySpec(rset.getString("cipherkeyspec"));
		new_cred.setAsymmetricCipherKeySpec(rset.getString("asymmetriccipherkeyspec"));
		new_cred.setPublicKeyBytes(rset.getBytes("publickey"));
		new_cred.setPrivateKeyBytes(rset.getBytes("privatekey"));
		return new_cred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		SecurityType use_map = (SecurityType)map;

		fields.add(QueryFields.getFieldPrimaryKey(use_map.getPrimaryKey()));
		fields.add(QueryFields.getFieldSymmetricKeyId(use_map.getSymmetricKeyId()));
		fields.add(QueryFields.getFieldPreviousKeyId(use_map.getPreviousKeyId()));
	}
	
	public boolean updateAsymmetricKey(SecurityType data) throws FactoryException, DataAccessException
	{	
		removeFromCache(data);
		return update(data, null);
	}
	public SecurityType getKeyByObjectId(String id, long organizationId) throws FactoryException, ArgumentException{
		List<NameIdType> sec = getByObjectId(id, organizationId);
		if(sec.size() > 0) return (SecurityType)sec.get(0);
		return null;
	}
	public SecurityType getPrimaryPersonalKey(UserType user) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldOrganizationKey(false));
		fields.add(QueryFields.getFieldGlobalKey(false));
		fields.add(QueryFields.getFieldOwner(user.getId()));
		fields.add(QueryFields.getFieldPrimaryKey(true));
		SecurityType outsec = null;
		List<NameIdType> recs = this.getByField(fields.toArray(new QueryField[0]), user.getOrganizationId());
		if(recs.size() > 0) outsec = (SecurityType)recs.get(0);
		return outsec;
	}
	public SecurityType getPrimaryOrganizationKey(long organizationId) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldOrganizationKey(true));
		fields.add(QueryFields.getFieldPrimaryKey(true));
		SecurityType outsec = null;
		List<NameIdType> recs = this.getByField(fields.toArray(new QueryField[0]), organizationId);
		if(recs.size() > 0) outsec = (SecurityType)recs.get(0);
		return outsec;
	}
	public List<SecurityType> listOrganizationKeys(long organizationId) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldOrganizationKey(true));
		return listKeys(fields,0L,0,organizationId);
	}
	public List<SecurityType> listByOwner(UserType user) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldOwner(user.getId()));
		return listKeys(fields,0L,0,user.getOrganizationId());
	}

	private List<SecurityType> listKeys(List<QueryField> fields, long start_record, int record_count, long organizationId) throws FactoryException, ArgumentException{
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(start_record);
		pi.setRecordCount(record_count);
		
		return getList(fields.toArray(new QueryField[0]), pi, organizationId);
	}
	public boolean deleteKeys(long organizationId) throws FactoryException, ArgumentException{
		int del = this.deleteByField(new QueryField[0], organizationId);
		return true;
	}
}
