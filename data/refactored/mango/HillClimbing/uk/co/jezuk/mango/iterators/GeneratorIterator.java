package uk.co.jezuk.mango.iterators;

import java.util.Iterator;

import uk.co.jezuk.mango.Generator;

public abstract class GeneratorIterator<T>  implements Iterator<T> {
    private Generator<? extends  T> generator_;

    public GeneratorIterator( Generator<? extends  T> generator)
     {
        generator_ = generator;
    } // GeneratorIterator

    public
     abstract T next()
     {
        return generator_.fn();
    } // next

    public
     abstract boolean hasNext()
     {
        return true;
    } // hasNext

    public
     abstract void remove()
     {
        throw new UnsupportedOperationException("uk.co.jezuk.mango.GeneratorIterator does not support the remove method");
    } // remove
} // class GeneratorIterator
