package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;

import java.io.PrintStream;

public class BinaryFunctionTest  extends TestCase {
    List<Integer> list;

    public BinaryFunctionTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(BinaryFunctionTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    private class Print2ndTo1st implements BinaryFunction<PrintStream, Integer, Void> {
        public Void fn(PrintStream ps, Integer i)
         {
            ps.println(i);
            return null;
        }
    } // Print2nd
    private class Print1stTo2nd implements BinaryFunction<Integer, PrintStream, Void> {
        public Void fn(Integer i, PrintStream ps)
         {
            ps.println(i);
            return null;
        }
    }

    public void test1()
     {
        Algorithms.forEach(list, Bind.First(new Print2ndTo1st(), System.out));
    } //

    public void test2()
     {
        Algorithms.forEach(list, Bind.Second(new Print1stTo2nd(), System.out));
    } //
} // BinaryFunctionTest
