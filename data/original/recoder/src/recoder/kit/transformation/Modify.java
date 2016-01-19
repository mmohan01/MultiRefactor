// This file is part of the RECODER library and protected by the LGPL.

package recoder.kit.transformation;

import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.bytecode.AccessFlags;
import recoder.java.Declaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.Modifier;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.Static;
import recoder.kit.ModifierKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;

/**
 * Syntactic transformation that modifies a declaration by adding/removing
 * single modifiers. This transformation replaces an existing visibility
 * modifier if there is one. The modifier codes are taken from the
 * {@link recoder.kit.ModifierKit}, including the
 * {@link recoder.kit.ModifierKit#PACKAGE}pseudo modifier. The transformation
 * obeys the standard JavaDOC modifier order convention: Visibility modifiers go
 * first, then abstract or static and then final. All others are simply
 * appended.
 * 
 * @author AL
 */
public class Modify extends TwoPassTransformation {

    private boolean isVisible;

    private int modifier;

    private Declaration decl;

    private int insertPosition = -1;

    private Modifier remove;

    private Modifier insert;

    /**
     * Creates a new transformation object that modifies a declaration.
     * 
     * @param sc
     *            the service configuration to use.
     * @param isVisible
     *            flag indicating if this transformation shall be visible.
     * @param decl
     *            the declaration to modify. may not be <CODE>null</CODE> and
     *            must denote a valid identifier name.
     * @param code
     *            the modifier to create, using the codes from
     *            {@link recoder.kit.ModifierKit}.
     */
    public Modify(CrossReferenceServiceConfiguration sc, boolean isVisible, Declaration decl, int code) {
        super(sc);
        if (decl == null) {
            throw new IllegalArgumentException("Missing declaration");
        }
        this.isVisible = isVisible;
        this.decl = decl;
        this.modifier = code;
    }

    public boolean isVisible() {
        return isVisible;
    }

    private int getLastModifierPosition() {
    	List<DeclarationSpecifier> mods = decl.getDeclarationSpecifiers();
        if (mods == null) {
            return 0;
        }
        return mods.size();
    }

    private static boolean containsModifier(Declaration decl, Class<? extends Modifier> mod) {
    	List<DeclarationSpecifier> mods = decl.getDeclarationSpecifiers();
        if (mods == null) {
            return false;
        }
        for (int i = 0; i < mods.size(); i += 1) {
            DeclarationSpecifier res = mods.get(i);
            if (mod.isInstance(res)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds out which modifier to remove and which to add, if any. This
     * transformation is syntactic.
     * 
     * @return the problem report.
     * @deprecated Does not check vadility of modification.
     */
    public ProblemReport analyze() {
        insertPosition = 0;
        Modifier mod = ModifierKit.createModifier(getProgramFactory(), modifier);
        if ((modifier != ModifierKit.PACKAGE) && containsModifier(decl, mod.getClass())) {
            return setProblemReport(IDENTITY);
        }
        switch (modifier) {
        case ModifierKit.PACKAGE:
            remove = ModifierKit.getVisibilityModifier(decl);
            break;
        case AccessFlags.PUBLIC:
            remove = ModifierKit.getVisibilityModifier(decl);
            if (remove instanceof Public) {
                break;
            }
            insert = getProgramFactory().createPublic();
            break;
        case AccessFlags.PROTECTED:
            remove = ModifierKit.getVisibilityModifier(decl);
            if (remove instanceof Protected) {
                break;
            }
            insert = getProgramFactory().createProtected();
            break;
        case AccessFlags.PRIVATE:
            remove = ModifierKit.getVisibilityModifier(decl);
            if (remove instanceof Private) {
                break;
            }
            insert = getProgramFactory().createPrivate();
            break;
        case AccessFlags.STATIC:
            if (ModifierKit.getVisibilityModifier(decl) != null) {
                insertPosition += 1;
            }
            insert = getProgramFactory().createStatic();
            break;
        case AccessFlags.FINAL:
            if (ModifierKit.getVisibilityModifier(decl) != null) {
                insertPosition += 1;
            }
            if (containsModifier(decl, Static.class)) {
                insertPosition += 1;
            }
            insert = getProgramFactory().createFinal();
            break;
        case AccessFlags.ABSTRACT:
            if (ModifierKit.getVisibilityModifier(decl) != null) {
                insertPosition += 1;
            }
            insert = getProgramFactory().createAbstract();
            break;
        case AccessFlags.SYNCHRONIZED:
            insertPosition = getLastModifierPosition();
            insert = getProgramFactory().createSynchronized();
            break;
        case AccessFlags.TRANSIENT:
            insertPosition = getLastModifierPosition();
            insert = getProgramFactory().createTransient();
            break;
        case AccessFlags.STRICT:
            insertPosition = getLastModifierPosition();
            insert = getProgramFactory().createStrictFp();
            break;
        case AccessFlags.VOLATILE:
            insertPosition = getLastModifierPosition();
            insert = getProgramFactory().createVolatile();
            break;
        case AccessFlags.NATIVE:
            insertPosition = getLastModifierPosition();
            insert = getProgramFactory().createNative();
            break;
        default:
            throw new IllegalArgumentException("Unsupported modifier code " + modifier);
        }
        return setProblemReport(NO_PROBLEM);
    }

    /**
     * Attaches or detaches modifiers when necessary.
     * 
     * @exception IllegalStateException
     *                if the analyzation has not been called.
     * @see #analyze()
     */
    public void transform() {
        super.transform();
        if (remove != null) {
            detach(remove);
        }
        if (insert != null) {
            attach(insert, decl, insertPosition);
        }
    }

    /**
     * Returns the modifier that is detached, or is to be detached.
     * 
     * @return the modifier that is / will be detached.
     */
    public Modifier getDetached() {
        return remove;
    }

    /**
     * Returns the modifier that is attached, or is to be attached.
     * 
     * @return the modifier that is / will be attached.
     */
    public Modifier getAttached() {
        return insert;
    }
}