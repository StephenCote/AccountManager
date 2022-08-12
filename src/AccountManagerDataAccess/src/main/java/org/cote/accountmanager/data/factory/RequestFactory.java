package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.EnumUtils;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class RequestFactory  extends NameIdFactory {
	private DatatypeFactory dtFactory = null;

	public RequestFactory(){
		super();
		this.hasOwnerId = true;
		this.hasParentId=true;
		this.hasUrn = false;
		this.hasName = false;
		this.hasObjectId = true;
		this.aggressiveKeyFlush = false;
		this.useThreadSafeCollections = false;
		this.scopeToOrganization = true;
		this.primaryTableName = "request";
		this.tableNames.add(primaryTableName);

		factoryType = FactoryEnumType.REQUEST;
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		AccessRequestType cit = (AccessRequestType)map;
		if(cit.getReferenceId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getReferenceId());
			if(tmpId.compareTo(0L) > 0) cit.setReferenceId(tmpId);
		}
		if(cit.getRequestorId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getRequestorId());
			if(tmpId.compareTo(0L) > 0) cit.setRequestorId(tmpId);
		}
		if(cit.getDelegateId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getDelegateId());
			if(tmpId.compareTo(0L) > 0) cit.setDelegateId(tmpId);
		}
		if(cit.getEntitlementId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getEntitlementId());
			if(tmpId.compareTo(0L) > 0) cit.setEntitlementId(tmpId);
		}
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		AccessRequestType t = (AccessRequestType)obj;
		return t.getActionType().toString() + "-" + t.getReferenceType().toString() + "-" + t.getReferenceId()+ "-" + t.getEntitlementType().toString() + "-" + t.getEntitlementId() + "-" + t.getRequestorType().toString() + "-" + t.getRequestorId() + "-" + t.getParentId() + "-" + t.getCreatedDate().toGregorianCalendar().getTimeInMillis();
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){

		}
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		AccessRequestType obj = (AccessRequestType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deleteByRequestor(NameIdType requestor, ApprovalResponseEnumType apprStatus, long organizationId) throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldRequestorType(ApproverEnumType.valueOf(requestor.getNameType().toString())));
		fields.add(QueryFields.getFieldRequestorId(requestor.getId()));
		if(apprStatus != ApprovalResponseEnumType.UNKNOWN) fields.add(QueryFields.getFieldApprovalStatus(apprStatus));
		return deleteByField(fields.toArray(new QueryField[0]), organizationId);
	}
	public int deleteByStatus(UserType owner, ApprovalResponseEnumType apprStatus, long organizationId) throws FactoryException, ArgumentException
	{
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldApprovalStatus(apprStatus));
		if(owner != null) fields.add(QueryFields.getFieldOwner(owner));
		return deleteByField(fields.toArray(new QueryField[0]), organizationId);
	}
	public AccessRequestType newAccessRequest(UserType owner, ActionEnumType action, NameIdType requestor, NameIdType delegate, NameIdType targetObject, NameIdType entitlement, long parentId) throws ArgumentException
	{
		if (owner == null || owner.getId().compareTo(0L)==0) throw new ArgumentException("Invalid owner");
		
		if(entitlement != null) {
			if(entitlement.getNameType() == NameEnumType.PERMISSION && (targetObject == null || targetObject.getNameType() == NameEnumType.UNKNOWN)) throw new ArgumentException("Invalid target object");
			if(EnumUtils.isValidEnum(ApproverEnumType.class, entitlement.getNameType().toString()) == false) throw new ArgumentException("Invalid entitlement type");
		}
		if(delegate != null && EnumUtils.isValidEnum(ApproverEnumType.class, delegate.getNameType().toString()) == false) throw new ArgumentException("Invalid delegate type");
		if(requestor == null) requestor = owner;
		AccessRequestType appr = new AccessRequestType();
		appr.setNameType(NameEnumType.REQUEST);
		appr.setActionType(ActionEnumType.valueOf(action.toString()));
		if(entitlement != null) {
			appr.setEntitlementId(entitlement.getId());
			appr.setEntitlementType(ApproverEnumType.valueOf(entitlement.getNameType().toString()));
		}
		appr.setApprovalStatus(ApprovalResponseEnumType.REQUEST);
		appr.setOrganizationId(owner.getOrganizationId());
		appr.setOwnerId(owner.getId());
		appr.setParentId(parentId);
		appr.setRequestorId(requestor.getId());
		appr.setRequestorType(ApproverEnumType.valueOf(requestor.getNameType().toString()));
		if(targetObject != null) {
			appr.setReferenceType(FactoryEnumType.valueOf(targetObject.getNameType().toString()));
			appr.setReferenceId(targetObject.getId());
		}
		if(delegate != null) {
			appr.setDelegateType(ApproverEnumType.valueOf(targetObject.getNameType().toString()));
			appr.setDelegateId(delegate.getId());			
		}
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		appr.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		appr.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.WEEK_OF_MONTH, 2);
		appr.setExpiryDate(dtFactory.newXMLGregorianCalendar(cal));

		return appr;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		AccessRequestType obj = (AccessRequestType)object;
		DataRow row = prepareAdd(obj, this.primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.APPROVALSTATUS),obj.getApprovalStatus().toString());
			row.setCellValue(Columns.get(ColumnEnumType.ACTIONTYPE),obj.getActionType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.DESCRIPTION), obj.getDescription());
			row.setCellValue(Columns.get(ColumnEnumType.CREATEDDATE), obj.getCreatedDate());
			row.setCellValue(Columns.get(ColumnEnumType.MODIFIEDDATE), obj.getModifiedDate());
			row.setCellValue(Columns.get(ColumnEnumType.EXPIRATIONDATE), obj.getExpiryDate());

			row.setCellValue(Columns.get(ColumnEnumType.REQUESTORTYPE),obj.getRequestorType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.REQUESTORID),obj.getRequestorId());
			row.setCellValue(Columns.get(ColumnEnumType.DELEGATETYPE),obj.getDelegateType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.DELEGATEID),obj.getDelegateId());

			row.setCellValue(Columns.get(ColumnEnumType.REFERENCETYPE),obj.getReferenceType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.REFERENCEID),obj.getReferenceId());
			row.setCellValue(Columns.get(ColumnEnumType.ENTITLEMENTTYPE),obj.getEntitlementType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.ENTITLEMENTID),obj.getEntitlementId());
		
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
		AccessRequestType newApr = new AccessRequestType();
		newApr.setNameType(NameEnumType.REQUEST);
		super.read(rset, newApr);
		newApr.setApprovalStatus(ApprovalResponseEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.APPROVALSTATUS))));
		newApr.setDelegateId(rset.getLong(Columns.get(ColumnEnumType.DELEGATEID)));
		newApr.setDelegateType(ApproverEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.DELEGATETYPE))));

		newApr.setRequestorId(rset.getLong(Columns.get(ColumnEnumType.REQUESTORID)));
		newApr.setRequestorType(ApproverEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.REQUESTORTYPE))));

		newApr.setDescription(rset.getString(Columns.get(ColumnEnumType.DESCRIPTION)));
		
		newApr.setReferenceId(rset.getLong(Columns.get(ColumnEnumType.REFERENCEID)));
		newApr.setReferenceType(FactoryEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.REFERENCETYPE))));
		newApr.setActionType(ActionEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.ACTIONTYPE))));
		newApr.setEntitlementId(rset.getLong(Columns.get(ColumnEnumType.ENTITLEMENTID)));
		newApr.setEntitlementType(ApproverEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.ENTITLEMENTTYPE))));
		
		newApr.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.CREATEDDATE))));
		newApr.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.MODIFIEDDATE))));
		newApr.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.EXPIRATIONDATE))));

		
		return newApr;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		AccessRequestType useMap = (AccessRequestType)map;

		fields.add(QueryFields.getFieldApprovalStatus(useMap.getApprovalStatus()));
		fields.add(QueryFields.getFieldRequestorId(useMap.getRequestorId()));
		fields.add(QueryFields.getFieldRequestorType(useMap.getRequestorType()));
		fields.add(QueryFields.getFieldDelegateId(useMap.getDelegateId()));
		fields.add(QueryFields.getFieldDelegateType(useMap.getDelegateType()));
		fields.add(QueryFields.getFieldDescription(useMap.getDescription()));
		fields.add(QueryFields.getFieldActionType(useMap.getActionType()));
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(useMap.getReferenceType()));
		fields.add(QueryFields.getFieldEntitlementId(useMap.getEntitlementId()));
		fields.add(QueryFields.getFieldEntitlementType(useMap.getEntitlementType()));
		fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(useMap.getExpiryDate()));
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));

	}
	public AccessRequestType getAccessRequestByObjectId(String id, long organizationId) throws FactoryException, ArgumentException{
		List<NameIdType> sec = getByObjectId(id, organizationId);
		if(!sec.isEmpty()) return (AccessRequestType)sec.get(0);
		return null;
	}
	
	private List<QueryField> getQueryFields(NameIdType requestor, NameIdType delegate, NameIdType targetObject, NameIdType entitlement, ApprovalResponseEnumType requestStatus, long parentId) {
		List<QueryField> fields = new ArrayList<>();
		// allow id of 0 for global control checks
		if(requestor == null && delegate == null && entitlement == null && parentId <= 0L) {
			//throw new ArgumentException("Expected a requestor, delegate, or entitlement");
			logger.warn("No requestor, delegate, or entitlement were specified");;
		}
		if(requestStatus != ApprovalResponseEnumType.UNKNOWN) {
			fields.add(QueryFields.getFieldApprovalStatus(requestStatus));
		}
		if(requestor != null) {
			fields.add(QueryFields.getFieldRequestorId(requestor.getId()));
			fields.add(QueryFields.getFieldRequestorType(ApproverEnumType.valueOf(requestor.getNameType().toString())));
		}
		if(delegate != null) {
			fields.add(QueryFields.getFieldDelegateId(delegate.getId()));
			fields.add(QueryFields.getFieldDelegateType(ApproverEnumType.valueOf(delegate.getNameType().toString())));
		}
		if(entitlement != null) {
			fields.add(QueryFields.getFieldEntitlementId(entitlement.getId()));
			fields.add(QueryFields.getFieldEntitlementType(ApproverEnumType.valueOf(entitlement.getNameType().toString())));
		}	
		if(targetObject != null) {
			fields.add(QueryFields.getFieldReferenceId(targetObject.getId()));
			fields.add(QueryFields.getFieldReferenceType(FactoryEnumType.valueOf(targetObject.getNameType().toString())));

		}
		if(parentId > 0L) {
			fields.add(QueryFields.getFieldParent(parentId));
		}
		return fields;
	}
	
	public List<AccessRequestType> getAccessRequestsForType(NameIdType requestor, NameIdType delegate, NameIdType targetObject, NameIdType entitlement, ApprovalResponseEnumType requestStatus, long parentId, long organizationId) throws FactoryException, ArgumentException{
		return getAccessRequestsForType(requestor, delegate, targetObject, entitlement, requestStatus, parentId, 0L, 0, organizationId);
	}
	public List<AccessRequestType> getAccessRequestsForType(
			NameIdType requestor,
			NameIdType delegate,
			NameIdType targetObject,
			NameIdType entitlement,
			ApprovalResponseEnumType requestStatus,
			long parentId,
			long startRecord,
			int recordCount,
			long organizationId
	) throws FactoryException, ArgumentException{
		List<QueryField> fields = getQueryFields(requestor, delegate, targetObject, entitlement, requestStatus, parentId);
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(startRecord);
		pi.setRecordCount(recordCount);
		pi.setOrderClause("createddate DESC");
		return list(fields.toArray(new QueryField[0]), pi, organizationId);
	}
	
	public int countAccessRequestsForType(NameIdType requestor, NameIdType delegate, NameIdType targetObject, NameIdType entitlement, ApprovalResponseEnumType requestStatus, long parentId, long organizationId) throws FactoryException, ArgumentException{
		List<QueryField> fields = getQueryFields(requestor, delegate, targetObject, entitlement, requestStatus, parentId);
		return getCountByField(this.getDataTables().get(0), fields.toArray(new QueryField[0]), organizationId);
	}
	
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		AccessRequestType data = (AccessRequestType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	


	
}