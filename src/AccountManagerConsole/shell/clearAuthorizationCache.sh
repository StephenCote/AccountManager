authName=$(./hash.sh "$1.$2")
url=$(./encode.sh "$(cat service.url)/cache/clearAuthorization")
echo "Request to clear authorization caches"
curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url"
echo ""
