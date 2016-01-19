/**
 * 
 */
package recoder.testsuite.exhaustive;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import recoder.backport.Backport;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import recoder.service.SemanticsChecker;
import recoder.service.TooManyErrorsException;
import recoder.testsuite.RecoderTestCase;

/**
 * @author Tobias Gutzmann
 *
 */
public class ReadCorpus extends RecoderTestCase {
    // Hard-coded paths for now...
    private static final String PATH_TO_CORPUS = "d:/qualitas_refined/";
    private static final String PATH_TO_JDK_6 = "C:/Program Files (x86)/Java/jdk1.6.0_35/";
    private static final String PATH_TO_JDK_5 = "C:/Program Files (x86)/Java/jdk1.5.0_22/";
    private static final String PATH_TO_JDK_1_4 = "C:/j2sdk1.4.2_19/";

    private static final boolean RUN_TRANSFORMATIONS = false;

    private void doIt(String projectName) throws Exception {
        doIt(projectName, 6);
    }

    private void doIt(String projectName, int javaVersion) throws Exception {
        final String base = PATH_TO_CORPUS + "/" + projectName + "/";
        assertTrue(new File(base).exists());
        String jdkPath;
        switch (javaVersion) {
            case 4:
                sc.getProjectSettings().setProperty(PropertyNames.JAVA_5, "false");
                jdkPath = PATH_TO_JDK_1_4;
                break;
            case 5:
                jdkPath = PATH_TO_JDK_5;
                break;
            case 6:
                jdkPath = PATH_TO_JDK_6;
                break;
            default: throw new IllegalArgumentException("invalid javaVersion: " + javaVersion);
        }
        assertTrue(new File(jdkPath).exists());
        String inputPath = jdkPath + "jre/lib/*.jar;" + ";" + base + "src/;";
        if (new File(base + "lib/").exists())
            inputPath += base + "lib/*.jar";

        if (RUN_TRANSFORMATIONS) {
            Backport.main(inputPath, "d:/tmp/corpus_trans");
            return;
        } // done, don't do any more

        sc.
        getProjectSettings().setProperty(PropertyNames.INPUT_PATH, inputPath);
        List<CompilationUnit> cus = sc.getSourceFileRepository().getAllCompilationUnitsFromPath(new FilenameFilter(){
            @Override
             public boolean accept(File dir, String name) {
                if (dir == null)
                    return false; // from archive, ignore in our case.
                return name.endsWith(".java");
            }
        });
        sc.getChangeHistory().updateModel();
        for (CompilationUnit cu: cus)
            cu.validateAll();
        new SemanticsChecker(sc).checkAllCompilationUnits();
        assertEquals("Project should be read error-free.", 0, sc.getProjectSettings().getErrorHandler().getErrorCount());
    }

    public void testANT() throws Exception {
        doIt("ant");
    }
    public void testANTLR() throws Exception {
        doIt("antlr");
    }
    public void testAOI() throws Exception {
        doIt("aoi");
    }
    public void testARGOUML() throws Exception {
        doIt("argouml");
    }
    public void testAXION() throws Exception {
        doIt("axion", 4);
    }
    public void testAZUREUS() throws Exception {
        doIt("azureus");
    }
    public void testC_JDBC() throws Exception {
        doIt("c_jdbc", 5);
    }
    public void testCAYENNE() throws Exception {
        doIt("cayenne");
    }
    public void testCHECKSTYLE() throws Exception {
        // Requires tools.jar
        doIt("checkstyle");
    }
    public void testCOBERTURA() throws Exception {
        doIt("cobertura");
    }
    public void testCOLT() throws Exception {
        doIt("colt");
    }
    public void testCOLUMBA() throws Exception {
        doIt("columba");
    }
    public void testCOMPIERE() throws Exception {
        // rename .zip to .jar in lib/ directory.
        doIt("compiere");
    }
    public void testDISPLAYTAG() throws Exception {
        doIt("displaytag");
    }
    public void testDRAWSWF() throws Exception {
        doIt("drawswf", 4);
    }
    public void testDRJAVA() throws Exception {
        // Requires tools.jar (from Java 5)
        doIt("drjava", 5);
    }
    public void testEMMA() throws Exception {
        doIt("emma", 4);
    }
    public void testFINDBUGS() throws Exception {
        doIt("findbugs");
    }
    public void testFITJAVA() throws Exception {
        doIt("fitjava");
    }
    public void testFITLIBRARYFORFITNESSE() throws Exception {
        doIt("fitlibraryforfitnesse");
    }
    public void testFREECOL() throws Exception {
        doIt("freecol");
    }
    public void testFREECS() throws Exception {
        doIt("freecs");
    }
    public void testGALLEON() throws Exception {
        doIt("galleon");
    }
    public void testGANTTPROJECT() throws Exception {
        doIt("ganttproject");
    }
    public void testHERITRIX() throws Exception {
        try {
            doIt("heritrix");
            fail("Exception expected");
        } catch (TooManyErrorsException e) {
            // invalid project setup (missing libraries), expected.
    }
    }
    public void testHSQLDB() throws Exception {
        // rename .zip to .jar in lib/ directory.
        doIt("hsqldb");
    }
    public void testHTMLUNIT() throws Exception {
        doIt("htmlunit");
    }
    public void testINFORMA() throws Exception {
        doIt("informa");
    }
    public void testITEXT() throws Exception {
        doIt("itext");
    }
    public void testIVATAGROUPWARE() throws Exception {
        doIt("ivatagroupware");
    }
    public void testJAG() throws Exception {
        doIt("jag");
    }
    public void testJASML() throws Exception {
        doIt("jasml");
    }
    public void testJASPERREPORTS() throws Exception {
        doIt("jasperreports");
    }
    public void testJAVACC() throws Exception {
        doIt("javacc");
    }
    public void testJBOSS() throws Exception {
        doIt("jboss");
    }
    public void testJCHEMPAINT() throws Exception {
        doIt("jchempaint");
    }
    public void testJEDIT() throws Exception {
        doIt("jedit");
    }
    public void testJENA() throws Exception {
        doIt("jena");
    }
    public void testJEXT() throws Exception {
        doIt("jext", 4);
    }
    public void testJFIN_DATEMATH() throws Exception {
        doIt("jFin_DateMath");
    }
    public void testJFREECHART() throws Exception {
        doIt("jfreechart");
    }
    public void testJGRAPH() throws Exception {
        doIt("jgraph");
    }
    public void testJGRAPHPAD() throws Exception {
        doIt("jgraphpad");
    }
    public void testJGRAPHT() throws Exception {
        doIt("jgrapht");
    }
    public void testJGROUPS() throws Exception {
        doIt("jgroups");
    }
    public void testJHOTDRAW() throws Exception {
        // Invalid code, compiles with javac only. 1 Error expected.
        try {
            doIt("jhotdraw");
        } catch (TooManyErrorsException e) {
            assertEquals(1, e.getErrors().size());
        }
    }
    public void testJMETER() throws Exception {
        doIt("jmeter");
    }
    public void testJMONEY() throws Exception {
        doIt("jmoney");
    }
    public void testJPD() throws Exception {
        doIt("jpf");
    }
    public void testJRAT() throws Exception {
        // Requires tools.jar
        doIt("jrat", 4);
    }
    public void testJREFACTORY() throws Exception {
        // Requires JAI. Download from http://download.java.net/media/jai/builds/release/1_1_3/
        doIt("jrefactory");
    }
    public void testJSPWIKI() throws Exception {
        doIt("jspwiki");
    }
    public void testJSXE() throws Exception {
        doIt("jsXe", 4);
    }
    public void testJUNG() throws Exception {
        doIt("jung", 5);
    }
    public void testJUNIT() throws Exception {
        doIt("junit");
    }
    public void testLOG4J() throws Exception {
        doIt("log4j");
    }
    public void testMARAUROA() throws Exception {
        doIt("marauroa");
    }
    public void testMAVEN() throws Exception {
        doIt("maven");
    }
    public void testMVNFORUM() throws Exception {
        doIt("mvnforum");
    }
    public void testNAKEDOBJECTS() throws Exception {
        doIt("nakedobjects");
    }
    public void testOPENJMS() throws Exception {
        doIt("openjms");
    }
    public void testPICOCONTAINER() throws Exception {
        doIt("picocontainer");
    }
    public void testPMD() throws Exception {
        doIt("pmd");
    }
    public void testPOI() throws Exception {
        // Requires jaxen. 1.1.4 works, get it from http://jaxen.codehaus.org/releases.html
        doIt("poi");
    }
    public void testPROGUARD() throws Exception {
        doIt("proguard");
    }
    public void testQUARTZ() throws Exception {
        doIt("quartz");
    }
    public void testQUICKSERVER() throws Exception {
        doIt("quickserver", 4);
    }
    public void testQUILT() throws Exception {
        doIt("quilt");
    }
    public void testROLLER() throws Exception {
        doIt("roller");
    }
    public void testSABLECC() throws Exception {
        doIt("sablecc");
    }
    public void testSUNFLOW() throws Exception {
        doIt("sunflow");
    }
    public void testTAPESTRY() throws Exception {
        doIt("tapestry");
    }
    public void testTOMCAT() throws Exception {
        doIt("tomcat");
    }
    public void testTROVE() throws Exception {
        doIt("trove");
    }
    public void testVELOCITY() throws Exception {
        doIt("velocity");
    }
    public void testWEBMAIL() throws Exception {
        doIt("webmail", 4);
    }
    public void testWEKA() throws Exception {
        doIt("weka");
    }
    public void testXALAN() throws Exception {
        doIt("xalan");
    }
    public void testXERCES() throws Exception {
        doIt("xerces");
    }
}
