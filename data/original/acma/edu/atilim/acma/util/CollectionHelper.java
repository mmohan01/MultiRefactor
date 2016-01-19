package edu.atilim.acma.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import edu.atilim.acma.util.Delegate.Func1P;

public final class CollectionHelper {
	private static Random random = new Random();
	
	public static <T> void addAll(Iterable<T> iterable, Collection<T> collection) {
		for (T item : iterable)
			collection.add(item);		
	}
	
	public static <T, U> List<U> map(List<T> from,  Func1P<U, T> selector) {
		ArrayList<U> slist = new ArrayList<U>(from.size());
		for (T t : from) {
			U item = selector.run(t);
			if (item != null)
				slist.add(selector.run(t));
		}
		return slist;
	}
	
	public static <T> T getRandom(Collection<T> collection) {
		List<T> list = new ArrayList<T>(collection);
		if (list.size() == 0) return null;
		return list.get(random.nextInt(list.size()));
	}
}
