@echo off
cd ..\AccountManagerConsole\target
echo ***** Configuring AM6 Schema *****
call java -jar AccountManagerConsole.jar -setup -rootPassword password -schema "C:\Users\Stephen Cote\Documents\GitHub\AccountManager\db\postgres\AM6_PG9_Schema.sql" -confirm
echo ***** Adding FirstContact organization *****
call java -jar AccountManagerConsole.jar -addOrganization -organization / -name FirstContact -adminPassword password -password password
cd ..\..\RocketConsole\target
call java -jar RocketConsole.jar -setup -adminPassword password -schema "C:\Users\Stephen Cote\Documents\GitHub\Rocket\db\postgres\Rocket_PG9_Schema.sql" -confirm
cd ..\..\AccountManagerConsole\target
echo ***** Extending authorization schema *****
call java -jar AccountManagerConsole.jar -generate -type POLICY -execute
call java -jar AccountManagerConsole.jar -generate -type LIFECYCLE -execute
echo ***** Creating default accounts *****
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Public -name steve -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Public -name FeedbackUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Accelerant/Rocket -name FeedbackUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Development -name RocketQAUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /FirstContact -name FeedbackUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /FirstContact -name "test@foo.bar" -password password
cd ..\..\AccountManagerService
echo ***** Configured! *****
