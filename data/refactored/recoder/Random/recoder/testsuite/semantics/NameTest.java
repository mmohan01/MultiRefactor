package recoder.testsuite.semantics;


/**
 * @author Ya Liu
 *
 */


public class NameTest {


    public String testAmbiguousName1() {
//The field i is ambiguous
        String cuText =
            "interface I1{int i = 1;}" +
            "interface I2{int i = 0;}" +
            "class Test implements I1,I2{" +
            "\u0009int j = i; //error!\n" +
            "}";
        return cuText;
    }


    public String testAmbiguousName2() {
//The import declaration is ambiguous
        String cuText =
            "import java.lang.Object;" +
            "class Object{" +
            "}";
        return cuText;
    }


    public String testAbstractClass3() {
//abstract class can't instantiated
        String cuText =
            "abstract class A{}" +
            "class Test extends A{" +
            "\u0009public static void main(){" +
            "\u0009\u0009A a1 = new Test(); //correct !\n" +
            "\u0009\u0009A a2 =  new A();//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }


    public String testAccessModifier4() {
//The class can be either abstract or final, not both
        String cuText =
            "abstract final class A{}//error!\n";
        return cuText;
    }


    public String testClassInherited5() {
//final can't have sub class
        String cuText =
            "final class A{}" +
            "class Test extends A{" +
            "}";
        return cuText;
    }


    //The field can only set one modifier, such as public / protected / private
    public String testAccessModifier6() {
        String cuText =
            "class Test{" +
            "\u0009public private int x;//error!\n" +
            "}";
        return cuText;
    }


    public String testAccessModifier7() {
//The final variable can be either final or volatile, not both
        String cuText =
            "class Test{" +
            "\u0009final volatile int x;//error!\n" +
            "}";
        return cuText;
    }

    public String testFieldReference8() {
//Cannot reference a field before it is defined
        String cuText =
            "class Test{" +
            "\u0009int x = y;//error!\n" +
            "\u0009int y = 9;" +
            "}";
        return cuText;
    }

    public String testFieldReferenceOK8() {
        String cuText =
            "class Test{" +
            "\u0009int x = 9;" +
            "\u0009int y = x;" +
            "}";
        return cuText;
    }

    public String testAmbiguousName9() {
//Field define is ambiguous
        String cuText =
            "interface I1 {int i = 9;}" +
            "interface I2 {int i = 10;}" +
            "class Test implements I1,I2{" +
            "\u0009int j = i;//error!\n" +
            "}";
        return cuText;
    }

    public String testDuplicateName10() {
//Duplicate method 1
        String cuText =
            "class Test{" +
            "\u0009void set(){}//error!\n" +
            "\u0009int set(){}//error!\n" +
            "}";
        return cuText;
    }

    public String testDuplicateName11() {
//Duplicate method 2
        String cuText =
            "abstract class Test{" +
            "\u0009abstract void set();" +
            "\u0009void set(){}//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier12() {
//Method Modifiers
        String cuText =
            "class Test{" +
            "\u0009public protected void set1();//error!\n" +
            "\u0009public private void set2();//error!\n" +
            "\u0009private protected void set3();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier13() {
//Method Modifiers
        String cuText =
            "abstract class Test{" +
            "\u0009abstract private void set2();//error!\n" +
            "\u0009native strictfp void set3();//error!\n" +
            "\u0009abstract static void set4();//error!\n" +
            "\u0009abstract final void set5();//error!\n" +
            "\u0009abstract synchronized void set6();//error!\n" +
            "\u0009abstract native void set7();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifierOK13() {
//Method Modifiers
        String cuText =
            "abstract class Test{" +
            "\u0009abstract public void set1();//correct!\n" +
            "\u0009abstract protected void set2();//correct!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier14() {
//Method  Modifiers
        String cuText =
            "abstract class Test{" +
            "\u0009abstract public protected void set2();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier15() {
//Method Dupilcate Modifiers
        String cuText =
            "interface I{" +
            "\u0009public public void set();//error!\n" +
            "}";
        return cuText;
    }
    public String testMethodModifier17() {
//Method  Modifiers
        String cuText =
            "interface I{" +
            "\u0009private void set();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier18() {
//Method Dupilcate Modifiers
        String cuText =
            "interface I{" +
            "\u0009abstract abstract void set();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier19() {
//Method Modifiers
        String cuText =
            "interface I{" +
            "\u0009abstract protected void set();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier20() {
//Method  Modifiers
        String cuText =
            "interface I{" +
            "\u0009abstract private void set();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier21() {
//Method Modifiers
        String cuText =
            "abstract class Test{" +
            "\u0009private abstract int set();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier22() {
//Method Modifiers
        String cuText =
            "class Test{" +
            "\u0009private abstract int set();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier23() {
//Method Modifiers
        String cuText =
            "class Test{" +
            "\u0009private  private public int set(){return 1;}//error!\n" +
            "}";
        return cuText;
    }

    //The instance method cannot override the static method from super class
    public String testOverrideName14() {
//Method override1
        String cuText =
            "class A{" +
            "\u0009static void set(){}" +
            "}" +
            "class Test extends A{" +
            "\u0009void set(){}//error!\n" +
            "}";
        return cuText;
    }


    //Can't reduce the visibility of the inherited method from super class
    public String testOverrideName15() {
//Method override2
        String cuText =
            "public class A{" +
            "\u0009public void set(){}" +
            "\u0009protected void get(){}" +
            "}" +
            "class Test extends A{" +
            "\u0009private void set(){}//error!\n" +
            "\u0009private void get(){}//error!\n" +
            "}";
        return cuText;
    }

    public String testOverrideNameOK15() {
//Method override2
        String cuText =
            "public class A{" +
            "\u0009public void set(){}" +
            "\u0009protected void get(){}" +
            "}" +
            "class B extends A{" +
            "\u0009public void set(){}//correct!\n" +
            "\u0009public void get(){}//correct!\n" +
            "}" +
            "class C extends A{" +
            "\u0009protected void get(){}//correct!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifier16() {
//Illegal modifier for the interface method
        String cuText =
            "interface I {" +
            "\u0009final void m1();//error!\n" +
            "\u0009static void m2();//error!\n" +
            "\u0009protected void m3();//error!\n" +
            "\u0009private void m4();//error!\n" +
            "}";
        return cuText;
    }

    public String testMethodModifierOK16() {
//correct modifier for the interface method
        String cuText =
            "interface I {" +
            "\u0009abstract void m1();//correct!\n" +
            "\u0009public void m2();//correct!\n" +
            "}";
        return cuText;
    }

    public String testOverrideName17() {
//overriding of Throws 1
        String cuText =
            "class A {" +
            "\u0009void m(){}" +
            "}" +
            "class B extends A{" +
            "\u0009public void m() throws Exception{//error!\n" +
            "\u0009\u0009throw new Exception();" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testOverrideName18() {
//overriding of Throws 2
        String cuText =
            "class A {" +
            "\u0009void m(){}" +
            "}" +
            "class B extends A{" +
            "\u0009public void m() {" +
            "\u0009\u0009throw new Exception();//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }
    public String testInterfaceName19() {
//interface naming
        //The public type I must be defined in its own file
        String cuText =
            "package test;" +
            "public interface I {//error!\n" +
            "\u0009int a;" +
            "}";
        return cuText;
    }
    public String testInterfaceName20() {
//interface naming
        //interface can't depends on itself
        String cuText =
            "interface I  extends I{//error!\n" +
            "\u0009int a;" +
            "}";
        return cuText;
    }

    public String testInterfaceNameOK20() {
//interface naming
        String cuText =
            "interface I {" +
            "\u0009void m();" +
            "}" +
            "class Test implements I{" +
            "\u0009public final void m(){ }//error!\n" +
            "}";
        return cuText;
    }

    public String testFieldReferenceName21() {
        //Cannot reference a field before it is defined
        String cuText =
            "class Test{" +
            "\u0009int a =  a + 1;//error!\n" +
            "}";
        return cuText;
    }

    public String testDuplicateName22() {
        //The nested type Local cannot hide an enclosing type
        String cuText =
            "class Test{" +
            "\u0009class Local{" +
            "\u0009\u0009class Local{ }//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testFieldReferenceName23() {
        //The local variable may not have been initialized
        String cuText =
            "class Test{" +
            "\u0009int x;" +
            "\u0009void m(){" +
            "\u0009\u0009int x = x;//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testName24() {
        //switch Only permitted int values or enum constants
        String cuText =
            "class Test{" +
            "\u0009void m1(String s){" +
            "\u0009\u0009switch(s){//error!\n" +
            "\u0009\u0009\u0009case 1:" +
            "       \u0009\u0009break;" +
            "\u0009\u0009}" +
            "\u0009}" +
            "\u0009void m2(char c){" +
            "\u0009\u0009switch(c){//error!\n" +
            "\u0009\u0009\u0009case 1:" +
            "       \u0009\u0009break;" +
            "\u0009\u0009}" +
            "\u0009}" +
            "\u0009void m3(float f){" +
            "\u0009\u0009switch(f){//error!\n" +
            "\u0009\u0009\u0009case 1:" +
            "       \u0009\u0009break;" +
            "\u0009\u0009}" +
            "\u0009}" +
            "\u0009void m4(byte b){" +
            "\u0009\u0009switch(b){//error!\n" +
            "\u0009\u0009\u0009case 1:" +
            "       \u0009\u0009break;" +
            "\u0009\u0009}" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testName25() {
//switch statement
        //Type mismatch: cannot convert from String to int
        String cuText =
            "class Test{" +
            "\u0009void m(int n){" +
            "\u0009\u0009switch(n){" +
            "\u0009\u0009\u0009case" + "\"" + "\"" + ":" + "//error!\n" +
            "\u0009\u0009\u0009\u0009break;" +
            "\u0009\u0009}" +
            "\u0009}" +
            "}";
        return cuText;
    }


    public String testReturnName26() {
//return statement
        //Void methods cannot return a value
        String cuText =
            "class Test{" +
            "\u0009void m(){" +
            "\u0009\u0009return 1;//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testReturnName27() {
//return statement
        //can't return from static initializers
        String cuText =
            "class Test{" +
            "\u0009static{" +
            "\u0009\u0009return ;//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testReturnName28() {
//return statement
        //can't return from instance initializers
        String cuText =
            "class Test{" +
            "\u0009{" +
            "\u0009\u0009return ;//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testStaticInitializer29() {
//this in static initializer
        //Cannot use this in a static context
        String cuText =
            "class Test{" +
            "\u0009static{" +
            "\u0009\u0009this.getClass() ;//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testStaticInitializer30() {
//super in static initializer
        //Cannot use super in a static context
        String cuText =
            "class A{" +
            "\u0009void m(){}" +
            "}" +
            "class Test extends A{" +
            "\u0009static{" +
            "\u0009\u0009super.m();//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testThisInConstructor31() {
//this in constructor
        //this must be the first statement in a constructor
        String cuText =
            "class Test {" +
            "\u0009int num;" +
            "\u0009int number;" +
            "\u0009Test(int n){" +
            "\u0009\u0009num = n;" +
            "\u0009}" +
            "\u0009Test(){" +
            "\u0009\u0009number = 9;" +
            "\u0009\u0009this(2);//error!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testThisInConstructorOK31() {
//this in constructor
        //this must be the first statement in a constructor
        String cuText =
            "class Test {" +
            "\u0009int num;" +
            "\u0009int number;" +
            "\u0009Test(int n){" +
            "\u0009\u0009num = n;" +
            "\u0009}" +
            "\u0009Test(){" +
            "\u0009\u0009this(2);//correct!\n" +
            "\u0009}" +
            "}";
        return cuText;
    }
    public String testAbstractClassOK32() {
//correct using
        String cuText =
            "abstract class Test {" +
            "\u0009@Deprecated public abstract void get();" +
            "}";
        return cuText;
    }

    public String testAbstractClass33() {
        String cuText =
            "abstract class Test {" +
            " \u0009public abstract strictfp void foo2();" +
            " \u0009public protected abstract void foo3();" +
            "}";
        return cuText;
    }

    public String testFieldOK34() {
//correct using field in static context
        String cuText =
            "class Test {" +
            " \u0009Test tfield;" +
            "\u0009static void foo(Test t){" +
            "\u0009\u0009t.tfield = null;" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testFieldOK35() {
//correct using field in static context
        String cuText =
            "class Test {" +
            " \u0009Test tfield;" +
            "\u0009static void foo(Test t){" +
            "\u0009\u0009{" +
            "\u0009\u0009\u0009t.tfield = null;" +
            "\u0009\u0009}" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testField36() {
        String cuText =
            "class H{" +
            "\u0009H h;" +
            "\u0009public static void go(H h){" +
            "\u0009\u0009H.h = null;" +
            "\u0009}" +
            "}";
        return cuText;
    }

    public String testDupilcateName37() {
//duplicate name of method
        String cuText =
            "class Father{" +
            "\u0009static int foo(){" +
            "\u0009\u0009return 1;" +
            "\u0009}" +
            "}" +
            "class Child extends Father{" +
            "\u0009int foo(){//error!\n" +
            "\u0009\u0009return 1;" +
            "\u0009}" +
            "}";
        return cuText;
    }
}
