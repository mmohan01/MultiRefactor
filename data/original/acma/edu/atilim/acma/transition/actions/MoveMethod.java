package edu.atilim.acma.transition.actions;

import java.util.List;
import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Method.Parameter;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;

public class MoveMethod {
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			for (Type type : design.getTypes()) {
			
				if(type.isInterface() || type.isAbstract() || type.isCompilerGenerated() || type.isAnnotation()) 
					continue;
			
				//method:
				for(Method m : type.getMethods() ){
					List<Parameter> parameterList = m.getParameters();
						
					if(parameterList == null || m.getAccess() == Accessibility.PRIVATE || m.getAccess() == Accessibility.PUBLIC ||  m.isCompilerGenerated() || m.isConstructor() ||  m.isClassConstructor()) 
						continue;
				
					for(Parameter p : parameterList){
						if(p.getDimension() != 0 || !m.canBeMovedTo(p.getType())) 
							continue;
						else{
							set.add(new Performer(type.getName(), m.getSignature(), p.getType().getName()));
							//break method;
						}
					}
				}
			}
		}
	}

	public static class Performer implements Action {
		private String typeName;
		private String methodName;
		private String newOwnerTypeName;

		public Performer(String typeName, String methodName, String newOwnerTypeName) {
			this.typeName = typeName;
			this.methodName = methodName;
			this.newOwnerTypeName = newOwnerTypeName;
		}

		@Override
		public void perform(Design d) {
			Method m = d.getType(typeName).getMethod(methodName);
			Type t = d.getType(newOwnerTypeName);
	
			if (m == null) {
				Log.severe("[MoveMethod] Can not find method: %s.", methodName);
				return;
			}
		
			m.setOwnerType(t);	
			m.addParameter(d.getType(typeName));
			
			for (Parameter p : m.getParameters()) {
				if (p.getType() == t && p.getDimension() == 0) {
					m.removeParameter(p);
					break;
				}
			}
		}

		@Override
		public String toString() {
			return String.format("[Move Method] '%s' of '%s' to its parameter type '%s'", methodName,typeName,newOwnerTypeName);
		}
	}
}

