package recoder.testsuite.java5test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.EventObject;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.abstraction.AnnotationUse;
import recoder.abstraction.ClassType;
import recoder.abstraction.Constructor;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.Type;
import recoder.abstraction.Variable;
import recoder.bytecode.AnnotationUseInfo;
import recoder.bytecode.MethodInfo;
import recoder.convenience.ForestWalker;
import recoder.convenience.Format;
import recoder.convenience.Naming;
import recoder.convenience.TreeWalker;
import recoder.java.Comment;
import recoder.java.CompilationUnit;
import recoder.java.Declaration;
import recoder.java.Expression;
import recoder.java.ProgramElement;
import recoder.java.SourceElement.Position;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.EnumConstantSpecification;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.VariableReference;
import recoder.kit.MiscKit;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;
import recoder.service.AmbiguousStaticFieldImportException;
import recoder.service.ErrorHandler;
import recoder.service.NameInfo;
import recoder.service.SourceInfo;
import recoder.service.UnresolvedReferenceException;
import recoder.service.ConstantEvaluator.EvaluationResult;
import recoder.testsuite.RecoderTestCase;
import recoder.util.Debug;
import recoder.util.HashCode;
import recoder.util.Index;
import recoder.util.Order;
import recoder.util.Sorting;

/*
 * Created on 11.03.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 */

/**
 * @author Tobias Gutzmann
 *
 */
public class Java5Test extends RecoderTestCase {
    private boolean silent = true;

    public void testComments() {
        setPath("test/java5/input/comments", "test/java5/output/comments");
        sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
        runIt();

        ForestWalker fw = new ForestWalker(sc.getSourceFileRepository().getCompilationUnits());
        while (fw.next()) {
            ProgramElement pe = fw.getProgramElement();
            List<Comment> cl = pe.getComments();
            if (cl != null) {
                for (int i = 0; i < cl.size(); i++) {
                    Comment c = cl.get(i);
                    String name = pe.getClass().getSimpleName();
                    if (c.getText().indexOf(name) == -1) {
                        //System.err.println(pe.getClass().getName() + " - " + c.getText());
    }
                }
            }
        }

        reparseAndCompare("comments");
    }

    public void testAmbiguities() {
        setPath("test/java5/input/errortest");
        final ErrorHandler defaultErrorHandler = sc.getProjectSettings().getErrorHandler();
        defaultErrorHandler.setErrorThreshold(9999);
        sc.getProjectSettings().setErrorHandler(new ErrorHandler(){
            private int errNum = 0;
            public int getErrorThreshold() {
                return 9999;
            }
            public void setErrorThreshold(int maxCount) { Debug.assertBoolean(false); }
            public void modelUpdating(EventObject event) { /* ignore */
    }
            public void modelUpdated(EventObject event) { assertTrue("Not enough errors, only " + errNum, errNum == 10); }

            public void reportError(Exception e) throws RuntimeException {
                switch (errNum++) {
                    case 0: assertTrue(e instanceof AmbiguousStaticFieldImportException);
                            break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                            assertTrue(e instanceof UnresolvedReferenceException);
                            break;
                    case 10:
                        // SemanticsChecker: abstract method in non-abstract class.
                        break;
                    case 11:
                        // TODO: caused by semantics checker (reports an error again...)
                        break;
                    default:
                            System.err.println("failing:\n" + "    " + e.getMessage());
                            fail("Too many errors");
                }
                if (!silent) {
                    System.out.print("ok: ");
                    // taken from DefaultErrorHandler and slightly modified
                    String className = e.getClass().getName();
                    className = className.substring(className.lastIndexOf('.') + 1);
                    System.out.println("*** " + errNum + ": " + className);
                    System.out.println("    " + e.getMessage());
                    System.out.println();
                    // end of 'taken from DefaultErrorHandler'
                }
            }
            @Override
             public int getErrorCount() {
                return errNum;
            }
        });

        runIt();
    }

        //reparseAndCompare("errortest"); would cause too many errors

    private String getAnnotationName(AnnotationUse au) {
        if (au instanceof AnnotationUseInfo) {
            AnnotationUseInfo aus = (AnnotationUseInfo)au;
            return sc.getByteCodeInfo().getAnnotationType(aus).getFullName();
        } else {
            AnnotationUseSpecification aus = (AnnotationUseSpecification)au;
            return sc.getSourceInfo().getAnnotationType(aus).getFullName();
        }
    }

    public void testAnnotations() {
        setPath("test/java5/input/annotationtest", "test/java5/output/annotationtest");
        sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
        runIt();
        // test source code annotation support
        NameInfo ni = sc.getNameInfo();
        Package p = ni.getPackage("annotationtest");
        List<? extends  AnnotationUse> ann = p.getPackageAnnotations();
        assertTrue(ann.size() == 1);
        assertTrue(getAnnotationName(ann.get(0)).equals("annotationtest.Annot"));

        p = ni.getPackage("a");
        ann = p.getPackageAnnotations();
        assertTrue(ann.size() == 1);
        assertTrue(getAnnotationName(ann.get(0)).equals("a.B"));
        assertTrue(ni.getType("annotationtest.package-info") == null);
        assertTrue(ni.getType("a.package-info") == null);

        // test bytecode annotation support
        ClassType ct = ni.getClassType("java.lang.annotation.Retention");
        assertTrue(ct != null);
        assertTrue(ct.isAnnotationType());
        assertTrue(ct.getAllSupertypes().size() == 3);

        ct = ni.getClassType("a.C");
        List<? extends  Method> ml = ct.getMethods();
        StringBuilder output = new StringBuilder();
        for (int i = 0, s = ml.size(); i < s; i++) {
            MethodInfo m = (MethodInfo)ml.get(i);
            String param[] = m.getParameterTypeNames();

            output.append(m.getName() + "(");
            boolean first = true;
            for (int j = 0; j < param.length; j++) {
                if (!first)
                    output.append(",");
                first = false;
                AnnotationUseInfo annot[] = m.getAnnotationsForParam(j);
                for (int k = 0; k < annot.length; k++) {
                    output.append(annot[k] + " ");
                }
                output.append(param[j]);
            }
            output.append(")");
        }
        assertEquals(output.toString(),
                "foo(@a.BC int)" +
                "bar(@a.BC int,@a.CD @a.BC int,@a.BC @a.CD int)" +
                "bar2(@a.BC int,int)");
        // 11 methods from Object + 1 from Annotation:
        assertTrue(
        ni.getClassType("a.BC").getAllMethods().size() == 12);

        reparseAndCompare("annotationtest");
    }
    public void testEnums() {
        setPath("test/java5/input/enumtest", "test/java5/output/enumtest");
        sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
        runIt();
        NameInfo ni = sc.getNameInfo();
        EnumDeclaration etd = (EnumDeclaration)ni.getType("enumtest.Color");
        Constructor c = etd.getConstructors().get(0);
        List<ConstructorReference> crl = sc.getCrossReferenceSourceInfo().getReferences(c);
        assertTrue(crl.size() == 3);

        EnumConstantSpecification ecd = (EnumConstantSpecification)ni.getField("enumtest.jls.Operation.PLUS");
        Method m = ecd.getConstructorReference().getClassDeclaration().getMethods().get(0);
        int s = sc.getCrossReferenceSourceInfo().getReferences(m).size();
        assertTrue(s == 1);
        reparseAndCompare("enumtest");
    }

    public void testGenerics() {
        setPath("test/java5/input/generictest", "test/java5/output/generictest");
        sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
        runIt();
        sc.getNameInfo().getType("a.BytecodeGenerics");

        NameInfo ni = sc.getNameInfo();
        TypeDeclaration td = (TypeDeclaration)ni.getType("generictest.D");
        for (int i = 0; i < td.getMethods().size(); i++) {
            Method m = td.getMethods().get(i);
            if (m.getName().equals("foobar")) {
                MethodDeclaration md = (MethodDeclaration)m;
                assertTrue("List<List<Map<String, List<String>>>>".equals(md.getTypeReference().toSource().trim()));
            }
        }
        reparseAndCompare("generictest");
    }

    // used for multiple instance testing...
    private static Object synchObj = new Object();
    private void reparseAndCompare(String path) {
        synchronized(synchObj) {
            try {
                StringWriter oldReport = new StringWriter(10000);
                createExtendedReport(oldReport);
                scrubOutputPath("test/java5/output/" + path + "/");
                writeBack();
                setPath("test/java5/output/" + path);
                runIt();
                StringWriter newReport = new StringWriter(10000);
                createExtendedReport(newReport);
                StringBuffer oldBuf = oldReport.getBuffer();
                StringBuffer newBuf = newReport.getBuffer();
                //assertTrue(oldBuf.length() == newBuf.length());

                for (
                int i = 0, rep = 5; i < Math.min(oldBuf.length(), newBuf.length()) && rep > 0; i++) {
                    if (oldBuf.charAt(i) != newBuf.charAt(i)) {
                        if (i > 1 && oldBuf.charAt(i - 1) == '.') {
                            // may be an anonymous class: skip till next '.', but at most 10(?) digits.
                            // TODO deal with numbers of different length...
                            int j = i + 1;
                            while (Character.isDigit(oldBuf.charAt(j)) && Character.isDigit(newBuf.charAt(j))
                                    && j < i + 10)
                                j++;
                            if (j - i > 6)
                                i = j;
                            continue;
                        }
                        rep--;
                    }
                }
            } catch (IOException e) {
                fail();
            }
        }
    }

    private void scrubOutputPath(String path) {
        File myPath = new File(path);
        File list[] = myPath.listFiles();
        if (list == null) return;
        for (int i = 0; i < list.length; i++) {
            File current = list[i];
            if (current.isDirectory()) {
                scrubOutputPath(current.getAbsolutePath());
                current.delete(); // if possible...
            }
             else if (current.isFile() && current.getName().endsWith(".java")) current.delete();
        }
    }

    public void testPrettyPrinter() {
        setPath("test/java5/input/prettyprinting", "test/java5/output/prettyprinting");
        sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
        runIt();
        ClassDeclaration cd = (ClassDeclaration)sc.getNameInfo().getClassType("A");
        List<? extends  Field> fl = cd.getFields();
        ASTList<Comment> cml = new ASTArrayList<Comment>(1);
        cml.add(new Comment("/*comment to field spec 'a'*/", true));
        ((FieldSpecification)fl.get(0)).setComments(cml);
        MiscKit.unindent((FieldSpecification)fl.get(0));

        reparseAndCompare("prettyprinting");
    }

    public void testParameterizedTypes() throws ParserException {
        // TODO this test doesn't really test so much right now...
        CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
        sc.getProjectSettings().ensureSystemClassesAreInPath();
        sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
        String cuText =
            "class A<T> { T foo(String dummy, T param) throws T { new A<String>(); return null; }}";
        CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
        sc.getChangeHistory().attached(cu);
        sc.getChangeHistory().updateModel();
        cu.validateAll();

        Type t = sc.getSourceInfo().getType(
        ((MethodDeclaration)cu.getTypeDeclarationAt(0).getMembers().get(0)).getBody().getStatementAt(0));
        assertTrue(t instanceof ParameterizedType);
    }

    final static Order UNIT_NAME_ORDER = new Order.CustomLexicalOrder(){
        protected String toString(Object x) {
            return Naming.toCanonicalName((CompilationUnit) x);
        }
    };

    protected void createExtendedReport(Writer out) throws IOException {
        List<CompilationUnit> units = sc.getSourceFileRepository().getCompilationUnits();
        // sort by name
        CompilationUnit[] uarray = new CompilationUnit[units.size()];
        for (int i = 0; i < uarray.length; i++) {
            uarray[i] = units.get(i);
        }
        Sorting.heapSort(uarray, UNIT_NAME_ORDER);
        SourceInfo si = sc.getSourceInfo();
        Index decl2num = new Index(HashCode.IDENTITY);
        EvaluationResult res = new EvaluationResult();
        for (int i = 0, n = 0; i < uarray.length; i += 1) {
            TreeWalker tw = new TreeWalker(uarray[i]);
            while (tw.next()) {
                ProgramElement pe = tw.getProgramElement();
                if (pe instanceof Declaration) {
                    decl2num.put(pe, n);
                }
                n += 1;
            }
        }
        StringBuffer line = new StringBuffer(1024);
        int number = 1;
        for (int i = 0; i < uarray.length; i += 1) {
            CompilationUnit unit = uarray[i];
            TreeWalker tw = new TreeWalker(unit);
            Position oldPos = Position.UNDEFINED;
            while (tw.next()) {
                ProgramElement pe = tw.getProgramElement();
                line.append("(" + (pe.getComments() == null ? 0 : pe.getComments().size()) + " comments)");
                //line.append(number);
                //line.append(' ');
                Position pos = pe.getFirstElement().getStartPosition();
                if (!pos.equals(oldPos)) {
                    //line.append(pos); // we're not really interested in exact positions
                    oldPos = pos;
                }
                line.append(' ');
                String name = pe.getClass().getName();
                name = name.substring(name.lastIndexOf('.') + 1);
                for (int k = 0, s = name.length(); k < s; k += 1) {
                    char c = name.charAt(k);
                    if (Character.isUpperCase(c)) {
                        line.append(c);
                    }
                }
                if (pe instanceof CompilationUnit) {
                    line.append(Format.toString(" \"%u\"", pe));
                }
                if (pe instanceof Expression) {
                    Type t = si.getType(pe);
                    if (t != null) {
                        line.append(" :");
                        if (t instanceof ProgramElement) {
                            line.append(decl2num.get(t));
                        } else {
                            line.append(Format.toString("%N", t));
                        }
                        if (sc.getConstantEvaluator().isCompileTimeConstant((Expression) pe, res)) {
                            line.append(" ==" + res.toString());
                        }
                    }
                }
                int ref = -1;
                if (pe instanceof Constructor) {
                    ref = sc.getCrossReferenceSourceInfo().getReferences((Constructor)pe).size();
                }
                if (pe instanceof Field) {
                    ref = sc.getCrossReferenceSourceInfo().getReferences((Field)pe).size();
                }
                if (pe instanceof Method) {
                    ref = sc.getCrossReferenceSourceInfo().getReferences((Method)pe).size();
                }
                if (pe instanceof Type) {
                    ref = sc.getCrossReferenceSourceInfo().getReferences((Type)pe).size();
                }
                if (pe instanceof Variable) {
                    ref = sc.getCrossReferenceSourceInfo().getReferences((Variable)pe).size();
                }
                if (ref != -1)
                    line.append("(" + ref + " refs)");

                ProgramModelElement dest = null;
                if (pe instanceof ConstructorReference) {
                    dest = si.getConstructor((ConstructorReference) pe);
                } else if (pe instanceof MethodReference) {
                    dest = si.getMethod((MethodReference) pe);
                } else if (pe instanceof VariableReference) {
                    dest = si.getVariable((VariableReference) pe);
                } else if (pe instanceof TypeReference) {
                    dest = si.getType((TypeReference) pe);
                } else if (pe instanceof PackageReference) {
                    dest = si.getPackage((PackageReference) pe);
                }
                if (dest != null) {
                    line.append(" ->");
                    if (dest instanceof ProgramElement) {
                        line.append(decl2num.get(dest));
                    } else {
                        line.append(Format.toString("%N", dest));
                    }
                }
                line.append("\n");
                out.write(line.toString());
                line.setLength(0);
                number += 1;
            }
        }
        out.flush();
    }
}
