/*
 * Created on 27.10.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.java.declaration;

import recoder.ModelException;
import recoder.abstraction.EnumConstant;
import recoder.java.Identifier;
import recoder.java.ProgramElement;
import recoder.java.SourceVisitor;
import recoder.java.reference.EnumConstructorReference;
import recoder.list.generic.ASTList;

/**
 * @author Tobias Gutzmann
 *
 */
public class EnumConstantSpecification extends FieldSpecification implements EnumConstant {
	/**
	 * serialization id
	 */
	private static final long serialVersionUID = -40018491975119655L;
	
	/**
	 * a reference to the enum type.
	 * Note that this must always be set, even if no parantheses are present in the concrete syntax.
	 * 
	 */
	private EnumConstructorReference ref;
	
	/**
	 * 
	 */
	public EnumConstantSpecification() {
		super();
	}
	
	public void accept(SourceVisitor v) {
		v.visitEnumConstantSpecification(this);
	}

	/**
	 * @param name
	 */
	public EnumConstantSpecification(Identifier name) {
		super(name);
	}

	/**
	 * @param name
	 * @param init
	 */
	public EnumConstantSpecification(Identifier name, EnumConstructorReference ref) {
		super(name);
		this.ref = ref;
		makeParentRoleValid();
	}

	/**
	 * @param proto
	 */
	protected EnumConstantSpecification(EnumConstantSpecification proto) {
		super(proto);
		if (proto.ref != null)
			ref = proto.ref.deepClone();
		makeParentRoleValid();
	}
	
    public EnumConstantSpecification deepClone() {
        return new EnumConstantSpecification(this);
    }
    
    public EnumConstantDeclaration getParent() {
    	return (EnumConstantDeclaration)parent;
    }
    
    public void makeParentRoleValid() {
    	super.makeParentRoleValid();
    	if (ref != null) ref.setParent(this);
    }
    
    @Override
    public int getChildCount() {
    	return super.getChildCount() + (ref == null ? 0 : 1);
    }
    
    @Override
    public ProgramElement getChildAt(int pos) {
    	if (pos == super.getChildCount() && ref != null) {
   			return ref;
    	}
    	return super.getChildAt(pos);
    }
    
    @Override
    public boolean replaceChild(ProgramElement p, ProgramElement q) {
    	if (p == null) {
    		throw new NullPointerException();
    	}
    	if (p == ref) {
    		ref = (EnumConstructorReference)q;
    		if (ref != null)
    			ref.setParent(this);
    		return true;
    	}
    	return super.replaceChild(p, q);
    }
    
    @Override
    public int getChildPositionCode(ProgramElement child) {
    	if (child == ref)
    		return 2;
    	return super.getChildPositionCode(child);
    }
    
    public EnumConstructorReference getConstructorReference() {
    	return ref;
    }
    
    public void setConstructorReference(EnumConstructorReference ref) {
    	this.ref = ref;
    }
    
    @Override
    public void validate() throws ModelException {
    	super.validate();
    	if (ref == null) throw new ModelException("EnumConstructorReference not set in " + getFullName());
    }

    @Override
    public ASTList<TypeArgumentDeclaration> getTypeArguments() {
    	return null;
    }
}
