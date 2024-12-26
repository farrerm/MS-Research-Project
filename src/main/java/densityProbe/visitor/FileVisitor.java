package densityProbe.visitor;

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

public class FileVisitor extends ASTVisitor {

    private Map<String, String> staticallyImportedMethods;
    private Map<String, List<String>> declarationToInvocations;
    private List<Integer> probeNums;
    private CompilationUnit cu;
    private String[] basePaths;
    String filePath;
    
    public FileVisitor(CompilationUnit cu, String filePath) {
    	
        this.staticallyImportedMethods = new HashMap<String, String>();
        this.declarationToInvocations = new HashMap<>();
        this.cu = cu;
       // this.basePaths = basePaths;
        this.filePath = filePath;
        probeNums = new ArrayList<>();
        probeNums.add(0);
        probeNums.add(0);
        probeNums.add(0);
        probeNums.add(0);
        probeNums.add(0);
        probeNums.add(0);
        
    }

    @Override
    public boolean visit(ImportDeclaration importDecl) {
        if (importDecl.isStatic()) {
            String statement = importDecl.toString()
                    .replace("import", "")
                    .replace("static", "")
                    .replace(";", "")
                    .trim();
            String[] split = statement.split("\\.");
            String methodName = split[split.length - 1];
            String[] classArray = new String[split.length - 1];
            System.arraycopy(split, 0, classArray, 0, split.length - 1);
            String className = Arrays.asList(classArray).stream().collect(Collectors.joining("."));
            staticallyImportedMethods.put(methodName, className);
        }
        return super.visit(importDecl);
    }
   @Override
   public boolean visit(FieldDeclaration node) {
	   int fieldNums = probeNums.get(4);
	   fieldNums += 1;
	   probeNums.set(4, fieldNums);
	   
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
       
		//int i = 0; 
		String fqn = getFQN(node, new ArrayList<ASTNode>());
		fqn = className + "."  + fqn;
				List<String> invocations = new ArrayList<>();
				invocations.add(0, Integer.toString(cu.getLineNumber(node.getStartPosition())));
		        invocations.add(1, Integer.toString(cu.getLineNumber(node.getStartPosition() + node.getLength())));
		        
				
		this.declarationToInvocations.put(fqn, invocations);
		
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
		for (int i = 0; i < annotationMods.size(); i++) {
			
			String nuAnnot = annotationMods.get(i).toString();
			String output = removeArgs(nuAnnot);
			if (output.contains("Null") || output.contains("null")) {
				int checkerFieldNums = probeNums.get(5);
				   checkerFieldNums += 1;
				   probeNums.set(5, checkerFieldNums);
				   break;
			}
			
		}
		
		return false;
	   
   }
   private String removeArgs(String input) {
	   
	   int startIndex = 0;
		int endIndex = startIndex;
   
		while (startIndex < input.length()) {
       
			if (input.charAt(startIndex) == '(') {
				input = input.substring(0, startIndex);
				input += "()";
			}
			startIndex++;    
		}
		return input;   
	   
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
    	int methodNums = probeNums.get(0);
    	methodNums += 1;
    	probeNums.set(0, methodNums);
    	
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
        
        //handle parameters
        List<SingleVariableDeclaration> params = node.parameters();
        for (int d = 0; d < params.size(); d++) {
            
        	int paramNums = probeNums.get(2);
        	paramNums += 1;
        	probeNums.set(2, paramNums);
        	
            List<ASTNode> mods = (List<ASTNode>) params.get(d).modifiers();
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
            for (int i = 0; i < annotationMods.size(); i++) {
    			
    			String nuAnnot = annotationMods.get(i).toString();
    			String output = removeArgs(nuAnnot);
    			if (output.contains("Null") || output.contains("null")) {
    				int checkerParamNums = probeNums.get(3);
    				   checkerParamNums += 1;
    				   System.out.println(filePath);
    				   probeNums.set(3, checkerParamNums);
    				   break;
    			}
    		}
        }
       
            
        List<ASTNode> modsM = (List<ASTNode>) node.modifiers();
		ArrayList<ASTNode> annotationModsM = new ArrayList<>();
		ArrayList<ASTNode> preModsM = new ArrayList<>();
		
		
		for (int i = 0; i < modsM.size(); i++) {
			if (	 ASTNodeTypePrinter.getSimpleType(modsM.get(i)).equals("MarkerAnnotation") ||
					ASTNodeTypePrinter.getSimpleType(modsM.get(i)).equals("SingleMemberAnnotation") ||
					ASTNodeTypePrinter.getSimpleType(modsM.get(i)).equals("NormalAnnotation")
					) {
				
				annotationModsM.add(modsM.get(i));
			}
			else if (modsM.get(i).toString() != "" &&
					modsM.get(i).toString().charAt(0) == '@') {
				annotationModsM.add(modsM.get(i));
			}
			else { 
				preModsM.add(modsM.get(i));
			}
		}
		
		for (int i = 0; i < annotationModsM.size(); i++) {
			
			String nuAnnot = annotationModsM.get(i).toString();
			String output = removeArgs(nuAnnot);
			if (output.contains("Null") || output.contains("null")) {
				int checkerMethodNums = probeNums.get(1);
				   checkerMethodNums += 1;
				  // System.out.println(filePath);
				   probeNums.set(1, checkerMethodNums);
				   break;
			}
		}
        return super.visit(node);
    }


    public Map<String, List<String>> getDeclarationToInvocations() {
        return this.declarationToInvocations;
    }
    public List<Integer> getProbeNums(){
    	return this.probeNums;
    }
}
