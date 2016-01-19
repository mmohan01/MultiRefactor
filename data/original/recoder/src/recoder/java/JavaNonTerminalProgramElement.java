// This file is part of the RECODER library and protected by the LGPL.

package recoder.java;

import recoder.ModelException;
import recoder.convenience.TreeWalker;

/**
 * Top level implementation of a Java {@link NonTerminalProgramElement}.
 * 
 * @author AL
 */

public abstract class JavaNonTerminalProgramElement extends JavaProgramElement implements NonTerminalProgramElement {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Java program element.
     */

    public JavaNonTerminalProgramElement() {
        // nothing to do here
    }

    /**
     * Java program element.
     * 
     * @param proto
     *            a java program element.
     */

    protected JavaNonTerminalProgramElement(JavaProgramElement proto) {
        super(proto);
    }

    /**
     * Defaults to do nothing.
     */

    public abstract void makeParentRoleValid();

    /**
     * Defaults to attempt a depth-first traversal using a
     * {@link recoder.convenience.TreeWalker}.
     */

    public final void makeAllParentRolesValid() {
        TreeWalker tw = new TreeWalker(this);
        while (tw.next(NonTerminalProgramElement.class)) {
            ((NonTerminalProgramElement) tw.getProgramElement()).makeParentRoleValid();
        }
    }

    /**
     * Extracts the role of a child from its position code.
     * 
     * @param positionCode
     *            the position code.
     * @return the role code of the given position code.
     * @see NonTerminalProgramElement#getChildPositionCode(ProgramElement)
     */
    public final int getRoleOfChild(int positionCode) {
        return positionCode & 15;
    }

    /**
     * Returns the index of the given child, or <CODE>-1</CODE> if there is no
     * such child. The child is searched for by identity: <CODE>
     * getChildAt(getIndexOfChild(x)) == x</CODE>.
     * 
     * @param child
     *            the exact child to look for.
     * @return the index of the given child, or <CODE>-1</CODE>.
     */
    public final int getIndexOfChild(ProgramElement child) {
        int i;
        for (i = getChildCount() - 1; i >= 0 && (getChildAt(i) != child); i--) {
            /* logic is contained in loop control */
        }
        return i;
    }
    
    public final void validateAll() throws ModelException { 
        TreeWalker tw = new TreeWalker(this);
        while (tw.next()) {
            tw.getProgramElement().validate();
        }    	
    }
}