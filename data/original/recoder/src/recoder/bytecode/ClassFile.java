// This file is part of the RECODER library and protected by the LGPL.

package recoder.bytecode;

import java.util.List;

import java.util.Collections;

import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Package;

public class ClassFile extends ByteCodeElement implements ClassType {

	boolean isInner;
	
	private String location; 

	private String fullName;

	private String physicalName;
    
	private String superName;

	private String[] interfaceNames;

	private List<FieldInfo> fields;

	private List<MethodInfo> methods;

	private List<ConstructorInfo> constructors;

	private List<AnnotationUseInfo> annotations;
    
	private List<TypeParameterInfo> typeParams;
    
	private String[] innerClasses;
    
	List<TypeArgumentInfo> superClassTypeArguments;
    
	List<TypeArgumentInfo>[] superInterfacesTypeArguments;

    private ArrayType arrayType;
    
    String enclosingMethod = null;
    
    int version;
    
    ClassFile() {
    	super();
    }
    
    public ArrayType getArrayType() {
    	return arrayType;
    }
    
    public ArrayType createArrayType() {
    	if (arrayType == null)
    		arrayType = new ArrayType(this, service.getServiceConfiguration().getImplicitElementInfo());
    	return arrayType;
    }
    
    void setFullName(String fullName) {
        this.fullName = fullName.intern();
    }

    void setPhysicalName(String phkName) {
        this.physicalName = phkName;
    }

    void setSuperName(String superName) {
        this.superName = superName;
        if (superName != null) {
            this.superName = superName.intern();
        }
    }

    void setInterfaceNames(String[] interfaceNames) {
        this.interfaceNames = interfaceNames;
        if (interfaceNames != null) {
            for (int i = 0; i < interfaceNames.length; i++) {
                interfaceNames[i] = interfaceNames[i].intern();
            }
        }
    }

    void setFields(List<FieldInfo> fields) {
        this.fields = Collections.unmodifiableList(fields);
    }

    void setMethods(List<MethodInfo> methods) {
        this.methods = Collections.unmodifiableList(methods);
    }

    void setConstructors(List<ConstructorInfo> constructors) {
        this.constructors = Collections.unmodifiableList(constructors);
    }

    void setInnerClassNames(String[] innerClassNames) {
        this.innerClasses = innerClassNames;
        if (innerClasses != null) {
            for (int i = 0; i < innerClassNames.length; i++) {
                innerClassNames[i] = innerClassNames[i].intern();
            }
        }
    }
    
    void setAnnotations(List<AnnotationUseInfo> annotations) {
        this.annotations = Collections.unmodifiableList(annotations);
    }

    public final String getTypeName() {
        return fullName;
    }

    public final String getSuperClassName() {
        return superName;
    }
    
    public final List<TypeArgumentInfo> getSuperClassTypeArguments() {
    	return superClassTypeArguments;
    }

    public final String[] getInterfaceNames() {
        return interfaceNames;
    }
    
    public final List<TypeArgumentInfo> getSuperInterfaceTypeArguments(int ifidx) {
    	return superInterfacesTypeArguments == null ? null : superInterfacesTypeArguments[ifidx];
    }

    public final List<FieldInfo> getFieldInfos() {
        return fields;
    }

    public final List<MethodInfo> getMethodInfos() {
        return methods;
    }

    public final List<ConstructorInfo> getConstructorInfos() {
        return constructors;
    }

    public final String[] getInnerClassNames() {
        return innerClasses;
    }

    public final String getFullName() {
        return fullName;
    }
    
	public String getBinaryName() {
		return physicalName;
	}

	/**
	 * Deprecated as of 0.92. Use <code>getBinaryName()</code> instead.
	 * @Deprecated. 
	 * @return the physical (=binary) name of this ClassFile
	 * @see getBinaryName
	 */
	@Deprecated
    public final String getPhysicalName() {
        return physicalName;
    }

    public final ClassTypeContainer getContainer() {
        return service.getClassTypeContainer(this);
    }

    public ClassFile getContainingClassType() {
        ClassTypeContainer ctc = service.getClassTypeContainer(this);
        return (ctc instanceof ClassFile) ? (ClassFile) ctc : null;
    }

    public final Package getPackage() {
        return service.getPackage(this);
    }

    public final boolean isInterface() {
        return (accessFlags & AccessFlags.INTERFACE) != 0;
    }
    
    public boolean isOrdinaryInterface() {
        return (accessFlags & AccessFlags.INTERFACE) != 0 &&
               (accessFlags & AccessFlags.ANNOTATION) == 0;
    }
    
    public boolean isAnnotationType() {
        return (accessFlags & AccessFlags.ANNOTATION) != 0;
    }

    public boolean isEnumType() {
        return (accessFlags & AccessFlags.ENUM) != 0;
    }

    public boolean isOrdinaryClass() {
        return (accessFlags & AccessFlags.INTERFACE) == 0 &&
        	   (accessFlags & AccessFlags.ENUM) == 0;
    }

    public final List<ClassType> getSupertypes() {
        return service.getSupertypes(this);
    }

    public final List<ClassType> getAllSupertypes() {
        return service.getAllSupertypes(this);
    }

    @SuppressWarnings("unchecked")
	public final List<FieldInfo> getFields() {
       return (List<FieldInfo>)service.getFields(this);
    }

    public final List<Field> getAllFields() {
        return service.getAllFields(this);
    }

    public final List<Method> getMethods() {
        return service.getMethods(this);
    }

    public final List<Method> getAllMethods() {
        return service.getAllMethods(this);
    }

    public final List<? extends Constructor> getConstructors() {
        return service.getConstructors(this);
    }

    @SuppressWarnings("unchecked")
	public final List<ClassFile> getTypes() {
        return (List<ClassFile>)service.getTypes(this);
    }

    public final List<ClassType> getAllTypes() {
        return service.getAllTypes(this);
    }

    /**
     * @return a list of annotations
     */
    public List<AnnotationUseInfo> getAnnotations() {
        return annotations;
    }

	public List<TypeParameterInfo> getTypeParameters() {
		return typeParams;
	}
	
	public void setTypeParameters(List<TypeParameterInfo> typeParams)  {
		this.typeParams = typeParams;
	}
	
	void setLocation(String location) {
		this.location = location;
	}
	
	public String getLocation() {
		return location;
	}
	
	@Override
	public String toString() {
		return "ClassFile " + getFullName();
	}

    public String getFullSignature() {
    	String res = getFullName();
    	if (getTypeParameters() == null || getTypeParameters().size() == 0)
    		return res;
    	res += "<";
    	String del = "";
    	for (TypeParameterInfo ta : getTypeParameters()) {
    		res = res + del + ta.getFullSignature();
    		del = ",";
    	}
    	res = res + ">";
    	return res;
    }
    
	private ErasedType erasedType = null;
	
	public ErasedType getErasedType() {
		if (erasedType == null)
			erasedType = new ErasedType(this, service.getServiceConfiguration().getImplicitElementInfo());
		return erasedType;
	}

	public boolean isInner() {
		return isInner;
	}

	public ClassType getBaseClassType() {
		return this;
	}

	/**
	 * NOT FOR PUBLIC USE, SUBJECT TO CHANGE / BE REMOVED !!
	 */
	public String getEnclosingMethod() {
		return enclosingMethod;
	}

	@Override
	public ClassFile getGenericMember() {
		return this;
	}

	public int getVersion()  {
		return version;
	}
}

