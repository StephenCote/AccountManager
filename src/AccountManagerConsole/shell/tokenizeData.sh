# org,username,dataString
authName=$(./hash.sh "$1.$2")
url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/token/resource");
token=$(curl -sS -X "POST" -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" -d "$3" "$url")
echo "Token: $token"
