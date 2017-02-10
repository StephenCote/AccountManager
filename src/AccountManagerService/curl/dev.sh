# org, username, communityName, projectName, altAdminName
echo "Clear local cache"
res=$(./clearLocalCache.sh)
echo "Delete community (if it exists)"
res=$(./deleteCommunity.sh "$1" "$2" "$3")
echo "Clear local cache (again)"
res=$(./clearLocalCache.sh)
echo "Add community"
res=$(./addCommunity.sh "$1" "$2" "$3")
echo "Add community project"
res=$(./addCommunityProject.sh "$1" "$2" "$3" "$4")
echo "Configure community admin"
res=$(./configureCommunityAdmin.sh "$1" "$2" "$3" "$5")
