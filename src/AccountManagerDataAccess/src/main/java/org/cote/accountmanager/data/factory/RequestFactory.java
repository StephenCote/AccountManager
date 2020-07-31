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
import org.cote.accountmanager.objects.ApprovalEnumType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
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
		// if(entitlement == null) throw new ArgumentException("Invalid entitlement object");
		
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
			row.setCellValue("approvalstatus",obj.getApprovalStatus().toString());
			row.setCellValue("actiontype",obj.getActionType().toString());
			row.setCellValue("description", obj.getDescription());
			row.setCellValue("createddate", obj.getCreatedDate());
			row.setCellValue("modifieddate", obj.getModifiedDate());
			row.setCellValue("expirationdate", obj.getExpiryDate());

			row.setCellValue("requestortype",obj.getRequestorType().toString());
			row.setCellValue("requestorid",obj.getRequestorId());
			row.setCellValue("delegatetype",obj.getDelegateType().toString());
			row.setCellValue("delegateid",obj.getDelegateId());

			row.setCellValue("referencetype",obj.getReferenceType().toString());
			row.setCellValue("referenceid",obj.getReferenceId());
			row.setCellValue("entitlementtype",obj.getEntitlementType().toString());
			row.setCellValue("entitlementid",obj.getEntitlementId());
		
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
		newApr.setApprovalStatus(ApprovalResponseEnumType.valueOf(rset.getString("approvalstatus")));
		newApr.setDelegateId(rset.getLong("delegateid"));
		newApr.setDelegateType(ApproverEnumType.fromValue(rset.getString("delegatetype")));

		newApr.setRequestorId(rset.getLong("requestorid"));
		newApr.setRequestorType(ApproverEnumType.fromValue(rset.getString("requestortype")));

		newApr.setDescription(rset.getString("description"));
		
		newApr.setReferenceId(rset.getLong("referenceid"));
		newApr.setReferenceType(FactoryEnumType.fromValue(rset.getString("referencetype")));
		newApr.setActionType(ActionEnumType.fromValue(rset.getString("actiontype")));
		newApr.setEntitlementId(rset.getLong("entitlementid"));
		newApr.setEntitlementType(ApproverEnumType.valueOf(rset.getString("entitlementtype")));
		
		newApr.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("createddate")));
		newApr.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("modifieddate")));
		newApr.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("expirationdate")));

		
		return newApr;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		AccessRequestType useMap = (AccessRequestType)map;
		/// logger.debug("Set status to " + useMap.getApprovalStatus().toString());
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
	
	public List<AccessRequestType> getAccessRequestsForType(NameIdType requestor, NameIdType delegate, NameIdType targetObject, NameIdType entitlement, ApprovalResponseEnumType requestStatus, long parentId, long organizationId) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		// allow id of 0 for global control checks
		if(requestor == null && delegate == null && entitlement == null && parentId <= 0L) {
			throw new ArgumentException("Expected a requestor, delegate, or entitlement");
		}
		if(requestStatus != ApprovalResponseEnumType.UNKNOWN) {
			fields.add(QueryFields.getFieldApprovalStatus(requestStatus));
		}
		if(requestor != null) {
			fields.add(QueryFields.getFieldRequestorId(requestor.getId()));
			fields.add(QueryFields.getFieldRequestorType(ApproverEnumType.valueOf(requestor.getNameType().toString())));
		}
		if(delegate != null) {
			fields.add(QueryFields.getFieldDelegateId(requestor.getId()));
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
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(0L);
		/// left at (2) for some reason
		pi.setRecordCount(0);
		
		return list(fields.toArray(new QueryField[0]), pi, organizationId);
	}
	
	
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		AccessRequestType data = (AccessRequestType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	


	
}