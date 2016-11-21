curl -sS -X "POST" -H "Content-Type: application/json" -d @cache/auth.$1.json http://127.0.0.1:8080/AccountManagerService/rest/token/jwt/authenticate/token > cache/auth.$1.token
cat cache/auth.$1.token
echo ""
