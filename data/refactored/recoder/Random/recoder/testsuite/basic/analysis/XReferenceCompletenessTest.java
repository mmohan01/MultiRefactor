package recoder.testsuite.basic.analysis;

import java.io.IOException;

import recoder.ParserException;
import recoder.abstraction.ProgramModelElement;
import recoder.convenience.Format;
import recoder.java.Reference;

public abstract class XReferenceCompletenessTest extends TestBase {
    protected String makeResolutionError(Reference r, ProgramModelElement x, ProgramModelElement y) {
        return Format.toString("%c %N @%p in %f", r) + " was found in reference list " + Format.toString("%c %N", x) + " but is resolved to " + Format.toString("%N", y);
    }
}
