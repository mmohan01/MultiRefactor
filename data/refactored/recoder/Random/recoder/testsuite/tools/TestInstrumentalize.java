package recoder.testsuite.tools;

import recoder.java.declaration.ClassDeclaration;
import recoder.testsuite.RecoderTestCase;

public class TestInstrumentalize extends RecoderTestCase {
    public void testBasic1() throws Exception {
        // basic test. Instrumentalize was broken since 0.93, fixed in 0.95.
        setPath("test/tools/Instrumentalize/1");
        runIt();
        new application.Instrumentalize(sc).execute();
    }
    public void testBasic2() throws Exception {
        // This was reported as not working, I cannot reproduce it, though.
        setPath("test/tools/Instrumentalize/2");
        runIt();
        new application.Instrumentalize(sc).execute();
    }
    public void testBasic4() throws Exception {
        setPath("test/tools/Instrumentalize/4");
        runIt();
        new application.Instrumentalize(sc).execute();
        System.out.println(((ClassDeclaration)sc.getNameInfo().getType("SysOutTest")).toSource());
    }

//    public void testCompleteJDK() throws Exception {
//    	String path =  System.getProperty("java.home");
//		path = path.substring(0, path.length()-3); // should remove "jre"...
//		path = path + "src.zip";
//
//		sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, path);
//		sc.getProjectSettings().ensureSystemClassesAreInPath();
//		sc.getProjectSettings().ensureExtensionClassesAreInPath();
//
//		ProgressListener pl = new ProgressListener() {
//			int x = 0;
//			public void workProgressed(ProgressEvent pe) {
//				if (x%50 == 0)
//					System.out.print(".");
//				if (++x == 3000) {
//					System.out.println();
//					x = 0;
//				}
//			}
//		};
//
//		sc.getProjectSettings().setErrorHandler(new DefaultErrorHandler() {
//			@Override
//			public void reportError(Exception e) {
//				if (e instanceof UnresolvedReferenceException) {
//					UnresolvedReferenceException ue = (UnresolvedReferenceException)e;
//					ProgramElement pe = ue.getUnresolvedReference();
//					if (pe instanceof MethodReference) {
//						if (recoder.convenience.Format.toString(pe).startsWith("\"constr.getAnnotation(propertyNamesClass).value()")) {
//							// bug in javac - recoder is of the hook here!
//							return;
//						}
//					}
//				}
//				super.reportError(e);
//			}
//		});
//
//		sc.getSourceFileRepository().addProgressListener(pl);
//		sc.getSourceInfo().addProgressListener(pl);
//
//		System.out.println("Start parsing...");
//		long start = System.currentTimeMillis();
//		List<CompilationUnit> cus = sc.getSourceFileRepository().getAllCompilationUnitsFromPath();
//		System.out.println("\nparsed " + cus.size() + " CUs in " +
//				(((System.currentTimeMillis() - start)/1000) + " seconds"));
//		start = System.currentTimeMillis();
//		System.out.println("Updating model...");
//		start = System.currentTimeMillis();
//		sc.getChangeHistory().updateModel();
//		System.out.println("\nbuilt model in " +
//				(((System.currentTimeMillis() - start)/1000) + " seconds"));
//		start = System.currentTimeMillis();
//		for (CompilationUnit cu : cus)
//			cu.validateAll();
//		System.out.println("\nvalidated in " +
//				(((System.currentTimeMillis() - start)/1000) + " seconds"));
//		start = System.currentTimeMillis();
//        new SemanticsChecker(sc).checkAllCompilationUnits();
//        System.out.println("\nsemantic checks performed in " +
//        		(((System.currentTimeMillis() - start)/1000) + " seconds"));
//
//        new application.Instrumentalize(sc).execute();
//    }
}
