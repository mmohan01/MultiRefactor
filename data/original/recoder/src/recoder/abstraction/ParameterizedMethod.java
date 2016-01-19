/**
 * 
 */
package recoder.abstraction;

import java.util.List;

import recoder.ModelException;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * A wrapper for methods belonging to a parameterized type. The 
 * type arguments to the parameterized type are replaced in the
 * formerly generic method.   
 * @author Tobias
 *
 */
public class ParameterizedMethod implements Method {

	private final Method genericMethod;
	private final ParameterizedType parent;
	
	private final ImplicitElementInfo service;
	/**
	 * 
	 */
	public ParameterizedMethod(Method genericMethod, ParameterizedType parentClassType,
			ImplicitElementInfo service) {
		// TODO the below happens in FindBugs. Should be okej,
		// but look into it - when does it happen?
//		assert !(genericMethod instanceof ParameterizedMethod);
		this.genericMethod = genericMethod;
		this.parent = parentClassType;
		this.service = service;
	}

	public Method getGenericMethod() {
		return genericMethod;
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
		return genericMethod.getTypeParameters();
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
		throw new RuntimeException("Not implemented yet!");
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getContainingClassType()
	 */
	
	public ParameterizedType getContainingClassType() {
		return parent;
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
	
	public ImplicitElementInfo getProgramModelInfo() {
		return service;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#setProgramModelInfo(recoder.service.ProgramModelInfo)
	 */
	
	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new RuntimeException("ParameterizedMethod.setPrograModelInfo() must not be called!");
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
		// not checked yet!

	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getContainer()
	 */
	
	public ClassTypeContainer getContainer() {
		return parent;
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
		throw new RuntimeException("Not implemented yet!");
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ParameterizedMethod))
			return false;
		ParameterizedMethod other = (ParameterizedMethod)o;
		return other.genericMethod == genericMethod && other.parent == parent;
	}
	
	@Override
	public int hashCode() {
		return genericMethod.hashCode() ^ parent.hashCode();
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
