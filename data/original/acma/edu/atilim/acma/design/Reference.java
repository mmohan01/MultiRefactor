package edu.atilim.acma.design;

import java.io.Serializable;

import edu.atilim.acma.util.Delegate.Func1P;

public abstract class Reference implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Node source;
	private int tag;
	private int dimension;
	
	static Reference get(Node from, Node to, int tag) {
		return new InnerReference(from, to, tag);
	}
	
	public static Reference get(Node from, String to, int tag) {
		return new OuterReference(from, to, tag);
	}
	
	public Reference(Node source, int tag) {
		this.source = source;
		this.tag = tag;
		this.dimension = 1;
	}
	
	public Node getSource() {
		return source;
	}
	
	public int getTag() {
		return tag;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getTarget(Class<T> cls) {
		Node target = getTarget();
		if (target != null && cls.isInstance(target))
			return (T)target;
		
		return null;
	}
	
	public abstract Node getTarget();
	public abstract boolean isInternal();
	
	public void release() {
		
	}
	
	public abstract String toRawString();
	
	public String toString(String inner) {
		StringBuilder sb = new StringBuilder();
		sb.append(inner);
		for (int i = 0; i < dimension; i++)
			sb.append("[]");
		return sb.toString();
	}
	
	static class InnerReference extends Reference {
		private static final long serialVersionUID = 1L;
		
		private Node target;

		public InnerReference(Node source, Node target, int tag) {
			super(source, tag);
			this.target = target;
		}

		@Override
		public Node getTarget() {
			return target;
		}
		
		@Override
		public boolean isInternal() {
			return true;
		}
		
		@Override
		public void release() {
			getTarget().removeReference(this);
			target = null;
		}
		
		@Override
		public String toRawString() {
			return target.getName();
		}
		
		@Override
		public String toString() {
			return super.toString(target.toString());
		}
	}
	
	static class OuterReference extends Reference {
		private static final long serialVersionUID = 1L;
		
		private String typeName;

		public OuterReference(Node source, String typeName, int tag) {
			super(source, tag);
			this.typeName = typeName;
		}

		@Override
		public Node getTarget() {
			return null;
		}
		
		@Override
		public boolean isInternal() {
			return false;
		}
		
		@Override
		public String toRawString() {
			return typeName;
		}
		
		@Override
		public String toString() {
			return super.toString(typeName);
		}
	}
	
	static class TargetSelector<T extends Node> implements Func1P<T, Reference> {
		private Class<T> cls;

		public TargetSelector(Class<T> cls) {
			this.cls = cls;
		}
		
		@Override
		public T run(Reference in) {
			return in.getTarget(cls);
		}
		
	}
}
