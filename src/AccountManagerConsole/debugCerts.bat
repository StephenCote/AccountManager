@ECHO OFF
del /F /Q c:\projects\ssl\certificates\private\*.*
del /F /Q c:\projects\ssl\certificates\requests\*.*
del /F /Q c:\projects\ssl\certificates\root\*.*
del /F /Q c:\projects\ssl\certificates\signed\*.*
del /F /Q c:\projects\ssl\keys\private\*.*
del /F /Q c:\projects\ssl\keys\public\*.*
del /F /Q c:\projects\ssl\stores\key\*.*
del /F /Q c:\projects\ssl\stores\trust\*.*
cd target

java -jar AccountManagerConsole.jar -openssl -root -name development -password password -expiry 720 -export -store am5root -storePassword password -private -trust
java -jar AccountManagerConsole.jar -store am5root -storePassword password -name development -password password
java -jar AccountManagerConsole.jar -setCertificate -organization /Public -adminPassword password -name development -password password

java -jar AccountManagerConsole.jar -openssl -request -sign -signer development -name whitefrost -password password -expiry 720 -export -store am5trust -storePassword password -private -trust
java -jar AccountManagerConsole.jar -store am5trust -storePassword password -name whitefrost -password password

java -jar AccountManagerConsole.jar -openssl -request -export -sign -signer whitefrost -name whitefrost.public -password password -expiry 720 -export
java -jar AccountManagerConsole.jar -store am5trust -storePassword password -name whitefrost.public -password password

java -jar AccountManagerConsole.jar -openssl -request -export -sign -signer whitefrost -name whitefrost.system -password password -expiry 720 -export
java -jar AccountManagerConsole.jar -store am5trust -storePassword password -name whitefrost.system -password password

java -jar AccountManagerConsole.jar -openssl -request -export -sign -signer whitefrost -name whitefrost.development -password password -expiry 720 -export
java -jar AccountManagerConsole.jar -store am5trust -storePassword password -name whitefrost.development -password password

java -jar AccountManagerConsole.jar -openssl -request -export -sign -signer whitefrost -name whitefrost.firstcontact -password password -expiry 720 -export
java -jar AccountManagerConsole.jar -store am5trust -storePassword password -name whitefrost.firstcontact -password password

cd ..