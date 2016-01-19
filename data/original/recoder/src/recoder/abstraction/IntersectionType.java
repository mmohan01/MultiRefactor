/*
 * Created on 04.01.2006
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.abstraction;

import java.util.ArrayList;
import java.util.List;

import recoder.ModelException;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * Represents an intersection type, which was introduced in java 5.
 * See JLS, 3rd edition, §4.9 for details.
 * @author Tobias Gutzmann
 *
 */
public class IntersectionType implements ClassType {
	private final List<ClassType> types;
	private ImplicitElementInfo pmi;
	private ClassType accessibility;
	
	private ArrayType arrayType;
	
	/**
	 * To be instantiated by ProgramModelInfo only...
	 */
	public IntersectionType(List<ClassType> types, ImplicitElementInfo pmi) {
		super();
		this.types = types;
		this.pmi = pmi;
	}

	public String getFullName() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < types.size(); i++) {
			if (i != 0)
				res.append(" & ");
			res.append(types.get(i).getFullName());
		}
		return res.toString();
	}
	
    public String getBinaryName() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < types.size(); i++) {
			if (i != 0)
				res.append(" & ");
			res.append(types.get(i).getBinaryName());
		}
		return res.toString();
	}
	
    public ArrayType getArrayType() {
    	return arrayType;
    }
    
    public ArrayType createArrayType() {
    	if (arrayType == null) {
    		arrayType = new ArrayType(this, pmi);
    	}
    	return arrayType;
    }

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getProgramModelInfo()
	 */
	public ImplicitElementInfo getProgramModelInfo() {
		return pmi;
	}

	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see recoder.NamedModelElement#getName()
	 */
	public String getName() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < types.size(); i++) {
			if (i != 0)
				res.append(" & ");
			res.append(types.get(i).getName());
		}
		return res.toString().intern();
	}

	public void validate() throws ModelException {
		// nothing to do
	}

	public boolean isInterface() {
		return false;
	}

	public boolean isOrdinaryInterface() {
		return false;
	}

	public boolean isAnnotationType() {
		return false;
	}

	public boolean isEnumType() {
		return false;
	}

	public boolean isOrdinaryClass() {
		return false;
	}

	public boolean isAbstract() {
		return true; // kinda is ?!
	}

	public List<ClassType> getSupertypes() {
		// TODO move this to ImplicitElementInfo...
		ArrayList<ClassType> res = new ArrayList<ClassType>(types.size());
		for (int i = 0; i < types.size(); i++) {
			Type t = types.get(i);
			res.add((ClassType)t);
		}
		return res;
	}

	public List<ClassType> getAllSupertypes() {
		return pmi.getAllSupertypes(this);
	}

	public List<? extends Field> getFields() {
		return null;
	}

	public List<Field> getAllFields() {
		return pmi.getAllFields(this);
	}

	public List<Method> getMethods() {
		return pmi.getMethods(this);
	}

	public List<Method> getAllMethods() {
		return pmi.getAllMethods(this);
	}

	public List<Constructor> getConstructors() {
		return null;
	}

	public List<ClassType> getAllTypes() {
		return null;
	}

	public List<? extends TypeParameter> getTypeParameters() {
		return null;
	}

	public boolean isFinal() {
		return false;
	}

	public boolean isStatic() {
		return false;
	}

	public boolean isPrivate() {
		return false;
	}

	public boolean isProtected() {
		return false;
	}

	public boolean isPublic() {
		return false;
	}

	public boolean isStrictFp() {
		return false;
	}

	public ClassType getContainingClassType() {
		return null;
	}

	public List<? extends AnnotationUse> getAnnotations() {
		return null;
	}

	public List<? extends ClassType> getTypes() {
		return null;
	}

	public Package getPackage() {
		return null;
	}

	public ClassTypeContainer getContainer() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		// I don't think this code is reachable, as IntersectionTypes cannot be declared
		// and are afaik never compared against each other, but oh well, let's implement
		// it just in case...
		if (o == this) return true;
		if (o instanceof IntersectionType) {
			IntersectionType it = (IntersectionType)o;
			return it.pmi == pmi && it.types.size() == types.size() && it.types.containsAll(types);
		} else
			return false;
	}
	
    public String getFullSignature() {
    	StringBuilder res = new StringBuilder(150);
		for (int i = 0; i < types.size(); i++) {
			if (i != 0)
				res.append(" & ");
			res.append(types.get(i).getFullSignature());
		}
		return res.toString();
    }
    
	public ErasedType getErasedType() {
		throw new UnsupportedOperationException();
	}

	public boolean isInner() {
		return false;
	}

	public ClassType getBaseClassType() {
		throw new UnsupportedOperationException();
	}

	public ClassType getAccessibility() {
		return accessibility;
	}
	public void setAccesibility(ClassType accessibility) {
		this.accessibility = accessibility;
	}

	@Override
	public Member getGenericMember() {
		throw new UnsupportedOperationException();
	}

}
