# org, user, name
authName=$(./hash.sh "$1.$2")
uid=$(./hash.sh "TMP.$1.$2.USER.$3")
cacheName=cache/object.$uid.json
tmpCacheName=$cacheName.tmp
#groupHash=$(./hash.sh "$1.GROUP.DATA.$3")
#groupCacheName=cache/object.$groupHash.json
#res=$(./searchGroup.sh "$1" "$2" "$3")
#groupOid=$(./extractObjectId.sh $groupCacheName)
echo "{\"name\":\"$3\",\"organizationPath\":\"$1\"}" > $tmpCacheName
url=$(./encode.sh "$(cat service.url)/resource/USER")
curl -sS -X "POST" -H "Content-Type: application/json" -d @$tmpCacheName -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url"
rm $tmpCacheName
echo ""
