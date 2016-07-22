package uk.co.jezuk.mango.iterators;

import java.util.Iterator;

import uk.co.jezuk.mango.Generator;

public class GeneratorIterator<T>  implements Iterator<T> {
    private final Generator<? extends  T> generator_;

    public GeneratorIterator( Generator<? extends  T> generator)
     {
        generator_ = generator;
    } // GeneratorIterator

    public T next()
     {
        return generator_.fn();
    } // next

    public boolean hasNext()
     {
        return true;
    } // hasNext

    public void remove()
     {
        throw new UnsupportedOperationException("uk.co.jezuk.mango.GeneratorIterator does not support the remove method");
    } // remove
} // class GeneratorIterator
