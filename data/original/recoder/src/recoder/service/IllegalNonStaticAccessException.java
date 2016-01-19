/**
 * Created on 9 sep 2010
 */
package recoder.service;

import recoder.ModelException;

/**
 * @author Tobias Gutzmann
 *
 */
public class IllegalNonStaticAccessException extends ModelException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2251714958077363371L;

	/**
	 * 
	 */
	public IllegalNonStaticAccessException() {
	}

	/**
	 * @param s
	 */
	public IllegalNonStaticAccessException(String s) {
		super(s);
	}

}
