package parserFG.utilities;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 *  Helper file to print either the full or simplified type
 *  of the Eclipse AST node.
 *  @author caseycas
 */
public class ASTNodeTypePrinter {
	 
	/**
	 * Helper method to remove the package structure from the type name.
	 * @param node
	 * @return
	 */
	public static String getSimpleType(ASTNode node)
	{
		String fullType = ASTNode.nodeClassForType(node.getNodeType()).getName();
		String[] pieces = fullType.split("\\.");
		String retVal = "";
		for (int i = 0; i < pieces.length; i++) {
			if (pieces[i].contentEquals("MarkerAnnotation")) {
				retVal = "MarkerAnnotation";
				break;
			}
			else if (pieces[i].contentEquals("SingleMemberAnnotation")) {
				retVal = "SingleMemberAnnotation";
				break;
			}
			else if (pieces[i].contentEquals("NormalAnnotation")) {
				retVal = "NormalAnnotation";
				break;
			}
			else {
				retVal = "";
			}
		}
		return retVal;
		//return pieces[pieces.length -1];
	}
	
	/**
	 * Returns a string with the fully qualified type.
	 * @param node
	 * @return
	 */
	public static String getType(ASTNode node)
	{
		return ASTNode.nodeClassForType(node.getNodeType()).getName();
	}

}