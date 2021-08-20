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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.CompressionEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class DataFactory extends NameIdFactory {
	private DatatypeFactory dtFactory = null;
	
	/// Default data memory cache size is 100MB.  This will cause problems if trying to stream large files
	/// The mechanism by which large files are streams needs to be reset - in previous versions it used to be handled, but in the current version it hasn't been added back in yet
	///
	private long maximumCacheSize = 1048576L*100L;
	private long currentCacheSize = 0L;

	public void setMaximumCacheSize(long l){
		maximumCacheSize = l;
		
	}
	@Override
	public void registerProvider(){
		AuthorizationService.registerAuthorizationProviders(
				FactoryEnumType.DATA,
				NameEnumType.DATA,
				FactoryEnumType.DATAPARTICIPATION
			);
	}

	public DataFactory(){
		super();
		this.scopeToOrganization = true;
		this.canJoinToAttribute = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
		this.hasObjectId = true;
		this.hasUrn = true;
		this.isVaulted = true;
		this.primaryTableName = "data";
		this.tableNames.add(this.primaryTableName);
		factoryType = FactoryEnumType.DATA;
		systemRoleNameReader = RoleService.ROLE_DATA_READERS;
		systemRoleNameAdministrator = RoleService.ROLE_DATA_ADMINISTRATOR;
	}
	@Override
	protected void checkCacheExpires(){
		super.checkCacheExpires();

		if(currentCacheSize >= maximumCacheSize){
			logger.info("Exceeded maximum data cache size " + (maximumCacheSize / 1024) + " KB.  Clearing data cache.");
			clearCache();
			currentCacheSize = 0L;
		}
	}
	
	@Override
	public synchronized boolean addToCache(NameIdType map, String keyName) throws ArgumentException{
		boolean ret = super.addToCache(map, keyName);
		if(ret){
			currentCacheSize += (long)((DataType)map).getSize();
		}
		return ret;
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		DataType t = (DataType)obj;
		return t.getName() + "-" + t.getGroupId();
	}
	
	@Override
	public <T> void normalize(T object) throws ArgumentException, FactoryException{
		super.normalize(object);
		if(object == null){
			throw new ArgumentException("Null object");
		}
		DataType obj = (DataType)object;
		if(obj.getGroupId() != null && obj.getGroupId().compareTo(0L) != 0) return;
		if(obj.getGroupPath() == null || obj.getGroupPath().length() == 0){
			logger.debug("Group path is not defined");
			return;
		}
		BaseGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null,GroupEnumType.DATA, obj.getGroupPath(), obj.getOrganizationId());
		if(dir == null){
			throw new ArgumentException("Invalid group path '" + obj.getGroupPath() + "'");
		}
		obj.setGroupId(dir.getId());
	}
	
	@Override
	public <T> void denormalize(T object) throws ArgumentException, FactoryException{
		super.denormalize(object);
		if(object == null){
			throw new ArgumentException("Null object");
		}
		DataType obj = (DataType)object;
		if(obj.getGroupId().compareTo(0L) == 0){
			throw new ArgumentException("Invalid object group");	
		}
		if(obj.getGroupPath() != null) return;
		BaseGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(obj.getGroupId(), obj.getOrganizationId());
		obj.setGroupPath(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getPath(dir));
	}
	
	
	protected void updateDataToCache(DataType data) throws ArgumentException{
		String keyName = data.getName() + "-" + data.getGroupId();
		if(this.haveCacheId(data.getId())) removeFromCache(data);
		addToCache(data, keyName);
	}
	protected void removeDataFromCache(DataType data){
		String keyName = getCacheKeyName(data);
		removeFromCache(data, keyName);
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(this.primaryTableName)){

		}
	}

	/// Create alternate select strings for select/updateDetails
	///
	@Override
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		int len = dataTables.size();

		CONNECTION_TYPE  connectionType = DBFactory.getConnectionType(connection);
		
		for(int i = 0; i < len; i++){
			/// should be cached
			///
			DataTable table = dataTables.get(i);

			configureTableRestrictions(table);

			StringBuilder buff = new StringBuilder();

			String lockHint = DBFactory.getNoLockHint(connectionType);

			buff.append("SELECT #TOP# ");
			int scount = 0;
			for (int c = 0; c < table.getColumnSize(); c++)
			{
				DataColumnType column = table.getColumns().get(c);
				if(column.getColumnName().equals("compressiontype") || column.getColumnName().equals("iscompressed") || column.getColumnName().equals("datastring") || column.getColumnName().equals("datablob")) continue;
				
				if (table.getCanSelectColumn(column.getColumnName()))
				{
					if (scount > 0) buff.append(",");
					buff.append(column.getColumnName());
					scount++;
				}

			}
			String tableClause = " FROM " + table.getName() + lockHint;
			table.setSelectDetailsTemplate(buff.toString() + " #PAGE# " + tableClause);
		}
	}
	
	public DataType newData(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || !user.getDatabaseRecord()) throw new ArgumentException("Invalid owner");
		DataType data = new DataType();
		data.setNameType(NameEnumType.DATA);
		data.setOrganizationId(user.getOrganizationId());
		data.setOwnerId(user.getId());
		data.setGroupId(groupId);

	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
	    
		data.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		data.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.YEAR, 1);
		data.setExpiryDate(dtFactory.newXMLGregorianCalendar(cal));
		return data;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		DataType newData = (DataType)object;
		if(newData.getName() == null || newData.getName().length() == 0) throw new FactoryException("Invalid object name");
			

		if (newData.getBlob() && newData.getReadDataBytes()) throw new FactoryException("Cannot add blob data whose byte store has been read");
		if (newData.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new data without a group");

		DataRow row = prepareAdd(newData, "data");
		try{
			row.setCellValue("description",newData.getDescription());
			row.setCellValue("mimetype", newData.getMimeType());
			
			row.setCellValue("groupid", newData.getGroupId());

			
			row.setCellValue("ispasswordprotected", newData.getPasswordProtected());
			row.setCellValue("iscompressed",newData.getCompressed());
			row.setCellValue("compressiontype", newData.getCompressionType().toString());
			row.setCellValue("dimensions", newData.getDimensions());
			row.setCellValue("size", newData.getSize());
			row.setCellValue("rating", newData.getRating());
			row.setCellValue("ispointer", newData.getPointer());
			row.setCellValue("hash", newData.getDataHash());
			row.setCellValue("createddate", newData.getCreatedDate());
			row.setCellValue("modifieddate", newData.getModifiedDate());
			row.setCellValue("expirationdate", newData.getExpiryDate());
			row.setCellValue("isblob", newData.getBlob());
			if(newData.getBlob()) row.setCellValue("datablob", newData.getDataBytesStore());
			else row.setCellValue("datastring", newData.getShortData());
	
			if (insertRow(row)) return true;
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		}
		return false;
	}
	
	public DataType getDataByName(String name, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException{
		return getDataByName(name, false, parentGroup);
	}
	public DataType getDataByName(String name, boolean detailsOnly, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException{
		String keyName = name + "-" + parentGroup.getId();

		DataType outData = readCache(keyName);
		if (outData != null && outData.getDetailsOnly() == detailsOnly) return outData;
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(detailsOnly);
		
		List<NameIdType> data = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldGroup(parentGroup.getId()) }, instruction,parentGroup.getOrganizationId());
		if (!data.isEmpty())
		{
			updateDataToCache((DataType)data.get(0));
			outData = (DataType)data.get(0);
		}

		return outData;
	}
	public DataType getDataById(long id, long organizationId) throws FactoryException, ArgumentException
	{
		return getDataById(id, false, organizationId);
	}
	public DataType getDataById(long id, boolean detailsOnly, long organizationId) throws FactoryException, ArgumentException
	{

		DataType outData = readCache(id);
		if (outData != null && outData.getDetailsOnly() == detailsOnly) return outData;
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(detailsOnly);

		List<NameIdType> data = getByField(new QueryField[] { QueryFields.getFieldId(id) }, instruction, organizationId);

		if (!data.isEmpty())
		{
			outData = (DataType)data.get(0);
			updateDataToCache(outData);
		}
		return outData;
	}
	public DataType getDataByObjectId(String id, long organizationId) throws FactoryException, ArgumentException
	{
		return getDataByObjectId(id, false, organizationId);
	}
	public DataType getDataByObjectId(String id, boolean detailsOnly, long organizationId) throws FactoryException, ArgumentException
	{

		DataType outData = readCache(id);
		if (outData != null && outData.getDetailsOnly() == detailsOnly) return outData;
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(detailsOnly);

		List<NameIdType> data = getByField(new QueryField[] { QueryFields.getFieldObjectId(id) }, instruction, organizationId);

		if (!data.isEmpty())
		{
			outData = (DataType)data.get(0);

			updateDataToCache(outData);
		}
		return outData;
	}
	public int deleteDataByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deleteDataByIds(ids, user.getOrganizationId());
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		DataType data = (DataType)object;
		removeFromCache(data);
		if(bulkMode) return true;
		
		int deleted = deleteById(data.getId(), data.getOrganizationId());

		return (deleted > 0);
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		DataType newData = new DataType();
		newData.setNameType(NameEnumType.DATA);
		super.read(rset, newData);
	
		newData.setDetailsOnly((instruction != null && instruction.getAlternateQuery()));
		newData.setPopulated(!newData.getDetailsOnly());
		newData.setMimeType(rset.getString("mimetype"));
		
		newData.setPasswordProtected(rset.getBoolean("ispasswordprotected"));
		newData.setCompressed(rset.getBoolean("iscompressed"));
		newData.setCompressionType(CompressionEnumType.valueOf(rset.getString("compressiontype")));
		newData.setDescription(rset.getString("description"));
		
		newData.setDimensions(rset.getString("dimensions"));
		newData.setSize(rset.getInt("size"));
		newData.setRating(rset.getDouble("rating"));
		newData.setPointer(rset.getBoolean("ispointer"));
		newData.setDataHash(rset.getString("hash"));

		newData.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		
		newData.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		newData.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));

		long groupId = rset.getLong("groupid");
		newData.setBlob(rset.getBoolean("isblob"));

		if(!newData.getDetailsOnly()){
			if(newData.getBlob()){
				
				newData.setDataBytesStore(rset.getBytes("datablob"));
			}
			else{
				newData.setShortData(rset.getString("datastring"));
			}
		}
		
		/// 2008/01/28
		/// Moved to bottom for Mono; Mono throws an 'array index' error on any sibling read operation
		///
		newData.setGroupId(groupId);
		return newData;
	}
	
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		DataType data = (DataType)object;
		removeFromCache(data);
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(data.getDetailsOnly());
		if(!data.getDetailsOnly() && data.getBlob() && data.getReadDataBytes()){
			throw new FactoryException("Cannot update data whose byte store has been read");
		}
        data.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(new Date()));
		return super.update(data, instruction);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		DataType useMap = (DataType)map;
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldMimeType(useMap.getMimeType()));
		fields.add(QueryFields.getFieldPasswordProtected(useMap.getPasswordProtected()));
		fields.add(QueryFields.getFieldGroup(useMap.getGroupId()));

		fields.add(QueryFields.getFieldDimensions(useMap.getDimensions()));
		fields.add(QueryFields.getFieldSize(useMap.getSize()));
		fields.add(QueryFields.getFieldRating(useMap.getRating()));
		fields.add(QueryFields.getFieldPointer(useMap.getPointer()));
		fields.add(QueryFields.getFieldDataHash(useMap.getDataHash()));
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));
		fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(useMap.getExpiryDate()));
		fields.add(QueryFields.getFieldBlob(useMap.getBlob()));
		if(instruction == null || !instruction.getAlternateQuery()){
			fields.add(QueryFields.getFieldCompressed(useMap.getCompressed()));
			fields.add(QueryFields.getFieldCompressionType(useMap.getCompressionType()));
			if(useMap.getBlob()){
				fields.add(QueryFields.getFieldDataBlob(useMap.getDataBytesStore()));
				fields.add(QueryFields.getFieldDataString(null));
			}
			else{
				fields.add(QueryFields.getFieldDataBlob(null));
				fields.add(QueryFields.getFieldDataString(useMap.getShortData()));
			}
		}
	}
	
	public int deleteDataByIds(long[] ids, long organizationId) throws FactoryException
	{
		return deleteById(ids, organizationId);

	}
	public int deleteDataInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		return deleteDataByIds(ids, group.getOrganizationId());
	}
	
	public List<DataType>  getDataListByGroup(DirectoryGroupType group, boolean detailsOnly, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getDataList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, detailsOnly, startRecord, recordCount, organizationId);
	}
	public List<DataType>  getDataListByGroup(DirectoryGroupType group, ProcessingInstructionType instruction, boolean detailsOnly, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		return getDataList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, instruction, detailsOnly, startRecord, recordCount,organizationId);
	}
	public List<DataType>  getDataList(QueryField[] fields, boolean detailsOnly, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getDataList(fields, instruction, detailsOnly, startRecord,recordCount,organizationId);
	}
	public List<DataType>  getDataList(QueryField[] fields, ProcessingInstructionType instruction,boolean detailsOnly, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0L && recordCount > 0 && !instruction.getPaginate())
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getDataList(fields, instruction, detailsOnly, organizationId);
	}
	public List<DataType> getDataList(QueryField[] fields, ProcessingInstructionType instruction,boolean detailsOnly, long organizationId) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(detailsOnly);

		List<NameIdType> dataList = getByField(fields, instruction, organizationId);
		return convertList(dataList);
	}
	
	public List<DataType> getDataListByIds(long[] dataIds, boolean detailsOnly, long organizationId) throws FactoryException, ArgumentException
	{
		StringBuilder buff = new StringBuilder();
		List<DataType> outList = new ArrayList<>();
		for (int i = 0; i < dataIds.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(dataIds[i]);
			if ((i > 0 || dataIds.length == 1) && ((i % BulkFactories.bulkQueryLimit == 0) || i == dataIds.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
				match.setComparator(ComparatorEnumType.ANY);
				List<DataType> tmpDataList = getDataList(new QueryField[] { match }, null, detailsOnly, organizationId);
				outList.addAll(tmpDataList);
				buff.delete(0,  buff.length());
			}
		}
		return outList;
	}

	public int getCount(DirectoryGroupType group) throws FactoryException
	{
		return getCountByField(this.getDataTables().get(0), new QueryField[]{QueryFields.getFieldGroup(group.getId())}, group.getOrganizationId());
	}
	
}
