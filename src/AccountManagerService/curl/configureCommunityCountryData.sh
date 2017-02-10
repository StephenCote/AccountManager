# org, username, communityName, codes (comma separated: US,CA), useAlt (true|false)
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.COMMUNITY.$3")
cacheName="cache/object.$objName.json"
res=$(./findCommunity.sh "$1" "$2" "$3")
if [ -f $parentCacheName ]; then
   objectId=$(./extractObjectId.sh $cacheName) 
   echo "Configure traits for community $objectId"
   url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/community/geo/country/LIFECYCLE/$objectId/$4/$5")
   echo $url
   curl -sS -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url"
   echo ""
fi
