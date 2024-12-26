
package inheritance.main;

import callgraph.visitor.CallGraphVisitor;
import inheritance.visitor.FileVisitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


public class InheritanceRunner {
	
	//static HashMap<String, ArrayList<String>> myMap;
	private static final String myCSV = //"/Users/matt/eclipse-workspace/densityStudy.csv";
			//"/home/mjfarrer/callgraph/densityStudy.csv";
			"/Users/matt/eclipse-workspace/inheritance.csv";
		//	"/mnt/inheritance.csv";
	static ArrayList<HashSet<String>> inheritanceSets;
	
	static String commitID;
	static String projectPath;
	static String repoName;
	
	
    public static void main(String args[]) throws IOException {
    	
    	inheritanceSets = new ArrayList<>();
    	projectPath = args[0];
    			//"/Users/matt/project/test/facebook/stetho";
    	repoName = projPath(args[0]);
    			//projPath("/Users/matt/project/test/facebook/stetho");
    	commitID = args[1];
    //	commitID = "xxx";		
    	
    	boolean append = false;
    	
    	File outputCSV = new File(myCSV);
    	if (outputCSV.exists()) {
    		append = true;
    	}
    	
    	//start = false;
        // Swap out the project base automatically
    	BufferedWriter writer = Files.newBufferedWriter(Paths.get(myCSV), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.
				DEFAULT.withHeader("Repo", "Commit", "Class Set").withSkipHeaderRecord(append));
		//				"Total Params", "Num Params w Checker", "Total Fields", "Num Fields w Checker" ));
		csvPrinter.flush();
    	//myMap = new HashMap<>();
        //String projectBasePath = args[0];
		String projectBasePath = "/Users/matt/project/test/facebook/stetho";
        	//"/Users/matt/eclipse-workspace/testOwner/testRepo4";
        		//"/Users/matt/Project/test/facebook/stetho";
        try {
            List<String> filePaths = Files.walk(Path.of(projectBasePath))
                    .map(p -> p.toString())
                    .filter(p -> p.endsWith(".java"))
                    .collect(Collectors.toList());
            String[] paths = new String[filePaths.size()];
            filePaths.toArray(paths);

           /* List<String> folderPaths = Files.walk(Path.of(projectBasePath))
                    .filter(path -> Files.isDirectory(path))
                    .map(path -> path.toString())
                    .filter(path -> !path.contains(".git"))
                    .collect(Collectors.toList());
            String[] basePaths = new String[folderPaths.size()];
            folderPaths.toArray(basePaths);*/
            
            int index = 1;
            for (String filePath : filePaths) {
            		index++;
            		System.out.println(filePath);
                    processFile(filePath);           
            }
           // csvPrinter.printRecord(numMethods, checkerMethods, numParams, checkerParams, numFields, checkerFields);
           // csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("final size " + inheritanceSets.size());
       ArrayList<String> output = stringify(inheritanceSets);
      // System.out.println("output size " + output.size());
       for(String s: output) {
    	   
    	   csvPrinter.printRecord(projPath(projectBasePath), commitID, s);
    	   csvPrinter.flush();
    	   System.out.println("S " + s);
    	   
       }
        
    }
    private static ArrayList<String> stringify(ArrayList<HashSet<String>> myList){
    	
    	ArrayList<String> retVal = new ArrayList<>();
    	for(HashSet<String> hs : myList) {
    		String next = "";
    		for(String s : hs) {
    			next += s + "\n";
    		}
    		retVal.add(next);
    	}
    	
    	return retVal;
    }
    private static String projPath(String input) {
    	
    	String[] names = input.split("/");
    	String retVal = "";
    	//i start if 4 if project is local
    	// i starts at 3 if project is production
    	for (int i = 4; i < names.length-1; i++) {
    		
    		retVal += names[i] + "/";
    	}
    	if(names.length > 0) {
    		retVal += names[names.length-1];
    	}
    	return retVal;
    }

    private static void processFile(String filePath) {
        ASTParser parser = ASTParser.newParser(AST.JLS12);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_12);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        
        
        parser.setEnvironment(new String[] {"."}, new String[] {""}, new String[] {"UTF-8"}, true);
        //parser.setEnvironment(new String[] {"."}, basePaths, encodings/*new String[] { "UTF-8" }*/, true);
        File file = new File(filePath);
        String text = getFileContent(file);
        parser.setSource(text.toCharArray());
        parser.setUnitName(file.getAbsolutePath());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
//        IProblem[] problems = cu.getProblems();
//        if (problems != null && problems.length > 0) {
//            System.out.println("Got {} problems compiling the source file: " +  problems.length);
//            for (IProblem problem : problems) {
//                System.out.println(problem);
//            }
//        }
        FileVisitor visitor = new FileVisitor(cu, filePath);
      
        cu.accept(visitor);
        ArrayList<String[]> myList = visitor.getInheritanceList();
      //  System.out.println("isize " + myList.size());
        
        ArrayList<HashSet<String>> mergedList = merge(myList);
      //  System.out.println("msize " + mergedList.size());
        for (int i = 0; i < mergedList.size(); i++) {
        	HashSet<String> cur = mergedList.get(i);
        	System.out.println("checking merged list");
        	for (String c: cur) {
        		System.out.println(c);
        	}
        	System.out.println("done checking merged list");
        	boolean found = false;
        	for(int j = 0; j < inheritanceSets.size(); j++) {
        		HashSet<String> next = inheritanceSets.get(j);
        		System.out.println("checking inheritanceSets");
        		for(String a: next) {
        			System.out.println(a);
        		}
        		System.out.println("done checking inheritanceSets");
        		for(String first : cur) {
        			//boolean found = false;
        			for(String second: next) {
        				if (first.equals(second)) {
        					for(String s: cur) {
        						next.add(s);
        						System.out.println("found match");
        						System.out.println("adding " + s);
        						found = true;
        						//break;
        					}
        					
        				}
        				if (found) {
        					break;
        				}
        			}
        		}
        	}
        	if(!found) {
        		inheritanceSets.add(mergedList.get(i));
        		
        			System.out.println("adding ");
        			HashSet<String> mySet = mergedList.get(i);
        			for(String b: mySet) {
        				System.out.print(b + " ");
        			}
        			System.out.println();
        		
        	}
        }
        //List<TypeDeclaration> myList = getTypes(cu);
        //for(TypeDeclaration td: myList) {
        //	System.out.println(td.resolveBinding().getQualifiedName());
        //}
       // return mergedList;
    }
    private static ArrayList<HashSet<String>> merge(ArrayList<String[]> myList){
    	
    	ArrayList<HashSet<String>> retVal = new ArrayList<>();
    	for(String[] temp: myList) {
    		boolean found = false;
    		for(int i = 0; i < retVal.size(); i++) {
    			HashSet<String> cur = retVal.get(i);
    			if (cur.contains(temp[0]) || cur.contains(temp[1])) {
    				found = true;
    				cur.add(temp[0]);
    				cur.add(temp[1]);
    				retVal.set(i, cur);
    			}
    		}
    		if (!found) {
    			HashSet<String> mySet = new HashSet<>();
    			mySet.add(temp[0]);
    			mySet.add(temp[1]);
    			retVal.add(mySet);
    		}
    	}
    	for(int i = 0; i < retVal.size(); i++) {
    		HashSet<String> first = retVal.get(i);
    		
    		for(int j = i + 1; j < retVal.size(); j++) {
    			HashSet<String> second = retVal.get(j);
    			boolean flag = false;
    			for(String fString : first) {
    				
    				for(String sString : second) {
    					if(fString.equals(sString)) {
    						
    						for(String tString: second) {
    							first.add(tString);
    						}
    						flag = true;
    						break;
    					}
    				}
    				if (flag == true) {
    					retVal.remove(j);
    					j--;
    					break;
    				}
    			}
    		}
    	}
    	return retVal;
    	
    	
    }
    private static List<TypeDeclaration> getTypes(CompilationUnit iCompilationUnit) {
        return ((List<AbstractTypeDeclaration>) iCompilationUnit.types())
                .stream()
                .filter(abstractTypeDeclaration -> abstractTypeDeclaration instanceof TypeDeclaration)
                .map(abstractTypeDeclaration -> (TypeDeclaration) abstractTypeDeclaration)
                .collect(Collectors.toList());
    }
    private static String getFileContent(File file) {
        try {
            return Files.readString(Path.of(file.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    private static String cleanInvok(String input) {
		System.out.println("cleaning invok " + input);
		int index = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '('){
				index = i;
				break;
			}
		}
		
		if (input.charAt(index+1)==')') {
			return input;
		}
		String mName = input.substring(0, index);
		
		String params = input.substring(index+1);
		params = params.substring(0, params.length()-1);
		if (params.equals(",")) {
			return "";
		}
		//split on commas not inside angle brackets
		int bracketCount = 0;
		ArrayList<String> myParams = new ArrayList<>();
		for (int i = 0; i < params.length(); i++) {
			if (params.charAt(i) == '<') {
				bracketCount++;
			}
			else if (params.charAt(i) == '>') {
				bracketCount--;
			}
			else if (bracketCount == 0 && params.charAt(i) == ',') {
				String temp = params.substring(0, i);
				myParams.add(temp);
				params = params.substring(i+1);
			}
		}
		//exit loop, take care of last param
		myParams.add(params);
		
		ArrayList<String> nuParams = new ArrayList<>();
		bracketCount = 0;
		for (int i = 0; i < myParams.size(); i++) {
			ArrayList<String> tempParam = new ArrayList<>();
			String temp = myParams.get(i);
			for (int j = 0; j < temp.length(); j++) {
				
				if (temp.charAt(j) == '<') {
					bracketCount++;
				}
				else if (temp.charAt(j) == '>'){
					bracketCount--;
				}
				else if (bracketCount == 0 && temp.charAt(j) == '.') {
					String subTemp = temp.substring(0, j);
					tempParam.add(subTemp);
					temp = temp.substring(j+1);
					j=-1;
				}
			}
			//take care of last piece
			tempParam.add(temp);
			
			nuParams.add(tempParam.get(tempParam.size()-1));
			
		}
		String retVal = mName;
		retVal += "(";
		for (int i = 0; i < nuParams.size()-1;i++) {
			retVal += "null,";
			
		}
		retVal += "null";
		
		retVal += ")";
		return retVal;
	}
    
    public static String getRelativePath(String path, String project, String owner) {
		String[] myPath = path.split("/");
		int index = 0;
		String retVal = "";
		
		for (int i = 0; i < myPath.length; i++) {
			if (myPath[i].equals(owner)) {
				index = i +1;
				break;
			}
		}
		for (int i = index + 1; i < myPath.length - 1; i++) {
			retVal += myPath[i];
			retVal += "/";
		}
		
		if (myPath.length > 0) {
			retVal += myPath[myPath.length - 1];
		}
		return retVal;
	}
    
    private static String cleanDecl(String myDecl) {
		System.out.println("cleaning decl " + myDecl);
		String nuString = "";
		String [] parsedDecl = myDecl.split(" ");
		int index = 0;
		for(int i = 0; i < parsedDecl.length; i++) {
			if (firstParens(parsedDecl[i])) {
				index = i;
				break;
			}
		}
		for (int i = index; i < parsedDecl.length -1; i++) {
			nuString += parsedDecl[i] + " ";
		}
		nuString += parsedDecl[parsedDecl.length-1];
		String revisedMethodName = getMethodName(nuString);
		ArrayList<String> paramList = getRevisedParams(nuString);
		revisedMethodName += "(";
		if (paramList.size() == 0) {
			revisedMethodName += ")";
			return revisedMethodName;
		}
		for(int i = 0; i < paramList.size()-1;i++) {
			revisedMethodName += paramList.get(i) + ",";
		}
		revisedMethodName += paramList.get(paramList.size()-1) + ")";
		return revisedMethodName;
	}
    
    private static ArrayList<String> getRevisedParams(String input) {
		
		int index = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '(') {
				index = i;
				break;
			}
		}
		ArrayList<String> retVal = new ArrayList<>();
		String nuString = input.substring(index+1);
		if (nuString.equals(")")) {
			return retVal;
		}
		//isolate parameters
		nuString = nuString.substring(0, nuString.length()-1);
		if (nuString.equals(")")){
			return retVal;
		}
		//String[] paramsTentative = nuString.split(",");
		int bracketCount = 0;
		ArrayList<String> myParamsTent = new ArrayList<>();
		for (int i = 0; i < nuString.length(); i++) {
			if (nuString.charAt(i) == '<') {
				bracketCount++;
			}
			else if (nuString.charAt(i) == '>') {
				bracketCount--;
			}
			
			else if (nuString.charAt(i) == ',' && bracketCount == 0) {
				String temp = nuString.substring(0, i);
				myParamsTent.add(temp);
				nuString = nuString.substring(i+1);
				i = -1;
			}	
		}
		//exit loop, take care of last param
		myParamsTent.add(nuString);
		
		for (int i = 0; i < myParamsTent.size(); i++) {
			
			String[] miniParams = myParamsTent.get(i).split(" ");
			String pNu = "";
			for (int j = 0; j < miniParams.length; j++) {
				if (miniParams[j].charAt(0) != '@' && j < miniParams.length-2) {
					pNu += miniParams[j] + " ";
					
				}
				else if (miniParams[j].charAt(0) != '@' && j < miniParams.length-1){
					pNu += miniParams[j];
					
				}
			}
			retVal.add(pNu);	
		}
		ArrayList<String> nuVal = new ArrayList<>();
		for(int i = 0; i < retVal.size();i++) {
			String temp = retVal.get(i);
			String nu = "";
			int bCount = 0;
			for(int j = 0; j < temp.length();j++) {
				if (temp.charAt(j)=='<') {
					bCount++;
				}
				else if (temp.charAt(j)=='>') {
					bCount--;
				}
				else if (bCount == 0 && temp.charAt(j)==' ' && i < temp.length()-1) {
					temp = temp.substring(j+1);
 				}	
			}
			nuVal.add(temp);
		}
		ArrayList<String> nuVal2 = new ArrayList<>();
		for (int i = 0; i < nuVal.size(); i++) {
			nuVal2.add("null");
		}
		return nuVal2;
	}
    
    private static String getMethodName(String input) {
		String retVal = "";
		for (int i = 0; i < input.length(); i++) {
			
			if (input.charAt(i) == '(') {
				retVal = input.substring(0, i);
				break;
			}
		}
		return retVal;
	}
    
    private static boolean firstParens(String input) {
		
		for (int i = 0; i < input.length(); i++) {
			
			if (input.charAt(i) == '(') {
				return true;
			}
		}
		return false;
	}
}
