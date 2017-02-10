./clearLocalCache.sh
./genPwdAuth.sh "$1" "$2" "$3"
./refreshToken.sh "$1" "$2"
credToken=$(./hash.sh "$1.$2")
rm cache/auth.$credToken.json
echo ""
