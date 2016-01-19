// This file is part of the RECODER library and protected by the LGPL.

package recoder.java.declaration;

import java.util.List;

import recoder.abstraction.ClassType;
import recoder.abstraction.Field;
import recoder.convenience.Naming;
import recoder.java.Expression;
import recoder.java.Identifier;
import recoder.java.ProgramElement;
import recoder.java.SourceVisitor;
import recoder.list.generic.ASTList;
import recoder.util.Debug;

public class FieldSpecification extends VariableSpecification implements Field {

    /**
	 * serialization id
	 */
	private static final long serialVersionUID = -8228158502939901347L;

	/**
     * Field specification.
     */

    public FieldSpecification() {
        // nothing to do here
    }

    /**
     * Field specification.
     * 
     * @param name
     *            an identifier.
     */

    public FieldSpecification(Identifier name) {
        super(name);
    }

    /**
     * Field specification.
     * 
     * @param name
     *            an identifier.
     * @param init
     *            an expression.
     */

    public FieldSpecification(Identifier name, Expression init) {
        super(name, init);
    }

    /**
     * Field specification.
     * 
     * @param name
     *            an identifier.
     * @param dimensions
     *            an int value.
     * @param init
     *            an expression.
     */

    public FieldSpecification(Identifier name, int dimensions, Expression init) {
        super(name, dimensions, init);
    }

    /**
     * Field specification.
     * 
     * @param proto
     *            a field specification.
     */

    protected FieldSpecification(FieldSpecification proto) {
        super(proto);
    }

    /**
     * Deep clone.
     * 
     * @return the object.
     */

    public FieldSpecification deepClone() {
        return new FieldSpecification(this);
    }

    /**
     * Set parent.
     * 
     * @param parent
     *            must be a field declaration.
     */

    public void setParent(VariableDeclaration parent) {
        setParent((FieldDeclaration) parent);
    }

    /**
     * Set parent.
     * 
     * @param parent
     *            a field declaration.
     */

    public void setParent(FieldDeclaration parent) {
        super.setParent(parent);
    }

    /**
     * Test whether the declaration is private.
     */

    public boolean isPrivate() {
        return parent.isPrivate();
    }

    /**
     * Test whether the declaration is protected.
     */

    public boolean isProtected() {
        return parent.isProtected();
    }

    /**
     * Test whether the declaration is public.
     */

    public boolean isPublic() {
        return parent.isPublic();
    }

    /**
     * Test whether the declaration is static.
     */

    public boolean isStatic() {
        return parent.isStatic();
    }

    /**
     * Test whether the declaration is transient.
     */

    public boolean isTransient() {
        return parent.isTransient();
    }

    /**
     * Test whether the declaration is volatile.
     */

    public boolean isVolatile() {
        return parent.isVolatile();
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

    public String getFullName() {
        return Naming.getFullName(this);
    }
    
    public String getBinaryName() {
    	return getContainingClassType().getBinaryName() + "." + getName();
    }

    public void accept(SourceVisitor v) {
        v.visitFieldSpecification(this);
    }

    /**
     * get annotations of enclosing declaration
     */
    public List<AnnotationUseSpecification> getAnnotations() {
        return parent.getAnnotations();
    }
    
    /**
     * get type arguments of parent's type reference 
     */
    public ASTList<TypeArgumentDeclaration> getTypeArguments() {
    	return getParent().getTypeReference().getTypeArguments();
    }
    
    @Override
    public String toString() {
    	return "<FieldSpecification> " + getName();
    }
    
    @Override
    public FieldDeclaration getParent() {
    	return (FieldDeclaration)super.getParent();
    }
    
	@Override
	public FieldSpecification getGenericMember() {
		return this;
	}


}