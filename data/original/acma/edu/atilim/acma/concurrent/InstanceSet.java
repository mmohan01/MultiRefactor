package edu.atilim.acma.concurrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class InstanceSet implements Iterable<Instance> {
	private Set<Instance> instances;
		
	public InstanceSet() {
		instances = Collections.synchronizedSet(new HashSet<Instance>());
	}
	
	public void add(Instance instance) {
		instances.add(instance);
	}
	
	public void remove(Instance instance) {
		instances.remove(instance);
	}
	
	public void broadcast(Serializable message) {
		for (Instance i : instances)
			i.send(message);
	}
	
	public <T extends Serializable> void scatter(ArrayList<T> elements) {
		int insindex = 0;
		int numinstances = instances.size();
		for (Instance i : instances) {
			ArrayList<T> ielems = new ArrayList<T>(elements.size());
			
			for (int ei = insindex; ei < elements.size(); ei += numinstances) {
				ielems.add(elements.get(ei));
			}
			
			i.send(ielems);
			
			insindex++;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Serializable> ArrayList<T> gather(Class<T> cls) {
		ArrayList<T> elements = new ArrayList<T>();
		for (Instance i : instances) {
			List<T> ielems = i.receive(elements.getClass());
			elements.addAll(ielems);
		}
		return elements;
	}
	
	@Override
	public Iterator<Instance> iterator() {
		return instances.iterator();
	}

	public int size() {
		return instances.size();
	}
	
	public void dispose() {
		for (Instance i : instances) {
			try { i.dispose(); } catch(Exception e) {}
		}
	}
}
