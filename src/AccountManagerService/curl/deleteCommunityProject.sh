# org, username,communityName, projectName
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.COMMUNITY.$3.$4")
cacheName="cache/object.$objName.json"
if [ ! -f $cacheName ]; then
   res=$(./findCommunityProject.sh "$1" "$2" "$3" "$4")
fi
objectId=$(./extractObjectId.sh $cacheName) 
echo "Deleting community project $objectId"
url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/community/project/$objectId")
   curl -sS -X "DELETE" -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
   echo ""
