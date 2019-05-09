# org, username, objectType, subType, path
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.$2.ownerPolicy")
cacheName="cache/object.$objName.json"
#echo Cache Name: $cacheName
if [ ! -f $cacheName ]; then
   echo "Fetching ... "
url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/approval/policy/owner")
   curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url" > $cacheName
fi
echo "Cache: $cacheName"
objectId=$(./extractObjectId.sh $cacheName)
echo "Object Id: ($objectId)"
