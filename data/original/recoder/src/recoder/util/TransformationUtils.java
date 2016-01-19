/**
 * 
 */
package recoder.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import recoder.convenience.ForestWalker;
import recoder.java.CompilationUnit;
import recoder.java.JavaProgramFactory.TraceItem;

/**
 * @author Tobias Gutzmann
 * @since 0.90
 *
 */
public class TransformationUtils {
	public static void checkAllProgramElementsInLists(List<CompilationUnit> cus, Collection<TraceItem> pes) {
		checkParentLinks(cus);
		if (true) return;
		
		HashSet<TraceItem> allPEs = new HashSet<TraceItem>(pes.size() * 2);
		for (TraceItem ti : pes)
			allPEs.add(ti);
		checkParentLinks(allPEs);
		
		ForestWalker fw = new ForestWalker(cus);
		while (fw.next()) {
			allPEs.remove(fw.getProgramElement());
		}
		System.out.println(allPEs.size() + " ProgramElements cannot be found in CU-list.");
		
		for (TraceItem pe : allPEs) {
			System.out.println(pe);
		}
		if (allPEs.size() > 0)
			System.exit(-1);
	}

	private static void checkParentLinks(List<CompilationUnit> cus) {
		throw new UnsupportedOperationException("Currently not supported (as of 0.93)... ");
//		ForestWalker fw = new ForestWalker(cus);
//		while (fw.next()) {
//			ProgramElement pe = fw.getProgramElement();
//			if (pe instanceof CompilationUnit) {
//				continue;
//			}
//			if (pe.getASTParent() == null) {
//				TraceItem ti = JavaProgramFactory.getInstance().getTraceItem(pe);
//				throw new RuntimeException("Invalid parent link found...\n" + ti);
//			}
//			if (pe.getASTParent().getIndexOfChild(pe) == -1) {
//				TraceItem ti = JavaProgramFactory.getInstance().getTraceItem(pe);
//				throw new RuntimeException("Invalid parent link found...\n" + ti);
//			}
//		}
	}
	
	private static void checkParentLinks(Set<TraceItem> pes) {
		for(TraceItem ti : pes) {
			if (ti.pe.getASTParent() == null)
				throw new RuntimeException("Null Parent link:\n" + ti);
			if (ti.pe.getASTParent().getIndexOfChild(ti.pe) == -1)
				throw new RuntimeException("No valid parent link:\n" + ti);
		}
	}
}
