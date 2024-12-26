package callgraph.main;

import callgraph.visitor.CallGraphVisitor;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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


public class Runner {
	
	static HashMap<String, ArrayList<String>> myMap;
	private static String myCSV = 
			//"/mnt/";
			"/Users/matt/eclipse-workspace/invocations2.csv";
	//static boolean start;
	
	
    public static void main(String args[]) throws IOException {
    	//start = false;
        // Swap out the project base automatically
    	String ownerRepo = args[args.length - 2];
    	String [] oR = ownerRepo.split("/");
    	myCSV += oR[oR.length-2] + oR[oR.length-1] + "invocations2.csv";
    	
    	BufferedWriter writer = Files.newBufferedWriter(Paths.get(myCSV));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.
				DEFAULT.withHeader("Path to Decl", "Method Declared", "Start Pos", "End Pos", "Method Invoked"));
		csvPrinter.flush();
    	myMap = new HashMap<>();
        String projectBasePath = //args[0];
        		"/Users/matt/eclipse-workspace/testOwner/testRepo4";
        		
        try {
            List<String> filePaths = Files.walk(Path.of(projectBasePath))
                    .map(p -> p.toString())
                    .filter(p -> p.endsWith(".java"))
                    .collect(Collectors.toList());
            String[] paths = new String[filePaths.size()];
            filePaths.toArray(paths);

            List<String> folderPaths = Files.walk(Path.of(projectBasePath))
                    .filter(path -> Files.isDirectory(path))
                    .map(path -> path.toString())
                    .filter(path -> !path.contains(".git"))
                    .collect(Collectors.toList());
            String[] basePaths = new String[folderPaths.size()];
            folderPaths.toArray(basePaths);
          //  for (int i = 0; i < basePaths.length; i++) {
          ///  	System.out.println(basePaths[i]);
          //  }
            int index = 1;
            for (String filePath : filePaths) {
            		index++;
                    Map<String, List<String>> declarationToInvocation = processFile(filePath, basePaths);
                    // Change the project name dynamically
                    appendToFile(declarationToInvocation, filePath, projectBasePath, args[1]);
                 for(Map.Entry<String, List<String>> e: declarationToInvocation.entrySet()) {
                    	String decl = e.getKey();
                    	String nuDecl = "";
                    	if (decl.length() > 0 && decl.charAt(decl.length()-1) == ')') {
                    		nuDecl = cleanDecl(decl);
                    	}
                    	else {
                    		nuDecl = decl;
                    	}
                    		
                    	List<String> invocs = e.getValue();
                    	//System.out.println("cleaned decl " + nuDecl);
                    	
                    	//System.out.println("no of invocs " + invocs.size());
                    	if (invocs.size() == 2) {
                    		csvPrinter.printRecord(filePath, nuDecl, invocs.get(0), invocs.get(1), "null");
                    		
                    	}
                    	//System.out.println("got here");
                    	
                    	for(int i = 2; i < invocs.size();i++) {
                    		String iString = "";
                    		if (invocs.get(i).charAt(invocs.get(i).length()-1) == ')') {
                    			iString = cleanInvok(invocs.get(i));
                    		}
                    		else {
                    			iString = invocs.get(i);
                    		}
                    			csvPrinter.printRecord(filePath, nuDecl, invocs.get(0), invocs.get(1), iString);
                    		//System.out.println(invocs.get(i));
                    			//System.out.println("got here");
                    		//System.out.println("invocs size " + invocs.size());
                    	}
                    	csvPrinter.flush();
                    	
                    	
                    }
                   
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void appendToFile(Map<String, List<String>> declarationToInvocation, String fileName, String projectName, String commitHash) {
        
        
        fileName = removeMJF(fileName);
    	String[] temp = projectName.split("/");
    	projectName = temp[temp.length-1] + ".txt";
    //	projectName += "/home/mjfarrer/callgraph/" + projectName;
    	projectName =  "/mnt/" + projectName;
    	//"/Users/matt/eclipse-workspace/" + projectName;
    	File file = new File(projectName);
        try(FileWriter writer = new FileWriter(file, true)) {
        	//if (!start) {
        		writer.write(commitHash + "\n");
        	//	start = true;
        	//}
            for (String key : declarationToInvocation.keySet()) {
                List<String> invocations = declarationToInvocation.get(key);
                String invocationString = "";
                for (int i = 2; i < invocations.size()-1; i++) {
                	invocationString += invocations.get(i) + ",";
                }
                if (invocations.size() > 2) {
                	invocationString += invocations.get(invocations.size()-1);
                }
                		//invocations.stream().collect(Collectors.joining(";"));
                writer.write(fileName + "," + key + ";" + invocationString + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   private static String removeMJF(String input) {
	   try {
	   String retVal = "";
	   String[] nuInput = input.split("/");
	   for (int i = 2; i < nuInput.length -1; i++) {
		   retVal +=  nuInput[i] + "/";
	   }
	   if (nuInput.length > 0) {
		   retVal += nuInput[nuInput.length - 1];
	   }
	   return retVal;
	   }
	   catch(Exception e) {
		   return "";
	   }
   }

    private static Map<String, List<String>> processFile(String filePath, String[] basePaths) {
        try {
    	ASTParser parser = ASTParser.newParser(AST.JLS12);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_12);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        String[] encodings = new String[basePaths.length];
        for(int i = 0; i < encodings.length; i++) {
        	encodings[i] = "UTF-8";
        }
        parser.setEnvironment(new String[] {"."}, basePaths, encodings/*new String[] { "UTF-8" }*/, true);
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
        CallGraphVisitor visitor = new CallGraphVisitor(cu, basePaths);
      
        cu.accept(visitor);
        return visitor.getDeclarationToInvocations();
        }
        catch(Exception e) {
        	Map<String, List<String>> myMap = new HashMap<>();
        	return myMap;
        }
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
		//System.out.println("cleaning invok " + input);
    	try {
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
    	catch (Exception e) {
    		return input;
    	}
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
		//System.out.println("cleaning decl " + myDecl);
    	try {
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
    	catch(Exception e) {
    		return myDecl;
    	}
	}
    
    private static ArrayList<String> getRevisedParams(String input) {
		try {
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
				if (miniParams[j].length() > 0 && miniParams[j].charAt(0) != '@' && j < miniParams.length-2) {
					pNu += miniParams[j] + " ";
					
				}
				else if (miniParams[j].length() > 0 && miniParams[j].charAt(0) != '@' && j < miniParams.length-1){
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
		catch(Exception e) {
			ArrayList<String> nuRet = new ArrayList<>();
			return nuRet;
		}
	}
    
    private static String getMethodName(String input) {
		try {
    	String retVal = "";
		for (int i = 0; i < input.length(); i++) {
			
			if (input.charAt(i) == '(') {
				retVal = input.substring(0, i);
				break;
			}
		}
		return retVal;
		}
		catch(Exception e) {
			return input;
		}
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
