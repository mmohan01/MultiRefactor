package edu.atilim.acma.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bi-directional map implementation
 * @author Antimon
 *
 */
public class BiMap<K, V> implements Map<K, V> {
	private static final long serialVersionUID = 1L;
	
	private HashMap<K, V> map;
	private HashMap<V, K> invMap;

	public BiMap() {
		map = new HashMap<K, V>();
		invMap = new HashMap<V, K>();
	}

	@Override
	public void clear() {
		map.clear();
		invMap.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return map.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return invMap.containsKey(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public V get(Object arg0) {
		return map.get(arg0);
	}
	
	public K getKey(Object value) {
		return invMap.get(value);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public V put(K arg0, V arg1) {
		if (map.containsKey(arg0)) {
			invMap.remove(invMap.get(arg0));
			map.remove(arg0);
		}
		
		if (invMap.containsKey(arg1)) {
			map.remove(invMap.get(arg1));
			invMap.remove(arg1);
		}
		
		invMap.put(arg1, arg0);
		return map.put(arg0, arg1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		for (Object k : arg0.keySet()) {
			put((K)k, arg0.get(k));
		}
	}

	@Override
	public V remove(Object arg0) {
		if (arg0 == null) return null;
		invMap.remove(map.get(arg0));
		return map.remove(arg0);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}
}
