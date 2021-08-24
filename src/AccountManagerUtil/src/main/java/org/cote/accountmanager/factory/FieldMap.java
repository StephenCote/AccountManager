package org.cote.accountmanager.factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cote.accountmanager.objects.types.ColumnEnumType;

public class FieldMap {

	private FieldMap() {
		
	}
	private static final Map<ColumnEnumType,String> columnMap = new HashMap<>();
	static {
	   columnMap.put(ColumnEnumType.FACTORYTYPE, "factorytype");
	   columnMap.put(ColumnEnumType.FACTTYPE, "facttype");
	   columnMap.put(ColumnEnumType.RULETYPE, "ruletype");
	   columnMap.put(ColumnEnumType.URN, "urn");
	   columnMap.put(ColumnEnumType.SCORE, "score");
	   columnMap.put(ColumnEnumType.FACTDATA, "factdata");
	   columnMap.put(ColumnEnumType.FUNCTIONURN, "functionurn");
	   columnMap.put(ColumnEnumType.FUNCTIONTYPE, "functiontype");
	   columnMap.put(ColumnEnumType.OPERATION, "operation");
	   columnMap.put(ColumnEnumType.OPERATIONTYPE, "operationtype");
	   columnMap.put(ColumnEnumType.PATTERNTYPE, "patterntype");
	   columnMap.put(ColumnEnumType.COMPARATOR, "comparator");
	   columnMap.put(ColumnEnumType.CONDITION, "condition");
	   columnMap.put(ColumnEnumType.MATCHURN, "matchurn");
	   columnMap.put(ColumnEnumType.OPERATIONURN, "operationurn");
	   columnMap.put(ColumnEnumType.FACTURN, "facturn");
	   columnMap.put(ColumnEnumType.SOURCEURN, "sourceurn");
	   columnMap.put(ColumnEnumType.SOURCEURL, "sourceurl");
	   columnMap.put(ColumnEnumType.SOURCETYPE, "sourcetype");
	   columnMap.put(ColumnEnumType.SOURCEDATATYPE, "sourcedatatype");
	   columnMap.put(ColumnEnumType.PREFERRED, "preferred");
	   columnMap.put(ColumnEnumType.ISVAULTED, "isvaulted");
	   columnMap.put(ColumnEnumType.VAULTID, "vaultid");
	   columnMap.put(ColumnEnumType.KEYID, "keyid");
	   columnMap.put(ColumnEnumType.MIMETYPE, "mimetype");
	   columnMap.put(ColumnEnumType.ENABLED, "enabled");
	   columnMap.put(ColumnEnumType.GROUPID, "groupid");
	   columnMap.put(ColumnEnumType.DATABLOB, "datablob");
	   columnMap.put(ColumnEnumType.DATASTRING, "datastring");
	   columnMap.put(ColumnEnumType.DIMENSIONS, "dimensions");
	   columnMap.put(ColumnEnumType.COMPRESSIONTYPE, "compressiontype");
	   columnMap.put(ColumnEnumType.ISCOMPRESSED, "iscompressed");
	   columnMap.put(ColumnEnumType.ISBLOB, "isblob");
	   columnMap.put(ColumnEnumType.ISENCIPHERED, "isenciphered");
	   columnMap.put(ColumnEnumType.ISPASSWORDPROTECTED, "ispasswordprotected");

	   columnMap.put(ColumnEnumType.ISPOINTER, "ispointer");
	   columnMap.put(ColumnEnumType.SIZE, "size");
	   columnMap.put(ColumnEnumType.RATING, "rating");
	   columnMap.put(ColumnEnumType.HASH, "hash");
	   columnMap.put(ColumnEnumType.HASHPROVIDER, "hashprovider");
	   columnMap.put(ColumnEnumType.CREATEDDATE, "createddate");
	   columnMap.put(ColumnEnumType.STARTDATE, "startdate");
	   columnMap.put(ColumnEnumType.MODIFIEDDATE, "modifieddate");
	   columnMap.put(ColumnEnumType.EXPIRATIONDATE, "expirationdate");
	   columnMap.put(ColumnEnumType.ACCESSEDDATE, "accesseddate");
	   columnMap.put(ColumnEnumType.STARTTIME, "starttime");
	   columnMap.put(ColumnEnumType.ENDTIME, "endtime");
	   columnMap.put(ColumnEnumType.APPROVERLEVEL, "approverlevel");
	   columnMap.put(ColumnEnumType.APPROVALTYPE, "approvaltype");
	   columnMap.put(ColumnEnumType.APPROVERTYPE, "approvertype");
	   columnMap.put(ColumnEnumType.APPROVERID, "approverid");
	   columnMap.put(ColumnEnumType.APPROVALID, "approvalid");
	   columnMap.put(ColumnEnumType.RESPONSE, "response");
	   columnMap.put(ColumnEnumType.RESPONSEMESSAGE, "responsemessage");
	   columnMap.put(ColumnEnumType.REQUESTID, "requestid");
	   columnMap.put(ColumnEnumType.SIGNERID, "signerid");
	   columnMap.put(ColumnEnumType.VALIDATIONID, "validationid");
	   columnMap.put(ColumnEnumType.SIGNATURE, "signature");
	   columnMap.put(ColumnEnumType.ENTITLEMENTTYPE, "entitlementtype");
	   columnMap.put(ColumnEnumType.ENTITLEMENTID, "entitlementid");
	   columnMap.put(ColumnEnumType.REQUESTORTYPE, "requestortype");
	   columnMap.put(ColumnEnumType.APPROVALSTATUS, "approvalstatus");
	   columnMap.put(ColumnEnumType.REQUESTORID, "requestorid");
	   columnMap.put(ColumnEnumType.DELEGATETYPE, "delegatetype");
	   columnMap.put(ColumnEnumType.DELEGATEID, "delegateid");
	   columnMap.put(ColumnEnumType.ORGANIZATIONID, "organizationid");
	   columnMap.put(ColumnEnumType.ID, "id");
	   columnMap.put(ColumnEnumType.OBJECTID, "objectid");
	   columnMap.put(ColumnEnumType.NAME, "name");
	   columnMap.put(ColumnEnumType.PARENTID, "parentid");
	   columnMap.put(ColumnEnumType.OWNERID, "ownerid");
	   columnMap.put(ColumnEnumType.REFERENCEID, "referenceid");
	   columnMap.put(ColumnEnumType.DESCRIPTION, "description");
	   columnMap.put(ColumnEnumType.LOGICALID, "logicalid");
	   columnMap.put(ColumnEnumType.LOGICALORDER, "logicalorder");
	   columnMap.put(ColumnEnumType.DECISIONAGE, "decisionage");
	   columnMap.put(ColumnEnumType.ORGANIZATIONTYPE, "organizationtype");
	   columnMap.put(ColumnEnumType.GROUPTYPE, "grouptype");
	   columnMap.put(ColumnEnumType.CONTACTINFORMATIONTYPE, "contactinformationtype");
	   columnMap.put(ColumnEnumType.CONTACTINFORMATIONID, "contactinformationid");
	   columnMap.put(ColumnEnumType.WEBSITE, "website");
	   columnMap.put(ColumnEnumType.TITLE, "title");
	   columnMap.put(ColumnEnumType.STATE, "state");
	   columnMap.put(ColumnEnumType.COUNTRY, "country");
	   columnMap.put(ColumnEnumType.CITY, "city");
	   columnMap.put(ColumnEnumType.POSTALCODE, "postalcode");
	   columnMap.put(ColumnEnumType.PHONE, "phone");
	   columnMap.put(ColumnEnumType.MIDDLENAME, "middlename");
	   columnMap.put(ColumnEnumType.LASTNAME, "lastname");
	   columnMap.put(ColumnEnumType.FIRSTNAME, "firstname");
	   columnMap.put(ColumnEnumType.ALIAS, "alias");
	   columnMap.put(ColumnEnumType.PREFIX, "prefix");
	   columnMap.put(ColumnEnumType.REGION, "region");
	   columnMap.put(ColumnEnumType.LOCATIONTYPE, "locationtype");
	   columnMap.put(ColumnEnumType.CONTACTTYPE, "contacttype");
	   columnMap.put(ColumnEnumType.CONTACTVALUE, "contactvalue");
	   columnMap.put(ColumnEnumType.SUFFIX, "suffix");
	   columnMap.put(ColumnEnumType.GENDER, "gender");
	   columnMap.put(ColumnEnumType.FAX, "fax");
	   columnMap.put(ColumnEnumType.EMAIL, "email");
	   columnMap.put(ColumnEnumType.BIRTHDATE, "birthdate");
	   columnMap.put(ColumnEnumType.ADDRESSLINE_1, "addressline1");
	   columnMap.put(ColumnEnumType.ADDRESSLINE_2, "addressline2");
	   columnMap.put(ColumnEnumType.STATISTICSTYPE, "statisticstype");
	   columnMap.put(ColumnEnumType.AFFECTID, "affectid");
	   columnMap.put(ColumnEnumType.AFFECTTYPE, "affecttype");
	   columnMap.put(ColumnEnumType.PARTICIPANTID, "participantid");
	   columnMap.put(ColumnEnumType.PARTICIPATIONID, "participationid");
	   columnMap.put(ColumnEnumType.PARTICIPANTTYPE, "participanttype");
	   columnMap.put(ColumnEnumType.ROLETYPE, "roletype");
	   columnMap.put(ColumnEnumType.PERMISSIONTYPE, "permissiontype");
	   columnMap.put(ColumnEnumType.SESSIONID, "sessionid");
	   columnMap.put(ColumnEnumType.USERID, "userid");
	   columnMap.put(ColumnEnumType.DATAID, "dataid");
	   columnMap.put(ColumnEnumType.ROLEID, "roleid");
	   columnMap.put(ColumnEnumType.DATA, "data");
	   columnMap.put(ColumnEnumType.SECURITYID, "securityid");
	   columnMap.put(ColumnEnumType.SESSIONCREATED, "sessioncreated");
	   columnMap.put(ColumnEnumType.SESSIONEXPIRATION, "sessionexpiration");
	   columnMap.put(ColumnEnumType.EXPIRATION, "expiration");
	   columnMap.put(ColumnEnumType.SESSIONACCESSED, "sessionaccessed");
	   columnMap.put(ColumnEnumType.SESSIONSTATUS, "sessionstatus");
	   columnMap.put(ColumnEnumType.SESSIONDATASIZE, "sessiondatasize");
	   columnMap.put(ColumnEnumType.GUID, "guid");
	   columnMap.put(ColumnEnumType.CURRENTLEVEL, "currentlevel");
	   columnMap.put(ColumnEnumType.ENDLEVEL, "endlevel");
	   columnMap.put(ColumnEnumType.PARENTGUID, "parentguid");
	   columnMap.put(ColumnEnumType.EXPIRES, "expires");
	   columnMap.put(ColumnEnumType.SPOOLSTATUS, "spoolstatus");
	   columnMap.put(ColumnEnumType.SPOOLBUCKETNAME, "spoolbucketname");
	   columnMap.put(ColumnEnumType.SPOOLBUCKETTYPE, "spoolbuckettype");
	   columnMap.put(ColumnEnumType.SPOOLDATA, "spooldata");
	   columnMap.put(ColumnEnumType.SPOOLVALUETYPE, "spoolvaluetype");
	   columnMap.put(ColumnEnumType.AUDITSOURCETYPE, "auditsourcetype");
	   columnMap.put(ColumnEnumType.AUDITSOURCEDATA, "auditsourcedata");
	   columnMap.put(ColumnEnumType.AUDITTARGETTYPE, "audittargettype");
	   columnMap.put(ColumnEnumType.AUDITTARGETDATA, "audittargetdata");
		columnMap.put(ColumnEnumType.AUDITACTIONSOURCE, "auditactionsource");
		columnMap.put(ColumnEnumType.AUDITLEVELTYPE, "auditleveltype");
		columnMap.put(ColumnEnumType.AUDITACTIONTYPE, "auditactiontype");
		columnMap.put(ColumnEnumType.AUDITDATE, "auditdate");
		columnMap.put(ColumnEnumType.AUDITRESULTDATE, "auditresultdate");
		columnMap.put(ColumnEnumType.AUDITEXPIRESDATE, "auditexpiresdate");
		columnMap.put(ColumnEnumType.AUDITRESULTDATA, "auditresultdata");
		columnMap.put(ColumnEnumType.AUDITRESULTTYPE, "auditresulttype");
		columnMap.put(ColumnEnumType.AUDITRETENTIONTYPE, "auditretentiontype");
	   columnMap.put(ColumnEnumType.ACTIONTYPE, "actiontype");
	   columnMap.put(ColumnEnumType.REFERENCETYPE, "referencetype");
	   columnMap.put(ColumnEnumType.CONTROLTYPE, "controltype");
	   columnMap.put(ColumnEnumType.CONTROLID, "controlid");
	   columnMap.put(ColumnEnumType.CONTROLACTION, "controlaction");
	   columnMap.put(ColumnEnumType.GLOBALKEY, "globalkey");
	   columnMap.put(ColumnEnumType.ORGANIZATIONKEY, "organizationkey");
	   columnMap.put(ColumnEnumType.PRIMARYCREDENTIAL, "primarycredential");
	   columnMap.put(ColumnEnumType.PRIMARYKEY, "primarykey");
	   columnMap.put(ColumnEnumType.CREDENTIALID, "credentialid");
	   columnMap.put(ColumnEnumType.RECIPIENTID, "recipientid");
	   columnMap.put(ColumnEnumType.TRANSPORTID, "transportid");
	   columnMap.put(ColumnEnumType.RECIPIENTTYPE, "recipienttype");
	   columnMap.put(ColumnEnumType.TRANSPORTTYPE, "transporttype");
	   columnMap.put(ColumnEnumType.PREVIOUSKEYID, "previouskeyid");
	   columnMap.put(ColumnEnumType.ASYMMETRICKEYID, "asymmetrickeyid");
	   columnMap.put(ColumnEnumType.SYMMETRICKEYID, "symmetrickeyid");
	   columnMap.put(ColumnEnumType.PREVIOUSCREDENTIALID, "previouscredentialid");
	   columnMap.put(ColumnEnumType.NEXTCREDENTIALID, "nextcredentialid");
	   columnMap.put(ColumnEnumType.CREDENTIALTYPE, "credentialtype");
	   columnMap.put(ColumnEnumType.CREDENTIAL, "credential");
	   columnMap.put(ColumnEnumType.SALT, "salt");
	   columnMap.put(ColumnEnumType.DATATYPE, "datatype");
	   columnMap.put(ColumnEnumType.VALUEINDEX, "valueindex");
	   columnMap.put(ColumnEnumType.VALUE, "value");
	   columnMap.put(ColumnEnumType.ACCOUNTID, "accountid");
	   columnMap.put(ColumnEnumType.ACCOUNTSTATUS, "accountstatus");
	   columnMap.put(ColumnEnumType.ACCOUNTTYPE, "accounttype");
	   columnMap.put(ColumnEnumType.TAGTYPE, "tagtype");
	   columnMap.put(ColumnEnumType.GOALTYPE, "goaltype");
	   columnMap.put(ColumnEnumType.COSTID, "costid");
	   columnMap.put(ColumnEnumType.TIMEID, "timeid");
	   columnMap.put(ColumnEnumType.BUDGETTYPE, "budgettype");
	   columnMap.put(ColumnEnumType.ASSIGNEDRESOURCEID, "assignedresourceid");
	   columnMap.put(ColumnEnumType.PRIORITY, "priority");
	   columnMap.put(ColumnEnumType.SEVERITY, "severity");
	   columnMap.put(ColumnEnumType.TICKETSTATUS, "ticketstatus");
	   columnMap.put(ColumnEnumType.ACTUALTIMEID, "actualtimeid");
	   columnMap.put(ColumnEnumType.ACTUALCOSTID, "actualcostid");
	   columnMap.put(ColumnEnumType.DUEDATE, "duedate");
	   columnMap.put(ColumnEnumType.CLOSEDDATE, "closeddate");
	   columnMap.put(ColumnEnumType.COMPLETEDDATE, "completeddate");
	   columnMap.put(ColumnEnumType.REOPENEDDATE, "reopeneddate");
	   columnMap.put(ColumnEnumType.CURRENCYTYPE, "currencytype");
	   columnMap.put(ColumnEnumType.BASISTYPE, "basistype");
	   columnMap.put(ColumnEnumType.ARTIFACTTYPE, "artifacttype");
	   columnMap.put(ColumnEnumType.PREVIOUSTRANSITIONID, "previoustransitionid");
	   columnMap.put(ColumnEnumType.NEXTTRANSITIONID, "nexttransitionid");
	   columnMap.put(ColumnEnumType.ARTIFACTDATAID, "artifactdataid");
	   columnMap.put(ColumnEnumType.REQUIREMENTTYPE, "requirementtype");
	   columnMap.put(ColumnEnumType.REQUIREMENTSTATUS, "requirementstatus");
	   columnMap.put(ColumnEnumType.MODELTYPE, "modeltype");
	   columnMap.put(ColumnEnumType.ESTIMATETYPE, "estimatetype");
	   columnMap.put(ColumnEnumType.TASKSTATUS, "taskstatus");
	   columnMap.put(ColumnEnumType.RESOURCETYPE, "resourcetype");
	   columnMap.put(ColumnEnumType.MODULETYPE, "moduletype");
	   columnMap.put(ColumnEnumType.CASETYPE, "casetype");
	   columnMap.put(ColumnEnumType.TRAITTYPE, "traittype");
	   columnMap.put(ColumnEnumType.ALIGNMENT, "alignment");
	   columnMap.put(ColumnEnumType.EVENTTYPE, "eventtype");
	   columnMap.put(ColumnEnumType.LOCATIONID, "locationid");
	   columnMap.put(ColumnEnumType.GEOGRAPHYTYPE, "geographytype");
	   columnMap.put(ColumnEnumType.CLASSIFICATION, "classification");
	   columnMap.put(ColumnEnumType.ESTIMATEID, "estimateid");
	   columnMap.put(ColumnEnumType.RESOURCEID, "resourceid");
	   columnMap.put(ColumnEnumType.SCHEDULEID, "scheduleid");
	   columnMap.put(ColumnEnumType.WORKID, "workid");
	   columnMap.put(ColumnEnumType.BUDGETID, "budgetid");
	   columnMap.put(ColumnEnumType.METHODOLOGYID, "methodologyid");
	   columnMap.put(ColumnEnumType.UTILIZATION, "utilization");
	   columnMap.put(ColumnEnumType.ITERATES, "iterates");
	   columnMap.put(ColumnEnumType.ISBINARY, "isbinary");
	   columnMap.put(ColumnEnumType.BINARYVALUEID, "binaryvalueid");
	   columnMap.put(ColumnEnumType.TEXTVALUE, "textvalue");
	   columnMap.put(ColumnEnumType.FORMELEMENTID, "formelementid");
	   columnMap.put(ColumnEnumType.ELEMENTTYPE, "elementtype");
	   columnMap.put(ColumnEnumType.ELEMENTNAME, "elementname");
	   columnMap.put(ColumnEnumType.ELEMENTLABEL, "elementlabel");
	   columnMap.put(ColumnEnumType.FORMID, "formid");
	   columnMap.put(ColumnEnumType.VALIDATIONRULEID, "validationruleid");
	   columnMap.put(ColumnEnumType.ALLOWNULL, "allownull");
	   columnMap.put(ColumnEnumType.TEMPLATEID, "templateid");
	   columnMap.put(ColumnEnumType.VIEWTEMPLATEID, "viewtemplateid");
	   columnMap.put(ColumnEnumType.ELEMENTTEMPLATEID, "elementtemplateid");
	   columnMap.put(ColumnEnumType.ISTEMPLATE, "istemplate");
	   columnMap.put(ColumnEnumType.ISGRID, "isgrid");
	   columnMap.put(ColumnEnumType.VALIDATIONTYPE, "validationtype");
	   columnMap.put(ColumnEnumType.ISRULESET, "isruleset");
	   columnMap.put(ColumnEnumType.COMPARISON, "comparison");
	   columnMap.put(ColumnEnumType.ISREPLACEMENTRULE, "isreplacementrule");
	   columnMap.put(ColumnEnumType.EXPRESSION, "expression");
	   columnMap.put(ColumnEnumType.ERRORMESSAGE, "errormessage");
	   columnMap.put(ColumnEnumType.REPLACEMENTVALUE, "replacementvalue");
	   columnMap.put(ColumnEnumType.REQUIREMENTID, "requirementid");
	   columnMap.put(ColumnEnumType.TEXT, "text");
		columnMap.put(ColumnEnumType.CIPHERPROVIDER, "cipherprovider");
		columnMap.put(ColumnEnumType.CIPHERKEYSPEC, "cipherkeyspec");
		columnMap.put(ColumnEnumType.ASYMMETRICCIPHERKEYSPEC, "asymmetriccipherkeyspec");
		columnMap.put(ColumnEnumType.SYMMETRICCIPHERKEYSPEC, "symmetriccipherkeyspec");
		columnMap.put(ColumnEnumType.CIPHERKEY, "cipherkey");
		columnMap.put(ColumnEnumType.CIPHERIV, "cipheriv");
		columnMap.put(ColumnEnumType.ENCRYPTEDKEY, "encryptedkey");
		columnMap.put(ColumnEnumType.SEEDLENGTH, "seedlength");
		columnMap.put(ColumnEnumType.PUBLICKEY, "publickey");
		columnMap.put(ColumnEnumType.PRIVATEKEY, "privatekey");
   }
	   
	public static final Map<ColumnEnumType,String> Columns = Collections.unmodifiableMap(columnMap);

}
