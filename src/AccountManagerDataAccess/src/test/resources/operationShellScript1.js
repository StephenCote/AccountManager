let OperationResponseEnumType = Java.type("org.cote.accountmanager.objects.OperationResponseEnumType");
let respError = OperationResponseEnumType.ERROR;
let respSucceeded = OperationResponseEnumType.SUCCEEDED;
let respFailed = OperationResponseEnumType.FAILED;
let respUnknown = OperationResponseEnumType.UNKNOWN;
logger.info("Comparing " + fact.getUrn() + " to " + match.getUrn());

respFailed;