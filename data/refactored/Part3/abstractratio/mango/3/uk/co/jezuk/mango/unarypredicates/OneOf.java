package uk.co.jezuk.mango.unarypredicates;

import uk.co.jezuk.mango.Collections;
import uk.co.jezuk.mango.Predicate;
import java.util.Iterator;
import java.util.List;

public class OneOf<T>  implements Predicate<T> {
    public OneOf(Iterator<Predicate<T>> preds)
     {
        preds_ = Collections.list(preds);
    } // OneOf

    public
     final boolean test(T x)
     {
        int t = 0;
        for (Iterator<Predicate<T>> i = preds_.iterator(); i.hasNext() && t <= 2;)
         if (i.next().test(x))
         ++t; ;
        return (t == 1);
    } // boolean

    private final List<Predicate<T>> preds_;
} // OneOf
