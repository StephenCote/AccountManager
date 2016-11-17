@echo off
cd target
call java -jar AccountManagerClient.jar -server localhost -url http://localhost:8080/AccountManagerService/rest
call java -jar AccountManagerClient.jar -server localhost -organization /Public -username steve -password password