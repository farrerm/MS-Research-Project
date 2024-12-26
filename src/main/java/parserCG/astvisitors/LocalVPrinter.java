package parserCG.astvisitors;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.*;

import java.util.stream.Collectors;

import ecs160.visitor.utilities.ASTNodeTypePrinter;
import ecs160.visitor.utilities.UtilReader;
import ecs160.visitor.astvisitors.LocalVsubPrinter;


/**
 * Class to print out information about every method
 * declaration we visit in the AST.
 * @author caseycas
 */
public class LocalVPrinter extends ASTVisitor{
	
	private ArrayList<String> nodeList = new ArrayList<>();
	public ArrayList<String> getNodeList(){
		return nodeList;
	}
	private CompilationUnit cu1;
	public LocalVPrinter(CompilationUnit cu) {
		this.cu1 = cu;
	}
	
	@Override
	public boolean visit(MethodDeclaration node)
	{
		String className = "";
		
		IMethodBinding binding = node.resolveBinding();
        if (binding != null) {
            ITypeBinding type = binding.getDeclaringClass();
            if (type != null) {
            	if (!type.getName().contentEquals("")) {
            		className = type.getQualifiedName();
            	}
            	else {
            		className = "null";
            	}
            	
               // System.out.println("Decl: " + type.getName());
            }
        }
        
        String paramList = "(" + (String) node.parameters().stream().map(p -> p.toString()).collect(Collectors.joining(",")) + ")";
        String fqn = /*className + "." + */node.getName().getFullyQualifiedName() + paramList;
        String returnType = "";
        
		
		List<ASTNode> modsM = (List<ASTNode>) node.modifiers();
        ArrayList<ASTNode> annotationModsM = new ArrayList<>();
        ArrayList<ASTNode> preModsM = new ArrayList<>();
        
        for (int i = 0; i < modsM.size(); i++) {
        	if (ASTNodeTypePrinter.getSimpleType(modsM.get(i)).equals("MarkerAnnotation") ||
                ASTNodeTypePrinter.getSimpleType(modsM.get(i)).equals("SingleMemberAnnotation") ||
                ASTNodeTypePrinter.getSimpleType(modsM.get(i)).equals("NormalAnnotation")
                ) {
        			annotationModsM.add(modsM.get(i));
        	}
        	else if (ASTNodeTypePrinter.getSimpleType(modsM.get(i)) != "" &&
                ASTNodeTypePrinter.getSimpleType(modsM.get(i)).charAt(0) == '@') {
        			annotationModsM.add(modsM.get(i));
        	}
        	else {
        		preModsM.add(modsM.get(i));
        	}
        }
        String methodFQN = getFQN2(fqn, preModsM);
		//System.out.println(node.getName().toString());
		//LocalVsubPrinter
		//if (node.isConstructor()) {
			
		//	System.out.println("constructor");
		//	IMethodBinding myBinding = node.resolveBinding();
		//	System.out.println(myBinding.toString());
			
		//}
		//String myPath = fileToSearch.toString();
		//System.out.println(myPath);
		try {
			//System.out.println("got here");
		//File file = new File(myPath);
			String text1 = node.getName().toString();
			String text = node.getBody().toString();
			
			//char [] myParam = new char[text.length() - 2];
			//for (int j = 0; j < myParam.length; j++) {
			//	myParam[j] = text.charAt(j+1);
			//}
		//	System.out.println(text1);
		//	System.out.println(text);
		
			ASTParser parser = ASTParser.newParser(AST.JLS12); // Create a parser for a version of the Java language (12
			// here)
			Map<String, String> options = JavaCore.getOptions(); // get the options for a type of Eclipse plugin that is
				// the basis of Java plugins
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_12); // Specify that we are on Java 12 and add it to
					// the options...
			parser.setCompilerOptions(options); // forward all these options to our parser
			parser.setKind(ASTParser.K_STATEMENTS); // What kind of constructions will be parsed by this parser.
		// K_COMPILATION_UNIT means we are parsing whole files.

			// K_COMPILATION_UNIT means we are parsing whole files.
			parser.setResolveBindings(true); // Enable looking for bindings/connections from this file to other parts of
			// the program.
			parser.setBindingsRecovery(true); // Also attempt to recover incomplete bindings (only can be set to true if
 			// above line is set to true).
			String[] classpath = { System.getProperty("java.home") + "/lib/rt.jar" }; // Link to your Java installation.
			parser.setEnvironment(classpath, new String[] { "" }, new String[] { "UTF-8" }, true);
			
			parser.setSource(text.toCharArray()); // Load in the text of the file to parse.
			//parser.setUnitName(file.getAbsolutePath()); // Load in the absolute path of the file to parse
			//System.out.println(myPath);
			Block cu = (Block) parser.createAST(null); // Create the tree and link to the root node.
			LocalVsubPrinter mySubPrinter = new LocalVsubPrinter();
			cu.accept(mySubPrinter);
			
			for (int i = 0; i < mySubPrinter.getNodeList().size(); i=i+2) {
				
				nodeList.add(className);
				nodeList.add(mySubPrinter.getNodeList().get(i));
				nodeList.add(methodFQN);
				int nextIndex = i + 1;
				nodeList.add(mySubPrinter.getNodeList().get(nextIndex));
				nodeList.add(Integer.toString(cu1.getLineNumber(node.getStartPosition())));
				nodeList.add(Integer.toString(cu1.getLineNumber(node.getStartPosition() + node.getLength())));
				
			}
			//ArrayList<String> myList = mySubPrinter.getNodeList();
			//nodeList = myList;
			//System.out.println(nodeList.size());
			
		} catch (NullPointerException e) {
			//System.out.println(e);
		}
	
		//System.out.println(node.getName().toString());
		return false;
	}
	
	private String getFQN(VariableDeclarationFragment node, ArrayList<ASTNode> preMods) {
        //node.getModifiers() + ":"
		
		String fqn = "";
	      //"\"";
	      for (int i = 0; i < preMods.size(); i++) {
	          fqn += preMods.get(i) + " ";
	      }
	      //+ node.toString();
	      ASTNode snParent = node.getParent();
			FieldDeclaration mySNParent = (FieldDeclaration) snParent;
	        fqn += mySNParent.getType() + " ";
	        //String fqn = node.getName().toString();
	        //fqn    += node.getName().toString();
	      
	      //  Object o = node.fragments().get(0);
	      //  String s = "";
	    //    if(o instanceof VariableDeclarationFragment){
	    //        s = ((VariableDeclarationFragment) o).getName().toString();
	        //    if(s.toUpperCase().equals(s))
	        //    System.out.println("-------------field: " + s);
	    //    }
	        fqn += node.getName().toString();
	          //fqn += node.
	      //fqn += " " +
	          //fqn += " " + node;
	          //fqn += "\"";
	        return fqn;
    }
	private String getFQN(MethodDeclaration node, ArrayList<ASTNode> preMods) {
        //node.getModifiers() + ":"
      String fqn ="";// = "\"";
      
      for (int i = 0; i < preMods.size(); i++) {
    	  fqn += preMods.get(i) + " ";
      }
      if (node.getReturnType2() != null) {
          
      	fqn += node.getReturnType2().toString() + " ";
     
      	//fqn += "\"";
      }
      else {
      	fqn += "null" + " ";
      	//fqn += "\"";
      }
    		  
    		  fqn += node.getName().toString();
        //String fqn = node.getName().toString();
        //fqn	+= node.getName().toString();
        
        List<ASTNode> params = (List<ASTNode>) node.parameters();
        
        fqn += "(";
        
        for (int i = 0; i < params.size() -1; i++) {
        	
        	fqn += params.get(i);
        	fqn += ", ";
        }
        if (params.size() > 0) {
        	fqn += params.get(params.size()-1);
        }
        fqn += ")";
        //+ node.parameters().stream().map(p -> {SingleVariableDeclaration single =  (SingleVariableDeclaration) p;
       //  return single.getName() + ":"+single.getType();}).collect(Collectors.joining(",")) + ")";
        
        return fqn;
        //return "";
    }
	private String getFQN2(String name, ArrayList<ASTNode> preMods) {
        //node.getModifiers() + ":"
      String fqn ="";// = "\"";
      
      for (int i = 0; i < preMods.size(); i++) {
    	  fqn += preMods.get(i) + " ";
      }
      String returnType = "";
      //we don't need return type for call graph
      //bc can't distinguish invocations based on return type
    		  
    		  fqn += name;
    				  //node.getName().toString();
        //String fqn = node.getName().toString();
        //fqn	+= node.getName().toString();
        
      //  List<ASTNode> params = (List<ASTNode>) node.parameters();
        
        //
       
        //+ node.parameters().stream().map(p -> {SingleVariableDeclaration single =  (SingleVariableDeclaration) p;
       //  return single.getName() + ":"+single.getType();}).collect(Collectors.joining(",")) + ")";
        
        return fqn;
        //return "";
    }






}