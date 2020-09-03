# org, username, communityName, projectName
authName=$(./hash.sh "$1.$2")

#res=$(./findCommunity.sh "$1" "$2" "$3")
#communityHash=$(./hash.sh "$1.COMMUNITY.$3")
#communityCacheName=cache/object.$communityHash.json
#communityOid=$(./extractObjectId.sh $communityCacheName)

objName=$(./hash.sh "$1.COMMUNITY.$3.$4")
cacheName="cache/object.$objName.json"
#echo Cache Name: $cacheName
if [ ! -f $cacheName ]; then
   echo "Fetching project from $communityOid "
url=$(./encode.sh "$(cat service.url)/community/find/$3/$4")
   curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url" > $cacheName
fi
echo "Cache: $cacheName"
objectId=$(./extractObjectId.sh $cacheName)
echo "Object Id: ($objectId)"
