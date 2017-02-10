#!/usr/bin/env python
import sys
import json
from pprint import pprint
json_data=open(sys.argv[1]).read()
srcData = json.loads(json_data)
mergeData = json.loads(sys.argv[2])

for attribute, value in mergeData.iteritems():
#        print attribute + "=" + value
        srcData[attribute]=value
with open(sys.argv[1] + ".update", 'w') as outfile:
    json.dump(srcData, outfile)
#print(srcData["description"])
#print(data["objectId"])
