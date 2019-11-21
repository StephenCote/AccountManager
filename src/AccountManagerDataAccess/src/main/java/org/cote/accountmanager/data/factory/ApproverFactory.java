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

import org.apache.commons.lang3.EnumUtils;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ApprovalEnumType;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class ApproverFactory extends NameIdFactory {

	public ApproverFactory(){
		super();
		this.hasOwnerId = true;
		this.hasParentId=false;
		this.hasUrn = false;
		this.hasName = false;
		this.hasObjectId = true;
		this.aggressiveKeyFlush = false;
		this.useThreadSafeCollections = false;
		this.scopeToOrganization = true;
		this.primaryTableName = "approver";
		this.tableNames.add(primaryTableName);

		factoryType = FactoryEnumType.APPROVER;

	}
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		ApproverType cit = (ApproverType)map;
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
		ApproverType t = (ApproverType)obj;
		return t.getApproverType().toString() + "-" + t.getApproverLevel() + "-" + t.getReferenceType().toString() + "-" + t.getReferenceId()+ "-" + t.getEntitlementType().toString() + "-" + t.getEntitlementId();
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase(primaryTableName)){

		}
	}

	@Override
	public <T> boolean delete(T object) throws FactoryException, ArgumentException
	{
		ApproverType obj = (ApproverType)object;
		removeFromCache(obj);
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	
	public ApproverType newApprover(UserType owner, NameIdType targetObject, NameIdType entitlement, NameIdType approver, ApprovalEnumType approvalType, int level) throws ArgumentException
	{
		if (owner == null || owner.getId().compareTo(0L)==0) throw new ArgumentException("Invalid owner");
		if(entitlement == null) throw new ArgumentException("Invalid entitlement object");
		
		if(entitlement.getNameType() == NameEnumType.PERMISSION && (targetObject == null || targetObject.getNameType() == NameEnumType.UNKNOWN)) throw new ArgumentException("Invalid target object");
		if(EnumUtils.isValidEnum(ApproverEnumType.class, entitlement.getNameType().toString()) == false) throw new ArgumentException("Invalid entitlement type");
		if(EnumUtils.isValidEnum(ApproverEnumType.class, approver.getNameType().toString()) == false) throw new ArgumentException("Invalid approver type");

		ApproverType appr = new ApproverType();
		appr.setNameType(NameEnumType.APPROVER);
		appr.setApproverType(ApproverEnumType.valueOf(approver.getNameType().toString()));
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
	}
	
	@Override
	public <T> boolean add(T object) throws ArgumentException,FactoryException
	{
		ApproverType obj = (ApproverType)object;
		DataRow row = prepareAdd(obj, this.primaryTableName);
		try{

			row.setCellValue("referencetype",obj.getReferenceType().toString());
			row.setCellValue("referenceid",obj.getReferenceId());
			row.setCellValue("approvertype",obj.getApproverType().toString());
			row.setCellValue("approvaltype",obj.getApprovalType().toString());
			row.setCellValue("approverid",obj.getApproverId());
			row.setCellValue("entitlementtype",obj.getEntitlementType().toString());
			row.setCellValue("entitlementid",obj.getEntitlementId());
			row.setCellValue("approverlevel",obj.getApproverLevel());
		
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
		ApproverType newCred = new ApproverType();
		newCred.setNameType(NameEnumType.APPROVER);
		super.read(rset, newCred);
		
		newCred.setReferenceId(rset.getLong("referenceid"));
		newCred.setReferenceType(FactoryEnumType.fromValue(rset.getString("referencetype")));
		newCred.setApproverId(rset.getLong("approverid"));
		newCred.setApproverType(ApproverEnumType.fromValue(rset.getString("approvertype")));
		newCred.setApprovalType(ApprovalEnumType.fromValue(rset.getString("approvaltype")));
		newCred.setEntitlementId(rset.getLong("entitlementid"));
		newCred.setEntitlementType(ApproverEnumType.fromValue(rset.getString("entitlementtype")));
		newCred.setApproverLevel(rset.getInt("approverlevel"));
		
		return newCred;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ApproverType useMap = (ApproverType)map;
		fields.add(QueryFields.getFieldReferenceId(useMap.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(useMap.getReferenceType()));
		fields.add(QueryFields.getFieldApproverId(useMap.getApproverId()));
		fields.add(QueryFields.getFieldApproverType(useMap.getApproverType()));
		fields.add(QueryFields.getFieldApprovalType(useMap.getApprovalType()));
		fields.add(QueryFields.getFieldEntitlementId(useMap.getEntitlementId()));
		fields.add(QueryFields.getFieldEntitlementType(useMap.getEntitlementType()));
		fields.add(QueryFields.getFieldApproverLevel(useMap.getApproverLevel()));
	}
	public ApproverType getApproverByObjectId(String id, long organizationId) throws FactoryException, ArgumentException{
		List<NameIdType> sec = getByObjectId(id, organizationId);
		if(!sec.isEmpty()) return (ApproverType)sec.get(0);
		return null;
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		ApproverType data = (ApproverType)object;
		removeFromCache(data);
		return super.update(data, null);
	}
	
	public List<ApproverType> getApproversForType(NameIdType obj, NameIdType entitlement, int level, ApprovalEnumType approvalType) throws FactoryException, ArgumentException{
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

	
}
