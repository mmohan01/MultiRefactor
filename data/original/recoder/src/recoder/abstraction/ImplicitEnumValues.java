/*
 * Created on 15.08.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.abstraction;

import java.util.List;

/**
 * @author Tobias Gutzmann
 *
 */
public class ImplicitEnumValues extends ImplicitEnumMethod {

	/**
	 * @param ownerClass
	 */
	public ImplicitEnumValues(ClassType ownerClass) {
		super(ownerClass);
	}

	private static final String VALUES_NAME = "values".intern();
	/* (non-Javadoc)
	 * @see recoder.NamedModelElement#getName()
	 */
	public String getName() {
		return VALUES_NAME;
	}

	public List<? extends TypeParameter> getTypeParameters() {
		return null;
	}

	@Override
	public ImplicitEnumValues getGenericMember() {
		return this;
	}
	@Override
	public String getFullSignature() {
		return Method.SignatureBuilder.buildSignature(this);
	}

}
