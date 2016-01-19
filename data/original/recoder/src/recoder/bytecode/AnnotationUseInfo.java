/*
 * Created on 27.05.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 */
package recoder.bytecode;

import java.util.List;

import recoder.abstraction.AnnotationUse;
import recoder.abstraction.ElementValuePair;

/**
 * @author gutzmann
 *
 */
public class AnnotationUseInfo implements AnnotationUse {
	protected List<ElementValuePairInfo> elementValuePairs;

	protected String fullAnnotationTypeName;
    /**
     * @param accessFlags
     * @param name
     */
    public AnnotationUseInfo(String fullName, List<ElementValuePairInfo> evpl) {
        super();
        this.elementValuePairs = evpl;
        this.fullAnnotationTypeName = fullName;
    }

	public List<ElementValuePairInfo> getElementValuePairs() {
		return elementValuePairs;
	}
	
	private String getParamStr() {
		if (elementValuePairs.size() == 0)
			return "";
		StringBuilder res = new StringBuilder("(");
		boolean first = true;
		for (ElementValuePair evp : elementValuePairs) {
			if (!first)
				res.append(",");
			first = false;
			res.append(evp.toString());
		}
		res.append(")");
		return res.toString();
	}
	
	public String toString() {
		return "@" + getFullReferencedName() + getParamStr();
	}

    public String getFullReferencedName() {
    	return fullAnnotationTypeName;
    }
    
	public String getReferencedName() {
		return fullAnnotationTypeName.substring(fullAnnotationTypeName.lastIndexOf('.')+1); 
	}
}
