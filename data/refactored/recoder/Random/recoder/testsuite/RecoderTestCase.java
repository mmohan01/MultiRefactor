/**
 * 
 */
package recoder.testsuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ModelException;
import recoder.ParserException;
import recoder.convenience.ForestWalker;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.SourceElement.Position;
import recoder.service.DefaultErrorHandler;
import recoder.service.ErrorHandler;
import recoder.service.SemanticsChecker;

/**
 * @author Tobias Gutzmann
 *
 */
public class RecoderTestCase extends TestCase {
    static {
        // lot's of assertions in our code...
        RecoderTestCase.class.getClassLoader().setDefaultAssertionStatus(true);
    }

    /**
	 * 
	 */
    public RecoderTestCase() {
        // constructor from super class...
    }

    /**
	 * @param name
	 */
    public RecoderTestCase(String name) {
        super(name);
        // constructor from super class...
    }

    ////////////////////////////////////////////////////////////
    // helper methods / classes
    ////////////////////////////////////////////////////////////
    protected CrossReferenceServiceConfiguration sc;
    protected final List<CompilationUnit> runIt(String ... cuTexts) {
        return runIt(null, cuTexts);
    }

    public void setUp() throws Exception {
        sc = new CrossReferenceServiceConfiguration();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        sc = null; // test-instance remains in memory, but we want to free the object!
    }

    protected void checkParentLinks(ProgramElement ... pes) {
        List<ProgramElement> lPes = Arrays.asList(pes);
        ForestWalker fw = new ForestWalker(lPes);
        while (fw.next()) {
            ProgramElement pe = fw.getProgramElement();
            if (!lPes.contains(pe)) {
                assertNotNull(pe.getASTParent());
                assertTrue(pe.getASTParent().getIndexOfChild(pe) != -1);
            }
        }
    }

    protected List<CompilationUnit> runIt(ErrorHandler eh, String ... cuTexts) {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        ArrayList<CompilationUnit> cus = new ArrayList<CompilationUnit>();
        for (String cuText: cuTexts) {
            CompilationUnit cu;
            try {
                cu = sc.getProgramFactory().parseCompilationUnit(cuText);
            } catch (ParserException e) {
                fail(e.toString());
                return null; // won't happen.
            }
            sc.getChangeHistory().attached(cu);
            cus.add(cu);
        }
        if (eh != null)
            sc.getProjectSettings().setErrorHandler(eh);
        sc.getChangeHistory().updateModel();
        for (CompilationUnit cu: cus)
            cu.validateAll();
        ForestWalker fw = new ForestWalker(cus);
        while (fw.next()) {
            ProgramElement pe = fw.getProgramElement();
            assertTrue("Start position of element type " + pe.getClass().getName() + " undefined", pe.getStartPosition() != Position.UNDEFINED);
            assertTrue("End position of element type " + pe.getClass().getName() + " undefined", pe.getEndPosition() != Position.UNDEFINED);
        }
        int errCnt = sc.getProjectSettings().getErrorHandler().getErrorCount();
        new SemanticsChecker(sc).checkAllCompilationUnits();
        assertEquals("SemanticsChecker found errors", errCnt, sc.getProjectSettings().getErrorHandler().getErrorCount());
        return cus;
    }

    private boolean writeBackAllowed;


    protected void setPath(String input) {
        setPath(input, null);
    }

    protected void setPath(String input, String output) {
        StringTokenizer st = new StringTokenizer(input, ";:");
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (!new File(s).exists())
                throw new RuntimeException(s + " not found");
        }
        writeBackAllowed = false;
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH,
                new File(input).getAbsolutePath());
        if (output != null) {
            if (input.equals(output))
                fail("Must not overwrite source files for test cases (input path is output path!");
            writeBackAllowed = true;
            sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH,
                    new File(output).getAbsolutePath());
        }
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
    }

    protected void runIt() {
        try {
            sc.getSourceFileRepository().getAllCompilationUnitsFromPath();
            sc.getChangeHistory().updateModel();
            for (CompilationUnit cu: sc.getSourceFileRepository().getCompilationUnits()) {
                cu.validateAll();
            }
            new SemanticsChecker(sc).checkAllCompilationUnits();
        } catch (ParserException pe) {
            System.err.println(pe.getMessage());
            fail("unexpected ParserException");
        }
    }


    protected void writeBack() {
        if (!writeBackAllowed)
            fail("Cannot write back to original data location for test cases!");
        try {
            sc.getSourceFileRepository().printAll(true);
        } catch (IOException e) {
            fail();
        }
    }

    public static class SilentErrorHandler extends DefaultErrorHandler {
        private final int exp;
        private int errCnt = 0;
        public SilentErrorHandler(int cnt) {
            exp = cnt;
        }
        @Override public void reportError(Exception e) {
            errCnt++;
        }
        @Override public void modelUpdated(EventObject event) {
            isUpdating = false;
            assertEquals(exp, errCnt);
        }
    }

    public static class ThrowingErrorHandler extends DefaultErrorHandler {
        @Override
         public void reportError(Exception e) {
            throw (ModelException)e;
        }
    }

    public static class IgnoringErrorHandler extends DefaultErrorHandler {
        private final List<Class<? extends  Exception>> ignore;
        public IgnoringErrorHandler(Class<? extends  Exception> ... ignore) {
            this.ignore = Arrays.asList(ignore);
        }
        @Override
         public void reportError(Exception e) {
            if (ignore.contains(e.getClass()))
                return;
            super.reportError(e);
        }
    }
}
