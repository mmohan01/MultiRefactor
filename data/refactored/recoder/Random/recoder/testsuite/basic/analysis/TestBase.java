/**
 * Created on 18 feb 2009
 */
package recoder.testsuite.basic.analysis;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.DefaultProjectFileIO;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.service.DefaultErrorHandler;
import recoder.service.SemanticsChecker;

/**
 * @author Tobias Gutzmann
 *
 */
public class TestBase extends TestCase { CrossReferenceServiceConfiguration config = new CrossReferenceServiceConfiguration();
    {
        config.getProjectSettings().setErrorHandler(new DefaultErrorHandler(0));
    }

    private File projectFile = new File("test/basic/collections.prj");
    protected File getProjectFile() {
        return projectFile;
    }

    @Override
     public void setUp() throws IOException, ParserException {
        SourceFileRepository sfr = config.getSourceFileRepository();
        sfr.getCompilationUnitsFromFiles(
                new DefaultProjectFileIO(config, getProjectFile()).load());
        if (!config.getProjectSettings().ensureSystemClassesAreInPath()) {
            throw new RuntimeException("Problem with system setup: Cannot locate system classes");
        }
        config.getChangeHistory().updateModel();
        for (CompilationUnit cu: config.getSourceFileRepository().getCompilationUnits())
            cu.validateAll();
        new SemanticsChecker(config).checkAllCompilationUnits();
    }

    @Override
     public void tearDown() {
        config = null;
        projectFile = null;
    }
}
