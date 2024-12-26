package ecs160.visitor.visitorexample4;

//MF imports
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import static java.nio.file.FileVisitOption.*;
import java.util.*;
//Casey's imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

//import App.Finder;
//import ecs160.visitor.additionalvisitors.CountIfPrintVisitor;
//import ecs160.visitor.additionalvisitors.CountPrintVisitor;
import ecs160.visitor.astvisitors.MethodPrinter;
import ecs160.visitor.astvisitors.FieldPrinter;
import ecs160.visitor.astvisitors.ParameterPrinter;
import ecs160.visitor.astvisitors.LocalVPrinter;
import ecs160.visitor.astvisitors.PackagePrinter;
import ecs160.visitor.utilities.UtilReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
//import org.apache.commons.lang3.text.StrBuilder;

/**
 * A class for setting up an AST visitor and parsing on a single Java file.
 * 
 * @author caseycas
 */
public class App {
	
	private static final String myCSV = "/Users/matt/eclipse-workspace/NuCSV22.csv";
	
	
	public static class Finder extends SimpleFileVisitor<Path> {

		ArrayList<Path> myList = new ArrayList<>();
		ArrayList<String> results = new ArrayList<>();
		Charset charset = Charset.forName("UTF-8");

		private final PathMatcher matcher;
		private int numMatches = 0;
		private String project;
		//private File output;
		private CSVPrinter csvPrinter;
		private String owner;

		Finder(String pattern, String project, CSVPrinter csvPrinter, String owner) {
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
			this.project = project;
			//this.output = output;
			this.csvPrinter = csvPrinter;
			this.owner = owner;
		}

		// Compares the glob pattern against
		// the file or directory name.
		synchronized void find(Path file) {
			Path name = file.getFileName();
			if (name != null && matcher.matches(name)) {
				numMatches++;
				myList.add(name);
				// System.out.println(name);
				// System.out.println(file);
				// myList.add(file);
				File myFile = new File(name.toString());
				// if (myFile.isFile()) {
				analyze(file, project, csvPrinter, owner);
				// }
			}
		}

		void done() {
			// System.out.println("Matched: "
			// + numMatches);
		}

		// Invoke the pattern matching
		// method on each file.
		@Override
		public synchronized FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return CONTINUE;
		}

		// Invoke the pattern matching
		// method on each directory.
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			find(dir);
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return CONTINUE;
		}

	}

	static void usage() {
		System.err.println("java Find <path>" + " -name \"<glob_pattern>\"");
		System.exit(-1);
	}

	public static void main(String[] args) {
		System.out.println("got here");

		File directory = new File("/Users/matt/eclipse-workspace/testOwner/");
			
				
		System.out.println("read dir");
		//list owners
		File[] contentsOfDirectory = directory.listFiles();
		System.out.println("list repos");
		
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(myCSV));
			
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.
					DEFAULT.withHeader("Owner", "Project", "Path", "Package", "FileName", "Class", "Node Type", "FQN", "FQN Parent", "Annotation"));
				
				){
			//csvPrinter.flush(); 
			for (File object : contentsOfDirectory) {
				
				String owner = object.getName();
				//list repos for that owner
				File[] nextContents = object.listFiles();
				
				//System.out.println("Directory name: " + object.getName());
				//System.out.println(object.getName());
				if (nextContents != null) {
				for (File nextObject: nextContents) {
					String[] nextArgs = new String[5];
					
					
					//path
					nextArgs[0] = args[0] + "/" + owner + "/" + nextObject.getName();
					System.out.println(nextArgs[0]);
					nextArgs[1] = "-name";
					nextArgs[2] = "*.java";
					//project name
					nextArgs[3] = nextObject.getName();
					nextArgs[4] = owner;
				
					
					//System.out.println(nextObject.getName());
				try {
					search(nextArgs, csvPrinter);
				} catch (IOException e) {
					System.out.println("IO Exception here");
				}
            
			}//for each nextObject
				}//if not null
		}//for each owner
	}
		
		catch (IOException e) {
			
             System.out.println("IO Exception heree");
         }
		
		
		}
	//}

	public static synchronized void search(String[] args, CSVPrinter csvPrinter) throws IOException {

		if (args.length < 3 || !args[1].equals("-name"))
			usage();

		Path startingDir = Paths.get(args[0]);
		String pattern = args[2];

		Finder finder = new Finder(pattern, args[3], csvPrinter, args[4]);
		Files.walkFileTree(startingDir, finder);
		finder.done();
		
	}

	public static synchronized void analyze(Path fileToSearch, String project,
			CSVPrinter csvPrinter, String owner) {
		String myPath = fileToSearch.toString();
		String fileName = getFileName(myPath);
		System.out.println(myPath);
		System.out.println(myPath);

		File file = new File(myPath);

		String text = "";
		try {
			text = UtilReader.read(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS12); // Create a parser for a version of the Java language (12
																// here)
			Map<String, String> options = JavaCore.getOptions(); // get the options for a type of Eclipse plugin that is
																	// the basis of Java plugins
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_12); // Specify that we are on Java 12 and add it to
																		// the options...
			parser.setCompilerOptions(options); // forward all these options to our parser
			parser.setKind(ASTParser.K_COMPILATION_UNIT); // What kind of constructions will be parsed by this parser.
															// K_COMPILATION_UNIT means we are parsing whole files.
			parser.setResolveBindings(true); // Enable looking for bindings/connections from this file to other parts of
												// the program.
			parser.setBindingsRecovery(true); // Also attempt to recover incomplete bindings (only can be set to true if
									 			// above line is set to true).
			String[] classpath = { System.getProperty("java.home") + "/lib/rt.jar" }; // Link to your Java installation.
			parser.setEnvironment(classpath, new String[] { "" }, new String[] { "UTF-8" }, true);
			parser.setSource(text.toCharArray()); // Load in the text of the file to parse.
			parser.setUnitName(file.getAbsolutePath()); // Load in the absolute path of the file to parse
			//System.out.println(myPath);
			CompilationUnit cu = (CompilationUnit) parser.createAST(null); // Create the tree and link to the root node.

			// Print out information about all methods in the tree.
			// System.out.println("Method Printer:");
			MethodPrinter myMethodPrinter = new MethodPrinter();
			cu.accept(myMethodPrinter);
			FieldPrinter myFieldPrinter = new FieldPrinter();
			cu.accept(myFieldPrinter);
			ParameterPrinter myParameterPrinter = new ParameterPrinter();
			cu.accept(myParameterPrinter);
			LocalVPrinter myLocalVPrinter = new LocalVPrinter();
			cu.accept(myLocalVPrinter);
			PackagePrinter myPackagePrinter = new PackagePrinter();
			cu.accept(myPackagePrinter);
			ArrayList<String> myList1 = myMethodPrinter.getNodeList();
			ArrayList<String> myList2 = myFieldPrinter.getNodeList();
			ArrayList<String> myList3 = myParameterPrinter.getNodeList();
			ArrayList<String> myList4 = myLocalVPrinter.getNodeList();
			ArrayList<String> myList5 = myPackagePrinter.getNodeList();
			
			String myPackage = "null";
			if (myList5.size() != 0) {
				myPackage = myList5.get(0);
			}
			// System.out.println(myPath);
			
			//BufferedWriter writer = Files.newBufferedWriter(Paths.get(myCSV));
			
			//CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.
			//DEFAULT);
				//withHeader("Project", "Path", "Node Type", "FQN", "Annotation"));
			
			if (!(myList1.isEmpty() && myList2.isEmpty() && myList3.isEmpty() && myList4.isEmpty()  )) {
				//PrintWriter writer = new PrintWriter(new FileWriter(output, true));
				
						
				//System.out.println(file.getName());
			//	System.out.println(myList1.size());
				for (int i = 0; i < myList1.size(); i++) {
					
					//StringBuilder sb = new StringBuilder();
					ArrayList<String> newList = new ArrayList<>();
					String relPath = getRelativePath(myPath, project, owner);
					newList.add(owner);
					newList.add(project);
					newList.add(relPath);
					newList.add(myPackage);
					newList.add(fileName);
					
					newList.add(myList1.get(i++));
					newList.add("Method");
					newList.add(myList1.get(i++));
					newList.add(myList1.get(i++));
					newList.add(myList1.get(i));
					//String name = newList.get(3);
					if (newList.size() == 10 && newList.get(6).equals("Method")) {
					//	System.out.println("method");
						csvPrinter.printRecord(newList.get(0), newList.get(1), newList.get(2),
								newList.get(3), newList.get(4), newList.get(5), newList.get(6), newList.get(7),
								newList.get(8), newList.get(9));
						
					
					}
					//System.out.println("got here");
				
				}
				for (int i = 0; i < myList2.size(); i++) {
					
					StringBuilder sb = new StringBuilder();
					ArrayList<String> newList = new ArrayList<>();
					String relPath = getRelativePath(myPath, project, owner);
					newList.add(owner);
					newList.add(project);
					newList.add(relPath);
					newList.add(myPackage);
					newList.add(fileName);
					newList.add(myList2.get(i++));
					newList.add("Field");
					newList.add(myList2.get(i++));
					newList.add(myList2.get(i++));
					newList.add(myList2.get(i));
				//	String name = newList.get(3);
					if (newList.size() == 10 && newList.get(6).equals("Field")) {
						
				//		writer.write(sb.toString());	
					//	System.out.println("field");
						csvPrinter.printRecord(newList.get(0), newList.get(1), newList.get(2),
								newList.get(3), newList.get(4), newList.get(5), newList.get(6), newList.get(7),
								newList.get(8), newList.get(9));
					}
					//System.out.println("field");
				}
				for (int i = 0; i < myList3.size(); i++) {
					
					StringBuilder sb = new StringBuilder();
					ArrayList<String> newList = new ArrayList<>();
					String relPath = getRelativePath(myPath, project, owner);
					newList.add(owner);
					newList.add(project);
					newList.add(relPath);
					newList.add(myPackage);
					newList.add(fileName);
					newList.add(myList3.get(i++));
					newList.add("Parameter");
					newList.add(myList3.get(i++));
					newList.add(myList3.get(i++));
					newList.add(myList3.get(i));
					String name = newList.get(3);
					if (newList.size() == 10 && newList.get(6).equals("Parameter")) {
						
				//		writer.write(sb.toString());	
					///	System.out.println("param");
						csvPrinter.printRecord(newList.get(0), newList.get(1), newList.get(2),
								newList.get(3), newList.get(4), newList.get(5), newList.get(6), newList.get(7),
								newList.get(8), newList.get(9));
					}
				//	System.out.println("params");
				
				}
				//System.out.println(myList4.size());
				for (int i = 0; i < myList4.size(); i++) {
					
					StringBuilder sb = new StringBuilder();
					ArrayList<String> newList = new ArrayList<>();
					String relPath = getRelativePath(myPath, project, owner);
					newList.add(owner);
					newList.add(project);
					newList.add(relPath);
					newList.add(myPackage);
					newList.add(fileName);
					newList.add(myList4.get(i++));
					newList.add("Local Variable");
					newList.add(myList4.get(i++));
					newList.add(myList4.get(i++));
					newList.add(myList4.get(i));
					String name = newList.get(3);
					if (newList.size() == 10 && newList.get(6).equals("Local Variable")) {
						
				//		writer.write(sb.toString());	
						//System.out.println("lv");
						csvPrinter.printRecord(newList.get(0), newList.get(1), newList.get(2),
								newList.get(3), newList.get(4), newList.get(5), newList.get(6), newList.get(7), 
								newList.get(8), newList.get(9));
					}
					//System.out.println("didn't throw");
				
				}
				
			}
		} catch (Exception e) {
			System.out.println("parsing");
			System.err.println(e);
		}

	}
	public static String getFileName(String path) {
		
		String[] myPath = path.split("/");
		String fileName = myPath[myPath.length - 1];
		return fileName;
		
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

}