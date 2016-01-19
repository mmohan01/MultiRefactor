// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import recoder.ServiceConfiguration;
import recoder.abstraction.AnnotationProperty;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.Constructor;
import recoder.abstraction.DummyGetClassMethod;
import recoder.abstraction.ErasedConstructor;
import recoder.abstraction.ErasedField;
import recoder.abstraction.ErasedMethod;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.ParameterizedConstructor;
import recoder.abstraction.ParameterizedField;
import recoder.abstraction.ParameterizedMethod;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.ResolvedGenericMethod;
import recoder.abstraction.Type;
import recoder.abstraction.Variable;
import recoder.convenience.Format;
import recoder.convenience.TreeWalker;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.Import;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.Reference;
import recoder.java.declaration.AnnotationElementValuePair;
import recoder.java.declaration.InheritanceSpecification;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.expression.operator.New;
import recoder.java.reference.AnnotationPropertyReference;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.TypeReferenceContainer;
import recoder.java.reference.UncollatedReferenceQualifier;
import recoder.java.reference.VariableReference;
import recoder.util.Debug;
import recoder.util.ProgressEvent;

/**
 * Implements queries for cross referencing.
 * 
 * @author AL
 */
public class DefaultCrossReferenceSourceInfo extends DefaultSourceInfo implements CrossReferenceSourceInfo {

    /**
     * Cache mapping elements to known references.
     */
	private final Map<ProgramModelElement, Set<Reference>>
		element2references = new HashMap<ProgramModelElement, Set<Reference>>(256);

    /**
     * Creates a new service.
     * 
     * @param config
     *            the configuration this services becomes part of.
     */
    public DefaultCrossReferenceSourceInfo(ServiceConfiguration config) {
        super(config);
    }

    /**
     * Change notification callback method.
     * 
     * @param config
     *            the configuration this services becomes part of.
     */
    public void modelChanged(ChangeHistoryEvent changes) {

        // Java allows use before definition, so as the first pass we
        // have to update scopes; this is done by the SourceInfo.

        // super.modelChanged(changes);

        List<TreeChange> changed = changes.getChanges();
        int s = changed.size();

        int c = 0;
        listeners.fireProgressEvent(0, 3 * s, "Building Scopes");

        // detached first
        for (int i = 0; i < s; i += 1) {
            TreeChange tc = changed.get(i);
            if (tc instanceof DetachChange) {
                if (!tc.isMinor()) {
                    processChange(tc);
                }
                listeners.fireProgressEvent(++c);
            }
        }
        for (int i = 0; i < s; i += 1) {
            TreeChange tc = changed.get(i);
            if (tc instanceof AttachChange) {
                if (!tc.isMinor()) {
                    processChange(tc);
                }
                listeners.fireProgressEvent(++c);
            }
        }

        listeners.fireProgressEvent(c, "Resolving References");

        TreeWalker tw = new TreeWalker(null);
        // TreeChangeList changed = changes.getChanges();
        // int s = changed.size();
        for (int i = 0; i < s; i += 1) {
            TreeChange tc = changed.get(i);
            if (tc instanceof DetachChange) {
                if (!tc.isMinor()) {
                    ProgramElement pe = tc.getChangeRoot();
                    if (pe instanceof TypeArgumentDeclaration) {
                    	// this may invalidate the parent reference...
                    	reset(true);
                    	return;
                    } else if (pe instanceof Reference) {
                    	// TODO we can most probably handle AnnotationUse more efficiently
                    	
                    	// We would have to remove this reference from
                    	// the entity it belongs to and do this
                    	// transitively for the outer reference.
                    	// If this is a type reference in
                    	// an inheritance specification, we have a problem.
                    	reset(true);
                    	return;
                    } else if (pe instanceof ProgramModelElement || pe instanceof VariableDeclaration) {
                        // We would have to revalidate all references to
                        // this element.
                    	// TODO deleting a single constructor which takes no parameters can be improved! (regardless of visibility)
                        reset(true);
                        return;
                    } else if (pe instanceof InheritanceSpecification) {
                        // References to supertype members may have changed
                        reset(true);
                        return;
                    } else if (pe instanceof AnnotationElementValuePair) {
                    	// may invalidate the reference of an annotation use
                    	reset(true);
                    	return;
                    } else if (pe instanceof Import) {
                    	// TODO update only the current CU
                    	reset(true);
                    	return;
                    }
                    tw.reset(pe);
                    tw.next(); // skip pe
                    while (tw.next()) {
                        ProgramElement p = tw.getProgramElement();
                        if (p instanceof Reference) {
                            deregisterReference((Reference) p);
                        }
                    }
                }
                listeners.fireProgressEvent(++c);
            }
        }
        for (int i = 0; i < s; i += 1) {
            TreeChange tc = changed.get(i);
            if (tc instanceof AttachChange) {
                if (!tc.isMinor()) {
                    ProgramElement pe = tc.getChangeRoot();
//                    NonTerminalProgramElement pa = tc.getChangeRootParent();
                    if (pe instanceof TypeArgumentDeclaration) {
                    	// this may invalidate the parent reference...
                    	reset(true);
                    	return;
                    } else if (pe instanceof Reference) {
                    	// TODO we can most probably handle AnnotationUse more efficiently
                        
                    	// We would have to handle type references in
                    	// inheritance specifications. Hard work.
                    	reset(true);
                    	return;
                    } else if (pe instanceof ProgramModelElement || pe instanceof VariableDeclaration) {
                        // We would have to find out whether this element
                        // hides some other element that is already referred to.
                        // If so, we must revalidate those elements.
                    	// Further, overloading must be considered.
                        // Program model elements in subtrees are not relevant
                        // as they must be in an inner scope (really?) and
                        // cannot have been referred to.
                    	// TODO replacing a default constructor can be improved, if proper visibility
                        reset(true);
                        return;
                    } else if (pe instanceof InheritanceSpecification) {
                        // References to supertype members may have changed
                        reset(true);
                        return;
                    } else if (pe instanceof AnnotationElementValuePair) {
                    	// may invalidate the reference of an annotation use
                    	reset(true);
                    	return;
                    } else if (pe instanceof Import) {
                    	// TODO update only the current CU
                    	reset(true);
                    	return;
                    }
                }
                listeners.fireProgressEvent(++c);
            }
        }
        for (int i = 0; i < s; i += 1) {
            TreeChange tc = changed.get(i);
            if (!tc.isMinor() && (tc instanceof AttachChange)) {
                AttachChange ac = (AttachChange) tc;
                ProgramElement pe = ac.getChangeRoot();
                // we expect that scopes are valid
                analyzeReferences(pe);
            }
            listeners.fireProgressEvent(++c);
        }
    }

    /**
     * Retrieves the list of references to a given method (or constructor). The
     * references stem from all known compilation units.
     * 
     * @param m
     *            a method.
     * @return a possibly empty list of references to the given method.
     */
    public List<MemberReference> getReferences(Method m) {
        Debug.assertNonnull(m);
        updateModel();
        
        m = getBaseMethod(m);
        
        Set<Reference> references =  element2references.get(m);
        if (references == null) {
            return Collections.emptyList();
        }
        int s = references.size();
        if (s == 0) {
            return Collections.emptyList();
        }
        List<MemberReference> result = new ArrayList<MemberReference>(s);
        for (Reference o : references) {
            result.add((MemberReference)o);
        }
        return result;
    }

    /**
     * Retrieves the list of references to a given constructor. The references
     * stem from all known compilation units.
     * 
     * @param c
     *            a constructor.
     * @return a possibly empty list of references to the given constructor.
     */
    public List<ConstructorReference> getReferences(Constructor c) {
        Debug.assertNonnull(c);
        updateModel();
        
        c = getBaseConstructor(c);
        
        Set<Reference> references = element2references.get(c);
        if (references == null) {
            return Collections.emptyList();
        }
        int s = references.size();
        if (s == 0) {
            return Collections.emptyList();
        }
        List<ConstructorReference> result = new ArrayList<ConstructorReference>(s);
        for (Reference o : references) {
            result.add((ConstructorReference)o);
        }
        return result;
    }

    /**
     * Retrieves the list of references to a given variable. The references stem
     * from all known compilation units.
     * 
     * @param v
     *            a variable.
     * @return a possibly empty list of references to the given variable.
     */
    public List<VariableReference> getReferences(Variable v) {
        Debug.assertNonnull(v);
        updateModel();
        Set<Reference> references =  element2references.get(v);
        if (references == null) {
            return Collections.emptyList();
        }
        int s = references.size();
        if (s == 0) {
            return Collections.emptyList();
        }
        List<VariableReference> result = new ArrayList<VariableReference>(s);
        for (Reference o : references) {
            result.add((VariableReference) o);
        }
        return result;
    }

    /**
     * Retrieves the list of references to a given field. The references stem
     * from all known compilation units.
     * 
     * @param f
     *            a field.
     * @return a possibly empty list of references to the given field, but never <code>null</code>.
     */
    public List<FieldReference> getReferences(Field f) {
        Debug.assertNonnull(f);
        updateModel();
        
        f = (Field)getBaseField(f);
        
        Set<Reference> references = element2references.get(f);
        if (references == null) {
            return Collections.emptyList();
        }
        int s = references.size();
        if (s == 0) {
            return Collections.emptyList();
        }
        List<FieldReference> result = new ArrayList<FieldReference>(s);
        for (Reference r : references) {
        	result.add((FieldReference)r);
        }
        return result;
    }

    /**
     * Retrieves the list of references to a given type. The references stem
     * from all known compilation units.
     * 
     * @param t
     *            a type.
     * @return a possibly empty list of references to the given type.
     */
    public List<TypeReference> getReferences(Type t) {
        Debug.assertNonnull(t);
        updateModel();
        
        t = getBaseType(t);
        
        Set<Reference> references =  element2references.get(t);
        if (references == null) {
            return Collections.emptyList();
        }
        int s = references.size();
        if (s == 0) {
            return Collections.emptyList();
        }
        List<TypeReference> result = new ArrayList<TypeReference>(s);
        for (Reference r : references) {
            result.add((TypeReference)r);
        }
        return result;
    }
    
    public List<TypeReference> getReferences(Type t, boolean includeArrayTypes) {
    	if (!includeArrayTypes) return getReferences(t);
    	if (t == null) throw new NullPointerException();
    	updateModel();
    	
    	ArrayList<TypeReference> result = new ArrayList<TypeReference>();
    	
    	t = getBaseType(t);
    	
    	do {
    		Set<Reference> references = element2references.get(t);
    		if (references != null) {
    			for (Reference r : references) {
    				result.add((TypeReference)r);
    			}
    		}
    		t = t.getArrayType();
    	} while (t != null);
    	result.trimToSize();
    	return result;
    }

    /**
     * Retrieves the list of references to a given package. The references stem
     * from all known compilation units.
     * 
     * @param p
     *            a package.
     * @return a possibly empty list of references to the given package.
     */
    public List<PackageReference> getReferences(Package p) {
        Debug.assertNonnull(p);
        updateModel();
        Set<Reference> references =  element2references.get(p);
        if (references == null) {
            return Collections.emptyList();
        }
        int s = references.size();
        if (s == 0) {
            return Collections.emptyList();
        }
        List<PackageReference> result = new ArrayList<PackageReference>(s);
        for (Reference pr : references) {
            result.add((PackageReference)pr);
        }
        return result;
    }
    
    private void registerVariableReference(VariableReference ref, Variable v) {
    	v = getBaseField(v);
    	registerReference(ref, v);
    }

	private Variable getBaseField(Variable v) {
		if (v instanceof ErasedField)
    		v = ((ErasedField)v).getGenericField();
    	while (v instanceof ParameterizedField)
    		v = ((ParameterizedField)v).getGenericField();
		return v;
	}
    
    private void registerMethodReference(MethodReference ref, Method m) {
    	m = getBaseMethod(m);
    	registerReference(ref, m);
    }

	private Method getBaseMethod(Method m) {
		if (m instanceof DummyGetClassMethod)
    		m = findObjectGetClass();
    	if (m instanceof ErasedMethod)
    		m = ((ErasedMethod)m).getGenericMethod();
    	while (m instanceof ParameterizedMethod)
    		m = ((ParameterizedMethod)m).getGenericMethod();
    	if (m instanceof ResolvedGenericMethod)
    		m = ((ResolvedGenericMethod)m).getGenericMethod();
    	while (m instanceof ParameterizedMethod) // TODO twice necessary?
    		m = ((ParameterizedMethod)m).getGenericMethod();
		return m;
	}
    
    private Method findObjectGetClass() {
    	ClassType ct = getNameInfo().getJavaLangObject();
    	for (Method m : ct.getMethods()) {
    		if (m.getName() == GETCLASS_NAME)
    			return m;
    	}
    	throw new Error(); // can't happen.
    }
    
    private void registerConstructorReference(ConstructorReference ref, Constructor c) {
    	c = getBaseConstructor(c);
    	registerReference(ref, c);
    }

	private Constructor getBaseConstructor(Constructor c) {
		if (c instanceof ErasedConstructor)
    		c = (Constructor)((ErasedConstructor)c).getGenericMethod();
    	if (c instanceof ParameterizedConstructor)
    		c = (Constructor)((ParameterizedConstructor)c).getGenericMethod();
		return c;
	}
    
    private void registerAnnotationPropertyReference(AnnotationPropertyReference ref, AnnotationProperty pme) {
    	registerReference(ref, pme);
    }
    
    private void registerTypeReference(TypeReference ref, Type t) {
    	// CapturedType ErasedType CapturedTypeArgument ParameterizedType
    	t = getBaseType(t);
    	registerReference(ref, t);
    }

	private Type getBaseType(Type t) {
		int dim = 0;
		while (t instanceof ArrayType) {
			t = ((ArrayType)t).getBaseType();
			dim++;
		}
    	if (t instanceof ErasedType)
    		t = ((ErasedType)t).getGenericType();
    	// TODO CapturedTypeArgument ??
    	while (t instanceof ParameterizedType)
    		t = ((ParameterizedType)t).getGenericType();
    	for (int i = 0; i < dim; i++) {
    		t = t.createArrayType();
    	}
		return t;
	}
    
    private void registerPackageReference(PackageReference ref, Package pme) {
    	registerReference(ref, pme);
    }
    
    private void registerReference(Reference ref, ProgramModelElement pme) {
        Set<Reference> set = element2references.get(pme);
        if (set == null) {
            element2references.put(pme, set = new HashSet<Reference>(4));
        }
        set.add(ref);
    }

    private void deregisterReference(Reference ref) {
        ProgramModelElement pme = reference2element.get(ref);
        if (pme == null) {
            // ThisReference
            return;
        }
        Set<Reference> set = element2references.get(pme);
        if (set == null) {
            // ThisReference
            return;
        } else {
            set.remove(ref);
        }
    }

    private FastWorkList worklist = new FastWorkList();
    private Stack<NonTerminalProgramElement> NTEs = new Stack<NonTerminalProgramElement>();
    /**
     * Collects all Method-, Constructor-, Variable- and TypeReferences in the
     * subtree.
     */
    private void analyzeReferences(ProgramElement pe) {
    	worklist.add(pe);
    	NTEs.push(null); // dummy item
    	while (!worklist.isEmpty()) {
    		pe = worklist.peekLast();
    		if (pe instanceof NonTerminalProgramElement) {
    			NonTerminalProgramElement nt = (NonTerminalProgramElement)pe;
    			if (NTEs.peek() != nt) {
    				NTEs.push(nt);
    				for (int i = nt.getChildCount()-1; i >= 0; i--)
    					worklist.add(nt.getChildAt(i));
    				continue; // continue in subtree.
    			}
    			worklist.removeLast(); // all children have been visited.
    			NTEs.pop();
    		} else {
    			worklist.removeLast(); // terminal. skip and continue.
    			continue;
    		}
//    	{
//    		if (pe instanceof NonTerminalProgramElement) {
//    			NonTerminalProgramElement nt = (NonTerminalProgramElement) pe;
//    			for (int i = 0, c = nt.getChildCount(); i < c; i += 1) {
//    				analyzeReferences(nt.getChildAt(i));
//    			}
//    		} else {
//    			return;
//    		}
    		if (pe instanceof Reference) {
    			if (pe instanceof UncollatedReferenceQualifier) {
    				try {
    					pe = resolveURQ((UncollatedReferenceQualifier) pe);
    				} catch (ClassCastException cce) {
    					getErrorHandler().reportError(
    							new UnresolvedReferenceException(Format.toString("Could not resolve " + ELEMENT_LONG + "(CR1)", pe),
    									pe));
    					// this might have been a field or class or package
    					// we have to let this URQ remain alive
    				}
    			}
    			// no else
    			if (pe instanceof VariableReference) {
    				VariableReference vr = (VariableReference) pe;
    				Variable v = getVariable(vr);
    				if (v == null) {
    					getErrorHandler().reportError(
    							new UnresolvedReferenceException(Format.toString("Could not resolve " + ELEMENT_LONG + "(CR2)", vr),
    									vr));
    					v = getNameInfo().getUnknownField();
    				}
    				registerVariableReference(vr, v);
    			} else if (pe instanceof TypeReference) {
    				TypeReference tr = (TypeReference) pe;
    				Type t = getType(tr);
    				if (t != null) { // void type otherwise
    					if (!(t instanceof DefaultNameInfo.UnknownClassType)) {
    						registerTypeReference(tr, t);
    						if (t instanceof ClassType) {
        						t = ((ClassType)t).getBaseClassType();
    							ClassType subType = null;
    							TypeReferenceContainer parent = tr.getParent();
    							if (parent instanceof InheritanceSpecification) {
    								subType = ((InheritanceSpecification) parent).getParent();
    							} else if (parent instanceof New) {
    								subType = ((New) parent).getClassDeclaration();
    							}
    							// TODO 0.90 adapt for enums !
    							if (subType != null) {
    								ClassType superType = (ClassType) t;
    								ProgramModelInfo pmi = superType.getProgramModelInfo();
    								((DefaultProgramModelInfo) pmi).registerSubtype(subType, superType);
    							}
    						}
    					}
    				}
    			} else if (pe instanceof MethodReference) {
    				MethodReference mr = (MethodReference) pe;
    				Method m = (Method)getMethod(mr).getGenericMember();
    				registerMethodReference(mr, m);
    			} else if (pe instanceof ConstructorReference) {
    				ConstructorReference cr = (ConstructorReference) pe;
    				Constructor c = (Constructor)getConstructor(cr).getGenericMember();
    				registerConstructorReference(cr, c);
    			} else if (pe instanceof AnnotationPropertyReference) {
    				AnnotationPropertyReference apr = (AnnotationPropertyReference)pe;
    				AnnotationProperty ap = getAnnotationProperty(apr);
    				registerAnnotationPropertyReference(apr, ap);
    			} else if (pe instanceof PackageReference) {
    				PackageReference pr = (PackageReference) pe;
    				Package p = getPackage(pr);
    				registerPackageReference(pr, p);
    			}
    		}
    	}
    }

    class SubTypeTopSort extends ClassTypeTopSort {

        protected final List<ClassType> getAdjacent(ClassType c) {
            return getSubtypes(c);
        }
    }


    public String information() {
        updateModel();
        int c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0;
        int r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0;
        for (ProgramModelElement pme : element2references.keySet()) {
            Set<Reference> set = element2references.get(pme);
            int size = set == null ? 0 : set.size();
            if (pme instanceof Variable) {
                c1 += 1;
                r1 += size;
            } else if (pme instanceof Method) {
                if (pme instanceof Constructor) {
                    c3 += 1;
                    r3 += size;
                } else {
                    c2 += 1;
                    r2 += size;
                }
            } else if (pme instanceof Type) {
                c4 += 1;
                r4 += size;
            } else if (pme instanceof Package) {
                c5 += 1;
                r5 += size;
            }
        }
        return "" + c1 + " variables with " + r1 + " references\n" + c2 + " methods with " + r2 + " references\n" + c3
                + " constructors with " + r3 + " references\n" + c4 + " types with " + r4 + " references\n" + c5
                + " packages with " + r5 + " references";
    }

    private void reset(boolean fire) {
        super.reset();
        element2references.clear();
        SourceFileRepository sfr = serviceConfiguration.getSourceFileRepository();
        List<CompilationUnit> cul = sfr.getCompilationUnits();
        int c = 0;
        if (fire) {
            ProgressEvent pe = listeners.getLastProgressEvent();
            c = pe.getWorkDoneCount();
            listeners.fireProgressEvent(c, c + cul.size());
        }
        for (int i = cul.size() - 1; i >= 0; i -= 1) {
            CompilationUnit cu = cul.get(i);
            analyzeReferences(cu);
            if (fire) {
                listeners.fireProgressEvent(++c);
            }
        }
    }

    /**
     * Resets all observable parts of the model.
     */
    public void reset() {
        reset(false);
    }
}

