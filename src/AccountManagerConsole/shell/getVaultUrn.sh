# org,username,path,type,objectName
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1."DATA"."~/.vault".$3")
./getObject.sh $1 $2 "~/.vault" "DATA" $3
cacheName=cache/object.$objName.json
urn=$(./extractUrn.sh $cacheName)
echo "Urn: ($urn)"