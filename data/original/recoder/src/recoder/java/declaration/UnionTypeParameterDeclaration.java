/**
 * 
 */
package recoder.java.declaration;

import recoder.java.ProgramElement;
import recoder.java.SourceVisitor;
import recoder.java.reference.TypeReference;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;

/**
 * A union type as used in multi-catch clauses.
 * 
 * @author Tobias Gutzmann
 *
 */
public class UnionTypeParameterDeclaration extends ParameterDeclaration {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4701076660649706284L;
	
	private ASTList<TypeReference> typeReferences;
	
	/**
	 * 
	 */
	public UnionTypeParameterDeclaration() {
		// do nothing
	}

	/**
	 * @param typeRef
	 * @param vs
	 */
	public UnionTypeParameterDeclaration(ASTList<TypeReference> typeRefs, VariableSpecification vs) {
		super(null, vs);
		this.typeReferences = typeRefs;
	}

	/**
	 * @param proto
	 */
	public UnionTypeParameterDeclaration(UnionTypeParameterDeclaration proto) {
		super(proto);
		if (proto.typeReferences != null) 
			typeReferences = proto.typeReferences.deepClone();
		makeParentRoleValid();
	}

	/**
	 * delegate to internal 
	 */
	@Override
	public TypeReference getTypeReference() {
		if (typeReferences.size() > 1)
			throw new IllegalStateException("This is a multi-typeReference, getTypeReference() must not be called upon it");
		if (typeReferences.size() == 1)
			return typeReferences.get(0);
		return null;
	}
	
	@Override
	public void setTypeReference(TypeReference t) {
		// overwrites existing ones
		typeReferences = new ASTArrayList<>(t);
	}
	
	@Override
	public int getTypeReferenceCount() {
		return typeReferences == null ? 0 : typeReferences.size();
	}
	
	@Override
	public TypeReference getTypeReferenceAt(int index) {
		if (typeReferences == null || index >= typeReferences.size())
			throw new ArrayIndexOutOfBoundsException();
		return typeReferences.get(index);
	}
	
	public ASTList<TypeReference> getTypeReferences() {
		return typeReferences;
	}
	
	public void setTypeReferences(ASTList<TypeReference> typeReferences) {
		this.typeReferences = typeReferences;
	}

    public int getChildCount() {
        int result = 0;
        if (declarationSpecifiers != null)
            result += declarationSpecifiers.size();
        if (getVariableSpecification() != null)
            result++;
        result += typeReferences == null ? 0 : typeReferences.size();
        return result;
    }

    public ProgramElement getChildAt(int index) {
        int len;
        if (declarationSpecifiers != null) {
            len = declarationSpecifiers.size();
            if (len > index) {
                return declarationSpecifiers.get(index);
            }
            index -= len;
        }
        if (typeReferences != null) {
            if (index < typeReferences.size())
                return typeReferences.get(index);
            index -= typeReferences.size();
        }
        if (getVariableSpecification() != null) {
            if (index == 0)
                return getVariableSpecification();
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getChildPositionCode(ProgramElement child) {
        // role 0 (IDX): modifier
        // role 1: type reference
        // role 2 (IDX): var specs
        if (declarationSpecifiers != null) {
            int index = declarationSpecifiers.indexOf(child);
            if (index >= 0) {
                return (index << 4) | 0;
            }
        }
        if (typeReferences != null) {
        	int idx = typeReferences.indexOf(child);
        	if (idx != -1)
        		return (idx << 4) | 1;
        }
        if (getVariableSpecification() == child) {
            return 2;
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
                    DeclarationSpecifier r = (DeclarationSpecifier) q;
                    declarationSpecifiers.set(i, r);
                    r.setParent(this);
                }
                return true;
            }
        }
        if (getTypeReference() == p) {
            TypeReference r = (TypeReference) q;
            setTypeReference(r);
            if (r != null) {
                r.setParent(this);
            }
            return true;
        }
        count = (typeReferences == null) ? 0 : typeReferences.size();
        for (int i = 0; i < count; i++) {
        	if (typeReferences.get(i) == p) {
        		if (q == null) {
        			typeReferences.remove(i);
        		} else {
        			TypeReference r = (TypeReference) q;
        			typeReferences.set(i,  r);
        			r.setParent(this);
        		}
        		return true;
        	}
        }
        return false;
    }

    @Override
    public void makeParentRoleValid() {
    	super.makeParentRoleValid();
    	if (typeReferences != null) {
    		for (TypeReference tr: typeReferences) {
    			tr.setParent(this);
    		}
    	}
    }
    
    @Override
    public void accept(SourceVisitor v) {
    	v.visitUnionTypeParameterDeclaration(this);
    }
}
