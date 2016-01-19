package edu.atilim.acma.transition.actions;

import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Method.Parameter;
import edu.atilim.acma.design.Type;

public class IntroduceFactory {
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			for (Type t : design.getTypes()) {
				if(t.isInterface() || t.isAbstract() || t.isCompilerGenerated() || t.isAnnotation()) 
					continue;
				
				for (Method m : t.getMethods()) {
					if (!m.isConstructor() || m.getAccess() == Accessibility.PRIVATE || m.isCompilerGenerated() || m.isAbstract() || m.isClassConstructor())
						continue;
					
					set.add(new Performer(t.getName(), m.getSignature()));
				}
			}
		}
		
	}
	
	public static class Performer implements Action {
		private String typeName;
		private String ctorName;
		
		public Performer(String typeName, String ctorName) {
			this.typeName = typeName;
			this.ctorName = ctorName;
		}

		@Override
		public void perform(Design d) {
			Type t = d.getType(typeName);
			if (t == null) return;
			Method m = t.getMethod(ctorName);
			if (m == null) return;
			
			Method factory = t.createMethod("create" + t.getName());
			factory.setStatic(true);
			factory.setAccess(m.getAccess());
			factory.addInstantiatedType(t);
			factory.setReturnType(t);
			
			for (Parameter p : m.getParameters())
				factory.addParameter(p.getType(), p.getDimension());
			
			for (Method cm : m.getCallerMethods()) {
				cm.removeCalledMethod(m);
				cm.addCalledMethod(factory);
			}
			
			factory.addCalledMethod(m);
			m.setAccess(Accessibility.PRIVATE);
		}
		
		@Override
		public String toString() {
			return String.format("[Introduce Factory] for %s.%s", typeName, ctorName);
		}
	}
}
