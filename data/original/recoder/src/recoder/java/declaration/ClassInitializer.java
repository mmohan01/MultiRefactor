// This file is part of the RECODER library and protected by the LGPL.

package recoder.java.declaration;

import java.util.Collections;
import java.util.List;

import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Member;
import recoder.abstraction.Package;
import recoder.convenience.Naming;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.ScopeDefiningElement;
import recoder.java.SourceElement;
import recoder.java.SourceVisitor;
import recoder.java.Statement;
import recoder.java.StatementBlock;
import recoder.java.StatementContainer;
import recoder.java.declaration.modifier.Static;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;
import recoder.service.ProgramModelInfo;
import recoder.service.SourceInfo;
import recoder.util.Debug;

/**
 * Class initializer.
 * 
 * @author <TT>AutoDoc</TT>
 */

public class ClassInitializer extends JavaDeclaration implements Member, MemberDeclaration, StatementContainer, ClassTypeContainer, ScopeDefiningElement {

    /**
	 * serialization id
	 */
	private static final long serialVersionUID = 7172264701196251395L;

	/**
     * Parent.
     */

	private TypeDeclaration parent;

    /**
     * Body.
     */

    private StatementBlock body;

    private SourceInfo service;
    
    /**
     * Class initializer.
     */

    public ClassInitializer() {
        // nothing to do
    }

    /**
     * Class initializer.
     * 
     * @param body
     *            a statement block.
     */

    public ClassInitializer(StatementBlock body) {
        setBody(body);
        makeParentRoleValid();
    }

    /**
     * Class initializer.
     * 
     * @param modifier
     *            a static.
     * @param body
     *            a statement block.
     */

    public ClassInitializer(Static modifier, StatementBlock body) {
        if (modifier != null) {
        	ASTList<DeclarationSpecifier> mods = new ASTArrayList<DeclarationSpecifier>(1);
            mods.add(modifier);
            setDeclarationSpecifiers(mods);
        }
        setBody(body);
        makeParentRoleValid();
    }

    /**
     * Class initializer.
     * 
     * @param proto
     *            a class initializer.
     */

    protected ClassInitializer(ClassInitializer proto) {
        super(proto);
        if (proto.body != null) {
            body = proto.body.deepClone();
        }
        makeParentRoleValid();
    }

    /**
     * Deep clone.
     * 
     * @return the object.
     */

    public ClassInitializer deepClone() {
        return new ClassInitializer(this);
    }

    /**
     * Make parent role valid.
     */

    public void makeParentRoleValid() {
        super.makeParentRoleValid();
        if (body != null) {
            body.setStatementContainer(this);
        }
    }

    public SourceElement getFirstElement() {
        if (declarationSpecifiers != null && !declarationSpecifiers.isEmpty()) {
            return declarationSpecifiers.get(0);
        } else {
            return body;
        }
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
     * Returns the number of children of this node.
     * 
     * @return an int giving the number of children of this node
     */

    public int getChildCount() {
        int result = 0;
        if (declarationSpecifiers != null)
            result += declarationSpecifiers.size();
        if (body != null)
            result++;
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
        if (body != null) {
            if (index == 0)
                return body;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getChildPositionCode(ProgramElement child) {
        // role 0: modifier
        // role 1: body
        if (declarationSpecifiers != null) {
            int index = declarationSpecifiers.indexOf(child);
            if (index >= 0) {
                return (index << 4) | 0;
            }
        }
        if (body == child) {
            return 1;
        }
        return -1;
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
                    Static r = (Static) q;
                    declarationSpecifiers.set(i, r);
                    r.setParent(this);
                }
                return true;
            }
        }
        if (body == p) {
            StatementBlock r = (StatementBlock) q;
            body = r;
            if (r != null) {
                r.setStatementContainer(this);
            }
            return true;
        }
        return false;
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
     * @param t
     *            a type declaration.
     */

    public void setMemberParent(TypeDeclaration t) {
        parent = t;
    }

    /** A binary class initializer does not occur. */

    public boolean isBinary() {
        return false;
    }

    /**
     * Initializers are never public.
     */

    public boolean isPublic() {
        return false;
    }

    /**
     * Initializers are never protected.
     */

    public boolean isProtected() {
        return false;
    }

    /**
     * Initializers are never private (at least not explicitly).
     */

    public boolean isPrivate() {
        return false;
    }

    /**
     * Initializers are never strictfp.
     */

    public boolean isStrictFp() {
        return false;
    }

    /**
     * Test whether the declaration is static.
     */

    public boolean isStatic() {
        return declarationSpecifiers != null && !declarationSpecifiers.isEmpty();
    }

    public SourceElement getLastElement() {
        return body;
    }

    public void accept(SourceVisitor v) {
        v.visitClassInitializer(this);
    }

    @Override
    public String getFullName() {
        return Naming.getFullName(this);
    }
    

	@Override
	public String getBinaryName() {
		return null;
	}

    @Override
    public SourceInfo getProgramModelInfo() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service;
    }

    @Override
    public void setProgramModelInfo(ProgramModelInfo service) {
    	if (!(service instanceof SourceInfo))
    		throw new IllegalArgumentException("service for MethodDeclaration must be of type SourceInfo.");
        this.service = (SourceInfo)service;
    }

    private void updateModel() {
        getFactory().getServiceConfiguration().getChangeHistory().updateModel();
    }

	@Override
	public String getName() {
		return null;
	}

    /**
     * returns the types declared in the corresponding StatementBlock, if there
     * is any (i.e. method is not abstract). Returns
     * <code>RecoderList<ClassType>.EMPTY_LIST</code> otherwise.
     * 
     */
	@Override
    public List<TypeDeclaration> getTypes() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return getBody() == null ? Collections.<TypeDeclaration>emptyList() : getBody().getTypesInScope();
    }

    @Override
    public Package getPackage() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service.getPackage(this);
    }

    @Override
    public ClassTypeContainer getContainer() {
        return getContainingClassType();
    }

	@Override
    public ClassType getContainingClassType() {
        if (service == null) {
            Debug.log("Zero service while " + Debug.makeStackTrace());
            updateModel();
        }
        return service.getContainingClassType((ProgramElement)this);
    }

	@Override
	public Member getGenericMember() {
		return this;
	}
	
	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
    public boolean isDefinedScope() {
        return true;
    }

	@Override
    @SuppressWarnings("all") public void setDefinedScope(boolean defined) {
        // ignore
    }

}