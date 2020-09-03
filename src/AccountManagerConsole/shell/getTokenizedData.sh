# org,username,dataString
authName=$(./hash.sh "$1.$2")
url=$(./encode.sh "$(cat service.url)/token/resource/$3");
token=$(curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url")
echo "Token: $token"
