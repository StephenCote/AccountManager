package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

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
import org.cote.accountmanager.objects.ApprovalType;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class ApprovalFactory  extends NameIdFactory {
	private DatatypeFactory dtFactory = null;
	public ApprovalFactory(){
		super();
		this.hasOwnerId = true;
		this.hasParentId=false;
		this.hasUrn = false;
		this.hasName = false;
		this.hasObjectId = true;
		this.aggressiveKeyFlush = false;
		this.useThreadSafeCollections = false;
		this.scopeToOrganization = true;
		this.primaryTableName = "approval";
		this.tableNames.add(primaryTableName);

		factoryType = FactoryEnumType.APPROVAL;
		
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

	}
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		ApprovalType cit = (ApprovalType)map;
		if(cit.getReferenceId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getReferenceId());
			if(tmpId.compareTo(0L) > 0) cit.setReferenceId(tmpId);
		}
		if(cit.getApproverId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getApproverId());
			if(tmpId.compareTo(0L) > 0) cit.setApproverId(tmpId);
		}
		if(cit.getEntitlementId().compareTo(0L) < 0){
			Long tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getEntitlementId());
			if(tmpId.compareTo(0L) > 0) cit.setEntitlementId(tmpId);
		}
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		ApprovalType t = (ApprovalType)obj;
		return t.getApprovalType().toString() + "-" + t.getApproverLevel() + "-" + t.getReferenceType().toString() + "-" + t.getReferenceId()+ "-" + t.getEntitlementType().toString() + "-" + t.getEntitlementId() +"-" + t.getCreatedDate().toGregorianCalendar().getTimeInMillis();
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){

		}
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		ApprovalType obj = (ApprovalType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public ApprovalType newApproval(AccessRequestType request, ApproverType approver) throws ArgumentException
	{
		ApprovalType approval = new ApprovalType();
		approval.setNameType(NameEnumType.APPROVAL);
		approval.setApprovalType(ApprovalEnumType.ACCESS);
		approval.setApproverId(approver.getApproverId());
		approval.setApproverType(approver.getApproverType());
		approval.setApprovalType(approver.getApprovalType());
		approval.setApproverLevel(approver.getApproverLevel());
		approval.setOwnerId(approver.getOwnerId());
		
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		approval.setCreatedDate(dtFactory.newXMLGregorianCalendar(cal));
		approval.setModifiedDate(dtFactory.newXMLGregorianCalendar(cal));
		cal.add(GregorianCalendar.WEEK_OF_MONTH, 2);
		approval.setExpiryDate(dtFactory.newXMLGregorianCalendar(cal));
		
		approval.setEntitlementType(request.getEntitlementType());
		approval.setEntitlementId(request.getEntitlementId());
		approval.setRequestId(request.getObjectId());
		approval.setResponse(ApprovalResponseEnumType.PENDING);
		approval.setOrganizationId(approver.getOrganizationId());

		return approval;
	}
	public ApprovalType newApproval(UserType owner, NameIdType targetObject, NameIdType entitlement, NameIdType approver, ApprovalEnumType approvalType, int level) throws ArgumentException
	{
		if (owner == null || owner.getId().compareTo(0L)==0) throw new ArgumentException("Invalid owner");
		if(entitlement == null) throw new ArgumentException("Invalid entitlement object");
		/*
		if(entitlement.getNameType() == NameEnumType.PERMISSION && (targetObject == null || targetObject.getNameType() == NameEnumType.UNKNOWN)) throw new ArgumentException("Invalid target object");
		if(EnumUtils.isValidEnum(ApproverEnumType.class, entitlement.getNameType().toString()) == false) throw new ArgumentException("Invalid entitlement type");
		if(EnumUtils.isValidEnum(ApproverEnumType.class, approver.getNameType().toString()) == false) throw new ArgumentException("Invalid approver type");

		ApprovalType appr = new ApprovalType();
		appr.setNameType(NameEnumType.APPROVER);
		appr.setApprovalType(ApprovalEnumType.valueOf(approver.getNameType().toString()));
		appr.setApproverId(approver.getId());
		appr.setEntitlementType(ApproverEnumType.valueOf(entitlement.getNameType().toString()));
		appr.setEntitlementId(entitlement.getId());
		appr.setOrganizationId(owner.getOrganizationId());
		appr.setOwnerId(owner.getId());
		appr.setReferenceType((targetObject == null ? FactoryEnumType.UNKNOWN : FactoryEnumType.valueOf(targetObject.getNameType().toString())));
		appr.setReferenceId((targetObject == null ? 0L : targetObject.getId()));
		appr.setApproverLevel(level);
		appr.setApprovalType(approvalType);
		return appr;
		*/
		return null;
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		ApprovalType obj = (ApprovalType)object;
		DataRow row = prepareAdd(obj, this.primaryTableName);
		try{
			row.setCellValue(Columns.get(ColumnEnumType.REQUESTID),obj.getRequestId());
			row.setCellValue(Columns.get(ColumnEnumType.RESPONSE),obj.getResponse().toString());
			row.setCellValue(Columns.get(ColumnEnumType.RESPONSEMESSAGE), obj.getResponseMessage());
			row.setCellValue(Columns.get(ColumnEnumType.SIGNERID),obj.getSignerId());
			row.setCellValue(Columns.get(ColumnEnumType.VALIDATIONID),obj.getValidationId());
			row.setCellValue(Columns.get(ColumnEnumType.SIGNATURE), obj.getSignature());

			row.setCellValue(Columns.get(ColumnEnumType.REFERENCETYPE),obj.getReferenceType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.REFERENCEID),obj.getReferenceId());
			row.setCellValue(Columns.get(ColumnEnumType.APPROVALTYPE),obj.getApprovalType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.APPROVERTYPE),obj.getApproverType().toString());
			row.setCellValue(Columns.get(ColumnEnumType.APPROVERID),obj.getApproverId());
			row.setCellValue(Columns.get(ColumnEnumType.APPROVALID),obj.getApproverId());
			row.setCellValue(Columns.get(ColumnEnumType.APPROVERLEVEL),obj.getApproverLevel());
			
			row.setCellValue(Columns.get(ColumnEnumType.CREATEDDATE), obj.getCreatedDate());
			row.setCellValue(Columns.get(ColumnEnumType.MODIFIEDDATE), obj.getModifiedDate());
			row.setCellValue(Columns.get(ColumnEnumType.EXPIRATIONDATE), obj.getExpiryDate());
		
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
		ApprovalType newAppr = new ApprovalType();
		newAppr.setNameType(NameEnumType.APPROVAL);
		super.read(rset, newAppr);
		newAppr.setRequestId(rset.getString(Columns.get(ColumnEnumType.REQUESTID)));
		newAppr.setSignature(rset.getBytes(Columns.get(ColumnEnumType.SIGNATURE)));
		newAppr.setSignerId(rset.getString(Columns.get(ColumnEnumType.SIGNERID)));
		newAppr.setValidationId(rset.getString(Columns.get(ColumnEnumType.VALIDATIONID)));
		newAppr.setResponseMessage(rset.getString(Columns.get(ColumnEnumType.RESPONSEMESSAGE)));
		newAppr.setResponse(ApprovalResponseEnumType.valueOf(rset.getString(Columns.get(ColumnEnumType.RESPONSE))));
		newAppr.setReferenceId(rset.getLong(Columns.get(ColumnEnumType.REFERENCEID)));
		newAppr.setReferenceType(FactoryEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.REFERENCETYPE))));
		newAppr.setApproverId(rset.getLong(Columns.get(ColumnEnumType.APPROVERID)));
		newAppr.setApproverType(ApproverEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.APPROVERTYPE))));
		newAppr.setApprovalType(ApprovalEnumType.fromValue(rset.getString(Columns.get(ColumnEnumType.APPROVALTYPE))));
		newAppr.setApprovalId(rset.getLong(Columns.get(ColumnEnumType.APPROVALID)));
		newAppr.setApproverLevel(rset.getInt(Columns.get(ColumnEnumType.APPROVERLEVEL)));
		newAppr.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.CREATEDDATE))));
		newAppr.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.MODIFIEDDATE))));
		newAppr.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp(Columns.get(ColumnEnumType.EXPIRATIONDATE))));
		
		return newAppr;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ApprovalType useMap = (ApprovalType)map;
		
		fields.add(QueryFields.getFieldValidationId(useMap.getValidationId()));
		fields.add(QueryFields.getFieldSignerId(useMap.getSignerId()));
		fields.add(QueryFields.getFieldSignature(useMap.getSignature()));
		fields.add(QueryFields.getFieldRequestId(useMap.getRequestId()));
		fields.add(QueryFields.getFieldResponse(useMap.getResponse()));
		fields.add(QueryFields.getFieldResponseMessage(useMap.getResponseMessage()));
		
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(useMap.getReferenceType()));
		fields.add(QueryFields.getFieldApproverId(useMap.getApproverId()));
		fields.add(QueryFields.getFieldApprovalType(useMap.getApprovalType()));
		fields.add(QueryFields.getFieldApprovalId(useMap.getApprovalId()));
		fields.add(QueryFields.getFieldApproverLevel(useMap.getApproverLevel()));
		
		fields.add(QueryFields.getFieldModifiedDate(useMap.getModifiedDate()));
		fields.add(QueryFields.getFieldExpirationDate(useMap.getExpiryDate()));
		fields.add(QueryFields.getFieldCreatedDate(useMap.getCreatedDate()));
	}
	public ApprovalType getApproverByObjectId(String id, long organizationId) throws FactoryException, ArgumentException{
		List<NameIdType> sec = getByObjectId(id, organizationId);
		if(!sec.isEmpty()) return (ApprovalType)sec.get(0);
		return null;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ApprovalType data = (ApprovalType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	public <T> List<T> listApprovalsForRequest(AccessRequestType request) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();

		fields.add(QueryFields.getFieldRequestId(request.getObjectId()));
		
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(0L);
		pi.setRecordCount(0);
		pi.setOrderClause(Columns.get(ColumnEnumType.APPROVERLEVEL) + " ASC");
		
		return list(fields.toArray(new QueryField[0]), pi, request.getOrganizationId());

	}
	/*
	public List<ApprovalType> getApproversForType(NameIdType obj, NameIdType entitlement, int level, ApprovalEnumType approvalType) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		// allow id of 0 for global approver checks
		if(obj == null && entitlement == null) throw new ArgumentException("Either an object or entitlement must be specified");
		if((obj == null || obj.getNameType() == NameEnumType.UNKNOWN) && (entitlement.getNameType() == NameEnumType.PERMISSION)) throw new ArgumentException("Invalid object reference");
		if(obj != null) {
			fields.add(QueryFields.getFieldReferenceId(obj.getId()));
			fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		}
		if(approvalType != ApprovalEnumType.UNKNOWN) fields.add(QueryFields.getFieldApprovalType(approvalType));
		if(level > 0) fields.add(QueryFields.getFieldApproverLevel(level));
		if(entitlement != null) {
			fields.add(QueryFields.getFieldEntitlementId(entitlement.getId()));
			fields.add(QueryFields.getFieldEntitlementType(ApproverEnumType.valueOf(entitlement.getNameType().toString())));
		}
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setStartIndex(0L);
		pi.setRecordCount(2);
		pi.setOrderClause("approverlevel ASC");
		return list(fields.toArray(new QueryField[0]), pi, (obj == null ? entitlement : obj).getOrganizationId());
	}
	public boolean deleteApproversForType(NameIdType obj, NameIdType entitlement) throws FactoryException, ArgumentException{
		List<QueryField> fields = new ArrayList<>();
		if(obj == null && entitlement == null) return false;
		if(obj != null) {
			fields.add(QueryFields.getFieldReferenceType(obj.getNameType()));	
			fields.add(QueryFields.getFieldReferenceId(obj.getId()));
		}
		if(entitlement != null) {
			fields.add(QueryFields.getFieldEntitlementType(ApproverEnumType.valueOf(entitlement.getNameType().toString())));	
			fields.add(QueryFields.getFieldEntitlementId(entitlement.getId()));
		}
		return (this.deleteByField(fields.toArray(new QueryField[0]), (obj == null ? entitlement : obj).getOrganizationId()) > 0);
	}
	*/

	
}
