package parserFG.astvisitors;


import java.util.List;
import java.util.*;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.dom.*;

import ecs160.visitor.utilities.ASTNodeTypePrinter;


/**
 * Class to print out information about every method
 * declaration we visit in the AST.
 * @author caseycas
 */
public class MethodPrinter extends ASTVisitor{
	
	private ArrayList<String> nodeList = new ArrayList<>();
	public ArrayList<String> getNodeList(){
		return nodeList;
	}
	private CompilationUnit cu;
	
	public MethodPrinter(CompilationUnit cu){
		this.cu = cu;
	}
	//@Override
	//public boolean visit(ImportDeclaration node) {
		
	//	System.out.println("Got shere");
		//String name = node.getName().toString();
	//	nodeList.add(name);
	//	System.out.println(name);
	//	return false;
	//}
	
	@Override
	public boolean visit(MethodDeclaration node)
	{
		String className = "null";
		
		IMethodBinding binding = node.resolveBinding();
        if (binding != null) {
            ITypeBinding type = binding.getDeclaringClass();
            if (type != null) {
            	if (!type.getName().contentEquals("")) {
            		className = type.getName();
            	}
            	else {
            		className = "Null";
            	}
               // System.out.println("Decl: " + type.getName());
            }
        }
		//System.out.println("---------------------------------------------------------------------");
		System.out.println("Method Declaration: " + node.getName());
		//List<SingleVariableDeclaration> params = (List<SingleVariableDeclaration>)node.parameters();
		//int i = 0;
		//for(SingleVariableDeclaration svd : params)
		//{
		//	System.out.print(" Parameter " + i + ") Type: " + svd.getType() + " Name: " + svd.getName() + " ");
		//	i++;
		//}
		//System.out.println(")");
		List<ASTNode> mods = (List<ASTNode>) node.modifiers();
		ArrayList<ASTNode> annotationMods = new ArrayList<>();
		ArrayList<ASTNode> preMods = new ArrayList<>();
		
		for (int i = 0; i < mods.size(); i++) {
			if (	 ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("MarkerAnnotation") ||
					ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("SingleMemberAnnotation") ||
					ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("NormalAnnotation")
					) {
				
				annotationMods.add(mods.get(i));
			}
			else if (mods.get(i).toString() != "" &&
					mods.get(i).toString().charAt(0) == '@') {
				annotationMods.add(mods.get(i));
			}
			else { 
				preMods.add(mods.get(i));
			}
		}
		int i = 0;
		if (annotationMods.size() == 0) {
			nodeList.add(className);
			String next = getFQN(node, preMods);
			nodeList.add(next);
			nodeList.add(next);
			nodeList.add("");
			nodeList.add(Integer.toString(cu.getLineNumber(node.getStartPosition())));
			nodeList.add(Integer.toString(cu.getLineNumber(node.getStartPosition()+ node.getLength())));
		}
		if (annotationMods.size() != 0) {
			//String next = node.getName() + "()";
			String next = getFQN(node, preMods);
			
			//System.out.println("---------------------------------------------------------------------");
			
		//System.out.println(")");
		
		for (ASTNode m : annotationMods)
		{ 
			//if (ASTNodeTypePrinter.getSimpleType(m).equals("MarkerAnnotation")) {
			
			String next1 = m.toString();
			int startIndex = 0;
			int beginIndex = 0;
			int endIndex = startIndex;
			while (startIndex < next1.length()) { 
				if (next1.charAt(startIndex) == '(') {	
					next1 = next1.substring(0, startIndex);
					next1 += "()";	
				}		
				if (next1.charAt(startIndex) == '(' && next1.length() > startIndex +1 &&
			  			next1.charAt(startIndex + 1) != ')') {
						//System.out.println("parens");
						endIndex = startIndex;
						while (endIndex < next1.length() && next1.charAt(endIndex) != ')'){
							endIndex++;
						}
					if (endIndex < next1.length() && next1.charAt(endIndex) == ')') {
						//System.out.println("the end");
						String replaceMe = next1.substring(startIndex +1, endIndex);
						System.out.println(replaceMe);
						next1 = next1.replace(replaceMe, "");
						System.out.println(next1);
					}
				}
				startIndex++;	
			}
			
			nodeList.add(className);
			nodeList.add(next);
			nodeList.add(next);
			nodeList.add(next1);
			nodeList.add(Integer.toString(cu.getLineNumber(node.getStartPosition())));
			nodeList.add(Integer.toString(cu.getLineNumber(node.getStartPosition()+ node.getLength())));
			//System.out.println(next1);
		
			//System.out.println("Modifier " + i + ") Type: " + ASTNodeTypePrinter.getSimpleType(m) + " Name: " + m);
			i++;
			//}
		} 
		//System.out.println("Is constructor: " + node.isConstructor());
		//System.out.println("Return type: " + node.getReturnType2()); //getReturnType was deprecated in Java 3.
		//System.out.println("{");
		//node.accept(new IfPrinter());
		//node.accept(new MethodInvocationPrinter());
		//System.out.println("}");
		//System.out.println("---------------------------------------------------------------------");
		
		}
		return false;
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


}