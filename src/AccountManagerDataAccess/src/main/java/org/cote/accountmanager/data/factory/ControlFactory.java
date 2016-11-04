package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class ControlFactory extends NameIdFactory {
	private DatatypeFactory dtFactory = null;
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
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error(e.getStackTrace());
		}
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

	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		ControlType obj = (ControlType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	
	public ControlType newControl(UserType owner, NameIdType targetObject) throws ArgumentException
	{
		if (owner == null || owner.getId().compareTo(0L)==0) throw new ArgumentException("Invalid owner");
		if(targetObject.getNameType() == NameEnumType.UNKNOWN) throw new ArgumentException("Invalid target object");
		/// It's ok if targetObject has no ID - this will be used to indicate its organization level
		/// This is needed for create operations of a given type
		///
		/// targetObject.getId().compareTo(0L) == 0 || 
		ControlType cred = new ControlType();
		cred.setNameType(NameEnumType.CREDENTIAL);
		cred.setControlType(ControlEnumType.UNKNOWN);
		cred.setControlAction(ControlActionEnumType.UNKNOWN);
		cred.setOrganizationId(owner.getOrganizationId());
		cred.setOwnerId(owner.getId());
		cred.setReferenceType(FactoryEnumType.valueOf(targetObject.getNameType().toString()));
		cred.setReferenceId(targetObject.getId());
		
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		cred.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		cred.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.YEAR, 5);
		cred.setExpiryDate(dtFactory.newXMLGregorianCalendar(cal));
		
		return cred;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		ControlType obj = (ControlType)object;
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
	public ControlType getControlByObjectId(String id, long organizationId) throws FactoryException, ArgumentException{
		List<NameIdType> sec = getByObjectId(id, organizationId);
		if(sec.size() > 0) return (ControlType)sec.get(0);
		return null;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ControlType data = (ControlType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	public List<ControlType> getControlsForType(NameIdType obj,boolean includeGlobal,boolean onlyGlobal) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		// allow id of 0 for global control checks
		// || obj.getId().compareTo(0L) == 0
		if(obj == null || obj.getNameType() == NameEnumType.UNKNOWN || obj.getOrganizationId() == null) throw new ArgumentException("Invalid object reference");
		fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		
		QueryField reference_id_filters = new QueryField(SqlDataEnumType.NULL,"referenceid",null);
		reference_id_filters.setComparator(ComparatorEnumType.GROUP_OR);
		if(onlyGlobal == false) reference_id_filters.getFields().add(QueryFields.getFieldReferenceId(obj.getId()));
		if(includeGlobal || onlyGlobal) reference_id_filters.getFields().add(QueryFields.getFieldReferenceId(0L));
		
		fields.add(reference_id_filters);
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(0L);
		pi.setRecordCount(2);
		
		return list(fields.toArray(new QueryField[0]), pi, obj.getOrganizationId());
	}
	public boolean deleteControlsForType(NameIdType obj) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<QueryField>();
		// allow id of 0 for global controls
		// || obj.getId().compareTo(0L) == 0
		if(obj == null || obj.getNameType() == NameEnumType.UNKNOWN || obj.getOrganizationId() == null) throw new ArgumentException("Invalid object reference");
		fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		
		fields.add(QueryFields.getFieldReferenceId(obj.getId()));

		return (this.deleteByField(fields.toArray(new QueryField[0]), obj.getOrganizationId()) > 0);
	}

	
}
