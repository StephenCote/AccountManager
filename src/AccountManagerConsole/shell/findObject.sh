# org, username, objectType, subType, path
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.$3.$4.$5")
cacheName="cache/object.$objName.json"
#echo Cache Name: $cacheName
if [ ! -f $cacheName ]; then
   echo "Fetching ... "
url=$(./encode.sh "$(cat service.url)/search/$3/$4/$5")
   curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url" > $cacheName
fi
echo "Cache: $cacheName"
objectId=$(./extractObjectId.sh $cacheName)
echo "Object Id: ($objectId)"
