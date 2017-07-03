# org, username, path
objName=$(./hash.sh "$1.GROUP.DATA.$3")
authName=$(./hash.sh "$1.$2")
cacheName="cache/object.$objName.json"
#echo Cache Name: $cacheName
if [ ! -f $cacheName ]; then
   echo "Fetching ... "
   url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/search/GROUP/DATA/$3")
   curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url" > $cacheName
   echo "Cached: $cacheName"
fi
objectId=$(./extractObjectId.sh $cacheName)
echo "Object Id: ($objectId)"
