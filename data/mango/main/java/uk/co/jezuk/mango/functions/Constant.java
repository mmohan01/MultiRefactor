package uk.co.jezuk.mango.functions;

import uk.co.jezuk.mango.Function;

public class Constant<R> implements Function<Object, R>
{
  private final R value_;

  public Constant(R value) { value_ = value; }

  public R fn(final Object o) { return value_; }
} // Constant
