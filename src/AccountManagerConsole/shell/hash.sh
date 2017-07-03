hash=$(echo -n "$1" | openssl dgst -sha256 | sed 's/^.* //')
echo $hash
