#!/usr/bin/env python


import os
import re
import sys
import time
import py_compile


if __name__=="__main__":

	res_file = open(sys.argv[1],'r')
	i = 0
	sim = 0
	while 1:
#	for fileLine in res_file:
		fileLine = res_file.readline()
		if not fileLine:
			break
		fileLine = fileLine.strip()
		if len (fileLine) > 0:
			m = re.match(r'Messages sent:\s+(\S+)', fileLine)
			if m is not None:
				sent = m.groups()[0]
				fileLine = res_file.readline()
				fileLine = fileLine.strip()
				m = re.match(r'Messages delivered:\s+(\S+)', fileLine)
				delivered = m.groups()[0]
				fileLine = res_file.readline()
				fileLine = res_file.readline()
				fileLine = fileLine.strip()
				m = re.match(r'Messages lost:\s+(\S+)', fileLine)
				lost = m.groups()[0]
				fileLine = res_file.readline()
				fileLine = res_file.readline()
				fileLine = res_file.readline()
				fileLine = fileLine.strip()
				m = re.match(r'Message stat \[sumRouteLength\]:\s+(\S+)', fileLine)
				sumLength = m.groups()[0]
				if i == 0:
					print(str(sim))
				print(str(i*10) + "," + sent + "," + delivered + "," + lost + "," + sumLength)
				if i == 9:
					i = 0
					sim = sim + 1
					print("")
				else:
					i = i + 1
	res_file.close()
	