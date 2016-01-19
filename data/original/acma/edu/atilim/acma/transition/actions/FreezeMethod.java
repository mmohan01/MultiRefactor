package edu.atilim.acma.transition.actions;

import java.util.Set;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;

public class FreezeMethod {

	public static class Checker implements ActionChecker {		
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			for (Type t : design.getTypes()) {
				for (Method m : t.getMethods()) {
					boolean flag = true;
					
					if(m.isCompilerGenerated() || m.isOverride() || m.isStatic() || m.isConstructor() || m.isClassConstructor()) continue;
					
					for (Field f : m.getAccessedFields()) {			
						if (f.getOwnerType() != t || !f.isStatic()){
							flag = false;
							break;
						}
					}
					
					for(Method mt : m.getCalledMethods()){
						if(mt.getOwnerType() != t ||  !mt.isStatic() ){
							flag = false;
							break;
						}
					}
					
					if(flag)
						set.add(new Performer(t.getName(), m.getSignature(), false));
					else
						set.add(new Performer(t.getName(), m.getSignature(), true));
				}
			}		
		}
	}//end of checker
	
	public static class Performer implements Action {	
			private String typeName;
			private String methodName;
			private boolean parameterizeFlag;
		
		public Performer(String typeName, String methodName, boolean parameterizeFlag) {
			this.typeName = typeName;
			this.methodName = methodName;
			this.parameterizeFlag = parameterizeFlag;
		}

		@Override
		public void perform(Design d) {
			Type t = d.getType(typeName);
			Method m = t.getMethod(methodName);
			
			if (m == null) {
				Log.severe("[FreezeMethod] Can not find method: %s.", methodName);
				return;
			}
			
			if(parameterizeFlag){
				m.setStatic(true);
				m.addParameter(t);
			}
				
			else
				m.setStatic(true);
		}
		
		@Override
		public String toString() {
			
			return String.format("[Freeze Method] Convert '%s' of '%s' to static ", methodName,typeName);
		}
	}//end of performer
}
