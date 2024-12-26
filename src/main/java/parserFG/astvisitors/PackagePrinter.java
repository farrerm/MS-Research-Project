package parserFG.astvisitors;


import java.util.*;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import java.util.stream.Collectors;

import ecs160.visitor.utilities.ASTNodeTypePrinter;

public class PackagePrinter extends ASTVisitor{
	
	private ArrayList<String> nodeList = new ArrayList<>();
	public ArrayList<String> getNodeList(){
		return nodeList;
	}
	
	@Override
	public boolean visit(PackageDeclaration node) {
		
		String myName = node.getName().getFullyQualifiedName();
		
		//if (myName != null) {
			nodeList.add(myName);
		//}
		
		return false;
		
	}
	

}
