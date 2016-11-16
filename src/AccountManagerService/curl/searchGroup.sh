curl -H "Authorization: Bearer $(cat token.$1.txt)" http://127.0.0.1:8080/AccountManagerService/rest/search/GROUP/DATA/$2
