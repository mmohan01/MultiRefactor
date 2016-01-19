package recoder.testsuite.basic.analysis;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import recoder.ParserException;
import recoder.abstraction.Constructor;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.Type;
import recoder.abstraction.Variable;
import recoder.convenience.Format;
import recoder.convenience.TreeWalker;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.Reference;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.UncollatedReferenceQualifier;
import recoder.java.reference.VariableReference;
import recoder.service.CrossReferenceSourceInfo;

/**
 * This test checks if all references in all compilation units are resolved and
 * contained in the reference lists of their corresponding target.
 */
public class ReferenceCompletenessTest extends TestBase {
    private String makeReferenceError(Reference r, ProgramModelElement x) {
        return Format.toString("%c %N @%p in %f", r) + " was not found in reference list of " + Format.toString("%c %N", x);
    }

    public void testReferenceCompleteness() {
        CrossReferenceSourceInfo xrsi = config.getCrossReferenceSourceInfo();
        SourceFileRepository sfr = config.getSourceFileRepository();

        List<CompilationUnit> units = sfr.getCompilationUnits();
        for (int i = 0; i < units.size(); i += 1) {
            CompilationUnit u = units.get(i);
            TreeWalker tw = new TreeWalker(u);
            while (tw.next()) {
                ProgramElement pe = tw.getProgramElement();
                if (pe instanceof Reference) {
                    Assert.assertTrue("Uncollated reference detected", !(pe instanceof UncollatedReferenceQualifier));
                    if (pe instanceof VariableReference) {
                        VariableReference r = (VariableReference) pe;
                        Variable x = xrsi.getVariable(r);
                        List<? extends  VariableReference> list = xrsi.getReferences(x);
                        if (list.indexOf(r) < 0) {
                            Assert.fail(makeReferenceError(r, x));
                        }
                    } else if (pe instanceof TypeReference) {
                        TypeReference r = (TypeReference) pe;
                        Type x = xrsi.getType(r);
                        // void type check
                        if (
                        x != null) {
                            List<TypeReference> list = xrsi.getReferences(x);
                            if (list.indexOf(r) < 0) {
                                Assert.fail(makeReferenceError(r, x));
                            }
                        }
                    } else if (pe instanceof MethodReference) {
                        MethodReference r = (MethodReference) pe;
                        Method x = xrsi.getMethod(r);
                        List<? extends  MemberReference> list = xrsi.getReferences(x);
                        if (list.indexOf(r) < 0) {
                            Assert.fail(makeReferenceError(r, x));
                        }
                    } else if (pe instanceof ConstructorReference) {
                        ConstructorReference r = (ConstructorReference) pe;
                        Constructor x = xrsi.getConstructor(r);
                        List<ConstructorReference> list = xrsi.getReferences(x);
                        if (list.indexOf(r) < 0) {
                            Assert.fail(makeReferenceError(r, x));
                        }
                    } else if (pe instanceof PackageReference) {
                        PackageReference r = (PackageReference) pe;
                        Package x = xrsi.getPackage(r);
                        List<PackageReference> list = xrsi.getReferences(x);
                        if (list.indexOf(r) < 0) {
                            Assert.fail(makeReferenceError(r, x));
                        }
                    }
                }
            }
        }
    }
}
