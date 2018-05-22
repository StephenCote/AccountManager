/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
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
		return getStringField("factorytype", type.toString());
	}
	public static QueryField getFieldFactType(FactEnumType type)
	{
		return getStringField("facttype", type.toString());
	}
	public static QueryField getFieldRuleType(RuleEnumType type)
	{
		return getStringField("ruletype", type.toString());
	}
	public static QueryField getFieldUrn(NameIdType obj)
	{
		return getFieldUrn(obj.getUrn());
	}
	public static QueryField getFieldUrn(String urn)
	{
		return getStringField("urn", urn);
	}
	public static QueryField getFieldScore(int score)
	{
		return getIntField("score", score);
	}

	public static QueryField getFieldFactData(String factData)
	{
		return getStringField("factdata", factData);
	}

	public static QueryField getFieldFunctionUrn(String urn)
	{
		return getStringField("functionurn", urn);
	}
	public static QueryField getFieldFunctionType(FunctionEnumType type)
	{
		return getStringField("functiontype", type.toString());
	}

	public static QueryField getFieldOperation(String op)
	{
		return getStringField("operation", op);
	}
	public static QueryField getFieldOperationType(OperationEnumType op)
	{
		return getStringField("operationtype", op.toString());
	}
	public static QueryField getFieldPatternType(PatternEnumType op)
	{
		return getStringField("patterntype", op.toString());
	}
	public static QueryField getFieldComparatorType(ComparatorEnumType op)
	{
		return getStringField("comparator", op.toString());
	}
	public static QueryField getFieldCondition(ConditionEnumType op)
	{
		return getStringField("condition", op.toString());
	}

	public static QueryField getFieldMatchUrn(String urn)
	{
		return getStringField("matchurn", urn);
	}
	public static QueryField getFieldOperationUrn(String urn)
	{
		return getStringField("operationurn", urn);
	}

	public static QueryField getFieldFactUrn(String urn)
	{
		return getStringField("facturn", urn);
	}
	public static QueryField getFieldSourceUrn(String urn)
	{
		return getStringField("sourceurn", urn);
	}
	public static QueryField getFieldSourceUrl(String url)
	{
		return getStringField("sourceurl", url);
	}
	public static QueryField getFieldSourceType(String type)
	{
		return getStringField("sourcetype", type);
	}

	public static QueryField getFieldSourceDataType(SqlDataEnumType dataType)
	{
		return getStringField("sourcedatatype", dataType.toString());
	}

	public static QueryField getFieldPreferred(boolean val)
	{
		return getBooleanField("preferred", val);
	}

	public static QueryField getFieldIsVaulted(boolean val)
	{
		return getBooleanField("isvaulted", val);
	}
	public static QueryField getFieldVaultId(String val)
	{
		return getStringField("vaultid", val);
	}
	public static QueryField getFieldKeyId(String val)
	{
		return getStringField("keyid", val);
	}
	public static QueryField getFieldMimeType(String val)
	{
		return getStringField("mimetype", val);
	}
	public static QueryField getFieldEnabled(boolean enabled)
	{
		return getBooleanField("enabled", enabled);
	}

	public static QueryField getFieldGroup(long id)
	{
		return getBigIntField("groupid", id);
	}
	public static QueryField getFieldDataBlob(byte[] val)
	{
		return getBytesField("datablob", val);
	}
	public static QueryField getFieldDataString(String val)
	{
		return getStringField("datastring", val);
	}
	public static QueryField getFieldDimensions(String val)
	{
		return getStringField("dimensions", val);
	}
	public static QueryField getFieldCompressed(boolean val)
	{
		return getBooleanField("iscompressed", val);
	}
	public static QueryField getFieldBlob(boolean val)
	{
		return getBooleanField("isblob", val);
	}
	public static QueryField getFieldEnciphered(boolean val)
	{
		return getBooleanField("isenciphered", val);
	}
	public static QueryField getFieldPasswordProtected(boolean val)
	{
		return getBooleanField("ispasswordprotected", val);
	}

	public static QueryField getFieldVaulted(boolean val)
	{
		return getBooleanField("isvaulted", val);
	}
	public static QueryField getFieldPointer(boolean val)
	{
		return getBooleanField("ispointer", val);
	}
	public static QueryField getFieldSize(int val)
	{
		return getIntField("size", val);
	}
	public static QueryField getFieldRating(double val)
	{
		return getDoubleField("rating", val);
	}
	public static QueryField getFieldDataHash(String val)
	{
		return getStringField("hash", val);
	}
	public static QueryField getFieldHashProvider(String hash)
	{
		return getStringField("hashprovider",hash);
	}
	public static QueryField getFieldCreatedDate(XMLGregorianCalendar val)
	{
		return getTimestampField("createddate", val);
	}
	public static QueryField getFieldStartDate(XMLGregorianCalendar val)
	{
		return getTimestampField("startdate", val);
	}

	public static QueryField getFieldModifiedDate(XMLGregorianCalendar val)
	{
		return getTimestampField("modifieddate", val);
	}
	public static QueryField getFieldExpirationDate(XMLGregorianCalendar val)
	{
		return getTimestampField("expirationdate", val);
	}
	public static QueryField getFieldAccessedDate(XMLGregorianCalendar val)
	{
		return getTimestampField("accesseddate", val);
	}
	public static QueryField getFieldStartTime(XMLGregorianCalendar val)
	{
		return getTimestampField("starttime", val);
	}

	public static QueryField getFieldEndTime(XMLGregorianCalendar val)
	{
		return getTimestampField("endtime", val);
	}
/*
	public static QueryField getFieldOrganization(NameIdType map)
	{
		return getFieldOrganization((map.getOrganization() != null ? map.getOrganization().getId() : 0));
	}
*/
	public static QueryField getFieldOrganization(long id)
	{
		QueryField of =  new QueryField(SqlDataEnumType.BIGINT, "organizationid", id);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}	
	public static QueryField getFieldId(NameIdType map)
	{
		return getFieldId(map.getId());
	}
	public static QueryField getFieldId(long id)
	{
		QueryField of =  new QueryField(SqlDataEnumType.BIGINT, "id", id);
		of.setComparator(ComparatorEnumType.EQUALS);
		return of;
	}
	public static QueryField getFieldObjectId(NameIdType map)
	{
		return getFieldObjectId(map.getObjectId());
	}
	public static QueryField getFieldObjectId(String objId)
	{
		return getStringField("objectid",objId);
	}
	public static QueryField getFieldName(NameIdType map)
	{
		return getFieldName(map.getName());
	}

	public static QueryField getFieldName(String name)
	{
		return getStringField("name",name);
	}
	public static QueryField getFieldParent(NameIdType map)
	{
		return getFieldParent(map.getParentId());
	}
	public static QueryField getFieldParent(long id)
	{
		return getBigIntField("parentid",id);
	}
	public static QueryField getFieldOwner(NameIdType map)
	{
		return getFieldOwner(map.getOwnerId());
	}
	public static QueryField getFieldOwner(long id)
	{
		return getBigIntField("ownerid",id);
	}
	public static QueryField getFieldReferenceId(long val)
	{
		return getBigIntField("referenceid", val);
	}		
	public static QueryField getFieldDescription(String desc)
	{
		return getStringField("description", desc);
	}
	
	public static QueryField getFieldLogicalId(OrganizationType map)
	{
		return getFieldLogicalId(map.getLogicalId());
	}
	public static QueryField getFieldLogicalId(long id)
	{
		return getBigIntField("logicalid", id);
	}
	public static QueryField getFieldLogicalOrder(int order){
		return getIntField("logicalorder", order);
	}
	public static QueryField getFieldDecisionAge(long age)
	{
		return getBigIntField("decisionage",age);
	}

	public static QueryField getFieldReferenceId(OrganizationType map)
	{
		return getFieldReferenceId(map.getLogicalId());
	}

	public static QueryField getFieldOrganizationType(OrganizationType map)
	{
		return getFieldOrganizationType(map.getOrganizationType());
	}
	public static QueryField getFieldOrganizationType(OrganizationEnumType org_type)
	{
		return getStringField("organizationtype", org_type.toString());
	}

	public static QueryField getFieldGroupType(String val)
	{
		return getStringField("grouptype", val);
	}
	public static QueryField getFieldGroupType(GroupEnumType group_type)
	{
		return getStringField("grouptype", group_type.toString());
	}
	public static QueryField getFieldContactInformationType(ContactInformationEnumType type)
	{
		return getStringField("contactinformationtype", type.toString());
	}
	public static QueryField getFieldContactInformationId(ContactInformationType obj)
	{
		return getBigIntField("contactinformationid", (obj != null ? obj.getId() : 0));
	}
	public static QueryField getFieldWebsite(String val)
	{
		return getStringField("website", val);
	}
	public static QueryField getFieldTitle(String val)
	{
		return getStringField("title", val);
	}
	public static QueryField getFieldState(String val)
	{
		return getStringField("state", val);
	}
	public static QueryField getFieldCountry(String val)
	{
		return getStringField("country", val);
	}
	public static QueryField getFieldCity(String val)
	{
		return getStringField("city", val);
	}
	public static QueryField getFieldPostalCode(String val)
	{
		return getStringField("postalcode", val);
	}
	public static QueryField getFieldPhone(String val)
	{
		return getStringField("phone", val);
	}
	public static QueryField getFieldMiddleName(String val)
	{
		return getStringField("middlename", val);
	}
	public static QueryField getFieldLastName(String val)
	{
		return getStringField("lastname", val);
	}
	public static QueryField getFieldFirstName(String val)
	{
		return getStringField("firstname", val);
	}
	public static QueryField getFieldAlias(String val)
	{
		return getStringField("alias", val);
	}
	public static QueryField getFieldPrefix(String val)
	{
		return getStringField("prefix", val);
	}
	public static QueryField getFieldRegion(String val)
	{
		return getStringField("region", val);
	}
	public static QueryField getFieldLocationType(LocationEnumType val)
	{
		return getStringField("locationtype", val.toString());
	}
	public static QueryField getFieldContactType(ContactEnumType val)
	{
		return getStringField("contacttype", val.toString());
	}
	public static QueryField getFieldContactValue(String val)
	{
		return getStringField("contactvalue", val);
	}
	public static QueryField getFieldSuffix(String val)
	{
		return getStringField("suffix", val);
	}

	public static QueryField getFieldGender(String val)
	{
		return getStringField("gender", val);
	}
	public static QueryField getFieldFax(String val)
	{
		return getStringField("fax", val);
	}
	public static QueryField getFieldEmail(String val)
	{
		return getStringField("email", val);
	}
	public static QueryField getFieldBirthDate(XMLGregorianCalendar val)
	{
		return getTimestampField("birthdate", val);
	}
	public static QueryField getFieldAddressLine1(String val)
	{
		return getStringField("addressline1", val);
	}
	public static QueryField getFieldAddressLine2(String val)
	{
		return getStringField("addressline2", val);
	}
	public static QueryField getFieldStatisticsType(StatisticsEnumType val){
		return getStringField("statisticstype", val.toString());
	}

	public static QueryField getFieldAffectId(BaseParticipantType participant)
	{
		return getBigIntField("affectid", participant.getAffectId());
	}
	public static QueryField getFieldAffectId(BasePermissionType permission)
	{
		return getBigIntField("affectid", permission.getId());
	}
	public static QueryField getFieldAffectType(BaseParticipantType participant)
	{
		return getStringField("affecttype", participant.getAffectType().toString());
	}
	public static QueryField getFieldAffectType(AffectEnumType affectType)
	{
		return getStringField("affecttype", affectType.toString());
	}
	public static QueryField getFieldParticipantId(NameIdType map)
	{
		return getBigIntField("participantid", map.getId());
	}

	public static QueryField getFieldParticipantId(BaseParticipantType participant)
	{
		return getBigIntField("participantid", participant.getParticipantId());
	}
	public static QueryField getFieldParticipationId(NameIdType map)
	{
		return getBigIntField("participationid", map.getId());
	}
	public static QueryField getFieldParticipationId(BaseParticipantType participant)
	{
		return getBigIntField("participationid", participant.getParticipationId());
	}
	public static QueryField getFieldParticipantType(ParticipantEnumType participantType)
	{
		return getStringField("participanttype", participantType.toString());
	}

	public static QueryField getFieldRoleType(RoleEnumType type){
		return getStringField("roletype",type.toString());
	}
	public static QueryField getFieldPermissionType(PermissionEnumType type){
		return getStringField("permissiontype", type.toString());
	}
	public static QueryField getFieldSessionId(String id){
		return getStringField("sessionid", id);
	}
	public static QueryField getFieldUserId(long id){
		return getBigIntField("userid", id);
	}
	public static QueryField getFieldDataId(long id){
		return getBigIntField("dataid", id);
	}
	public static QueryField getFieldRoleId(long id){
		return getBigIntField("roleid", id);
	}
	public static QueryField getFieldData(String val){
		return getStringField("data", val);
	}
	public static QueryField getFieldSecurityId(String id){
		return getStringField("securityid", id);
	}
	public static QueryField getFieldSessionCreated(XMLGregorianCalendar val){
		return getTimestampField("sessioncreated", val);
	}
	public static QueryField getFieldSessionExpiration(XMLGregorianCalendar val){
		return getTimestampField("sessionexpiration", val);
	}
	public static QueryField getFieldExpiration(XMLGregorianCalendar val){
		return getTimestampField("expiration", val);
	}

	public static QueryField getFieldSessionAccessed(XMLGregorianCalendar val){
		return getTimestampField("sessionaccessed", val);
	}
	public static QueryField getFieldSessionStatus(SessionStatusEnumType val)
	{
		return getStringField("sessionstatus", val.toString());
	}
	public static QueryField getFieldSessionDataSize(int val)
	{
		return getIntField("sessiondatasize", val);
	}

	public static QueryField getFieldGuid(String guid)
	{
		return getStringField("guid", guid);
	}
	public static QueryField getFieldClassification(String cls)
	{
		return getStringField("classification", cls);
	}
	public static QueryField getFieldCurrentLevel(int level)
	{
		return getIntField("currentlevel", level);
	}
	public static QueryField getFieldEndLevel(int level)
	{
		return getIntField("endlevel", level);
	}
	public static QueryField getFieldParentGuid(String guid)
	{
		return getStringField("parentguid", guid);
	}
	
	public static QueryField getFieldExpires(boolean val)
	{
		return getBooleanField("expires", val);
	}
	public static QueryField getFieldSpoolStatus(SpoolStatusEnumType val)
	{
		return getStringField("spoolstatus", val.toString());
	}
	public static QueryField getFieldSpoolBucketName(SpoolNameEnumType spool_type){
		return getStringField("spoolbucketname",spool_type.toString());
	}
	public static QueryField getFieldSpoolBucketType(SpoolBucketEnumType spool_type){
		return getStringField("spoolbuckettype",spool_type.toString());
	}
	public static QueryField getFieldSpoolData(byte[] data){
		return getBytesField("spooldata",data);
	}
	public static QueryField getFieldSpoolValueType(ValueEnumType spool_type){
		return getStringField("spoolvaluetype",spool_type.toString());
	}
	public static QueryField getFieldAuditSourceType(AuditEnumType audit_type){
		return getStringField("auditsourcetype",audit_type.toString());
	}
	public static QueryField getFieldAuditSourceData(String audit_data){
		return getStringField("auditsourcedata",audit_data);
	}
	public static QueryField getFieldAuditTargetType(AuditEnumType audit_type){
		return getStringField("audittargettype",audit_type.toString());
	}
	public static QueryField getFieldAuditTargetData(String audit_data){
		return getStringField("audittargetdata",audit_data);
	}
	public static QueryField getFieldReferenceType(FactoryEnumType type){
		return getStringField("referencetype",type.toString());
	}
	public static QueryField getFieldReferenceType(NameEnumType type){
		return getStringField("referencetype",type.toString());
	}
	public static QueryField getFieldControlType(ControlEnumType type){
		return getStringField("controltype",type.toString());
	}
	public static QueryField getFieldControlId(long id){
		return getBigIntField("controlid",id);
	}
	public static QueryField getFieldControlAction(ControlActionEnumType type){
		return getStringField("controlaction",type.toString());
	}
	public static QueryField getFieldGlobalKey(boolean b)
	{
		return getBooleanField("globalkey", b);
	}
	public static QueryField getFieldOrganizationKey(boolean b)
	{
		return getBooleanField("organizationkey", b);
	}
	public static QueryField getFieldPrimaryCredential(boolean b)
	{
		return getBooleanField("primarycredential", b);
	}
	public static QueryField getFieldPrimaryKey(boolean b)
	{
		return getBooleanField("primarykey", b);
	}
	public static QueryField getFieldCredentialId(long b)
	{
		return getBigIntField("credentialid", b);
	}
	public static QueryField getFieldRecipientId(long b)
	{
		return getBigIntField("recipientid", b);
	}
	public static QueryField getFieldTransportId(long b)
	{
		return getBigIntField("transportid", b);
	}
	public static QueryField getFieldRecipientType(FactoryEnumType type)
	{
		return getStringField("recipienttype", type.toString());
	}
	public static QueryField getFieldTransportType(FactoryEnumType type)
	{
		return getStringField("transporttype", type.toString());
	}
	public static QueryField getFieldPreviousKeyId(long b)
	{
		return getBigIntField("previouskeyid", b);
	}
	public static QueryField getFieldAsymmetricKeyId(long b)
	{
		return getBigIntField("asymmetrickeyid", b);
	}
	public static QueryField getFieldSymmetricKeyId(long b)
	{
		return getBigIntField("symmetrickeyid", b);
	}
	public static QueryField getFieldPreviousCredentialId(long id){
		return getBigIntField("previouscredentialid",id);
	}
	public static QueryField getFieldNextCredentialId(long id){
		return getBigIntField("nextcredentialid",id);
	}
	public static QueryField getFieldCredentialType(CredentialEnumType type){
		return getStringField("credentialtype",type.toString());
	}
	public static QueryField getFieldCredential(byte[] val)
	{
		return getBytesField("credential", val);
	}
	public static QueryField getFieldSalt(byte[] val)
	{
		return getBytesField("salt", val);
	}

	public static QueryField getFieldReferenceType(SqlDataEnumType type){
		return getStringField("datatype",type.toString());
	}
	public static QueryField getFieldIndex(int index){
		return getIntField("valueindex",index);
	}
	public static QueryField getFieldValue(String val){
		return getStringField("value",val);
	}
	public static QueryField getFieldAccountId(String val){
		return getStringField("accountid",val);
	}
	public static QueryField getFieldAccountStatus(AccountStatusEnumType val){
		return getStringField("accountstatus",val.toString());
	}
	public static QueryField getFieldAccountType(AccountEnumType val){
		return getStringField("accounttype",val.toString());
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

		QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", getFilteredLongList(FactoryBase.convertLongList(ints)));
		match.setComparator(ComparatorEnumType.IN);
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

		QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", getFilteredLongList(FactoryBase.convertLongList(ints)));
		match.setComparator(ComparatorEnumType.IN);
		return match;
	}
	public static String getFilteredLongList(long[] longs){
		StringBuilder buff = new StringBuilder();
		Map<Long,Boolean> id_map = new HashMap<Long, Boolean>();
		int counter = 0;
		for(int i = 0; i < longs.length; i++){
			if(id_map.containsKey(longs[i])) continue;
			if(counter > 0) buff.append(",");
			buff.append(longs[i]);
			id_map.put(longs[i], true);
			counter++;
		}
		return buff.toString();
	}
	public static String getFilteredIntList(int[] ints){
		StringBuilder buff = new StringBuilder();
		Map<Integer,Boolean> id_map = new HashMap<Integer, Boolean>();
		int counter = 0;
		for(int i = 0; i < ints.length; i++){
			if(id_map.containsKey(ints[i])) continue;
			if(counter > 0) buff.append(",");
			buff.append(ints[i]);
			id_map.put(ints[i], true);
			counter++;
		}
		return buff.toString();
	}
	public static QueryField getFieldTagType(TagEnumType type){
		return getStringField("tagtype", type.toString());
	}
	
}
