# TypeCheckerProject

This code, data and writeup was submitted as my Master's Degree Research Project at Davis, California on June 30, 2021.

################

We have updated the repo to include programs and data.  The programs are written in both Java and Python.

It should be possible to clone this repo and open it as a Java (Maven) project in Eclipse or IntelliJ, for example.  This should automatically provide all needed Java dependencies.  Python dependencies like PyDriller may need to be installed.  All Python dependencies are found in the import statements at the top of each Python program. 

Here are a couple of pointers about cloning into Eclipse.  Eclipse can be a bit finicky (we heard with IntelliJ it is more like just drag and drop.)  In Eclipse, first create a folder and clone the repo into this folder.  From inside Eclipse, choose File -> Open Project from File System.  Choose the top of the file tree, which is "TypeCheckerProject".  (Note - don't choose the folder where you put the project.)  After that, select the blue field "Show other specialized import wizards".  Then, select Existing Maven Projects.  Assuming there are no naming conflicts, you should be able to import the project now as a Maven project.  Finally, to compile some of the Java programs, you must tell Eclipse to use the correct Java version.  We are using Java 11 (project may not compile using Java 10 or below).  In Eclipse, Java version can be managed from right clicking the project, selecting Properties, then Java Build Path -> Libraries.  You may need to remove your current JRE System Library and choose a different one.  

We will try and discuss each program in the order they were writen.

1. ecs160.visitor.visitorexample4.App.java

This was the first program we wrote, well the second actually.  We first wrote a script to identify the top 100 Java projects ranked by Github stars. This was simply a command entered at the command line invoking the Git API v3 (Rest API), requesting the top 100 Java repos by stars.  Our first task was to collect annotations from these repositories, as the annotations existed at that time.  This program requires one argument, which is the path to a directory containing Java repositories.  The program will produce a csv file, nuCSV.csv, listing all annotations (not only nullcheckers) that are applied to methods, method parameters, local variables (inside methods), and fields.  The output path for the csv file is hardcoded at line 45, so this should be changed as needed.  

A count of the lines of nuCSV.csv shows that the total number of all annotations across the 100 projects is 710,996.  Of these, 73,470 contain the substring "null" and appear to be null checkers.  These 73,470 nullcheckers are found in 57 of the top 100 Java projects.  We found that the 73,470 breaks down as follows:

Parameters: 44,170
Methods: 20,568
Fields: 8,357
Local Variables: 373

Thus, we can infer that approximately 10 percent of the Java annotations found in these projects that are attached to these constructs, are null checkers.  Nullcheckers that regulate the flow of null into and out of methods are the most popular.  A quick note about constructors: it turns out the Eclipse JDT parses constructors as method declarations.  When a method declaration receives a nullchecker annotation, the annotation is applied to the method's return value.  Of course, constructors do not have a return value, however, they often do have parameters.  We have not done any analysis to determine what proportion of the 44,170 parameter nullecheckers are applied to constructor parameters.  Although this appears less common than annotating ordinary method parameters, it is not entirely uncommon.

2. 4newfgNullCSVtest.py

fg stands for "fine grained".  This program represents our next step after program 1.  Program 1 gives us a csv file listing all annotations for the repositories.  Our next steps were: 

First, filter the annotation.csv to only include nullcheckers, i.e. annotations that include the substring "null" or "Null".

Second, iterate over the Git history using PyDriller, and find the "first introduction" of each nullchecker annotation.  I.e., at some point a particular annotation "appears" in the repository for the first time.  We want to find that commit for each annotation, and then print out any Git commentary, such as commit message, pull request, or any Git issues mentioned in the commit or pull request.

This program requires as input the "nuCSV.csv" file that was output by program 1.  It outputs two new csv files: first, myNulls.csv, which is just nuCSV filtered by annotations that include the substring "null"; second, 12fgTestNullAnnotations.csv.  The 12fg file identifies the commit that introduced each nullchecker and provides the Git commentary.  

Examining the 12fg file shows that there are 70,217 rows.  Thus, we have found the first introduction of 70,217 out of the 73,470 nullchecker annotations.  There is some margin of error, but not a huge one (less than 5 percent).  


We should note that the Git commentary here is very much hit or miss.  Sometimes there is no commentary at all.  Other times (especially for large checker commits), we may see some messages about adopting the checkers.  Other times, a checker change may be just one part of a larger commit.  So, we may see Git commentary that is off topic.  (Note - we later ran a separate study to collect git commentary.  The results appear in git.Data.csv.zip).

Program 3 is a Python program, but it uses a Java program to parse the change set provided by PyDriller.  

3. parserFG.visitorexample4.App.java

This program is used by program 2 to parse the change sets.  We converted it to a jar file to interact with program 2.  It uses some file paths that are hard coded - an input path of a file for it to parse.  This is given at line 146.  It will write its output to a path computed at lines 116-117.  These paths should be modified as needed.

4. R programs

After obtaining the 12fg csv file, we have some data about about when and where different checker annotations first appear in each repo.  This spawned a number of studies that analyze this data.  One metric of interest is the number of checkers added in a commit.  Sometimes the number of checkers is quite large, perhaps over 1000.  A good way to see this is to inspect the csv file, 2dataNumbers.csv.  This file lists every commit that introduces a nullchecker (at least one), and also includes a column for how many checkers were introduced in said commit.  A quick sort of the csv file by checker number will show all of the larger ones at the top.  Commit messages are included here and, perhaps unsurprisingly, tend more often to address null checker related issues for the commits that add more null checkers.

These difference raise several research questions: what is different about the commits that add checkers versus the ones that don't?  And what about the commits that add large numbers of checkers versus the ones that add small ones?  How does the addition of checkers in a commit vary with respect to the lines of code added or removed?  The number of files touched?  How many unique authors/committers are involved in checker adding commits, versus commits that don't add checkers?  Do they tend to be added early in a repo's history or later?

Several theories were discussed at this stage of the project.  One theory to explain commits that add large numbers of null checkers is the concept of a "flag day".  The general meaning of "flag day" is something like this: Company X has has decided that null checkers are good.  From now on, workers must add null checkers to the code.  The directive comes down from management, and a large number of null checkers are added.

Some other theories are that adding nullcheckers may be a specialized job.  Thus, we expect that fewer developers are involved in adding the checkers as compared to code in general.

Our first attempt at exploring these topics is shown in the data file, Commits_Null.csv.  This was created using Null_Touches.csv, myNull.csv, and the Program NullCommits.py.  Commits_Null.csv groups the commits for each project according to whether they do or do not introduce null checkers, and prints several numbers for each group.  The numbers considered are lines of code added or removed, average files touched per commit, number of unique authors and number of unique committers.  

Null_Touches.csv was created using the Python program Null_Touches2.py.  This was a modified version of the 4newfg program. It iterates through and print data regarding all commits in a repository's history, the prints a field "Yes" or "No" which indicates whether the commit in question introduced a null checker.

Regarding authors and committers, Commits_Null.csv indicates these numbers are lower for the group of commits that introduce null checkers.  However, this may simply be because there are fewer commits that introduce the checkers.  Lines of code are sometimes less and sometimes greater for commits with versus without checkers.  However, we are simply considering the two groups of commits at this point, without accounting for the difference in number of commits.   We also print he average number of files touched for each group of commits.  Notably, this number is greater for the group of commits that add null checkers.  Even so, an average may be skewed by outliers.

This data was sufficient to pique our curiosity, but has some weaknesses, as noted above.  We sought to explore these issues further.  We needed a way to account for the fact that the commits that introduce the checkers tend to be fewer in number than commits overall.  Ultimately, we used two techniques to gather better statistics: bootstrapping and Mann-Whitney Wilcoxon test.  

For files touched and lines of code added and removed, we used Mann-Whitney Wilcoxon test, specifically a two sample unpaired test.  The alternative hypothesis is that the commits that introduce the checkers touch more files and lines of code.  myDF.csv gives a p-value for these metrics that is produced by the test.  The small p-value indicates we should reject the null hypothesis and accept the alternative hypothesis for files and lines of code touched.  As an additional measure, we compute the Benjamini-Hochberg modified p-value.  However, the p-values are extremely small here, so applying Benjamini-Hochberg does not affect the outcome.

Mann-Whitney works by ranking each sample according to a numerical value.  We cannot do this for unique authors and committers because we do not have a numerical value for each commit.  Instead, for these metrics we use bootstrapping.

With bootstrapping, we draw 100,000 samples of commits from each project.  Each sample is the same size as the number of commits for the project that introduce checkers.  However, the sample is drawn at random from all commits, i.e. both those that include checkers as well as those that do not.  We wish to compare the bootstrapped distribution of the metrics of interest with those found in the commits that introduce the checkers.  The end goal is to produce a p-value which should indicate the likelihood that the "null hypothesis" is true.  In this case the null hypothesis is that there is no difference between the distribution of these metrics between commits that do versus do not add checkers.

For authors and committers, the alternative hypothesis was that these numbers are smaller for commits that introduce checkers.  We compute the p-values by first computing the cumulative probability distribution function for the bootstrapped distribution.  We then simply ask R for the cumulative probability of our reading from the commits that introduce the checkers.

During this part of the study, we reduced the number of repositories under consideration to 26.  The reason for eliminating the other repos was inadequate number of commits that introduce null checkers.  The threshold chosen was to require a the Git history for the repository to have at least 25 commits that add checkers.  

The code for this study is in MWhitney.Rmd, in the folder Rprograms.  The results of this study are in the Data folder, in the file myDF.csv.  myDF.csv includes p-values for each metric: files touched, lines of code added or removed, unique authors and unique committers.  P-values always range from 0 to 1.  A very small p-value will refute the null hypothesis.  

In the case of files touched and lines of code added or removed, myDF.csv shows that the p-values are invariably very small across the 26 projects.  However, the numbers for unique authors and committers produced by this study are less conclusive.  myDF.csv shows that although the p-values for these metrics are indeed sometimes very small, in other cases they are large.  This suggests there is not a consistent pattern with authors and committers as there is with files touched and lines of code added or removed.

Further R programs

myDF.csv suggests a curious phenomenon regarding the addition of the nullchecker annotations.  According to this data, when we consider the commits that add nullcheckers to the 26 projects, these commits affect more files and lines of codes when compared to commits that do not add the nullcheckers.  This is a curious fact, and one that we have still not fully explained.  Following this result, we pursued a number of other follow up studies to try and explain this data.

We will first describe some of the theories that were discussed.  First, we are curious whether this relationship is significant or whether it is caused by some confounding variable.  For example, if nullcheckers are always added later in a project, the project will be bigger at that time both when measured by files or lines of code.  So, perhaps the explanation is based on the timing of when nullcheckers are added.  

We used the R program regressionStudy.rmd along with the data file processed.xlsx to examine the relationship between the time during a project when nullcheckers are added and how many are added.  The results are given in the data file checkerTimes.csv.  We first compute a "midpoint" date, which is the point in time halfway between the first day when nullcheckers are added to a project and the last day when they are added.  We then compute how many nullcheckers were added before and after this day.

The results do not show a strong correlation in either direction, although for a majority of projects more nullcheckers are added after the midpoint instead of before.  

The addition of annotations versus time to each project is also shown in the graphs included in the folder AnnvTimeGraphs, which is inside the dataFiles folder.  Computing these graphs is straightforward using the existing data about annotation introducing commits.  We include the R program used here, annotsVTime.Rmd.  A review of these graphs shows again that there is no particular pattern to when during a project's history the nullcheckers are added.  

The annotation versus time graphs also disclose another interesting phenomenon, which is the inequality in how many checkers are added for each commit.  Looking through the graphs, it seems they often shoot up at points where a large number of checkers is added.  At other times they move in a more linear fashion.  Generally, there seems to be a large inequality for almost every project in the number of annotations that are added per annotation adding commit.   

As noted, we do not see a strong correlation between large annotation adding commits and time.  A related idea is that perhaps the larger annotation adding commits occur when the project is larger in terms of lines of code.  We carried out this comparison using the R program regressionStudy.rmd.  Graphs of the linear regression are shown in the folder regressionPics, inside the dataFiles folder.  

In the regresion graphs, we plot the number of annotations added per commit against current total lines of code in the project.  Interestingly, there is no prevailing pattern here.  The lines drawn by the model are mostly flat, suggesting that there is no strong correlation.  Of course, particular projects may have linear models drawn that have small upward or downward slopes.

These graphs are also interesting because they show clearly that the vast majority of annotations adding commits across all projects are small.  Commits that add large numbers of annotations are unusual. 

Noticing that most checker adding commits add a small number of checkers, with some occasional checkers that add larger numbers of checkers, gave rise to some further studies of this phenomenon.  Gini coefficient is used in statistics as a measure of inequality found in a distribution.  A gini coefficient is in the range 0 <= GF < 1.  A higher gini coefficient means greater inequality.  A gini coefficient of zero means all quantities in the distribution are equal.  

The same information regarding inequality of a distribution can be shown graphically using a Lorenz curve.  A Lorenz curve represents inequality based on divergence from a straight line.  The greater the divergence, the greater the inequality found in the distribution.

Our first attempt at this kind of study is LorenzCurves.Rmd.  We printed the Lorenz curve for the distribution of checker commits added for each checker adding commit.  The results from this program are stored in the LorenzCurves folder inside the dataFiles folder.  A review of these graphs shows that none of them are straight lines.  The greater the accentuation of the curve, the greater the inequality of the distribution.

A Lorenz curve is useful, but the same information can be conveyed as a Gini coefficient.  We compute the same information, i.e. inequality of number of checkers added among checker adding commits, and express it as a Gini coefficient for each project.  The code to perform this computation is included as Gini.rmd.  The Gini coefficients are included as ginisPerProj.csv.  The Gini coefficients range between .469 and .899.  

So far we have gathered some data regarding individual commits, and also regarding an entire repository.  However, at this point we decided to gather some data regarding smaller time windows within a project.  Our first attempt at this kind of study is AddAuthors2Processed.Rmd.  Here, we are still looking at data about commits that add nullcheckers, however, this time we are looking specifically at time windows of maximum size 90 days.  Our approach here is to initialize our day range to 0 based on the first commit that adds a nullchecker.  We iterate forward in the commit history.  Once we hit a commit that is greater than 90 days past the first commit, we compile the data from the first window, then start a new window with time == 0 set to the current commit.

The results from AddAuthors2Processed are two new data files.  We have checkDatwAuthors.csv.  Essentially, this adds information about authors and committers to the file processed.xlsx.  Also, we have ginisByMonth.csv.  This file provides data about checker adding commits within 90 day ranges for each project.  The information includes the start date and end date for each 90 day window.  Also, we print the lines of code in the project at the end of each window, and the number of unique authors who contributed to code that introduced checkers during that window.  Finally, we print the Gini coefficient that measures inequality in the distribution of number of checkers added by checker adding commits during each 90 day window.  

This line of inquiry was pursued to see if there are any trends as far as changes in patterns of how checkers are added to projects over time.  For instance, it would seem reasonable that in general, the number of authors who add checkers to a repo during a time window should generally increase during the repository's history.  While this does appear generally true, it is not always the case.  Further, we were curious if there were any patterns in terms of changes to the Gini coefficients over time.  Again, there does not seem to be a clear pattern here.  The Gini coefficient is probably larger during time windows when large numbers of checkers were added.  However, as noted previously, this does not always happen in a consistent part of a repository's history across all repositories.  I.e., sometimes large numbers of checkers could be added earlier versus later, depending on the project.

We later supplemented ginisByMonth.csv with some additional data.  The updated data is shown in the file GinisBy90DayWindow.csv.  The R program we used for this was NuGinis.Rmd.  First, we replace the column for uniqueAuthors, which showed unique authors for each 90 day window, with uniqueAuthorsFromBeginning.  uniqueAuthorsFromBeginning, as the name suggests, gives the number of authors that have contributed nullcheckers from the beginning of the project's history.  Next, we add columns for numAnnotationsThisWindow and numCommitsThisWindow.  Our hope was that these additional metrics might help shed some light on how checkers become more widespread in a project.

Our last study using R programs to analyze commit histories was a cooperative effort with Lei Chen.  The code that Matt wrote is in NuGinis.Rmd.  (We need to get Lei's code and talk to him about what his Gini numbers represent).

First of all, we use the Python program MakeTouches2.py to creat a modified version of Null_Touches.csv, which we call NullTouches2.csv.  With NullTouches2.csv, instead of a simple "Yes" or "No" with regard to whether any nullcheckers were introduced, we print the number of nullcheckers introduced.  

Our next step is to read in NullTouches2.csv as a data frame.  Initially,
we compute date ranges as unique integers that will represent a 3 month interval, and add these to our data frame.  

The output file for this program is GinisBig.csv.

We print the following information for each date range: cumulative unique Authors (for all commits), cumulative number of commits, cumulative null checkers.  Then, we also print unique authors, commits and nullcheckers for the subject time window only.  Similarly, we print both cumulative and non-cumulative data for lines of code added during each time interval. 

Several Gini coefficients are also computed, both for cumulative and non cumulative data.  The columns ginisCumCommits, ginisCumAuthors, ginisWindowAuthors, and ginisWindowCommits, are recording the Gini coefficient that measures the inequality of number of checkers added.  We compute this number both per commit, as well as based on a single number of checkers added by particular authors in the time window.  These Ginis are all high, quite often above .9.  We believe this is because of the presence of many zero values for commits that do not add any checkers.

Following these four Gini coefficients for each time interval, we also print information about the lines of code added for each time interval, both cumulatively and non-cumulatively.  

We then print 4 additional Gini coefficients for each time interval.  (Since Lei computed these, we need to check with him how they are computed.) 
 
5. 6finalCallGraph.py

Program 2 and the RPrograms gave rise to some discussions where we tried to explain the different kinds of commits that introduce checkers, namely, why do some of them add large numbers of checkers while others do not?  One theory is that checker changes are connected.  In other words, addition of a checker like "@NonNull" at a particular location, e.g. a method parameter, may necessitate the addition of another checker somewhere else in the code.  Program 5 seeks to study this problem, and also introduces a detailed way of detecting checker changes in the Git history.

Program 5 includes both a Python part and a Java part, similar to program 2.  For the Python part, we iterate over the commits searching for any change with respect to nullcheckers.  The kinds of changes we consider are addition (nullchecker appears in repo), deletion (nullchecker is removed from repo), and modification (nullchecker changes to a different nullchecker, e.g. @NonNull changes to @Nullable).

For this study, we are interested in commits that exhibit a change with respect to nullcheckers.  If we see such a change, we run the Java program to collect addition information about the state of the repo after that commit.  This information is collectively known as the "call graph".

These programs produce 4 data files for every repo upon which they are executed.  The first, annotsOut.csv, gives a high level view of each commit wherein a checker change is found.  Each row in this csv file contains information about a particular commit, like how many checkers were added or removed.  

The second, annotList.csv, gives more detailed information about each checker change.  It contains one row for each nullchecker that is added, removed, or modified.  It also gives information like what method or field the annotation is attached to and commit hash.

There is a slight difference between which changes are included in annotsOut.csv (and for which we generate the call graph), versus annotList.csv.  annotList.csv includes even those changes where the thing the annotation was attached to (like method or field), or even the file where it existed, is removed.  annotsOut.csv and the call graph data do not include these kinds of changes.  The rationale for this difference is that in the case where, e.g. a file containing a checker was completely removed from the repo, this commit is lacking the kind of call graph connections that are of interest.

The third data file is a text file.  Its name will be the the name of the github repo, with the suffix '.txt' appended.  This file contains the raw data of the call graph.  It uses AST visitors from the Eclipse JDT to visit all fields and methods declarations in the entire repo (not just the change set).  It enters all method declaration bodies and looks for method invocations and field accesses.  This data is recorded in the following format, for each file: 

commit hash
For each method declaration of field declaration, the file name and then the fully qualified name of the method declaration or field declaration, followed by a semi colon.  In the case of method declarations, the semi colon may be followed by the fully qualified name of any method invocation or field access contained in the method declaration body.  All such invocations/accesses are given as a comma separated list.

It should be noted that we were not always able to resolve the class bindings of method invocation arguments.  For example, the arguments could be instances of classes that are external to the repository.  This issue comes up later when we wrote programs to clean this data for analysis.

The fourth file is functionsOut.csv.  When we create the text file, we also write a csv file at the same time.  The csv file contains the information concerning field declarations, method declarations and invocations/accesses.  The Python program reads this data and constructs data about the distance of changes from checker changes in the call graph.  Each method or field that experiences a checker change is assigned a distance of 0 in the call graph.  Then, the upstream and downstream distances of other methods and fields are computed, up to a maximum distance of 4.  Any method or field that is not within distance 4 of a checker change is assigned a distance of -1.

Call Graph Data

We were not able to upload the call graph data onto Github due to size restrictions.  Some of the files are large, even after compression.  We have made the call graph data available at the following link:

https://drive.google.com/drive/folders/1qIvWigF5D6xPbmfpDwz46bxj7InL0A5v?usp=sharing

6. parserCG.visitorexample4.App.java

The call graph code uses a slightly different version of the AST Parser.  This was done to match the code inside the call graph Runner, which is discussed below.  The main difference is how fully qualified names are computed based on resolution of class bindings.  Modifications like return types are ommitted.  These changes are appropriate in the context of a call graph because we need the names of invocations to match the names of declarations.  

7. callgraph.main.Runner.java

This is the Java program that is invoked when we find a checker change in the change set using program 5.  It will check every file in a repository after a particular commit, and print out the information regarding method declarations, field declarations, and any invocations/accesses.

8. metaScraper.py

Toward the end of the project, we decided to collect meta data on each file for the 26 projects of interest.  We use the Python program metaScraper.py.  The data is stored in the file metaData.csv.  We record data for every file that changes in a commit (i.e., the "change set"). We include data about the type of change (i.e., add, delete, or modify).  We also include information about the file's author and committer.  Part of the purpose of this study was to keep track of which authors are changing which files.  We also make a note of number of lines added, lines removed, and total lines of code in the file.  (note that total lines of code can be less than the "lines" in a file, according to PyDriller).

9. nullReference.py

This program iterates over all commits for each of the 26 projects.  It first scans the commit message and any associated pull request message for the substring "null".  It also scans any issues mentioned in the commit message or pull request for the substring "null".  If it finds the substring "null" in any of these places, it prints any of the commit message, pull request message, or issues (issue body) that are found.

The motivation for this study was to obtain any Git commentary that relates to the null checkers.  One use of this data is to find out if null pointer exception related bugs are decreasing after null checkers are added to the repositories.  

Parsing the commit message and pull request messages for issues turns out to be a bit ad hoc.  We looked for issue numbers preceded by the following strings: "Iss ", "pull/ ", "issues/ ", and "#".  This was based on observing issue numbers preceded by these strings in the data.  It is possible that there are other issue naming conventions that we didn't notice.  

10. Call Graph Cleaners

Toward the end of the project, we wanted to do some analysis using the output from the call graph program, program 5.  This required writing some miscellaneous programs to do things like cleaning and processing the call graph data.  Using the call graph data, it is possible to reconstruct the state of the repository with respect to nullcheckers, at each data point where a checker change was encountered.  

First, there is cleanCGnoFields.src.cleanCG.CG_Cleaner.java.  Also, its counterpart, cleanCGwFields.str.cleanCG.CG_Cleaner.java.  Both of these create cleaned version of the text file where the call graph data is dumped.  There are at least two reasons why this was necessary.  
First, when we dumped the call graph data, we were not careful to substitute every method parameter with the string "null".  A word on this.  When dumping the call graph, we print both method declarations and invocations.  In either case, initially we sought to print any arguments found.  However, in practice we noticed that it was not always possible to resolve bindings of method parameters.  This is because our compilation unit is the code found in the repository itself, and does not include external libraries.  All data is collected statically, without building and running the projects.

As a compromise, we store methods as [resolved class binding] + [method name] + [comma separated list containing the string "null" for each parameter].  Thus, our call graph is an approximation in the sense that it cannot distinguish between methods that are identical with respect to class name and method name, and are the same in number but not in type of arguments.  In other words, it may fail to distinguish overloaded methods.

In any event, if we wish to reconstruct data about the call graph, it is necessary to produce a "cleaned" version, so that the invocations and declarations will match.  This is the purpose of the CG_Cleaner.java programs.  There are two versions, one that includes fields and one that removes fields.  Both may be useful for different purposes.

11. densityProbe.main.ProbeRunner.java

Around the same time we decided to retrieve the call graph data, we also decided to retrieve the nullchecker "density".  The program retrieves, for every commit in a given project, data about: how many methods, parameters, and fields exist in the repository after that commit.  Further, how many methods, parameters, fields in the repository after that commit have a nullchecker.  Thus, we retrieve the nullchecker "density", i.e. the proportion of these constructs at each commit that have received a null checker.

12. inheritance.py

The call graph data provides raw data, upon which different types of analysis are possible. 
During Spring 2021, Lei Chen started working with us on this project.  We have not tried to include all of his work here.  Regardless, one of his tasks was to collect data regarding the in and out degree of methods for commits that receive a null checker change.  Further, he was to determine whether there was any difference for in and out degree of the methods that have checkers applied versus ones that don't.  This can be computed using the call graph data. 

While reviewing his results, we noticed that sometimes method invocations are present in the call graph data (text file) that are not declared.  There may sometimes be a benign explanation for this, e.g. methods are imported from an external library.

However, we decided that a possible threat to our methodology was not considering the effects of inheritance in Java.  Part of the data printed out by the call graph is the call graph "distance" from a checker change.  However, this is computed statically.  For our parser to see a checker change, it must explicitly be attached to the method or field declaration which is invoked.  

We can imagine several scenarios involving inheritance that might evade this methodology.  For example, suppose we have an abstract base class "A", which is extended by derived class "B".  Suppose A contains an abstract method "foo()", which is implemented in B.  Suppose that the checker change is applied to B.foo(), yet a second method "bar()" invokes A.foo().  In that case, our call graph file functionsOut.csv cannot detect that bar() is within distance 1 of a checker change.

Fortunately, it is possible to reconstruct functionsOut.csv with inheritance data included.  A first step to doing this is to retrieve the inheritance hierarchy.  We do this using inheritance.py and inheritanceRunner.java.  This will print, for every commit that experiences a checker change, the inheritance hierarchy of the repository after that commit. 

13. inheritance.main.inheritanceRunner.java

This program will retrieve the inheritance hierarchy of a repository after a particular commit.  We retrieve the inheritance as a list of sets.  This is an approximation, since in reality an inheritance hierarchy is a tree, not a set.  We are overly inclusive in identifying checker changes, rather than underinclusive.  This approach attributes a method declared by one class in a set to all classes in that set.

This program works by visiting every class definition in the repo.  Then, for every class definition, it iteratively retreives that class's parents, its parents' parents, and so on.  We stop when we hit the class Object, since otherwise all classes are grouped into the same set.

14. cgUpdated.py

This program showcases the ability to perform different analysis of interest using the call graph data.  This program takes the four call graph data files and the inheritance.csv file for a repository as input, and updates the functionsOut.csv file for that repository by applying the inheritance data.

We would expect to find a larger number of methods and fields within distance four of a checker change using this approach.  So far we applied it to only one repository, which is facebook/stetho.  We observed a moderate increase of about 10 percent in the number of methods and fields in the change set that are within distance 4 of a checker change, after applying the inheritance data.  There are still a reasonably large number of constructs that change that are not within distance 4.