/**
 * Created on Oct 5, 2012
 */
package recoder.service;

import recoder.ModelException;
import recoder.java.reference.TypeReference;

/**
 * @author Tobias Gutzmann
 *
 */
public class GenericsUseException extends ModelException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5221880025545574729L;
	private TypeReference typeRef;
	
	public GenericsUseException(String message, TypeReference typeRef) {
		super(message);
		this.typeRef = typeRef;
	}
	
	public TypeReference getTypeReference() {
		return typeRef;
	}
}
