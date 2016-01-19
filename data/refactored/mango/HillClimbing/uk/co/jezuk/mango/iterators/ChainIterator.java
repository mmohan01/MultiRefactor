package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public abstract class ChainIterator<T>  implements Iterator<T> {
    private Iterator<Iterator<T>> chain_;
    private Iterator<T> current_;

    public ChainIterator( Object ... iterables)
     { List<Iterator<T>> list = Helpers.toIterators(iterables);
        chain_ = list.iterator();
        current_ = chain_.hasNext() ? chain_.next() : null;
    } // ChainIterator

    public
     abstract boolean hasNext()
     {
        if (current_ == null)
         return false;

        if (current_.hasNext())
         return true;

        current_ = chain_.hasNext() ? chain_.next() : null;
        return hasNext();
    } // hasNext

    public
     abstract T next()
     {
        return current_.next();
    } // next

    public
     abstract void remove()
     {
        throw new UnsupportedOperationException("uk.co.jezuk.mango.ChainIterator does not support the remove method");
    } // remove
} // ChainIterator
