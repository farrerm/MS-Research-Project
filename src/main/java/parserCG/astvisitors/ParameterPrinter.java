package parserCG.astvisitors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.*;

import ecs160.visitor.utilities.ASTNodeTypePrinter;


public class ParameterPrinter extends ASTVisitor{

    
    private ArrayList<String> nodeList = new ArrayList<>();
    public ArrayList<String> getNodeList(){
        return nodeList;
    }
    private CompilationUnit cu;
    public ParameterPrinter(CompilationUnit cu) {
    	this.cu = cu;
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
        
    	
        List<SingleVariableDeclaration> params = node.parameters();
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
        
            int i = 0;
            
            
            if (annotationMods.size() != 0) {
              
            	String next = getFQN(params.get(d), preMods);
            	String nextM = getFQN2(fqn, preModsM);
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
                nodeList.add(nextM);
                nodeList.add(next1);
                nodeList.add(Integer.toString(cu.getLineNumber(node.getStartPosition())));
                nodeList.add(Integer.toString(cu.getLineNumber(node.getStartPosition() + node.getLength())));
            
                
                //System.out.println("Modifier " + i + ") Type: " + ASTNodeTypePrinter.getSimpleType(m) + " Name: " + m);
                i++;
                //}
            }
        }//
    
        }//
        return false;
        /*
        //get method parameters
        //HashMap<String, String> myMap = new HashMap<>();
        
        
        List<SingleVariableDeclaration> params = node.parameters();
        
        //List<ASTNode> bigMods;
        
        for (int i = 0; i < params.size(); i++) {
            
            
            
            List<ASTNode> mods = (List<ASTNode>) params.get(i).modifiers();
            ArrayList<ASTNode> annotationMods = new ArrayList<>();
            ArrayList<ASTNode> preMods = new ArrayList<>();
            
            for (int j = 0; j < mods.size(); j++) {
                if (ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("MarkerAnnotation") ||
                        ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("SingleMemberAnnotation") ||
                        ASTNodeTypePrinter.getSimpleType(mods.get(i)).equals("NormalAnnotation")
                        ) {
                    annotationMods.add(mods.get(j));
                }
                else if (ASTNodeTypePrinter.getSimpleType(mods.get(j)) != "" &&
                        ASTNodeTypePrinter.getSimpleType(mods.get(j)).charAt(0) == '@') {
                    annotationMods.add(mods.get(j));
                }
                else {
                    preMods.add(mods.get(j));
                }
            }
            
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
                    nodeList.add(next);
                    nodeList.add(next1);
                
                    
                    //System.out.println("Modifier " + i + ") Type: " + ASTNodeTypePrinter.getSimpleType(m) + " Name: " + m);
                    i++;
                    //}
                }
        
            }
            
        }
        
        //= (List<ASTNode>) node.modifiers();
        //ArrayList<ASTNode> annotationMods = new ArrayList<>();
        
        
        for (int i = 0; i < bigMods.size(); i++) {
            if (     ASTNodeTypePrinter.getSimpleType(bigMods.get(i)).equals("MarkerAnnotation") ||
                    ASTNodeTypePrinter.getSimpleType(bigMods.get(i)).equals("SingleMemberAnnotation") ||
                    ASTNodeTypePrinter.getSimpleType(bigMods.get(i)).equals("NormalAnnotation")
                    ) {
                
                annotationMods.add(bigMods.get(i));
            }
            else if (bigMods.get(i).toString() != "" &&
                    bigMods.get(i).toString().charAt(0) == '@') {
                annotationMods.add(bigMods.get(i));
            }
            else {
                preMods.add(mods.get(i));
            }
        }
        
        int i = 0;
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
                
                startIndex++;    
            }
            
            nodeList.add(next);
            nodeList.add(next1);
            i++;
            //}
        }
        
        }
        return false;*/
        
    }
    private String getFQN(SingleVariableDeclaration node, ArrayList<ASTNode> preMods) {
        //node.getModifiers() + ":"
      String fqn = "";
      //"\"";
      for (int i = 0; i < preMods.size(); i++) {
          fqn += preMods.get(i) + " ";
      }
      //+ node.toString();
        fqn += node.getType() + " ";
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
        //return "";
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


