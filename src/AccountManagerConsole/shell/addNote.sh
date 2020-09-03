# org, user, groupPath, name, text
authName=$(./hash.sh "$1.$2")
uid=$(./hash.sh "$1.DATA.$3.$4")
cacheName=cache/object.$uid.json
tmpCacheName=$cacheName.tmp
groupHash=$(./hash.sh "$1.GROUP.DATA.$3")
groupCacheName=cache/object.$groupHash.json
res=$(./searchGroup.sh "$1" "$2" "$3")
groupOid=$(./extractObjectId.sh $groupCacheName)\

echo "{\"name\":\"$4\",\"organizationPath\":\"$1\",\"groupPath\":\"$3\",\"text\":\"$5\"}" > $tmpCacheName

url=$(./encode.sh "$(cat service.url)/resource/NOTE")
resp=$(curl -sS -X "POST" -H "Content-Type: application/json" -d @$tmpCacheName -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url")
if [ $resp == "true" ]; then
   echo "Created note $3/$4"
   rm $tmpCacheName
else
   echo "Failed to create $3/$4"
fi
echo ""
