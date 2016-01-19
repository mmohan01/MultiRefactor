package edu.atilim.acma.util;

import java.util.Collection;

public final class IterableHelper {
	public static <T> void addAll(Iterable<T> iterable, Collection<T> collection) {
		for (T item : iterable)
			collection.add(item);		
	}
}
