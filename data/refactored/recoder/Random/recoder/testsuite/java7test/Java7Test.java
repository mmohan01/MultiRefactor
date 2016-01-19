/**
 * Created on 9 maj 2011
 */
package recoder.testsuite.java7test;

import java.io.StringReader;

import junit.framework.AssertionFailedError;
import recoder.ModelException;
import recoder.ParserException;
import recoder.abstraction.ClassType;
import recoder.abstraction.Type;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import recoder.java.JavaProgramFactory;
import recoder.java.expression.operator.New;
import recoder.parser.JavaCCParser;
import recoder.parser.JavaCCParserConstants;
import recoder.parser.ParseException;
import recoder.parser.Token;
import recoder.parser.TokenMgrError;
import recoder.testsuite.RecoderTestCase;

/**
 * @author Tobias Gutzmann
 *
 */
public class Java7Test extends RecoderTestCase {
    private void lexerLiteralOk(String s) throws ParseException {
        JavaCCParser parser = ((JavaProgramFactory)sc.getProgramFactory()).getParser();
        parser.initialize(new StringReader(s), (JavaProgramFactory)sc.getProgramFactory());
        parser.Literal();
        Token t = parser.getNextToken();
        assertEquals(JavaCCParserConstants.EOF, t.kind);
    }
    private void lexerLiteralFail(String s) {
        try {
            lexerLiteralOk(s);
            fail("Literal " + s + " should fail parsing.");
        } catch (AssertionFailedError e) {
            // ok!
    } catch (ParserException e) {
            // ok!
    } catch (TokenMgrError e) {
            // ok!
    }
    }

    public void testOkCases() throws Exception {
        lexerLiteralOk("0b00001101");
        lexerLiteralOk("0b0_1_1__0");
        lexerLiteralOk("2_2");
        lexerLiteralOk("5__2__3");
        lexerLiteralOk("0x2_2");
        lexerLiteralOk("0x5___1_1");
        lexerLiteralOk("05312");
        lexerLiteralOk("0_22"); // ok according to http://mail.openjdk.java.net/pipermail/coin-dev/2009-April/001628.html
        lexerLiteralOk(
        "05__3112");
        lexerLiteralOk("1.2__3");
        lexerLiteralOk("0x1_23.p7");
    }

    public void testFaultyCases() throws Exception {
        lexerLiteralFail("0b0121");
        lexerLiteralFail("_22"); // it's an identifier, not a literal
        lexerLiteralFail(
        "123_");
        lexerLiteralFail("0b_1");
        lexerLiteralFail("0b1_");
        lexerLiteralFail("0x_52");
        lexerLiteralFail("0x52_");
        lexerLiteralFail("0_x52");
    }

    public void testStringSwitch() throws Exception {
        sc.getProjectSettings().setProperty(PropertyNames.JAVA_7, "true");
        String cuText = "class A {\n" +
                "  void foo(String s) {\n" +
                "    switch (s) {\n" +
                "      case \"A\": break;\n" +
                "      case \"jo...\": break; \n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        runIt(cuText);
    }

    public void testStringSwitchFailing() throws Exception {
        sc.getProjectSettings().setProperty(PropertyNames.JAVA_7, "false");
        String cuText = "class A {\n" +
                "  void foo(String s) {\n" +
                "    switch (s) {\n" +
                "      case \"A\": break;\n" +
                "      case \"jo...\": break; \n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        try {
            runIt(new ThrowingErrorHandler(), cuText);
            fail("This should be rejected.");
        } catch (Exception t) {
            // ok. TODO check that only one error was reported?
    }
    }

    public void testDiamondOperator() throws Exception {
        String cuText = "import java.util.*;\n" +
                "\n" +
                "class A {\n" +
                "\u0009List<String> l = new ArrayList<>();" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(), cuText).get(0);
        New initializer = (New)cu.getTypeDeclarationAt(0).getFields().get(0).getInitializer();
        Type it = sc.getSourceInfo().getType(initializer);
        assertEquals("java.util.ArrayList<java.lang.String>", it.getFullSignature());
        assertEquals("new ArrayList<>()", initializer.toSource().trim());
    }

    public void testDiamondOperator2() throws Exception {
        String cuText = "import java.util.*;\n" +
                "\n" +
                "class A {\n" +
                "\u0009HashMap<String,String> l = new HashMap<>();" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(), cuText).get(0);
        New initializer = (New)cu.getTypeDeclarationAt(0).getFields().get(0).getInitializer();
        Type it = sc.getSourceInfo().getType(initializer);
        assertEquals("java.util.HashMap<java.lang.String,java.lang.String>", it.getFullSignature());
        assertEquals("new HashMap<>()", initializer.toSource().trim());
    }


    public void testDiamondOperator_Wildcard() throws Exception {
        String cuText = "import java.util.*;\n" +
                "\n" +
                "class A {\n" +
                "\u0009List<?> l = new ArrayList<>();" +
                "}\n";
        try {
            runIt(new ThrowingErrorHandler(), cuText).get(0);
        } catch (ModelException e) {
            assertTrue(e.getMessage().contains("Cannot use DiamondOperator for wilcard"));
        }
    }

    public void testDiamondOperator_2() throws Exception {
        String cuText = "import java.util.*;\n" +
                "\n" +
                "class A {\n" +
                "    Map<String, List<Number>> l = new HashMap<>();" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(),
                cuText).get(0);
        ClassType it =
                (ClassType)sc.getSourceInfo().getType(cu.getTypeDeclarationAt(0).getFields().get(0).getInitializer());

        assertEquals("java.util.HashMap<java.lang.String,java.util.List<java.lang.Number>>", it.getFullSignature());
    }

    public void testDiamondOperatorWithTypeParams() throws Exception {
        String cuText =
                "class A<K,V> {\n" +
                "   A(K k, V v) { }\n" +
                "   A<K,V> a = new A<>(null, null);\n" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(),
                cuText).get(0);
        ClassType it =
                (ClassType)sc.getSourceInfo().getType(cu.getTypeDeclarationAt(0).getFields().get(0).getInitializer());
        assertEquals("A<A.K,A.V>", it.getFullSignature());
    }

    public void testDiamondOperatorWithTypeParamsAndRaw() throws Exception {
        String cuText =
                "class A<K extends Number,V> {\n" +
                "   A(K k, V v) { }\n" +
                "   A a = new A<>(null, null);\n" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(),
                cuText).get(0);
        ClassType it =
                (ClassType)sc.getSourceInfo().getType(cu.getTypeDeclarationAt(0).getFields().get(0).getInitializer());
        assertEquals("A<java.lang.Number,java.lang.Object>", it.getFullSignature());
    }

    public void testDiamondOperator_invalid() throws Exception {
        // Test: Type inference from arguments come before type inference from assignment.
        // Note: Semantics checker shouldn't allow this in the first hand. This test case must be adapted once that's been improved.
        String cuText = "import java.util.*;\n" +
                "\n" +
                "class A {\n" +
                "    Set<Number> l = new HashSet<>(Arrays.asList(new Long[] {2L}));" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(),
                cuText).get(0);
        ClassType it =
                (ClassType)sc.getSourceInfo().getType(cu.getTypeDeclarationAt(0).getFields().get(0).getInitializer());
        assertEquals("java.util.HashSet<java.lang.Long>", it.getFullSignature());
    }

    public void testTryWithResources() throws Exception {
        String cuText = "class A {\n" +
                "\u0009private void foo() throws Exception {\n" +
                "\u0009\u0009try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(\"a.zip\"); java.io.BufferedWriter bw = new java.io.BufferedWriter(null)) {\n" +
                "\u0009\u0009\u0009zf.getName();\n" +
                "\u0009\u0009\u0009bw.flush();\n" +
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(), cuText).get(0);
        String s = cu.toSource().replaceAll("( |\n)", "");
        assertEquals("classA{privatevoidfoo()throwsException{try(java.util.zip.ZipFilezf=newjava.util.zip.ZipFile(\"a.zip\");java.io.BufferedWriterbw=newjava.io.BufferedWriter(null);){"
                        + "zf.getName();bw.flush();}}}", s);
    }

    public void testTryWithResourcesWithCatch() throws Exception {
        String cuText = "class A {\n" +
                "\u0009private void foo() throws Exception {\n" +
                "\u0009\u0009try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(\"a.zip\"); java.io.BufferedWriter bw = new java.io.BufferedWriter(null)) {\n" +
                "\u0009\u0009\u0009zf.getName();\n" +
                "\u0009\u0009\u0009bw.flush();\n" +
                "\u0009\u0009} catch (Exception e) {\n" +
                "\u0009}   }\n" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(), cuText).get(0);
        String s = cu.toSource().replaceAll("( |\n)", "");
        assertEquals("classA{privatevoidfoo()throwsException{try(java.util.zip.ZipFilezf=newjava.util.zip.ZipFile(\"a.zip\");java.io.BufferedWriterbw=newjava.io.BufferedWriter(null);){"
                        + "zf.getName();bw.flush();}catch(Exceptione){}}}", s);
    }

    public void testMultiCatch() throws Exception {
        String cuText = "import java.lang.reflect.*;\n" +
                "class A {\n" +
                "  private void foo(java.lang.reflect.Method m) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {\n" +
                "    try {\n" +
                "      m.invoke(null);\n" +
                "    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { \n" +
                "      System.out.println(e);\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        CompilationUnit cu = runIt(new ThrowingErrorHandler(), cuText).get(0);
        String s = cu.toSource().replaceAll("( |\n)", "");
        assertEquals(cuText.replaceAll("( |\n)", ""), s);
    }
}
