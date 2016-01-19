package edu.atilim.acma.design;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.atilim.acma.util.CollectionHelper;
import edu.atilim.acma.util.Delegate.Func1P;

public class Method extends Node {
	public class Parameter {
		private Type type;
		private int dimension;
		
		Parameter(Type type, int dimension) {
			this.type = type;
			this.dimension = dimension;
		}
		
		public int getDimension() {
			return dimension;
		}

		public Type getType() {
			return type;
		}
	}
	
	private class ParameterMapper implements Func1P<Parameter, Reference> {
		@Override
		public Parameter run(Reference in) {
			Type t = in.getTarget(Type.class);
			if (t != null)
				return new Parameter(in.getTarget(Type.class), in.getDimension());
			return null;
		}
	}
	
	private static final long serialVersionUID = 1L;
	private Reference ownerType;
	private Reference returnType;
	private List<Reference> paramTypes;
	private List<Reference> calledMethods;
	
	private List<Reference> accessedFields;
	
	private List<Reference> instantiatedTypes;
	
	public Method(String name, Design design) {
		super(name, design);
		
		this.paramTypes = new ArrayList<Reference>();
		this.calledMethods = new ArrayList<Reference>();
		this.accessedFields = new ArrayList<Reference>();
		this.instantiatedTypes = new ArrayList<Reference>();
	}
	
	public void addAccessedField(Field f) {
		if (f == null) return;
		for (Reference ref : accessedFields)
			if (f == ref.getTarget()) return;
		accessedFields.add(getReference(this, f, Tags.REF_DEPEND));
	}
	
	public void addCalledMethod(Method m) {
		if (m == null) return;
		for (Reference ref : calledMethods)
			if (m == ref.getTarget()) return;
		calledMethods.add(getReference(this, m, Tags.REF_DEPEND));
	}
	
	public void addInstantiatedType(Type t) {
		if (t == null) return;
		for (Reference ref : instantiatedTypes)
			if (t == ref.getTarget()) return;
		instantiatedTypes.add(getReference(this, t, Tags.REF_INSTANTIATE));
	}
	
	public void addParameter(String t, int dimension) {
		Reference ref = getReference(this, t, Tags.REF_PARAMETER);
		ref.setDimension(dimension);
		paramTypes.add(ref);
	}
	
	public void addParameter(Type t) {
		addParameter(t, 0);
	}
	
	public void addParameter(Type t, int dimension) {
		Reference ref = getReference(this, t, Tags.REF_PARAMETER);
		ref.setDimension(dimension);
		paramTypes.add(ref);
	}
	
	public boolean canBeMovedTo (Type type){
		if(type == null || type.isAbstract() || type.isAnnotation() || type.isCompilerGenerated() || type.isInterface())
			return false;
		
		for(Method m : type.getMethods()){
			if(m.getSignature().equals(getSignature()))
				return false;
		}
		
		for (Field f : getAccessedFields()) {
			if (!type.canAccess(f)) 
				return false;
		}
		
		for(Method m : getCalledMethods()){
			if(!type.canAccess(m))
				return false;	
		}
		
		for(Parameter p : getParameters()){
			if(!type.canAccess(p.getType()))
				return false;
		}
		
		if(getReturnType() != null){
			if(!type.canAccess(getReturnType()))
				return false;
		}
		
		return true;
	}

	public boolean containsParameter(Type t) {
		return containsParameter(t, false);
	}
	
	public boolean containsParameter(Type t, boolean array) {
		for (Reference r : paramTypes)
			if (t == r.getTarget()) return true;
		return false;
	}

	/**
	 * Get All fields accessed from this method
	 * @return Unmodifiable list of fields
	 */
	public List<Field> getAccessedFields() {
		List<Field> methodList = CollectionHelper.map(accessedFields, new Reference.TargetSelector<Field>(Field.class));
		return Collections.unmodifiableList(methodList);
	}
	
	/**
	 * Get all methods that are called from this method
	 * @return Unmodifiable list of methods
	 */
	public List<Method> getCalledMethods() {
		List<Method> methodList = CollectionHelper.map(calledMethods, new Reference.TargetSelector<Method>(Method.class));
		return Collections.unmodifiableList(methodList);
	}
	
	/**
	 * Get all methods that this method potentially calls.
	 * @return Unmodifiable list of methods
	 */
	public List<Method> getCallerMethods() {
		return getReferers(Tags.REF_DEPEND, Method.class);
	}
	
	/**
	 * Get all types that are instantiated by this method.
	 * For example; if such a statement exists in the body of the method:
	 * b = new B();
	 * B is returned as an instantiated type.
	 * @return Unmodifiable list of types
	 */
	public List<Type> getInstantiatedTypes() {
		List<Type> methodList = CollectionHelper.map(instantiatedTypes, new Reference.TargetSelector<Type>(Type.class));
		return Collections.unmodifiableList(methodList);
	}
	
	public Type getOwnerType() {
		return ownerType == null ? null : ownerType.getTarget(Type.class);
	}
	
	@Override
	public Package getPackage() {
		Type owner = getOwnerType();
		if (owner == null) return Package.emptyPackage(getDesign());
		return owner.getPackage();
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> typeList = CollectionHelper.map(paramTypes, new ParameterMapper());
		return Collections.unmodifiableList(typeList);
	}
	
	List<Reference> getRawParameters() {
		return Collections.unmodifiableList(paramTypes);
	}
	
	public Type getReturnType() {
		return returnType == null ? null : returnType.getTarget(Type.class);
	}
	
	public String getSignature() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName())
		.append('(');
		for (int i = 0; i < paramTypes.size(); i++) {
			sb.append(paramTypes.get(i).toString());
			
			if (i != paramTypes.size() - 1)
				sb.append(',');
		}
		sb.append(')');
		
		return sb.toString();
	}
	
	public boolean isAccessingThisPointer() {
		return getFlag(Tags.ACCESSES_THIS);
	}
	
	public boolean isMoved() {
		return getFlag(Tags.ACMA_MOVED);
	}
	
	public boolean isClassConstructor() {
		return getName().startsWith("<clinit>");
	}
	
	public boolean isConstructor() {
		return getName().startsWith("<init>");
	}
	
	public boolean isGetter() {
		return getName().startsWith("get");
	}
	
	public boolean isSetter() {
		return getName().startsWith("set");
	}
	
	/**
	 * Determine if this method is an override of one in ancestors.
	 * The signatures must match to assume an override
	 * @return true if override
	 */
	public boolean isOverride() {
		if (isStatic()) return false;
		
		Type owner = getOwnerType();
		if (owner == null) return false;
		
		boolean lastOwnerRoot = owner.isRootType();
		owner = owner.getSuperType();
		
		while (owner != null) {
			if (owner.getMethod(getSignature()) != null) return true;
			lastOwnerRoot = owner.isRootType();
			
			owner = owner.getSuperType();
		}
		
		if (!lastOwnerRoot) return true;
		
		String[] objMethods = new String[] {
			"clone", "equals", "finalize", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait"	
		};
		
		return Arrays.asList(objMethods).contains(getName());
	}
	
	/**
	 * Removes the method from graph.
	 * All connections are removed upon this operation.
	 */
	@Override
	public boolean remove() {
		if (!super.remove()) return false;
		if (ownerType != null)
			ownerType.release();
		if (returnType != null)
			returnType.release();
		for (Reference ref : paramTypes) ref.release();
		for (Reference ref : calledMethods) ref.release();
		for (Reference ref : accessedFields) ref.release();
		for (Reference ref : instantiatedTypes) ref.release();
		return true;
	}
	
	public void removeAccessedField(Field f) {
		for (Reference r : accessedFields) {
			if (f == r.getTarget()) {
				r.release();
				accessedFields.remove(r);
				return;
			}
		}
	}
	
	public void removeCalledMethod(Method m) {
		for (Reference r : calledMethods) {
			if (m == r.getTarget()) {
				r.release();
				calledMethods.remove(r);
				return;
			}
		}
	}
	
	public void removeInstantiatedType(Type t) {
		for (Reference r : instantiatedTypes) {
			if (t == r.getTarget()) {
				r.release();
				instantiatedTypes.remove(r);
				return;
			}
		}
	}
	
	public void removeParameter(int i) {
		Reference ref = paramTypes.get(i);
		ref.release();
		paramTypes.remove(ref);
	}
	
	public void removeParameter(Parameter p) {
		for (Reference r : paramTypes) {
			if (p.getType() == r.getTarget() && p.getDimension() == r.getDimension()) {
				r.release();
				paramTypes.remove(r);
				return;
			}
		}
	}
	
	public void setAccessingThisPointer(boolean value) {
		setFlag(Tags.ACCESSES_THIS, value);
	}
	
	public void setMoved(boolean value) {
		setFlag(Tags.ACMA_MOVED, value);
	}
	
	public void setOwnerType(Type ownerType) {
		if (this.ownerType != null)
			this.ownerType.release();
		
		this.ownerType = getReference(this, ownerType, Tags.REF_PARENT);
	}
	
	public void setReturnType(Type returnType) {
		if (this.returnType != null)
			this.returnType.release();
		
		this.returnType = getReference(this, returnType, Tags.REF_RETURN);
	}
	
	@Override
	public String toString() {
		return getSignature();
	}
}
