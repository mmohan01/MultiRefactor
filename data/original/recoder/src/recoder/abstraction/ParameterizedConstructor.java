/**
 * 
 */
package recoder.abstraction;

import recoder.service.ImplicitElementInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public class ParameterizedConstructor extends ParameterizedMethod implements
		Constructor {

	/**
	 * @param genericMethod
	 * @param parentClassType
	 * @param service
	 */
	public ParameterizedConstructor(Constructor genericConstr,
			ParameterizedType parentClassType, ImplicitElementInfo service) {
		super(genericConstr, parentClassType, service);
	}

}
