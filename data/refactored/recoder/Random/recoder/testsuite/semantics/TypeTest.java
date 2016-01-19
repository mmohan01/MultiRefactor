package recoder.testsuite.semantics;
/**
 * @author  Ya Liu
 *
 */


public class TypeTest {
    private String text = null;

    /*integer type conversion*/
    public String IntConversion() {
        text =
            "class Text{" +
            "\u0009public void testCast(){" +
            "\u0009\u0009int i ;" +
            "\u0009\u0009float f = 8.0f;" +
            "\u0009\u0009long l = 9l;" +
            "\u0009\u0009double d = 7d;" +
            "\u0009\u0009i = f;//error!\n" +
            "\u0009\u0009i = l;//error!\n" +
            "\u0009\u0009i = d;//error!\n" +
            "\u0009\u0009int[] a = new int[2];" +
            "\u0009\u0009byte[] b = new byte[2];" +
            "\u0009\u0009a = b;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }


    public String IntConversionC() {
        text =
            "class Text{" +
            "\u0009public void testCast(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 8;" +
            "\u0009\u0009long l = i;" +
            "\u0009\u0009double d = i;" +
            "\u0009\u0009float ff = 8.0f;" +
            "\u0009\u0009long ll = 9l;" +
            "\u0009\u0009double dd = 7d;" +
            "\u0009\u0009int j;" +
            "\u0009\u0009j = (int) ff; //correct!\n" +
            "\u0009\u0009j = (int) ll; //correct!\n" +
            "\u0009\u0009j = (int) dd; //correct!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    /*long type conversion*/

    public String LongConversion() {
        text =
            "class Text{" +
            "\u0009public void testCast(){" +
            "\u0009\u0009long l ;" +
            "\u0009\u0009float f = 0.0f;" +
            "\u0009\u0009double d = 9d;" +
            "\u0009\u0009l = f;//error!\n" +
            "\u0009\u0009l = d;//error!\n" +
            "\u0009\u0009long ll = 1l;" +
            "\u0009\u0009short s = ll;//error!\n" +
            "\u0009\u0009char c = ll;//error!\n" +
            "\u0009\u0009byte b = ll;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }


    public String LongConversionC() {
        text =
            "class Text{" +
            "\u0009public void testCast(){" +
            "\u0009\u0009long l = 1l;" +
            "\u0009\u0009float f = l;" +
            "\u0009\u0009double d = l;" +
            "\u0009\u0009short s = (short)l;//correct!\n" +
            "\u0009\u0009char c = (char)l;//correct!\n" +
            "\u0009\u0009byte b = (byte)l;//correct!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    /*Float type conversion*/
    public String FloatConversion() {
        text =
            "class Text{" +
            "\u0009public void testCast(){" +
            "\u0009\u0009int i = 0.0f;//error!\n" +
            "\u0009\u0009float f = 2.0f;" +
            "\u0009\u0009int sum = sum + f;//error!\n" +
            "\u0009\u0009int times = times * f;//error!\n" +
            "\u0009\u0009long l = f;//error!\n" +
            "\u0009\u0009double d = 9;" +
            "\u0009\u0009float ff = d;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String FloatConversionC() {
        text =
            "class Text{" +
            "\u0009public void testCast(){" +
            "\u0009\u0009int i = (int) 0.0f;//correct!\n" +
            "\u0009\u0009float f = 1.0f;" +
            "\u0009\u0009int sum = (int)(sum + f);//correct!\n" +
            "\u0009\u0009int times = (int) (times * f);//correct!\n" +
            "\u0009\u0009double d = f ;//correct!\n" +
            "\u0009\u0009long l = (long)f; //correct!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    /*byte type*/
    public String ByteConversion() {
        text =
            "class Conversion{" +
            "\u0009public void conver(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 9.9f;" +
            "\u0009\u0009long l = 89l;" +
            "\u0009\u0009double d = 8.8d;" +
            "\u0009\u0009short s = 9;" +
            "\u0009\u0009byte b;" +
            "\u0009\u0009b = i;//error!\n" +
            "\u0009\u0009b = f;//error!\n" +
            "\u0009\u0009b = l;//error!\n" +
            "\u0009\u0009b = d;//error!\n" +
            "\u0009\u0009b = s;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String ByteConversionC() {
        text =
            "class Conversion{" +
            "\u0009public void conver(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 9.9f;" +
            "\u0009\u0009long l = 89l;" +
            "\u0009\u0009double d = 8.8d;" +
            "\u0009\u0009short s = 9;" +
            "\u0009\u0009byte b;" +
            "\u0009\u0009b = (byte)i;//correct!\n" +
            "\u0009\u0009b = (byte)f;//correct!\n" +
            "\u0009\u0009b = (byte)l;//correct!\n" +
            "\u0009\u0009b = (byte)d;//correct!\n" +
            "\u0009\u0009b = (byte)s;//correct!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    /*short type*/
    public String ShortConversion() {
        text =
            "class Conversion{" +
            "\u0009public void conver(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 9.9f;" +
            "\u0009\u0009long l = 89l;" +
            "\u0009\u0009double d = 8.8d;" +
            "\u0009\u0009short s;" +
            "\u0009\u0009s = i;//error!\n" +
            "\u0009\u0009s = f;//error!\n" +
            "\u0009\u0009s = l;//error!\n" +
            "\u0009\u0009s = d;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String ShortConversionC() {
        text =
            "class Conversion{" +
            "\u0009public void conver(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 9.9f;" +
            "\u0009\u0009long l = 89l;" +
            "\u0009\u0009double d = 8.8d;" +
            "\u0009\u0009short s;" +
            "\u0009\u0009s = (short)i;//correct!\n" +
            "\u0009\u0009s = (short)f;//correct!\n" +
            "\u0009\u0009s = (short)l;//correct!\n" +
            "\u0009\u0009s = (short)d;//correct!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    /*char type*/
    public String CharConversion() {
        text =
            "class Conversion{" +
            "\u0009public void conver(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 9.9f;" +
            "\u0009\u0009long l = 89l;" +
            "\u0009\u0009double d = 8.8d;" +
            "\u0009\u0009char c;" +
            "\u0009\u0009c = i;//error!\n" +
            "\u0009\u0009c = f;//error!\n" +
            "\u0009\u0009c = l;//error!\n" +
            "\u0009\u0009c = d;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String CharConversionC() {
        text =
            "class Conversion{" +
            "\u0009public void conver(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 9.9f;" +
            "\u0009\u0009long l = 89l;" +
            "\u0009\u0009double d = 8.8d;" +
            "\u0009\u0009char c;" +
            "\u0009\u0009c = (char)i;//correct!\n" +
            "\u0009\u0009c = (char)f;//correct!\n" +
            "\u0009\u0009c = (char)l;//correct!\n" +
            "\u0009\u0009c = (char)d;//correct!\n" +
            "\u0009}" +
            "}";
        return text;
    }


    /*String conversion*/
    public String StringConversion() {
        text =
            "class Conversion{" +
            "\u0009public void conver(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 9.9f;" +
            "\u0009\u0009long l = 89l;" +
            "\u0009\u0009double d = 8.8d;" +
            "\u0009\u0009char c = (char)i;" +
            "\u0009\u0009String s ;" +
            "\u0009\u0009s = i;//error!\n" +
            "\u0009\u0009s = f;//error!\n" +
            "\u0009\u0009s = l;//error!\n" +
            "\u0009\u0009s = d;//error!\n" +
            "\u0009\u0009s = c;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String StringConversionC1() {
        text =
            "class Conversion{" +
            "\u0009public void conver(){" +
            "\u0009\u0009int i = 1;" +
            "\u0009\u0009float f = 9.9f;" +
            "\u0009\u0009long l = 89l;" +
            "\u0009\u0009double d = 8.8d;" +
            "\u0009\u0009String s = " + "\"" + "string" + "\"" + ";" +
            "\u0009\u0009s = s + i + f + l+ d ;" +
            "\u0009}" +
            "}";
        return text;
    }

    /*Assignment conversion*/
    public String ClassTypeMismatch1() {
        text =
            "class A{ }" +
            "class B extends A{ }" +
            "class Test{" +
            "\u0009public static void main(){" +
            "\u0009\u0009A a =new A();" +
            "\u0009\u0009a = new B();" +
            "\u0009\u0009B b = a;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String ClassTypeMismatch2() {
        text =
            "class A{ }" +
            "class Test{" +
            "\u0009public static void main(){" +
            "\u0009\u0009A a =new Integer(2);//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String ClassTypeMismatch3() {
        text =
            "class A{ }" +
            "class Test{" +
            "\u0009public static void main(){" +
            "\u0009int[] ai;" +
            "\u0009\u0009A a = ai;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String ClassTypeMismatch4() {
        text =
            "class A{ }" +
            "class B extends A{ }" +
            "class Test{" +
            "\u0009public static void main(){" +
            "\u0009\u0009A[] a = new A[10];" +
            "\u0009\u0009B[] b = new B[10];" +
            "\u0009\u0009a = b;//correct!\n" +
            "\u0009\u0009b = a;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String ClassTypeMismatch5() {
        text =
            "class A{ }" +
            "final class B extends A{ }" +
            "interface I{}" +
            "class Test{" +
            "\u0009public static void main(){" +
            "\u0009\u0009A a = new A();" +
            "\u0009\u0009B b = new B();" +
            "\u0009\u0009b =(I) a;//error!\n" +
            "\u0009}" +
            "}";
        return text;
    }

    public String ClassTypeMismatchC1() {
        text =
            "class A{ }" +
            "class B extends A{ }" +
            "class Test{" +
            "\u0009public static void main(){" +
            "\u0009\u0009A a =new A();" +
            "\u0009\u0009a = new B();" +
            "\u0009\u0009B b = (B) a;//correct!\n" +
            "\u0009}" +
            "}";
        return text;
    }
}
