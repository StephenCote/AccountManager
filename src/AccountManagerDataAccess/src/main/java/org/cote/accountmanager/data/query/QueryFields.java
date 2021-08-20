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
package org.cote.accountmanager.data.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.factory.FieldMap;
import org.cote.accountmanager.objects.ApprovalEnumType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.CompressionEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.ContactInformationEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.SpoolStatusEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.StatisticsEnumType;
import org.cote.accountmanager.objects.types.TagEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;

public class QueryFields {
	public static final Logger logger = LogManager.getLogger(QueryFields.class);
	

	
	public QueryFields(){
		
	}
	public static QueryField getBooleanField(String name, boolean val)
	{
		QueryField of = new QueryField(SqlDataEnumType.BOOLEAN, name, val);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}
	public static QueryField getBigIntField(String name, long val)
	{
		QueryField of = new QueryField(SqlDataEnumType.BIGINT, name, val);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}
	public static QueryField getIntField(String name, int val)
	{
		QueryField of = new QueryField(SqlDataEnumType.INTEGER, name, val);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}
	public static QueryField getDoubleField(String name, double val)
	{
		QueryField of = new QueryField(SqlDataEnumType.DOUBLE, name, val);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}
	public static QueryField getStringField(String name, String val)
	{
		QueryField of = new QueryField(SqlDataEnumType.VARCHAR, name, val);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}
	public static QueryField getBytesField(String name, byte[] val)
	{
		QueryField of = new QueryField(SqlDataEnumType.BLOB, name, val);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}	
	public static QueryField getTimestampField(String name, XMLGregorianCalendar val)
	{
		QueryField of = new QueryField(SqlDataEnumType.TIMESTAMP, name, val);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}
	public static QueryField getFieldFactoryType(FactoryEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.FACTORYTYPE), type.toString());
	}
	public static QueryField getFieldFactType(FactEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.FACTTYPE), type.toString());
	}
	public static QueryField getFieldRuleType(RuleEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.RULETYPE), type.toString());
	}
	public static QueryField getFieldUrn(NameIdType obj)
	{
		return getFieldUrn(obj.getUrn());
	}
	public static QueryField getFieldUrn(String urn)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.URN), urn);
	}
	public static QueryField getFieldScore(int score)
	{
		return getIntField(FieldMap.Columns.get(ColumnEnumType.SCORE), score);
	}

	public static QueryField getFieldFactData(String factData)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.FACTDATA), factData);
	}

	public static QueryField getFieldFunctionUrn(String urn)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.FUNCTIONURN), urn);
	}
	public static QueryField getFieldFunctionType(FunctionEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.FUNCTIONTYPE), type.toString());
	}

	public static QueryField getFieldOperation(String op)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.OPERATION), op);
	}
	public static QueryField getFieldOperationType(OperationEnumType op)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.OPERATIONTYPE), op.toString());
	}
	public static QueryField getFieldPatternType(PatternEnumType op)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.PATTERNTYPE), op.toString());
	}
	public static QueryField getFieldComparatorType(ComparatorEnumType op)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.COMPARATOR), op.toString());
	}
	public static QueryField getFieldCondition(ConditionEnumType op)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CONDITION), op.toString());
	}

	public static QueryField getFieldMatchUrn(String urn)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.MATCHURN), urn);
	}
	public static QueryField getFieldOperationUrn(String urn)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.OPERATIONURN), urn);
	}

	public static QueryField getFieldFactUrn(String urn)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.FACTURN), urn);
	}
	public static QueryField getFieldSourceUrn(String urn)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SOURCEURN), urn);
	}
	public static QueryField getFieldSourceUrl(String url)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SOURCEURL), url);
	}
	public static QueryField getFieldSourceType(String type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SOURCETYPE), type);
	}

	public static QueryField getFieldSourceDataType(SqlDataEnumType dataType)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SOURCEDATATYPE), dataType.toString());
	}

	public static QueryField getFieldPreferred(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.PREFERRED), val);
	}

	public static QueryField getFieldIsVaulted(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISVAULTED), val);
	}
	public static QueryField getFieldVaultId(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.VAULTID), val);
	}
	public static QueryField getFieldKeyId(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.KEYID), val);
	}
	public static QueryField getFieldMimeType(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.MIMETYPE), val);
	}
	public static QueryField getFieldEnabled(boolean enabled)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ENABLED), enabled);
	}

	public static QueryField getFieldGroup(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.GROUPID), id);
	}
	public static QueryField getFieldDataBlob(byte[] val)
	{
		return getBytesField(FieldMap.Columns.get(ColumnEnumType.DATABLOB), val);
	}
	public static QueryField getFieldDataString(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.DATASTRING), val);
	}
	public static QueryField getFieldDimensions(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.DIMENSIONS), val);
	}
	public static QueryField getFieldCompressionType(CompressionEnumType ctype) {
		return getStringField(FieldMap.Columns.get(ColumnEnumType.COMPRESSIONTYPE),ctype.toString());
	}
	public static QueryField getFieldCompressed(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISCOMPRESSED), val);
	}
	public static QueryField getFieldBlob(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISBLOB), val);
	}
	public static QueryField getFieldEnciphered(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISENCIPHERED), val);
	}
	public static QueryField getFieldPasswordProtected(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISPASSWORDPROTECTED), val);
	}

	public static QueryField getFieldVaulted(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISVAULTED), val);
	}
	public static QueryField getFieldPointer(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISPOINTER), val);
	}
	public static QueryField getFieldSize(int val)
	{
		return getIntField(FieldMap.Columns.get(ColumnEnumType.SIZE), val);
	}
	public static QueryField getFieldRating(double val)
	{
		return getDoubleField(FieldMap.Columns.get(ColumnEnumType.RATING), val);
	}
	public static QueryField getFieldDataHash(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.HASH), val);
	}
	public static QueryField getFieldHashProvider(String hash)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.HASHPROVIDER),hash);
	}
	public static QueryField getFieldCreatedDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.CREATEDDATE), val);
	}
	public static QueryField getFieldStartDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.STARTDATE), val);
	}

	public static QueryField getFieldModifiedDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.MODIFIEDDATE), val);
	}
	public static QueryField getFieldExpirationDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.EXPIRATIONDATE), val);
	}
	public static QueryField getFieldAccessedDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.ACCESSEDDATE), val);
	}
	public static QueryField getFieldStartTime(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.STARTTIME), val);
	}

	public static QueryField getFieldEndTime(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.ENDTIME), val);
	}
	public static QueryField getFieldApproverLevel(int level)
	{
		return getIntField(FieldMap.Columns.get(ColumnEnumType.APPROVERLEVEL), level);
	}
	public static QueryField getFieldApprovalType(ApprovalEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.APPROVALTYPE), type.toString());
	}
	public static QueryField getFieldApproverType(ApproverEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.APPROVERTYPE), type.toString());
	}
	public static QueryField getFieldApproverId(NameIdType obj)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.APPROVERID), (obj != null ? obj.getId() : 0));
	}
	public static QueryField getFieldApproverId(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.APPROVERID), id);
	}
	public static QueryField getFieldApprovalId(NameIdType obj)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.APPROVALID), (obj != null ? obj.getId() : 0));
	}
	public static QueryField getFieldApprovalId(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.APPROVALID), id);
	}
	public static QueryField getFieldResponse(ApprovalResponseEnumType art)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.RESPONSE), art.toString());
	}
	public static QueryField getFieldResponseMessage(String msg)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.RESPONSEMESSAGE), msg);
	}
	public static QueryField getFieldRequestId(String objId)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REQUESTID), objId);
	}
	public static QueryField getFieldSignerId(String objId)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SIGNERID), objId);
	}
	public static QueryField getFieldValidationId(String objId)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.VALIDATIONID), objId);
	}
	public static QueryField getFieldSignature(byte[] data)
	{
		return getBytesField(FieldMap.Columns.get(ColumnEnumType.SIGNATURE), data);
	}
	public static QueryField getFieldEntitlementType(ApproverEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ENTITLEMENTTYPE), type.toString());
	}
	public static QueryField getFieldEntitlementId(NameIdType obj)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ENTITLEMENTID), (obj != null ? obj.getId() : 0));
	}
	public static QueryField getFieldEntitlementId(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ENTITLEMENTID), id);
	}
	public static QueryField getFieldRequestorType(ApproverEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REQUESTORTYPE), type.toString());
	}
	public static QueryField getFieldApprovalStatus(ApprovalResponseEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.APPROVALSTATUS), type.toString());
	}
	public static QueryField getFieldRequestorId(NameIdType obj)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.REQUESTORID), (obj != null ? obj.getId() : 0));
	}
	public static QueryField getFieldRequestorId(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.REQUESTORID), id);
	}
	public static QueryField getFieldDelegateType(ApproverEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.DELEGATETYPE), type.toString());
	}
	public static QueryField getFieldDelegateId(NameIdType obj)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.DELEGATEID), (obj != null ? obj.getId() : 0));
	}
	public static QueryField getFieldDelegateId(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.DELEGATEID), id);
	}

	public static QueryField getFieldOrganization(long id)
	{
		QueryField of =  new QueryField(SqlDataEnumType.BIGINT, FieldMap.Columns.get(ColumnEnumType.ORGANIZATIONID), id);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}	
	public static QueryField getFieldId(NameIdType map)
	{
		return getFieldId(map.getId());
	}
	public static QueryField getFieldId(long id)
	{
		QueryField of =  new QueryField(SqlDataEnumType.BIGINT, FieldMap.Columns.get(ColumnEnumType.ID), id);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}
	public static QueryField getFieldObjectId(NameIdType map)
	{
		return getFieldObjectId(map.getObjectId());
	}
	public static QueryField getFieldObjectId(String objId)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.OBJECTID),objId);
	}
	public static QueryField getFieldName(NameIdType map)
	{
		return getFieldName(map.getName());
	}

	public static QueryField getFieldName(String name)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.NAME),name);
	}
	public static QueryField getFieldParent(NameIdType map)
	{
		return getFieldParent(map.getParentId());
	}
	public static QueryField getFieldParent(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.PARENTID),id);
	}
	public static QueryField getFieldOwner(NameIdType map)
	{
		return getFieldOwner(map.getOwnerId());
	}
	public static QueryField getFieldOwner(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.OWNERID),id);
	}
	public static QueryField getFieldReferenceId(long val)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.REFERENCEID), val);
	}		
	public static QueryField getFieldDescription(String desc)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.DESCRIPTION), desc);
	}
	
	public static QueryField getFieldLogicalId(OrganizationType map)
	{
		return getFieldLogicalId(map.getLogicalId());
	}
	public static QueryField getFieldLogicalId(long id)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.LOGICALID), id);
	}
	public static QueryField getFieldLogicalOrder(int order){
		return getIntField(FieldMap.Columns.get(ColumnEnumType.LOGICALORDER), order);
	}
	public static QueryField getFieldDecisionAge(long age)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.DECISIONAGE),age);
	}

	public static QueryField getFieldReferenceId(OrganizationType map)
	{
		return getFieldReferenceId(map.getLogicalId());
	}

	public static QueryField getFieldOrganizationType(OrganizationType map)
	{
		return getFieldOrganizationType(map.getOrganizationType());
	}
	public static QueryField getFieldOrganizationType(OrganizationEnumType orgType)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ORGANIZATIONTYPE), orgType.toString());
	}

	public static QueryField getFieldGroupType(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.GROUPTYPE), val);
	}
	public static QueryField getFieldGroupType(GroupEnumType groupType)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.GROUPTYPE), groupType.toString());
	}
	public static QueryField getFieldContactInformationType(ContactInformationEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CONTACTINFORMATIONTYPE), type.toString());
	}
	public static QueryField getFieldContactInformationId(ContactInformationType obj)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.CONTACTINFORMATIONID), (obj != null ? obj.getId() : 0));
	}
	public static QueryField getFieldWebsite(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.WEBSITE), val);
	}
	public static QueryField getFieldTitle(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.TITLE), val);
	}
	public static QueryField getFieldState(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.STATE), val);
	}
	public static QueryField getFieldCountry(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.COUNTRY), val);
	}
	public static QueryField getFieldCity(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CITY), val);
	}
	public static QueryField getFieldPostalCode(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.POSTALCODE), val);
	}
	public static QueryField getFieldPhone(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.PHONE), val);
	}
	public static QueryField getFieldMiddleName(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.MIDDLENAME), val);
	}
	public static QueryField getFieldLastName(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.LASTNAME), val);
	}
	public static QueryField getFieldFirstName(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.FIRSTNAME), val);
	}
	public static QueryField getFieldAlias(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ALIAS), val);
	}
	public static QueryField getFieldPrefix(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.PREFIX), val);
	}
	public static QueryField getFieldRegion(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REGION), val);
	}
	public static QueryField getFieldLocationType(LocationEnumType val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.LOCATIONTYPE), val.toString());
	}
	public static QueryField getFieldContactType(ContactEnumType val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CONTACTTYPE), val.toString());
	}
	public static QueryField getFieldContactValue(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CONTACTVALUE), val);
	}
	public static QueryField getFieldSuffix(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SUFFIX), val);
	}

	public static QueryField getFieldGender(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.GENDER), val);
	}
	public static QueryField getFieldFax(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.FAX), val);
	}
	public static QueryField getFieldEmail(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.EMAIL), val);
	}
	public static QueryField getFieldBirthDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.BIRTHDATE), val);
	}
	public static QueryField getFieldAddressLine1(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ADDRESSLINE_1), val);
	}
	public static QueryField getFieldAddressLine2(String val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ADDRESSLINE_2), val);
	}
	public static QueryField getFieldStatisticsType(StatisticsEnumType val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.STATISTICSTYPE), val.toString());
	}

	public static QueryField getFieldAffectId(BaseParticipantType participant)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.AFFECTID), participant.getAffectId());
	}
	public static QueryField getFieldAffectId(BasePermissionType permission)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.AFFECTID), permission.getId());
	}
	public static QueryField getFieldAffectType(BaseParticipantType participant)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.AFFECTTYPE), participant.getAffectType().toString());
	}
	public static QueryField getFieldAffectType(AffectEnumType affectType)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.AFFECTTYPE), affectType.toString());
	}
	public static QueryField getFieldParticipantId(NameIdType map)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.PARTICIPANTID), map.getId());
	}

	public static QueryField getFieldParticipantId(BaseParticipantType participant)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.PARTICIPANTID), participant.getParticipantId());
	}
	public static QueryField getFieldParticipationId(NameIdType map)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.PARTICIPATIONID), map.getId());
	}
	public static QueryField getFieldParticipationId(BaseParticipantType participant)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.PARTICIPATIONID), participant.getParticipationId());
	}
	public static QueryField getFieldParticipantType(ParticipantEnumType participantType)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.PARTICIPANTTYPE), participantType.toString());
	}

	public static QueryField getFieldRoleType(RoleEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ROLETYPE),type.toString());
	}
	public static QueryField getFieldPermissionType(PermissionEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.PERMISSIONTYPE), type.toString());
	}
	public static QueryField getFieldSessionId(String id){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SESSIONID), id);
	}
	public static QueryField getFieldUserId(long id){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.USERID), id);
	}
	public static QueryField getFieldDataId(long id){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.DATAID), id);
	}
	public static QueryField getFieldRoleId(long id){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ROLEID), id);
	}
	public static QueryField getFieldData(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.DATA), val);
	}
	public static QueryField getFieldSecurityId(String id){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SECURITYID), id);
	}
	public static QueryField getFieldSessionCreated(XMLGregorianCalendar val){
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.SESSIONCREATED), val);
	}
	public static QueryField getFieldSessionExpiration(XMLGregorianCalendar val){
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.SESSIONEXPIRATION), val);
	}
	public static QueryField getFieldExpiration(XMLGregorianCalendar val){
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.EXPIRATION), val);
	}

	public static QueryField getFieldSessionAccessed(XMLGregorianCalendar val){
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.SESSIONACCESSED), val);
	}
	public static QueryField getFieldSessionStatus(SessionStatusEnumType val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SESSIONSTATUS), val.toString());
	}
	public static QueryField getFieldSessionDataSize(int val)
	{
		return getIntField(FieldMap.Columns.get(ColumnEnumType.SESSIONDATASIZE), val);
	}

	public static QueryField getFieldGuid(String guid)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.GUID), guid);
	}
	public static QueryField getFieldClassification(String cls)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CLASSIFICATION), cls);
	}
	public static QueryField getFieldCurrentLevel(int level)
	{
		return getIntField(FieldMap.Columns.get(ColumnEnumType.CURRENTLEVEL), level);
	}
	public static QueryField getFieldEndLevel(int level)
	{
		return getIntField(FieldMap.Columns.get(ColumnEnumType.ENDLEVEL), level);
	}
	public static QueryField getFieldParentGuid(String guid)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.PARENTGUID), guid);
	}
	
	public static QueryField getFieldExpires(boolean val)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.EXPIRES), val);
	}
	public static QueryField getFieldSpoolStatus(SpoolStatusEnumType val)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SPOOLSTATUS), val.toString());
	}
	public static QueryField getFieldSpoolBucketName(SpoolNameEnumType spoolType){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SPOOLBUCKETNAME),spoolType.toString());
	}
	public static QueryField getFieldSpoolBucketType(SpoolBucketEnumType spoolType){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SPOOLBUCKETTYPE),spoolType.toString());
	}
	public static QueryField getFieldSpoolData(byte[] data){
		return getBytesField(FieldMap.Columns.get(ColumnEnumType.SPOOLDATA),data);
	}
	public static QueryField getFieldSpoolValueType(ValueEnumType spoolType){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SPOOLVALUETYPE),spoolType.toString());
	}
	public static QueryField getFieldAuditSourceType(AuditEnumType auditType){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.AUDITSOURCETYPE),auditType.toString());
	}
	public static QueryField getFieldAuditSourceData(String auditData){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.AUDITSOURCEDATA),auditData);
	}
	public static QueryField getFieldAuditTargetType(AuditEnumType auditType){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.AUDITTARGETTYPE),auditType.toString());
	}
	public static QueryField getFieldAuditTargetData(String auditData){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.AUDITTARGETDATA),auditData);
	}
	public static QueryField getFieldActionType(ActionEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ACTIONTYPE),type.toString());
	}
	public static QueryField getFieldReferenceType(FactoryEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REFERENCETYPE),type.toString());
	}
	public static QueryField getFieldReferenceType(NameEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REFERENCETYPE),type.toString());
	}
	public static QueryField getFieldControlType(ControlEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CONTROLTYPE),type.toString());
	}
	public static QueryField getFieldControlId(long id){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.CONTROLID),id);
	}
	public static QueryField getFieldControlAction(ControlActionEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CONTROLACTION),type.toString());
	}
	public static QueryField getFieldGlobalKey(boolean b)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.GLOBALKEY), b);
	}
	public static QueryField getFieldOrganizationKey(boolean b)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ORGANIZATIONKEY), b);
	}
	public static QueryField getFieldPrimaryCredential(boolean b)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.PRIMARYCREDENTIAL), b);
	}
	public static QueryField getFieldPrimaryKey(boolean b)
	{
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.PRIMARYKEY), b);
	}
	public static QueryField getFieldCredentialId(long b)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.CREDENTIALID), b);
	}
	public static QueryField getFieldRecipientId(long b)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.RECIPIENTID), b);
	}
	public static QueryField getFieldTransportId(long b)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.TRANSPORTID), b);
	}
	public static QueryField getFieldRecipientType(FactoryEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.RECIPIENTTYPE), type.toString());
	}
	public static QueryField getFieldTransportType(FactoryEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.TRANSPORTTYPE), type.toString());
	}
	public static QueryField getFieldPreviousKeyId(long b)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.PREVIOUSKEYID), b);
	}
	public static QueryField getFieldAsymmetricKeyId(long b)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ASYMMETRICKEYID), b);
	}
	public static QueryField getFieldSymmetricKeyId(long b)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.SYMMETRICKEYID), b);
	}
	public static QueryField getFieldPreviousCredentialId(long id){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.PREVIOUSCREDENTIALID),id);
	}
	public static QueryField getFieldNextCredentialId(long id){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.NEXTCREDENTIALID),id);
	}
	public static QueryField getFieldCredentialType(CredentialEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CREDENTIALTYPE),type.toString());
	}
	public static QueryField getFieldCredential(byte[] val)
	{
		return getBytesField(FieldMap.Columns.get(ColumnEnumType.CREDENTIAL), val);
	}
	public static QueryField getFieldSalt(byte[] val)
	{
		return getBytesField(FieldMap.Columns.get(ColumnEnumType.SALT), val);
	}

	public static QueryField getFieldReferenceType(SqlDataEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.DATATYPE),type.toString());
	}
	public static QueryField getFieldIndex(int index){
		return getIntField(FieldMap.Columns.get(ColumnEnumType.VALUEINDEX),index);
	}
	public static QueryField getFieldValue(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.VALUE),val);
	}
	public static QueryField getFieldAccountId(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ACCOUNTID),val);
	}
	public static QueryField getFieldAccountStatus(AccountStatusEnumType val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ACCOUNTSTATUS),val.toString());
	}
	public static QueryField getFieldAccountType(AccountEnumType val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ACCOUNTTYPE),val.toString());
	}


	public static QueryField[] getFieldParticipantMatch(NameIdType map, ParticipantEnumType type){
		List<QueryField> matches = new ArrayList<>();
		matches.add(getFieldParticipantType(type));
		matches.add(getFieldParticipantId(map));
		return matches.toArray(new QueryField[0]);
	}
	public static QueryField[] getFieldParticipationMatch(NameIdType map, ParticipantEnumType type){
		List<QueryField> matches = new ArrayList<>();
		matches.add(getFieldParticipantType(type));
		matches.add(getFieldParticipationId(map));
		return matches.toArray(new QueryField[0]);
	}
	public static QueryField[] getFieldParticipantsMatch(NameIdType participation, NameIdType participant, ParticipantEnumType participantType, BasePermissionType permission, AffectEnumType affectType){
		List<QueryField> fields = new ArrayList<>();

		fields.add(QueryFields.getFieldParticipantId(participant));
		fields.add(QueryFields.getFieldParticipantType(participantType));
		fields.add(QueryFields.getFieldParticipationId(participation));
		if (permission != null)
		{
			fields.add(QueryFields.getFieldAffectType(affectType));
			fields.add(QueryFields.getFieldAffectId(permission));
		}
		return fields.toArray(new QueryField[0]);
	}
	public static <T> QueryField getFieldParticipantIds(T[] list)
	{
		if (list.length == 0) return null;
		List<Long> ints = new ArrayList<>();
		for (int i = 0; i < list.length; i++)
		{
			ints.add(((BaseParticipantType)list[i]).getParticipantId());
		}

		QueryField match = new QueryField(SqlDataEnumType.BIGINT, FieldMap.Columns.get(ColumnEnumType.ID), getFilteredLongList(FactoryBase.convertLongList(ints)));
		match.setComparator(ComparatorEnumType.ANY);
		return match;
	}
	public static <T> QueryField getFieldParticipationIds(T[] list)
	{
		if (list.length == 0) return null;
		List<Long> ints = new ArrayList<>();
		for (int i = 0; i < list.length; i++)
		{
			ints.add(((BaseParticipantType)list[i]).getParticipationId());
		}

		QueryField match = new QueryField(SqlDataEnumType.BIGINT, FieldMap.Columns.get(ColumnEnumType.ID), getFilteredLongList(FactoryBase.convertLongList(ints)));
		match.setComparator(ComparatorEnumType.ANY);
		return match;
	}
	public static String getFilteredLongList(long[] longs){
		StringBuilder buff = new StringBuilder();
		Map<Long,Boolean> idMap = new HashMap<>();
		int counter = 0;
		for(int i = 0; i < longs.length; i++){
			if(idMap.containsKey(longs[i])) continue;
			if(counter > 0) buff.append(",");
			buff.append(longs[i]);
			idMap.put(longs[i], true);
			counter++;
		}
		return buff.toString();
	}
	public static String getFilteredIntList(int[] ints){
		StringBuilder buff = new StringBuilder();
		Map<Integer,Boolean> idMap = new HashMap<>();
		int counter = 0;
		for(int i = 0; i < ints.length; i++){
			if(idMap.containsKey(ints[i])) continue;
			if(counter > 0) buff.append(",");
			buff.append(ints[i]);
			idMap.put(ints[i], true);
			counter++;
		}
		return buff.toString();
	}
	public static QueryField getFieldTagType(TagEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.TAGTYPE), type.toString());
	}
	
}
