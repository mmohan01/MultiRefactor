package uk.co.jezuk.mango.generators;

import uk.co.jezuk.mango.Generator;

/**
 * Returns seed, seed+1, seed+2 ... on successive calls to fn()
 * Sequence begins at 0 if no seed value is given.
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class IntegerSequence implements Generator<Integer>
{
  public IntegerSequence()
  {
    seed_ = 0;
  } // IntegerSequence

  public IntegerSequence(int seed)
  {
    seed_ = seed;
  } // IntegerSequence

  public IntegerSequence(Integer seed)
  {
    seed_ = seed.intValue();
  } // IntegerSequence

  public Integer fn()
  {
    return Integer.valueOf(seed_++);
  } // fn

  private int seed_;
} // Generator

