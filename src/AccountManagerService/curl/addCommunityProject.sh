# org, username, communityName, projectName
authName=$(./hash.sh "$1.$2")
echo "Add community project $3 $4"
res=$(./findCommunity.sh "$1" "$2" "$3")
communityHash=$(./hash.sh "$1.COMMUNITY.$3")
communityCacheName=cache/object.$communityHash.json
communityOid=$(./extractObjectId.sh $communityCacheName)
#projectHash=$(./hash.sh "$1.COMMUNITY.$3.$4")
#projectCacheName=cache/object.$projectHash.json
#if [ ! -f $projectCacheName ]; then
#   echo "Fetching ..."
   url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/community/new/$communityOid/$4")
   curl -sS -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
#fi
#communityOid=$(./extractObjectId.sh $communityCacheName)

#url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/community/new/$3")
#curl -sS -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
echo ""
