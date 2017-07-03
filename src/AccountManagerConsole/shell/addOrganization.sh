# org, username, newOrgName, newOrgPassword
parentObjName=$(./hash.sh "$1.ORGANIZATION.UNKNOWN.$1")
#objName=$(./hash.sh "$1/$2.ORGANIZATION.UNKNOWN.$1/$2")
#cacheName="cache/object.$objName.json"
parentCacheName="cache/object.$parentObjName.json"

res=$(./findObject.sh "$1" "$2" ORGANIZATION UNKNOWN "$1")
#echo "Result: $res"
#echo Cache Name: $cacheName
if [ -f $parentCacheName ]; then
   parentObjectId=$(./extractObjectId.sh $parentCacheName) 
   echo "Composing add $1/$3 organization request in $1 (id: $parentObjectId)..."
   # create the credential for the new admin
   res=$(./genPwdAuth.sh "$1/$3" Admin "$4")
   #echo "Res Auth: $res"
   authName=$(./hash.sh "$1.$2")
   newAuthName=$(./hash.sh "$1/$3.Admin")
   url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/organization/$parentObjectId/$3")
   echo "Url: $url"
   curl -sS -X "POST" -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" -d @cache/auth.$newAuthName.json $url
   #echo "Cached: $cacheName"
   echo "Requesting token for new administrator"
   res=$(./refreshToken.sh "$1/$3" "Admin" "$4")
   echo "Token: $res"
   echo ""
else
   echo "Parent organization not found."
fi
#objectId=$(./extractObjectId.sh $cacheName)
#echo "Object Id: ($objectId)"
