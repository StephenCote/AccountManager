#!/usr/bin/env python
import sys
import json
from pprint import pprint
json_data=open(sys.argv[1]).read()
data = json.loads(json_data)
print(data["urn"])
