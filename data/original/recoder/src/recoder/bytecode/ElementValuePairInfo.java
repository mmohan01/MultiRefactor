/*
 * Created on 10.06.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.bytecode;

import org.objectweb.asm.Type;

import recoder.abstraction.ElementValuePair;

/**
 * 
 * @author Tobias Gutzmann
 *
 */
public class ElementValuePairInfo implements ElementValuePair {
	private String elementName;
	private Object value;

	public ElementValuePairInfo(String elementName, Object value) {
		this.elementName = elementName;
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}

	public String getElementName() {
		return elementName;
	}
	
	public String toString() {
		String s;
		if (value instanceof Object[]) {
			Object[] arr = (Object[])value;
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			for (int i=0; i<arr.length; i++) {
				if (i != 0)
					sb.append(",");
				sb.append(getString(arr[i]));
			}
			sb.append("}");
			s = sb.toString();
		} else {
			s = getString(value);
		}
		return getElementName() + "=" + s;
	}
	private String getString(Object o) {
		if (o instanceof String) {
			return "\"" + o.toString() + "\"";
		}
		if (o instanceof Type) {
			return ((Type)o).getClassName() + ".class";
		}
		return o.toString();
	}
}
