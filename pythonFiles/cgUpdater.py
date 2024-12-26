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

def isHash(line):
	for i in range(len(line)):
		if line[i] == ',':
			return False
	return True

def getInvocs(line):
	index1 = 0
	index2 = 0
	retVal = set()
	while index1 < len(line):
		#bypass filename
		if line[index1] == ',':
			index1 += 1
			index2 = index1
			break
		index1 += 1
	while index2 < len(line):
		if line[index2] == ';':
			break
		index2 += 1
	index1 = index2 + 1
	index2 = index1
	parensCount = 0
	bracketCount = 0
	while(index2 < len(line)):
		if line[index2] == '(':
			parensCount += 1
		elif line[index2] == ')':
			parensCount -= 1
		elif line[index2] == '<':
			bracketCount += 1
		elif line[index2] == '>':
			bracketCount -= 1
		elif parensCount == 0 and bracketCount == 0 and line[index2] == ',':
			nextInvoc = line[index1:index2]
			retVal.add(nextInvoc)
			index1 = index2 + 1
			index2 = index1
		elif line[index2] == '\n':
			nextInvoc = line[index1:index2]
			retVal.add(nextInvoc)
		index2 += 1
	return retVal

def extractFunctionDeclOrFieldDecl(line):
	index1 = 0
	index2 = 0
	while index1 < len(line):
		if line[index1] == ',':
			index1 += 1 
			index2 = index1
			break
		index1 += 1
	while index2 < len(line):
		if line[index2] == ';':
			break
		index2 += 1
	curFunc = line[index1:index2]
	return curFunc

def isFunction(line):
	if line[len(line) -1] == ')':
		return True
	return False

def getClass(fDecl):
	fList = fDecl.split('.')
	retVal = ''
	for i in range(len(fList)-1):
		retVal += fList[i]
	return retVal


def getName(fDecl):
	fList = fDecl.split('.')
	return fList[len(fList)-1]

def getAllAliases(decl, aliasList):
	dName = getName(decl)
	dClass = getClass(decl)
	retVal = [decl]
	for cSet in aliasList:
		if dClass in cSet:
			for oClass in cSet:
				candidate = oClass + '.' + dName
				if candidate != decl:
					retVal.append(candidate)
	return retVal

repoNamePath = ''
repoLocationPath = ''
metaDataPath = ''
leisDataPath = ''
mattsDataPath = ''
outputPath = ''


if len(sys.argv) == 1:
	print('error, exiting now')
	sys.exit()	
if sys.argv[1] == 'production':
	repoNamePath = '/home/mjfarrer/callgraph/myDF4.csv'
	repoLocationPath = '/mnt/meta26repos/'
	metaDataPath = '/mnt/metaData.csv'
elif sys.argv[1] == 'local':
	outputPath = '/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/nuStetho/nufacebookstethofunctionsOut.csv'
	#leisDataPath = '/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/stetho.csv'
	#mattsDataPath = '/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/annotList.csv'
	inheritancePath = '/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/nuStetho/inheritance.csv'
	annotsOutPath = '/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/nuStetho/facebookstethoannotsOut.csv'
	annotListPath = '/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/nuStetho/facebookstethoannotList.csv'

	functionsOutPath = '/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/nuStetho/facebookstethofunctionsOut.csv'
	callDumpPath = '/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/nuStetho/nuStetho.txt'


else:
	print('error, exiting now')
	sys.exit()

commitSet = set()
commitList = []

outCSV = open(outputPath, 'w', newline = '')
writer = csv.writer(outCSV)
writer.writerow(['Commit', 'Declaration', 'min downstream distance', 'min upstream distance'])
outCSV.flush()

#read inheritance info into dictionary {}
inheritanceSrc = open(inheritancePath, newline='')
inheritanceReader = csv.DictReader(inheritanceSrc)
inheritanceDict = {}
for row in inheritanceReader:
	bigList = inheritanceDict.get(row['Commit'], [])
	smallList = row['Class Set'].split('\n')
	classSet = set()
	for function in smallList:
		classSet.add(function)
	bigList.append(classSet)
	inheritanceDict[row['Commit']] = bigList

#functionsOutDict{} gives us all declarations that changed per commit
functionsOutSrc = open(functionsOutPath, newline='')
functionsOutReader = csv.DictReader(functionsOutSrc)
functionsOutDict = {}
for row in functionsOutReader:
	#create commit list and commit set
	if row['Commit'] not in commitSet:
		commitList.append(row['Commit'])
		commitSet.add(row['Commit'])
	functionSet = functionsOutDict.get(row['Commit'], set())
	functionSet.add(row['function'])
	functionsOutDict[row['Commit']] = functionSet

#{} read static declarations and invocations into dictionary{}
myFile = open(callDumpPath, 'r')
callDumpDict = {}
curHash = ''
#need to change this to record fields as well as functions
#this is only extracting declarations
#we need to also get function invocations and fields
while True:
	line = myFile.readline()
	#print(line)
	if isHash(line):
		line = line[0:len(line)-1]
		#print(line)
		curHash = line
		
	else:
		curDecl = extractFunctionDeclOrFieldDecl(line)
		invocSet = getInvocs(line)
		#if isFunction(curFunction):
			#print(curFunction)
		functionDeclDict = callDumpDict.get(curHash, {})
		#cSet.add(curFunction)
		functionDeclDict[curDecl] = invocSet
			#callDumpDict[curHash] = cSet
		callDumpDict[curHash] = functionDeclDict
	if not line:
		break
myFile.close()

#filter this based on decls in txt file
annotListSrc = open(annotListPath, newline='')
annotListReader = csv.DictReader(annotListSrc)
annotListDict = {}
for row in annotListReader:
	declSet = annotListDict.get(row['Commit'], set())
	#filter hereD
	if row['Commit'] in callDumpDict:
		txtDeclDict = callDumpDict[row['Commit']]
		if row['FQN Parent'] in txtDeclDict:
			declSet.add(row['FQN Parent'])
			annotListDict[row['Commit']] = declSet

for commit in commitList:
	print('commit ' + commit)
	functionCallerDict = {}
	functionCalleeDict = {}
	annotListSet = annotListDict[commit]
	aliasList = inheritanceDict[commit]
	functionsOutSet = functionsOutDict[commit]
	functionDumpDict = callDumpDict[commit]
	zeroSet = set()
	plusOneSet = set()
	plusTwoSet = set()
	plusThreeSet = set()
	plusFourSet = set()
	minusOneSet = set()
	minusTwoSet = set()
	minusThreeSet = set()
	minusFourSet = set()
	print('building zero set')
	for decl in annotListSet:
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			if d in functionsOutSet:
				functionCallerDict[d] = 0
				functionCalleeDict[d] = 0
				zeroSet.add(d)
	print('building plusOneSet')
	for decl in zeroSet:
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			if d in functionDumpDict:
				invocSet = functionDumpDict[d]
				for invoc in invocSet:
					iList = getAllAliases(invoc, aliasList)
					for i in iList:

						if i not in zeroSet and i in functionsOutSet:
							plusOneSet.add(i)
							functionCalleeDict[i] = 1
	print('building plusTwoSet')
	for decl in plusOneSet:
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			if d in functionDumpDict:
				invocSet = functionDumpDict[d]
				for invoc in invocSet:
					iList = getAllAliases(invoc, aliasList)
					for i in iList:
						if i not in zeroSet and i not in plusOneSet and i in functionsOutSet:
							plusTwoSet.add(i)
							functionCalleeDict[i] = 2
	print('building plusThreeSet')
	for decl in plusTwoSet:
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			if d in functionDumpDict:
				invocSet = functionDumpDict[d]
				for invoc in invocSet:
					iList = getAllAliases(invoc, aliasList)
					for i in iList:
						if i not in zeroSet and i not in plusOneSet and i not in plusTwoSet and i in functionsOutSet:
							plusThreeSet.add(i)
							functionCalleeDict[i] = 3
	print('building plusFourSet')
	for decl in plusThreeSet:
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			if d in functionDumpDict:
				invocSet = functionDumpDict[d]
				for invoc in invocSet:
					iList = getAllAliases(invoc, aliasList)
					for i in iList:
						if i not in zeroSet and i not in plusOneSet and i not in plusTwoSet and i not in plusThreeSet and i in functionsOutSet:
							plusFourSet.add(i)
							functionCalleeDict[i] = 4

	###ok, now do callers / minus direction
	##########	
	print('building minusOneSet')
	for decl in functionsOutSet:
		#attribute any invocations that are in aliases to decl
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			#print(d)
			if d in functionDumpDict:
				invocSet = functionDumpDict[d]
				for invoc in invocSet:
					iList = getAllAliases(invoc, aliasList)
					for i in iList:
						if i in zeroSet and decl not in zeroSet:
							minusOneSet.add(decl)
							functionCallerDict[decl] = 1
	print('building minusTwoSet')
	for decl in functionsOutSet:
		#attribute any invocations that are in aliases to decl
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			if d in functionDumpDict:
				invocSet = functionDumpDict[d]
				for invoc in invocSet:
					iList = getAllAliases(invoc, aliasList)
					for i in iList:
						if i in minusOneSet and decl not in zeroSet and decl not in minusOneSet:
							minusTwoSet.add(decl)
							functionCallerDict[decl] = 2
	print('building minusThreeSet')
	for decl in functionsOutSet:
		#attribute any invocations that are in aliases to decl
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			if d in functionDumpDict:
				invocSet = functionDumpDict[d]
				for invoc in invocSet:
					iList = getAllAliases(invoc, aliasList)
					for i in iList:
						if i in minusTwoSet and decl not in zeroSet and decl not in minusOneSet and decl not in minusTwoSet:
							minusThreeSet.add(decl)
							functionCallerDict[decl] = 3
	print('building minusFourSet')
	for decl in functionsOutSet:
		#attribute any invocations that are in aliases to decl
		declList = getAllAliases(decl, aliasList)
		for d in declList:
			if d in functionDumpDict:
				invocSet = functionDumpDict[d]
				for invoc in invocSet:
					iList = getAllAliases(invoc, aliasList)
					for i in iList:
						if i in minusThreeSet and decl not in zeroSet and decl not in minusOneSet and decl not in minusTwoSet and decl not in minusThreeSet:
							minusFourSet.add(decl)
							functionCallerDict[decl] = 4

	for function in functionsOutSet:
		listToWrite = []
		listToWrite.append(commit)
		listToWrite.append(function)
		if function in functionCalleeDict:
			listToWrite.append(functionCalleeDict[function])
		else:
			listToWrite.append(-1)
		if function in functionCallerDict:
			listToWrite.append(functionCallerDict[function])
		else:
			listToWrite.append(-1)
		writer.writerow(listToWrite)
		outCSV.flush()








			






#oops, need to add in here all aliases
#annotListSrc = open(annotListPath, newline='')
#annotListReader = csv.DictReader(annotListSrc)
#3annotListDict = {}
#for row in annotListReader:
#	inRepoSet = callDumpDict.get(row['Commit'])
#	if row['function'] in inRepoSet:
#		functionSet = annotListDict.get(row['Commit'], set())
#		functionSet.add(row['function'])
#		functionsOutDict[row['Commit']] = functionSet

###now these are the functions at distance 0
# can use these to compute call grpah distances

####tomorrow ...












