echo Organization: $1
echo User: $2
cred="$3"
ph=$(echo $cred|openssl base64)
mkdir -p cache
echo Credential: "$ph" from "$3"
echo Credential Type: HASHED_PASSWORD
echo "{\"credentialType\":\"HASHED_PASSWORD\",\"credential\":\"$ph\",\"subject\":\"$2\",\"organizationPath\":\"$1\"}" > cache/auth.$2.json
