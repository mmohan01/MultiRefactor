package uk.co.jezuk.mango.generators;

import uk.co.jezuk.mango.Generator;

/**
 * Returns null for each call to fn()
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public enum NullSequence implements Generator<Object>
{
  INSTANCE;

  public Object fn() { return null; }
} // class GeneratorSequence
