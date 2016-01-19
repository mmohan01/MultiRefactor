package uk.co.jezuk.mango.generators;

import uk.co.jezuk.mango.Generator;

/**
 * Returns seed, seed, ... on successive calls to fn()
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class ConstantSequence<R> implements Generator<R>
{
  private final R seed_;

  public ConstantSequence(final R seed)
  {
    seed_ = seed;
  } // ConstantSequence    

  public R fn() { return seed_; }
} // class ConstantSequence