package edu.atilim.acma.transition.actions;

import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;

public class DecreaseMethodSecurity {
	public static class Checker implements ActionChecker {

		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			
			for (Type t : design.getTypes()) {
				for (Method m : t.getMethods()) {
					
					if (m.isCompilerGenerated() || m.isOverride() || m.isFinal() ||  m.getAccess() == Accessibility.PUBLIC || m.isConstructor() || m.isClassConstructor()) 
						continue;
					
					Accessibility newaccess = Accessibility.PUBLIC;
					
					if (m.getAccess() == Accessibility.PRIVATE)
						newaccess = Accessibility.PACKAGE;
					if (m.getAccess() == Accessibility.PACKAGE)
						newaccess = Accessibility.PROTECTED;
					if (m.getAccess() == Accessibility.PROTECTED)
						newaccess = Accessibility.PUBLIC;
					
					set.add(new Performer(t.getName(), m.getSignature(), newaccess));
				}
			}
		}
	}
	
	public static class Performer implements Action {
		private String typeName;
		private String methodName;
		private Accessibility newAccess;

		public Performer(String typeName, String methodName, Accessibility newAccess) {
			this.typeName = typeName;
			this.methodName = methodName;
			this.newAccess = newAccess;
		}

		@Override
		public void perform(Design d) {
			Method m = d.getType(typeName).getMethod(methodName);
			
			if (m == null) {
				Log.severe("[DecreaseMethodSecurity] Can not find type: %s.", methodName);
				return;
			}
			
			m.setAccess(newAccess);
		}
		
		@Override
		public String toString() {
			return String.format("[Decrease Method Security] '%s' of '%s' to '%s'", methodName, typeName, newAccess);
		}
	}
}

