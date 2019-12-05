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

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.VaultService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.SecurityUtil;


public class AttributeFactory extends NameIdFactory{

	private int maximumInsBatch = 2000;
	private VaultService vaultService = null;
	public AttributeFactory(){
		super();
		this.vaultService = new VaultService();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = false;
		this.hasName = true;
		this.primaryTableName = "attribute";
		this.tableNames.add(primaryTableName);
		this.bulkMode = false;
	}
	public String[] getEncipheredValues(AttributeType attr) throws ArgumentException, FactoryException, UnsupportedEncodingException {
		if(attr.getEnciphered() == false || attr.getKeyId() == null) throw new ArgumentException("Attribute is not enciphered or is missing a key id");
		SecurityBean bean = KeyService.getSymmetricKeyByObjectId(attr.getKeyId(), attr.getOrganizationId());
		return getEncipheredValues(attr, bean);
	}
	public String[] getEncipheredValues(AttributeType attr, SecurityBean bean) throws ArgumentException, FactoryException, UnsupportedEncodingException {
		List<String> vals = new ArrayList<>();
		if(bean == null) throw new FactoryException("Failed to retrieve symmetric key with id " + attr.getKeyId());
		for(String val : attr.getValues()) {
			vals.add(
				new String(
						SecurityUtil.decipher(bean, BinaryUtil.fromBase64(val.getBytes("UTF-8")))
				,"UTF-8")
			);
		}
		return vals.toArray(new String[0]);

	}

	public AttributeType newEncipheredAttribute(NameIdType obj, SecurityBean cipher, String name, String val){
		AttributeType attr = newAttribute(obj);
		attr.setName(name);
		if(setEncipheredAttributeValues(attr, cipher, new String[] {val})) {
			attr.setKeyId(cipher.getObjectId());
			attr.setEnciphered(true);
		}
		return attr;
	}
	public boolean setEncipheredAttributeValues(NameIdType obj, String attrName, String[] values) throws FactoryException, ArgumentException {
		AttributeType attr = this.getAttributeByName(obj, attrName);
		if(attr == null) return false;
		return setEncipheredAttributeValues(attr, values);
	}
	public boolean setEncipheredAttributeValues(AttributeType attr, String[] values) throws FactoryException, ArgumentException {
		if(attr.getKeyId() == null) throw new ArgumentException("Cannot encipher attribute without a key id");
		SecurityBean bean = KeyService.getSymmetricKeyByObjectId(attr.getKeyId(), attr.getOrganizationId());
		if(bean == null) throw new FactoryException("Failed to retrieve symmetric key with id " + attr.getKeyId());
		return setEncipheredAttributeValues(attr, bean, values);
	}
	public boolean setEncipheredAttributeValues(AttributeType attr, SecurityBean cipher, String[] values) {
		attr.getValues().clear();
		boolean outBool = false;
		try {
			for(String val : values) {
				attr.getValues().add(BinaryUtil.toBase64Str(SecurityUtil.encipher(cipher, val.getBytes("UTF-8"))));
			}
			outBool = true;
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		return outBool;
	}
	
	public AttributeType newAttribute(NameIdType obj, String name, String val){
		AttributeType attr = newAttribute(obj);
		attr.setName(name);
		attr.getValues().add(val);
		return attr;
	}
	public AttributeType newAttribute(NameIdType obj){
		AttributeType attr = new AttributeType();
		attr.setDataType(SqlDataEnumType.VARCHAR);
		/// Note: Reference id will be 0L for new objects, and < 0L for bulk objects
		///
		if(obj != null){
			attr.setReferenceId(obj.getId());
			attr.setReferenceType(obj.getNameType());
			attr.setOrganizationId(obj.getOrganizationId());
		}
		return attr;
	}
	public void repopulateAttributes(NameIdType obj){
		obj.setAttributesPopulated(false);
		populateAttributes(obj);
	}
	public void populateAttributes(NameIdType obj){
		if(obj.getAttributesPopulated())
			return;
		try {
			if(!obj.getAttributes().isEmpty()){
				logger.warn("Populating attributes for object " + obj.getName() + " (" + obj.getUrn() + ") which already includes attribute values.  This will wipe out the original attribute set.");
			}
			obj.getAttributes().clear();
			obj.getAttributes().addAll(getAttributes(obj));
			obj.setAttributesPopulated(true);
		} catch (FactoryException e) {

			logger.error(e);
		}
	}
	public boolean hasAttribute(NameIdType obj, String name, String value){
		return hasAttribute(obj.getAttributes(), name, value);
	}
	public boolean hasAttribute(List<AttributeType> attrs, String name, String value){
		boolean outBool = false;
		AttributeType comp = null;
		for(int i = 0; i < attrs.size();i++){
			comp = attrs.get(i);
			if(comp.getName().equals(name)){
				for(int v = 0; v < comp.getValues().size();v++){
					if(comp.getValues().get(v).equals(value)){
						outBool = true;
						break;
					}
				}
			}
		}
		return outBool;
	}
	public String getAttributeValueByName(NameIdType obj, String name){
		return getAttributeValueByName(obj.getAttributes(), name);
	}
	public String getAttributeValueByName(List<AttributeType> attrs, String name){
		AttributeType attr = getAttributeByName(attrs, name);
		if(attr == null || attr.getValues().isEmpty())
			return null;
		if(attr.getEnciphered()) {
			String[] vals = new String[0];
			try {
				vals = getEncipheredValues(attr);
			} catch (UnsupportedEncodingException | ArgumentException | FactoryException e) {
				logger.error(e);
			}
			if(vals.length > 0) return vals[0];
			return null;
		}
		return attr.getValues().get(0);
	}

	public String getVaultAttributeValueByName(VaultBean vault, NameIdType obj, String name){
		return getVaultAttributeValueByName(vault, obj.getAttributes(), name);
	}
	public String getVaultAttributeValueByName(VaultBean vault, List<AttributeType> attrs, String name){
		AttributeType attr = getAttributeByName(attrs, name);
		if(attr == null || attr.getValues().isEmpty())
			return null;
		if(!attr.getVaulted()) return null;
		
		String[] vals = new String[0];
		try {
			vals = vaultService.extractVaultAttributeValues(vault, attr);
		} catch (UnsupportedEncodingException | ArgumentException | FactoryException | DataException e) {
			logger.error(e);
		}
		if(vals.length > 0) return vals[0];
		return null;
	}
	
	public AttributeType getAttributeByName(NameIdType obj, String name){
		return getAttributeByName(obj.getAttributes(), name);
	}
	public AttributeType getAttributeByName(List<AttributeType> attrs, String name){
		AttributeType attr = null;
		for(int i = 0; i < attrs.size();i++){
			if(attrs.get(i).getName().equals(name)){
				attr = attrs.get(i);
				break;
			}
		}
		return attr;
	}
	public boolean updateAttributes(NameIdType obj){
		return updateAttributes(new NameIdType[]{obj});
	}
	public boolean updateAttributes(NameIdType[] obj){
		deleteAttributesForObjects(obj);
		addAttributes(obj);
		return true;
	}
	public boolean addAttributes(NameIdType obj){
		return addAttributes(new NameIdType[]{obj});
	}
	public long[] searchForReferenceId(QueryField[] fields) throws ArgumentException{
		List<Long> ids = new ArrayList<>();

		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(connectionType);
		String queryClause = getQueryClause(null,fields, token);
		if(queryClause == null || queryClause.length() == 0){
			throw new ArgumentException("Invalid query fields");
		}
		String sql = String.format("SELECT referenceid FROM attribute WHERE %s;",queryClause);
		PreparedStatement psa = null;
		ResultSet rset = null;
		try{
			psa = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(fields, psa);
			rset = psa.executeQuery();
			while(rset.next()){
				ids.add(rset.getLong("referenceid"));
			}

		}
		catch (SQLException | FactoryException e) {
			logger.error(e.getMessage());
		}
		finally{
			try{
				if(rset != null) rset.close();
				if(psa != null) psa.close();
				connection.close();
			}
			catch (SQLException e) {
				
				logger.error(e.getMessage());
			}
		}
		return ArrayUtils.toPrimitive(ids.toArray(new Long[0]));
	}
	public boolean addAttributes(NameIdType[] objs){
		boolean outBool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(connectionType);

		String sql = String.format("INSERT INTO attribute (referenceid, referencetype, name, datatype, valueindex, value, isenciphered, keyid, isvaulted, vaultid, organizationid) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);",token,token,token,token,token,token,token,token,token,token,token);
		PreparedStatement psa = null;
		try{
			psa = connection.prepareStatement(sql);
			int aiter = 0;
			for(NameIdType obj : objs){
				for(AttributeType attr : obj.getAttributes()){
					for(int v = 0; v < attr.getValues().size();v++){
						psa.setLong(1, obj.getId());
						psa.setString(2, obj.getNameType().toString());
						psa.setString(3,attr.getName());
						psa.setString(4, attr.getDataType().toString());
						psa.setInt(5, v);
						psa.setString(6,attr.getValues().get(v));
						psa.setBoolean(7,attr.getEnciphered());
						psa.setString(8,attr.getKeyId());
						psa.setBoolean(9,attr.getVaulted());
						psa.setString(10,attr.getVaultId());
						psa.setLong(11,obj.getOrganizationId());
						psa.addBatch();
						if(aiter++ >= maximumInsBatch){
							psa.executeBatch();
							psa.clearBatch();
							aiter=0;
						}
						
					}
				}
			}
			if(aiter > 0){
				psa.executeBatch();
				psa.clearBatch();
			}
			outBool = true;
		}
		catch (SQLException e) {
			
			logger.error(e.getMessage());
			if(e.getNextException() != null) logger.error(e.getNextException().getMessage());
			outBool = false;
		}
		finally{
			try{
				if(psa != null) psa.close();
				if(connection != null) connection.close();
			}
			catch (SQLException e) {
				
				logger.error(e.getMessage());
			}
		}
		return outBool;
	}

	public List<AttributeType> getAttributes(NameIdType obj) throws FactoryException {
		List<AttributeType> attributes = new ArrayList<>();
		List<QueryField> fieldList = new ArrayList<>();
		fieldList.add(QueryFields.getFieldReferenceId(obj.getId()));
		fieldList.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		QueryField[] fields = fieldList.toArray(new QueryField[0]);
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("NAME ASC");

		if(this.dataTables.size() > 1)
			throw new FactoryException("Multiple table select statements not yet supported");
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = this.dataTables.get(0);
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, obj.getOrganizationId());
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			rset = statement.executeQuery();
			
			String lastName = null;
			AttributeType currentAttribute = null;
			int valueIndex = 0;
			while(rset.next()){
				String name = rset.getString("name");
				if(lastName == null || !lastName.equals(name)){
					currentAttribute = new AttributeType();
					valueIndex = 0;
					lastName = name;
					currentAttribute.setName(name);
					currentAttribute.setDataType(SqlDataEnumType.valueOf(rset.getString("datatype")));
					currentAttribute.setReferenceType(NameEnumType.valueOf(rset.getString("referencetype")));
					currentAttribute.setOrganizationId(rset.getLong("organizationid"));
					currentAttribute.setReferenceId(rset.getLong("referenceid"));
					currentAttribute.setVaultId(rset.getString("vaultid"));
					currentAttribute.setKeyId(rset.getString("keyid"));
					currentAttribute.setVaulted(rset.getBoolean("isvaulted"));
					currentAttribute.setEnciphered(rset.getBoolean("isenciphered"));
					attributes.add(currentAttribute);
				}
				currentAttribute.getValues().add(rset.getString("value"));
				currentAttribute.setIndex(valueIndex);
				valueIndex++;
			}
			rset.close();
			
		} catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error(e);
			throw new FactoryException(e.getMessage());
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(e);
			}
		}

		
		return attributes;
	}
	public boolean deleteAttributes(NameIdType object){
		return deleteAttributes(object,false);
	}
	public boolean deleteAttributes(NameIdType object, boolean preserveValues){
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldReferenceId(object.getId()));
		fields.add(QueryFields.getFieldReferenceType(object.getNameType()));

		boolean outBool = false;
		try {
			int delCount = deleteByField(fields.toArray(new QueryField[0]),object.getOrganizationId());
			outBool = (delCount > 0);
			if(!preserveValues) object.getAttributes().clear();
		} catch (FactoryException e) {
			
			logger.error(e);
		}
		return outBool;
	}
	public <T> boolean deleteAttributesForObjects(T[] objects){
		long[] ids = new long[objects.length];
		if(ids.length == 0) return true;
		NameEnumType ntype = NameEnumType.UNKNOWN;
		NameIdType nobj = null;
		long organizationId = 0L;

		for(int i = 0; i < ids.length;i++){
			nobj = (NameIdType)objects[i];
			if(i==0){
				ntype = nobj.getNameType();
				organizationId = nobj.getOrganizationId();
			}
			ids[i] = nobj.getId();
		}
		return (deleteByReferenceId(ids, ntype, organizationId) > 0);
	}	

	public boolean deleteAttribute(AttributeType attribute){
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldReferenceId(attribute.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(attribute.getReferenceType()));
		fields.add(QueryFields.getFieldName(attribute.getName()));
		boolean outBool = false;
		try {
			int delCount = deleteByField(fields.toArray(new QueryField[0]),attribute.getOrganizationId());
			outBool = (delCount > 0);
		} catch (FactoryException e) {
			
			logger.error(e);
		}
		return outBool;
	}
	protected int deleteByReferenceId(long[] ids, NameEnumType nType, long organizationId)
	{
		if (ids.length == 0)
			return 0;
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(connectionType);
		PreparedStatement statement = null;
		int deletedRecords = 0;
		try {
			String sql = String.format("DELETE FROM %s WHERE referencetype = %s AND referenceid = %s",this.primaryTableName, token, token);
			statement = connection.prepareStatement(sql);
			for (int i = 0; i < ids.length; i++)
			{
				statement.setString(1, nType.toString());
				statement.setLong(2, ids[i]);
				statement.addBatch();
				if((i > 0 || ids.length ==1 ) && ((i % BulkFactories.bulkBatchSize == 0) || i == ids.length - 1)){
					int[] del = statement.executeBatch();
					for(int d = 0; d < del.length; d++)
						deletedRecords += del[d];
				}
			}
			statement.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			
			logger.error(e);
		}
		finally{
			try {
				if(statement != null) statement.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				
				logger.error(e);
			}
		}

		return deletedRecords;
	}
	
	
}
