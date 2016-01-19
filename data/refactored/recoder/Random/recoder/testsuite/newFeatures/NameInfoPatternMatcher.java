package recoder.testsuite.newFeatures;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ServiceConfiguration;
import recoder.abstraction.ClassType;
import recoder.abstraction.Method;
import recoder.abstraction.Type;
import recoder.service.NameInfo;

public class NameInfoPatternMatcher extends TestCase {
    public void testGetTypes() {
        ServiceConfiguration sc = new CrossReferenceServiceConfiguration();
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        NameInfo ni = sc.getNameInfo();

        // populate name info first
        ni.getJavaLangObject();
        ni.getClassType("java.util.List");

        List<ClassType> cts = ni.getClassTypes("**Object");
        check(cts, "java.lang.Object");
        cts = ni.getClassTypes("*Object");
        check(cts);
    }

    private void check(List<ClassType> cts, String ... exp) {
        assertEquals(exp.length, cts.size());
        for (int i = 0; i < exp.length; i++)
            assertEquals(exp[i], cts.get(i).getFullName());
    }

    private void check2(List<Method> cts, String ... exp) {
        List<String> exps = Arrays.asList(exp);
        try {
            assertEquals(exp.length, cts.size());
            for (int i = 0; i < exp.length; i++) {
                String s = cts.get(i).getFullName() + "(" + makeSigString(cts.get(i).getSignature(), false) + ")";
                assertTrue(exps.contains(s));
            }
        } catch (junit.framework.AssertionFailedError e) {
            int i = 0;
            for (Method m: cts) {
                System.out.println(m.getFullName() + "(" + makeSigString(cts.get(i).getSignature(), false) + ")");
                i++;
            }
            for (String s: exp) {
                System.out.println(s);
            }
            throw e;
        }
    }

    private String makeSigString(List<Type> signature, boolean fullName) {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (Type t: signature) {
            if (!first)
                result.append(',');
             else
                first = false;
            if (fullName)
                result.append(t.getFullName());
             else
                result.append(t.getName());
        }
        return result.toString();
    }


    public void testGetMethods() {
        ServiceConfiguration sc = new CrossReferenceServiceConfiguration();
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        NameInfo ni = sc.getNameInfo();

        // populate name info first
        ni.getJavaLangObject();
        ni.getClassType("java.util.List");

        List<Method> ms = ni.getMethods("**.equals(*)");
        check2(ms, "java.lang.Object.equals(Object)",
                "java.util.List.equals(Object)", "java.util.Collection.equals(Object)");
    }
}
