#!/usr/bin/env python
import sys
import json
from pprint import pprint
json_data=open("cache/entity.json").read()
data = json.loads(json_data)
print(data["personType"])
