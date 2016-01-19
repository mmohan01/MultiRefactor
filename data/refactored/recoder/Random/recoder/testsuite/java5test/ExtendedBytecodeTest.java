/**
 * created May 14, 2010
 */
package recoder.testsuite.java5test;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.Type;

import recoder.abstraction.ClassType;
import recoder.abstraction.ElementValuePair;
import recoder.abstraction.Method;
import recoder.bytecode.ASMBytecodeParser;
import recoder.bytecode.AnnotationPropertyInfo;
import recoder.bytecode.AnnotationUseInfo;
import recoder.bytecode.ClassFile;
import recoder.bytecode.ElementValuePairInfo;
import recoder.bytecode.EnumConstantReferenceInfo;
import recoder.bytecode.FieldInfo;
import recoder.bytecode.MethodInfo;
import recoder.bytecode.TypeArgumentInfo;
import recoder.bytecode.TypeParameterInfo;
import recoder.service.NameInfo;
import recoder.testsuite.RecoderTestCase;
import recoder.util.FileUtils;

/**
 * @author Tobias Gutzmann
 *
 */
public class ExtendedBytecodeTest extends RecoderTestCase {
    public void testTypeVarException() throws Exception {
        // source code of compiled byte code:
        //class X<T extends Exception> {
        //	<E extends Exception> void foo(E e) throws E, IOException, T {
        //		throw e;
        //	}
        //	void bar() throws Exception {
        //		this.<IOException>foo(null);
        //	}
        //}
        setPath("test/java5/input/extendedBytecode/typeVarException");
        List<ClassType> exc = sc.getNameInfo().getClassType("X").getMethods().get(0).getExceptions();
        assertEquals(3, exc.size());
        assertTrue(exc.get(0) instanceof TypeParameterInfo);
        assertTrue(exc.get(0).getName().equals("E"));
        assertTrue(exc.get(1) instanceof ClassFile);
        assertTrue(exc.get(1).getFullName().equals("java.io.IOException"));
        assertTrue(exc.get(2) instanceof TypeParameterInfo);
        assertTrue(exc.get(2).getName().equals("T"));
    }

    public void testClassTypeBound() throws Exception {
        setPath("test/java5/input/extendedBytecode/innerClassTypeBound");
        // source code of compiled byte code:
//		class Y<E> {
//			E hello() { return null; }
//			<T extends Y<String>> T foo() {
//				return null;
//			}
//		}
        String cuText =
            "class SC {\n" +
            "\u0009void foo(Y<Number> t) {\n" +
            "\u0009\u0009t.foo().hello().equalsIgnoreCase(\"\");\n" +
            "\u0009}\n" +
            "}\n";
        runIt(cuText);
    }

    public void testInnerClassTypeBound() throws Exception {
        setPath("test/java5/input/extendedBytecode/innerClassTypeBound");
        // source code of compiled byte code:
//		class X<E> {
//			class Inner {
//				E hello() { return null; }
//				<T extends X<String>.Inner> T foo() {
//					return null;
//				}
//			}
//		}
        String cuText =
            "class SC {\n" +
            "\u0009void foo(X<Number>.Inner t) {\n" +
            "\u0009\u0009t.foo().hello().equalsIgnoreCase(\"\");\n" +
            "\u0009}\n" +
            "}\n";
        runIt(cuText);
    }
    public void testInnerClassTypeBound_2() throws Exception {
        // as above, but invalid source code
        setPath("test/java5/input/extendedBytecode/innerClassTypeBound");
        String cuText =
            "class SC {\n" +
            "\u0009void foo(X.Inner t) {\n" +
            "\u0009\u0009t.foo().hello().equalsIgnoreCase(\"\");\n" +
            "\u0009}\n" +
            "}\n";
        runIt(new SilentErrorHandler(1), cuText);
    }
    public void testInnerClassTypeBoundCompletelyInSourceCode() throws Exception {
        // same as above, but completely in source code
        String cuText =
            "class X<E> {\n" +
            "\u0009class Inner {\n" +
            "\u0009\u0009E hello() { return null; }\n" +
            "\u0009\u0009<T extends X<String>.Inner> T foo() {\n" +
            "\u0009\u0009\u0009return null;\n" +
            "\u0009\u0009}\n" +
            "\u0009}\n" +
            "}\n" +
            "class SC {\n" +
            "\u0009void foo(X<Number>.Inner t) {\n" +
            "\u0009\u0009t.foo().hello().equalsIgnoreCase(\"\");\n" +
            "\u0009}\n" +
            "}\n";
        runIt(cuText);
    }
    public void testInnerClassTypeBoundCompletelyInSourceCode_2() throws Exception {
        // same as above, but completely in source code
        String cuText =
            "class X<E> {\n" +
            "\u0009class Inner {\n" +
            "\u0009\u0009E hello() { return null; }\n" +
            "\u0009\u0009<T extends X<String>.Inner> T foo() {\n" +
            "\u0009\u0009\u0009return null;\n" +
            "\u0009\u0009}\n" +
            "\u0009}\n" +
            "}\n" +
            "class SC {\n" +
            "\u0009void foo(X.Inner t) {\n" +
            "\u0009\u0009t.foo().hello().equalsIgnoreCase(\"\");\n" +
            "\u0009}\n" +
            "}\n";
        runIt(new SilentErrorHandler(1), cuText);
    }

    public void testAnnotations() throws Exception {
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        ClassType override = sc.getNameInfo().getClassType("java.lang.Override");
        assertEquals(2, override.getAnnotations().size());

        sc.getNameInfo().getClassType("java.lang.String");
        Method deprMetho = sc.getNameInfo().getMethods("java.lang.String.String(byte[],int,int,int)", true).get(0);
        assertEquals(1, deprMetho.getAnnotations().size());

        assertEquals(1, sc.getNameInfo().getField("java.lang.Character.UnicodeBlock.SURROGATES_AREA").getAnnotations().size());
    }

    public void testAnnotations2() throws Exception {
        setPath("test/java5/input/extendedBytecode/annotation/");
        sc.getProjectSettings().ensureSystemClassesAreInPath();

        NameInfo ni = sc.getNameInfo();
        List<AnnotationUseInfo> ans;
        ClassFile ct;
        AnnotationUseInfo an;
        List<ElementValuePairInfo> evps;
        ElementValuePair evp;

        ct = (ClassFile)ni.getClassType("java.beans.ConstructorProperties");
        ans = ct.getAnnotations(); // @Documented @Target(CONSTRUCTOR) @Retention(RUNTIME)
        assertEquals(
        "java.lang.annotation.Documented", ans.get(0).getFullReferencedName());
        an = ans.get(1);
        assertEquals("java.lang.annotation.Target", an.getFullReferencedName());
        evps = an.getElementValuePairs();
        assertEquals(1, evps.size());

        assertEquals("@java.lang.annotation.Documented", ans.get(0).toString());
        assertEquals("@java.lang.annotation.Target(value={java.lang.annotation.ElementType.CONSTRUCTOR})", ans.get(1).toString());
        assertEquals("@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)", ans.get(2).toString());

        ct = (ClassFile)ni.getClassType("X");
        ans = ct.getFields().get(0).getAnnotations();
        // @javax.xml.bind.annotation.XmlElements(value={@javax.xml.bind.annotation.XmlElement(name="Text",
        // namespace="aNameSpace",
        // type=java.lang.Object.class)})
        an = ans.get(0);
        assertEquals("@javax.xml.bind.annotation.XmlElements(value={@javax.xml.bind.annotation.XmlElement(name=\"Text\",namespace=\"aNameSpace\",type=java.lang.Object.class)})", an.toString());

        // java.lang.management.LockInfo
        // public LockInfo(String className, int identityHashCode)
        // @ConstructorProperties({"className", "identityHashCode"})
        ct = (ClassFile)ni.getClassType("java.lang.management.LockInfo");
        an = ct.getConstructorInfos().get(0).getAnnotations().get(0);
        assertEquals("@java.beans.ConstructorProperties(value={\"className\",\"identityHashCode\"})", an.toString());

        // com.sun.xml.internal.ws.api.FeatureConstructor:
        // default of value() is {}.
        ct = (ClassFile)ni.getClassType("com.sun.xml.internal.ws.api.FeatureConstructor");
        Object defaultValue = ((AnnotationPropertyInfo)ct.getMethodInfos().get(0)).getDefaultValue();
        assertTrue(defaultValue instanceof Object[]);
        assertEquals(0, ((Object[])defaultValue).length);

        // javax.xml.ws.RespectBinding
        // @Target(ElementType.TYPE)
        // @Retention(RetentionPolicy.RUNTIME)
        // @Documented
        // @WebServiceFeatureAnnotation(id=RespectBindingFeature.ID,bean=RespectBindingFeature.class)
        ct = (ClassFile)ni.getClassType("javax.xml.ws.RespectBinding");
        ans = ct.getAnnotations();
        String targetStr;
        if (ct.getVersion() >= 51) {
            targetStr = "@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE,java.lang.annotation.ElementType.METHOD,java.lang.annotation.ElementType.FIELD})";
        } else {
            targetStr = "@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE})";
        }
        assertEquals(targetStr, ans.get(0).toString());
        assertEquals("@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)", ans.get(1).toString());
        assertEquals("@java.lang.annotation.Documented", ans.get(2).toString());
        assertEquals("@javax.xml.ws.spi.WebServiceFeatureAnnotation(id=\"javax.xml.ws.RespectBindingFeature\",bean=javax.xml.ws.RespectBindingFeature.class)", ans.get(3).toString());

        ct = (ClassFile)ni.getClassType("javax.annotation.Resource");
        for (MethodInfo mi: ct.getMethodInfos()) {
            AnnotationPropertyInfo api = (AnnotationPropertyInfo)mi;
            defaultValue = api.getDefaultValue();
            if (api.getName().equals("name")) {
                assertTrue(defaultValue instanceof String);
                assertEquals("", defaultValue);
            } else if (api.getName().equals("type")) {
                assertTrue(defaultValue instanceof Type); // will fail with old bytecode parser.
                assertEquals(
                "java.lang.Object", ((Type)defaultValue).getClassName());
            } else if (api.getName().equals("authenticationType")) {
                assertTrue(defaultValue instanceof EnumConstantReferenceInfo);
                assertEquals("javax.annotation.Resource.AuthenticationType.CONTAINER", defaultValue.toString());
            } else if (api.getName().equals("shareable")) {
                assertTrue(defaultValue instanceof Boolean);
                assertEquals(Boolean.TRUE, defaultValue);
            }
        // javax.annotation.Resource
        // default values (different annotation properties)
        }
    }

    public void testReadCompleteRTJar() throws Exception {
        File f = FileUtils.getPathOfSystemClasses();
        readJar(f);
    }

    public void testReadGoogleGuava() throws Exception {
        readJar(new File("test/java5/input/googleGuava/guava-r09.jar"));
        readJar(new File("test/java5/input/googleGuava/guava-r09-gwt.jar"));
    }

    private void readJar(File f) throws Exception {
        ZipFile zf = new ZipFile(f);
        Enumeration<? extends  ZipEntry> zes = zf.entries();
        while (zes.hasMoreElements()) {
            ZipEntry ze = zes.nextElement();
            if (!ze.getName().endsWith(".class"))
                continue;
            InputStream is = zf.getInputStream(ze);
            ClassFile cf = new ASMBytecodeParser().parseClassFile(is, ze.getName(), true);

            check(cf);
        }
    }

    private void check(ClassFile cf) {
        checkMe(cf);
        checkMethods(cf.getMethodInfos());
        checkMethods(cf.getConstructorInfos());
        checkAnnotations(cf.getAnnotations());
        checkFields(cf.getFieldInfos());
        checkTypeArg(cf.getSuperClassTypeArguments());
        for (int i = 0; i < cf.getInterfaceNames().length; i++)
            checkTypeArg(cf.getSuperInterfaceTypeArguments(i));
        checkTypeParams(cf.getTypeParameters());
    }
    private void checkMethods(List<? extends  MethodInfo> mis) {
        if (mis == null)
            return;
        for (MethodInfo mi: mis) {
            checkMe(mi);
            checkAnnotations(mi.getAnnotations());
            for (int i = 0; i < mi.getParameterTypeNames().length; i++)
                checkTypeArg(mi.getTypeArgumentsForParam(i));
            checkTypeArg(mi.getTypeArgumentsForReturnType());
        }
    }
    private void checkAnnotations(List<AnnotationUseInfo> annots) {
        if (annots == null)
            return;
        for (AnnotationUseInfo ai: annots) {
            checkMe(ai);
            checkElementValuePairs(ai.getElementValuePairs());
        }
    }
    private void checkTypeArg(List<TypeArgumentInfo> tais) {
        if (tais == null)
            return;
        for (TypeArgumentInfo tai: tais) {
            checkMe(tai);
            checkTypeArg(tai.getTypeArguments());
        }
    }
    private void checkTypeParams(List<TypeParameterInfo> tpis) {
        if (tpis == null)
            return;
        for (TypeParameterInfo tp: tpis) {
            checkMe(tp);
            for (int i = 0; i < tp.getBoundCount(); i++) {
                checkTypeArg(tp.getBoundTypeArguments(i));
            }
        }
    }
    private void checkFields(List<FieldInfo> fis) {
        if (fis == null)
            return;
        for (FieldInfo fi: fis) {
            checkMe(fi);
            checkAnnotations(fi.getAnnotations());
            checkTypeArg(fi.getTypeArguments());
        }
    }
    private void checkElementValuePairs(List<ElementValuePairInfo> evps) {
        if (evps == null)
            return;
        for (ElementValuePairInfo evp: evps) {
            checkMe(evp);
            Object value = evp.getValue();
            if (value instanceof AnnotationUseInfo) {
                checkAnnotations(Collections.singletonList((AnnotationUseInfo)value));
            }
        }
    }


    private void checkMe(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field: fields) {
            if (List.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    List<?> tmpList = (List<?>)field.get(o);
                    if (!(tmpList instanceof ArrayList))
                        continue;
                    ArrayList<?> aal = (ArrayList<?>)tmpList;
                    try {
                        Field f = ArrayList.class.getDeclaredField("elementData");
                        f.setAccessible(true);
                        if (((Object[])f.get(aal)).length != aal.size())
                            System.out.println("Trimming: " + o.getClass().getName() + "." + field.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                    aal.trimToSize();
                } catch (Exception e) {
                    // ignore!
    }
            }
        }
    }
}
