/**
 * Created on 14 maj 2009
 */
package recoder.kit.transformation.java5to4;

import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.java.CompilationUnit;
import recoder.java.declaration.TypeDeclaration;
import recoder.kit.ProblemReport;
import recoder.kit.Transformation;
import recoder.kit.transformation.java5to4.methodRepl.ApplyRetrotranslatorLibs;
import recoder.kit.transformation.java5to4.methodRepl.ReplaceOthers;

/**
 * TODO needs enhancements, like proper error reporting. Currently, this
 * transformation simply performs one transformation after the other.
 * @author Tobias Gutzmann
 *
 */
public class TransformAll extends Transformation {
	/**
	 * @param sc
	 */
	public TransformAll(CrossReferenceServiceConfiguration sc) {
		super(sc);
	}

	@Override
	public ProblemReport execute() {
		CrossReferenceServiceConfiguration crsc = getServiceConfiguration();
		List<CompilationUnit> cul = crsc.getSourceFileRepository().getCompilationUnits();

		System.out.println("Conditionals");
    	MakeConditionalCompatible mcc = new MakeConditionalCompatible(crsc, cul);
    	mcc.execute();
    	
    	System.out.println("Enhanced For");
    	EnhancedFor2For eff = new EnhancedFor2For(crsc, cul);
    	eff.execute();
    	
    	System.out.println("Generics");
    	ResolveGenerics rg = new ResolveGenerics(crsc, cul);
    	rg.execute();
    	
    	System.out.println("Covariant Return Types");
    	RemoveCoVariantReturnTypes rc = new RemoveCoVariantReturnTypes(crsc, cul);
    	rc.execute();
    	
    	System.out.println("Annotations");
    	RemoveAnnotations ra = new RemoveAnnotations(crsc, cul);
    	ra.execute();
    	
    	System.out.println("Static Imports");
    	RemoveStaticImports rsi = new RemoveStaticImports(crsc, cul);
    	rsi.execute();
    	
    	System.out.println("Varargs");
    	ResolveVarArgs rva = new ResolveVarArgs(crsc, cul);
    	rva.execute();
    	
    	System.out.println("Boxing");
    	ResolveBoxing rb = new ResolveBoxing(crsc, cul);
    	rb.execute();
    	
    	System.out.println("Boxing 2 (hot fix for a rare bug)");
    	ResolveBoxing rb2 = new ResolveBoxing(crsc, cul);
    	rb2.execute();
    	
    	System.out.println("Enumerations");
    	ReplaceEnums re = new ReplaceEnums(crsc);
    	re.execute();
    	
    	System.out.println("Hexadecimal floating points");
    	new FloatingPoints(crsc, cul).execute();

    	if (crsc.getNameInfo().getClassType("java.util.Collections") instanceof TypeDeclaration) {
    		System.out.println("Skipping remaining transformations (API replacements). " +
    				"Transformed sources seem to be part of the JDK.");
    	} else {
    		System.out.println("RetroLibs");
    		ApplyRetrotranslatorLibs arl = new ApplyRetrotranslatorLibs(crsc, "c:/workspace/recoder/lib");
    		arl.execute();

    		System.out.println("Others...");
    		ReplaceOthers ro = new ReplaceOthers(crsc);
    		ro.execute();
    	}
    	
		return setProblemReport(NO_PROBLEM);
	}
	
}
