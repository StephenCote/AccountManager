# org, user, groupPath, fileName
authName=$(./hash.sh "$1.$2")
bname=$(basename "$4")
uid=$(./hash.sh "$1.DATA.$3.$bname")
cacheName=cache/object.$uid.json
tmpCacheName=$cacheName.tmp
groupHash=$(./hash.sh "$1.GROUP.DATA.$3")
groupCacheName=cache/object.$groupHash.json
res=$(./searchGroup.sh "$1" "$2" "$3")
groupOid=$(./extractObjectId.sh $groupCacheName)\

echo "{\"name\":\"$bname\",\"organizationPath\":\"$1\",\"blob\":\"true\",\"groupPath\":\"$3\",\"dataBytesStore\":\"$(cat "$4"|openssl base64 -A)\"}" > $tmpCacheName

url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/resource/DATA")
curl -sS -X "POST" -H "Content-Type: application/json" -d @$tmpCacheName -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url"
rm $tmpCacheName
echo ""
