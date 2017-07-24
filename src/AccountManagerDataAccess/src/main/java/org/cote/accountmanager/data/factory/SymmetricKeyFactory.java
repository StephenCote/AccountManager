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
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class SymmetricKeyFactory extends NameIdFactory {
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.SYMMETRICKEY, SymmetricKeyFactory.class); }
	public SymmetricKeyFactory(){
		super();
		this.hasOwnerId = true;
		this.hasParentId = false;
		this.hasUrn = false;
		this.hasName = false;
		this.hasObjectId = true;
		this.aggressiveKeyFlush = false;
		this.useThreadSafeCollections = false;
		this.scopeToOrganization = true;

		this.tableNames.add("symmetrickeys");

		factoryType = FactoryEnumType.SYMMETRICKEY;
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		SecurityType t = (SecurityType)obj;
		return t.getObjectId() + "-" + t.getId();
	}
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("control")){
			table.setRestrictUpdateColumn("cipherprovider",true);
			table.setRestrictUpdateColumn("cipherkeyspec",true);
			table.setRestrictUpdateColumn("symmetriccipherkeyspec",true);
			table.setRestrictUpdateColumn("hashprovider",true);
			table.setRestrictUpdateColumn("seedlength",true);
			table.setRestrictUpdateColumn("cipherkey",true);
			table.setRestrictUpdateColumn("cipheriv",true);
			table.setRestrictUpdateColumn("ownerid",true);
			table.setRestrictUpdateColumn("organizationkey",true);
			table.setRestrictUpdateColumn("globalkey",true);
			table.setRestrictUpdateColumn("encryptedkey", true);

		}
	}
	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		SecurityType obj = (SecurityType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}

	@Override
	public <T> boolean add(T object) throws FactoryException
	{
		SecurityType obj = (SecurityType)object;
		DataRow row = prepareAdd(obj, "symmetrickeys");


		try{

			row.setCellValue("globalkey",obj.getGlobalKey());
			row.setCellValue("primarykey",obj.getPrimaryKey());
			row.setCellValue("organizationkey",obj.getOrganizationKey());
			row.setCellValue("cipherprovider",obj.getCipherProvider());
			row.setCellValue("cipherkeyspec",obj.getCipherKeySpec());
			row.setCellValue("symmetriccipherkeyspec",obj.getSymmetricCipherKeySpec());
			row.setCellValue("hashprovider",obj.getHashProvider());
			row.setCellValue("seedlength",obj.getRandomSeedLength());
			row.setCellValue("previouskeyid",obj.getPreviousKeyId());
			row.setCellValue("asymmetrickeyid",obj.getAsymmetricKeyId());
			if(obj.getEncryptCipherKey()){
				row.setCellValue("cipherkey",obj.getEncryptedCipherKey());
				row.setCellValue("cipheriv",obj.getEncryptedCipherIV());				
			}
			else{
				row.setCellValue("cipherkey",obj.getCipherKey());
				row.setCellValue("cipheriv",obj.getCipherIV());
			}
			row.setCellValue("encryptedkey",obj.getEncryptCipherKey());

			
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
		new_cred.setEncryptCipherKey(rset.getBoolean("encryptedkey"));
		new_cred.setOrganizationKey(rset.getBoolean("organizationkey"));
		new_cred.setPrimaryKey(rset.getBoolean("primarykey"));
		new_cred.setHashProvider(rset.getString("hashprovider"));
		new_cred.setRandomSeedLength(rset.getLong("seedlength"));
		new_cred.setPreviousKeyId(rset.getLong("previouskeyid"));
		new_cred.setAsymmetricKeyId(rset.getLong("asymmetrickeyid"));
		new_cred.setCipherProvider(rset.getString("cipherprovider"));
		new_cred.setCipherKeySpec(rset.getString("cipherkeyspec"));
		new_cred.setSymmetricCipherKeySpec(rset.getString("symmetriccipherkeyspec"));
		if(new_cred.getEncryptCipherKey()){
			new_cred.setEncryptedCipherIV(rset.getBytes("cipheriv"));
			new_cred.setEncryptedCipherKey(rset.getBytes("cipherkey"));
		}
		else{
			new_cred.setCipherKey(rset.getBytes("cipherkey"));
			new_cred.setCipherIV(rset.getBytes("cipheriv"));
		}
		//logger.info("Symmetric Key Read: " + JSONUtil.exportObject(new_cred));
		return new_cred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		SecurityType use_map = (SecurityType)map;

		fields.add(QueryFields.getFieldPrimaryKey(use_map.getPrimaryKey()));
		fields.add(QueryFields.getFieldAsymmetricKeyId(use_map.getAsymmetricKeyId()));
		fields.add(QueryFields.getFieldPreviousKeyId(use_map.getPreviousKeyId()));
	}
	
	@Override
	public <T> boolean update(T object) throws FactoryException
	{	
		SecurityType data = (SecurityType)object;
		removeFromCache(data);
		return super.update(data, null);
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
	public SecurityType getPrimaryOrganizationKey(long org) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldOrganizationKey(true));
		fields.add(QueryFields.getFieldPrimaryKey(true));
		SecurityType outsec = null;
		List<NameIdType> recs = this.getByField(fields.toArray(new QueryField[0]), org);
		if(recs.size() > 0) outsec = (SecurityType)recs.get(0);
		return outsec;
	}
	public List<SecurityType> listOrganizationKeys(long org) throws FactoryException, ArgumentException{
		return list(new QueryField[]{QueryFields.getFieldOrganizationKey(true)},org);
	}

/*
	private List<SecurityType> listKeys(List<QueryField> fields, long start_record, int record_count, long org) throws FactoryException, ArgumentException{
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(start_record);
		pi.setRecordCount(record_count);
		
		return list(fields.toArray(new QueryField[0]), pi, org);
	}
	public boolean deleteKeys(long org) throws FactoryException, ArgumentException{
		int del = this.deleteByField(new QueryField[0], org);
		return true;
	}
*/
}
