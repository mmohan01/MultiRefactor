// This file is part of the RECODER library and protected by the LGPL.

package recoder.java.declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.convenience.Naming;
import recoder.java.Identifier;
import recoder.java.NamedProgramElement;
import recoder.java.NonTerminalProgramElement;
import recoder.java.SourceElement;
import recoder.java.TypeScope;
import recoder.java.VariableScope;
import recoder.list.generic.ASTList;
import recoder.service.ProgramModelInfo;
import recoder.service.SourceInfo;
import recoder.util.Debug;

/**
 * Type declaration.
 * 
 * @author <TT>AutoDoc</TT>
 */
public abstract class TypeDeclaration extends JavaDeclaration implements NamedProgramElement, MemberDeclaration,
        TypeDeclarationContainer, ClassType, VariableScope, TypeScope {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Name.
     */
	Identifier name;

    /**
     * Parent.
     */
	TypeDeclarationContainer parent;

    /**
     * Members.
     */
    ASTList<MemberDeclaration> members;

    /**
     * Service.
     */
    private  SourceInfo service;

    private ArrayType arrayType;
    
    /**
     * Undefined scope tag.
     */
    @SuppressWarnings("all") private final static Map UNDEFINED_SCOPE = Collections.unmodifiableMap(new HashMap(0));

    /**
     * Scope table for types.
     */
    @SuppressWarnings("unchecked") protected Map<String, TypeDeclaration> name2type = UNDEFINED_SCOPE;

    /**
     * Scope table for fields.
     */
    @SuppressWarnings("unchecked") protected Map<String, FieldSpecification> name2field = UNDEFINED_SCOPE;

    /**
     * Type declaration.
     */
    public TypeDeclaration() {
        // nothing to do here
    }

    /**
     * Type declaration.
     * 
     * @param name
     *            an identifier.
     */
    public TypeDeclaration(Identifier name) {
        setIdentifier(name);
        // makeParentRoleValid() called by subclasses' constructors
    }

    /**
     * Type declaration.
     * 
     * @param mods
     *            a modifier mutable list.
     * @param name
     *            an identifier.
     */
    public TypeDeclaration(ASTList<DeclarationSpecifier> mods, Identifier name) {
        super(mods);
        setIdentifier(name);
        // makeParentRoleValid() called by subclasses' constructors
    }

    /**
     * Type declaration.
     * 
     * @param proto
     *            a type declaration.
     */
    protected TypeDeclaration(TypeDeclaration proto) {
        super(proto);
        if (proto.name != null) {
            name = proto.name.deepClone();
        }
        if (proto.members != null) {
            members = proto.members.deepClone();
        }
        // makeParentRoleValid() called by subclasses' constructors
    }

    /**
     * Make parent role valid.
     */
    public void makeParentRoleValid() {
        if (declarationSpecifiers != null) {
            for (int i = declarationSpecifiers.size() - 1; i >= 0; i -= 1) {
                declarationSpecifiers.get(i).setParent(this);
            }
        }
        if (name != null) {
            name.setParent(this);
        }
        if (members != null) {
            for (int i = members.size() - 1; i >= 0; i -= 1) {
                members.get(i).setMemberParent(this);
            }
        }
    }

    public SourceElement getFirstElement() {
        if (declarationSpecifiers != null && !declarationSpecifiers.isEmpty()) {
            return declarationSpecifiers.get(0);
        } else {
            return this;
        }
    }

    public SourceElement getLastElement() {
        return this;
    }

    /**
     * Get name.
     * 
     * @return the string.
     */
    public final String getName() {
        return (name == null) ? null : name.getText();
    }

    /**
     * Get identifier.
     * 
     * @return the identifier.
     */
    public Identifier getIdentifier() {
        return name;
    }

    /**
     * Set identifier.
     * 
     * @param id
     *            an identifier.
     */
    public void setIdentifier(Identifier id) {
        name = id;
    }

    /**
     * Get parent.
     * 
     * @return the type declaration container.
     */
    public TypeDeclarationContainer getParent() {
        return parent;
    }

    /**
     * Get member parent.
     * 
     * @return the type declaration.
     */
    public TypeDeclaration getMemberParent() {
        if (parent instanceof TypeDeclaration) {
            return (TypeDeclaration) parent;
        } else {
            return null;
        }
    }

    /**
     * Set parent.
     * 
     * @param p
     *            a type declaration container.
     */
    public void setParent(TypeDeclarationContainer p) {
        parent = p;
    }

    /**
     * Set member parent.
     * 
     * @param p
     *            a type declaration.
     */
    public void setMemberParent(TypeDeclaration p) {
        parent = p;
    }

    /**
     * Get AST parent.
     * 
     * @return the non terminal program element.
     */
    public NonTerminalProgramElement getASTParent() {
        return parent;
    }

    /**
     * Get members.
     * 
     * @return the member declaration mutable list.
     */
    public ASTList<MemberDeclaration> getMembers() {
        return members;
    }

    /**
     * Set members.
     * 
     * @param list
     *            a member declaration mutable list.
     */
    public void setMembers(ASTList<MemberDeclaration> list) {
        members = list;
    }

    /**
     * Get the number of type declarations in this container.
     * 
     * @return the number of type declarations.
     */
    public int getTypeDeclarationCount() {
        int count = 0;
        if (members != null) {
            for (int i = members.size() - 1; i >= 0; i -= 1) {
                if (members.get(i) instanceof TypeDeclaration) {
                    count += 1;
                }
            }
        }
        if (getTypeParameters() != null)
        	count += getTypeParameters().size();
        return count;
    }

    /*
     * Return the type declaration at the specified index in this node's
     * "virtual" type declaration array. @param index an index for a type
     * declaration. @return the type declaration with the given index.
     * @exception ArrayIndexOutOfBoundsException if <tt> index </tt> is out of
     * bounds.
     */
    public TypeDeclaration getTypeDeclarationAt(int index) {
        if (members != null) {
            int s = members.size();
            for (int i = 0; i < s && index >= 0; i++) {
                MemberDeclaration md = members.get(i);
                if (md instanceof TypeDeclaration) {
                    if (index == 0) {
                        return (TypeDeclaration) md;
                    }
                    index--;
                }
            }
        }
        if (getTypeParameters() != null) {
        	return getTypeParameters().get(index); // may throw exception
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Test whether the declaration is final.
     */
    public boolean isFinal() {
        return super.isFinal();
    }

    /**
     * Test whether the declaration is private.
     */
    public boolean isPrivate() {
        return super.isPrivate();
    }

    /**
     * Test whether the declaration is protected.
     */
    public boolean isProtected() {
        return super.isProtected();
    }

    /**
     * Test whether the declaration is public.
     */
    public boolean isPublic() {
        return (getASTParent() instanceof InterfaceDeclaration) || super.isPublic();
    }

    /**
     * Test whether the declaration is static.
     */
    public boolean isStatic() {
        return (getASTParent() instanceof InterfaceDeclaration) || super.isStatic();
    }

    /**
     * Test whether the declaration is strictfp.
     */
    public boolean isStrictFp() {
        return super.isStrictFp();
    }

    /**
     * Test whether the declaration is abstract.
     */
    public boolean isAbstract() {
        return super.isAbstract();
    }

    public ProgramModelInfo getProgramModelInfo() {
        return service;
    }

    public void setProgramModelInfo(ProgramModelInfo service) {
    	if (!(service instanceof SourceInfo))
    		throw new IllegalArgumentException("service for TypeDeclaration must be of type SourceInfo.");
        this.service = (SourceInfo)service;
    }

    private void updateModel() {
        getFactory().getServiceConfiguration().getChangeHistory().updateModel();
    }

    public String getFullName() {
        return Naming.getFullName(this);
    }
    
    
    
	public String getBinaryName() {
		// TODO needs some other refactorings first... (TypeDeclarationContainer!?)
		if (true)
			throw new RuntimeException();
    	ClassTypeContainer ctc = getContainer();
    	if (getName() == null) {
    		// anonymous
    	}
    	
    	if (ctc instanceof Package)
			return getFullName(); // Top-level class
		if (ctc instanceof ClassType)
			return ((ClassType)ctc).getBinaryName() + "$" + getName();
		if (ctc instanceof Method)
			return getFullName(); // TODO local class
		if (ctc == null) {
			// TODO 0.92 ???
		}
		return null;
	}


    public ClassTypeContainer getContainer() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getClassTypeContainer(this);
    }

    public TypeDeclaration getContainingClassType() {
    	NonTerminalProgramElement cur = parent;
    	while (cur != null) {
    		if (cur instanceof TypeDeclaration)
    			return (TypeDeclaration)cur;
    		cur = cur.getASTParent();
    	}
    	return null;
    }

    public Package getPackage() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getPackage(this);
    }

    public abstract boolean isInterface();

    public List<ClassType> getSupertypes() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getSupertypes(this);
    }

    public List<ClassType> getAllSupertypes() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getAllSupertypes(this);
    }

    public List<FieldSpecification> getFields() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        @SuppressWarnings("unchecked")
    	List<FieldSpecification> fields = (List<FieldSpecification>)service.getFields(this);
		return fields;
    }

    public List<Field> getAllFields() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getAllFields(this);
    }

    public List<Method> getMethods() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getMethods(this);
    }

    public List<Method> getAllMethods() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getAllMethods(this);
    }

    public List<? extends Constructor> getConstructors() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getConstructors(this);
    }

   public List<TypeDeclaration> getTypes() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        @SuppressWarnings("unchecked")
    	List<TypeDeclaration> types = (List<TypeDeclaration>)service.getTypes(this);
		return types;
    }

    public List<ClassType> getAllTypes() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        if (service == null) {
            Debug.error("Service not defined in TypeDeclaration " + getName());
        }
        return service.getAllTypes(this);
    }

    public boolean isDefinedScope() {
        return name2type != UNDEFINED_SCOPE;
    }

    @SuppressWarnings("unchecked")
	public void setDefinedScope(boolean defined) {
        if (!defined) {
            name2type = UNDEFINED_SCOPE;
            name2field = UNDEFINED_SCOPE;
        } else {
            name2type = null;
            name2field = null;
        }
    }

    public List<TypeDeclaration> getTypesInScope() {
        if (name2type == null || name2type.isEmpty()) {
            return Collections.emptyList();
        }
        List<TypeDeclaration> res = new ArrayList<TypeDeclaration>();
        for(TypeDeclaration td : name2type.values()) {
            res.add(td);
        }
        return res;
    }

    public ClassType getTypeInScope(String tname) {
        Debug.assertNonnull(tname);
        if (name2type == null) {
            return null;
        }
        return name2type.get(tname);
    }

    public void addTypeToScope(ClassType type, String tname) {
        Debug.assertNonnull(type, tname);
        if (name2type == null || name2type == UNDEFINED_SCOPE) {
            name2type = new HashMap<String, TypeDeclaration>();
        }
        name2type.put(tname, (TypeDeclaration)type);
    }

    public void removeTypeFromScope(String tname) {
        Debug.assertNonnull(tname);
        if (name2type == null || name2type == UNDEFINED_SCOPE) {
            return;
        }
        name2type.remove(tname);
    }

    public List<FieldSpecification> getFieldsInScope() {
        if (name2field == null || name2field.isEmpty()) {
        	return Collections.emptyList();
        }
        List<FieldSpecification> res = new ArrayList<FieldSpecification>(name2field.size());
        for (FieldSpecification fs : name2field.values()) {
            res.add(fs);
        }
        return res;
    }

    public List<? extends VariableSpecification> getVariablesInScope() {
        return getFieldsInScope();
    }

    public VariableSpecification getVariableInScope(String tname) {
        Debug.assertNonnull(tname);
        if (name2field == null) {
            return null;
        }
        return name2field.get(tname);
    }

    public void addVariableToScope(VariableSpecification var) {
        Debug.assertBoolean(var instanceof FieldSpecification || (var instanceof EnumConstantSpecification && this instanceof EnumDeclaration));
        if (name2field == null || name2field == UNDEFINED_SCOPE) {
            name2field = new HashMap<String, FieldSpecification>();
        }
        name2field.put(var.getName(), (FieldSpecification)var);
    }

    public void removeVariableFromScope(String tname) {
        Debug.assertNonnull(tname);
        if (name2field == null || name2field == UNDEFINED_SCOPE) {
            return;
        }
        name2field.remove(tname);
    }
    
    public abstract ASTList<TypeParameterDeclaration> getTypeParameters();
    
    @Override
    public String toString() {
    	return getClass().getSimpleName() + " " + getFullName();
    }
    
    public abstract TypeDeclaration deepClone();

	public String getFullSignature() {
		String res = getFullName();
		ASTList<TypeParameterDeclaration> tps = getTypeParameters();
		if (tps == null || tps.size() == 0)
			return res;
		res += "<";
		String delim = "";
		for (TypeParameterDeclaration tpd : tps) {
			res += delim;
			res += tpd.getFullSignature();
			delim = ",";
		}
		res += ">";
		return res;
	}

	public ArrayType getArrayType() {
		return arrayType;
	}

	public ArrayType createArrayType() {
		if (arrayType == null)
			arrayType = new ArrayType(this, service.getServiceConfiguration().getImplicitElementInfo());
		return arrayType;
	}
	
	private ErasedType erasedType;
	
	public ErasedType getErasedType() {
		if (erasedType == null)
			erasedType = new ErasedType(this, getFactory().getServiceConfiguration().getImplicitElementInfo());
		return erasedType;
	}
	
	public ClassType getBaseClassType() {
		return this;
	}

	@Override
	public TypeDeclaration getGenericMember() {
		return this;
	}

}