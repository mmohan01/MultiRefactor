/*
 * Created on 18.04.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 */
package recoder.testsuite;

import java.io.IOException;

import junit.framework.TestSuite;
import recoder.testsuite.basic.BasicTestsSuite;
import recoder.testsuite.completeCoverage.CompleteCoverage;
import recoder.testsuite.fixedbugs.FixedBugs;
import recoder.testsuite.fixedbugs.UnfixedBugs;
import recoder.testsuite.java5test.ExtendedBytecodeTest;
import recoder.testsuite.java5test.Java5Test;
import recoder.testsuite.java7test.Java7Test;
import recoder.testsuite.newFeatures.NameInfoPatternMatcher;
import recoder.testsuite.newFeatures.SmallFeatures;
import recoder.testsuite.parser.ParserTest;
import recoder.testsuite.semantics.ASTChecks;
import recoder.testsuite.semantics.NewSemanticsChecks;
import recoder.testsuite.semantics.SemanticsChecks;
import recoder.testsuite.semantics.SemanticsTest;
import recoder.testsuite.tools.TestInstrumentalize;
import recoder.testsuite.transformation.TransformationTests;

/**
 * @author gutzmann
 *
 */
public class CompleteTestSuite extends TestSuite {

    /**
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws IOException
     * 
     */
    public CompleteTestSuite() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        super();
        addTest(BasicTestsSuite.suite());
        addTestSuite(FixedBugs.class);
        addTestSuite(TransformationTests.class);
        addTestSuite(Java5Test.class);
        addTestSuite(ExtendedBytecodeTest.class);
        addTest(CompleteCoverage.suite());
        addTestSuite(NameInfoPatternMatcher.class);
        addTestSuite(SmallFeatures.class);
        addTestSuite(ASTChecks.class);
        addTestSuite(SemanticsChecks.class);
        addTestSuite(SemanticsTest.class);
        addTestSuite(NewSemanticsChecks.class);
        addTestSuite(TestInstrumentalize.class);
        addTestSuite(ParserTest.class);
        addTestSuite(Java7Test.class);
        addTestSuite(UnfixedBugs.class);
    }

    public static TestSuite suite() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return new CompleteTestSuite();
    }
}
