package recoder.testsuite.basic.analysis;

import java.util.List;

import junit.framework.Assert;
import recoder.abstraction.Type;
import recoder.java.reference.TypeReference;
import recoder.service.CrossReferenceSourceInfo;
import recoder.service.NameInfo;

public class TypeXReferenceCompletenessTest extends XReferenceCompletenessTest {

    public void testTypeXReferenceCompleteness() {
        CrossReferenceSourceInfo xrsi = config.getCrossReferenceSourceInfo();
        NameInfo ni = config.getNameInfo();

        List<Type> types = ni.getTypes();
        for (int i = 0; i < types.size(); i += 1) {
            Type x = types.get(i);
            List<TypeReference> list = xrsi.getReferences(x);
            for (int j = 0; j < list.size(); j += 1) {
                TypeReference r = list.get(j);
                Type y = xrsi.getType(r);
                if (x != y) {
                    Assert.fail(makeResolutionError(r, x, y));
                }
            }
        }
    }
}
