curl -sS -X "POST" -H "Content-Type: application/json" -d @auth.$1.json http://127.0.0.1:8080/AccountManagerService/rest/token/jwt/authenticate > auth.$1.token
cat auth.$1.token
echo ""
