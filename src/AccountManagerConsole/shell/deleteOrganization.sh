# org, username
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.ORGANIZATION.UNKNOWN.$1")
cacheName="cache/object.$objName.json"
res=$(./findObject.sh "$1" "$2" ORGANIZATION UNKNOWN "$1")
if [ -f $parentCacheName ]; then
   objectId=$(./extractObjectId.sh $cacheName) 
   echo "Deleting organization $objectId"
   url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/organization/$objectId")
   curl -sS -X "DELETE" -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
   echo ""
else
   echo "Parent organization not found."
fi
#objectId=$(./extractObjectId.sh $cacheName)
#echo "Object Id: ($objectId)"
