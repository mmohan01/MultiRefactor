/*
 * Created on 25.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.bytecode;

import java.util.List;

import recoder.ModelException;
import recoder.abstraction.AnnotationUse;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.TypeParameter;
import recoder.convenience.Naming;
import recoder.service.ProgramModelInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public class TypeParameterInfo implements TypeParameter {
	private String name;
	
	private String boundNames[];
	
	private List<TypeArgumentInfo> boundArgs[]; 
	
	private ClassTypeContainer container;
	
	private ArrayType arrayType;
	/**
	 * 
	 */
	public TypeParameterInfo(String name, String[] boundNames, List<TypeArgumentInfo> boundArgs[], ClassTypeContainer container) {
		super();
		this.name = name.intern();
		this.boundNames = boundNames;
		this.boundArgs = boundArgs;
		this.container = container;
	}
	
    public ArrayType getArrayType() {
    	return arrayType;
    }
    
    public ArrayType createArrayType() {
    	if (arrayType == null)
    		arrayType = new ArrayType(this, container.getProgramModelInfo().getServiceConfiguration().getImplicitElementInfo());
    	return arrayType;
    }

    void setContainer(MethodInfo mi) {
    	container = mi;
    }
    
	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getFullName()
	 */
	public String getFullName() {
		return Naming.dot(container.getFullName(), name);
	}
	
	public String getBinaryName() {
		return getContainingClassType().getBinaryName() + "." + getName();
	}


	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getProgramModelInfo()
	 */
	public ProgramModelInfo getProgramModelInfo() {
		return container.getProgramModelInfo();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#setProgramModelInfo(recoder.service.ProgramModelInfo)
	 */
	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new UnsupportedOperationException(pmi.getClass().getName() + " should not be set for TypeParamterInfo");
	}

	/* (non-Javadoc)
	 * @see recoder.NamedModelElement#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see recoder.ModelElement#validate()
	 */
	public void validate() throws ModelException {
		// nothing to do
	}

	public String getParameterName() {
		return name;
	}

	public int getBoundCount() {
		return boundNames.length; // never null as java.lang.Object is mandatory
	}

	public String getBoundName(int boundidx) {
		return boundNames[boundidx];
	}

	public List<TypeArgumentInfo> getBoundTypeArguments(int boundidx) {
		return boundArgs[boundidx];
	}
	
	@Override
	public int hashCode() {
		return container.hashCode() ^ name.hashCode();
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
		return false;
	}

	public List<ClassType> getSupertypes() {
		return container.getProgramModelInfo().getSupertypes(this);
	}

	public List<ClassType> getAllSupertypes() {
		return container.getProgramModelInfo().getAllSupertypes(this);
	}

	public List<FieldInfo> getFields() {
		return null;
	}

	public List<Field> getAllFields() {
		return null;
	}

	public List<Method> getMethods() {
		return null;
	}

	public List<Method> getAllMethods() {
		return container.getProgramModelInfo().getAllMethods(this);
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
		// we think this should be ok...
		return true;
	}

	public boolean isStrictFp() {
		return false;
	}

	public ClassType getContainingClassType() {
		return container instanceof ClassType ? (ClassType)container : null;
	}

	public List<? extends AnnotationUse> getAnnotations() {
		// no annotations possible
		return null;
	}

	public List<ClassType> getTypes() {
		return null;
	}

	public Package getPackage() {
		return container.getPackage();
	}

	public ClassTypeContainer getContainer() {
		return container;
	}

	public String getFullSignature() {
		return TypeParameter.DescrImp.getFullSignature(this);
	}
	
	private ErasedType erasedType = null;
	
	public ErasedType getErasedType() {
		// TODO return getFirstBound().erasedType!!!!!
		if (erasedType == null)
			erasedType = new ErasedType(this, container.getProgramModelInfo().getServiceConfiguration().getImplicitElementInfo());
		return erasedType;
	}

	public boolean isInner() {
		return false;
	}

	@Override
	public String toString() {
		return getFullName();
	}
	
	public ClassType getBaseClassType() {
		return container.getProgramModelInfo().getServiceConfiguration().getNameInfo().getClassType(boundNames[0]).getBaseClassType();
	}

	@Override
	public TypeParameterInfo getGenericMember() {
		return this;
	}


}
