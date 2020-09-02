authName=$(./hash.sh "$1.$2")
curl -sS -X "POST" -H "Content-Type: application/json" -d @cache/auth.$authName.json http://0.0.0.0:8080/AccountManagerService/rest/token/jwt/authenticate/token > "cache/auth.$authName.token"
cat "cache/auth.$authName.token"
echo ""
