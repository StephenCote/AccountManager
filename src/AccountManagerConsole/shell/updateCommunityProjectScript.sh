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
scriptCacheName="cache/script.$scriptObjName.txt"
if [ -f $scriptCacheName ]; then
   echo "Updating community project script for $lcObjectId community $projObjectId project"
   url=$(./encode.sh "$(cat service.url)/script/community/$lcObjectId/$projObjectId/$5")
   curl -sS -X "POST" -H "Content-Type: text/plain" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" --data-binary "@$scriptCacheName" $url
   echo ""
else
   echo "Script not available"
fi


