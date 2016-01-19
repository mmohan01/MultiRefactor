// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import recoder.IllegalTransformationException;
import recoder.ModelException;
import recoder.ProgramFactory;
import recoder.ServiceConfiguration;
import recoder.abstraction.AnnotationProperty;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.EnumConstant;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.ImplicitEnumMethod;
import recoder.abstraction.IntersectionType;
import recoder.abstraction.Member;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.ParameterizedMethod;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.ResolvedGenericMethod;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeArgument.CapturedTypeArgument;
import recoder.abstraction.TypeArgument.WildcardMode;
import recoder.abstraction.TypeParameter;
import recoder.abstraction.Variable;
import recoder.convenience.Format;
import recoder.convenience.Formats;
import recoder.convenience.Naming;
import recoder.convenience.TreeWalker;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.Identifier;
import recoder.java.Import;
import recoder.java.NonTerminalProgramElement;
import recoder.java.PackageSpecification;
import recoder.java.ProgramElement;
import recoder.java.Reference;
import recoder.java.ScopeDefiningElement;
import recoder.java.Statement;
import recoder.java.StatementBlock;
import recoder.java.StatementContainer;
import recoder.java.TerminalProgramElement;
import recoder.java.TypeScope;
import recoder.java.VariableScope;
import recoder.java.declaration.AnnotationDeclaration;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ClassInitializer;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.EnumConstantDeclaration;
import recoder.java.declaration.EnumConstantSpecification;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.Extends;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.Implements;
import recoder.java.declaration.InheritanceSpecification;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.ParameterDeclaration;
import recoder.java.declaration.Throws;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeDeclarationContainer;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.declaration.UnionTypeParameterDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.declaration.VariableSpecification;
import recoder.java.expression.ArrayInitializer;
import recoder.java.expression.Assignment;
import recoder.java.expression.ElementValueArrayInitializer;
import recoder.java.expression.Literal;
import recoder.java.expression.Operator;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.literal.BooleanLiteral;
import recoder.java.expression.literal.CharLiteral;
import recoder.java.expression.literal.DoubleLiteral;
import recoder.java.expression.literal.FloatLiteral;
import recoder.java.expression.literal.IntLiteral;
import recoder.java.expression.literal.LongLiteral;
import recoder.java.expression.literal.NullLiteral;
import recoder.java.expression.literal.StringLiteral;
import recoder.java.expression.operator.BinaryAnd;
import recoder.java.expression.operator.BinaryNot;
import recoder.java.expression.operator.BinaryOr;
import recoder.java.expression.operator.BinaryXOr;
import recoder.java.expression.operator.ComparativeOperator;
import recoder.java.expression.operator.Conditional;
import recoder.java.expression.operator.CopyAssignment;
import recoder.java.expression.operator.Divide;
import recoder.java.expression.operator.Instanceof;
import recoder.java.expression.operator.LogicalAnd;
import recoder.java.expression.operator.LogicalNot;
import recoder.java.expression.operator.LogicalOr;
import recoder.java.expression.operator.Minus;
import recoder.java.expression.operator.Modulo;
import recoder.java.expression.operator.Negative;
import recoder.java.expression.operator.New;
import recoder.java.expression.operator.NewArray;
import recoder.java.expression.operator.Plus;
import recoder.java.expression.operator.Positive;
import recoder.java.expression.operator.PostDecrement;
import recoder.java.expression.operator.PostIncrement;
import recoder.java.expression.operator.PreDecrement;
import recoder.java.expression.operator.PreIncrement;
import recoder.java.expression.operator.ShiftLeft;
import recoder.java.expression.operator.ShiftRight;
import recoder.java.expression.operator.Times;
import recoder.java.expression.operator.TypeCast;
import recoder.java.expression.operator.TypeOperator;
import recoder.java.expression.operator.UnsignedShiftRight;
import recoder.java.reference.AnnotationPropertyReference;
import recoder.java.reference.ArrayReference;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.EnumConstructorReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MetaClassReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.ReferenceSuffix;
import recoder.java.reference.SuperConstructorReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisConstructorReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.TypeReferenceInfix;
import recoder.java.reference.UncollatedReferenceQualifier;
import recoder.java.reference.VariableReference;
import recoder.java.statement.Branch;
import recoder.java.statement.Break;
import recoder.java.statement.Case;
import recoder.java.statement.Catch;
import recoder.java.statement.Continue;
import recoder.java.statement.Default;
import recoder.java.statement.ExpressionJumpStatement;
import recoder.java.statement.Finally;
import recoder.java.statement.If;
import recoder.java.statement.LabeledStatement;
import recoder.java.statement.LoopStatement;
import recoder.java.statement.Return;
import recoder.java.statement.Switch;
import recoder.java.statement.SynchronizedBlock;
import recoder.java.statement.Try;
import recoder.kit.MiscKit;
import recoder.kit.StatementKit;
import recoder.kit.UnitKit;
import recoder.list.generic.ASTList;
import recoder.service.DefaultNameInfo.UnknownClassType;
import recoder.util.Debug;
import recoder.util.ProgressListener;
import recoder.util.ProgressListenerManager;

/**
 * Implements queries for program model elements with concrete syntactical
 * representations.
 * 
 * @author RN, AL, DH, VK, TG
 */
public class DefaultSourceInfo extends DefaultProgramModelInfo implements SourceInfo, ChangeHistoryListener, Formats {
	
    private final static boolean DEBUG = false;

    /**
     * Cache mapping (package|type|variable|method|constructor)references to
     * program model elements: <Reference, ProgramModelElement>
     */
    Map<Reference, ProgramModelElement> reference2element = new HashMap<Reference, ProgramModelElement>(1024);

    private final Map<MethodDeclaration, List<Type>> sigCache = new HashMap<MethodDeclaration, List<Type>>();
    
    ProgressListenerManager listeners = new ProgressListenerManager(this);

    /**
     * Creates a new service.
     * 
     * @param config
     *            the configuration this services becomes part of.
     */
    public DefaultSourceInfo(ServiceConfiguration config) {
        super(config);
    }

    /**
     * Performance cache for predefined primitive types.
     */
    private final Map<String, Type> name2primitiveType = new HashMap<String, Type>(16);

    /**
     * Initializes the new service; called by the configuration.
     * 
     * @param cfg
     *            the configuration this services becomes part of.
     */
    public void initialize(ServiceConfiguration cfg) {
        super.initialize(cfg);
        cfg.getChangeHistory().addChangeHistoryListener(this);
        NameInfo ni = getNameInfo();
        name2primitiveType.put("boolean", ni.getBooleanType());
        name2primitiveType.put("char", ni.getCharType());
        name2primitiveType.put("int", ni.getIntType());
        name2primitiveType.put("float", ni.getFloatType());
        name2primitiveType.put("double", ni.getDoubleType());
        name2primitiveType.put("byte", ni.getByteType());
        name2primitiveType.put("short", ni.getShortType());
        name2primitiveType.put("long", ni.getLongType());
    }

    public void addProgressListener(ProgressListener l) {
        listeners.addProgressListener(l);
    }

    public void removeProgressListener(ProgressListener l) {
        listeners.removeProgressListener(l);
    }

    /**
     * Change notification callback method.
     * 
     * @param config
     *            the configuration this services becomes part of.
     */
    public void modelChanged(ChangeHistoryEvent changes) {
    	List<TreeChange> changed = changes.getChanges();
        int s = changed.size();

        listeners.fireProgressEvent(0, s);
        int c = 0;

        // detached first
        for (int i = 0; i < s; i += 1) {
            TreeChange tc = changed.get(i);
            if (!tc.isMinor() && (tc instanceof DetachChange)) {
                processChange(tc);
                listeners.fireProgressEvent(++c);
            }
        }
        for (int i = 0; i < s; i += 1) {
            TreeChange tc = changed.get(i);
            if (!tc.isMinor() && (tc instanceof AttachChange)) {
                processChange(tc);
                listeners.fireProgressEvent(++c);
            }
        }
    }

    /**
     * handles the given change by trying not to invalidate too much pre
     * computed information.
     * 
     * @param attached
     *            true if the program elements was attached, false otherwise
     * @param changed
     *            the program element that was changed
     */
    void processChange(TreeChange change) {
        // the following code implements a very restrictive way to invalidate
        // previously computed information.
        ProgramElement changed = change.getChangeRoot();
        if (isPartOf(changed, PackageSpecification.class) || isPartOf(changed, Import.class)
                || determinesGlobalEntityName(changed) || determinesGlobalEntityType(changed)
                || isPartOf(changed, TypeArgumentDeclaration.class)) {
            // pessimistically clear the caches
            super.reset();
            reference2element.clear();
            /* The class type cache could be reused for most part. However, since
             * the cross referencer resets the caches, extra handling is not really
             * worthwhile here.
             */
        }
        // if package specification, update NameInfo (which can reuse old cache entries)
        if (changed instanceof PackageSpecification) {
            PackageSpecification pkgSpec = (PackageSpecification) changed;
            boolean isAttach = change instanceof AttachChange;
            handleCUPackageChange(pkgSpec.getParent(), Naming.toPathName(pkgSpec.getPackageReference()),isAttach);
        }
        if (changed instanceof PackageReference && isPartOf(changed, PackageSpecification.class)) {
        	// TODO see also DefaultNameInfo.getType(String) - prints warning as well
        	System.err.println("WARNING: name info may now contain invalid mappings name2type... (TO BE FIXED)");
        }
        if (changed instanceof Identifier) {
            NonTerminalProgramElement par = changed.getASTParent();
            if (change instanceof AttachChange) {
                register(par);
            } else {
                String oldname = ((Identifier) changed).getText();
                if (par instanceof VariableSpecification) {
                    unregister((VariableSpecification) par, oldname);
                } else if (par instanceof TypeDeclaration) {
                    unregister((TypeDeclaration) par, oldname);
                }
            }
            // now inform NameInfo
            processIdentifierChanged(change);
        } else {
            if (change instanceof AttachChange) {
                register(changed);
            } else {
                unregister(changed);
            }
        }
    }

	private void handleCUPackageChange(CompilationUnit cu, String originalPkgName, boolean isAttach) {
		DefaultNameInfo dni = (DefaultNameInfo)getNameInfo();
		for (int j = 0, l = cu.getTypeDeclarationCount(); j < l; j++) {
		    ClassType ct = cu.getTypeDeclarationAt(j);
		    String fullPath = originalPkgName + ("".equals(originalPkgName) ? "" : ".") + ct.getName();
		    String defaultPkgPath = ct.getName();
		    if (isAttach) {
		        dni.handleTypeRename(ct, defaultPkgPath, fullPath);
		    } else {
		        dni.handleTypeRename(ct, fullPath, defaultPkgPath);
		    }
		}
	}
    
    private void processIdentifierChanged(TreeChange tc) {
        if (!(getNameInfo() instanceof DefaultNameInfo)) return;
        
        DefaultNameInfo dni = (DefaultNameInfo)getNameInfo();
        Identifier id = (Identifier) tc.getChangeRoot();
        NonTerminalProgramElement npe = id.getParent();
        /*
         * an identifier could be used by: 
         * - LabeledStatement (ignore) 
         * - MethodDeclaration (ignore) 
         * - TypeDeclaration (handle) 
         * - VariableSpecification (ignore) 
         * - FieldSpecification (handle) 
         * - any subtype of NameReference (ignore)
         * - package reference -> parent is package specification (handle)
         */
        if (npe instanceof TypeDeclaration) {
            TypeDeclaration td = (TypeDeclaration)npe;
            // name info will automatically conserve array references (see there)
            if (tc instanceof AttachChange) {
                Object old = dni.getType(td.getFullName());
                if (old == null || old == dni.getUnknownType())
                    dni.register(td); // otherwise, we got a nameclash; preserve first
            } else {
                Object old = dni.getType(td.getFullName());
                if (old == td) // special handling which might occur if a nameclash happened before, which would now be resolved
                    dni.unregisterClassType(td.getFullName());
            }
        } else if (npe instanceof FieldSpecification) {
            FieldSpecification fs = (FieldSpecification)npe;
            if (tc instanceof AttachChange) {
                dni.register(fs);
            } else {
                dni.unregisterField(fs.getFullName());
            }
        } else if (npe instanceof PackageReference &&  isPartOf(npe, PackageSpecification.class)) {
        	throw new IllegalTransformationException(
        			"Changing an Identifier contained in a PackageSpecification is not valid." +
        			" Change either the containing PackageReference or PackageSpecification instead.");
        }
    }

    /**
     * determines whether or not the given element is part of a tree node of the
     * given type. Especially, this is true if the program element is itself an
     * object of the given class.
     * 
     * @param pe
     *            the program element to be checked
     * @param c
     *            the class type of the expected parent
     * @return true iff any tree parent (including pe itself) is an instance of
     *         c
     */
    static boolean isPartOf(ProgramElement pe, Class<? extends ProgramElement> c) {
        while (pe != null && !c.isInstance(pe)) {
            pe = pe.getASTParent();
        }
        return pe != null;
    }
    
    @SuppressWarnings("unchecked")
	private static <E> E getGrandParentOfType(ProgramElement pe, Class<E> c) {
    	while (pe != null && !c.isInstance(pe)) {
    		pe = pe.getASTParent();
    	}
    	return (E)pe;
    }

    /**
     * determines whether or not the given program element is the name of a
     * "globally" visible entity.
     * 
     * @param pe
     *            the program element to be checked
     * @return true iff the given element determines the name or is part of the
     *         name of an entity.
     */
    private boolean determinesGlobalEntityName(ProgramElement pe) {
        if (pe instanceof Identifier) {
            ProgramElement parent = pe.getASTParent();
            return (parent instanceof MemberDeclaration);
        }
        return false;
    }

    /**
     * determines whether or not the given progran element specifies the type of
     * a "globally" visible entity.
     * 
     * @param pe
     *            the program element to be checked
     * @return true iff the given element deteremines the type or is part of the
     *         type specification.
     */
    private boolean determinesGlobalEntityType(ProgramElement pe) {
        if (isPartOf(pe, TypeReference.class)
                && (isPartOf(pe, FieldDeclaration.class) || isPartOf(pe, InheritanceSpecification.class))) {
            return true;
        }
        return false;
    }

    private ProgramElement getDeclaration(ProgramModelElement pme) {
        return (pme instanceof ProgramElement) ? (ProgramElement) pme : null;
    }

    public final TypeDeclaration getTypeDeclaration(ClassType ct) {
        return (ct instanceof TypeDeclaration) ? (TypeDeclaration) ct : null;
    }

    public final MethodDeclaration getMethodDeclaration(Method m) {
        return (m instanceof MethodDeclaration) ? (MethodDeclaration) m : null;
    }

    public final VariableSpecification getVariableSpecification(Variable v) {
        return (v instanceof VariableSpecification) ? (VariableSpecification) v : null;
    }

    public final ConstructorDeclaration getConstructorDeclaration(Constructor c) {
        return (c instanceof ConstructorDeclaration) ? (ConstructorDeclaration) c : null;
    }

    private ClassType getFromUnitPackage(String name, CompilationUnit cu) {
        String xname = Naming.getPackageName(cu);
        if (xname.length() > 0) {
            xname = Naming.dot(xname, name);
        }
        if (DEBUG)
            Debug.log("Checking unit package type " + xname);
        return getNameInfo().getClassType(xname);
    }

    // traverse *all* directly imported types
    private ClassType getFromTypeImports(String name, List<Import> il) {
        if (DEBUG)
            Debug.log("Checking " + name + " in type imports");
        ClassType result = null;
        NameInfo ni = getNameInfo();
        for (int i = il.size() - 1; i >= 0; i -= 1) {
            Import imp = il.get(i);
            if (imp.isMultiImport()) {
                continue;
            }
            TypeReference tr = imp.getTypeReference();
            ClassType newResult = null;
            String trname = imp.isStaticImport() ? imp.getStaticIdentifier().getText() : tr.getName();
            if (DEBUG)
                Debug.log(" Checking against " + trname);
            // trname must end with the start of name
            if (name.startsWith(trname)) {
                int tlen = trname.length();
                int nlen = name.length();
                // the start of name must be a prefix (ending with '.')
                if (tlen == nlen || name.charAt(tlen) == '.') {
                    ReferencePrefix rp = imp.isStaticImport() ? tr : tr.getReferencePrefix(); // if static, tr is referenceprefix of the static identifier
                    if (rp == null) {
                        // direct import of requested type
                        trname = name;
                    } else {
                        // import of a valid prefix of the requested type
                        trname = Naming.toPathName(rp, name);
                    }
                    newResult = ni.getClassType(trname);
                    if (DEBUG) {
                        Debug.log(" Trying " + trname);
                        Debug.log(Format.toString(" Found %N", newResult));
                    }
                }
            } else if (name.endsWith(trname) && name.equals(trname = Naming.toPathName(tr))) {
                newResult = ni.getClassType(trname);
            }
            // newResult may be invalid if type is not static, but this is a static import
            if (newResult != null && (newResult.isStatic() || !imp.isStaticImport())) {
                if (result != null && result != newResult) {
                    getErrorHandler().reportError(
                            new AmbiguousImportException("Ambiguous import " + Format.toString(ELEMENT_LONG, imp)
                                    + " - could be " + Format.toString("%N", result) + " or "
                                    + Format.toString("%N", newResult), imp, result, newResult));
                    // ignore if forced to do so
                }
                result = newResult;
            }
        }
        return result;
    }

    // an evil hack. See below.
    private HashSet<ClassType> hack = new HashSet<ClassType>();
    // traverse *all* imported packages (to check for ambiguities)
    // TODO make private again (?) 
    public ClassType getFromPackageImports(String name, List<Import> il, ClassType searchingFrom) {
        if (DEBUG)
            Debug.log("Checking " + name + " in package imports");
        ClassType result = null;
        NameInfo ni = getNameInfo();
        for (int i = il.size() - 1; i >= 0; i--) {
            Import imp = il.get(i);
            if (imp.isMultiImport()) {
                TypeReferenceInfix ref = imp.getReference();
                String xname = Naming.toPathName(ref, name);
                if (DEBUG)
                    Debug.log("Checking wildcard type " + xname);
                ClassType newResult;
                newResult = ni.getClassType(xname);
                if (newResult == null && (ref instanceof TypeReference || ref instanceof UncollatedReferenceQualifier)) {
                	// maybe an inherited type, so the type may not be registered under "xname" in NameInfo.
                	// Fixes the bug tested in testFindInheritedTypeThrougPackageImport().
                	ClassType t = getNameInfo().getClassType(Naming.toPathName(ref));
                	if (t != null) { // in case we have incomplete code or URQ actually isn't a TypeReference
                		// this may cause a cycle / stack overflow if two CUs import
                		// each other. Therefore, some extra measures are taken to prevent this.
                		// TODO think of something smarter, this may not be 100% accurate!!
                		if (hack.add(t)) {
                			if (imp.isStaticImport()) {
                				// check inherited types as well.
                				newResult = getMemberType(name, t);
                			} else {
                				// check locally defined types only.
                				List<? extends ClassType> innerTypes = t.getTypes();
                				for (ClassType candid : innerTypes) {
                					if (name.equals(candid.getName())) {
                						newResult = candid;
                						break;
                					}
                				}
                			}
                		}
            			hack.remove(t);
                	}
                }
                if (newResult != null && !isVisibleFor(newResult, searchingFrom ))
                {
                    newResult = null;
                }
                // newResult may be invalid if it's non-static but import is static
                if (newResult != null && (newResult.isStatic() || !imp.isStaticImport())) {
                    if (result != null && result != newResult) {
                        getErrorHandler().reportError(
                                new AmbiguousImportException("Ambiguous import of type " + name + ": could be "
                                        + Format.toString("%N", result) + " or " + Format.toString("%N", newResult),
                                        imp, result, newResult));
                        // ignore problem to resume
                    }
                    result = newResult;
                }
            }
        }
        return result;
    }

    /**
     * Searches the given short name as a member type of the given class.
     */
    private ClassType getMemberType(String shortName, ClassType ct) {
        if (DEBUG) {
            Debug.log("Checking for type " + shortName + " within " + ct.getFullName());
        }
        List<? extends ClassType> innerTypes = ct.getTypes();
        for (int i = innerTypes.size() - 1; i >= 0; i -= 1) {
            ClassType candid = innerTypes.get(i);
            if (shortName.equals(candid.getName())) {
                return candid;
            }
        }
        // search supertypes
        List<? extends ClassType> superTypes = ct.getSupertypes();
        for (int i = 0; i < superTypes.size(); i++) {
        	// fixed in recoder 0.80: interfaces may contain member interfaces
            ClassType possibleSuperclass = superTypes.get(i);
            ClassType result = getMemberType(shortName, possibleSuperclass);
            if (result != null)
            	return result;
        }
        return null;
    }

    /**
     * Searches the given type name in the given scope. The name may be a
     * partial name such as <CODE>A.B</CODE> where <CODE>b</CODE> is a
     * member class of <CODE>A</CODE>.
     */
    private ClassType getLocalType(String name, TypeScope scope) {
        ClassType result = null;
        int dotPos = name.indexOf('.');
        String shortName = (dotPos == -1) ? name : name.substring(0, dotPos);
        if (DEBUG) {
            String output = "Looking for type " + shortName + " in scope of " + Format.toString("%c[%p]: ", scope);
            List<? extends ClassType> ctl = scope.getTypesInScope();
            if (ctl != null && ctl.size() > 0) {
                output += " " + Format.toString("%n", ctl);
            }
            Debug.log(output);
        }
        result = scope.getTypeInScope(shortName);
        while (result != null && dotPos != -1) {
            dotPos += 1;
            int nextDotPos = name.indexOf('.', dotPos);
            shortName = (nextDotPos == -1) ? name.substring(dotPos) : name.substring(dotPos, nextDotPos);
            dotPos = nextDotPos;
            result = getMemberType(shortName, result);
        }
        return result;
    }

    /**
     * Searches an inherited member type of the given name. The method does also
     * report locally defined member types of the given type.
     */
    private ClassType getInheritedType(String name, ClassType ct) {
        int dotPos = name.indexOf('.');
        String shortName = (dotPos == -1) ? name : name.substring(0, dotPos);
        // it does not pay to check if ct has any non-trivial supertypes
        List<ClassType> ctl = getAllTypes(ct);
        if (DEBUG)
            Debug.log("Checking type " + shortName + " as inherited member of " + ct.getFullName() + ": "
                    + Format.toString("%N", ctl));
        ClassType result = null;
        int nc = ctl.size();
        // starting at i = ct.getTypes().size() would have little to no
        // influence on performance
        for (int i = 0; i < nc; i++) {
            ClassType c = ctl.get(i);
            if (!(c instanceof TypeParameter) && shortName.equals(c.getName())) {
                result = c;
                break;
            }
        }
        while (result != null && dotPos != -1) {
            dotPos += 1;
            int nextDotPos = name.indexOf('.', dotPos);
            shortName = (nextDotPos == -1) ? name.substring(dotPos) : name.substring(dotPos, nextDotPos);
            dotPos = nextDotPos;
            result = getMemberType(shortName, result);
        }
        return result;
    }
    
    /**
     * Tries to find a type with the given name using the given program element
     * as context. Useful to check for name clashes when introducing a new
     * identifier. Neither name nor context may be <CODE>null</CODE>.
     * 
     * @param name
     *            the name for the type to be looked up; may or may not be
     *            qualified.
     * @param context
     *            a program element defining the lookup context (scope).
     * @return the corresponding type (may be <CODE>null</CODE>).
     */
    public Type getType(String name, ProgramElement context) {
        Debug.assertNonnull(name, context);

        NameInfo ni = getNameInfo();

        // check primitive types and void --- these happen often
        Type t = name2primitiveType.get(name);
        if (t != null) {
            return t;
        }
        if (name.equals("void")) {
            return null;
        }
        // catch array types
        if (name.endsWith("]")) {
            int px = name.indexOf('[');
            // compute base type
            t = getType(name.substring(0, px), context);
            if (t == null) {
                return null;
            }
            for (int dim = (name.length() - px) / 2; dim > 0; dim--) {
            	t = t.createArrayType();
            }
            return t;
        }

        if (DEBUG)
            Debug.log("Looking for type " + name + Format.toString(" @%p in %u", context));

        updateModel();

        // in the very special case that we are asking from the point of
        // view of a supertype reference, we must move to the enclosing unit
        // or parent type
//        if (context.getASTParent() instanceof InheritanceSpecification) {
//            context = context.getASTParent().getASTParent().getASTParent();
//        }
        if (isPartOf(context, InheritanceSpecification.class)) {
        	while (!(context instanceof InheritanceSpecification)) {
        		context = context.getASTParent();
        	}
        	context = context.getASTParent();
        	if (context instanceof TypeDeclaration) {
        		TypeDeclaration td = (TypeDeclaration)context;
        		List<TypeParameterDeclaration> tpds = td.getTypeParameters(); 
        		for (int i = 0, s = tpds == null ? 0 : tpds.size(); i < s; i++) {
        			if (name.equals(tpds.get(i).getName()))
        				return tpds.get(i);
        		}
        	}
        	context = context.getASTParent();
        }
        // the same goes for type parameter bounds:
        if (isPartOf(context, TypeParameterDeclaration.class)) {
        	while (!(context instanceof TypeParameterDeclaration)) {
        		context = context.getASTParent();
        	}
        	context = context.getASTParent();
        }

        ProgramElement pe = context;
        while (pe != null && !(pe instanceof TypeScope)) {
            context = pe;
            pe = pe.getASTParent();
        }
        TypeScope scope = (TypeScope) pe;
        if (scope == null) {
            Debug.log("Null scope during type query " + name + "("+context.getID()+") in context "
                    + Format.toString(Formats.ELEMENT_LONG, context));
            Debug.log(Debug.makeStackTrace());
        }
        ClassType result = null;

        // do the scope walk
        TypeScope s = scope;
        while (s != null) {
            result = getLocalType(name, s);
            if (result != null) {
                // must double check this result - rare cases of confusion
                // involving type references before a local class of the
                // corresponding name has been specified
                if (s instanceof StatementBlock) {
                    StatementContainer cont = (StatementBlock) s;
                    for (int i = 0; true; i += 1) {
                        Statement stmt = cont.getStatementAt(i);
                        if (stmt == result) {
                            // stop if definition comes first
                            break;
                        }
                        if (stmt == context) {
                            // tricky: reference before definition - must
                            // ignore the definition :(
                            result = null;
                            break;
                        }
                    }
                }
                if (result != null) {
                    // leave _now_
                    break;
                }
            }
            if (s instanceof TypeDeclaration) {
                TypeDeclaration td = (TypeDeclaration) s;
            	// rare case: Inherited member type with same name as the actual type itself.
                if (name.equals(td.getName())) {
                	result = td;
                	break;
                }
                result = getInheritedType(name, td);
                if (result != null)
                	break;
            }
            scope = s;
            pe = s.getASTParent();
            while (pe != null && !(pe instanceof TypeScope)) {
                context = pe;
                pe = pe.getASTParent();
            }
            s = (TypeScope) pe;
        }
        if (result != null) {
            if (DEBUG)
                Debug.log(Format.toString("Found %N", result));
            return result;
        }

        // now the outer scope is null, so we have arrived at the top
        CompilationUnit cu = (CompilationUnit) scope;

        List<Import> il = cu.getImports();
        if (il != null) {
            // first check type imports
            result = getFromTypeImports(name, il);
        }
        if (result == null) {
            // then check same package
            result = getFromUnitPackage(name, cu);
            if (result == null && il != null) {
                // then check package imports
                result = getFromPackageImports(name, il, 
                		cu.getTypeDeclarationCount() == 0 ? null 
                				: cu.getTypeDeclarationAt(0 /* doesn't matter which one to check, since this is important for static imports only */));
            }
        }
        if (result == null) {
            // check global types: if unqualified, attempt "java.lang.<name>":
            // any unqualified local type would have been imported already!
        	// TODO should be superfluous? NameInfo does this, now...
            String defaultName = Naming.dot("java.lang", name);
            if (DEBUG)
                Debug.log("Checking type " + defaultName);
            result = ni.getClassType(defaultName);
            if (result == null) {
                if (DEBUG)
                    Debug.log("Checking type " + name);
                result = ni.getClassType(name);
            }
        }
        if (result != null) {
            scope.addTypeToScope(result, name); // add it to the CU scope
        }
        if (DEBUG)
            Debug.log(Format.toString("Found %N", result));
        return result;
    }

    public Type getType(TypeReference tr) {
    	Type res = (Type) reference2element.get(tr);
        if (res != null) {
            return res;
        }
        ReferencePrefix rp = tr.getReferencePrefix();
        
        if (rp instanceof UncollatedReferenceQualifier) {
        	// This happens if we have an URQ in a method's signature.
        	// Usually, type resolution works anyway, but look at
        	// FixedBugs.
        	rp = (ReferencePrefix)resolveURQ((UncollatedReferenceQualifier)rp);
        }
        
        if (rp instanceof PackageReference) {
            String name = Naming.toPathName( rp, tr.getName());
            res = getNameInfo().getClassType(name);
            if (res != null) {
                for (int d = tr.getDimensions(); d > 0; d -= 1) {
                    res = getNameInfo().createArrayType(res);
                }
            }
        } else if (rp == null && tr.getASTParent() instanceof New) {
        	New neww = (New)tr.getASTParent();
        	ReferencePrefix nrp = neww.getReferencePrefix();
        	if (nrp != null) {
        		// inner class instantiation.
        		if (tr.getReferencePrefix() != null) {
        			// report error - this is not allowed!
                    getErrorHandler().reportError(
                            new UnresolvedReferenceException(
                            		Format.toString("Cannot use qualified class names in inner class creation " + ELEMENT_LONG + "(15)", tr),
                                    tr));
                	res = getNameInfo().getUnknownType();
        		} else {
        			ClassType context = (ClassType)getType(nrp);
        			String innerName = tr.getName();
        			for (Type t : context.getAllTypes()) {
        				if (t.getName() == innerName) {
            				// Note: A test could be added here if class is actually inner (not declared static).
        					// But that's not required for building the model.
        					// It's done in Semantics checker instead.
        					res = t;
        					break;
        				}
        			}
        		}
        	} else {
        		// normal handling as below...
        		res = getType(Naming.toPathName(tr), tr);
        	}
        } else {
            res = getType(Naming.toPathName(tr), tr);
            if (res == null && rp instanceof TypeReference) {
            	res = getMemberType(tr.getName(), (ClassType)getType(rp));
            }
        }
        if (res == null && !"void".equals(tr.getName())) {
            getErrorHandler().reportError(
                    new UnresolvedReferenceException(Format.toString("Could not resolve " + ELEMENT_LONG + "(14)", tr),
                            tr));
        	res = getNameInfo().getUnknownType();
        }
        if (res != null) {
        	ClassType rpType = rp instanceof TypeReference ? (ClassType)getType(rp) : null;
            if (rpType instanceof ParameterizedType || (tr.getTypeArguments() != null && tr.getTypeArguments().size() != 0)) {
            	if (res instanceof ArrayType) {
            		// this happens if variable is declared like this:
            		// MyClass<String>[] m;
            		// but not if like this:
            		// MyClass<String> m[];
            		res = makeParameterizedArrayType((ArrayType)res, tr.getTypeArguments());
            	} else {
            		res = getServiceConfiguration().getImplicitElementInfo().getParameterizedType((ClassType)res, tr.getTypeArguments(), rpType instanceof ParameterizedType ? (ParameterizedType)rpType : null);
            	}
            } else {
            	res = checkEraseRequired(tr, res);
            }
            reference2element.put(tr, res);
        }
        return res;
    }

    private Type checkEraseRequired(TypeReference tr, Type t) {
    	if (java5Allowed() && t instanceof ClassType && !(t instanceof ArrayType)
    			&& (tr.getTypeArguments() == null || tr.getTypeArguments().size() == 0)){
        	// Is it a raw type then? 
        	// we test for java5 in case that a pre-java5 program is analyzed against a "modern" library/jdk 
        	ClassType ct = (ClassType)t;
        	if (ct.getTypeParameters() != null && ct.getTypeParameters().size() > 0) {
        		// The type reference has no type arguments, but the type has type parameters.
        		t = ((ClassType)t).getErasedType();
        	} else if (tr.getReferencePrefix() instanceof TypeReference && getType(tr.getReferencePrefix()) instanceof ErasedType) {
        		// the prefix is an erased type -> this is an erased type as well. 
        		// Scenario where this happens: X.Inner, where X is Erased but Inner has no type parameters 
        		t = ((ClassType)t).getErasedType();
        	}
        } else if (t instanceof ArrayType) {
        	int dim = 0;
        	while (t instanceof ArrayType) {
        		t = ((ArrayType)t).getBaseType();
        		dim++;
        	}
        	t = checkEraseRequired(tr, t);
        	for (int i = 0; i < dim; i++) {
        		t = t.createArrayType();
        	}
        }
    	return t;
    }
    
    public final ClassType getType(TypeDeclaration td) {
        return td;
    }
    
    public Type getType(VariableSpecification vs) {
    	if (vs instanceof EnumConstantSpecification)
    		return getType((EnumConstantSpecification)vs);
        updateModel(); // probably not necessary
        
        if (vs.getParent() instanceof UnionTypeParameterDeclaration) {
        	// very special case (Java 7 multi-catch)
        	// The type is the most common class type (must be at least Throwable)
        	UnionTypeParameterDeclaration parent = (UnionTypeParameterDeclaration)vs.getParent();
        	List<ClassType> cts = new ArrayList<ClassType>(parent.getTypeReferenceCount());
        	for (TypeReference tr: parent.getTypeReferences()) {
        		ClassType ct = (ClassType)getType(tr);
        		if (ct != null && ct != getNameInfo().getUnknownClassType()) {
        			cts.add(ct); 
        		}
        	}
    		if (cts.isEmpty()) {
    			cts.add(getNameInfo().getClassType("java.lang.Throwable"));
    		}
    		// a bit circumstantial (could write this probably better), but oh well, let's use the methods that are already there
    		ClassType res = super.getCommonSupertype(cts.toArray(new ClassType[0]));
    		if (res instanceof IntersectionType) {
    			IntersectionType it = (IntersectionType)res;
    			for (ClassType ct2: it.getSupertypes()) {
    				if (!ct2.isInterface()) {
    					res = ct2;
    					break;
    				}
    			}
    		}
    		return res;
        }
        
        TypeReference tr = (vs.getParent()).getTypeReference();
        Type result = getType(tr);
        if (result != null) {
        	int d = vs.getDimensions();
        	if (vs.getASTParent() instanceof ParameterDeclaration) {
        		ParameterDeclaration pd = (ParameterDeclaration)vs.getASTParent();
        		if (pd.isVarArg())
        			d++;
        	}
            for (; d > 0; d -= 1) {
                result = getNameInfo().createArrayType(result);
            }
        }
        return result;
    }
    
    private Type getType(EnumConstantSpecification ecs) {
    	Type cd = ecs.getConstructorReference().getClassDeclaration();
    	if (cd != null) return cd; // anonymous type
    	return ecs.getParent().getParent(); // enum type itself
    }

    /**
     * Returns the type of the given program element. For declarations, this is
     * the type declared by the given declaration, for references, this means
     * the referenced type, and for expressions this is the result type.
     * 
     * @param pe
     *            the program element to analyze.
     * @return the type of the program element or <tt>null</tt> if the type is
     *         void; <tt>DefaultNameInfo.unknownClassType</tt> if the type is unknown or unavailable.
     */
    public Type getType(ProgramElement pe) {
        updateModel();
        Type result = null;
        if (pe instanceof Expression) {
            result = getType((Expression) pe);
        } else if (pe instanceof UncollatedReferenceQualifier) {
            result = getType((UncollatedReferenceQualifier) pe);
        } else if (pe instanceof TypeReference) {
            result = getType((TypeReference) pe);
        } else if (pe instanceof VariableSpecification) {
            result = getType((VariableSpecification) pe);
        } else if (pe instanceof EnumConstantSpecification) {
        	result = getType((EnumConstantSpecification)pe);
        } else if (pe instanceof MethodDeclaration) {
            if (!(pe instanceof ConstructorDeclaration)) {
                result = getReturnType((MethodDeclaration) pe);
            }
        } else if (pe instanceof TypeDeclaration) {
            result = getType((TypeDeclaration) pe);
        }
        return result;
    }

    private Type getType(UncollatedReferenceQualifier urq) {
        Reference r = resolveURQ(urq);
        if (r instanceof UncollatedReferenceQualifier) {
            // resolution failed, continue anyway
            return getNameInfo().getUnknownClassType();
        }
        return getType(r);
    }

    public Type getType(Expression expr) {
        Type result = null;
        if (expr instanceof Operator) { ///////////////// Operators
            Operator op = (Operator) expr;
            ASTList<Expression> args = op.getArguments();
            if (op instanceof Assignment) {
                result = getType(args.get(0));
            } else if (op instanceof TypeCast) {
                result = getType(((TypeOperator) op).getTypeReference());
            } else if (op instanceof New) {
            	result = ((New)op).getClassDeclaration();
            	if (result == null)
            		result = getType(((New)op).getTypeReference());
            	if (((New)op).withDiamondOperator()) {
            		result = ((ErasedType)result).getGenericMember();
            		List<TypeArgument> inferredTypes = new ArrayList<TypeArgument>(((ClassType)result).getTypeParameters().size());
            		List<ClassType> inferredClassTypes = new ArrayList<ClassType>(((ClassType)result).getTypeParameters().size());
            		for (int i = ((ClassType)result).getTypeParameters().size() - 1; i >= 0; i--) {
            			TypeParameter tp = ((ClassType)result).getTypeParameters().get(i);
            			ClassType inferred = inferType((Constructor)getConstructor((New)op).getGenericMember(), ((New)expr).getArguments(), expr.getASTParent(), tp, ((ClassType)result).getTypeParameters(), inferredClassTypes);
            			if (inferred != null) {
            				inferredClassTypes.add(inferred);
            				if (inferred instanceof ParameterizedType) {
            					inferredTypes.add(new ResolvedTypeArgument(WildcardMode.None, 
            							((ParameterizedType)inferred).getGenericType(), 
            							((ParameterizedType)inferred).getTypeArgs(), null)); // TODO check last argument
            				} else {
            					inferredTypes.add(new ResolvedTypeArgument(WildcardMode.None, inferred, null, null));  // TODO check last argument
            				}
            			} else {
            				// the context to infer from is raw. This doesn't make much sense, but anyway we need to support it.
            				// Example: Map m = new HashMap<>(); -- should infer <Object,Object>
            				inferredTypes.add(new ResolvedTypeArgument(WildcardMode.None, getClassTypeFromTypeParameter(tp, 0), null, null));
            			}
            		}
        			Collections.reverse(inferredTypes);
            		result = serviceConfiguration.getImplicitElementInfo().getParameterizedType((ClassType)result, inferredTypes);	
            	}
            } else if (op instanceof NewArray) {
                NewArray n = (NewArray) op;
                TypeReference tr = n.getTypeReference();
                result = getType(tr);
                for (int d = n.getDimensions(); d > 0; d -= 1) {
                    Type oldResult = result;
                    result = getNameInfo().getArrayType(result);
                    if (result == null) {
                        result = getNameInfo().createArrayType(oldResult);
                    }
                }
            } else if ((op instanceof PreIncrement) || (op instanceof PostIncrement) 
            		|| (op instanceof PreDecrement) || (op instanceof PostDecrement)
                    || (op instanceof ParenthesizedExpression) || (op instanceof BinaryNot)) {
            	result = getType(args.get(0));
            } else if ((op instanceof Positive) || (op instanceof Negative) ) {
            	result = getType(args.get(0));
            	if (java5Allowed() && result instanceof ClassType) {
            		result = getUnboxedType((ClassType)result);
            	}
            } else if ((op instanceof Plus) || (op instanceof Minus) || (op instanceof Times) || (op instanceof Divide)
            		|| (op instanceof Modulo)) {
            	Type t2 = getType(args.get(1));
            	if (op instanceof Plus && 
            			(t2 == getNameInfo().getJavaLangString() || t2 == null)) {
            		// this is a quick-path to resolve long recursion as it occurs when
            		// having "" + "" + "" + ... + "" - yes, it happens in some cases
            		// in practice!
            		result = getNameInfo().getJavaLangString();
            	} else {
            		Type t1 = getType(args.get(0));
            		if ((op instanceof Plus)
            				&& ((t1 == getNameInfo().getJavaLangString()) || (t2 == getNameInfo().getJavaLangString())
            						|| (t1 == null) || (t2 == null))) {
            			// all primitive types are known -
            			// one of these must be a class type
            			result = getNameInfo().getJavaLangString();
            			// any object-plus-operation must result in a string type
            		} else { 
            			if (java5Allowed()) {
            				if (t1 instanceof ClassType)
            					t1 = getUnboxedType((ClassType)t1);
            				if (t2 instanceof ClassType)
            					t2 = getUnboxedType((ClassType)t2);
            			}
            			if ((t1 instanceof PrimitiveType) && (t2 instanceof PrimitiveType)) {
            				result = getPromotedType((PrimitiveType) t1, (PrimitiveType) t2);
            				if (result == null) {
            					getErrorHandler().reportError(
            							new TypingException("Boolean types cannot be promoted in " + op, op));
            					result = getNameInfo().getUnknownType();
            				}
            			} else {
            				if ((t1 != null) && (t2 != null)) {
            					getErrorHandler().reportError(
            							new TypingException("Illegal operand types for plus " + t1 + " + " + t2
            									+ " in expression " + op, op));
            					result = getNameInfo().getUnknownType();
            				}
            			}
            		}
            	}
            } else if ((op instanceof ShiftRight) || (op instanceof UnsignedShiftRight) || (op instanceof ShiftLeft)) {
        		Type t1 = getType(args.get(0));
            	if (java5Allowed()) {
            		Type t2 = getType(args.get(1));
            		if (t1 instanceof ClassType && t2 instanceof PrimitiveType)
            			t1 = getUnboxedType((ClassType)t1);
            	}
            	result = t1;
            } else if ((op instanceof BinaryAnd) || (op instanceof BinaryOr) || (op instanceof BinaryXOr)) {
            	// fixed since 0.96. Prior to 0.96, this was handled like ShiftRight etc. above.
            	// however, JLS 15.22 (3rd edition) resp. 15.21 (1st edition) clearly state that binary numeric promotion is performed.
            	Type t1 = getType(args.get(0));
            	Type t2 = getType(args.get(1));
            	if (java5Allowed()) {
            		if (t1 instanceof ClassType)
            			t1 = getUnboxedType((ClassType)t1);
            		if (t2 instanceof ClassType)
            			t2 = getUnboxedType((ClassType)t2);
            	}
            	PrimitiveType booleanType = getNameInfo().getBooleanType();
            	if (t1 == booleanType && t2 == booleanType)
            		result = booleanType;
            	else if (t1 instanceof PrimitiveType && t2 instanceof PrimitiveType)
            		result = getPromotedType((PrimitiveType)t1, (PrimitiveType)t2);
            	else {
            		getErrorHandler().reportError(new TypingException("Non-promotable type(s) in bitwise operator", op));
                    result = getNameInfo().getUnknownType();
            	}
            } else if ((op instanceof ComparativeOperator) || (op instanceof LogicalAnd) || (op instanceof LogicalOr)
                    || (op instanceof LogicalNot) || (op instanceof Instanceof)) {
                result = getNameInfo().getBooleanType();
            } else if (op instanceof Conditional) {
                Expression e1 = args.get(1);
                Expression e2 = args.get(2);
                Type t1 = getType(e1);
                Type t2 = getType(e2);
                if (t1 == t2) {
                	// often the case, check before (un)boxing.
                    return t1;
                }
                if (java5Allowed()) {
                    // (un-)boxing support (see JLS 3rd edition pg. 511)
                	if (t1 instanceof PrimitiveType && t2 instanceof ClassType) {
                		Type tmp = getUnboxedType((ClassType)t2);
                		if (tmp != null) 
                			t2 = tmp;
                		else 
                			t1 = getBoxedType((PrimitiveType)t1);
                	}
                	else if (t1 instanceof ClassType && t2 instanceof PrimitiveType) {
                		Type tmp = getUnboxedType((ClassType)t1);
                		if (tmp != null) 
                			t1 = tmp;
                		else 
                			t2 = getBoxedType((PrimitiveType)t2);
                	} else if (t1 instanceof ClassType && t2 instanceof ClassType) {
                		 Type unb1 = getUnboxedType((ClassType)t1);
                		 Type unb2 = getUnboxedType((ClassType)t2);
                         NameInfo ni = getNameInfo();
                		 PrimitiveType _boolean = ni.getBooleanType();
                		 if (unb1 != null && unb2 != null && unb1 != _boolean && unb2 != _boolean) {
                			 t1 = unb1;
                			 t2 = unb2;
                		 }
                	} else if (t1 instanceof PrimitiveType && t2 instanceof PrimitiveType) {
                		if (t1 == getNameInfo().getBooleanType() ^ t2 == getNameInfo().getBooleanType()) {
                			// box BOTH
                			t1 = getBoxedType((PrimitiveType)t1);
                			t2 = getBoxedType((PrimitiveType)t2);
                		}
                	}
                	if (t1 == t2) {
                		// check again.
                		return t1;
                	}
                }
                if (t1 instanceof PrimitiveType && t2 instanceof PrimitiveType) {
                    NameInfo ni = getNameInfo();
                    if ((t1 == ni.getShortType() && t2 == ni.getByteType())
                            || (t2 == ni.getShortType() && t1 == ni.getByteType())) {
                        result = ni.getShortType();
                    } else {
                        result = serviceConfiguration.getConstantEvaluator().getCompileTimeConstantType(op);
                        if (result == null) {
                            if (isNarrowingTo(e1, (PrimitiveType) t2)) {
                                return t2;
                            }
                            if (isNarrowingTo(e2, (PrimitiveType) t1)) {
                                return t1;
                            }
                            result = getPromotedType((PrimitiveType) t1, (PrimitiveType) t2);
                        }
                    }
                } else if (t1 instanceof PrimitiveType || t2 instanceof PrimitiveType) {
                    getErrorHandler().reportError(new TypingException("Incompatible types in conditional", op));
                    result = getNameInfo().getUnknownType();
                } else { // two reference types
                    if (t1 == getNameInfo().getNullType()) {
                        result = t2; // null x T --> T
                    } else if (t2 == getNameInfo().getNullType()) {
                        result = t1; // T x null --> T
                    } else {
                    	// TODO the following should probably be moved somewhere else, but where?
                    	if (t1 instanceof CapturedTypeArgument) { t1 = getBaseType(((CapturedTypeArgument) t1).getTypeArgument()); }
                    	if (t2 instanceof CapturedTypeArgument) { t2 = getBaseType(((CapturedTypeArgument) t2).getTypeArgument()); }
                    	// rare case: same type, but one is raw, the other isn't. isWidening() will yield "true" for both.
                    	// Workaround: Make both raw first.
                    	if (t1 instanceof ErasedType || t2 instanceof ErasedType) {
                    		t1 = ((ClassType)t1).getErasedType();
                    		t2 = ((ClassType)t2).getErasedType();
                    	}
                        if (isWidening(t1, t2)) {
                            result = t2;
                        } else if (isWidening(t2, t1)) {
                            result = t1;
                        } else {
                        	if (java5Allowed() && t1 instanceof ClassType && t2 instanceof ClassType) {
                            	// intersection type allowed since java 5.
                        		result = getCommonSupertype((ClassType)t1, (ClassType)t2);
                        		if (result instanceof IntersectionType) {
                        			((IntersectionType)result).setAccesibility(MiscKit.getParentTypeDeclaration(expr));
                        		}
                        	} else {
                        		getErrorHandler().reportError(new TypingException("Incompatible types in conditional", op));
                        		result = getNameInfo().getUnknownType();
                        	}
                        }
                    }
                }
            } else {
                Debug.error("Type resolution not implemented for operation " + op.getClass().getName());
            }
        } else if (expr instanceof Literal) { ///////////////// Literals
            if (expr instanceof NullLiteral) {
                result = getNameInfo().getNullType();
            } else if (expr instanceof BooleanLiteral) {
                result = getNameInfo().getBooleanType();
            } else if (expr instanceof LongLiteral) {
                result = getNameInfo().getLongType();
            } else if (expr instanceof IntLiteral) {
                result = getNameInfo().getIntType();
            } else if (expr instanceof FloatLiteral) {
                result = getNameInfo().getFloatType();
            } else if (expr instanceof DoubleLiteral) {
                result = getNameInfo().getDoubleType();
            } else if (expr instanceof CharLiteral) {
                result = getNameInfo().getCharType();
            } else if (expr instanceof StringLiteral) {
                result = getNameInfo().getJavaLangString();
            }
        } else if (expr instanceof Reference) { ///////////////// References
            if (expr instanceof UncollatedReferenceQualifier) {
                result = getType((UncollatedReferenceQualifier) expr);
            } else if (expr instanceof MetaClassReference) {
            	if (java5Allowed()) {
            		// following JLS, 3rd edition, § 15.8.2
            		Type pref_type = getType(((MetaClassReference)expr).getReferencePrefix());
            		if (pref_type instanceof ClassType) 
            			pref_type = ((ClassType)pref_type).getBaseClassType(); // Don't use raw/generic type, Java doesn't allow that.
        			if (pref_type instanceof PrimitiveType)
        				pref_type = getBoxedType((PrimitiveType)pref_type);
        			if (pref_type == null)
        				pref_type = getNameInfo().getType("java.lang.Void");
        			TypeArgument ta = new DefaultProgramModelInfo.ResolvedTypeArgument(
        					WildcardMode.None, (ClassType)pref_type, null, null);  // TODO check last argument
        			ArrayList<TypeArgument> tas = new ArrayList<TypeArgument>(1);
        			tas.add(ta);
        			result = getServiceConfiguration().getImplicitElementInfo().getParameterizedType(
        					getNameInfo().getJavaLangClass(), 
        					tas);

            	} else {
            		result = getNameInfo().getJavaLangClass();
            	}
            } else if (expr instanceof VariableReference) {
                // look for the variable declaration
                Variable v = getVariable((VariableReference) expr);
                if (v != null) {
                	result = v.getType();
                } else {
                    getErrorHandler().reportError(
                            new UnresolvedReferenceException(Format.toString("Could not resolve " + ELEMENT_LONG, expr)
                                    + " (01)", expr));
                    v = getNameInfo().getUnknownField();
                }
            } else if (expr instanceof MethodReference) {
                // look for the return type of the method
            	MethodReference mr = (MethodReference)expr;
                Method m = getMethod(mr);
                if (m != null) {
                    result = m.getReturnType();
                    if (result instanceof ClassType && ((ClassType)result).isInner()) {
                    	// TODO !!!
                    	if (mr.getReferencePrefix() != null) {
                    		ClassType pref = (ClassType)getType(mr.getReferencePrefix());
                    		// TODO the same with field references???
                    		if (pref instanceof ParameterizedType) {
                    			result = getServiceConfiguration().getImplicitElementInfo().getParameterizedType((ClassType)result, null, (ParameterizedType)pref);
                    		}
                    	}
                    }
                }
            } else if (expr instanceof AnnotationPropertyReference) {
            	AnnotationProperty ap = getAnnotationProperty((AnnotationPropertyReference)expr);
            	if (ap != null) {
            		result = ap.getReturnType(); 
            	}
            } else if (expr instanceof ArrayReference) {
            	ArrayReference aref = (ArrayReference) expr;
                Type ht = getType(aref.getReferencePrefix());
                if (ht != null && !(ht instanceof UnknownClassType)) {
                	List<Expression> dimExprs = aref.getDimensionExpressions();
                    int dims = dimExprs.size();
                    for (int i = 0; i < dims; i++) {
                        ht = ((ArrayType) ht).getBaseType();
                    }
                    if (ht != null) {
                        result = ht;
                    } else {
                        getErrorHandler().reportError(
                                new TypingException("Not an array type: " + ht + " in expression " + expr, expr));
                        result = getNameInfo().getUnknownType();
                    }
                }
            } else if (expr instanceof ThisReference) {
                ReferencePrefix rp = ((ThisReference) expr).getReferencePrefix();
                if (rp == null) {
                    result = getContainingClassType(expr);
                } else {
                    // Find proper enclosing type, which may have type
                	// parameters (in contrast to the rp, which must
                	// not have type args).
                    String prefix = Naming.toPathName(rp);
                    ProgramElement pe = expr;
                	do {
                		pe = (ProgramElement)getContainingClassType(pe);
                		if (pe == null) { 
                			break; // incorrect code.
                		}
                	} while (((TypeDeclaration)pe).getName() == null || // pe.getName() == null in case of anonymous classes.
                			(!((TypeDeclaration)pe).getName().equals(prefix) && !((TypeDeclaration)pe).getFullName().equals(prefix)));
                    result = (ClassType)pe;
                }
            } else if (expr instanceof SuperReference) {
                ReferencePrefix rp = ((SuperReference) expr).getReferencePrefix();
                ClassType thisType;
                if (rp == null) {
                    thisType = getContainingClassType(expr);
                } else {
                    thisType = (ClassType) getType(rp);
                }
                List<ClassType> supers = thisType.getSupertypes();
                if ((supers != null) && (!supers.isEmpty())) {
                    result = supers.get(0);
                }
            }
        } else if (expr instanceof ArrayInitializer) { //// ArrayInitializer
            ProgramElement pe = expr;
            while ((pe != null) && !(pe instanceof VariableSpecification) && !(pe instanceof NewArray)) {
                pe = pe.getASTParent();
            }
            result = getType(pe);
        } else if (expr instanceof ElementValueArrayInitializer) {
        	ProgramElement pe = expr;
        	while ((pe != null) && !(pe instanceof VariableSpecification)) {
        		pe = pe.getASTParent();
        	}
        	result = getType(pe);
        } else if (expr instanceof AnnotationUseSpecification) {
        	result = getType(((AnnotationUseSpecification)expr).getTypeReference());
        } else {
            Debug.error("Type analysis for general expressions is currently not implemented: " + expr + " <"
                    + expr.getClass().getName() + ">");
        }

        return result;
    }
    
    private boolean containsTypeParameterInSig(Method m, TypeParameter typeParam) {
    	List<Type> sig = m.getSignature();
    	for (int j = 0; j < sig.size(); j++) {
    		if (containsTypeParameter(sig.get(j), typeParam))
    			return true;
    	}
    	return false;
    }
    
    private ClassType inferType(Method m, List<Expression> mrArgs, ProgramElement parent, TypeParameter typeParam, List<? extends TypeParameter> allTypeParams, List<ClassType> previouslyinferredTypes) {
		String typeParamName = typeParam.getName();
    	List<ClassType> result = new ArrayList<ClassType>();
    	List<Type> sig = m.getSignature();
    	for (int j = 0; j < sig.size(); j++) {
    		// look at all parameters
    		Type t = sig.get(j);
    		// fix a bug: handling of var arg parameters!
    		if (m.isVarArgMethod() && j == sig.size()-1) {
    			int mrArgsSz = mrArgs == null ? 0 : mrArgs.size();
    			if (mrArgsSz == sig.size()) {
    				// in this case, do NOT create an array type if it already is an array type...
    			 	Expression e = mrArgs.get(sig.size()-1);
    			 	Type actualTypeArg = getType(e);
    			 	if (actualTypeArg == getNameInfo().getNullType())
    			 		continue; // Don't infer from null type.
    			 	if (actualTypeArg instanceof ArrayType)
    			 		inferType1(typeParamName, result, t, actualTypeArg);
    			 	else
    			 		inferType1(typeParamName, result, t, actualTypeArg.createArrayType());
    			} else {
    				for (int k = sig.size()-1; k < mrArgsSz; k++) {
    					Expression e = mrArgs.get(k);
    					Type actualArgType = getType(e);
    					if (actualArgType == getNameInfo().getNullType())
    						continue; // Don't infer from null type.
    					// we cheat here: instead of combining all vararg-arguments to one array,
    					// we pretend that each single one is an array.
    					inferType1(typeParamName, result, t, actualArgType.createArrayType());
    				}
    			}
    		} else {
    			Expression e = mrArgs.get(j);
    			Type actualArgType = getType(e);
				if (actualArgType == getNameInfo().getNullType())
					continue; // Don't infer from null type.
    			inferType1(typeParamName, result, t, actualArgType);
    		}
    	}
    	if (result.size() == 0) {
    		// Fix for 0.95: Need to follow JLS, §15.12.2.8 - take possible assignment into consideration
    		while (parent instanceof ParenthesizedExpression) {
    			parent = parent.getASTParent();
    		}
    		if (parent instanceof CopyAssignment) {
    			CopyAssignment ca = (CopyAssignment)parent;
    			Type exp = getType(ca.getArguments().get(0));
    			if (!inferFromContext(typeParamName, result, m, exp))
    				return null;
    		} else if (parent instanceof VariableSpecification) {
    			VariableSpecification vs = (VariableSpecification)parent;
    			Type exp = getType(vs.getType());
    			if (!inferFromContext(typeParamName, result, m, exp))
    				return null;
    		} else if (parent instanceof Return) {
    			Return r = (Return)parent;
    			Type exp = getType(MiscKit.getParentMemberDeclaration(r));
    			if (!inferFromContext(typeParamName, result, m, exp))
    				return null;
    		} else if (containsTypeParameterInSig(m, typeParam)) {
    			// if raw types were used, nothing may have been inferred
    			// take null instead, this will indicate later on that types
    			// need to be erased.
    			return null;
    		} else {
    			// rare case: <T, U extends T> and T is not inferred otherwise: assume "U".
    			// TODO no clue why, but both eclipse and javac seem to do that. Check out the theory behind it.
    			int idx = allTypeParams.indexOf(typeParam);
    			List<ClassType> meAsBound = new ArrayList<ClassType>();
    			for (int i = idx+1; i < allTypeParams.size(); i++) {
    				if (allTypeParams.get(i).getBoundName(0).equals(typeParam.getName())) {
    					meAsBound.add(previouslyinferredTypes.get(allTypeParams.size()-1-i));
    				}
    			}
    			if (meAsBound.size() > 0) {
    				result = Collections.singletonList(getCommonSupertype(meAsBound.toArray(new ClassType[meAsBound.size()])));
    			} else {
    				// return the bounds of the type parameter, e.g., if a method has no formal parameters (rare, but valid).
    				if (result.size() == 0)
    					result.addAll(typeParam.getSupertypes());
    			}
    		}
    	}
    	if (result.size() == 0 && m instanceof ParameterizedMethod && typeParam.getBoundCount() == 1 && typeParam.getBoundName(0).indexOf('.') == -1) {
    		// hopefully a rare case: class A<T> { <X extends T> Iterator<T> getI(T t) { ... } } --- makes no real sense to me but is found a couple of time.s
    		ParameterizedMethod pm = (ParameterizedMethod)m;
    		int idx = 0;
    		for (TypeParameter tp: pm.getContainingClassType().getGenericType().getTypeParameters()) {
    			if (tp.getName().equals(typeParam.getBoundName(0))) {
    				result.add(new TypeArgument.CapturedTypeArgument(pm.getContainingClassType().getTypeArgs().get(idx), getServiceConfiguration().getImplicitElementInfo()));
    				break;
    			}
    			idx++;
    		}
    	}
    	removeSupertypesFromList(result);
    	if (result.size() == 0) {
    		// ok let's assume that the type is not used *anywhere* in the return type or signature.
    		// we just make it an intersection type...
    		result.addAll(typeParam.getAllSupertypes());
    	}
    	if (result.size() == 1) {
    		return result.get(0);
    	} else {
    		IntersectionType res = new IntersectionType(result, getServiceConfiguration().getImplicitElementInfo());
    		res.setAccesibility(MiscKit.getParentTypeDeclaration(parent));
    		return res;
    	}
    }
    
    private boolean inferFromContext(String typeParamName, List<ClassType> result, Method m, Type expectedFromContext) {
    	if (expectedFromContext instanceof ErasedType) {
			return false;
		}
		ClassType ret;
		if (m instanceof Constructor) {
			ret = m.getContainingClassType();
			// we'll get a plain ClassType here, might have to parameterize it ourselves.
			List<? extends TypeParameter> tps = ret.getTypeParameters();
			if (tps != null && tps.size() > 0) {
				// ugly. Must "convert" each type param to a type arg.
				List<ResolvedTypeArgument> tas = new ArrayList<ResolvedTypeArgument>(tps.size());
				for (TypeParameter tp: tps) {
					tas.add(new ResolvedTypeArgument(WildcardMode.None, tp, null, tp));
				}
				ret = getServiceConfiguration().getImplicitElementInfo().getParameterizedType(ret, tas, null);
			}
		} else {
			ret = (ClassType)m.getReturnType();
		}
		// find proper matching supertype of "ret" that has the same generic base type as exp.
		for (ClassType t: ret.getAllSupertypes()) {
			if (((ClassType)expectedFromContext).getBaseClassType() == t.getBaseClassType()) {
				ret = t;
				break;
			}
		}
		if (ret instanceof TypeParameter) {
			result.add((ClassType)expectedFromContext);
			return true;
		}
		if (!(ret instanceof ParameterizedType)) {
			// rare case: Object o = getSomething() with declaration like <T> Comparable<T> getSomething();
			return false;
		}
		inferType1(typeParamName, result, ret, expectedFromContext);
		return true;
    }
    
	private void inferType1(String typeParamName, List<ClassType> result, Type t, Type actualArgType) {
		inferType2(typeParamName, result, t, actualArgType);
		if (t instanceof ParameterizedType && actualArgType instanceof ClassType) {
			// consider type arguments now...
			ParameterizedType tp = (ParameterizedType)t;
			// we need to test against the exact matching supertype of actualTypeArg.
			// see bug 2045354 for an example.
			List<ClassType> supers = removeErasedTypesFromList(((ClassType)actualArgType).getAllSupertypes());
			ParameterizedType inferFrom = null;
			for (ClassType temp : supers) {
				if (temp instanceof ParameterizedType
					&& ((ParameterizedType)temp).getGenericType() == tp.getGenericType()) {
					inferFrom = (ParameterizedType)temp;
					break;
				}
			}
			if (inferFrom == null) 
				return; // skip
			for (int i = 0; i < tp.getAllTypeArgs().size(); i++) {
				inferType1(typeParamName, result, getBaseType(tp.getAllTypeArgs().get(i), false), getBaseType(inferFrom.getAllTypeArgs().get(i), false));
			}
		}
	}

	private void inferType2(String typeParamName, List<ClassType> result, Type t, Type actualArgType) {
		Type toAdd = actualArgType;
		int reduceDim = 0;
		while (t instanceof ArrayType) {
			t = ((ArrayType)t).getBaseType();
			reduceDim++;
		}
		if (t instanceof TypeParameter) {
			if (t.getName().equals(typeParamName)) {
				while (reduceDim > 0) {
					try {
						toAdd = ((ArrayType)toAdd).getBaseType();
					} catch (RuntimeException e) {
						throw e;
					}
					reduceDim--;
				}
				List<ClassType> ctl = new ArrayList<ClassType>();
				if (toAdd instanceof PrimitiveType) {
					toAdd = getBoxedType((PrimitiveType)toAdd);
				}
				ctl = removeErasedTypesFromList(getAllSupertypes((ClassType)toAdd));
				if (result.isEmpty()) {
					// first match: add all; at least java.lang.Object will retain in list afterwards  
					result.addAll(ctl); 
				} else {
					// intersect the two lists ("retainAll")
					for (int i = result.size()-1; i >= 0; i--) {
						if (ctl.indexOf(result.get(i)) == -1) {
							result.remove(i);
						}
					}
					// rare case: It can happen that *un*erased Class is intersected with Class<?>, namely in the case of MetaClassReferences.
					// In that case, add Erased Class to the list. 
					if (actualArgType == getNameInfo().getJavaLangClass()) {
						result.add(((ClassType)actualArgType).getErasedType());
					}
				}
			}
		}
	}
    


    public boolean isNarrowingTo(Expression expr, PrimitiveType to) {
        NameInfo ni = getNameInfo();
        int minValue, maxValue;
        if (to == ni.getByteType()) {
            minValue = Byte.MIN_VALUE;
            maxValue = Byte.MAX_VALUE;
        } else if (to == ni.getCharType()) {
            minValue = Character.MIN_VALUE;
            maxValue = Character.MAX_VALUE;
        } else if (to == ni.getShortType()) {
            minValue = Short.MIN_VALUE;
            maxValue = Short.MAX_VALUE;
        } else {
            return false;
        }
        ConstantEvaluator ce = serviceConfiguration.getConstantEvaluator();
        ConstantEvaluator.EvaluationResult res = new ConstantEvaluator.EvaluationResult();
        if (!ce.isCompileTimeConstant(expr, res) || res.getTypeCode() != ConstantEvaluator.INT_TYPE) {
            return false;
        }
        int value = res.getInt();
        return (minValue <= value) && (value <= maxValue);
    }

    public Type getType(ProgramModelElement pme) {
        Debug.assertNonnull(pme);
        // updateModel(); not necessary
        Type result = null;
        if (pme instanceof Type) {
            result = (Type) pme;
        } else {
            if (pme instanceof ProgramElement) {
                result = getType((ProgramElement) pme);
                if ((result == null) && (pme instanceof VariableSpecification)) {
                	if (pme instanceof EnumConstantSpecification) {
                		// this would mean an enum constant specification outside an enum, which
                		// shouldn't be possible to construct (syntax error)
                		throw new IllegalStateException("Enum constant outside an enum, this shouldn't even be possible");
                	}
                    // void is acceptable for method decls
                    getErrorHandler().reportError(
                            new UnresolvedReferenceException(Format.toString("Unknown type of " + ELEMENT_LONG, pme),
                                    (((VariableSpecification) pme).getParent()).getTypeReference()));
                    result = getNameInfo().getUnknownType();
                }
                if (result == null && pme instanceof EnumConstantDeclaration) {
                	// this can't happen!
                	throw new Error("fatal error: EnumConstantDeclaration occured outside enum declaration");
                }
            } else {
                result = pme.getProgramModelInfo().getType(pme);
            }
        }
        return result;
    }

    public ClassType getContainingClassType(ProgramElement context) {
        Debug.assertNonnull(context);
        //updateModel(); not necessary
        if (context instanceof TypeDeclaration) {
            context = context.getASTParent();
        }
        do {
            if (context instanceof ClassType) {
                return (ClassType) context;
            }
            context = context.getASTParent();
        } while (context != null);
        return null;
    }

    public ClassType getContainingClassType(Member m) {
        Debug.assertNonnull(m);
        // updateModel(); not necessary
        ClassType result = null;
        ProgramElement pe = getDeclaration(m);
        if (pe == null) {
            result = m.getProgramModelInfo().getContainingClassType(m);
        } else {
            result = getContainingClassType(pe);
        }
        return result;
    }

    /*
     * Returns a field with the given name from the given class type or from the
     * bottommost supertype that defines it.
     */
    private Field getInheritedField(String name, ClassType ct) {
        // for private use only - no model update required
        List<? extends Field> fl = ct.getAllFields();
        int nf = fl.size();
        for (int i = 0; i < nf; i++) {
            Field f = fl.get(i);
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    /* context can make a difference under rare circumstances
     * a context before a local declaration will not locate the declaration
     * and will look for a variable in an outer scope
     */
    public Variable getVariable(String name, ProgramElement context) {
        ProgramElement originalContext = context;
        Debug.assertNonnull(name, context);
        updateModel();
        if (DEBUG)
            Debug.log("Looking for variable " + name);
        // special case handling for java 5 enums first:
        if (java5Allowed() && 
        		(context instanceof VariableReference || context instanceof UncollatedReferenceQualifier)
        		&& context.getASTParent() instanceof Case 
        		&& getType(((Case)context.getASTParent()).getParent().getExpression()) instanceof ClassType 
        		&& ((ClassType)getType(((Case)context.getASTParent()).getParent().getExpression())).isEnumType()) {
            /* is it an enum constant? Possible iff:
             * 1) parent is "case"
             * 2) switch-selector is an enum type (that way, the selector specifies the scope!)
             */
        	//EnumConstantSpecification ecs = (EnumConstantSpecification)((EnumDeclaration)getType(((Case)context.getASTParent()).getParent().getExpression())).getVariableInScope(name);
//        	EnumConstant ec = (EnumConstant)((ClassType)getType(((Case)context.getASTParent()).getParent().getExpression())).getVariableInScope(name)
        	ClassType container = (ClassType)getType(((Case)context.getASTParent()).getParent().getExpression());
        	for (Field f : container.getFields()) {
        		if (f instanceof EnumConstant && f.getName().equals(name))
        			return f;
        	}
        	// must not resolve! qualifying enum constant in case-statements is forbidden! 
        	return null;
        } 
        // look for the next variable scope equals to or parent of context
        ProgramElement pe = context;
        while (pe != null && !(pe instanceof VariableScope)) {
            context = pe;
            pe = pe.getASTParent();
        }
        if (DEBUG)
            Debug.log("Found scope " + Format.toString("%c @%p", pe));
        if (pe == null) {
            if (java5Allowed() && isPartOf(originalContext, PackageSpecification.class)) {
            	// if an AnnotationElementValuePair is resolved through a static import, this case may occur
            	List<Import> imports = UnitKit.getCompilationUnit(originalContext).getImports();
            	return getVariableFromStaticSingleImport(name, imports, null); 
            }
        	
        	// a null scope can happen if we try to find a variable
            // speculatively (for URQ resolution)
        	return null;
        }
        VariableScope scope = (VariableScope) pe;
        Variable result;
        do {
            result = scope.getVariableInScope(name);
            if (result != null) {
                if (DEBUG)
                    Debug.log("Found variable in scope " + Format.toString("%c @%p", scope));

                // must double check this result - rare cases of confusion
                // involving field references before a local variable of the
                // same name has been specified
                if (scope instanceof StatementBlock) {
                	StatementBlock cont = (StatementBlock) scope;
                    // we need the topmost var-scope including context,
                    // or context itself if the found scope is the topmost one
                    VariableDeclaration def = ((VariableSpecification) result).getParent();
                    for (int i = 0; true; i += 1) {
                        Statement s = cont.getStatementAt(i);
                        if (s == def) {
                            // stop if definition comes first
                            break;
                        }
                        if (s == context) {
                            // tricky: reference before definition - must
                            // ignore the definition :(
                            result = null;
                            break;
                        }
                    }
                } else if (scope instanceof Switch) {
                	// Two cases: (1) case 1: x = f; char f;
                	//			  (2) case 1: x = f; case 2: char f;
                	// case 2:
                	Switch sw = (Switch)scope;
                	NonTerminalProgramElement myCase = (NonTerminalProgramElement)context;
                	while (!(myCase instanceof Case || myCase instanceof Default)) {
                		myCase = myCase.getASTParent();
                	}
                	NonTerminalProgramElement otherCase = ((VariableSpecification) result).getParent();
                	while (!(otherCase instanceof Case || myCase instanceof Default)) {
                		otherCase = otherCase.getASTParent();
                	}
                	if (myCase == otherCase) {
                    	// case 1:
                    	Branch c = (Branch)context; // if scope is Switch, then context is Case.
                    	VariableDeclaration def = ((VariableSpecification) result).getParent();
                    	for (Statement s: c instanceof Case ? ((Case)c).getBody() : ((Default)c).getBody()) {
                    		if (s == def) {
                    			// stop if definition comes first
                                break;
                    		}
                            if (isTransitiveChildOf(originalContext, s)) {
                                // tricky: reference before definition - must
                                // ignore the definition :(
                                result = null;
                                break;
                            }
                    	}
                	} else {
                		// case 2:
                		if (sw.getIndexOfChild(myCase) < sw.getIndexOfChild(otherCase))
                			result = null;
                	}
                }
                if (result != null) {
                    // leave _now_
                    break;
                }
            }
            if (scope instanceof TypeDeclaration) {
                result = getInheritedField(name, (TypeDeclaration) scope);
                if (result != null) {
                    break;
                }
                // might want to check for ambiguity of outer class fields!!!
            }
            pe = scope.getASTParent();
            while (pe != null && !(pe instanceof VariableScope)) {
                context = pe; // proceed the context
                pe = pe.getASTParent();
            }
            scope = (VariableScope) pe;
        } while (scope != null);

        // last chance: field imported through static import?
        if (result == null && java5Allowed()) {
            List<Import> imports = UnitKit.getCompilationUnit(context).getImports();
            ClassType ct = originalContext instanceof ClassType ? 
            		(ClassType)originalContext : MiscKit.getParentTypeDeclaration(originalContext);
            
            result = getVariableFromStaticSingleImport(name, imports, ct);
            if (result == null) // try on demand
                result = getVariableFromStaticOnDemandImport(name, imports, ct);
        }
        return result;
    }
    
    private boolean isTransitiveChildOf(ProgramElement originalContext,
			Statement s) {
		NonTerminalProgramElement cur = originalContext.getASTParent();
		while (cur != null) {
			if (cur == s)
				return true;
			cur = cur.getASTParent();
		}
		return false;
	}

	// TODO make private again!
    public Variable getVariableFromStaticSingleImport(String name, List<Import> imports, ClassType context) {
        Variable result = null;
        Variable oldResult = null;
        Import firstImport = null; // for error handling only
        for (int i = 0, max = imports.size(); i < max; i++) {
            Import imp = imports.get(i);
            if (!imp.isStaticImport() || imp.isMultiImport()) continue;
            // has import correct name?
            if (!name.equals(imp.getStaticIdentifier().getText())) continue;
            // try to get field from this type's context.
            List<? extends Field> fields = getFields((ClassType)getType(imp.getTypeReference()));
            // see if any visible field matches
            for (int f = 0, maxF = fields.size(); f < maxF; f++) {
                Field field = fields.get(f);
                if (!field.isStatic()) continue;
                if (!field.getName().equals(name)) continue;
                // TODO the context == null is for the case that we're trying to
                // find a field through a static import in the context of a package specification
                // and we don't have any type declaration. Then, isVisibleFor() is not applicable
                // and we will need to write us a specialized isVisibleFor() method. That's the
                // work to be done...
                // also see inferType() !
                if (context == null || isVisibleFor(field, context)) {
                    result = field;
                    if (oldResult != null && oldResult != result) {
                        // Ambiguity
                        getErrorHandler().reportError(
                                new AmbiguousStaticFieldImportException(firstImport, imp, oldResult, result));
                        // go on if neccessary
                    }
                    firstImport = imp;
                    oldResult = field;
                    break; // maximum of one match per import
                }
                
            }
        }
        return result;
    }
    
    // TODO make private again !!!!
    public Variable getVariableFromStaticOnDemandImport(String name, List<Import> imports, ClassType context) {
        Debug.assertNonnull(name);
        Debug.assertNonnull(imports);
        Debug.assertNonnull(context);
        Variable result = null;
        Variable oldResult = null;
        Import firstImport = null; // for error handling only
        for (int i = 0, max = imports.size(); i < max; i++) {
            Import imp = imports.get(i);
            if (!imp.isStaticImport() || !imp.isMultiImport()) continue;
            // try to get field from this type's context.
            List<? extends Field> fields = getAllFields((ClassType)getType(imp.getTypeReference()));
            // see if any visible field matches
            for (int f = 0, maxF = fields.size(); f < maxF; f++) {
                Field field = fields.get(f);
                if (!field.isStatic()) continue;
                if (!field.getName().equals(name)) continue;
                if (isVisibleFor(field, context)) {
                    result = field;
                    if (oldResult != null && oldResult != result) {
                        // Ambiguity
                        getErrorHandler().reportError(
                                new AmbiguousStaticFieldImportException(firstImport, imp, oldResult, result));
                        // go on if neccessary
                    }
                    firstImport = imp;
                    oldResult = field;
                    break; // maximum of one match per import
                }
                
            }
        }
        return result;    }

    public final Variable getVariable(VariableSpecification vs) {
        return vs;
    }

    public Field getField(FieldReference fr) {
        Field res = (Field) reference2element.get(fr);
        if (res != null) {
            return res;
        }
        updateModel();
        String name = fr.getName();
        ReferencePrefix rp = fr.getReferencePrefix();
        if (rp == null) {
            res = (Field) getVariable(name, fr);
            if (res != null) {
                reference2element.put(fr, res);
            }
            return res;
        } else {
        	// Note: I out-commented the following. I don't see any reason
        	// why this is required, plus: It may simply happen
        	// for annotation uses on a package-declaration. There is 
        	// a case where this causes a problem.
        	// HOWEVER, that does not happen on initial model building
        	// but only after a transformation (that doesn't even affect
        	// the problematic source code) - TODO look into this!
        	// T. Gutzmann, July 10, 2009
//          ClassType thisType = getContainingClassType(fr);
//            if (thisType == null) {
//                return null;
//            }
            ClassType ct = (ClassType) getType(rp);
            if (ct == null || ct instanceof UnknownClassType) {
                return null;
            }
            List<? extends Field> fl = ct.getAllFields();
            if (fl == null) {
                return null;
            }
            for (int i = fl.size() - 1; i >= 0; i--) {
                res = fl.get(i);
                // going by == is ok, as Identifiers always use String.intern()
                if (res.getName() == name) {
                    reference2element.put(fr, res);
                    return res;
                }
            }
            return null;
        }
    }

    public Variable getVariable(VariableReference vr) {
        if (vr instanceof FieldReference) {
            return getField((FieldReference) vr);
        } else {
            Variable res = (Variable) reference2element.get(vr);
            if (res != null) {
                return res;
            }
            res = getVariable(vr.getName(), vr);
            if (res != null) {
                reference2element.put(vr, res);
            }
            return res;
        }
    }

    // args == null is admissible
    public List<Type> makeSignature(List<Expression> args) {
        if (args == null || args.isEmpty()) {
            return Collections.emptyList();
        }
        // updateModel(); not necessary
        int arity = args.size();
        List<Type> result = new ArrayList<Type>(arity);
        for (int i = 0; i < arity; i++) {
            Expression e = args.get(i);
            Type et = getType(e);
            if (et == null) {
                getErrorHandler().reportError(
                        new TypingException("Unknown type for argument #" + i + " in call "
                                + Format.toString(ELEMENT_LONG, e.getExpressionContainer()), e));
                et = getNameInfo().getUnknownType();
            }
            result.add(et);
        }
        return result;
    }

    public final Method getMethod(MethodDeclaration md) {
        return md;
    }

    public final Constructor getConstructor(ConstructorDeclaration cd) {
        return cd;
    }

    
    public AnnotationProperty getAnnotationProperty(AnnotationPropertyReference apr) {
    	AnnotationProperty res = (AnnotationProperty) reference2element.get(apr);
    	if (res != null) {
    		return res;
    	}
    	Type at = getType(apr.getParent().getParent().getTypeReference());
    	if (at instanceof ClassType && ((ClassType)at).isAnnotationType()) {
    		ClassType ct = (ClassType)at;
    		List<? extends Method> ml = ct.getMethods();
    		for (int i = 0; i < ml.size(); i++) {
    			if (ml.get(i).getName() == apr.getIdentifier().getText()) {
    				// TODO check for ambiguities (which mean invalid code)
    				// TODO better exception if it's actually a method and not an annotation property?
    				res = (AnnotationProperty) ml.get(i);
    				break;
    			}
    		}
			if (res == null) {
				getErrorHandler().reportError(
	                    new UnresolvedReferenceException(
	                            Format.toString("Could not resolve " + ELEMENT_LONG + " (12)", apr), apr));
	    		res = getNameInfo().getUnknownAnnotationProperty();
			}
    	} else {
    		if (at == null) {
				getErrorHandler().reportError(
	                    new UnresolvedReferenceException(
	                            Format.toString("Could not resolve " + ELEMENT_LONG + " (13)", apr), apr));
    		} else {
    			getErrorHandler().reportError(
    					new ModelException(
    							Format.toString(ELEMENT_LONG + " does not reference an annotation type!", apr)));
    			res = getNameInfo().getUnknownAnnotationProperty();
    		}
    	}
    	reference2element.put(apr, res);
    	return res;
    }

    public Method getMethod(MethodReference mr) {
    	Method res = (Method) reference2element.get(mr);
        if (res != null) {
            return res;
        }
        List<? extends Method> mlist = getMethods(mr);
        if (mlist == null || mlist.isEmpty()) {
            getErrorHandler().reportError(
                    new UnresolvedReferenceException(
                            Format.toString("Could not resolve " + ELEMENT_LONG + " (02)", mr), mr));
            return getNameInfo().getUnknownMethod();
        } else if (mlist.size() > 1) {
             getErrorHandler().reportError(
            		new AmbiguousReferenceException(Format.toString(ELEMENT_LONG
                            + " is ambiguous - it could be one of ", mr)
                            + Format.toString("%N", mlist), mr, mlist));
            // if we have to resume, use the first for the time being
        }
        res = mlist.get(0);

        if (res.getTypeParameters() != null && res.getTypeParameters().size() > 0) {
        	// generic method
        	if (mr.getTypeArguments() != null && mr.getTypeArguments().size() > 0) {
        		if (!(mr.getTypeArguments().size() == res.getTypeParameters().size())) {
        			// error
        			// TODO better exception
        			getErrorHandler().reportError(
        					new UnresolvedReferenceException(
        							Format.toString("Wrong number of type arguments " + ELEMENT_LONG, mr), mr));
        			return getNameInfo().getUnknownMethod();
        		}
        		res = new ResolvedGenericMethod(res, false, 
        				getTypeListFromTypeArgs(mr.getTypeArguments()), 
        				getServiceConfiguration().getImplicitElementInfo());
        	} else {
        		// infer type parameters
        		List<ClassType> inferredTypes = new ArrayList<ClassType>(res.getTypeParameters().size());
        		for (int i = res.getTypeParameters().size() - 1; i >= 0; i--) {
        			TypeParameter tp = res.getTypeParameters().get(i);
        			ClassType inferred = inferType(res, mr.getArguments(), mr.getASTParent(), tp, res.getTypeParameters(), inferredTypes);
        			inferredTypes.add(inferred);
        		}
    			Collections.reverse(inferredTypes);
        		res = new ResolvedGenericMethod(res, true,
        				inferredTypes, getServiceConfiguration().getImplicitElementInfo());
        	}
        }
        reference2element.put(mr, res);
        return res;
    }

    public List<Method> getMethods(MethodReference mr) {
        Debug.assertNonnull(mr);
        updateModel();
        List<Method> result = null; 
        List<Type> signature = makeSignature(mr.getArguments());
        ReferencePrefix rp = mr.getReferencePrefix();
        if (rp == null) {
            ClassType targetClass = getContainingClassType(mr);
            result = getMethods(targetClass, mr.getName(), signature, mr.getTypeArguments(), MiscKit.getParentTypeDeclaration(mr));
            // if we didn't find an adequate method - the target class may be
            // an inner or anonymous class. So we have to look "outside"
            if (result != null && result.size() > 0) {
                return result;
            }
            for (ClassTypeContainer ctc = targetClass.getContainer(); ctc != null; ctc = ctc.getContainer()) {
                if (ctc instanceof ClassType) {
                    result = getMethods((ClassType) ctc, mr.getName(), signature, mr.getTypeArguments(), MiscKit.getParentTypeDeclaration(mr));
                    if ((result != null) && (result.size() > 0)) {
                        return result;
                    }
                }
            }
            // If java 5 is supported, check if an appropriate method is imported through a static import
            if (java5Allowed()) {
            	List<Import> imports = UnitKit.getCompilationUnit(mr).getImports();
                result = getMethodsFromStaticSingleImports(mr, imports);
                if (result != null && result.size() > 0)
                    return result;
                result = getMethodsFromStaticOnDemandImports(mr, imports);
                if (result != null && result.size() > 0)
                    return result;
            }
            getErrorHandler().reportError(
                    new UnresolvedReferenceException(
                            Format.toString("Could not resolve " + ELEMENT_LONG + " (03)", mr), mr));
            List<Method> list = new ArrayList<Method>(1);
            list.add(getNameInfo().getUnknownMethod());
            result = list;
        } else {
        	Type rpt = getType(rp);
            if (rpt == null) {
                // TODO: voidMethod().illegal reports that voidMethod() cannot be resolved although
                // it exists and a more specific error message should occur
                getErrorHandler().reportError(
                        new UnresolvedReferenceException(Format.toString("Could not resolve " + ELEMENT_LONG + " (04)",
                                rp), rp));
                List<Method> list = new ArrayList<Method>(1);
                list.add(getNameInfo().getUnknownMethod());
                return list;
            }
            result = getMethods((ClassType) rpt, mr.getName(), signature, mr.getTypeArguments(), MiscKit.getParentTypeDeclaration(mr));
        }
        return result;
    }

    /**
     * @param mr
     * @param imports
     * @return
     */
    // TODO make private again!!??
    public List<Method> getMethodsFromStaticOnDemandImports(MethodReference mr, List<Import> imports) {
        NameInfo ni = getNameInfo();
        List<Method> result = new ArrayList<Method>();
        for (int i = 0, max = imports.size(); i < max; i++) {
            Import imp = imports.get(i);
            if (!imp.isStaticImport() || !imp.isMultiImport())
                continue;
            ClassType ct = ni.getClassType(Naming.toPathName(imp.getTypeReference()));
            if (ct == null) // unknown type. Already reported, and the unresolved method will be reported as well.
            	return Collections.emptyList();
            List<? extends Method> ml = ct.getMethods();
            for (int j = 0; j < ml.size(); j++) {
                Method m = ml.get(j);
                // is method static and has matching name?
                if (m.isStatic() && m.getName() == mr.getName())
                    result.add(m);
            }
        }
        List<Type> sig = makeSignature(mr.getArguments());
        return doThreePhaseFilter(result, sig, mr.getName(), MiscKit.getParentTypeDeclaration(mr));
    }

    /**
     * traverses all single static import declaration and looks for appropriate methods.
     * @param mr
     * @param imports
     * @return
     * @throws AmbiguousImportException if there are any ambiguities 
     */
    // TODO make private again!?
    public List<Method> getMethodsFromStaticSingleImports(MethodReference mr, List<Import> imports) {
        NameInfo ni = getNameInfo();
        List<Method> result = new ArrayList<Method>();
        for (int i = 0, max = imports.size(); i < max; i++) {
            Import imp = imports.get(i);
            if (!imp.isStaticImport() || imp.isMultiImport())
                continue;
            // is import applicable?
            if (imp.getStaticIdentifier().getText() != mr.getName())
                continue;
            ClassType ct = ni.getClassType(Naming.toPathName(imp.getTypeReference()));
            if (ct == null) // unknown type. Already reported, and the unresolved method will be reported as well.
            	return Collections.emptyList();
            List<? extends Method> ml = ct.getMethods();
            for (int j = 0; j < ml.size(); j++) {
                Method m = ml.get(j);
                // is method static and has matching name? (This is also checked again later)
                if (m.isStatic() && m.getName() == mr.getName())
                    result.add(m);
                // Could remove duplicates here (imports may be listed twice), but that's automatically done
                // by first pass of filterMostSpecificMethods()
            }
        }
        List<Type> sig = makeSignature(mr.getArguments());
        return doThreePhaseFilter(result, sig, mr.getName(), MiscKit.getParentTypeDeclaration(mr));
    }

    public Constructor getConstructor(ConstructorReference cr) {
        Constructor res = (Constructor) reference2element.get(cr);
        if (res != null) {
            return res;
        }
        List<? extends Constructor> clist = getConstructors(cr);
        if (clist == null || clist.isEmpty()) {
        	getErrorHandler().reportError(
                    new UnresolvedReferenceException(
                            Format.toString("Could not resolve " + ELEMENT_LONG + " (05)", cr), cr));
            return getNameInfo().getUnknownConstructor();
        } else if (clist.size() > 1) {
            getErrorHandler().reportError(
                    new AmbiguousReferenceException(Format.toString(ELEMENT_LONG
                            + " is ambiguous - it could be one of ", cr)
                            + Format.toString("%N", clist), cr, clist));
            // use the first, if we do have to continue
        }
        res = clist.get(0);
        reference2element.put(cr, res);
        return res;
    }

    public List<? extends Constructor> getConstructors(ConstructorReference cr) {
        updateModel();
        ClassType type = null;
        if (cr instanceof New) {
            New n = (New) cr;
            ReferencePrefix rp = n.getReferencePrefix();
            if (rp != null) {
                // In this case we need not do anything
            }
            type = (ClassType) getType(n.getTypeReference());
        } else if (cr instanceof ThisConstructorReference) {
            type = getContainingClassType(cr);
        } else if (cr instanceof SuperConstructorReference) {
            type = getContainingClassType(cr);
            List<? extends ClassType> superTypes = getSupertypes(type);
            for (int i = 0; i < superTypes.size(); i += 1) {
                type = superTypes.get(i);
                if (!type.isInterface()) {
                    break; // there must be one concrete class
                    // the exception would be parsing a super() call inside
                    // java.lang.Object ;)
                }
            }
        } else if (cr instanceof EnumConstructorReference) {
        	type = getContainingClassType(cr);
        } else {
            Debug.error("Unknown Constructor Reference " + cr);
        }
        if (type == null) {
            getErrorHandler().reportError(
                    new UnresolvedReferenceException(
                            Format.toString("Could not resolve " + ELEMENT_LONG + " (06)", cr), cr));
            List<Constructor> list = new ArrayList<Constructor>(1);
            list.add(getNameInfo().getUnknownConstructor());
            return list;
        }
        ClassType visibilityContext = cr instanceof New && ((New)cr).getClassDeclaration() != null ? ((New)cr).getClassDeclaration() : MiscKit.getParentTypeDeclaration(cr);  
        List<? extends Constructor> constrs = getConstructors(type, makeSignature(cr.getArguments()), visibilityContext);
        return constrs;
    }

    public List<TypeDeclaration> getTypes(TypeDeclaration td) {
        Debug.assertNonnull(td);
        updateModel();
        List<MemberDeclaration> members = td.getMembers();
        if (members == null) {
            return Collections.emptyList();
        }
        int s = members.size();
        ArrayList<TypeDeclaration> result = new ArrayList<TypeDeclaration>();
        for (int i = 0; i < s; i += 1) {
            MemberDeclaration m = members.get(i);
            if (m instanceof TypeDeclaration) {
                result.add((TypeDeclaration) m);
            }
        }
        result.trimToSize();
        return result;
    }

    public List<FieldSpecification> getFields(TypeDeclaration td) {
        Debug.assertNonnull(td);
        updateModel();
        List<MemberDeclaration> members = td.getMembers();
        if (members == null) {
            return Collections.emptyList();
        }
        int s = members.size();
        ArrayList<FieldSpecification> result = new ArrayList<FieldSpecification>();
        for (int i = 0; i < s; i += 1) {
            MemberDeclaration m = members.get(i);
            if (m instanceof FieldDeclaration) {
                result.addAll(((FieldDeclaration) m).getFieldSpecifications());
            } else if (m instanceof EnumConstantDeclaration) {
            	result.add(((EnumConstantDeclaration)m).getEnumConstantSpecification());
            }
        }
        result.trimToSize();
        return result;
        // was: td.getFieldsInScope(); -- faster, but not order preserving
    }

    public List<Method> getMethods(TypeDeclaration td) {
        Debug.assertNonnull(td);
        updateModel();
        List<MemberDeclaration> members = td.getMembers();
        if (members == null && !(td instanceof EnumDeclaration)) {
            return Collections.emptyList();
        }
        int s = (members == null) ? 0 : members.size();
        ArrayList<Method> result = new ArrayList<Method>();
        for (int i = 0; i < s; i += 1) {
            MemberDeclaration m = members.get(i);
            if (m instanceof MethodDeclaration) {
                if (!(m instanceof ConstructorDeclaration)) {
                    result.add((MethodDeclaration) m);
                }
            }
        }
        if (td instanceof EnumDeclaration) {
        	List<ImplicitEnumMethod> rl = serviceConfiguration.getImplicitElementInfo().getImplicitEnumMethods((EnumDeclaration)td);
        	result.add(rl.get(0));
        	result.add(rl.get(1));
        }
        result.trimToSize();
        return result;
    }

    public List<Constructor> getConstructors(TypeDeclaration td) {
        Debug.assertNonnull(td);
        updateModel();
        ArrayList<Constructor> result = new ArrayList<Constructor>();
        List<MemberDeclaration> members = td.getMembers();
        int s = (members == null) ? 0 : members.size();
        for (int i = 0; i < s; i += 1) {
            MemberDeclaration m = members.get(i);
            if (m instanceof ConstructorDeclaration) {
                result.add((ConstructorDeclaration) m);
            }
        }
        if (result.isEmpty() && !td.isInterface() && td.getName() != null) {
            result.add(serviceConfiguration.getImplicitElementInfo().getDefaultConstructor(td));
        }
        result.trimToSize();
        return result;
    }

    public Package getPackage(PackageReference pr) {
        Package res = (Package) reference2element.get(pr);
        if (res != null) {
            return res;
        }
        res = getNameInfo().createPackage(Naming.toPathName(pr));
        if (res != null) {
            reference2element.put(pr, res);
        }
        return res;
    }

    public Package getPackage(ProgramModelElement pme) {
        Debug.assertNonnull(pme);
        updateModel();
        Package result = null;
        ProgramElement pe = getDeclaration(pme);
        if (pe == null) {
            result = pme.getProgramModelInfo().getPackage(pme);
        } else {
            result = getNameInfo().createPackage(Naming.getPackageName(UnitKit.getCompilationUnit(pe)));
        }
        return result;
    }

    public List<? extends ClassType> getTypes(ClassTypeContainer ctc) {
        Debug.assertNonnull(ctc);
        updateModel();
        ProgramElement decl = getDeclaration(ctc);
        if (decl == null) {
            return ctc.getProgramModelInfo().getTypes(ctc);
        } else {
            while (decl != null && !(decl instanceof TypeScope)) {
                decl = decl.getASTParent();
            }
            Debug.assertNonnull(decl, "Internal error - scope inconsistency");
            return ((TypeScope) decl).getTypesInScope();
        }
    }

    public ClassTypeContainer getClassTypeContainer(ClassType ct) {
        Debug.assertNonnull(ct);
        TypeDeclaration td = getTypeDeclaration(ct);
        if (td == null) {
            return ct.getProgramModelInfo().getClassTypeContainer(ct);
        }
        // updateModel(); not necessary
        ProgramElement cur = td;
        NonTerminalProgramElement par = cur.getASTParent();
        while (par != null) {
            cur = par;
            if (cur instanceof ClassTypeContainer) {
                return (ClassTypeContainer) cur;
            }
            par = cur.getASTParent();
        }
        return getNameInfo().createPackage(Naming.getPackageName((CompilationUnit) cur));
    }

    List<ClassType> getTypeList(List<TypeReference> trl) {
        updateModel();
        int s = (trl != null) ? trl.size() : 0;
        List<ClassType> result = new ArrayList<ClassType>(s);
        for (int i = 0; i < s; i++) {
            result.add((ClassType) getType(trl.get(i)));
        }
        return result;
    }
    
    List<ClassType> getTypeListFromTypeArgs(List<TypeArgumentDeclaration> targs) {
    	updateModel();
    	List<ClassType> res = new ArrayList<ClassType>(targs.size());
    	for (TypeArgumentDeclaration tad : targs) {
    		if (tad.getWildcardMode() != WildcardMode.None)
    			throw new RuntimeException(); // TODO better exception...
    		res.add((ClassType) getType(tad.getTypeReference()));
    	}
    	return res;
    	
    }

    void addToTypeList(ArrayList<ClassType> result, List<TypeReference> trl) {
        //	updateModel();
        int s = (trl != null) ? trl.size() : 0;
        result.ensureCapacity(result.size() + s);
        for (int i = 0; i < s; i++) {
            TypeReference tr = trl.get(i);
            if (tr != null) {
                ClassType ct = (ClassType) getType(tr);
                if (ct == null) {
                    getErrorHandler().reportError(
                            new UnresolvedReferenceException(Format.toString("Unable to resolve " + ELEMENT_LONG, tr),
                                    tr));
                    ct = getNameInfo().getUnknownClassType();
                }
                result.add(ct);
            }
        }
    }

    public List<ClassType> getSupertypes(ClassType ct) {
        Debug.assertNonnull(ct);
        updateModel();
        TypeDeclaration td = getTypeDeclaration(ct);
        if (td == null) {
            return ct.getProgramModelInfo().getSupertypes(ct);
        } else {
            ClassTypeCacheEntry ctce = classTypeCache.get(ct);
            if (ctce == null) {
                classTypeCache.put(ct, ctce = new ClassTypeCacheEntry());
            } else if (ctce.supertypes != null) {
                return new ArrayList<ClassType>(ctce.supertypes);
            }
            ArrayList<ClassType> res = new ArrayList<ClassType>();
            if (td instanceof EnumDeclaration) {
            	// java.lang.Enum
            	res.add(getNameInfo().getJavaLangEnum());
            	// plus interfaces
            	Implements imp = ((EnumDeclaration)td).getImplementedTypes();
                if (imp != null) {
                    addToTypeList(res, imp.getSupertypes());
                }
            } else if (td instanceof AnnotationDeclaration) {
                // only java.lang.annotation.Annotation and java.lang.Object
                res.add(getNameInfo().getJavaLangAnnotationAnnotation());
                res.add(getNameInfo().getJavaLangObject());
            } else if (td instanceof InterfaceDeclaration) {
                InterfaceDeclaration id = (InterfaceDeclaration) td;
                Extends ext = id.getExtendedTypes();
                if (ext != null) {
                    addToTypeList(res, ext.getSupertypes());
                }
                res.add(getNameInfo().getJavaLangObject());
            } else if (td instanceof TypeParameterDeclaration) {
            	TypeParameterDeclaration tp = (TypeParameterDeclaration)td;
            	if (tp.getBounds() == null || tp.getBounds().size() == 0) {
            		// see JLS 3rd edition §4.4
                	res.add(getNameInfo().getJavaLangObject());
            	} else {
            		for (TypeReference tr : tp.getBounds()) {
            			res.add((ClassType) getType(tr));
            		}
            	}
            } else {
                ClassDeclaration cd = (ClassDeclaration) td;
                assert cd.getClass() == ClassDeclaration.class; // we haven't forgotten anything, have we?
                ClassType jlo = getNameInfo().getJavaLangObject();

                // Anonymous classes need special care
                TypeDeclarationContainer con = cd.getParent();
                if (con instanceof New) {
                    TypeReference tr = ((New) con).getTypeReference();
                    ClassType newType = (ClassType) getType(tr);
                    if (newType.isInterface())
                    	res.add(jlo); // this is extra!
                    res.add(newType);
                } else if (con instanceof EnumConstructorReference) { 
                	EnumDeclaration ed = (EnumDeclaration)con.getASTParent().getASTParent().getASTParent();
                	res.add(ed);
                } else {
                    Extends ext = cd.getExtendedTypes();
                    if (ext == null || ext.getSupertypes().size() == 0) {
                    	if (cd != jlo) // implicitly
                    		res.add(getNameInfo().getJavaLangObject());
                    } else {
                        addToTypeList(res, ext.getSupertypes());
                    }
                    Implements imp = cd.getImplementedTypes();
                    if (imp != null) {
                        addToTypeList(res, imp.getSupertypes());
                    }
                }
            }
            res.trimToSize();
            ctce.supertypes = res;
            // copy! Don't expose internal data structure!
            return new ArrayList<ClassType>(res);
        }
    }

    public List<? extends Field> getFields(ClassType ct) {
        Debug.assertNonnull(ct);
        updateModel();
        List<? extends Field> result = null;
        TypeDeclaration td = getTypeDeclaration(ct);
        if (td == null) {
            result = ct.getProgramModelInfo().getFields(ct);
        } else {
            result = getFields(td);
        }
        return result;
    }

    public List<Method> getMethods(ClassType ct) {
        Debug.assertNonnull(ct);
        updateModel();
        List<Method> result = null;
        TypeDeclaration td = getTypeDeclaration(ct);
        if (td == null) {
            result = ct.getProgramModelInfo().getMethods(ct);
        } else {
            result = getMethods(td);
        }
        return result;
    }

    public List<? extends Constructor> getConstructors(ClassType ct) {
        Debug.assertNonnull(ct);
        updateModel();
        List<? extends Constructor> result = null;
        TypeDeclaration td = getTypeDeclaration(ct);
        if (td == null) {
            result = ct.getProgramModelInfo().getConstructors(ct);
        } else {
            result = getConstructors(td);
        }
        return result;
    }

    public List<Type> getSignature(Method m) {
        Debug.assertNonnull(m);
        updateModel();
        MethodDeclaration md = getMethodDeclaration(m);
        if (md == null) {
            return m.getProgramModelInfo().getSignature(m);
        } else {
        	List<Type> res = sigCache.get(md);
        	if (res != null)
        		return new ArrayList<Type>(res); // don't pass original to the outside!
        	List<ParameterDeclaration> pdl = md.getParameters();
            int params = (pdl == null) ? 0 : pdl.size();
        	res = new ArrayList<Type>(params);
            for (int i = 0; i < params; i++) {
            	Type ptype = getType(pdl.get(i).getVariables().get(0));
            	res.add(ptype);
            }
            sigCache.put(md, res);
            return new ArrayList<Type>(res); // don't pass original to the outside!
        }
    }

    public List<ClassType> getExceptions(Method m) {
        Debug.assertNonnull(m);
        updateModel();
        List<ClassType> result = Collections.emptyList();
        MethodDeclaration md = getMethodDeclaration(m);
        if (md == null) {
            result = m.getProgramModelInfo().getExceptions(m);
        } else {
            Throws t = md.getThrown();
            if (t != null) {
                result = getTypeList(t.getExceptions());
            }
        }
        return result;
    }

    public Type getReturnType(Method m) {
        Debug.assertNonnull(m);
        updateModel();
        Type result = null;
        MethodDeclaration md = getMethodDeclaration(m);
        if (md == null) {
            result = m.getProgramModelInfo().getReturnType(m);
        } else {
            TypeReference tr = md.getTypeReference();
            if (tr != null && !"void".equals(tr.getName())) {
                result = getType(tr);
            }
        }
        return result;
    }
    
    public Type getAnnotationType(AnnotationUseSpecification au) {
    	Debug.assertNonnull(au);
    	updateModel();
    	Type result = null;
   		TypeReference tr = au.getTypeReference();
   		if (tr != null) { // TODO what if tr == null? That'd be wrong...
   			result = getType(tr);
    	}
    	return result;
    }

    public Reference resolveURQ(UncollatedReferenceQualifier urq) {
        boolean allowVariables = true;
    	NonTerminalProgramElement parent = urq;
        while (parent instanceof UncollatedReferenceQualifier) {
        	parent = parent.getASTParent();
        	if (parent instanceof TypeReference || parent instanceof PackageReference) {
        		allowVariables = false;
        		break;
        	}
        }
        return resolveURQ(urq, allowVariables);
    }

    private Reference resolveURQ(UncollatedReferenceQualifier urq, boolean allowVariables) {
        Debug.assertNonnull(urq);
        ReferencePrefix rp = urq.getReferencePrefix();
        if (rp instanceof UncollatedReferenceQualifier) {
            rp = (ReferencePrefix) resolveURQ((UncollatedReferenceQualifier) rp, allowVariables);
        }
        updateModel();
        Reference result = null;
        NameInfo ni = getNameInfo();
        NonTerminalProgramElement parent = urq.getASTParent();
        String urqName = urq.getName();

        if (rp == null) {
            if (allowVariables) {
                // is it a variable?
                Variable v = getVariable(urqName, urq);
                if (v != null) {
                    result = (v instanceof Field) ? urq.toFieldReference() : urq.toVariableReference();
                    reference2element.put(result, v);
                }
            }
            /*
             * else if (parent instanceof MethodReference) { // this case is
             * common enough for special treatment result =
             * urq.toTypeReference(); }
             */
            if (result == null) {
            	// is the URQ a reference to an already known package?
                Package pkg = ni.getPackage(urqName);
                if (pkg != null) {
                    result = urq.toPackageReference();
                    reference2element.put(result, pkg);
                } else {
                    // the urq might only be either a type or a package ref
                    Type t = getType(urqName, urq);
                    if (t != null) {
                        result = urq.toTypeReference();
                        if (urq.getTypeArguments() != null && urq.getTypeArguments().size() > 0) {
                        	t = getServiceConfiguration().getImplicitElementInfo().getParameterizedType((ClassType)t, urq.getTypeArguments()); 
                        } else {
                        	t = checkEraseRequired((TypeReference)result, t);
                        }
                        reference2element.put(result, t);
                    } else if (resolveURQaggressively(urq)) {
                    	// now try again. Packages to an obviously existing (bytecode) type
                    	// should now be registered.
                    	// fixes bug 2134267
                    	
                    	// no need to register reference - done by recursive call
                    	return resolveURQ(urq, allowVariables);  
                    } else if (isPartOf(urq, Import.class) && getGrandParentOfType(urq, Import.class).isMultiImport()) {
                    	// assume it's a package reference. If this was a multi-import with a type reference,
                    	// then the type is definitely not known. However, this can also be a package reference
                    	// to a yet-unknown package. That would be a valid case.
                    	// It doesn't really hurt if this is not a known package yet we don't report an error, as this
                    	// import is then surely unused anyway.
                    	result = urq.toPackageReference();
                    } else if (urqName.charAt(0) >= 'A' && urqName.charAt(0) <= 'Z') {
                        // assume coding conventions were followed! There is no other
                        // means of telling what this is at this time...
                        result = urq.toTypeReference();
                        // unknown type...
                        getErrorHandler().reportError(
                                new UnresolvedReferenceException(Format.toString("Could not resolve "
                                        + ELEMENT_LONG + " (07b)", urq), urq));
                    } else {
                        // should be a reference to an unknown package
                        // however, this can also be something else, but once again,
                        // we can't tell...
                        try {
                            result = urq.toPackageReference();
                        } catch (ClassCastException cce) {
                            getErrorHandler().reportError(
                                    new UnresolvedReferenceException(Format.toString("Could not resolve "
                                            + ELEMENT_LONG + " (07)", urq), urq));
                            result = urq.toTypeReference();
                        }
                    }
                }
            }
        } else if (rp instanceof ThisReference) {
            // the URQ can only be a local inner type or an attribute
            TypeScope thisScope;
            ReferencePrefix rpp = ((ThisReference) rp).getReferencePrefix();
            if (rpp == null) {
                thisScope = (TypeScope) getContainingClassType(urq);
            } else {
                TypeReference tr = (rpp instanceof TypeReference) ? (TypeReference) rpp : (TypeReference) resolveURQ(
                        (UncollatedReferenceQualifier) rpp, false);
                Type type = getType(tr);
                if (type instanceof ErasedType)
                	type = ((ErasedType)type).getGenericType();
                if (type instanceof ParameterizedType)
                	type = ((ParameterizedType)type).getGenericType();
                thisScope = (TypeDeclaration)type; 
            }
            Variable v = getVariable(urqName, thisScope);
            if (v != null) {
                result = urq.toFieldReference();
                reference2element.put(result, v);
            } else {
                // the URQ is either a type reference or invalid
                Type refT = thisScope.getTypeInScope(urqName);
                if (refT != null) {
                    result = urq.toTypeReference();
                    refT = checkEraseRequired((TypeReference)result, refT);
                    reference2element.put(result, refT);
                }
            }
        } else if (rp instanceof SuperReference) {
            // the URQ can only be an inner type or a field reference
            ClassType superType = (ClassType) getType(rp);
            Field f = getInheritedField(urq.getName(), superType);
            if (f != null) {
                result = urq.toFieldReference();
                reference2element.put(result, f);
            } else {
                String fullname = Naming.getFullName(superType, urq.getName());
                ClassType ct = ni.getClassType(fullname);
                if (ct != null) {
                    result = urq.toTypeReference();
                    ct = (ClassType)checkEraseRequired((TypeReference)result, ct);
                    reference2element.put(result, ct);
                }
            }
        } else if (rp instanceof PackageReference) {
            String fullRefName = Naming.toPathName(urq);
            // is the URQ a reference to an already known package?
            Package pkg = ni.getPackage(fullRefName);
            if (pkg != null) {
                result = urq.toPackageReference();
                reference2element.put(result, pkg);
            } else {
                // is it a type?
                Type t = ni.getClassType(fullRefName);
                if (t != null) {
                    result = urq.toTypeReference();
                    t = checkEraseRequired((TypeReference)result, t);
                    reference2element.put(result, t);
                } else {
                    // if the reference suffix is a method/constructor or field reference, then this must be an unknown type.
                    if (urq.getReferenceSuffix() instanceof MethodReference || 
                            (allowVariables && urq.getReferenceSuffix() instanceof FieldReference))
                            {
                        result = urq.toTypeReference();
                    } else {
                        // this should be a package reference otherwise.
                        // we can't really say, so we will regard it as a package reference and 
                        // cope with some special handling later
                        result = urq.toPackageReference();
                    }
                }
            }
        } else if ((rp instanceof TypeReference) || (rp instanceof Expression)) {
            //  includes VariableReferences
            Type refT = getType(rp);
            if (refT instanceof ClassType) {
            	ClassType ct = (ClassType)refT;
                if (allowVariables) {
                    Field f = getInheritedField(urq.getName(), ct);
                    if (f != null) {
                        result = urq.toFieldReference();
                        reference2element.put(result, f);
                    }
                }
                if (result == null) {
                    String fullname = Naming.getFullName((ClassType) refT, urq.getName());
                    ClassType innerType = ni.getClassType(fullname);
                    if (getInheritedType(urq.getName(), (ClassType)refT) != null) {
                    	result = urq.toTypeReference();
                        innerType = (ClassType)checkEraseRequired((TypeReference)result, innerType);
                    	reference2element.put(result, innerType);
                    }
                }
            } else {
                getErrorHandler().reportError(
                        new UnresolvedReferenceException(Format.toString("Could not resolve " + ELEMENT_LONG + " (09)",
                                rp), rp));
                // this would have been a class or a field
                result = urq;
            }
        } else {
            getErrorHandler().reportError(
                    new UnresolvedReferenceException(
                            Format.toString("Could not resolve " + ELEMENT_LONG + " (10)", rp), rp));
            // this would have been a class or a field or a package
            result = urq;
        }
        if (result == null) {
            getErrorHandler().reportError(
                    new UnresolvedReferenceException(Format
                            .toString("Could not resolve " + ELEMENT_LONG + " (11)", urq), urq));
            result = urq;
        } else if (result != urq) {
            try {
                parent.replaceChild(urq, result);
            } catch (ClassCastException cce) {
                /* If guessed wrong before further up in this method
                 * about whether something unknown was a package, type, or field reference,
                 * this (hopefully) corrects that bad guess.
                 * This special case arises if a field to an unknown
                 * type, or a not existing field to a known type 
                 * is referenced in an expression.
                 * We now can at least make another guess.
                 */
                
                boolean throwAgain = true;
                if (!(result instanceof Expression)) {
                    if (result instanceof PackageReference) {
                        PackageReference pr = (PackageReference)result;
                        ProgramFactory pf = result.getFactory();
                        Package pack = pf.getServiceConfiguration().getNameInfo().getPackage(pr.toSource());
                        if (pack == null) {
                            PackageReference pkgToBeReplacedByType = pr.getPackageReference();
                            PackageReference newPr = null;
                            TypeReference typeRef = null;
                            if (pkgToBeReplacedByType != null) {
                                newPr = pr.getPackageReference().getPackageReference();
                                typeRef = pf.createTypeReference(newPr, pkgToBeReplacedByType.getIdentifier());
                            }
                            result = pf.createFieldReference(typeRef, pr.getIdentifier());
                            result.setStartPosition(pr.getStartPosition());
                            result.setEndPosition(pr.getEndPosition());
                            result.setRelativePosition(pr.getRelativePosition());
                            result.setComments(pr.getComments());
                            throwAgain = false;
                            parent.replaceChild(urq, result);
                        }
                    }
                    if (urq.getASTParent() instanceof MethodReference && ((urq.getASTParent().getChildPositionCode(urq) & 2) == 2)) {
                    	// an expression is required. Thus, the URQ should be a field reference. Fixes bug 3346259
                    	result = urq.toFieldReference();
                    	parent.replaceChild(urq, result);
                    }
                }
                if (throwAgain) throw cce;
            }
            Debug.assertBoolean(parent == result.getASTParent());
        }
        return result;
    }


    
    private boolean resolveURQaggressively(UncollatedReferenceQualifier urq) {
		ReferenceSuffix suf = urq.getReferenceSuffix();
		NameInfo ni = getNameInfo();
    	while (suf instanceof TypeReferenceInfix) {
    		ClassType ct;
			if ((ct = ni.getClassType(Naming.toPathName((TypeReferenceInfix)suf))) != null) {
				while (ct instanceof ArrayType) {
					ct = (ClassType)((ArrayType)ct).getBaseType();
				}
				// TODO this is a QUICK bugfix - investigate this, w/o this we sometimes get
				// endless recursion/stack overflow!!! Because for some reason,
				// PARENT-packages may not be registered in name info !?
				String pkg = ct.getPackage().getFullName();
				for(;;) {
					getNameInfo().createPackage(pkg);
					int idx = pkg.lastIndexOf('.');
					if (idx == -1) break;
					pkg = pkg.substring(0, idx);
				}
				
				return true;
			}
			suf = ((TypeReferenceInfix)suf).getReferenceSuffix();
		}
    	return false;
	}

	/**
     * looks for the next variable scope that is a parent of the given element
     * 
     * @param pe
     *            a program element
     * @return the outer variable scope of the program element or <tt>null</tt>
     */
    private static VariableScope findOuterVariableScope(VariableScope ts) {
        NonTerminalProgramElement pe = ts.getASTParent();
        while (pe != null && !(pe instanceof VariableScope)) {
            pe = pe.getASTParent();
        }
        return (VariableScope) pe;
    }
    
    public List<Statement> getSucceedingStatements(Statement s) {
    	List<Statement> list = new ArrayList<Statement>();
        if (s instanceof LoopStatement) {
            LoopStatement loop = (LoopStatement) s;
            switch (getBooleanStatus(loop.getGuard())) {
            case CONSTANT_TRUE:
                if (loop.getBody() != null) {
                    list.add(loop.getBody());
                }
                break;
            case CONSTANT_FALSE:
                if (loop.isCheckedBeforeIteration()) {
                    // while, for
                    addSequentialFollower(s, list);
                } else {
                    // do
                    if (loop.getBody() != null) {
                        list.add(loop.getBody());
                    }
                    addSequentialFollower(s, list);
                }
                break;
            case NOT_CONSTANT:
                if (loop.getBody() != null) {
                    list.add(loop.getBody());
                }
                addSequentialFollower(s, list);
                break;
            }
        } else if (s instanceof LabeledStatement) {
            list.add(((LabeledStatement) s).getBody());
        } else if (s instanceof StatementBlock) {
            List<Statement> slist = ((StatementBlock) s).getBody();
            if (slist == null || slist.isEmpty()) {
                addSequentialFollower(s, list);
            } else {
                list.add(slist.get(0));
            }
        } else if (s instanceof SynchronizedBlock) {
        	List<Statement> slist = ((SynchronizedBlock) s).getBody().getBody();
            if (slist == null || slist.isEmpty()) {
                addSequentialFollower(s, list);
            } else {
                list.add(slist.get(0));
            }
        } else if (s instanceof If) {
            If ifstmt = (If) s;
            if (ifstmt.getElse() != null) {
                list.add(ifstmt.getThen().getBody());
                list.add(ifstmt.getElse().getBody());
            } else {
                list.add(ifstmt.getThen().getBody());
                addSequentialFollower(s, list);
            }
        } else if (s instanceof Switch) {
            List<Branch> branches = ((Switch) s).getBranchList();
            if (branches == null || branches.isEmpty()) {
                addSequentialFollower(s, list);
            } else {
                boolean hasDefault = false;
                for (int i = 0, c = branches.size(); i < c; i += 1) {
                    Branch b = branches.get(i);
                    List<Statement> stats = null;
                    if (b instanceof Default) {
                        stats = ((Default) b).getBody();
                        if (i < c - 1 || (stats != null && !stats.isEmpty())) {
                            // an empty default as last branch is not
                            // significant
                            hasDefault = true;
                        }
                    } else if (b instanceof Case) {
                        stats = ((Case) b).getBody();
                    }
                    if (stats != null && !stats.isEmpty()) {
                        list.add(stats.get(0));
                    }
                }
                if (!hasDefault) {
                    addSequentialFollower(s, list);
                }
            }
        } else if (s instanceof Try) {
            list.add(((Try) s).getBody());
            List<Branch> branches = ((Try) s).getBranchList();
            if (branches == null || branches.isEmpty()) {
                addSequentialFollower(s, list);
                return list;
            }
            for (int i = 0; i < branches.size(); i += 1) {
                Branch b = branches.get(i);
                if (b instanceof Catch) {
                    Catch ca = (Catch) b;
                    boolean newException = true;
                    if (i > 0) {
                        ClassType ex = (ClassType) getType(ca.getParameterDeclaration().getTypeReference());
                        for (int j = i - 1; j >= 0; j -= 1) {
                            if (branches.get(j) instanceof Catch) {
                                ClassType dx = (ClassType) getType(((Catch) branches.get(j))
                                        .getParameterDeclaration().getTypeReference());
                                if (isSubtype(ex, dx)) {
                                    // exception was already caught
                                    newException = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (newException) {
                        list.add(ca.getBody());
                    }
                } else if (b instanceof Finally) {
                    list.add(((Finally) b).getBody());
                }
            }
            addSequentialFollower(s, list);
        } else if (s instanceof ExpressionJumpStatement) {
            // Return, Throw
            list.add(METHOD_EXIT);
        } else if (s instanceof Break) {
            if (((Break) s).getIdentifier() == null) {
                addSequentialFollower(findInnermostBreakBlock(s), list);
            } else {
                addSequentialFollower(StatementKit.getCorrespondingLabel((Break) s), list);
            }
        } else if (s instanceof Continue) {
            if (((Continue) s).getIdentifier() == null) {
                list.add(findInnermostLoop(s));
            } else {
                list.add(StatementKit.getCorrespondingLabel((Continue) s).getBody());
            }
        } else {
            /*
             * ConstructorReference: EmptyStatement: ExpressionStatement:
             * LoopInitializer: ClassDeclaration: Assert:
             */
            addSequentialFollower(s, list);
        }
        return list;
    }

    private static void addSequentialFollower(Statement s, List<Statement> list) {
        Debug.assertNonnull(s);
        StatementContainer parent = s.getStatementContainer();
        while (true) {
            int c = parent.getStatementCount();
            int p = 0;
            while (parent.getStatementAt(p) != s) {
                p += 1;
            }
            if (p < c - 1) {
                list.add(parent.getStatementAt(p + 1));
                break;
            }
            if (parent instanceof MemberDeclaration) {
                list.add(METHOD_EXIT);
                break;
            }
            if (parent instanceof Statement) {
                if (parent instanceof LoopStatement) {
                    LoopStatement loop = (LoopStatement) parent;

                    list.add(loop);
                    return;
                }
                s = (Statement) parent;
                parent = ((Statement) parent).getStatementContainer();
            } else {
                while (parent instanceof Branch) {
                    s = ((Branch) parent).getParent();
                    parent = s.getStatementContainer();
                }
            }
        }
    }

    private final static int CONSTANT_FALSE = 0;

    private final static int CONSTANT_TRUE = 1;

    private final static int NOT_CONSTANT = -1;

    private int getBooleanStatus(Expression expr) {
        if (expr == null) { // handle "for(...;;...)" situation
            return CONSTANT_TRUE;
        }
        ConstantEvaluator.EvaluationResult evr = new ConstantEvaluator.EvaluationResult();
        if (serviceConfiguration.getConstantEvaluator().isCompileTimeConstant(expr, evr)) {
            return evr.getBoolean() ? CONSTANT_TRUE : CONSTANT_FALSE;
        }
        return NOT_CONSTANT;
    }

    // LoopStatement or Switch
    private static Statement findInnermostBreakBlock(Statement s) {
        NonTerminalProgramElement parent = s.getStatementContainer();
        while (parent != null && !(parent instanceof MemberDeclaration)) {
            if ((parent instanceof LoopStatement) || (parent instanceof Switch)) {
                return (Statement) parent;
            }
            parent = parent.getASTParent();
        }
        return null;
    }

    private static LoopStatement findInnermostLoop(Statement s) {
        NonTerminalProgramElement parent = s.getStatementContainer();
        while (parent != null && !(parent instanceof MemberDeclaration)) {
            if (parent instanceof LoopStatement) {
                return (LoopStatement) parent;
            }
            parent = parent.getASTParent();
        }
        return null;
    }

    /**
     * Analyzes the given program subtree. It is required that the tree has
     * consistent parent links; this is done by the parser frontends or by
     * calling make(All)ParentRole(s)Valid(). If the program element is not a
     * CompilationUnit, it must have a valid parent.
     * 
     * @param pe
     *            the program element to add.
     */
    public void register(ProgramElement pe) {
        Debug.assertNonnull(pe);
        if (pe instanceof CompilationUnit) {
            if (!((CompilationUnit) pe).isDefinedScope()) {
                analyzeProgramElement(pe);
            }
        } else {
            Debug.assertNonnull(pe.getASTParent());
            analyzeProgramElement(pe);
        }
    }

    /**
     * analyzes the given tree element within the specified scope.
     * 
     * @param pe
     *            the root element of the tree to be analyzed
     */
    private void analyzeProgramElement(ProgramElement pe) {
        Debug.assertNonnull(pe);
        if (pe instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) pe;
            String packageName = Naming.getPackageName(cu);
            getNameInfo().createPackage(packageName);
        }
        analyzeProgramElement0(pe);
    }

    static final class FastWorkList {
    	private int size = 0;
    	private ProgramElement[] pes = new ProgramElement[1024];
    	void add(ProgramElement pe) {
    		int l = pes.length;
    		if (size == l) {
    			ProgramElement[] npes = new ProgramElement[l*2];
    			System.arraycopy(pes, 0, npes, 0, l);
    			pes = npes;
    		}
    		pes[size++] = pe;
    	}
    	ProgramElement removeLast() {
    		ProgramElement res = pes[--size];
    		pes[size] = null;
    		return res;
    	}
    	ProgramElement peekLast() {
    		return pes[size-1];
    	}
    	boolean isEmpty() { return size == 0; }
    }
    
    private FastWorkList worklist = new FastWorkList();
    private void analyzeProgramElement0(ProgramElement pe) {
    	worklist.add(pe);
    	while (!worklist.isEmpty()) {
    		pe = worklist.removeLast();
    		
    		if (pe instanceof TerminalProgramElement) {
    			continue;
    		}
    		// traversal will continue with the children of this element
    		if (pe instanceof ScopeDefiningElement) {
    			((ScopeDefiningElement) pe).setDefinedScope(true);
    			if (pe instanceof MethodDeclaration) {
    				// also for ConstructorDeclarations
    				((MethodDeclaration) pe).setProgramModelInfo(this);
    			} else if (pe instanceof TypeDeclaration) {
    				TypeDeclaration td = (TypeDeclaration) pe;
    				td.setProgramModelInfo(this);
    				String typename = td.getName();
    				if (typename != null) {
    					NonTerminalProgramElement parent = pe.getASTParent();
    					// usually, the type scope is just the parent
    					// there are few exceptions, such as labeled or switch
    					// statements
    					while (!(parent instanceof TypeScope)) {
    						parent = parent.getASTParent();
    					}
    					TypeScope scope = (TypeScope) parent;
    					ClassType dup = scope.getTypeInScope(typename);
    					if (dup != null && dup != td) {
    						getErrorHandler().reportError(
    								new AmbiguousDeclarationException("Duplicate declaration of "
    										+ Format.toString(ELEMENT_SHORT, td) + " - was "
    										+ Format.toString(ELEMENT_SHORT, dup), td, dup));
    						// continue anyway, if we have to
    					}
    					scope.addTypeToScope(td, typename);
    					if (DEBUG)
    						Debug.log(Format.toString("Registering %N", td));
    					getNameInfo().register(td);
    				}
    			} else if (pe instanceof ClassInitializer) {
    				((ClassInitializer)pe).setProgramModelInfo(this);
    			}
    		} else if (pe instanceof VariableSpecification) {
    			// also for FieldSpecification
    			VariableSpecification vs = (VariableSpecification) pe;
    			vs.setProgramModelInfo(this);
    			NonTerminalProgramElement parent = vs.getASTParent().getASTParent();
    			// usually, the variable scope is the grand parent
    			// there are few exceptions, such as labeled or switch statements
    			while (!(parent instanceof VariableScope)) {
    				parent = parent.getASTParent();
    			}
    			VariableScope scope = (VariableScope) parent;

    			String vname = vs.getName();
    			Variable dup = scope.getVariableInScope(vname);
    			if (dup != null && dup != vs) {
    				getErrorHandler().reportError(
    						new AmbiguousDeclarationException("Duplicate declaration of "
    								+ Format.toString(ELEMENT_SHORT, vs) + " - was " + Format.toString(ELEMENT_SHORT, dup),
    								vs, dup));
    				// continue anyway, if we have to resume
    			}
    			// check if the new variable hides a local variable
    			if (!(scope instanceof TypeDeclaration)) {
    				for (VariableScope outer = findOuterVariableScope(scope); !(outer instanceof TypeDeclaration); outer = findOuterVariableScope(outer)) {
    					dup = outer.getVariableInScope(vname);
    					if (dup != null && isDefinedBefore((VariableSpecification)dup, vs)) {
    						getErrorHandler().reportError(
    								new AmbiguousDeclarationException("Hidden local declaration: "
    										+ Format.toString(ELEMENT_SHORT, vs) + " - hides "
    										+ Format.toString(ELEMENT_SHORT, dup), vs, dup));
    						// resume anyway
    					}
    				}
    			}
    			scope.addVariableToScope(vs);
    			if (vs instanceof FieldSpecification) {
    				getNameInfo().register((Field) vs);
    			}
    		}
    		NonTerminalProgramElement cont = (NonTerminalProgramElement) pe;
    		int childCount = cont.getChildCount();
    		for (int i = childCount-1; i >= 0; i--) {
    			worklist.add(cont.getChildAt(i));
    		}
    	}
    }

    // NOTE: this code is only supposed to be used for fixing bug 2071287
	private boolean isDefinedBefore(VariableSpecification before, VariableSpecification after) {
		NonTerminalProgramElement hackk = before; 
		NonTerminalProgramElement parent = before.getASTParent();
		while (!(parent instanceof VariableScope)) {
			hackk = parent;
			parent = parent.getASTParent();
		}
		NonTerminalProgramElement afterP = after.getASTParent();
		while (afterP != null && afterP.getASTParent() != parent)
			afterP = afterP.getASTParent();
		if (afterP == null) 
			return true; // ??? <- imitate old behaviour, I currently have no clue if this is correct...
		int idxBefore = parent.getIndexOfChild(hackk);
		int idxAfter = parent.getIndexOfChild(afterP);
		return idxBefore < idxAfter;
	}

	void unregister(TypeDeclaration td) {
        unregister(td, td.getName());
    }

    /**
     * Remove given type from outer scope, from name info global dictionary, and
     * from subtype list of all known supertypes (if necessary).
     */
    void unregister(TypeDeclaration td, String shortname) {
        if (shortname != null) {
            ((TypeScope) (td.getASTParent())).removeTypeFromScope(shortname);
        }
        getNameInfo().unregisterClassType(td.getFullName());
        ClassTypeCacheEntry ctce = classTypeCache.get(td);
        if (ctce != null) {
            List<? extends ClassType> superTypes = ctce.supertypes;
            if (superTypes != null) {
                for (int i = superTypes.size() - 1; i >= 0; i -= 1) {
                    removeSubtype(td, superTypes.get(i));
                }
            }
        }
    }

    void unregister(VariableSpecification vs) {
        unregister(vs, vs.getName());
    }

    void unregister(VariableSpecification vs, String shortname) {
        ProgramElement pe = vs.getASTParent().getASTParent();
        while (!(pe instanceof VariableScope)) {
            pe = pe.getASTParent();
        }
        ((VariableScope) pe).removeVariableFromScope(shortname);
        if (vs instanceof FieldSpecification) {
            ClassType ct = ((Field) vs).getContainingClassType();
            getNameInfo().unregisterField(Naming.getFullName(ct, shortname));
        }
    }

    /**
     * unregisters the information, that has been computed when registering the
     * given element.
     * 
     * @param pe
     *            the program element to be unregistered
     */
    void unregister(ProgramElement pe) {
        Debug.assertNonnull(pe);
        if (pe instanceof TypeDeclaration) {
            unregister((TypeDeclaration) pe);
        } else if (pe instanceof VariableSpecification) {
            unregister((VariableSpecification) pe);
        } else if (pe instanceof VariableDeclaration) {
        	List<? extends VariableSpecification> vspecs = ((VariableDeclaration) pe).getVariables();
            for (int i = vspecs.size() - 1; i >= 0; i -= 1) {
                unregister(vspecs.get(i));
            }
        }
        TreeWalker tw = new TreeWalker(pe);
        while (tw.next()) {
            pe = tw.getProgramElement();
            if (pe instanceof ScopeDefiningElement) {
                flushScopes((ScopeDefiningElement) pe);
            }
        }
    }

    void flushScopes(ScopeDefiningElement sde) {
        DefaultNameInfo dni = (DefaultNameInfo) getNameInfo();
        if (sde instanceof TypeScope) {
            List<? extends ClassType> ctl = ((TypeScope) sde).getTypesInScope();
            if (sde instanceof CompilationUnit) {
                // handle special case of top level CU scopes
                // caching known imported types
                // --- should be redone somewhen
                for (int j = ctl.size() - 1; j >= 0; j -= 1) {
                    ClassType ct = ctl.get(j);
                    if ((ct instanceof TypeDeclaration) && ((TypeDeclaration) ct).getASTParent() == sde) {
                        dni.unregisterClassType(ct.getFullName());
                    }
                }
            } else {
                for (int j = ctl.size() - 1; j >= 0; j -= 1) {
                    dni.unregisterClassType(ctl.get(j).getFullName());
                }
            }
        }
        if (sde instanceof TypeDeclaration) {
        	List<FieldSpecification> fl = ((TypeDeclaration) sde).getFieldsInScope();
            for (int j = fl.size() - 1; j >= 0; j -= 1) {
                dni.unregisterField(fl.get(j).getFullName());
            }
        }
        sde.setDefinedScope(false);
    }

    public void reset() {
        super.reset();

        reference2element.clear();
        sigCache.clear();

        SourceFileRepository sfr = serviceConfiguration.getSourceFileRepository();
        List<CompilationUnit> cul = sfr.getCompilationUnits();
        DefaultNameInfo dni = (DefaultNameInfo) getNameInfo();
        
        dni.paramTypesCache.clear();
        ((DefaultImplicitElementInfo)serviceConfiguration.getImplicitElementInfo()).reset();
        
        dni.reregisterPackages();
        for (CompilationUnit cu : cul) {
            // remove all scopes
            unregister(cu);
            // now rebuild scopes
            analyzeProgramElement(cu);
        }
    }
}
