# org, username,communityName, projectName, scriptName
# 
res1=$(./updateCommunityProjectScript.sh "$1" "$2" "$3" "$4" "$5")
#echo "$res1"
res2=$(./executeCommunityProjectScript.sh "$1" "$2" "$3" "$4" "$5")
echo "$res2"
