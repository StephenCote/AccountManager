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
import java.util.List;

import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
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
		this.isVaulted = true;
		this.primaryTableName = "symmetrickeys";
		this.tableNames.add(primaryTableName);

		factoryType = FactoryEnumType.SYMMETRICKEY;
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		SecurityType t = (SecurityType)obj;
		return t.getObjectId() + "-" + t.getId();
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.CIPHERPROVIDER),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.CIPHERKEYSPEC),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.SYMMETRICCIPHERKEYSPEC),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.HASHPROVIDER),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.SEEDLENGTH),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.CIPHERKEY),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.CIPHERIV),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.OWNERID),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.ORGANIZATIONKEY),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.GLOBALKEY),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.ENCRYPTEDKEY), true);

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

			row.setCellValue(Columns.get(ColumnEnumType.GLOBALKEY),obj.getGlobalKey());
			row.setCellValue(Columns.get(ColumnEnumType.PRIMARYKEY),obj.getPrimaryKey());
			row.setCellValue(Columns.get(ColumnEnumType.ORGANIZATIONKEY),obj.getOrganizationKey());
			row.setCellValue(Columns.get(ColumnEnumType.CIPHERPROVIDER),obj.getCipherProvider());
			row.setCellValue(Columns.get(ColumnEnumType.CIPHERKEYSPEC),obj.getCipherKeySpec());
			row.setCellValue(Columns.get(ColumnEnumType.SYMMETRICCIPHERKEYSPEC),obj.getSymmetricCipherKeySpec());
			row.setCellValue(Columns.get(ColumnEnumType.HASHPROVIDER),obj.getHashProvider());
			row.setCellValue(Columns.get(ColumnEnumType.SEEDLENGTH),obj.getRandomSeedLength());
			row.setCellValue(Columns.get(ColumnEnumType.PREVIOUSKEYID),obj.getPreviousKeyId());
			row.setCellValue(Columns.get(ColumnEnumType.ASYMMETRICKEYID),obj.getAsymmetricKeyId());
			if(obj.getEncryptCipherKey()){
				row.setCellValue(Columns.get(ColumnEnumType.CIPHERKEY),obj.getEncryptedCipherKey());
				row.setCellValue(Columns.get(ColumnEnumType.CIPHERIV),obj.getEncryptedCipherIV());				
			}
			else{
				row.setCellValue(Columns.get(ColumnEnumType.CIPHERKEY),obj.getCipherKey());
				row.setCellValue(Columns.get(ColumnEnumType.CIPHERIV),obj.getCipherIV());
			}
			row.setCellValue(Columns.get(ColumnEnumType.ENCRYPTEDKEY),obj.getEncryptCipherKey());

			
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
		SecurityType newCred = new SecurityType();
		newCred.setNameType(NameEnumType.SECURITY);
		super.read(rset, newCred);
		
		newCred.setGlobalKey(rset.getBoolean(Columns.get(ColumnEnumType.GLOBALKEY)));
		newCred.setEncryptCipherKey(rset.getBoolean(Columns.get(ColumnEnumType.ENCRYPTEDKEY)));
		newCred.setOrganizationKey(rset.getBoolean(Columns.get(ColumnEnumType.ORGANIZATIONKEY)));
		newCred.setPrimaryKey(rset.getBoolean(Columns.get(ColumnEnumType.PRIMARYKEY)));
		newCred.setHashProvider(rset.getString(Columns.get(ColumnEnumType.HASHPROVIDER)));
		newCred.setRandomSeedLength(rset.getLong(Columns.get(ColumnEnumType.SEEDLENGTH)));
		newCred.setPreviousKeyId(rset.getLong(Columns.get(ColumnEnumType.PREVIOUSKEYID)));
		newCred.setAsymmetricKeyId(rset.getLong(Columns.get(ColumnEnumType.ASYMMETRICKEYID)));
		newCred.setCipherProvider(rset.getString(Columns.get(ColumnEnumType.CIPHERPROVIDER)));
		newCred.setCipherKeySpec(rset.getString(Columns.get(ColumnEnumType.CIPHERKEYSPEC)));
		newCred.setSymmetricCipherKeySpec(rset.getString(Columns.get(ColumnEnumType.SYMMETRICCIPHERKEYSPEC)));
		if(newCred.getEncryptCipherKey()){
			newCred.setEncryptedCipherIV(rset.getBytes(Columns.get(ColumnEnumType.CIPHERIV)));
			newCred.setEncryptedCipherKey(rset.getBytes(Columns.get(ColumnEnumType.CIPHERKEY)));
		}
		else{
			newCred.setCipherKey(rset.getBytes(Columns.get(ColumnEnumType.CIPHERKEY)));
			newCred.setCipherIV(rset.getBytes(Columns.get(ColumnEnumType.CIPHERIV)));
		}

		return newCred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		SecurityType useMap = (SecurityType)map;

		fields.add(QueryFields.getFieldPrimaryKey(useMap.getPrimaryKey()));
		fields.add(QueryFields.getFieldAsymmetricKeyId(useMap.getAsymmetricKeyId()));
		fields.add(QueryFields.getFieldPreviousKeyId(useMap.getPreviousKeyId()));
	}
	
	@Override
	public <T> boolean update(T object) throws FactoryException
	{	
		SecurityType data = (SecurityType)object;
		removeFromCache(data);
		return super.update(data, null);
	}

	public SecurityType getPrimaryPersonalKey(UserType user) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
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
		List<QueryField> fields = new ArrayList<>();
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
