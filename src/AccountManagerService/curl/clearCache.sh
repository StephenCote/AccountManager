authName=$(./hash.sh "$1.$2")
url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/cache/clearAll")
#echo $url
echo "Request to clear factory and authorization caches"
curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url"
echo ""
