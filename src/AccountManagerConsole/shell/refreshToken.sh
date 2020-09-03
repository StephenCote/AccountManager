authName=$(./hash.sh "$1.$2")
curl -sS -X "POST" -H "Content-Type: application/json" -d @cache/auth.$authName.json $(cat service.url)/token/jwt/authenticate/token > "cache/auth.$authName.token"
cat "cache/auth.$authName.token"
echo ""
