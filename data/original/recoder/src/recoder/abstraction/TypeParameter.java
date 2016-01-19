/*
 * Created on 25.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.abstraction;

import java.util.List;

/**
 * @author Tobias Gutzmann
 *
 */
public interface TypeParameter extends ClassType {
	public String getParameterName();
	
	/**
	 * Guaranteed to be >= 1. Implementations of 
	 * this class make sure that at least 
	 * java.lang.Object is declared.
	 * @return The number of bounds.
	 */
	public int getBoundCount();
	/**
	 *
	 * @param boundidx
	 * @return the fully qualified name, unless bound refers to a type parameter, in which case it's the simple name of the type parameter.
	 */
	public String getBoundName(int boundidx);
	/**
	 * @return The type arguments to the bound at position <code>i</code>.
     * possibly <code>null</code>.
	 */
	public List<? extends TypeArgument> getBoundTypeArguments(int boundidx);
	
	public static class DescrImp {
		public static String getFullSignature(TypeParameter tp) {			
			String res = tp.getParameterName();
			String del = " extends ";
			for (int i = 0; i < tp.getBoundCount(); i++) {
				res += del;
				res += tp.getBoundName(i);
				List<? extends TypeArgument> tas = tp.getBoundTypeArguments(i);
				if (tas != null && tas.size() > 0) {
					res += "<";
					String delim2 = "";
					for (TypeArgument ta : tas) {
						res += delim2;
						res += ta.getFullSignature();
						delim2 = ",";
					}
					res += ">";
				}
				del = ",";
			}
			return res;
		}
	}
}
