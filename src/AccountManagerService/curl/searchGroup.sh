curl -H "Authorization: Bearer $(cat auth.$1.token)" http://127.0.0.1:8080/AccountManagerService/rest/search/GROUP/DATA/$2
