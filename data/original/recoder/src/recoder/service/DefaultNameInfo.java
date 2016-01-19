// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import recoder.AbstractService;
import recoder.ParserException;
import recoder.ServiceConfiguration;
import recoder.abstraction.AnnotationProperty;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.EnumConstant;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.Member;
import recoder.abstraction.Method;
import recoder.abstraction.NullType;
import recoder.abstraction.Package;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeArgument.WildcardMode;
import recoder.abstraction.TypeParameter;
import recoder.abstraction.Variable;
import recoder.bytecode.ClassFile;
import recoder.bytecode.FieldInfo;
import recoder.bytecode.ReflectionImport;
import recoder.convenience.Format;
import recoder.io.ClassFileRepository;
import recoder.io.DefaultClassFileRepository;
import recoder.io.PropertyNames;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.JavaProgramFactory;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.parser.JavaCCParser;
import recoder.util.Debug;

public class DefaultNameInfo extends AbstractService implements NameInfo, PropertyChangeListener {

    /** Maps fully qualified class names to their according types. */
    private final Map<String, Type> name2type = new HashMap<String, Type>(128);

    /** Caches queries for parameterized types */
    final Map<String, ParameterizedType> paramTypesCache = new HashMap<String, ParameterizedType>(128);

    /** maps fully qualified variable names to their according variables */
    private final Map<String, Field> name2field = new HashMap<String, Field>(128);

    /** maps package names to package objects */
    private Map<String, Package> name2package = new HashMap<String, Package>(64);
    
    // the predefined types

    private ClassType nullType;

    private ClassType javaLangObject;

    private ClassType javaLangString;

    private ClassType javaLangClass;

    private ClassType javaLangCloneable;

    private ClassType javaIoSerializable;

    private ClassType javaLangRunnable;
    
    private ClassType javaLangBoolean;
    
    private ClassType javaLangByte;
    
    private ClassType javaLangCharacter;

    private ClassType javaLangShort;
    
    private ClassType javaLangInteger;

    private ClassType javaLangLong;

    private ClassType javaLangFloat;
    
    private ClassType javaLangDouble;

    private ClassType javaLangAnnotationAnnotation;
    
    private ClassType javaLangEnum;

    private ClassType javaLangIterable;

    private final PrimitiveType intType = new PrimitiveType("int");
    private final PrimitiveType booleanType = new PrimitiveType("boolean");
    private final PrimitiveType longType = new PrimitiveType("long");
    private final PrimitiveType doubleType = new PrimitiveType("double");
    private final PrimitiveType charType = new PrimitiveType("char");
    private final PrimitiveType floatType = new PrimitiveType("float");
    private final PrimitiveType byteType = new PrimitiveType("byte");
    private final PrimitiveType shortType = new PrimitiveType("short");
    
    /**
     * Creates a new initialized definition table.
     * 
     * @param config
     *            the configuration this services becomes part of.
     */
    public DefaultNameInfo(ServiceConfiguration config) {
        super(config);
        PrimitiveType[] pts = new PrimitiveType[] { intType, booleanType, longType, doubleType,
        							charType, floatType, byteType, shortType};
        for (PrimitiveType pt : pts) {
        	name2type.put(pt.getName(), pt);
        	pt.setProgramModelInfo(getImplicitElementInfo());
        }
    }

    public void initialize(ServiceConfiguration cfg) {
        super.initialize(cfg);
        nullType = new NullType(cfg.getImplicitElementInfo());
        createPackage("java.lang");
        cfg.getProjectSettings().addPropertyChangeListener(this);
        updateSearchMode();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String changedProp = evt.getPropertyName();
        if (changedProp.equals(PropertyNames.CLASS_SEARCH_MODE)) {
            updateSearchMode();
        }
    }

    // search mode codes
    private final static int NO_SEARCH = 0;

    private final static int SEARCH_SOURCE = 1;

    private final static int SEARCH_CLASS = 2;

    private final static int SEARCH_REFLECT = 3;

    // the current search mode
    private int[] searchMode;

    // parses the class search mode property and creates the internal
    // representation. Ignores everything that does not fit.
    private void updateSearchMode() {
        String prop = serviceConfiguration.getProjectSettings().getProperty(PropertyNames.CLASS_SEARCH_MODE);
        if (prop == null) {
            // just in case...
            prop = "";
        }
        searchMode = new int[prop.length()];
        for (int i = 0; i < searchMode.length; i++) {
            switch (prop.charAt(i)) {
            case 's':
            case 'S':
                searchMode[i] = SEARCH_SOURCE;
                break;
            case 'c':
            case 'C':
                searchMode[i] = SEARCH_CLASS;
                break;
            case 'r':
            case 'R':
                searchMode[i] = SEARCH_REFLECT;
                break;
            default:
                searchMode[i] = NO_SEARCH;
            }
        }
    }


    final void updateModel() {
        serviceConfiguration.getChangeHistory().updateModel();
    }

    ClassFileRepository getClassFileRepository() {
        return serviceConfiguration.getClassFileRepository();
    }

    SourceFileRepository getSourceFileRepository() {
        return serviceConfiguration.getSourceFileRepository();
    }

    ByteCodeInfo getByteCodeInfo() {
        return serviceConfiguration.getByteCodeInfo();
    }

    SourceInfo getSourceInfo() {
        return serviceConfiguration.getSourceInfo();
    }

    ImplicitElementInfo getImplicitElementInfo() {
        return serviceConfiguration.getImplicitElementInfo();
    }

    public void register(ClassType ct) {
        Debug.assertNonnull(ct);
        String name = ct.getFullName();
        Type ob = name2type.put(name, ct);
        if (ob != null && ob != ct && !(ob instanceof UnknownClassType)) { 
            Debug.log("Internal Warning - Multiple registration of " + Format.toString("%N [%i]", ct)
                    + Format.toString(" --- was: %N [%i]", ob));
        }
    }

    public void register(Field f) {
        Debug.assertNonnull(f);
        name2field.put(f.getFullName(), f);
    }

    public ClassType getJavaLangObject() {
        if (javaLangObject == null) {
            javaLangObject = getClassType("java.lang.Object");
        }
        return javaLangObject;
    }

    public ClassType getJavaLangString() {
        if (javaLangString == null) {
            javaLangString = getClassType("java.lang.String");
        }
        return javaLangString;
    }

    public ClassType getJavaLangBoolean() {
        if (javaLangBoolean == null) {
            javaLangBoolean = getClassType("java.lang.Boolean");
        }
        return javaLangBoolean;
    }

    public ClassType getJavaLangByte() {
        if (javaLangByte == null) {
            javaLangByte = getClassType("java.lang.Byte");
        }
        return javaLangByte;
    }

    public ClassType getJavaLangCharacter() {
        if (javaLangCharacter == null) {
            javaLangCharacter = getClassType("java.lang.Character");
        }
        return javaLangCharacter;
    }
    
    public ClassType getJavaLangShort() {
        if (javaLangShort == null) {
            javaLangShort = getClassType("java.lang.Short");
        }
        return javaLangShort;
    }  
    
    public ClassType getJavaLangInteger() {
        if (javaLangInteger == null) {
            javaLangInteger = getClassType("java.lang.Integer");
        }
        return javaLangInteger;
    }    
    
    public ClassType getJavaLangLong() {
        if (javaLangLong == null) {
            javaLangLong = getClassType("java.lang.Long");
        }
        return javaLangLong;
    }    

    public ClassType getJavaLangFloat() {
        if (javaLangFloat == null) {
            javaLangFloat = getClassType("java.lang.Float");
        }
        return javaLangFloat;
    }

    public ClassType getJavaLangDouble() {
        if (javaLangDouble == null) {
            javaLangDouble = getClassType("java.lang.Double");
        }
        return javaLangDouble;
    }
    
    public ClassType getJavaLangClass() {
        if (javaLangClass == null) {
            javaLangClass = getClassType("java.lang.Class");
        }
        return javaLangClass;
    }

    public ClassType getJavaLangCloneable() {
        if (javaLangCloneable == null) {
            javaLangCloneable = getClassType("java.lang.Cloneable");
        }
        return javaLangCloneable;
    }

    public ClassType getJavaLangRunnable() {
        if (javaLangRunnable == null) {
            javaLangRunnable = getClassType("java.lang.Runnable");
        }
        return javaLangRunnable;
    }

    public ClassType getJavaIoSerializable() {
        if (javaIoSerializable == null) {
            javaIoSerializable = getClassType("java.io.Serializable");
        }
        return javaIoSerializable;
    }
    
    public ClassType getJavaLangAnnotationAnnotation() {
        if (javaLangAnnotationAnnotation == null) {
            javaLangAnnotationAnnotation = getClassType("java.lang.annotation.Annotation");
        }
        return javaLangAnnotationAnnotation;
    }
    
    public ClassType getJavaLangEnum() {
    	if (javaLangEnum == null) {
    		javaLangEnum = getClassType("java.lang.Enum");
    	}
    	return javaLangEnum;
    }
    
    public ClassType getJavaLangIterable() {
    	if (javaLangIterable == null) {
    		javaLangIterable = getClassType("java.lang.Iterable");
    	}
    	return javaLangIterable;
    }

    public ClassType getNullType() {
        return nullType;
    }

    public PrimitiveType getShortType() {
        return shortType;
    }

    public PrimitiveType getByteType() {
        return byteType;
    }

    public PrimitiveType getBooleanType() {
        return booleanType;
    }

    public PrimitiveType getIntType() {
        return intType;
    }

    public PrimitiveType getLongType() {
        return longType;
    }

    public PrimitiveType getFloatType() {
        return floatType;
    }

    public PrimitiveType getDoubleType() {
        return doubleType;
    }

    public PrimitiveType getCharType() {
        return charType;
    }

    public boolean isPackage(String name) {
        updateModel();
        return name2package.get(name) != null;
    }

    public Package createPackage(String name) {
        Package result = name2package.get(name);
        if (result == null) {
            result = new Package(name, serviceConfiguration.getImplicitElementInfo());
            name2package.put(result.getName(), result);
            int ldp = name.lastIndexOf('.');
            if (ldp > 0) {
                createPackage(name.substring(0, ldp));
            }
        }
        return result;
    }

    public Package getPackage(String name) {
        Debug.assertNonnull(name);
        updateModel();
        return name2package.get(name);
    }

    public List<Package> getPackages() {
        updateModel();
        int size = name2package.size();
        List<Package> result = new ArrayList<Package>(size);
        for (Package p : name2package.values()) {
            result.add(p);
        }
        return result;
    }

    public ClassType getClassType(String name) {
        Type result = getType(name);
        if (result instanceof ClassType) {
            return (ClassType) result;
        }
        return null;
    }

    @Deprecated
    public ArrayType createArrayType(Type basetype) {
    	return basetype.createArrayType();
    }
    
    @Deprecated
    public ArrayType createArrayType(Type basetype, int dimensions) {
    	if (dimensions < 1)
    		throw new IllegalArgumentException("dimensions must be >= 1");
    	Type result = basetype; 
    	while (dimensions-- > 0) {
    		result = result.createArrayType();
    	}
    	return (ArrayType)result;
    }

    @Deprecated
    public ArrayType getArrayType(Type basetype) {
        Debug.assertNonnull(basetype);
        return basetype.getArrayType();
    }

    public Type getType(String name) {
    	String sname = name.trim();
        Debug.assertNonnull(name);
        updateModel();
        
        int dim = 0;
        while (sname.endsWith("[]")) {
        	dim++;
        	sname = sname.substring(0, sname.length()-2);
        }
        Type result = null;
        
        String name_with_args = sname;
        String typeArgs = null;
        int idx = sname.indexOf('<');
        if (idx > -1) {
        	result = paramTypesCache.get(sname);
        	if (result == null) { // otherwise, "fall through" to array creation below
        		typeArgs = sname.substring(idx, sname.length()); 
        		sname = sname.substring(0, idx);
        	}
        }

        if (result == null)
        	result = name2type.get(sname);
        
        if (result == null) {
        	result = name2type.get("java.lang."+sname);
        	if (result == unknownType)
        		result = null;
        }

        if (result == unknownType) {
            return null; // report null´
        } else if (result == null) {
            // try to load the required information
            if (loadClass(sname)) {
                result = name2type.get(sname);
            } 
            if ((result == null || result == unknownType) && loadClass("java.lang."+sname)) {
            	result = name2type.get("java.lang."+sname);
                if (result == unknownType) {
                    return null;
                }
            }

            // cache positive or negative results
            name2type.put(sname, (result != null) ? result : unknownType);
        }
        if (typeArgs != null && result != null) {
        	// new in 0.90 - allow to query for ParameterizedType
        	// TODO more efficient !!!???
        	StringReader sr = new StringReader(typeArgs);
        	JavaCCParser parser = new JavaCCParser(sr);
        	parser.initialize(sr, (JavaProgramFactory)getServiceConfiguration().getProgramFactory());
        	List<TypeArgumentDeclaration> tads = null;
    		try {
				tads = parser.TypeArguments();
				if (!parser.getNextToken().image.equals(""))
					throw new IllegalArgumentException(name); // tailing characters->error
			} catch (ParserException e) { // includes IOException, which won't occur
				throw new IllegalArgumentException(name);
			}
    		List<TypeArgument> tis = new ArrayList<TypeArgument>(tads.size());
    		for (TypeArgumentDeclaration tad : tads) {
    			WildcardMode wm = tad.getWildcardMode();
    			if (wm != WildcardMode.Any) {
    				tad.setWildcardMode(WildcardMode.None);
    				String sig = TypeArgument.DescriptionImpl.getFullDescription(tad, true);
    				ClassType argType = getClassType(sig);
    				if (argType == null)
    					return null; // can't resolve one of the type args -> return null for complete type
    				DefaultProgramModelInfo pmi = (DefaultProgramModelInfo)getServiceConfiguration().getSourceInfo();
    				if (argType instanceof ParameterizedType) {
    					ParameterizedType pt = (ParameterizedType)argType;
    					tis.add(pmi.new ResolvedTypeArgument(wm, pt.getGenericType(), pt.getTypeArgs(), null));  // TODO check last param
    				} else {
    					tis.add(pmi.new ResolvedTypeArgument(wm, argType, null, null));  // TODO check last param
    				}
    			} else {
    				DefaultProgramModelInfo pmi = (DefaultProgramModelInfo)getServiceConfiguration().getSourceInfo();
    				tis.add(pmi.new ResolvedTypeArgument(wm, null, null, null));   // TODO check last param
    			}
    		}
        	result = getServiceConfiguration().getImplicitElementInfo().getParameterizedType((ClassType)result, tis);
            paramTypesCache.put(name_with_args, (ParameterizedType)result);
        }
        // this goes last - no array type should end up in the caches!
        if (result != null) {
           	for (int i = 0; i < dim; i++)
           		result = result.createArrayType();
        }
        return result;
    }

    public List<Type> getTypes() {
        updateModel();
        int size = name2type.size();
        ArrayList<Type> result = new ArrayList<Type>(size);
        // size: most types are expected to be known
        for (Type t : name2type.values()) {
            if (t != unknownType) {
                result.add(t);
            }
        }
        result.trimToSize();
        return result;
    }

    /*
     * Here is room for improvement: Cache that stuff.
     * That'd require incremental update, though!
     */
    public List<ClassType> getTypes(Package pkg) {
        Debug.assertNonnull(pkg);
        updateModel();
        ArrayList<ClassType> result = new ArrayList<ClassType>();
        List<Type> tl = getTypes();
        int s = tl.size();
        for (int i = 0; i < s; i++) {
            Type t = tl.get(i);
            if (t instanceof ClassType) {
                ClassType ct = (ClassType) t;
                if (ct.getContainer() == pkg) {
                    result.add(ct);
                }
            }
        }
        result.trimToSize();
        return result;
    }

    public List<ClassType> getClassTypes() {
        updateModel();
        ArrayList<ClassType> result = new ArrayList<ClassType>(name2type.size() - 8);
        List<Type> tl = getTypes();
        int s = tl.size();
        for (int i = 0; i < s; i++) {
            Type t = tl.get(i);
            if (t instanceof ClassType) {
                result.add((ClassType) t);
            }
        }
        result.trimToSize();
        return result;
    }

    public Field getField(String name) {
        Debug.assertNonnull(name);
        updateModel();
        Field result = name2field.get(name);
        if (result != null) {
            return result;
        }
        // we can try to get the type first
        int ldp = name.lastIndexOf('.');
        if (ldp == -1) {
            return null;
        }
        ClassType ct = getClassType(name.substring(0, ldp));
        if (ct == null) {
            return null;
        }
        List<? extends Field> fields = ct.getFields();
        if (fields == null) {
            return null;
        }
        String shortname = name.substring(ldp+1);
        for (int i = 0; i < fields.size(); i++) {
            String fname = fields.get(i).getName();
            if (/*name == fname || */shortname.equals(fname)) {
                result = fields.get(i);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    public List<Field> getFields() {
        updateModel();
        int size = name2field.size();
        ArrayList<Field> result = new ArrayList<Field>(size);
        for (Field f : name2field.values()) {
            result.add(f);
        }
        result.trimToSize();
        return result;
    }

    private boolean loadClass(String classname) {
        boolean result = false;
        for (int i = 0; !result && i < searchMode.length; i += 1) {
            switch (searchMode[i]) {
            case SEARCH_SOURCE:
                result = loadClassFromSourceCode(classname);
                break;
            case SEARCH_CLASS:
                result = loadClassFromPrecompiledCode(classname);
                break;
            case SEARCH_REFLECT:
                result = loadClassByReflection(classname);
                break;
            default:
                break;
            }
        }
        return result;
    }

    private boolean loadClassFromPrecompiledCode(String classname) {
        boolean result = false;
        ClassFileRepository cfr = getClassFileRepository();
        ClassFile cf = cfr.getClassFile(classname);
        if (cf != null) {
            getByteCodeInfo().register(cf);
            result = true;
        }
        return result;
    }

    private boolean loadClassFromSourceCode(String classname) {
        boolean result = false;
        CompilationUnit cu = null;
        try {
            cu = getSourceFileRepository().getCompilationUnit(classname);
            if (cu == null) {
                // try to load member classes by loading outer classes
                int ldp = classname.lastIndexOf('.');
                if (ldp >= 0) {
                    String shortedname = classname.substring(0, ldp);
                    // not a top-level type, parent type was loaded
                    // and member type has been registered:
                    return !name2package.containsKey(shortedname) && loadClassFromSourceCode(shortedname)
                            && name2type.containsKey(classname);
                }
            }
            if (cu != null) {
                getSourceInfo().register(cu);
                result = true;
            }
        } catch (Exception e) {
            Debug.error("Error trying to retrieve source file for type " + classname + "\n" + "Exception was " + e);
            e.printStackTrace();
        }
        // added check the for name2type.containsKey below as of 0.93. 
        // In the rare case that there is a class B in a package a.b, 
        // and the input folder is set to src/a (thus not the "root"),
        // and a check for a class B in a package b is performed, then
        // "true" is incorrectly returned. Happens e.g. when analyzing
        // com.sun from the JDK sources and specifying com/sun as the
        // root input folder.
        return result && name2type.containsKey(classname);
    }

    private boolean loadClassByReflection(String classname) {
        ClassFile cf = ReflectionImport.getClassFile(classname);
        if (cf != null) {
            getByteCodeInfo().register(cf);
            return true;
        }
        return false;
    }

    public String information() {
        int unknown = 0;
        for (Type t : name2type.values()) {
            if (t == unknownType) {
                unknown += 1;
            }
        }
        return "" + name2package.size() + " packages with " + (name2type.size() - unknown) + " types (" + unknown
                + " were pure speculations) and " + name2field.size() + " fields";
    }
    
    public void unregisterClassType(String fullname) {
        Debug.assertNonnull(fullname);
        name2type.remove(fullname);
    }

    public void unregisterField(String fullname) {
        Debug.assertNonnull(fullname);
        name2field.remove(fullname);
    }

    void reregisterPackages() {
        Map<String, Package> n2p = new HashMap<String, Package>(64);
        List<ClassFile> cf = getClassFileRepository().getKnownClassFiles();
        for (int i = cf.size() - 1; i >= 0; i -= 1) {
            ClassTypeContainer ctc = cf.get(i).getContainer();
            if (ctc instanceof Package) {
                n2p.put(ctc.getFullName(), (Package)ctc);
            }
        }
        name2package = n2p;
    }

    private class UnknownProgramModelElementInfo extends DefaultProgramModelInfo {
    	UnknownProgramModelElementInfo() {
    		super(DefaultNameInfo.this.getServiceConfiguration());
    	}
		public ClassTypeContainer getClassTypeContainer(ClassType ct) {
			return unknownClassType;
		}

		public List<? extends Constructor> getConstructors(ClassType ct) {
			ArrayList<Constructor> res = new ArrayList<Constructor>(1);
			res.add(unknownConstructor);
			return res;
		}

		public ClassType getContainingClassType(Member m) {
			return unknownClassType;
		}

		/**
		 * returns Collections.emptyList()
		 */
		public List<ClassType> getExceptions(Method m) {
			// we don't know...
			return Collections.emptyList();
		}

		/**
		 * returns Collections.emptyList()
		 */

		public List<? extends Field> getFields(ClassType ct) {
			// we don't know...
			return Collections.emptyList();
		}

		/**
		 * returns Collections.emptyList()
		 */

		public List<Method> getMethods(ClassType ct) {
			// we don't know... do we ?
			return Collections.emptyList();
		}

		public Package getPackage(ProgramModelElement pme) {
			return unknownPackage;
		}

		public Type getReturnType(Method m) {
			return unknownType;
		}

		public List<Type> getSignature(Method m) {
			// we don't know...
			return null;
		}

		public List<ClassType> getSupertypes(ClassType ct) {
			// we don't know...
			// TODO return empty list?
			return null;
		}

		public Type getType(ProgramModelElement pme) {
			// we don't know...
			return null;
		}

		public List<? extends ClassType> getTypes(ClassTypeContainer ctc) {
			// we don't know...
			// TODO return empty list ?
			return null;
		}
    }

	private UnknownProgramModelElementInfo unknownProgramModelInfo = new UnknownProgramModelElementInfo();

    class UnknownProgramModelElement implements ProgramModelElement {
        public String getName() {
            return "<unkownElement>";
        }

        public String getFullName() {
            return getName();
        }
        
        public String getBinaryName() {
        	return getFullName();
        }

        public ProgramModelInfo getProgramModelInfo() {
            return unknownProgramModelInfo;
        }

        public void setProgramModelInfo(ProgramModelInfo pmi) {
        	// ignore/won't happen
        }

        public void validate() {
        	// always valid
        }
        public UnknownProgramModelElement deepClone() {
        	throw new UnsupportedOperationException("Cannot deep-clone implicit information");
        }

    }

    abstract class UnknownMember extends UnknownProgramModelElement implements Member {
        public ClassType getContainingClassType() {
            return unknownClassType;
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
    	
        @Override
    	public UnknownMember getGenericMember() {
    		return this;
    	}

    }

    class UnknownClassType extends UnknownMember implements ClassType {
        public String getName() {
            return "<unknownClassType>";
        }

        public ClassTypeContainer getContainer() {
            return null;
        }

        public List<ClassType> getTypes() {
            return Collections.emptyList();
        }

        public Package getPackage() {
            return unknownPackage;
        }

        public boolean isInterface() {
            return false;
        }
        
        public boolean isOrdinaryInterface() {
            return false;
        }
        
        public boolean isAnnotationType() {
            return true;
        }

        public boolean isEnumType() {
            return false;
        }

        public boolean isOrdinaryClass() {
            return true;
        }

        public boolean isAbstract() {
            return false;
        }

        public List<ClassType> getSupertypes() {
            return Collections.singletonList(getJavaLangObject());
        }

        public List<ClassType> getAllSupertypes() {
            ArrayList<ClassType> result = new ArrayList<ClassType>();
            result.add(this);
            result.add(getJavaLangObject());
            result.trimToSize();
            return result;
        }

        public List<? extends Field> getFields() {
            return getJavaLangObject().getFields();
        }

        public List<Field> getAllFields() {
            return getJavaLangObject().getAllFields();
        }

        public List<Method> getMethods() {
            return getJavaLangObject().getMethods();
        }

        public List<Method> getAllMethods() {
            return getJavaLangObject().getAllMethods();
        }

        public List<Constructor> getConstructors() {
            return Collections.emptyList();
        }

        public List<ClassType> getAllTypes() {
            return Collections.emptyList();
        }

        public List<AnnotationUseSpecification> getAnnotations() {
            return null;
        }

		public List<? extends EnumConstant> getEnumConstants() {
			return null;
		}

		public List<TypeParameterDeclaration> getTypeParameters() {
			return null;
		}
		
		public String getFullSignature() {
			return getFullName();
		}

		private ArrayType arrayType;
		

		public ArrayType createArrayType() {
			if (arrayType == null)
				arrayType = new ArrayType(this, getServiceConfiguration().getImplicitElementInfo());
			return arrayType;
		}

		public ArrayType getArrayType() {
			return arrayType;
		}

		private final ErasedType erasedType = 
			new ErasedType(this, getServiceConfiguration().getImplicitElementInfo());
		
		public ErasedType getErasedType() {
			return erasedType;
		}

		public boolean isInner() {
			return false;
		}
		public ClassType getBaseClassType() {
			return this;
		}
    }

    class UnknownMethod extends UnknownMember implements Method {
        public String getName() {
            return "<unknownMethod>";
        }

        public Package getPackage() {
            return unknownPackage;
        }

        public ClassTypeContainer getContainer() {
            return unknownClassType;
        }

        public List<ClassType> getTypes() {
            return Collections.emptyList();
        }

        public boolean isAbstract() {
            return false;
        }

        public boolean isNative() {
            return false;
        }

        public boolean isStrictFp() {
            return false;
        }

        public boolean isSynchronized() {
            return false;
        }

        public List<ClassType> getExceptions() {
            return Collections.emptyList();
        }

        public Type getReturnType() {
            return unknownType;
        }

        public List<Type> getSignature() {
            return Collections.emptyList();
        }

        public boolean isVarArgMethod() {
            return false;
        }

        public List<AnnotationUseSpecification> getAnnotations() {
            return null;
        }

		public List<? extends TypeParameter> getTypeParameters() {
			return null;
		}

		@Override
		public String getFullSignature() {
			return getName() + "(<UnknownParameters>)";
		}
    }

    class UnknownConstructor extends UnknownMethod implements Constructor {
        public String getName() {
            return "<unknownConstructor>";
        }
    }

    class UnknownVariable extends UnknownProgramModelElement implements Variable {
        public String getName() {
            return "<unknownVariable>";
        }

        public boolean isFinal() {
            return false;
        }

        public Type getType() {
            return unknownType;
        }
    }

    class UnknownField extends UnknownMember implements Field {
        public String getName() {
            return "<unknownField>";
        }

        public Type getType() {
            return unknownType;
        }

        public List<AnnotationUseSpecification> getAnnotations() {
            return null;
        }

		public List<? extends TypeArgument> getTypeArguments() {
			return null;
		}
    }
    
    class UnknownAnnotationProperty extends UnknownMethod implements AnnotationProperty {
		public Object getDefaultValue() {
			return null;
		}
	}

    /**
     * The unknown elements. They are used for error handling and to mark
     * entities as "known-as-unknown" internally.
     */
    private final ProgramModelElement unknownElement = new UnknownProgramModelElement();

    private final ClassType unknownClassType = new UnknownClassType();

    private final Type unknownType = unknownClassType;

    private final Package unknownPackage = new Package("<unknownPackage>", null);

    private final Method unknownMethod = new UnknownMethod();

    private final Constructor unknownConstructor = new UnknownConstructor();

    private final Variable unknownVariable = new UnknownVariable();

    private final Field unknownField = new UnknownField();
    
    private final AnnotationProperty unknownAnnotationProperty = new UnknownAnnotationProperty();

    public ClassType getUnknownClassType() {
        return unknownClassType;
    }

    public ProgramModelElement getUnknownElement() {
        return unknownElement;
    }

    public Package getUnknownPackage() {
        return unknownPackage;
    }

    public Method getUnknownMethod() {
        return unknownMethod;
    }

    public Constructor getUnknownConstructor() {
        return unknownConstructor;
    }

    public Variable getUnknownVariable() {
        return unknownVariable;
    }

    public Field getUnknownField() {
        return unknownField;
    }

    public Type getUnknownType() {
        return unknownType;
    }
    
    public AnnotationProperty getUnknownAnnotationProperty() {
    	return unknownAnnotationProperty;
    }

    /**
     * this method is used if a type rename can be identified by the source info, mainly when
     * an AttachChange of an Identifier follows directly to a DetachChange of an Identifier
     * on the same parent. This leaves ArrayTypes untouched.
     * @param ct
     * @param unregisterFrom
     * @param registerTo
     */
    void handleTypeRename(ClassType ct, String unregisterFrom, String registerTo) {
        boolean register = false;
        boolean unregister = false;
        Object old = name2type.get(registerTo);
        // this might be part of a valid package move, so do not corrupt caches
        if (old == null || old == unknownType)
            register = true;
        old = name2type.get(unregisterFrom);
        if (old == ct)
            unregister = true;
        if (unregister)
            unregisterClassType(unregisterFrom);
        Type removed;
        // TODO check - arrays shouldn't be in cache any more... This should be removed, right?
        String newArrayName = registerTo + "[]";
        String arrayRemove = unregisterFrom + "[]";
        while ((removed = name2type.remove(arrayRemove)) != null) {
            name2type.put(newArrayName, removed);
            arrayRemove += "[]";
            newArrayName += "[]";
        }
        if (register)
            register(ct);

        // original type name is now known to be unknown
        name2type.put(unregisterFrom, unknownClassType); // this prevents reloading of class file from disk

        // fields of this type
        List<? extends Field> fl = ct.getFields();
        for (int f = 0, fm = fl.size(); f < fm; f++) {
            Field currentField = fl.get(f);
            String fieldremove = unregisterFrom + "." + currentField.getName();
            if (unregister)
                unregisterField(fieldremove);
            if (register)
                register(currentField);
        }
    }

	public List<ClassType> getClassTypes(String pattern) {
		pattern = adjustPattern(pattern);
		Pattern patt = Pattern.compile(pattern);
		ArrayList<ClassType> result = new ArrayList<ClassType>();
		for (ClassType t : getClassTypes()) {
			if (patt.matcher(t.getFullName()).matches())
				result.add(t);
		}
		result.trimToSize();
		return result;
	}

	private String adjustPattern(String pattern) {
		pattern = pattern.replace("**", "!");
		pattern = pattern.replace("*", "(\\w)*");
		pattern = pattern.replace("!", "(\\w|\\.)*");
		pattern = pattern.replace("?", ".");
		pattern = pattern.replace("$", "\\$");
		return pattern;
	}

	public List<Method> getMethods(String pattern) {
		return getMethods(pattern, false);
	}
	
	public List<Method> getMethods(String pattern, boolean includeConstructors) {
		String type = pattern.substring(0, pattern.indexOf('('));
		String type2 = null;
		if (type.lastIndexOf('.') > 0)
			type2 = type.substring(0, type.lastIndexOf('.'));
		String sig = pattern.substring(pattern.indexOf('(')+1, pattern.length()-1);
		
		sig = sig.replace("**", "!");
		sig = sig.replace("*", "(\\w|\\.|\\[|\\])+");
		sig = sig.replace("!", "(\\w|,|\\.|\\[|\\])*");
		sig = sig.replace("?", ".");
		sig = sig.replace("$", "\\$");
		sig = sig.replace("[", "\\[");
		sig = sig.replace("]", "\\]");
		
		Pattern sigPattern = Pattern.compile(sig);
		
		List<ClassType> types = getClassTypes(type);
		if (type2 != null) {
			List<ClassType> types2 = getClassTypes(type2);
			types.removeAll(types2); // TODO quick (imperformant) fix for avoiding duplicates...
			types.addAll(types2);
		}
		
		ArrayList<Method> result = new ArrayList<Method>();
		type = adjustPattern(type);
		Pattern methPattern = Pattern.compile(type);
		for (ClassType t : types) {
			List<Method> methsAndConstrs = t.getMethods();
			if (includeConstructors)
				methsAndConstrs.addAll(t.getConstructors());
			for (Method m : methsAndConstrs) {
				// need to check again if the full method name matches (the type-thing above is just an approximation)
				if (!methPattern.matcher(m.getFullName()).matches())
					continue;
				if (sigPattern.matcher(makeSigString(m.getSignature(), true)).matches())
					result.add(m);
				else if (sigPattern.matcher(makeSigString(m.getSignature(), false)).matches())
					result.add(m);
			}
		}
		result.trimToSize();
		return result;
	}

	private String makeSigString(List<Type> signature, boolean fullName) {
		StringBuilder result = new StringBuilder();

		boolean first = true;
		for (Type t : signature) {
			if (!first)
				result.append(',');
			else
				first = false;
			if (fullName)
				result.append(t.getFullName());
			else
				result.append(t.getName());
		}
		return result.toString();
	}

	public void resetBytecode() {
		paramTypesCache.clear();
		for (Iterator<Type> it = name2type.values().iterator(); it.hasNext();) {
			Type t = it.next();
			if (t instanceof ClassFile)
				it.remove();
		}
		for (Iterator<Field> it = name2field.values().iterator(); it.hasNext();) {
			Field f = it.next();
			if (f instanceof FieldInfo)
				it.remove();
		}
		if (javaIoSerializable instanceof ClassFile)
			javaIoSerializable = null;
		if (javaLangAnnotationAnnotation instanceof ClassFile)
			javaLangAnnotationAnnotation = null;
		if (javaLangBoolean instanceof ClassFile)
			javaLangBoolean = null;
		if (javaLangByte instanceof ClassFile)
			javaLangByte = null;
		if (javaLangCharacter instanceof ClassFile)
			javaLangCharacter = null;
		if (javaLangClass instanceof ClassFile)
			javaLangClass = null;
		if (javaLangCloneable instanceof ClassFile)
			javaLangCloneable = null;
		if (javaLangDouble instanceof ClassFile)
			javaLangDouble = null;
		if (javaLangEnum instanceof ClassFile)
			javaLangEnum = null;
		if (javaLangFloat instanceof ClassFile)
			javaLangFloat = null;
		if (javaLangInteger instanceof ClassFile)
			javaLangInteger = null;
		if (javaLangLong instanceof ClassFile)
			javaLangLong = null;
		if (javaLangObject instanceof ClassFile)
			javaLangObject = null;
		if (javaLangRunnable instanceof ClassFile)
			javaLangRunnable = null;
		if (javaLangShort instanceof ClassFile)
			javaLangShort = null;
		if (javaLangString instanceof ClassFile)
			javaLangString = null;
		((DefaultByteCodeInfo)getByteCodeInfo()).clear();
		((DefaultClassFileRepository)getClassFileRepository()).reset();
	}
}
