package uk.co.jezuk.mango.binarypredicates;

import uk.co.jezuk.mango.BinaryPredicate;

public enum False implements BinaryPredicate<Object, Object> {
    
     INSTANCE;

  /**
   * @return <code>true</code>, always
   */
    public abstract boolean test(Object x, Object y)
     {
        return false;
    } // test
} // False
