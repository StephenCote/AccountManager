package org.cote.accountmanager.factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class FieldMap {

	private FieldMap() {
		
	}
	private static final Map<ColumnEnumType,String> columnMap = new HashMap<>();
	private static final Map<ColumnEnumType,SqlDataEnumType> dataMap = new HashMap<>();
	/*
	private static void map(ColumnEnumType type, String columnName) {
	    map(type, columnName, SqlDataEnumType.VARCHAR);
	}
	*/
   private static void mapVar(ColumnEnumType type, String columnName) {
        map(type, columnName, SqlDataEnumType.VARCHAR);
    }
   private static void mapBlob(ColumnEnumType type, String columnName) {
       map(type, columnName, SqlDataEnumType.BLOB);
   }
   private static void mapLong(ColumnEnumType type, String columnName) {
       map(type, columnName, SqlDataEnumType.BIGINT);
   }
   private static void mapBool(ColumnEnumType type, String columnName) {
       map(type, columnName, SqlDataEnumType.BOOLEAN);
   }
   private static void mapInt(ColumnEnumType type, String columnName) {
       map(type, columnName, SqlDataEnumType.INTEGER);
   }
   private static void mapTime(ColumnEnumType type, String columnName) {
       map(type, columnName, SqlDataEnumType.TIMESTAMP);
   }
   private static void mapDouble(ColumnEnumType type, String columnName) {
       map(type, columnName, SqlDataEnumType.DOUBLE);
   }
	private static void map(ColumnEnumType type, String columnName, SqlDataEnumType dataType) {
	    columnMap.put(type, columnName);
	    dataMap.put(type,  dataType);
	}
	static {
	    
	   /// VARCHAR
	    mapVar(ColumnEnumType.FACTORYTYPE, "factorytype");
	    mapVar(ColumnEnumType.FACTTYPE, "facttype");
	    mapVar(ColumnEnumType.RULETYPE, "ruletype");
	    mapVar(ColumnEnumType.URN, "urn");
	    mapVar(ColumnEnumType.FACTDATA, "factdata");
	    mapVar(ColumnEnumType.FUNCTIONURN, "functionurn");
	    mapVar(ColumnEnumType.FUNCTIONTYPE, "functiontype");
	   mapVar(ColumnEnumType.OPERATIONTYPE, "operationtype");
	   mapVar(ColumnEnumType.PATTERNTYPE, "patterntype");
	   mapVar(ColumnEnumType.COMPARATOR, "comparator");
	   mapVar(ColumnEnumType.CONDITION, "condition");
	   mapVar(ColumnEnumType.MATCHURN, "matchurn");
	   mapVar(ColumnEnumType.OPERATIONURN, "operationurn");
	   mapVar(ColumnEnumType.FACTURN, "facturn");
	   mapVar(ColumnEnumType.SOURCEURN, "sourceurn");
	   mapVar(ColumnEnumType.SOURCEURL, "sourceurl");
	   mapVar(ColumnEnumType.SOURCETYPE, "sourcetype");
	   mapVar(ColumnEnumType.SOURCEDATATYPE, "sourcedatatype");
	   mapVar(ColumnEnumType.DATASTRING, "datastring");
	   mapVar(ColumnEnumType.DIMENSIONS, "dimensions");
	   mapVar(ColumnEnumType.COMPRESSIONTYPE, "compressiontype");
	   mapVar(ColumnEnumType.MIMETYPE, "mimetype");
	   mapVar(ColumnEnumType.VAULTID, "vaultid");
	   mapVar(ColumnEnumType.KEYID, "keyid");	
	   mapVar(ColumnEnumType.OPERATION, "operation");	   
	   mapVar(ColumnEnumType.HASH, "hash");
	   mapVar(ColumnEnumType.HASHPROVIDER, "hashprovider");	
	   mapVar(ColumnEnumType.APPROVALTYPE, "approvaltype");
	   mapVar(ColumnEnumType.APPROVERTYPE, "approvertype");	   
	   mapVar(ColumnEnumType.RESPONSE, "response");
	   mapVar(ColumnEnumType.RESPONSEMESSAGE, "responsemessage");	   
	   mapVar(ColumnEnumType.REQUESTID, "requestid");	   
	   mapVar(ColumnEnumType.VALIDATIONID, "validationid");	   
	   mapVar(ColumnEnumType.SIGNERID, "signerid");	   
	   mapVar(ColumnEnumType.SIGNATURE, "signature");
	   mapVar(ColumnEnumType.ENTITLEMENTTYPE, "entitlementtype");
       mapVar(ColumnEnumType.REQUESTORTYPE, "requestortype");
       mapVar(ColumnEnumType.APPROVALSTATUS, "approvalstatus");
       mapVar(ColumnEnumType.DELEGATETYPE, "delegatetype");
       mapVar(ColumnEnumType.OBJECTID, "objectid");
       mapVar(ColumnEnumType.NAME, "name");
       mapVar(ColumnEnumType.DESCRIPTION, "description");
       mapVar(ColumnEnumType.ORGANIZATIONTYPE, "organizationtype");
       mapVar(ColumnEnumType.GROUPTYPE, "grouptype");
       mapVar(ColumnEnumType.CONTACTINFORMATIONTYPE, "contactinformationtype");
       mapVar(ColumnEnumType.WEBSITE, "website");
       mapVar(ColumnEnumType.TITLE, "title");
       mapVar(ColumnEnumType.STATE, "state");
       mapVar(ColumnEnumType.COUNTRY, "country");
       mapVar(ColumnEnumType.CITY, "city");
       mapVar(ColumnEnumType.POSTALCODE, "postalcode");
       mapVar(ColumnEnumType.PHONE, "phone");
       mapVar(ColumnEnumType.MIDDLENAME, "middlename");
       mapVar(ColumnEnumType.LASTNAME, "lastname");
       mapVar(ColumnEnumType.FIRSTNAME, "firstname");
       mapVar(ColumnEnumType.ALIAS, "alias");
       mapVar(ColumnEnumType.PREFIX, "prefix");
       mapVar(ColumnEnumType.REGION, "region");
       mapVar(ColumnEnumType.LOCATIONTYPE, "locationtype");
       mapVar(ColumnEnumType.CONTACTTYPE, "contacttype");
       mapVar(ColumnEnumType.CONTACTVALUE, "contactvalue");
       mapVar(ColumnEnumType.SUFFIX, "suffix");
       mapVar(ColumnEnumType.GENDER, "gender");
       mapVar(ColumnEnumType.FAX, "fax");
       mapVar(ColumnEnumType.EMAIL, "email");
       mapVar(ColumnEnumType.ADDRESSLINE_1, "addressline1");
       mapVar(ColumnEnumType.ADDRESSLINE_2, "addressline2");
       mapVar(ColumnEnumType.STATISTICSTYPE, "statisticstype");
       mapVar(ColumnEnumType.AFFECTTYPE, "affecttype");
       mapVar(ColumnEnumType.PARTICIPANTTYPE, "participanttype");
       mapVar(ColumnEnumType.ROLETYPE, "roletype");
       mapVar(ColumnEnumType.PERMISSIONTYPE, "permissiontype");
       mapVar(ColumnEnumType.SESSIONID, "sessionid");
       mapVar(ColumnEnumType.USERID, "userid");
       mapVar(ColumnEnumType.SESSIONSTATUS, "sessionstatus");
       mapVar(ColumnEnumType.SPOOLSTATUS, "spoolstatus");
       mapVar(ColumnEnumType.SPOOLBUCKETNAME, "spoolbucketname");
       mapVar(ColumnEnumType.SPOOLBUCKETTYPE, "spoolbuckettype");
       mapVar(ColumnEnumType.PARENTOBJECTID, "parentobjectid");
       mapVar(ColumnEnumType.SPOOLVALUETYPE, "spoolvaluetype");
       mapVar(ColumnEnumType.AUDITSOURCETYPE, "auditsourcetype");
       mapVar(ColumnEnumType.AUDITSOURCEDATA, "auditsourcedata");
       mapVar(ColumnEnumType.AUDITTARGETTYPE, "audittargettype");
       mapVar(ColumnEnumType.AUDITTARGETDATA, "audittargetdata");
        mapVar(ColumnEnumType.AUDITACTIONSOURCE, "auditactionsource");
        mapVar(ColumnEnumType.AUDITLEVELTYPE, "auditleveltype");
        mapVar(ColumnEnumType.AUDITACTIONTYPE, "auditactiontype");
        mapVar(ColumnEnumType.AUDITRESULTTYPE, "auditresulttype");
        mapVar(ColumnEnumType.AUDITRETENTIONTYPE, "auditretentiontype");
       mapVar(ColumnEnumType.ACTIONTYPE, "actiontype");
       mapVar(ColumnEnumType.REFERENCETYPE, "referencetype");
       mapVar(ColumnEnumType.CONTROLTYPE, "controltype");
       mapVar(ColumnEnumType.CONTROLACTION, "controlaction");
       mapVar(ColumnEnumType.SENDERTYPE, "sendertype");
       mapVar(ColumnEnumType.RECIPIENTTYPE, "recipienttype");
       mapVar(ColumnEnumType.TRANSPORTTYPE, "transporttype");
       mapVar(ColumnEnumType.CREDENTIALTYPE, "credentialtype");
       mapVar(ColumnEnumType.DATATYPE, "datatype");
       mapVar(ColumnEnumType.VALUE, "value");
       mapVar(ColumnEnumType.ACCOUNTSTATUS, "accountstatus");
       mapVar(ColumnEnumType.ACCOUNTTYPE, "accounttype");
       mapVar(ColumnEnumType.TAGTYPE, "tagtype");
       mapVar(ColumnEnumType.GOALTYPE, "goaltype");
       mapVar(ColumnEnumType.BUDGETTYPE, "budgettype");
       mapVar(ColumnEnumType.PRIORITY, "priority");
       mapVar(ColumnEnumType.SEVERITY, "severity");
       mapVar(ColumnEnumType.TICKETSTATUS, "ticketstatus");
       mapVar(ColumnEnumType.CURRENCYTYPE, "currencytype");
       mapVar(ColumnEnumType.BASISTYPE, "basistype");
       mapVar(ColumnEnumType.ARTIFACTTYPE, "artifacttype");
       mapVar(ColumnEnumType.REQUIREMENTTYPE, "requirementtype");
       mapVar(ColumnEnumType.REQUIREMENTSTATUS, "requirementstatus");
       mapVar(ColumnEnumType.MODELTYPE, "modeltype");
       mapVar(ColumnEnumType.ESTIMATETYPE, "estimatetype");
       mapVar(ColumnEnumType.TASKSTATUS, "taskstatus");
       mapVar(ColumnEnumType.RESOURCETYPE, "resourcetype");
       mapVar(ColumnEnumType.MODULETYPE, "moduletype");
       mapVar(ColumnEnumType.CASETYPE, "casetype");
       mapVar(ColumnEnumType.TRAITTYPE, "traittype");
       mapVar(ColumnEnumType.ALIGNMENT, "alignment");
       mapVar(ColumnEnumType.EVENTTYPE, "eventtype");
       mapVar(ColumnEnumType.GEOGRAPHYTYPE, "geographytype");
       mapVar(ColumnEnumType.CLASSIFICATION, "classification");
       mapVar(ColumnEnumType.TEXTVALUE, "textvalue");
       mapVar(ColumnEnumType.ELEMENTTYPE, "elementtype");
       mapVar(ColumnEnumType.ELEMENTNAME, "elementname");
       mapVar(ColumnEnumType.ELEMENTLABEL, "elementlabel");
       mapVar(ColumnEnumType.VALIDATIONTYPE, "validationtype");
       mapVar(ColumnEnumType.COMPARISON, "comparison");
       mapVar(ColumnEnumType.EXPRESSION, "expression");
       mapVar(ColumnEnumType.ERRORMESSAGE, "errormessage");
       mapVar(ColumnEnumType.REPLACEMENTVALUE, "replacementvalue");
       mapVar(ColumnEnumType.TEXT, "text");
        mapVar(ColumnEnumType.CIPHERPROVIDER, "cipherprovider");
        mapVar(ColumnEnumType.CIPHERKEYSPEC, "cipherkeyspec");
        mapVar(ColumnEnumType.ASYMMETRICCIPHERKEYSPEC, "asymmetriccipherkeyspec");
        mapVar(ColumnEnumType.SYMMETRICCIPHERKEYSPEC, "symmetriccipherkeyspec");
        mapVar(ColumnEnumType.USERTYPE, "usertype");
        mapVar(ColumnEnumType.USERSTATUS, "userstatus");
        mapVar(ColumnEnumType.ENDDATE, "enddate");
        mapVar(ColumnEnumType.CURVENAME, "curvename");
        mapVar(ColumnEnumType.KEYAGREEMENTSPEC, "keyagreementspec");


       
	   /// BINARY
	   mapBlob(ColumnEnumType.DATABLOB, "datablob");
	   mapBlob(ColumnEnumType.SPOOLDATA, "spooldata");
       mapBlob(ColumnEnumType.CREDENTIAL, "credential");
       mapBlob(ColumnEnumType.SALT, "salt");
       mapBlob(ColumnEnumType.CIPHERKEY, "cipherkey");
       mapBlob(ColumnEnumType.CIPHERIV, "cipheriv");
       mapBlob(ColumnEnumType.PUBLICKEY, "publickey");
       mapBlob(ColumnEnumType.PRIVATEKEY, "privatekey");

	    
	   /// LONG
       mapLong(ColumnEnumType.ASSIGNEDRESOURCEID, "assignedresourceid");
       mapLong(ColumnEnumType.ACTUALTIMEID, "actualtimeid");
       mapLong(ColumnEnumType.ACTUALCOSTID, "actualcostid");
       mapLong(ColumnEnumType.COSTID, "costid");
       mapLong(ColumnEnumType.TIMEID, "timeid");
	   mapLong(ColumnEnumType.GROUPID, "groupid");
	   mapLong(ColumnEnumType.APPROVERID, "approverid");
	   mapLong(ColumnEnumType.APPROVALID, "approvalid");	   
	   mapLong(ColumnEnumType.ENTITLEMENTID, "entitlementid");
	   mapLong(ColumnEnumType.REQUESTORID, "requestorid");
       mapLong(ColumnEnumType.DELEGATEID, "delegateid");
       mapLong(ColumnEnumType.ORGANIZATIONID, "organizationid");
       mapLong(ColumnEnumType.ID, "id");
       mapLong(ColumnEnumType.PARENTID, "parentid");
       mapLong(ColumnEnumType.OWNERID, "ownerid");
       mapLong(ColumnEnumType.REFERENCEID, "referenceid");
       mapLong(ColumnEnumType.LOGICALID, "logicalid");
       mapLong(ColumnEnumType.DECISIONAGE, "decisionage");
       mapLong(ColumnEnumType.CONTACTINFORMATIONID, "contactinformationid");
       mapLong(ColumnEnumType.AFFECTID, "affectid");
       mapLong(ColumnEnumType.PARTICIPANTID, "participantid");
       mapLong(ColumnEnumType.PARTICIPATIONID, "participationid");
       mapLong(ColumnEnumType.DATAID, "dataid");
       mapLong(ColumnEnumType.ROLEID, "roleid");
       mapLong(ColumnEnumType.SECURITYID, "securityid");
       mapLong(ColumnEnumType.CONTROLID, "controlid");
       mapLong(ColumnEnumType.CREDENTIALID, "credentialid");
       mapLong(ColumnEnumType.SENDERID, "senderid");
       mapLong(ColumnEnumType.RECIPIENTID, "recipientid");
       mapLong(ColumnEnumType.TRANSPORTID, "transportid");
       mapLong(ColumnEnumType.PREVIOUSKEYID, "previouskeyid");
       mapLong(ColumnEnumType.ASYMMETRICKEYID, "asymmetrickeyid");
       mapLong(ColumnEnumType.SYMMETRICKEYID, "symmetrickeyid");
       mapLong(ColumnEnumType.PREVIOUSCREDENTIALID, "previouscredentialid");
       mapLong(ColumnEnumType.NEXTCREDENTIALID, "nextcredentialid");
       mapLong(ColumnEnumType.ACCOUNTID, "accountid");
       mapLong(ColumnEnumType.PREVIOUSTRANSITIONID, "previoustransitionid");
       mapLong(ColumnEnumType.NEXTTRANSITIONID, "nexttransitionid");
       mapLong(ColumnEnumType.ARTIFACTDATAID, "artifactdataid");
       mapLong(ColumnEnumType.LOCATIONID, "locationid");
       mapLong(ColumnEnumType.ESTIMATEID, "estimateid");
       mapLong(ColumnEnumType.RESOURCEID, "resourceid");
       mapLong(ColumnEnumType.SCHEDULEID, "scheduleid");
       mapLong(ColumnEnumType.WORKID, "workid");
       mapLong(ColumnEnumType.BUDGETID, "budgetid");
       mapLong(ColumnEnumType.METHODOLOGYID, "methodologyid");
       mapLong(ColumnEnumType.BINARYVALUEID, "binaryvalueid");
       mapLong(ColumnEnumType.FORMELEMENTID, "formelementid");
       mapLong(ColumnEnumType.FORMID, "formid");
       mapLong(ColumnEnumType.VALIDATIONRULEID, "validationruleid");
       mapLong(ColumnEnumType.TEMPLATEID, "templateid");
       mapLong(ColumnEnumType.VIEWTEMPLATEID, "viewtemplateid");
       mapLong(ColumnEnumType.ELEMENTTEMPLATEID, "elementtemplateid");
       mapLong(ColumnEnumType.REQUIREMENTID, "requirementid");
       mapLong(ColumnEnumType.NOTEID, "noteid");
	   
	   /// BOOLEAN
	   mapBool(ColumnEnumType.ISCOMPRESSED, "iscompressed");
	   mapBool(ColumnEnumType.ISBLOB, "isblob");
	   mapBool(ColumnEnumType.ISENCIPHERED, "isenciphered");
	   mapBool(ColumnEnumType.ISPASSWORDPROTECTED, "ispasswordprotected");	    
	   mapBool(ColumnEnumType.ISVAULTED, "isvaulted");	  
	   mapBool(ColumnEnumType.PREFERRED, "preferred");	   
	   mapBool(ColumnEnumType.ENABLED, "enabled");	   
	   mapBool(ColumnEnumType.ISPOINTER, "ispointer");
       mapBool(ColumnEnumType.EXPIRES, "expires");
       mapBool(ColumnEnumType.GLOBALKEY, "globalkey");
       mapBool(ColumnEnumType.ORGANIZATIONKEY, "organizationkey");
       mapBool(ColumnEnumType.PRIMARYCREDENTIAL, "primarycredential");
       mapBool(ColumnEnumType.PRIMARYKEY, "primarykey");
       mapBool(ColumnEnumType.ITERATES, "iterates");
       mapBool(ColumnEnumType.ISBINARY, "isbinary");
       mapBool(ColumnEnumType.ALLOWNULL, "allownull");
       mapBool(ColumnEnumType.ISTEMPLATE, "istemplate");
       mapBool(ColumnEnumType.ISGRID, "isgrid");
       mapBool(ColumnEnumType.ISRULESET, "isruleset");
       mapBool(ColumnEnumType.ISREPLACEMENTRULE, "isreplacementrule");
       mapBool(ColumnEnumType.ENCRYPTEDKEY, "encryptedkey");


	   /// INT
	   mapInt(ColumnEnumType.SCORE, "score");
	   mapInt(ColumnEnumType.SIZE, "size");
	   mapInt(ColumnEnumType.RATING, "rating");
	   mapInt(ColumnEnumType.APPROVERLEVEL, "approverlevel");
       mapInt(ColumnEnumType.LOGICALORDER, "logicalorder");
       mapInt(ColumnEnumType.SESSIONDATASIZE, "sessiondatasize");
       mapInt(ColumnEnumType.CURRENTLEVEL, "currentlevel");
       mapInt(ColumnEnumType.ENDLEVEL, "endlevel");
       mapInt(ColumnEnumType.VALUEINDEX, "valueindex");
       mapInt(ColumnEnumType.SEEDLENGTH, "seedlength");


	   /// DATETIME
	   mapTime(ColumnEnumType.CREATEDDATE, "createddate");
	   mapTime(ColumnEnumType.STARTDATE, "startdate");
	   mapTime(ColumnEnumType.MODIFIEDDATE, "modifieddate");
	   mapTime(ColumnEnumType.EXPIRATIONDATE, "expirationdate");
	   mapTime(ColumnEnumType.ACCESSEDDATE, "accesseddate");
	   mapTime(ColumnEnumType.STARTTIME, "starttime");
	   mapTime(ColumnEnumType.ENDTIME, "endtime");
	   mapTime(ColumnEnumType.BIRTHDATE, "birthdate");
       mapTime(ColumnEnumType.SESSIONCREATED, "sessioncreated");
       mapTime(ColumnEnumType.SESSIONEXPIRATION, "sessionexpiration");
       mapTime(ColumnEnumType.SESSIONACCESSED, "sessionaccessed");
		mapTime(ColumnEnumType.AUDITDATE, "auditdate");
		mapTime(ColumnEnumType.AUDITRESULTDATE, "auditresultdate");
		mapTime(ColumnEnumType.AUDITEXPIRESDATE, "auditexpiresdate");
		mapTime(ColumnEnumType.AUDITRESULTDATA, "auditresultdata");
	   mapTime(ColumnEnumType.DUEDATE, "duedate");
	   mapTime(ColumnEnumType.CLOSEDDATE, "closeddate");
	   mapTime(ColumnEnumType.COMPLETEDDATE, "completeddate");
	   mapTime(ColumnEnumType.REOPENEDDATE, "reopeneddate");

	   /// DOUBLE
	   mapDouble(ColumnEnumType.UTILIZATION, "utilization");

   }
	   
	public static final Map<ColumnEnumType, String> Columns = Collections.unmodifiableMap(columnMap);
	public static final Map<ColumnEnumType, SqlDataEnumType> ColumnDataTypes = Collections.unmodifiableMap(dataMap);

}
