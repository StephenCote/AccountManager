# org, user, groupPath, objectType, name
authName=$(./hash.sh "$1.$2")
uid=$(./hash.sh "TMP.$1.$2.$3.$4.$5")
cacheName=cache/object.$uid.json
tmpCacheName=$cacheName.tmp
groupHash=$(./hash.sh "$1.GROUP.DATA.$3")
groupCacheName=cache/object.$groupHash.json
res=$(./searchGroup.sh "$1" "$2" "$3")
groupOid=$(./extractObjectId.sh $groupCacheName)
echo "{\"name\":\"$5\",\"organizationPath\":\"$1\",\"groupPath\":\"$3\"}" > $tmpCacheName
url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/resource/$4")
curl -sS -X "POST" -H "Content-Type: application/json" -d @$tmpCacheName -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url"
rm $tmpCacheName
echo ""
