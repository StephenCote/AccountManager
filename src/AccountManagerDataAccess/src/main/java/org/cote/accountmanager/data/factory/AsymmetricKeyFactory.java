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
		this.primaryTableName = "asymmetrickeys";
		this.tableNames.add(primaryTableName);
		this.isVaulted = true;
		factoryType = FactoryEnumType.ASYMMETRICKEY;
	}

	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.CIPHERPROVIDER),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.CIPHERKEYSPEC),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.ASYMMETRICCIPHERKEYSPEC),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.HASHPROVIDER),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.SEEDLENGTH),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.PUBLICKEY),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.PRIVATEKEY),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.OWNERID),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.ORGANIZATIONKEY),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.GLOBALKEY),true);
			table.setRestrictUpdateColumn(Columns.get(ColumnEnumType.CURVENAME),true);

		}
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		SecurityType t = (SecurityType)obj;
		return t.getObjectId() + "-" + t.getId();
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

		DataRow row = prepareAdd(obj, "asymmetrickeys");


		try{

			row.setCellValue(Columns.get(ColumnEnumType.GLOBALKEY),obj.getGlobalKey());
			row.setCellValue(Columns.get(ColumnEnumType.PRIMARYKEY),obj.getPrimaryKey());
			row.setCellValue(Columns.get(ColumnEnumType.ORGANIZATIONKEY),obj.getOrganizationKey());
			row.setCellValue(Columns.get(ColumnEnumType.CIPHERPROVIDER),obj.getCipherProvider());
			row.setCellValue(Columns.get(ColumnEnumType.CIPHERKEYSPEC),obj.getCipherKeySpec());
			row.setCellValue(Columns.get(ColumnEnumType.ASYMMETRICCIPHERKEYSPEC),obj.getAsymmetricCipherKeySpec());
			row.setCellValue(Columns.get(ColumnEnumType.HASHPROVIDER),obj.getHashProvider());
			row.setCellValue(Columns.get(ColumnEnumType.SEEDLENGTH),obj.getRandomSeedLength());
			row.setCellValue(Columns.get(ColumnEnumType.PREVIOUSKEYID),obj.getPreviousKeyId());
			row.setCellValue(Columns.get(ColumnEnumType.SYMMETRICKEYID),obj.getSymmetricKeyId());
			row.setCellValue(Columns.get(ColumnEnumType.PUBLICKEY),obj.getPublicKeyBytes());
			row.setCellValue(Columns.get(ColumnEnumType.PRIVATEKEY),obj.getPrivateKeyBytes());
			row.setCellValue(Columns.get(ColumnEnumType.CURVENAME),obj.getCurveName());

			
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
		newCred.setOrganizationKey(rset.getBoolean(Columns.get(ColumnEnumType.ORGANIZATIONKEY)));
		newCred.setPrimaryKey(rset.getBoolean(Columns.get(ColumnEnumType.PRIMARYKEY)));
		newCred.setHashProvider(rset.getString(Columns.get(ColumnEnumType.HASHPROVIDER)));
		newCred.setRandomSeedLength(rset.getLong(Columns.get(ColumnEnumType.SEEDLENGTH)));
		newCred.setPreviousKeyId(rset.getLong(Columns.get(ColumnEnumType.PREVIOUSKEYID)));
		newCred.setSymmetricKeyId(rset.getLong(Columns.get(ColumnEnumType.SYMMETRICKEYID)));
		newCred.setCipherProvider(rset.getString(Columns.get(ColumnEnumType.CIPHERPROVIDER)));
		newCred.setCipherKeySpec(rset.getString(Columns.get(ColumnEnumType.CIPHERKEYSPEC)));
		newCred.setAsymmetricCipherKeySpec(rset.getString(Columns.get(ColumnEnumType.ASYMMETRICCIPHERKEYSPEC)));
		newCred.setPublicKeyBytes(rset.getBytes(Columns.get(ColumnEnumType.PUBLICKEY)));
		newCred.setPrivateKeyBytes(rset.getBytes(Columns.get(ColumnEnumType.PRIVATEKEY)));
		newCred.setCurveName(rset.getString(Columns.get(ColumnEnumType.CURVENAME)));
		return newCred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		SecurityType useMap = (SecurityType)map;

		fields.add(QueryFields.getFieldPrimaryKey(useMap.getPrimaryKey()));
		fields.add(QueryFields.getFieldSymmetricKeyId(useMap.getSymmetricKeyId()));
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
		if(!recs.isEmpty()) outsec = (SecurityType)recs.get(0);
		return outsec;
	}
	public SecurityType getPrimaryOrganizationKey(long organizationId) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldOrganizationKey(true));
		fields.add(QueryFields.getFieldPrimaryKey(true));
		SecurityType outsec = null;
		List<NameIdType> recs = this.getByField(fields.toArray(new QueryField[0]), organizationId);
		if(!recs.isEmpty()) outsec = (SecurityType)recs.get(0);
		return outsec;
	}
	public List<SecurityType> listOrganizationKeys(long organizationId) throws FactoryException, ArgumentException{
		return list(new QueryField[]{QueryFields.getFieldOrganizationKey(true)}, organizationId);
	}

}
