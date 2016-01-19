/*
 * Created on 11.03.2005

 *
 * This file is part of the RECODER library and protected by the LGPL.
 */
package recoder.testsuite.transformation;
import java.util.List;

import recoder.abstraction.ClassType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.java.CompilationUnit;
import recoder.java.Identifier;
import recoder.java.StatementBlock;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.VisibilityModifier;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.kit.NoProblem;
import recoder.kit.transformation.java5to4.EnhancedFor2For;
import recoder.kit.transformation.java5to4.FloatingPoints;
import recoder.kit.transformation.java5to4.MakeConditionalCompatible;
import recoder.kit.transformation.java5to4.RemoveAnnotations;
import recoder.kit.transformation.java5to4.RemoveCoVariantReturnTypes;
import recoder.kit.transformation.java5to4.RemoveStaticImports;
import recoder.kit.transformation.java5to4.ReplaceEnums;
import recoder.kit.transformation.java5to4.ResolveBoxing;
import recoder.kit.transformation.java5to4.ResolveGenerics;
import recoder.kit.transformation.java5to4.ResolveVarArgs;
import recoder.kit.transformation.java5to4.methodRepl.ApplyRetrotranslatorLibs;
import recoder.kit.transformation.java5to4.methodRepl.ReplaceEmptyCollections;
import recoder.service.ChangeHistory;
import recoder.service.NameInfo;
import recoder.service.TooManyErrorsException;
import recoder.testsuite.RecoderTestCase;
import application.Obfuscate;


/**
 * @author Tobias Gutzmann
 *
 * Tests some transformations.
 */
public class TransformationTests extends RecoderTestCase {
    private boolean silent = true;


    public void testObfuscater() {
        setPath("test/transformation/obfuscate");
        runIt();
        Obfuscate of = new Obfuscate(sc);
        if (of.analyze() instanceof NoProblem)
            of.transform();
        // TODO write back and compare!
    }

    public void testReadOnly() {
        setPath("test/transformation/readOnly");
        runIt();

        List<TypeReference> trl = sc.getCrossReferenceSourceInfo().getReferences(sc.getNameInfo().getType("Test"));
        for (int i = 0; i < trl.size(); i++) {
            TypeReference tr = trl.get(i);
            if (!silent)
                System.out.println(tr.toSource());
        }
    }

    private void defaultConstructorReferences(VisibilityModifier vm) {
        setPath("test/transformation/defaultCons");
        runIt();
        // add a constructor now
        ChangeHistory ch = sc.getChangeHistory();
        NameInfo ni = sc.getNameInfo();
        ClassDeclaration cd = (ClassDeclaration)ni.getType("DefaultCons");
        ConstructorDeclaration cons = new ConstructorDeclaration(vm, new Identifier("DefaultCons"),
                null, null, new StatementBlock());
        cd.getMembers().add(cons);
        cd.makeParentRoleValid();
        ch.attached(cons);
        ch.updateModel();
        for (CompilationUnit cu: sc.getSourceFileRepository().getCompilationUnits()) {
            cu.validateAll();
        }
        // now, the default constructor no longer exists.
        // The added constructor should be referenced, however, if it is still visible,
        // and an error should occur if not.

        int newref = sc.getCrossReferenceSourceInfo().getReferences(cons).size();

        MethodDeclaration md = (MethodDeclaration)cd.getMethods().get(0); // main()
    }

    public void testDefaultConstructorReferences1() {
        defaultConstructorReferences(new Public());
    }

    public void testDefaultConstructorReferences2() {
        try {
            defaultConstructorReferences(new Private());
            fail("Shouldn't get here.");
        } catch (TooManyErrorsException e) {
            // expected!
    }
    }

    public void testReplaceEmptyCollections() {
        setPath("test/transformation/emptyCollections");
        runIt();
        ClassType coll = sc.getNameInfo().getClassType("java.util.Collections");
        checkFRefCnt(coll, "EMPTY_LIST", 0);
        checkFRefCnt(coll, "EMPTY_MAP", 0);
        checkFRefCnt(coll, "EMPTY_SET", 0);
        checkFRefCnt(coll, "emptyList", 2);
        checkFRefCnt(coll, "emptyMap", 2);
        checkFRefCnt(coll, "emptySet", 2);
        new ReplaceEmptyCollections(sc).execute();
        sc.getChangeHistory().updateModel();
        checkFRefCnt(coll, "EMPTY_LIST", 2);
        checkFRefCnt(coll, "EMPTY_MAP", 2);
        checkFRefCnt(coll, "EMPTY_SET", 2);
        checkFRefCnt(coll, "emptyList", 0);
        checkFRefCnt(coll, "emptyMap", 0);
        checkFRefCnt(coll, "emptySet", 0);
    }
    // helper method for testReplaceEmptyCollections
    private void checkFRefCnt(ClassType coll, String memberName, int cnt) {
        boolean found = false;
        for (Field f: coll.getFields()) {
            if (memberName.equals(f.getName())) {
                assertTrue(cnt == sc.getCrossReferenceSourceInfo().getReferences(f).size());
                found = true;
                break;
            }
        }
        if (!found) {
            for (Method m: coll.getMethods()) {
                if (memberName.equals(m.getName())) {
                    assertTrue(cnt == sc.getCrossReferenceSourceInfo().getReferences(m).size());
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
    }

    // Testing for different issues in java5to4:
    public void testXXX() throws Exception {
        String cuText =
            "import java.util.*;" +
            "class X {\n" +
            "public int hashCode() {\n" +
            "List<Collection> l = null;" +
            "String s = null;" +
            "return (l != null ? l : s).hashCode();" +
            "}}";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        // TODO move to TestTransformations
    }

    public void testZZZ() throws Exception {
        String cuText1 =
            "abstract class A<T> {" +
            "abstract void foo(T t);" +
            "}" +
            "class B extends A<String> {" +
            "void foo(String s) {}" +
            "}" +
            "class C<T> extends A<T> {" +
            "void foo(String s) { }" +
            "void foo(T t) { }" +
            "}";
        List<CompilationUnit> cus = runIt(cuText1);
        new ResolveGenerics(sc, cus).execute();
        // TODO move to TestTransformations
    }

    public void testYASDF() throws Exception {
        String cuText1 =
            "class A<Q> {\n" +
            "\u0009<A> A foo(Class<A> c) { return null; }\n" +
            "}\n" +
            "class B {\n" +
            "\u0009void bar(A<?> a) {\n" +
            "\u0009\u0009String s = a.foo(String.class);\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText1);
        new ResolveGenerics(sc, cus).execute();
        // TODO move to TestTransformations
    }

    public void testQQQ() throws Exception {
        String cuText1 =
            "import java.io.*;\n" +
            "class A {\n" +
            "<T extends Exception> int foo(Class<T> exc) throws T { return 0; }\n" +
            "void bar() throws IOException {\n" +
            "foo(IOException.class);\n" +
            "int i = foo(IOException.class);\n" +
            "}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText1);
        new ResolveGenerics(sc, cus).execute();
        // TODO move to TestTransformations
    }
    public void testWrongStaticImport() throws Exception {
        String cuText1 =
            "package a; \n public class X { protected int i; }\n";
        String cuText2 =
            "package b;\n" +
            "import static a.X.*;\n" +
            "import static a.X.i;\n" +
            "class Y extends a.X {\n" +
            "\u0009void foo() { int j = i; }\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText1, cuText2);
        new RemoveStaticImports(sc, cus).execute();
        assertEquals(
                "packageb;classYextendsa.X{voidfoo(){intj=i;}}",
                cus.get(1).toSource().replaceAll("( |\n)", ""));
    }

    public void testGenericsAndCovariant() throws Exception {
        String cuText1 =
            "class RAPII extends APII<String> implements RAPI { }\n" +
            "class APII<T> extends PII<T> { }\n" +
            "class PII<T> { T foo() { } }\n" +
            "interface RAPI extends RPI {}\n" +
            "interface RPI { String foo(); }";
        List<CompilationUnit> cus = runIt(cuText1);
        new ResolveGenerics(sc, cus).execute();
        new RemoveCoVariantReturnTypes(sc, cus).execute();
        assertEquals(
                "classRAPIIextendsAPIIimplementsRAPI{}" +
                "classAPIIextendsPII{}" +
                "classPII{Objectfoo(){}}" +
                "interfaceRAPIextendsRPI{}" +
                "interfaceRPI{Objectfoo();}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }



    public void testChangeSigsWhenResolvingGenerics() throws Exception {
        String cuText1 =
            "interface I<T> { <A extends Number> A foo(Class<A> s, T t); }\n" +
            "interface J<T> extends I<T> { }\n" +
            "abstract class A<T> implements I<T> {" +
            "}" +
            "class B extends A<String> {" +
            "public <A extends Number> A foo(Class<A> s, String t) {}" +
            "}\n" +
            "class C extends A<String> implements J<String> {" +
            "public <A extends Number> A foo(Class<A> s, String t) {}" +
            "}";
        List<CompilationUnit> cus = runIt(cuText1);
        new ResolveGenerics(sc, cus).execute();
        assertEquals(
                "interfaceI{Numberfoo(Classs,Objectt);}" +
                "interfaceJextendsI{}" +
                "abstractclassAimplementsI{}" +
                "classBextendsA{publicNumberfoo(Classs,Objectt){}}" +
                "classCextendsAimplementsJ{publicNumberfoo(Classs,Objectt){}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testApplicableMethodsIssue() throws Exception {
        String cuText1 =
            "import java.util.*;" +
            "class A {\n" +
            "\u0009void foo() {" +
            "\u0009\u0009List<String> l = new ArrayList<String>();\n" +
            "\u0009\u0009l.toArray(new String[0]);" +
            "\u0009}" +
            "}";
        // TODO test somehow that DefaultProgramModelInfo.internalGetApplicableMethods returns
        // only 1 method (not 2 or more). That would be a bug (!)
        List<CompilationUnit> cus = runIt(cuText1);
        MethodDeclaration md = (MethodDeclaration)cus.get(0).getTypeDeclarationAt(0).getMembers().get(0);
        MethodReference mr = (MethodReference)md.getBody().getBody().get(1);
        List<Method> m = sc.getSourceInfo().getMethods(mr);
    }

    public void testArrayIssue() throws Exception {
        String cuText1 =
            "abstract class A<T> {" +
            "\u0009abstract void foo(T a);\n" +
            "}\n" +
            "class B<T> extends A<T[]> {\n" +
            "\u0009void foo(T a[]) { int i = a.length; }\n" +
            "}";
        List<CompilationUnit> cus = runIt(cuText1);
        // actual test!
        MethodDeclaration md = (MethodDeclaration)cus.get(0).getTypeDeclarationAt(1).getMembers().get(0);
        List<Method> redef = MethodKit.getAllRedefinedMethods(md);
        assertEquals(1, redef.size());
        new ResolveGenerics(sc, cus).execute();
        assertEquals(
                "abstractclassA{abstractvoidfoo(Objecta);}" +
                "classBextendsA{voidfoo(Objecta){inti=((Object[])a).length;}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }


    public void testJustAnotherCovariantIssue() throws Exception {
        String cuText1 =
            "class A {\n" +
            "\u0009Number foo() { return null;}\n" +
            "}\n" +
            "class B extends A {\n" +
            "\u0009Integer foo() { return null;}\n" +
            "\u0009void bar(B b) { Number n = b.foo(); }\n" +
            "\u0009void foobar(B b) { Integer i = b.foo(); }\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText1);
        new RemoveCoVariantReturnTypes(sc, cus).execute();
        assertEquals(
                "classA{Numberfoo(){returnnull;}}" +
                "classBextendsA{Numberfoo(){returnnull;}voidbar(Bb){Numbern=b.foo();}" +
                "voidfoobar(Bb){Integeri=((Integer)b.foo());}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }



    public void testGenericsSignatureChanges() throws Exception {
        String cuText1 =
            "interface A<T, U> {\n" +
            "\u0009void foo(T t, U u);\n" +
            "}\n" +
            "interface B<T> extends A<T, String>{\n" +
            "\u0009void foo(T t, String s);\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText1);
        new ResolveGenerics(sc, cus).execute();
        assertEquals(
                "interfaceA{voidfoo(Objectt,Objectu);}" +
                "interfaceBextendsA{voidfoo(Objectt,Objects);}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testResolveGenericsWithVarArgsPresent() throws Exception {
        String cuText1 =
            "class A<T extends Number> {\n" +
            "\u0009void foo(T ... t) {\n " +
            "\u0009\u0009int i = t.length;\n" +
            "\u0009\u0009t[0].intValue();\n" +
            "\u0009\u0009bar(t);\n " +
            "\u0009\u0009foobar(t[0]);\n" +
            "\u0009};\n" +
            "\u0009void bar(Number[] o) { }\n" +
            "\u0009void foobar(Number n) { }\n" +
            "}\n" +
            "class Q {\n" +
            "\u0009void foo(A<Integer> a) {\n" +
            "\u0009\u0009a.foo(new Integer(3));\n" +
            "\u0009}" +
            "}";

        List<CompilationUnit> cus = runIt(cuText1);
        new ResolveGenerics(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals(
                "classA{voidfoo(Number...t){" +
                "inti=t.length;t[0].intValue();bar(t);foobar(t[0]);}" +
                "voidbar(Number[]o){}voidfoobar(Numbern){}}" +
                "classQ{voidfoo(Aa){a.foo(newInteger(3));}}" +
                "" +
                "",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testResolveGenericsWithVarArgsPresent_2() throws Exception {
        String cuText1 =
            "class A<T> {\n" +
            "\u0009void foo(T ... t) {\n " +
            "\u0009}\n" +
            "}\n" +
            "class Q extends A<String> {\n" +
            "\u0009void foo(String ... t) { }\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText1);
        new ResolveGenerics(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classA{voidfoo(Object...t){}}classQextendsA{voidfoo(Object...t){}}",
            cus.get(0).toSource().replaceAll("( |\n)", ""));
    }


    public void testCovariantWithMemberTypes() throws Exception {
        String cuText1 =
            "interface T { }\n" +
            "interface CT { CT foo(); }\n" +
            "class UT implements T { }\n" +
            "class N {\n" +
            "\u0009static class UCT extends UT implements CT { UCT foo(); }\n" +
            "}";
        List<CompilationUnit> cus = runIt(cuText1);
        new RemoveCoVariantReturnTypes(sc, cus).execute();
        assertEquals(
                "interfaceT{}interfaceCT{CTfoo();}classUTimplementsT{}" +
                "classN{staticclassUCTextendsUTimplementsCT{CTfoo();}}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testCovariantArraysAndClone() throws Exception {
        String cuText1 =
            "import recoder.list.generic.*;\n" +
            "class A {" +
            "\u0009ASTArrayList a1 = null;\n" +
            "\u0009ASTArrayList a2 = a1.clone();\n" +
            "\u0009String[] t = new String[1];\n" +
            "\u0009String[] s = t.clone();\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText1);
        new RemoveCoVariantReturnTypes(sc, cus).execute();
        assertEquals(
                "importrecoder.list.generic.*;classA{ASTArrayLista1=null;" +
                "ASTArrayLista2=((ASTArrayList)a1.clone());String[]t=newString[1];" +
                "String[]s=((String[])t.clone());}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testCovariantRelatedMethods() throws Exception {
        String cuText1 =
            "interface I { java.io.Serializable f(); }\n" +
            "interface J { Number f(); }\n" +
            "interface K { Number f(); }\n" +
            "class C implements I, J { public Number f() { return null; }}\n" +
            "class D implements J, K { public Number f() { return null; }}\n" +
            "class E { private K d = new K() { public Number f() { return null; }};}\n";
        List<CompilationUnit> cus = runIt(cuText1);
        new RemoveCoVariantReturnTypes(sc, cus).execute();
        assertEquals(
                "interfaceI{java.io.Serializablef();}" +
                "interfaceJ{java.io.Serializablef();}" +
                "interfaceK{java.io.Serializablef();}" +
                "classCimplementsI,J{publicjava.io.Serializablef(){returnnull;}}" +
                "classDimplementsJ,K{publicjava.io.Serializablef(){returnnull;}}" +
                "classE{privateKd=newK(){publicjava.io.Serializablef(){returnnull;}};}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testConditionalWithBoxing() throws Exception {
        String cuText =
            "class A { void foo(String s) {\n" +
            "\u0009Boolean b = null;\n" +
            "\u0009boolean c = s == null ? false : b;\n" +
            "}}";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        new ResolveBoxing(sc, cus).execute();
        assertEquals("classA{voidfoo(Strings){Booleanb=null;booleanc=s==null?false:b.booleanValue();}}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testBoxingIssue() throws Exception {
        String cuText =
            "class A<T> {\n" +
            "\u0009void set(T i) { }\n" +
            "\u0009T get() { return null; }\n" +
            "\u0009void foo() {\n " +
            "\u0009\u0009A<Integer> a = new A<Integer>();\n" +
            "\u0009\u0009a.set(a.get() + 1);\n" +
            "\u0009}\n" +
            "}";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveGenerics(sc, cus).execute();
        new ResolveBoxing(sc, cus).execute();
        assertEquals("classA{voidset(Objecti){}Objectget(){returnnull;}" +
                "voidfoo(){Aa=newA();a.set(Integer.valueOf((((Integer)a.get()).intValue())+1));}}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testBoxingIssue2() throws Exception {
        String cuText =
            "class A {\n" +
            "\u0009{\n" +
            "\u0009\u0009Boolean v = true;\n" +
            "\u0009\u0009int i = (v) ? 1 : 0;\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveBoxing(sc, cus).execute();
        new ResolveBoxing(sc, cus).execute();
        assertEquals(
                "classA{{Booleanv=Boolean.valueOf(true);inti=(v.booleanValue())?1:0;}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }
    public void testBoxingIssue3() throws Exception {
        String cuText =
            "class A {\n" +
            "\u0009void foo(Number n) {\n" +
            "\u0009\u0009double d = (n instanceof Float) ? (Float)n : (Double)n;\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        new ResolveBoxing(sc, cus).execute();
        new ResolveBoxing(sc, cus).execute();
        assertEquals(
                "classA{voidfoo(Numbern){doubled=(ninstanceofFloat)?(double)((Float)n)." +
                "floatValue():((Double)n).doubleValue();}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }
    public void testMakeSureVarArgsBeforeBoxing() throws Exception {
        String cuText =
            "class A {\n" +
            "\u0009void bar(Object ... o) {\n" +
            "\u0009\u0009bar(new Object(), true, 3);\n" +
            "\u0009}\n" +
            "\u0009void foo(boolean ... b) {\n" +
            "\u0009\u0009foo(true, false);" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveVarArgs(sc, cus).execute();
        new ResolveBoxing(sc, cus).execute();
        assertEquals(
            "classA{" +
            "voidbar(Object[]o){bar(newObject[]{newObject(),Boolean.valueOf(true),Integer.valueOf(3)});}" +
            "voidfoo(boolean[]b){foo(newboolean[]{true,false});}}",
            cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testNewArray() throws Exception {
        String cuText =
            "class A {\n" +
            "\u0009boolean [][] bs = new boolean[][] {{ true }, {false}};" +
            "\u0009Object[] os = new Object[] { 3, 5};\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveBoxing(sc, cus).execute();
        assertEquals(
                "classA{" +
                "boolean[][]bs=newboolean[][]{{true},{false}};" +
                "Object[]os=newObject[]{Integer.valueOf(3),Integer.valueOf(5)};}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testDoubleUnboxing() throws Exception {
        String cuText =
            "class X {\n" +
            "Integer max;\n" +
            "Integer foo(X lhs, X rhs) {\n" +
            "\u0009boolean x = lhs.max == rhs.max;\n" +
            "\u0009return (lhs.max==null||rhs.max==null)?\n" +
            "\u0009\u0009\u0009(lhs.max + rhs.max):\n" +
            "\u0009\u0009\u0009\u0009(Integer)(lhs.max + rhs.max);\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        new ResolveBoxing(sc, cus).execute();
        assertEquals(
                "classX{Integermax;Integerfoo(Xlhs,Xrhs){" +
                "booleanx=lhs.max==rhs.max;" +
                "returnInteger.valueOf((lhs.max==null||rhs.max==null)?" +
                "(lhs.max.intValue()+rhs.max.intValue()):" +
                "((Integer)(Integer.valueOf(lhs.max.intValue()+rhs.max.intValue()))).intValue());}}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }
    public void testCovariantIssue() throws Exception {
        String cuText =
            "class C {\n" +
            "\u0009interface F {\n" +
            "\u0009\u0009Object m();\n" +
            "\u0009}\n" +
            "}\n" +
            "class B {\n" +
            "\u0009void p() {\n" +
            "\u0009\u0009new C.F() { B m() { return null; }};\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new RemoveCoVariantReturnTypes(sc, cus).execute();
        assertEquals(
                "classC{interfaceF{Objectm();}}" +
                "classB{voidp(){newC.F(){Objectm(){returnnull;}};}}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testUnboxingIssue() throws Exception {
        String cuText =
            "class C {\n" +
            "\u0009void foo(Integer i, Object[] arr) {\n" +
            "\u0009\u0009Object o = arr[i];\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveBoxing(sc, cus).execute();
        assertEquals(
                "classC{voidfoo(Integeri,Object[]arr){" +
                "Objecto=arr[i.intValue()];}}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testUnboxAndBoxBackIssue() throws Exception {
        String cuText =
            "class A {\n" +
            "\u0009Integer i;\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009i++;\n" +
            "\u0009\u0009Integer j = --i;\n" +
            "\u0009\u0009j = i++;\n" +
            "\u0009\u0009while (i-- > 0) { }\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveBoxing(sc, cus).execute();
        assertEquals(
                "classA{Integeri;voidfoo(){" +
                "i=Integer.valueOf(i.intValue()+1);" +
                "Integerj=(i=Integer.valueOf(i.intValue()-1));" +
                "j=((i=Integer.valueOf(i.intValue()+1))-1);" +
                "while(((i=Integer.valueOf(i.intValue()-1))+1)>0){}" +
                "}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testIncDecOperatorWithBoxing() throws Exception {
        String cuText =
            "class A {\n" +
            "\u0009Integer i;\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009i++;" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        assertEquals("java.lang.Integer", sc.getSourceInfo().getType(
                ((MethodDeclaration)cus.get(0).getTypeDeclarationAt(0).getMembers().get(1)).getBody().getBody().get(0)).getFullName());
    }

    public void testIntegerValueOfIssue() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
            "class A{\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009Integer i = 5;\n" +
            "\u0009}\n" +
            "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        new EnhancedFor2For(sc, cus).execute();
        new ResolveGenerics(sc, cus).execute();
        new RemoveCoVariantReturnTypes(sc, cus).execute();
        new RemoveAnnotations(sc, cus).execute();
        new RemoveStaticImports(sc, cus).execute();
        new ResolveVarArgs(sc, cus).execute();
        new ResolveBoxing(sc, cus).execute();
        new ReplaceEnums(sc).execute();
        new FloatingPoints(sc, cus).execute();
        new ApplyRetrotranslatorLibs(sc, "lib/").execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classA{voidfoo(){Integeri=net.sf.retrotranslator.runtime.java.lang._Integer.valueOf(5);}}",
                cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testConditionalWithBoxing_2() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "import java.util.*;\n" +
                "class A{\n" +
                "\u0009Map<Integer, String> c;" +
                "\u0009short foo() {\n" +
                "\u0009\u0009return (short)(c.size() == 0 ? -1 : c.keySet().iterator().next());\n" +
                "\u0009}\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("importjava.util.*;classA{Map<Integer,String>c;shortfoo(){return(short)(c.size()==0?-1:c.keySet().iterator().next());}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
        new ResolveBoxing(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("importjava.util.*;classA{Map<Integer,String>c;shortfoo(){return(short)(c.size()==0?-1:c.keySet().iterator().next().intValue());}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testConditionalWithBoxing_3() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "import java.util.*;\n" +
                "class A{\n" +
                "\u0009void foo() {\n" +
                "\u0009\u0009boolean b = true;\n" +
                "\u0009\u0009int i = 5;\n" +
                "\u0009\u0009String s = \"\" + (b ? \"\" : i);\n" +
                "\u0009}\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("importjava.util.*;classA{voidfoo(){booleanb=true;inti=5;Strings=\"\"+(b?\"\":String.valueOf(i));}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testConditionalWithBoxing_4() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "class A{\n" +
                "\u0009void foo() {\n" +
                "\u0009\u0009boolean b = true;\n" +
                "\u0009\u0009int i = 5;\n" +
                "\u0009\u0009int x = b ? i : new Integer(5);\n" +
                "\u0009}\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classA{voidfoo(){booleanb=true;inti=5;intx=b?i:newInteger(5);}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
        new ResolveBoxing(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classA{voidfoo(){booleanb=true;inti=5;intx=b?i:newInteger(5).intValue();}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testConditionalWithBoxing_5() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "class A {\n" +
                "\u0009void foo(Integer i) { }\n" +
                "\u0009void bar() {\n" +
                "\u0009\u0009boolean b = true;\n" +
                "\u0009\u0009Integer x = null;\n" +
                "\u0009\u0009foo((x == null ? 0 : x) + 1);\n" +
                "\u0009}\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new MakeConditionalCompatible(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classA{voidfoo(Integeri){}voidbar(){booleanb=true;Integerx=null;foo((x==null?0:x)+1);}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
        new ResolveBoxing(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classA{voidfoo(Integeri){}voidbar(){booleanb=true;Integerx=null;foo(Integer.valueOf((x==null?0:x.intValue())+1));}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testGenericsWithMetaClassReferenceTypeCachingIssue() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "class A<T> {\n" +
                "\u0009Class<?> foo(int i, int j) { }\n" +
                "\u0009static {\n" +
                "\u0009\u0009java.lang.reflect.Method m = A.class.getMethod(\"foo\", int.class, int.class);\n" +
                "\u0009}\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveGenerics(sc, cus).execute();
        sc.getChangeHistory().updateModel();
    }

    public void testEnhancedForWithCapture() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "import java.util.*;\n" +
                "class A{\n" +
                "\u0009Class<? extends Enum<?>> c;" +
                "\u0009void foo() {\n" +
                "\u0009\u0009for (Enum<?> e : c.getEnumConstants()) { }" +
                "\u0009}\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new EnhancedFor2For(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("importjava.util.*;classA{Class<?extendsEnum<?>>c;voidfoo(){{java.lang.Enum<?>[]a0=c.getEnumConstants();for(inti0=0;i0<a0.length;i0++){Enum<?>e=a0[i0];}}}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testGenericsStringIssue() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "class A implements Comparable<String> {\n" +
                "\u0009void foo(String s) { }\n" +
                "\u0009int compareTo(String other) {\n" +
                "\u0009\u0009foo(other + 5);\n" +
                "\u0009\u0009return 0;\n" +
                "\u0009}\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveGenerics(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classAimplementsComparable{voidfoo(Strings){}intcompareTo(Objectother){foo(((String)other)+5);return0;}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testNoCovariantIntoVarArgs() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "class A  {\n" +
                "\u0009void foo(String ... s) { }\n" +
                "\u0009{ foo(\"\", new Integer(5).toString()); } \n" +
                "\u0009{ foo(new Integer(5).toString()); } \n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new RemoveCoVariantReturnTypes(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classA{voidfoo(String...s){}{foo(\"\",newInteger(5).toString());}{foo(newInteger(5).toString());}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testGenericsWithBoxedTypeIssue() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "class A  {\n" +
                "\u0009void foo(int i) { }\n" +
                "\u0009java.util.Map<Integer, Integer> map;\n" +
                "\u0009{ for (java.util.Map.Entry<Integer, Integer> e : map.entrySet()) { foo(-e.getValue()); } }\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveGenerics(sc, cus).execute();
        sc.getChangeHistory().updateModel();
        assertEquals("classA{voidfoo(inti){}java.util.Mapmap;{for(java.util.Map.Entrye:map.entrySet()){foo(-((Integer)e.getValue()));}}}", cus.get(0).toSource().replaceAll("( |\n)", ""));
    }

    public void testGenericsWithParameterizedExceptionsIssue() throws Exception {
        sc.getProjectSettings().ensureExtensionClassesAreInPath();
        String cuText =
                "class A {\n" +
                "\u0009<T extends Exception> T foo() throws T {\n" +
                "\u0009\u0009return foo();\n" +
                "\u0009}\n" +
                "}\n";
        List<CompilationUnit> cus = runIt(cuText);
        new ResolveGenerics(sc, cus).execute();
        System.out.println(cus.get(0).toSource());
        sc.getChangeHistory().updateModel();
    }
}
