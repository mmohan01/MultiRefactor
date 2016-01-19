/*
 * Created on 27.11.2005
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.bytecode;

import java.util.List;

import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeParameter;
import recoder.service.DefaultImplicitElementInfo;
import recoder.service.NameInfo;

/**
 * 
 * @author Tobias Gutzmann
 *
 */
public class TypeArgumentInfo implements TypeArgument {
	final WildcardMode wildcardMode;
	private final String typeName;
	final List<TypeArgumentInfo> typeArgs;
	private final boolean isTypeVariable;
	private ClassType typeVariable;
	ClassTypeContainer parent;
	/**
	 * 
	 */
	public TypeArgumentInfo(WildcardMode wildcardMode, String typeName, List<TypeArgumentInfo> typeArgs, ClassTypeContainer parent, boolean isTypeVariable) {
		super();
		if ((typeName == null && wildcardMode != WildcardMode.Any) || wildcardMode == null || parent == null
				|| typeName != null && wildcardMode == WildcardMode.Any) 
			throw new IllegalArgumentException();
		this.wildcardMode = wildcardMode;
		if (typeName != null)
			this.typeName = typeName.intern();
		else
			this.typeName = "";
		this.typeArgs = typeArgs;
		this.isTypeVariable = isTypeVariable;
		this.parent = parent;
	}
	public WildcardMode getWildcardMode() {
		return wildcardMode;
	}
	public String getTypeName() {
		return typeName;
	}
	public List<TypeArgumentInfo> getTypeArguments() {
		return typeArgs;
	}
	
	public ClassType getTypeParameter() {
		if (!isTypeVariable)
			return null;
		if (typeVariable == null) {
			ClassTypeContainer ctc = getContainer();
			String taName = typeName;
			int dim = 0;
			while (taName.endsWith("[]")) {
				taName = taName.substring(0, taName.length()-2);
				dim++;
			}
			while (true) {
				if (ctc instanceof MethodInfo) {
					MethodInfo mi = (MethodInfo)ctc;
					if (mi.getTypeParameters() != null) {
						for (int i = 0; i < mi.getTypeParameters().size(); i++) {
							if (mi.getTypeParameters().get(i).getName().equals(taName)) {
								ClassType res = mi.getTypeParameters().get(i);
								while (dim-- > 0)
									res = res.createArrayType();
								typeVariable = res;
								return res;
							}
						}
					}
				}
				else if (ctc instanceof ClassFile) {
					ClassFile cf = (ClassFile)ctc;
					if (cf.getTypeParameters() != null) {
						for (int i= 0; i < cf.getTypeParameters().size(); i++) {
							if (cf.getTypeParameters().get(i).getName().equals(taName)) {
								ClassType res = cf.getTypeParameters().get(i);
								while (dim-- > 0)
									res = res.createArrayType();
								typeVariable = res;
								return res;
							}
						}
					}
				} else {
					// ???
					throw new RuntimeException();
				}
				ctc = ctc.getContainer();
			}
		}
		return typeVariable;
	}
	
	public boolean isTypeVariable() {
		return isTypeVariable;
	}
	
	public ClassTypeContainer getContainer() {
		return parent;
	}
	
	public ClassFile getContainingClassFile() {
		if (parent instanceof ClassFile)
			return (ClassFile)parent;
		else return (ClassFile)((MethodInfo)parent).getContainingClassType();
	}
	public MethodInfo getContainingMethodInfo() {
		if (parent instanceof MethodInfo)
			return (MethodInfo)parent;
		return null;
	}
	
	public boolean semanticalEquality(TypeArgument ta) {
		// TODO clean up the call below... 
		return TypeArgument.EqualsImpl.equals(this, ta, (DefaultImplicitElementInfo)parent.getProgramModelInfo().getServiceConfiguration().getImplicitElementInfo());
	}

	public int semanticalHashCode() {
		return TypeArgument.EqualsImpl.semanticalHashCode(this);
	}
		
	public TypeParameter getTargetedTypeParameter() {
		if (parent instanceof MethodInfo) {
			MethodInfo mi = (MethodInfo)parent;
			int idx;
			if (mi.getTypeParameters() != null) {
				int i = -1;
				for (TypeParameterInfo tp: mi.getTypeParameters()) {
					i++;
					if ((idx = tp.getBoundTypeArguments(i).indexOf(this)) != -1) {
						ClassType targetedType = getNameInfo().getClassType(tp.getBoundName(i));
						while (targetedType instanceof ArrayType) 
							targetedType = (ClassType)((ArrayType)targetedType).getBaseType();
						return targetedType.getTypeParameters().get(idx);
					}
					TypeParameter inTAs = searchIn(tp.getBoundTypeArguments(i));
					if (inTAs != null)
						return inTAs;
				}
			}
			if (mi.getTypeArgumentsForReturnType() != null) {
				if ((idx = mi.getTypeArgumentsForReturnType().indexOf(this)) != -1) {
					ClassType targetedType = parent.getProgramModelInfo().getServiceConfiguration().getNameInfo().getClassType(mi.getTypeName());
					while (targetedType instanceof ArrayType)
						targetedType = (ClassType)((ArrayType)targetedType).getBaseType();
					return targetedType.getTypeParameters().get(idx);
				}
				TypeParameter inTAs = searchIn(mi.getTypeArgumentsForReturnType());
				if (inTAs != null)
					return inTAs;
			}
			for (int i = 0; i < mi.getParameterTypeNames().length; i++) {
				if ((idx = mi.getTypeArgumentsForParam(i).indexOf(this)) != -1) {
					ClassType targetedType = parent.getProgramModelInfo().getServiceConfiguration().getNameInfo().getClassType(mi.getParameterTypeNames()[i]);
					while (targetedType instanceof ArrayType)
						targetedType = (ClassType)((ArrayType)targetedType).getBaseType();
					return targetedType.getTypeParameters().get(idx);
				}
				TypeParameter inTAs = searchIn(mi.getTypeArgumentsForParam(i));
				if (inTAs != null)
					return inTAs;
			}
		} else if (parent instanceof ClassFile) {
			ClassFile cf = (ClassFile)parent;
			// check in "extends", then "implements"
			int idx;
			if (cf.getTypeParameters() != null) {
				int i = -1;
				for (TypeParameterInfo tp: cf.getTypeParameters()) {
					i++;
					if ((idx = tp.getBoundTypeArguments(i).indexOf(this)) != -1) {
						ClassType targetedType = getNameInfo().getClassType(tp.getBoundName(i));
						while (targetedType instanceof ArrayType) 
							targetedType = (ClassType)((ArrayType)targetedType).getBaseType();
						return targetedType.getTypeParameters().get(idx);
					}
					TypeParameter inTAs = searchIn(tp.getBoundTypeArguments(i));
					if (inTAs != null)
						return inTAs;
				}
			}
			if (cf.getSuperClassTypeArguments() != null) {
				if ((idx = cf.getSuperClassTypeArguments().indexOf(this)) != -1) {
					ClassType targetedType = parent.getProgramModelInfo().getServiceConfiguration().getNameInfo().getClassType(cf.getSuperClassName());
					while (targetedType instanceof ArrayType)
						targetedType = (ClassType)((ArrayType)targetedType).getBaseType();
					return targetedType.getTypeParameters().get(idx);
				}
				TypeParameter inTAs = searchIn(cf.getSuperClassTypeArguments());
				if (inTAs != null)
					return inTAs;
			}
			for (int i = 0; i < cf.getInterfaceNames().length; i++) {
				if ((idx = cf.getSuperInterfaceTypeArguments(i).indexOf(this)) != -1) {
					ClassType targetedType = parent.getProgramModelInfo().getServiceConfiguration().getNameInfo().getClassType(cf.getInterfaceNames()[i]);
					while (targetedType instanceof ArrayType)
						targetedType = (ClassType)((ArrayType)targetedType).getBaseType();
					return targetedType.getTypeParameters().get(idx);
				}
				TypeParameter inTAs = searchIn(cf.getSuperInterfaceTypeArguments(i));
				if (inTAs != null)
					return inTAs;
			}
		}
		throw new RuntimeException("TODO !?");
	}
	
	private TypeParameter searchIn(List<? extends TypeArgument> tais) {
		if (tais == null)
			return null;
		for (TypeArgument tai: tais) {
			if (tai.getTypeArguments() != null) {
				int idx;
				if ((idx = tai.getTypeArguments().indexOf(this)) != -1) {
					ClassType targetedType = getNameInfo().getClassType(tai.getTypeName());
					while (targetedType instanceof ArrayType)
						targetedType = (ClassType)((ArrayType)targetedType).getBaseType();
					return targetedType.getTypeParameters().get(idx);
				}
				TypeParameter inTAIs = searchIn(tai.getTypeArguments());
				if (inTAIs != null)
					return inTAIs;
			}
		}
		return null;
	}
	private NameInfo getNameInfo() {
		return parent.getProgramModelInfo().getServiceConfiguration().getNameInfo();
	}

    public String getFullSignature() {
    	return TypeArgument.DescriptionImpl.getFullDescription(this);
    }

}
