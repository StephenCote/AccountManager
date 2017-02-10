# org, username, communityName, countryCodes, useAlternate (true|false)
echo "Configuring traits ..."
res=$(./configureCommunityTraits.sh "$1" "$2" "$3")
echo "Configuring country info ..."
res=$(./configureCommunityCountryInfo.sh "$1" "$2" "$3")
echo "Configuring admin1 codes ..."
res=$(./configureCommunityAdmin1Codes.sh "$1" "$2" "$3")
echo "Configuring admin2 codes ..."
res=$(./configureCommunityAdmin2Codes.sh "$1" "$2" "$3")
echo "Configuring country data for $4 ..."
res=$(./configureCommunityCountryData.sh "$1" "$2" "$3" "$4" "$5")
echo ""
