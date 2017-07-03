# org, username,communityName
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.COMMUNITY.$3")
cacheName="cache/object.$objName.json"
if [ ! -f $cacheName ]; then
   res=$(./findCommunity.sh "$1" "$2" "$3")
fi
objectId=$(./extractObjectId.sh $cacheName) 
echo "Deleting community $objectId"
url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/community/$objectId")
   curl -sS -X "DELETE" -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
   echo ""
