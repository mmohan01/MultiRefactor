/**
 * 
 */
package recoder.abstraction;

import java.util.Collections;
import java.util.List;

import recoder.ModelException;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * TODO 0.90 doc / RENAME
 * TODO 0.90 instances could/should be cached
 * @author Tobias Gutzmann
 *
 */
public final class DummyGetClassMethod implements Method {
	private final ClassType parent;
	private final ImplicitElementInfo service;
	public DummyGetClassMethod(ClassType parent, ImplicitElementInfo service) {
		this.parent = parent;
		this.service = service;
	}
	
	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getExceptions()
	 */
	public List<ClassType> getExceptions() {
		return Collections.<ClassType>emptyList();
	}

	public ClassType getParentClass() {
		return parent;
	}
	
	public ClassType getReturnType() {
		return (ClassType)service.getReturnType(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getSignature()
	 */
	public List<Type> getSignature() {
		return Collections.<Type>emptyList();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#getTypeParameters()
	 */
	public List<? extends TypeParameter> getTypeParameters() {
		return Collections.<TypeParameter>emptyList();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isAbstract()
	 */
	public boolean isAbstract() {
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isNative()
	 */
	public boolean isNative() {
		return true;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isSynchronized()
	 */
	public boolean isSynchronized() {
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Method#isVarArgMethod()
	 */
	public boolean isVarArgMethod() {
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getAnnotations()
	 */
	public List<? extends AnnotationUse> getAnnotations() {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getContainingClassType()
	 */
	public ClassType getContainingClassType() {
		return service.getServiceConfiguration().getNameInfo().getJavaLangObject();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isFinal()
	 */
	public boolean isFinal() {
		return true;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPrivate()
	 */
	public boolean isPrivate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isProtected()
	 */
	public boolean isProtected() {
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPublic()
	 */
	public boolean isPublic() {
		return true;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStrictFp()
	 */
	public boolean isStrictFp() {
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getFullName()
	 */
	public String getFullName() {
		return "java.lang.Object.getClass";
	}
	
    public String getBinaryName() {
		return getFullName();
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

	private static final String CLASS_NAME = "getClass".intern();
	
	/* (non-Javadoc)
	 * @see recoder.NamedModelElement#getName()
	 */
	public String getName() {
		return CLASS_NAME;
	}

	/* (non-Javadoc)
	 * @see recoder.ModelElement#validate()
	 */
	public void validate() throws ModelException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getContainer()
	 */
	public ClassTypeContainer getContainer() {
		return getContainingClassType();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getPackage()
	 */
	public Package getPackage() {
		return service.getServiceConfiguration().getNameInfo().getPackage("java.lang");
	}

	/**
	 * @return the empty list.
	 */
	public List<ClassType> getTypes() {
		return Collections.<ClassType>emptyList();
	}

	@Override
	public Method getGenericMember() {
		return service.getJavaLangObjectGetClass(); 
	}

	@Override
	public String getFullSignature() {
		return Method.SignatureBuilder.buildSignature(this);
	}
}
