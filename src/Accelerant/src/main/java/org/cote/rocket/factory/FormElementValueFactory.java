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
package org.cote.rocket.factory;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormType;
import org.cote.rocket.query.QueryFields;


public class FormElementValueFactory extends NameIdFactory {
	
	public FormElementValueFactory(){
		super();
		this.hasParentId = false;
		this.primaryTableName = "formelementvalue";
		this.tableNames.add(primaryTableName);
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		FormElementValueType t = (FormElementValueType)obj;
		return t.getName() + "-" + t.getFormElementId() + "-" + t.getOrganizationId();
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){
			/// restrict columns
		}
	}

	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		FormElementValueType formElementValue = (FormElementValueType)obj;
		if(formElementValue.getPopulated().booleanValue()) return;
		formElementValue.setPopulated(true);
		updateToCache(formElementValue);
	}

	public FormElementValueType newFormElementValue(UserType user, FormType form, FormElementType formElement) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		if (form == null || form.getId().compareTo(0L)==0 || formElement == null || formElement.getId() <= 0) throw new ArgumentException("Cannot add new FormElementValue without a valid FormElement");
		FormElementValueType obj = new FormElementValueType();
		obj.setFormElementId(formElement.getId());
		obj.setFormId(form.getId());
		obj.setOrganizationId(formElement.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setNameType(NameEnumType.FORMELEMENTVALUE);
		obj.setName(formElement.getName());
		return obj;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException, FactoryException
	{
		FormElementValueType obj = (FormElementValueType)object;
		if (obj.getFormElementId() <= 0) throw new FactoryException("Cannot add new FormElementValue without a FormElement");

		DataRow row = prepareAdd(obj, primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.FORMID), obj.getFormId());
			row.setCellValue(Columns.get(ColumnEnumType.FORMELEMENTID), obj.getFormElementId());
			row.setCellValue(Columns.get(ColumnEnumType.ISBINARY), obj.getIsBinary());
			if(!obj.getIsBinary().booleanValue() && obj.getTextValue() != null) row.setCellValue(Columns.get(ColumnEnumType.TEXTVALUE), obj.getTextValue());
			if(obj.getIsBinary().booleanValue()){
				row.setCellValue(Columns.get(ColumnEnumType.BINARYVALUEID), obj.getBinaryId());
			}

			if (insertRow(row)) return true;
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> T getByNameInGroup(String name, FormType form, FormElementType formElement) throws FactoryException,ArgumentException{

		String keyName = name + "-" + formElement.getId() + "-" + formElement.getOrganizationId();
		T outData = readCache(keyName);
		if (outData != null) return outData;

		List<NameIdType> objList = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldFormElementId(formElement.getId()) }, formElement.getOrganizationId());

		if (objList.size() > 0)
		{
			addToCache(objList.get(0),keyName);
			outData = (T)objList.get(0);
		}

		return outData;
	}
	
	public List<FormElementValueType> getByForm(FormType form) throws FactoryException,ArgumentException{


		return list(new QueryField[] { QueryFields.getBigIntField(Columns.get(ColumnEnumType.FORMID),form.getId()) }, form.getOrganizationId());
	}
	public List<FormElementValueType> getByFormElement(FormType form, FormElementType formElement) throws FactoryException,ArgumentException{


		return list(new QueryField[] { QueryFields.getBigIntField(Columns.get(ColumnEnumType.FORMID),form.getId()), QueryFields.getBigIntField(Columns.get(ColumnEnumType.FORMELEMENTID),formElement.getId()) }, form.getOrganizationId());
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		FormElementValueType newObj = new FormElementValueType();
		newObj.setNameType(NameEnumType.FORMELEMENTVALUE);
		super.read(rset, newObj);

		newObj.setIsBinary(rset.getBoolean(Columns.get(ColumnEnumType.ISBINARY)));
		if(!newObj.getIsBinary()) newObj.setTextValue(rset.getString(Columns.get(ColumnEnumType.TEXTVALUE)));
		else{
			newObj.setBinaryId(rset.getLong(Columns.get(ColumnEnumType.BINARYVALUEID)));
		}
		newObj.setFormElementId(rset.getLong(Columns.get(ColumnEnumType.FORMELEMENTID)));
		newObj.setFormId(rset.getLong(Columns.get(ColumnEnumType.FORMID)));
		return newObj;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		NameIdType data = (NameIdType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		FormElementValueType useMap = (FormElementValueType)map;
		
		/// Form values cannot be changed to different elements
		///
		fields.add(QueryFields.getFieldTextValue(useMap.getTextValue()));
		fields.add(QueryFields.getFieldBinaryValueId((useMap.getIsBinary() ? useMap.getBinaryId() : 0)));
		fields.add(QueryFields.getFieldIsBinary(useMap.getIsBinary()));
	}
	public int deleteFormElementValuesByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteFormElementValuesByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		NameIdType obj = (NameIdType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteFormElementValuesByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);
	}
	public int deleteFormElementValuesByElementIds(long[] ids, long organizationId) throws FactoryException
	{
		QueryField match = new QueryField(SqlDataEnumType.BIGINT, Columns.get(ColumnEnumType.FORMELEMENTID), QueryFields.getFilteredLongList(ids));
		match.setComparator(ComparatorEnumType.ANY);
		return deleteByField(new QueryField[]{match}, organizationId);
	}
	public int deleteFormElementValuesByFormIds(long[] ids, long organizationId) throws FactoryException
	{
		QueryField match = new QueryField(SqlDataEnumType.BIGINT, Columns.get(ColumnEnumType.FORMID), QueryFields.getFilteredLongList(ids));
		match.setComparator(ComparatorEnumType.ANY);
		return deleteByField(new QueryField[]{match}, organizationId);
	}

	public List<FormElementValueType>  getFormElementValueList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return paginateList(fields, startRecord, recordCount, organizationId);
	}
	public List<FormElementValueType> getFormElementValueListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return listByIds(ids, organizationId);
	}
	
}
