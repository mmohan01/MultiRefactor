/**
 * 
 */
package recoder.kit.transformation.java5to4.methodRepl;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.ClassType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.ReferencePrefix;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;

/**
 * Replaces all occurrences to <code>java.util.Collections.emptyList()</code>,
 * <code>java.util.Collections.emptySet()</code>, and
 * <code>java util.Collections.emptyMap()</code> to it's corresponding fields
 * <code>java.util.Collections.EMPTY_LIST</code>,
 * <code>java.util.Collections.EMPTY_SET</code>, and
 * <code>java.util.Collections.EMPTY_MAP</code>.
 * 
 *  The transformation does not check if the replacement is valid,
 *  i.e., if "useless" uses of the methods are performed. Examples for those are:
 *  <ul>
 *  <li>Collections.emptyList();<code></code> 
 *  <li>Collections.&lt;String&gt;emptyList().get(0).toUpperCase;<code></code>
 *  </ul>
 *  A field reference is invalid in the first case, and in the second case,
 *  and exception would always cause an exception. Both cases are useless,
 *  but valid Java code. However, the transformation will fail in the first 
 *  case, and in the second case, the transformed code won't compile. 
 *  But maybe that's a good thing!?
 * @author Tobias Gutzmann
 *
 */
public class ReplaceEmptyCollections extends TwoPassTransformation {
	/**
	 * This constructor collects the required information by querying the
	 * CrossReferenceServiceConfiguration, i.e., all occurrences in
	 * the current project are replaced.
	 * @param sc
	 */
	public ReplaceEmptyCollections(CrossReferenceServiceConfiguration sc) {
		super(sc);
	}
	@Override
	public ProblemReport analyze() {
		ClassType coll = getNameInfo().getClassType("java.util.Collections");
		
		Field list = null; 
		Field map = null;
		Field set = null;
		for (Field f : coll.getFields()) {
			if ("EMPTY_LIST".equals(f.getName()))
				list = f;
			else if ("EMPTY_MAP".equals(f.getName()))
				map = f;
			else if ("EMPTY_SET".equals(f.getName()))
				set = f;
		}
		if (list == null) {
			System.out.println("When using ReplaceEmptyCollections, please " +
					"provide a JDK 1.5 or higher");
			return setProblemReport(IDENTITY);
		}
		assert map != null && set != null;
		
		for (Method m : coll.getMethods()) {
			if ("emptyList".equals(m.getName())) {
				List<MemberReference> refs = getCrossReferenceSourceInfo().getReferences(m);
				for (MemberReference mr : refs) {
					replList.add((MethodReference)mr);
				}
			}
			else if ("emptyMap".equals(m.getName())) {
				List<MemberReference> refs = getCrossReferenceSourceInfo().getReferences(m);
				for (MemberReference mr : refs) {
					replMap.add((MethodReference)mr);
				}
			}
			else if ("emptySet".equals(m.getName())) {
				List<MemberReference> refs = getCrossReferenceSourceInfo().getReferences(m);
				for (MemberReference mr : refs) {
					replSet.add((MethodReference)mr);
				}
			}
		}

		if (replList.size() == 0 && replMap.size() == 0 && replSet.size() == 0)
			return setProblemReport(IDENTITY);
		return setProblemReport(EQUIVALENCE); // hehe
	}
	
	private List<MethodReference> replList = new ArrayList<MethodReference>();
	private List<MethodReference> replMap = new ArrayList<MethodReference>();
	private List<MethodReference> replSet = new ArrayList<MethodReference>();
	@Override
	public void transform() {
		super.transform();
		ProgramFactory f = getProgramFactory();
		for (MethodReference mr : replList) {
			ReferencePrefix prefix = mr.getReferencePrefix(); // keep!!!
			FieldReference fr = f.createFieldReference(f.createIdentifier("EMPTY_LIST"));
			fr.setReferencePrefix(prefix);
			if (prefix != null)
				prefix.setReferenceSuffix(fr); // update parent link
			replace(mr, fr); // informs ChangeHistory
		}
		for (MethodReference mr : replMap) {
			ReferencePrefix prefix = mr.getReferencePrefix(); // keep!!!
			FieldReference fr = f.createFieldReference(f.createIdentifier("EMPTY_MAP"));
			fr.setReferencePrefix(prefix);
			if (prefix != null)
				prefix.setReferenceSuffix(fr); // update parent link
			replace(mr, fr); // informs ChangeHistory
		}
		for (MethodReference mr : replSet) {
			ReferencePrefix prefix = mr.getReferencePrefix(); // keep!!!
			FieldReference fr = f.createFieldReference(f.createIdentifier("EMPTY_SET"));
			fr.setReferencePrefix(prefix);
			if (prefix != null)
				prefix.setReferenceSuffix(fr); // update parent link
			replace(mr, fr); // informs ChangeHistory
		}
		return;
	}

}
