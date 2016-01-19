/**
 * 
 */
package recoder.service;

import recoder.ModelException;
import recoder.java.JavaProgramElement;

/**
 * A ModelException indicating that a given JavaProgramElement
 * is syntactically incorrect. This can happen only after
 * transformations, because the parser ensures syntactical
 * correctness. Should only be thrown by implementations
 * of <code>recoder.ModelElement.validate()</code>. 
 * @author Tobias Gutzmann
 */
public class SyntaxException extends ModelException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1250184152706720604L;
	private final JavaProgramElement failing;
	
	/**
	 * @param pe The failing program element
	 * @param s An additional error description
	 */
	public SyntaxException(JavaProgramElement failing, String s) {
		super(s);
		this.failing = failing;
	}
	
	public JavaProgramElement getFailingElement() {
		return failing;
	}

}
