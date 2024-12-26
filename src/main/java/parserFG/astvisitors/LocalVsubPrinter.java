package parserFG.astvisitors;


import java.util.*;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import java.util.stream.Collectors;

import ecs160.visitor.utilities.ASTNodeTypePrinter;


/**
 * Class to print out information about every method
 * declaration we visit in the AST.
 * @author caseycas
 */
public class LocalVsubPrinter extends ASTVisitor{
	
	private ArrayList<String> nodeList = new ArrayList<>();
	public ArrayList<String> getNodeList(){
		return nodeList;
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node)
	{
		//System.out.println("localV");
		
		//System.out.println(node.getName().toString());
		
		
		ASTNode snParent = node.getParent();
		//System.out.print(snParent.toString());
		
		List<ASTNode> mods = new ArrayList<>();
		if (snParent instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement mySNParent = (VariableDeclarationStatement) snParent;
			mods = (List<ASTNode>) mySNParent.modifiers();
		}
		else if (snParent instanceof VariableDeclarationExpression) {
			VariableDeclarationExpression mySNParent = (VariableDeclarationExpression) snParent;
			mods = (List<ASTNode>) mySNParent.modifiers();
		}
		else if (snParent instanceof FieldDeclaration) {
			FieldDeclaration mySNParent = (FieldDeclaration) snParent;
			mods = (List<ASTNode>) mySNParent.modifiers();
		}
		//VariableDeclarationStatement mySNParent = (VariableDeclarationStatement) snParent;
		//String myName = mySNParent.toString();
		//System.out.print(myName);
		
		//List<ASTNode> mods = (List<ASTNode>) mySNParent.modifiers();
		
		//List<ASTNode> parentMods = (List<ASTNode>) node.modifiers();
		//for (int i = 0; i < mods.size(); i++) {
		//	System.out.print(mods.get(i) + " ");
		//}
		//System.out.println();
		
	//	System.out.println(mods.size());
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
		
		//System.out.println(preMods.size());
		if (annotationMods.size() != 0) {
			//System.out.println(annotationMods.size());
			
			String next = getFQN(node, preMods);
			//System.out.println(next);
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
						//	System.out.println(replaceMe);
							next1 = next1.replace(replaceMe, "");
						//	System.out.println(next1);
						}
						
					}
					startIndex++;	
				}
				//System.out.println(next);
				//System.out.println(next1);
				
				nodeList.add(next);
				nodeList.add(next1);
			
				
				//System.out.println("Modifier " + i + ") Type: " + ASTNodeTypePrinter.getSimpleType(m) + " Name: " + m);
				i++;
				//}}
			}
		}
		
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
	      if (snParent instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement mySNParent = (VariableDeclarationStatement) snParent;
				//mods = (List<ASTNode>) mySNParent.modifiers();
				fqn += mySNParent.getType() + " ";
			}
			else if (snParent instanceof VariableDeclarationExpression) {
				VariableDeclarationExpression mySNParent = (VariableDeclarationExpression) snParent;
				//mods = (List<ASTNode>) mySNParent.modifiers();
				fqn += mySNParent.getType() + " ";
			}
			else if (snParent instanceof FieldDeclaration) {
				FieldDeclaration mySNParent = (FieldDeclaration) snParent;
				//mods = (List<ASTNode>) mySNParent.modifiers();
				fqn += mySNParent.getType() + " ";
			}
			//VariableDeclarationStatement mySNParent = (VariableDeclarationStatement) snParent;
	      //  fqn += mySNParent.getType() + " ";
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


}