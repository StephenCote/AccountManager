cd ..\AccountManagerConsole\target
call java -jar AccountManagerConsole.jar -setup -rootPassword password -schema C:\Users\swcot\Documents\GitHub\AccountManager\db\postgres\AM6_PG9_Schema.sql -confirm
call java -jar AccountManagerConsole.jar -addOrganization -organization / -name FirstContact -adminPassword password -password password
cd ..\..\RocketConsole\target
C:\Users\swcot\workspace\RocketConsole\target>java -jar RocketConsole.jar -setup -adminPassword password -schema C:\Users\swcot\Documents\GitHub\Rocket\db\postgres\Rocket_PG9_Schema.sql -confirm
cd ..\..\AccountManagerConsole\target
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Public -name steve -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Public -name FeedbackUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Accelerant/Rocket -name FeedbackUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /Development -name RocketQAUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /FirstContact -name FeedbackUser -password password
call java -jar AccountManagerConsole.jar -addUser -adminPassword password -organization /FirstContact -name "test@foo.bar" -password password



