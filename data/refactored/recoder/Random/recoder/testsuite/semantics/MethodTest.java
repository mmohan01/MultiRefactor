/**
 * 
 */
package recoder.testsuite.semantics;


/**
 * @author Ya Liu
 *
 */

public class MethodTest {
    private String cuText = null;

    public String testThisInStaticMethod() {
        cuText =
            "class Addtion {" +
            "\u0009static int get(){return get(1);}" +
            "\u0009private static int get(int i) { return 2*i; }" +
            "   public static void main(){" +
            "\u0009\u0009this.get();" +
            "\u0009}" +
            "}";
        return cuText;
    }

    /* Can't make a static reference to the non-static field */
    public String testStaticMethodInvocation2() {
        cuText =
            "class TestStatic {" +
            "\u0009int i = 100;//non-static field \n" +
            "\u0009int a;" +
            "\u0009static void set() {" +
            "\u0009\u0009int j = i; //error \n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testStaticMethodInvocationOK3() {
        cuText =
            "class TestStatic {" +
            "\u0009static int i = 100;" +
            "\u0009int a;" +
            "\u0009static void set() {" +
            "\u0009\u0009int j = i; //correct \n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    /* abstract method can only define in an abstract class or interface*/
    public String testAbstractMethodInvocation1() {
        cuText =
            "class IsStatic {" +
            "\u0009abstract void is(){}" +
            "}";
        return cuText;
    }

    public String testAbstractMethodInvocationOK2() {
        cuText =
            "abstract class IsStatic {" +
            "\u0009abstract void is (){}" +
            "}" +
            "interface TestStatic {" +
            "\u0009abstract void is(){ }" +
            "}";
        return cuText;
    }

    public String testAbstractMethodInvocationOK3() {
        cuText =
            "abstract class ant{" +
            "\u0009abstract void run();" +
            "}" +
            "abstract class queen extends ant{" +
            "\u0009void eat(){  }  //correct! \n" +
            "}";
        return cuText;
    }

//	public String testMethodInvocationWrong4(){//java.lang.object
//		cuText =
//			"interface I extends Cloneable { Object clone() throws CloneNotSupportedException; }"+
//			"class Animal implements I {  "+
//			"	Animal animal = new Animal();"+
//			"	class Bee { "+
//			"		void fly() throws Exception {"+
//			"			animal.clone(); //error!\n"+
//			"		}"+
//			"	}"+
//			"}";
//		return cuText;
//	}

    /*correct using super*/
    public String testSuperInConstructorOK1() {
        cuText =
            "class Parent {" +
            "\u0009Parent() {}" +
            "}" +
            "class Child extends Parent{" +
            "\u0009Child() {" +
            " \u0009\u0009super();" +
            " \u0009}" +
            "} ";
        return cuText;
    }

    /*correct using this*/
    public String testThisInConstructorOK1() {
        cuText =
            "class Naming {" +
            "\u0009String name;" +
            "\u0009Naming(String input) {" +
            "        name = input;" +
            "\u0009}" +
            "\u0009Naming() {" +
            "        this(" + "\"" + "mary" + "\"" + ");//correct!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }
    /* static reference to static method*/
    public String testStaticMethodInvocationOK4() {
        cuText =
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
        return cuText;
    }

    /*static reference to non-static method*/
    public String testStaticMethodInvocation5() {
        cuText =
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
        return cuText;
    }

    /*static reference to non-static method*/
    public String testStaticMethodInvocation6() {
        cuText =
            "class Test {" +
            "\u0009int number;" +
            "\u0009void nostaticset(){" +
            "\u0009\u0009number = 3;" +
            "\u0009}" +
            "\u0009static void testnostatic(){" +
            "\u0009\u0009nostaticset();//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    /*static reference to static method*/
    public String testStaticMethodInvocationOK7() {
        cuText =
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
        return cuText;
    }

    /*lack of one abstract method implement*/
    public String testAbstractMethodInvocationOK4() {
//not checked
        cuText =
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
        return cuText;
    }
    /*correct using abstract and super*/
    public String testSuperInConstructorOK2() {
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
        return cuText;
    }

    /*abstract method in abstract class, the visibilty only can be public or protected*/
    public String testAbstractMethodInvocation5() {
        cuText =
            "abstract class IsAbstract{" +
            "\u0009abstract final void one();//error \n" +
            "\u0009abstract static void two();//error \n" +
            "}";
        return cuText;
    }

    public String testAbstractMethodInvocationOK6() {
        String cuText =
            "abstract class IsAbstract{" +
            "\u0009abstract public void one();//correct \n" +
            "\u0009abstract protected void two();//correct \n" +
            "}";
        return cuText;
    }


    public String testStaticMethodInvocationOK8() {
        String cuText =
            "class outer{" +
            "\u0009outer(){int i=2;}" +
            "\u0009protected Object clone(){" +
            "\u0009\u0009return outer.this.hashCode();" +
            "\u0009}" +
            "\u0009class inner{" +
            "\u0009\u0009void set(){\u0009int j=19;}" +
            "\u0009}" +
            "\u0009public static void main(){" +
            "\u0009\u0009outer ou=new outer();" +
            "\u0009\u0009ou.clone();" +
            //"   	new outer.inner.set();//error \n"+
            "\u0009}" +
            "}";
        return cuText;
    }

    /*can't directly invoke super class's abstract method*/
    public String testSuperMethodInvocation3() {
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
        return cuText;
    }

    public String testSuperMethodInvocationOK4() {
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
        return cuText;
    }

    /*can't directly invoke super class's abstract method*/
    public String testSuperMethodInvocation5() {
        String cuText =
            "abstract class ant{" +
            "\u0009abstract void run();" +
            "}" +
            "class queen extends ant{" +
            "\u0009void run(){" +
            "\u0009\u0009super.run();//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testSuperMethodInvocation6() {
        String cuText =
            "abstract class ant{" +
            "\u0009abstract void run();" +
            "}" +
            "class queen extends ant{" +
            "\u0009void run(){" +
            "\u0009\u0009super.hashCode();" +
            "\u0009}" +
            "}";
        return cuText;
    }




    /* check expression object invoked*/
    public String testStaticMethodInvocationOK9() {
        String cuText =
            "class StringTest {" +
            "\u0009public static void main ( ){" +
            "\u0009\u0009String s = " + "\"" + "check" + "\"" + ";" +
            "\u0009\u0009if (s.endsWith(" + "\"" + "k" + "\"" + ")){//correct!\n" +
            "\u0009\u0009\u0009s = " + "\"" + "ok!" + "\"" + ";" +
            "\u0009\u0009}" +
            "\u0009}" +
            "}";
        return cuText;
    }

    /*correct super using*/
    public String testSuperMethodInvocationOK7() {
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
        return cuText;
    }

    public String testStaticMethodInvocationOK10() {
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
        return cuText;
    }

    /* Cannot use this in a static context*/
    public String testThisMethodInvocation2() {
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
        return cuText;
    }

    /*No enclosing instance of the type Hello.H1 is accessible in scope*/
    public String testThisMethodInvocation3() {
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
        return cuText;
    }

    /*correct using*/
    public String testMethodInvocationOK27() {
        String cuText =
            "class Hello{" +
            "\u0009void say(){" +
            "\u0009\u0009answer();//correct!\n" +
            "\u0009}" +
            "\u0009void answer(){}" +
            "}";
        return cuText;
    }

    /* check reference method return type*/
    public String testReturnMethodInvocation1() {
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
        return cuText;
    }

    protected String testReturnMethodInvocation2() {
        String cuText =
            "import java.util.ArrayList;" +
            "import java.util.List;" +
            "class H{" +
            "\u0009ArrayList getList(){return new ArrayList();}" +
            "\u0009void foo(){" +
            "\u0009\u0009List list = foo();" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testReturnMethodInvocationOK3() {
        String cuText =
            "class A {" +
            "\u0009static void meet(){ }" +
            "\u0009static A test(){return null;}" +
            "   void test(){return;}" +
            "\u0009public static void main ( ){" +
            "\u0009\u0009test().meet();//correct!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }
}
