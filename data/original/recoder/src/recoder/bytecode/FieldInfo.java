// This file is part of the RECODER library and protected by the LGPL.

package recoder.bytecode;

import java.util.List;

import recoder.abstraction.Field;
import recoder.abstraction.Type;
import recoder.convenience.Naming;

public class FieldInfo extends MemberInfo implements Field {

    protected String type;

    protected String constantValue;
    
    protected List<TypeArgumentInfo> typeArgs;

    public FieldInfo(int accessFlags, String name, String type, boolean typeIsTypeVariable, ClassFile cf, String constantValue, List<TypeArgumentInfo> typeArgs) {
        super(accessFlags, name, cf, typeIsTypeVariable);
        this.type = type;
        this.constantValue = constantValue;
        this.typeArgs = typeArgs;
    }

    public final String getTypeName() {
        return type;
    }

    // can be null
    public final String getConstantValue() {
        return constantValue;
    }

    public Type getType() {
        return service.getType(this);
    }

    public String getFullName() {
        return Naming.getFullName(this);
    }
    
    
	public String getBinaryName() {
		return getContainingClassType().getBinaryName() + "." + getName();
	}
    
    public List<TypeArgumentInfo> getTypeArguments() {
    	return typeArgs;
    }
    
    @Override
    public String toString() {
    	return "%BC%" + getName();
    }
    
	@Override
	public FieldInfo getGenericMember() {
		return this;
	}

}