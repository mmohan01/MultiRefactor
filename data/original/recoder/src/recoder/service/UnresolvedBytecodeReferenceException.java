// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import recoder.ModelException;

/**
 * Exception indicating that a particular reference (or reference prefix) found in bytecode could
 * not be resolved.
 * 
 * @author Tobias Gutzmann
 */
public class UnresolvedBytecodeReferenceException extends ModelException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -572997323334751936L;
	private final String message;
	
    public UnresolvedBytecodeReferenceException(String whatsMissing, String where) {
        message = "cannot find " + whatsMissing + " in " + where;
    }

    @Override
    public String toString() {
    	return "UnresolvedByteCodeReferenceException: " + message;
    }
    
    @Override
    public String getMessage() {
    	return message;
    }
}