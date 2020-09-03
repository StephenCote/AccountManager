# org,username,path,type,objectName
authName=$(./hash.sh "$1.$2")
groupId=$(./getGroupObjectId.sh "$1" "$2" "$3")
objName=$(./hash.sh "$1.$4.$3.$5")
cacheName=cache/object.$objName.json
#echo "Group Id: $groupId"
echo "Get Cache Name $cacheName"
if [[ ! -f $cacheName ]]; then
   #echo Fetching $(cat service.url)/resource/$2/$groupId/$4
   url=$(./encode.sh "$(cat service.url)/resource/$4/$groupId/$5");
   echo "Url=$url"
   curl -sS -H "Authorization: Bearer $(cat cache/auth.$authName.token)" "$url" > $cacheName
   if [[ ! -s $cacheName ]]; then
      echo "File was empty"
      rm $cacheName
   fi
fi
if [[ -s $cacheName ]];
then
   objectId=$(./extractObjectId.sh $cacheName)
   echo "Object Id: ($objectId)"
else
   echo Invalid reference
fi
