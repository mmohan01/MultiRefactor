package recoder.testsuite.fixedbugs;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.ClassType;
import recoder.abstraction.Method;
import recoder.bytecode.MethodInfo;
import recoder.java.CompilationUnit;
import recoder.java.declaration.ClassDeclaration;
import recoder.service.TooManyErrorsException;
import recoder.testsuite.RecoderTestCase;

/**
 * This class contains test cases for known, yet not solved bugs.
 * @author Tobias Gutzmann
 *
 */
public class UnfixedBugs extends RecoderTestCase {

    public void testXXX() throws Exception {
        // TODO This is invalid source code. Write a bug report and fix it.
        String cuText =
            "package p1;\n" +
            "class A {\n" +
            "  protected int f;\n" +
            "}\n";
        String cuText2 =
            "package p2;\n" +
            "class B extends p1.A{\n" +
            "  void foo() { new p1.A().f = 3; }\n" +
            "}\n";
        try {
            runIt(cuText, cuText2);
            fail("This code should not be accepted!");
        } finally {}
    }


    public void testLocalTypesReadFromBytecode() throws Exception {
        // TODO fix...
        sc = new CrossReferenceServiceConfiguration();
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        ClassType coll = sc.getNameInfo().getClassType("java.util.Collections");
        for (Method m: coll.getMethods()) {
            if (m.getName().equals("enumeration")) {
                MethodInfo mi = (MethodInfo)m;
                ClassType inner = mi.getTypes().get(0);
            }
        }
    }


    public void testBug3450561() throws Exception {
        // Bad comment assignment.
        String cuText = "class Jtar {\n" +
         "public Jtar()\n" +
         "/***********************************************/\n" +
         "/** Function name: Jtar */\n" +
         "/**Purpose: Constructor for the Jtar class*/\n" +
         "/** Parameters : */\n" +
         "/**Returns : */\n" +
         "/**********************************************/\n" +
         "{\n" +
         "}\n" +
         "}\n";

        CompilationUnit cu = runIt(cuText).get(0);

        assertEquals(cuText.replaceAll(" ", ""), cu.toSource().replaceAll(" ", ""));
    }

    public void testBug3496290() throws Exception {
        // This is invalid source code.
        String cuText =
            "package p1;\n" +
            "class A {\n" +
            "  protected int f;\n" +
            "}\n";
        String cuText2 =
            "package p2;\n" +
            "class B extends p1.A{\n" +
            "  void foo() { new p1.A().f = 3; }\n" +
            "}\n";
        try {
            runIt(cuText, cuText2);
            fail("This code should not be accepted!");
        } finally {}
    }

    public void testBug3496290_2() throws Exception {
        // The following is incorrect code according to §8.8.9 of JLS, third edition.
        // The class is visible, but the constructor is not as it is protected and in a different package.
        String cuText = "package p1;\n" +
            "public class A {\n" +
            " \u0009protected class I{}\n" +
            "}\n" +
            "\n";
        String cuText2 = "package p2;\n" +
            "class B extends p1.A {\n" +
            "\u0009void foo() {\n" +
            " \u0009\u0009new I();\n" +  // not visible!!
            "\u0009}\n" +
            "}";

        try {
            runIt(cuText, cuText2);
            fail("This code should not be accepted!");
        } finally {}
    }


    public final void testBasic3() throws Exception {
        // TODO fix and move back to tools.TestInstrumentation
        // not working in 0.95 yet...
        setPath("test/tools/Instrumentalize/3");
        runIt();
        new application.Instrumentalize(sc).execute();
        String exp =
            "publicclassNested{intfoo(Objecto){return-1;}Stringbar()" +
            "{returnnull;}voidfoobar()" +
            "{if(true){System.out.println(\"CalltoNested.barfromNested\");" +
            "Stringstring=bar();System.out.println(\"CalltoNested.foofromNested\");" +
            "foo(string);}}intf;{System.out.println(\"CalltoNested.barfromNested\");" +
            "Stringstr=bar();System.out.println(\"CalltoNested.foofromNested\");" +
            "f=foo(str);}}";
        assertEquals(exp, ((ClassDeclaration)sc.getNameInfo().getType("Nested")).toSource().replaceAll("( |\n)", ""));
        //System.out.println(((ClassDeclaration)sc.getNameInfo().getType("Nested")).toSource());
    }

    public void testGenericMethodInvocation() throws Exception {
        String cuText =
         "class X {\n" +
         "\u0009<T extends Number> T foo(T t) { }\n" +
         "\u0009{ String s = this.<String>foo(3); }\n" +
         "}\n";
        try {
            runIt(cuText);
            fail("Exception expected!");
        } catch (TooManyErrorsException e) {
            // expected
    }
    }
}
