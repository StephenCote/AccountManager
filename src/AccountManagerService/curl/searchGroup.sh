objName=$(./hash.sh $1.GROUP.DATA.$2)
cacheName="cache/object.$objName.json"
#echo Cache Name: $cacheName
if [ ! -f $cacheName ]; then
   echo "Fetching ... "
   curl -sS -H "Authorization: Bearer $(cat cache/auth.$1.token)" http://127.0.0.1:8080/AccountManagerService/rest/search/GROUP/DATA/$2 > $cacheName
   echo "Cached: $cacheName"
fi
objectId=$(./extractObjectId.sh $cacheName)
echo "Object Id: ($objectId)"
