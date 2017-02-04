@echo off
cd ..\AccountManagerConsole\target
echo ***** Generating Organization Certificates *****
call java -jar AccountManagerConsole.jar -openssl -root -name root -password "password" -expiry 720
call java -jar AccountManagerConsole.jar -openssl -request -name organizations -password password -expiry 720 -sign -signer root -export
call java -jar AccountManagerConsole.jar -openssl -request -name development -password password -expiry 720 -sign -signer organizations -export
call java -jar AccountManagerConsole.jar -openssl -request -name public -password password -expiry 720 -sign -signer organizations -export
call java -jar AccountManagerConsole.jar -openssl -request -name system -password password -expiry 720 -sign -signer organizations -export
call java -jar AccountManagerConsole.jar -openssl -request -name accelerant -password password -expiry 720 -sign -signer organizations -export
call java -jar AccountManagerConsole.jar -openssl -request -name rocket -password password -expiry 720 -sign -signer accelerant -export
call java -jar AccountManagerConsole.jar -openssl -request -name firstcontact -password password -expiry 720 -sign -signer organizations -export
REM call java -jar AccountManagerConsole.jar -openssl -request -name test@foo.bar -password password -expiry 720 -sign -signer firstcontact -export
echo ***** Generating Development Server Certificate *****
call java -jar AccountManagerConsole.jar -openssl -request -name localhost.localdomain -password password -expiry 720 -sign -signer organizations -export
call java -jar AccountManagerConsole.jar -openssl -request -name localhost -password password -expiry 720 -sign -signer organizations -export

echo ***** Building Certificate Trust Store *****
call java -jar AccountManagerConsole.jar -store am6trust -storePassword password -password password -private -name organizations -trust
call java -jar AccountManagerConsole.jar -store am6trust -storePassword password -password password -private -name development -trust
call java -jar AccountManagerConsole.jar -store am6trust -storePassword password -password password -private -name public -trust
call java -jar AccountManagerConsole.jar -store am6trust -storePassword password -password password -private -name system -trust
call java -jar AccountManagerConsole.jar -store am6trust -storePassword password -password password -private -name accelerant -trust
call java -jar AccountManagerConsole.jar -store am6trust -storePassword password -password password -private -name rocket -trust
call java -jar AccountManagerConsole.jar -store am6trust -storePassword password -password password -private -name firstcontact -trust
echo ***** Building Certificate Key Store *****
call java -jar AccountManagerConsole.jar -store am6key -storePassword password -password password -private -name organizations
call java -jar AccountManagerConsole.jar -store am6key -storePassword password -password password -private -name development
call java -jar AccountManagerConsole.jar -store am6key -storePassword password -password password -private -name public
call java -jar AccountManagerConsole.jar -store am6key -storePassword password -password password -private -name system
call java -jar AccountManagerConsole.jar -store am6key -storePassword password -password password -private -name accelerant
call java -jar AccountManagerConsole.jar -store am6key -storePassword password -password password -private -name rocket
call java -jar AccountManagerConsole.jar -store am6key -storePassword password -password password -private -name firstcontact
echo ***** Building Development Server Store *****
call java -jar AccountManagerConsole.jar -store tomcat -storePassword password -password password -private -name localhost.localdomain -trust
call java -jar AccountManagerConsole.jar -store tomcat -storePassword password -password password -name localhost.localdomain

call java -jar AccountManagerConsole.jar -store tomcat -storePassword password -password password -name "test@foo.bar"
echo ***** Setting Certificates To Organizations *****
call java -jar AccountManagerConsole.jar -setCertificate -organization /Public -password password -adminPassword password -name public
call java -jar AccountManagerConsole.jar -setCertificate -organization /Development -password password -adminPassword password -name development
call java -jar AccountManagerConsole.jar -setCertificate -organization /System -password password -adminPassword password -name system
call java -jar AccountManagerConsole.jar -setCertificate -organization /Accelerant -password password -adminPassword password -name accelerant
call java -jar AccountManagerConsole.jar -setCertificate -organization /Accelerant/Rocket -password password -adminPassword password -name rocket
call java -jar AccountManagerConsole.jar -setCertificate -organization /FirstContact -password password -adminPassword password -name firstcontact
cd ..\..\AccountManagerService