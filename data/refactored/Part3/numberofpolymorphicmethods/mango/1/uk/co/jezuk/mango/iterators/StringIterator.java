package uk.co.jezuk.mango.iterators;

import java.util.Iterator;

/**
 *
 * @author  jez@jezuk.co.uk
 */
public class StringIterator implements Iterator<String> {
    public StringIterator(String s)
     {
        s_ = s;
        if ((s_ != null) && (s_.length() > 0))
            pos_ = 0; // StringIterator
    }

    public boolean hasNext()
     {
        return (pos_ != -1);
    } // hasNext

    public String next()
     {
        String c = s_.substring(pos_, pos_ + 1);
        if (++pos_ == s_.length())
            pos_ = -1;
        return c;
    } // next

    public void remove()
     {
        throw new UnsupportedOperationException("uk.co.jezuk.mango.StringIterator does not support the remove method.  In fact it's probably a logic error that you called it at all.  Strings are immutable");
    } // remove

    private String s_;
    private int pos_ = -1;
} // StringIterator
