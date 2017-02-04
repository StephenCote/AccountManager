# NOTE: -Eo for OSX, -Po for other
data=$(cat $1)
exp='"objectId":"([A-Za-z0-9\\-]+)"'
if [[ $data =~ $exp ]] ; then echo "${BASH_REMATCH[1]}"; fi
