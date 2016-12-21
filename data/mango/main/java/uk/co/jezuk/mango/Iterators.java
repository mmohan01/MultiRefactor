package uk.co.jezuk.mango;

import java.util.Iterator;
import java.util.List;

import uk.co.jezuk.mango.iterators.*;

/**
 * <strong>The Mango Library Iterators</strong>
 * <br/><br/>
 *
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class Iterators
{
  /**
   * A <code>StringIterator</code> iterators over a String, returning each 
   * character in turn as a <code>String</code> of length 1.
   * e.g. StringIterator("123") will return "1", "2", "3"
   */
  static public Iterator<String> StringIterator(String s) 
  { 
    return new StringIterator(s); 
  } // StringIterator

  /**
   * A <code>NullIterator</code> iterates over nothing.  That is, <code>hasNext</code>
   * always returns <code>false</code>.
   */
  static public <T> Iterator<T> NullIterator() 
  { 
    return (Iterator<T>)NullIterator.INSTANCE;
  } // NullIterator
  
  /**
   * A <code>BoundedIterator</code> enumerates of a subset of a collection, in the
   * range [<code>start</code>, <code>end</code>).  
   * <p>
   * A conventional <code>Iterator</code>, obtained by a call to say 
   * <code>java.util.List.iterator()</code>, travels the entire sequence of the
   * <code>java.util.Collection</code> it points to. It starts at the beginning 
   * and keeps on going until you hit the end or get bored.
   * <p>
   * A BoundedIterator enumerates of a subset of a collection, in the range [start, end) - 
   * a normal <code>Iterator</code> traverses [0, collection.size()). A 
   * <code>BoundedIterator</code> therefore allows you to pick out a sub-set without
   * using <code>list.subList()</code> or equivalent.
   */
  static public <T> Iterator<T> BoundedIterator(Iterator<? extends T> iterator, 
                                                int start, int end) 
  { 
    return new BoundedIterator<T>(iterator, start, end); 
  } // BoundedIterator

  static public <T> Iterator<T> BoundedIterator(List<? extends T> list, 
                                                int start, int end) 
  { 
    return new BoundedIterator<T>(list, start, end); 
  } // BoundedIterator

  /**
   * A <code>SelectingIterator</code> enumerates only those elements of a collection
   * that match the supplied <code>Predicate</code>.
   * <p>
   * It takes a {@link Predicate} which encapsulates some test, and only 
   * returns those Objects in the sequence which pass the test.
   * <p>
   * Say you have a list of <code>String</code>s, myStringList and you're only 
   * interested in those that begin with 'S'. What you need is
   *
   * <pre>
Iterator iter = Iterators.SelectingIterator(myStringList.iterator(), 
                                       new {@link Predicate}() {
                                           boolean test(Object o) {
                                             String s = (String)o;
                                             return s.charAt(0) == 'S';
                                           }
                                       });</pre>
   * <p>
   * A <code>SelectingIterator</code> implements the <code>Iterator</code> interface, 
   * and is constructed by wrapping around an existing iterator. 
   */
  static public <T> Iterator<T> SelectingIterator(Iterator<T> iterator, 
                                                  Predicate<? super T> predicate) 
  { 
    return new SelectingIterator<T>(iterator, predicate); 
  } // SelectingIterator

  /**
   * A <code>SkippingIterator</code> enumerates a sequence,
   * stepping over the elements
   * that match the supplied <code>Predicate</code>.
   * <p>
   * Is it equivalent to <code>SelectingIterator(iter, Not(predicate))</code>
   * 
   * @see #SelectingIterator
   */ 
  static public <T> Iterator<T> SkippingIterator(Iterator<T> iterator, 
                                                 Predicate<? super T> predicate) 
  { 
    return new SkippingIterator<T>(iterator, predicate); 
  } // SkippingIterator

  /** 
   * Iterates over an array of objects.
   * <p>
   * An <code>ArrayIterator</code> puts a <code>Iterator</code> face on an 
   * object array, allowing it be treated as you would a <code>java.util.Collection.</code>
   */
  static public <T> Iterator<T> ArrayIterator(T[] array) 
  { 
    return new ArrayIterator<T>(array); 
  } // ArrayIterator

  /** 
   * Iterates over a single object.
   * <p>
   * Usually an iterator moves over some sequence. A <code>SingletonIterator</code> treats a 
   * single object as it if it were a list containing one object. Since <code>SingletonIterator</code>
   * implements the <code>Iterator</code> interface, it provides a convienent way of
   * passing a single object to an algorithm or other iterator consumer.
   */
  static public <T> Iterator<T> SingletonIterator(T object) 
  { 
    return new SingletonIterator<T>(object); 
  } // SingletonIterator

  /**
   * A <code>TransfromIterator</code> applies a <code>{@link Function}</code> to 
   * each element in the sequence, returning the the function result at each step.
   * <p>
   * Say you have a list of some complex type, and you want to find on by name.
   * You could (caution! trivial example follows)
   * <pre>
     Iterator i = list.iterator();
     while(i.hasNext()) {
       MyComplexObject mco = (MyComplexObject)i.next();
       if(mco.GetName().equals(theSearchName)) 
         .. do something
     }
     // did I find it or not - do the right thing here
     </pre>
   * or you could
   * <pre>
     MyComplexObject mco = (MyComplexObject)Algorithms.find(
                                Iterators.TransformIterator(list.iterator(), 
                                                        Adapt.ArgumentMethod("GetName"),
                                theSearchName);
     if(mco != null) 
       ... found!
     else 
       ...
     </pre> 
   */ 
  static public <T, R> Iterator<R> TransformIterator(Iterator<T> iterator, 
                                                     Function<? super T, R> transform) 
  { 
    return new TransformIterator<T, R>(iterator, transform); 
  } // TransformIterator

  /**
   * A <code>ReverseIterator</code> traverses a list from the end to the beginning, rather than the conventional
   * beginning to end traversal your normal every day iterator performs.
   */
  static public <T> Iterator<T> ReverseIterator(List<? extends T> list) 
  { 
    return new ReverseIterator<T>(list); 
  } // ReverseIterator

  /**
   * <code>ChainIterator</code> creates an iterator which will traverse 
   * each of the iterables in turn.  When one is exhausted it moves to the 
   * next, and so on, until all are exhausted.
   * The <code>ChainIterator</code> can operate over other iterators, lists,
   * arrays, individual objects, or any combination thereof.
   */
  static public <T> Iterator<T> ChainIterator(final Object... iterables)
  {
    return new ChainIterator<T>(iterables);
  } // ChainIterator

  static public <T> Iterator<List<T>> ZipIterator(final Object... iterables)
  {
    return new ZipIterator<T>(iterables);
  } // ZipIterator

  /**
   * <code>TeeIterator</code> creates any number of copies of an iterator,
   * each of which can be iterated seperately.  The source iterator does not 
   * have to modified at all, nor does it have to be cloneable.  The 
   * TeeIterator will buffer as necessary. 
   */
  static public <T> List<Iterator<T>> TeeIterator(final Iterator<T> iterator, final int count)
  {
    return TeeIterator.wrap(iterator, count);
  } // TeeIterator

  /**
   * <code>GeneratorIterator</code> puts an iterator face onto a
   * <code>Generator</code> object.
   */
  static public <T> Iterator<T> GeneratorIterator(final Generator<? extends T> generator)
  {
    return new GeneratorIterator<T>(generator);
  } // GeneratorIterator

  //////////////////////////////////
  private Iterators() { }
} // Iterator
