package uk.co.jezuk.mango.unarypredicates;

import uk.co.jezuk.mango.Predicate;

public enum False implements Predicate<Object>
{
  INSTANCE;

  public boolean test(Object x) { return false; }
} // False
