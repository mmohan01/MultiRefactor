package edu.atilim.acma.transition.actions;

import java.util.List;
import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;

public class MoveDownMethod {
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			for (Type type : design.getTypes()) {
				List<Type> childTypeList = type.getExtenders();
				
				if(childTypeList == null || type.isInterface() || type.isAbstract() || type.isCompilerGenerated() || type.isAnnotation()) 
					continue;
				
				for(Method m : type.getMethods() ){
					if(m.getAccess() == Accessibility.PRIVATE || m.getAccess() == Accessibility.PUBLIC ||  m.isCompilerGenerated() || m.isStatic() || m.isConstructor() ||  m.isClassConstructor()) 
						continue;
					
					for(Type t : childTypeList){
						if(!m.canBeMovedTo(t)) 
							continue;
						else
							set.add(new Performer(type.getName(), m.getSignature(), t.getName()));
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
				Log.severe("[MoveDownMethod] Can not find method: %s.", methodName);
				return;
			}
			
			m.setOwnerType(t);	
		}
	
		@Override
		public String toString() {
			return String.format("[Move Down Method] '%s' of '%s' to its child class '%s'", methodName,typeName,newOwnerTypeName);
		}
	}
}

