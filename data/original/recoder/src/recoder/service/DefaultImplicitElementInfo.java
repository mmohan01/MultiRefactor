// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import recoder.ServiceConfiguration;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ArrayType.ArrayCloneMethod;
import recoder.abstraction.ArrayType.ArrayLengthField;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.DefaultConstructor;
import recoder.abstraction.DummyGetClassMethod;
import recoder.abstraction.ErasedConstructor;
import recoder.abstraction.ErasedField;
import recoder.abstraction.ErasedMethod;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.ImplicitEnumMethod;
import recoder.abstraction.ImplicitEnumValueOf;
import recoder.abstraction.ImplicitEnumValues;
import recoder.abstraction.IntersectionType;
import recoder.abstraction.Member;
import recoder.abstraction.Method;
import recoder.abstraction.NullType;
import recoder.abstraction.Package;
import recoder.abstraction.ParameterizedConstructor;
import recoder.abstraction.ParameterizedField;
import recoder.abstraction.ParameterizedMethod;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.ResolvedGenericMethod;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeArgument.CapturedTypeArgument;
import recoder.abstraction.TypeArgument.WildcardMode;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.util.Debug;

/**
 * Handles requests for implicitly defined program model elements. In
 * particular these are {@link recoder.abstraction.NullType},
 * {@link recoder.abstraction.Package},
 * {@link recoder.abstraction.ArrayType},
 * {@link recoder.abstraction.DefaultConstructor},
 * {@link recoder.abstraction.ImplicitEnumValueOf},
 * {@link recoder.abstraction.ImplicitEnumValues},
 * {@link recoder.abstraction.Parameterized*},
 * {@link recoder.abstraction.Erased*},
 * {@link recoder.abstraction.DummyGetClassMethod},
 * {@link recoder.abstraction.ResolvedGenericMethod},
 * and {@link recoder.abstraction.IntersectionType}.
 */
public class DefaultImplicitElementInfo extends DefaultProgramModelInfo implements ImplicitElementInfo {

    /** maps type declarations to default constructors */
    private final Map<ClassType, DefaultConstructor> type2defaultConstructor = new IdentityHashMap<ClassType, DefaultConstructor>();
    
    private final Map<EnumDeclaration,List<ImplicitEnumMethod>> type2implicitEnumMethods = new IdentityHashMap<EnumDeclaration,List<ImplicitEnumMethod>>();

    /** Mapping method -> its return type, if the type is "calculated" in DefaultImplicitElementInfo.
     * This is the case, e.g., for ParameterizedTypes. 
     */
    private final Map<Method, Type> methodToReturnType = new IdentityHashMap<Method, Type>();
    private final Map<ParameterizedMethod, List<ClassType>> methodToExceptions = new IdentityHashMap<ParameterizedMethod, List<ClassType>>();
    private final Map<Method, List<Type>> methodToSig = new IdentityHashMap<Method, List<Type>>();
    
    private Method javaLangObjectGetClass;
    /**
     * @param config
     *            the configuration this services becomes part of.
     */
    public DefaultImplicitElementInfo(ServiceConfiguration config) {
        super(config);
    }

    public DefaultConstructor getDefaultConstructor(ClassType ct) {
        Debug.assertNonnull(ct);
        updateModel();
        DefaultConstructor cons = type2defaultConstructor.get(ct);
        if (cons == null) {
            cons = new DefaultConstructor(ct);
            cons.setProgramModelInfo(this);
            type2defaultConstructor.put(ct, cons);
        }
        return cons;
    }
    
    public List<ImplicitEnumMethod> getImplicitEnumMethods(EnumDeclaration etd) {
    	if (etd == null) throw new NullPointerException();
    	updateModel();
    	List<ImplicitEnumMethod> res = type2implicitEnumMethods.get(etd);
    	if (res == null) {
    		res = new ArrayList<ImplicitEnumMethod>(2);
    		ImplicitEnumMethod meth = new ImplicitEnumValueOf(etd);
    		meth.setProgramModelInfo(this);
    		res.add(meth);
    		meth = new ImplicitEnumValues(etd);
    		meth.setProgramModelInfo(this);
    		res.add(meth);
    		type2implicitEnumMethods.put(etd, res);
    	}
    	return res;
    }
    
    public Type getType(ProgramModelElement pme) {
        if (pme instanceof NullType 
        		|| pme instanceof ArrayType 
        		|| pme instanceof IntersectionType
        		|| pme instanceof ErasedType
        		|| pme instanceof CapturedTypeArgument) {
            return (Type) pme;
        } else if (pme instanceof Package) {
            // valid for Package
            return null; 
        } else if (pme instanceof ErasedField) {
        	return eraseType(((ErasedField)pme).getGenericField().getType());
        } else if (pme instanceof ParameterizedField) {
        	ParameterizedField pf = (ParameterizedField)pme;
    		Type genRet = pf.getGenericField().getType();
    		
    		return replaceAllTypeParametersWithArgs(genRet, 
    				pf.getContainingClassType().getDefinedTypeParameters(), 
    				pf.getContainingClassType().getAllTypeArgs());
        } else if (pme instanceof ArrayLengthField) {
        	return getNameInfo().getIntType();
        } else {
        	return getReturnType((Method)pme);
        }
    }

    public Package getPackage(ProgramModelElement pme) {
        if (pme instanceof Package) {
            return (Package) pme;
        }
        if (pme instanceof DefaultConstructor || pme instanceof ImplicitEnumMethod) {
            updateModel();
            return getContainingClassType((Method) pme).getPackage();
        }
        // TODO valid for ... ?
        return null;
    }

    public List<? extends ClassType> getTypes(ClassTypeContainer ctc) {
        if (ctc instanceof Package) {
            return serviceConfiguration.getNameInfo().getTypes((Package) ctc);
        }
        if (ctc instanceof DefaultConstructor) {
            return Collections.emptyList();
        }
        if (ctc instanceof ErasedMethod) {
        	return eraseTypes(((ErasedMethod)ctc).getGenericMethod().getTypes());
        }
        if (ctc instanceof ErasedType) {
        	return eraseTypes(((ErasedType)ctc).getGenericType().getTypes());
        }
        if (ctc instanceof ParameterizedType) {
        	ParameterizedType pt = (ParameterizedType)ctc;
        	List<? extends ClassType> genericTypes = pt.getGenericType().getTypes();
        	ArrayList<ClassType> res = new ArrayList<ClassType>(genericTypes.size());
        	for (ClassType ct : genericTypes) {
        		if (ct.isInner())
            		// TODO check this
        			res.add(getParameterizedType(ct, null, (ParameterizedType)ctc));
        		else
        			res.add(ct);
        	}
        	return res;
        }
        if (ctc instanceof ResolvedGenericMethod) {
        	// TODO 
        	throw new RuntimeException();
        }
        // TODO valid for ... ?
        return null;
    }

    public List<ClassType> getAllTypes(ClassType ct) {
    	if (ct instanceof ErasedType || ct instanceof ParameterizedType
    			|| ct instanceof IntersectionType)
    		return super.getAllTypes(ct);
        // valid for NullType, ArrayType
    	assert ct == getNameInfo().getNullType()
    			|| ct instanceof ArrayType : ct.getClass().getName();
        return null;
    }

    public ClassTypeContainer getClassTypeContainer(ClassType ct) {
        // valid for NullType
    	assert ct == getNameInfo().getNullType();
        return null;
    }
    
    private ClassType makeParentParameterizedType(ParameterizedType subType, ClassType st) {
    	if (!(st instanceof ParameterizedType))
    		return st;
    	ParameterizedType superType = (ParameterizedType)st;

    	ClassType res = replaceAllTypeParametersWithArgs(superType,
    			subType.getDefinedTypeParameters(), subType.getAllTypeArgs());
    	
    	return res;
    }
    	
    public List<ClassType> getSupertypes(ClassType ct) {
    	if (ct instanceof IntersectionType) {
    		return ((IntersectionType)ct).getSupertypes();
    	}
    	if (ct instanceof ParameterizedType) {
    		ParameterizedType pt = (ParameterizedType)ct;
    		List<ClassType> genericSupers = pt.getGenericType().getSupertypes();
    		ArrayList<ClassType> res = new ArrayList<ClassType>(genericSupers.size()+1);
    		res.add(pt.getGenericType().getErasedType());
    		for (ClassType genSuper : genericSupers) {
    			res.add(makeParentParameterizedType(pt, genSuper));
    		}
    		return res;
    	}
    	if (ct instanceof ErasedType) {
    		ErasedType rt = (ErasedType)ct;
    		List<ClassType> genericSupers = rt.getGenericType().getSupertypes();
    		ArrayList<ClassType> res = new ArrayList<ClassType>(genericSupers.size());
    		for (ClassType sup : genericSupers) {
    			res.add(sup.getErasedType()); // parameterized types return the erased type of the generic type anyway
    		}
    		return res;
    	}
    	if (ct instanceof NullType)
    		// actually, every reference type there is... JLS 3rd edition, §4.10.2,
    		// but we handle null explicitly in Recoder
    		return null;
    	if (ct instanceof ArrayType) {
    		/* JLS 3rd edition, § 4.10.3 lists the rules for 
    		 * direct subtyping among array types:
    		 * (1) S and T reference types, T direct subtype of S -> T[] direct subtype of S[]
    		 * (2) Object[] direct subtype of Object, Cloneable, and java.io.Serializable
    		 * (3) if p is a primitive type:
    		 *     p[] direct subtype of Object, Cloneable, java.io.Serializable
    		 * 
    		 */
    		ArrayList<ClassType> res = new ArrayList<ClassType>();
    		NameInfo ni = getNameInfo();
    		ArrayType at = (ArrayType)ct;    		
    		Type baseType = at.getBaseType();
    		if (baseType instanceof ErasedType) 
    			baseType = ((ErasedType)baseType).getGenericMember();
    		if (baseType instanceof PrimitiveType || baseType == ni.getJavaLangObject()) {
    			// rule 2 + 3
    			res.add(ni.getJavaIoSerializable());
    			res.add(ni.getJavaLangCloneable());
    			res.add(ni.getJavaLangObject());
    		} else {
    			// rule 1
    			List<ClassType> baseTypesSupers = ((ClassType)baseType).getSupertypes();
    			for (ClassType sup : baseTypesSupers) {
    				res.add(ni.createArrayType(sup));
    			}
    		}
    		res.trimToSize();
    		return res;
    	}
    	if (ct instanceof CapturedTypeArgument) {
    		CapturedTypeArgument cta = (CapturedTypeArgument)ct;
    		TypeArgument ta = cta.getTypeArgument();
    		if (ta.getWildcardMode() == WildcardMode.Any
    				|| ta.getWildcardMode() == WildcardMode.Super) {
        		// TODO fix "? super T"-handling, this is just a hack for now!!
        		return ta.getTargetedTypeParameter().getSupertypes();
    		}
    		// TODO  ?
    		return getBaseType(ta).getSupertypes();
    	}
    		
    	return ct.getProgramModelInfo().getSupertypes(ct); 
    }

    public List<ClassType> getAllSupertypes(ClassType ct) {
    	ProgramModelInfo pmi = ct.getProgramModelInfo();
    	if (pmi != this)
    		return pmi.getAllSupertypes(ct);
        if (ct instanceof ParameterizedType || ct instanceof ArrayType
        		|| ct instanceof IntersectionType || ct instanceof ErasedType
        		|| ct instanceof CapturedTypeArgument)
        	return super.getAllSupertypes(ct);
        if (ct instanceof NullType) {
        	List<ClassType> result = new ArrayList<ClassType>(1);
        	result.add(ct);
            return result;
        }
        // must not be reached
        throw new RuntimeException("DefaultImplicitElementInfo not a valid service for " + ct.getClass().getName());
    }

    public List<? extends Field> getFields(ClassType ct) {
    	if (ct instanceof ParameterizedType) {
    		ParameterizedType pt = (ParameterizedType)ct;
    		List<? extends Field> temp = pt.getGenericType().getFields();
    		List<Field> res = new ArrayList<Field>(temp.size());
    		for (Field f : temp) {
    			if (getServiceConfiguration().getSourceInfo().containsTypeParameter(f))
    				res.add(new ParameterizedField(f, pt, this));
    			else 
    				res.add(f);
    		}
    		return res;
    	}
    	if (ct instanceof ArrayType) {
    		ArrayType at = (ArrayType)ct;
    		ArrayList<Field> res = new ArrayList<Field>(1);
    		res.add(at.getArrayLengthField());
    		return res;
    	}
    	if (ct instanceof ErasedType) {
    		ErasedType et = (ErasedType)ct;
    		List<? extends Field> genericFields = et.getGenericType().getFields();
    		ArrayList<Field> res = new ArrayList<Field>(genericFields.size());
    		for (Field f : genericFields) {
    			// we do create erased fields even if not a type variable or parameterized.
    			// this way, the reference back to the containing class type is the 
    			// erased field.
    			// However, do not create an erased field for static fields.
    			if (f.isStatic()) {
    				res.add(f);
    			} else {
    				res.add(new ErasedField(f, this));
    			}
    		}
    		return res;
    	}
    	if (ct instanceof CapturedTypeArgument) {
    		TypeArgument ta = ((CapturedTypeArgument)ct).getTypeArgument();
    		if (ta.getWildcardMode() == WildcardMode.Any
        			|| ta.getWildcardMode() == WildcardMode.Super) {
        		// special handling required, but it's done in getSupertypes...
        		// None therefore...
        		return Collections.<Field>emptyList();
        	}
    		return getBaseType(ta).getFields();
    	}

        // valid for NullType
    	assert ct == getNameInfo().getNullType();
        return null;
    }

    public List<Field> getAllFields(ClassType ct) {
        // valid for NullType
    	if (ct instanceof IntersectionType 
    		|| ct instanceof ParameterizedType 
    		|| ct instanceof ArrayType
    		|| ct instanceof ErasedType
    		|| ct instanceof CapturedTypeArgument)
    		return super.getAllFields(ct);
        return null;
    }

    public List<Method> getMethods(ClassType ct) {
    	if (ct instanceof ArrayType) {
        	// not cached
    		ArrayList<Method> res = new ArrayList<Method>(1);
    		ArrayType at = (ArrayType)ct;
    		res.add(at.getArrayCloneMethod());
    		return res;
    	}
    	List<Method> res;
    	if (ct instanceof ParameterizedType) {
    		ParameterizedType pt = (ParameterizedType)ct;
    		List<Method> temp = pt.getGenericType().getMethods();
    		res = new ArrayList<Method>(temp.size());
    		for (Method m : temp) {
    			Method pm = m;
    			if (containsTypeParameter(m))
    				pm = new ParameterizedMethod(m, pt, this);

    			// TODO 0.90 handling somewhere else ?
    			// check: is return type an inner type?
    			Type ret = m.getReturnType(); 
    			if (ret instanceof ClassType && ((ClassType)ret).isInner()) {
    				// TODO 0.92 need to carry over type parameters from ct to m now... But how ?
    				//ClassType cc = (ClassType)ret;
    			}
    			res.add(pm);
    		}
    		return res;
    	}
    	if (ct instanceof ErasedType) {
    		ErasedType et = (ErasedType)ct;
    		List<Method> methods = et.getGenericType().getMethods();
    		res = new ArrayList<Method>(methods.size());
    		for (Method m : methods) {
    			// only non-static methods are erased!
    			if (m.isStatic())
    				res.add(m);
    			else
    				res.add(new ErasedMethod(m, this));
    		}
    		return res;
    	}
        if (ct instanceof CapturedTypeArgument) {
        	// TODO 0.90 !!!
        	TypeArgument ta = ((CapturedTypeArgument)ct).getTypeArgument();
        	if (ta.getWildcardMode() == WildcardMode.Any
        			|| ta.getWildcardMode() == WildcardMode.Super) {
        		// special handling required, but it's done in getSupertypes...
        		// None therefore...
        		return Collections.<Method>emptyList();
        	}
        	return getBaseType(ta).getMethods();
        }
        if (ct instanceof IntersectionType) {
        	return null; // no locally defined methods; inherits methods from superclass only.
        }
    	assert ct == getNameInfo().getNullType() : ct;
        return null;
    }

    public List<Method> getAllMethods(ClassType ct) {
        if (ct instanceof IntersectionType 
    			|| ct instanceof ParameterizedType 
    			|| ct instanceof ArrayType
    			|| ct instanceof ErasedType
    			|| ct instanceof CapturedTypeArgument)
    		return super.getAllMethods(ct);
        return null;
    }

    public List<Constructor> getConstructors(ClassType ct) {
    	if (ct instanceof ParameterizedType) {
    		ParameterizedType pt = (ParameterizedType)ct;
    		List<? extends Constructor> temp = pt.getGenericType().getConstructors();
    		List<Constructor> res = new ArrayList<Constructor>(temp.size());
    		for (Constructor c : temp) {
    			if (getServiceConfiguration().getSourceInfo().containsTypeParameter(c))
    				res.add(new ParameterizedConstructor(c, pt, this));
    			else 
    				res.add(c);
    		}
    		return res;
    	}
    	if (ct instanceof ErasedType) {
    		ErasedType et = (ErasedType)ct;
    		List<? extends Constructor> cons = et.getGenericType().getConstructors();
    		List<Constructor> res = new ArrayList<Constructor>(cons.size());
    		for (Constructor m : cons) {
    			res.add(new ErasedConstructor(m, this));
    		}
    		return res;
    	}
    	if (ct instanceof ArrayType) {
    		return Collections.emptyList();
    	}
    	if (ct instanceof CapturedTypeArgument) {
    		return null;
    	}
        // valid for NullType
    	assert ct == getNameInfo().getNullType();
        return null;
    }

    public ClassType getContainingClassType(Member m) {
        if (m instanceof DefaultConstructor || m instanceof ImplicitEnumMethod) {
            return m.getContainingClassType();
        }
        // TODO valid for ...
        return null;
    }

    public List<Type> getSignature(Method m) {
    	if (m instanceof ImplicitEnumValueOf) {
    		ArrayList<Type> tal = new ArrayList<Type>(1);
        	tal.add(getServiceConfiguration().getNameInfo().getJavaLangString());
        	return tal;
    	} 
        if (m instanceof DefaultConstructor || m instanceof ArrayCloneMethod || m instanceof ImplicitEnumValues)
        	return Collections.<Type>emptyList();
		List<Type> res = methodToSig.get(m);
		
		if (res != null)
			return res;
		
    	if (m instanceof ParameterizedMethod) {
    		ParameterizedMethod pm = (ParameterizedMethod)m;
    		res = replaceAllTypeParametersWithArgs(pm.getGenericMethod().getSignature(), 
    				pm.getContainingClassType().getDefinedTypeParameters(), 
    				pm.getContainingClassType().getAllTypeArgs());
    	} else if (m instanceof ErasedMethod) {
    		res = eraseTypes(((ErasedMethod)m).getGenericMethod().getSignature());
    	} else if (m instanceof ResolvedGenericMethod) {
    		ResolvedGenericMethod rgm = (ResolvedGenericMethod)m;
    		res = replaceAllTypeParameters(rgm.getGenericMethod().getSignature(), 
    				rgm.getGenericMethod().getTypeParameters(), 
    				rgm.getReplacementType());
        }
		methodToSig.put(m, res);
		return res;
    }

    // always the same per-service! 
    // TODO if the types actually change, what then?? What when transforming the JDK??
    private List<ClassType> enumValueOfExceptions = null;
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ClassType> getExceptions(Method m) {
    	if (m instanceof ImplicitEnumValueOf) {
    		if (enumValueOfExceptions == null) {
    		    // since list is not visible as mutable, can cache result here.  
    			enumValueOfExceptions = new ArrayList<ClassType>(2);
    			enumValueOfExceptions.add(getNameInfo().getClassType("java.lang.IllegalArgumentException"));
    			enumValueOfExceptions.add(getNameInfo().getClassType("java.lang.NullPointerException"));
    			enumValueOfExceptions = Collections.unmodifiableList(enumValueOfExceptions);
    	    }
    		return enumValueOfExceptions;
    	} else if (m instanceof ParameterizedMethod) {
    		ParameterizedMethod pm = (ParameterizedMethod)m;
    		List<ClassType> res = methodToExceptions.get(pm); 
    		if (res == null) {
    			res = replaceAllTypeParametersWithArgs(pm.getGenericMethod().getExceptions(), 
    					pm.getContainingClassType().getDefinedTypeParameters(), 
    					pm.getContainingClassType().getAllTypeArgs());
    			methodToExceptions.put(pm, res);
    		}
    		return res;
    	} else if (m instanceof ErasedMethod) {
    		return eraseTypes(((ErasedMethod)m).getGenericMethod().getExceptions());
    	} else if (m instanceof ResolvedGenericMethod) {
    		ResolvedGenericMethod rgm = (ResolvedGenericMethod)m;
    		// The following cast is effectively a cast from List<Type> to List<ClassType> but it's ok because in this context, there cannot
    		// be any non-ClassTypes.
        	return (List)replaceAllTypeParameters(rgm.getGenericMethod().getExceptions(),
        			rgm.getGenericMethod().getTypeParameters(), rgm.getReplacementType());
        }
    	
        // valid for Default Constructor and values() in enums.
    	assert m instanceof DefaultConstructor || m instanceof ImplicitEnumValues;
        return Collections.emptyList();
    }
    
    public Type getReturnType(Method m) {
    	if (m instanceof DummyGetClassMethod) {
    		// build this together
    		Type returnType = methodToReturnType.get(m);
    		if (returnType == null) {
    			ClassType ct = getNameInfo().getJavaLangClass();
    			TypeArgument ta = new ResolvedTypeArgument(
    					WildcardMode.Extends, 
    					(ClassType)eraseType(((DummyGetClassMethod)m).getParentClass()), null, ct.getTypeParameters().get(0));
    			ArrayList<TypeArgument> tas = new ArrayList<TypeArgument>(1);
    			tas.add(ta);
    			returnType = getParameterizedType(ct, tas);
    			methodToReturnType.put(m, returnType);
    		}
    		return returnType;
    	} else if (m instanceof ImplicitEnumValueOf) {
    		return m.getContainingClassType();
    	} else if (m instanceof ImplicitEnumValues) {
    		return getServiceConfiguration().getNameInfo().createArrayType(m.getContainingClassType());
    	} else if (m instanceof ParameterizedMethod) {
    		Type res = methodToReturnType.get(m);
    		if (res == null) {
    			ParameterizedMethod pm = (ParameterizedMethod)m;
    			Type genRet = pm.getGenericMethod().getReturnType();
    			res = replaceAllTypeParametersWithArgs(genRet, 
    					pm.getContainingClassType().getDefinedTypeParameters(), 
    					pm.getContainingClassType().getAllTypeArgs(),
    					pm.getGenericMethod().getTypeParameters());
    			methodToReturnType.put(m, res);
    		}
    		return res;
    	} else if (m instanceof ArrayCloneMethod) {
    		// JLS §10.7 (2nd / 3rd edition, respectively)
    		if (getServiceConfiguration().getProjectSettings().java5Allowed()) {
    			ArrayCloneMethod acm = (ArrayCloneMethod)m;
    			return acm.getContainingClassType();
    		}
    		else { 
    			return getServiceConfiguration().getNameInfo().getJavaLangObject();
    		}
    	} else if (m instanceof ErasedMethod) {
    		ErasedMethod em = (ErasedMethod)m;
    		// erase ONLY if the enclosing type is "erasable" itself.
    		ClassType gt = ((ErasedType)em.getContainingClassType()).getGenericType(); 
    		if (isErasable(gt)) {
    			return eraseType(em.getGenericMethod().getReturnType());
    		} else {
    			return em.getGenericMethod().getReturnType();
    		}
    	} else if (m instanceof ResolvedGenericMethod) {
    		ResolvedGenericMethod rgm = (ResolvedGenericMethod)m;
    		Type res = rgm.getGenericMethod().getReturnType();
    		if (res == null) {
    			// generic method, but void as return type. This is valid,
    			// e.g. java.util.Arrays.sort()
    			return null;
    		}
    		res = replaceAllTypeParameters(res, rgm.getGenericMethod().getTypeParameters(),
    				rgm.getReplacementType());
    		return res;
    	}
        // valid for Default Constructor
    	assert m instanceof DefaultConstructor;
        return null;
    }
    
    private boolean isErasable(ClassType ct) {
    	if (ct.getTypeParameters() != null && ct.getTypeParameters().size() > 0)
    		return true;
    	if (ct.getContainingClassType() != null)
    		return isErasable(ct.getContainingClassType());
    	return false;
    }
	
    /**
     * ParameterizedType but with different equals() implementation. Can't reuse TypeArgumentDeclarations, unfortunately...
     * @author Tobias Gutzmann
     *
     */
    private class WrappedParameterizedType {
    	final ParameterizedType pt;
    	
    	WrappedParameterizedType(ParameterizedType pt) {
    		this.pt = pt;
    	}
    	
    	@Override
    	public boolean equals(Object o) {
    		if (o == this)
    			return true;
    		if (!(o instanceof ParameterizedType))
    			return false;
    		ParameterizedType opt = (ParameterizedType)o;
    		if (pt.getProgramModelInfo() != opt.getProgramModelInfo())
    			return false; // TODO multiple instances, ok, but should this happen anyway ??
    		if (pt.getGenericType() != opt.getGenericType())
    			return false;
    		if (pt.getEnclosingType() != opt.getEnclosingType())
    			return false;
    		// check type arguments. The two types MUST have the same number of type arguments!
    		List<? extends TypeArgument> ta1 = opt.getAllTypeArgs();
    		List<? extends TypeArgument> ta2 = pt.getAllTypeArgs();
    		if (ta1.size() != ta2.size()) {
    			return false;
    		}
    		for (int i = 0; i < ta1.size(); i++) {
    			if (ta1.get(i) == ta2.get(i))
    				continue;
    			TypeArgument _1 = ta1.get(i);
    			TypeArgument _2 = ta2.get(i);
    			if (_1 instanceof TypeArgumentDeclaration || _2 instanceof TypeArgumentDeclaration)
    				return false; // Can't reuse.
    			if (_1 instanceof CapturedTypeArgument && ((CapturedTypeArgument)_1).getTypeArgument() instanceof TypeArgumentDeclaration)
    				return false; // Can't reuse.
    			if (_2 instanceof CapturedTypeArgument && ((CapturedTypeArgument)_2).getTypeArgument() instanceof TypeArgumentDeclaration)
    				return false; // Can't reuse.
    			if (!ta1.get(i).semanticalEquality(ta2.get(i))) {
    				return false;
    			}
    		}
    		return true;    	
    	}
    }
    
	private HashMap<WrappedParameterizedType, WrappedParameterizedType> ptypes = new HashMap<WrappedParameterizedType, WrappedParameterizedType>();
	
	public ParameterizedType getParameterizedType(ClassType genericType, List<? extends TypeArgument> typeArgs) {
		return getParameterizedType(genericType, typeArgs, null);
	}

	public ParameterizedType getParameterizedType(ClassType innerGenericType, 
		    List<? extends TypeArgument> typeArgs,
			ParameterizedType enclosingType) {
		if (innerGenericType instanceof ParameterizedType) {
			ParameterizedType innerPT = (ParameterizedType)innerGenericType;
			if (innerPT.getEnclosingType() != null) {
				return getParameterizedType(innerPT.getGenericType(), typeArgs, innerPT.getEnclosingType());
			}
		}
		WrappedParameterizedType newRes = new WrappedParameterizedType(new ParameterizedType(innerGenericType, typeArgs, enclosingType, this));
		WrappedParameterizedType oldRes = ptypes.get(newRes);
		if (oldRes != null)
			return oldRes.pt;
		ptypes.put(newRes, newRes);
		return newRes.pt;
	}
	
	
	// TODO steered by SourceInfo for now...
	public void reset() {
		super.reset();
		methodToReturnType.clear();
		methodToExceptions.clear();
		methodToSig.clear();
		ptypes.clear();
	}
	
	@Override
	public Method getJavaLangObjectGetClass() {
		if (javaLangObjectGetClass == null) {
	        javaLangObjectGetClass = serviceConfiguration.getNameInfo().getMethods("java.lang.Object.getClass()").get(0);
		}
		return javaLangObjectGetClass;
	}
}
