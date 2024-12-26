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
with open ('/Users/matt/eclipse-workspace/4myNulls.csv', newline='') as myNulls:

	nullsReader = csv.DictReader(myNulls)
	for row in nullsReader:
		repo = row['Owner'] + '/' + row['Project']
		print(repo)
		localCount = countNulls.get(repo, 0)
		localCount += 1
		countNulls[repo] = localCount


	with open ('/Users/matt/eclipse-workspace/Commits_Null.csv', 'w', newline='') as commitsNull:
		writer = csv.writer(commitsNull)
		writer.writerow(['Project', 'How Many Nullchekcers', 'Commits No Checkers', 'LOC No Checkers', 'Commits_Checkers', 'LOC_Checkers', 'Avg Files Per Commit No checkers', 'Avg Files Per Commit Yes checkers', 'Authors no Checker', 'Authors Yes Checker', 'Committers no Checker', 'Committer Yes Checker'])
		commitsNull.flush()

		with open ('/Users/matt/eclipse-workspace/Null_Touches.csv', newline='') as otherfile:
	
			otherReader = csv.DictReader(otherfile)
		#myList = list(otherReader)

			myDict = {}
			myAuthors = {}
			myCommitters = {}
			myAuthorsNull = {}
			myCommittersNull = {}

		
		#with open ('/Users/matt/eclipse-workspace/missing.csv', 'w', newline='') as otherfile:
	
			for row in otherReader:
			#missing.flush()
			#sum = 0
			#vals = []
			#row = myList[i]
				repo = row['Repo']

				data = myDict.get(repo, [0, 0, 0, 0, 0, 0, 0, 0])
				authors = myAuthors.get(repo, {})
				committers = myCommitters.get(repo, {})
				authorsN = myAuthorsNull.get(repo, {})
				committersN = myCommittersNull.get(repo, {})


				NNCommits = data[0]
			#Commits +=1
			#data[0] = tCommits
				NNLOC = data[1]
			#tLOC += int(row['Lines of Code Added or Removed'])
			#data[1] = tLOC
				nCommits = data[2]
				nLOC = data[3]

				numFilesNN = data[4]
				numFilesN = data[5]
				numCommitsNN = data[6]
				numCommitsN = data[7]

				if row['NullCheckers?'] == 'Yes':
					nCommits += 1
					nLOC += int(row['Lines of Code Added or Removed'])
					numFilesN += int(row['NumFiles'])
					numCommitsN += 1
					authorsN[row['Author Name']] = 1
					committersN[row['Comitter Name']] = 1
					myAuthorsNull[repo] = authorsN
					myCommittersNull[repo] = committersN

				else:
				#numFilesNN = data[5]
					NNCommits += 1
					NNLOC += int(row['Lines of Code Added or Removed'])
					numFilesNN += int(row['NumFiles'])
					numCommitsNN += 1
					authors[row['Author Name']] = 1
					committers[row['Comitter Name']] = 1
					myAuthors[repo] = authors
					myCommitters[repo] = committers

				data[0] = NNCommits
				data[1] = NNLOC

				data[2] = nCommits
				data[3] = nLOC
				data[4] = numFilesNN
				data[5] = numFilesN
				data[6] = numCommitsNN
				data[7] = numCommitsN
				myDict[repo] = data

			for key in myDict:
				data = myDict.get(key)
				vals = []
				vals.append(key)

				numCheckers = countNulls.get(key)
			#print(numCheckers)
				vals.append(numCheckers)

				vals.append(data[0])
				vals.append(data[1])
				vals.append(data[2])
				vals.append(data[3])	
				filesPerNNCommit = data[4] / data[6]
				filesPerNCommit = data[5] / data[7]
				
				vals.append(round(filesPerNNCommit, 2))
				vals.append(round(filesPerNCommit, 2))
				authors = myAuthors[key]
				vals.append(len(authors))
				authorsN = myAuthorsNull[key]
				vals.append(len(authorsN))
				committers = myCommitters[key]
				vals.append(len(committers))
				committersN = myCommittersNull[key]
				vals.append(len(committersN))


				writer.writerow(vals)

		

			
		
