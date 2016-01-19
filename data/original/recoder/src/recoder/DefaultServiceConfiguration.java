// This file is part of the RECODER library and protected by the LGPL.

package recoder;

import recoder.io.ClassFileRepository;
import recoder.io.DefaultClassFileRepository;
import recoder.io.DefaultSourceFileRepository;
import recoder.io.ProjectSettings;
import recoder.io.SourceFileRepository;
import recoder.java.JavaProgramFactory;
import recoder.service.ByteCodeInfo;
import recoder.service.ChangeHistory;
import recoder.service.ConstantEvaluator;
import recoder.service.DefaultByteCodeInfo;
import recoder.service.DefaultConstantEvaluator;
import recoder.service.DefaultImplicitElementInfo;
import recoder.service.DefaultNameInfo;
import recoder.service.DefaultSourceInfo;
import recoder.service.ImplicitElementInfo;
import recoder.service.NameInfo;
import recoder.service.SourceInfo;

public class DefaultServiceConfiguration extends ServiceConfiguration {

    protected ProjectSettings makeProjectSettings() {
        return new ProjectSettings(this);
    }

    protected ChangeHistory makeChangeHistory() {
        return new ChangeHistory(this);
    }

    protected ProgramFactory makeProgramFactory() {
        return new JavaProgramFactory();
    }

    protected SourceFileRepository makeSourceFileRepository() {
        return new DefaultSourceFileRepository(this);
    }

    protected ClassFileRepository makeClassFileRepository() {
        return new DefaultClassFileRepository(this);
    }

    protected SourceInfo makeSourceInfo() {
        return new DefaultSourceInfo(this);
    }

    protected ByteCodeInfo makeByteCodeInfo() {
        return new DefaultByteCodeInfo(this);
    }

    protected ImplicitElementInfo makeImplicitElementInfo() {
        return new DefaultImplicitElementInfo(this);
    }

    protected NameInfo makeNameInfo() {
        return new DefaultNameInfo(this);
    }

    protected ConstantEvaluator makeConstantEvaluator() {
        return new DefaultConstantEvaluator(this);
    }
}

