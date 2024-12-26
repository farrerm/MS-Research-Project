package cleanCGnoFields.src.cleanCG;
import java.io.*;
import java.util.*;

public class CG_Cleaner {

	public static void main(String[] args) throws Exception {
		System.out.println("hello");
		File input = new File("/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/nuStetho/stetho.txt");
		Scanner myScanner = new Scanner(input);
		FileWriter output = new FileWriter("/Users/matt/eclipse-workspace/visitorexample4/parsedFiles/nuStetho/nuStetho.txt");
		//output.write(string);
		while(myScanner.hasNextLine()) {
			//System.out.println("got a line");
			String next = myScanner.nextLine();
			if (isHash(next)) {
				next = next + '\n';
				System.out.println(next);
				output.write(next);
				output.flush();
			}
			//MF needs field declarations for revising functionsOut.csv
			else if (isFieldDecl(next)) {
			//	System.out.println("found field decl");
				//for revising 
				next = next + '\n';
				output.write(next);
				output.flush();
				continue;
			}
			else {
			//	System.out.println("not hash or field decl");
				ArrayList<String> myList = new ArrayList<>();
				int index = 0;
				while(next.charAt(index) != ',') {
					index++;
				}
				String fileName = next.substring(0, index);
			//	System.out.println(fileName);
				fileName += ',';
				String line = fileName;
				//myList.add(fileName);
				index++;
				int nuIndex = index;
				while(next.charAt(nuIndex) != ';') {
					nuIndex++;
				}
				
				String methodDecl = next.substring(index, nuIndex);
			//	System.out.println(methodDecl);
				String nuMethod = cleanMethod(methodDecl);
			//	System.out.println(nuMethod);
				line += nuMethod + ";";
			//	System.out.println(line);
				if(nuIndex + 1 == next.length()) {
				
					
					output.write(line + '\n');
					output.flush();
					continue;
				}
				index = nuIndex+1;
				nuIndex = index;
				int bracketCount = 0;
				int parensCount = 0;
			//	System.out.println("entering loop");
				while (nuIndex < next.length()) {
					if (next.charAt(nuIndex) == '<' ) {
						bracketCount++;
					}
					else if (next.charAt(nuIndex) == '>') {
						bracketCount--;
					}
					else if(next.charAt(nuIndex) == '(') {
						parensCount++;
					}
					else if (next.charAt(nuIndex) == ')') {
						parensCount--;
					}
					else if (bracketCount == 0 && parensCount == 0 &&
							next.charAt(nuIndex) == ',') {
						String candidate = next.substring(index, nuIndex);
						//System.out.println(candidate);
						//this is where we filter out fields.
						if (candidate.charAt(candidate.length()-1) != ')') {
							//System.out.println(candidate);
							nuIndex++;
							index = nuIndex;
							continue;
						}
						else {
							String nuCandidate = cleanMethod(candidate);
							line += nuCandidate + ',';
							index = nuIndex+1;
							nuIndex = index;
							continue;
						}
					}
					nuIndex++;
				}
				String candidate = next.substring(index, nuIndex);
				//System.out.println("at the end");
				if (candidate.charAt(candidate.length()-1) == ')') {
					
					candidate = cleanMethod(candidate);
					line += candidate;
				}
				if (line.charAt(line.length()-1) == ',') {
					line = line.substring(0, line.length()-1);
				}
				
				line += '\n';
			//	System.out.println("final line " + line);
				output.write(line);
				output.flush();
						
			}
		}
		
	}
	private static String cleanMethod(String input) {
		
		int index = 0;
		int parensCount = 0;
		int bracketCount = 0;
		String name = "";
		String nuParams = "";
		//System.out.println("cleaning method");
		while(index < input.length()) {
			
			if (input.charAt(index) == '(') {
				name = input.substring(0, index);
				//System.out.println("Calling");
				nuParams = getRevisedParams(input.substring(index, input.length()));
			//	System.out.println("returned");
				break;
			}
			index++;
		}
		return name + nuParams;
	}
	private static String getRevisedParams(String input) {
		if(input.length() == 2) {
			return "()";
		}
		int parensCount = 0;
		int bracketCount = 0;
		int index = 1;
		int nuIndex = 1;
		ArrayList<String> params = new ArrayList<>();
		while (nuIndex < input.length()) {
			
			
			if (input.charAt(nuIndex) == '<') {
				bracketCount++;
			}
			else if (input.charAt(nuIndex) == '>') {
				bracketCount--;
			}
			if (bracketCount == 0 && input.charAt(nuIndex) == ',') {
				params.add(input.substring(index, nuIndex));
				index = nuIndex;
			}
			else if (bracketCount == 0 && input.charAt(nuIndex) == ')') {
				params.add(input.substring(index, nuIndex));
			}
			
			nuIndex++;
		}
		String retVal = "(";
		for (int i = 0; i < params.size()-1; i++) {
			retVal += "null" + ',';
		}
		retVal += "null";
		retVal += ")";
		return retVal;
	}
	
	private static boolean isFieldDecl(String input) {
		for(int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '(') {
				return false;
			}
		}
		return true;
		
	}
	private static boolean isHash(String input) {
		for(int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '/') {
				return false;
			}
		}
		return true;
	}
}
