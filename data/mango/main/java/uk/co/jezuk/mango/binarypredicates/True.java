package uk.co.jezuk.mango.binarypredicates;

import uk.co.jezuk.mango.BinaryPredicate;

public enum True implements BinaryPredicate<Object, Object>
{
  INSTANCE;

  /**
   * @return <code>true</code>, always
   */
  public boolean test(Object x, Object y)
  {
    return true;
  } // test
} // True