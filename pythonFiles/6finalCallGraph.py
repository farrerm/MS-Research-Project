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
	annotsListPath = '/home/mjfarrer/callgraph/annotList.csv'
elif sys.argv[1] == 'local':
	#repoNamePath = '/Users/matt/eclipse-workspace/visitorexample4/myDF4.csv'
	repoNamePath = '/Users/matt/eclipse-workspace/visitorexample4/myDF5.csv'
	commitOutputPath = '/Users/matt/eclipse-workspace/visitorexample4/annotsOut.csv'
	functionOutputPath = '/Users/matt/eclipse-workspace/visitorexample4/functionsOut.csv'
	
	repoLocationPath = '/Users/matt/eclipse-workspace/'
	#repoLocationPath = '/Users/matt/Project/100repos/'
	fileOutputPath = '/Users/matt/eclipse-workspace/visitorexample4/output.java'
	astJarPath = '/Users/matt/eclipse-workspace/AST.jar'
	astOutputPath = '/Users/matt/eclipse-workspace/testFolder/fgCSV.csv'
	cgJarPath = '/Users/matt/eclipse-workspace/CG.jar'
	invoksOutputPath = '/Users/matt/eclipse-workspace/invocations2.csv'
	annotsListPath = '/Users/matt/eclipse-workspace/annotList.csv'
else:
	print('error, exiting now')
	sys.exit()

repoSrc = open(repoNamePath, newline='')
repoReader = csv.DictReader(repoSrc)
outCSV = open(commitOutputPath, 'w', newline = '')
writer = csv.writer(outCSV)
functionsOut = open(functionOutputPath, 'w', newline = '')
functionWriter = csv.writer(functionsOut)

annotList = open(annotsListPath, 'w', newline = '')
annotListWriter = csv.writer(annotList)
annotListWriter.writerow(['Repo', 'Commit', 'Date', 'path', 'FQN Parent', 'FQN Child', 'Annotation', 'kind of checker change', 'Type'])
annotList.flush()
functionWriter.writerow(['Repo', 'Commit', 'Date', 'path', 'function', 'checker change?', 'kind of checker change', 'min distance +', 'min distance -', '# of commits to file', 'function size' , 'age of file (days)'])
writer.writerow(['Repo', 'Commit', 'Date','Commit msg', 'Annots Added', 'Annots Removed', 'Annots Modified', 'Annoted Functions/Fields Changed', 'Overall functions/fields changed', 'files changed w checker change', 'overall files changed'])
outCSV.flush()
functionsOut.flush()

for row in repoReader:
	repo = row['repos']
	#'spring-projects/spring-framework'
	repoArr = repo.split('/')
	owner = repoArr[0]
	repoPath = repoLocationPath + repo
	index = 1
	fileAges = {} #to calculate age of file
	fileNumCommits = {} # track # of commits to a file
	functionPrevSize = {}
	functionCurSize = {}
	#for i in range(1):
	for commit in RepositoryMining(repoPath).traverse_commits():
		#gr = GitRepository('/Users/matt/Project/100repos/spring-projects/spring-framework')
		#commit = gr.get_commit('e648245eb3e7a5baca8d2b281bd2d3a746ee9242')
		

		moddedFiles = 0
		print(repoPath)
		print(index)
		print(commit.hash)
		#fileMap2 = {}
		index += 1
		numAdded = 0
		numModified = 0
		numDeleted = 0
		functionAnnotChangeinclDel = set() # functions that have a checker that change
										# somehow.  include deleted functions
										#include checker -> no checker change?
		functionAnnotChangeCurrent = set() #functions that have a checker that changes
										#don't include deleted functions
										#must have checker in current state
		functionAllChangeinclDel = set() #we have this in addition to functionAllCurrent to record
											#functions that are deleted in the commit
		functionAllChangeCurrent = set() #This should track all functions declared in this commit
									#if this is more than change set we should
									#populate during call graph stage
		
		functionAnnotAdd = set() #set of functions that had annotation added
		functionAnnotRemove = set() #removed - including deleted functions
		functionAnnotModified = set() #modified
		#some judgement call on which, could overlap among all 3
		functionAnnotDelete = {}
		#need to track total number of functions w annotations that are deleted
		
		#will use dictionary structure with FQNParent, FQN, annotSet
		#functionAllDelete = set()
		
		functionCallerDist = {} #to track min distance from a currently declared annot change?
		functionCalleeDist = {}
		#gr = GitRepository(repoPath)
		#commit = gr.get_commit('a86a698e5b3394c9b6721a784c8fe251611ff16b')
		#fileSet = set()
		#if True:
		if commit.in_main_branch:
			annotationModdedFiles = 0 #this should include annotated deleted functions
			fileFlag = False #to record annotation changes including function deletions
			fileMap = {} #we will get the path from CG
			#print('modifications size ' + str(len(commit.modifications)))
			for change in commit.modifications:

				TypeDict = {}
				fileFlag = False
				linesDeleted = set()
				linesAdded = set()
				functionChanged = {}
				for entry in change.diff_parsed['added']:
					linesAdded.add(entry[0])
				for entry in change.diff_parsed['deleted']:
					linesDeleted.add(entry[0])
				#print(change.change_type.name)
				if change.change_type.name == 'ADD':
					#only want to consider .java files
					if not change.new_path == None and not '.java' in change.new_path:
						continue
					fileNumCommits[change.new_path] = 1
					f = open(fileOutputPath, 'w+')
					f.write(change.source_code)
					f.close()
					str1 = 'java'
					str2 = '-jar'
					str3 = astJarPath
					outPut = subprocess.check_output([str1, str2, str3])
					readingDict = {}
					csvfile = open(astOutputPath, newline='')
					readingDict = csv.DictReader(csvfile)
					#os.remove(astOutputPath)
					fileAges[change.new_path] = commit.committer_date
					for row in readingDict:
						#we can detect any nullcheckers bc they will have the substring "null"
						nuDecl = row['FQN Parent']
						if nuDecl[len(nuDecl)-1] == ')':
							nuDecl = cleanDecl(row['FQN Parent'])
						nuDecl = row['Class'] + '.' + nuDecl
						
						fileMap[nuDecl] = change.new_path
						#print('commit.committer_date ' + str(commit.committer_date))
						functionPrevSize[nuDecl] = 0
						functionCurSize[nuDecl] = int(row['End Pos']) - int(row['Start Pos']) + 1
						if 'Null' in row['Annotation'] or 'null' in row['Annotation']:
							#print('found checker')
							fileFlag = True
							numAdded += 1
						#	found = true
							#fileSet.add(change.new_path)
							functionAnnotChangeinclDel.add(nuDecl)
							functionAnnotChangeCurrent.add(nuDecl)
							
							functionAllChangeinclDel.add(nuDecl)
							functionAllChangeCurrent.add(nuDecl)

							functionAnnotAdd.add(nuDecl)
							#need to write annotation
							myList = []
							myList.append(repo)
							myList.append(commit.hash)
							myList.append(commit.committer_date)
							fileString = ''
							if not change.new_path is None:
								fileString = change.new_path
							myList.append(fileString)
							myList.append(nuDecl)
							myList.append(row['FQN'])
							myList.append(row['Annotation'])
							myList.append('add')
							myList.append(row['Type'])
							annotListWriter.writerow(myList)
							annotList.flush()
						else:	
							functionAllChangeinclDel.add(nuDecl)
							functionAllChangeCurrent.add(nuDecl)
				elif change.change_type.name == 'DELETE':	
				#in this case we will parse source_code_before looking for null checkers
					if not change.old_path is None and '.java' not in change.old_path:
						continue
					commitNums = fileNumCommits.get(change.old_path, 0)
					commitNums += 1
					fileNumCommits[change.old_path] = commitNums
					f = open(fileOutputPath, 'w+')
					f.write(change.source_code_before)
					f.close()
					str1 = 'java'
					str2 = '-jar'
					str3 = astJarPath
					outPut = subprocess.check_output([str1, str2, str3])
					#my_file = Path("/Users/matt/eclipse-workspace/testFolder/fgCSV.csv")
					readingDict = {}
					csvfile = open(astOutputPath, newline='')
					readingDict = csv.DictReader(csvfile)
					os.remove(astOutputPath)
					for row in readingDict:
						#we can detect any nullcheckers bc they will have the substring "null"
						nuDecl = row['FQN Parent']
						if nuDecl[len(nuDecl)-1] == ')':
							nuDecl = cleanDecl(row['FQN Parent'])
						nuDecl = row['Class'] + '.' + nuDecl
						
						functionPrevSize[nuDecl] = int(row['End Pos']) - int(row['Start Pos']) + 1
						#functionCurSize[nuDecl]
						functionCurSize[nuDecl] = 0
						
						fileMap[nuDecl] = change.old_path
						if 'Null' in row['Annotation'] or 'null' in row['Annotation']:
							numDeleted += 1
							fileFlag = True
							#found = true
							functionAnnotChangeinclDel.add(nuDecl)
							functionAllChangeinclDel.add(nuDecl)

							functionAnnotRemove.add(nuDecl)
							########
							fqnMap = functionAnnotDelete.get(nuDecl, {})
							annotSet = fqnMap.get(row['FQN'], set())
							annotSet.add(row['Annotation'])
							fqnMap[row['FQN']] = annotSet
							functionAnnotDelete[nuDecl] = fqnMap

							myList = []
							myList.append(repo)
							myList.append(commit.hash)
							myList.append(commit.committer_date)
							fileString = ''
							if not change.old_path is None:
								fileString = change.new_path
							myList.append(fileString)
							myList.append(nuDecl)
							myList.append(row['FQN'])
							myList.append(row['Annotation'])
							myList.append('delete')
							myList.append(row['Type'])
							annotListWriter.writerow(myList)
							annotList.flush()

							
						else:
							functionAllChangeinclDel.add(nuDecl)
				elif change.change_type.name == 'MODIFY' or change.change_type.name == 'RENAME':
					#print('modify')
					#wasThereBeforeChange = set() #to record fqns of functions present
					if change.change_type.name == 'MODIFY':
						commitNums = fileNumCommits.get(change.new_path, 0)
						commitNums += 1
						fileNumCommits[change.new_path] = commitNums
					elif change.change_type.name == 'RENAME':
						commitNums = fileNumCommits.get(change.old_path, 0)
						commitNums += 1
						fileNumCommits[change.new_path] = commitNums
						#fileAges[change.new_path] = fileAges[change.old_path]
						try:
							fileAges[change.new_path] = fileAges[change.old_path]
						except Exception:
							fileAges[change.new_path] = commit.committer_date
					if change.source_code_before is None:
						continue
					if not change.new_path is None and '.java' not in change.new_path:
						continue

					f = open(fileOutputPath, 'w+')
					f.write(change.source_code_before)
					f.close()
					str1 = 'java'
					str2 = '-jar'
					str3 = astJarPath
					outPut = subprocess.check_output([str1, str2, str3])
					#my_file = Path("/Users/matt/eclipse-workspace/testFolder/fgCSV.csv")
					#readingDict = {}
					csvfile1 = open(astOutputPath, newline='')
					beforeDict = csv.DictReader(csvfile1)
					os.remove(astOutputPath)
					f = open(fileOutputPath, 'w+')
					f.write(change.source_code)
					f.close()
					str1 = 'java'
					str2 = '-jar'
					str3 = astJarPath
					outPut = subprocess.check_output([str1, str2, str3])
					#my_file = Path("/Users/matt/eclipse-workspace/testFolder/fgCSV.csv")
					#readingDict = {}
					csvfile2 = open(astOutputPath, newline='')
					afterDict = csv.DictReader(csvfile2)
					os.remove(astOutputPath)
					fqnAnnotParentsBefore = {}
					fqnAnnotParentsAfter = {}

					#fqnNoAnnotsBeforeNoLinesDeleted = set()
					fqnNoAnnotsBeforeLinesDeleted = set()
					fqnAnnotsBeforeLinesDeleted = set()
					#fqnAnnotsBeforeLinesDeleted = set()

					for row in beforeDict:
						nuDecl = row['FQN Parent']
						if nuDecl[len(nuDecl)-1] == ')':
							nuDecl = cleanDecl(row['FQN Parent'])
						nuDecl = row['Class'] + '.' + nuDecl
						subTypeDict = TypeDict.get(nuDecl, {})
						subTypeDict[row['FQN']] = row['Type']
						TypeDict[nuDecl] = subTypeDict
						#if row['Package'] != 'null':
						#	nuDecl = row['Package'] + '.' + row['Class'] + '.' + nuDecl
						#else:
						#	nuDecl = row['Class'] + '.' + nuDecl	
						#print('modify - putting into fileMap - ' + nuDecl)
						#fileMap[nuDecl] = change.new_path

						functionPrevSize[nuDecl] = int(row['End Pos']) - int(row['Start Pos']) + 1
						if 'Null' in row['Annotation'] or 'null' in row['Annotation']:				
							fqnAnnotDict = fqnAnnotParentsBefore.get(nuDecl, {})
							annotSet = fqnAnnotDict.get(row['FQN'], set())
							annotSet.add(row['Annotation'])
							fqnAnnotDict[row['FQN']] = annotSet
							fqnAnnotParentsBefore[nuDecl] = fqnAnnotDict
							flag = False

							for i in range(int(row['Start Pos']), int(row['End Pos'])+1):
								if i in linesDeleted:
									#flag = True
									functionAnnotChangeinclDel.add(nuDecl)
									functionAllChangeinclDel.add(nuDecl)
								#functionAllSet.add(nuDecl)
									fqnAnnotsBeforeLinesDeleted.add(nuDecl)

									#functionPrevSize[nuDecl] = int(row['End Pos']) - int(row['Start Pos']) + 1
									break
						
						elif row['Annotation'] == '':
							#fqnNoAnnotsBefore.add(nuDecl)
							for i in range(int(row['Start Pos']), int(row['End Pos'])+1):
								if i in linesDeleted:
									#functionAnnotChangeinclDel.add(nuDecl)
									fqnNoAnnotsBeforeLinesDeleted.add(nuDecl)
									functionAllChangeinclDel.add(nuDecl)
								#functionAllSet.add(nuDecl)
									#functionPrevSize[nuDecl] = functionCurSize(nuDecl)
									#functionPrevSize[nuDecl]= int(row['End Pos']) - int(row['Start Pos']) + 1
									break

					for row in afterDict:
						#print(row)
						nuDecl = row['FQN Parent']
						if nuDecl[len(nuDecl)-1] == ')':
							nuDecl = cleanDecl(row['FQN Parent'])
						nuDecl = row['Class'] + '.' + nuDecl
						

						subTypeDict = TypeDict.get(nuDecl, {})
						subTypeDict[row['FQN']] = row['Type']
						TypeDict[nuDecl] = subTypeDict
						##if row['Package'] != 'null':
						##	nuDecl = row['Package'] + '.' + row['Class'] + '.' + nuDecl
						#else:
						#	nuDecl = row['Class'] + '.' + nuDecl
						fileMap[nuDecl] = change.new_path
						functionCurSize[nuDecl] = int(row['End Pos']) - int(row['Start Pos']) +1
						if nuDecl not in functionPrevSize:
							functionPrevSize[nuDecl] = 0
						#track all current functions
						#functionAllCurrent.add(nuDecl)
						if nuDecl in fqnNoAnnotsBeforeLinesDeleted or nuDecl in fqnAnnotsBeforeLinesDeleted:
							functionAllChangeCurrent.add(nuDecl)
						functionCurSize[nuDecl] = int(row['End Pos']) - int(row['Start Pos']) +1
						if 'Null' in row['Annotation'] or 'null' in row['Annotation']:
							fqnDict = fqnAnnotParentsAfter.get(nuDecl, {})
							annotSet = fqnDict.get(row['FQN'], set())
							annotSet.add(row['Annotation'])
							fqnDict[row['FQN']] = annotSet
							fqnAnnotParentsAfter[nuDecl] = fqnDict

							for i in range(int(row['Start Pos']), int(row['End Pos'])+1):
								if i in linesAdded:
									#functionAnnotChangeinclDel.add(nuDecl)
									#functionAnnotChangeCurrent.add(nuDecl)
									functionAllChangeinclDel.add(nuDecl)
									functionAllChangeCurrent.add(nuDecl)
									break
						elif row['Annotation'] == '':
							#functionAllChangeCurrent.add(nuDecl)
							for i in range(int(row['Start Pos']), int(row['End Pos'])+1):
								if i in linesAdded:
									if nuDecl in fqnAnnotParentsBefore:
										functionAnnotRemove.add(nuDecl)
										functionAllChangeinclDel.add(nuDecl)
										functionAllChangeCurrent.add(nuDecl)
										
									elif nuDecl in fqnNoAnnotsBeforeLinesDeleted:
										functionAllChangeinclDel.add(nuDecl)
										functionAllChangeCurrent.add(nuDecl)
										
									else:
										functionAllChangeinclDel.add(nuDecl)
										functionAllChangeCurrent.add(nuDecl)
									break
					for fqnParentBefore in fqnAnnotParentsBefore:
						#print('fqnParent before ' + fqnParentBefore)
						fqnsBefore = fqnAnnotParentsBefore[fqnParentBefore]
						for fqnBefore in fqnsBefore:
							annotsBefore = fqnsBefore[fqnBefore]
							if fqnParentBefore not in fqnAnnotParentsAfter:
							#	print('no fqnParent')
								numDeleted += len(annotsBefore)
								fileFlag = True
								functionAnnotRemove.add(fqnParentBefore)
								functionAnnotChangeinclDel.add(fqnParentBefore)
								functionAllChangeinclDel.add(fqnParentBefore)
								#####
								fqnMap = fqnAnnotParentsBefore[fqnParentBefore]
								if fqnParentBefore in functionCurSize:
									functionPrevSize[fqnParentBefore] = functionCurSize[fqnParentBefore]
									functionCurSize[fqnParentBefore] = 0
								else:
									functionCurSize[fqnParentBefore] = 0
								
								
								#### ha ha ha ha
								functionAnnotDelete[fqnParentBefore] = fqnMap
								for annot in annotsBefore:
									myList = []
									myList.append(repo)
									myList.append(commit.hash)
									myList.append(commit.committer_date)
									fileString = ''
									if not change.new_path is None:
										fileString = change.new_path
									elif not change.old_path is None:
										fileStrinng = change.old_path
									myList.append(fileString)
									myList.append(fqnParentBefore)
									myList.append(fqnBefore)
									myList.append(annot)
									myList.append('delete')
									sType = ''
									if not TypeDict[fqnParentBefore] is None:
										sDict = TypeDict[fqnParentBefore]
										if not sDict[fqnBefore] is None:
											sType = sDict[fqnBefore]
									myList.append(sType)

									annotListWriter.writerow(myList)
									annotList.flush()

								#functionAnnotSet.add(fqnParentBefore)
								#functionAllSet.add(fqnParentBefore)
							else:
								fqnsAfter = fqnAnnotParentsAfter[fqnParentBefore]

								if fqnBefore not in fqnsAfter:

									fqnMap = fqnAnnotParentsBefore[fqnParentBefore]


									#fqnMap

									numDeleted += len(annotsBefore)
									fileFlag = True
									functionAnnotRemove.add(fqnParentBefore)

									functionAnnotChangeinclDel.add(fqnParentBefore)
									functionAnnotChangeCurrent.add(fqnParentBefore)
									functionAllChangeinclDel.add(fqnParentBefore)
									functionAllChangeCurrent.add(fqnParentBefore)

									for annot in annotsBefore:
										myList = []
										myList.append(repo)
										myList.append(commit.hash)
										myList.append(commit.committer_date)
										fileString = ''
										if not change.new_path is None:
											fileString = change.new_path
										elif not change.old_path is None:
											fileStrinng = change.old_path
										myList.append(fileString)
										myList.append(fqnParentBefore)
										myList.append(fqnBefore)
										myList.append(annot)
										myList.append('delete')
										sType = ''
										if not TypeDict[fqnParentBefore] is None:
											sDict = TypeDict[fqnParentBefore]
											if not sDict[fqnBefore] is None:
												sType = sDict[fqnBefore]
										myList.append(sType)
										annotListWriter.writerow(myList)
										annotList.flush()

									#functionAllSet.add(fqnParentBefore)
								else:
									annotsAfter = fqnsAfter[fqnBefore]
									if len(annotsBefore) > len(annotsAfter):
										numDeleted += len(annotsBefore) - len(annotsAfter)
										fileFlag = True

										functionAnnotRemove.add(fqnParentBefore)

										functionAnnotChangeinclDel.add(fqnParentBefore)
										functionAnnotChangeCurrent.add(fqnParentBefore)
										functionAllChangeinclDel.add(fqnParentBefore)
										functionAllChangeCurrent.add(fqnParentBefore)

										for annot in annotsBefore:
											if annot not in annotsAfter:
												myList = []
												myList.append(repo)
												myList.append(commit.hash)
												myList.append(commit.committer_date)
												fileString = ''
												if not change.new_path is None:
													fileString = change.new_path
												elif not change.old_path is None:
													fileStrinng = change.old_path
												myList.append(fileString)
												myList.append(fqnParentBefore)
												myList.append(fqnBefore)
												myList.append(annot)
												myList.append('delete')
												sType = ''
												if not TypeDict[fqnParentBefore] is None:
													sDict = TypeDict[fqnParentBefore]
													if not sDict[fqnBefore] is None:
														sType = sDict[fqnBefore]
												myList.append(sType)
												annotListWriter.writerow(myList)
												annotList.flush()


										#functionAllSet.add(fqnParentBefore)
									elif len(annotsBefore) < len(annotsAfter):
										#print('adding')
										numAdded += len(annotsAfter) - len(annotsBefore)
										fileFlag = True
										functionAnnotAdd.add(fqnParentBefore)

										functionAnnotChangeinclDel.add(fqnParentBefore)
										functionAnnotChangeCurrent.add(fqnParentBefore)
										functionAllChangeinclDel.add(fqnParentBefore)
										functionAllChangeCurrent.add(fqnParentBefore)
										for annot in annotsAfter:
											if annot not in annotsBefore:
												myList = []
												myList.append(repo)
												myList.append(commit.hash)
												myList.append(commit.committer_date)
												fileString = ''
												if not change.new_path is None:
													fileString = change.new_path
												elif not change.old_path is None:
													fileString = change.old_path
												myList.append(fileString)
												myList.append(fqnParentBefore)
												myList.append(fqnBefore)
												myList.append(annot)
												myList.append('add')
												sType = ''
												if not TypeDict[fqnParentBefore] is None:
													sDict = TypeDict[fqnParentBefore]
													if not sDict[fqnBefore] is None:
														sType = sDict[fqnBefore]
												myList.append(sType)
												annotListWriter.writerow(myList)
												annotList.flush()


										#functionAllSet.add(fqnParentBefore)
									else:
										for annotB in annotsBefore:
											if annotB not in annotsAfter:
												numModified += 1
												functionAnnotModified.add(fqnParentBefore)

												fileFlag = True

												functionAnnotChangeinclDel.add(fqnParentBefore)
												functionAnnotChangeCurrent.add(fqnParentBefore)
												#functionAllSet.add(fqnParentBefore)
												functionAllChangeinclDel.add(fqnParentBefore)
												functionAllChangeCurrent.add(fqnParentBefore)


												myList = []
												myList.append(repo)
												myList.append(commit.hash)
												myList.append(commit.committer_date)
												fileString = ''
												if not change.new_path is None:
													fileString = change.new_path
												elif not change.old_path is None:
													fileStrinng = change.old_path
												myList.append(fileString)
												myList.append(fqnParentBefore)
												myList.append(fqnBefore)
												myList.append(annotB)
												myList.append('mod - removed')
												sType = ''
												if not TypeDict[fqnParentBefore] is None:
													sDict = TypeDict[fqnParentBefore]
													if not sDict[fqnBefore] is None:
														sType = sDict[fqnBefore]
												myList.append(sType)
												annotListWriter.writerow(myList)
												annotList.flush()
										for annotA in annotsAfter:
											if annotA not in annotsBefore:
												myList = []
												myList.append(repo)
												myList.append(commit.hash)
												myList.append(commit.committer_date)
												fileString = ''
												if not change.new_path is None:
													fileString = change.new_path
												elif not change.old_path is None:
													fileString = change.old_path
												myList.append(fileString)
												myList.append(fqnParentBefore)
												myList.append(fqnBefore)
												myList.append(annotA)
												myList.append('mod - added')
												sType = ''
												if not TypeDict[fqnParentBefore] is None:
													sDict = TypeDict[fqnParentBefore]
													if not sDict[fqnBefore] is None:
														sType = sDict[fqnBefore]
												myList.append(sType)
												annotListWriter.writerow(myList)
												annotList.flush()

					#put logic here to get all modded functions
					for fqnParentAfter in fqnAnnotParentsAfter:
						#print('checking fqn Parents after')
						fqnsAfter = fqnAnnotParentsAfter[fqnParentAfter]
						for fqnAfter in fqnsAfter:
							annotsAfter = fqnsAfter[fqnAfter]
							if fqnParentAfter not in fqnAnnotParentsBefore:
								#print('no fqn parent')
								numAdded += len(annotsAfter)
								functionAnnotAdd.add(fqnParentAfter)
								fileFlag = True
								functionAnnotChangeinclDel.add(fqnParentAfter)
								functionAnnotChangeCurrent.add(fqnParentAfter)
								functionAllChangeinclDel.add(fqnParentAfter)
								functionAllChangeCurrent.add(fqnParentAfter)
								
								for annot in annotsAfter:
									myList = []
									myList.append(repo)
									myList.append(commit.hash)
									myList.append(commit.committer_date)
									fileString = ''
									if not change.new_path is None:
										fileString = change.new_path
									elif not change.old_path is None:
										fileStrinng = change.old_path
									myList.append(fileString)
									myList.append(fqnParentAfter)
									myList.append(fqnAfter)
									myList.append(annot)
									myList.append('add')
									sType = ''
									if not TypeDict[fqnParentAfter] is None:
										sDict = TypeDict[fqnParentAfter]
										if not sDict[fqnAfter] is None:
											sType = sDict[fqnAfter]
									myList.append(sType)
									annotListWriter.writerow(myList)
									annotList.flush()

								#functionAnnotSet.add(fqnParentAfter)
								#functionAllSet.add(fqnParentAfter)
							else:
								fqnsBefore = fqnAnnotParentsBefore[fqnParentAfter]
								#annotsBefore = fqnsBefore[fqnAfter]
								if fqnAfter not in fqnsBefore:
									numAdded += len(annotsAfter)
									fileFlag = True

									functionAnnotAdd.add(fqnParentAfter)
									functionAnnotChangeinclDel.add(fqnParentAfter)
									functionAnnotChangeCurrent.add(fqnParentAfter)
									functionAllChangeinclDel.add(fqnParentAfter)
									functionAllChangeCurrent.add(fqnParentAfter)

									for annot in annotsAfter:
										myList = []
										myList.append(repo)
										myList.append(commit.hash)
										myList.append(commit.committer_date)
										fileString = ''
										if not change.new_path is None:
											fileString = change.new_path
										elif not change.old_path is None:
											fileStrinng = change.old_path
										myList.append(fileString)
										myList.append(fqnParentAfter)
										myList.append(fqnAfter)
										myList.append(annot)
										myList.append('add')
										sType = ''
										if not TypeDict[fqnParentAfter] is None:
											sDict = TypeDict[fqnParentAfter]
											if not sDict[fqnAfter] is None:
												sType = sDict[fqnAfter]
										myList.append(sType)
										annotListWriter.writerow(myList)
										annotList.flush()

				if fileFlag is True:
					moddedFiles += 1
			numDeletedFunctionsDeleted = 0
			###add special set to help with call graph
			for fqnParent in functionAnnotDelete:
				if fqnParent not in functionAllChangeCurrent:
					fqnMap = functionAnnotDelete[fqnParent]
					for fqn in fqnMap:
						annotSet = fqnMap[fqn]
						numDeletedFunctionsDeleted += len(annotSet)
				else:
					functionAnnotChangeCurrent.add(fqnParent)

			#print('done processing commit modifications')
			if numAdded == 0 and numModified == 0 and numDeleted <= numDeletedFunctionsDeleted:
				continue

		###we've now processed each file in the change set
		#adding functions that had annotations added/deleted/modified
		#need to create call graph
			print('got here, creating methodSet')
			methodSet = set()
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
			strCCC = cgJarPath
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
			
			cg = open(invoksOutputPath, newline='')
			cgReader = csv.DictReader(cg)
			invok2decls = {}
			#use similar dictionary for reverse direction
			decl2invoks = {}
			
			for row in cgReader:
				#print('reading call graph')
				#print('method declaration: ' + row['Method Declared'])
				methodSet.add(row['Method Declared'])
				if (row['Method Declared']) not in functionAllChangeCurrent:
					functionPrevSize[row['Method Declared']] = int(row['End Pos']) - int(row['Start Pos']) + 1
					functionCurSize[row['Method Declared']] = int(row['End Pos']) - int(row['Start Pos']) + 1
				#methodSet.add(row['Method Invoked'])
				if row['Method Invoked'] != 'null':
					iSet = invok2decls.get(row['Method Invoked'], set())
					if not row['Method Declared'] in iSet:
						iSet.add(row['Method Declared'])
					invok2decls[row['Method Invoked']] = iSet
				path = getRelativePath(row['Path to Decl'], owner)
				
				fileMap[row['Method Declared']] = path
					#invok2decls[row['Method Invoked']] = iSet
				jSet = decl2invoks.get(row['Method Declared'], set())
				if row['Method Invoked'] != 'null' and not row['Method Invoked'] in jSet:
					jSet.add(row['Method Invoked'])
				decl2invoks[row['Method Declared']] = jSet
			#now we have invocations mapped to declarations
		#fMap will store a mapping of declarations to their invocations in the call graphs
		#there are 4 sets for distance 1, 2, 3 and 4
			#missing field from method set
			
			fMap = {}
			#foutMap = {}
			for decl in functionAnnotChangeCurrent:

				mainL = []
				for i in range(0, 8):
					iSet = set()
					mainL.append(iSet)
				#if decl in methodSet:
				fMap[decl] = mainL
			#first pass through the map
			for decl in fMap:
				invokSet = decl2invoks.get(decl, set())
				#check to make sure invok is declared
				#for iDecl in list(invokSet):
				#	if not iDecl in functionAllCurrent:
				#		invokSet.remove(iDecl)
				for invok in invokSet:
					#print('invocation at distance 1')
					#print(invok)
					functionCalleeDist[invok] = 1
				mainL = fMap[decl]
				mainL[4] = invokSet
				fMap[decl] = mainL
			
			for decl in fMap:
				mainL = fMap[decl]
				set1 = mainL[4]
				iSet = set()
				for subDecl in set1:
					tempSet = decl2invoks.get(subDecl, set())
					#for iDecl in list(tempSet):
					#	if not iDecl in functionAllCurrent:
					#		tempSet.remove(iDecl)	
					for d in tempSet:
						if not d == decl and d not in set1:
							iSet.add(d)
						#curMin = 2
							if d in functionCalleeDist and functionCalleeDist[d] > 1:
								functionCalleeDist[d] = 2
							elif d not in functionCalleeDist:
								functionCalleeDist[d] = 2
				mainL[5] = iSet
				fMap[decl] = mainL
			for decl in fMap:
				mainL = fMap[decl]
				l1 = mainL[4]
				l2 = mainL[5]
				iSet = set()
				for subDecl in l2:
					tempSet = decl2invoks.get(subDecl, set())
					#for iDecl in list(tempSet):
					#	if not iDecl in functionAllCurrent:
					#		tempSet.remove(iDecl)
					for d in tempSet:
						if not d == decl and d not in l1 and d not in l2:
							iSet.add(d)
						#curMin = 3
							if d in functionCalleeDist and functionCalleeDist[d] > 2:
								functionCalleeDist[d] = 3
							elif d not in functionCalleeDist:
								functionCalleeDist[d] = 3
				mainL[6] = iSet
				fMap[decl] = mainL
			for decl in fMap:
				mainL = fMap[decl]
				l1 = mainL[4]
				l2 = mainL[5]
				l3 = mainL[6]
				iSet = set()
				for subDecl in l3:
					tempSet = decl2invoks.get(subDecl, set())
					#for iDecl in list(tempSet):
					#	if not iDecl in functionAllSet:
					#		tempSet.remove(iDecl)
					for d in tempSet:
						if not d == decl and d not in l1 and d not in l2 and d not in l3:
							iSet.add(d)
							if d in functionCalleeDist and functionCalleeDist[d] > 3:
								functionCalleeDist[d] = 4
							elif d not in functionCalleeDist:
								functionCalleeDist[d] = 4
				mainL[7] = iSet
				fMap[decl] = mainL

			for decl in fMap:
				#if decl in decl2invok:
				invokSet = invok2decls.get(decl, set())
				#for iDecl in list(invokSet):
				#	if not iDecl in functionAllCurrent:
				#		invokSet.remove(iDecl)]
				for invok in invokSet:
					#print('invok dist 1')
					#print(invok)
					functionCallerDist[invok] = 1
				mainL = fMap[decl]
				mainL[0] = invokSet
				fMap[decl] = mainL
			#second pass through the map
			for decl in fMap:
				#????
				mainL = fMap[decl]
				set1 = mainL[0]
				iSet = set()
				for subDecl in set1:
					tempSet = invok2decls.get(subDecl, set())
					#for iDecl in list(tempSet):
					#	if not iDecl in functionAllCurrent:
					#		tempSet.remove(iDecl)
					for d in tempSet:
						if not d == decl and d not in set1:
							iSet.add(d)
							if d in functionCallerDist and functionCallerDist[d] > 1:
								functionCallerDist[d] = 2
							elif d not in functionCallerDist:
								functionCallerDist[d] = 2
				mainL[1] = iSet
				fMap[decl] = mainL
			#third pass through the map
			for decl in fMap:
				mainL = fMap[decl]
				l1 = mainL[0]
				l2 = mainL[1]
				iSet = set()
				for subDecl in l2:
					tempSet = invok2decls.get(subDecl, set())
					#for iDecl in list(tempSet):
					#	if not iDecl in functionAllSet:
					#		tempSet.remove(iDecl)
					for d in tempSet:
						if not d == decl and d not in l1 and d not in l2:
							iSet.add(d)
							if d in functionCallerDist and functionCallerDist[d] > 2:
								functionCallerDist[d] = 3
							elif d not in functionCallerDist:
								functionCallerDist[d] = 3
				mainL[2] = iSet
				fMap[decl] = mainL
			#fourth and (whew) final pass through map
			for decl in fMap:
				mainL = fMap[decl]
				l1 = mainL[0]
				l2 = mainL[1]
				l3 = mainL[2]
				iSet = set()
				for subDecl in l3:
					tempSet = invok2decls.get(subDecl, set())
					#for iDecl in list(tempSet):
					#	if not iDecl in functionAllCurrent:
					#		tempSet.remove(iDecl)
					for d in tempSet:
						if not d == decl and d not in l1 and d not in l2 and d not in l3:
							iSet.add(d)
							if d in functionCallerDist and functionCallerDist[d] > 3:
								functionCallerDist[d] = 4
							elif d not in functionCallerDist:
								functionCallerDist[d] = 4
				mainL[3] = iSet
				fMap[decl] = mainL

			if True:
				#if decl != 'null':
				for decl in methodSet:
					if decl in functionAllChangeCurrent:
				#indent following
						path = ''
						if decl in fileMap:
							path = fileMap[decl]
						else:
							path = 'undefined'
				#if len(fMap[decl][0]) != 0:
						othaListToWrite = []
						othaListToWrite.append(repo)
						othaListToWrite.append(commit.hash)
						othaListToWrite.append(commit.committer_date)
						othaListToWrite.append(path)
						othaListToWrite.append(decl)
				
						functionAnnotChanged = ''
						if decl in functionAnnotChangeCurrent:
							functionAnnotChanged = 'Yes'
						else:
							functionAnnotChanged = 'No'
						othaListToWrite.append(functionAnnotChanged)
						functionAnnotChangeType = ''
						if decl in functionAnnotModified:
							functionAnnotChangeType = 'Modified'
						elif decl in functionAnnotAdd:
							functionAnnotChangeType = 'Added'
						elif decl in functionAnnotRemove:
							functionAnnotChangeType = 'Removed'
						else:
							functionAnnotChangeType = 'NA'
						othaListToWrite.append(functionAnnotChangeType)

						fCallerDist = -1
						fCalleeDist = -1
						if decl in functionAnnotChangeCurrent:
							fCallerDist = 0
							fCalleeDist = 0
						else:
							if decl in functionCallerDist:
								fCallerDist = functionCallerDist[decl]
							if decl in functionCalleeDist:
								fCalleeDist = functionCalleeDist[decl]
						if fCalleeDist == -1:
							othaListToWrite.append(-1)
						else:
							othaListToWrite.append(fCalleeDist)
						if fCallerDist == -1:
							othaListToWrite.append(-1)
						else:
							othaListToWrite.append(fCallerDist)
						if path in fileNumCommits:
							othaListToWrite.append(fileNumCommits[path])
						else:
							othaListToWrite.append(1)
							fileNumCommits[path] = 1
				
						functionSize = 0
						if decl in functionAllChangeCurrent:
							if decl in functionPrevSize:
								functionSize = functionPrevSize[decl]
							else:
								functionSize = -1
						else:
						#functionSize = functionCurSize[decl]
							if decl in functionCurSize:
								functionSize = functionCurSize[decl]
							else:

								functionSize = -1
						othaListToWrite.append(functionSize)
				
				#get age of file
						endDate = commit.committer_date
						startDate = commit.committer_date


						if path in fileAges:
							startDate = fileAges[path]
				
							delta = ageDifference(str(startDate), str(endDate))
							othaListToWrite.append(delta)
						else:
							othaListToWrite.append(0)
							fileAges[path] = commit.committer_date

						functionWriter.writerow(othaListToWrite)
						functionsOut.flush()
			#for i in functionAnnotSet:
				#print('functionAnnotSet ' + i)
		
			listToWrite = []
			listToWrite.append(repo)
			listToWrite.append(commit.hash)
			listToWrite.append(commit.committer_date)
			listToWrite.append(commit.msg)
			listToWrite.append(numAdded)
			listToWrite.append(numDeleted)
			listToWrite.append(numModified)			
			listToWrite.append(len(functionAnnotChangeinclDel))
			listToWrite.append(len(functionAllChangeinclDel))
			listToWrite.append(moddedFiles)
			listToWrite.append(len(commit.modifications))
			#listToWrite.append(fileString)
			writer.writerow(listToWrite)

			outCSV.flush()
			

