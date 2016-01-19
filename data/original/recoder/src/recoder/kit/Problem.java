// This file is part of the RECODER library and protected by the LGPL.

package recoder.kit;

/**
 * Problem report returned by the analysis phase of a {@link Transformation}
 * indicating that the planned transformation is not applicable. This is dual to
 * the {@link recoder.kit.NoProblem}report. This class is also an exception but
 * is usually not thrown, but passed on as ordinary return value.
 * 
 * @see recoder.kit.NoProblem
 * 
 * @author AL
 */
public abstract class Problem extends RuntimeException implements ProblemReport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}

