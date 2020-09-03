# org, username, name
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.COMMUNITY.$3")
cacheName="cache/object.$objName.json"
#echo Cache Name: $cacheName
if [ ! -f $cacheName ]; then
   echo "Fetching ... "
url=$(./encode.sh "$(cat service.url)/community/find/$3")
   curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url" > $cacheName
fi
echo "Cache: $cacheName"
objectId=$(./extractObjectId.sh $cacheName)
echo "Object Id: ($objectId)"
