package inheritance.visitor;

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

   // private Map<String, String> staticallyImportedMethods;
    //private Map<String, List<String>> declarationToInvocations;
   // private List<Integer> probeNums;
    private CompilationUnit cu;
   // private String[] basePaths;
    String filePath;
    ASTParser myParser;
    ArrayList<String[]> inheritanceList;
    
    public ArrayList<String[]> getInheritanceList(){
    	return this.inheritanceList;
    }
    
    public FileVisitor(CompilationUnit cu, String filePath) {
    	
       // this.staticallyImportedMethods = new HashMap<String, String>();
       // this.declarationToInvocations = new HashMap<>();
        this.cu = cu;
       // this.basePaths = basePaths;
      //  System.out.println("making visitor");
        this.filePath = filePath;
      //  this.myParser = myParser;
        this.inheritanceList = new ArrayList<>();
       
    }

  /*  @Override
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
    }*/
   @Override
   public boolean visit(TypeDeclaration node) {
	 //  System.out.println("visiting class");
	  // System.out.println(node.resolveBinding().getQualifiedName());
	   ITypeBinding fClass = node.resolveBinding();
	   ITypeBinding sClass = node.resolveBinding().getSuperclass();
	   if(sClass != null) {
		  /// System.out.println("node " + fClass.getQualifiedName());
		  // System.out.println("super node " + sClass.getQualifiedName());
	   }
	 //  System.out.println(sClass.getQualifiedName());
	   while (sClass != null && !((sClass.getQualifiedName()).equals("java.lang.Object"))) {
		   
		   String fClassName = fClass.getQualifiedName();
		   
		   String sClassName = sClass.getQualifiedName();
		  // if (fClassName.equals("java.lang.Object") || sClassName.equals("java.lang.Object")) {
		//	   System.out.println("Found object");
		  // }
		   String [] temp = new String[2];
		   temp[0] = fClassName;
		   temp[1] = sClassName;
		   this.inheritanceList.add(temp);
		   fClass = sClass;
		   sClass = sClass.getSuperclass();
	   }
			   
	  // Type sClass = node.getSuperclassType();
	  // String sClassString = "";
	  // if (sClass != null) {
	//	   sClassString = sClass.resolveBinding().getQualifiedName();
	//	   String[] temp = new String[2];
	//	   temp[0] = node.resolveBinding().getQualifiedName();
	//	   temp[1] = sClassString;
	//	   this.inheritanceList.add(temp);
		//   System.out.println("extends " + sClass);
	  // }
	   TypeDeclaration[] myTypes = node.getTypes();
	   for (TypeDeclaration td: myTypes) {
		   visit(td);
	   }
	 /*  IVariableBinding binding = getVariableBinding(node);
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
       }*/
       
		//int i = 0; 
		/*String fqn = getFQN(node, new ArrayList<ASTNode>());
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
			
		}*/
		
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
       // System.out.println(className + "." + node.getName());
        //handle parameters
        List<SingleVariableDeclaration> params = node.parameters();
        for (int d = 0; d < params.size(); d++) {
            
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
    			/*if (output.contains("Null") || output.contains("null")) {
    				int checkerParamNums = probeNums.get(3);
    				   checkerParamNums += 1;
    				   System.out.println(filePath);
    				   probeNums.set(3, checkerParamNums);
    				   break;
    			}*/
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
		/*	if (output.contains("Null") || output.contains("null")) {
				int checkerMethodNums = probeNums.get(1);
				   checkerMethodNums += 1;
				  // System.out.println(filePath);
				   probeNums.set(1, checkerMethodNums);
				   break;
			}*/
		}
        return super.visit(node);
    }


  /*  public Map<String, List<String>> getDeclarationToInvocations() {
        return this.declarationToInvocations;
    }*/
    public List<Integer> getProbeNums(){
    	return new ArrayList<Integer>();
    }
}
