// This file is part of the RECODER library and protected by the LGPL.

package recoder.abstraction;

import java.util.List;

import recoder.service.ProgramModelInfo;

/**
 * A program model element representing primitive types.
 * 
 * @author AL
 * @author RN
 * @author Tobias Gutzmann
 */
public class PrimitiveType implements Type {
	private ArrayType arrayType;
	
    private String name;

    private ProgramModelInfo pmi;

    public PrimitiveType(String name) {
        this.name = name.intern();
    }
    
    public ArrayType getArrayType() {
    	return arrayType;
    }
    
    public ArrayType createArrayType() {
    	if (arrayType == null)
    		arrayType = new ArrayType(this, pmi.getServiceConfiguration().getImplicitElementInfo());
    	return arrayType;
    }

    /**
     * Returns the name of this type.
     * 
     * @return the name of this type.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of type.
     * 
     * @return the full name of this program model element.
     */
    public String getFullName() {
        return name;
    }
    
	public String getBinaryName() {
		return name;
	}

    /**
     * Returns the instance that can retrieve information about this program
     * model element.
     * 
     * @return the program model info of this element.
     */
    public ProgramModelInfo getProgramModelInfo() {
        return pmi;
    }

    /**
     * Sets the instance that can retrieve information about this program model
     * element.
     * 
     * @param service
     *            the program model info for this element.
     */
    public void setProgramModelInfo(ProgramModelInfo service) {
    	if (pmi != null)
    		throw new RuntimeException("Service already set!");
        this.pmi = service;
    }

    public void validate() {
    	// not checked yet
    }
    
    public String getFullSignature() {
    	return getFullName();
    }
    
    public PrimitiveType deepClone() {
    	throw new UnsupportedOperationException("Cannot deep-clone primitive types");
    }

	public List<PrimitiveType> getAllSupertypes() {
		return pmi.getAllSupertypes(this);
	}

	public List<PrimitiveType> getSupertypes() {
		return pmi.getSupertypes(this);
	}
	
	@Override 
	public String toString() {
		return "<Primitive Type> " + name;
	}
}