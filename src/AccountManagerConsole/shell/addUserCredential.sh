# org, username, targetUserName, newUserPassword, currentPassword
# Note: currentPassword is required for non-admin initiated resets
#
parentObjName=$(./hash.sh "$1.USER.$3")
userCacheName="cache/object.$parentObjName.json"

res=$(./getUser.sh "$1" "$2" "$3")
if [ -f $userCacheName ]; then
   userObjectId=$(./extractObjectId.sh $userCacheName) 
   echo "Composing add $1/$3 credential request in $1 (id: $userObjectId)..."
   # create the new credential stub
   res=$(./genPwdAuth.sh "$1" "$3" "$4" "$5")

   authName=$(./hash.sh "$1.$2")
   newAuthName=$(./hash.sh "$1.$3")
   url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/credential/USER/$userObjectId")
   curl -sS -X "POST" -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" -d @cache/auth.$newAuthName.json $url
   echo ""
   echo "Note: Previous credentials are demoted from being primary, but persist until they are removed or expired"
   echo "Requesting new token for $3"
   res=$(./auth.sh "$1" "$3" "$4")
   echo "Token: $res"
   echo ""
else
   echo "User $3 not found in $1."
fi
