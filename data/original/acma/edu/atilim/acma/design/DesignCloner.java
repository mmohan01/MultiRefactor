package edu.atilim.acma.design;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public final class DesignCloner {
	public static Design clone(Design d) {
		Map<Node, Node> cloneMap = new HashMap<Node, Node>();
		
		Design nd = new Design(new ArrayList<String>(d.getModifications()));
		
		if (d.getTag() != null)
			nd.setTag((Serializable)d.getTag());
		
		//Explore
		for (Type t : d.getTypes()) {
			Type cloneType = nd.create(t.getName(), Type.class);
			cloneType.setFlags(t.getFlags());
			
			cloneMap.put(t, cloneType);
			
			for (Field f : t.getFields()) {
				Field cloneField = cloneType.createField(f.getName());
				cloneField.setFlags(f.getFlags());
				
				cloneMap.put(f, cloneField);
			}
			
			for (Method m : t.getMethods()) {
				Method cloneMethod = cloneType.createMethod(m.getName());
				cloneMethod.setFlags(m.getFlags());
				
				cloneMap.put(m, cloneMethod);
			}
		}
		
		//Reconstruct
		for (Entry<Node, Node> e : cloneMap.entrySet()) {
			Node org = e.getKey();
			Node cln = e.getValue();
			
			if (org instanceof Type) {
				Type orgt = (Type)org;
				Type clnt = (Type)cln;
				
				clnt.setParentType((Type)cloneMap.get(orgt.getParentType()));
				clnt.setSuperType((Type)cloneMap.get(orgt.getSuperType()));
				
				for (Type i : orgt.getInterfaces()) {
					clnt.addInterface((Type)cloneMap.get(i));
				}
			}
			
			if (org instanceof Field) {
				Field orgf = (Field)org;
				Field clnf = (Field)cln;
				
				clnf.setType((Type)cloneMap.get(orgf.getType()));
			}
			
			if (org instanceof Method) {
				Method orgm = (Method)org;
				Method clnm = (Method)cln;
				
				clnm.setReturnType((Type)cloneMap.get(orgm.getReturnType()));
				
				for (Method m : orgm.getCalledMethods()) {
					clnm.addCalledMethod((Method)cloneMap.get(m));
				}
				
				for (Field f : orgm.getAccessedFields()) {
					clnm.addAccessedField((Field)cloneMap.get(f));
				}
				
				for (Type t : orgm.getInstantiatedTypes()) {
					clnm.addInstantiatedType((Type)cloneMap.get(t));
				}
				
				for (Reference p : orgm.getRawParameters()) {
					Type target = (Type)p.getTarget();
					
					if (target == null)
						clnm.addParameter(p.toRawString(), p.getDimension());
					else
						clnm.addParameter((Type)cloneMap.get(target), p.getDimension());
				}
			}
		}
		
		return nd;
	}
}
