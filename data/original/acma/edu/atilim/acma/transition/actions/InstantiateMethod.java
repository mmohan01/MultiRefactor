package edu.atilim.acma.transition.actions;

import java.util.List;
import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.design.Method.Parameter;
import edu.atilim.acma.util.Log;

public class InstantiateMethod {
	public static class Checker implements ActionChecker {
		List<Parameter> parameterList;
	
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			for (Type type : design.getTypes()) {
			
				if(type.isInterface() || type.isAbstract() || type.isCompilerGenerated() || type.isAnnotation()) 
					continue;
			
				for(Method m : type.getMethods() ){
					parameterList = m.getParameters();
						
					if(parameterList == null || m.getAccess() == Accessibility.PROTECTED || m.getAccess() == Accessibility.PUBLIC ||  m.isCompilerGenerated() || m.isConstructor() ||  m.isClassConstructor()) 
							continue;
				
					if(m.isStatic()){
						for(Parameter p : parameterList){
							if(!m.canBeMovedTo(p.getType())) 
								continue;
							else
								set.add(new Performer(type.getName(), m.getSignature(), p.getType().getName()));
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
				Log.severe("[InstantiateMethod] Can not find method: %s.", methodName);
				return;
			}
		
			m.setOwnerType(t);
			
			for(Parameter p : m.getParameters()){
				if(p.getType() == t){
					m.removeParameter(p);
					break;
				}
			}
		}

		@Override
		public String toString() {
			return String.format("[Instantiate Method] Move static method '%s' of '%s' to its parameter type '%s'", methodName,typeName,newOwnerTypeName);
		}
	}
}
