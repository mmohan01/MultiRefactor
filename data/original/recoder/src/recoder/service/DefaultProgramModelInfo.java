// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import recoder.AbstractService;
import recoder.ModelException;
import recoder.ServiceConfiguration;
import recoder.abstraction.AnnotationUse;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.DummyGetClassMethod;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.IntersectionType;
import recoder.abstraction.Member;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeArgument.CapturedTypeArgument;
import recoder.abstraction.TypeArgument.WildcardMode;
import recoder.abstraction.TypeParameter;
import recoder.bytecode.TypeArgumentInfo;
import recoder.bytecode.TypeParameterInfo;
import recoder.convenience.Naming;
import recoder.io.PropertyNames;
import recoder.java.NonTerminalProgramElement;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.expression.operator.New;
import recoder.list.generic.ASTList;
import recoder.util.Debug;

public abstract class DefaultProgramModelInfo extends AbstractService implements ProgramModelInfo {
		
    static class ClassTypeCacheEntry {
        List<ClassType> supertypes; // used in specialized services only

        Set<ClassType> subtypes;

        List<ClassType> allSupertypes;

        List<ClassType> allMemberTypes;

        List<Field> allFields;

        List<Method> allMethods;
    }

    final Map<ClassType, ClassTypeCacheEntry> classTypeCache = new HashMap<ClassType, ClassTypeCacheEntry>(256);

    /**
     * @param config
     *            the configuration this services becomes part of.
     */
    protected DefaultProgramModelInfo(ServiceConfiguration config) {
        super(config);
    }

    final ChangeHistory getChangeHistory() {
        return serviceConfiguration.getChangeHistory();
    }

    ErrorHandler getErrorHandler() {
        return serviceConfiguration.getProjectSettings().getErrorHandler();
    }

    final NameInfo getNameInfo() {
        return serviceConfiguration.getNameInfo();
    }

    final void updateModel() {
        getChangeHistory().updateModel();
    }

    /**
     * Internally used to register a subtype link.
     */
    void registerSubtype(ClassType subtype, ClassType supertype) {
        ProgramModelInfo pmi = supertype.getProgramModelInfo();
        if (pmi != this) {
            ((DefaultProgramModelInfo) pmi).registerSubtype(subtype, supertype);
        }
        ClassTypeCacheEntry ctce = classTypeCache.get(supertype);
        if (ctce == null) {
            classTypeCache.put(supertype, ctce = new ClassTypeCacheEntry());
        }
        if (ctce.subtypes == null) {
            ctce.subtypes = new HashSet<ClassType>();
        }
        ctce.subtypes.add(subtype);
    }

    /**
     * Internally used to remove a subtype link.
     */
    void removeSubtype(ClassType subtype, ClassType supertype) {
        ProgramModelInfo pmi = supertype.getProgramModelInfo();
        if (pmi != this) {
            ((DefaultProgramModelInfo) pmi).registerSubtype(subtype, supertype);
        }
        ClassTypeCacheEntry ctce = classTypeCache.get(supertype);
        if (ctce != null) {
            if (ctce.subtypes != null) {
                ctce.subtypes.remove(subtype);
            }
        }
    }

    public List<ClassType> getSubtypes(ClassType ct) {
    	if (ct == null) throw new NullPointerException();
        Debug.assertNonnull(ct);
        updateModel();
        ProgramModelInfo pmi = ct.getProgramModelInfo();
        if (pmi != this) {
            Debug.assertNonnull(pmi);
            return pmi.getSubtypes(ct);
        }
        ClassTypeCacheEntry ctce = classTypeCache.get(ct);
        if (ctce == null) {
            classTypeCache.put(ct, ctce = new ClassTypeCacheEntry());
        }
        if (ctce.subtypes == null) {
            return Collections.emptyList();
        }
        return new ArrayList<ClassType>(ctce.subtypes);
    }

    public List<ClassType> getAllSubtypes(ClassType ct) {
    	if (ct == null) throw new NullPointerException();
        updateModel();
        List<ClassType> ctl = new SubTypeTopSort().getAllTypes(ct);
        // begin at second entry - the top sort includes the input class
        ctl.remove(0);
        return ctl;
    }

    public List<ClassType> getAllSupertypes(ClassType ct) {
    	if (ct == null) 
    		throw new NullPointerException();
        updateModel();
        ProgramModelInfo pmi = ct.getProgramModelInfo();
        if (pmi != this) {
            Debug.assertNonnull(pmi);
            return pmi.getAllSupertypes(ct);
        }
        ClassTypeCacheEntry ctce = classTypeCache.get(ct);
        if (ctce == null) {
            classTypeCache.put(ct, ctce = new ClassTypeCacheEntry());
        }
        if (ctce.allSupertypes == null) {
            computeAllSupertypes(ct, ctce);
        }
        return new ArrayList<ClassType>(ctce.allSupertypes);
    }

    private void computeAllSupertypes(ClassType ct, ClassTypeCacheEntry ctce) {
        ctce.allSupertypes = new SuperTypeTopSort().getAllTypes(ct);
    }

    public List<Field> getAllFields(ClassType ct) {
        updateModel();
        ProgramModelInfo pmi = ct.getProgramModelInfo();
        if (pmi != this) {
            Debug.assertNonnull(pmi);
            return pmi.getAllFields(ct);
        }
        ClassTypeCacheEntry ctce = classTypeCache.get(ct);
        if (ctce == null) {
            classTypeCache.put(ct, ctce = new ClassTypeCacheEntry());
        }
        if (ctce.allFields == null) {
            computeAllFields(ct, ctce);
        }
        return new ArrayList<Field>(ctce.allFields);
    }

    private void computeAllFields(ClassType ct, ClassTypeCacheEntry ctce) {
        if (ctce.allSupertypes == null) {
            computeAllSupertypes(ct, ctce);
        }
        List<? extends ClassType> classes = ctce.allSupertypes;
        classes = removeErasedTypesFromList(classes);
        // if (classes == null) return null;
        int s = classes.size();
        ArrayList<Field> result = new ArrayList<Field>(s * 4); // simple heuristic
        int result_size = 0;
        for (int i = 0; i < s; i++) {
            ClassType c = classes.get(i);
            List<? extends Field> fl = c.getFields();
            if (fl == null) {
                continue;
            }
            int fs = fl.size();
            add_fields: for (int j = 0; j < fs; j++) {
                Field f = fl.get(j);
                if (isVisibleFor(f, ct)) {
                    String fname = f.getName();
                    for (int k = 0; k < result_size; k++) {
                        Field rf = result.get(k);
                        if (rf.getName() == fname) {
                            continue add_fields; // hidden by a field in a subclass
                        }
                    }
                    result.add(f);
                    result_size++;
                }
            }
        }
        result.trimToSize();
        ctce.allFields = result;
    }

    public List<Method> getAllMethods(ClassType ct) {
        updateModel();
        ProgramModelInfo pmi = ct.getProgramModelInfo();
        if (pmi != this) {
            Debug.assertNonnull(pmi);
            return pmi.getAllMethods(ct);
        }
        ClassTypeCacheEntry ctce = classTypeCache.get(ct);
        if (ctce == null) {
            classTypeCache.put(ct, ctce = new ClassTypeCacheEntry());
        }
        if (ctce.allMethods == null) {
            computeAllMethods(ct, ctce);
        }
        return new ArrayList<Method>(ctce.allMethods);
    }

    static final String GETCLASS_NAME = "getClass".intern();
    private void computeAllMethods(ClassType ct, ClassTypeCacheEntry ctce) {
        if (ctce.allSupertypes == null) {
            computeAllSupertypes(ct, ctce);
        }
        List<? extends ClassType> classes = ctce.allSupertypes;
        classes = removeErasedTypesFromList(classes);
        int s = classes.size();
        ArrayList<Method> result = new ArrayList<Method>(s * 8);

        ClassType jlo = getNameInfo().getJavaLangObject();
        
        int result_size = 0;
        for (int i = 0; i < s; i++) {
            ClassType c = classes.get(i);
            List<? extends Method> ml = c.getMethods();
            if (ml == null) {
                continue;
            }
            int ms = ml.size();
            add_methods: for (int j = 0; j < ms; j++) {
                Method m = ml.get(j);
                if (isVisibleFor(m, ct)) {
                	List<? extends Type> msig = m.getSignature();
                    String mname = m.getName();
                    // we can go by mname == GETCLASS_NAME, intern() is used everywhere
                    if ((c.getBaseClassType() == jlo & mname == GETCLASS_NAME) && java5Allowed()) {
                    	// always added, but return type of the
                    	// method is special. Also see JLS as well
                    	// as recoder bug 2046337
                    	result.add(new DummyGetClassMethod(ct, getServiceConfiguration().getImplicitElementInfo()));
                    	result_size++;
                    } else {
                    	for (int k = 0; k < result_size; k++) {
                    		Method rm = result.get(k);
                    		if (rm.getName() == mname) {
                    			List<? extends Type> rsig = rm.getSignature();
                    			if (signaturesEquals(rsig, msig))
                    				continue add_methods;
                    		}
                    	}
                        result.add(m);
                        result_size++;
                    }
                }
            }
        }
        result.trimToSize();
        ctce.allMethods = result;
    }
    
    /**
     * Used for checking overriding.
     * We go by type erasure. NOTE: This essentially misses a semantic check which should
     * at some point be moved to SemanticsChecker.  
     * @param rsig
     * @param msig
     * @return
     */
    private boolean signaturesEquals(List<? extends Type> rsig,
			List<? extends Type> msig) {
		if (rsig.size() != msig.size())
			return false;
		for (int i = 0, s = rsig.size(); i < s; i++) {
			Type t1 = rsig.get(i);
			Type t2 = msig.get(i);
			while (t1 instanceof ArrayType && t2 instanceof ArrayType) {
				t1 = ((ArrayType)t1).getBaseType();
				t2 = ((ArrayType)t2).getBaseType();
			}
			if (t1 instanceof ClassType) {
				if (t2 instanceof ClassType) {
					t1 = ((ClassType)t1).getBaseClassType();
					t2 = ((ClassType)t2).getBaseClassType();
				} else {
					return false; // shortcut
				}
			}
			if (!t1.equals(t2)) 
				return false;
		}
		return true;
	}

	public List<ClassType> removeErasedTypesFromList(
			List<? extends ClassType> classes) {
		ArrayList<ClassType> res = new ArrayList<ClassType>(classes.size());
		OUTER: for (int i = 0; i < classes.size(); i++) {
			// TODO this can be improved !?
			// TODO do we really need to reject "ErasedType"s only ?
			ClassType cur = classes.get(i);
			if (!(cur instanceof ErasedType)) {
				res.add(cur);
				continue;
			}
			ErasedType et = (ErasedType)cur;
			ClassType generic = et.getGenericType();
			for (int j = 0; j < classes.size(); j++) {
				if (i == j) continue; // don't reject everything ;-)
				ClassType xyz = classes.get(j);
				if (xyz == generic)
					continue OUTER; // reject
				if (xyz instanceof ParameterizedType
						&& ((ParameterizedType)xyz).getGenericType() == generic)
					continue OUTER; // reject as well
			}
			res.add(cur); // not rejected
		}
		res.trimToSize();
		return res;
	}
    
    public List<ClassType> getAllTypes(ClassType ct) {
        updateModel();
        ProgramModelInfo pmi = ct.getProgramModelInfo();
        if (pmi != this) {
            Debug.assertNonnull(pmi);
            return pmi.getAllTypes(ct);
        }
        ClassTypeCacheEntry ctce = classTypeCache.get(ct);
        if (ctce == null) {
            classTypeCache.put(ct, ctce = new ClassTypeCacheEntry());
        }
        if (ctce.allMemberTypes == null) {
            computeAllMemberTypes(ct, ctce);
        }
        return new ArrayList<ClassType>(ctce.allMemberTypes);
    }

    private void computeAllMemberTypes(ClassType ct, ClassTypeCacheEntry ctce) {
        if (ctce.allSupertypes == null) {
            computeAllSupertypes(ct, ctce);
        }
        List<? extends ClassType> classes = ctce.allSupertypes;
        // TODO evaluate performance and possibly look for faster solution
        Set<String> invisibleAndIgnore = new HashSet<String>(); // see bug 2838441
        classes = removeErasedTypesFromList(classes);
        int s = classes.size();
        ArrayList<ClassType> result = new ArrayList<ClassType>(s);
        int result_size = 0;
        for (ClassType c : classes) {
            List<? extends ClassType> cl = c.getTypes();
            if (cl == null) {
                continue;
            }
            int cs = cl.size();
            add_ClassTypes: for (int j = 0; j < cs; j++) {
                ClassType hc = cl.get(j);
                // hc == ct may occur as it is admissible for a member class
                // to extend its parent class
                if ((hc != ct) && isVisibleFor(hc, ct)) {
                    String cname = hc.getName();
                	if (invisibleAndIgnore.contains(cname))
                		continue add_ClassTypes;
                    for (int k = 0; k < result_size; k++) {
                        ClassType rc = result.get(k);
                        if (rc.getName() == cname) {
                            continue add_ClassTypes;
                        }
                    }
                    result.add(hc);
                    result_size++;
                } else if (hc != ct)
                	invisibleAndIgnore.add(hc.getName());
            }
        }
        result.trimToSize();
        ctce.allMemberTypes = result;
    }

    static class SuperTypeTopSort extends ClassTypeTopSort {

        protected final List<ClassType> getAdjacent(ClassType c) {
            return c.getSupertypes();
        }
    }

    class SubTypeTopSort extends ClassTypeTopSort {

        protected final List<ClassType> getAdjacent(ClassType c) {
            return getSubtypes(c);
        }
    }

    public PrimitiveType getPromotedType(PrimitiveType a, PrimitiveType b) {
    	// fixed in 0.96: promoted type is "at least" int
        NameInfo ni = getNameInfo();
        if (a == ni.getBooleanType() && a == b) {
            return a;
        }
        if (a == ni.getBooleanType() || b == ni.getBooleanType()) {
            return null;
        }
        if (a == ni.getDoubleType() || b == ni.getDoubleType()) {
            return ni.getDoubleType();
        }
        if (a == ni.getFloatType() || b == ni.getFloatType()) {
            return ni.getFloatType();
        }
        if (a == ni.getLongType() || b == ni.getLongType()) {
            return ni.getLongType();
        }
        return ni.getIntType();
    }

    public boolean isWidening(PrimitiveType from, PrimitiveType to) {
        // we do not handle null's
        if (from == null || to == null)
            return false;
        // equal types can be coerced
        if (from == to)
            return true;
        NameInfo ni = getNameInfo();
        // boolean types cannot be coerced into something else
        if (from == ni.getBooleanType() || to == ni.getBooleanType())
            return false;
        // everything else can be coerced to a double
        if (to == ni.getDoubleType())
            return true;
        // but a double cannot be coerced to anything else
        if (from == ni.getDoubleType())
            return false;
        // everything except doubles can be coerced to a float
        if (to == ni.getFloatType())
            return true;
        // but a float cannot be coerced to anything but float or double
        if (from == ni.getFloatType())
            return false;
        // everything except float or double can be coerced to a long
        if (to == ni.getLongType())
            return true;
        // but a long cannot be coerced to anything but float, double or long
        if (from == ni.getLongType())
            return false;
        // everything except long, float or double can be coerced to an int
        if (to == ni.getIntType())
            return true;
        // but an int cannot be coerced to the remaining byte, char, short
        if (from == ni.getIntType())
            return false;
        // between byte, char, short, only one conversion is admissible
        return (from == ni.getByteType() && to == ni.getShortType());
    }

    public boolean isWidening(ClassType from, ClassType to) {
        return isSubtype(from, to);
    }

    public boolean isWidening(ArrayType from, ArrayType to) {
        Type toBase = to.getBaseType();
        if (toBase == getNameInfo().getJavaLangObject()) {
            return true;
        }
        Type fromBase = from.getBaseType();
        if (toBase instanceof PrimitiveType) {
            return toBase == fromBase;
        }
        return isWidening(fromBase, toBase);
    }

    public boolean isWidening(Type from, Type to) {
    	if (from instanceof ArrayType && to instanceof ArrayType)
    		return isWidening((ArrayType)from, (ArrayType)to);
        if (from instanceof ClassType) {
            if (to instanceof ClassType) {
                return isWidening((ClassType) from, (ClassType) to);
            }
        } else if (from instanceof PrimitiveType) {
            if (to instanceof PrimitiveType) {
                return isWidening((PrimitiveType) from, (PrimitiveType) to);
            }
        }
        return false;
    }

    public boolean isSubtype(ClassType a, ClassType b) {
        boolean result = false;
        
        // TODO this fixes parts of bug 3000357, but is it the right place to do ?
        if (b instanceof ErasedType && ((ErasedType)b).getGenericType() == getNameInfo().getJavaLangObject())
        	b = getNameInfo().getJavaLangObject();
        
    	// If one type is "raw" / original and one not, and both are of the same generic type, then 
        // they match
        if (a instanceof ErasedType && ((ErasedType)a).getGenericType() == b)
        	return true;
        else if (b instanceof ErasedType && ((ErasedType)b).getGenericType() == a)
        	return true;
        if (a instanceof ParameterizedType && b instanceof ErasedType &&
        		((ParameterizedType)a).getGenericType() == ((ErasedType)b).getGenericType()) {
        	return true;
        } else if (b instanceof ParameterizedType && a instanceof ErasedType &&
        		((ParameterizedType)b).getGenericType() == ((ErasedType)a).getGenericType()) {
        	return true;
        } else if (a instanceof ParameterizedType && !(b instanceof ParameterizedType) &&
        		((ParameterizedType)a).getGenericType() == b) {
        	return true;
        } else if (b instanceof ParameterizedType && !(a instanceof ParameterizedType) &&
        		((ParameterizedType)b).getGenericType() == a) {
        	return true;
        } else if (a instanceof ParameterizedType && b instanceof ParameterizedType &&
        		((ParameterizedType)a).getGenericType() == ((ParameterizedType)b).getGenericType()) {
        	// check type parameters!
        	ParameterizedType pa = (ParameterizedType)a;
        	ParameterizedType pb = (ParameterizedType)b;
        	if (pa.getAllTypeArgs().size() != pb.getAllTypeArgs().size()) {
        		// Note: Wrong use of generics. SemanticsChecker will report it.
        		return false;
        	} else {
        		for (int i=0, s=pa.getAllTypeArgs().size(); i<s; i++) {
        			if (!paramMatches(
        					getCapture(pa.getAllTypeArgs().get(i)), 
        					getCapture(pb.getAllTypeArgs().get(i)), false)
        			)
        				return false;
        		}
        		return true;
        	}
        }

        // now we really need to look at subtyping.
        if (a instanceof TypeParameter && b instanceof TypeParameter) {
        	result = checkBoundsMatch((TypeParameter)a, (TypeParameter)b);
        } else if ((a != null) && (b != null)) {
            if ((a == b) || (a == getNameInfo().getNullType()) || (b == getNameInfo().getJavaLangObject())) {
                result = true;
            } else {
                // Optimization by non-recursive bfs possible!!!
                List<? extends ClassType> superA = a.getSupertypes();
                if (superA != null) {
                	int s = superA.size();
                    // if this is not a parameterized type, skip the last one in supertype list
                    for (int i = a instanceof ParameterizedType ? 1 : 0; (i < s) && !result; i++) {
                        ClassType sa = superA.get(i);
                        if (sa == a) {
                            getErrorHandler().reportError(new CyclicInheritanceException(a));
                        }
                        if (isSubtype(sa, b)) {
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean doNotCheckTypeArgs() {
		return Boolean.valueOf(getServiceConfiguration().getProjectSettings().getProperty(PropertyNames.DO_NOT_CHECK_TYPE_ARGUMENTS_FOR_PARAMETER_MATCHES));
	}

	public boolean isSupertype(ClassType a, ClassType b) {
        return isSubtype(b, a);
    }
    
    private final boolean paramMatches(Type ta, Type tb, boolean allowAutoboxing) {
    	if (ta == tb) return true;
    	// I added the checks for ErasedType to fix a new bug that occurred when changing
    	// code so that any type has it's erased type as a supertype (not just those having
    	// type arguments). 
    	if (allowAutoboxing && ta instanceof ErasedType && tb instanceof PrimitiveType)
    		ta = ((ErasedType)ta).getGenericType();
    	if (allowAutoboxing && tb instanceof ErasedType && ta instanceof PrimitiveType)
    		tb = ((ErasedType)tb).getGenericType();
    	if (ta instanceof TypeArgument.CapturedTypeArgument)
    		ta = getBaseType(((TypeArgument.CapturedTypeArgument)ta).getTypeArgument());
    	if (tb instanceof TypeArgument.CapturedTypeArgument)
    		// TODO this isn't the full deal...
    		tb = getBaseType(((TypeArgument.CapturedTypeArgument)tb).getTypeArgument());
    	while (ta instanceof ArrayType && tb instanceof ArrayType) {
        	// if we got arrays of parameterized types, this helps avoiding special cases
    		ta = ((ArrayType)ta).getBaseType();
    		tb = ((ArrayType)tb).getBaseType();
    	}
    	if (ta instanceof IntersectionType) {
    		// It's enough if one of TA's types matches tb. 
    		IntersectionType ta_is = (IntersectionType)ta;
    		for (ClassType t: ta_is.getSupertypes()) {
    			if (paramMatches(t, tb, false))
    				return true;
    		}
    		return false;
    	}
    	if (ta instanceof TypeParameter && tb instanceof TypeParameter) {
    		// extra check ONLY here: 
			TypeParameter tp1 = (TypeParameter)ta;
			TypeParameter tp2 = (TypeParameter)tb;
			return checkBoundsMatch(tp1, tp2);
    	}
    	if (tb instanceof TypeParameter && ta instanceof ArrayType) {
    		TypeParameter tp = (TypeParameter)tb;
    		// only java.lang.Object is allowed as one and only bound
    		if (tp.getBoundCount() > 1) return false;
    		return tp.getBoundName(0).equals("java.lang.Object");
    	}
    	if (allowAutoboxing && tb instanceof TypeParameter && ta instanceof PrimitiveType) {
    		// raw type in combination with primitive type.
    		// will also be handled in next if-clause.
    		ta = getBoxedType((PrimitiveType)ta);
    	}
    	if (tb instanceof TypeParameter && ta instanceof ClassType) {
    		TypeParameter tp = (TypeParameter)tb;
    		ArrayDeque<ClassType> worklist = new ArrayDeque<ClassType>();
    		for (int i=0; i < tp.getBoundCount(); i++) {
        		worklist.add(getClassTypeFromTypeParameter(tp,i));
    		}
    		while (!worklist.isEmpty()) {
    			// must be compatible to all bounds!
    			ClassType t = worklist.poll();
    			if (t instanceof TypeParameter) {
    				// Add the bounds of this TypeParameter as well
    				tp = (TypeParameter)t;
    	    		for (int i=0; i < tp.getBoundCount(); i++) {
    	        		worklist.add(getClassTypeFromTypeParameter(tp,i));
    	    		}
    				continue;
    			}
    			if (t == null) {
    				// TODO error! (?)
    				throw new RuntimeException("???");
    			}
    			if (!isWidening(ta, t))
    				return false;
    		}
    		return true;
    	}
    	if (doNotCheckTypeArgs()) {
    		if (ta instanceof ParameterizedType)
    			ta = ((ParameterizedType)ta).getGenericType();
    		if (tb instanceof ParameterizedType)
    			tb = ((ParameterizedType)tb).getGenericType();
    	}
        if (ta != null && !isWidening(ta, tb)) {
            // (un-)boxing conversion possible? (Java 5 only)
            if (allowAutoboxing) {
                if (ta instanceof PrimitiveType
                        && isWidening(getBoxedType((PrimitiveType)ta), tb)) {
                    return true; // ok
                } else {
                    if ( !(ta instanceof ClassType)) return false; // Arrays/Primitive Types can't be unboxed
                    PrimitiveType unboxedType = getUnboxedType((ClassType)ta);
                    if ( isWidening(unboxedType, tb) ) {
                        return true; // ok
                    }
                }
            } // boxing not successful.
            return false; 
        }
        return true;
    }
    
    private boolean checkBoundsMatch(TypeParameter tp1, TypeParameter tp2) {
		List<ClassType> bounds1 = new ArrayList<ClassType>();
		for (int i = 0; i < tp1.getBoundCount(); i++) {
			String tpName = tp1.getBoundName(i);
			ClassType ct = null;
			if (tpName.indexOf('.') == -1) {
				// Check if it's a type variable. Unfortunately, this information is currently not stored in parameter signature... 
				ClassTypeContainer container = tp1.getContainer();
				OUTER: while (container != null) {
					List<? extends TypeParameter> tps = null;
					if (container instanceof Method) {
						tps =  ((Method)container).getTypeParameters();
					} else if (container instanceof ClassType) {
						tps = ((ClassType)container).getTypeParameters();
					}
					if (tps != null) {
						for (TypeParameter tp: tps) {
							if (tp.getName().equals(tpName)) {
								ct = tp;
								break OUTER;
							}
						}
					}
					container = container.getContainer();
				}
			}
			if (ct == null) {
				ct = getNameInfo().getClassType(tp1.getBoundName(i));
			}
			if (ct != null && tp1.getBoundTypeArguments(i) != null && tp1.getBoundTypeArguments(i).size() > 0) {
				ct = getServiceConfiguration().getImplicitElementInfo().getParameterizedType(ct, tp1.getBoundTypeArguments(i));
			}
			if (ct == null) {
				// incomplete code.
				ct = getNameInfo().getUnknownClassType();
			}
			bounds1.addAll(getAllSupertypes(ct));
		}
		for (int i = 0; i < tp2.getBoundCount(); i++) {
			String tpName = tp2.getBoundName(i);
			ClassType ct = null;
			if (tpName.indexOf('.') == -1) {
				// Check if it's a type variable. Unfortunately, this information is currently not stored in parameter signature... 
				ClassTypeContainer container = tp2.getContainer();
				OUTER: while (container != null) {
					List<? extends TypeParameter> tps = null;
					if (container instanceof Method) {
						tps =  ((Method)container).getTypeParameters();
					} else if (container instanceof ClassType) {
						tps = ((ClassType)container).getTypeParameters();
					}
					if (tps != null) {
						for (TypeParameter tp: tps) {
							if (tp.getName().equals(tpName)) {
								ct = tp;
								break OUTER;
							}
						}
					}
					container = container.getContainer();
				}
			}
			if (ct == null) {
				ct = getNameInfo().getClassType(tp2.getBoundName(i));
			}
			if (ct != null && tp2.getBoundTypeArguments(i) != null && tp2.getBoundTypeArguments(i).size() > 0) {
				ct = getServiceConfiguration().getImplicitElementInfo().getParameterizedType(ct, tp2.getBoundTypeArguments(i));
			}
			if (!containsSpecial(bounds1,ct))
				return false;
		}
		return true;
	}

    // TODO this is a hack for breaking recursion, isn't there a better solution?
    private final Deque<TypeArgument[]> prev = new ArrayDeque<TypeArgument[]>();
    
	private boolean containsSpecial(List<ClassType> bounds1, ClassType ct) {
		for (ClassType b: bounds1) {
			if (b.getGenericMember() == ct.getGenericMember()) {
				if (b instanceof ParameterizedType && ct instanceof ParameterizedType) {
					// check if type args match as well. It's more than a simple equals(), though,
					// as type args may contain type parameters that are not equal (but have same bounds)
					ParameterizedType p1 = (ParameterizedType)b;
					ParameterizedType p2 = (ParameterizedType)ct;
					for (int i = 0; i < p1.getTypeArgs().size(); i++) {
						if (containsIdentity(p1.getTypeArgs().get(i), p2.getTypeArgs().get(i))) {
							// break recursion in cases where class B<Q extends ArrayList<Q>> extends A<Q>
							// see FixedBugs.testTypeArgMatchesWithTypeParams_2().
							continue;   
						}
						prev.add(new TypeArgument[] {p1.getTypeArgs().get(i), p2.getTypeArgs().get(i) });
						if (!paramMatches(getCapture(p1.getTypeArgs().get(i)), getCapture(p2.getTypeArgs().get(i)), false)) {
							prev.removeLast();
							return false;
						}
						prev.removeLast();
					}
					return true;
				} else if (b instanceof ParameterizedType || ct instanceof ParameterizedType) {
					// no match, actually, there should be a parameterized version (i.e., not erased) as well in the list.
					continue;
				}
				return true; // both are non-generic anyway.
			}
		}
		return false; // not found.
	}

	private boolean containsIdentity(TypeArgument typeArgument,
			TypeArgument typeArgument2) {
		for (TypeArgument[] ta: prev) {
			if (ta[0] == typeArgument && ta[1] == typeArgument2)
				return true;
		}
		return false;
	}

	ClassType getClassTypeFromTypeParameter(TypeParameter tp, int i) {
		ClassType t;
		// this could be a type parameter of an enclosing container. Check that first:
		ClassTypeContainer container = tp.getContainer();
		while (container != null) {
			List<? extends TypeParameter> typeParams;
			if (container instanceof ClassType) {
				typeParams = ((ClassType)container).getTypeParameters();
			} else if (container instanceof Method) {
				typeParams = ((Method)container).getTypeParameters();
			} else if (container instanceof Package) {
				break; // top level reached (this happens for ClassFiles)
			} else throw new RuntimeException("Only methods and classes may be parents here!");
			if (typeParams != null) {
				for (TypeParameter ctp: typeParams) {
					if (ctp.getName().equals(tp.getBoundName(i))) {
						return ctp;
					}
				}
			}
			container = container.getContainer();
		}
		t = getNameInfo().getClassType(tp.getBoundName(i));
		return t;
	}

    private final boolean internalIsCompatibleSignature(List<Type> a, List<Type> b, boolean allowAutoboxing, boolean isVarArgMethod) {
        int s = b.size();
        int n = a.size();
        if (isVarArgMethod) {
            if (s > n+1)
                return false; // too few arguments
            // there are arguments that must be matches
            // consider only a's n-(s-1) and b's last arguments, i.e. the var arg part.
            if (s == n) {
                // tb is an array type. However, ta may be the base type of that array, too
                Type ta = a.get(s-1);
                Type tb = ((ArrayType)b.get(s-1)).getBaseType();
                if (paramMatches(ta, tb, allowAutoboxing)) {
                    s--; // param ok, don't check again later
                }
                // else: param may match anyway.
            } else {
                Type tb = ((ArrayType)b.get(s-1)).getBaseType(); // b's variable arity parameter
                for (int i = s-1; i < n; i++) {
                    Type ta = a.get(i);
                    if (!paramMatches(ta, tb, allowAutoboxing))
                        return false; // no match
                }
                s--; // last parameter has already been checked
            }
        } else if (s != n) return false; // no var args allowed / wrong number or arguments
        for (int i = 0; i < s; i += 1) {
            Type ta = a.get(i);
            Type tb = b.get(i);
            if (!paramMatches(ta,tb,allowAutoboxing))
                return false;
        }
        return true;
    }

    public final boolean isCompatibleSignature(List<Type> a, List<Type> b) {
        return internalIsCompatibleSignature(a, b, false, false);
    }
    
    public final boolean isCompatibleSignature(List<Type> a, List<Type> b, boolean allowAutoboxing, boolean bIsVariableArityMethod) {
        return internalIsCompatibleSignature(a, b, allowAutoboxing, bIsVariableArityMethod);
    }
    
    public ClassType getBoxedType(PrimitiveType unboxedType) {
        NameInfo ni = getNameInfo();
        if (unboxedType == ni.getBooleanType()) return ni.getJavaLangBoolean();
        if (unboxedType == ni.getByteType()) return ni.getJavaLangByte();
        if (unboxedType == ni.getCharType()) return ni.getJavaLangCharacter();
        if (unboxedType == ni.getShortType()) return ni.getJavaLangShort();
        if (unboxedType == ni.getIntType()) return ni.getJavaLangInteger();
        if (unboxedType == ni.getLongType()) return ni.getJavaLangLong();
        if (unboxedType == ni.getFloatType()) return ni.getJavaLangFloat();
        if (unboxedType == ni.getDoubleType()) return ni.getJavaLangDouble();
        throw new Error("Unknown primitive type " + unboxedType.getFullName());
    }
    
    public PrimitiveType getUnboxedType(ClassType boxedType) {
    	if (boxedType instanceof ErasedType)
    		boxedType = ((ErasedType)boxedType).getGenericType(); // how does this happen??
        NameInfo ni = getNameInfo();
        if (boxedType == ni.getJavaLangBoolean()) return ni.getBooleanType();
        if (boxedType == ni.getJavaLangByte()) return ni.getByteType();
        if (boxedType == ni.getJavaLangCharacter()) return ni.getCharType();
        if (boxedType == ni.getJavaLangShort()) return ni.getShortType();
        if (boxedType == ni.getJavaLangInteger()) return ni.getIntType();
        if (boxedType == ni.getJavaLangLong()) return ni.getLongType();
        if (boxedType == ni.getJavaLangFloat()) return ni.getFloatType();
        if (boxedType == ni.getJavaLangDouble()) return ni.getDoubleType();
        return null;
    }

    protected ClassType getOutermostType(ClassType t) {
        ClassTypeContainer c = t;
        ClassTypeContainer cc = t.getContainer();
        while (cc != null && !(cc instanceof Package)) {
            c = cc;
            cc = cc.getContainer();
        }
        return (ClassType) c;
    }

    public boolean isVisibleFor(Member m, ClassType t) {
    	if (m instanceof ErasedType)
    		m = ((ErasedType)m).getGenericType();
    	else if (m instanceof ParameterizedType)
    		m = ((ParameterizedType)m).getGenericType();
    	if (t instanceof ParameterizedType) 
    		t = ((ParameterizedType)t).getGenericType();
    	else if (t instanceof ErasedType)
    		t = ((ErasedType)t).getGenericType();
    	else if (t instanceof CapturedTypeArgument)
    		t = getBaseType(((CapturedTypeArgument)t).getTypeArgument());
        if (m.isPublic()) {
            // public members are always visible
        	// TODO - what if containing type is not visible to t ???
        	// also look at a bugfix 2838441 in computeAllMemberTypes
            return true;
        }
        if (t instanceof IntersectionType)
        	t = ((IntersectionType)t).getAccessibility();
        
        ClassType mt = m.getContainingClassType();
        if (mt == null) {
            // a classless member is not visible
            return false;
        }
        if (mt instanceof ParameterizedType)
        	mt = ((ParameterizedType)mt).getGenericType();
        if (mt instanceof ErasedType)
        	mt = ((ErasedType)mt).getGenericType();
        if (mt == t) {
            // all members are visible to their own class
            return true;
        }
        if (m.isPrivate()) {
            // private members are only visible to members that share
            // an outer type
            return getOutermostType(t) == getOutermostType(mt);
        }
        if (mt.getPackage() == t.getPackage()) {
            // non-private members are visible to their own package
            return true;
        }
        if (m.isProtected()) {
            if (isSubtype(t, mt)) {
                // protected members are visible to subtypes
                return true;
            }
        }
        
        if (t.getContainingClassType() != null) {
        	// check if accessible through enclosing type.
        	return isVisibleFor(m, t.getContainingClassType());
        }
        
        // all others are not visible
        return false;
    }
    
    // TODO Hack and to be removed (i.e., assign ProgramModelInfo to TypeArgument)
    public ClassType getBaseType(TypeArgument ta) {
    	return getBaseType(ta, true);
    }
    
    // TODO Hack and to be removed (i.e., assign ProgramModelInfo to TypeArgument)
    // TODO this is supposed to do capture conversion but doesn't really do it...
    ClassType getBaseType(TypeArgument ta, boolean returnObjectForSuper) {
    	if (ta.getWildcardMode() == WildcardMode.Any) {
    		return ta.getTargetedTypeParameter().getBaseClassType();
    	}
    	if (returnObjectForSuper && ta.getWildcardMode() == WildcardMode.Super) {
    		// TODO this is not the full deal...
    		return getNameInfo().getJavaLangObject();
    	}
    	// WildcardMode.None / WildcardMode.extends
    	// TODO the following cases should be mergable !?
    	List<? extends TypeArgument> targs = ta.getTypeArguments();
    	if (ta instanceof TypeArgumentInfo) {
    		TypeArgumentInfo tai = (TypeArgumentInfo)ta;
    		if (tai.isTypeVariable()) {
    			String taiName = tai.getTypeName();
    			int dim = 0;
    			int idx = taiName.indexOf('[');
    			dim = idx==-1 ? 0 : (taiName.length() - idx) / 2;
    			if (dim > 0)
    				taiName = taiName.substring(0, idx);
    			if (tai.getContainingMethodInfo() != null) {
    				if (tai.getContainingMethodInfo().getTypeParameters() != null)
    					for (TypeParameterInfo tpi: tai.getContainingMethodInfo().getTypeParameters()) {
    						if (tpi.getName() == taiName) {
    							Type res = tpi;
    							while (dim-- > 0) 
    								res = res.createArrayType();
    							return tpi;
    						}
    					}
    			}
    			// check containing class file, and parent classes of inner types
    			ClassType container = tai.getContainingClassFile();
    			for(;;) {
    				for (TypeParameter tpi: container.getTypeParameters()) {
    					if (tpi.getName() == taiName) {
							Type res = tpi;
							while (dim-- > 0) 
								res = res.createArrayType();
							return tpi;
    					}
    				}
    				if (container.isInner()) {
    					container = container.getContainingClassType();
    				} else break;
    			} 
    			// TODO this can appear when:
    			// anonymous class in a method; usually, recoder doesn't get to these.
    			// however, when querying it directly, this may happen.
    			// what to do ??? See java.util.Collections$1 ...
    			return getNameInfo().getJavaLangObject();
    		} else {
    			ClassType ct = getNameInfo().getClassType(ta.getTypeName()); 
    			return targs == null || targs.size() == 0 ? ct : getServiceConfiguration().getImplicitElementInfo().getParameterizedType(ct, targs);
    		}
    	}
    	if (ta instanceof TypeArgumentDeclaration) {
    		SourceInfo si = getServiceConfiguration().getSourceInfo();
    		TypeArgumentDeclaration tad = (TypeArgumentDeclaration)ta;
    		// now we have two cases: 
    		// (1) type argument refers to a type parameter declaration, or
    		// (2) type argument refers to an external type
    		// in case of (2), we need to go out of type declaration in type search!
    		// however, in case of (1), we must not check the type declaration scope
    		// but we need to check the type parameter declarations only. 
    		// we do that explicitly here, but:
    		// TODO check if there's a better/cleaner solution !?
    		NonTerminalProgramElement context = tad;
    		context = context.getASTParent();
    		while (context.getASTParent() instanceof TypeArgumentDeclaration) {
    			// TODO check if this get called!!! (Nested TypeArgumentDeclarations)
    			context = context.getASTParent();
    			context = context.getASTParent();
    		}
    		/* the test for !(instanceof MethodDeclaration) fixes the bug
    		 * testHidingTypeParameterDeclarationBug() - I kind of understand
    		 * why but the whole thing below should be cleaned up!!!
    		 * TODO clean up the code below!*/
    		String typename = Naming.toPathName(tad.getTypeReferenceAt(0));
    		if (context.getASTParent().getASTParent() instanceof TypeDeclaration
    				&& typename.indexOf('.') == -1 && !(context.getASTParent() instanceof MethodDeclaration)) {
    			TypeDeclaration td = (TypeDeclaration)context.getASTParent().getASTParent();
    			ASTList<TypeParameterDeclaration> typeParams = td.getTypeParameters();
    			if (typeParams != null) {
        			int dim = 0;
        			String tp_name = typename;
        			while (tp_name.endsWith("[]")) {
        				tp_name = tp_name.substring(0, tp_name.length()-2);
        				dim++;
        			}
    				for (TypeParameterDeclaration tp : typeParams) {
    					if (tp.getName().equals(tp_name)) {
    						ClassType res = tp;
    						while (dim-- > 0)
    							res = res.createArrayType();
    						return res;
    					}
    				}
    			}
    		} else {
    			// this is fine. Not part of an inheritance specification.
    		}
    		ClassType ct = (ClassType)si.getType(typename, context);
    		if (ct == null)
    			ct = getNameInfo().getUnknownClassType(); // no need to report. There're already warnings somewhere on the console for sure.
    		if (targs != null && targs.size() != 0) {
    			ct = makeParameterizedArrayType(ct, targs);
    		} 
    		return ct;
    	}
    	if (ta instanceof ResolvedTypeArgument) {
    		ResolvedTypeArgument ra = (ResolvedTypeArgument)ta;
    		if (ra.typeArgs != null && ra.typeArgs.size() > 0)
    			return getServiceConfiguration().getImplicitElementInfo().getParameterizedType(ra.type, ra.typeArgs);
    		return ra.type;
    	}
    	if (ta instanceof WrappedTypeArgument)
    		return ((WrappedTypeArgument)ta).type;
    	throw new RuntimeException(); // not reachable any longer!?
    }
    
    // typeArguments are the type arguments of an explicit generic invocation.
    private void internalFilterApplicableMethods(List<Method> list, String name, List<Type> signature, ClassType context, 
    			boolean allowJava5) {
    	Debug.assertNonnull(name, signature, context);
        name = name.intern(); // necessary - user may query via public methods! 
        
        // the following looks complicated but it pays off
        int s = list.size();
        int i = 0;
        while (i < s) {
            Method m = list.get(i);
            // easy/fast computations first
            if (name != m.getName() || !isVisibleFor(m, context))
            	break;
            List<Type> methodSig = m.getSignature(); 
            // if the method has type parameters, then internalIsCompatibleSignature
            // checks applicability only. Type inference is not performed here.
            if (!internalIsCompatibleSignature(signature, methodSig, allowJava5, m.isVarArgMethod() & allowJava5)) {
                break;
            } else {
                i += 1;
            }
        }
        // if no element has been rejected, we are done
        if (i < s) {
            int j = i;
            for (i += 1; i < s; i += 1) {
                Method m = list.get(i);
                // easy/fast computations first
                if (name != m.getName() || !isVisibleFor(m, context))
                	continue;
                List<Type> methodSig = m.getSignature();
                // if the method has type parameters, then internalIsCompatibleSignature
                // checks applicability only. Type inference is not performed here.
                if (internalIsCompatibleSignature(signature, methodSig, allowJava5, m.isVarArgMethod() & allowJava5)) {
                    list.set(j, m);
                    j += 1;
                }
            }
            removeRange(list, j);
        }
    }
    
    private static void removeRange(List<?> list, int from) {
    	for (int i = list.size()-1; i >= from; i--)
    		list.remove(i);
    }
    
    private static void removeRange(List<?> list, int from, int to) {
    	if (from > to)
    		to ^= from ^= to ^= from;
    	int cnt = to-from;
    	while (cnt-- > 0)
    		list.remove(from);
    }

    public void filterMostSpecificMethods(List<Method> list) {
        internalFilterMostSpecificMethods(list, false, false, null);
    }
    
    public void filterMostSpecificMethodsPhase2(List<Method> list) {
        internalFilterMostSpecificMethods(list, true, false, null);
    }
    
    public void filterMostSpecificMethodsPhase3(List<Method> list) {
        internalFilterMostSpecificMethods(list, true, true, null);
    }
    
    private void internalFilterMostSpecificMethods(List<Method> list, boolean allowAutoboxing, boolean allowVarArgs, ClassType visibilityContext) {
        int size = list.size();
        if (size <= 1) {
            return;
        }
        // cache signatures (avoid multiple allocations)
        @SuppressWarnings("unchecked") List<Type>[] signatures = new List[size];
        signatures[0] = list.get(0).getSignature();
        // size should not be very large - using a naive n² algorithm
        // signatures/methods to be removed are marked as null
        for (int i = 1; i < size; i += 1) {
            List<Type> sig = signatures[i] = list.get(i).getSignature();
            if (sig != null) {
                for (int j = i - 1; j >= 0; j -= 1) {
                    List<Type> sig2 = signatures[j];
                    if (sig2 != null) {
                        if (internalIsCompatibleSignature(sig2, sig, allowAutoboxing, allowVarArgs & list.get(i).isVarArgMethod())) {
                            // need to doublecheck: is compatible vice versa? (can happen only with autoboxing or if signatures are exactly the same)
                            if (!allowAutoboxing || !internalIsCompatibleSignature(sig, sig2, allowAutoboxing, false))
                                signatures[i] = null;
                        } else if (internalIsCompatibleSignature(sig, sig2, allowAutoboxing, allowVarArgs & list.get(j).isVarArgMethod())) {
                            // the above special case cannot happen here: vice versa-check has already been implicitly done)
                            signatures[j] = null;
                            //break; removed because if more than 2 compatible signatures are found, an error might be falsely reported
                        } 
                    }
                }
            }
        }
        // do the cleanup work - remove all less specific methods
        int k = 0;
        for (int i = size - 1; i >= 0; i -= 1) {
            if (signatures[i] == null) {
                k += 1;
            } else if (k > 0) {
                removeRange(list, i + 1, i + k + 1);
                k = 0;
            }
        }
        if (k > 0) {
            removeRange(list, 0, k);
        }
    }

    public List<? extends Constructor> getConstructors(ClassType ct, List<Type> signature, List<TypeArgumentDeclaration> typeArgs, ClassType visibilityContext) {
        Debug.assertNonnull(ct, signature);
        if (ct.isInterface()) {
            if (signature.isEmpty()) {
                // Fake: yield java.lang.Object()
                return getNameInfo().getJavaLangObject().getConstructors();
            }
            return Collections.emptyList();
        }
        String name = ct.getName();
        name = name.substring(name.lastIndexOf('.') + 1);

        List<Method> meths = internalGetMostSpecificMethods(ct, name, signature, ct.getConstructors(), typeArgs, visibilityContext);
        List<Constructor> result = new ArrayList<Constructor>(meths.size());
        for (int i = 0, s = meths.size(); i < s; i += 1) {
            result.add((Constructor) meths.get(i));
        }
        return result;
    }
    
    public List<Method> getMethods(ClassType ct, String name, List<Type> signature, List<? extends TypeArgument> typeArgs, ClassType visibilityContext) {
    	return internalGetMostSpecificMethods(ct, name, signature, ct.getAllMethods(), typeArgs, visibilityContext);
    }
    
    private List<Method> internalGetMostSpecificMethods(ClassType ct, String name, List<Type> signature, List<? extends Method> meths, 
    					@SuppressWarnings("unused") List<? extends TypeArgument> typeArgs, ClassType visibilityContext) {
    	// TODO check if the arguments match the provided type args !!!
        Debug.assertNonnull(ct, name, signature);
        boolean allowJava5 = getServiceConfiguration().getProjectSettings().java5Allowed();
        
        List<Method> result;
        if (allowJava5) {
            /* see JLS 3rd edition chapter 15.12.2 */
            result = doThreePhaseFilter(meths, signature, name, visibilityContext);
        } else {
            // No java 5 - just one pass
        	result = new ArrayList<Method>(meths);
            internalFilterApplicableMethods(result, name, signature, visibilityContext, false);
            filterMostSpecificMethods(result);
        }
        return result;
    }
    
    List<Method> doThreePhaseFilter(List<? extends Method> methods, List<Type> signature, String name, ClassType context) {
        /* phase 1. see JLS 3rd edition chapter 15.12.2 */
    	List<Method> applicableMethods = new ArrayList<Method>(methods.size()+1);
        applicableMethods.addAll(methods);
        internalFilterApplicableMethods(applicableMethods, name, signature, context, true);
        if (applicableMethods.size() < 2) return applicableMethods;

        // applicableMethods now contains correct content. Work on copy of this list, now  
        List<Method> result = new ArrayList<Method>(applicableMethods.size()+1);
        result.addAll(applicableMethods);

        // for first pass, we need to filter again, but on already reduced set only
        internalFilterApplicableMethods(result, name, signature, context, false);
        filterMostSpecificMethods(result);
        if (result.size() > 0) 
        	return result;
        
        result.addAll(applicableMethods); // result is empty at this point
        filterMostSpecificMethodsPhase2(result);
        if (result.size() == 1) 
        	return result;
        result.clear(); // either result is empty, or no most specific method has been found yet.
        result.addAll(applicableMethods);
        filterMostSpecificMethodsPhase3(result);
        return result;
    }

    public void reset() {
        // it would be possible to reuse cache entry objects by
        // iterating over the cache and erasing all cached lists only.
        // however, whole class types might have vanished and their entries
        // have to vanish, too, so there is little choice
        classTypeCache.clear();
    }

    /**
     * Takes an (Array|Class)Type and adds type arguments to it.
     * @return
     * @throws ClassCastException if t is an array type with a primitive type as base type.
     */ 
    protected ClassType makeParameterizedArrayType(ClassType t, List<? extends TypeArgument> typeArgs) {
    	ClassType result = t;
    	int dim = 0;
    	while (result instanceof ArrayType) {
    		result = (ClassType)((ArrayType)result).getBaseType();
    		dim++;
    	}
    	result = getServiceConfiguration().getImplicitElementInfo().getParameterizedType(result, typeArgs);
    	for(int i = 0; i < dim; i++) {
    		result = getNameInfo().createArrayType(result);
    	}
    	return result;
    }

	public List<Method> getMethods(ClassType ct, String name, List<Type> signature, ClassType visibilityContext) {
		return getMethods(ct, name, signature, null, visibilityContext);
	}

	public List<Method> getMethods(ClassType ct, String name, List<Type> signature, 
			boolean allowBoxing, boolean allowVarArgs, ClassType visibilityContext) {
		List<Method> result = getMethods(ct, name, signature, visibilityContext);
		if (allowBoxing & allowVarArgs)
			return result;
		for (Iterator<Method> it = result.iterator(); it.hasNext();) {
			Method m = it.next();
			if (!internalIsCompatibleSignature(signature, m.getSignature(), allowBoxing, 
					m.isVarArgMethod() & allowVarArgs))
				it.remove();
		}
		return result;
	}

	
	public List<? extends Constructor> getConstructors(ClassType ct, List<Type> signature, ClassType visibilityContext) {
		return getConstructors(ct, signature, null, visibilityContext);
	}
	
	public ClassType getCommonSupertype(ClassType ... types) {
		if (types == null || types.length == 0)
			throw new IllegalArgumentException();
		if (types.length == 1)
			return types[0];
		OUTER: for (int i = 0; i < types.length; i++) {
			if (types[i] instanceof ErasedType) {
				for (int j = 0; j < types.length; j++)
					types[j] = types[j].getErasedType();
				break OUTER;
			}
		}
		
		ClassType result;
		ArrayList<ClassType> tml = new ArrayList<ClassType>();
		tml.addAll(getAllSupertypes(types[0]));
		for (int i = 1; i < types.length; i++) {
			List<? extends ClassType> comp = getAllSupertypes(types[i]);
			for (int j = tml.size()-1; j >=0; j--) {
				if (comp.indexOf(tml.get(j)) == -1)
					tml.remove(j);
			}
		}
		removeSupertypesFromList(tml);
		if (tml.size() == 0)
			throw new Error("Cannot compute common supertype for " + Arrays.toString(types)); // why is java.lang.Object not found ?
		if (tml.size() == 1)
			result = tml.get(0);
		else {
			result = new IntersectionType(tml, getServiceConfiguration().getImplicitElementInfo());
		}
		tml.trimToSize();
		return result;
	}
	
	/**
     * removes every type from the list if any of its subtypes is on the list, too  
     * @param result
     * @return
     */
	void removeSupertypesFromList(List<ClassType> result) {
		// now, remove everything that is redundant
    	for (int j = result.size()-1; j >= 0; j--) {
    		for (int k = 0; k < result.size()-1; k++) {
    			if (j == k) continue;
    			ClassType a = result.get(j);
    			ClassType b = result.get(k);
    			if (a instanceof ArrayType) {
    				assert b instanceof ArrayType;
    				// assert dimensions are equal ?
    				while (a instanceof ArrayType) {
    					a = (ClassType)((ArrayType)a).getBaseType();
    					b = (ClassType)((ArrayType)b).getBaseType();
    				}
    			}
    			if (isSupertype(a,b)) {
    				result.remove(j);
    				break; // continue with outer loop
    			}
    		}
    	}
	}
	
    public boolean containsTypeParameter(Type t) {
    	while (t instanceof ArrayType)
    		t = ((ArrayType)t).getBaseType();
    	if (!(t instanceof ClassType)) return false;
    	if (t instanceof TypeParameter)
    		return true;
		if (t instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType)t;
			if (pt.getGenericType() instanceof TypeParameter)
				return true;
			for (TypeArgument ta: pt.getAllTypeArgs()) {
				if (containsTypeParameter(ta))
					return true;
			}
		}
    	return false;
	}
    
    private boolean containsTypeParameter(TypeArgument ta) {
    	if (ta.getWildcardMode() == WildcardMode.Any)
    		return false;
    	Type bt = getBaseType(ta);
    	while (bt instanceof ArrayType) 
    		bt = ((ArrayType)bt).getBaseType();
    	if (bt instanceof TypeParameter)
    		return true;
    	if (ta.getTypeArguments() != null) {
    		for (TypeArgument nta: ta.getTypeArguments()) {
    			if (containsTypeParameter(nta))
    				return true;
    		}
    	}
    	return false;
    }

    boolean containsTypeParameter(Type t, TypeParameter tp) {
    	while (t instanceof ArrayType)
    		t = ((ArrayType)t).getBaseType();
    	if (!(t instanceof ClassType)) return false;
    	if (t == tp)
    		return true;
		if (t instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType)t;
			if (pt.getGenericType() == tp)
				return true;
			for (TypeArgument ta: pt.getAllTypeArgs()) {
				if (containsTypeParameter(ta, tp))
					return true;
			}
		}
    	return false;
    }

    private boolean containsTypeParameter(TypeArgument ta, TypeParameter tp) {
    	if (getBaseType(ta) == tp)
    		return true;
    	if (ta.getTypeArguments() != null) {
    		for (TypeArgument nta: ta.getTypeArguments()) {
    			if (containsTypeParameter(nta, tp))
    				return true;
    		}
    	}
    	return false;
    }


    
    public boolean containsTypeParameter(Method m) {
    	Type rt = m.getReturnType();
    	if (rt != null && containsTypeParameter(rt)) return true;
    	for (Type t : m.getSignature()) {
    		if (containsTypeParameter(t)) return true;
    	}
    	for (Type t : m.getExceptions()) {
    		if (containsTypeParameter(t)) return true;
    	}
    	return false;
    }
    
    
    public boolean containsTypeParameter(Field f) {
    	Type t = f.getType();
    	return containsTypeParameter(t);
    }
    
	public List<? extends Type> getAllSubtypes(Type t) {
		if (t == null)
			throw new NullPointerException();
		if (t instanceof PrimitiveType)
			return getAllSubtypes((PrimitiveType)t);
		if (t instanceof ClassType)
			return getAllSubtypes((ClassType)t);
		throw new Error("This shouldn't happen: All cases covered!");
	}

	public List<PrimitiveType> getAllSubtypes(PrimitiveType pt) {
		NameInfo ni = getNameInfo();
		if (pt == ni.getBooleanType())
			return Collections.<PrimitiveType>emptyList(); 
		if (pt == ni.getByteType())
			return Collections.<PrimitiveType>emptyList();
		if (pt == ni.getShortType())
			return makeList(ni.getByteType());
		if (pt == ni.getLongType())
			return makeList(ni.getIntType(),
				ni.getShortType(), ni.getCharType(), 
				ni.getByteType());
		if (pt == ni.getIntType())
			return makeList(ni.getShortType(), 
				ni.getCharType(), ni.getByteType());
		if (pt == ni.getFloatType())
			return makeList(ni.getLongType(),
				ni.getIntType(), ni.getShortType(), 
				ni.getCharType(),	ni.getByteType());
		if (pt == ni.getDoubleType())
			return makeList(ni.getFloatType(),
				ni.getLongType(),	ni.getIntType(), 
				ni.getShortType(), ni.getCharType(),	
				ni.getByteType());
		if (pt == ni.getCharType())
			return Collections.<PrimitiveType>emptyList();
		throw new Error(); // all cases covered
	}

	public List<PrimitiveType> getAllSupertypes(PrimitiveType pt) {
		NameInfo ni = getNameInfo();
		if (pt == ni.getBooleanType())
			return makeList(ni.getBooleanType()); 
		if (pt == ni.getByteType())
			return makeList(
				ni.getByteType(), ni.getShortType(), 
				ni.getIntType(), ni.getLongType(), 
				ni.getFloatType(), ni.getDoubleType());
		if (pt == ni.getShortType())
			return makeList(
					ni.getShortType(), ni.getIntType(),
					ni.getLongType(), ni.getFloatType(),
					ni.getDoubleType());
		if (pt == ni.getLongType())
			return makeList(
					ni.getLongType(), ni.getFloatType(),
					ni.getDoubleType());
		if (pt == ni.getIntType())
			return makeList(
					ni.getIntType(), ni.getLongType(),
					ni.getFloatType(), ni.getDoubleType());
		if (pt == ni.getFloatType())
			return makeList(
					ni.getFloatType(), ni.getDoubleType());
		if (pt == ni.getDoubleType())
			return makeList(ni.getDoubleType()); 
		if (pt == ni.getCharType())
			return makeList(
					ni.getCharType(), ni.getIntType(),
					ni.getLongType(), ni.getFloatType(), 
					ni.getDoubleType());
		throw new Error(); // all cases covered
	}

	public List<PrimitiveType> getSubtypes(PrimitiveType pt) {
		NameInfo ni = getNameInfo();
		if (pt == ni.getBooleanType())
			return Collections.<PrimitiveType>emptyList();
		if (pt == ni.getByteType())
			return Collections.<PrimitiveType>emptyList();
		if (pt == ni.getShortType())
			return makeList(ni.getByteType());
		if (pt == ni.getLongType())
			return makeList(ni.getIntType());
		if (pt == ni.getIntType())
			return makeList(ni.getShortType(), ni.getCharType());
		if (pt == ni.getFloatType())
			return makeList(ni.getLongType());
		if (pt == ni.getDoubleType())
			return makeList(ni.getFloatType()); 
		if (pt == ni.getCharType())
			return Collections.<PrimitiveType>emptyList();
		throw new Error(); // all cases covered
	}

	public List<PrimitiveType> getSupertypes(PrimitiveType pt) {
		NameInfo ni = getNameInfo();
		if (pt == ni.getBooleanType())
			return Collections.<PrimitiveType>emptyList();
		if (pt == ni.getByteType())
			return makeList(ni.getShortType());
		if (pt == ni.getShortType())
			return makeList(ni.getIntType());
		if (pt == ni.getLongType())
			return makeList(ni.getFloatType());
		if (pt == ni.getIntType())
			return makeList(ni.getLongType());
		if (pt == ni.getFloatType())
			return makeList(ni.getDoubleType());
		if (pt == ni.getDoubleType())
			return Collections.<PrimitiveType>emptyList();
		if (pt == ni.getCharType())
			return makeList(ni.getIntType());
		throw new Error(); // all cases covered
	}
	
	private ArrayList<PrimitiveType> makeList(PrimitiveType ... types) {
		ArrayList<PrimitiveType> res = new ArrayList<PrimitiveType>(types.length);
		for (PrimitiveType pt : types) {
			res.add(pt);
		}
		return res;
	}
	

	public Type eraseType(Type t) {
		// JLS 3rd edition, § 4.6 
		// TODO check if properly used everywhere + testcases.
		if (t instanceof ParameterizedType)
			t = ((ParameterizedType)t).getGenericType();
		// TODO "The erasure of a nested type T.C is |T|.C."
		
		int dim = 0;
		while (t instanceof ArrayType) {
			t = ((ArrayType)t).getBaseType();
			dim++;
		}
		if (dim > 0) {
			t = eraseType(t);
			for (int i = 0; i < dim; i++) {
				t = t.createArrayType();
			}
		}
		if (t instanceof PrimitiveType)
			return t;
		if (t instanceof TypeParameter) {
			t = getClassTypeFromTypeParameter((TypeParameter)t, 0);
		}
		if (t instanceof IntersectionType) {
			IntersectionType it = (IntersectionType)t;
			for (ClassType ct : it.getSupertypes()) {
				if (!ct.isInterface())
					return ct;
			}
			// uff... and now? We guess and print a warning.
			System.err.println("WARNING: cannot determine type erasure of intersection type " + it.getFullName() + " - guessing!");
			return it.getSupertypes().get(0);
		}
		if (t == null)
			return null;
		ClassType ct = (ClassType)t;
		// TODO !! 0.93 - the below is a hot-fix. It needs to be double-checked.
		// Should a type also be erased if one of its supertypes contains type parameters? 
		// 22.07.2009 - I now changed it to all-not-erased-types have their erased type as super type...
		if (!(ct instanceof ErasedType) && !(ct instanceof ArrayType)/*&& ct.getTypeParameters() != null && ct.getTypeParameters().size() > 0*/)
			t = ct.getErasedType();
		return t;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends Type> List<T> eraseTypes(List<T> types) {
		ArrayList res = new ArrayList(types.size());
		for (Type t : types) {
			res.add(eraseType(t));
		}
		return res;
	}
	
	List<Type> replaceAllTypeParameters(
			List<? extends Type> replaceIn, 
			List<? extends TypeParameter> typeParams,
			List<? extends ClassType> replaceWith) {
		ArrayList<Type> res = new ArrayList<Type>(replaceIn.size());
		for (Type t : replaceIn) {
			res.add(replaceAllTypeParameters(t, typeParams, replaceWith));
		}
		return res;
	}
	
	// TODO This requires some clean up, I guess...
	Type replaceAllTypeParameters(
			Type replaceIn,
			List<? extends TypeParameter> typeParams,
			List<? extends ClassType> replaceWith) {
		Type repl = replaceIn;
		int dim = 0;
		while (repl instanceof ArrayType) {
			repl = ((ArrayType)repl).getBaseType();
			dim++;
		}
		if (repl instanceof PrimitiveType) {
			return replaceIn; // maybe an array type of a primitive type, so take original!
		}
		List<? extends TypeArgument> targs = null;
		if (repl instanceof ParameterizedType) {
			targs = ((ParameterizedType)repl).getAllTypeArgs();
			repl = ((ParameterizedType)repl).getGenericType();
		}
		if (replaceWith.contains(null))
			targs = null; // must be erased. We drop type args!
		int i = -1;
		for (TypeParameter tp : typeParams) {
			i++;
			// goByName is used for generic methods from bytecode...
			if (repl instanceof TypeParameter) {
				if (tp == repl) { // QQQQ
//				if (tp.inheritanceEqual((TypeParameter)repl)) { /
					//			if ((goByName && tp.getName() == repl.getName()) || tp.equals(repl)) {
					if (replaceWith.get(i) == null) {
						// we can stop here - return type is being replaced by
						// erasure of leftmost bound, and that's all that we need to do.
						repl = getNameInfo().getClassType(tp.getBoundName(0)).getErasedType();
						targs = null;
						break;
					}
					repl = replaceWith.get(i);
				}
			} else if (targs != null) { // TODO really "else" ?
				// TODO clean this up!
				ArrayList<TypeParameter> single_tp = new ArrayList<TypeParameter>(1);
				single_tp.add(tp);
				ArrayList<TypeArgument> single_cp = new ArrayList<TypeArgument>(1);
				single_cp.add(new WrappedTypeArgument(replaceWith.get(i)));
				targs = replaceAllTypeParametersWithArgsInArgs(targs, 
						single_tp, single_cp);
			}
		}
		if (targs != null)
			repl = getServiceConfiguration().getImplicitElementInfo().getParameterizedType((ClassType)repl,  targs);
		while (dim-- > 0)
			repl = repl.createArrayType();
		return repl;
	}
	
	// TODO DOC
	@SuppressWarnings({ "unchecked", "rawtypes" }) // we only put in what we get out...
	public <X extends Type> List<X> replaceAllTypeParametersWithArgs(
			List<X> replaceIn,
			List<? extends TypeParameter> typeParams,
			List<? extends TypeArgument> replaceWith) {
		ArrayList res = new ArrayList(replaceIn.size());
		for (Type t : replaceIn) {
			res.add(replaceAllTypeParametersWithArgs(t, typeParams, replaceWith));
		}
		return res;
	}
	
	private static class WrappedTypeArgument implements TypeArgument {
		final ClassType type;
		WrappedTypeArgument(ClassType ct) {
			this.type = ct;
		}
		public String getFullSignature() {
			return type.getFullSignature();
		}
		public TypeParameter getTargetedTypeParameter() {
			// TODO Auto-generated method stub
			return null;
		}
		public List<? extends TypeArgument> getTypeArguments() {
			if (type instanceof ParameterizedType) {
				return ((ParameterizedType)type).getTypeArgs();
			}
			return null;
		}
		public String getTypeName() {
			return type.getFullName();
		}
		public WildcardMode getWildcardMode() {
			return WildcardMode.None;
		}
		public boolean semanticalEquality(TypeArgument ta) {
			return TypeArgument.EqualsImpl.equals(this, ta, (DefaultImplicitElementInfo)type.getProgramModelInfo().getServiceConfiguration().getImplicitElementInfo());
		}
		public int semanticalHashCode() {
			return TypeArgument.EqualsImpl.semanticalHashCode(this);
		}
		public boolean isTypeVariable() {
			return false;
		}
	}
	
	/**
	 * Helper class used for method matching, if type parameters are used in bounds.
	 * @author Tobias Gutzmann
	 *
	 */
	private class ResolvedTypeParameter implements TypeParameter {
		private final TypeParameter original;
		private final Map<Integer, ClassType> repls;
		
		ResolvedTypeParameter(TypeParameter original, Map<Integer, ClassType> repls) {
			this.original = original;
			this.repls = repls;
		}
		@Override
		public boolean isInterface() {
			return false;
		}

		@Override
		public boolean isOrdinaryInterface() {
			return false;
		}

		@Override
		public boolean isAnnotationType() {
			return false;
		}

		@Override
		public boolean isEnumType() {
			return false;
		}

		@Override
		public boolean isOrdinaryClass() {
			return false;
		}

		@Override
		public boolean isAbstract() {
			return false;
		}

		@Override
		public boolean isInner() {
			return false;
		}

		@Override
		public List<ClassType> getSupertypes() {
			List<ClassType> origSupers = original.getSupertypes();
			List<ClassType> res = new ArrayList<ClassType>(origSupers.size());
			for (int i = 0; i < original.getBoundCount(); i++) {
				ClassType ct = repls.get(i);
				if (ct != null)
					res.add(ct);
				else 
					res.add(origSupers.get(i));
			}
			return res;
		}

		@Override
		public List<ClassType> getAllSupertypes() {
			return DefaultProgramModelInfo.this.getAllSupertypes(this);
		}

		@Override
		public List<? extends Field> getFields() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Field> getAllFields() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Method> getMethods() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Method> getAllMethods() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<? extends Constructor> getConstructors() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<ClassType> getAllTypes() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<? extends TypeParameter> getTypeParameters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClassType getErasedType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClassType getBaseClassType() {
			return original.getBaseClassType();
		}

		@Override
		public ArrayType getArrayType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ArrayType createArrayType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getFullSignature() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getFullName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getBinaryName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ProgramModelInfo getProgramModelInfo() {
			return original.getProgramModelInfo();
		}

		@Override
		public void setProgramModelInfo(ProgramModelInfo pmi) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return original.getName();
		}

		@Override
		public void validate() throws ModelException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isFinal() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isStatic() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isPrivate() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isProtected() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isPublic() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isStrictFp() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClassType getContainingClassType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<? extends AnnotationUse> getAnnotations() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Member getGenericMember() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<? extends ClassType> getTypes() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Package getPackage() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClassTypeContainer getContainer() {
			return null;
		}

		@Override
		public String getParameterName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getBoundCount() {
			return original.getBoundCount();
		}

		@Override
		public String getBoundName(int boundidx) {
			ClassType ct = repls.get(boundidx);
			if (ct != null) {
				if (ct instanceof TypeParameter)
					return ct.getName();
				else
					return ct.getFullName();
			}
			return original.getBoundName(boundidx);
		}

		@Override
		public List<? extends TypeArgument> getBoundTypeArguments(int boundidx) {
			ClassType ct = repls.get(boundidx);
			if (ct != null) {
				if (ct instanceof ParameterizedType)
					return ((ParameterizedType)ct).getTypeArgs();
				return null;
			}
			return original.getBoundTypeArguments(boundidx);
		}
		
	}
	
	public class ResolvedTypeArgument implements TypeArgument {
		final WildcardMode wm;
		final ClassType type;
		final List<? extends TypeArgument> typeArgs;
		final TypeParameter targetedTypeParameter;
		
		public ResolvedTypeArgument(WildcardMode wm, ClassType type, List<? extends TypeArgument> typeArgs, TypeParameter targetedTypeParameter) {
			this.wm = wm;
			// TODO check when/why this happens!!
			if (type instanceof ParameterizedType)
				type = ((ParameterizedType)type).getGenericType();
			this.type = type;
			this.typeArgs = (typeArgs == null ? Collections.<TypeArgument>emptyList() : typeArgs);
			this.targetedTypeParameter = targetedTypeParameter;
			assert wm == WildcardMode.Any ^ type != null;
		}
		
		public WildcardMode getWildcardMode() {
			return wm;
		}

		public String getTypeName() {
			// TODO make this better, this is a quick fix!!
			if (type instanceof ClassDeclaration && ((ClassDeclaration)type).getASTParent() instanceof New) {
				return Naming.toPathName(((New)((ClassDeclaration)type).getASTParent()).getTypeReference());
			} else if (wm == WildcardMode.Any) {
				return null;
			} else return type.getFullName();
		}

		public List<? extends TypeArgument> getTypeArguments() {
			return typeArgs;
		}
		
		public boolean semanticalEquality(TypeArgument o) {
			return TypeArgument.EqualsImpl.equals(this, o, (DefaultImplicitElementInfo)DefaultProgramModelInfo.this.getServiceConfiguration().getImplicitElementInfo());
		}
		public int semanticalHashCode() {
			return TypeArgument.EqualsImpl.semanticalHashCode(this);
		}
		public String getFullSignature() {
			if (wm == null)
				return getTypeName();
			return TypeArgument.DescriptionImpl.getFullDescription(this);
		}

		public TypeParameter getTargetedTypeParameter() {
			return targetedTypeParameter;
		}

		public boolean isTypeVariable() {
			return false;
		}
	}

	public <X extends Type> X replaceAllTypeParametersWithArgs(
			X replaceIn,
			List<? extends TypeParameter> typeParams,
			List<? extends TypeArgument> replaceWith) {
		return replaceAllTypeParametersWithArgs(
				replaceIn, typeParams, replaceWith, Collections.<TypeParameter>emptyList());
	}
	
	@SuppressWarnings("unchecked")
	<X extends Type> X replaceAllTypeParametersWithArgs(
			X replaceIn,
			List<? extends TypeParameter> typeParams,
			List<? extends TypeArgument> replaceWith,
			List<? extends TypeParameter> skip) {
		if (skip == null) skip = Collections.<TypeParameter>emptyList();
		// TODO this is a quick solution only, no capture-conversion performed
		// e.g.: List<? extends X> shouldn't allow add(X)... 
		// so, replace getBaseType() with something more useful...
		Type replaced = replaceIn;
		int dim = 0;
		while (replaced instanceof ArrayType) {
			replaced = ((ArrayType)replaced).getBaseType();
			dim++;
		}
		if (replaced instanceof PrimitiveType) {
			return replaceIn; // maybe an array type of a primitive type, so take original!
		}
		List<? extends TypeArgument> targs = null;
		if (replaced instanceof ParameterizedType) {
			targs = ((ParameterizedType)replaced).getAllTypeArgs();
			replaced = ((ParameterizedType)replaced).getGenericType();
		} else if (replaced instanceof TypeParameter) {
			// check if type parameter is found in bounds:
			TypeParameter tp = (TypeParameter)replaced;
			HashMap<Integer, ClassType> repls = new HashMap<Integer, ClassType>();
			for (int i = 0; i < tp.getBoundCount(); i++) {
				for (TypeParameter toRepl : typeParams) {
					if (tp.getBoundName(i).equals(toRepl.getName())) {
						repls.put(i, getCapture(replaceWith.get(i))); // TODO probably not correct
					}
				}
			}
			if (repls.size() > 0)
				replaced = new ResolvedTypeParameter((TypeParameter)replaced, repls);
		}
		int i = -1;
		// TODO this may not be entirely proper code :-/
		OUTER: for (TypeParameter tp : typeParams) {
			i++;
			if (tp.equals(replaced)) {
				// bugfix for bug 2046005 
				// TODO this is an ugly solution!!
				for (TypeParameter sk : skip) {
					if (sk.getName() == replaced.getName())
						// why this: generic method may have a type parameter with same name
						// as it's containing class has... Ugly code, but we have to cope with it.
						continue OUTER;
				}
				// TODO shouldn't be checked if the bounds match ? 
				replaced = getCapture(replaceWith.get(i)); 
				while (dim-- > 0)
					replaced = replaced.createArrayType();
				return (X)replaced;
			}
		}
		// no? then look in type arguments instead...
		if (targs != null) {
			targs = replaceAllTypeParametersWithArgsInArgs(targs, typeParams, replaceWith);
			replaced = getServiceConfiguration().getImplicitElementInfo().getParameterizedType((ClassType)replaced,  targs);
		}
		while (dim-- > 0)
			replaced = replaced.createArrayType();
		return (X)replaced;
	}
	
	private List<TypeArgument> replaceAllTypeParametersWithArgsInArgs(
			List<? extends TypeArgument> replaceIn, 
			List<? extends TypeParameter> typeParams,
			List<? extends TypeArgument> replaceWith) {
		ArrayList<TypeArgument> res = new ArrayList<TypeArgument>(replaceIn.size());
		for (TypeArgument t : replaceIn) {
			res.add(replaceAllTypeParametersWithArgs(t, typeParams, replaceWith));
		}
		return res;
	}
	
	private TypeArgument replaceAllTypeParametersWithArgs(
			TypeArgument replaceIn,
			List<? extends TypeParameter> typeParams,
			List<? extends TypeArgument> replaceWith) {
		if (replaceIn.isTypeVariable()) {
			if (replaceIn.getWildcardMode() == WildcardMode.Super) {
				return new ResolvedTypeArgument(WildcardMode.None, getNameInfo().getJavaLangObject(), null, null); // TODO check last param
			}
			// can stop here afterwards, no further recursive call...
			// this MUST match one type parameter!
			ClassType repl = getBaseType(replaceIn);
			int addDim = 0;
			while (repl instanceof ArrayType) {
				// This happens when arrays of type parameters are used in type arguments.
				repl = (ClassType)((ArrayType)repl).getBaseType();
				addDim++;
			}
			
			TypeParameter bt = (TypeParameter)repl;
			for (int i = 0; i < typeParams.size(); i++) {
				if (bt == typeParams.get(i))
					return addDimensions(replaceWith.get(i), addDim);
				for (int j = 0; j < bt.getBoundCount(); j++) {
					// "second chance"
					if (bt.getBoundName(j).equals(typeParams.get(i).getName())) {
						return addDimensions(replaceWith.get(i), addDim);
					}
				}
			}
			// okej, this MAY be okej if the parameter actually belongs to a generic method. But WHY isn't that resolved before? 
			// TODO we let it pass for now, but investigate!
			return addDimensions(replaceIn, addDim);
		}
		List<? extends TypeArgument> targs = replaceIn.getTypeArguments();
		if (targs != null && targs.size() > 0) {
			List<? extends TypeArgument> new_targs =
				replaceAllTypeParametersWithArgsInArgs(targs, typeParams, replaceWith);
			return new ResolvedTypeArgument(WildcardMode.None, getBaseType(replaceIn), new_targs, null);  // TODO check last param
		} else return replaceIn; // nothing to be done
	}
	
	private TypeArgument addDimensions(TypeArgument ta, int dim) {
		if (dim == 0)
			return ta;
		ClassType ct = getBaseType(ta);
		for (int i = 0; i < dim; i++)
			ct = ct.createArrayType();
		return new ResolvedTypeArgument(ta.getWildcardMode(), ct, ta.getTypeArguments(), ta.getTargetedTypeParameter());
	}

	// TODO DOC
	public ClassType getCapture(TypeArgument ta) {
		switch (ta.getWildcardMode()) {
		case None    : return getBaseType(ta);
		case Any     : 
		case Extends :  
		case Super   : return new TypeArgument.CapturedTypeArgument(ta, 
				getServiceConfiguration().getImplicitElementInfo());
		default: throw new RuntimeException(); // all cases covered.
		}
	}

    boolean java5Allowed() {
    	return serviceConfiguration.getProjectSettings().java5Allowed();
    }
    
	public boolean isMatchingTypeArg(TypeArgument typeArgument,
			TypeParameter typeParameter) {
		if (typeArgument.getWildcardMode() == WildcardMode.Any) {
			// This can occur in variable declarations only, where it's always allowed. 
			return true;
		} else if (typeArgument.getWildcardMode() == WildcardMode.Super) {
			return paramMatches(typeParameter, getCapture(typeArgument), false);
		} else {
			return paramMatches(getCapture(typeArgument), typeParameter, false);
		}
	}

}

