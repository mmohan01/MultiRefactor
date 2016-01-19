/**
 * created on 19.10.2007
 */
package recoder.testsuite.fixedbugs;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import recoder.ModelException;
import recoder.ParserException;
import recoder.ProgramFactory;
import recoder.abstraction.ClassType;
import recoder.abstraction.ErasedConstructor;
import recoder.abstraction.ErasedType;
import recoder.abstraction.IntersectionType;
import recoder.abstraction.Method;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.Type;
import recoder.bytecode.ASMBytecodeParser;
import recoder.bytecode.AnnotationUseInfo;
import recoder.bytecode.ByteCodeParser;
import recoder.bytecode.ClassFile;
import recoder.bytecode.ReflectionImport;
import recoder.convenience.CustomTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.Import;
import recoder.java.JavaProgramFactory;
import recoder.java.StatementBlock;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ClassInitializer;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.LocalVariableDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.modifier.Final;
import recoder.java.declaration.modifier.Private;
import recoder.java.expression.Assignment;
import recoder.java.expression.operator.New;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.java.statement.LoopStatement;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.Transformation;
import recoder.kit.TypeKit;
import recoder.kit.UnitKit;
import recoder.service.NameInfo;
import recoder.service.SemanticsChecker;
import recoder.service.SourceInfo;
import recoder.service.TypingException;
import recoder.service.UnresolvedReferenceException;
import recoder.testsuite.RecoderTestCase;
/**
 * @author Tobias Gutzmann
 *
 */
@SuppressWarnings("deprecation")
 public class FixedBugs extends RecoderTestCase {
    ////////////////////////////////////////////////////////////
    // test cases
    ////////////////////////////////////////////////////////////

    public void testConstructorStartPosition() throws ParserException {
        ProgramFactory f = sc.getProgramFactory();
        CompilationUnit cu = f.parseCompilationUnit(
                "public class Test\n{\nTest s;\npublic Test(Test s)" +
                "\n{\nthis.s = s;\n}\n}");
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        assertTrue(((ConstructorDeclaration)sc.getNameInfo().getClassType("Test").getConstructors().get(0)).getStartPosition().getLine() == 4);
    }

    /**
	 * Test for absence of a bug in PrettyPrinter that, after transformation, may 
	 * mess up single line comments.
	 * Bug reported and testcase submitted by M.Ullbrich 
	 * @throws ParserException
	 */
    public void testSingleLineCommentBug() throws ParserException {
        ProgramFactory f = sc.getProgramFactory();
        CompilationUnit cu = f.parseCompilationUnit("class A {\n\n\n" +
                        "//some comment\r\nA a; } class B {}");
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        FieldDeclaration fd = (FieldDeclaration) cu.getDeclarations().get(0).getMembers().get(0);
        TypeReference oldType = fd.getTypeReference();
        TypeReference newType = TypeKit.createTypeReference(sc.getProgramFactory(), "B");
        fd.replaceChild(oldType, newType);
        sc.getChangeHistory().replaced(oldType, newType);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        String s = cu.toSource().replaceAll(" ", "");
        assertTrue(s.equals("classA{\n\n\n//somecomment\nBa;\n}classB{\n}\n"));
    }

    /**
     * Test for implemented generic type argument resolving in field references
     * @throws ParserException
     */
    public void testFieldTypes() throws ParserException {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        ProgramFactory f = sc.getProgramFactory();

        CompilationUnit cu = f.parseCompilationUnit("class B { } class G<E> { E field;   void m() { B b; b = new G<B>().field; } }");
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();

        ClassDeclaration classB = (ClassDeclaration) cu.getDeclarations().get(0);
        ClassDeclaration classG = (ClassDeclaration) cu.getDeclarations().get(1);
        MethodDeclaration methodM = (MethodDeclaration) classG.getMethods().get(0);
        Assignment assignment = (Assignment) methodM.getBody().getChildAt(1);
        Expression rhs = (Expression) assignment.getChildAt(1);
        Type rhsType = sc.getSourceInfo().getType(rhs);

        assertEquals(rhsType, classB);
    }

    public void testBasicReflectionImport() {
        // make sure non-public fields can be read...
        assertTrue(ReflectionImport.getClassFile("java.lang.String") != null);
        assertTrue(ReflectionImport.getClassFile("asdasdasd") == null);
    }

    public void testIncorrectFileEnd() {
        ProgramFactory f = sc.getProgramFactory();
        String cuText = "class B { }//";
        for (int i = 0; i < 4081; i++) {
            cuText += " ";
        }
        for (int i = 4081; i < 4087; i++) {
            // that's around the critical part, where the
            // size of the CU matches the JavaCCParser buffer
            try {
                f.parseCompilationUnit(cuText);
                cuText += " ";
            } catch (ParserException pe) {
                fail();
            }
        }
    }

    public void testBug1894463() throws ParserException {
        ProgramFactory f = sc.getProgramFactory();
        String cuText = "public class MockClass1 {\n" +
         "public class InnerClass {" +
         "public class InnerInnerClass { }" +
         "}" +
         "}" +
         "class MockClass2 {" +
         "void test(MockClass1 mockClass1) {" +
         "mockClass1.new InnerClass();" +
         "MockClass1.InnerClass ic = new InnerClass();" +
         "ic.new InnerInnerClass();" +
         "new InnerClass();" +
         "}" +
         "public class InnerClass {" +
         "}" +
         "}";
        CompilationUnit cu = f.parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        ClassDeclaration mock2 = (ClassDeclaration)sc.getNameInfo().getType("MockClass2");
        StatementBlock sb = ((MethodDeclaration)mock2.getMembers().get(0)).getBody();
        assertTrue(
                ((ClassDeclaration)sc.getCrossReferenceSourceInfo().getType(
                        ((New)sb.getStatementAt(0)))).getFullName().equals("MockClass1.InnerClass"));
        assertTrue(
                ((ClassDeclaration)sc.getCrossReferenceSourceInfo().getType(
                        ((New)sb.getStatementAt(2)))).getFullName().equals("MockClass1.InnerClass.InnerInnerClass"));
        assertTrue(
                ((ClassDeclaration)sc.getCrossReferenceSourceInfo().getType(
                        ((New)sb.getStatementAt(3)))).getFullName().equals("MockClass2.InnerClass"));
    }

    public void testBug1895973() throws ParserException {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        ProgramFactory f = sc.getProgramFactory();
        String cuText =
         "import java.util.ArrayList;\n" +
         "public class MockClass extends ArrayList<String> {\n" +
         "public String test() {\n" +
         "// Recoder doesn't detect that 'get(0)' returns a String !\n" +
         "return get(0).toLowerCase();\n" +
         "}\n}\n";
        CompilationUnit cu = f.parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testBug1911073() throws ParserException {
        // PrettyPrinter shouldn't be messed up when statement-blocks are empty
        String cuText =
            "class A {\n\tint l;\n\tvoid m() {\n\t\tswitch(l) {\n\t\t}\n\t}\n}\n";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        // check for proper indentation at end of file:
        assertTrue(
        cu.toSource().endsWith("        }\n    }\n}\n"));
    }

    public void testBug1936528() throws ParserException {
        // find most specific method when applicable methods all have varargs
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "class A {\n" +
            "void method(String s, Object... params){ }\n" +
            "void method(String s, Throwable t, Object... params){ \n" +
            "method(\"toto\", new Exception());}}\n";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }


    public void testBug1936842() throws ParserException {
        // raw types and autoboxing should work together
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
         "import java.util.*;\n" +
         "class Mock\n" +
         "{\n" +
         "void method()\n" +
         "{\n" +
         "List l = new ArrayList();\n" +
         "l.add(0);\n" +

         "List<Object> l2 = new ArrayList<Object>();\n" +
         "l2.add(0);\n" +
         "}\n" +
         "}\n";

        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testCommentAttachment() throws ParserException {
        String cuText =
            "class A {\n" +
            "\tvoid m(/*c1*/ int p /*c2*/) {\n" +
            "\t\t//Comment\n" +
            "\t\tint x = /*c3*/3;\n" +
            "\t}\n" +
            "}\n";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        MethodDeclaration md = (MethodDeclaration)cu.getTypeDeclarationAt(0).getMembers().get(0);
        assertTrue(
                md.getBody().getStatementAt(0).getComments().size() == 1);
        assertTrue(md.getParameterDeclarationAt(0).getComments().size() == 2);
        assertTrue(
                ((LocalVariableDeclaration)md.getBody().getStatementAt(0)).getVariableSpecifications().get(0).getInitializer().getComments().size() == 1);
    }

    public void testBug1948045() throws ParserException {
        // parser should accept final inner classes
        String cuText =
            "public class FinalInnerClass {" +
            "public void method(){" +
            "final class InnerClass{" +
            "}}}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        assertTrue(
        ((MethodDeclaration)cu.getTypeDeclarationAt(0).getMethods().get(0)).getTypes().get(0).getDeclarationSpecifiers().get(0).getClass() == Final.class);
    }

    public void testBug1965975() throws ParserException {
        // Endless loop when querying Methods of a parameterized Type (bytecode)
        sc.getProjectSettings().ensureSystemClassesAreInPath();

        String cuText =
            "import java.util.List; class A { void m() { List<String> l; } }";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        sc.getSourceInfo().getMethods((ClassType)
        sc.getSourceInfo().getType((((LocalVariableDeclaration)
        ((MethodDeclaration)cu.getTypeDeclarationAt(0).getMethods().get(0)).getBody().getStatementAt(0))).getTypeReference()));
    }

    public void testBug1974988() throws ParserException {
        String declText =
            "@Annot String decl = \"Test\";";
        FieldDeclaration fd = new JavaProgramFactory().parseFieldDeclaration(declText);
        fd.deepClone();
    }

    public void testBug1988746() throws ParserException {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "class A { void setDouble(Double p) {" +
            "Double toto = new Double(1) ;  " +
            "Double titi = new Double(2) ;\u0009" +
            "this.setDouble(toto * titi);}}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testBug1996819() throws ParserException {
        // as of java 5, return types of array types' clone()-methods
        // have the return type
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "class A { void m(String[] s) {" +
            "\u0009s.clone(); ((Object)s).clone();}}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        MethodReference expr = (MethodReference)
                ((MethodDeclaration)cu.getTypeDeclarationAt(0).getMethods().get(0)).getBody().getStatementAt(0);
        assertTrue(sc.getSourceInfo().getType(expr).getName().equals("String[]"));
        Method cloneMethod =
                sc.getSourceInfo().getMethod(expr);
        Type clonedType = sc.getSourceInfo().getType(cloneMethod);

        assertTrue(clonedType.getFullName().equals("java.lang.String[]"));
        // use the opportunity to check another bug:
        // array types' clone() methods overwrite java.lang.Object.clone()
        // and do not throw (checked) exceptions ("older" java versions and java 5)
        assertTrue(cloneMethod.getExceptions().size() == 0);
        Method objCloneMethod = sc.getSourceInfo().getMethod((MethodReference)
                ((MethodDeclaration)cu.getTypeDeclarationAt(0).getMethods().get(0)).getBody().getStatementAt(1));
        assertTrue(objCloneMethod != cloneMethod);
        assertTrue(MethodKit.getAllRedefinedMethods(cloneMethod).contains(objCloneMethod));
        // TODO 0.90 check the below check...
//        assertTrue(MethodKit.getRedefiningMethods(sc.getCrossReferenceSourceInfo(), objCloneMethod).contains(cloneMethod));
    }

    public void testBug2020825() throws ParserException {
        // inner types don't "inherit" type parameters of outer classes
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        // this should be valid code:
        String cuText =
            "import java.util.*; class A<E> { class B extends ArrayList<E> {} B m() { new A<String>().m().get(0).toLowerCase(); return null; }}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testBug2013315() throws ParserException {
        // generic fields in bytecode
        // this is the source code of Test.class:
        //import java.util.*;
        //public class Test
        //{
        //public Map<String, Number> myMap = new HashMap<String, Number>() ;
        //}

        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/Bug2013315");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "class X {{Test instance = new Test();" +
            "instance.myMap.get(\"\").shortValue(); }}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testBug2044230() throws ParserException {
        // this should parse...
        String cuText =
            "class A { public static final double MAX_VALUE = 0x1.fffffffffffffP+1023;}";
        sc.getProgramFactory().parseCompilationUnit(cuText);
    }

    public void testBug2045181() throws ParserException {
        // on demand imports don't find imported types
        String cuText =
            "package B; class A { static class InA {}}";
        String cu2Text =
            "package B;" +
            "import B.A.*;" +
            "class B extends InA {}";
        runIt(cuText, cu2Text);
    }

    public void testBug2045207() throws ParserException {
        // type parameters not supported for constructor declarations
        // this should compile:
        String cuText =
            "class A {public <T> A(T p1, Class<T> p2) { }}";
        runIt(cuText);
    }

    public void testBug2045354() throws ParserException {
        // this should be valid code, but cu.validateAll() fails currently:
        String cuText =
            "import java.security.*;" +
            "class A implements PrivilegedAction<String> {" +
            "public String run() {" +
            "\u0009return null;" +
            "}" +
            "{ A a = new A(); String s; s = AccessController.doPrivileged(a); }" +
            "}";
        runIt(cuText);
    }

    public void testBug2046005() throws ParserException {
        // the code below should be valid
        // this is actually not bug 2046005, but highly related.
        // note the <?> in Constructor<?> constr
        // which makes the code valid. In the initial bugreport at sf.net,
        // <?> was not present, making the code invalid (but still compilable with javac).
        // we use this method now to fix the related bug...
        String cuText =
            "class A<T> {\n" +
            "\u0009<T> T foo(Class<T> p) { return null;}" +
            "}\n" +
            "class B {\n" +
            "{\n" +
            "final Class<String> v = String.class;\n" +
            "A<?> a = null;\n" +
            "a.foo(v).toLowerCase();\n" +
            "}\n" +
            "}\n";
        runIt(cuText);
    }

    public void testBug2046005_2() {
        // like above, but made invalid as described there
        String cuText =
            "class A<T> {\n" +
            "\u0009<T> T foo(Class<T> p) { return null;}" +
            "}\n" +
            "class B {\n" +
            "{\n" +
            "final Class<String> v = String.class;\n" +
            "A a = null;\n" +
            "a.foo(v).toLowerCase();\n" +
            "}\n" +
            "}\n";
        runIt(new SilentErrorHandler(1), cuText); // expect one failure
    }

    public void testBug2046005_3() throws ParserException {
        // similar to above, but with some changes
        String cuText =
            "class A<T> {\n" +
            "\u0009<Q> Q foo(String s, Class<Q> c) { return null; }" +
            "\u0009<T> T foo(Class<T> p) { return null;}" +
            "}\n" +
            "class B {\n" +
            "{\n" +
            "final Class<String> v = String.class;\n" +
            "A x = null;\n" +
            "A<?> a = null;\n" +
            "a.foo(v).toLowerCase();\n" +
            "}\n" +
            "}\n";
        runIt(cuText);
    }

    public void testBug2046167() throws ParserException {
        // TODO should be rewritten to not use com.sun...
        String cuText =
            "package com.sun.org.apache.xerces.internal.impl.xs.identity;" +
            "import com.sun.org.apache.xerces.internal.impl.xpath.XPathException;" +
            "import com.sun.org.apache.xerces.internal.util.SymbolTable;" +
            "import com.sun.org.apache.xerces.internal.xni.NamespaceContext;" +
            "public class Field {" +
            "public static class XPath" +
            "    extends com.sun.org.apache.xerces.internal.impl.xpath.XPath {" +
            "    public XPath() throws XPathException {" +
            "        super(null, null, null);" +
            "        com.sun.org.apache.xerces.internal.impl.xpath.XPath.Axis axis = null;" +
            "        if (axis.type == XPath.Axis.ATTRIBUTE) {" +
            "               throw new XPathException(null);" +
            "        }" +
            "    }" +
            "}" +
            "}";
        runIt(cuText);
    }

    public void testBug2046337() throws ParserException {
        String cuText = "class A { void foo() {new String[3].getClass();}}";
        CompilationUnit cus = runIt(cuText).get(0);
        getClass();
        ClassType t = (ClassType)cus.getFactory().getServiceConfiguration().getSourceInfo().getType(
        ((MethodDeclaration)cus.getTypeDeclarationAt(0).getMembers().get(0)).getBody().getStatementAt(0));
        assertTrue(t.getFullSignature().equals("java.lang.Class<? extends java.lang.String[]>"));
    }
    public void testExplicitGenInvokeParserTest() throws ParserException {
        String cuText = "class A{{java.util.Collections.<String, String[]>emptyMap();}}";
        runIt(cuText);
    }

    public void testCaptureOfIssue() throws ParserException {
        // unreported, as in development version only. Issue
        // when passing "capture-of types" as arguments.
        String cuText = "class A {" +
                "void foo(Number n) { }" +
                "void bar() {" +
                "  java.util.Iterator<? extends Number> it = null;" +
                "  foo(it.next());" +
                "}" +
                "}";
        runIt(cuText);
    }


    public void testEnumSupertypeBug() throws ParserException {
        String cuText = "enum A implements java.util.Comparator<Number> {" +
                "; { Number[] n = null; java.util.Arrays.sort(n, this); }}";
        runIt(cuText);
    }

    public void testCaptureBugParamMatches() throws ParserException {
        // unfiled bug, DefaultProgramModelInfo.paramMatches() had a problem.
        String cuText =
            "import java.util.Comparator;" +
            "class A {" +
            "private static <T> void binarySearch0(T[] a, T key, Comparator<? super T> c) {" +
            "\u0009    c.compare(key, key);" +
            "}" +
            "}";
        runIt(cuText);
    }

    public void testClassLiteralTypes() throws ParserException {
        // correct types for class literals? related to bug 2046337
        String cuText =
            "class A {" +
            "void m() {" +
            "void.class.cast(null);" +
            "int.class.cast(null);" +
            "Integer.class.cast(null);}}";
        CompilationUnit cu = runIt(cuText).get(0);
        StatementBlock sb = ((MethodDeclaration)cu.getTypeDeclarationAt(0).getMembers().get(0)).getBody();
        SourceInfo si = cu.getFactory().getServiceConfiguration().getSourceInfo();
        assertTrue(((ClassType)si.getType(sb.getStatementAt(0))).getFullSignature().equals("java.lang.Void"));
        assertTrue(((ClassType)si.getType(sb.getStatementAt(1))).getFullSignature().equals("java.lang.Integer"));
        assertTrue(((ClassType)si.getType(sb.getStatementAt(2))).getFullSignature().equals("java.lang.Integer"));
    }

    public void testValidateBug() throws ParserException {
        // unfiled validate()-bug. Code below is valid but wasn't in
        // intermediate development version
        String cuText =
            "class A { {byte b; b = 5; " +
            "Class<?> classes[]; classes = new Class[20];" +
            "}}";
        runIt(cuText);
    }

    public void testTypeInferenceBug() throws ParserException {
        // unfiled. Type inference issue.
        String cuText =
            "import java.util.Collection;" +
            "class A { " +
            "static class B<E> {" +
            "E bar[];" +
            "}" +
            "static class C<E> extends B<E> {" +
            "public void foo(Collection<? extends E> c) {" +
            "      E[] a = null;" +
            "      a = c.toArray(bar);" +
            "}" +
            "\u0009}" +
            "}";
        runIt(cuText);
    }

    public void testAnonymousEnumInstanceParent() throws ParserException {
        // unfiled. Recoder didn't properly return
        // the supertypes of an anonymous enum class body.
        String cuText = "public enum A {" +
              "CONST { public int foo() { return 0; }};" +
              "public int foo() { return -1; }" +
              "}" +

              "class B {" +
              "void foo(A a) { foo(A.CONST); }" +
              "}";
        runIt(cuText);
    }

    public void testValidateBug2() throws ParserException {
        // another unfiled validate() bug
        String cuText = "public class A<E> extends java.util.AbstractQueue<E> {" +
         "private final Object lock;" +
         "private class B {" +
         "public void foo() { final Object i = A.this.lock; }" +
         "}}";
        runIt(cuText);
    }

    public void testBug2044375() throws ParserException {
        // should report an error, not throw an exception.
        String cuText =
            "import java.util.HashMap;" +
            "class A {" +
            "  HashMap<String, Unknown<String, String>> field;" +
            "  public Unknown<String, String> getChangeString(String s) {" +
            "    return field.get(s);" +
            "  }" +
            "}";
        try {
            runIt(new SilentErrorHandler(2), cuText);
        } catch (ModelException me) {
            // okej, we expect this!
    }
    }

    public void testEnumValueOfReturnType() throws ParserException {
        // unfiled
        String cuText = "public class A { void foo() { " +
                "Class c = null; Enum.valueOf(c, \"WAITING\"); }}";
        CompilationUnit cu = runIt(cuText).get(0);
        SourceInfo si = cu.getFactory().getServiceConfiguration().getSourceInfo();
        Type t = si.getType(((MethodDeclaration)cu.getTypeDeclarationAt(0).getMembers().get(0)).getBody().getStatementAt(1));
        assertEquals(t.getFullName(), "java.lang.Enum"); // leftmost bound
    }

    public void testArraySupertypes() {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        ClassType ct = sc.getNameInfo().getClassType("java.lang.String[]");
        Set<String> supStr = new HashSet<String>();
        for (ClassType sup: ct.getSupertypes())
            supStr.add(sup.getFullSignature());
        assertTrue(supStr.size() == 4);
        assertTrue(supStr.contains("java.lang.Object[]"));
        assertTrue(supStr.contains("java.io.Serializable[]"));
        assertTrue(supStr.contains("java.lang.Comparable<java.lang.String>[]"));
        assertTrue(supStr.contains("java.lang.CharSequence[]"));
    }


    public void testRHSUnboxing() throws ParserException {
        // unreported bug: instead of boxing LHS, unbox RHS of an assignment.
        String cuText =
            "class A { {" +
            "float num = 0;" +
            "int i = 0;" +
            "int[] args = null;" +
         "\u0009i++;" +
         "\u0009num = Integer.valueOf(args[i]);" +
         "}" +
         "}";
        CompilationUnit cu = runIt(cuText).get(0);
        cu.validateAll();
    }

    public void testBug2071287() throws ParserException {
        // scoping after transformation
        String cuText =
            "class A {\n" +
            "void m() {\n" +
            "  {\n" +
            "    for (;;) {\n" +
            "      int n;\n" +
            "    }\n" +
            "  }\n" +
            "  int n;\n" +
            "}\n" +
            "}\n";
        List<CompilationUnit> cul = runIt(cuText);
        for (CompilationUnit cu: cul)
            cu.validateAll();

        TreeWalker tw = new TreeWalker(cul.get(0));
        while (tw.next(LoopStatement.class)) {
            LoopStatement ls = ((LoopStatement)tw.getProgramElement());
            LoopStatement repl = ls.deepClone();
            ls.getASTParent().replaceChild(ls, repl);
            sc.getChangeHistory().replaced(ls, repl);
        }
        sc.getChangeHistory().updateModel();
    }

    public void testBytecodeInnerClass() {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        ClassType itr = sc.getNameInfo().getClassType("java.util.AbstractList.Itr");
        assertTrue(itr.isInner());
    }

    public void testBug2088980() throws ParserException {
        // actually, a duplicate of bug 2000780. But we don't have an explicit
        // testcase for that one.
        String cuText =
                "import java.util.*;" +
                "class A {" +
                "\u0009public <T> T methodCallingGenericMethod(T bean) {" +
                "\u0009\u0009Collection<T> aList = new ArrayList<T>();" +
                "\u0009\u0009aList.add(Collections.singletonList(bean).get(0));" +
                "\u0009}" +
                "}";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validateAll();
    }

    public void testBug2132665() throws ParserException {
        // anonymous class with following call to a newly declared method
        String cuText =
            "import java.awt.Dialog;" +
            "import java.awt.Frame;" +

            "public class Main {" +
            "public static void main(String[] args) {" +
            "Frame frame = new Frame();" +

            "new Dialog(frame){" +
            "public void init(){" +
            "//initme\n" +
            "}" +
            "}.init();" +
            "}" +
            "}";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validateAll();
        // the following is some experimental testing. Reported via email
        // to me, but I don't think it's a bug (getName() == null should
        // be desired behavior!?)
        CustomTreeWalker ctw = new CustomTreeWalker(cus.get(0));
        ctw.next(MethodDeclaration.class);
        ctw.next(MethodDeclaration.class);
        MethodDeclaration method = (MethodDeclaration)ctw.getProgramElement();
        assertNotNull(method.getContainingClassType().getFullName());
        // TODO think about this
//        System.out.println(method.getContainingClassType().getName());
    }

    public void testBug2134267() throws ParserException {
        // problem resolving package names from bytecode classes when naming conventions are not follow (e.g., COM.xyz)
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/Bug2134267/*.jar");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "package runner;" +
            "public class Main {" +
            "public static void main(String[] args) {" +
            "COM.AClass value = null;" +
            "}" +
            "}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testBug2155226() throws ParserException {
        // this should report a ModelException, not throw an UnsupportedOperationException
        String cuText =
            "class KnownBasicClass" +
            "{}" +

            "class TestFailed1 extends UnknownGenericClass<KnownBasicClass>{" +
            "}";
        runIt(new SilentErrorHandler(1), cuText);
    }

    public void testBug2230018() throws ParserException {
        // should again report a ModelException, not throw an Exception...
        String cuText =
            "import com.pkg.UnknownClass;" +
            "class KnownBasicClass" +
            "{" +
            "    int nb = UnknownClass.CONSTANT;" +
            "}";
        runIt(new SilentErrorHandler(4), cuText);
    }

    public void testBug2230018_2() throws ParserException {
        // should again report a ModelException, not throw an Exception...
        // DO NOT MERGE WITH testBug2230018(), OTHERWISE THIS BUG WON'T BE DETECTED DUE TO CACHING ISSUES!
        String cuText =
            "package com.example.generics;" +
            "import com.Blubb;" +
            "public class ExtendsUnknownGeneric extends Toto<DynamicType>" +
            "{" +
            "  public void method()" +
            "  {" +
            "    String s = Blubb.CONSTANTE ;" +
            "  }" +
            "}";
        runIt(new SilentErrorHandler(6), cuText);
    }

    public void testOverwritingWithPrimitives() throws ParserException {
        // Never been a bug, and never should be, so we add this here:
        // don't use autoboxing for determining overwriting methods ;-)
        String cuText =
            "abstract class A<T> { abstract void foo(T a); }\n" +
            "class B extends A<Integer> {\n" +
            "\u0009void foo(int a) {}\n" +
            "}\n" +
            "class C extends B {" +
            "\u0009void foo(long a) {" +
            "\u0009}\n" +
            "}";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
        assertEquals(MethodKit.getRedefinedMethods(
                cus.get(0).getTypeDeclarationAt(1).getMethods().get(0)).size(), 0);
        assertEquals(sc.getSourceInfo().getAllMethods(cus.get(0).getTypeDeclarationAt(1)).size(), 13);
        assertEquals(MethodKit.getRedefinedMethods(
                cus.get(0).getTypeDeclarationAt(2).getMethods().get(0)).size(), 0);
        assertEquals(sc.getSourceInfo().getAllMethods(cus.get(0).getTypeDeclarationAt(2)).size(), 14);
    }

    public void testCrossReferencer() throws ParserException {
        // Never been a bug, and never should be. I just suspected it once,
        // and now that it's written, it stays for the sake of testing :-P
        String cuText =
            "class A {\n" +
            "\u0009void foo(long a) {" +
            "\u0009\u0009java.util.LinkedList ll = new java.util.LinkedList();\n" +
            "\u0009}" +
            "}";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
        assertEquals(sc.getCrossReferenceSourceInfo().getReferences(
                sc.getNameInfo().getType("java.util.LinkedList")).size(), 2);
    }


    public void testBug2343547() throws Exception {
        new ByteCodeParser().parseClassFile(new FileInputStream("test/fixedBugs/Bug2343547/ICacheManager.class"));
    }

    public void testBug2510517() throws Exception {
        // not necessary any longer as of 0.94.
    }

    public void testBug2523272() throws Exception {

        // the .class file is corrupted so that it references an unknown file!
        // further, recoder is not in classpath, so it would even be sufficient to not modify it...
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/Bug2523272");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        sc.getProjectSettings().setErrorHandler(new SilentErrorHandler(0));
        String cuText =
            "class X { void foo(recoder.convenience.ASTIteratorListener o) {} }";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();

        ClassType ct = sc.getNameInfo().getClassType("recoder.convenience.ASTIteratorListener");
        List<Type> sig = ct.getMethods().get(0).getSignature();
        assertNotNull(sig.get(0));
        assertNotNull(sig.get(1));
        assertEquals(sig.get(0), sc.getNameInfo().getUnknownClassType());
        assertEquals(sig.get(1), sc.getNameInfo().getUnknownClassType());
    }

    public void testBug2609084() throws Exception {
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/Bug2609084");
        sc.getProjectSettings().ensureSystemClassesAreInPath();

        String cuText =
            "class Y { String s; void foo(X<String> x) {s = x.o.toUpperCase(); } }";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
        assertNotSame(sc.getNameInfo().getClassType("X").getFields().get(0).getType(), sc.getNameInfo().getUnknownClassType());
    }

    public void testBug2808379() throws Exception {
        String cuText =
            "class A {\n" +
            "\u0009static Object foo() { return null; }\n" +
            "}\n" +
            "class B extends A {\n" +
            "\u0009static String foo() { return null; }\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        assertEquals(1,
                MethodKit.getRedefiningMethods(sc.getCrossReferenceSourceInfo(), cus.get(0).getTypeDeclarationAt(0).getMethods().get(0)).size());
        assertEquals(1,
                MethodKit.getRedefinedMethods(cus.get(0).getTypeDeclarationAt(1).getMethods().get(0)).size());
    }

    public void testBug2813956() throws Exception {
        String cuText =
            "class Y<T> {\n" +
            "\u0009public void foo(Class<T> c) { }\n" +
            "}\n" +
            "class X extends Y {\n" +
            "\u0009public void foo(Class c) { } \n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
        assertEquals(1, MethodKit.getAllRedefinedMethods(
                cus.get(0).getTypeDeclarationAt(1).getMethods().get(0)).size());
    }

    public void testBug2824786() throws Exception {
        String cuText =
            "import static a.X2.I;" +
            "class X {" +
            "\u0009I e = null;" +
            "}";
        String cuText2 =
            "package a;" +
            "public class X2 {" +
            "\u0009public interface I { void foo(); }" +
            "}";
        List<CompilationUnit> cus = runIt(cuText, cuText2);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testBug2828305() throws Exception {
        String cuText =
            "class C {" +
            "@Deprecated <A extends Number> A foo(A a) { return null; }" +
            "}";

        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testBugImportButNoTypeDeclaration() throws Exception {
        // error in DefaultSourceInfo: No type declaration, but imports,
        // causing an IndexOutOfBoundsException.
        // further, x can't be resolved. Bug has been not reported
        // resolved
        String cuText =
            "package a;\n public class A { public @interface AA { String s(); }; static final String x = \"\";}";
        String cuText2 =
            "@A.AA(s = x) package a;\n import static a.A.x;";

        List<CompilationUnit> cus = runIt(cuText, cuText2);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testBug2828368() throws Exception {
        String cuText =
            "class X {" +
            "interface I { }" +
            "class Y extends java.util.ArrayList<Number> implements I { }" +
            "void g(I o) { }" +
            "void g(java.util.Collection<String> p) { }" +
            "void h() {" +
            "\u0009g(new Y());" +
            "}}";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void test2828368_2() throws Exception {
        // something that dug up during fixing of bug 2828368. Not reported, but related.
        // the reference to the constructor of Y cannot be resolved in an
        // intermediate Recoder version.
        String cuText =
            "public class X { " +
            "\u0009void foo() { new Y(new java.util.ArrayList()); }" +
            "}";
        String cuText2 =
            "public class Y extends X {" +
            "\u0009Y(java.util.List<X> l) { }" +
            "}";
        List<CompilationUnit> cus = runIt(cuText, cuText2);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void test2828368_3() throws Exception {
        // the return type of java.util.Collections.enumeration(...) should be
        // %raw%Enumeration!
        String cuText =
            "public class S { " +
            "\u0009S(java.util.Enumeration<? extends Number> p) { }" +
            "\u0009void foo() {" +
            "\u0009\u0009new S(java.util.Collections.enumeration(new java.util.ArrayList()));" +
            "\u0009}" +
            "}";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testHidingTypeParameterDeclarationBug() throws Exception {
        // there is a bug when it comes to type parameter declarations
        // in methods hiding a surrounding one
        String cuText =
            "import java.util.*;\n" +
            "class X {\n" +
            "{\n" +
            "\u0009\u0009X comp = null;\n" +
            "\u0009\u0009List<String> nameList = null;\n" +
            "\u0009\u0009comp.compile(Y.from(nameList.toArray(new String[0])));\n" +
            "\u0009}\n" +
            "\u0009void compile(List<String> f) { }\n" +
            "}\n" +
            "class Y<A> {\n" +
            "\u0009static <A> List<A> from(A[] array) { return null; }\n" +
            "}\n";

        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testFindInheritedTypeAndFieldThroughPackageImport() throws Exception {
        String cuText =
            "import static x.A.*;" +
            "class X {" +
            "\u0009Member m = null;" +
            "\u0009int x = intField;" +
            "}";
        String cuText2 =
            "package x;" +
            "public class A extends Base {}";
        String cuText3 =
            "package x;" +
            "public class Base {" +
            "\u0009public static class Member { }" +
            "\u0009public static final int intField = 123;" +
            "}";
        List<CompilationUnit> cus = runIt(cuText, cuText2, cuText3);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testInheritedTypeByOuterClass() throws Exception {
        // the actual cause for this bug was that InterfaceDeclaration
        // contained a bug: it reported that an interface is "never protected".
        // That's just not the case, the code below is valid!
        String cuText =
            "package a; public class A { protected interface I {}; }";
        String cuText2 =
            "public class B extends a.A {" +
            "\u0009I i = null;" +
            "\u0009class Inner {" +
            "\u0009\u0009I i = null;" +
            "\u0009}" +
            "}";
        List<CompilationUnit> cus = runIt(cuText, cuText2);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testResolveTypeParameterReference() throws Exception {
        String cuText =
            "abstract class X<T extends Number> {" +
            "private static abstract class Member<T extends Number> extends X<T> {" +
            "\u0009protected F<T> filter() {" +
            "\u0009\u0009return new F<T>() {" +
            "\u0009\u0009\u0009protected boolean matches(T d) {" +
            "\u0009\u0009\u0009\u0009return match(d);" +
            "\u0009\u0009\u0009}" +
            "\u0009\u0009};" +
            "\u0009}" +
            "\u0009protected abstract boolean match(T d);" +
            "}" +
            "}" +
            // if the name of type parameter was something other than T,
            // then there wasn't a bug in the first place
            "abstract class F<T> {" +
            "\u0009protected abstract boolean matches(T t);" +
            "}";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testGetRawMemberType() throws Exception {
        // strange bug that occurred in an intermediate version of
        // recoder only.
        String cuText =
            "class X {" +
            "\u0009private class Inner extends a.Other {" +
            "\u0009\u0009private class X extends Member { }" +
            "\u0009}" +
            "}";
        String cuText2 =
            "package a;" +
            "public abstract class Other<M> {" +
            "\u0009protected abstract static class Member { }" +
            "}";
        List<CompilationUnit> cus = runIt(cuText, cuText2);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }

    public void testPrintEmptyMethodComments() throws Exception {
        String cuText =
            "class X {\n" +
            "\u0009X() { /* empty */ }\n" +
            "\u0009X(int i) {\n // empty \n}\n" +
            "\u0009foo() {\n" +
            "\u0009\u0009// empty as well\n" +
            "\u0009}\n" +
            "\u0009bar() {\n" +
            "\u0009\u0009/* empty 4*/\n" +
            "\u0009}\n" +
            "}\n" +
            "interface A { /* interfaceComment*/ }";
        List<CompilationUnit> cus = runIt(cuText);
        for (CompilationUnit cu: cus)
            cu.validate();
        new SemanticsChecker(sc).checkAllCompilationUnits();
        String s = cus.get(0).toSource().replaceAll("( |\n)", "");
        assertEquals("classX{X(){/*empty*/}X(inti){//empty}foo(){//emptyaswell}" +
                "bar(){/*empty4*/}}interfaceA{/*interfaceComment*/}", s);
    }

    public void testImportBug() throws Exception {
        // for some strange reasons, this below shall fail: X cannot be resolved.
        // obviously, in the case of imports, inherited members are *not* considered.
        // accessing class X is both valid in the context of class A, or when changing
        // the import statement to import p.A.*; or to import static p.A2.*;
        // That's at least the behavior of Eclipse and javac.
        // TODO check in JLS why this is.
        String cuText1 =
            "package p;\n" +
            "class A {\n" +
            "\u0009public static class X {\n" +
            "\u0009\u0009public static final int q = 3;\n" +
            "\u0009}\n" +
            "\u0009public class Y { public static final int q = 5;\n }" +
            "}\n";
        String cuText2 = "package p;\n" +
                "class A2 extends A { }\n";
        String cuText3 = // faulty!
            "package q;\n" +
            "import p.A2.*;" +
            "class I {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009int i = X.q;" +
            "\u0009}\n" +
            "}\n";
        String cuText4 = // ok !
            "package q;\n" +
            "import static p.A2.*;" +
            "class J {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009int i = X.q;" +
            "\u0009}\n" +
            "}\n";
        String cuText5 = // faulty again!
            "package q;\n" +
            "import static p.A2.*;" +
            "class K {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009int i = Y.q;" +
            "\u0009}\n" +
            "}\n";
        runIt(new SilentErrorHandler(6), cuText1, cuText2, cuText3, cuText4, cuText5);
    }

    public void testAmbiguousMethodBecauseOfErasure() throws Exception {
        // in 0.93, Recoder accepted the code below. However, class C is invalid:
        // both foo(Object) and foo(T) have the same erasure! Recoder should
        // discover this when resolving the method invocation.
        String cuText1 =
            "abstract class A<T> {" +
            "void foo(T t) { C<T> c = new C<T>(); c.foo(t); } " +
            "}" +
            "class B extends A<String> {" +
            "void foo(String s) { }" +
            "}" +
            "class C<T> extends A<T> {" +
            "void foo(Object s) { }" +
            "void foo(T t) { }" +
            "}";
        List<CompilationUnit> cus = runIt(cuText1);
        // TODO make this fail!
    }

    public void testBug2838441() throws Exception {
        // the following is incorrect code(!)
        String cuText1 =
            "import java.util.*;\n" +
            "class X extends HashMap {\n" +
            "\u0009void foo(Entry e) { }\n" +  // error!
            "\u0009void bar(Map.Entry e) { }\n" + // valid!
            "}\n";
        runIt(new SilentErrorHandler(1), cuText1);
    }

    public void testGenericConstructors() throws Exception {
        String cuText1 =
            "class NN {\n" +
            "<T> NN (T t1, T t2) { }\n" +
            "NN() {\n" +
            "\u0009<String>this(null, null);\n" +
            "}\n" +
            "}\n" +
            "class MM extends NN {\n" +
            "MM() {\n" +
            "\u0009<String>super(null, null);\n" +
            "\u0009new <String>NN(null, null);\n" +
            "}\n" +
            "}\n";
        CompilationUnit cu = runIt(cuText1).get(0);
        String res = cu.toSource();
        assertEquals(cuText1.replaceAll("( |\n|\t)", ""), res.replaceAll("( |\n|\t)", ""));
        // TODO not only check if it parses, but also if the result remains the same!
    }

    public void testBug2838979() throws Exception {
        // the following caused a stack overflow in 0.93 (and probably before as well):
        String cuText1 =
            "class B<T> { }\n" +
            "class A {\n" +
            "  public void foobar( B<B<B<A>>> a, B<B<A>> b) {}\n" +
            "}\n";
        runIt(cuText1);
    }

    public void testBug2843524() throws Exception {
        // most likely a duplicate of 2838979. Anyhow, an additional test case
        // can't hurt.
        String cuText =
            "public abstract class SomeClass {\n" +
            "    public abstract <T extends Comparable<T>> boolean check(T x);\n" +
            "    public boolean match(String s) {\n" +
            "        return check(s);\n" +
            "    }\n" +
            "}\n";
        runIt(cuText);
    }

    public void testGenericsIssue() throws Exception {
        // didn't run in an intermediate Recoder version.
        String cuText1 =
            "package p;" +
            "import java.util.*;" +
            "class D {\n" +
            "\u0009void foo() { " +
            "\u0009\u0009String x[] = new String[1];" +
            "\u0009\u0009Arrays.asList(x).get(0).toLowerCase(); " +
            "\u0009}" +
            "}\n";
        runIt(cuText1);
    }

    public void testOverwriteWithMethodTypeParams() throws Exception {
        // overwrite / implements bug (matching signatures in conjunction with method type parameters)
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        SourceInfo si = sc.getSourceInfo();
        NameInfo ni = sc.getNameInfo();
        ClassType ct = ni.getClassType("java.util.AbstractList");
        List<Method> ms = si.getMethods(ct, "remove", Collections.<Type>singletonList(ni.getIntType()), ct);
        ClassType ct2 = ni.getClassType("java.util.AbstractCollection");
        List<Method> ms2 = si.getMethods(ct2, "remove", ms.get(0).getSignature(), false, false, ct2);
        assertEquals(0, ms2.size());
    }

    public void testBug2849358() throws Exception {
        String cuText =
            "class A {" +
            "A(java.awt.Window s) { }" +
            "A(java.awt.Frame n) { }" +
            "void foo() {" +
            "  new A(null);" +
            "}" +
            "}";
        CompilationUnit cu = runIt(cuText).get(0);
        ConstructorReference cr = (ConstructorReference)((MethodDeclaration)cu.getTypeDeclarationAt(0).getMembers().get(2)).getBody().getBody().get(0);
        assertEquals(cu.getTypeDeclarationAt(0).getMembers().get(1),
                sc.getSourceInfo().getConstructor(cr));
    }


    public void testBug2860511() throws Exception {
        String cuText1 =
            "class A {\n" +
            "\u0009void foo() {\n " +
            "\u0009\u0009String s = new String[] {\"aa\"} [0];\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText1);
        assertEquals(
                "classA{voidfoo(){Strings=newString[]{\"aa\"}[0];}}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testInnerClass() throws Exception {
        // not a bug, but something that tests a strange implementation. See ClassFile.isInner() for explanation.
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        assertTrue(sc.getNameInfo().getClassType("java.util.HashMap.Values").isInner());
        assertFalse(sc.getNameInfo().getClassType("java.util.HashMap.Entry").isInner());
    }

    public void testBug2910715() throws Exception {
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/Bug2910715/gin-1.0-20090622.jar");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "import com.google.gwt.inject.client.binder.GinLinkedBindingBuilder;\n" +
            "public class TestCase { }\n";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testBug2912087() throws Exception {
        String cuText1 =
            "@interface A {\n" +
            "\u0009public int A_CONST = 0; " +
            "}\n";
        runIt(cuText1);
    }

    public void testBug2927536_WorksWithSourceCodeOnly() throws Exception {
        // The source code for the following two test cases; no problem with source code only.
        String cuText = "public class A<X, Y>" +
         "{" +
         "\u0009protected void parentMethod1(A<?, ?> p) { }" +
         "\u0009protected void parentMethod2(Y input) { }" +
         "}" +
         "class A2<X> extends A<java.util.List<X>, X>" +
         "{" +
         "\u0009public void childMethod() {}" +
         "}";
        String cuText2 =
            "public class Test" +
            "{" +
              "public void test(A2<String> child)" +
              "{" +
                "child.childMethod();" +
                "child.parentMethod1(child);" +
              "}" +
            "}";
        runIt(cuText, cuText2);
    }

    public void testBug2927536() throws Exception {
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/Bug2927536");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "public class Test" +
            "{" +
              "public void test(A2<String> child)" +
              "{" +
                "child.childMethod();" +
              "}" +
            "}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testBug2927536_2() throws Exception {
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/Bug2927536_2");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "public class Test" +
            "{" +
              "public void test(A2<String> child)" +
              "{" +
                "child.parentMethod1(child);" +
              "}" +
            "}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();
    }

    public void testBug2980457() throws Exception {
        String cuText =
            "import p.q.C;\n" +
            "import static p.q.C.CONST;\n" +
            "public class Test {\n" +
            "\u0009private C f = CONST;\n" +
            "}\n";
        runIt(new SilentErrorHandler(5), cuText);
    }


    public void testBug2983137() throws Exception {
        // the following is valid code. Recoder 0.94c throws a NullPointerException.
        String cuText =
            "class A {\n" +
                "int f;\n" +
            "}\n" +
            "class B extends A implements java.io.Serializable {}\n" +
            "class C extends A implements java.io.Serializable {}\n" +
            "public class IT2 {\n" +
            "\u0009public static void main(String[] args) {\n" +
            "\u0009\u0009new IT2().foo(new B(), new C()).f = 3;\n" +
            "\u0009}\n" +
            "\u0009<T> T foo(T t1, T t2) {" +
            "\u0009\u0009return null;" +
            "\u0009}" +
            "}";
        runIt(cuText);
    }
    public void testGetClass1() throws Exception {
        // This is invalid source code.
        // The type of type argument argument of Class<T> is the *erasure* of the prefix, not the prefix
        String cuText = "class A {" +
         "\u0009int f;" +
         "}" +
         "class B extends A implements I {" +
         "\u0009public void bar() { }" +
         "}" +
         "class C extends A implements I {" +
         "\u0009public void bar() { }" +
         "}" +
         "interface I {" +
         "\u0009void bar();" +
         "}" +
         "public class IT2 extends A {" +
         "\u0009public static void main(String[] args) {" +
         "\u0009\u0009new IT2().foo(new B(), new C()).getClass().newInstance().bar();" +
         "\u0009}" +
         "\u0009<T> T foo(T t1, T t2) {" +
         "\u0009\u0009return null;" +
         "\u0009}" +
         "}";
        try {
            runIt(cuText);
            fail("This code should not be accepted!");
        } catch (ModelException e) {
            // yes :-)
    }
    }

    public void testGetClass2() throws Exception {
        // On the other hand, if I is the most common supertype (more common than Object), this should pass.
        String cuText = "class A {" +
         "\u0009int f;" +
         "}" +
         "class B implements I {" +
         "\u0009public void bar() { }" +
         "}" +
         "class C implements I {" +
         "\u0009public void bar() { }" +
         "}" +
         "interface I {" +
         "\u0009void bar();" +
         "}" +
         "public class IT2 extends A {" +
         "\u0009public static void main(String[] args) {" +
         "\u0009\u0009new IT2().foo(new B(), new C()).getClass().newInstance().bar();" +
         "\u0009}" +
         "\u0009<T> T foo(T t1, T t2) {" +
         "\u0009\u0009return null;" +
         "\u0009}" +
         "}";
        runIt(cuText);
    }

    public void testGetClass3() throws Exception {
        // This one will print a warning on syserr.
        // TODO figure out how to really do it!
        String cuText = "class A {" +
         "\u0009int f;" +
         "}" +
         "class B implements I,I2 {" +
         "\u0009public void bar() { }" +
         "}" +
         "class C implements I,I2 {" +
         "\u0009public void bar() { }" +
         "}" +
         "interface I2 { }\n " +
         "interface I {" +
         "\u0009void bar();" +
         "}" +
         "public class IT2 extends A {" +
         "\u0009public static void main(String[] args) {" +
         "\u0009\u0009new IT2().foo(new B(), new C()).getClass().newInstance().bar();" +
         "\u0009}" +
         "\u0009<T> T foo(T t1, T t2) {" +
         "\u0009\u0009return null;" +
         "\u0009}" +
         "}";
        runIt(cuText);
    }

    public void testBug3000357() throws Exception {
        String cuText =
            "public class Test\n" +
            "{\n" +
              "public void test()\n" +
              "{\n" +
                "java.util.List l = null;\n" +
                "String[] s = null;" +
                "l.add(l.toArray(s));\n" +
              "}\n" +
            "}\n";
        runIt(cuText);
    }

    public void testBug3000357_2() throws Exception {
        String cuText =
            "import java.util.List;" +
            "public class Test<E>\n" +
            "{\n" +
            "  Test(E e) { }\n" +
            "  Test(E[] e) { int i = 0;/*to indicate correct resolved */ }\n" +
              "public void test()\n" +
              "{\n" +
                "new Test(new Object[1]);" +
              "}\n" +
            "}\n";
        final
         CompilationUnit cu = runIt(cuText).get(0);
        assertEquals(
        ((ConstructorDeclaration)((ErasedConstructor)
        sc.getSourceInfo().getConstructor((ConstructorReference)
        ((MethodDeclaration)cu.getTypeDeclarationAt(0).getMembers().get(2)).getBody().getBody().get(0))).getGenericMethod()).getBody().getBody().size(), 1);
    }

    public void testBug3073325() throws Exception {
        String cuText = "public class X { }";
        CompilationUnit cu = runIt(cuText).get(0);
        Method m = cu.getTypeDeclarationAt(0).getAllMethods().get(0);
        assertEquals("getClass", m.getName());
        m.getProgramModelInfo().getReturnType(m);
    }

    public void testBug3139168() throws Exception {
        String cuText = "class A {\n" +
                "\tvoid foo(java.util.List<String> l) {\n" +
                "\t\tfinal @SuppressWarnings(\"unused\") Object o = null;\n" +
                "\t\tfor (@SuppressWarnings(\"unused\") String s : l) { }\n" +
                "\t}" +
                "}";
        CompilationUnit cu = runIt(cuText).get(0);
        assertEquals(cuText.replaceAll("( |\n|\t)", ""), cu.toSource().replaceAll("( |\n|\t)", ""));
    }

    public void testLineFeedAfterMLComment() throws Exception {
        String cuText = "class A {\n" +
         "    /*Test\n" +
         "    Test\n" +
         "    Test*/\n" +
         "    int f;\n" +
         "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        MiscKit.unindent(cu);
        assertEquals(cuText, cu.toSource());
    }

    public void testConstructorExceptions() throws Exception {
        String cuText = "class A {\n" +
                "    A() throws Exception {}\n" +
                "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        assertEquals(cuText, cu.toSource());
    }

    public void testBug3195113() throws Exception {
        String cuText = "package recoder.CipherTest;\n" +
                "import javax.crypto.Cipher;\n" +
                "public class CryptFactorySymetric {\n" +
                "\tprivate Cipher cipher = null;\n" +
                "}";
        //sc.getProjectSettings().ensureExtensionClassesAreInPath();
        sc.
        getProjectSettings().setProperty(PropertyNames.INPUT_PATH,
                "c:/program files/java/jdk1.5.0_18/jre/lib/*.jar");
        runIt(cuText);
    }

    public void testBug3202867() throws Exception {
        // source code of class file:
        //package p;
        //import javax.persistence.Table;
        //@Table(name="T_P_UTILISATEUR", uniqueConstraints={@javax.persistence.UniqueConstraint(columnNames={"UTI_CODE"})})
        //public class A { }
        ClassFile cf = new ASMBytecodeParser().parseClassFile(new FileInputStream("test/fixedBugs/Bug3202867/A.class"));
        List<AnnotationUseInfo> annots = cf.getAnnotations();
        assertEquals("[@javax.persistence.Table(name=\"T_P_UTILISATEUR\",uniqueConstraints={@javax.persistence.UniqueConstraint(columnNames={\"UTI_CODE\"})})]", annots.toString());
    }

    public void testBug3216466() throws Exception {
        String cuText = "class A { { java.util.Arrays.asList(); }}\n";
        runIt(cuText);
        Arrays.asList();
    }

    public void testBug3219362() throws Exception {
        // source of .class file:
        // import java.util.*;
        // public class X {
        //       public List<Double[]> f = new ArrayList();
        // }
        String cuText = "import java.util.*;\n" +
                        "public class Test {\n" +
                        "  public void method(X u) {\n" +
                        "    u.f.add(new Double[]{0.0d, 0.0d});\n" +
                        "  }\n" +
                        "}\n";
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/Bug3219362");
        runIt(cuText);
    }

    public void testBugNonInnerMemberTypes() throws Exception {
        // the following is valid code. 0.95b stumbles accross it, though (bytecode only).
        String cuText = "import java.util.*;" +
                        "class A {\n" +
                        " void foo(HashMap<String, Object> hm) {\n" +
                        "    for (Map.Entry<String, Object> e : hm.entrySet());\n" +
                        " }\n" +
                        "}\n";
        runIt(new ThrowingErrorHandler(), cuText);
    }

    public void testBugEnhancedForWidening() throws Exception {
        // Semantics checker in 0.95b stumbled across this.
        String cuText = "" +
                "import java.util.*;\n" +
                "class A {\n" +
                "static {\n" +
                "Collection<Integer> numbers = new ArrayList<Integer>();\n" +
                "for (Number n : numbers) {\n" +
                "}\n" +
                "}\n" +
                "}\n";
        runIt(cuText);
    }

    public void testBugXYZ() throws Exception {
        // See https://sourceforge.net/projects/recoder/forums/forum/88905/topic/4584344 (can't reproduce yet)
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        Type t = sc.getNameInfo().getType("java.util.Map.Entry<String,String>");
        TypeReference tr = TypeKit.createTypeReference(sc.getProgramFactory(), t);
        checkParentLinks(tr);
    }

    public void testBug3346233() throws Exception {
        String cuText = "class A {\n ;\n }\n";
        CompilationUnit cu = runIt(cuText).get(0);
        System.out.println(cu.toSource());
    }

    public void testBug3346259() throws Exception {
        sc.getProjectSettings().setErrorHandler(new IgnoringErrorHandler(UnresolvedReferenceException.class, TypingException.class));
        // this first example should be fine according to bug report:
        String cuText = "public class Test implements ITest\n" + "{\n" + "  String test()\n" + "  {\n" + "    return AA;\n" + "  }\n" + "}";
        runIt(cuText);
        // but this one is not:
        cuText
         = "public class Test2 implements ITest\n"
                + "{\n"
                + "  String test()\n"
                + "  {\n"
                + "    return System.out.println(AA);\n"
                + "  }\n"
                + "}\n";
        runIt(cuText);
    }

    public void testBug3450542() throws Exception {
        String cuText = "import java.lang.reflect.Field;\n" +
         "public class test{\n" +
         "public void foo(){\n" +
         "Field[] i;\n" +
         "}\n" +
         "}\n";
        CompilationUnit cu = runIt(cuText).get(0);

        List<Import> unusedImportList = UnitKit.getUnnecessaryImports(sc.getCrossReferenceSourceInfo(), cu);
        assertTrue(unusedImportList.isEmpty());
    }

    public void testBug3450568() throws Exception {
        // TODO can't reproduce this bug (though the comment is moved incorrectly)
        String cuText = "public class Person{\n" +
                "//comment\n" +
                "private String firstName;\n" +
                "}\n";

        CompilationUnit cu = runIt(cuText).get(0);

        Private priv = (Private)cu.getTypeDeclarationAt(0).getMembers().get(0).getDeclarationSpecifiers().get(0);
        Transformation.doDetach(priv);
        sc.getChangeHistory().detached(priv, 0);

        System.out.println(cu.toSource());
    }

    public void testConditionalBoxing_1() throws Exception {
        String cuText = "public class P{{\n" +
                "int i = 0;\n" +
                "boolean b = true;\n" +
                "long l = 5L;\n" +
                "long q = b ? i : l;\n" +
                "}}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer ci = ((ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(0));

        Type t = sc.getSourceInfo().getType(((LocalVariableDeclaration)
                ci.getBody().getStatementAt(3)).getVariableSpecifications().get(0).getInitializer());
        assertEquals(sc.getNameInfo().getLongType(), t);
    }

    public void testConditionalBoxing_2() throws Exception {
        String cuText = "public class P{{\n" +
                "boolean b = true;\n" +
                "long l = 5L;\n" +
                "Object q = b ? b : l;\n" +
                "}}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer ci = ((ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(0));

        Type t = sc.getSourceInfo().getType(((LocalVariableDeclaration)
                ci.getBody().getStatementAt(2)).getVariableSpecifications().get(0).getInitializer());

        assertTrue(t instanceof IntersectionType);
        List<ClassType> istypes = ((IntersectionType)t).getSupertypes();
        assertTrue(istypes.contains(sc.getNameInfo().getClassType("java.lang.Comparable").getErasedType()));
        assertTrue(istypes.contains(sc.getNameInfo().getJavaIoSerializable()));
    }

    public void testConditionalBoxing_3() throws Exception {
        String cuText = "public class P{{\n" +
                "short i = 0;\n" +
                "boolean b = true;\n" +
                "byte l = 5L;\n" +
                "short q = b ? i : l;\n" +
                "}}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer ci = ((ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(0));

        Type t = sc.getSourceInfo().getType(((LocalVariableDeclaration)
                ci.getBody().getStatementAt(3)).getVariableSpecifications().get(0).getInitializer());
        assertEquals(sc.getNameInfo().getShortType(), t);
    }

    public void testConditionalBoxing_4() throws Exception {
        String cuText = "public class P{{\n" +
                "short i = 0;\n" +
                "boolean b = true;\n" +
                "short q = b ? i : 5;\n" +
                "}}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer ci = ((ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(0));

        Type t = sc.getSourceInfo().getType(((LocalVariableDeclaration)
                ci.getBody().getStatementAt(2)).getVariableSpecifications().get(0).getInitializer());
        assertEquals(sc.getNameInfo().getShortType(), t);
    }

    public void testConditionalBoxing_5() throws Exception {
        String cuText = "public class P{{\n" +
                "Short i = new Short(\"5\");\n" +
                "boolean b = true;\n" +
                "short q = b ? i : 5;\n" +
                "}}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer ci = ((ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(0));

        Type t = sc.getSourceInfo().getType(((LocalVariableDeclaration)
                ci.getBody().getStatementAt(2)).getVariableSpecifications().get(0).getInitializer());
        assertEquals(sc.getNameInfo().getShortType(), t);
    }

    public void testInferTypeFromMethodRefSubtype() throws Exception {
        // The following does not work in Recoder 0.95c because the method return ArrayList<K>.
        // If it was List<K>, it would have worked even in 0.95c.
        String cuText = "import java.util.*;\n" +
                "public class P{\n" +
                "{List<String> l = newArrayList();}\n" +
                "<K> ArrayList<K> newArrayList() { return new ArrayList<K>(); }\n" +
                "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer ci = ((ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(0));

        ParameterizedType t = (ParameterizedType)sc.getSourceInfo().getType(((LocalVariableDeclaration)
                ci.getBody().getStatementAt(0)).getVariableSpecifications().get(0).getInitializer());
        assertEquals("java.lang.String", t.getTypeArgs().get(0).getTypeName());
    }

    public void testInferTypeFromMethodRefSubtype_2() throws Exception {
        // Addition to above, just a bit more complicated.
        String cuText = "class X<T, U> { }\n" +
                "class Y<Q> extends X<Q, String> { }\n" +
                "class Z {\n" +
                "\u0009<T> Y<T> newT() { return null; }\n" +
                "\u0009{\n" +
                "\u0009\u0009X<String, String> x = newT();\n" +
                "\u0009}\n" +
                "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer ci = ((ClassInitializer)cu.getTypeDeclarationAt(2).getMembers().get(1));

        ParameterizedType t = (ParameterizedType)sc.getSourceInfo().getType(((LocalVariableDeclaration)
                ci.getBody().getStatementAt(0)).getVariableSpecifications().get(0).getInitializer());
        assertEquals("java.lang.String", t.getTypeArgs().get(0).getTypeName());
    }

    public void testTypeParamRefInEnclosingType() throws Exception {
        // Pre-Java 5: The type of X.this was "X", however, since Java 5 it's "X<E>" which Recoder 0.95c
        // did not properly resolve.
        String cuText = "class X<E> {\n" +
                "E foo() { return null; }\n" +
                "class Y {\n" +
                "{ E e = X.this.foo(); }\n" +
                "}" +
                "}";
        runIt(cuText);
        assertEquals(0, sc.getProjectSettings().getErrorHandler().getErrorCount());
    }

    public void testParameterizedTypeEquals() throws Exception {
        String cuText = "import java.util.*;\n" +
                "class X<S> {\n" +
                "  <K,V> Map<K,V> newMap() { return null; }\n" +
                "  Map<String,List<S>> m = newMap();\n" +
                "  Map<String,List<String>> m2 = newMap();\n" +
                "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        assertEquals("java.util.Map<java.lang.String,java.util.List<S>>", sc.getSourceInfo().getType(((FieldDeclaration)cu.getTypeDeclarationAt(0).getMembers().get(1)).getFieldSpecifications().get(0).getInitializer()).getFullSignature());
        assertEquals("java.util.Map<java.lang.String,java.util.List<java.lang.String>>", sc.getSourceInfo().getType(((FieldDeclaration)cu.getTypeDeclarationAt(0).getMembers().get(2)).getFieldSpecifications().get(0).getInitializer()).getFullSignature());
    }

    public void testArrayOfParameterizedTypeInSignature() throws Exception {
        String cuText = "import java.util.*;\n" +
                        "  class A {\n" +
                        "    private HashMap<Class<? extends Number>, Class<?>[]> cm =\n" +
                        "           new HashMap<Class<? extends Number>, Class<?>[]>();\n" +
                        "    {\n" +
                        "        cm.put(Number.class, new Class<?>[] { }\n" +
                        "     );\n" +
                        "  }\n" +
                        "}\n";
        runIt(cuText).get(0);
    }

    public void testNIgetAnyInTypeArg() throws Exception {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        sc.getNameInfo().getJavaLangClass();
        Type t = sc.getNameInfo().getType("Class<?>[]");
        assertEquals("java.lang.Class<?>[]", t.getFullSignature());
    }

    public void testRawtypesFromBytecode_1() throws Exception {
        // bug in 0.95c
        String cuText = "import java.util.*;\n" +
                "class A {\n" +
                "  void foo(Collection<String> c) { }\n" +
                "  { foo(Collections.EMPTY_LIST); }\n" +
                "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer md = (ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(1);
        Expression e = ((MethodReference)md.getBody().getBody().get(0)).getArguments().get(0);
        assertTrue(sc.getSourceInfo().getType(e) instanceof ErasedType);
    }

    public void testRawtypesFromSourcecode() throws Exception {
        // no bug in 0.95c, just for double-checking that this works properly...
        String cuText = "import java.util.*;\n" +
                "class A {\n" +
                "  List EMPTY_LIST;" +
                "  void foo(Collection<String> c) { }\n" +
                "  { foo(EMPTY_LIST); }\n" +
                "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        ClassInitializer md = (ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(2);
        Expression e = ((MethodReference)md.getBody().getBody().get(0)).getArguments().get(0);
        assertTrue(sc.getSourceInfo().getType(e) instanceof ErasedType);
    }

    public void testRawtypesFromBytecode_2() throws Exception {
        // Same as above, but with a method instead of a field.
        // corresponding source code to the bytecode file:

//    	import java.util.ArrayList;
//    	import java.util.List;
//
//    	public class X {
//    		List giveMeAList() { return new ArrayList(); }
//    		List[] giveMeAListArray() { return new ArrayList[0]; }
//    		final List[] LIST_ARRAY = null;
//    	}

        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/BugRawtypesFromBytecode");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "import java.util.*;\n" +
            "public class Test {\n" +
            "  void foo(Collection<String> c) { }\n" +
            "  {  foo(X.giveMeAList()); }\n" +
            "}\n";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();

        ClassInitializer md = (ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(1);
        Expression e = ((MethodReference)md.getBody().getBody().get(0)).getArguments().get(0);
        assertTrue(sc.getSourceInfo().getType(e) instanceof ErasedType);
    }

    public void testRawtypesFromBytecode_3() throws Exception {
        // Same as above, but with a method array instead of a field.
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/BugRawtypesFromBytecode");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "import java.util.*;\n" +
            "public class Test {\n" +
            "  void foo(Collection<String> c) { }\n" +
            "  {  foo(X.giveMeAListArray()[0]); }\n" +
            "}\n";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();

        ClassInitializer md = (ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(1);
        Expression e = ((MethodReference)md.getBody().getBody().get(0)).getArguments().get(0);
        assertTrue(sc.getSourceInfo().getType(e) instanceof ErasedType);
    }

    public void testRawtypesFromBytecode_4() throws Exception {
        // Same as above, but with a field array instead of a field.
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/BugRawtypesFromBytecode");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        String cuText =
            "import java.util.*;\n" +
            "public class Test {\n" +
            "  void foo(Collection<String> c) { }\n" +
            "  {  foo(X.LIST_ARRAY[0]); }\n" +
            "}\n";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();

        ClassInitializer md = (ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(1);
        Expression e = ((MethodReference)md.getBody().getBody().get(0)).getArguments().get(0);
        assertTrue(sc.getSourceInfo().getType(e) instanceof ErasedType);
    }

    public void testPackageImportBeforeKnownTypeInPackage() throws Exception {
        // It doesn't matter if package "A" is known or not, Recoder can just assume that it's a multi-import from a package.
        // Fixes a bug when no type from the package is known prior to running recoder (i.e., package is yet unknown), but
        // it's a valid package. Recoder should first look at context (import) prior to coding-conventions.
        String cuText = "import A.*;\n" +
                        "import COM.bla.blubb.*;\n";
        runIt(cuText);
    }

    public void testVariableNameLikePackageName() throws Exception {
        String cuText = "package a.b;\n" +
                        "class C {\n" +
                        "  a.b.C a;\n" +
                        "}\n;";
        runIt(cuText);
    }

    public void testInnerClassConstructorReferenceIssueFromBytecode() throws Exception {
        String cuText =
                "class B extends javax.swing.text.html.HTMLDocument {\n" +
                "\u0009Object o = new DefaultDocumentEvent(0, 2, null);\n" +
                "}\n";
        runIt(cuText);
    }

    public void testInnerClassesScoptingIssue_FromBytecode() throws Exception {
        String cuText =
                "class B {\n" +
                "\u0009Object o = new javax.swing.text.html.HTMLDocument().new DefaultDocumentEvent(0, 2, null);\n" +
                "}\n";
        runIt(cuText);
    }

    public void testInnerClassesScoptingIssue_FromSourceCode() throws Exception {
        String cuText =
                "class B {\n" +
                "\u0009class I {}\n" +
                "}\n";
        String cuText2 =
                "class C {\n" +
                "\u0009{ new B().new I(); }\n" +
                "}\n";
        runIt(cuText, cuText2);
    }

    public void testInheritedInnerClassCreation() throws Exception {
        String cuText =
            "class A {\n" +
            "   class Inner {}\n" +
            "}\n" +
            "class B extends A { }\n" +
            "class C { Object o = new B().new Inner(); }\n";
        runIt(cuText);
    }

    public void testXYZ() throws Exception {
        String cuText =
            "package pa;\n" +
            "import pb.*;\n" +
            "public class Bar { " +
            "  { Foo foo = new Foo();\n " +
            "    foo.method(); }\n" +
            "}\n";
        String cuText2 =
            "package pa;\n" +
            "public class Foo {\n" +
            "  void method() { }\n" +
            "}\n";
        String cuText3 =
            "package pb;\n" +
            "public class Foo { }\n";
        runIt(cuText, cuText2, cuText3);
    }

    public void testBugWithTypeBoundOfMethodTPIsTypeParam() throws Exception {
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/BugWithTypeBoundOfMethodTPIsTypeParam/");
        String cuText = "class B {\n" +
                "  { new A().toString(); }\n" +
                "}\n";
        // Source code of class A as below in testBugWithTypeBoundOfMethodTPIsTypeParam_2.
        CompilationUnit cu = runIt(cuText).get(0);
        MethodReference mr = (MethodReference)((ClassInitializer)cu.getTypeDeclarationAt(0).getMembers().get(0)).getBody().getBody().get(0);
        List<Method> meths = ((ClassType)sc.getSourceInfo().getType(mr.getReferencePrefix())).getMethods();
        assertNotNull(meths.get(0).getSignature().get(0));
    }

    public void testBugWithTypeBoundOfMethodTPIsTypeParam_2() throws Exception {
        String cuText =
            "class A<T> {\n" +
            "  <M extends T> void foo(M m) { }\n" +
            "}\n" +
            "class B {\n" +
            "  { new A().toString(); }\n" +
            "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        MethodReference mr = (MethodReference)((ClassInitializer)cu.getTypeDeclarationAt(1).getMembers().get(0)).getBody().getBody().get(0);
        List<Method> meths = ((ClassType)sc.getSourceInfo().getType(mr.getReferencePrefix())).getMethods();
        assertNotNull(meths.get(0).getSignature().get(0));
    }

    public void testReferenceInheritedInnerTypeBeforeURQResolving() throws Exception {
        // Bug: B2.C could be resolved in X's  parameter list.
        // The signature of X() had to be built, though as class A references it.
        // The problem was that B2 was still a URQ and therefore B2.C was
        // not yet registered in the compilation unit's scope.
        String cuText0 =
            "package a.q.w.r;\n" +
            "class A {\n" +
            "  { new X(null); }" +
            "}\n";
        String cuText =
            "package a.q.w.r;\n" +
            "class X {" +
            "  X (B2.C arg) { }\n" +
            "}\n";
        String cuText2 =
            "package a.q.w.r;\n" +
            "class B {\n" +
            "   class C { }\n" +
            "}\n";
        String cuText3 =
            "package a.q.w.r;\n" +
            "class B2 extends B { }\n";
        runIt(cuText0, cuText3, cuText, cuText2).get(0);
    }

    public void testGenericAndParameterizedMethod() throws Exception {
        // A bit tricky: method that is both generic and owned by a parameterized type.
        // Rare, but happens.
        String cuText =
            "class A<T> {\n" +
            "  <X extends T> T foo(X other) { return null; }\n" +
            "  static void bar() { A<Number> a = null; a.foo(5); }\n" +
            "}";
        runIt(cuText);
    }

    public void testGenericAndParameterizedMethod_2() throws Exception {
        // An alternation of above: Should fail as type arguments don't match!
        String cuText =
            "class A<T> {\n" +
            "  <X extends T> T foo(X other) { return null; }\n" +
            "  static void bar() { A<String> a = null; a.foo(5L); }\n" +
            "}";
        try {
            runIt(new ThrowingErrorHandler(), cuText);
            fail("Exception expected!");
        } catch (UnresolvedReferenceException e) {
            // expected!
    }
    }

    public void testGenericAndParameterizedMethod_3() throws Exception {
        // Another alteration, this time, it should be accepted.
        String cuText =
            "class A<T extends Number> {\n" +
            "  <X extends T> T foo(X other) { return null; }\n" +
            "  Object foo(Object other) { return null; }\n" +
            "  static void bar() { A<Number> a = null; a.foo(5L); }\n" +
            "}";
        runIt(new ThrowingErrorHandler(), cuText).get(0);
    }

    public void testGenericAndParameterizedMethod_4() throws Exception {
        // Again valid, just a bit more tricky
        String cuText =
            "class A<T> {\n" +
            "  <X extends T> T foo(java.util.List<X> other) { return null; }\n" +
            "  static void bar() { A<String> a = null; a.foo(new java.util.ArrayList<String>()); }\n" +
            "}";
        runIt(new ThrowingErrorHandler(), cuText).get(0);
    }

    public void testArrayOfIntersectionType() throws Exception {
        // Wow, arrays of intersection types CAN be created:
        String cuText =
            "class X {\n" +
            "\u0009<T> T[] combine(T[] ... ts) { return null; }\n" +
            "\u0009static void foo(Class[] c) { }\n" +
            "\u0009static <T> Class<T>[] getC(Class<T> c) { return null; }" +
            "\u0009{ CharSequence[] o = combine(new String[0], new StringBuilder[0]); }\n" +
            "\u0009{ Object[] o = combine(new String[0], new StringBuilder[0]); }\n" +
            "\u0009{ java.io.Serializable[] o = combine(new String[0], new StringBuilder[0]); }\n" +
            "\u0009{ combine(getC(String.class), getC(StringBuilder.class)); }\n" +
            "}\n";
        runIt(cuText);
    }

    public void testNullInferenceIssue() throws Exception {
        // Don't attempt to create array of "null":
        String cuText =
            "class X {\n" +
            "\u0009static <T> T foo(T ... ts) { return null; }\n" +
            "\u0009{ foo(null); }" +
            "}";
        runIt(new ThrowingErrorHandler(), cuText);
    }

    public void testNullInferenceIssue_2() throws Exception {
        String cuText =
            "class X {\n" +
            "\u0009static <T> T foo(T o1, T o2) { return null; }\n" +
            "\u0009{ String s = foo(\"\", null); }" +
            "}";
        runIt(new ThrowingErrorHandler(), cuText);
    }

    public void testEraseTypesInArrays() throws Exception {
        String cuText =
            "import java.util.*;\n" +
            "class F<T> extends ArrayList<T> { }\n" +

            "class A {\n" +
            "\u0009A(String s, List<String> l) { }\n" +
            "}\n" +

            "class B {\n" +
            "\u0009F[] fs;" +
            "\u0009{ new A(\"\", fs[0]); } \n" +
            "}\n";
        runIt(cuText);
    }

    public void testBoundMatchIssueIfBoundIsTypeParam() throws Exception {
        String cuText =
                "import java.util.*;\n" +
                "class A {\n" +
                "\u0009static { foo(new ArrayList<Object>()); }" +
                "\u0009static <T, U extends T> Collection<T> foo(List<U> l) { return new HashSet<T>(l); }\n " +
                "}";
        runIt(cuText);
    }

    public void testAnyIssue() throws Exception {
        String cuText =
                "import java.util.*;\n" +
                "class C<T extends Number> {\n" +
                "   static T create() { return null; }\n" +
                "\u0009void foo(C<?> c) {\n" +
                "\u0009\u0009List<Number> ln = null;\n" +
                "\u0009\u0009ln.add(c.create());\n" +
                "\u0009}" +
                "}\n";
        runIt(cuText);
    }

    public void testLengthIssue() throws Exception {
        // never in any released version, just for one day in SVN: "Any"-type argument would remove the dimension.
        String cuText = "class A {\n" +
                "\u0009void foo(java.lang.reflect.Method m) {\n" +
                "\u0009\u0009int x = m.getParameterTypes().length;\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
    }

    public void testClassLiteralIssue() throws Exception {
        // Class literals: Do *not* use raw/erased types.
        String cuText = "class A {" +
            "Class<Number> clazz;\n" +
            "static <T> T foo(final T o, final Class<T> c) { return null; }\n" +
            "void foo(Class<Class<?>> c)\n" +
            "{ clazz = foo(c, Class.class); }\n" +
            "}";
        runIt(cuText);
    }

    public void testResolvedTypeParameterIssue() throws Exception {
        String cuText = "class X<T> {\n" +
                "<X extends T> void foo(Class<X> c) {\n" +
                " X v = bar(c);\n" +
                "}\n" +
                "<X extends T> X bar(Class<X> t) { return null; }\n" +
                "}\n";
        runIt(cuText);
    }

    public void testTypeParameterMatchingIssue() throws Exception {
        // another one of those...
        String cuText = "interface I<T> { }";
        String cuText2 =
                "class C {\n" +
                "\u0009public static <T> I<T> foo( final I<T> a, final I<? extends T> b ) { return null; }\n" +
                "}\n";
        String cuText3 =
                "class D<T> implements I<T> {\n" +
                "\u0009public <X extends T> I<T> andThen( I<X> other ) {\n" +
                "\u0009\u0009return C.foo(this, other);\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText, cuText2, cuText3);
    }

    public void testScopingBugWithSwitch() throws Exception {
        String cuText = "class A {\n" +
                "\u0009String s;\n" +
                "\u0009void foo(String s) { }\n" +
                "\u0009void bar() {\n" +
                "\u0009\u0009switch(3) {\n" +
                "\u0009\u0009\u0009case 0: foo(s);\n" +
                "\u0009\u0009\u0009case 1: char s;\n" +
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
    }

    public void testScopingBugWithSwitch_2() throws Exception {
        String cuText = "class A {\n" +
                "\u0009String s;\n" +
                "\u0009void foo(String s) { }\n" +
                "\u0009void bar() {\n" +
                "\u0009\u0009switch(3) {\n" +
                "\u0009\u0009\u0009case 0: foo(s);\n" +
                "\u0009\u0009\u0009\u0009\u0009char s;\n" +
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
    }

    public void testScopingBugWithSwitch_3() throws Exception {
        String cuText = "class A {\n" +
                "\u0009String s;\n" +
                "\u0009void foo(String s) { }\n" +
                "\u0009void bar() {\n" +
                "\u0009\u0009switch(3) {\n" +
                "\u0009\u0009\u0009case 0: { foo(s); }\n" +
                "\u0009\u0009\u0009\u0009\u0009char s;\n" +
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
    }

    public void testScopingBugWithSwitch_4() throws Exception {
        String cuText = "class A {\n" +
                "\u0009void foo(String s) { }\n" +
                "\u0009void bar() {\n" +
                "\u0009\u0009switch(3) {\n" +
                "\u0009\u0009\u0009case 0: char s;\n" +
                "\u0009\u0009\u0009case 1:\u0009s = 'a';\n" +
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
    }

    public void testIssueInferenceWithExtends() throws Exception {
        // TODO Both javac and eclipse infer "String" for V in call to A.l(), though
        // I don't understand why. Must be because of the 'extends T', so that T gets the same as V.
        // Check out the theory.
        String cuText =
                "import java.util.*;\n" +
                "class A {\n " +
                "\u0009public static <T, V extends T> List<T> l(V... elements) {" +
                "\u0009\u0009return new ArrayList<T>(Arrays.asList(elements));\n" +
                "\u0009}\n" +
                "}\n";
        String cuText2 =
                "import java.util.*;\n" +
                "class B{\n" +
                "\u0009{ foo(A.l(new String[0])); }\n" +
                "\u0009static void foo(List<String> s) { }" +
                "}\n";
        runIt(cuText, cuText2);
    }

    public void testIntersectionTypeAsArgument() throws Exception {
        String cuText =
                "class C {\n " +
                "\u0009void foo(J j) { }" +
                "\u0009void bar(boolean b) { foo(b ? new A() : new B()); }" +
                "}\n" +
                "interface I { }\n" +
                "interface J { }\n" +
                "class A implements I, J { }\n" +
                "class B implements I, J { }\n";
        runIt(cuText);
    }

    public void testTypeInferenceIssue() throws Exception {
        String cuText =
                "interface I<T> extends Comparable<T> { }\n" +
                "class X<S> {\n" +
                "\u0009static <T> I<T> getI() { return null; }\n" +
                "\u0009Object o = getI();\n" +
                "\u0009Comparable<S> s = getI();\n" +
                "}";
        runIt(cuText);
    }

    public void testIntersectionTypeIssueWithConditionalAndTypeParameters() throws Exception {
        String cuText =
                "import java.util.*;\n" +
                "class A<T> {\n" +
                "  Iterator<? extends T> iter;\n" +
                "  T getT() { return null; }\n" +
                "  T foo(boolean b) { return b ? iter.next() : getT(); }\n" +
                "}\n";
        runIt(cuText);
    }

    public void testYetAnotherGenericsIssue() throws Exception {
        String cuText =
                "import java.util.*;\n" +
                "class A {\n" +
                "  static <T> T cast (Object o) { return (T)o; }\n" +
                "  ArrayList<String> a1 = new ArrayList<String>();\n" +
                "  ArrayList<String> a2 = cast(a1.clone());\n" +
                "}\n";
        runIt(cuText);
    }


    public void testVisibilityAndOverloading_Constr() throws Exception {
        String cuText =
                "public class A {\n" +
                "  public A(String s) { }\n" +
                "  private A(Integer i) { }\n" +
                "}\n";
        String cuText2 =
                "package a;" +
                "class B {\n" +
                "\u0009{ new A(null); }\n" +
                "}\n";
        runIt(cuText, cuText2);
    }

    public void testVisibilityAndOverloading_Meth() throws Exception {
        String cuText =
                "public class A {\n" +
                "  public void a(String s) { }\n" +
                "  private void a(Integer i) { }\n" +
                "}\n";
        String cuText2 =
                "package a;" +
                "class B {\n" +
                "\u0009{ new A().a(null); }\n" +
                "}\n";
        runIt(cuText, cuText2);
    }

    public void testVisibilityIssue() throws Exception {
        String cuText =
                "import java.io.*;\n" +
                "class X extends ObjectInputStream {\n" +
                "\u0009protected X() throws IOException, SecurityException {\n" +
                "\u0009\u0009new Object() {\n" +
                "\u0009\u0009\u0009public void foo() {\n" +
                "\u0009\u0009\u0009\u0009enableResolveObject(true);\n" +
                "\u0009\u0009\u0009}\n" +
                "\u0009\u0009};\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
    }

    public void testVisibilityIssue_2() throws Exception {
        String cuText =
                "import java.io.*;\n" +
                "class X extends ObjectInputStream {\n" +
                "\u0009protected X() throws IOException, SecurityException { }\n" +
                "\u0009class Y {\n" +
                "\u0009\u0009{ " +
                "\u0009\u0009\u0009new Object() {\n" +
                "\u0009\u0009\u0009\u0009public void foo() {\n" +
                "\u0009\u0009\u0009\u0009\u0009enableResolveObject(true);\n" +
                "\u0009\u0009\u0009\u0009}\n" +
                "\u0009\u0009\u0009};\n" +
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
    }

    public void testVisibilityIssue_3() throws Exception {
        String cuText =
                "class A {\n" +
                "\u0009{\n" +
                "\u0009\u0009new AssertionError(\"Jaha\");" + // most specific, but not visible...
                "\u0009}\n" +
                "}\n" +
                "" +
                "" +
                "";
        runIt(cuText);
    }

    public void testSelfTypeBeforeImport_1() throws Exception {
        String cuText =
                "package p;\n" +
                "public class A { " +
                "\u0009public static class Inner {" +
                "\u0009\u0009private Inner() { } " +
                "\u0009}" +
                "}\n" +
                "";
        String cuText2 =
                "package p2;\n" +
                "import p.A;\n" +
                "class A extends p.A { " +
                "\u0009static class Inner {" +
                "\u0009\u0009private static Inner a = new Inner(); " +
                "\u0009}" +
                "}\n";
        runIt(cuText, cuText2);
    }

    public void testInferSuperTypeArgs() throws Exception {
        String cuText =
            "import java.util.*;\n" +
            "class A {\n" +
            "   static <V,W> boolean foo(Map<? super V, ? super W> src, Map<V, W> dst) { " +
            "\u0009\u0009boolean b = bar(src, dst.keySet());\n" +
            "\u0009\u0009return b;\n" +
            "\u0009}\n" +
            "   static <V,W> boolean bar(Map<? super V, ? super W> src, Collection<? extends V> col) { return true; }\n" +
            "}\n";
        runIt(cuText);
    }

    public void testInferSuperTypeArgs_2() throws Exception {
        String cuText =
            "import java.util.*;\n" +
            "class A {\n" +
            "   static <V,W> void foo(Map<String, String> src) { " +
            "\u0009\u0009String s = bar(src);\n " +
            "\u0009}\n" +
            "   static <V,W> V bar(Map<? super V, ? super W> src) { return null; }\n" +
            "}\n";
        runIt(cuText);
    }

    public void testTypeArgMatchesWithTypeParams() throws Exception {
        String cuText =
            "import java.util.*;\n" +
            "class A<D, T extends List<D>> { }\n" +
            "class B<Q, T extends ArrayList<Q>> extends A<Q, T> { }\n";
        runIt(cuText);
    }

    public void testTypeArgMatchesWithTypeParams_2() throws Exception {
        String cuText =
            "import java.util.*;\n" +
            "class A<D extends List<D>> { }\n" +
            "class B<Q extends ArrayList<Q>> extends A<Q> { }\n";
        runIt(cuText);
    }

    public void testBugWithMultipleTypeArgsInBounds() throws Exception {
        sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, "test/fixedBugs/BugWithMultipleTypeArgsInBounds");
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        // The type in C is declared as
        // C<T extends List<String>, U extends Map<Number, Object>>
        assertEquals(
        "C<T extends java.util.List<java.lang.String>,U extends java.util.Map<java.lang.Number,java.lang.Object>>",
                sc.getNameInfo().getClassType("C").getFullSignature());
    }

    public void testBugWithIllegalErasing() throws Exception {
        String cuText =
         "import java.util.*;" +
         "class X {\n" +
         "\u0009List<String> getList() { return null; }\n" +
         "}\n" +
         "class A {\n" +
         "\u0009X getX() { return null; }\n" +
         "}\n" +
         "class B<T> extends A { } \n" +
         "class C extends B {\n" +
         "\u0009void foo() {\n" +
         "\u0009\u0009getX().getList().get(0).toLowerCase();\n" +
         "\u0009}\n" +
         "}\n";
        runIt(cuText);
    }

    public void testOverrideWithTypeArg() throws Exception {
        String cuText =
            "import java.util.*;\n" +
            "class A { <T> void foo(List<T> l, T t) { } }\n" +
            "class B extends A {\n" +
            "\u0009<T> void foo(List<T> l, T t) { }\n" +
            "\u0009void bar() { foo(new ArrayList<Boolean>(), true); }\n" +
            "}\n";
        runIt(cuText);
    }

    public void testUnusedTypeParameterInGenericMethod() throws Exception {
        String cuText =
            "import java.util.*;\n" +
            "class B {\n" +
            "\u0009public static <V,E,G extends Map<V,E>> Collection<G>\n" +
            "\u0009\u0009bar(Collection<? extends Collection<V>>\n" +
            "\u0009\u0009\u0009vertex_collections, G graph) { return null; }\n" +
            "}";
        String cuText2 =
            "import java.util.*;\n" +
            "class A<V,E> {\n" +
            "\u0009void foo(Map<V, E> map) {\n" +
            "\u0009\u0009Set<Set<V>> component_vertices = null;\n" +
            "\u0009\u0009Collection<Map<V,E>> components =\n" +
            "\u0009\u0009\u0009B.bar(component_vertices, map);\n" +
            "\u0009}\n" +
            "}\n";
        runIt(cuText, cuText2);
    }

    public void testInferFromExtendsAndContext() throws Exception {
        String cuText =
            "class A {\n" +
            " \u0009static <T> T getT() { return null; }\n" +
            " \u0009void foo() {\n" +
            " \u0009\u0009Integer i = getT();\n" +
            "\u0009}\n" +
            "}\n";
        runIt(cuText);
    }

    public void testRawGetClassReference() throws Exception {
        String cuText =
            "import java.util.Map;\n" +
            "class A {\n" +
            "void foo(Map map) throws Exception {\n" +
            "Map newMap = map.getClass().newInstance();\n" +
            "}\n" +
            "}\n";
        runIt(cuText);
    }

    public void testIntersectionIssueWithRaw() throws Exception {
        String cuText =
            "class A {\n" +
            "\u0009void foo(Class<?> c1, Class<? extends Number> c2, boolean b) {\n" +
            "\u0009\u0009Class c = null;\n" +
            "\u0009\u0009c2 = b ? c: c1;\n" +
            "\u0009}\n" +
            "}\n";
        runIt(cuText);
    }

    public void testBug99() throws Exception {
        String cuText =
                "package test;\n" +
                "abstract class SampleServiceCollection<Factory> {\n" +
                "\u0009protected class ServiceCall<RequestType, ResponseType> {\n" +
                "\u0009\u0009public ResponseType execute(String a, String b, String c) {\n" +
                "\u0009\u0009\u0009return null;\n" +
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n" +
                "public class SampleServices extends SampleServiceCollection<Integer> {\n" +
                "\u0009public Integer query(final String[] attributes, final String[] actions) {\n" +
                "\u0009\u0009return new ServiceCall<String, Integer>().execute(\"a\", \"b\", \"c\");\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
    }


    public void testBug103() throws Exception {
        String cuText =
                "import static java.lang.T.*;\n" +
                "import static java.lang.Q.methodInT;\n" +
                "class X {\n" +
                "  int l = methodInT();\n" +
                "}\n";
        runIt(new SilentErrorHandler(3), cuText); // expect 2x TypeReference and once MethodReference not resolved
    }

    public void testInferenceWithWildcardArrayType() throws Exception {
        String cuText =
                "import java.util.*;\n" +
                "class A<T> {\n" +
                "  static <E> A<E[]> foo(List<E> p) { return null; }\n" +
                "}\n" +
                "class B {\n" +
                "  List<?> l;\n" +
                "  A<?> f = A.foo(l);\n" +
                "}\n";
        runIt(cuText);
    }

    public void testUseArraysOfTypeParamsInTypeArgs() throws Exception {
        String cuText =
                "import java.util.*;\n" +
                "import java.util.Map.Entry;\n" +
                "class VCS {\n" +
                "\u0009private VCLM map = new VCLM();\n" +
                "\u0009void foo() {\n" +
                "\u0009\u0009 for (Entry<String, VCL[]> entry : this.map.getEntries()) { }\n" +
                "\u0009}\n" +
                "   private static final class VCLM extends CLM<VCL> { }\n" +
                "}\n" +
                "abstract class CLM<L extends EventListener> { \n" +
                "   public final Set<Entry<String, L[]>> getEntries() { return null; }\n" +
                "}\n" +
                "interface VCL extends java.util.EventListener { }\n";
        runIt(cuText);
    }
}
