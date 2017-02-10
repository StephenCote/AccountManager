#!/usr/bin/env python
import sys
import json
from pprint import pprint
#print sys.argv[1]
#from optparse import OptionParser

#parser = OptionParser()
#parser.add_option("-f", "--file", dest="filename", help="specify json file", metavar="FILE")
#(options, args) = parser.parse_args()
json_data=open(sys.argv[1]).read()
data = json.loads(json_data)
print(data["objectId"])
