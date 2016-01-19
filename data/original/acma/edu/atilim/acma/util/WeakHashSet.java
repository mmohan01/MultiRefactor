package edu.atilim.acma.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.WeakHashMap;

public class WeakHashSet<E> extends AbstractSet<E> {
	private WeakHashMap<E, Boolean> map;
	
	public WeakHashSet() {
		map = new WeakHashMap<E, Boolean>();
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}
	
	public boolean add(E e) {
		map.put(e, Boolean.TRUE);
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		return map.remove(o);
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}
}
