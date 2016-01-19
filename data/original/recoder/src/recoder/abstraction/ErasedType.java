/**
 * 
 */
package recoder.abstraction;

import java.util.List;

import recoder.ModelException;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public class ErasedType implements ClassType {
	private final ClassType genericType;
	private final ImplicitElementInfo service;
	
	private ArrayType arrayType;
	
	/**
	 * TODO 0.90 factory / cache !
	 * TODO 0.90 raw type / erased type naming conventions ?
	 */
	public ErasedType(ClassType genericType, ImplicitElementInfo service) {
		this.genericType = genericType;
		this.service = service;
		assert !(genericType instanceof ErasedType || genericType instanceof ParameterizedType);
		assert !(genericType instanceof ArrayType);
		assert (genericType != null);
	}
	
    public ArrayType getArrayType() {
    	return arrayType;
    }
    
    public ArrayType createArrayType() {
    	if (arrayType == null)
    		arrayType = new ArrayType(this, service.getServiceConfiguration().getImplicitElementInfo());
    	return arrayType;
    }
	
	public ClassType getGenericType() {
		return genericType;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getAllFields()
	 */
	public List<Field> getAllFields() {
		return service.getAllFields(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getAllMethods()
	 */
	public List<Method> getAllMethods() {
		return service.getAllMethods(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getAllSupertypes()
	 */
	public List<ClassType> getAllSupertypes() {
		return service.getAllSupertypes(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getAllTypes()
	 */
	public List<ClassType> getAllTypes() {
		return service.getAllTypes(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getConstructors()
	 */
	public List<? extends Constructor> getConstructors() {
		return service.getConstructors(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getFields()
	 */
	public List<? extends Field> getFields() {
		return service.getFields(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getFullDescription()
	 */
	public String getFullSignature() {
		return getFullName() + "%RAW%";
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getMethods()
	 */
	public List<Method> getMethods() {
		return service.getMethods(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getSupertypes()
	 */
	public List<ClassType> getSupertypes() {
		return service.getSupertypes(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#getTypeParameters()
	 */
	public List<? extends TypeParameter> getTypeParameters() {
		// Erased/raw, so none!
		return null;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#isAbstract()
	 */
	public boolean isAbstract() {
		return genericType.isAbstract();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#isAnnotationType()
	 */
	public boolean isAnnotationType() {
		return genericType.isAnnotationType();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#isEnumType()
	 */
	public boolean isEnumType() {
		return genericType.isEnumType();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#isInterface()
	 */
	public boolean isInterface() {
		return genericType.isInterface();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#isOrdinaryClass()
	 */
	public boolean isOrdinaryClass() {
		return genericType.isOrdinaryClass();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassType#isOrdinaryInterface()
	 */
	public boolean isOrdinaryInterface() {
		return genericType.isOrdinaryInterface();
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
		return service;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#setProgramModelInfo(recoder.service.ProgramModelInfo)
	 */
	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new RuntimeException();
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

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getAnnotations()
	 */
	public List<? extends AnnotationUse> getAnnotations() {
		return genericType.getAnnotations(); // ???
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getContainingClassType()
	 */
	public ClassType getContainingClassType() {
		return genericType.getContainingClassType(); // ???
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isFinal()
	 */
	public boolean isFinal() {
		return genericType.isFinal();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPrivate()
	 */
	public boolean isPrivate() {
		return genericType.isPrivate();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isProtected()
	 */
	public boolean isProtected() {
		return genericType.isProtected();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPublic()
	 */
	public boolean isPublic() {
		return genericType.isPublic();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStatic()
	 */
	public boolean isStatic() {
		return genericType.isStatic();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStrictFp()
	 */
	public boolean isStrictFp() {
		return genericType.isStrictFp();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getContainer()
	 */
	public ClassTypeContainer getContainer() {
		return genericType.getContainer(); // TODO erase ???
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getPackage()
	 */
	public Package getPackage() {
		return genericType.getPackage();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ClassTypeContainer#getTypes()
	 */
	public List<? extends ClassType> getTypes() {
		return service.getTypes(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ErasedType))
			return false;
		return this.genericType.equals(((ErasedType)o).genericType);
	}
	
	@Override
	public int hashCode() {
		return genericType.hashCode();
	}

	public ErasedType getErasedType() {
		return this;
	}

	public boolean isInner() {
		return genericType.isInner();
	}
	
	@Override
	public String toString() {
		return "%RAW%"+getFullName();
	}

	public ClassType getBaseClassType() {
		return genericType;
	}

	@Override
	public ClassType getGenericMember() {
		return genericType;
	}

}
