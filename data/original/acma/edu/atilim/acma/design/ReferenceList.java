package edu.atilim.acma.design;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class ReferenceList extends ArrayList<Reference> {
	private static final long serialVersionUID = 1L;
	
	public List<Reference> getByTag(int tag) {
		LinkedList<Reference> selected = new LinkedList<Reference>();
		for (Reference ref : this) {
			if (tag < 0 || ref.getTag() == tag)
				selected.add(ref);
		}
		return Collections.unmodifiableList(selected);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Node> List<T> getSourcesByTag(int tag, Class<T> cls) {
		LinkedList<T> selected = new LinkedList<T>();
		for (Reference ref : this) {
			if ((tag < 0 || ref.getTag() == tag) && cls.isInstance(ref.getSource()))
				selected.add((T)ref.getSource());
		}
		return Collections.unmodifiableList(selected);
	}
}
