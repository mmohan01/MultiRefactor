package uk.co.jezuk.mango.collections;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;

import uk.co.jezuk.mango.Collections.MapBuilder;

public abstract class MapFactory<K, V>  {

    abstract public <K, V> MapBuilder<K, V> map(K key, V value)
     {
        Builder<K, V> builder = new Builder<K, V>();
        return builder.map(key, value);
    } // map

    static
     abstract public class Builder<K, V>  implements MapBuilder<K, V> {
        private Map<K, V> backing_;

        private Builder()
         {
            backing_ = new HashMap<K, V>();
        } // Builder

        public
         abstract MapBuilder<K, V> map(K key, V value)
         {
            backing_.put(key, value);
            return this;
        } // map

        public
         abstract void clear() { backing_.clear(); }
        public abstract boolean containsKey(Object key) { return backing_.containsKey(key); }
        public abstract boolean containsValue(Object value) { return backing_.containsKey(value); }
        public abstract Set<Map.Entry<K, V>> entrySet() { return backing_.entrySet(); }
        public abstract boolean equals(Object o) { return backing_.equals(o); }
        public abstract V get(Object key) { return backing_.get(key); }
        public abstract int hashCode() { return backing_.hashCode(); }
        public abstract boolean isEmpty() { return backing_.isEmpty(); }
        public abstract Set<K> keySet() { return backing_.keySet(); }
        public abstract V put(K key, V value) { return backing_.put(key, value); }
        public abstract void putAll(Map<? extends  K,? extends  V> m) { backing_.putAll(m); }
        public abstract V remove(Object key) { return backing_.remove(key); }
        public abstract int size() { return backing_.size(); }
        public abstract Collection<V> values() { return backing_.values(); }
    } // class Builder
} // class MapBuilder
