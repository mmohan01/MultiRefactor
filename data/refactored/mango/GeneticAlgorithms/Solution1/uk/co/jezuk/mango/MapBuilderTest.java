package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.Map;

public class MapBuilderTest extends TestCase {
    public MapBuilderTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(MapBuilderTest.class); }

    public void test1()
     {
        Map<String, String> map = Collections.map("fish", "bicycle");
        assertEquals(1, map.size());
        assertEquals("bicycle", map.get("fish"));
    } // test1

    public void test2()
     {
        Map<String, String> map = Collections.map("fish", "bicycle").
                                          map("croup", "throat");
        assertEquals(2, map.size());
        assertEquals("bicycle", map.get("fish"));
        assertEquals("throat", map.get("croup"));
    } // test2

    public void test3()
     {
        Map<String, String> map = Collections.map("fish", "bicycle").
                                          map("croup", "throat").
                                          map("bobby", "widget");
        assertEquals(3, map.size());
        assertEquals("bicycle", map.get("fish"));
        assertEquals("throat", map.get("croup"));
        assertEquals("widget", map.get("bobby"));
    } // test3

    public void test4()
     {
        Map<String, String> map = Collections.map("fish", "bicycle").
                                          map("croup", "throat").
                                          map("bobby", "widget").
                                          map("fish", "tandem");

        assertEquals(3, map.size());
        assertEquals("tandem", map.get("fish"));
        assertEquals("throat", map.get("croup"));
        assertEquals("widget", map.get("bobby"));
    } // test4
} // MapBuilderTest
