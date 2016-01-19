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
import recoder.service.GenericsUseException;
import recoder.service.SemanticsChecker;
import recoder.service.TypingException;
import recoder.service.UnresolvedReferenceException;

/**
 * @author Tobias Gutzmann
 *
 */
public class SemanticsChecks extends TestCase {
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

    public void testMethodInvocationOK1() {
        String cuText =
            "class Addtion {" +
            "\u0009static int get(){return get(1);}" +
            "\u0009private static int get(int i) { return 2*i; }" +
            "   public static void main(){" +
            "\u0009\u0009this.get();" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (ModelException e) {
            // as expected!!
    }
    }



    public void testMethodInvocation2() {
//Can't make a static reference to the non-static field
        String cuText =
            "class TestStatic {" +
            "\u0009int i = 100;//non-static field \n" +
            "\u0009int a;" +
            "\u0009static void set() {" +
            "\u0009\u0009int j = i; //error \n" +
            "\u0009}" +
            "}";
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

    public void testMethodInvocationOK2() {
        String cuText =
            "class TestStatic {" +
            "\u0009static int i = 100;" +
            "\u0009int a;" +
            "\u0009static void set() {" +
            "\u0009\u0009int j = i; //correct \n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (ModelException e) {
            // as expected!!
    }
    }

    public void testMethodInvocation3() {
//abstract method can only define in an abstract class or interface
        String cuText =
            "class IsStatic {" +
            "\u0009abstract void is(){}" +
            "}";
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

    public void testMethodInvocationOK3a() {
//abstract method can only define in an abstract class or interface
        String cuText =
            "abstract class IsStatic {" +
            "\u0009abstract void is (){}" +
            "}" +
            "interface TestStatic {" +
            "\u0009abstract void is(){ }" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (ModelException e) {
            // as expected!!
    }
    }


    public void testMethodInvocationOK3b() {
        String cuText =
            "abstract class ant{" +
            "\u0009abstract void run();" +
            "}" +
            "abstract class queen extends ant{" +
            "\u0009void eat(){  }  //correct! \n" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (ModelException e) {
            // as expected!!
    }
    }

//	public void testMethodInvocationWrong4(){//java.lang.object
//		String cuText =
//			"interface I extends Cloneable { Object clone() throws CloneNotSupportedException; }"+
//			"class Animal implements I {  "+
//			"	Animal animal = new Animal();"+
//			"	class Bee { "+
//			"		void fly() throws Exception {"+
//			"			animal.clone(); //error!\n"+
//			"		}"+
//			"	}"+
//			"}";
//		CompilationUnit cu = null;
//		try {
//			cu = runIt(cuText).get(0);
//		} catch (ParserException e) {
//			fail(e.getMessage());
//		}
//		try {
//			new SemanticsChecker(sc).check(cu);
//			fail();
//		} catch (Exception e) {
//			// as expected!!
//		}
//	}


    public void testMethodInvocationOK6() {
        String cuText =
            "class Parent {" +
            "\u0009Parent() {}" +
            "}" +
            "class Child extends Parent{" +
            "\u0009Child() {" +
            " \u0009\u0009super();" +
            " \u0009}" +
            "} ";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (ModelException e) {
            // as expected!!
    }
    }

    public void testMethodInvocationOK7() {
        String cuText =
            "class Naming {" +
            "\u0009String name;" +
            "\u0009Naming(String input) {" +
            "        name = input;" +
            "\u0009}" +
            "\u0009Naming() {" +
            "        this(" + "\"" + "mary" + "\"" + ");//correct!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocationOK8() {
//static reference to static method
        String cuText =
            "class Parent {" +
            "\u0009int number;" +
            "\u0009static void print(){System.out.println(" + "\"" + "Parent" + "\"" + ");}" +
            "\u0009static void set(){} {" +
            "        number = 1;" +
            "\u0009}" +
            "}" +
            "class Children {" +
            "\u0009public static void main() {" +
            "        Parent.print(); //correct!\n" +
            "\u0009     Parent.set();//correct!\n" +
            "\u0009\u0009 Parent one = new Parent();" +
            "\u0009\u0009 one.set();//correct!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }


    public void testMethodInvocation8() {
//static reference to non-static method
        String cuText =
            "class Parent {" +
            "\u0009int number;" +
            "\u0009void set(){} {" +
            "        number = 1;" +
            "\u0009}" +
            "}" +
            "class Children {" +
            "\u0009public static void main() {" +
            "\u0009     Parent.set();//error!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation9() {
//static reference to non-static method
        String cuText =
            "class Test {" +
            "\u0009int number;" +
            "\u0009void nostaticset(){" +
            "\u0009\u0009number = 3;" +
            "\u0009}" +
            "\u0009static void testnostatic(){" +
            "\u0009\u0009nostaticset();//error!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocationOK9() {
//static reference to static method
        String cuText =
            "class Test {" +
            "\u0009int number;" +
            "\u0009static void staticset(){} {" +
            "       number = 1;" +
            "\u0009}" +
            "\u0009void nostaticset(){" +
            "\u0009\u0009number = 3;" +
            "\u0009}" +
            "\u0009static void teststaticset(){" +
            "\u0009\u0009staticset();//correct!\n" +
            "\u0009}" +
            "\u0009void testnostaticset(){" +
            "\u0009\u0009staticset();//correct!\n" +
            "\u0009\u0009nostaticset();//correct!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }


    public void testMethodInvocation10() {
//lack of one abstract method implement
        String cuText =
            "interface I{" +
            "\u0009void write();" +
            "\u0009void read();" +
            "}" +
            "abstract class X1 implements I {" +
            "\u0009static int i=9;" +
            "\u0009static void have(){}" +
            "\u0009abstract void pick();" +
            "}" +
            "class X2 extends X1 {//error! lack of unimplement abstract method of X \n" +
            "\u0009public void read() {  }" +
            "\u0009public void write(){  }" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            //fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation11() {
//abstract and super correct
        String cuText =
            "class Z{" +
            "\u0009Z(){int z=10;}" +
            "}" +
            "class S extends Z {" +
            "\u0009S (){super();}" +
            "}" +
            "abstract class X1  extends S{" +
            "\u0009int i;" +
            "\u0009X1(){super();}" +
            "\u0009abstract void has();" +
            "\u0009void put(){i = 9;}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation12() {
// the visibilty only can be public or protected
        String cuText =
            "abstract class IsAbstract{" +
            "\u0009abstract final void one();//error \n" +
            "\u0009abstract static void two();//error \n" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocationOK12() {
// the visibilty only can be public or protected
        String cuText =
            "abstract class IsAbstract{" +
            "\u0009abstract public void one();//correct! \n" +
            "\u0009abstract protected void two();//correct! \n" +
            "\u0009abstract void three();//correct! \n" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocationOK13() {
//correcr using
        String cuText =
            "class outer{" +
//			"	outer(){int i=2;}"+
//			"	protected Object clone(){"+
//			"		return outer.this.hashCode();"+
//			"	}"+
//			"	class inner{"+
//			"		void set(){	int j=19;}"+
 //			"	}"+
//			"	public static void main(){"+
//			"		outer ou=new outer();"+
//			"		ou.clone();"+
//			"   	new outer.inner.set();//error \n"+
//			"	}"+
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            //fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation14() {
//can not directly invoke the method
        String cuText =
            "abstract class animal{" +
            "\u0009public abstract String toString();" +
            "}" +
            "class ant extends animal{ " +
            "\u0009int age;" +
            "\u0009public String toString(){" +
            "\u0009\u0009return super.toString()+ age;//error!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocationOK14() {
//correct
        String cuText =
            "abstract class animal{" +
            "\u0009public abstract String toString();" +
            "\u0009protected String objString(){" +
            "\u0009\u0009return super.toString();" +
            "\u0009}" +
            "}" +
            "class ant extends animal{ " +
            "\u0009int age;" +
            "\u0009public String toString(){" +
            "\u0009\u0009return super.toString()+ age;//correct!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation15() {
//can't not directly invoke the abstract method
        String cuText =
            "abstract class ant{" +
            "\u0009abstract void run();" +
            "}" +
            "class queen extends ant{" +
            "\u0009void run(){" +
            "\u0009\u0009super.run();" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocationOK15() {
        String cuText =
            "abstract class ant{" +
            "\u0009abstract void run();" +
            "}" +
            "class queen extends ant{" +
            "\u0009void run(){" +
            "\u0009\u0009super.hashCode();" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }



    public void testMethodInvocation17() {
//check reference method return type
        String cuText =
            "class A {int a;}" +
            "class B extends A{int b;}" +
            "class Test {" +
            "\u0009static int test (B b){" +
            "\u0009\u0009return b.b;" +
            "\u0009}" +
            "\u0009static String test (A a){" +
            "\u0009\u0009return " + "\"" + "A" + "\"" + ";" +
            "\u0009}" +
            "\u0009public static void main(String[] args){" +
            "\u0009\u0009B testB = new B();" +
            "\u0009\u0009String s = test(testB);//error!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }


    public void testMethodInvocation18() {
        String cuText =
            "class A {" +
            "\u0009static void meet(){ }" +
            "\u0009static A test(){return null;}" +
            "\u0009public static void main ( ){" +
            "\u0009\u0009test().meet();//correct!\n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation19() {
//check expression object invoked
        String cuText =
            "class StringTest {" +
            "\u0009public static void main ( ){" +
            "\u0009\u0009String s = " + "\"" + "check" + "\"" + ";" +
            "\u0009\u0009if (s.endsWith(" + "\"" + "k" + "\"" + ")){//correct!\n" +
            "\u0009\u0009\u0009s = " + "\"" + "ok!" + "\"" + ";" +
            "\u0009\u0009}" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation20() {
//about super
        String cuText =
            "class AAA {" +
            "\u0009int charge(){return 7;}" +
            "}" +
            "class AA extends AAA{" +
            "\u0009int charge(){return 5;}" +
            "}" +
            "class Battery extends AA{" +
            "\u0009int charge(){return 0;}" +
            "\u0009void test(){" +
            "\u0009\u0009super.charge();" +
            "\u0009\u0009((AA)this).charge();" +
            "\u0009\u0009((AAA)this).charge();" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation21() {
        String cuText =
            "class Hello{" +
            "\u0009void say(){}" +
            "\u0009class H{" +
            "\u0009\u0009void say(){}" +
            "\u0009}" +
            "\u0009public static void main(){" +
            "\u0009\u0009Hello h = new Hello();" +
            "\u0009\u0009h.say();" +
            "\u0009\u0009h.new H().say();//correct! \n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation22() {
//Cannot use this in a static context
        String cuText =
            "class Hello{" +
            "\u0009void say(){}" +
            "\u0009class H{" +
            "\u0009\u0009void say(){}" +
            "\u0009}" +
            "\u0009public static void main(){" +
            "\u0009\u0009Hello.this.say();//error! \n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation23() {
//No enclosing instance of the type Hello.H1 is accessible in scope
        String cuText =
            "class Hello{" +
            "\u0009void say(){}" +
            "\u0009class H{" +
            "\u0009\u0009void say(){}" +
            "\u0009}" +
            "\u0009public static void main(){" +
            "\u0009\u0009H.this.say();//error! \n" +
            "\u0009}" +
            "}";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        } catch (UnresolvedReferenceException e) {
            // as expected, this must be caught during type resolving already!
    }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }
    public void testMethodInvocation24() {
        String cuText =
            "class Hello{" +
            "\u0009void say(){" +
            "\u0009\u0009answer();//correct!\n" +
            "\u0009}" +
            "\u0009void answer(){}" +
            "}";

        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testMethodInvocation25() {
        String cuText =
            "import java.util.ArrayList;" +
            "import java.util.List;" +
            "class H{" +
            "\u0009ArrayList getList(){return new ArrayList();}" +
            "\u0009void foo(){" +
            "\u0009\u0009List list = foo();" +
            "\u0009}" +
            "}";

        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testSubclass1() {
//final class can't have subclass
        String cuText =
            "class Point { int x, y; }" +
            "final class WhitePoint extends Point  { int white; }" +
            "class ColorPoint extends WhitePoint { int black; }//error! \n";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            //TODO
            //fail();
        } catch (Exception e) {
            // as expected!!
    }
    }

    public void testSubclassOK1() {
        String cuText =
            "class Point { int x, y; }" +
            "class WhitePoint extends Point  { int white; }" +
            "class ColorPoint extends WhitePoint { int black; }//correct! \n";
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            // as expected!!
    }
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
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            //TODO
            //fail();
        } catch (Exception e) {
            // as expected!!
    }
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
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
            fail();
        } catch (GenericsUseException te) {
            // as expected!!
    }
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
        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (GenericsUseException te) {
            // as expected!!
    }
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

        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (ModelException e) {
            assertTrue(e.getMessage(), true);// as expected!!
        }
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

        CompilationUnit cu = null;
        try {
            cu = runIt(cuText).get(0);
        } catch (ParserException e) {
            fail(e.getMessage());
        }
        try {
            new SemanticsChecker(sc).check(cu);
        } catch (Exception e) {
            assertTrue(e.getMessage(), true);// as expected!!
        }
    }

    public void testIf() {
        String cuText =
            "class A {\n" +
            "\u0009void foo() {\n" +
            "\u0009\u0009if (new Object()) { }" +
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

    public void testSwitch() throws Exception {
        String cuText = "class A{\n" +
                "\u0009void foo(byte b) {\n" +
                "\u0009\u0009switch (b) {\n" +
                "\u0009\u0009\u0009case 130: break; \n" + // should fail
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        try {
            runIt(cuText);
            new SemanticsChecker(sc).checkAllCompilationUnits();
            fail("Switch-constant should be reported as out-of-bounds...");
        } catch (ModelException e) {
            // as expected.
    }
    }
    public void testSwitch2() throws Exception {
        String cuText = "class A{\n" +
                "\u0009void foo(int i) {\n" +
                "\u0009\u0009switch (i) {\n" +
                "\u0009\u0009\u0009case 2L: break; \n" + // should fail (in range, but declared as long)
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        try {
            runIt(cuText);
            new SemanticsChecker(sc).checkAllCompilationUnits();
            fail("Switch-constant must not be of type Long...");
        } catch (ModelException e) {
            // as expected.
    }
    }
    public void testSwitch3() throws Exception {
        String cuText = "class A{\n" +
                "\u0009public static final short X = 3;\n" +
                "\u0009void foo(short s) {\n" +
                "\u0009\u0009switch (s) {\n" +
                "\u0009\u0009\u0009case X: break; \n" +
                "\u0009\u0009\u0009case 33: break;" +
                "\u0009\u0009}\n" +
                "\u0009}\n" +
                "}\n";
        runIt(cuText);
        new SemanticsChecker(sc).checkAllCompilationUnits();
    }
}
