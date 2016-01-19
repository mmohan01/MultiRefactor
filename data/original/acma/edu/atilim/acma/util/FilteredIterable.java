package edu.atilim.acma.util;

import java.util.Iterator;

public class FilteredIterable<Tin, Tout> implements Iterable<Tout> {
	
	private Selector<Tin, Tout> selector;
	private Iterable<Tin> innerIterable;
	
	public FilteredIterable(Iterable<Tin> innerIterable, Selector<Tin, Tout> selector) {
		super();
		this.selector = selector;
		this.innerIterable = innerIterable;
	}

	@Override
	public Iterator<Tout> iterator() {
		return new Iter();
	}
	
	private class Iter implements Iterator<Tout> {
		
		private Iterator<Tin> innerIterator;
		private Tout next;
		
		public Iter() {
			innerIterator = innerIterable.iterator();
			next = null;
		}
		
		@Override
		public boolean hasNext() {
			if (next != null) return true;
			while (innerIterator.hasNext()) {
				Tout item = selector.select(innerIterator.next());
				if (item != null) {
					next = item;
					return true;
				}
			}
			return false;
		}

		@Override
		public Tout next() {
			if (!hasNext()) return null;
			
			Tout item = next;
			next = null;
			return item;
		}

		@Override
		public void remove() {
			innerIterator.remove();			
		}
	}
}
