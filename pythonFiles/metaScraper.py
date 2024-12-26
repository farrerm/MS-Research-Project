import csv
import sys
from datetime import date
from pydriller import RepositoryMining
from pydriller import GitRepository
import subprocess
import re
import json
import time
import textwrap
import collections
from pathlib import Path
import os
import shutil

from collections import Counter

def firstParens(input):
	for i in range(0, len(input)):
		if input[i] == '(':
			return True
	return False

def getMethodName(input):
	retVal = ''
	for i in range(0, len(input)):
		if input[i] == '(':
			retVal = input[:i]
			break
	return retVal

def getRevisedParams(input):
	index = 0
	for i in range(0, len(input)):
		if input[i] == '(':
			index = i
			break
	retVal = []
	nuString = input[index+1:]
	if nuString == ')':
		return retVal
	nuString = nuString[:len(nuString)-1]
	if nuString == ')':
		return retVal
	bracketCount = 0
	myParamsTent = []
	#print('nuString: ' + nuString)
	i = 0
	while i < len(nuString):
		#print('i ' + str(i))
		if nuString[i] == '<':
			bracketCount += 1
		elif nuString[i] == '>':
			bracketCount -= 1
		elif nuString[i] == ',' and bracketCount == 0:
			temp = nuString[:i]
			if temp[0] == ' ':
				temp = temp[1:]
			myParamsTent.append(temp)
			nuString = nuString[i+1:]
			if nuString[0] == ' ':
				nuString = nuString[1:]

			#print(nuString)
			i=-1
		i += 1
	if nuString[0] == ' ':
		nuString = nuString[1:]
	myParamsTent.append(nuString)

	for i in range(0, len(myParamsTent)):
		miniParams = myParamsTent[i].split(' ')
		pNu = ''
		#print("size of miniParams: " + str(len(miniParams)))
		for j in range(0, len(miniParams)):
			if len(miniParams[j]) == 0:
				continue
			if miniParams[j][0] != '@' and j < len(miniParams)-2:
				pNu += miniParams[j] + ' '
			elif miniParams[j][0] != '@' and j < len(miniParams)-1:
				pNu += miniParams[j]
		retVal.append(pNu)
	retVal2 = []
	for i in range(0, len(retVal)):
		bracketCount = 0
		temp = retVal[i]
		j = 0
		while j < len(temp):
			if temp[j] == '<':
				bracketCount += 1
				j += 1
			elif temp[j] == '>':
				bracketCount -= 1
				j += 1
			elif temp[j] == ' ' and j < len(temp) -1:
				temp = temp[j+1:]
				j = 0
			else:
				j+=1
		retVal2.append(temp)
	retVal3 = []
	for i in range(0, len(retVal2)):
		retVal3.append('null')

	return retVal3

def cleanDecl(decl):
	parsedDecl = decl.split(' ')
	index = 0
	for i in range(0, len(parsedDecl)):
		if(firstParens(parsedDecl[i])):
			index = i
			break
	nuString = ''
	for i in range(index, len(parsedDecl)-1):
		nuString += parsedDecl[i] + ' '
	nuString += parsedDecl[len(parsedDecl)-1]
	revisedMethodName = getMethodName(nuString)
	paramList = getRevisedParams(nuString)
	revisedMethodName += '('
	if len(paramList) == 0:
		revisedMethodName += ')'
		return revisedMethodName
	for i in range(0, len(paramList)-1):
		revisedMethodName += paramList[i] + ','
	revisedMethodName += paramList[len(paramList)-1] + ')'
	return revisedMethodName


def getRelativePath(path, owner):
	pathList = path.split('/')
	# print(pathList)
	# print('owner = ' + owner)
	index = 0
	retVal = ''
	for i in range(len(pathList)):
		if pathList[i] == owner:
			index = i + 2
			break
	for i in range(index, len(pathList)-1):
		retVal += pathList[i] + '/'
	if (len(pathList)>0):
		retVal += pathList[len(pathList)-1]
	#print(retVal)
	return retVal

def ageDifference(startDate, endDate):
	#print('startDate ' + startDate)
	#print('endDate ' + endDate)
	startYear = startDate[0:4]
	startMonth = startDate[5:7]
	startDay = startDate[8:10]
	#print('startYear ' + startYear)
	#print('startMonth ' + startMonth)
	#print('startDay ' + startDay)
	
	endYear = endDate[0:4]
	endMonth = endDate[5:7]
	endDay = endDate[8:10]
	#print('end year ' + endYear)
	#print('end month ' + endMonth)
	#print('endDay ' + endDay)
	d0 = date(int(startYear), int(startMonth), int(startDay))
	d1 = date(int(endYear), int(endMonth), int(endDay))
	delta = d1 - d0
	return delta.days

def fetch():
	strAAA = 'git'
	strBBB = 'fetch'
	strCCC = 'origin'
	time.sleep(1)
	try:
		retList = [-1]
		outPut = subprocess.check_output([strAAA, strBBB, strCCC])
		retList = [0]
		return retList
	except:
		return retList

repoNamePath = ''
repoLocationPath = ''
metaDataPath = ''


if len(sys.argv) == 1:
	print('error, exiting now')
	sys.exit()	
if sys.argv[1] == 'production':
	repoNamePath = '/home/mjfarrer/callgraph/myDF4.csv'
	repoLocationPath = '/mnt/meta26repos/'
	metaDataPath = '/mnt/metaData.csv'

else:
	print('error, exiting now')
	sys.exit()


repoSrc = open(repoNamePath, newline='')
repoReader = csv.DictReader(repoSrc)

metaList = open(metaDataPath, 'w', newline = '')
metaDataWriter = csv.writer(metaList)
metaDataWriter.writerow(['Repo', 'Commit', 'Date', 'file before', 'file after', 'change Type', 'author', 'committer', 'lines added', 'lines removed', 'total lines of code'])
metaList.flush()

print('got here')
for row in repoReader:
	repo = row['repos']
	#'spring-projects/spring-framework'
	print(repo)
	repoArr = repo.split('/')
	owner = repoArr[0]
	repoPath = repoLocationPath + repo
	print(repoPath)

	index = 0
	#for i in range(1):
	for commit in RepositoryMining(repoPath).traverse_commits():
		#gr = GitRepository('/Users/matt/Project/100repos/spring-projects/spring-framework')
		#commit = gr.get_commit('e648245eb3e7a5baca8d2b281bd2d3a746ee9242')
		moddedFiles = 0
		print(repoPath)
		print(index)
		index += 1
		print(commit.hash)
		#fileMap2 = {}
		
		if commit.in_main_branch:
			
			print('modifications size ' + str(len(commit.modifications)))
			for change in commit.modifications:

				if change.change_type.name == 'ADD':
					#only want to consider .java files
					print('file added')
					if not change.new_path == None and not '.java' in change.new_path:
						continue
					if change.new_path == None:
						continue
					print('write add data')
#'Repo', 'Commit', 'Date', 'file', 'change Type', 'author', 'committer', 'lines added', 'lines removed', 'total lines'])
					try:
						listToWrite = []
						listToWrite.append(repo)
						listToWrite.append(commit.hash)
						listToWrite.append(commit.committer_date)
						listToWrite.append('')
						listToWrite.append(change.new_path)
						listToWrite.append('add')
						listToWrite.append(commit.author.name)
						listToWrite.append(commit.committer.name)
						listToWrite.append(change.added)
						listToWrite.append(change.removed)
						listToWrite.append(change.nloc)
						metaDataWriter.writerow(listToWrite)
						metaList.flush()
						print('wrote it')
					except:
						print('exception writing')
						continue
					
					
				elif change.change_type.name == 'DELETE':
					continue	
				#in this case we will parse source_code_before looking for null checkers
				elif change.change_type.name == 'RENAME':
					print('file renamed')
					if not change.new_path == None and not '.java' in change.new_path:
						continue
					if change.new_path == None:
						continue
					try:

						listToWrite = []
						listToWrite.append(repo)
						listToWrite.append(commit.hash)
						listToWrite.append(commit.committer_date)
						listToWrite.append(change.old_path)
						listToWrite.append(change.new_path)
						listToWrite.append('rename')
						listToWrite.append(commit.author.name)
						listToWrite.append(commit.committer.name)
						listToWrite.append(change.added)
						listToWrite.append(change.removed)
						listToWrite.append(change.nloc)
						metaDataWriter.writerow(listToWrite)
						metaList.flush()
						print('wrote it')
					except:
						print('exception writing')
						continue

				elif change.change_type.name == 'MODIFY':
					#print('modify')
					#w
					print('file modified')
					if not change.new_path == None and not '.java' in change.new_path:
						continue
					if change.new_path == None:
						continue

					print('write modify data')
					try:

						listToWrite = []
						listToWrite.append(repo)
						listToWrite.append(commit.hash)
						listToWrite.append(commit.committer_date)
						listToWrite.append(change.old_path)
						listToWrite.append(change.new_path)
						listToWrite.append('modify')
						listToWrite.append(commit.author.name)
						listToWrite.append(commit.committer.name)
						listToWrite.append(change.added)
						listToWrite.append(change.removed)
						listToWrite.append(change.nloc)
						metaDataWriter.writerow(listToWrite)
						metaList.flush()
						print('wrote it')
					except:
						print('exception writing')
						continue
			

