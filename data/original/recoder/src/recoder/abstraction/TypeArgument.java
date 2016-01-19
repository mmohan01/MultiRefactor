/*
 * Created on 27.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.abstraction;

import java.util.List;

import recoder.ModelException;
import recoder.bytecode.TypeArgumentInfo;
import recoder.convenience.Naming;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.reference.TypeReference;
import recoder.service.DefaultImplicitElementInfo;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public interface TypeArgument {
	/**
	 * The wildcard mode of a TypeArgument. 
	 *
	 */
	public static enum WildcardMode {
		None,		// Type()
		Any,		// ?
		Extends,	// ? extends Type()
		Super;		// ? super Type()
	}
	
	public static class CapturedTypeArgument implements ClassType {
		private final TypeArgument parent;
		private final ImplicitElementInfo service;
		private ArrayType array;
		public CapturedTypeArgument(TypeArgument ta, ImplicitElementInfo service) {
			this.parent = ta;
			this.service = service;
		}
		
		public TypeArgument getTypeArgument() {
			return parent;
		}

		public List<Field> getAllFields() {
			return service.getAllFields(this);
		}

		public List<Method> getAllMethods() {
			return service.getAllMethods(this);
		}

		public List<ClassType> getAllSupertypes() {
			return service.getAllSupertypes(this);
		}

		public List<ClassType> getAllTypes() {
			throw new UnsupportedOperationException();
		}

		public List<? extends Constructor> getConstructors() {
			throw new UnsupportedOperationException();
		}

		public ClassType getErasedType() {
			// TODO !!! Return first bound's erased type !!!
			return this;
		}

		public List<? extends Field> getFields() {
			return service.getFields(this);
		}

		public String getFullSignature() {
			return "capture of " + parent.getFullSignature();
		}

		public List<Method> getMethods() {
			return service.getMethods(this);
		}

		public List<ClassType> getSupertypes() {
			return service.getSupertypes(this);
		}

		public List<? extends TypeParameter> getTypeParameters() {
			throw new UnsupportedOperationException();
		}

		public boolean isAbstract() {
			throw new UnsupportedOperationException();
		}

		public boolean isAnnotationType() {
			// TODO 0.90 ?
			throw new RuntimeException();
		}

		public boolean isEnumType() {
			// TODO 0.90 ?
			throw new RuntimeException();
		}

		public boolean isInner() {
			return false;
		}

		public boolean isInterface() {
			// TODO 0.90 ?
			throw new RuntimeException();
		}

		public boolean isOrdinaryClass() {
			// TODO 0.90 ?
			throw new RuntimeException();
		}

		public boolean isOrdinaryInterface() {
			// TODO 0.90 ?
			throw new RuntimeException();
		}

		public ArrayType createArrayType() {
			if (array == null)
				array = new ArrayType(this, getProgramModelInfo().getServiceConfiguration().getImplicitElementInfo());
			return array;
		}

		public ArrayType getArrayType() {
			return array;
		}

		public String getFullName() {
			// TODO 0.92 !?
			return getFullSignature();
		}
		
		public String getBinaryName() {
			// TODO 0.92 !!
			return getFullSignature();
		}

		public ImplicitElementInfo getProgramModelInfo() {
			return service;
		}

		public void setProgramModelInfo(ProgramModelInfo pmi) {
			throw new UnsupportedOperationException();
		}

		public String getName() {
			throw new UnsupportedOperationException();
		}

		public void validate() throws ModelException {
			// implicitly defined, not to be validated
		}

		public List<? extends AnnotationUse> getAnnotations() {
			throw new UnsupportedOperationException();
		}

		public ClassType getContainingClassType() {
			throw new RuntimeException();
			// TODO 0.90
		}

		public boolean isFinal() {
			throw new UnsupportedOperationException();
		}

		public boolean isPrivate() {
			throw new UnsupportedOperationException();
		}

		public boolean isProtected() {
			throw new UnsupportedOperationException();
		}

		public boolean isPublic() {
			throw new UnsupportedOperationException();
		}

		public boolean isStatic() {
			throw new UnsupportedOperationException();
		}

		public boolean isStrictFp() {
			throw new UnsupportedOperationException();
		}

		public ClassTypeContainer getContainer() {
			// TODO 0.90 ???
			return null;
		}

		public Package getPackage() {
			return null;
		}

		public List<? extends ClassType> getTypes() {
			throw new UnsupportedOperationException();
		}
		
		public ClassType getBaseClassType() {
			return ((DefaultImplicitElementInfo)service).getBaseType(parent);
		}

		@Override
		public Member getGenericMember() {
			throw new UnsupportedOperationException();
		}

		
	}
	
	
	public WildcardMode getWildcardMode();
	public String getTypeName();
	public List<? extends TypeArgument> getTypeArguments();
	
	public boolean isTypeVariable();
	
	/**
	 * 
	 * @return the targeted TypeParameter of the type, if the parent
	 * of the type argument is a TypeReference. <code>null</code> otherwise. 
	 */
	public TypeParameter getTargetedTypeParameter();
	
	public boolean semanticalEquality(TypeArgument ta);
	public int semanticalHashCode();
	
	// TODO 0.90 remove or ...
	public String getFullSignature();
	/**
	 * This class is not intended for public use, subject to change!
	 * @author Tobias Gutzmann
	 *
	 */
	public static final class DescriptionImpl {
		public static String getFullDescription(TypeArgument ta) {
			String res;
			switch (ta.getWildcardMode()) {
			case None: res = ""; break;
			case Any: return "?"; // shortcut
			case Extends: res = "? extends "; break;
			case Super: res = "? super "; break;
			default: throw new Error();
			}
			res += ta.getTypeName();
			List<? extends TypeArgument> targs = ta.getTypeArguments();
			if (targs == null || targs.size() == 0)
				return res;
			res +="<";
			String delim = "";
			for (TypeArgument ta_sub : ta.getTypeArguments()) {
				res += delim;
				res += ta_sub.getFullSignature();
				delim = ",";
			}
			res +=">";
			return res;
		}
		public static String getFullDescription(TypeArgumentDeclaration ta, boolean alreadyFullyQualified) {
			assert (alreadyFullyQualified);
			String res;
			switch (ta.getWildcardMode()) {
			case None: res = ""; break;
			case Any: return "?"; // shortcut
			case Extends: res = "? extends "; break;
			case Super: res = "? super "; break;
			default: throw new Error();
			}
			res += Naming.toPathName(ta.getTypeReference());
			List<? extends TypeArgument> targs = ta.getTypeArguments();
			if (targs == null || targs.size() == 0)
				return res;
			res +="<";
			String delim = "";
			for (TypeArgumentDeclaration ta_sub : ta.getTypeArguments()) {
				res += delim;
				res += getFullDescription(ta_sub, true);
				delim = ",";
			}
			res +=">";
			return res;
		}
		
	}
	
	public static final class EqualsImpl {
		private static Type getTypeParameterFromTAInfo(TypeArgument ta, DefaultImplicitElementInfo si) {
			if (ta instanceof TypeArgumentInfo) {
				TypeArgumentInfo tai1 = (TypeArgumentInfo)ta;
				return tai1.getTypeParameter();
			} else {
				TypeReference typeReference = ((TypeArgumentDeclaration)ta).getTypeReference();
				return si.getServiceConfiguration().getSourceInfo().getType(typeReference);
			}
		}
		
		public static int semanticalHashCode(TypeArgument ta) {
			int res = 0;
			switch (ta.getWildcardMode()) {
			case None: res = 12315; break;
			case Super: res = 135091; break;
			case Extends: res = 1571251; break;
			case Any: return 9123561;
			}
			List<? extends TypeArgument> tas = ta.getTypeArguments();
			if (tas != null) {
				res += tas.size() << 16;
				for (int i = 0; i < tas.size(); i++) {
					res += semanticalHashCode(tas.get(i));
				}
			}
			res += ta.getTypeName().hashCode();
			return res;
		}
		
		public static boolean equals(TypeArgument ta1, TypeArgument ta2, DefaultImplicitElementInfo si) {
			if (ta1==ta2)
				return true;
			if (ta1.getWildcardMode() != ta2.getWildcardMode())
				return false;

			// one or two type parameters ?
			Type tp1 = null;
			Type tp2 = null;
			
			if (ta1.isTypeVariable() && ta2.isTypeVariable()) {
				if (ta1.getTypeName().equals(ta2.getTypeName())) {
					tp1 = getTypeParameterFromTAInfo(ta1, si);
					tp2 = getTypeParameterFromTAInfo(ta2, si);
					return tp1 == tp2;
				}
				return false;
			} else if (ta1.isTypeVariable() || ta2.isTypeVariable())
				return false;
			
			String n1 = ta1.getTypeName();
			String n2 = ta2.getTypeName();
			
			if (n1 == null && n2 == null) {
				assert ta1.getWildcardMode() == WildcardMode.Any;
				assert ta2.getWildcardMode() == WildcardMode.Any;
				return true; // WildcardMode "Any" in both
			}
			
			if (n1 == null || n2 == null) {
				return false;
			}
			if (!n1.equals(n2)) {
				return false;
			}
			
			List<? extends TypeArgument> ta1args = ta1.getTypeArguments();
			List<? extends TypeArgument> ta2args = ta2.getTypeArguments();
			int s1 = ta1args == null ? 0 : ta1args.size();
			int s2 = ta2args == null ? 0 : ta2args.size();
			if (s1 != s2) {
				assert s1==0 || s2==0; // one of them must be a raw type, error otherwise
				return false;
			}
			if (s1 == 0) {
				return true; // no type args in either one
			}

			for (int i = 0; i < s1; i++) {
				if (!equals(ta1args.get(i), ta2args.get(i), si)) {
					return false;
				}
			}
			return true;
		}
	}
}
