// This file is part of the RECODER library and protected by the LGPL.

package recoder.java.declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.Type;
import recoder.convenience.Naming;
import recoder.java.Identifier;
import recoder.java.NamedProgramElement;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ParameterContainer;
import recoder.java.ProgramElement;
import recoder.java.SourceElement;
import recoder.java.SourceVisitor;
import recoder.java.Statement;
import recoder.java.StatementBlock;
import recoder.java.TypeScope;
import recoder.java.VariableScope;
import recoder.java.reference.TypeReference;
import recoder.java.reference.TypeReferenceContainer;
import recoder.list.generic.ASTList;
import recoder.service.ProgramModelInfo;
import recoder.service.SourceInfo;
import recoder.util.Debug;

/**
 * Method declaration.
 * 
 * @author <TT>AutoDoc</TT>
 */

public class MethodDeclaration extends JavaDeclaration implements MemberDeclaration, TypeReferenceContainer,
        NamedProgramElement, ParameterContainer, Method, VariableScope, TypeDeclarationContainer, TypeScope {

    /**
	 * serialization id
	 */
	private static final long serialVersionUID = -5270980702156620518L;

	/**
     * Parent.
     */

	private TypeDeclaration parent;

    /**
     * Return type.
     */

    private TypeReference returnType;

    /**
     * Name.
     */

    private Identifier name;

    /**
     * Parameters.
     */

    private ASTList<ParameterDeclaration> parameters;

    /**
     * Exceptions.
     */

    private Throws exceptions;

    /**
     * Body.
     */

    private StatementBlock body;

    /**
     * Service.
     */

    private SourceInfo service;
    

    /**
     * Type parameters.
     */
    private ASTList<TypeParameterDeclaration> typeParameters;

    /**
     * Method declaration.
     */
    public MethodDeclaration() {
        // nothing to do here
    }

    /**
     * Method declaration.
     * 
     * @param modifiers
     *            a modifier mutable list.
     * @param returnType
     *            a type reference.
     * @param name
     *            an identifier.
     * @param parameters
     *            a parameter declaration mutable list.
     * @param exceptions
     *            a throws.
     */

    public MethodDeclaration(ASTList<DeclarationSpecifier> modifiers, TypeReference returnType, Identifier name,
    		ASTList<ParameterDeclaration> parameters, Throws exceptions) {
        super(modifiers);
        setTypeReference(returnType);
        setIdentifier(name);
        setParameters(parameters);
        setThrown(exceptions);
        makeParentRoleValid();
    }

    /**
     * Method declaration.
     * 
     * @param modifiers
     *            a modifier mutable list.
     * @param returnType
     *            a type reference.
     * @param name
     *            an identifier.
     * @param parameters
     *            a parameter declaration mutable list.
     * @param exceptions
     *            a throws.
     * @param body
     *            a statement block.
     */

    public MethodDeclaration(ASTList<DeclarationSpecifier> modifiers, TypeReference returnType, Identifier name,
    		ASTList<ParameterDeclaration> parameters, Throws exceptions, StatementBlock body) {
        super(modifiers);
        setTypeReference(returnType);
        setIdentifier(name);
        setParameters(parameters);
        setThrown(exceptions);
        setBody(body);
        makeParentRoleValid();
    }

    /**
     * Method declaration.
     * 
     * @param proto
     *            a method declaration.
     */

    protected MethodDeclaration(MethodDeclaration proto) {
        super(proto);
        if (proto.returnType != null) {
            returnType = proto.returnType.deepClone();
        }
        if (proto.name != null) {
            name = proto.name.deepClone();
        }
        if (proto.parameters != null) {
            parameters = proto.parameters.deepClone();
        }
        if (proto.exceptions != null) {
            exceptions = proto.exceptions.deepClone();
        }
        if (proto.body != null) {
            body = proto.body.deepClone();
        }
        if (proto.typeParameters != null) {
        	typeParameters = proto.typeParameters.deepClone();
        }
        makeParentRoleValid();
    }

    /**
     * Deep clone.
     * 
     * @return the object.
     */

    public MethodDeclaration deepClone() {
        return new MethodDeclaration(this);
    }

    /**
     * Make parent role valid.
     */

    public void makeParentRoleValid() {
        super.makeParentRoleValid();
        if (returnType != null) {
            returnType.setParent(this);
        }
        if (name != null) {
            name.setParent(this);
        }
        if (exceptions != null) {
            exceptions.setParent(this);
        }
        if (parameters != null) {
            for (int i = parameters.size() - 1; i >= 0; i -= 1) {
                parameters.get(i).setParameterContainer(this);
            }
        }
        if (body != null) {
            body.setStatementContainer(this);
        }
//        if (isVarArg != null) {
//            isVarArg.setParent(this);
//        }
        if (typeParameters != null) {
        	for (TypeParameterDeclaration tpd: typeParameters) {
        		tpd.setParent(this);
        	}
        }
    }

    public int getChildPositionCode(ProgramElement child) {
        // role 0 (IDX): modifier
        // role 1: return type (note: no occurrence in constructors)
        // role 2: name
        // role 3 (IDX): parameter
        // role 4: throws
        // role 5: body
        // role 6: (not used...) 
    	// role 7 (IDX): type parameter
    	// role 8: default value (AnnotationPropertyDeclaration only)
        if (declarationSpecifiers != null) {
            int index = declarationSpecifiers.indexOf(child);
            if (index >= 0) {
                return (index << 4) | 0;
            }
        }
        if (returnType == child) {
            return 1;
        }
        if (name == child) {
            return 2;
        }
        if (parameters != null) {
            int index = parameters.indexOf(child);
            if (index >= 0) {
                return (index << 4) | 3;
            }
        }
        if (exceptions == child) {
            return 4;
        }
        if (body == child) {
            return 5;
        }
        // role 6 used to be used for VarArgSpecifier, which
        // was only present in an intermediate development version 
        // of Recoder. Changing this back to (6) would, however,
        // require to change ChangHistory as well. We rather leave
        // it as it is.
        if (typeParameters != null) {
        	int index = typeParameters.indexOf(child);
        	if (index != -1)
        		return (index << 4) | 7;
        }
        return -1;
    }

    public SourceElement getFirstElement() {
    	// do not return a type parameter declaration as first element:
    	// syntactically, the '<' would be first element then!
    	SourceElement ch = getChildAt(0);
    	return ch instanceof TypeParameterDeclaration ? this : ch.getFirstElement();
    }

    public SourceElement getLastElement() {
        return getChildAt(getChildCount() - 1).getLastElement();
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
     * Get member parent.
     * 
     * @return the type declaration.
     */

    public TypeDeclaration getMemberParent() {
        return parent;
    }

    /**
     * Set member parent.
     * 
     * @param decl
     *            a type declaration.
     */

    public void setMemberParent(TypeDeclaration decl) {
        parent = decl;
    }

    /**
     * Returns the number of children of this node.
     * 
     * @return an int giving the number of children of this node
     */

    public int getChildCount() {
        int result = 0;
        if (declarationSpecifiers != null)
            result += declarationSpecifiers.size();
        if (returnType != null)
            result++;
        if (name != null)
            result++;
        if (parameters != null)
            result += parameters.size();
        if (exceptions != null)
            result++;
        if (body != null)
            result++;
        if (typeParameters != null)
        	result += typeParameters.size();
        return result;
    }

    /**
     * Returns the child at the specified index in this node's "virtual" child
     * array
     * 
     * @param index
     *            an index into this node's "virtual" child array
     * @return the program element at the given position
     * @exception ArrayIndexOutOfBoundsException
     *                if <tt>index</tt> is out of bounds
     */

    public ProgramElement getChildAt(int index) {
        int len;
        if (declarationSpecifiers != null) {
            len = declarationSpecifiers.size();
            if (len > index) {
                return declarationSpecifiers.get(index);
            }
            index -= len;
        }
        if (typeParameters != null) {
        	len = typeParameters.size();
        	if (len > index)
        		return typeParameters.get(index);
        	index -= len;
        }
        if (returnType != null) {
            if (index == 0)
                return returnType;
            index--;
        }
        if (name != null) {
            if (index == 0)
                return name;
            index--;
        }
        if (parameters != null) {
            len = parameters.size();
            if (len > index) {
                return parameters.get(index);
            }
            index -= len;
        }
        if (exceptions != null) {
            if (index == 0)
                return exceptions;
            index--;
        }
        if (body != null) {
            if (index == 0)
                return body;
            index--;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Get the number of statements in this container.
     * 
     * @return the number of statements.
     */

    public int getStatementCount() {
        return (body != null) ? 1 : 0;
    }

    /*
     * Return the statement at the specified index in this node's "virtual"
     * statement array. @param index an index for a statement. @return the
     * statement with the given index. @exception ArrayIndexOutOfBoundsException
     * if <tt> index </tt> is out of bounds.
     */
    public Statement getStatementAt(int index) {
        if (body != null && index == 0) {
            return body;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Replace a single child in the current node. The child to replace is
     * matched by identity and hence must be known exactly. The replacement
     * element can be null - in that case, the child is effectively removed. The
     * parent role of the new child is validated, while the parent link of the
     * replaced child is left untouched.
     * 
     * @param p
     *            the old child.
     * @param p
     *            the new child.
     * @return true if a replacement has occured, false otherwise.
     * @exception ClassCastException
     *                if the new child cannot take over the role of the old one.
     */

    public boolean replaceChild(ProgramElement p, ProgramElement q) {
        if (p == null) {
            throw new NullPointerException();
        }
        int count;
        count = (declarationSpecifiers == null) ? 0 : declarationSpecifiers.size();
        for (int i = 0; i < count; i++) {
            if (declarationSpecifiers.get(i) == p) {
                if (q == null) {
                    declarationSpecifiers.remove(i);
                } else {
                    DeclarationSpecifier r = (DeclarationSpecifier) q;
                    declarationSpecifiers.set(i, r);
                    r.setParent(this);
                }
                return true;
            }
        }
        if (returnType == p) {
            TypeReference r = (TypeReference) q;
            returnType = r;
            if (r != null) {
                r.setParent(this);
            }
            return true;
        }
        if (name == p) {
            Identifier r = (Identifier) q;
            name = r;
            if (r != null) {
                r.setParent(this);
            }
            return true;
        }
        count = (parameters == null) ? 0 : parameters.size();
        for (int i = 0; i < count; i++) {
            if (parameters.get(i) == p) {
                if (q == null) {
                    parameters.remove(i);
                } else {
                    ParameterDeclaration r = (ParameterDeclaration) q;
                    parameters.set(i, r);
                    r.setParameterContainer(this);
                }
                return true;
            }
        }
        if (exceptions == p) {
            Throws r = (Throws) q;
            exceptions = r;
            if (r != null) {
                r.setParent(this);
            }
            return true;
        }
        if (body == p) {
            StatementBlock r = (StatementBlock) q;
            body = r;
            if (r != null) {
                r.setStatementContainer(this);
            }
            return true;
        }
        if (typeParameters != null) {
        	int i =  typeParameters.indexOf(p);
        	if (i != -1) {
        		if (q == null) {
        			typeParameters.remove(i);
        		} else {
        			typeParameters.set(i, (TypeParameterDeclaration)q);
        			((TypeParameterDeclaration)q).setParent(this);
        		}
        		return true;
        	}
        }

        return false;
    }

    /**
     * Get the number of type references in this container.
     * 
     * @return the number of type references.
     */

    public int getTypeReferenceCount() {
        return (returnType != null) ? 1 : 0;
    }

    /*
     * Return the type reference at the specified index in this node's "virtual"
     * type reference array. @param index an index for a type reference. @return
     * the type reference with the given index. @exception
     * ArrayIndexOutOfBoundsException if <tt> index </tt> is out of bounds.
     */

    public TypeReference getTypeReferenceAt(int index) {
        if (returnType != null && index == 0) {
            return returnType;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Get the number of parameters in this container.
     * 
     * @return the number of parameters.
     */

    public int getParameterDeclarationCount() {
        return (parameters != null) ? parameters.size() : 0;
    }

    /*
     * Return the parameter declaration at the specified index in this node's
     * "virtual" parameter declaration array. @param index an index for a
     * parameter declaration. @return the parameter declaration with the given
     * index. @exception ArrayIndexOutOfBoundsException if <tt> index </tt> is
     * out of bounds.
     */

    public ParameterDeclaration getParameterDeclarationAt(int index) {
        if (parameters != null) {
            return parameters.get(index);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Get return type.
     * 
     * @return the type reference.
     */

    public TypeReference getTypeReference() {
        return returnType;
    }

    /**
     * Sets the return type. Null is okay for subclass ConstructorDeclaration.
     */

    public void setTypeReference(TypeReference type) {
        returnType = type;
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
     * Get parameters.
     * 
     * @return the parameter declaration mutable list.
     */

    public ASTList<ParameterDeclaration> getParameters() {
        return parameters;
    }

    /**
     * Set parameters.
     * 
     * @param list
     *            a parameter declaration mutable list.
     */

    public void setParameters(ASTList<ParameterDeclaration> list) {
        parameters = list;
    }

    /**
     * Get thrown.
     * 
     * @return the throws.
     */

    public Throws getThrown() {
        return exceptions;
    }

    /**
     * Set thrown.
     * 
     * @param exceptions
     *            a throws.
     */

    public void setThrown(Throws exceptions) {
        this.exceptions = exceptions;
    }

    /**
     * Get body.
     * 
     * @return the statement block.
     */

    public StatementBlock getBody() {
        return body;
    }

    /**
     * Set body.
     * 
     * @param body
     *            a statement block.
     */

    public void setBody(StatementBlock body) {
        this.body = body;
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
     * Test whether the declaration is public. Methods of interfaces are always
     * public.
     */

    public boolean isPublic() {
        return (getASTParent() instanceof InterfaceDeclaration) || super.isPublic();
    }

    /**
     * Test whether the declaration is static.
     */

    public boolean isStatic() {
        return super.isStatic();
    }

    /**
     * Test whether the declaration is strictfp.
     */

    public boolean isStrictFp() {
        return super.isStrictFp();
    }

    /**
     * Test whether the declaration is abstract. Methods of interfaces are
     * always abstract.
     */

    public boolean isAbstract() {
        return (getASTParent() instanceof InterfaceDeclaration) || super.isAbstract();
    }

    /**
     * Test whether the declaration is native. Constructors are never native.
     */

    public boolean isNative() {
        return super.isNative();
    }

    /**
     * Test whether the declaration is synchronized.
     */

    public boolean isSynchronized() {
        return super.isSynchronized();
    }

    public SourceInfo getProgramModelInfo() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service;
    }

    public void setProgramModelInfo(ProgramModelInfo service) {
    	if (!(service instanceof SourceInfo))
    		throw new IllegalArgumentException("service for MethodDeclaration must be of type SourceInfo.");
        this.service = (SourceInfo)service;
    }

    private void updateModel() {
        getFactory().getServiceConfiguration().getChangeHistory().updateModel();
    }

    public ClassType getContainingClassType() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service.getContainingClassType((ProgramElement)this);
    }

    public Type getReturnType() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service.getReturnType(this);
    }

    public List<Type> getSignature() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service.getSignature(this);
    }

    public List<ClassType> getExceptions() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service.getExceptions(this);
    }

    public ClassTypeContainer getContainer() {
        return getContainingClassType();
    }

    public Package getPackage() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service.getPackage(this);
    }

    /**
     * returns the types declared in the corresponding StatementBlock, if there
     * is any (i.e. method is not abstract). Returns
     * <code>RecoderList<ClassType>.EMPTY_LIST</code> otherwise.
     * 
     */
    public List<TypeDeclaration> getTypes() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return getBody() == null ? Collections.<TypeDeclaration>emptyList() : getBody().getTypesInScope();
    }

    public String getFullName() {
        return Naming.getFullName(this);
    }
    
    public String getBinaryName() {
    	return parent.getBinaryName() + "." + getName();
    }

    public boolean isDefinedScope() {
        return true;
    }

    @SuppressWarnings("all") public void setDefinedScope(boolean defined) {
        // ignore
    }

    public List<VariableSpecification> getVariablesInScope() {
        if (parameters == null || parameters.isEmpty()) {
        	return Collections.emptyList();
        }
        int s = parameters.size();
        List<VariableSpecification> res = new ArrayList<VariableSpecification>(s);
        for (int i = 0; i < s; i += 1) {
            res.add(parameters.get(i).getVariableSpecification());
        }
        return res;
    }

    public VariableSpecification getVariableInScope(String tname) {
        Debug.assertNonnull(tname);
        if (parameters == null) {
            return null;
        }
        for (int i = 0, s = parameters.size(); i < s; i += 1) {
            VariableSpecification res = parameters.get(i).getVariableSpecification();
            if (tname.equals(res.getName())) {
                return res;
            }
        }
        return null;
    }

    public void addVariableToScope(VariableSpecification var) {
        Debug.assertNonnull(var);
    }

    public void removeVariableFromScope(String tname) {
        Debug.assertNonnull(tname);
    }

    public void accept(SourceVisitor v) {
        v.visitMethodDeclaration(this);
    }

    public boolean isVarArgMethod() {
    	if (parameters == null || parameters.size() == 0)
    		return false;
    	return parameters.get(parameters.size()-1).isVarArg();
    }
    
	public ASTList<TypeParameterDeclaration> getTypeParameters() {
		return typeParameters;
	}
	
	public void setTypeParameters(ASTList<TypeParameterDeclaration> typeParameters) {
		this.typeParameters = typeParameters;
	}

	public int getTypeDeclarationCount() {
		return typeParameters == null ? 0 : typeParameters.size();
	}

	public TypeDeclaration getTypeDeclarationAt(int index) {
		if (typeParameters == null)
			throw new IndexOutOfBoundsException();
		return typeParameters.get(index);
	}

	public List<TypeDeclaration> getTypesInScope() {
		if (typeParameters == null || typeParameters.isEmpty())
			return Collections.emptyList();
		List<TypeDeclaration> ctl = new ArrayList<TypeDeclaration>(typeParameters.size());
		for (TypeParameterDeclaration t: typeParameters) {
			ctl.add(t);
		}
		return ctl;
	}

	public ClassType getTypeInScope(String typename) {
		if (typeParameters == null)
			return null;
		for(TypeParameterDeclaration tpd: typeParameters) {
			if (typename.equals(tpd.getName()))
				return tpd;
		}
		return null;
	}

    @SuppressWarnings("all") public void addTypeToScope(ClassType type, String name) {
		// ignore
	}

    @SuppressWarnings("all") public void removeTypeFromScope(String name) {
		// ignore
	}
    
    @Override
    public String toString() {
    	return "<"+getClass().getSimpleName()+"> " + name.getText() + "(" + (parameters==null ? 0 : parameters.size()) + ")";
    }

	@Override
	public MethodDeclaration getGenericMember() {
		return this;
	}

	@Override
	public String getFullSignature() {
		return Method.SignatureBuilder.buildSignature(this);
	}
}