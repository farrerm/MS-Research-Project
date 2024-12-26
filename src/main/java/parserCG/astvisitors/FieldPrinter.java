package parserCG.astvisitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
//import org.eclipse.jdt.core.dom.IFieldBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Expression;
//import org.eclipse.jdt.;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


import ecs160.visitor.utilities.ASTNodeTypePrinter;

/**
 * Class to print out information about every method
 * declaration we visit in the AST.
 * @author caseycas
 */
public class FieldPrinter extends ASTVisitor{
	
	private ArrayList<String> nodeList = new ArrayList<>();
	public ArrayList<String> getNodeList(){
		return nodeList;
	}
	@Override
	public boolean visit(FieldDeclaration node)
	{
		
		
		String className = "null";
		
		IVariableBinding binding = getVariableBinding(node);
        if (binding != null) {
            ITypeBinding type = binding.getDeclaringClass();
            if (type != null) {
            	if (!type.getName().contentEquals("")) {
            		className = type.getQualifiedName();
            	}
            	else {
            		className = "Null";
            	}
            	
               // System.out.println("Decl: " + type.getName());
            }
        }
	
        
		List<ASTNode> mods = (List<ASTNode>) node.modifiers();
		//System.out.println(mods.size());
		ArrayList<ASTNode> annotationMods = new ArrayList<>();
		ArrayList<ASTNode> preMods = new ArrayList<>();
		
		for (int i = 0; i < mods.size(); i++) {
			if (ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("MarkerAnnotation") ||
					ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("SingleMemberAnnotation") ||
					ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("NormalAnnotation")
					) {
				annotationMods.add(mods.get(i));
			}
			else if (ASTNodeTypePrinter.getSimpleType(mods.get(i)) != "" &&
					ASTNodeTypePrinter.getSimpleType(mods.get(i)).charAt(0) == '@') {
				annotationMods.add(mods.get(i));
			}
			else {
				preMods.add(mods.get(i));
			}
		}
		
		int i = 0; 
		if (annotationMods.size() != 0) {
			  
			String next = getFQN(node, preMods);
			//System.out.println("---------------------------------------------------------------------");
			//System.out.print("Field Declaration: " + node.getType() + " (");
			//System.out.println(")");
			for (ASTNode m : annotationMods)
			{ 
				//if (ASTNodeTypePrinter.getSimpleType(m).equals("MarkerAnnotation")) {
				String next1 = m.toString();
				
				int startIndex = 0;
				int endIndex = startIndex;
				
				while (startIndex < next1.length()) {
					
					if (next1.charAt(startIndex) == '(') {
						
						next1 = next1.substring(0, startIndex);
						next1 += "()";
						
					}
					
						
				
					startIndex++;	
				}
				nodeList.add(className);
				nodeList.add(next);
				nodeList.add(next);
				nodeList.add(next1);
				nodeList.add("0");
				nodeList.add("0");
			
				
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
	private String getFQN(FieldDeclaration node, ArrayList<ASTNode> preMods) {
        //node.getModifiers() + ":"
      String fqn = "";
      //"\"";
     // for (int i = 0; i < preMods.size(); i++) {
    //	  fqn += preMods.get(i) + " ";
     // }
      //+ node.toString();
        //fqn += node.getType() + " ";
        //String fqn = node.getName().toString();
        //fqn	+= node.getName().toString();
      
        Object o = node.fragments().get(0);
        String s = "";
		if(o instanceof VariableDeclarationFragment){
			s = ((VariableDeclarationFragment) o).getName().toString();
		//	if(s.toUpperCase().equals(s))
		//	System.out.println("-------------field: " + s);
		}
		fqn += s;
      	//fqn += node.
      //fqn += " " + 
      	//fqn += " " + node;
      	//fqn += "\"";
        return fqn;
        //return "";
    }
	private IVariableBinding getVariableBinding(FieldDeclaration declaration) {
	    for (Object fragment : declaration.fragments()) {
	        if (fragment instanceof VariableDeclarationFragment) {
	            VariableDeclarationFragment varDecl = (VariableDeclarationFragment) fragment;
	            IVariableBinding binding = varDecl.resolveBinding();
	            if (binding != null) {
	                return binding;
	            }
	        }
	    }
	    return null;
	}

}