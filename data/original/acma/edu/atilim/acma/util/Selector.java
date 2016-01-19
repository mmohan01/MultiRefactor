package edu.atilim.acma.util;

public interface Selector<Tin, Tout> {
	public Tout select(Tin in);
}
