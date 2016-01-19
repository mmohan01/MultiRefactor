// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import recoder.ServiceConfiguration;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.Field;
import recoder.abstraction.Member;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.Type;
import recoder.abstraction.TypeParameter;
import recoder.bytecode.AnnotationUseInfo;
import recoder.bytecode.ByteCodeElement;
import recoder.bytecode.ClassFile;
import recoder.bytecode.ConstructorInfo;
import recoder.bytecode.FieldInfo;
import recoder.bytecode.MemberInfo;
import recoder.bytecode.MethodInfo;
import recoder.bytecode.TypeArgumentInfo;
import recoder.bytecode.TypeParameterInfo;
import recoder.convenience.Format;
import recoder.util.Debug;


public class DefaultByteCodeInfo extends DefaultProgramModelInfo implements ByteCodeInfo {

    /*
     * We reuse the class type cache for the class file cache entries. We can do
     * that as we create cache entries during registration of class files and
     * registration comes before any query. There is a cache entry for a class
     * file if and only if it has been registered. Therefore, the class file
     * cache may not be reset, which would happen after a call to reset().
     * However, the byte code info should never have to be reset as long as
     * we do not change byte code.
     * TODO necessary to do when retrotranslator is run !?
     */
    static class ClassFileCacheEntry extends ClassTypeCacheEntry {
        // could be extended by containment links?
    }

    /**
     * Containment relation. This could be made internal part of the
     * ByteCodeInfo hierarchy.
     */
    private final Map<ProgramModelElement, ClassTypeContainer> element2container = new HashMap<ProgramModelElement, ClassTypeContainer>(256);

    /**
     * Member and inner type relation. This could be made part of the NameInfo
     * for packages and part of the ClassFile or the ClassFileCacheEntry.
     */
    private final Map<ClassTypeContainer, ArrayList<ClassType>> containedTypes = new HashMap<ClassTypeContainer, ArrayList<ClassType>>(32);

    /** signature caching */
    private final Map<Method, List<Type>> method2signature = new HashMap<Method, List<Type>>(128);

    /**
     * @param config
     *            the configuration this services becomes part of.
     */
    public DefaultByteCodeInfo(ServiceConfiguration config) {
        super(config);
    }

    private ByteCodeElement getByteCodeElement(ProgramModelElement pme) {
        if (pme instanceof ByteCodeElement) {
            return (ByteCodeElement) pme;
        } else {
            return null;
        }
    }

    public final ClassFile getClassFile(ClassType ct) {
        return (ClassFile) getByteCodeElement(ct);
    }

    public final MethodInfo getMethodInfo(Method m) {
        return (MethodInfo) getByteCodeElement(m);
    }

    public final ConstructorInfo getConstructorInfo(Constructor c) {
        return (ConstructorInfo) getByteCodeElement(c);
    }

    public final FieldInfo getFieldInfo(Field f) {
        return (FieldInfo) getByteCodeElement(f);
    }

    private TypeParameterInfo findTypeParameter(MemberInfo mi, String name) {
    	TypeParameter result = null;
    	if (mi instanceof MethodInfo) {
			if (((MethodInfo)mi).getTypeParameters() != null) {
				for (TypeParameter tp : ((MethodInfo)mi).getTypeParameters()) {
					if (name.equals(tp.getName())) {
						result = tp;
						break;
					}
				}
			}
		}
    	ClassType container = mi.getContainingClassType();
		FOUND: while (result == null && container != null) {
    		List<? extends TypeParameter> tpl = container.getTypeParameters();
    		if (tpl != null) {
    			for (TypeParameter tp : tpl) {
    				if (name.equals(tp.getName())) {
    					result = tp;
    					break FOUND;
    				}
    			}
    		}
    		container = container.getContainingClassType();
		}
		return (TypeParameterInfo)result;
    }
    
    public Type getType(ByteCodeElement bce) {
    	if (bce instanceof ConstructorInfo) {
    		return null; 
    	}
    	Type result = null;
    	String typeName = bce.getTypeName();
    	if (typeName.equals("void"))
    		return null;
    	if (bce instanceof MemberInfo) {
    		MemberInfo mi = (MemberInfo)bce;
    		int idx = typeName.indexOf('[');
    		int dim = idx == -1 ? 0 : (typeName.length() - idx) / 2;
    		String baseTypeName = idx == - 1 ? typeName : typeName.substring(0, idx); 

    		// TODO 0.95 should be changed in  
    		if (mi.isTypeVariable()) {
        		result = findTypeParameter(mi, baseTypeName);
        		if (result != null) {
        			while (dim-- > 0)
        				result = result.createArrayType();
        		}
    		}
    		
    	}
    	if (result == null) 
    		result = getNameInfo().getType(typeName);
    	if (bce instanceof MemberInfo) {
    		List<TypeArgumentInfo> typeArgs;
    		if (bce instanceof MethodInfo)
    			typeArgs = ((MethodInfo)bce).getTypeArgumentsForReturnType();
    		else
    			typeArgs = ((FieldInfo)bce).getTypeArguments();
    		if (typeArgs != null && typeArgs.size() != 0) {
    			if (result instanceof ArrayType) {
    				result = makeParameterizedArrayType((ArrayType)result, typeArgs);
    			} else {
        			// can safely cast, result must not be primitive type if it has type arguments
    				result = getServiceConfiguration().getImplicitElementInfo().getParameterizedType((ClassType)result, typeArgs);
    			}
    		}
    	}
    	if (result == null) {
   			getErrorHandler().reportError(
                    new UnresolvedBytecodeReferenceException(typeName, bce.getFullName()));
        	result = getServiceConfiguration().getNameInfo().getUnknownType();
    	}
    	result = checkEraseRequired(result);
    	return result;
    }

    private Type checkEraseRequired(Type t) {
    	int dim = 0;
    	while (t instanceof ArrayType) {
    		t = ((ArrayType)t).getBaseType();
    		dim++;
    	}
    	if (java5Allowed() && t instanceof ClassType && !(t instanceof ArrayType) && !(t instanceof ParameterizedType)){
        	ClassType ct = (ClassType)t;
        	if (ct.getTypeParameters() != null && ct.getTypeParameters().size() > 0) {
        		// The type reference has no type arguments, but the type has type parameters.
        		t = ((ClassType)t).getErasedType();
        	}
        }
    	while (dim-- > 0) {
    		t = t.createArrayType();
    	}
    	return t;
    }
    
    public Type getType(ProgramModelElement pme) {
        Debug.assertNonnull(pme);
        Type result = null;
        if (pme instanceof Type) {
            result = (Type) pme;
        } else {
            ByteCodeElement bci = getByteCodeElement(pme);
            if (bci == null) {
                result = pme.getProgramModelInfo().getType(pme);
            } else {
                result = getType(bci);
            }
        }
        return result;
    }

    public Package getPackage(ProgramModelElement pme) {
        Debug.assertNonnull(pme);
        ProgramModelElement x = element2container.get(pme);
        while ((x != null) && !(x instanceof Package)) {
            x =element2container.get(x);
        }
        return (Package) x;
    }

    public List<? extends ClassType> getTypes(ClassTypeContainer ctc) {
        Debug.assertNonnull(ctc);
        if (ctc instanceof ByteCodeElement) {
            List<ClassType> ctl = containedTypes.get(ctc);
            if (ctl == null)
            	return Collections.emptyList();
            return new ArrayList<ClassType>(ctl);
        } else {
            return ctc.getProgramModelInfo().getTypes(ctc);
        }
    }

    public ClassTypeContainer getClassTypeContainer(ClassType ct) {
        return element2container.get(ct);
    }

    public List<ClassType> getSupertypes(ClassType ct) {
        Debug.assertNonnull(ct);
        // TODO cache / register (?)
        if (ct instanceof TypeParameterInfo) {
        	TypeParameterInfo tp = (TypeParameterInfo)ct;
        	List<ClassType> res;
        	res = new ArrayList<ClassType>(tp.getBoundCount());
        	for (int i = 0; i < tp.getBoundCount(); i++) {
        		List<TypeArgumentInfo> boundTAs = tp.getBoundTypeArguments(i);
        		ClassType boundCT = getNameInfo().getClassType(tp.getBoundName(i)); 
        		if (boundTAs != null && boundTAs.size() > 0) {
        			// may be an inner type! TypeArgs for the most inner types have to be set.
        			// outer types may be raw under circumstances (in case of static member types)
        			// example, for type X<T>.Y<U>: X<String>.Y is never allowed. X.Y<String> may be allowed.
        			ClassType finalCT = makeParameterizedInnerTypeRec(boundCT, boundTAs);
        			res.add(finalCT);
        		} else {
        			res.add(boundCT);
        		}
        	}
        	return res;
        }
        ClassFile cf = getClassFile(ct);
        if (cf == null) {
            return ct.getProgramModelInfo().getSupertypes(ct);
        }
        ClassFileCacheEntry cfce = (ClassFileCacheEntry) classTypeCache.get(ct);
        Debug.assertNonnull(cfce); // created during registration
        Debug.assertNonnull(cfce.supertypes); // created during registration
        return new ArrayList<ClassType>(cfce.supertypes); // copy!!
    }

    private ParameterizedType makeParameterizedInnerTypeRec(ClassType boundCT,
			List<TypeArgumentInfo> boundTAs) {
    	ImplicitElementInfo iei = getServiceConfiguration().getImplicitElementInfo();
    	int myTPCount = boundCT.getTypeParameters() == null ? 0 : boundCT.getTypeParameters().size(); 
    	if (myTPCount == boundTAs.size()) {
        	return iei.getParameterizedType(boundCT, boundTAs);
    	}
    	List<TypeArgumentInfo> outerTAs = boundTAs.subList(0, boundTAs.size()-myTPCount);
    	List<TypeArgumentInfo> myTAs = boundTAs.subList(boundTAs.size()-myTPCount, boundTAs.size());
    	ParameterizedType outerCT =  makeParameterizedInnerTypeRec(boundCT.getContainingClassType(), outerTAs);
    	return iei.getParameterizedType(boundCT, myTAs, outerCT);
	}

	public List<? extends Field> getFields(ClassType ct) {
        Debug.assertNonnull(ct);
        if (ct instanceof ClassFile) {
            return ((ClassFile) ct).getFieldInfos();
            // return ((ClassFileCacheEntry)classTypeCache.get(ct)).fields;
        } else {
            return ct.getProgramModelInfo().getFields(ct);
        }
    }

    public List<Method> getMethods(ClassType ct) {
        Debug.assertNonnull(ct);
        if (ct instanceof ClassFile) {
            return new ArrayList<Method>(((ClassFile) ct).getMethodInfos());
            // return ((ClassFileCacheEntry)classTypeCache.get(ct)).methods;
        } else {
            return ct.getProgramModelInfo().getMethods(ct);
        }
    }

    public List<? extends Constructor> getConstructors(ClassType ct) {
        Debug.assertNonnull(ct);
        if (ct instanceof ClassFile) {
            return ((ClassFile) ct).getConstructorInfos();
            // return
            // ((ClassFileCacheEntry)classTypeCache.get(ct)).constructors;
        } else {
            return ct.getProgramModelInfo().getConstructors(ct);
        }
    }

    public ClassType getContainingClassType(Member m) {
        return (ClassType) element2container.get(m);
    }

    public List<Type> getSignature(Method m) {
        Debug.assertNonnull(m);
        List<Type> result;
        MethodInfo mi = getMethodInfo(m);
        if (mi == null) {
            result = m.getProgramModelInfo().getSignature(m);
        } else {
            String[] ptypes = mi.getParameterTypeNames();
            int pcount = ptypes.length;
            if (pcount == 0) {
                return Collections.emptyList(); // return directly!
            } else {
                result = method2signature.get(m);
                if (result == null) {
                    NameInfo ni = getNameInfo();
                    List<Type> res = new ArrayList<Type>(pcount);
                    // TODO 0.95 - this shouldn't be necessary - 
                    // set in MethodInfo if it's a type parameter, it's information 
                    // available in bytecode!
                    for (int i = 0; i < pcount; i++) {
                    	Type t = null;
                   		String basename = ptypes[i];
                   		int dim;
                   		if ((dim = basename.indexOf('[')) != -1) // for now, dim isn't the real dimension.
                   			basename = basename.substring(0, dim);
                   		List<? extends TypeParameter> tpl;
                   		boolean checkClassTypeParameters = true;
                   		// method's type parameters
                   		// TODO this is copy&paste... use only one list!
                   		tpl = mi.getTypeParameters();
                   		if (tpl != null) {
                   			for (TypeParameter tp : tpl) {
                   				if (basename.equals(tp.getName())) {
                   					t = tp;
                   					if (dim != -1) {
                   						dim = (ptypes[i].length() - dim) / 2;
                   						while(dim != 0) {
                   							t = ni.createArrayType(tp);
                   							dim--;
                   						}
                   					}
                   					checkClassTypeParameters = false;
                   					break;
                   				}
                   			}
                   		}
                   		// class's type parameters
                   		if (checkClassTypeParameters) {
                   			ClassType container = mi.getContainingClassType(); 
                   			OUTER: do {
                   				tpl = container.getTypeParameters();
                   				if (tpl != null) {
                   					for (TypeParameter tp : tpl) {
                   						if (basename.equals(tp.getName())) {
                   							t = tp;
                   							if (dim != -1) {
                   								dim = (ptypes[i].length() - dim) / 2;
                   								while(dim != 0) {
                   									t = ni.createArrayType(tp);
                   									dim--;
                   								}
                   							}
                   							break OUTER;
                   						}
                   					}
                   				}
                   				container = container.getContainingClassType();
                   			} while (container != null);
                   		}
                   		if (t == null)
                   			t = ni.getType(ptypes[i]);
                   		if (t == null) {
                   			getErrorHandler().reportError(
                                    new UnresolvedBytecodeReferenceException(ptypes[i], m.getFullName()));
                        	t = getServiceConfiguration().getNameInfo().getUnknownType();
                   		}
                   		if (mi.getTypeArgumentsForParam(i) != null
                   				&& mi.getTypeArgumentsForParam(i).size() > 0) {
                   			if (t instanceof ArrayType) {
                   				t = makeParameterizedArrayType((ArrayType)t, mi.getTypeArgumentsForParam(i));
                   			} else {
                   				t = getServiceConfiguration().getImplicitElementInfo().getParameterizedType((ClassType)t, mi.getTypeArgumentsForParam(i));
                   			}
                   		}
                    	res.add(t);
                    }
                    result = res;
                    method2signature.put(m, result);
                }
            }
        }
        return new ArrayList<Type>(result);
    }

    public List<ClassType> getExceptions(Method m) {
        Debug.assertNonnull(m);
        MethodInfo mi = getMethodInfo(m);
        if (mi == null) {
            return m.getProgramModelInfo().getExceptions(m);
        } else {
            String[] etypes = mi.getExceptionsInfo();
            if (etypes == null || etypes.length == 0) {
                return Collections.emptyList();
            }
            List<ClassType> res = new ArrayList<ClassType>(etypes.length);
            NameInfo ni = getNameInfo();
            for (String exc : etypes) {
            	ClassType excT = findTypeParameter(mi, exc);
            	if (excT == null)
            		excT = ni.getClassType(exc);
            	if (excT == null) {
           			getErrorHandler().reportError(
                            new UnresolvedBytecodeReferenceException(exc, mi.getFullName() + " (a declared exception)"));
                	excT = getServiceConfiguration().getNameInfo().getUnknownClassType();
            	}
                res.add(excT);
            }
            return res;
        }
    }

    public Type getReturnType(Method m) {
        return getType(m);
    }

    public void register(ClassFile cf) {
        Debug.assertNonnull(cf);
        ClassFileCacheEntry cfce = (ClassFileCacheEntry) classTypeCache.get(cf);
        if (cfce != null) {
            // already registered
            return;
        }
        
        if (cf.getName().equals("package-info")) {
        	// this is not really a type, but compiled package annotations. Skip this.
        	return;
        }
        
        cfce = new ClassFileCacheEntry();
        classTypeCache.put(cf, cfce);
        String classname = cf.getBinaryName();
        NameInfo ni = getNameInfo();

        // register this class name and set model info to avoid cycle
        cf.setProgramModelInfo(this);
        ni.register(cf);

        // get outer scope: package, or outer class
        int ldp = classname.lastIndexOf('$');
        
        // TODO 0.95 - this is a quick fix. Clean up!!
        ClassTypeContainer ctc;
        if (cf.getEnclosingMethod() != null) {
        	ctc = null;
        	String s = cf.getEnclosingMethod();
        	ClassFile clazz = (ClassFile)getNameInfo().getClassType(s.substring(0, s.lastIndexOf('.')).replace('/', '.').replace('$', '.'));
        	String mname = s.substring(s.lastIndexOf('.')+1, s.lastIndexOf('('));
        	FIND_METHOD: for (Method mi : clazz.getMethods()) {
        		if (!mi.getName().equals(mname))
        			continue;
        		// TODO 0.95 properly check signature...
        		String msig = s.substring(s.indexOf('('));
        		org.objectweb.asm.Type[] sigTypes = org.objectweb.asm.Type.getArgumentTypes(msig);
        		String[] sig = new String[sigTypes.length + 1];
        		for (int i = 0; i < sigTypes.length; i++)
        			sig[i] = sigTypes[i].getClassName();
        		sig[sigTypes.length] = org.objectweb.asm.Type.getReturnType(s).getClassName();
        		if (sig.length-1 != mi.getSignature().size())
        			continue; 
        		for (int i = 0; i < sig.length-1; i++) {
        			if (!sig[i].equals(mi.getSignature().get(i).getFullName()))
        				continue FIND_METHOD;
        		}
        		if (mi.getReturnType() != null && !mi.getReturnType().getFullName().equals(sig[sig.length-1]))
        			continue; // might be a bridge method!
        		
        		if (ctc != null)
        			throw new UnsupportedOperationException("Two methods with same name, one has local/anonymous type - not supported yet!");
        		ctc = mi;
        	}
        } else if (ldp >= 0) {
            // we are an inner class
            String outerClassName = classname.substring(0, ldp);
            classname = classname.substring(ldp + 1);
            // hint for name info: we expect a class file here?
            ClassType outerClass = ni.getClassType(outerClassName.replace('$', '.'));
            if (outerClass == null) {
                // shit happens - here in the form of _local_ classes;
                // these are translated into mother$1$child form, but
                // there is no class file "mother$1".
                do {
                    ldp = outerClassName.lastIndexOf('$');
                    if (ldp >= 0) {
                        outerClassName = outerClassName.substring(0, ldp);
                        if (outerClassName.length() > 0) {
                            outerClass = ni.getClassType(outerClassName.replace('$', '.'));
                        }
                    }
                } while (ldp >= 0 && outerClass == null);
            }
            if (outerClass instanceof ClassFile) {
                register((ClassFile) outerClass);
            } else {
                Debug.log("Found a non-ClassFile outer class of " + classname + ":"
                        + Format.toString("%c %N", outerClass));
            }

            // set containment
            ctc = outerClass;
        } else {
            // find package, or create a new one, respectively
            ldp = classname.lastIndexOf('.');
            String packageName = "";
            if (ldp != -1) {
                packageName = classname.substring(0, ldp);
            }
            // set containment link
            ctc = ni.createPackage(packageName);
        }

        // register class in container
        element2container.put(cf, ctc);

        if (ctc instanceof ByteCodeElement) {
            ArrayList<ClassType> ctl = containedTypes.get(ctc);
            if (ctl == null) {
                containedTypes.put(ctc, ctl = new ArrayList<ClassType>());
            }
            ctl.add(cf);
            ctl.trimToSize();
        }

        // register fields
        List<? extends Field> fl = cf.getFieldInfos();
        for (int i = 0, s = fl.size(); i < s; i++) {
            Field f = fl.get(i);
            f.setProgramModelInfo(this);
            element2container.put(f, cf);
            ni.register(f);
        }

        // register methods
        List<? extends Method> ml = cf.getMethodInfos();
        for (int i = 0, s = ml.size(); i < s; i++) {
            Method m = ml.get(i);
            m.setProgramModelInfo(this);
            element2container.put(m, cf);
        }

        // register constructors
        List<? extends Constructor> cl = cf.getConstructorInfos();
        for (int i = 0, s = cl.size(); i < s; i++) {
            Constructor c = cl.get(i);
            c.setProgramModelInfo(this);
            element2container.put(c, cf);
        }
        if (cl.isEmpty() && !cf.isInterface() && !cf.isEnumType() && Character.isJavaIdentifierStart(cf.getName().charAt(0))) {
            Debug.log("No constructor defined in " + cf.getFullName());
            // serviceConfiguration.getImplicitElementInfo().getDefaultConstructor(cf)
        }

        // register inner classes, set containment links
        String[] innerClasses = cf.getInnerClassNames();
        if (innerClasses != null) {
            String fullName = cf.getFullName();
            for (int i = 0; i < innerClasses.length; i++) {
                String cn = innerClasses[i];
                if (cn.equals(fullName)) {
                    continue;
                    // inner classes refer to themselves as inner classes
                    // STUPID
                }
                ni.getClassType(cn); // bad, bad side-effect programming ;)
                /*
                 * Remark by T.Gutzmann:
                 * The inner class info is meant for the sole purpose of type resolving:
                 * an inner class and a package-level class may have the same name
                 * (although that violates naming conventions). There are rules for 
                 * resolving this problem on source code level; the information required
                 * are not available in bytecode any more, except in the inner class info ;-)
                 * As of Recoder 0.80, references to inner classes of other types are filtered out
                 * by the bytecode parser.
                 * 
                 ** It is actually possible to receive a non-classfile here! The
                 ** semantics of inner class chunks in class files seems to be a
                 ** bit weird.
                 ** com.sun.java.swing.plaf.windows.WindowsLookAndFeel.LazyFileChooserIcon
                 ** contains an inner class link to
                 ** javax.swing.UIDefaults.LazyValue, even though it is just a
                 ** subtype. Strange.
                 */

            }
        }

        // create supertypes
        String sname = cf.getSuperClassName();
        String[] inames = cf.getInterfaceNames();
        List<ClassType> list = new ArrayList<ClassType>(inames.length + 2);
        if (sname != null) {
            ClassType ct = ni.getClassType(sname);
            if (ct == null) {
                getErrorHandler().reportError(
                        new MissingClassFileException("Unknown byte code supertype " + sname + " in class "
                                + cf.getFullName(), sname));

            } else {
            	List<TypeArgumentInfo> tais = cf.getSuperClassTypeArguments();
            	// TODO 0.90
            	if (tais != null && tais.size() > 0)
            		ct = getServiceConfiguration().getImplicitElementInfo().getParameterizedType(ct, tais);
                list.add(ct);
            }
        }
        for (int i = 0; i < inames.length; i++) {
            String iname = inames[i];
            ClassType ct = ni.getClassType(iname);
            if (ct == null) {
                getErrorHandler().reportError(
                        new MissingClassFileException("Unknown byte code supertype " + iname + " in class "
                                + cf.getFullName(), iname));

            } else {
            	List<TypeArgumentInfo> tais = cf.getSuperInterfaceTypeArguments(i);
            	// TODO 0.90
            	if (tais != null && tais.size() > 0)
            		ct = getServiceConfiguration().getImplicitElementInfo().getParameterizedType(ct, tais);
                list.add(ct);
            }
        }
        if (list.isEmpty()) {
        	// TODO not necessary? java.lang.Object is automatically inserted by compiler?
            ClassType jlo = ni.getJavaLangObject();
            if (cf != jlo) {
                list.add(jlo);
            }
        }
        cfce.supertypes = list;
        for (int i = 0; i < list.size(); i += 1) {
            registerSubtype(cf, list.get(i));
        }
    }

	public Type getAnnotationType(AnnotationUseInfo au) {
		return getNameInfo().getType(au.getFullReferencedName());
	}
	
	void clear() {
		containedTypes.clear();
		element2container.clear();
		method2signature.clear();
	}
}