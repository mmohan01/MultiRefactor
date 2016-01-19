//This file is part of the RECODER library and protected by the LGPL.

package recoder.testsuite.basic;

import java.io.IOException;

import junit.framework.TestSuite;
import recoder.testsuite.basic.analysis.AnalysisReportTest;
import recoder.testsuite.basic.analysis.GetAllRelatedMethodsTest;
import recoder.testsuite.basic.analysis.LocalVariableXReferenceCompletenessTest;
import recoder.testsuite.basic.analysis.MemberXReferenceCompletenessTest;
import recoder.testsuite.basic.analysis.ModelRebuildTest;
import recoder.testsuite.basic.analysis.PackageXReferenceCompletenessTest;
import recoder.testsuite.basic.analysis.TestBase;
import recoder.testsuite.basic.analysis.ReferenceCompletenessTest;
import recoder.testsuite.basic.analysis.TypeXReferenceCompletenessTest;
import recoder.testsuite.basic.syntax.CloneTest;
import recoder.testsuite.basic.syntax.ParserPrinterTest;
import recoder.testsuite.basic.syntax.WalkPositionTest;

/**
 * Call example: java test.TransformationTests standard.tst collections.prj
 */

public class BasicTestsSuite extends TestSuite {

    protected static BasicTestsSuite suite() throws IOException, ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        return new BasicTestsSuite();
    }

    public BasicTestsSuite() {
        addTestSuite(WalkPositionTest.class);
        addTestSuite(CloneTest.class);
        addTestSuite(ParserPrinterTest.class);
        addTestSuite(AnalysisReportTest.class);
        addTestSuite(ModelRebuildTest.class);
        addTestSuite(MemberXReferenceCompletenessTest.class);
        addTestSuite(PackageXReferenceCompletenessTest.class);
        addTestSuite(ReferenceCompletenessTest.class);
        addTestSuite(TypeXReferenceCompletenessTest.class);
        addTestSuite(LocalVariableXReferenceCompletenessTest.class);
        addTestSuite(GetAllRelatedMethodsTest.class);
    }

//    public BasicTestsSuite(String testConfig, String projectConfig) throws IOException, ClassNotFoundException,
//            IllegalAccessException, InstantiationException {
//        config = new CrossReferenceServiceConfiguration();
//        // should use a specialized error handler instead
//        // to check if errors are reported correctly
//        config.getProjectSettings().setErrorHandler(new DefaultErrorHandler(0));
//
//        projectFile = new File(projectConfig);
//        if (!projectFile.exists()) throw new FileNotFoundException("Project File not found!");
//
//        addTest(new ParseFilesTest("testParseFiles"));
//        addTest(new SetupModelTest("testSetupModel"));
//        BufferedReader reader = new BufferedReader(new FileReader(testConfig));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            line = line.trim();
//            if (line.length() > 0 && !line.startsWith("#")) {
//                Class<?> c = Class.forName(line);
//                if (!Test.class.isAssignableFrom(c)) {
//                    throw new IllegalArgumentException("Class to load is no TestCase");
//                }
//                addTestSuite(c);
//            }
//            // should parse arguments and pass them as String arguments?
//        }
//        reader.close();
//    }

//    public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
//            IllegalAccessException, IOException {
//        BasicTestsSuite suite = new BasicTestsSuite("test/basic/standard.tst", "test/basic/collections.prj");
//        TestResult result = new TestResult();
//        suite.run(result);
//        System.out.println("Number of errors: " + result.errorCount() + " / " + result.runCount());
//        System.out.println("Number of failures: " + result.failureCount() + " / " + result.runCount());
//    }
}
