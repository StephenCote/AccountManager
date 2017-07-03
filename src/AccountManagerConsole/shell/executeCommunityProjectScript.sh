# org, username,communityName, projectName, scriptName
# 
authName=$(./hash.sh "$1.$2")

lcObjName=$(./hash.sh "$1.COMMUNITY.$3")
lcCacheName="cache/object.$lcObjName.json"
if [ ! -f $lcCacheName ]; then
   res=$(./findCommunity.sh "$1" "$2" "$3")
fi
lcObjectId=$(./extractObjectId.sh $lcCacheName)

projObjName=$(./hash.sh "$1.COMMUNITY.$3.$4")
projCacheName="cache/object.$projObjName.json"
if [ ! -f $projCacheName ]; then
   res=$(./findCommunityProject.sh "$1" "$2" "$3" "$4")
fi
projObjectId=$(./extractObjectId.sh $projCacheName) 

scriptObjName=$(./hash.sh "$1.COMMUNITY.SCRIPT.$5")
scriptCacheName="cache/result.$scriptObjName.txt"
#if [ ! -f $scriptCacheName ]; then
   echo "Executing community project script for $lcObjectId community $projObjectId project"
   url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/script/community/exec/$lcObjectId/$projObjectId/$5")
   curl -sS -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url > $scriptCacheName
   echo ""
#fi

if [ -f $scriptCacheName ]; then
   cat $scriptCacheName
else
   echo "No result"
fi


