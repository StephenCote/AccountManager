# org,username,name
authName=$(./hash.sh "$1.$2")
objName=$(./hash.sh "$1.USER.$3")
cacheName=cache/object.$objName.json
echo "Get Cache Name $cacheName"
if [[ ! -f $cacheName ]]; then
   url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/resource/USER/null/$3");
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
