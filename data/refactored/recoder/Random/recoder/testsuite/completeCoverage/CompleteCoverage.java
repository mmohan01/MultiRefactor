package recoder.testsuite.completeCoverage;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CompleteCoverage {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for recoder.testsuite.completeCoverage");
        //$JUnit-BEGIN$
        suite.
        addTestSuite(NameInfoCoverage.class);
        suite.addTestSuite(KitCoverage.class);
        //$JUnit-END$
        return suite;
    }
}
