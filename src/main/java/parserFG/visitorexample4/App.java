package parserFG.visitorexample4;

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
import parserFG.astvisitors.MethodPrinter;
import parserFG.astvisitors.FieldPrinter;
import parserFG.astvisitors.ParameterPrinter;
import parserFG.astvisitors.LocalVPrinter;
import parserFG.astvisitors.PackagePrinter;
import parserFG.utilities.UtilReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


public class App {
	
	private static final String myCSV = "/Users/matt/eclipse-workspace/fgCSV.csv";
	
	
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
				//analyze(file, project, csvPrinter, owner);
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

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(myCSV));
			
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.
					//assume fqn, annotation pairs are unique within a file
					//they will not be identical between fields and methods
					DEFAULT.withHeader("Package", "Class", "FQN", "FQN Parent", "Annotation", "Start Pos", "End Pos"));
				
				){
			//csvPrinter.flush(); 
	
					analyze(csvPrinter);
				
		
				}//if not null
		
		catch (IOException e) {
			
             System.out.println("IO Exception");
         }
		
		
		}
	//}

	public static synchronized void analyze(CSVPrinter csvPrinter) {
		//String myPath = "output.java";
		//String myPath = "/Users/matt/eclipse-workspace/testFolder/google/guava/LocalCache.java";
		String myPath = "/Users/matt/eclipse-workspace/testOwner/testRepo4/Car.java";
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
			MethodPrinter myMethodPrinter = new MethodPrinter(cu);
			cu.accept(myMethodPrinter);
			//FieldPrinter myFieldPrinter = new FieldPrinter();
			//cu.accept(myFieldPrinter);
			ParameterPrinter myParameterPrinter = new ParameterPrinter(cu);
			cu.accept(myParameterPrinter);
			LocalVPrinter myLocalVPrinter = new LocalVPrinter(cu);
			cu.accept(myLocalVPrinter);
			PackagePrinter myPackagePrinter = new PackagePrinter();
			cu.accept(myPackagePrinter);
			ArrayList<String> myList1 = myMethodPrinter.getNodeList();
			//ArrayList<String> myList2 = myFieldPrinter.getNodeList();
			ArrayList<String> myList3 = myParameterPrinter.getNodeList();
			ArrayList<String> myList4 = myLocalVPrinter.getNodeList();
			ArrayList<String> myList5 = myPackagePrinter.getNodeList();
			// System.out.println(myPath);
			
			String myPackage = "null";
			if (myList5.size() != 0) {
				myPackage = myList5.get(0);
			}
			
			if (!(myList1.isEmpty() && /*myList2.isEmpty() &&*/ myList3.isEmpty() && myList4.isEmpty())  ) {
				//PrintWriter writer = new PrintWriter(new FileWriter(output, true));
				
				//System.out.println(file.getName());
			//	System.out.println(myList1.size());
				for (int i = 0; i < myList1.size(); i++) {
					
					//StringBuilder sb = new StringBuilder();
					ArrayList<String> newList = new ArrayList<>();
					newList.add(myList1.get(i++));
					newList.add(myList1.get(i++));
					newList.add(myList1.get(i++));
					newList.add(myList1.get(i++));
					newList.add(myList1.get(i++));
					newList.add(myList1.get(i));
					//String name = newList.get(3);(
					//int lineStart = cu.getLineNumber(Integer.parseInt(newList.get(4)+1));
					//int lineEnd = lineStart-1 + Integer.parseInt(newList.get(5));
						
						csvPrinter.printRecord(myPackage, newList.get(0), newList.get(1), newList.get(2), newList.get(3), newList.get(4), newList.get(5));
						
				}
				/*for (int i = 0; i < myList2.size(); i++) {
					
					//StringBuilder sb = new StringBuilder();
					ArrayList<String> newList = new ArrayList<>();
					newList.add(myList2.get(i++));
					newList.add(myList2.get(i++));
					newList.add(myList2.get(i++));
					newList.add(myList2.get(i++));
					newList.add(myList2.get(i++));
					newList.add(myList2.get(i));
					//String name = newList.get(3);
						
						csvPrinter.printRecord(myPackage, newList.get(0), newList.get(1), newList.get(2), newList.get(3), 0, 0);
				
				}*/
				for (int i = 0; i < myList3.size(); i++) {
					
					//StringBuilder sb = new StringBuilder();
					ArrayList<String> newList = new ArrayList<>();
					newList.add(myList3.get(i++));
					newList.add(myList3.get(i++));
					newList.add(myList3.get(i++));
					newList.add(myList3.get(i++));
					newList.add(myList3.get(i++));
					newList.add(myList3.get(i));
					//String name = newList.get(3);
					int lineStart = cu.getLineNumber(Integer.parseInt(newList.get(4)+1));
					int lineEnd = lineStart-1 + Integer.parseInt(newList.get(5));
						
						csvPrinter.printRecord(myPackage, newList.get(0), newList.get(1), newList.get(2), newList.get(3), newList.get(4), newList.get(5));
						
				}
				for (int i = 0; i < myList4.size(); i++) {
					
					//StringBuilder sb = new StringBuilder();
					ArrayList<String> newList = new ArrayList<>();
					newList.add(myList4.get(i++));
					newList.add(myList4.get(i++));
					newList.add(myList4.get(i++));
					newList.add(myList4.get(i++));
					newList.add(myList4.get(i++));
					newList.add(myList4.get(i));
					//String name = newList.get(3);
					int lineStart = cu.getLineNumber(Integer.parseInt(newList.get(4)+1));
					int lineEnd = lineStart-1 + Integer.parseInt(newList.get(5));
						
						csvPrinter.printRecord(myPackage, newList.get(0), newList.get(1), newList.get(2), newList.get(3), newList.get(4), newList.get(5));
						
				}
				csvPrinter.close();
				
			}
		} catch (Exception e) {
			System.out.println("Some exception here");
			System.err.println(e);
		}

	}

}