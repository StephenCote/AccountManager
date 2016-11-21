# username-type-parent-objectname
groupId=$(sh getGroupObjectid.sh $1 $3)
objName=$(sh hash.sh $1.$2.$groupId.$4)
cacheName=cache/object.$objName.json
if [[ ! -f $cacheName ]]; then
   #echo Fetching http://127.0.0.1:8080/AccountManagerService/rest/resource/$2/$groupId/$4
   curl -sS -H "Authorization: Bearer $(cat cache/auth.$1.token)" http://127.0.0.1:8080/AccountManagerService/rest/resource/$2/$groupId/$4 > $cacheName
   if [[ ! -s $cacheName ]]; then
      echo "File was empty"
      rm $cacheName
   fi
fi
if [[ -s $cacheName ]];
then
   objectId=$(sh extractObjectId.sh $cacheName)
   echo "Object Id: ($objectId)"
else
   echo Invalid reference
fi
