package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class ControlFactory extends NameIdFactory {
	public ControlFactory(){
		super();
		this.hasOwnerId = true;
		this.hasParentId=false;
		this.hasUrn = false;
		this.hasName = false;
		this.hasObjectId = true;
		this.aggressiveKeyFlush = false;
		this.useThreadSafeCollections = false;
		this.scopeToOrganization = true;

		this.tableNames.add("control");

		factoryType = FactoryEnumType.CONTROL;
	}
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		ControlType cit = (ControlType)map;
		Long tmpId = 0L;
		if(cit.getReferenceId().compareTo(0L) < 0){
			tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getReferenceId());
			if(tmpId.compareTo(0L) > 0) cit.setReferenceId(tmpId.longValue());
		}
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		ControlType t = (ControlType)obj;
		return t.getControlType().toString() + "-" + t.getControlAction().toString() + "-" + t.getReferenceType().toString() + "-" + t.getReferenceId();
	}
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("control")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}

	public boolean deleteControl(ControlType obj) throws FactoryException
	{
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganization().getId());
		return (deleted > 0);
	}
	
	public ControlType newControl(UserType owner, NameIdType targetObject) throws ArgumentException
	{
		if (owner == null || owner.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		if(targetObject.getId() == 0L || targetObject.getNameType() == NameEnumType.UNKNOWN) throw new ArgumentException("Invalid target object");
		
		ControlType cred = new ControlType();
		cred.setNameType(NameEnumType.CREDENTIAL);
		cred.setControlType(ControlEnumType.UNKNOWN);
		cred.setControlAction(ControlActionEnumType.UNKNOWN);
		
		cred.setOwnerId(owner.getId());
		cred.setReferenceType(FactoryEnumType.valueOf(targetObject.getNameType().toString()));
		cred.setReferenceId(targetObject.getId());
		
		return cred;
	}
	
	public boolean addControl(ControlType obj) throws FactoryException
	{

		DataRow row = prepareAdd(obj, "control");


		try{

			row.setCellValue("controlid",obj.getControlId());
			row.setCellValue("referencetype",obj.getReferenceType().toString());
			row.setCellValue("referenceid",obj.getReferenceId());
			row.setCellValue("createddate", obj.getCreatedDate());
			row.setCellValue("modifieddate", obj.getModifiedDate());
			row.setCellValue("expirationdate", obj.getExpiryDate());
			row.setCellValue("controltype",obj.getControlType().toString());
			row.setCellValue("controlaction",obj.getControlAction().toString());

			
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
		ControlType new_cred = new ControlType();
		new_cred.setNameType(NameEnumType.CREDENTIAL);
		super.read(rset, new_cred);
		
		new_cred.setReferenceId(rset.getLong("referenceid"));
		new_cred.setReferenceType(FactoryEnumType.fromValue(rset.getString("referencetype")));
		new_cred.setControlId(rset.getLong("controlid"));
		new_cred.setControlType(ControlEnumType.fromValue(rset.getString("controltype")));
		new_cred.setControlAction(ControlActionEnumType.fromValue(rset.getString("controlaction")));

		new_cred.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		new_cred.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		new_cred.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));

		
		return new_cred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ControlType use_map = (ControlType)map;
		fields.add(QueryFields.getFieldModifiedDate(use_map.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(use_map.getExpiryDate()));
		fields.add(QueryFields.getFieldCreatedDate(use_map.getCreatedDate()));
		fields.add(QueryFields.getFieldReferenceId(use_map.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(use_map.getReferenceType()));
		fields.add(QueryFields.getFieldControlId(use_map.getControlId()));
		fields.add(QueryFields.getFieldControlType(use_map.getControlType()));
		fields.add(QueryFields.getFieldControlAction(use_map.getControlAction()));
	}
	
	public boolean updateControl(ControlType data) throws FactoryException, DataAccessException
	{	
		removeFromCache(data);
		return update(data, null);
	}
	
	

	
}
