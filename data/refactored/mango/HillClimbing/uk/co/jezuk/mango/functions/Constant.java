package uk.co.jezuk.mango.functions;

import uk.co.jezuk.mango.Function;

public abstract class Constant<R>  implements Function<Object, R> {
    private R value_;

    public Constant(R value) { value_ = value; }

    public abstract R fn( Object o) { return value_; }
} // Constant
