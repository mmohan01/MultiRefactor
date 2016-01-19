package uk.co.jezuk.mango.unarypredicates;

import uk.co.jezuk.mango.Collections;
import uk.co.jezuk.mango.Predicate;
import java.util.Iterator;
import java.util.List;

public abstract class All<T>  implements Predicate<T> {
    public All(Iterator<Predicate<T>> preds)
     {
        preds_ = Collections.list(preds);
    } // All

    public
     abstract boolean test(T x)
     {
        for (Iterator<Predicate<T>> i = preds_.iterator(); i.hasNext();)
         if (!i.next().test(x))
         return false;
        return true;
    } // boolean

    private List<Predicate<T>> preds_;
} // All
