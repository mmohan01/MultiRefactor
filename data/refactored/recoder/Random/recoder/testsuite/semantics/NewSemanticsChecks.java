package recoder.testsuite.semantics;

import java.io.File;
import java.io.FileFilter;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ModelException;
import recoder.service.DefaultErrorHandler;
import recoder.testsuite.RecoderTestCase;

public class NewSemanticsChecks extends RecoderTestCase {
    private static class ThrowingErrorHandler extends DefaultErrorHandler {
        @Override
         public void reportError(Exception e) {
            throw (ModelException)e;
        }
    }

    @Override
     protected void runIt() {
        sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
        super.runIt();
    }

    public void testValidPrograms() {
        runTests("test/semantics/valid", true);
    }
    public void testInvalidPrograms() {
        runTests("test/semantics/invalid", false);
    }
    private void runTests(String path, boolean validProgram) {
        boolean successfull = true;
        File f = new File(path);
        if (!f.isDirectory())
            throw new RuntimeException(); // ???
        File[] dirs = f.listFiles(new FileFilter(){
            @Override
             public boolean accept(File pathname) {
                return !(pathname.getName().endsWith(".svn")) && pathname.isDirectory();
            }
        });
        for (File dir: dirs) {
            try {
                sc = new CrossReferenceServiceConfiguration(); // resets everything.
                setPath(
                dir.getPath());
                runIt();
                if (!validProgram) {
                    System.err.println("Invalid program " + dir + " ran through without error.");
                    successfull = false;
                }
            } catch (ModelException e) {
                if (validProgram) {
                    System.err.println("Valid program " + dir + " failed due to " + e.toString());
                    successfull = false;
                }
                // else expected.
            }
        }
        if (!successfull)
            fail("There are programs that did not run through as expected; check stderr-output for details.");
    }
}
