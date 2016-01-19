package edu.atilim.acma.util;

import java.util.ArrayList;

public class RouletteWheel<T> {
	private ArrayList<Piece> wheel;
	private double total = 0;
	
	public RouletteWheel() {
		wheel = new ArrayList<RouletteWheel<T>.Piece>();
	}
	
	public void add(T item, double fitness) {
		wheel.add(new Piece(item, fitness));
		total += fitness;
	}
	
	public T roll() {
		double slice = ACMAUtil.RANDOM.nextDouble() * total;
		
		Piece selected = null;
		double cumulative = 0;
		for (Piece p : wheel) {
			cumulative += p.fitness;
			
			if (cumulative >= slice) {
				selected = p;
				break;
			}
		}
		
		if (selected != null) {
			wheel.remove(selected);
			total -= selected.fitness;
		}
		
		if (selected == null) {
			double aha = 0;
			for (Piece p : wheel) {
				aha += p.fitness;
			}
			System.out.println(aha);
			System.out.println();
			
		}
		
		return selected.item;
	}
	
	private class Piece implements Comparable<Piece> {
		private T item;
		private double fitness;
		
		private Piece(T item, double fitness) {
			this.item = item;
			this.fitness = fitness;
		}

		@Override
		public int compareTo(Piece o) {
			return Double.compare(fitness, o.fitness);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			return new Double(fitness).equals(((Piece)obj).fitness);
		}
	}
}
