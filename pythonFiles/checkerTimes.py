import csv
import sys
from datetime import date
from pydriller import RepositoryMining
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
				nuString= nuString[1:]

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
	#print(pathList)
	#print('owner = ' + owner)
	index = 0
	retVal = ''
	for i in range(len(pathList)):
		if pathList[i] == owner:
			index = i + 2
			break
	for i in range (index, len(pathList)-1):
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
	#print('startYear ' + str(int(startYear)))
	#print('startMonth ' + str(int(startMonth)))
	#print('startDay ' + str(int(startDay)))
	
	endYear = endDate[0:4]
	endMonth = endDate[5:7]
	endDay = endDate[8:10]
	d0 = date(int(startYear), int(startMonth), int(startDay))
	d1 = date(int(endYear), int(endMonth), int(endDay))
	delta = d1 - d0
	return delta.days

repoNamePath = ''
commitOutputPath = ''
functionOutputPath = ''
repoLocationPath = ''
fileOutputPath = ''
astJarPath = ''
astOutputPath = ''
invoksOutputPath = ''

if len(sys.argv) == 1:
	print('error, exiting now')
	sys.exit()	
if sys.argv[1] == 'production':
	repoNamePath = '/home/mjfarrer/callgraph/myDF4.csv'
	commitOutputPath = '/home/mjfarrer/callgraph/annotsOut.csv'
	functionOutputPath = '/home/mjfarrer/callgraph/functionsOut.csv'
	repoLocationPath = '/home/mjfarrer/26repos/'
	fileOutputPath = '/home/mjfarrer/callgraph/output.java'
	astJarPath = '/home/mjfarrer/callgraph/AST.jar'
	astOutputPath = '/home/mjfarrer/callgraph/fgCSV.csv'
	cgJarPath = '/home/mjfarrer/callgraph/CG.jar'
	invoksOutputPath = '/home/mjfarrer/callgraph/invocations2.csv'
elif sys.argv[1] == 'local':
	repoNamePath = '/Users/matt/eclipse-workspace/visitorexample4/myDF4.csv'
	commitOutputPath = '/Users/matt/eclipse-workspace/visitorexample4/annotsOut.csv'
	functionOutputPath = '/Users/matt/eclipse-workspace/visitorexample4/functionsOut.csv'
	repoLocationPath = '/Users/matt/eclipse-workspace/'
	fileOutputPath = '/Users/matt/eclipse-workspace/visitorexample4/output.java'
	astJarPath = '/Users/matt/eclipse-workspace/AST.jar'
	astOutputPath = '/Users/matt/eclipse-workspace/testFolder/fgCSV.csv'
	cgJarPath = '/Users/matt/eclipse-workspace/CG.jar'
	invoksOutputPath = '/Users/matt/eclipse-workspace/invocations2.csv'
else:
	print('error, exiting now')
	sys.exit()

repoSrc = open(repoNamePath, newline='')
repoReader = csv.DictReader(repoSrc)
repoSet = set()
for row in repoReader:
	repoSet.add(row['repos'])
dataSrc = open('/users/matt/eclipse-workspace/12fgTestNullAnnotations.csv', newline='')
dataReader = csv.DictReader(dataSrc)


outCSV = open('/users/matt/eclipse-workspace/visitorexample4/2dataNumbers.csv', 'w', newline = '')
writer = csv.writer(outCSV)
writer.writerow(['Repo', 'Commit', 'NullCheckers Added', 'Commit Date', 'Commit Message'])

prevHash = ''
count = 0
curList = []
for row in dataReader:
	if row['Repo'] in repoSet:
		curHash = row['Commit ID']
		if curHash != prevHash:
			count = 1
			if len(curList) != 0:
				writer.writerow(curList)
				outCSV.flush()
			curList = []
			curList.append(row['Repo'])
			curList.append(row['Commit ID'])
			curList.append(1)
			curList.append(row['Date'])
			curList.append(row['Commit Message'])
		else:
			curList[2] += 1
		prevHash = curHash
writer.writerow(curList)
outCSV.flush()







