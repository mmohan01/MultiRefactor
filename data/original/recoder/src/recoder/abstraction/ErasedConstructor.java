/**
 * 
 */
package recoder.abstraction;

import recoder.service.ImplicitElementInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public class ErasedConstructor extends ErasedMethod implements Constructor {

	/**
	 * @param genericMethod
	 * @param service
	 */
	public ErasedConstructor(Method genericMethod, ImplicitElementInfo service) {
		super(genericMethod, service);
	}

}
