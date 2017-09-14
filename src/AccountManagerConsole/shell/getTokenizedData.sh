# org,username,dataString
authName=$(./hash.sh "$1.$2")
url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/token/resource/$3");
token=$(curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url")
echo "Token: $token"
