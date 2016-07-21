package uk.co.jezuk.mango.functions;

import uk.co.jezuk.mango.Function;

public enum Identity implements Function<Object, Object> {
     INSTANCE;

    public Object fn(final Object o) { return o; }
} // Identity
