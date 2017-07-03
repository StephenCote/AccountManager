# org, username, communityName
authName=$(./hash.sh "$1.$2")
echo "Add community $3"
url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/community/new/$3")
curl -sS -H "Content-Type: application/json" -H "Authorization: Bearer $(cat cache/auth.$authName.token)" $url
echo ""
