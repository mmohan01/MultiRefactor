// This file is part of the RECODER library and protected by the LGPL.

package recoder.abstraction;

import java.util.Collections;
import java.util.List;

import recoder.ModelException;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * A program model element representing array types.
 * 
 * @author AL
 * @author RN
 */
public final class ArrayType implements ClassType {
	/**
	 * @author Tobias
	 *
	 */
	public final class ArrayLengthField implements Field {
		/**
		 * 
		 */
		private ArrayLengthField() {
			// private
		}

		public List<? extends TypeArgument> getTypeArguments() {
			return null;
		}

		public Type getType() {
			return pmi.getServiceConfiguration().getNameInfo().getIntType();
		}

		public boolean isFinal() {
			return true;
		}

		public String getFullName() {
			return ArrayType.this.getFullName() + ".length";
		}

		public ImplicitElementInfo getProgramModelInfo() {
			return pmi;
		}

		public void setProgramModelInfo(ProgramModelInfo pmi) {
			throw new UnsupportedOperationException();
		}

		public String getName() {
			return "length";
		}

		public void validate() throws ModelException {
			// always valid - implicitly defined.
		}

		public List<? extends AnnotationUse> getAnnotations() {
			return null;
		}

		public ArrayType getContainingClassType() {
			return ArrayType.this;
		}

		public boolean isPrivate() {
			return false;
		}

		public boolean isProtected() {
			return false;
		}

		public boolean isPublic() {
			return true;
		}

		public boolean isStatic() {
			return false;
		}

		public boolean isStrictFp() {
			return false;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ArrayLengthField)) return false;
			return ((ArrayLengthField)o).getContainingClassType().equals(ArrayType.this);
		}
		
		@Override
		public int hashCode() {
			return ArrayType.this.hashCode();
		}
		
		public String getBinaryName() {
			return ArrayType.this.getBinaryName() + ".length";
		}
		
		@Override
		public ArrayLengthField getGenericMember() {
			return this;
		}
	}
	
	private static final String CLONE_NAME = "clone".intern();
	public final class ArrayCloneMethod implements Method {
		/**
		 * 
		 */
		private ArrayCloneMethod() {
			// private
		}

		public List<ClassType> getExceptions() {
			// ArrayType.clone() throws no checked exception, 
			// as described in JLS §10.7
			return Collections.<ClassType>emptyList();
		}

		public Type getReturnType() {
			return pmi.getReturnType(this);
		}

		public List<Type> getSignature() {
			return pmi.getSignature(this);
		}

		public List<? extends TypeParameter> getTypeParameters() {
			return null;
		}

		public boolean isAbstract() {
			return false;
		}

		public boolean isNative() {
			return true;
		}

		public boolean isSynchronized() {
			return false;
		}

		public boolean isVarArgMethod() {
			return false;
		}

		public List<? extends AnnotationUse> getAnnotations() {
			return null;
		}

		public ArrayType getContainingClassType() {
			return ArrayType.this;
		}

		public boolean isFinal() {
			return false;
		}

		public boolean isPrivate() {
			return false;
		}

		public boolean isProtected() {
			return false;
		}

		public boolean isPublic() {
			return true;
		}

		public boolean isStatic() {
			return false;
		}

		public boolean isStrictFp() {
			return false;
		}

		public String getFullName() {
			return ArrayType.this.getFullName() + ".clone";
		}

		public ImplicitElementInfo getProgramModelInfo() {
			return pmi;
		}

		public void setProgramModelInfo(ProgramModelInfo pmi) {
			throw new UnsupportedOperationException();
		}

		public String getName() {
			return CLONE_NAME;
		}

		public void validate() throws ModelException {
			// always valid, as an implicit Method.
		}

		public ClassTypeContainer getContainer() {
			return ArrayType.this;
		}

		public Package getPackage() {
			return null;
		}

		public List<? extends ClassType> getTypes() {
			return null;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ArrayCloneMethod)) return false;
			return ((ArrayCloneMethod)o).getContainingClassType().equals(this);
		}
		
		@Override
		public int hashCode() {
			return ArrayType.this.hashCode();
		}
		
		public String getBinaryName() {
			return ArrayType.this.getBinaryName() + ".clone";
		}
		
		@Override
		public ArrayCloneMethod getGenericMember() {
			return this;
		}

		@Override
		public String getFullSignature() {
			return Method.SignatureBuilder.buildSignature(this);
		}
	}
	
    private Type basetype;

    private ImplicitElementInfo pmi;
    
    private final ArrayLengthField lengthField;
    
    private final ArrayCloneMethod cloneMethod;

    private ArrayType arrayType;
    
    /**
     * Creates a new array type for the given base type, organized by the given
     * program model info.
     * 
     * @param basetype
     *            the base type of the array.
     * @param pmi
     *            the program model info responsible for this type.
     */
    public ArrayType(Type basetype, ImplicitElementInfo pmi) {
        this.basetype = basetype;
        this.pmi = pmi;
        lengthField = new ArrayLengthField();
        cloneMethod = new ArrayCloneMethod();
    }
    
    public ArrayType getArrayType() {
    	return arrayType;
    }
    
    public ArrayType createArrayType() {
    	if (arrayType == null)
    		arrayType = new ArrayType(this, pmi.getServiceConfiguration().getImplicitElementInfo());
    	return arrayType;
    }
    
    public ArrayLengthField getArrayLengthField() {
    	return lengthField;
    }
    
    public ArrayCloneMethod getArrayCloneMethod() {
    	return cloneMethod;
    }
 
    /**
     * Returns the base type of this array type.
     * 
     * @return the base type.
     */
    public Type getBaseType() {
        return basetype;
    }

    /**
     * Returns the name of this array type.
     * 
     * @return the name of this type.
     */
    public String getName() {
        return (basetype.getName() + "[]").intern();
    }

    /**
     * Returns the maximal expanded name including all applicable qualifiers.
     * 
     * @return the full name of this program model element.
     */
    public String getFullName() {
        return basetype.getFullName() + "[]";
    }
    
    public String getBinaryName() {
		return basetype.getBinaryName() + "[]";
	}

    /**
     * Returns the instance that can retrieve information about this program
     * model element.
     * 
     * @return the program model info of this element.
     */
    public ImplicitElementInfo getProgramModelInfo() {
        return pmi;
    }

    /**
     * Sets the instance that can retrieve information about this program model
     * element.
     * 
     * @param service
     *            the program model info for this element.
     */
    public void setProgramModelInfo(ProgramModelInfo service) {
        throw new UnsupportedOperationException();
    }

    public void validate() {
    	// not checked yet
    }
    
    @Override
    public boolean equals(Object o2) {
    	if (!(o2 instanceof ArrayType)) return false;
    	Type other = (ArrayType)o2;
    	Type me = this;
    	while (me instanceof ArrayType && other instanceof ArrayType) {
    		me = ((ArrayType)me).basetype;
    		other = ((ArrayType)other).basetype;
    	}
    	if (me instanceof ArrayType || other instanceof ArrayType) return false;
    	return me.equals(other);
    }

	public List<Field> getAllFields() {
		return pmi.getAllFields(this);
	}

	public List<Method> getAllMethods() {
		return pmi.getAllMethods(this);
	}

	public List<ClassType> getAllSupertypes() {
		return pmi.getAllSupertypes(this);
	}

	public List<ClassType> getAllTypes() {
		return pmi.getAllTypes(this);
	}

	public List<? extends Constructor> getConstructors() {
		return pmi.getConstructors(this);
	}

	public List<? extends Field> getFields() {
		return pmi.getFields(this);
	}

	public List<Method> getMethods() {
		return pmi.getMethods(this);
	}

	public List<ClassType> getSupertypes() {
		return pmi.getSupertypes(this);
	}

	public List<? extends TypeParameter> getTypeParameters() {
		// Maybe in Java 7 then... ?
		return Collections.<TypeParameter>emptyList();
	}

	public boolean isAbstract() {
		return false;
	}

	public boolean isAnnotationType() {
		return false;
	}

	public boolean isEnumType() {
		return false;
	}

	public boolean isInterface() {
		return false;
	}

	public boolean isOrdinaryClass() {
		return false;
	}

	public boolean isOrdinaryInterface() {
		return false;
	}

	public List<? extends AnnotationUse> getAnnotations() {
		// an array cannot be annotated.
		throw new RuntimeException("Not implemented yet!");
	}

	public ClassType getContainingClassType() {
		return null;
	}

	public boolean isFinal() {
		return true;
	}

	public boolean isPrivate() {
		return basetype instanceof PrimitiveType ? false : 
			((ClassType)basetype).isPrivate();
	}

	public boolean isProtected() {
		return basetype instanceof PrimitiveType ? false : 
			((ClassType)basetype).isProtected();
	}

	public boolean isPublic() {
		return basetype instanceof PrimitiveType ? true : 
			((ClassType)basetype).isPublic();
	}

	public boolean isStatic() {
		return false;
	}

	public boolean isStrictFp() {
		return false;
	}

	public ClassTypeContainer getContainer() {
		return null;
	}

	public Package getPackage() {
		return null;
	}

	public List<? extends ClassType> getTypes() {
		return null;
	}
	
	@Override
	public String toString() {
		return "ArrayType " + getFullName();
	}

    @Override
    public int hashCode() {
    	Type t = this;
    	int dim  = 0;
    	while (t instanceof ArrayType) {
    		t = ((ArrayType)t).getBaseType();
    		dim++;
    	}
    	return t.hashCode() + dim;
    }

    public String getFullSignature() {
    	if (basetype instanceof PrimitiveType)
    		return getFullName();
    	return ((ClassType)basetype).getFullSignature() + "[]";
    }

	public ErasedType getErasedType() {
		throw new UnsupportedOperationException();
	}

	public boolean isInner() {
		return false;
	}

	public ClassType getBaseClassType() {
		return this;
	}

	@Override
	public ArrayType getGenericMember() {
		return this;
	}
}