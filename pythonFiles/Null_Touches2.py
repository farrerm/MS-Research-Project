import csv
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

def formatString(message):

	charCounter = 0
	if len(message) < 100:
		return message
	newString = re.sub('(.{50})', '\\1\n', message, 0, re.DOTALL)
	return newString

def eventSearch(repoName, commitIDList, issueNum):
	outPut3 = {'dummy': 2}
	retList = []
	pageNo = 0
	for i in range(0, 10):
		
		str1 = 'curl'
		#str2 = '-H'
		#str3 = 'Accept: application/vnd.github.cloak-preview' 
		#str3 = 'application/vnd.github.sailor-v-preview+json'
		#str3aa = '-H'
		#str3bb = 'application/vnd.github.sailor-v-preview+json'

		str3a = '-H'
		str3b = 'Authorization: token ************************TO DO'
		str4 = 'https://api.github.com/repos/'
		str4 += repoName + '/'
		str4 += 'issues/'
		str4 += str(issueNum)
		str4 += '/'
		str4 += 'events'
		str4 += '?page='
		
		pageNo += 1
		#print(str(index))
		str4 += str(pageNo)
		#str4 += '&per_page=30'
		print(str4)
		outPut = subprocess.check_output([str1, str3a, str3b, str4])
		#outputList = eval(outPut)
		#gitIssues.write(outPut)
		outPut2 = outPut.decode('utf-8')
		#print(str1)
		#print(str4)
		
		#gitIssues.write(outPut2)
		try:
			#index += 1
			#print(str(index))'
			#print('in eventSearch()')
			#print('repoName: ' + repoName)
			#print('commitID: ' + str(commitID))
			#print('issueNum: ' + str(issueNum))
			#print(outPut2)	
			#print('repoName: ' + repoName)
			#print('length of json: ' + str(len(outPut2)))
			#outPut33 = '{' + outPut2[1:len(outPut2) -2] + '}'
			outPut3 = json.loads(outPut2)
			print('loaded json')
			print('page no ' + str(pageNo))

			if len(outPut3) == 0:
				print('returning from eventSearch')
				return retList
			
			if len(outPut3) > 0:
				if outPut3 is None:
					#break
					continue
				listCounter = 0
				for i in outPut3:
					print(i)
					print('in eventSearch()')
					print('repo: ' + repoName)
					print('commitID: ' + str(commitID))
					print('IssueNum: ' + str(issueNum))
					print('list length: ' + str(len(outPut3)))
					listCounter += 1
					print('list entry number: ' + str(listCounter))
					#print(i)
					if not i['commit_id'] is None and i['commit_id'] == commitID:
						print('matching commit')
						retList.append(i['issue']['number'])
						print('adding issue:' + str(i['issue']['number']))
						retList.append(i['issue']['title'])
						print('adding issue title: ' + i['issue']['title'])
						retList.append('adding issue body: ' + i['issue']['body'])
						print(i['issue']['body'])
				#if outPut3["items"] is None:
				#	continue
			#if 'null' in outPut3["message"] or 'Null' in outPut3["message"]:
				#	gitIssues.write(json.dumps(x, indent = 4))
				#	print(json.dumps(outPut3, indent = 4))
		except:
			print("Error in Event Search")
			#break
			return retList
		time.sleep(1)
	return retList

def issueSearch(repoName, commitID):

	retIssueList = []
	index = 0
	outPut = []
	outPut3 = [1]
	while not len(outPut3) == 0:
	#outPut != '[\n\n]\n':
		str1 = 'curl'
		str2 = "-H"
		str3 = 'Authorization: token ****************************TO DO'
		str4 = 'https://api.github.com/repos/'
		#outStrBegin = 'https://api.github.com/repos/'
		str4 += repoName
		outStrMid = '/issues?state=all&page='
		str4 += outStrMid
		index += 1
		str4 += str(index)
		outStrEnd = '&per_page=100'
		str4 += outStrEnd
		outPut = -1
		#while not outPut == 0:
		outPut = subprocess.check_output([str1, str2, str3, str4])
		#if not outPut == 0:
		#	continue

		print('in issueSearch()')
		print('repoName: ' + repoName)
		print('commitID: ' + str(commitID))

		try:
			outPut3 = json.loads(outPut.decode("utf-8"))
			

			if len(outPut3) > 0:
				for x in outPut3:
					if x is None:
						continue
					if x["number"] is None:
						continue
					issueNum = x["number"]
					print('calling eventSearch from IssueSearch()')
					print('issue Num: ' + str(issueNum))
					time.sleep(1)
					eventList = eventSearch(repoName, commitID, issueNum)
					if not len(eventList) == 0:
						for j in eventList:
							retIssueList.append(str(j))
		except:
			print("error in Issue Search")
			retIssueList.append('error getting issue text')
			continue
	print('exiting issueSearch')
	return retIssueList

def pullSearch(repoName, commitID):
	print('pullSearch() ' + repoName + ' ' + str(commitID))
	#outPut3 = [1]
	#while not len(outPut3) == 0:
	print('pulling')
	retPullList = []
	#index = 0
	outPut = []
	outPut3 = None
	#while not len(outPut3) == 0:
	#outPut != '[\n\n]\n':
	time.sleep(1)
	str1 = 'curl'
	str2 = "-H"
	str3 = 'Authorization: token ******************************TO DO'
	str3a = "-H"
	str3b = 'Accept: application/vnd.github.groot-preview+json'
	str4 = 'https://api.github.com/repos/'
		#outStrBegin = 'https://api.github.com/repos/'

	str4 += repoName
	str4 += '/commits/'
	str4 += str(commitID) + '/'
	str4 += 'pulls'
		#outPut = -1
	#while not outPut == 0:
	
	#if not outPut == 0:
	#	continue

	try:
		retPullList = [-1]
		outPut = subprocess.check_output([str1, str2, str3, str3a, str3b, str4])
		outPut3 = json.loads(outPut.decode("utf-8"))
		retPullList = []

		if len(outPut3) > 0:
			for pull in outPut3:
				if pull is None:
					break
				if pull["number"] is None:
					break
				
				retPullList.append(str(pull["number"]))
				retPullList.append(pull["title"])
				retPullList.append(pull["body"])	
				
					
	except:
		print("error in Pull Search")
		return retPullList
	#print('exiting issueSearch')
	return retPullList

def parseMessage(repoName, commitMessage):
	
	issueNumList = []
	ownerID = repoName[0] + repoName[1] + repoName[2]
	
	ownerID = ownerID.upper()
	ownerID += '-'
	print('In parseMessage, Owner ID ' + ownerID)
	#for i in range(0, len(commitMessage) - len(ownerID) - 1):
	#	parseNum = ''
	#	if commitMessage[i:i + len(ownerID)] == ownerID:
	#		charIndex = i + len(ownerID)
	#		while charIndex < len(commitMessage) and commitMessage[charIndex].isdigit():
		#		parseNum += commitMessage[charIndex]
		#		charIndex += 1
		#	
		#	if not parseNum == '':
		#		print(parseNum)
		#		issueNumList.append(int(parseNum))

	issueString = '\033[4missue/\033[0m'
	#end = '\033[0m'
	#underline = '\033[4m'
	#issueString = underline + issueString + end
#	for i in range(0, len(commitMessage) - len(issueString) -1):
	#	parseNum = ''
	#	if commitMessage[i:i + len(issueString) - 4] == issueString[0: len(issueString) -4]:
	#		charIndex = i + len(issueString) - 4
		#	while charIndex < len(commitMessage) and commitMessage[charIndex].isdigit():
		#		parseNum += commitMessage[charIndex] 
		#		charIndex +=1
		#	if not parseNum == '':
		#		print(parseNum)
		#		issueNumList.append(int(parseNum))

	issueString = 'Iss '
	for i in range(0, len(commitMessage) - len(issueString) -1):
		parseNum = ''
		if commitMessage[i:i + len(issueString)] == issueString:
			charIndex = i + len(issueString)
			while charIndex < len(commitMessage) and commitMessage[charIndex].isdigit():
				parseNum += commitMessage[charIndex] 
				charIndex +=1
			if not parseNum == '':
				print(parseNum)
				issueNumList.append(int(parseNum))

	pullString = 'pull/'
	for i in range(0, len(commitMessage) - len(pullString) -1):
		parseNum = ''
		if commitMessage[i:i + len(pullString)] == pullString:
			charIndex = i + len(pullString)
			while charIndex < len(commitMessage) and commitMessage[charIndex].isdigit():
				parseNum += commitMessage[charIndex] 
				charIndex +=1
			if not parseNum == '':
				print(parseNum)
				issueNumList.append(int(parseNum))

	pullString = 'issues/'
	for i in range(0, len(commitMessage) - len(pullString) -1):
		parseNum = ''
		if commitMessage[i:i + len(pullString)] == pullString:
			charIndex = i + len(pullString)
			while charIndex < len(commitMessage) and commitMessage[charIndex].isdigit():
				parseNum += commitMessage[charIndex] 
				charIndex +=1
			if not parseNum == '':
				print(parseNum)
				issueNumList.append(int(parseNum))

	for i in range(0, len(commitMessage) - 1):
		parseNum = ''
		if commitMessage[i] == '#':
			charIndex = i + 1
			while charIndex < len(commitMessage) and commitMessage[charIndex].isdigit():
				parseNum += commitMessage[charIndex]
				charIndex += 1
			if not parseNum == '':
				print(parseNum)
				issueNumList.append(int(parseNum))
	return issueNumList

def getIssue(repoName, issueNum):
	retIssueList = []
	index = 0
	outPut = []
	outPut3 = [1]
	time.sleep(1)
	str1 = 'curl'
	str2 = "-H"
	str3 = 'Authorization: token *******************************TO DO'
	str4 = 'https://api.github.com/repos/'
		#outStrBegin = 'https://api.github.com/repos/'
	str4 += repoName
	str4 += '/issues/'
	str4 += str(issueNum)
		
	print(str4)
	outPut = subprocess.check_output([str1, str2, str3, str4])

	print('getting issue ' + str(issueNum))
	print('repoName: ' + repoName)
	#print('commitID: ' + str(commitID))

	try:
		print('trying1')
		outPut3 = json.loads(outPut.decode("utf-8"))
		print('trying2')
		if outPut3 is None:
			return retIssueList
		print('trying3')
		retIssueList.append(str(outPut3["number"]))
		retIssueList.append(str(outPut3["title"]))
		retIssueList.append(str(outPut3["body"]))
				
	except:
		print("error getting Issue")
		#if not outPut3["message"] == None:
			#retIssueList.append(str(outPut3["message"]))
		#else:
		retIssueList.append('error getting issue')
		return retIssueList
	print('returning issue')
	return retIssueList

def contains(smallList, bigList):
	for i in range(0, len(bigList)):
		flag = True
		otherList = bigList[i]
		#do not include path in matching
		for j in range(3, len(smallList)):
			if not smallList[j] == otherList[j]:
				flag = False
		if flag == True:
			return True
	return False

def getCurPath(smallList, bigList):
	for i in range(0, len(bigList)):
		flag = True
		otherList = bigList[i]
		#do not include path in matching
		for j in range(2, len(smallList)):
			if not smallList[j] == otherList[j]:
				flag = False
		if flag == True:
			return bigList[i][0]
	return "null"

def getAnnotationType(smallList, bigList):
	for i in range(0, len(bigList)):
		flag = True
		otherList = bigList[i]
		#do not include path in matching
		#or filename
		for j in range(3, len(smallList)):
			if not smallList[j] == otherList[j]:
				flag = False
		if flag == True:
			return bigList[i][1]
	return "null"

def remove(smallList, bigList):
	#print ('BigList')
	#print(bigList)
	#print('SmallList')
	#print(smallList)
	for i in range(0, len(bigList)):
		flag = True
		otherList = bigList[i]


		for j in range(3, len(smallList)):
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

def containsFileName(fileName, bigList):
	for i in range(0, len(bigList)):
		flag = True
		otherList = bigList[i]

		#for j in range(0, len(smallList)):
		if not otherList[2] == fileName:
			flag = False
		if flag == True:
			return True
	return False

def countFilesTouched(mods):
	return len(mods)

def countLOCtouched(mods):
	sum = 0
	for m in mods:
		sum = sum + m.added 
		sum = sum + m.removed
	return sum



##begin main
cnt = Counter()
myDict = {}


#build dictionary of null annotations
#keys are projects, values are list of null annotations for that project
with open('/Users/matt/eclipse-workspace/nuCSV.csv', newline='') as csvfile:

	with open('/Users/matt/eclipse-workspace/6myNulls.csv', 'w', newline='') as nullsCSV:
		numMethods = 0
		numFields = 0
		numParams = 0
		numLVs = 0
		writer = csv.writer(nullsCSV)
	
		writer.writerow(['Owner', 'Project', 'Path', 'Package', 'FileName', 'Class', 'Node Type', 'FQN', 'FQN Parent', 'Annotation'])
		
		#writer.flush()
		myReader = csv.DictReader(csvfile)
		for row in myReader:
			if ('null' in row['Annotation'] or 'Null' in row['Annotation']) and row['Node Type'] == 'Method':
				numMethods += 1
				#nullsCSV
				listToWrite = []
				listToWrite.append(row['Owner'])
				listToWrite.append(row['Project'])

				listToWrite.append(row['Path'])
				listToWrite.append(row['Package'])
				listToWrite.append(row['FileName'])
				listToWrite.append(row['Class'])
				listToWrite.append(row['Node Type'])
				listToWrite.append(row['FQN'])
				listToWrite.append(row['FQN Parent'])
				listToWrite.append(row['Annotation'])
				writer.writerow(listToWrite)
				#writer.flush()
			if ('null' in row['Annotation'] or 'Null' in row['Annotation']) and row['Node Type'] == 'Field':
				numFields += 1
				listToWrite = []
				listToWrite.append(row['Owner'])
				listToWrite.append(row['Project'])
				listToWrite.append(row['Path'])
				listToWrite.append(row['Package'])
				listToWrite.append(row['FileName'])
				listToWrite.append(row['Class'])
				listToWrite.append(row['Node Type'])
				listToWrite.append(row['FQN'])
				listToWrite.append(row['FQN Parent'])
				listToWrite.append(row['Annotation'])
				writer.writerow(listToWrite)
				#writer.flush()
			if ('null' in row['Annotation'] or 'Null' in row['Annotation']) and row['Node Type'] == 'Parameter':
				numParams += 1
				listToWrite = []
				listToWrite.append(row['Owner'])
				listToWrite.append(row['Project'])
				listToWrite.append(row['Path'])
				listToWrite.append(row['Package'])
				listToWrite.append(row['FileName'])
				listToWrite.append(row['Class'])
				listToWrite.append(row['Node Type'])
				listToWrite.append(row['FQN'])
				listToWrite.append(row['FQN Parent'])
				listToWrite.append(row['Annotation'])
				writer.writerow(listToWrite)
				#writer.flush()
			if ('null' in row['Annotation'] or 'Null' in row['Annotation']) and row['Node Type'] == 'Local Variable':
				numLVs += 1
				listToWrite = []
				listToWrite.append(row['Owner'])
				listToWrite.append(row['Project'])
				listToWrite.append(row['Path'])
				listToWrite.append(row['Package'])
				listToWrite.append(row['FileName'])
				listToWrite.append(row['Class'])
				listToWrite.append(row['Node Type'])
				listToWrite.append(row['FQN'])
				listToWrite.append(row['FQN Parent'])
				listToWrite.append(row['Annotation'])
				writer.writerow(listToWrite)
				#writer.flush()
	print('numMethods ' + str(numMethods))
	print('numFields ' + str(numFields))
	print('numParams ' + str(numParams))
	print('numLVs ' + str(numLVs))
	
	#with open('/Users/matt/eclipse-workspace/fgTestNullAnnotations.csv', newline = '') as outcsv:
	
#
	#firstReader = csv.DictReader(csvfile)
	#secondReader = csv.DictReader(outcsv)

	#	with open('/Users/matt/eclipse-workspace/results.csv', 'w', newline = '') as myResults:

			#for row in firstReader:

	#writer = csv.writer(outcsv)
with open('/Users/matt/eclipse-workspace/6myNulls.csv', newline='') as nullsCSV2:
	spamreader = csv.DictReader(nullsCSV2)
	counter = 0
	countInMap = 0
	countNotInMap = 0
	repoCount = 0
	for row in spamreader:
		#if 'null' in row['Annotation'] or 'Null' in row['Annotation']:
			
		projRepo = row['Owner'] + '/' + row['Project']
		bigList = myDict.get(projRepo, [])

		vals = []
		vals.append(row['Path'])
		vals.append(row['Node Type'])
		vals.append(row['FileName'])
		vals.append(row['Class'])
		vals.append(row['FQN Parent'])
		vals.append(row['FQN'])
		vals.append(row['Annotation'])
		if not contains(vals, bigList):
			bigList.append(vals)
		myDict[projRepo] = bigList
	print('num repos: ')
	print(len(myDict))


with open('/Users/matt/eclipse-workspace/Null_Touches.csv', 'w', newline = '') as outcsv:
	writer = csv.writer(outcsv)


	writer.writerow(['Repo', 'Commit ID', 'Author Name', 'Author Email', 'Comitter Name', 'Commiter Email', 'Commit Date', 'Commit Message', 'NumFiles', 'List of Files Changed', 'Lines of Code Added or Removed', 'NullCheckers?'])
	outcsv.flush()
	#myDict1 = collections.OrderedDict(sorted(myDict.items()))

	
	for key in myDict:
		repoName = '/Users/matt/Project/100reposNext2/'
		#repoName = '/Users/matt/Project/100reposNext2/google/exoplayer'
		repoName = repoName + key
		#fileMap = myDict.get(key)
		print(repoName)
		

		bigList = myDict.get(key, [])
		#if (len(bigList) == 0):
			#continue
		for commit in RepositoryMining(repoName).traverse_commits():

			#either commit touches null or it does not
			if commit.in_main_branch:
			#if len(nullsList) == 0:
				#break
				commitFlag = -1

				listToWrite = []
				listToWrite.append(key)
				listToWrite.append(commit.hash)
				listToWrite.append(commit.author.name)
				listToWrite.append(commit.author.email)
				listToWrite.append(commit.committer.name)
				listToWrite.append(commit.committer.email)
				listToWrite.append(commit.committer_date)
				wrapper = textwrap.TextWrapper(width=50, replace_whitespace=True)

				word_list = wrapper.wrap(text=commit.msg)
				commitMessage = ''
				for line in word_list:
					commitMessage += line
					commitMessage += '\n'
								#commitMessage += str(len(commitMessage))
				listToWrite.append(commitMessage)
				fileList = ''
				for change in commit.modifications:
					if not change.new_path is None:
						fileList = fileList + change.new_path
						fileList += '\n'
					else:
						fileList = fileList + 'None'
						fileList += '\n'
				listToWrite.append(str(len(commit.modifications)))

				listToWrite.append(fileList)

				loc = 0
				loc = countLOCtouched(commit.modifications)
				listToWrite.append(str(loc))

				for change in commit.modifications:

					#if change.source_code is None:
					#	continue
					commitFileName = change.filename
				#we don't care if it contains path any more
					if not change.new_path is None and containsFileName(commitFileName, bigList):
				
						if change.source_code is None:
							continue
						f = open('/Users/matt/eclipse-workspace/output.java', 'w+')
					#print(change.source_code)
						f.write(change.source_code)
						f.close()

						str1 = 'java'
						str2 = '-jar'
						str3 = '/Users/matt/eclipse-workspace/ASTParser.jar'
					#print('Calling java program')
						outPut = subprocess.check_output([str1, str2, str3])

						my_file = Path("/Users/matt/eclipse-workspace/testFolder/fgCSV.csv")

						readingDict = {}

					#while True:
						if my_file.is_file():
							
							csvfile = open('/Users/matt/eclipse-workspace/testFolder/fgCSV.csv', newline='')
							#print('reading in CSV from java')
							readingDict = csv.DictReader(csvfile)
							#print('removing CSV file')
							os.remove('/Users/matt/eclipse-workspace/testFolder/fgCSV.csv')
							#break
						rowCount = 0

					#classMap = fileMap.get(change.new_path)
					
						for row in readingDict:
						#print(row)
						#rowCount += 1
						#print(row['Class'])
						#print(row['FQN Parent'])
							rowAsList = []
						#new_path will be a placeholder, is not compared by contains()
						#or maybe we will use path
							rowAsList.append(change.new_path)
					#why are we only appending method here?
							rowAsList.append(row['Node Type'])
							#rowAsList.append('method')
							rowAsList.append(change.filename)
							rowAsList.append(row['Class'])
							rowAsList.append(row['FQN'])
							rowAsList.append(row['FQN Parent'])
							rowAsList.append(row['Annotation'])
							if contains(rowAsList, bigList):

								print('found')
								#now must count files and loc
								if commitFlag == -1:
									listToWrite.append('Yes')
									commitFlag = 0

								remove(rowAsList, bigList)
				if commitFlag == -1:
					listToWrite.append('No')				

				writer.writerow(listToWrite)
				outcsv.flush()
								

											
					
#print('rowCount :' + str(rowCount))
					
				
							