groupHash=$(sh hash.sh $1.GROUP.DATA.$2)
groupCacheName=cache/object.$groupHash.json
res=$(sh searchGroup.sh $1 "$2")
groupOid=$(sh extractObjectId.sh $groupCacheName)
echo $groupOid
