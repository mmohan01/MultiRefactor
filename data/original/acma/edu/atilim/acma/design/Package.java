package edu.atilim.acma.design;

import java.util.ArrayList;
import java.util.List;

public class Package implements Comparable<Package> {
	private String packageName;
	private Design design;
	
	Package(String packageName, Design design) {
		this.packageName = packageName;
		this.design = design;
	}
	
	static Package emptyPackage(Design d) {
		return new Package("", d);
	}

	public Design getDesign() {
		return design;
	}

	public String getName() {
		return packageName;
	}

	public List<Type> getTypes() {
		ArrayList<Type> ptypes = new ArrayList<Type>();
		for (Type t : design.getTypes()) {
			if (this.equals(t.getPackage()))
				ptypes.add(t);
		}
		return ptypes;
	}
	
	@Override
	public String toString() {
		return packageName;
	}
	
	@Override
	public int hashCode() {
		return packageName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Package)) return false;
		return ((Package)obj).packageName.equals(packageName);
	}
	
	@Override
	public int compareTo(Package o) {
		return packageName.compareTo(o.packageName);
	}
}
