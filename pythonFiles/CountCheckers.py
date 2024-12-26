import csv
import sys
import numpy as np
import matplotlib.pyplot as plt
from pydriller import RepositoryMining
import subprocess
import re
import json
import time
import textwrap
import collections
from pathlib import Path
import os

from collections import Counter


def contains(smallList, bigList):
	for i in range(0, len(bigList)):
		flag = True
		otherList = bigList[i]

		for j in range(0, len(smallList)):
			if not smallList[j] == otherList[j]:
				flag = False
		if flag == True:
			return True
	return False

def remove(smallList, bigList):
	#print ('BigList')
	#print(bigList)
	#print('SmallList')
	#print(smallList)
	for i in range(0, len(bigList)):
		flag = True
		otherList = bigList[i]


		for j in range(0, len(smallList)):
			first = smallList[j]
			second = otherList[j]
			if not first == second:
				flag = False
		if flag == True:
			del bigList[i]
			return
	

def containsPath(path, bigList):
	for i in range(0, len(bigList)):
		flag = True
		otherList = bigList[i]

		#for j in range(0, len(smallList)):
		if not otherList[0] == path:
			flag = False
		if flag == True:
			return True
	return False

def same(first, second):
	for i in range(0, len(first)):
		flag = True
		
		if not first[i] == second[i]:
			return False

		
	return True



##begin main
cnt = Counter()
#myDict = {}

count = 0
csv.field_size_limit(sys.maxsize)
#build dictionary of null annotations
#keys are projects, values are list of null annotations for that project
#spamreader = csv.DictReader(csvfile)
countNulls = {}
print('hello')
myNulls = open ('/Users/matt/eclipse-workspace/visitorexample4/NullTouches2.csv', newline='')

nullsReader = csv.DictReader(myNulls)
for row in nullsReader:
	count += int(row['numCheckers'])
print(count)
		#localCount = countNulls.get(repo, 0)
		#localCount += 1
		#countNulls[repo] = localCount

