url=$(./encode.sh "$(cat service.url)/schema/entity")
curl -sS "$url" > "cache/entity.json"
echo "Entity map cached to cache/entity.json"
