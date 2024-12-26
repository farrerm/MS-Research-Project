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
	outputPath = '/home/mjfarrer/callgraph/density.csv'
	#functionOutputPath = '/home/mjfarrer/callgraph/functionsOut.csv'
	repoLocationPath = '/home/mjfarrer/26repos/'
	#fileOutputPath = '/home/mjfarrer/callgraph/output.java'
	#astJarPath = '/home/mjfarrer/callgraph/AST.jar'
	#astOutputPath = '/home/mjfarrer/callgraph/fgCSV.csv'
	dpJarPath = '/home/mjfarrer/callgraph/DP.jar'
	dataPath = '/home/mjfarrer/callgraph/densityStudy.csv'
	#invoksOutputPath = '/home/mjfarrer/callgraph/invocations2.csv'
	#annotsListPath = '/home/mjfarrer/callgraph/annotList.csv'
elif sys.argv[1] == 'local':
	repoNamePath = '/Users/matt/eclipse-workspace/visitorexample4/myDF4.csv'
	#repoNamePath = '/Users/matt/eclipse-workspace/visitorexample4/myDF5.csv'
	outputPath = '/Users/matt/eclipse-workspace/visitorexample4/density.csv'
	#functionOutputPath = '/Users/matt/eclipse-workspace/visitorexample4/functionsOut.csv'
	
	#repoLocationPath = '/Users/matt/eclipse-workspace/'
	repoLocationPath = '/Users/matt/Project/100repos/'
	#fileOutputPath = '/Users/matt/eclipse-workspace/visitorexample4/output.java'
	dpJarPath = '/Users/matt/eclipse-workspace/DP.jar'
	#astOutputPath = '/Users/matt/eclipse-workspace/testFolder/fgCSV.csv'
	#cgJarPath = '/Users/matt/eclipse-workspace/CG.jar'
	dataPath = '/Users/matt/eclipse-workspace/densityStudy.csv'
	#annotsListPath = '/Users/matt/eclipse-workspace/annotList.csv'
else:
	print('error, exiting now')
	sys.exit()

repoSrc = open(repoNamePath, newline='')
repoReader = csv.DictReader(repoSrc)

outCSV = open(outputPath, 'w', newline = '')
writer = csv.writer(outCSV)

writer.writerow(['Repo', 'Commit', 'Date', 'Methods', 'Methods w Checkers', 'Params', 'Params w Checkers', 'Fields', 'Fields w Checkers'])
outCSV.flush()



for row in repoReader:
	repo = row['repos']
	#'spring-projects/spring-framework'
	repoArr = repo.split('/')
	owner = repoArr[0]
	repoPath = repoLocationPath + repo
	index = 1
	
	#for i in range(1):
	for commit in RepositoryMining(repoPath).traverse_commits():
		
		print(repoPath)
		print(index)
		print(commit.hash)
		
		index += 1
	
		if commit.in_main_branch:
			
			target = repoLocationPath
			index2 = -1
			for i in range(0, len(repo)):
				if repo[i] == '/':
					index2 = i
					break
			owner = repo[:index2]
			shortRepo = repo[index2+1:]
			wd = os.getcwd()	
			os.chdir(target + owner + '/' + shortRepo)
			strAA = 'git'
			strBB = 'checkout'
			strCC = commit.hash
			subprocess.check_output([strAA, strBB, strCC])
			strAAA = 'java'
			strBBB = '-jar'
			strCCC = dpJarPath
			strDDD = target + owner + '/' + shortRepo
			#strDDD = '/Users/matt/eclipse-workspace/landingPad2/testOwner/testRepo4'
			subprocess.check_output([strAAA, strBBB, strCCC, strDDD, commit.hash])
			#print('just about to remove it')
			
			strAAA = 'git'
			strBBB = 'checkout'
			strCCC = 'master'
			try:
				subprocess.check_output([strAAA, strBBB, strCCC])
				os.chdir(wd)
			except:
				fetchData = [-1]
				myCounter = 0
				while len(fetchData) > 1 and int(pullData[0]) == -1:
					print('trying to fetch')
					print(myCounter)
					myCounter += 1
					fetchData = fetch()
					

				#subprocess.check_output([strAAA, strBBB, strCCC])
				strAAA = 'git'
				strBBB = 'reset'
				strCCC = '--hard'
				strDDD = 'origin/master'
				subprocess.check_output([strAAA, strBBB, strCCC, strDDD])
				strAAA = 'git'
				strBBB = 'checkout'
				strCCC = 'master'
				subprocess.check_output([strAAA, strBBB, strCCC])
				os.chdir(wd)
			
			data = open(dataPath, newline='')
			dataReader = csv.DictReader(data)
			
			
			for row in dataReader:
			
		
				listToWrite = []
				listToWrite.append(repo)
				listToWrite.append(commit.hash)
				listToWrite.append(commit.committer_date)

				listToWrite.append(row['Total Methods'])
				listToWrite.append(row['Num Methods w Checker'])
				listToWrite.append(row['Total Params'])
				listToWrite.append(row['Num Params w Checker'])	
				listToWrite.append(row['Total Fields'])		
				listToWrite.append(row['Num Fields w Checker'])
			
				writer.writerow(listToWrite)

				outCSV.flush()
			

