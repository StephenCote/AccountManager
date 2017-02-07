echo Organization: $1
echo User: $2
echo Group Path: $3
echo Object Type: $4
bname=$(basename "$5")
echo Object Name: $bname
echo File: $5
uid=$(./hash.sh "$2.DATA.$3.$bname")
cacheName=cache/object.$uid.json
tmpCacheName=$cacheName.tmp
groupHash=$(./hash.sh "$2.GROUP.DATA.$3")
groupCacheName=cache/object.$groupHash.json
#echo Invoke search group
res=$(./searchGroup.sh $2 "$3")
#echo finished: $res
groupOid=$(./extractObjectId.sh $groupCacheName)
echo Group Id: $groupOid
#objData=cache/data.$uid.json
#fvar="$5"
#cat "$fvar"
#echo $(cat "$5"|openssl base64) > $objData
echo "{\n\"name\":\"$bname\",\n\"organizationPath\":\"$1\",\n\"blob\":\"true\",\n\"groupPath\":\"$3\",\n\"dataBytesStore\":\"$(cat "$5"|openssl base64 -A)\"\n}" > $tmpCacheName
curl -sS -X "POST" -H "Content-Type: application/json" -d @$tmpCacheName -H "Authorization: Bearer $(cat cache/auth.$2.token)" http://127.0.0.1:8080/AccountManagerService/rest/resource/$4
rm $tmpCacheName
echo ""
