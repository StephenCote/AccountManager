# org, username, communityName, userName
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.COMMUNITY.$3")
usrName=$(./hash.sh "$1.USER.$4")
cacheName="cache/object.$objName.json"
usrCacheName="cache/object.$usrName.json"
res=$(./findCommunity.sh "$1" "$2" "$3")
res=$(./getUser.sh "$1" "$2" "$4")
if [ -f $cacheName ]; then
   objectId=$(./extractObjectId.sh $cacheName) 
   usrObjectId=$(./extractObjectId.sh $usrCacheName)
   echo "Enroll user $usrObjectId as admin in community $objectId"
   url=$(./encode.sh "$(cat service.url)/community/enroll/admin/$objectId/$usrObjectId")
   curl -sS -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
   echo ""
fi
