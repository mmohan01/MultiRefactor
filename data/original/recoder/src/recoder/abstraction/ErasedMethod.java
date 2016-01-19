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
public class ErasedMethod implements Method {
	private final Method genericMethod;
	private final ImplicitElementInfo service;
	
	/**
	 * 
	 */
	public ErasedMethod(Method genericMethod, ImplicitElementInfo service) {
		this.service = service;
		this.genericMethod = genericMethod;
		assert !(genericMethod instanceof ErasedMethod 
				|| genericMethod instanceof ParameterizedMethod);
	}
	
	public Method getGenericMethod() {
		return genericMethod;
	}

	@Override
	public List<ClassType> getExceptions() {
		return service.getExceptions(this);
	}

	@Override
	public Type getReturnType() {
		return service.getReturnType(this);
	}

	@Override
	public List<Type> getSignature() {
		return service.getSignature(this);
	}

	@Override
	public List<? extends TypeParameter> getTypeParameters() {
		return null;
	}

	@Override
	public boolean isAbstract() {
		return genericMethod.isAbstract();
	}

	@Override
	public boolean isNative() {
		return genericMethod.isNative();
	}

	@Override
	public boolean isSynchronized() {
		return genericMethod.isSynchronized();
	}

	@Override
	public boolean isVarArgMethod() {
		return genericMethod.isVarArgMethod();
	}

	@Override
	public List<? extends AnnotationUse> getAnnotations() {
		return getGenericMethod().getAnnotations(); // ??
	}

	@Override
	public ClassType getContainingClassType() {
		return genericMethod.getContainingClassType().getErasedType();
	}

	@Override
	public boolean isFinal() {
		return genericMethod.isFinal();
	}

	@Override
	public boolean isPrivate() {
		return genericMethod.isPrivate();
	}

	@Override
	public boolean isProtected() {
		return genericMethod.isProtected();
	}

	@Override
	public boolean isPublic() {
		return genericMethod.isPublic();
	}

	@Override
	public boolean isStatic() {
		return genericMethod.isStatic();
	}

	@Override
	public boolean isStrictFp() {
		return genericMethod.isStrictFp();
	}

	@Override
	public String getFullName() {
		return genericMethod.getFullName();
	}
	
	@Override
    public String getBinaryName() {
		return genericMethod.getBinaryName();
	}


	@Override
	public ImplicitElementInfo getProgramModelInfo() {
		return service;
	}

	@Override
	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new RuntimeException();
	}

	@Override
	public String getName() {
		return genericMethod.getName();
	}

	@Override
	public void validate() throws ModelException {
		// TODO Auto-generated method stub

	}

	@Override
	public ClassTypeContainer getContainer() {
		return genericMethod.getContainer(); // ??
	}

	@Override
	public Package getPackage() {
		return genericMethod.getPackage();
	}

	@Override
	public List<? extends ClassType> getTypes() {
		return service.getTypes(this); 
	}

	@Override
	public String toString() {
		return genericMethod.toString() + "%ERASED%";
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
