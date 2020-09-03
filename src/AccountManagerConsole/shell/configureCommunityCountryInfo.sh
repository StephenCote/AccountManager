# org, username, communityName
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.COMMUNITY.$3")
cacheName="cache/object.$objName.json"
res=$(./findCommunity.sh "$1" "$2" "$3")
if [ -f $parentCacheName ]; then
   objectId=$(./extractObjectId.sh $cacheName) 
   echo "Configure traits for community $objectId"
   url=$(./encode.sh "$(cat service.url)/community/geo/countryInfo/LIFECYCLE/$objectId")
   curl -sS -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
   echo ""
fi
