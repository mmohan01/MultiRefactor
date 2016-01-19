/**
 * 
 */
package recoder.testsuite.exhaustive;

import java.io.File;
import java.util.List;

import recoder.ParserException;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.reference.MethodReference;
import recoder.kit.UnitKit;
import recoder.service.DefaultErrorHandler;
import recoder.service.SemanticsChecker;
import recoder.service.TypingException;
import recoder.service.UnresolvedReferenceException;
import recoder.testsuite.RecoderTestCase;
import recoder.util.ProgressEvent;
import recoder.util.ProgressListener;

/**
 * @author Tobias Gutzmann
 *
 */
public class ReadJDKSources extends RecoderTestCase {

    public void testReadJDKSrc() throws ParserException {
        String path =  System.getProperty("java.home");
        path = path.substring(0, path.length() - 3); // should remove "jre"...
        path
         = path + "src.zip";
        if (!new File(path).exists()) {
            fail("src.zip not found. Test case needs to be run on a jre delivered with JDK. Also so far tested under Windows only.");
        }
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, path);
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        sc.getProjectSettings().ensureExtensionClassesAreInPath();

        ProgressListener pl = new ProgressListener(){
            int x = 0;
            public void workProgressed(ProgressEvent pe) {
                if (x % 50 == 0)
                    System.out.print(".");
                if (++x == 3000) {
                    System.out.println();
                    x = 0;
                }
            }
        };

        sc.getProjectSettings().setErrorHandler(new DefaultErrorHandler(){
            @Override
             public void reportError(Exception e) {
                if (e.toString().indexOf("com/sun/java/swing/plaf/gtk/") != -1) {
                    return;
                } // ignore
                if (
                e instanceof UnresolvedReferenceException) {
                    UnresolvedReferenceException ue = (UnresolvedReferenceException)e;
                    ProgramElement pe = ue.getUnresolvedReference();
                    if (pe instanceof MethodReference) {
                        if (recoder.convenience.Format.toString(pe).startsWith("\"constr.getAnnotation(propertyNamesClass).value()")) {
                            // bug in javac - recoder is off the hook here!
                            return;
                        }
                    }
                } else if (e instanceof TypingException) {
                    TypingException te = (TypingException)e;
                    if (te.getUntypableExpression().toSource().trim().equals("constructor.getAnnotation(ConstructorProperties.class)")
                            && UnitKit.getCompilationUnit(te.getUntypableExpression()).getName().endsWith("java/beans/MetaData.java")) {
                        // bug in javac - recoder is off the hook once again!
                        return;
                    }
                }
                super.reportError(e);
            }
        });

        sc.getSourceFileRepository().addProgressListener(pl);
        sc.getSourceInfo().addProgressListener(pl);

        System.out.println("Start parsing...");
        long start = System.currentTimeMillis();
        List<CompilationUnit> cus = sc.getSourceFileRepository().getAllCompilationUnitsFromPath();
        System.out.println("\nparsed " + cus.size() + " CUs in " +
                (((System.currentTimeMillis() - start) / 1000) + " seconds"));
        start = System.currentTimeMillis();
        System.out.println("Updating model...");
        start = System.currentTimeMillis();
        sc.getChangeHistory().updateModel();
        System.out.println("\nbuilt model in " +
                (((System.currentTimeMillis() - start) / 1000) + " seconds"));
        start = System.currentTimeMillis();
        for (CompilationUnit cu: cus)
            cu.validateAll();
        System.out.println("\nvalidated in " +
                (((System.currentTimeMillis() - start) / 1000) + " seconds"));
        start = System.currentTimeMillis();
        new SemanticsChecker(sc).checkAllCompilationUnits();
        System.out.println("\nsemantic checks performed in " +
                (((System.currentTimeMillis() - start) / 1000) + " seconds"));

        assertEquals("Project should be read error-free.", 0, sc.getProjectSettings().getErrorHandler().getErrorCount());
    }

    public void testReadJDKSrc_7() throws ParserException {
        // TODO temp only for development!
        String path =  "c:/program files (x86)/java/jdk1.7.0_07/src.zip";
        if (!new File(path).exists()) {
            fail("src.zip not found. Test case needs to be run on a jre delivered with JDK. Also so far tested under Windows only.");
        }
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, path +
                ";C:/Program Files (x86)/Java/jdk1.7.0_07/jre/lib/*.jar");

        ProgressListener pl = new ProgressListener(){
            int x = 0;
            public void workProgressed(ProgressEvent pe) {
                if (x % 50 == 0)
                    System.out.print(".");
                if (++x == 3000) {
                    System.out.println();
                    x = 0;
                }
            }
        };

        sc.getProjectSettings().setErrorHandler(new DefaultErrorHandler(){
            @Override
             public void reportError(Exception e) {
                if (e instanceof UnresolvedReferenceException) {
                    UnresolvedReferenceException ue = (UnresolvedReferenceException)e;
                    ProgramElement pe = ue.getUnresolvedReference();
                    if (pe instanceof MethodReference) {
                        if (recoder.convenience.Format.toString(pe).startsWith("\"constr.getAnnotation(propertyNamesClass).value()")) {
                            // bug in javac - recoder is of the hook here!
                            return;
                        }
                    }
                }
                super.reportError(e);
            }
        });

        sc.getSourceFileRepository().addProgressListener(pl);
        sc.getSourceInfo().addProgressListener(pl);

        System.out.println("Start parsing...");
        long start = System.currentTimeMillis();
        List<CompilationUnit> cus = sc.getSourceFileRepository().getAllCompilationUnitsFromPath();
        System.out.println("\nparsed " + cus.size() + " CUs in " +
                (((System.currentTimeMillis() - start) / 1000) + " seconds"));
        start = System.currentTimeMillis();
        System.out.println("Updating model...");
        start = System.currentTimeMillis();
        sc.getChangeHistory().updateModel();
        System.out.println("\nbuilt model in " +
                (((System.currentTimeMillis() - start) / 1000) + " seconds"));
        start = System.currentTimeMillis();
        for (CompilationUnit cu: cus)
            cu.validateAll();
        System.out.println("\nvalidated in " +
                (((System.currentTimeMillis() - start) / 1000) + " seconds"));
        start = System.currentTimeMillis();
        new SemanticsChecker(sc).checkAllCompilationUnits();
        System.out.println("\nsemantic checks performed in " +
                (((System.currentTimeMillis() - start) / 1000) + " seconds"));
    }
}
