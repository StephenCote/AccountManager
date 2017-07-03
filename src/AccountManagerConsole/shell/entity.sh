url=$(./encode.sh "http://127.0.0.1:8080/AccountManagerService/rest/schema/entity")
curl -sS "$url" > "cache/entity.json"
echo "Entity map cached to cache/entity.json"
