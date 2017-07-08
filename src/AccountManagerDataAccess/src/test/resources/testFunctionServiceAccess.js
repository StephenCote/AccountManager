/*
 * From ScriptService
 * organizationPath
 * user
 * 
 * contextUser - the user executing the policy
 * user - the user executing the script (same as contextUser)
 * subject - the subject of the request (could by any factory object)
 * logger - pointer to the ScriptService logger
 * fact - the FactType specified for the policy
 * matchFact - the matching FactType specified for the policy (for a script, it would be a FUNCTION of type JAVASCRIPT)
 * 
 */

var BaseService =  Java.type("org.cote.accountmanager.service.rest.BaseService");
var GroupEnumType = Java.type("org.cote.accountmanager.objects.types.GroupEnumType");
logger.info("Group: " + GroupEnumType.DATA);

var homeDirectory = BaseService.findGroup(subject,GroupEnumType.DATA, "~");


logger.info("testFunctionServiceAccess.js");
logger.info("Test in function script");
logger.info("Context User: " + (contextUser == null ? "null" : contextUser.getName()));
logger.info("Subject: " + (subject == null ? "null" : subject.getNameType() + " " + subject.getName()));


"" + 3;