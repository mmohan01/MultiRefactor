/*
 * Created on 15.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.java.declaration;

import java.util.List;

import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeParameter;
import recoder.convenience.Naming;
import recoder.java.JavaNonTerminalProgramElement;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.Reference;
import recoder.java.SourceElement;
import recoder.java.SourceVisitor;
import recoder.java.expression.operator.New;
import recoder.java.reference.MethodReference;
import recoder.java.reference.SpecialConstructorReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.TypeReferenceContainer;
import recoder.java.reference.UncollatedReferenceQualifier;
import recoder.list.generic.ASTList;
import recoder.service.DefaultImplicitElementInfo;
import recoder.service.DefaultSourceInfo;

/**
 * This class represents a TypeArgument, as e.g. given in variable declarations.
 * 
 * @author Tobias Gutzmann
 *
 */
public class TypeArgumentDeclaration extends JavaNonTerminalProgramElement implements TypeReferenceContainer, TypeArgument {
	/**
	 * serialization id
	 */
	private static final long serialVersionUID = -8369885569636132718L;

	private TypeReference typeReference;
	
	// the wildcard mode 
	private WildcardMode wildcardMode;
	
	// either a TypeReference, a URQ, or a MethodReference 
	private Reference parent;

	/**
	 * 
	 */
	public TypeArgumentDeclaration() {
		this(null, WildcardMode.Any);
	}

	/**
	 * @param proto
	 */
	protected TypeArgumentDeclaration(TypeArgumentDeclaration proto) {
		super(proto);
		this.wildcardMode = proto.wildcardMode;
		if (proto.typeReference != null)
			this.typeReference = proto.typeReference.deepClone();
		makeParentRoleValid();
	}
	
	public TypeArgumentDeclaration(TypeReference tr) {
		this(tr, WildcardMode.None);
	}
	
	public TypeArgumentDeclaration(TypeReference tr, WildcardMode wm) {
		this.typeReference = tr;
		this.wildcardMode = wm;
		makeParentRoleValid();
	}

	/* (non-Javadoc)
	 * @see recoder.java.reference.TypeReferenceContainer#getTypeReferenceCount()
	 */
	public int getTypeReferenceCount() {
		return typeReference == null ? 0 : 1;
	}

	/* (non-Javadoc)
	 * @see recoder.java.reference.TypeReferenceContainer#getTypeReferenceAt(int)
	 */
	public TypeReference getTypeReferenceAt(int index) {
		if (index == 0 && typeReference != null)
			return typeReference;
		throw new ArrayIndexOutOfBoundsException(index);
	}

	/* (non-Javadoc)
	 * @see recoder.java.NonTerminalProgramElement#getChildCount()
	 */
	public int getChildCount() {
		return getTypeReferenceCount();
	}

	/* (non-Javadoc)
	 * @see recoder.java.NonTerminalProgramElement#getChildAt(int)
	 */
	public ProgramElement getChildAt(int index) {
		return getTypeReferenceAt(index);
	}

	/* (non-Javadoc)
	 * @see recoder.java.NonTerminalProgramElement#getChildPositionCode(recoder.java.ProgramElement)
	 */
	public int getChildPositionCode(ProgramElement child) {
		// 0: typeReference
		if (child == typeReference)
			return 0;
		return -1;
	}

	/* (non-Javadoc)
	 * @see recoder.java.NonTerminalProgramElement#replaceChild(recoder.java.ProgramElement, recoder.java.ProgramElement)
	 */
	public boolean replaceChild(ProgramElement p, ProgramElement q) {
		if (p == null)
			throw new NullPointerException();
		if (p == typeReference) {
			typeReference = (TypeReference)q;
			if (typeReference != null)
				typeReference.setParent(this);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see recoder.java.ProgramElement#getASTParent()
	 */
	public NonTerminalProgramElement getASTParent() {
		return (NonTerminalProgramElement)parent;
	}
	
	public Reference getParent() {
		return parent;
	}
	
	/**
	 * 
	 * @param tr either a TypeReference, a URQ, an MethodReference, a SpecialConstructorReference, or New 
	 * @throws IllegalArgumentException if <code>tr</code> isn't of type 
	 *  TypeReference, URQ, or MethodReference 
	 */
	public void setParent(Reference tr) {
		parent = tr;
		if (!(tr instanceof TypeReference || tr instanceof UncollatedReferenceQualifier ||
				tr instanceof MethodReference
				|| tr instanceof SpecialConstructorReference || tr instanceof New))
			throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see recoder.java.SourceElement#accept(recoder.java.SourceVisitor)
	 */
	public void accept(SourceVisitor v) {
		v.visitTypeArgument(this);
	}

	/* (non-Javadoc)
	 * @see recoder.java.SourceElement#deepClone()
	 */
	public TypeArgumentDeclaration deepClone() {
		return new TypeArgumentDeclaration(this);
	}
	
	public void makeParentRoleValid() {
		if (typeReference != null)
			typeReference.setParent(this);
	}
	
	public WildcardMode getWildcardMode() {
		return wildcardMode;
	}

	public String getTypeName() {
		if (isTypeVariable()) {
			String res = typeReference.getName(); 
			for (int i = 0; i < typeReference.getDimensions(); i++) {
				res += "[]";
			}
			return res;
		}
		if (typeReference == null) {
			assert wildcardMode == WildcardMode.Any;
			return ""; 
		}
		String trName = Naming.toPathName(typeReference);
		Type t = ((DefaultSourceInfo)getFactory().getServiceConfiguration().getSourceInfo()).getType(trName, typeReference);
		if (t == null) {
			// in case of non-compilable code...
			return "<Unknown>." + typeReference.getName();
		}
		return t.getFullName();
	}

	/**
	 * Returns type reference's type arguments, or null if wildcardMode == WildcardMode.Any
	 */
	public ASTList<TypeArgumentDeclaration> getTypeArguments() {
		if (wildcardMode == WildcardMode.Any)
			return null;
		// otherwise, type reference must be set. Leave at it is for now for error detection
		return typeReference.getTypeArguments();
	}
	
	public void setWildcardMode(WildcardMode wm) {
		this.wildcardMode = wm;
	}
	
	public TypeReference getTypeReference() {
		return typeReference;
	}
	
	public void setTypeReference(TypeReference tr) {
		this.typeReference = tr;
	}

	@Override
	public SourceElement getFirstElement() {
		if (wildcardMode != WildcardMode.None)
			return this;
		return typeReference == null ? this : typeReference.getFirstElement();
	}
	
	public boolean semanticalEquality(TypeArgument tad) {
		// TODO clean up the call below...
		return TypeArgument.EqualsImpl.equals(this, tad, (DefaultImplicitElementInfo)parent.getFactory().getServiceConfiguration().getImplicitElementInfo());
	}
	
	public int semanticalHashCode() {
		return TypeArgument.EqualsImpl.semanticalHashCode(this);
	}
	
	public TypeParameter getTargetedTypeParameter() {
		// TODO clean up! Does this belong here?
		// TypeReference, a URQ, or a MethodReference
		if (parent instanceof TypeReference) {
			int idx = ((TypeReference)parent).getTypeArguments().indexOf(this);
			DefaultSourceInfo dsi = (DefaultSourceInfo)getFactory().getServiceConfiguration().getSourceInfo();
			ClassType t = (ClassType)dsi.getType(Naming.toPathName((TypeReference)parent), parent);
			while (t instanceof ArrayType) {
				// happens in case type reference is like this: X<?>[].
				t = (ClassType)((ArrayType)t).getBaseType();
			}
//			assert !(t instanceof ParameterizedType);
			return t.getTypeParameters().get(idx);
		} 
		return null;
	}
	
	public String getFullSignature() {
		return TypeArgument.DescriptionImpl.getFullDescription(this);
	}

	public boolean isTypeVariable() {
		if (typeReference == null) {
			assert wildcardMode == WildcardMode.Any;
			return false;
		}
		if (typeReference.getReferencePrefix() != null) {
			return false;
		}
		NonTerminalProgramElement nt = (NonTerminalProgramElement)parent;
		while (nt != null) {
			if (nt instanceof MethodDeclaration) {
				List<TypeParameterDeclaration> tps = ((MethodDeclaration)nt).getTypeParameters();
				if (tps != null) {
					for (int i = 0; i < tps.size(); i++) {
						if (tps.get(i).getName().equals(typeReference.getName())) {
							return true;
						}
					}
				}
			} else if (nt instanceof TypeDeclaration) {
				List<TypeParameterDeclaration> tps = ((TypeDeclaration)nt).getTypeParameters();
				if (tps != null) {
					for (int i = 0; i < tps.size(); i++) {
						if (tps.get(i).getName().equals(typeReference.getName())) {
							return true;
						}
					}
				}
			} 
			nt = nt.getASTParent();
		}
		return false;
	}

	@Override
	public String toString() {
		return getFullSignature();
	}
}
