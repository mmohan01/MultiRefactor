package uk.co.jezuk.mango.functions;

import uk.co.jezuk.mango.Function;

public class Constant<R>  implements Function<Object, R> {
    private R value_;

    public Constant(R value) { value_ = value; }

    public final R fn(final Object o) { return value_; }
} // Constant
