# org, username,communityName, projectName, appName, usePerm, useGroups, seedSize, maxSize, distribution
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

echo "Generating community project application"
url=$(./encode.sh "$(cat service.url)/community/generate/application/$lcObjectId/$projObjectId/$5/$6/$7/$8/$9/${10}")
curl -sS -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
echo ""
