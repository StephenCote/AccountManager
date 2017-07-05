#org, username, credential, checkCredential
#echo User: $2
cred="$3"
ckCred="$4"
ph=$(echo $cred|openssl base64)
ck=""
if [ ! -z "$4" ]; then
	ck=$(echo $ckCred|openssl base64)
fi
authName=$(./hash.sh "$1.$2")
#cacheName=cache/auth.$objName.json
mkdir -p cache
#echo Credential: "$ph" from "$3"
#echo Credential Type: HASHED_PASSWORD
echo "{\"credentialType\":\"HASHED_PASSWORD\",\"checkCredential\":\"$ck\",\"credential\":\"$ph\",\"subject\":\"$2\",\"organizationPath\":\"$1\"}" > cache/auth.$authName.json
