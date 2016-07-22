package uk.co.jezuk.mango;

import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;

import uk.co.jezuk.mango.collections.*;

/** 
 * <strong>The Mango Library Collection Utilities</strong>
 *
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class Collections {
    static public interface MapBuilder<K, V>  extends Map<K, V> {
        public MapBuilder<K, V> map(K key, V v);
    } // interface MapBuilder

  /**
   * Method to directly initialise lists <br/>
   * <code>List&lt;String&gt; list = Collections.list("fish", "prod", "nose");<br/>
   * is equivalent to <br/>
   * <code>List&lt;String&gt; list = new ArrayList&lt;String&gt;();<br/>
   * list.add("fish");<br/>
   * list.add("prod");<br/>
   * list.add("nose");</code>
   */
    static public <T> List<T> list(final T ... values)
     {
        return ListFactory.list(values);
    } // list

    static public <T> List<T> list(final Iterator<T> values)
     {
        return ListFactory.list(values);
    } // list

    static public <T> List<T> list(final Collection<T> values)
     {
        return list(values.iterator());
    } // list

    private Collections() {}
} // Collections
