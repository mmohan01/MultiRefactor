package recoder.testsuite.newFeatures;

import java.util.List;

import recoder.ParserException;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument;
import recoder.convenience.Naming;
import recoder.java.CompilationUnit;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.TypeReference;
import recoder.kit.TypeKit;
import recoder.testsuite.RecoderTestCase;

public class SmallFeatures extends RecoderTestCase {

    public void testCrossReferenceIssues() throws ParserException {
        String cuText =
            "class A<T> {\n" +
            "\u0009A<java.util.List> f1;\n" +
            "\u0009A[] f2;\n" +
            "\u0009A<java.util.List> f3;\n" +
            "}";
        List<CompilationUnit> cus = runIt(cuText);
        TypeDeclaration typeA = cus.get(0).getTypeDeclarationAt(0);
        List<TypeReference> trs = sc.getCrossReferenceSourceInfo().getReferences(typeA);
        assertEquals(2, trs.size());
        trs = sc.getCrossReferenceSourceInfo().getReferences(typeA, true);
        assertEquals(3, trs.size());
        trs = sc.getCrossReferenceSourceInfo().getReferences(typeA, false);
        assertEquals(2, trs.size());
        trs = sc.getCrossReferenceSourceInfo().getReferences(sc.getNameInfo().getClassType("java.util.List"));
        assertEquals(2, trs.size());
    }

    public void testTypeKitCreateTypeReferenceForParamType() throws ParserException {
        String cuText =
            "import java.util.*;\n class A { void foo() { }}";
        MethodDeclaration md = (MethodDeclaration)runIt(cuText).get(0).getTypeDeclarationAt(0).getMembers().get(0);
        Type t = sc.getNameInfo().getType("java.util.HashMap<java.lang.String,? extends java.awt.AWTEvent>");
        TypeReference tr = TypeKit.createTypeReference(sc.getSourceInfo(), t, md);
        String s = Naming.toPathName(tr) + "<";
        boolean first = true;
        for (TypeArgumentDeclaration ta: tr.getTypeArguments()) {
            if (!first)
                s += ",";
            first = false;
            s = s + TypeArgument.DescriptionImpl.getFullDescription(ta, true);
        }
        s += ">";
        assertEquals("HashMap<String,? extends java.awt.AWTEvent>", s);
    }
}
