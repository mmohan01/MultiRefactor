// This file is part of the RECODER library and protected by the LGPL.

package recoder.java;

import recoder.java.declaration.DeclarationSpecifier;
import recoder.list.generic.ASTList;


/**
 * Declaration.
 * 
 * @author <TT>AutoDoc</TT>
 */

public interface Declaration extends NonTerminalProgramElement {
    
    /**
     * Gets the declaration specifiers (annotations plus modifiers)
     * @return the list of declaration specifiers
     */
	ASTList<DeclarationSpecifier> getDeclarationSpecifiers();
    
    /**
     * Sets the declaration specifiers (annotations plus modifiers)
     * @param m the new list of declaration specifiers
     */
    void setDeclarationSpecifiers(ASTList<DeclarationSpecifier> d);
}