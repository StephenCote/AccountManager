# org,username,path,type,objectName,jsonString
authName=$(./hash.sh "$1.$2")
#groupId=$(./getGroupObjectId.sh "$1" "$2" "$3")
res=$(./getObject.sh "$1" "$2" "$3" "$4" "$5")
objName=$(./hash.sh "$1.$4.$3.$5")
cacheName=cache/object.$objName.json
echo "Update cache name $cacheName"
#echo "Group Id: $groupId"
if [[ -f $cacheName ]]; then
   objectId=$(./extractObjectId.sh $cacheName)
   #echo Fetching $(cat service.url)/resource/$2/$groupId/$4
   url=$(./encode.sh "$(cat service.url)/resource/$4/$objectId/");
   #echo "Url: $url"
   res=$(./mergeObject.py "$cacheName" "$6")
   upCacheName="${cacheName}.update"
   curl -v -sS -X "POST" -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" -d @$upCacheName "$url"
   if [[ ! -s $cacheName ]]; then
      echo "File was empty"
      rm $cacheName
   fi
else
   echo "Cache $cacheName not found"
fi
