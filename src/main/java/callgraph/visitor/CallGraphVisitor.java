package callgraph.visitor;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ecs160.visitor.utilities.ASTNodeTypePrinter;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
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

public class CallGraphVisitor extends ASTVisitor {

    private Map<String, String> staticallyImportedMethods;
    private Map<String, List<String>> declarationToInvocations;
    private CompilationUnit cu;
    private String[] basePaths;

    public CallGraphVisitor(CompilationUnit cu, String [] basePaths) {
        this.staticallyImportedMethods = new HashMap<String, String>();
        this.declarationToInvocations = new HashMap<>();
        this.cu = cu;
        this.basePaths = basePaths;
    }

    @Override
    public boolean visit(ImportDeclaration importDecl) {
        if (importDecl.isStatic()) {
        	//System.out.println("import Decl " + importDecl.getName());
            String statement = importDecl.toString()
                    .replace("import", "")
                    .replace("static", "")
                    .replace(";", "")
                    .trim();
            String[] split = statement.split("\\.");
            String methodName = split[split.length - 1];
           // System.out.println("method name = " + methodName);
            String[] classArray = new String[split.length - 1];
            System.arraycopy(split, 0, classArray, 0, split.length - 1);
            String className = Arrays.asList(classArray).stream().collect(Collectors.joining("."));
            staticallyImportedMethods.put(methodName, className);
        }
        return super.visit(importDecl);
    }
   @Override
   public boolean visit(FieldDeclaration node) {
	   
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
       
		int i = 0; 
		String fqn = getFQN(node, new ArrayList<ASTNode>());
		fqn = className + "."  + fqn;
				List<String> invocations = new ArrayList<>();
				invocations.add(0, Integer.toString(cu.getLineNumber(node.getStartPosition())));
		        invocations.add(1, Integer.toString(cu.getLineNumber(node.getStartPosition() + node.getLength())));
		        
	//	System.out.println("field decl " + fqn);	
		this.declarationToInvocations.put(fqn, invocations);
		return false;
	   
   }
   private String getFQN(FieldDeclaration node, ArrayList<ASTNode> preMods) {
       //node.getModifiers() + ":"
     String fqn = "";
     
       Object o = node.fragments().get(0);
       String s = "";
		if(o instanceof VariableDeclarationFragment){
			s = ((VariableDeclarationFragment) o).getName().toString();
		}
		fqn += s;
     	
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

    @Override
    public boolean visit(MethodDeclaration node) {
    	//System.out.println("visiting declaration");
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
            }
        }
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor(staticallyImportedMethods);
        node.accept(visitor);
        List<String> invocations = visitor.getMethodInvocations();
        invocations.add(0, Integer.toString(cu.getLineNumber(node.getStartPosition())));
        invocations.add(1, Integer.toString(cu.getLineNumber(node.getStartPosition() + node.getLength())));
        System.out.println(node.getName());
        System.out.println(cu.getLineNumber(node.getStartPosition()));
        System.out.println(cu.getLineNumber(node.getStartPosition() + node.getLength()));
        
        
        String paramList = "(" + (String) node.parameters().stream().map(p -> p.toString()).collect(Collectors.joining(",")) + ")";
        String fqn = className + "." + node.getName().getFullyQualifiedName() + paramList;
        String returnType = "";
        if (node.getReturnType2() != null) {
            if(node.getReturnType2().resolveBinding() != null) {
                returnType = node.getReturnType2().resolveBinding().getQualifiedName();
            }
        }
        String mod = "";
        if (binding != null) {
            if (Modifier.isPrivate(binding.getModifiers())) {
                mod = "private";
            } else if (Modifier.isPublic(binding.getModifiers())) {
                mod = "public";
            } else {
                mod = "protected";
            }
            if (Modifier.isStatic(binding.getModifiers())) {
                mod = mod + " static";
            }
        }
        //fqn = mod + " " + returnType + " " + fqn;
        this.declarationToInvocations.put(fqn, invocations);
    //   System.out.println("method decl: " + fqn);
       // CompilationUnit cu = (CompilationUnit) parser.createAST(null);
//        IProblem[] problems = cu.getProblems();
//        if (problems != null && problems.length > 0) {
//            System.out.println("Got {} problems compiling the source file: " +  problems.length);
//            for (IProblem problem : problems) {
//                System.out.println(problem);
//            }
//        }
      
        return super.visit(node);
    }


    public Map<String, List<String>> getDeclarationToInvocations() {
        return this.declarationToInvocations;
    }
}
