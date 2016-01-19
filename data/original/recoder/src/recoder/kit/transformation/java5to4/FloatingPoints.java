/**
 * 
 */
package recoder.kit.transformation.java5to4;

import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.ForestWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.expression.literal.DoubleLiteral;
import recoder.java.expression.literal.FloatLiteral;
import recoder.kit.ProblemReport;
import recoder.kit.Transformation;

/**
 * @author Tobias
 *
 */
public class FloatingPoints extends Transformation {

	private final List<CompilationUnit> cus;
	/**
	 * @param sc
	 */
	public FloatingPoints(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cus) {
		super(sc);
		this.cus = cus;
	}

	@Override
	public ProblemReport execute() {
		ForestWalker fw = new ForestWalker(cus);
		while (fw.next()) {
			ProgramElement pe = fw.getProgramElement();
			if (pe instanceof FloatLiteral) {
				FloatLiteral fl = (FloatLiteral)pe;
				if (fl.getValue().toLowerCase().startsWith("0x")) {
					replace(fl,
						getProgramFactory().createFloatLiteral(
							Float.valueOf(fl.getValue()))	
						);
				}
			} else if (pe instanceof DoubleLiteral) {
				DoubleLiteral dl = (DoubleLiteral)pe;
				if (dl.getValue().toLowerCase().startsWith("0x")) {
					replace(dl,
						getProgramFactory().createDoubleLiteral(
							Double.valueOf(dl.getValue()))	
						);
				}
			}
		}
		
		return setProblemReport(NO_PROBLEM);
	}
}
