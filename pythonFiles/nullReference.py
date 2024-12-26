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
import sys

from collections import Counter

def formatString(message):

	charCounter = 0
	if len(message) < 100:
		return message
	newString = re.sub('(.{50})', '\\1\n', message, 0, re.DOTALL)
	return newString

def commitGraphQL():
	
	strAAA = 'curl'
	strBBB = '-H'
	strCCC = 'Authorization: bearer ********************************TO DO'
	strDDD =  '-X' 
	strEEE = 'POST'
	strFFF =  '-d'
	strGGG = '{' + \
   		'\"query\": \"query { repository(owner:facebook,name: stetho){login}}\"' + \
 	'} '
	strHHH = 'https://api.github.com/graphql'
	print(strAAA + ' ' + strBBB + ' ' + strCCC + ' ' + strDDD + ' ' + strEEE + ' ' + strFFF \
		+ ' ' + strGGG + ' ' + strHHH)
	outPut = subprocess.check_output([strAAA, strBBB, strCCC, strDDD, strEEE, strFFF, strGGG, strHHH])
	outPut2 = outPut.decode('utf-8')
	print(outPut2)
	#outPut3 = json.loads(outPut2)
	#print(outPut3)
	#print(outPut3['data'])

def commitQuery(owner, repo, hash):
	
	strAAA = 'curl'
	strBBB = '-H'
	strCCC = 'Authorization: bearer *******************************TO DO'
	strDDD =  '-X' 
	strEEE = 'POST'
	strFFF =  '-d'
	strGGG = '{' + \
   		'\"query\": \"query { repository(owner:facebook,name: stetho){login}}\"' + \
 	'} '
	strHHH = 'https://api.github.com/graphql'
	print(strAAA + ' ' + strBBB + ' ' + strCCC + ' ' + strDDD + ' ' + strEEE + ' ' + strFFF \
		+ ' ' + strGGG + ' ' + strHHH)
	outPut = subprocess.check_output([strAAA, strBBB, strCCC, strDDD, strEEE, strFFF, strGGG, strHHH])
	outPut2 = outPut.decode('utf-8')
	print(outPut2)
	#outPut3 = json.loads(outPut2)
	#print(outPut3)
	#print(outPut3['data'])



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
		str3b = 'Authorization: token ****************************TO DO'
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
		str3 = 'Authorization: token *****************************TO DO'
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
	str3 = 'Authorization: token ***************************TO DO'
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
	str3 = 'Authorization: token *********************************TO DO'
	str4 = 'https://api.github.com/repos/'
		#outStrBegin = 'https://api.github.com/repos/'
	str4 += repoName
	str4 += '/issues/'
	str4 += str(issueNum)
		
	print(str4)
	

	print('getting issue ' + str(issueNum))
	print('repoName: ' + repoName)
	#print('commitID: ' + str(commitID))

	try:
		retIssueList = [-1]
		outPut = subprocess.check_output([str1, str2, str3, str4])
		print('trying1')
		
		outPut3 = json.loads(outPut.decode("utf-8"))
		print(outPut3)
		retIssueList = []
			#print(outPut3)
		print('trying2')
		if outPut3 is None:
			return retIssueList
		print('trying3')
		#retIssueList = []
		retIssueList.append(str(outPut3["number"]))
		retIssueList.append(str(outPut3["title"]))
		retIssueList.append(str(outPut3["body"]))
		print('retIssueList ' + str(retIssueList[0]))
	except:
		#retIssueList = ['error getting issue']
		return retIssueList

	print('returning issue')
	return retIssueList

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
gitDataPath = ''

if len(sys.argv) == 1:
	print('error, exiting now')
	sys.exit()	
if sys.argv[1] == 'production':
	repoNamePath = '/home/mjfarrer/callgraph/myDF4.csv'
	repoLocationPath = '/mnt/meta26repos/'
	gitDataPath = '/mnt/gitData.csv'
elif sys.argv[1] == 'local':
	#repoNamePath = '/Users/matt/eclipse-workspace/visitorexample4/myDF4.csv'
	repoNamePath = '/Users/matt/eclipse-workspace/myDF4.csv'
	
	
	repoLocationPath = '/Users/matt/project/test/'
	#repoLocationPath = '/Users/matt/Project/100repos/'
	gitDataPath = '/Users/matt/eclipse-workspace/visitorexample4/gitData.csv'

else:
	print('error, exiting now')
	sys.exit()


repoSrc = open(repoNamePath, newline='')
repoReader = csv.DictReader(repoSrc)
gitList = open(gitDataPath, 'w', newline = '')
gitDataWriter = csv.writer(gitList)
gitDataWriter.writerow(['Repo', 'Commit', 'Date', 'Commit Message', 'Pull Message', 'Issues'])
gitList.flush()

print('got here')
index = 0
for row in repoReader:
	repo = row['repos']
	#'spring-projects/spring-framework'
	print(repo)
	repoArr = repo.split('/')
	owner = repoArr[0]
	project = repoArr[1]
	repoPath = repoLocationPath + repo
	commitIndex = 0
	for commit in RepositoryMining(repoPath).traverse_commits():
		print('commit no ' + str(commitIndex))
		commitIndex += 1
		index += 1
		nullFound = False
		commitString = ''
		pullString = ''
		issueString = ''
		wrapper = textwrap.TextWrapper(width=50, replace_whitespace=True)
		if commit.in_main_branch:
			#commitQuery(repo, owner, commit.hash)
			word_list = wrapper.wrap(text=commit.msg)
			#commitMessage = ''
			for line in word_list:
				commitString += line
				commitString += '\n'

			if 'null' in commit.msg.lower():
				nullFound = True
				#print(index)
				#print(commit.msg)
			pullData = [-1]
			myCounter = 0
			while len(pullData) > 0 and int(pullData[0]) == -1:
				print('trying to pull')
				print(myCounter)
				myCounter += 1
				pullData = pullSearch(repo, commit.hash)
			#print(pullData)
			pullNum = -1
			if not len(pullData) == 0:
				pullNum = int(pullData[0])
			for item in pullData:
				pullString += str(item)
				pullString += '\n'
			#pullString2 = formatString(pullString)
			word_list = wrapper.wrap(text=pullString)
			pullString2 = ''
			for line in word_list:
				pullString2 += line
				pullString2 += '\n'
			pullString = pullString2
			if 'null' in pullString2.lower():
				nullFound = True
			issueNums = []
			issueNumListTentative = parseMessage(repo, commit.msg)
			for num in issueNumListTentative:
				if not num in issueNums and not num == pullNum:
					issueNums.append(num)
					nextNumListTentative = parseMessage(repo, pullString)
					for num in nextNumListTentative:
						if not num in issueNums and not num == pullNum:
							issueNums.append(num)
			issueMasterList = []
			#if issueSeen == False:
			for num in issueNums:
				print('issue ' + str(num))
				issueList = [-1]
				counter = 0
				while len(issueList) > 0 and int(issueList[0]) == -1:
					print('getting issue ' + str(num))
					print('counter ' + str(counter))
					counter += 1
					issueList = getIssue(repo, num)
				#print(issueList)
				if (len(issueList) > 0):
					for item in issueList:
						issueMasterList.append(item)
				else:
					issueMasterList.append('Error getting issue.')
				issueMasterList.append('\n')
			issueString = ''
			for item in issueMasterList:
				issueString += item
				issueString += '\n'
			#issueStringNew = formatString(issueString)
			issueStringNew = wrapper.wrap(text=issueString)
			issueUlt = ''				
			for line in issueStringNew:
				issueUlt += line
				issueUlt += '\n'
			#print(issueUlt)	
			issueString = issueUlt
			if 'null' in issueString.lower():
				nullFound = True
			if nullFound == True:
				listToWrite = []
				listToWrite.append(repo)
				listToWrite.append(commit.hash)
				listToWrite.append(commit.committer_date)
				listToWrite.append(commitString)
				listToWrite.append(pullString)
				listToWrite.append(issueString)
				gitDataWriter.writerow(listToWrite)
				gitList.flush()

##begin main
