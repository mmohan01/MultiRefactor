// This file is part of the RECODER library and protected by the LGPL.

package recoder.java;

/**
 * Identifier.
 * 
 * @author AL
 * @author <TT>AutoDoc</TT>
 */

public final class Identifier extends JavaProgramElement implements TerminalProgramElement {

    /**
	 * serialization id
	 */
	private static final long serialVersionUID = 4261793022531143013L;

	/**
     * Parent.
     */

	private NonTerminalProgramElement parent;

    /**
     * Id.
     */

    private final String id;

    /**
     * Identifier.
     */

    public Identifier() {
        id = "";
    }

    /**
     * Identifier.
     * 
     * @param text
     *            a string.
     */

    public Identifier(String text) {
        if (!Character.isJavaIdentifierStart(text.charAt(0))) {
		    throw new IllegalArgumentException((text + " is not a valid Java identifier"));
		}
		for (int i = text.length() - 1; i >= 1; i -= 1) {
		    if (!Character.isJavaIdentifierPart(text.charAt(i))) {
		        throw new IllegalArgumentException((text + " is not a valid Java identifier"));
		    }
		}
		id = text.intern();
    }

    /**
     * Identifier.
     * 
     * @param proto
     *            an identifier.
     */

    protected Identifier(Identifier proto) {
        super(proto);
        id = proto.id;
    }

    /**
     * Deep clone.
     * 
     * @return the object.
     */

    public Identifier deepClone() {
        return new Identifier(this);
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
     * Get parent.
     * 
     * @return the named program element.
     */

    public NonTerminalProgramElement getParent() {
        return parent;
    }

    /**
     * Set parent.
     * 
     * @param p
     *            a named program element.
     */

    public void setParent(NonTerminalProgramElement p) {
        parent = p;
    }
    
    /**
     * Get text. The String is made unambiguous.
     * 
     * @return the string.
     */

    public final String getText() {
        return id;
    }

    public void accept(SourceVisitor v) {
        v.visitIdentifier(this);
    }
    
    @Override
    public String toString() {
    	return "<Identifier> " + id;
    }
}