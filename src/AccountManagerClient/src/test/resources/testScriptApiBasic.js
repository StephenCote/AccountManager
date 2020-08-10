
load(ScriptResolver.contextPath() + "/src/test/resources/testScriptImport.js");

var haveUser = (user != null);
var haveContext = (clientContext != null);
console.log("testScriptApiBasic.js");

console.log("Have user: " + haveUser);
console.log("Name: " + user.getName());
var util = org.cote.accountmanager.client.util.AM6Util;
var homeDirectory = util.findObject(clientContext, org.cote.accountmanager.objects.DirectoryGroupType.class, org.cote.accountmanager.objects.types.NameEnumType.GROUP, "DATA", "~");

console.log("Have directory: " + (homeDirectory != null));
if(homeDirectory != null) console.log(homeDirectory.getPath());
