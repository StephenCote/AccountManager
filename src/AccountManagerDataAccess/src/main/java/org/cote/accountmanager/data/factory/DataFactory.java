package org.cote.accountmanager.data.factory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.CompressionEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class DataFactory extends NameIdFactory {
	private DatatypeFactory dtFactory = null;
	
	private long currentCacheSize = 0L;
	
	/// Max cache size = 50MB;
	///
	private long maximumCacheSize = 1048576L*100L;
	public DataFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = true;
		this.hasUrn = true;
		this.tableNames.add("data");
		factoryType = FactoryEnumType.DATA;
	}
	@Override
	protected void checkCacheExpires(){
		super.checkCacheExpires();
		//logger.info("Current cache size: " + (currentCacheSize > 0L ? (currentCacheSize / 1024) + " kb" : "0"));
		if(currentCacheSize >= maximumCacheSize){
			logger.info("Exceeded maximum data cache size " + (maximumCacheSize / 1024) + " KB.  Clearing data cache.");
			clearCache();
			currentCacheSize = 0L;
		}
	}
	@Override
	public synchronized boolean addToCache(NameIdType map, String key_name) throws ArgumentException{
		boolean ret = super.addToCache(map, key_name);
		if(ret){
			currentCacheSize += (long)((DataType)map).getSize();
		}
		return ret;
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		DataType t = (DataType)obj;
		return t.getName() + "-" + t.getGroup().getId();
	}
	protected void updateDataToCache(DataType data) throws ArgumentException{
		String key_name = data.getName() + "-" + data.getGroup().getId();
		//System.out.println("Update data to cache: " + key_name);
		if(this.haveCacheId(data.getId())) removeFromCache(data);
		addToCache(data, key_name);
	}
	protected void removeDataFromCache(DataType data){
		String key_name = getCacheKeyName(data);
		//String key_name = data.getName() + "-" + data.getGroup().getId();
		//System.out.println("Remove data from cache: " + key_name);
		removeFromCache(data, key_name);
	}
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("data")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	protected String getSelectTemplate(DataTable table, ProcessingInstructionType instruction){
		return table.getSelectFullTemplate();
	}
	
	/// Create alternate select strings for select/updateDetails
	///
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int len = dataTables.size();

		CONNECTION_TYPE  connectionType = DBFactory.getConnectionType(connection);
		
		for(int i = 0; i < len; i++){
			//String tableName = tableNames.get(i);
			/// should be cached
			///
			DataTable table = dataTables.get(i);

			configureTableRestrictions(table);

			StringBuffer buff = new StringBuffer();
			//StringBuffer ubuff = new StringBuffer();

			String lock_hint = DBFactory.getNoLockHint(connectionType);
			String token = DBFactory.getParamToken(connectionType);

			buff.append("SELECT #TOP# ");
			//ubuff.append("UPDATE " + table.getName() + " SET ");
			int ucount = 0;
			int scount = 0;
			for (int c = 0; c < table.getColumnSize(); c++)
			{
				DataColumnType column = table.getColumns().get(c);
				if(column.getColumnName().equals("datastring") || column.getColumnName().equals("datablob")) continue;
				
				if (table.getCanSelectColumn(column.getColumnName()))
				{
					if (scount > 0) buff.append(",");
					buff.append(column.getColumnName());
					scount++;
				}
				/*
				if (table.getCanUpdateColumn(column.getColumnName()))
				{
					if (ucount > 0) ubuff.append(",");
					ubuff.append(column.getColumnName() + " = " + token + column.getColumnName());
					ucount++;
				}
				*/
			}
			String table_clause = " FROM " + table.getName() + lock_hint;
			table.setSelectDetailsTemplate(buff.toString() + " #PAGE# " + table_clause);
			//table.setUpdateDetailsTemplate(ubuff.toString());
		}

	}
	public DataType newData(UserType user, DirectoryGroupType group) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		DataType data = new DataType();
		data.setNameType(NameEnumType.DATA);
		data.setOrganization(group.getOrganization());
		data.setOwnerId(user.getId());
		data.setGroup(group);

	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
	    //cal.add(GregorianCalendar.YEAR, 1);
	    
		data.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		data.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.YEAR, 1);
		data.setExpiryDate(dtFactory.newXMLGregorianCalendar(cal));
		return data;
	}
	
	public boolean addData(DataType new_data) throws FactoryException
	{
		if(new_data.getName() == null || new_data.getName().length() == 0) throw new FactoryException("Invalid object name");
			

		if (new_data.getBlob() && new_data.getReadDataBytes()) throw new FactoryException("Cannot add blob data whose byte store has been read");
		if (new_data.getGroup() == null) throw new FactoryException("Cannot add new data without a group");

		DataRow row = prepareAdd(new_data, "data");
		try{
			row.setCellValue("description",new_data.getDescription());
			row.setCellValue("mimetype", new_data.getMimeType());
			row.setCellValue("vaultid",new_data.getVaultId());
			row.setCellValue("groupid", new_data.getGroup().getId());
			row.setCellValue("keyid", new_data.getKeyId());
			row.setCellValue("isvaulted", new_data.getVaulted());
			row.setCellValue("isenciphered", new_data.getEnciphered());
			row.setCellValue("ispasswordprotected", new_data.getPasswordProtected());
			row.setCellValue("iscompressed",new_data.getCompressed());
			row.setCellValue("compressiontype", new_data.getCompressionType().toString());
			row.setCellValue("dimensions", new_data.getDimensions());
			row.setCellValue("size", new_data.getSize());
			row.setCellValue("rating", new_data.getRating());
			row.setCellValue("ispointer", new_data.getPointer());
			row.setCellValue("hash", new_data.getDataHash());
			row.setCellValue("createddate", new_data.getCreatedDate());
			row.setCellValue("modifieddate", new_data.getModifiedDate());
			row.setCellValue("expirationdate", new_data.getExpiryDate());
			row.setCellValue("isblob", new_data.getBlob());
			if(new_data.getBlob()) row.setCellValue("datablob", new_data.getDataBytesStore());
			else row.setCellValue("datastring", new_data.getShortData());
	
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
		//OrganizationType out_org = null;
		String key_name = name + "-" + parentGroup.getId();

		DataType out_data = readCache(key_name);
		if (out_data != null && out_data.getDetailsOnly() == detailsOnly) return out_data;
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(detailsOnly);
		
		List<NameIdType> data = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldGroup(parentGroup.getId()) }, instruction,parentGroup.getOrganization().getId());
			//GetByName(name);
		if (data.size() > 0)
		{
			updateDataToCache((DataType)data.get(0));
			out_data = (DataType)data.get(0);
		}
		else{
			//System.out.println("No results for " + name + " in " + parentGroup.getId());
		}
		return out_data;
	}
	public DataType getDataById(long id, OrganizationType org) throws FactoryException, ArgumentException
	{
		return getDataById(id, false, org);
	}
	public DataType getDataById(long id, boolean detailsOnly, OrganizationType org) throws FactoryException, ArgumentException
	{

		DataType out_data = readCache(id);
		if (out_data != null && out_data.getDetailsOnly() == detailsOnly) return out_data;
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(detailsOnly);

		List<NameIdType> data = getByField(new QueryField[] { QueryFields.getFieldId(id) }, instruction, org.getId());

		if (data.size() > 0)
		{
			out_data = (DataType)data.get(0);
			//String key_name = id + "-" + out_data.getGroup().getId();
			updateDataToCache(out_data);
		}
		return out_data;
	}
	public int deleteDataByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganization().getId());
		return deleteDataByIds(ids, user.getOrganization());
	}

	public boolean deleteData(DataType data) throws FactoryException
	{
		removeFromCache(data);
		int deleted = deleteById(data.getId(), data.getOrganization().getId());
		/*
		if (deleted > 0)
		{
			OrganizationSecurity.deleteSecurityKeys(organization);
		}
		*/
		return (deleted > 0);
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		DataType new_data = new DataType();
		new_data.setNameType(NameEnumType.DATA);
		super.read(rset, new_data);
	
		new_data.setDetailsOnly((instruction != null && instruction.getAlternateQuery()));
		new_data.setPopulated(!new_data.getDetailsOnly());
		new_data.setMimeType(rset.getString("mimetype"));
		new_data.setVaultId(rset.getString("vaultid"));
		new_data.setKeyId(rset.getString("keyid"));
		new_data.setVaulted(rset.getBoolean("isvaulted"));
		
		// Make a note that if the data is marked as being encrypted, then the internal data buffer is coming in encrypted.
		// This bit is unset when the data is accessed and decrypted
		//
		new_data.setEnciphered(rset.getBoolean("isenciphered"));
		new_data.setPasswordProtected(rset.getBoolean("ispasswordprotected"));
		
		new_data.setCompressed(rset.getBoolean("iscompressed"));
		new_data.setCompressionType(CompressionEnumType.valueOf(rset.getString("compressiontype")));
		new_data.setDescription(rset.getString("description"));
		
		new_data.setDimensions(rset.getString("dimensions"));
		new_data.setSize(rset.getInt("size"));
		new_data.setRating(rset.getDouble("rating"));
		new_data.setPointer(rset.getBoolean("ispointer"));
		new_data.setDataHash(rset.getString("hash"));

		new_data.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		
		new_data.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		new_data.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));

		long group_id = rset.getLong("groupid");
		new_data.setBlob(rset.getBoolean("isblob"));

		if(new_data.getDetailsOnly() == false){
			if(new_data.getBlob()){
				
				new_data.setDataBytesStore(rset.getBytes("datablob"));
			}
			else{
				new_data.setShortData(rset.getString("datastring"));
			}
		}
		
		/// 2008/01/28
		/// Moved to bottom for Mono; Mono throws an 'array index' error on any sibling read operation
		///
		new_data.setGroup(Factories.getGroupFactory().getDirectoryById(group_id, new_data.getOrganization()));
		return new_data;
	}
	public boolean updateData(DataType data) throws FactoryException, DataAccessException
	{
		removeFromCache(data);
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(data.getDetailsOnly());
		if(!data.getDetailsOnly() && data.getBlob() && data.getReadDataBytes()) throw new DataAccessException("Cannot update data whose byte store has been read");
        data.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(new Date()));
		return update(data, instruction);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		DataType use_map = (DataType)map;
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldMimeType(use_map.getMimeType()));
		fields.add(QueryFields.getFieldKeyId(use_map.getKeyId()));
		fields.add(QueryFields.getFieldVaultId(use_map.getVaultId()));
		fields.add(QueryFields.getFieldVaulted(use_map.getVaulted()));
		fields.add(QueryFields.getFieldEnciphered(use_map.getEnciphered()));
		fields.add(QueryFields.getFieldPasswordProtected(use_map.getPasswordProtected()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroup().getId()));
		fields.add(QueryFields.getFieldCompressed(use_map.getCompressed()));
		fields.add(QueryFields.getFieldDimensions(use_map.getDimensions()));
		fields.add(QueryFields.getFieldSize(use_map.getSize()));
		fields.add(QueryFields.getFieldRating(use_map.getRating()));
		fields.add(QueryFields.getFieldPointer(use_map.getPointer()));
		fields.add(QueryFields.getFieldDataHash(use_map.getDataHash()));
		fields.add(QueryFields.getFieldCreatedDate(use_map.getCreatedDate()));
		fields.add(QueryFields.getFieldModifiedDate(use_map.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(use_map.getExpiryDate()));
		fields.add(QueryFields.getFieldBlob(use_map.getBlob()));
		if(instruction == null || instruction.getAlternateQuery() == false){
			if(use_map.getBlob()){
				fields.add(QueryFields.getFieldDataBlob(use_map.getDataBytesStore()));
				fields.add(QueryFields.getFieldDataString(null));
			}
			else{
				fields.add(QueryFields.getFieldDataBlob(null));
				fields.add(QueryFields.getFieldDataString(use_map.getShortData()));
			}
		}
	}
	
	public int deleteDataByIds(long[] ids, OrganizationType organization) throws FactoryException
	{
		int deleted = deleteById(ids, organization.getId());
		if (deleted > 0)
		{
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organization);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organization);
			*/
		}
		return deleted;
	}
	public int deleteDataInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		//logger.info("Deleting group data for " + group.getName());
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganization().getId());
		return deleteDataByIds(ids, group.getOrganization());
	}
	
	public List<DataType>  getDataListByGroup(DirectoryGroupType group, boolean detailsOnly, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getDataList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, detailsOnly, startRecord, recordCount, organization);
	}
	public List<DataType>  getDataListByGroup(DirectoryGroupType group, ProcessingInstructionType instruction, boolean detailsOnly, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		return getDataList(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, instruction, detailsOnly, startRecord, recordCount,organization);
	}
	public List<DataType>  getDataList(QueryField[] fields, boolean detailsOnly, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("name ASC");
		return getDataList(fields, instruction, detailsOnly, startRecord,recordCount,organization);
	}
	public List<DataType>  getDataList(QueryField[] fields, ProcessingInstructionType instruction,boolean detailsOnly, long startRecord, int recordCount, OrganizationType organization)  throws FactoryException, ArgumentException
	{
		/// If pagination not 
		///
		if (instruction != null && startRecord >= 0L && recordCount > 0 && instruction.getPaginate() == false)
		{
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		return getDataList(fields, instruction, detailsOnly, organization);
	}
	public List<DataType> getDataList(QueryField[] fields, ProcessingInstructionType instruction,boolean detailsOnly, OrganizationType organization) throws FactoryException, ArgumentException
	{

		if(instruction == null) instruction = new ProcessingInstructionType();
		instruction.setAlternateQuery(detailsOnly);

		List<NameIdType> dataList = getByField(fields, instruction, organization.getId());
		return convertList(dataList);
		//return data_list.toArray(new DataType[0]);
		//return data_list.ConvertAll(new Converter<NameId, Core.Tools.AccountManager.Map.Data>(MapConverter));
	}
	
	public List<DataType> getDataListByIds(long[] data_ids, boolean detailsOnly, OrganizationType organization) throws FactoryException, ArgumentException
	{
		StringBuffer buff = new StringBuffer();
		int deleted = 0;
		List<DataType> out_list = new ArrayList<DataType>();
		for (int i = 0; i < data_ids.length; i++)
		{
			if (buff.length() > 0) buff.append(",");
			buff.append(data_ids[i]);
			if ((i > 0 || data_ids.length == 1) && ((i % 250 == 0) || i == data_ids.length - 1))
			{
				QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
				match.setComparator(ComparatorEnumType.IN);
				List<DataType> tmp_data_list = getDataList(new QueryField[] { match }, null, detailsOnly, organization);
				out_list.addAll(tmp_data_list);
				buff.delete(0,  buff.length());
			}
		}
		return out_list;
	}

	public int getCount(DirectoryGroupType group) throws FactoryException
	{
		return getCountByField(this.getDataTables().get(0), new QueryField[]{QueryFields.getFieldGroup(group.getId())}, group.getOrganization().getId());
	}
	
}
