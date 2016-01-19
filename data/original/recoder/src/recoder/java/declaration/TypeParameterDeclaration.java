/*
 * Created on 23.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.java.declaration;

import recoder.ModelException;
import recoder.abstraction.ClassType;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Type;
import recoder.abstraction.TypeParameter;
import recoder.convenience.Naming;
import recoder.java.Identifier;
import recoder.java.ProgramElement;
import recoder.java.SourceElement;
import recoder.java.SourceVisitor;
import recoder.java.reference.TypeReference;
import recoder.java.reference.TypeReferenceContainer;
import recoder.list.generic.ASTList;
import recoder.service.SourceInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public class TypeParameterDeclaration extends TypeDeclaration implements TypeReferenceContainer, TypeParameter {

	/**
	 * serialization id 
	 */
	private static final long serialVersionUID = -6480521507901415027L;
	
	private ASTList<TypeReference> bound;
	/**
	 * 
	 */
	public TypeParameterDeclaration() {
		super();
	}
	
	public TypeParameterDeclaration(Identifier name, ASTList<TypeReference> bound) {
		super(name);
		this.bound = bound;
		makeParentRoleValid();
	}

	/**
	 * @param proto
	 */
	protected TypeParameterDeclaration(TypeParameterDeclaration proto) {
		super(proto);
		if (proto.bound != null)
			bound = proto.bound.deepClone();
		makeParentRoleValid();
	}

	/* (non-Javadoc)
	 * @see recoder.java.reference.TypeReferenceContainer#getTypeReferenceCount()
	 */
	public int getTypeReferenceCount() {
		return bound == null ? 0 : bound.size();
	}

	/* (non-Javadoc)
	 * @see recoder.java.reference.TypeReferenceContainer#getTypeReferenceAt(int)
	 */
	public TypeReference getTypeReferenceAt(int index) {
		if (bound != null)
			return bound.get(index);
		throw new ArrayIndexOutOfBoundsException(index);
	}

	/* (non-Javadoc)
	 * @see recoder.java.NonTerminalProgramElement#getChildCount()
	 */
	public int getChildCount() {
		return (name != null ? 1 : 0) + (bound != null ? bound.size() : 0);
	}

	/* (non-Javadoc)
	 * @see recoder.java.NonTerminalProgramElement#getChildAt(int)
	 */
	public ProgramElement getChildAt(int index) {
		if (name != null) {
			if (index == 0)
				return name;
			index--;
		}
		if (bound != null) {
			return bound.get(index); // may throw exception
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	/* (non-Javadoc)
	 * @see recoder.java.NonTerminalProgramElement#getChildPositionCode(recoder.java.ProgramElement)
	 */
	public int getChildPositionCode(ProgramElement child) {
		// 0      : name
		// 1(idx) : bound
		if (child == name) return 0;
		int idx = bound.indexOf(child);
		if (idx != -1)
			return (idx << 4) | 1;
		return -1;
	}

	/* (non-Javadoc)
	 * @see recoder.java.NonTerminalProgramElement#replaceChild(recoder.java.ProgramElement, recoder.java.ProgramElement)
	 */
	public boolean replaceChild(ProgramElement p, ProgramElement q) {
		if (p == null)
			throw new NullPointerException();
		if (p == name) {
			name = (Identifier)q;
			if (name != null)
				name.setParent(this);
			return true;
		}
		if (bound != null) {
			int idx = bound.indexOf(p);
			if (idx != -1) {
				if (q == null) {
					bound.remove(idx);
				} else {
					TypeReference tr = (TypeReference)q;
					bound.set(idx, tr);
					tr.setParent(this);
				}
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.java.SourceElement#accept(recoder.java.SourceVisitor)
	 */
	public void accept(SourceVisitor v) {
		v.visitTypeParameter(this);
	}
	
	/* (non-Javadoc)
	 * @see recoder.java.SourceElement#deepClone()
	 */
	public TypeParameterDeclaration deepClone() {
		return new TypeParameterDeclaration(this);
	}

	@Override
	public void makeParentRoleValid() {
		super.makeParentRoleValid();
		if (bound != null) {
			for (TypeReference tr : bound) 
				tr.setParent(this);
		}
	}

	@Override
	public void validate() throws ModelException {
		if (members != null && members.size() != 0)
			throw new ModelException("No members allowed in TypeParameter");
	}

	public boolean isInterface() {
		return false;
	}

	public boolean isOrdinaryInterface() {
		return false;
	}

	public boolean isAnnotationType() {
		return false;
	}

	public boolean isEnumType() {
		return false;
	}

	public boolean isOrdinaryClass() {
		return false;
	}

	@Override
	public ASTList<TypeParameterDeclaration> getTypeParameters() {
		return null;
	}
	
	public ASTList<TypeReference> getBounds() {
		return bound;
	}

	public String getParameterName() {
		return getName();
	}

	public int getBoundCount() {
		return bound == null || bound.isEmpty() ? 1 : bound.size(); // no bound: java.lang.Object!
	}

	public String getBoundName(int boundidx) {
		if ((bound == null || bound.isEmpty()) && boundidx == 0) {
			return "java.lang.Object";
		}
		// drop type arguments. This avoids recursion! 
		Type t = ((SourceInfo)getProgramModelInfo()).getType(
				Naming.toPathName(bound.get(boundidx)), bound.get(boundidx));
		if (t instanceof TypeParameter)
			return t.getName(); // this is what happens when run with bytecode; be consistent.
		if (t == null) {
			// can happen if bound name is unknown type. For simplicity, return the name of the bound.
			// Quite sure that there are already warnings somewhere...
			return Naming.toPathName(bound.get(boundidx));
		}
		return t.getFullName();
	}

	public ASTList<TypeArgumentDeclaration> getBoundTypeArguments(int boundidx) {
		if ((bound == null || bound.isEmpty()) && boundidx == 0)
			return null;
		return bound.get(boundidx).getTypeArguments();
	}
	
	public void setBound(ASTList<TypeReference> bound) {
		this.bound = bound;
	}

	@Override
	public SourceElement getFirstElement() {
		return name;
	}
	
	@Override
	public SourceElement getLastElement() {
		if (bound != null)
			return bound.get(bound.size()-1);
		return name;
	}

	@Override
	public ClassType getTypeInScope(String tname) {
		return null;
	}

	@Override
	public void addTypeToScope(ClassType type, String tname) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addVariableToScope(VariableSpecification var) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeTypeFromScope(String tname) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeVariableFromScope(String tname) {
		throw new UnsupportedOperationException();
	}
	
	public String getFullSignature() {
		return TypeParameter.DescrImp.getFullSignature(this);
	}

	public boolean isInner() {
		return false;
	}

	@Override
	public ErasedType getErasedType() {
		// TODO return getFirstBound().erasedType() !!!!
		return super.getErasedType();
	}
	
	public ClassType getBaseClassType() {
		if (bound == null || bound.size() == 0)
			return getProgramModelInfo().getServiceConfiguration().getNameInfo().getJavaLangObject();
		return ((ClassType)((SourceInfo)getProgramModelInfo()).getType(bound.get(0))).getBaseClassType();
	}



}
