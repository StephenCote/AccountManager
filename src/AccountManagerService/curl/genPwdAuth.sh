echo Organization: $1
echo User: $2
cred="$3"
ph=$(echo $cred|openssl base64)
echo Credential: "$ph" from "$3"
echo Credential Type: HASHED_PASSWORD
echo "{\n\"credentialType\":\"HASHED_PASSWORD\",\n\"credential\":\"$ph\",\n\"subject\":\"$2\",\n\"organizationPath\":\"$1\"\n}" > cache/auth.$2.json
