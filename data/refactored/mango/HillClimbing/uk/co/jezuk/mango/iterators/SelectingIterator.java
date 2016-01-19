package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import uk.co.jezuk.mango.Predicate;

/**
 * A <code>SelectingIterator</code> enumerates only those elements of a collection
 * that match the supplied <code>Predicate</code>.
 * 
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public abstract class SelectingIterator<T>  implements Iterator<T> {
    public SelectingIterator( Iterator<T> iterator, Predicate<? super  T> predicate)
     {
        iter_ = iterator;
        pred_ = predicate;
    } // SelectingIterator

    public
     abstract boolean hasNext()
     {
        valid_ = false;
        while (iter_.hasNext() && valid_ == false)
         {
            T candidate = iter_.next();
            if (pred_.test(candidate))
             {
                next_ = candidate;
                valid_ = true;
            } // while
        }
        return valid_;
    } // hasNext

    public
     abstract T next()
     {
        if (!valid_)
         throw new NoSuchElementException();
        return next_;
    } // next

    public
     abstract void remove()
     {
        iter_.remove();
    } // remove

  ////////////////////////
    private Iterator<T> iter_;
    private Predicate<? super  T> pred_;
    private boolean valid_;
    private T next_;
} // SelectingIterator
