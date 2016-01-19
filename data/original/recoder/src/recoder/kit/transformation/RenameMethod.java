// This file is part of the RECODER library and protected by the LGPL.

package recoder.kit.transformation;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.Method;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.kit.IllegalName;
import recoder.kit.MethodKit;
import recoder.kit.MissingSources;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.service.CrossReferenceSourceInfo;

/**
 * Transformation that renames a method and all known references to that method.
 * The new name should not be used for another method with the same signature.
 * <P>
 * <B>Implementation warning: </B> does not (yet) check vadility of new name in
 * the context
 * 
 * @author AL
 * @author AM
 */
public class RenameMethod extends TwoPassTransformation {

    private Method methodToRename;

    private String newName;

    private List<MethodDeclaration> methods;

    private List<MethodReference> refs;

    /**
     * Creates a new transformation object that renames a method and all
     * redefined versions and all references to them. The new name should not
     * conflict with another method in any of the occurances.
     * 
     * @param sc
     *            the service configuration to use.
     * @param method
     *            the method to be renamed; may not be <CODE>null</CODE>.
     * @param newName
     *            the new name for the element; may not be <CODE>null</CODE>
     *            and must denote a valid identifier name.
     */
    public RenameMethod(CrossReferenceServiceConfiguration sc, MethodDeclaration method, String newName) {
        super(sc);
        if (method == null) {
            throw new IllegalArgumentException("Missing method");
        }
        if (newName == null) {
            throw new IllegalArgumentException("Missing name");
        }
        this.methodToRename = method;
        this.newName = newName;
    }

    /**
     * Problem report indicating that certain method declarations redefining the
     * method to rename are not available in source code.
     */
    public static class MissingMethodDeclarations extends MissingSources {

        /**
		 * serialization id
		 */
		private static final long serialVersionUID = 9214788084198236635L;
		private List<Method> nonMethodDeclarations;

        MissingMethodDeclarations(List<Method> nonMethodDeclarations) {
            this.nonMethodDeclarations = nonMethodDeclarations;
        }

        public List<Method> getNonMethodDeclarations() {
            return nonMethodDeclarations;
        }
    }

    /**
     * Collects all related methods and all references.
     * 
     * @return the problem report: {@link recoder.kit.Identity}(the name has
     *         not changed), {@link recoder.kit.Equivalence}, or
     *         {@link MissingMethodDeclarations}(some of related methods are
     *         not available as source code), or {@link IllegalName}.
     * @see recoder.kit.MethodKit#getAllRelatedMethods(CrossReferenceSourceInfo,
     *      Method)
     */
    public ProblemReport analyze() {
        if (newName.equals(methodToRename.getName())) {
            return setProblemReport(IDENTITY);
        }
        CrossReferenceSourceInfo xr = getCrossReferenceSourceInfo();

        methods = new ArrayList<MethodDeclaration>();
        List<Method> relatedMethods = MethodKit.getAllRelatedMethods(xr, methodToRename);
        List<Method> problems = null;
        for (int i = relatedMethods.size() - 1; i >= 0; i -= 1) {
            Method m = relatedMethods.get(i);
            if (m instanceof MethodDeclaration) {
                methods.add((MethodDeclaration) m);
            } else {
                if (problems == null) {
                    problems = new ArrayList<Method>();
                }
                problems.add(m);
            }
        }
        if (problems != null) {
            return setProblemReport(new MissingMethodDeclarations(problems));
        }
        refs = new ArrayList<MethodReference>();
        for (int j = methods.size() - 1; j >= 0; j -= 1) {
            MethodDeclaration mdecl = methods.get(j);
            List<MemberReference> mrefs = xr.getReferences(mdecl);
            for (int i = 0, s = mrefs.size(); i < s; i += 1) {
                MemberReference mr = mrefs.get(i);
                if (mr instanceof MethodReference) {
                    refs.add((MethodReference) mr);
                }
            }
        }
        return setProblemReport(EQUIVALENCE);
    }

    /**
     * Locally renames the method declaration and all method references
     * collected in the analyzation phase.
     * 
     * @exception IllegalStateException
     *                if the analyzation has not been called.
     * @see #analyze()
     */
    public void transform() {
        super.transform();
        ProgramFactory pf = getProgramFactory();
        for (int i = methods.size() - 1; i >= 0; i -= 1) {
            replace(methods.get(i).getIdentifier(), pf.createIdentifier(newName));
        }
        for (int i = refs.size() - 1; i >= 0; i -= 1) {
            replace(refs.get(i).getIdentifier(), pf.createIdentifier(newName));
        }
    }

    /**
     * Returns a list of method declaration that are to be renamed (valid after
     * analyze has been called), or that have been renamed (after transform has
     * been called).
     */
    public List<MethodDeclaration> getRenamedMethods() {
        return methods;
    }
}