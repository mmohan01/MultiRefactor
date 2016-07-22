package uk.co.jezuk.mango.unarypredicates;

import uk.co.jezuk.mango.Collections;
import uk.co.jezuk.mango.Predicate;
import java.util.Iterator;
import java.util.List;

public class Any<T>  implements Predicate<T> {
    public Any(Iterator<Predicate<T>> preds)
     {
        preds_ = Collections.list(preds);
    } // Any

    public
     final boolean test(T x)
     {
        for (Iterator<Predicate<T>> i = preds_.iterator(); i.hasNext();)
         if (i.next().test(x))
         return true;
        return false;
    } // boolean

    private final List<Predicate<T>> preds_;
} // Any
