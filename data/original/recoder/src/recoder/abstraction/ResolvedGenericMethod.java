/**
 * 
 */
package recoder.abstraction;

import java.util.List;

import recoder.ModelException;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * @author Tobias
 *
 */
public class ResolvedGenericMethod implements Method {
	private final Method genericMethod;
	private final boolean inferred;
	private final List<ClassType> types;
	private final ImplicitElementInfo service;
	
	public ResolvedGenericMethod(
			Method genericMethod, boolean inferred, List<ClassType> types,
			ImplicitElementInfo service) {
		this.genericMethod = genericMethod;
		this.inferred = inferred;
		this.types = types; // TODO 0.90 copy !?
		this.service = service;
		
		assert types.size() == genericMethod.getTypeParameters().size();
		assert !(genericMethod instanceof ResolvedGenericMethod);
	}
	
	
	public Method getGenericMethod() {
		return genericMethod;
	}
	
	public List<ClassType> getReplacementType() {
		return types;
	}
	
	/**
	 * 
	 * @return true if this generic method has been resolved through type inference, 
	 * false otherwise.
	 */
	public boolean isInferred() {
		return inferred;
	}
	
	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getExceptions()
	 */
	public List<ClassType> getExceptions() {
		return service.getExceptions(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getReturnType()
	 */
	public Type getReturnType() {
		return service.getReturnType(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getSignature()
	 */
	public List<Type> getSignature() {
		return service.getSignature(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getTypeParameters()
	 */
	public List<? extends TypeParameter> getTypeParameters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isAbstract()
	 */
	public boolean isAbstract() {
		return genericMethod.isAbstract();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isNative()
	 */
	public boolean isNative() {
		return genericMethod.isNative();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isSynchronized()
	 */
	public boolean isSynchronized() {
		return genericMethod.isSynchronized();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isVarArgMethod()
	 */
	public boolean isVarArgMethod() {
		return genericMethod.isVarArgMethod();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getAnnotations()
	 */
	public List<? extends AnnotationUse> getAnnotations() {
		return genericMethod.getAnnotations(); // ??
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getContainingClassType()
	 */
	public ClassType getContainingClassType() {
		return genericMethod.getContainingClassType();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isFinal()
	 */
	public boolean isFinal() {
		return genericMethod.isFinal();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPrivate()
	 */
	public boolean isPrivate() {
		return genericMethod.isPrivate();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isProtected()
	 */
	public boolean isProtected() {
		return genericMethod.isProtected();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPublic()
	 */
	public boolean isPublic() {
		return genericMethod.isPublic();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStatic()
	 */
	public boolean isStatic() {
		return genericMethod.isStatic();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStrictFp()
	 */
	public boolean isStrictFp() {
		return genericMethod.isStrictFp();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getFullName()
	 */
	public String getFullName() {
		return genericMethod.getFullName();
	}
	
	public String getBinaryName() {
		return genericMethod.getBinaryName();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getProgramModelInfo()
	 */
	public ProgramModelInfo getProgramModelInfo() {
		return service;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#setProgramModelInfo(recoder.service.ProgramModelInfo)
	 */
	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see recoder.NamedModelElement#getName()
	 */
	public String getName() {
		return genericMethod.getName();
	}

	/* (non-Javadoc)
	 * @see recoder.ModelElement#validate()
	 */
	public void validate() throws ModelException {
		// always valid - implicit model element
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getContainer()
	 */
	public ClassTypeContainer getContainer() {
		return genericMethod.getContainer();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getPackage()
	 */
	public Package getPackage() {
		return genericMethod.getPackage();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getTypes()
	 */
	public List<? extends ClassType> getTypes() {
		return service.getTypes(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof ResolvedGenericMethod))
			return false;
		ResolvedGenericMethod rgm = (ResolvedGenericMethod)o;
		if (!rgm.genericMethod.equals(genericMethod))
			return false;
		for (int i = 0; i < types.size(); i++) {
			// TODO .equals() check required below? Shouldn't types be comparable with == ? 
			if (types.get(i) != rgm.types.get(i) || (types.get(i) != null && !(types.get(i).equals(rgm.types.get(i)))))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		// TODO improve
		return genericMethod.hashCode();
	}
	
	@Override
	public Method getGenericMember() {
		return genericMethod;
	}
	@Override
	public String getFullSignature() {
		return Method.SignatureBuilder.buildSignature(this);
	}

}
