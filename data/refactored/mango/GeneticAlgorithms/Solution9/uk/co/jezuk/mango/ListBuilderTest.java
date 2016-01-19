package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.List;

public class ListBuilderTest extends TestCase {
    public ListBuilderTest(final String name) { super(name); }
    public static Test suite() { return new TestSuite(ListBuilderTest.class); }

    public void test1()
     {
        final List<String> l = Collections.list("fish");
        assertEquals(1, l.size());
        assertEquals("fish", l.get(0));
    } // test1

    public void test2()
     {
        final List<String> l = Collections.list("fish", "pish");
        assertEquals(2, l.size());
        assertEquals("fish", l.get(0));
        assertEquals("pish", l.get(1));
    } // test2
} // ListBuilderTest
