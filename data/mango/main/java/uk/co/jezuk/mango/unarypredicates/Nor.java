package uk.co.jezuk.mango.unarypredicates;

import uk.co.jezuk.mango.Predicate;

public class Nor<T> implements Predicate<T>
{
  public Nor(Predicate<T> pred1, 
            Predicate<T> pred2) 
  { 
    p1_ = pred1; 
    p2_ = pred2; 
  } // Nor

  public boolean test(T x) 
  { 
    return !(p1_.test(x) || p2_.test(x)); 
  } // test

  private Predicate<T> p1_;
  private Predicate<T> p2_;
} // Nor
