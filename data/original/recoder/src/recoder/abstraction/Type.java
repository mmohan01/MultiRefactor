// This file is part of the RECODER library and protected by the LGPL.

package recoder.abstraction;


/**
 * A program model element representing types.
 * 
 * @author AL
 * @author RN
 */
public interface Type extends ProgramModelElement {
	/**
	 * 
	 * @return
	 * @since 0.90
	 */
	public ArrayType getArrayType();
	
	/**
	 * 
	 * @return
	 * @since 0.90
	 */
	public ArrayType createArrayType();

	/**
	 * Returns a full type signature of this type,
	 * i.e., the full name plus possible type arguments.
	 * Works as <code>getFullName()</code> on primitive types.
	 * @since 0.90
	 */
	public String getFullSignature();
}

