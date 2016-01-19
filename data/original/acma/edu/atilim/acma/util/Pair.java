package edu.atilim.acma.util;

public class Pair<T, V> {
	private T first;
	private V second;
	
	public static <T, V> Pair<T, V> create(T first, V second) {
		return new Pair<T, V>(first, second);
	}
	
	public T getFirst() {
		return first;
	}
	
	public void setFirst(T first) {
		this.first = first;
	}
	
	public V getSecond() {
		return second;
	}
	
	public void setSecond(V second) {
		this.second = second;
	}

	public Pair(T first, V second) {
		this.first = first;
		this.second = second;
	}
}
