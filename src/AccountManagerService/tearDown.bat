@echo off
cd ..\AccountManagerConsole\target
echo ***** Configuring AM6 and Rocket Schema *****
REM call java -jar AccountManagerConsole.jar -setup -rootPassword password -schema "C:\Users\Stephen Cote\Documents\GitHub\AccountManager\db\postgres\AM6_PG9_Schema.sql" -confirm
call java -jar AccountManagerConsole.jar -setup -rootPassword password -schema "C:\Users\Stephen Cote\Documents\GitHub\AccountManager\db\postgres\AM6_PG9_Schema.sql" -rocketSchema "C:\Users\Stephen Cote\Documents\GitHub\AccountManager\db\postgres\Rocket_PG9_Schema.sql" -confirm
echo ***** Adding FirstContact organization *****
echo NOTE: Organizations added at the root can only be added via the AccountManagerConsole.  This is by design.
call java -jar AccountManagerConsole.jar -addOrganization -organization / -name FirstContact -adminPassword password -password password
REM echo ***** Extending authorization schema *****
REM call java -jar AccountManagerConsole.jar -generate -type POLICY -execute
REM call java -jar AccountManagerConsole.jar -generate -type LIFECYCLE -execute
echo ***** Creating default accounts *****
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Public -name steve -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Development -name RocketQAUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /FirstContact -name "test@foo.bar" -password password
cd ..\..\AccountManagerService
echo ***** Configured! *****
