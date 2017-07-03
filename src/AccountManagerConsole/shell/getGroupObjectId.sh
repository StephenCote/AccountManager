groupHash=$(./hash.sh "$1.GROUP.DATA.$3")
groupCacheName=cache/object.$groupHash.json
res=$(./searchGroup.sh "$1" "$2" "$3")
groupOid=$(./extractObjectId.sh $groupCacheName)
echo $groupOid
