/*
 * Created on 23.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.abstraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import recoder.ModelException;
import recoder.abstraction.TypeArgument.CapturedTypeArgument;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.service.DefaultProgramModelInfo.ResolvedTypeArgument;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * A parameterized type, meaning a generic type plus actual type arguments.
 * This is for internal representation and not an AST representation element.
 * 
 * <b>Note:</b> A parameterized type may become invalid after a model update!
 * 
 * @author Tobias Gutzmann
 *
 */
public class ParameterizedType implements ClassType {
	
	private final ClassType genericType;
	private final List<? extends TypeArgument> typeArgs;
	private final ParameterizedType enclosingType;
	private final int hashCode;
	
	private ArrayType arrayType;
	
	private final ImplicitElementInfo pmi;
	/**
	 * 
	 */
	public ParameterizedType(ClassType genericType, List<? extends TypeArgument> typeArgs,
							 ImplicitElementInfo service) {
		this (genericType, typeArgs, null, service);
	}
	
	public ParameterizedType(ClassType innerGenericType, List<? extends TypeArgument> typeArgs,
			ParameterizedType enclosingType, ImplicitElementInfo service) {
		if (innerGenericType instanceof ArrayType) {
			throw new IllegalArgumentException("ArrayTypes must not be parameterized - " + innerGenericType.getFullSignature());
		}
		if (innerGenericType == null) 
			throw new NullPointerException();
		if (typeArgs == null) 
			typeArgs = Collections.<TypeArgument>emptyList();
		this.genericType = innerGenericType;
		this.typeArgs = typeArgs;
		this.pmi = service;
		this.enclosingType = enclosingType;
		
		assert typeArgs.size() != 0 || enclosingType != null;
		
		int hash = genericType.hashCode();
		if (enclosingType != null)
			hash += enclosingType.hashCode;
		hash += typeArgs.size();
		for (int i = 0; i < typeArgs.size(); i++) {
			TypeArgument ta = typeArgs.get(i);
			hash += ta.semanticalHashCode();
		}
		hashCode = hash;
		
		// below, the case > can happen if type parameters are taken from an outer class or not type variables at all
//		assert typeArgs.size() >= genericType.getTypeParameters().size();
		// TODO above assert may cause NullPointerException on inner types without own type parameters 
	}
	
	// TODO 0.90 doc / rename ?
	public List<TypeParameter> getDefinedTypeParameters() {
		if (genericType.getTypeParameters() == null)
			return enclosingType.getDefinedTypeParameters(); // this MUST be defined!
		List<TypeParameter> outerTPs = enclosingType == null ? 
				Collections.<TypeParameter>emptyList() : enclosingType.getDefinedTypeParameters();  
		int sz = genericType.getTypeParameters().size() + 
					outerTPs.size();
		List<TypeParameter> res = new ArrayList<TypeParameter>(sz);
		res.addAll(genericType.getTypeParameters());
		res.addAll(outerTPs);
		return res;
	}
	
    public ArrayType getArrayType() {
    	return arrayType;
    }
    
    public ArrayType createArrayType() {
    	if (arrayType == null)
    		arrayType = new ArrayType(this, pmi.getServiceConfiguration().getImplicitElementInfo());
    	return arrayType;
    }
    
    // TODO 0.90 check references if they shouldn't use getDefinedTypeParameters
	public ClassType getGenericType() {
		return genericType;
	}
	
	public ParameterizedType getEnclosingType() {
		return enclosingType;
	}
	
	private List<TypeArgument> allTypeArgs = null;
	/**
	 * includes the type args of a possibly enclosing type
	 * @return
	 */
	public List<TypeArgument> getAllTypeArgs() {
		if (allTypeArgs != null)
			return allTypeArgs;
		List<TypeArgument> outerArgs = enclosingType == null ? 
				Collections.<TypeArgument>emptyList() : enclosingType.getAllTypeArgs();  
		int sz = typeArgs.size() + 
					outerArgs.size();
		List<TypeArgument> res = new ArrayList<TypeArgument>(sz);
		res.addAll(typeArgs);
		res.addAll(outerArgs);
		allTypeArgs = res;
		return res;
	}
	
	public List<? extends TypeArgument> getTypeArgs() {
		return typeArgs;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getFullName()
	 */
	public String getFullName() {
		return genericType.getFullName();
	}
	
	public String getBinaryName() {
		return genericType.getBinaryName();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getProgramModelInfo()
	 */
	public ImplicitElementInfo getProgramModelInfo() {
		return pmi;
	}

	/**
	 * @throws RuntimeException - not to be called but set by constructor!
	 */
	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new RuntimeException("ParameterizedType.setProgramModelInfo must not be called!");
	}

	/* (non-Javadoc)
	 * @see recoder.NamedModelElement#getName()
	 */
	public String getName() {
		return genericType.getName();
	}

	/* (non-Javadoc)
	 * @see recoder.ModelElement#validate()
	 */
	public void validate() throws ModelException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the empty list
	 */
	public List<TypeParameter> getTypeParameters() {
		return Collections.<TypeParameter>emptyList();
	}

	public boolean isInterface() {
		return genericType.isInterface();
	}

	public boolean isOrdinaryInterface() {
		return genericType.isOrdinaryInterface();
	}

	public boolean isAnnotationType() {
		return genericType.isAnnotationType();
	}

	public boolean isEnumType() {
		return genericType.isEnumType();
	}

	public boolean isOrdinaryClass() {
		return genericType.isOrdinaryClass();
	}

	public boolean isAbstract() {
		return genericType.isAbstract();
	}

	public List<ClassType> getSupertypes() {
		return pmi.getSupertypes(this);
	}

	public List<ClassType> getAllSupertypes() {
		return pmi.getAllSupertypes(this);
	}

	public List<? extends Field> getFields() {
		return pmi.getFields(this);
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

	public List<? extends Constructor> getConstructors() {
		return pmi.getConstructors(this);
	}

	public List<ClassType> getAllTypes() {
		return pmi.getAllTypes(this);
	}

	public boolean isFinal() {
		return genericType.isFinal();
	}

	public boolean isStatic() {
		return genericType.isStatic();
	}

	public boolean isPrivate() {
		return genericType.isPrivate();
	}

	public boolean isProtected() {
		return genericType.isProtected();
	}

	public boolean isPublic() {
		return genericType.isPublic();
	}

	public boolean isStrictFp() {
		return genericType.isStrictFp();
	}

	public ClassType getContainingClassType() {
		// TODO 0.90 !?
		return genericType.getContainingClassType();
	}

	public List<? extends AnnotationUse> getAnnotations() {
		return genericType.getAnnotations();
	}

	public List<? extends ClassType> getTypes() {
		return pmi.getTypes(this);
	}

	public Package getPackage() {
		return genericType.getPackage();
	}

	public ClassTypeContainer getContainer() {
		return genericType.getContainer();
	}

	/**
	 * Implements *semantical* equality.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ParameterizedType))
			return false;
		ParameterizedType pt = (ParameterizedType)o;
		if (pt.pmi != pmi)
			return false; // TODO multiple instances, ok, but should this happen anyway ??
		if (pt.getGenericType() != getGenericType())
			return false;
		if (pt.enclosingType != enclosingType)
			return false;
		// check type arguments. The two types MUST have the same number of type arguments!
		List<? extends TypeArgument> ta1 = getAllTypeArgs();
		List<? extends TypeArgument> ta2 = pt.getAllTypeArgs();
		if (ta1.size() != ta2.size()) {
			return false;
		}
		for (int i = 0; i < ta1.size(); i++) {
			if (ta1.get(i) == ta2.get(i))
				continue;
			if (!ta1.get(i).semanticalEquality(ta2.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
    public String getFullSignature() {
    	String res;
    	if (enclosingType != null)
    		res = enclosingType.getFullSignature() + "." + getName();
    	else 
    		res = getFullName();
    	if (typeArgs.size() == 0) return res;
    	else res += "<";
    	String del = "";
    	for (TypeArgument ta : getTypeArgs()) {
    		res = res + del + ta.getFullSignature();
    		del = ",";
    	}
    	res = res + ">";
    	return res;
    }
    
    @Override
    public String toString() {
    	return getFullSignature();
    }
    
	public ErasedType getErasedType() {
		return (ErasedType)getGenericType().getErasedType();
	}

	public boolean isInner() {
		return genericType.isInner();
	}
	
	public ClassType getBaseClassType() {
		return genericType;
	}

	@Override
	public ClassType getGenericMember() {
		return genericType;
	}

}
