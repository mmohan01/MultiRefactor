/**
 * 
 */
package recoder.testsuite.semantics;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import junit.framework.TestCase;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ModelException;
import recoder.ParserException;
import recoder.java.CompilationUnit;
import recoder.service.DefaultErrorHandler;
import recoder.service.ErrorHandler;
import recoder.service.SemanticsChecker;
import recoder.service.TypingException;


/**
 * @author Tobias Gutzmann, Ya Liu
 *
 */
public class SemanticsTest extends TestCase {
    // TODO evil copy & paste from FixedBugs, with some adaptions...

    ////////////////////////////////////////////////////////////
    // helper methods / classes
    ////////////////////////////////////////////////////////////
    private CrossReferenceServiceConfiguration sc;
    private List<CompilationUnit> runIt(String ... cuTexts) throws ParserException {
        return runIt(null, cuTexts);
    }

    private List<CompilationUnit> runIt(ErrorHandler eh, String ... cuTexts) throws ParserException {
        sc = new CrossReferenceServiceConfiguration();
        sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        ArrayList<CompilationUnit> cus = new ArrayList<CompilationUnit>();
        for (String cuText: cuTexts) {
            CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
            sc.getChangeHistory().attached(cu);
            cus.add(cu);
        }
        if (eh != null)
            sc.getProjectSettings().setErrorHandler(eh);
        sc.getChangeHistory().updateModel();
        for (CompilationUnit cu: cus)
            cu.validateAll();
        return cus;
    }

    private static class ThrowingErrorHandler extends DefaultErrorHandler {
        @Override
         public void reportError(Exception e) {
            throw (ModelException)e;
        }
    }

    private static class SilentErrorHandler extends DefaultErrorHandler {
        private final int exp;
        private int errCnt = 0;
        SilentErrorHandler(int cnt) {
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

    ////////////////////////////////////////////////////////////
    // The actual test cases
    ////////////////////////////////////////////////////////////


    private void testRunOK(String cuText) {
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        new SemanticsChecker(sc).check(cu);
    }

    private void testRunFail(String cuText) {
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (ModelException e) {
            // as expected!!
    }
    }

    public void testMethodInvocation() {
        MethodTest mt = new MethodTest();

        //test static
        testRunFail(mt.testThisInStaticMethod());
        testRunFail(mt.testStaticMethodInvocation2());
//		testRunOK(mt.testStaticMethodInvocationOK3());
//		testRunOK(mt.testStaticMethodInvocationOK4());
//		testRunFail(mt.testStaticMethodInvocation5());
//		testRunFail(mt.testStaticMethodInvocation6());
//		testRunOK(mt.testStaticMethodInvocationOK7());
//		testRunOK(mt.testStaticMethodInvocationOK8());
//		testRunOK(mt.testStaticMethodInvocationOK9());
//		testRunOK(mt.testStaticMethodInvocationOK10());
//		testRunOK(mt.testStaticMethodInvocationOK11());
//
//		//test abstract method invocation
//		testRunFail(mt.testAbstractMethodInvocation1());
//		testRunOK(mt.testAbstractMethodInvocationOK2());
//		testRunOK(mt.testAbstractMethodInvocationOK3());
//		testRunOK(mt.testAbstractMethodInvocationOK4());
//		testRunFail(mt.testAbstractMethodInvocation5());
//		testRunOK(mt.testAbstractMethodInvocationOK6());
//
//		//test super
//		testRunOK(mt.testSuperInConstructorOK1());
//		testRunOK(mt.testSuperInConstructorOK2());
//		testRunFail(mt.testSuperMethodInvocation3());
//		testRunOK(mt.testSuperMethodInvocationOK4());
//		testRunFail(mt.testSuperMethodInvocation5());
//		testRunOK(mt.testSuperMethodInvocation6());
//		testRunOK(mt.testSuperMethodInvocationOK7());
//
//		//test this
//		testRunOK(mt.testThisInConstructorOK1());
//		testRunFail(mt.testThisMethodInvocation2());
//		testRunFail(mt.testThisMethodInvocation3());
//
//		testRunOK(mt.testMethodInvocationOK27());
//
//		//test return
//		testRunFail(mt.testReturnMethodInvocation1());
//		testRunFail(mt.testReturnMethodInvocation2());
//		testRunOK(mt.testReturnMethodInvocationOK3());
    }

//	public void testTypeCast(){
//		TypeTest tt = new TypeTest();
//
//		//error cases
//		testRunFail(tt.FloatConversion());//TODO fail
//		testRunFail(tt.ByteConversion());
//		testRunFail(tt.CharConversion());
//		testRunFail(tt.IntConversion());
//		testRunFail(tt.LongConversion());
//		testRunFail(tt.ShortConversion());
//		testRunFail(tt.StringConversion());
//		testRunFail(tt.ClassTypeMismatch1());//TODO fail
//		testRunFail(tt.ClassTypeMismatch2());//TODO fail
//		testRunFail(tt.ClassTypeMismatch3());//TODO fail
//		testRunFail(tt.ClassTypeMismatch4());
//		testRunFail(tt.ClassTypeMismatch5());
//
//		//correct cases
//		testRunOK(tt.FloatConversionC());
//		testRunOK(tt.FloatConversion());
//		testRunOK(tt.ByteConversion());
//		testRunOK(tt.CharConversion());
//		testRunOK(tt.IntConversion());
//		testRunOK(tt.LongConversion());
//		testRunOK(tt.ShortConversion());
//		testRunOK(tt.StringConversion());
//	}

    public void testName() {
        NameTest nt = new NameTest();


//		//test ambiguous
//		testRunFail(nt.testAmbiguousName1());//TODO Fail
//		testRunFail(nt.testAmbiguousName2());//TODO Fail
//		testRunFail(nt.testAmbiguousName9());//TODO Fail
//
//
//		testRunFail(nt.testAbstractClass3());//TODO Fail
//		testRunOK(nt.testAbstractClassOK32());
//		testRunFail(nt.testAbstractClass33());

        testRunOK(nt.testFieldOK34());
        testRunOK(nt.testFieldOK35());
        testRunFail(nt.testField36());

//		//test access modifier
        testRunFail(nt.testAccessModifier4());//TODO Fail
        testRunFail(
        nt.testAccessModifier6());//TODO Fail
        testRunFail(
        nt.testAccessModifier7());//TODO Fail
        testRunFail(
        nt.testMethodModifier12());//Fail  ---fixed now
        testRunFail(
        nt.testMethodModifier13());
        testRunOK(nt.testMethodModifierOK13());
        testRunFail(nt.testMethodModifier14());
        testRunFail(nt.testMethodModifier15());
        testRunFail(nt.testMethodModifier16());//Fail	--fixed now
        testRunOK(
        nt.testMethodModifierOK16());
        testRunFail(nt.testMethodModifier17());
        testRunFail(nt.testMethodModifier18());
        testRunFail(nt.testMethodModifier19());
        testRunFail(nt.testMethodModifier20());
        testRunFail(nt.testMethodModifier21());
        testRunFail(nt.testMethodModifier22());
        testRunFail(nt.testMethodModifier23());
//
//		//test inherited
//		testRunFail(nt.testClassInherited5());//TODO Fail
//
//		//test field reference
//		testRunFail(nt.testFieldReference8());//TODO Fail
//		testRunOK(nt.testFieldReferenceOK8());
//		testRunFail(nt.testFieldReferenceName21());//TODO Fail
//		testRunFail(nt.testFieldReferenceName23());//TODO Fail
//
//		//test duplicated name
//		testRunFail(nt.testDuplicateName10());//TODO Fail
//		testRunFail(nt.testDuplicateName11());//TODO Fail
//		testRunFail(nt.testDuplicateName22());//TODO Fail
//		testRunFail(nt.testDuplicateName37());//TODO Fail
//
//		//test override name
//		testRunFail(nt.testOverrideName14());//TODO Fail
//		testRunFail(nt.testOverrideName15());//TODO Fail
//		testRunOK(nt.testOverrideNameOK15());
//		testRunFail(nt.testOverrideName17());//TODO Fail
//		testRunFail(nt.testOverrideName18());//TODO Fail
//
//		//test interface name
//		testRunFail(nt.testInterfaceName19());//TODO Fail
//		testRunFail(nt.testInterfaceName20());//TODO Fail
//		testRunOK(nt.testInterfaceNameOK20());
//
//		testRunFail(nt.testName24());//TODO Fail
//		testRunFail(nt.testName25());//TODO Fail
//
//		//test return name
//		testRunFail(nt.testReturnName26());//TODO Fail
//		testRunFail(nt.testReturnName27());//TODO Fail
//		testRunFail(nt.testReturnName28());//TODO Fail
//
//		//test static initializer
//		testRunFail(nt.testStaticInitializer29());
//		testRunFail(nt.testStaticInitializer30());
//
//		//test this in constructor
//		testRunFail(nt.testThisInConstructor31());//TODO Fail
//		testRunOK(nt.testThisInConstructorOK31());
    }


    public void testSubclass1() {
//final class can't have subclass
        String cuText =
            "class Point { int x, y; }" +
            "final class WhitePoint extends Point  { int white; }" +
            "class ColorPoint extends WhitePoint { int black; }//error! \n";

            //TODO
            //fail();
        testRunOK(cuText);
    }

    public void testSubclassOK1() {
        String cuText =
            "class Point { int x, y; }" +
            "class WhitePoint extends Point  { int white; }" +
            "class ColorPoint extends WhitePoint { int black; }//correct! \n";
        testRunOK(cuText);
    }

    public void testClassMethodMatch1() {
//method declaration doesn't match super class's
        String cuText =
            "class animal{" +
            "\u0009int x = 0, y = 0 , age;" +
            "\u0009void grow(int dx, int dy){ x += dx;y += dy;}" +
            "\u0009int getX() {return x;}" +
            "\u0009int getY() {return y;}" +
            "}" +
            "class ant extends animal{ " +
            "\u0009float x = 0.0f , y = 0.0f;" +
            "\u0009void grow (int dx, int dy){grow((float)dx,(float)dy);}" +
            "\u0009void grow(float dx,float dy){x+= dx; y+= dy;}" +
            "\u0009float getX(){return x;}" +
            "\u0009float getY(){return y;}" +
            "}";
            //TODO
            //fail();
            testRunOK(cuText);
    }


    public void testRawInnerTypes1() {
        String cuText =
            "class Outer<T>{" +
            "\u0009class Inner<S> {" +
            "\u0009\u0009S s;" +
            "\u0009}" +
            "}" +
            "" +
            "class A {" +
            "\u0009Outer.Inner<Double> x = null; // error!\n" +
            "}";
        testRunFail(cuText);
    }

    public void testRawOuterTypes1() {
        String cuText =
            "class Outer<T>{" +
            "\u0009class Inner<S> {" +
            "\u0009\u0009S s;" +
            "\u0009}" +
            "}" +
            "" +
            "class A {" +
            "\u0009Outer<String>.Inner x = null; // error!\n" +
            "}";
        testRunFail(cuText);
    }

    public void testRawOKTypes1() {
        String cuText =
            "class Outer<T>{" +
            "\u0009class Inner<S> {" +
            "\u0009\u0009S s;" +
            "\u0009}" +
            "}" +
            "" +
            "class A {" +
            "   Outer<Double>.Inner<Double> y = null; // ok!\n" +
            "}";

        testRunOK(cuText);
    }

    public void testRawOKTypes2() {
        String cuText =
            "class A {" +
              "class B<T> { " +
                "void foo() {" +
                    " A.B<String> ab = new A.B<String>();" +
                "}" +
              "}" +
            "}";

        testRunOK(cuText);
    }

    public void testIf() {
        String cuText =
            "class A {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009if (new Object()) { }" +
            "\u0009}\n" +
            "}\n";
        testRunFail(cuText);
    }

    public void testEnhancedFor() {
        String cuText =
            "class A {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009for (String s: new Object[3]) {\n" +
            "\u0009\u0009}" +
            "\u0009}\n" +
            "}\n";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (TypingException te) {
            // as expected!!
    }
        cuText =
            "class A {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009for (String s: new String[3]) {\n" +
            "\u0009\u0009}" +
            "\u0009}\n" +
            "}\n";
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        new SemanticsChecker(sc).check(cu);
        cuText =
            "class A {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009for (Object o: new String[3]) {\n" +
            "\u0009\u0009}" +
            "\u0009}\n" +
            "}\n";
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }

        // now test collection types!
        cuText =
            "class A {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009for (String s: new java.util.ArrayList<String>()) {\n" +
            "\u0009\u0009}" +
            "\u0009}\n" +
            "}\n";
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        new SemanticsChecker(sc).check(cu);
        cuText =
            "class A {\n" +
            "\u0009static final <E> void foo(java.util.Collection<E> c) {\n" +
            "\u0009\u0009java.util.List<E> list = new java.util.ArrayList<E>();\n" +
            "\u0009\u0009for (E e : c) {}\n" +
            "\u0009}\n" +
            "}\n";
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        new SemanticsChecker(sc).check(cu);
    }
}
