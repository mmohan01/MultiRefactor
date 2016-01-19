// This file is part of the RECODER library and protected by the LGPL.

package recoder.java;

/** Any non-SingleLineComment is a multi line comment. */

public class SingleLineComment extends Comment {

    /**
	 * serialization id
	 */
	private static final long serialVersionUID = -1462907890949586507L;

	/**
     * Single line comment.
     */

    public SingleLineComment() {
        // nothing to do here
    }

    /**
     * Single line comment.
     * 
     * @param text
     *            a string.
     */

    public SingleLineComment(String text) {
        setText(text);
    }

    /**
     * Single line comment.
     * 
     * @param proto
     *            a single line comment.
     */

    private SingleLineComment(SingleLineComment proto) {
        super(proto);
    }

    @Override
    public void setText(String text) {
    	if (!text.startsWith("//"))
        	throw new IllegalArgumentException();
        text = text.trim();
        super.setText(text);
    }
    

    
    /**
     * Deep clone.
     * 
     * @return the object.
     */

    public SingleLineComment deepClone() {
        return new SingleLineComment(this);
    }

    public void accept(SourceVisitor v) {
        v.visitSingleLineComment(this);
    }

}