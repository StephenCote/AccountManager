groupHash=$(./hash.sh $1.GROUP.DATA.$2)
groupCacheName=cache/object.$groupHash.json
res=$(./searchGroup.sh $1 "$2")
groupOid=$(./extractObjectId.sh $groupCacheName)
echo $groupOid
