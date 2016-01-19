// This file is part of the RECODER library and protected by the LGPL

package recoder.util;

/**
 * @author RN
 */
public abstract class OptionException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String opt;

    protected OptionException(String opt) {
        this.opt = opt;
    }

    public abstract String toString();

}