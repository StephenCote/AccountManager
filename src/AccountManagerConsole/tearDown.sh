@echo off
cd target
echo ***** Configuring AM6 and Rocket Schema *****
java -jar AccountManagerConsole.jar -setup -rootPassword password -schema "/home/steve/eclipse-workspace/AM6_PG9_Schema.sql" -rocketSchema "/home/steve/eclipse-workspace/Rocket_PG9_Schema.sql" -confirm
echo ***** Adding FirstContact organization *****
echo NOTE: Organizations added at the root can only be added via the AccountManagerConsole.  This is by design.
java -jar AccountManagerConsole.jar -addOrganization -organization / -name FirstContact -adminPassword password -password password
REM echo ***** Extending authorization schema *****
REM call java -jar AccountManagerConsole.jar -generate -type POLICY -execute
REM call java -jar AccountManagerConsole.jar -generate -type LIFECYCLE -execute
echo ***** Creating default accounts *****
java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Public -name steve -password password
java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Development -name RocketQAUser -password password
java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /FirstContact -name "test@foo.bar" -password password
cd ..
echo ***** Configured! *****
