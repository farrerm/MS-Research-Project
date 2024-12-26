package callgraph.visitor;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ecs160.visitor.utilities.ASTNodeTypePrinter;

public class MethodDeclarationVisitor extends ASTVisitor {

    private List<String> methodInvocations;
    private List<String> fieldAccess;
    private Map<String, String> staticallyImportedMethods;

    public MethodDeclarationVisitor(Map<String, String> staticallyImportedMethods) {
        
    	this.methodInvocations = new ArrayList<String>();
        this.staticallyImportedMethods = staticallyImportedMethods;
       // for (Map.Entry<String, String> e : this.staticallyImportedMethods.entrySet()) {
       // 	System.out.println(e);
       // }
    }
    
    //we can use the following to find and resolve bindings for 
    //field accesses
    @Override
    public boolean visit(SimpleName node) {
    	String fqn = "";
    	if(node.resolveBinding() instanceof ITypeBinding) {
    		//System.out.println(node.resolveBinding().getName());
    		return super.visit(node);
    	}
    	if(!(node.resolveBinding() instanceof IVariableBinding)) {
    		return super.visit(node);
    	}
    	IVariableBinding binding = (IVariableBinding)node.resolveBinding();
    	binding = binding.getVariableDeclaration();
    	if(binding.isField()) {
    		ITypeBinding it = binding.getDeclaringClass();
    		if (it != null) {
    			fqn = it.getQualifiedName() + "." + node.resolveBinding().getName();	
    		}
    	}
    	if (fqn != "") {
    		//System.out.println("field access: " + fqn);
    		methodInvocations.add(fqn);
    	}
    	return super.visit(node);
    }
    
  
    /*@Override
    public boolean visit(FieldAccess node) {
        /*if (node.getExpression().resolveTypeBinding() != null) {
            String fqn = node.getExpression().resolveTypeBinding().getQualifiedName() + "." + 
            		node.getName().toString();
            methodInvocations.add(fqn);
        }
    	System.out.println(" u found it");
    	
    	String fqn = "";
    	
    	
    	if (node.getExpression().resolveTypeBinding() != null) {
    		
            fqn = node.getExpression().resolveTypeBinding().getQualifiedName() + "." +
            		node.getName().toString();
            //methodInvocations.add(fqn);
        }
    	
    	if (fqn != "") {
    		methodInvocations.add(fqn);
    	}
    	System.out.println("field access " + fqn);
    	//methodInvocations.add("a");
        return super.visit(node);
       // return true;
    }*/
    @Override
    public boolean visit(MethodInvocation node) {
    	//System.out.println("got here");
        String fqn = "";
        
        if (staticallyImportedMethods.containsKey(node.getName().toString())) {
        	
            List<String> params = new ArrayList<>();
            for (Object o : node.arguments()){
                Expression exp = (Expression)o;
                ITypeBinding binding = exp.resolveTypeBinding();
                if(binding !=null) {
                    params.add(binding.getQualifiedName());
                }
                else {
             	   params.add("null");
                }
            }
         //   System.out.println("static import " + node.getName().toString());
            fqn = staticallyImportedMethods.get(node.getName().toString()) + "." + node.getName();
           // System.out.println("invok fqn "  + fqn);
            String paramString = params.stream().collect(Collectors.joining(","));
            fqn = fqn + "(" + paramString + ")";
        } else if (node.getExpression() != null) {
        	//System.out.println("expression not null");
           ITypeBinding b = node.getExpression().resolveTypeBinding();
           if (b != null) {
               fqn = b.getQualifiedName() + "." + node.getName().toString();
               List<String> params = new ArrayList<>();
               for (Object o : node.arguments()){
                   Expression exp = (Expression)o;
                   ITypeBinding binding = exp.resolveTypeBinding();
                   if(binding !=null) {
                       params.add(binding.getQualifiedName());
                   }
                   else {
                	   params.add("null");
                   }
               }
               String paramString = params.stream().collect(Collectors.joining(","));
               fqn = fqn + "(" + paramString + ")";
           }
        } else {
        		//System.out.println("expression was null");
        		List<String> params = new ArrayList<>();
        		IMethodBinding mb = (IMethodBinding)node.resolveMethodBinding();
        		if (mb != null) {
        		//	System.out.println("resolved binding");
        			String declClass = mb.getDeclaringClass().getQualifiedName();
        		//	System.out.println("got declaring class");
        			fqn += declClass + "." + node.getName();
        			for (Object o : node.arguments()){
                  //  Expression exp = (Expression)o;
                   // ITypeBinding binding = exp.resolveTypeBinding();
                   // if(binding !=null) {
                   //     params.add(binding.getQualifiedName());
                  //  }
                  //  else {
                 	   params.add("null");
                  //  }
        			}
        		String paramString = params.stream().collect(Collectors.joining(","));
                fqn = fqn + "(" + paramString + ")";
        		}
        }
        if (!fqn.equals("")) {
            methodInvocations.add(fqn);
           // System.out.println("Method invoc " + fqn);
        }
        super.visit(node);
        return true;
    }
    @Override
    public boolean visit(ClassInstanceCreation node) {
    	
    	//System.out.println("got here");
        String fqn = "";
        
       if (node.getExpression() != null) {
        	//System.out.println("expression not null");
           ITypeBinding b = node.getExpression().resolveTypeBinding();
           if (b != null) {
               fqn = b.getQualifiedName() + "." + node.getType().toString();
               List<String> params = new ArrayList<>();
               for (Object o : node.arguments()){
                   Expression exp = (Expression)o;
                   ITypeBinding binding = exp.resolveTypeBinding();
                   if(binding !=null) {
                       params.add(binding.getQualifiedName());
                   }
                   else {
                	   params.add("null");
                   }
               }
               String paramString = params.stream().collect(Collectors.joining(","));
               fqn = fqn + "(" + paramString + ")";
           }
        } else {
        		//System.out.println("expression was null");
        		List<String> params = new ArrayList<>();
        		IMethodBinding mb = (IMethodBinding)node.resolveConstructorBinding();
        		if (mb != null) {
        		//	System.out.println("resolved binding");
        			String declClass = mb.getDeclaringClass().getQualifiedName();
        			//System.out.println("got declaring class");
        			fqn += declClass + "." + node.getType();
        			for (Object o : node.arguments()){
                  //  Expression exp = (Expression)o;
                   // ITypeBinding binding = exp.resolveTypeBinding();
                   // if(binding !=null) {
                   //     params.add(binding.getQualifiedName());
                  //  }
                  //  else {
                 	   params.add("null");
                  //  }
        			}
        		String paramString = params.stream().collect(Collectors.joining(","));
                fqn = fqn + "(" + paramString + ")";
        		}
        }
        if (!fqn.equals("")) {
            methodInvocations.add(fqn);
          //  System.out.println("Method invoc " + fqn);
        }
        super.visit(node);
        return true;
    	
    }
    
    

    public List<String> getMethodInvocations() {
        return this.methodInvocations;
    }
}
