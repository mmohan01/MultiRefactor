package uk.co.jezuk.mango;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;

import uk.co.jezuk.mango.algorithms.*;

/**
 * <strong>The Mango Library Algorithms</strong>
 * <br/><br/>
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class Algorithms
{
  /**
   * Algorithm intersection finds the common elements in both collections
   * See http://en.wikipedia.org/wiki/Intersection_(set_theory)
   */
  static public <T> List<T> intersection(Collection<? extends T> coll1, 
					 Collection<? extends T> coll2)
    { return Intersection.execute(coll1.iterator(), coll2, new ArrayList<T>()); }
  static public <T, C extends Collection<? super T>>
			      C intersection(Collection<? extends T> coll1, 
					     Collection<? extends T> coll2, 
					     C results) 
  { return Intersection.execute(coll1.iterator(), coll2, results); }
  static public <T> List<T> intersection(Iterator<? extends T> iter1, 
					 Collection<? extends T> coll2)
    { return Intersection.execute(iter1, coll2, new ArrayList<T>()); }
  static public <T, C extends Collection<? super T>> 
			      C intersection(Iterator<? extends T> iter1, 
					     Collection<? extends T> coll2, 
					     C results) 
  { return Intersection.execute(iter1, coll2, results); }
  static public <T> List<T> intersection(Iterator<? extends T> iter1, 
					 Iterator<? extends T> iter2)
  { return Intersection.execute(iter1, iter2, new ArrayList<T>()); }
  static public <T, C extends Collection<? super T>>
			      C intersection(Iterator<? extends T> iter1, 
					     Iterator<? extends T> iter2, 
					     C results)
  { return Intersection.execute(iter1, iter2, results); }

  /**
   * Algorithm symmetricDifference returns the elements that are on coll1 
   * and not in coll2, and those elements in coll2 that are not in coll1.
   * See http://en.wikipedia.org/wiki/Symmetric_difference
   */
  static public <T> List<T> symmetricDifference(Collection<? extends T> coll1, 
                                                Collection<? extends T> coll2)
  { return SymmetricDifference.execute(coll1.iterator(), coll2, new ArrayList<T>()); }
  static public <T, C extends Collection<? super T>> C symmetricDifference(Collection<? extends T> coll1, 
                                                                           Collection<? extends T> coll2, 
                                                                           C results) 
  { return SymmetricDifference.execute(coll1.iterator(), coll2, results); }
  static public <T> List<T> symmetricDifference(Iterator<? extends T> iter1, 
                                                Collection<? extends T> coll2) 
  { return SymmetricDifference.execute(iter1, coll2, new ArrayList<T>()); }
  static public <T, C extends Collection<? super T>> C  symmetricDifference(Iterator<? extends T> iter1, 
                                                                            Collection<? extends T> coll2, 
                                                                            C results) 
  { return SymmetricDifference.execute(iter1, coll2, results); }
  static public <T> List<T> symmetricDifference(Iterator<? extends T> iter1, 
                                                Iterator<? extends T> iter2)
  { return SymmetricDifference.execute(iter1, iter2, new ArrayList<T>()); }


  /**
   * The algorithm ForEach applies the function <code>fn</code> to
   * each element in the <code>iterator</code> sequence. 
   */
  static public <T, R> void forEach(Collection<T> collection, 
                                    Function<? super T, R> fn) 
  { ForEach.execute(collection.iterator(), fn); }
  static public <T, R> void forEach(Collection<T> collection, 
                                    int start, 
                                    int end, 
                                    Function<? super T, R> fn) 
  { ForEach.execute(Iterators.BoundedIterator(collection.iterator(), start, end), fn); }
  static public <T, R> void forEach(List<T> list, 
                                    int start, 
                                    int end, 
                                    Function<? super T, R> fn) 
  { ForEach.execute(Iterators.BoundedIterator(list, start, end), fn); }
  static public <T, R> void forEach(Iterator<T> iterator, 
                                    Function<? super T, R> fn) 
  { ForEach.execute(iterator, fn); } 

  /**
   * The algorithm Transform applies the function <code>fn</code> to
   * each element in the <code>iterator</code> sequence.
   * The return value of <code>fn</code> is added to the <code>results</code>
   * collection.  If the return value of <code>fn</code> is itself a
   * collection, then each member of that collection is added to 
   * <code>results</code>.
   */
  static public <T, R> List<R> transform(Collection<T> collection, 
                                         Function<? super T, R> fn)
  { return Transform.execute(collection.iterator(), fn, new ArrayList<R>()); }
  static public <T, R, C extends Collection<? super R>>
                         C transform(Collection<T> collection, 
                                     Function<? super T, R> fn,
                                     C results) 
  { return Transform.execute(collection.iterator(), fn, results); }
  static public <T, R> List<R> transform(Collection<T> collection,
                                         int start, 
                                         int end,
                                         Function<? super T, R> fn)
  { return Transform.execute(Iterators.BoundedIterator(collection.iterator(), start, end), fn, new ArrayList<R>()); }
  static public <T, R, C extends Collection<? super R>>
                         C transform(Collection<T> collection, 
                                     int start, 
                                     int end, 
                                     Function<? super T, R> fn, 
                                     C results) 
  { return Transform.execute(Iterators.BoundedIterator(collection.iterator(), start, end), fn, results); }
  static public <T, R> List<R> transform(List<T> collection,
                                         int start, 
                                         int end,
                                         Function<? super T, R> fn)
  { return Transform.execute(Iterators.BoundedIterator(collection, start, end), fn, new ArrayList<R>()); }
  static public <T, R, C extends Collection<? super R>>
                         C transform(List<T> list, 
                                     int start, 
                                     int end, 
                                     Function<? super T, R> fn, 
                                     C results) 
  { return Transform.execute(Iterators.BoundedIterator(list, start, end), fn, results); }
  static public <T, R> List<R> transform(Iterator<T> iterator,
                                         Function<? super T, R> fn)
  { return Transform.execute(iterator, fn, new ArrayList<R>()); } 
  static public <T, R, C extends Collection<? super R>>
                         C transform(Iterator<T> iterator, 
                                     Function<? super T, R> fn, 
                                     C results) 
  { return Transform.execute(iterator, fn, results); } 
  
  /**
   * <code>Count</code> computes the number of elements in the sequence that 
   * are equal to <code>value</code>.  <br>
   * <code>value</code> may be <code>null</code>.<br>
   * The objects in the sequence and <code>value</code> must be comparable using
   * <code>Object.equals</code> (unless <code>value</code> is <code>null</code>).
   */
  static public <T> int count(Collection<? extends T> collection, 
                              T value) 
  { return Count.execute(collection.iterator(), value); }
  static public <T> int count(Collection<? extends T> collection, 
                              int start, 
                              int end, 
                              T value) 
  { return Count.execute(Iterators.BoundedIterator(collection.iterator(), start, end), value); }
  static public <T> int count(List<? extends T> list, 
                              int start, 
                              int end, 
                              T value) 
  { return Count.execute(Iterators.BoundedIterator(list, start, end), value); }
  static public <T> int count(Iterator<? extends T> iterator, 
                              T value) 
  { return Count.execute(iterator, value); } 
  
  /**
   * <code>CountIf</code> is similar to <code>Count</code>, but more general.
   * It computes the number of elements in the sequence which satisfy some condition.  
   * The condition is a described in the user-supplied <code>test</code> object, and 
   * <code>CountIf</code> computes the number of objects such that <code>test.test(o)</code>
   * is <code>true</code>.
   */ 
  static public <T> int countIf(Collection<T> collection, 
                                Predicate<? super T> test) 
  { return CountIf.execute(collection.iterator(), test); }
  static public <T> int countIf(Collection<T> collection, 
                                int start, 
                                int end, 
                                Predicate<? super T> test)
  { return CountIf.execute(Iterators.BoundedIterator(collection.iterator(), start, end), test); }
  static public <T> int countIf(List<T> list, 
                                int start, 
                                int end, 
                                Predicate<? super T> test) 
  { return CountIf.execute(Iterators.BoundedIterator(list, start, end), test); }
  static public <T> int countIf(Iterator<T> iterator, 
                                Predicate<? super T> test) 
  { return CountIf.execute(iterator, test); } 
  
  /**
   * <code>CountIfNot</code> is the complement of <code>CountIf</code>.
   * It counts the number of elements in the sequence which fail some condition.  
   * The condition is a described in the user-supplied <code>test</code> object, and 
   * <code>CountIfNot</code> computes the number of objects such that <code>test.test(o)</code>
   * is <code>false</code>.
   */ 
  static public <T> int countIfNot(Collection<T> collection, 
                                   Predicate<? super T> test) 
  { return CountIfNot.execute(collection.iterator(), test); }
  static public <T> int countIfNot(Collection<T> collection, 
                                   int start, 
                                   int end, 
                                   Predicate<? super T> test) 
  { return CountIfNot.execute(Iterators.BoundedIterator(collection.iterator(), start, end), test); }
  static public <T> int countIfNot(List<T> list, 
                                   int start, 
                                   int end, 
                                   Predicate<? super T> test) 
  { return CountIfNot.execute(Iterators.BoundedIterator(list, start, end), test); }
  static public <T> int countIfNot(Iterator<T> iterator, 
                                   Predicate<? super T> test) 
  { return CountIfNot.execute(iterator, test); } 
  
  /**
   * Searchs the sequence travesed by the Iterator for the given value.
   * Returns the <code>Object</code>, or <code>null</code> if the value
   * is not found.  The iterator will have been advanced to the next object 
   * in the sequence.
   * The objects in the sequence and <code>value</code> must be comparable using
   * <code>Object.equals</code> (unless <code>value</code> is <code>null</code>).
   */
  static public <T> T find(Collection<? extends T> collection, 
                           T value) 
  { return Find.execute(collection.iterator(), value); }
  static public <T> T find(Collection<? extends T> collection, 
                           int start, 
                           int end, 
                           T value) 
  { return Find.execute(Iterators.BoundedIterator(collection.iterator(), start, end), value); }
  static public <T> T find(List<? extends T> list, 
                           int start, 
                           int end, 
                           T value) 
  { return Find.execute(Iterators.BoundedIterator(list, start, end), value); }
  static public <T> T find(Iterator<? extends T> iterator, 
                           T value) 
  { return Find.execute(iterator, value); } 
  
  /**
   * Searchs the sequence travesed by the Iterator for the given value.
   * Returns the index of the value in the collection, or <code>-1</code>
	 * if the value is not found.  The iterator will have been advanced to 
	 * the next object in the sequence.
   * The objects in the sequence and <code>value</code> must be comparable using
   * <code>Object.equals</code> (unless <code>value</code> is <code>null</code>).
   */
  static public <T> int findPosition(Collection<? extends T> collection, 
                                     T value) 
  { return FindPosition.execute(collection.iterator(), value); }
  static public <T> int findPosition(Collection<? extends T> collection, 
                                     int start, 
                                     int end, 
                                     T value) 
  { return FindPosition.execute(Iterators.BoundedIterator(collection.iterator(), start, end), value); }
  static public <T> int findPosition(List<? extends T> list, 
                                     int start, 
                                     int end, 
                                     T value) 
  { return FindPosition.execute(Iterators.BoundedIterator(list, start, end), value); }
  static public <T> int findPosition(Iterator<? extends T> iterator, 
                                     T value) 
  { return FindPosition.execute(iterator, value); } 
  static public <T> int findPositionIf(Collection<T> collection, 
                                       Predicate<? super T> pred) 
  { return FindPositionIf.execute(collection.iterator(), pred); }
  static public <T> int findPositionIf(Collection<T> collection, 
                                       int start, 
                                       int end, 
                                       Predicate<? super T> pred) 
  { return FindPositionIf.execute(Iterators.BoundedIterator(collection.iterator(), start, end), pred); }
  static public <T> int findPositionIf(List<T> list, 
                                       int start, 
                                       int end, 
                                       Predicate<? super T> pred) 
  { return FindPositionIf.execute(Iterators.BoundedIterator(list, start, end), pred); }
  static public <T> int findPositionIf(Iterator<T> iterator, 
                                       Predicate<? super T> pred) 
  { return FindPositionIf.execute(iterator, pred); } 
  
  /**
   * Searchs the sequence traversed by the Iterator and returns the first
   * object encountered for which the Predicate returns <code>true</code>.
   * Returns the <code>Object</code>, or <code>null</code> if the value
   * is not found.  The iterator will have been advanced to the next object 
   * in the sequence.
   */
  static public <T> T findIf(Collection<T> collection, 
                             Predicate<? super T> test) 
  { return FindIf.execute(collection.iterator(), test); }
  static public <T> T findIf(Collection<T> collection, 
                             int start, 
                             int end, 
                             Predicate<? super T> test) 
  { return FindIf.execute(Iterators.BoundedIterator(collection.iterator(), start, end), test); }
  static public <T> T findIf(List<T> list, 
                             int start, 
                             int end, 
                             Predicate<? super T> test) 
  { return FindIf.execute(Iterators.BoundedIterator(list, start, end), test); }
  static public <T> T findIf(Iterator<T> iterator, 
                             Predicate<? super T> test) 
  { return FindIf.execute(iterator, test); } 

  /**
   * Searchs the sequence traversed by the Iterator and returns the first
   * object encountered for which the Predicate returns <code>false</code>.
   * The iterator will have been advanced to the next object 
   * in the sequence.
   */
  static public <T> T findIfNot(Collection<T> collection, Predicate<? super T> test) { return FindIfNot.execute(collection.iterator(), test); }
  static public <T> T findIfNot(Collection<T> collection, int start, int end, Predicate<? super T> test) { return FindIfNot.execute(Iterators.BoundedIterator(collection.iterator(), start, end), test); }
  static public <T> T findIfNot(List<T> list, int start, int end, Predicate<? super T> test) { return FindIfNot.execute(Iterators.BoundedIterator(list, start, end), test); }
  static public <T> T findIfNot(Iterator<T> iterator, Predicate<? super T> test) { return FindIfNot.execute(iterator, test); } 

  /**
   * Removes objects equal to <code>value</code> from the sequence.
   */
  static public <T> void remove(Collection<? extends T> collection, T value) { Remove.execute(collection.iterator(), value); }
  static public <T> void remove(Collection<? extends T> collection, int start, int end, T value) { Remove.execute(Iterators.BoundedIterator(collection.iterator(), start, end), value); }
  static public <T> void remove(List<? extends T> list, int start, int end, T value) { Remove.execute(Iterators.BoundedIterator(list, start, end), value); }
  static public <T> void remove(Iterator<? extends T> iterator, T value) { Remove.execute(iterator, value); } 
  
  /**
   * Removes objects which match <code>test</code> from the sequence.
   */
  static public <T> void removeIf(Collection<T> collection, Predicate<? super T> pred) { RemoveIf.execute(collection.iterator(), pred); }
  static public <T> void removeIf(Collection<T> collection, int start, int end, Predicate<? super T> pred) { RemoveIf.execute(Iterators.BoundedIterator(collection.iterator(), start, end), pred); }
  static public <T> void removeIf(List<T> list, int start, int end, Predicate<? super T> pred) { RemoveIf.execute(Iterators.BoundedIterator(list, start, end), pred); }
  static public <T> void removeIf(Iterator<T> iterator, Predicate<? super T> pred) { RemoveIf.execute(iterator, pred); } 
 
 /**
   * Partitions the supplied collections into two.  Objects matching the Predicate 
	 * are removed from the collection and added to the results Collection.
	 * Returns the result collection.
   */
  static public <T> List<T> partition(Collection<T> collection, 
                                      Predicate<? super T> pred)
  { return Partition.execute(collection.iterator(), pred, new ArrayList<T>()); }
  static public <T, C extends Collection<? super T>>
                      C partition(Collection<T> collection, 
                                  Predicate<? super T> pred, 
                                  C results) 
  { return Partition.execute(collection.iterator(), pred, results); }
  static public <T> List <T> partition(Collection<T> collection, 
                                       int start, 
                                       int end, 
                                       Predicate<? super T> pred)
  { return Partition.execute(Iterators.BoundedIterator(collection.iterator(), start, end), pred, new ArrayList<T>()); }
  static public <T, C extends Collection<? super T>>
                      C partition(Collection<T> collection, 
                                  int start, 
                                  int end, 
                                  Predicate<? super T> pred, 
                                  C results) 
  { return Partition.execute(Iterators.BoundedIterator(collection.iterator(), start, end), pred, results); }
  static public <T> List<T> partition(List<T> list, 
                                      int start, 
                                      int end, 
                                      Predicate<? super T> pred)
  { return Partition.execute(Iterators.BoundedIterator(list, start, end), pred, new ArrayList<T>()); }
  static public <T, C extends Collection<? super T>>
                      C partition(List<T> list, 
                                  int start, 
                                  int end, 
                                  Predicate<? super T> pred, 
                                  C results) 
  { return Partition.execute(Iterators.BoundedIterator(list, start, end), pred, results); }
  static public <T> List<T> partition(Iterator<T> iterator, 
                                      Predicate<? super T> pred)
  { return Partition.execute(iterator, pred, new ArrayList<T>()); } 
  static public <T, C extends Collection<? super T>>
                      C partition(Iterator<T> iterator, 
                                  Predicate<? super T> pred, 
                                  C results) 
  { return Partition.execute(iterator, pred, results); } 

  /**
   * Removes duplicate elements.  Whenever a consecutive groups of duplicate objects
   * occur in the sequence, <code>unique</code> removes all but the first objects
   * in each group.
   * <code>iterator</code> must support the <code>remove</code> method.
   * @see Iterator
   * @see Comparator
   */	
    static public <T> void unique(Collection<T> collection) { Unique.execute(collection.iterator(), null); } 
    static public <T> void unique(Collection<T> collection, int start, int end) { Unique.execute(Iterators.BoundedIterator(collection.iterator(), start, end), null); }
    static public <T> void unique(List<T> list, int start, int end) { Unique.execute(Iterators.BoundedIterator(list, start, end), null); } 
    static public <T> void unique(Iterator<T> iterator) { Unique.execute(iterator, null);	} 
    static public <T> void unique(Collection<T> collection, Comparator<? super T> comparator) { Unique.execute(collection.iterator(), comparator);	} 
    static public <T> void unique(Collection<T> collection, int start, int end, Comparator<? super T> comparator) { Unique.execute(Iterators.BoundedIterator(collection.iterator(), start, end), comparator);	} 
    static public <T> void unique(List<T> list, int start, int end, Comparator<? super T> comparator) { Unique.execute(Iterators.BoundedIterator(list, start, end), comparator); } 
    static public <T> void unique(Iterator<T> iterator, Comparator<? super T> comparator) { Unique.execute(iterator, comparator); } 
  
    private Algorithms() { } // prevent instantiation
} // public class Algorithms
