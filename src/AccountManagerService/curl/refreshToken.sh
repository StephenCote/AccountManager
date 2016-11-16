curl -sS -X "POST" -H "Content-Type: application/json" -d @auth.$1.json http://127.0.0.1:8080/AccountManagerService/rest/token/jwt/authenticate > token.$1.txt
cat token.$1.txt
echo ""
