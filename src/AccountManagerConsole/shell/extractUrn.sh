# NOTE: -Eo for OSX, -Po for other
# NOTE 2: Because some objects are complex, it's not reliable to just find the first objectId since there may be more than one
# So use the python script to parse the complex object and take the id from the top most object
data=$(./extractUrn.py "$1")
#data=$(cat $1)
#exp='"urn":"([A-Za-z0-9\\-:]+)"'
#if [[ $data =~ $exp ]] ; then echo "${BASH_REMATCH[1]}"; fi
echo "$data"
