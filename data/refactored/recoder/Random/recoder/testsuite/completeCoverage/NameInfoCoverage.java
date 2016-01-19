/**
 * 
 */
package recoder.testsuite.completeCoverage;

import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ParameterizedType;
import recoder.service.NameInfo;
import recoder.testsuite.RecoderTestCase;

/**
 * @author Tobias Gutzmann
 *
 */
public class NameInfoCoverage extends RecoderTestCase {
    public void testGetParameterizedType() {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        NameInfo ni = sc.getNameInfo();
        ClassType ct = ni.getClassType("java.util.List<java.lang.Number>");
        assertTrue(ct.getFullSignature().equals("java.util.List<java.lang.Number>"));
        ClassType ct2 = ni.getClassType("java.util.List<? extends java.lang.Number>");
        assertTrue(ct != ct2);
        assertTrue(!ct.equals(ct2));
        assertTrue(ct2.getFullSignature().equals("java.util.List<? extends java.lang.Number>"));
        ClassType ct3 = ni.getClassType("java.util.List<java.lang.String>[]");
        assertTrue(ct3.getFullSignature().equals("java.util.List<java.lang.String>[]"));
        ArrayType at3 = (ArrayType)ct3;
        ParameterizedType pt3 = (ParameterizedType)at3.getBaseType();
        assertTrue(pt3.getFullSignature().equals("java.util.List<java.lang.String>"));
        // query ct3 again, to see if caching works properly in NameInfo:
        ClassType ct3_2 = ni.getClassType("java.util.List<java.lang.String>[]");
        ArrayType at3_2 = (ArrayType)ct3_2;
        ParameterizedType pt3_2 = (ParameterizedType)at3_2.getBaseType();
        assertTrue(pt3_2 == pt3);
        assertTrue(at3_2 == at3);
        assertTrue(ct3_2 == ct3);
        ClassType ct4 = ni.getClassType("java.util.List<java.util.List<java.lang.String>>");
        assertTrue(ct4.getFullSignature().equals("java.util.List<java.util.List<java.lang.String>>"));
        ClassType ct5 = ni.getClassType("java.util.List<CannotFindThis>");
        assertNull(ct5); // should be null entirely!

        // try java.lang loading
        ClassType ct6 = ni.getClassType("String");
        assertTrue(ct6 == ni.getJavaLangString());
        ClassType ct7 = ni.getClassType("java.util.List<String>");
        assertEquals(ct7, pt3);
        ClassType ct8 = ni.getClassType("Thread");
        assertNotNull(ct8);
    }
}
