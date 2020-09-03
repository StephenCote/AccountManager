authName=$(./hash.sh "$1.$2")
res=$(./clearLocalCache.sh)
url=$(./encode.sh "$(cat service.url)/cache/clearAll")
#echo $url
echo "Request to clear factory and authorization caches"
curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url"
echo ""
