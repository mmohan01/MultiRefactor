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

    static public <T> List<T> list(final Iterator<T> values)
     {
        return ListFactory.list(values);
    } Collections() {} // list
} // Collections
