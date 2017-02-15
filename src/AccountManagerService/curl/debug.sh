# org, username, communityName, projectName, altAdminName
echo "Delete community project"
res=$(./deleteCommunityProject.sh "$1" "$2" "$3" "$4")
echo "Clear local object cache"
res=$(./clearLocalCache.sh)

echo "Add community project"
res=$(./addCommunityProject.sh "$1" "$2" "$3" "$4")
echo "Configure community project"
res=$(./configureCommunityProjectRegion.sh "$1" "$2" "$3" "$4" 5 250)
echo "Evolving community project"
res=$(./evolveCommunityProjectRegion.sh "$1" "$2" "$3" "$4" 100 12)
echo "Generating report"
rep=$(./reportCommunityProjectRegion.sh "$1" "$2" "$3" "$4")
echo "$rep"
echo ""
