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
		SecurityType newCred = new SecurityType();
		newCred.setNameType(NameEnumType.SECURITY);
		super.read(rset, newCred);
		newCred.setGlobalKey(rset.getBoolean("globalkey"));
		newCred.setOrganizationKey(rset.getBoolean("organizationkey"));
		newCred.setPrimaryKey(rset.getBoolean("primarykey"));
		newCred.setHashProvider(rset.getString("hashprovider"));
		newCred.setRandomSeedLength(rset.getLong("seedlength"));
		newCred.setPreviousKeyId(rset.getLong("previouskeyid"));
		newCred.setSymmetricKeyId(rset.getLong("symmetrickeyid"));
		newCred.setCipherProvider(rset.getString("cipherprovider"));
		newCred.setCipherKeySpec(rset.getString("cipherkeyspec"));
		newCred.setAsymmetricCipherKeySpec(rset.getString("asymmetriccipherkeyspec"));
		newCred.setPublicKeyBytes(rset.getBytes("publickey"));
		newCred.setPrivateKeyBytes(rset.getBytes("privatekey"));
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
